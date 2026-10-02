package storage

import "telkomsecure-backend/internal/model"

// ClearAllData completely wipes subscribers, threats, logs, and otps from the backend.
func (s *Storage) ClearAllData() {
	s.mu.Lock()
	defer s.mu.Unlock()

	s.subscribers = make(map[string]*model.Subscriber)
	s.threats = make([]model.ThreatEvent, 0)
	s.otps = make(map[string]string)
	_ = s.saveToFile()
}

// ClearThreatsAndLogs removes all recorded threats and clears scan logs from all subscribers.
func (s *Storage) ClearThreatsAndLogs() {
	s.mu.Lock()
	defer s.mu.Unlock()

	s.threats = make([]model.ThreatEvent, 0)
	for _, sub := range s.subscribers {
		sub.RecentScanLogs = make([]model.ScanLog, 0)
		sub.QuarantineCount = 0
	}
	_ = s.saveToFile()
}

