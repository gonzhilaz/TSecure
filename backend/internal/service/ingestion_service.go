package service

import (
	"telkomsecure-backend/internal/model"
)

func (s *SubscriptionService) VerifyApiKey(key string) (*model.IngestionApiKey, bool) {
	return s.store.VerifyApiKey(key)
}

func (s *SubscriptionService) ListApiKeys() []*model.IngestionApiKey {
	return s.store.ListApiKeys()
}

func (s *SubscriptionService) CreateApiKey(name, source string) (*model.IngestionApiKey, error) {
	return s.store.CreateApiKey(name, source)
}

func (s *SubscriptionService) ProcessNdpIngest(payload model.NdpIngestPayload) (*model.Subscriber, error) {
	return s.store.ProcessNdpIngest(payload)
}

func (s *SubscriptionService) ProcessProxyThreatIngest(payload model.ProxyThreatIngestPayload) (int, error) {
	return s.store.ProcessProxyThreatIngest(payload)
}

func (s *SubscriptionService) RecordDeadLetter(source, rawPayload, errMsg, severity string) model.DeadLetterRecord {
	return s.store.RecordDeadLetter(source, rawPayload, errMsg, severity)
}

func (s *SubscriptionService) ListDLQRecords(status string) []model.DeadLetterRecord {
	return s.store.ListDLQRecords(status)
}

func (s *SubscriptionService) ResolveDLQRecord(id, newStatus, notes string) (*model.DeadLetterRecord, error) {
	return s.store.ResolveDLQRecord(id, newStatus, notes)
}
