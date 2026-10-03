package model

import "time"

// PackagePurchaseRecord represents historical purchase events on MyTelkomsel / USSD / BSS.
type PackagePurchaseRecord struct {
	ID           string    `json:"id"`
	PackageName  string    `json:"package_name"`
	PurchasedAt  time.Time `json:"purchased_at"`
	DurationDays int       `json:"duration_days"`
	Price        float64   `json:"price"`
	Channel      string    `json:"channel"` // MyTelkomsel, USSD *363#, BSS
	Status       string    `json:"status"`  // SUCCESS, ACTIVE, EXPIRED
}

// ScanLog represents one of the latest device security scans and Kaspersky module checks.
type ScanLog struct {
	ID           string    `json:"id"`
	Module       string    `json:"module"`        // ANTIVIRUS, WEB_PROTECTION, SIM_WATCH, RASP, SYSTEM_AUDIT
	ScanType     string    `json:"scan_type"`     // FULL_SCAN, QUICK_SCAN, REALTIME_MONITOR, TAMPER_CHECK
	Result       string    `json:"result"`        // CLEAN, THREAT_BLOCKED, FILE_QUARANTINED, TAMPER_PREVENTED
	ItemsScanned int       `json:"items_scanned"` // Jumlah file / endpoint yang dipindai
	ThreatsFound int       `json:"threats_found"`
	Details      string    `json:"details"`
	Timestamp    time.Time `json:"timestamp"`
}

// Subscriber represents a Telkomsel Secure end-user with diagnostic & telemetry details.
type Subscriber struct {
	ID                   string                  `json:"id"`                     // Kode unik pelanggan (e.g. SUB-081299887766)
	MSISDN               string                  `json:"msisdn"`                 // Nomor HP Telkomsel
	MobileID             string                  `json:"mobile_id"`              // Bound Mobile ID device
	DeviceModel          string                  `json:"device_model"`           // e.g. Google Pixel 6
	OSVersion            string                  `json:"os_version"`             // e.g. Android 14 / UpsideDownCake
	PlanName             string                  `json:"plan_name"`              // Paket pelanggan
	PurchaseTimestamp    time.Time               `json:"purchase_timestamp"`     // Jam & tanggal pembelian di MyTelkomsel
	ActivePeriodStart    time.Time               `json:"active_period_start"`    // Masa aktif mulai
	ActivePeriodEnd      time.Time               `json:"active_period_end"`      // Masa aktif berakhir (NDP)
	KasperskyExpiryDate  time.Time               `json:"kaspersky_expiry_date"`  // Masa aktif riil token lisensi Kaspersky
	DesyncDays           int                     `json:"desync_days"`            // Selisih hari antara NDP dan Kaspersky
	IsActive             bool                    `json:"is_active"`              // Status aktif
	ActivationCode       string                  `json:"activation_code"`        // Kode aktivasi / OTP SMS (e.g. TK-889123)
	ActivationStatus     string                  `json:"activation_status"`      // ACTIVATED, SMS_FAILED, PENDING_CODE, DESYNC_WARNING, EXPIRED
	KasperskyLicenseKey  string                  `json:"kaspersky_license_key"`  // B2B license key
	RootStatus           string                  `json:"root_status"`            // CLEAN, ROOT_DETECTED (Magisk/KernelSU)
	HookStatus           string                  `json:"hook_status"`            // CLEAN, HOOK_DETECTED (Frida/Xposed)
	BoundIccid           string                  `json:"bound_iccid"`            // ICCID SIM card resmi
	CurrentIccid         string                  `json:"current_iccid"`          // ICCID yang terpasang saat ini (SIM Watch)
	SimSlot              string                  `json:"sim_slot"`               // Slot 1 (Telkomsel Halo)
	QuarantineCount      int                     `json:"quarantine_count"`       // Jumlah file malware yang dikarantina
	DeviceMigrationCount int                     `json:"device_migration_count"` // Riwayat ganti device
	PurchaseHistory      []PackagePurchaseRecord `json:"purchase_history"`       // List histori pembelian paket
	RecentScanLogs       []ScanLog               `json:"recent_scan_logs"`       // 10 log scan terakhir device & modul Kaspersky
	DataRetentionDays    int                     `json:"data_retention_days"`    // Default 30 hari (1 bulan)
	RetentionExpiresAt   time.Time               `json:"retention_expires_at"`   // Tanggal jadwal auto-archive
	IsArchived           bool                    `json:"is_archived"`            // Status arsip cold storage
	LastCheckedAt        time.Time               `json:"last_checked_at"`        // Terakhir kali device check-in
	CreatedAt            time.Time               `json:"created_at"`
}

// ActivePeriodResponse is returned to the mobile app during splash verification.
type ActivePeriodResponse struct {
	IsValid               bool      `json:"is_valid"`
	IsExpired             bool      `json:"is_expired"`
	IsPendingProvisioning bool      `json:"is_pending_provisioning"`
	ActivationStatus      string    `json:"activation_status"`
	PlanName              string    `json:"plan_name"`
	StartDate             time.Time `json:"start_date"`
	EndDate               time.Time `json:"end_date"`
	DaysRemaining         int       `json:"days_remaining"`
	LicenseKey            string    `json:"license_key"`
	Message               string    `json:"message"`
}

// ThreatEvent represents security telemetry reported by mobile devices.
type ThreatEvent struct {
	ID          string    `json:"id"`
	MobileID    string    `json:"mobile_id"`
	MSISDN      string    `json:"msisdn"`
	ThreatType  string    `json:"threat_type"` // PHISHING, MALWARE, EICAR, SIM_WATCH, RASP, WIFI
	Target      string    `json:"target"`      // URL, file path, SSID, or component
	Severity    string    `json:"severity"`    // CRITICAL, HIGH, MEDIUM, LOW
	Description string    `json:"description"`
	ActionTaken string    `json:"action_taken"` // BLOCKED, ISOLATED, REPORTED
	Latitude    float64   `json:"latitude"`
	Longitude   float64   `json:"longitude"`
	City        string    `json:"city"`
	LocationTag string    `json:"location_tag"`
	NetworkType string    `json:"network_type"` // e.g. "WiFi (Public)" or "Telkomsel 5G"
	Timestamp   time.Time `json:"timestamp"`
}

// NdpOrderRequest represents an order instruction from Telkomsel NDP/BSS billing.
type NdpOrderRequest struct {
	MSISDN       string  `json:"msisdn"`
	PackageID    string  `json:"package_id"`
	PackageName  string  `json:"package_name"`
	DurationDays int     `json:"duration_days"`
	Price        float64 `json:"price"`
	Channel      string  `json:"channel"` // MyTelkomsel, BSS, Web
}

// DeviceMigrationRequest handles customer device swap mid-subscription.
type DeviceMigrationRequest struct {
	MSISDN         string `json:"msisdn"`
	NewMobileID    string `json:"new_mobile_id"`
	NewDeviceModel string `json:"new_device_model"`
	NewOSVersion   string `json:"new_os_version"`
	Reason         string `json:"reason"`
}

// AdminActionResponse standardizes response for diagnostic tools.
type AdminActionResponse struct {
	Status     string      `json:"status"`
	Message    string      `json:"message"`
	Subscriber *Subscriber `json:"subscriber,omitempty"`
}

// DashboardStats summarizes key operational metrics for the Next.js SOC Dashboard.
type DashboardStats struct {
	TotalSubscribers     int           `json:"total_subscribers"`
	ActiveSubscribers    int           `json:"active_subscribers"`
	ExpiredSubscribers   int           `json:"expired_subscribers"`
	PendingActivation    int           `json:"pending_activation"`
	SmsDeliveryFailed    int           `json:"sms_delivery_failed"`
	DesyncWarnings       int           `json:"desync_warnings"`
	RootedDevices        int           `json:"rooted_devices"`
	SimSwapAlerts        int           `json:"sim_swap_alerts"`
	TotalThreatsBlocked  int           `json:"total_threats_blocked"`
	ThreatsToday         int           `json:"threats_today"`
	KasperskyQuotaTotal  int           `json:"kaspersky_quota_total"`
	KasperskyQuotaUsed   int           `json:"kaspersky_quota_used"`
	AverageSecurityScore int           `json:"average_security_score"`
	RecentThreats        []ThreatEvent `json:"recent_threats"`
}

// OtpRequest represents a request to dispatch an SMS OTP code.
type OtpRequest struct {
	MSISDN   string `json:"msisdn"`
	MobileID string `json:"mobile_id"`
}

// OtpResponse is returned after generating an OTP code.
type OtpResponse struct {
	Status               string `json:"status"`
	Message              string `json:"message"`
	ResendTimeoutSeconds int    `json:"resend_timeout_seconds"`
	MockOtp              string `json:"mock_otp,omitempty"`
}

// VerifyOtpRequest verifies an entered OTP.
type VerifyOtpRequest struct {
	MSISDN   string `json:"msisdn"`
	MobileID string `json:"mobile_id"`
	OtpCode  string `json:"otp_code"`
}

// VerifyOtpResponse returns session token and subscriber subscription details.
type VerifyOtpResponse struct {
	Success         bool                  `json:"success"`
	NeedsActivation bool                  `json:"needs_activation"`
	SessionToken    string                `json:"session_token"`
	ExpiresInDays   int                   `json:"expires_in_days"`
	Message         string                `json:"message"`
	ActivePeriod    *ActivePeriodResponse `json:"active_period,omitempty"`
}

// LicenseActivationRequest handles binding Kaspersky license to Mobile ID
type LicenseActivationRequest struct {
	MSISDN             string `json:"msisdn"`
	MobileID           string `json:"mobile_id"`
	DeviceModel        string `json:"device_model,omitempty"`
	OSVersion          string `json:"os_version,omitempty"`
	SimulateKspOutage  bool   `json:"simulate_ksp_outage"`
	SimulatePendingNDP bool   `json:"simulate_pending_ndp"`
}

// LicenseActivationResponse represents result of multi-stage activation wizard
type LicenseActivationResponse struct {
	Success          bool   `json:"success"`
	ActivationStatus string `json:"activation_status"` // ACTIVATED, ACTIVATION_PENDING_KSP, PENDING_PROVISIONING
	Stage            int    `json:"stage"`             // 1, 2, 3
	StatusMessage    string `json:"status_message"`
	LicenseKey       string `json:"license_key,omitempty"`
}

// AppUpdateResponse represents OTA In-App APK update metadata
type AppUpdateResponse struct {
	HasUpdate         bool     `json:"has_update"`
	LatestVersion     string   `json:"latest_version"`
	LatestBuildNumber int      `json:"latest_build_number"`
	MinSupportedBuild int      `json:"min_supported_build"`
	IsMandatory       bool     `json:"is_mandatory"`
	ReleaseNotes      []string `json:"release_notes"`
	DownloadURL       string   `json:"download_url"`
	ApkSizeMB         float64  `json:"apk_size_mb"`
	PublishedAt       string   `json:"published_at"`
}
