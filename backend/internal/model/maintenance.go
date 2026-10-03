package model

import "time"

// DatabaseMaintenanceStats provides health and lifecycle metrics of the backend storage.
type DatabaseMaintenanceStats struct {
	DataFilePath         string    `json:"data_file_path"`
	FileSizeBytes        int64     `json:"file_size_bytes"`
	TotalSubscribers     int       `json:"total_subscribers"`
	TotalThreats         int       `json:"total_threats"`
	TotalPhishingDomains int       `json:"total_phishing_domains"`
	TotalAuditLogs       int       `json:"total_audit_logs"`
	TotalDLQRecords      int       `json:"total_dlq_records"`
	TotalOperators       int       `json:"total_operators"`
	LastBackupAt         time.Time `json:"last_backup_at"`
	LastRetentionRunAt   time.Time `json:"last_retention_run_at"`
	StorageEngine        string    `json:"storage_engine"` // "JSON File Persistence + Memory Cache"
	HealthStatus         string    `json:"health_status"`  // "HEALTHY", "WARNING", "DEGRADED"
}

// RetentionRunResult describes the result of a database cleanup execution.
type RetentionRunResult struct {
	ThreatsPruned int       `json:"threats_pruned"`
	AuditPruned   int       `json:"audit_pruned"`
	DLQPruned     int       `json:"dlq_pruned"`
	ExecutedAt    time.Time `json:"executed_at"`
}

// DatabaseSnapshot is the exported backup archive payload.
type DatabaseSnapshot struct {
	Version         string                     `json:"version"`
	ExportedAt      time.Time                  `json:"exported_at"`
	ExportedBy      string                     `json:"exported_by"`
	Subscribers     map[string]*Subscriber     `json:"subscribers"`
	Threats         []ThreatEvent              `json:"threats"`
	PhishingRecords map[string]*PhishingRecord `json:"phishing_records"`
	Operators       map[string]*Operator       `json:"operators"`
	AuditLogs       []AuditLog                 `json:"audit_logs"`
	ApiKeys         map[string]*IngestionApiKey `json:"api_keys"`
	DLQRecords      []DeadLetterRecord         `json:"dlq_records"`
}
