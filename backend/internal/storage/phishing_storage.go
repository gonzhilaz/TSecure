package storage

import (
	"bytes"
	"encoding/csv"
	"fmt"
	"net/url"
	"sort"
	"strconv"
	"strings"
	"time"

	"telkomsecure-backend/internal/model"
)

func (s *Storage) seedInitialPhishing() {
	now := time.Now()
	seeds := []*model.PhishingRecord{
		{
			ID:              "PHISH-2026-001",
			URL:             "https://telkomsel-poin-tukar-hadiah.top/login",
			Domain:          "telkomsel-poin-tukar-hadiah.top",
			Category:        "PHISHING",
			TargetBrand:     "Telkomsel",
			Severity:        "CRITICAL",
			Status:          "ACTIVE_THREAT",
			HitCount:        142,
			FirstDetectedAt: now.Add(-72 * time.Hour),
			LastDetectedAt:  now.Add(-15 * time.Minute),
			TargetedMSISDNs: []string{"081234567890", "082199887766"},
			ActionTaken:     "BLOCKED",
			KSNVerdict:      "Dangerous URL / Phishing heuristic match",
			Notes:           "Fake login targeting MyTelkomsel points with SMS OTP harvester",
		},
		{
			ID:              "PHISH-2026-002",
			URL:             "https://klik-bca-pembaruan-tarif.info/auth",
			Domain:          "klik-bca-pembaruan-tarif.info",
			Category:        "FAKE_LOGIN",
			TargetBrand:     "Bank BCA",
			Severity:        "CRITICAL",
			Status:          "REPORTED_KOMINFO",
			HitCount:        98,
			FirstDetectedAt: now.Add(-48 * time.Hour),
			LastDetectedAt:  now.Add(-2 * time.Hour),
			TargetedMSISDNs: []string{"081122334455"},
			ActionTaken:     "BLOCKED",
			KSNVerdict:      "Malicious credential phishing",
			Notes:           "Impersonates KlikBCA Individual login with KeyBCA token sniffer",
		},
		{
			ID:              "PHISH-2026-003",
			URL:             "https://bri-mo-verifikasi-keamanan.vip/aktivasi",
			Domain:          "bri-mo-verifikasi-keamanan.vip",
			Category:        "SMISHING",
			TargetBrand:     "Bank BRI",
			Severity:        "CRITICAL",
			Status:          "ACTIVE_THREAT",
			HitCount:        76,
			FirstDetectedAt: now.Add(-36 * time.Hour),
			LastDetectedAt:  now.Add(-45 * time.Minute),
			TargetedMSISDNs: []string{"085298765432"},
			ActionTaken:     "BLOCKED",
			KSNVerdict:      "Phishing domain (Trojan-Banker redirect)",
			Notes:           "Distributed via WhatsApp/SMS APK scam redirect",
		},
		{
			ID:              "PHISH-2026-004",
			URL:             "https://djp-pajak-surat-peringatan.online/ebilling",
			Domain:          "djp-pajak-surat-peringatan.online",
			Category:        "MALWARE_URL",
			TargetBrand:     "Instansi Pemerintah",
			Severity:        "HIGH",
			Status:          "TAKEN_DOWN",
			HitCount:        34,
			FirstDetectedAt: now.Add(-120 * time.Hour),
			LastDetectedAt:  now.Add(-24 * time.Hour),
			TargetedMSISDNs: []string{"081344556677"},
			ActionTaken:     "ISOLATED",
			KSNVerdict:      "Known malicious host / Dropper",
			Notes:           "Spoofs Direktorat Jenderal Pajak (DJP) e-Billing notification",
		},
		{
			ID:              "PHISH-2026-005",
			URL:             "https://dana-kaget-amplop-ramadhan.xyz/claim",
			Domain:          "dana-kaget-amplop-ramadhan.xyz",
			Category:        "PHISHING",
			TargetBrand:     "E-Wallet & Fintech",
			Severity:        "MEDIUM",
			Status:          "ACTIVE_THREAT",
			HitCount:        52,
			FirstDetectedAt: now.Add(-24 * time.Hour),
			LastDetectedAt:  now.Add(-10 * time.Minute),
			TargetedMSISDNs: []string{"087788990011"},
			ActionTaken:     "BLOCKED",
			KSNVerdict:      "Fraud / Social engineering",
			Notes:           "Fake DANA Kaget reward page soliciting PIN and OTP",
		},
	}

	for _, seed := range seeds {
		s.phishingRecords[seed.Domain] = seed
	}
}

// recordPhishingLocked records or aggregates an incoming threat event into the phishing database.
// Must be called with s.mu held.
func (s *Storage) recordPhishingLocked(event model.ThreatEvent) {
	targetURL := strings.TrimSpace(event.Target)
	if targetURL == "" {
		return
	}
	domain := extractDomain(targetURL)
	if domain == "" {
		return
	}

	now := time.Now()
	if existing, exists := s.phishingRecords[domain]; exists {
		existing.HitCount++
		existing.LastDetectedAt = now
		if event.Description != "" {
			existing.KSNVerdict = event.Description
		}
		if event.MSISDN != "" {
			cleaned := cleanMsisdn(event.MSISDN)
			hasMsisdn := false
			for _, m := range existing.TargetedMSISDNs {
				if m == cleaned {
					hasMsisdn = true
					break
				}
			}
			if !hasMsisdn {
				existing.TargetedMSISDNs = append(existing.TargetedMSISDNs, cleaned)
			}
		}
		return
	}

	// Create new phishing intelligence record
	brand := detectTargetBrand(targetURL, event.Description)
	category := "PHISHING"
	if event.ThreatType == "MALWARE_URL" {
		category = "MALWARE_URL"
	}

	newRecord := &model.PhishingRecord{
		ID:              fmt.Sprintf("PHISH-%s-%04d", now.Format("2006"), len(s.phishingRecords)+1),
		URL:             targetURL,
		Domain:          domain,
		Category:        category,
		TargetBrand:     brand,
		Severity:        "CRITICAL",
		Status:          "ACTIVE_THREAT",
		HitCount:        1,
		FirstDetectedAt: now,
		LastDetectedAt:  now,
		TargetedMSISDNs: make([]string, 0),
		ActionTaken:     "BLOCKED",
		KSNVerdict:      event.Description,
		Notes:           "Auto-harvested via TelkomSecure Web Protection Telemetry",
	}

	if event.MSISDN != "" {
		newRecord.TargetedMSISDNs = append(newRecord.TargetedMSISDNs, cleanMsisdn(event.MSISDN))
	}
	if newRecord.KSNVerdict == "" {
		newRecord.KSNVerdict = "Malicious URL detected by Kaspersky Web Protection"
	}

	s.phishingRecords[domain] = newRecord
}

// RecordPhishingFromThreat is thread-safe and updates persistent storage.
func (s *Storage) RecordPhishingFromThreat(event model.ThreatEvent) {
	s.mu.Lock()
	defer s.mu.Unlock()
	s.recordPhishingLocked(event)
	_ = s.saveToFile()
}

// ListPhishingRecords returns phishing intelligence records filtered by brand, status, and query.
func (s *Storage) ListPhishingRecords(brand, status, query string) []*model.PhishingRecord {
	s.mu.RLock()
	defer s.mu.RUnlock()

	q := strings.ToLower(strings.TrimSpace(query))
	bFilter := strings.ToLower(strings.TrimSpace(brand))
	sFilter := strings.ToUpper(strings.TrimSpace(status))

	out := make([]*model.PhishingRecord, 0, len(s.phishingRecords))
	for _, rec := range s.phishingRecords {
		if q != "" {
			match := strings.Contains(strings.ToLower(rec.Domain), q) ||
				strings.Contains(strings.ToLower(rec.URL), q) ||
				strings.Contains(strings.ToLower(rec.ID), q) ||
				strings.Contains(strings.ToLower(rec.TargetBrand), q)
			if !match {
				continue
			}
		}
		if bFilter != "" && bFilter != "all" {
			if strings.ToLower(rec.TargetBrand) != bFilter {
				continue
			}
		}
		if sFilter != "" && sFilter != "ALL" {
			if strings.ToUpper(rec.Status) != sFilter {
				continue
			}
		}
		copied := *rec
		out = append(out, &copied)
	}

	// Sort descending by HitCount then LastDetectedAt
	sort.Slice(out, func(i, j int) bool {
		if out[i].HitCount == out[j].HitCount {
			return out[i].LastDetectedAt.After(out[j].LastDetectedAt)
		}
		return out[i].HitCount > out[j].HitCount
	})

	return out
}

// GetPhishingStats calculates real-time metrics for SOC Phishing Intel Desk.
func (s *Storage) GetPhishingStats() model.PhishingStats {
	s.mu.RLock()
	defer s.mu.RUnlock()

	totalHits := 0
	activeCount := 0
	takenDownCount := 0
	brandCounts := make(map[string]int)

	for _, rec := range s.phishingRecords {
		totalHits += rec.HitCount
		if rec.Status == "ACTIVE_THREAT" {
			activeCount++
		}
		if rec.Status == "TAKEN_DOWN" {
			takenDownCount++
		}
		brandCounts[rec.TargetBrand]++
	}

	return model.PhishingStats{
		TotalUniqueDomains: len(s.phishingRecords),
		TotalHitsBlocked:   totalHits,
		ActiveThreatsCount: activeCount,
		TakenDownCount:     takenDownCount,
		TopTargetedBrands:  brandCounts,
	}
}

// UpdatePhishingStatus updates threat triage and status notes.
func (s *Storage) UpdatePhishingStatus(id, status, notes string) (*model.PhishingRecord, error) {
	s.mu.Lock()
	defer s.mu.Unlock()

	for _, rec := range s.phishingRecords {
		if rec.ID == id || rec.Domain == id {
			rec.Status = strings.ToUpper(status)
			if notes != "" {
				rec.Notes = notes
			}
			_ = s.saveToFile()
			copied := *rec
			return &copied, nil
		}
	}
	return nil, fmt.Errorf("phishing record %s not found", id)
}

// ExportPhishingCSV generates standard RFC-4180 CSV for CSIRT / Kominfo reporting.
func (s *Storage) ExportPhishingCSV() ([]byte, error) {
	s.mu.RLock()
	defer s.mu.RUnlock()

	var buf bytes.Buffer
	writer := csv.NewWriter(&buf)

	headers := []string{"ID", "Domain", "URL", "Category", "TargetBrand", "Severity", "Status", "HitCount", "FirstDetectedAt", "LastDetectedAt", "ActionTaken", "TargetedVictimsCount", "Notes"}
	if err := writer.Write(headers); err != nil {
		return nil, err
	}

	for _, rec := range s.phishingRecords {
		row := []string{
			rec.ID,
			rec.Domain,
			rec.URL,
			rec.Category,
			rec.TargetBrand,
			rec.Severity,
			rec.Status,
			strconv.Itoa(rec.HitCount),
			rec.FirstDetectedAt.Format(time.RFC3339),
			rec.LastDetectedAt.Format(time.RFC3339),
			rec.ActionTaken,
			strconv.Itoa(len(rec.TargetedMSISDNs)),
			rec.Notes,
		}
		if err := writer.Write(row); err != nil {
			return nil, err
		}
	}

	writer.Flush()
	return buf.Bytes(), writer.Error()
}

func extractDomain(rawURL string) string {
	raw := strings.TrimSpace(rawURL)
	if !strings.HasPrefix(raw, "http://") && !strings.HasPrefix(raw, "https://") {
		raw = "http://" + raw
	}
	u, err := url.Parse(raw)
	if err == nil && u.Hostname() != "" {
		return strings.ToLower(u.Hostname())
	}
	trimmed := strings.TrimPrefix(strings.TrimPrefix(rawURL, "https://"), "http://")
	parts := strings.Split(trimmed, "/")
	return strings.ToLower(parts[0])
}

func detectTargetBrand(urlStr, desc string) string {
	combined := strings.ToLower(urlStr + " " + desc)
	switch {
	case strings.Contains(combined, "telkomsel") || strings.Contains(combined, "mytelkomsel") || strings.Contains(combined, "tsel"):
		return "Telkomsel"
	case strings.Contains(combined, "bca") || strings.Contains(combined, "klikbca") || strings.Contains(combined, "mybca"):
		return "Bank BCA"
	case strings.Contains(combined, "bri") || strings.Contains(combined, "brimo"):
		return "Bank BRI"
	case strings.Contains(combined, "mandiri") || strings.Contains(combined, "livin"):
		return "Bank Mandiri"
	case strings.Contains(combined, "bni") || strings.Contains(combined, "wondr"):
		return "Bank BNI"
	case strings.Contains(combined, "pajak") || strings.Contains(combined, "djp") || strings.Contains(combined, "gov") || strings.Contains(combined, "kemkes"):
		return "Instansi Pemerintah"
	case strings.Contains(combined, "dana") || strings.Contains(combined, "gopay") || strings.Contains(combined, "ovo") || strings.Contains(combined, "shopee"):
		return "E-Wallet & Fintech"
	default:
		return "General Phishing"
	}
}
