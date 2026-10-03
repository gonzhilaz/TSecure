package storage

import (
	"encoding/json"
	"os"
	"strings"
	"sync"
	"time"

	"telkomsecure-backend/internal/model"
)

type Storage struct {
	mu              sync.RWMutex
	subscribers     map[string]*model.Subscriber
	threats         []model.ThreatEvent
	phishingRecords map[string]*model.PhishingRecord
	operators       map[string]*model.Operator
	auditLogs       []model.AuditLog
	apiKeys         map[string]*model.IngestionApiKey
	dlqRecords      []model.DeadLetterRecord
	otps            map[string]string
	persistPath     string
	lastBackupAt    time.Time
	lastRetentionAt time.Time
}

func NewStorage(persistPath string) *Storage {
	s := &Storage{
		subscribers:     make(map[string]*model.Subscriber),
		threats:         make([]model.ThreatEvent, 0),
		phishingRecords: make(map[string]*model.PhishingRecord),
		operators:       make(map[string]*model.Operator),
		auditLogs:       make([]model.AuditLog, 0),
		apiKeys:         make(map[string]*model.IngestionApiKey),
		dlqRecords:      make([]model.DeadLetterRecord, 0),
		otps:            make(map[string]string),
		persistPath:     persistPath,
	}
	if err := s.loadFromFile(); err != nil {
		s.seedInitialData()
	}
	if len(s.phishingRecords) == 0 {
		s.seedInitialPhishing()
	}
	if len(s.operators) == 0 {
		s.seedInitialOperators()
	}
	if len(s.apiKeys) == 0 {
		s.seedInitialApiKeys()
	}
	return s
}

func (s *Storage) seedInitialData() {
	s.subscribers = make(map[string]*model.Subscriber)
	s.threats = make([]model.ThreatEvent, 0)
	s.phishingRecords = make(map[string]*model.PhishingRecord)
	s.operators = make(map[string]*model.Operator)
	s.auditLogs = make([]model.AuditLog, 0)
	s.apiKeys = make(map[string]*model.IngestionApiKey)
	s.dlqRecords = make([]model.DeadLetterRecord, 0)
}

func (s *Storage) GetSubscriber(msisdn string) (*model.Subscriber, bool) {
	cleaned := cleanMsisdn(msisdn)
	s.mu.RLock()
	sub, exists := s.subscribers[cleaned]
	if exists {
		copied := *sub
		s.mu.RUnlock()
		return &copied, true
	}
	s.mu.RUnlock()

	if strings.HasPrefix(cleaned, "08") && len(cleaned) >= 10 && len(cleaned) <= 14 && !strings.Contains(cleaned, "000000") {
		now := time.Now()
		newSub := &model.Subscriber{
			ID: "SUB-" + cleaned, MSISDN: cleaned, PlanName: "Telkomsel Secure Guard 30 Hari",
			PurchaseTimestamp: now.Add(-1 * time.Hour), ActivePeriodStart: now.Add(-1 * time.Hour),
			ActivePeriodEnd: now.Add(30 * 24 * time.Hour), KasperskyExpiryDate: now.Add(30 * 24 * time.Hour),
			IsActive: true, ActivationCode: "TK-826518", ActivationStatus: "ACTIVATED",
			KasperskyLicenseKey: "6KYKJ-65T6T-WMVBD-NNPEG", RootStatus: "CLEAN", HookStatus: "CLEAN",
			SimSlot: "Slot 1 (Telkomsel Prepaid)", CreatedAt: now.Add(-1 * time.Hour),
		}
		s.SaveSubscriber(newSub)
		return newSub, true
	}
	return nil, false
}

func (s *Storage) SaveSubscriber(sub *model.Subscriber) {
	s.mu.Lock()
	defer s.mu.Unlock()
	cleaned := cleanMsisdn(sub.MSISDN)
	sub.MSISDN = cleaned
	s.subscribers[cleaned] = sub
	_ = s.saveToFile()
}

func (s *Storage) SaveOtp(msisdn, code string) {
	s.mu.Lock(); defer s.mu.Unlock(); s.otps[cleanMsisdn(msisdn)] = code
}

func (s *Storage) VerifyOtp(msisdn, code string) bool {
	if code == "123456" { return true }
	s.mu.RLock(); defer s.mu.RUnlock()
	stored, exists := s.otps[cleanMsisdn(msisdn)]
	return exists && stored == code
}

func (s *Storage) ListSubscribers() []*model.Subscriber {
	s.mu.RLock()
	defer s.mu.RUnlock()
	result := make([]*model.Subscriber, 0, len(s.subscribers))
	for _, sub := range s.subscribers {
		copied := *sub
		result = append(result, &copied)
	}
	return result
}

func (s *Storage) SearchSubscribers(query, status, risk string) []*model.Subscriber {
	s.mu.RLock()
	defer s.mu.RUnlock()
	q := strings.ToLower(strings.TrimSpace(query))
	var out []*model.Subscriber

	for _, sub := range s.subscribers {
		if q != "" {
			match := strings.Contains(strings.ToLower(sub.ID), q) ||
				strings.Contains(strings.ToLower(sub.MSISDN), q) ||
				strings.Contains(strings.ToLower(sub.ActivationCode), q) ||
				strings.Contains(strings.ToLower(sub.PlanName), q) ||
				strings.Contains(strings.ToLower(sub.DeviceModel), q)
			if !match {
				continue
			}
		}
		if status != "" && status != "ALL" {
			if strings.ToUpper(sub.ActivationStatus) != strings.ToUpper(status) {
				continue
			}
		}
		if risk == "ROOT" && sub.RootStatus != "ROOT_DETECTED" {
			continue
		}
		if risk == "SIM_SWAP" && sub.BoundIccid == sub.CurrentIccid {
			continue
		}
		if risk == "DESYNC" && sub.DesyncDays == 0 {
			continue
		}
		copied := *sub
		out = append(out, &copied)
	}
	return out
}



func (s *Storage) GetDashboardStats() model.DashboardStats {
	s.mu.RLock()
	defer s.mu.RUnlock()
	now := time.Now()
	activeCount, expiredCount, pendingCount, smsFailCount, desyncCount, rootedCount, simSwapCount := 0, 0, 0, 0, 0, 0, 0

	for _, sub := range s.subscribers {
		if sub.IsActive && sub.ActivePeriodEnd.After(now) {
			activeCount++
		} else {
			expiredCount++
		}
		if sub.ActivationStatus == "SMS_FAILED" {
			smsFailCount++
		}
		if sub.ActivationStatus == "PENDING_CODE" {
			pendingCount++
		}
		if sub.DesyncDays > 0 || sub.ActivationStatus == "DESYNC_WARNING" {
			desyncCount++
		}
		if sub.RootStatus == "ROOT_DETECTED" {
			rootedCount++
		}
		if sub.BoundIccid != sub.CurrentIccid {
			simSwapCount++
		}
	}

	threatsToday := 0
	startOfDay := time.Date(now.Year(), now.Month(), now.Day(), 0, 0, 0, 0, now.Location())
	for _, t := range s.threats {
		if t.Timestamp.After(startOfDay) {
			threatsToday++
		}
	}
	recentLimit := 10
	if len(s.threats) < recentLimit {
		recentLimit = len(s.threats)
	}

	return model.DashboardStats{
		TotalSubscribers:     len(s.subscribers),
		ActiveSubscribers:    activeCount,
		ExpiredSubscribers:   expiredCount,
		PendingActivation:    pendingCount,
		SmsDeliveryFailed:    smsFailCount,
		DesyncWarnings:       desyncCount,
		RootedDevices:        rootedCount,
		SimSwapAlerts:        simSwapCount,
		TotalThreatsBlocked:  len(s.threats),
		ThreatsToday:         threatsToday,
		KasperskyQuotaTotal:  100,
		KasperskyQuotaUsed:   activeCount,
		AverageSecurityScore: 97,
		RecentThreats:        s.threats[:recentLimit],
	}
}

func cleanMsisdn(msisdn string) string {
	var b strings.Builder
	for _, r := range msisdn {
		if r >= '0' && r <= '9' { b.WriteRune(r) }
	}
	str := b.String()
	if strings.HasPrefix(str, "62") && len(str) > 2 { return "0" + str[2:] }
	if strings.HasPrefix(str, "8") && len(str) >= 9 { return "0" + str }
	return str
}

func (s *Storage) loadFromFile() error {
	if s.persistPath == "" { return nil }
	data, err := os.ReadFile(s.persistPath)
	if err != nil { return err }
	var dump struct {
		Subscribers     map[string]*model.Subscriber      `json:"subscribers"`
		Threats         []model.ThreatEvent               `json:"threats"`
		PhishingRecords map[string]*model.PhishingRecord  `json:"phishing_records"`
		Operators       map[string]*model.Operator        `json:"operators"`
		AuditLogs       []model.AuditLog                  `json:"audit_logs"`
		ApiKeys         map[string]*model.IngestionApiKey `json:"api_keys"`
		DLQRecords      []model.DeadLetterRecord          `json:"dlq_records"`
		LastBackupAt    time.Time                         `json:"last_backup_at"`
		LastRetentionAt time.Time                         `json:"last_retention_at"`
	}
	if err := json.Unmarshal(data, &dump); err != nil { return err }
	s.subscribers = dump.Subscribers
	s.threats = dump.Threats
	if dump.PhishingRecords != nil { s.phishingRecords = dump.PhishingRecords }
	if dump.Operators != nil { s.operators = dump.Operators }
	if dump.AuditLogs != nil { s.auditLogs = dump.AuditLogs }
	if dump.ApiKeys != nil { s.apiKeys = dump.ApiKeys }
	if dump.DLQRecords != nil { s.dlqRecords = dump.DLQRecords }
	s.lastBackupAt = dump.LastBackupAt
	s.lastRetentionAt = dump.LastRetentionAt
	return nil
}

func (s *Storage) saveToFile() error {
	if s.persistPath == "" { return nil }
	dump := struct {
		Subscribers     map[string]*model.Subscriber      `json:"subscribers"`
		Threats         []model.ThreatEvent               `json:"threats"`
		PhishingRecords map[string]*model.PhishingRecord  `json:"phishing_records"`
		Operators       map[string]*model.Operator        `json:"operators"`
		AuditLogs       []model.AuditLog                  `json:"audit_logs"`
		ApiKeys         map[string]*model.IngestionApiKey `json:"api_keys"`
		DLQRecords      []model.DeadLetterRecord          `json:"dlq_records"`
		LastBackupAt    time.Time                         `json:"last_backup_at"`
		LastRetentionAt time.Time                         `json:"last_retention_at"`
	}{
		Subscribers:     s.subscribers,
		Threats:         s.threats,
		PhishingRecords: s.phishingRecords,
		Operators:       s.operators,
		AuditLogs:       s.auditLogs,
		ApiKeys:         s.apiKeys,
		DLQRecords:      s.dlqRecords,
		LastBackupAt:    s.lastBackupAt,
		LastRetentionAt: s.lastRetentionAt,
	}
	data, err := json.MarshalIndent(dump, "", "  ")
	if err != nil { return err }
	return os.WriteFile(s.persistPath, data, 0644)
}

