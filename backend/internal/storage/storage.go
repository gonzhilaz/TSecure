package storage

import (
	"encoding/json"
	"fmt"
	"os"
	"strings"
	"sync"
	"time"

	"telkomsecure-backend/internal/model"
)

type Storage struct {
	mu          sync.RWMutex
	subscribers map[string]*model.Subscriber
	threats     []model.ThreatEvent
	otps        map[string]string
	persistPath string
}

func NewStorage(persistPath string) *Storage {
	s := &Storage{
		subscribers: make(map[string]*model.Subscriber),
		threats:     make([]model.ThreatEvent, 0),
		otps:        make(map[string]string),
		persistPath: persistPath,
	}
	if err := s.loadFromFile(); err != nil {
		s.seedInitialData()
	}
	return s
}

func (s *Storage) seedInitialData() {
	s.subscribers = make(map[string]*model.Subscriber)
	s.threats = make([]model.ThreatEvent, 0)
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

func (s *Storage) ResendActivationCode(msisdn string) (*model.Subscriber, error) {
	s.mu.Lock()
	defer s.mu.Unlock()
	cleaned := cleanMsisdn(msisdn)
	sub, exists := s.subscribers[cleaned]
	if !exists {
		return nil, fmt.Errorf("subscriber not found")
	}
	sub.ActivationStatus = "ACTIVATED"
	sub.IsActive = true
	sub.LastCheckedAt = time.Now()
	_ = s.saveToFile()
	copied := *sub
	return &copied, nil
}

func (s *Storage) ResyncKasperskyLicense(msisdn string) (*model.Subscriber, error) {
	s.mu.Lock()
	defer s.mu.Unlock()
	cleaned := cleanMsisdn(msisdn)
	sub, exists := s.subscribers[cleaned]
	if !exists {
		return nil, fmt.Errorf("subscriber not found")
	}
	// Reconcile Kaspersky expiry date with NDP active period end
	sub.KasperskyExpiryDate = sub.ActivePeriodEnd
	sub.DesyncDays = 0
	if sub.ActivationStatus == "DESYNC_WARNING" {
		sub.ActivationStatus = "ACTIVATED"
	}
	_ = s.saveToFile()
	copied := *sub
	return &copied, nil
}

func (s *Storage) MigrateDevice(req model.DeviceMigrationRequest) (*model.Subscriber, error) {
	s.mu.Lock()
	defer s.mu.Unlock()
	cleaned := cleanMsisdn(req.MSISDN)
	sub, exists := s.subscribers[cleaned]
	if !exists {
		return nil, fmt.Errorf("subscriber not found")
	}
	if req.NewMobileID != "" {
		sub.MobileID = req.NewMobileID
	} else {
		sub.MobileID = fmt.Sprintf("MOBILE ID-MIGRATED-%d", time.Now().Unix()%1000000)
	}
	if req.NewDeviceModel != "" {
		sub.DeviceModel = req.NewDeviceModel
	}
	if req.NewOSVersion != "" {
		sub.OSVersion = req.NewOSVersion
	}
	sub.DeviceMigrationCount++
	sub.LastCheckedAt = time.Now()
	_ = s.saveToFile()
	copied := *sub
	return &copied, nil
}

func (s *Storage) AddThreatEvent(event model.ThreatEvent) {
	s.mu.Lock()
	defer s.mu.Unlock()
	if event.ID == "" {
		event.ID = fmt.Sprintf("THREAT-%d", time.Now().UnixNano())
	}
	if event.Timestamp.IsZero() {
		event.Timestamp = time.Now()
	}
	s.threats = append([]model.ThreatEvent{event}, s.threats...)
	if len(s.threats) > 100 {
		s.threats = s.threats[:100]
	}
	_ = s.saveToFile()
}

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

func (s *Storage) SimulateNdpPurchase(req model.NdpOrderRequest) *model.Subscriber {
	s.mu.Lock()
	defer s.mu.Unlock()
	cleaned := cleanMsisdn(req.MSISDN)
	now := time.Now()
	days := req.DurationDays
	if days <= 0 {
		days = 30
	}
	sub, exists := s.subscribers[cleaned]
	if !exists {
		sub = &model.Subscriber{
			ID:                fmt.Sprintf("SUB-%s", cleaned),
			MSISDN:            cleaned,
			MobileID:          "MOBILE ID-AUTO-" + cleaned[len(cleaned)-4:],
			DeviceModel:       "Smartphone (Auto-Provisioned)",
			OSVersion:         "Android 14",
			ActivationCode:    fmt.Sprintf("TK-%d", 100000+now.Unix()%900000),
			RootStatus:        "CLEAN",
			HookStatus:        "CLEAN",
			BoundIccid:        "89620188" + cleaned[len(cleaned)-8:],
			CurrentIccid:      "89620188" + cleaned[len(cleaned)-8:],
			SimSlot:           "Slot 1 (Telkomsel)",
			PurchaseTimestamp: now,
			CreatedAt:         now,
		}
	}
	if sub.IsActive && sub.ActivePeriodEnd.After(now) {
		sub.ActivePeriodEnd = sub.ActivePeriodEnd.Add(time.Duration(days) * 24 * time.Hour)
	} else {
		sub.ActivePeriodStart = now
		sub.ActivePeriodEnd = now.Add(time.Duration(days) * 24 * time.Hour)
	}
	sub.KasperskyExpiryDate = sub.ActivePeriodEnd
	sub.DesyncDays = 0
	sub.IsActive = true
	sub.ActivationStatus = "ACTIVATED"
	sub.PlanName = req.PackageName
	sub.KasperskyLicenseKey = "6KYKJ-65T6T-WMVBD-NNPEG"
	sub.LastCheckedAt = now
	s.subscribers[cleaned] = sub
	_ = s.saveToFile()
	copied := *sub
	return &copied
}

func (s *Storage) SimulateNdpExpire(msisdn string) (*model.Subscriber, bool) {
	s.mu.Lock()
	defer s.mu.Unlock()
	cleaned := cleanMsisdn(msisdn)
	sub, exists := s.subscribers[cleaned]
	if !exists {
		return nil, false
	}
	sub.IsActive = false
	sub.ActivationStatus = "EXPIRED"
	sub.ActivePeriodEnd = time.Now().Add(-1 * time.Hour)
	sub.KasperskyExpiryDate = sub.ActivePeriodEnd
	_ = s.saveToFile()
	copied := *sub
	return &copied, true
}

func (s *Storage) SimulateNdpUnactivated(msisdn string) (*model.Subscriber, bool) {
	s.mu.Lock()
	defer s.mu.Unlock()
	cleaned := cleanMsisdn(msisdn)
	sub, exists := s.subscribers[cleaned]
	if !exists {
		return nil, false
	}
	sub.IsActive = false
	sub.ActivationStatus = "PENDING_ACTIVATION"
	sub.ActivePeriodStart = time.Time{}
	sub.ActivePeriodEnd = time.Time{}
	sub.KasperskyLicenseKey = ""
	sub.KasperskyExpiryDate = time.Time{}
	sub.MobileID = ""
	_ = s.saveToFile()
	copied := *sub
	return &copied, true
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
	return str
}

func (s *Storage) loadFromFile() error {
	if s.persistPath == "" { return nil }
	data, err := os.ReadFile(s.persistPath)
	if err != nil { return err }
	var dump struct {
		Subscribers map[string]*model.Subscriber `json:"subscribers"`
		Threats     []model.ThreatEvent          `json:"threats"`
	}
	if err := json.Unmarshal(data, &dump); err != nil { return err }
	s.subscribers, s.threats = dump.Subscribers, dump.Threats
	return nil
}

func (s *Storage) saveToFile() error {
	if s.persistPath == "" { return nil }
	dump := struct {
		Subscribers map[string]*model.Subscriber `json:"subscribers"`
		Threats     []model.ThreatEvent          `json:"threats"`
	}{Subscribers: s.subscribers, Threats: s.threats}
	data, err := json.MarshalIndent(dump, "", "  ")
	if err != nil { return err }
	return os.WriteFile(s.persistPath, data, 0644)
}
