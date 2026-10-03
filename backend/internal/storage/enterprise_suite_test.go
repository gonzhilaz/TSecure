package storage

import (
	"testing"
	"time"

	"telkomsecure-backend/internal/model"
)

func TestEnterpriseSuite(t *testing.T) {
	s := NewStorage("")

	// 1. Operator Auth & RBAC Test
	op, err := s.AuthenticateOperator("admin@telkomsel.co.id", "admin123")
	if err != nil {
		t.Fatalf("failed to authenticate admin: %v", err)
	}
	if op.Role != "SUPERADMIN" {
		t.Errorf("expected role SUPERADMIN, got %s", op.Role)
	}

	_, err = s.AuthenticateOperator("admin@telkomsel.co.id", "wrongpassword")
	if err == nil {
		t.Fatalf("expected error on wrong password")
	}

	// 2. Audit Trail Test
	s.RecordAuditLog(model.AuditLog{
		OperatorEmail:  op.Email,
		OperatorName:   op.Name,
		Action:         "TEST_ACTION",
		TargetResource: "TEST_RES",
		Details:        "Automated integration test",
		IPAddress:      "127.0.0.1",
	})
	logs := s.ListAuditLogs(10)
	if len(logs) == 0 {
		t.Fatalf("expected audit logs to contain recorded action")
	}

	// 3. API Key & Ingestion Test
	key, err := s.CreateApiKey("Test External Gateway", "TELCO_PROXY")
	if err != nil {
		t.Fatalf("failed to create api key: %v", err)
	}
	verifiedKey, ok := s.VerifyApiKey(key.Key)
	if !ok {
		t.Fatalf("failed to verify created api key")
	}
	if verifiedKey.RequestCount != 1 {
		t.Errorf("expected request count 1, got %d", verifiedKey.RequestCount)
	}

	// 4. Ingest NDP Billing Test
	sub, err := s.ProcessNdpIngest(model.NdpIngestPayload{
		MSISDN:       "081288776655",
		PackageName:  "Telkomsel Secure Guard 30 Hari",
		DurationDays: 30,
		Price:        15000,
		Channel:      "NDP_API",
		Action:       "PURCHASE",
	})
	if err != nil {
		t.Fatalf("failed to ingest NDP purchase: %v", err)
	}
	if !sub.IsActive {
		t.Errorf("expected subscriber to be active after NDP purchase")
	}

	// 5. Ingest Proxy Threats Test
	proxyThreatCount, err := s.ProcessProxyThreatIngest(model.ProxyThreatIngestPayload{
		GatewayID: "PROXY-TEST-01",
		Threats: []model.ProxyThreatItem{
			{
				URL:        "https://bca-palsu-promo-hadiah.com/login",
				ThreatType: "PHISHING",
				Verdict:    "Known Phishing URL",
				MSISDN:     "081288776655",
				Timestamp:  time.Now(),
			},
		},
	})
	if err != nil || proxyThreatCount != 1 {
		t.Fatalf("expected proxy threats ingest count 1, got %d (err: %v)", proxyThreatCount, err)
	}

	// 6. Dead-Letter Queue (DLQ) Mitigation Test
	dlq := s.RecordDeadLetter("NDP_BILLING", `{"bad":"json"}`, "Schema Validation Error", "HIGH")
	if dlq.Status != "PENDING" {
		t.Errorf("expected status PENDING, got %s", dlq.Status)
	}

	resolved, err := s.ResolveDLQRecord(dlq.ID, "REPLAYED", "Fixed schema error")
	if err != nil || resolved.Status != "REPLAYED" {
		t.Fatalf("failed to resolve DLQ record: %v", err)
	}

	// 7. Database Snapshot & Maintenance Test
	stats := s.GetMaintenanceStats()
	if stats.HealthStatus != "HEALTHY" {
		t.Errorf("expected health status HEALTHY, got %s", stats.HealthStatus)
	}

	snap, err := s.ExportSnapshot(op.Email)
	if err != nil {
		t.Fatalf("failed to export snapshot: %v", err)
	}
	if len(snap.Subscribers) == 0 {
		t.Errorf("expected snapshot to include subscribers")
	}

	retentionRes := s.RunRetentionPolicy(90, 180, 30, op.Email)
	if retentionRes.ExecutedAt.IsZero() {
		t.Errorf("expected retention execution timestamp")
	}
}
