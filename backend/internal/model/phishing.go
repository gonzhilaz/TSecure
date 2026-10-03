package model

import "time"

// PhishingRecord represents an aggregated phishing/malware domain intelligence entry.
type PhishingRecord struct {
	ID              string    `json:"id"`                // e.g. PHISH-2026-001
	URL             string    `json:"url"`               // Complete target URL
	Domain          string    `json:"domain"`            // Extracted Host / FQDN (e.g. login-telkomsel-palsu.xyz)
	Category        string    `json:"category"`          // PHISHING, MALWARE_URL, FAKE_LOGIN, SMISHING
	TargetBrand     string    `json:"target_brand"`      // Telkomsel, MyTelkomsel, BCA, BRI, Mandiri, Gov/Pajak, etc.
	Severity        string    `json:"severity"`          // CRITICAL, HIGH, MEDIUM
	Status          string    `json:"status"`            // ACTIVE_THREAT, REPORTED_KOMINFO, TAKEN_DOWN, WHITELISTED
	HitCount        int       `json:"hit_count"`         // Frequency of interception across devices
	FirstDetectedAt time.Time `json:"first_detected_at"`
	LastDetectedAt  time.Time `json:"last_detected_at"`
	TargetedMSISDNs []string  `json:"targeted_msisdns"`  // Unique victims targeted
	ActionTaken     string    `json:"action_taken"`      // BLOCKED, ISOLATED
	KSNVerdict      string    `json:"ksn_verdict"`       // Verdict from Kaspersky Cloud
	Notes           string    `json:"notes"`             // Internal analyst / takedown notes
}

// PhishingStats provides executive intelligence summaries for the SOC dashboard.
type PhishingStats struct {
	TotalUniqueDomains int            `json:"total_unique_domains"`
	TotalHitsBlocked   int            `json:"total_hits_blocked"`
	ActiveThreatsCount int            `json:"active_threats_count"`
	TakenDownCount     int            `json:"taken_down_count"`
	TopTargetedBrands  map[string]int `json:"top_targeted_brands"`
}

// PhishingStatusUpdateRequest represents an admin payload to update threat mitigation status.
type PhishingStatusUpdateRequest struct {
	ID     string `json:"id"`
	Status string `json:"status"` // ACTIVE_THREAT, REPORTED_KOMINFO, TAKEN_DOWN, WHITELISTED
	Notes  string `json:"notes"`
}
