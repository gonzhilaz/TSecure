package storage

import (
	"crypto/sha256"
	"encoding/hex"
	"fmt"
	"strings"
	"time"

	"telkomsecure-backend/internal/model"
)

const operatorSalt = "telkomsecure_soc_salt_2026"

func hashPassword(pass string) string {
	h := sha256.New()
	h.Write([]byte(pass + operatorSalt))
	return hex.EncodeToString(h.Sum(nil))
}

func (s *Storage) seedInitialOperators() {
	now := time.Now()
	seeds := []*model.Operator{
		{
			ID:           "OP-001",
			Name:         "Budi Darmawan",
			Email:        "admin@telkomsel.co.id",
			PasswordHash: hashPassword("admin123"),
			Role:         "SUPERADMIN",
			BadgeNumber:  "TS-SEC-9901",
			IsActive:     true,
			CreatedAt:    now.Add(-90 * 24 * time.Hour),
		},
		{
			ID:           "OP-002",
			Name:         "Siti Rahmawati",
			Email:        "analyst@telkomsel.co.id",
			PasswordHash: hashPassword("analyst123"),
			Role:         "SOC_ANALYST",
			BadgeNumber:  "TS-SOC-4412",
			IsActive:     true,
			CreatedAt:    now.Add(-60 * 24 * time.Hour),
		},
		{
			ID:           "OP-003",
			Name:         "Rian Pratama",
			Email:        "helpdesk@telkomsel.co.id",
			PasswordHash: hashPassword("helpdesk123"),
			Role:         "CUSTOMER_CARE",
			BadgeNumber:  "TS-CARE-2105",
			IsActive:     true,
			CreatedAt:    now.Add(-30 * 24 * time.Hour),
		},
		{
			ID:           "OP-004",
			Name:         "Dewi Lestari",
			Email:        "auditor@telkomsel.co.id",
			PasswordHash: hashPassword("auditor123"),
			Role:         "AUDITOR",
			BadgeNumber:  "TS-AUD-1088",
			IsActive:     true,
			CreatedAt:    now.Add(-15 * 24 * time.Hour),
		},
	}

	for _, op := range seeds {
		s.operators[op.Email] = op
	}

	// Seed an initial audit log
	s.auditLogs = append(s.auditLogs, model.AuditLog{
		ID:             "AUDIT-INIT",
		OperatorEmail:  "system@telkomsel.co.id",
		OperatorName:   "TelkomSecure Security Core",
		Action:         "SYSTEM_INITIALIZE",
		TargetResource: "SOC_RBAC",
		Details:        "Initialized default SOC operators and RBAC matrix",
		IPAddress:      "127.0.0.1",
		Timestamp:      now,
	})
}

// AuthenticateOperator validates credentials and returns operator profile.
func (s *Storage) AuthenticateOperator(email, password string) (*model.Operator, error) {
	s.mu.Lock()
	defer s.mu.Unlock()

	cleanedEmail := strings.ToLower(strings.TrimSpace(email))
	op, exists := s.operators[cleanedEmail]
	if !exists {
		return nil, fmt.Errorf("email operator tidak terdaftar")
	}
	if !op.IsActive {
		return nil, fmt.Errorf("akun operator dinonaktifkan oleh administrator")
	}

	expectedHash := hashPassword(password)
	if op.PasswordHash != expectedHash {
		return nil, fmt.Errorf("kata sandi tidak cocok")
	}

	op.LastLoginAt = time.Now()
	_ = s.saveToFile()

	copied := *op
	return &copied, nil
}

// GetOperator returns operator profile by email.
func (s *Storage) GetOperator(email string) (*model.Operator, bool) {
	s.mu.RLock()
	defer s.mu.RUnlock()

	cleanedEmail := strings.ToLower(strings.TrimSpace(email))
	op, exists := s.operators[cleanedEmail]
	if !exists {
		return nil, false
	}
	copied := *op
	return &copied, true
}

// ListOperators returns all registered SOC operators.
func (s *Storage) ListOperators() []*model.Operator {
	s.mu.RLock()
	defer s.mu.RUnlock()

	res := make([]*model.Operator, 0, len(s.operators))
	for _, op := range s.operators {
		copied := *op
		res = append(res, &copied)
	}
	return res
}

// CreateOperator registers a new operator in the SOC.
func (s *Storage) CreateOperator(req model.OperatorCreateRequest) (*model.Operator, error) {
	s.mu.Lock()
	defer s.mu.Unlock()

	cleanedEmail := strings.ToLower(strings.TrimSpace(req.Email))
	if cleanedEmail == "" || req.Password == "" {
		return nil, fmt.Errorf("email dan kata sandi wajib diisi")
	}
	if _, exists := s.operators[cleanedEmail]; exists {
		return nil, fmt.Errorf("operator dengan email tersebut sudah terdaftar")
	}

	role := strings.ToUpper(strings.TrimSpace(req.Role))
	if role == "" {
		role = "SOC_ANALYST"
	}

	badge := req.BadgeNumber
	if badge == "" {
		badge = fmt.Sprintf("TS-SOC-%d", time.Now().Unix()%10000)
	}

	newOp := &model.Operator{
		ID:           fmt.Sprintf("OP-%03d", len(s.operators)+1),
		Name:         strings.TrimSpace(req.Name),
		Email:        cleanedEmail,
		PasswordHash: hashPassword(req.Password),
		Role:         role,
		BadgeNumber:  badge,
		IsActive:     true,
		CreatedAt:    time.Now(),
	}

	s.operators[cleanedEmail] = newOp
	_ = s.saveToFile()

	copied := *newOp
	return &copied, nil
}

// ToggleOperatorStatus activates or deactivates an operator account.
func (s *Storage) ToggleOperatorStatus(id string, active bool) (*model.Operator, error) {
	s.mu.Lock()
	defer s.mu.Unlock()

	for _, op := range s.operators {
		if op.ID == id || op.Email == id {
			op.IsActive = active
			_ = s.saveToFile()
			copied := *op
			return &copied, nil
		}
	}
	return nil, fmt.Errorf("operator tidak ditemukan")
}

// RecordAuditLog adds a security event into immutable audit trail.
func (s *Storage) RecordAuditLog(log model.AuditLog) {
	s.mu.Lock()
	defer s.mu.Unlock()

	if log.ID == "" {
		log.ID = fmt.Sprintf("AUDIT-%d", time.Now().UnixNano())
	}
	if log.Timestamp.IsZero() {
		log.Timestamp = time.Now()
	}

	s.auditLogs = append([]model.AuditLog{log}, s.auditLogs...)
	if len(s.auditLogs) > 500 {
		s.auditLogs = s.auditLogs[:500]
	}
	_ = s.saveToFile()
}

// ListAuditLogs retrieves recent audit logs.
func (s *Storage) ListAuditLogs(limit int) []model.AuditLog {
	s.mu.RLock()
	defer s.mu.RUnlock()

	if limit <= 0 || limit > len(s.auditLogs) {
		limit = len(s.auditLogs)
	}
	res := make([]model.AuditLog, limit)
	copy(res, s.auditLogs[:limit])
	return res
}
