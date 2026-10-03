package service

import (
	"telkomsecure-backend/internal/model"
)

func (s *SubscriptionService) AuthenticateOperator(email, password string) (*model.Operator, error) {
	return s.store.AuthenticateOperator(email, password)
}

func (s *SubscriptionService) GetOperator(email string) (*model.Operator, bool) {
	return s.store.GetOperator(email)
}

func (s *SubscriptionService) ListOperators() []*model.Operator {
	return s.store.ListOperators()
}

func (s *SubscriptionService) CreateOperator(req model.OperatorCreateRequest) (*model.Operator, error) {
	return s.store.CreateOperator(req)
}

func (s *SubscriptionService) ToggleOperatorStatus(id string, active bool) (*model.Operator, error) {
	return s.store.ToggleOperatorStatus(id, active)
}

func (s *SubscriptionService) RecordAuditLog(log model.AuditLog) {
	s.store.RecordAuditLog(log)
}

func (s *SubscriptionService) ListAuditLogs(limit int) []model.AuditLog {
	return s.store.ListAuditLogs(limit)
}
