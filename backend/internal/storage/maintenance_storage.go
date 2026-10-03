package storage

import (
	"fmt"
	"os"
	"time"

	"telkomsecure-backend/internal/model"
)

// GetMaintenanceStats calculates storage footprint and maintenance metrics.
func (s *Storage) GetMaintenanceStats() model.DatabaseMaintenanceStats {
	s.mu.RLock()
	defer s.mu.RUnlock()

	var fileSize int64 = 0
	if s.persistPath != "" {
		if fi, err := os.Stat(s.persistPath); err == nil {
			fileSize = fi.Size()
		}
	}

	return model.DatabaseMaintenanceStats{
		DataFilePath:         s.persistPath,
		FileSizeBytes:        fileSize,
		TotalSubscribers:     len(s.subscribers),
		TotalThreats:         len(s.threats),
		TotalPhishingDomains: len(s.phishingRecords),
		TotalAuditLogs:       len(s.auditLogs),
		TotalDLQRecords:      len(s.dlqRecords),
		TotalOperators:       len(s.operators),
		LastBackupAt:         s.lastBackupAt,
		LastRetentionRunAt:   s.lastRetentionAt,
		StorageEngine:        "JSON Persistence + In-Memory RW Cache",
		HealthStatus:         "HEALTHY",
	}
}

// ExportSnapshot creates a complete verifiable backup archive.
func (s *Storage) ExportSnapshot(operatorEmail string) (*model.DatabaseSnapshot, error) {
	s.mu.Lock()
	defer s.mu.Unlock()

	now := time.Now()
	s.lastBackupAt = now

	// Deep copy maps
	subsCopy := make(map[string]*model.Subscriber, len(s.subscribers))
	for k, v := range s.subscribers {
		copied := *v
		subsCopy[k] = &copied
	}

	threatsCopy := make([]model.ThreatEvent, len(s.threats))
	copy(threatsCopy, s.threats)

	phishCopy := make(map[string]*model.PhishingRecord, len(s.phishingRecords))
	for k, v := range s.phishingRecords {
		copied := *v
		phishCopy[k] = &copied
	}

	opsCopy := make(map[string]*model.Operator, len(s.operators))
	for k, v := range s.operators {
		copied := *v
		opsCopy[k] = &copied
	}

	keysCopy := make(map[string]*model.IngestionApiKey, len(s.apiKeys))
	for k, v := range s.apiKeys {
		copied := *v
		keysCopy[k] = &copied
	}

	snapshot := &model.DatabaseSnapshot{
		Version:         "2.4",
		ExportedAt:      now,
		ExportedBy:      operatorEmail,
		Subscribers:     subsCopy,
		Threats:         threatsCopy,
		PhishingRecords: phishCopy,
		Operators:       opsCopy,
		AuditLogs:       s.auditLogs,
		ApiKeys:         keysCopy,
		DLQRecords:      s.dlqRecords,
	}

	_ = s.saveToFile()
	return snapshot, nil
}

// RestoreSnapshot restores all state from a verified backup archive.
func (s *Storage) RestoreSnapshot(snap model.DatabaseSnapshot, operatorEmail string) error {
	s.mu.Lock()
	defer s.mu.Unlock()

	if len(snap.Subscribers) == 0 && len(snap.Operators) == 0 {
		return fmt.Errorf("berkas cadangan (backup) tidak valid atau kosong")
	}

	if snap.Subscribers != nil {
		s.subscribers = snap.Subscribers
	}
	if snap.Threats != nil {
		s.threats = snap.Threats
	}
	if snap.PhishingRecords != nil {
		s.phishingRecords = snap.PhishingRecords
	}
	if snap.Operators != nil {
		s.operators = snap.Operators
	}
	if snap.ApiKeys != nil {
		s.apiKeys = snap.ApiKeys
	}
	if snap.DLQRecords != nil {
		s.dlqRecords = snap.DLQRecords
	}

	now := time.Now()
	s.auditLogs = append([]model.AuditLog{
		{
			ID:             fmt.Sprintf("AUDIT-%d", now.UnixNano()),
			OperatorEmail:  operatorEmail,
			OperatorName:   operatorEmail,
			Action:         "DATABASE_RESTORE",
			TargetResource: "ALL_STORAGE",
			Details:        fmt.Sprintf("Restored snapshot exported at %s", snap.ExportedAt.Format(time.RFC3339)),
			IPAddress:      "SOC_CONSOLE",
			Timestamp:      now,
		},
	}, s.auditLogs...)

	_ = s.saveToFile()
	return nil
}

// RunRetentionPolicy purges old threats, logs, and resolved DLQ records.
func (s *Storage) RunRetentionPolicy(threatDays, auditDays, dlqDays int, operatorEmail string) model.RetentionRunResult {
	s.mu.Lock()
	defer s.mu.Unlock()

	now := time.Now()
	s.lastRetentionAt = now

	threatCutoff := now.Add(-time.Duration(threatDays) * 24 * time.Hour)
	auditCutoff := now.Add(-time.Duration(auditDays) * 24 * time.Hour)
	dlqCutoff := now.Add(-time.Duration(dlqDays) * 24 * time.Hour)

	// 1. Prune threats
	var newThreats []model.ThreatEvent
	prunedThreats := 0
	for _, t := range s.threats {
		if t.Timestamp.After(threatCutoff) {
			newThreats = append(newThreats, t)
		} else {
			prunedThreats++
		}
	}
	s.threats = newThreats

	// 2. Prune audit logs
	var newAudit []model.AuditLog
	prunedAudit := 0
	for _, a := range s.auditLogs {
		if a.Timestamp.After(auditCutoff) {
			newAudit = append(newAudit, a)
		} else {
			prunedAudit++
		}
	}
	s.auditLogs = newAudit

	// 3. Prune DLQ records
	var newDLQ []model.DeadLetterRecord
	prunedDLQ := 0
	for _, d := range s.dlqRecords {
		if d.Status == "PENDING" || d.Timestamp.After(dlqCutoff) {
			newDLQ = append(newDLQ, d)
		} else {
			prunedDLQ++
		}
	}
	s.dlqRecords = newDLQ

	_ = s.saveToFile()

	return model.RetentionRunResult{
		ThreatsPruned: prunedThreats,
		AuditPruned:   prunedAudit,
		DLQPruned:     prunedDLQ,
		ExecutedAt:    now,
	}
}
