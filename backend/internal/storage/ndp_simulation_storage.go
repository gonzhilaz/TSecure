package storage

import (
	"fmt"
	"time"

	"telkomsecure-backend/internal/model"
)

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
	now := time.Now()
	sub, exists := s.subscribers[cleaned]
	if !exists {
		sub = &model.Subscriber{
			ID:                  fmt.Sprintf("SUB-%s", cleaned),
			MSISDN:              cleaned,
			MobileID:            "MOBILE ID-AUTO-" + cleaned[len(cleaned)-4:],
			DeviceModel:         "Smartphone (Auto-Provisioned)",
			OSVersion:           "Android 15",
			ActivationCode:      fmt.Sprintf("TK-%d", 100000+now.Unix()%900000),
			PlanName:            "Telkomsel Secure Guard 30 Hari",
			RootStatus:          "CLEAN",
			HookStatus:          "CLEAN",
			BoundIccid:          "89620188" + cleaned[len(cleaned)-8:],
			CurrentIccid:        "89620188" + cleaned[len(cleaned)-8:],
			SimSlot:             "Slot 1 (Telkomsel)",
			PurchaseTimestamp:   now.Add(-31 * 24 * time.Hour),
			ActivePeriodStart:   now.Add(-31 * 24 * time.Hour),
			ActivePeriodEnd:     now.Add(-1 * time.Hour),
			KasperskyExpiryDate: now.Add(-1 * time.Hour),
			IsActive:            false,
			ActivationStatus:    "EXPIRED",
			KasperskyLicenseKey: "6KYKJ-65T6T-WMVBD-NNPEG",
			CreatedAt:           now.Add(-31 * 24 * time.Hour),
		}
	} else {
		sub.IsActive = false
		sub.ActivationStatus = "EXPIRED"
		sub.ActivePeriodEnd = now.Add(-1 * time.Hour)
		sub.KasperskyExpiryDate = sub.ActivePeriodEnd
	}
	s.subscribers[cleaned] = sub
	_ = s.saveToFile()
	copied := *sub
	return &copied, true
}

func (s *Storage) SimulateNdpUnactivated(msisdn string) (*model.Subscriber, bool) {
	s.mu.Lock()
	defer s.mu.Unlock()
	cleaned := cleanMsisdn(msisdn)
	now := time.Now()
	sub, exists := s.subscribers[cleaned]
	if !exists {
		sub = &model.Subscriber{
			ID:                  fmt.Sprintf("SUB-%s", cleaned),
			MSISDN:              cleaned,
			MobileID:            "",
			DeviceModel:         "Smartphone (Auto-Provisioned)",
			OSVersion:           "Android 15",
			ActivationCode:      fmt.Sprintf("TK-%d", 100000+now.Unix()%900000),
			PlanName:            "Telkomsel Secure Guard (Belum Aktivasi)",
			RootStatus:          "CLEAN",
			HookStatus:          "CLEAN",
			BoundIccid:          "89620188" + cleaned[len(cleaned)-8:],
			CurrentIccid:        "89620188" + cleaned[len(cleaned)-8:],
			SimSlot:             "Slot 1 (Telkomsel)",
			PurchaseTimestamp:   now,
			ActivePeriodStart:   time.Time{},
			ActivePeriodEnd:     time.Time{},
			KasperskyExpiryDate: time.Time{},
			IsActive:            false,
			ActivationStatus:    "PENDING_ACTIVATION",
			KasperskyLicenseKey: "",
			CreatedAt:           now,
		}
	} else {
		sub.IsActive = false
		sub.ActivationStatus = "PENDING_ACTIVATION"
		sub.ActivePeriodStart = time.Time{}
		sub.ActivePeriodEnd = time.Time{}
		sub.KasperskyExpiryDate = time.Time{}
		sub.KasperskyLicenseKey = ""
	}
	s.subscribers[cleaned] = sub
	_ = s.saveToFile()
	copied := *sub
	return &copied, true
}
