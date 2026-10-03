package model

import "time"

// Operator represents a SOC administrator or analyst user.
type Operator struct {
	ID           string    `json:"id"`
	Name         string    `json:"name"`
	Email        string    `json:"email"`
	PasswordHash string    `json:"-"` // Never serialized in JSON
	Role         string    `json:"role"` // SUPERADMIN, SOC_ANALYST, CUSTOMER_CARE, AUDITOR
	BadgeNumber  string    `json:"badge_number"`
	IsActive     bool      `json:"is_active"`
	LastLoginAt  time.Time `json:"last_login_at"`
	CreatedAt    time.Time `json:"created_at"`
}

// OperatorLoginRequest is the payload for SOC login.
type OperatorLoginRequest struct {
	Email    string `json:"email"`
	Password string `json:"password"`
}

// OperatorLoginResponse returns session token and operator profile.
type OperatorLoginResponse struct {
	Token     string    `json:"token"`
	Operator  Operator  `json:"operator"`
	ExpiresAt time.Time `json:"expires_at"`
}

// OperatorCreateRequest is the payload to add a new operator.
type OperatorCreateRequest struct {
	Name        string `json:"name"`
	Email       string `json:"email"`
	Password    string `json:"password"`
	Role        string `json:"role"`
	BadgeNumber string `json:"badge_number"`
}

// AuditLog tracks every sensitive administrative action in the SOC.
type AuditLog struct {
	ID             string    `json:"id"`
	OperatorEmail  string    `json:"operator_email"`
	OperatorName   string    `json:"operator_name"`
	Action         string    `json:"action"` // USER_CREATE, BACKUP_CREATE, DLQ_REPLAY, TAKEDOWN_SUBMIT, etc.
	TargetResource string    `json:"target_resource"`
	Details        string    `json:"details"`
	IPAddress      string    `json:"ip_address"`
	Timestamp      time.Time `json:"timestamp"`
}
