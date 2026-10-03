package model

import "time"

// IngestionApiKey represents an authorized server-to-server credential.
type IngestionApiKey struct {
	ID           string    `json:"id"`
	Name         string    `json:"name"`
	Key          string    `json:"key"` // e.g. tk_sec_ndp_live_xxx
	Source       string    `json:"source"` // NDP_BILLING, TELCO_PROXY, WAF_GATEWAY, EXTERNAL_SOC
	IsActive     bool      `json:"is_active"`
	CreatedAt    time.Time `json:"created_at"`
	LastUsedAt   time.Time `json:"last_used_at"`
	RequestCount int       `json:"request_count"`
}

// NdpIngestPayload is incoming billing/package data from Telkomsel NDP server.
type NdpIngestPayload struct {
	MSISDN       string `json:"msisdn"`
	PackageName  string `json:"package_name"`
	DurationDays int    `json:"duration_days"`
	Price        int    `json:"price"`
	Channel      string `json:"channel"`
	Action       string `json:"action"` // PURCHASE, RENEWAL, EXPIRE, SUSPEND
	OrderID      string `json:"order_id"`
}

// ProxyThreatItem represents a malicious URL blocked by a telco proxy/DNS gateway.
type ProxyThreatItem struct {
	URL         string    `json:"url"`
	Domain      string    `json:"domain"`
	ThreatType  string    `json:"threat_type"` // PHISHING, MALWARE_URL, C2_SERVER
	Verdict     string    `json:"verdict"`
	SourceProxy string    `json:"source_proxy"`
	ClientIP    string    `json:"client_ip"`
	MSISDN      string    `json:"msisdn"`
	Timestamp   time.Time `json:"timestamp"`
}

// ProxyThreatIngestPayload is incoming batch telemetry from Telkom proxy/WAF.
type ProxyThreatIngestPayload struct {
	GatewayID string            `json:"gateway_id"`
	BatchID   string            `json:"batch_id"`
	Threats   []ProxyThreatItem `json:"threats"`
}

// DeadLetterRecord stores malformed, failed, or unparseable ingestion events for mitigation.
type DeadLetterRecord struct {
	ID           string    `json:"id"`
	Source       string    `json:"source"` // NDP_BILLING, TELCO_PROXY, UNKNOWN
	PayloadRaw   string    `json:"payload_raw"`
	ErrorMessage string    `json:"error_message"`
	Severity     string    `json:"severity"` // CRITICAL, HIGH, MEDIUM
	Status       string    `json:"status"` // PENDING, REPLAYED, DISCARDED
	Timestamp    time.Time `json:"timestamp"`
	ResolvedAt   time.Time `json:"resolved_at,omitempty"`
	Notes        string    `json:"notes,omitempty"`
}
