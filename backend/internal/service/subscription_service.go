package service

import (
	"fmt"
	"math"
	"time"

	"telkomsecure-backend/internal/model"
	"telkomsecure-backend/internal/storage"
)

type SubscriptionService struct {
	store *storage.Storage
}

func NewSubscriptionService(store *storage.Storage) *SubscriptionService {
	return &SubscriptionService{store: store}
}

func (s *SubscriptionService) CheckActivePeriod(msisdn, mobileID, deviceModel, osVersion string) model.ActivePeriodResponse {
	now := time.Now()
	sub, exists := s.store.GetSubscriber(msisdn)

	if !exists {
		return model.ActivePeriodResponse{
			IsValid:       false,
			IsExpired:     true,
			PlanName:      "-",
			StartDate:     time.Time{},
			EndDate:       time.Time{},
			DaysRemaining: 0,
			LicenseKey:    "",
			Message:       "Nomor ponsel belum berlangganan paket Telkomsel Secure. Silakan aktivasi melalui aplikasi MyTelkomsel.",
		}
	}

	if mobileID != "" {
		sub.MobileID = mobileID
	}
	if deviceModel != "" {
		sub.DeviceModel = deviceModel
	}
	if osVersion != "" {
		sub.OSVersion = osVersion
	}
	sub.LastCheckedAt = now
	s.store.SaveSubscriber(sub)

	isPendingActivation := sub.ActivationStatus == "PENDING_ACTIVATION"
	isExpired := !isPendingActivation && (!sub.IsActive || now.After(sub.ActivePeriodEnd))
	daysRemaining := int(math.Ceil(sub.ActivePeriodEnd.Sub(now).Hours() / 24))
	if daysRemaining < 0 || isPendingActivation {
		daysRemaining = 0
	}

	licenseKey := sub.KasperskyLicenseKey
	if isPendingActivation {
		licenseKey = ""
	}

	msg := "Masa aktif paket aktif dan terlindungi penuh."
	if isPendingActivation {
		msg = "Paket terdaftar di NDP Telkomsel. Silakan selesaikan prosedur aktivasi lisensi pada perangkat."
	} else if isExpired {
		msg = "Masa aktif paket telah berakhir. Silakan perpanjang paket di MyTelkomsel."
	}

	return model.ActivePeriodResponse{
		IsValid:               !isExpired && !isPendingActivation,
		IsExpired:             isExpired,
		IsPendingProvisioning: sub.ActivationStatus == "PENDING_PROVISIONING",
		ActivationStatus:      sub.ActivationStatus,
		PlanName:              sub.PlanName,
		StartDate:             sub.ActivePeriodStart,
		EndDate:               sub.ActivePeriodEnd,
		DaysRemaining:         daysRemaining,
		LicenseKey:            licenseKey,
		Message:               msg,
	}
}

func (s *SubscriptionService) SimulatePurchase(req model.NdpOrderRequest) *model.Subscriber {
	return s.store.SimulateNdpPurchase(req)
}

func (s *SubscriptionService) SimulateExpire(msisdn string) (*model.Subscriber, bool) {
	return s.store.SimulateNdpExpire(msisdn)
}

func (s *SubscriptionService) SimulateUnactivated(msisdn string) (*model.Subscriber, bool) {
	return s.store.SimulateNdpUnactivated(msisdn)
}

func (s *SubscriptionService) ResendActivationCode(msisdn string) (*model.Subscriber, error) {
	return s.store.ResendActivationCode(msisdn)
}

func (s *SubscriptionService) ResyncKasperskyLicense(msisdn string) (*model.Subscriber, error) {
	return s.store.ResyncKasperskyLicense(msisdn)
}

func (s *SubscriptionService) MigrateDevice(req model.DeviceMigrationRequest) (*model.Subscriber, error) {
	return s.store.MigrateDevice(req)
}

func (s *SubscriptionService) SearchSubscribers(query, status, risk string) []*model.Subscriber {
	return s.store.SearchSubscribers(query, status, risk)
}

func (s *SubscriptionService) RecordThreat(threat model.ThreatEvent) {
	s.store.AddThreatEvent(threat)
}

func (s *SubscriptionService) GetStats() model.DashboardStats {
	return s.store.GetDashboardStats()
}

func (s *SubscriptionService) ListSubscribers() []*model.Subscriber {
	return s.store.ListSubscribers()
}

func (s *SubscriptionService) ListThreats(limit int) []model.ThreatEvent {
	return s.store.ListThreats(limit)
}

func (s *SubscriptionService) RequestOtp(msisdn, mobileID string) (model.OtpResponse, error) {
	now := time.Now()
	sub, exists := s.store.GetSubscriber(msisdn)
	if !exists {
		return model.OtpResponse{}, fmt.Errorf("Nomor ponsel belum berlangganan paket Telkomsel Secure. Silakan aktivasi melalui aplikasi MyTelkomsel.")
	}
	if !sub.IsActive || now.After(sub.ActivePeriodEnd) {
		return model.OtpResponse{}, fmt.Errorf("Masa aktif paket telah berakhir. Silakan perpanjang paket di MyTelkomsel.")
	}

	code := "123456" // Default mock OTP for reliable automated POC testing
	s.store.SaveOtp(msisdn, code)

	return model.OtpResponse{
		Status:               "SENT",
		Message:              "Kode OTP 6-digit berhasil dikirim melalui SMS.",
		ResendTimeoutSeconds: 60,
		MockOtp:              code,
	}, nil
}

func (s *SubscriptionService) VerifyOtp(msisdn, mobileID, code string) (model.VerifyOtpResponse, error) {
	if !s.store.VerifyOtp(msisdn, code) {
		return model.VerifyOtpResponse{
			Success: false,
			Message: "Kode OTP salah atau tidak valid. Silakan coba lagi.",
		}, nil
	}

	activePeriod := s.CheckActivePeriod(msisdn, mobileID, "", "")
	sessionToken := fmt.Sprintf("TSEL-SEC-%s-%d", msisdn, time.Now().Unix())

	sub, _ := s.store.GetSubscriber(msisdn)
	needsActivation := true
	if sub != nil && sub.ActivationStatus == "ACTIVATED" {
		needsActivation = false
		if mobileID != "" && sub.MobileID != mobileID {
			sub.MobileID = mobileID
			s.store.SaveSubscriber(sub)
		}
	}

	return model.VerifyOtpResponse{
		Success:         true,
		NeedsActivation: needsActivation,
		SessionToken:    sessionToken,
		ExpiresInDays:   30,
		Message:         "Autentikasi OTP berhasil. Sesi login tersimpan selama 30 hari.",
		ActivePeriod:    &activePeriod,
	}, nil
}

func (s *SubscriptionService) ActivateLicense(req model.LicenseActivationRequest) (model.LicenseActivationResponse, error) {
	sub, exists := s.store.GetSubscriber(req.MSISDN)
	if !exists {
		return model.LicenseActivationResponse{
			Success:          false,
			ActivationStatus: "NOT_SUBSCRIBED",
			Stage:            1,
			StatusMessage:    "Nomor ponsel belum berlangganan paket Telkomsel Secure.",
		}, nil
	}

	// Tahap 1: Verifikasi Paket MyTelkomsel
	if req.SimulatePendingNDP || sub.ActivationStatus == "PENDING_PROVISIONING" {
		return model.LicenseActivationResponse{
			Success:          false,
			ActivationStatus: "PENDING_PROVISIONING",
			Stage:            1,
			StatusMessage:    "Dalam Proses...",
		}, nil
	}

	now := time.Now()
	if sub.ActivationStatus == "PENDING_ACTIVATION" {
		sub.IsActive = true
		sub.ActivePeriodStart = now
		sub.ActivePeriodEnd = now.Add(30 * 24 * time.Hour)
		sub.KasperskyExpiryDate = sub.ActivePeriodEnd
	} else if !sub.IsActive || now.After(sub.ActivePeriodEnd) {
		return model.LicenseActivationResponse{
			Success:          false,
			ActivationStatus: "EXPIRED",
			Stage:            1,
			StatusMessage:    "Masa aktif paket telah berakhir. Silakan perpanjang di MyTelkomsel.",
		}, nil
	}

	// Tahap 2: Menerbitkan & Mengikat Lisensi Kaspersky B2B (Mobile ID += 1)
	if req.SimulateKspOutage {
		sub.ActivationStatus = "ACTIVATION_PENDING_KSP"
		s.store.SaveSubscriber(sub)
		return model.LicenseActivationResponse{
			Success:          true, // Pengguna tetap diberikan akses masuk ke aplikasi
			ActivationStatus: "ACTIVATION_PENDING_KSP",
			Stage:            2,
			StatusMessage:    "Sinkronisasi sedang berjalan",
		}, nil
	}

	// Tahap 3: Selesai
	sub.ActivationStatus = "ACTIVATED"
	if req.MobileID != "" {
		sub.MobileID = req.MobileID
	}
	if req.DeviceModel != "" {
		sub.DeviceModel = req.DeviceModel
	}
	if req.OSVersion != "" {
		sub.OSVersion = req.OSVersion
	}
	if sub.KasperskyLicenseKey == "" {
		sub.KasperskyLicenseKey = "6KYKJ-65T6T-WMVBD-NNPEG"
	}
	s.store.SaveSubscriber(sub)

	return model.LicenseActivationResponse{
		Success:          true,
		ActivationStatus: "ACTIVATED",
		Stage:            3,
		StatusMessage:    "Perangkat Berhasil Dilindungi",
		LicenseKey:       sub.KasperskyLicenseKey,
	}, nil
}

func (s *SubscriptionService) ClearThreatsAndLogs() {
	s.store.ClearThreatsAndLogs()
}

func (s *SubscriptionService) ClearAllData() {
	s.store.ClearAllData()
}

// Phishing Threat Intelligence delegations
func (s *SubscriptionService) ListPhishingRecords(brand, status, query string) []*model.PhishingRecord {
	return s.store.ListPhishingRecords(brand, status, query)
}

func (s *SubscriptionService) GetPhishingStats() model.PhishingStats {
	return s.store.GetPhishingStats()
}

func (s *SubscriptionService) UpdatePhishingStatus(id, status, notes string) (*model.PhishingRecord, error) {
	return s.store.UpdatePhishingStatus(id, status, notes)
}

func (s *SubscriptionService) ExportPhishingCSV() ([]byte, error) {
	return s.store.ExportPhishingCSV()
}

func (s *SubscriptionService) RecordPhishingFromThreat(event model.ThreatEvent) {
	s.store.RecordPhishingFromThreat(event)
}


