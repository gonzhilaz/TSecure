package storage

import (
	"time"

	"telkomsecure-backend/internal/model"
)

// generateSampleThreats generates initial security threat telemetry for the SOC dashboard.
func generateSampleThreats(now time.Time) []model.ThreatEvent {
	return []model.ThreatEvent{
		{
			ID:          "THR-20261003-001",
			MobileID:    "MOBILE_TEST",
			MSISDN:      "081299887766",
			ThreatType:  "PHISHING",
			Target:      "https://my-telkomsel-poin-klaim.xyz/login",
			Severity:    "CRITICAL",
			Description: "Percobaan akses phishing credential harvesting Telkomsel Poin diblokir oleh Web Filter.",
			ActionTaken: "BLOCKED",
			Timestamp:   now.Add(-25 * time.Minute),
		},
		{
			ID:          "THR-20261003-002",
			MobileID:    "MOBILE_TEST",
			MSISDN:      "081299887766",
			ThreatType:  "MALWARE",
			Target:      "/storage/emulated/0/Download/apk_undangan_pernikahan.apk",
			Severity:    "CRITICAL",
			Description: "Trojan-Banker.AndroidOS.FakeDoc berhasil diidentifikasi dan dikarantina.",
			ActionTaken: "ISOLATED",
			Timestamp:   now.Add(-2 * time.Hour),
		},
		{
			ID:          "THR-20261003-003",
			MobileID:    "MOBILE_S24_02",
			MSISDN:      "081122334455",
			ThreatType:  "RASP",
			Target:      "libhook.so / Xposed Framework",
			Severity:    "HIGH",
			Description: "Injeksi runtime hooking terdeteksi pada memori proses. BlackWall RASP mengaktifkan tampered shield.",
			ActionTaken: "REPORTED",
			Timestamp:   now.Add(-5 * time.Hour),
		},
		{
			ID:          "THR-20261003-004",
			MobileID:    "MOBILE_ROG_03",
			MSISDN:      "085233445566",
			ThreatType:  "SIM_WATCH",
			Target:      "SIM Slot 1 (ICCID Changed)",
			Severity:    "HIGH",
			Description: "Deteksi pergantian SIM Card tanpa otorisasi. Sinyal proteksi SIM Watch dikirimkan ke SOC.",
			ActionTaken: "REPORTED",
			Timestamp:   now.Add(-8 * time.Hour),
		},
		{
			ID:          "THR-20261003-005",
			MobileID:    "MOBILE_IP15_04",
			MSISDN:      "082155667788",
			ThreatType:  "PHISHING",
			Target:      "http://bca-klik-secure-update.com/auth",
			Severity:    "CRITICAL",
			Description: "Situs perbankan palsu dicegah oleh Kaspersky Web Protection SDK.",
			ActionTaken: "BLOCKED",
			Timestamp:   now.Add(-14 * time.Hour),
		},
	}
}
