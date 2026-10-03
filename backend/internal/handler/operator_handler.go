package handler

import (
	"encoding/json"
	"net/http"
	"strconv"
	"strings"
	"time"

	"telkomsecure-backend/internal/model"
)

// LoginSOC authenticates SOC operators and issues signed JWT session token.
func (h *APIHandler) LoginSOC(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		writeJSON(w, http.StatusMethodNotAllowed, map[string]string{"error": "method not allowed"})
		return
	}

	var req model.OperatorLoginRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		writeJSON(w, http.StatusBadRequest, map[string]string{"error": "payload login tidak valid"})
		return
	}

	op, err := h.svc.AuthenticateOperator(req.Email, req.Password)
	if err != nil {
		h.svc.RecordAuditLog(model.AuditLog{
			OperatorEmail:  req.Email,
			OperatorName:   req.Email,
			Action:         "LOGIN_FAILED",
			TargetResource: "AUTH_GATEWAY",
			Details:        err.Error(),
			IPAddress:      r.RemoteAddr,
		})
		writeJSON(w, http.StatusUnauthorized, map[string]string{"error": err.Error()})
		return
	}

	// 24 hour session duration
	token, exp, err := GenerateToken(op, 24*time.Hour)
	if err != nil {
		writeJSON(w, http.StatusInternalServerError, map[string]string{"error": "gagal membuat token sesi"})
		return
	}

	h.svc.RecordAuditLog(model.AuditLog{
		OperatorEmail:  op.Email,
		OperatorName:   op.Name,
		Action:         "LOGIN_SUCCESS",
		TargetResource: "SOC_CONSOLE",
		Details:        "Berhasil masuk ke konsol SOC (" + op.Role + ")",
		IPAddress:      r.RemoteAddr,
	})

	writeJSON(w, http.StatusOK, model.OperatorLoginResponse{
		Token:     token,
		Operator:  *op,
		ExpiresAt: exp,
	})
}

// LogoutSOC records logout event.
func (h *APIHandler) LogoutSOC(w http.ResponseWriter, r *http.Request) {
	authHeader := r.Header.Get("Authorization")
	tokenStr := strings.TrimPrefix(authHeader, "Bearer ")
	claims, _ := ValidateToken(tokenStr)

	email := "anonymous"
	name := "anonymous"
	if claims != nil {
		email = claims.Email
		name = claims.Name
	}

	h.svc.RecordAuditLog(model.AuditLog{
		OperatorEmail:  email,
		OperatorName:   name,
		Action:         "LOGOUT",
		TargetResource: "SOC_CONSOLE",
		Details:        "Sesi login diakhiri secara manual",
		IPAddress:      r.RemoteAddr,
	})

	writeJSON(w, http.StatusOK, map[string]any{
		"success": true,
		"message": "Sesi berhasil ditutup",
	})
}

// GetSOCMe returns the currently authenticated operator claims.
func (h *APIHandler) GetSOCMe(w http.ResponseWriter, r *http.Request) {
	tokenStr := strings.TrimPrefix(r.Header.Get("Authorization"), "Bearer ")
	claims, err := ValidateToken(tokenStr)
	if err != nil {
		writeJSON(w, http.StatusUnauthorized, map[string]string{"error": err.Error()})
		return
	}

	op, exists := h.svc.GetOperator(claims.Email)
	if !exists {
		writeJSON(w, http.StatusNotFound, map[string]string{"error": "operator tidak ditemukan"})
		return
	}

	writeJSON(w, http.StatusOK, map[string]any{
		"success":  true,
		"operator": op,
	})
}

// ListOperators returns all registered SOC operators.
func (h *APIHandler) ListOperators(w http.ResponseWriter, r *http.Request) {
	ops := h.svc.ListOperators()
	writeJSON(w, http.StatusOK, map[string]any{
		"success": true,
		"data":    ops,
		"count":   len(ops),
	})
}

// CreateOperator registers a new operator (Admin only).
func (h *APIHandler) CreateOperator(w http.ResponseWriter, r *http.Request) {
	var req model.OperatorCreateRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		writeJSON(w, http.StatusBadRequest, map[string]string{"error": "payload tidak valid: " + err.Error()})
		return
	}

	op, err := h.svc.CreateOperator(req)
	if err != nil {
		writeJSON(w, http.StatusBadRequest, map[string]string{"error": err.Error()})
		return
	}

	h.svc.RecordAuditLog(model.AuditLog{
		OperatorEmail:  "admin@telkomsel.co.id",
		OperatorName:   "Security Admin",
		Action:         "OPERATOR_CREATE",
		TargetResource: "OPERATORS",
		Details:        "Menambahkan operator baru: " + op.Email + " (" + op.Role + ")",
		IPAddress:      r.RemoteAddr,
	})

	writeJSON(w, http.StatusCreated, map[string]any{
		"success":  true,
		"message":  "Operator berhasil ditambahkan",
		"operator": op,
	})
}

// ToggleOperatorStatus enables or disables an operator.
func (h *APIHandler) ToggleOperatorStatus(w http.ResponseWriter, r *http.Request) {
	var body struct {
		ID     string `json:"id"`
		Active bool   `json:"active"`
	}
	if err := json.NewDecoder(r.Body).Decode(&body); err != nil {
		writeJSON(w, http.StatusBadRequest, map[string]string{"error": "payload tidak valid"})
		return
	}

	op, err := h.svc.ToggleOperatorStatus(body.ID, body.Active)
	if err != nil {
		writeJSON(w, http.StatusNotFound, map[string]string{"error": err.Error()})
		return
	}

	statusStr := "dinonaktifkan"
	if body.Active {
		statusStr = "diaktifkan"
	}

	h.svc.RecordAuditLog(model.AuditLog{
		OperatorEmail:  "admin@telkomsel.co.id",
		OperatorName:   "Security Admin",
		Action:         "OPERATOR_STATUS_UPDATE",
		TargetResource: op.Email,
		Details:        "Akun operator " + op.Email + " " + statusStr,
		IPAddress:      r.RemoteAddr,
	})

	writeJSON(w, http.StatusOK, map[string]any{
		"success":  true,
		"operator": op,
	})
}

// ListAuditLogs returns recent administrative audit logs.
func (h *APIHandler) ListAuditLogs(w http.ResponseWriter, r *http.Request) {
	limitStr := r.URL.Query().Get("limit")
	limit := 50
	if l, err := strconv.Atoi(limitStr); err == nil && l > 0 {
		limit = l
	}

	logs := h.svc.ListAuditLogs(limit)
	writeJSON(w, http.StatusOK, map[string]any{
		"success": true,
		"data":    logs,
		"count":   len(logs),
	})
}
