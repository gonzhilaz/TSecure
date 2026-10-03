package storage

import (
	"fmt"
	"strings"
	"time"

	"telkomsecure-backend/internal/model"
)

// AddThreatEvent records an incoming threat from mobile telemetry or SOC feed.
// It also attaches the threat event into the subscriber's RecentScanLogs for Customer Diagnostics.
func (s *Storage) AddThreatEvent(event model.ThreatEvent) {
	s.mu.Lock()
	defer s.mu.Unlock()

	if event.ID == "" {
		event.ID = fmt.Sprintf("THREAT-%d", time.Now().UnixNano())
	}
	if event.Timestamp.IsZero() {
		event.Timestamp = time.Now()
	}

	// 1. Prepend to global threat stream (capped at 100 recent threats)
	s.threats = append([]model.ThreatEvent{event}, s.threats...)
	if len(s.threats) > 100 {
		s.threats = s.threats[:100]
	}

	// 2. Correlate with subscriber if MSISDN or MobileID is provided
	if event.MSISDN != "" {
		cleaned := cleanMsisdn(event.MSISDN)
		if sub, exists := s.subscribers[cleaned]; exists {
			module := event.ThreatType
			if module == "PHISHING" || module == "MALWARE_URL" {
				module = "WEB_PROTECTION"
			} else if module == "" {
				module = "ANTIVIRUS"
			}

			newLog := model.ScanLog{
				ID:           event.ID,
				Module:       module,
				ScanType:     "REALTIME_MONITOR",
				Result:       "THREAT_BLOCKED",
				ItemsScanned: 1,
				ThreatsFound: 1,
				Details:      fmt.Sprintf("%s: %s (%s)", event.ThreatType, event.Target, event.Description),
				Timestamp:    event.Timestamp,
			}

			// Prepend to subscriber's recent scan logs (keep max 10)
			sub.RecentScanLogs = append([]model.ScanLog{newLog}, sub.RecentScanLogs...)
			if len(sub.RecentScanLogs) > 10 {
				sub.RecentScanLogs = sub.RecentScanLogs[:10]
			}

			if event.ActionTaken == "ISOLATED" || event.ActionTaken == "QUARANTINED" {
				sub.QuarantineCount++
			}
			sub.LastCheckedAt = event.Timestamp
		}
	}

	// 3. Aggregate web phishing / malware URLs into Phishing Threat Intelligence DB
	if event.ThreatType == "PHISHING" || event.ThreatType == "MALWARE_URL" ||
		strings.HasPrefix(event.Target, "http://") || strings.HasPrefix(event.Target, "https://") {
		s.recordPhishingLocked(event)
	}

	_ = s.saveToFile()
}

// ListThreats returns up to limit recent threat events.
func (s *Storage) ListThreats(limit int) []model.ThreatEvent {
	s.mu.RLock()
	defer s.mu.RUnlock()

	if limit <= 0 || limit > len(s.threats) {
		limit = len(s.threats)
	}
	result := make([]model.ThreatEvent, limit)
	copy(result, s.threats[:limit])
	return result
}
