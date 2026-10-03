package service

import (
	"telkomsecure-backend/internal/model"
)

func (s *SubscriptionService) GetMaintenanceStats() model.DatabaseMaintenanceStats {
	return s.store.GetMaintenanceStats()
}

func (s *SubscriptionService) ExportSnapshot(operatorEmail string) (*model.DatabaseSnapshot, error) {
	return s.store.ExportSnapshot(operatorEmail)
}

func (s *SubscriptionService) RestoreSnapshot(snap model.DatabaseSnapshot, operatorEmail string) error {
	return s.store.RestoreSnapshot(snap, operatorEmail)
}

func (s *SubscriptionService) RunRetentionPolicy(threatDays, auditDays, dlqDays int, operatorEmail string) model.RetentionRunResult {
	return s.store.RunRetentionPolicy(threatDays, auditDays, dlqDays, operatorEmail)
}
