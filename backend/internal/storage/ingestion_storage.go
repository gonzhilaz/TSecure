package storage

import (
	"crypto/rand"
	"encoding/hex"
	"fmt"
	"strings"
	"time"

	"telkomsecure-backend/internal/model"
)

func (s *Storage) seedInitialApiKeys() {
	now := time.Now()
	seeds := []*model.IngestionApiKey{
		{
			ID:           "KEY-NDP-01",
			Name:         "Telkomsel NDP Billing Core",
			Key:          "tk_sec_ndp_live_9921",
			Source:       "NDP_BILLING",
			IsActive:     true,
			CreatedAt:    now.Add(-60 * 24 * time.Hour),
			LastUsedAt:   now.Add(-10 * time.Minute),
			RequestCount: 1420,
		},
		{
			ID:           "KEY-PROXY-01",
			Name:         "Telkom Secure Web Gateway Proxy",
			Key:          "tk_sec_proxy_jkt_4410",
			Source:       "TELCO_PROXY",
			IsActive:     true,
			CreatedAt:    now.Add(-30 * 24 * time.Hour),
			LastUsedAt:   now.Add(-5 * time.Minute),
			RequestCount: 890,
		},
	}

	for _, k := range seeds {
		s.apiKeys[k.Key] = k
	}

	// Seed sample Dead-Letter Queue (DLQ) item for demonstration of mitigation
	s.dlqRecords = append(s.dlqRecords, model.DeadLetterRecord{
		ID:           "DLQ-2026-001",
		Source:       "NDP_BILLING",
		PayloadRaw:   `{"msisdn":"081299999999","action":"PURCHASE","duration_days":-5,"package_name":""}`,
		ErrorMessage: "Invalid duration_days (-5) and empty package_name from legacy billing adapter",
		Severity:     "HIGH",
		Status:       "PENDING",
		Timestamp:    now.Add(-2 * time.Hour),
		Notes:        "Awaiting schema transformation fix",
	})
}

// VerifyApiKey validates incoming server-to-server request credential.
func (s *Storage) VerifyApiKey(key string) (*model.IngestionApiKey, bool) {
	s.mu.Lock()
	defer s.mu.Unlock()

	cleaned := strings.TrimSpace(key)
	k, exists := s.apiKeys[cleaned]
	if !exists || !k.IsActive {
		return nil, false
	}

	k.RequestCount++
	k.LastUsedAt = time.Now()
	_ = s.saveToFile()

	copied := *k
	return &copied, true
}

// ListApiKeys returns all provisioned server ingestion keys.
func (s *Storage) ListApiKeys() []*model.IngestionApiKey {
	s.mu.RLock()
	defer s.mu.RUnlock()

	res := make([]*model.IngestionApiKey, 0, len(s.apiKeys))
	for _, k := range s.apiKeys {
		copied := *k
		res = append(res, &copied)
	}
	return res
}

// CreateApiKey provisions a new authorization key for an external server.
func (s *Storage) CreateApiKey(name, source string) (*model.IngestionApiKey, error) {
	s.mu.Lock()
	defer s.mu.Unlock()

	b := make([]byte, 16)
	_, _ = rand.Read(b)
	generatedKey := fmt.Sprintf("tk_sec_%s_%s", strings.ToLower(source), hex.EncodeToString(b))

	newKey := &model.IngestionApiKey{
		ID:           fmt.Sprintf("KEY-%03d", len(s.apiKeys)+1),
		Name:         strings.TrimSpace(name),
		Key:          generatedKey,
		Source:       strings.ToUpper(strings.TrimSpace(source)),
		IsActive:     true,
		CreatedAt:    time.Now(),
		LastUsedAt:   time.Time{},
		RequestCount: 0,
	}

	s.apiKeys[generatedKey] = newKey
	_ = s.saveToFile()

	copied := *newKey
	return &copied, nil
}

// ProcessNdpIngest ingests billing updates from external NDP servers.
func (s *Storage) ProcessNdpIngest(payload model.NdpIngestPayload) (*model.Subscriber, error) {
	cleaned := cleanMsisdn(payload.MSISDN)
	if cleaned == "" {
		return nil, fmt.Errorf("msisdn is required")
	}

	req := model.NdpOrderRequest{
		MSISDN:       cleaned,
		PackageName:  payload.PackageName,
		DurationDays: payload.DurationDays,
		Price:        float64(payload.Price),
		Channel:      payload.Channel,
	}

	switch strings.ToUpper(payload.Action) {
	case "EXPIRE", "SUSPEND":
		sub, ok := s.SimulateNdpExpire(cleaned)
		if !ok {
			return nil, fmt.Errorf("subscriber not found to expire")
		}
		return sub, nil
	default: // "PURCHASE", "RENEWAL"
		return s.SimulateNdpPurchase(req), nil
	}
}

// ProcessProxyThreatIngest ingests batch malicious URLs blocked by telco proxies.
func (s *Storage) ProcessProxyThreatIngest(payload model.ProxyThreatIngestPayload) (int, error) {
	count := 0
	for _, t := range payload.Threats {
		if strings.TrimSpace(t.URL) == "" {
			continue
		}
		threatEvent := model.ThreatEvent{
			ID:          fmt.Sprintf("THREAT-PROXY-%d", time.Now().UnixNano()),
			MSISDN:      t.MSISDN,
			ThreatType:  t.ThreatType,
			Target:      t.URL,
			Severity:    "CRITICAL",
			Description: fmt.Sprintf("Proxy Gateway [%s]: %s", payload.GatewayID, t.Verdict),
			ActionTaken: "BLOCKED",
			Timestamp:   t.Timestamp,
		}
		if threatEvent.Timestamp.IsZero() {
			threatEvent.Timestamp = time.Now()
		}
		if threatEvent.ThreatType == "" {
			threatEvent.ThreatType = "PHISHING"
		}
		s.AddThreatEvent(threatEvent)
		count++
	}
	return count, nil
}

// RecordDeadLetter stores malformed ingestion payloads in DLQ.
func (s *Storage) RecordDeadLetter(source, rawPayload, errMsg, severity string) model.DeadLetterRecord {
	s.mu.Lock()
	defer s.mu.Unlock()

	rec := model.DeadLetterRecord{
		ID:           fmt.Sprintf("DLQ-%d", time.Now().UnixNano()%1000000),
		Source:       source,
		PayloadRaw:   rawPayload,
		ErrorMessage: errMsg,
		Severity:     severity,
		Status:       "PENDING",
		Timestamp:    time.Now(),
	}

	s.dlqRecords = append([]model.DeadLetterRecord{rec}, s.dlqRecords...)
	if len(s.dlqRecords) > 300 {
		s.dlqRecords = s.dlqRecords[:300]
	}
	_ = s.saveToFile()
	return rec
}

// ListDLQRecords returns dead-letter queue records.
func (s *Storage) ListDLQRecords(status string) []model.DeadLetterRecord {
	s.mu.RLock()
	defer s.mu.RUnlock()

	sFilter := strings.ToUpper(strings.TrimSpace(status))
	res := make([]model.DeadLetterRecord, 0, len(s.dlqRecords))
	for _, rec := range s.dlqRecords {
		if sFilter != "" && sFilter != "ALL" && rec.Status != sFilter {
			continue
		}
		res = append(res, rec)
	}
	return res
}

// ResolveDLQRecord updates dead letter status (REPLAYED / DISCARDED).
func (s *Storage) ResolveDLQRecord(id, newStatus, notes string) (*model.DeadLetterRecord, error) {
	s.mu.Lock()
	defer s.mu.Unlock()

	for i := range s.dlqRecords {
		if s.dlqRecords[i].ID == id {
			s.dlqRecords[i].Status = strings.ToUpper(newStatus)
			s.dlqRecords[i].ResolvedAt = time.Now()
			if notes != "" {
				s.dlqRecords[i].Notes = notes
			}
			_ = s.saveToFile()
			copied := s.dlqRecords[i]
			return &copied, nil
		}
	}
	return nil, fmt.Errorf("dlq record not found")
}
