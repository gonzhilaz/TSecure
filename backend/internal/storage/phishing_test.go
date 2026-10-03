package storage

import (
	"testing"
	"time"

	"telkomsecure-backend/internal/model"
)

func TestPhishingDatabase(t *testing.T) {
	s := NewStorage("")

	stats := s.GetPhishingStats()
	if stats.TotalUniqueDomains == 0 {
		t.Fatalf("expected seed phishing records, got %d", stats.TotalUniqueDomains)
	}

	// Test adding a threat event
	event := model.ThreatEvent{
		ID:          "THREAT-TEST-1",
		MSISDN:      "081299887766",
		ThreatType:  "PHISHING",
		Target:      "https://klikbca-update-tarif-palsu.com/login",
		Severity:    "CRITICAL",
		Description: "BCA Credential Harvesting Phishing",
		ActionTaken: "BLOCKED",
		Timestamp:   time.Now(),
	}
	s.AddThreatEvent(event)

	records := s.ListPhishingRecords("Bank BCA", "ALL", "klikbca-update-tarif-palsu.com")
	if len(records) == 0 {
		t.Fatalf("expected to find newly added phishing domain")
	}

	rec := records[0]
	if rec.Domain != "klikbca-update-tarif-palsu.com" {
		t.Errorf("expected domain klikbca-update-tarif-palsu.com, got %s", rec.Domain)
	}
	if rec.TargetBrand != "Bank BCA" {
		t.Errorf("expected TargetBrand Bank BCA, got %s", rec.TargetBrand)
	}
	if rec.HitCount < 1 {
		t.Errorf("expected hit count >= 1, got %d", rec.HitCount)
	}

	// Test status update
	updated, err := s.UpdatePhishingStatus(rec.ID, "REPORTED_KOMINFO", "Ticket #ID-CSIRT-9812")
	if err != nil {
		t.Fatalf("failed to update status: %v", err)
	}
	if updated.Status != "REPORTED_KOMINFO" {
		t.Errorf("expected status REPORTED_KOMINFO, got %s", updated.Status)
	}

	// Test CSV export
	csvBytes, err := s.ExportPhishingCSV()
	if err != nil || len(csvBytes) == 0 {
		t.Fatalf("failed to export CSV: %v", err)
	}
}
