package handler

import (
	"encoding/json"
	"fmt"
	"net/http"
	"time"

	"telkomsecure-backend/internal/model"
)

// GetMaintenanceStats returns database metrics, size, and lifecycle state.
func (h *APIHandler) GetMaintenanceStats(w http.ResponseWriter, r *http.Request) {
	stats := h.svc.GetMaintenanceStats()
	writeJSON(w, http.StatusOK, map[string]any{
		"success": true,
		"data":    stats,
	})
}

// ExportSnapshot downloads complete database snapshot as JSON file.
func (h *APIHandler) ExportSnapshot(w http.ResponseWriter, r *http.Request) {
	operatorEmail := "admin@telkomsel.co.id"
	if auth := r.Header.Get("Authorization"); auth != "" {
		if claims, err := ValidateToken(stringsTrimBearer(auth)); err == nil {
			operatorEmail = claims.Email
		}
	}

	snap, err := h.svc.ExportSnapshot(operatorEmail)
	if err != nil {
		writeJSON(w, http.StatusInternalServerError, map[string]string{"error": "gagal membuat cadangan database: " + err.Error()})
		return
	}

	snapBytes, err := json.MarshalIndent(snap, "", "  ")
	if err != nil {
		writeJSON(w, http.StatusInternalServerError, map[string]string{"error": "gagal serialisasi cadangan database"})
		return
	}

	filename := fmt.Sprintf("telkomsecure_db_snapshot_%s.json", time.Now().Format("20060102_150405"))
	w.Header().Set("Content-Type", "application/json")
	w.Header().Set("Content-Disposition", fmt.Sprintf("attachment; filename=\"%s\"", filename))
	w.WriteHeader(http.StatusOK)
	_, _ = w.Write(snapBytes)
}

// RestoreSnapshot restores database state from an uploaded backup JSON.
func (h *APIHandler) RestoreSnapshot(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		writeJSON(w, http.StatusMethodNotAllowed, map[string]string{"error": "method not allowed"})
		return
	}

	operatorEmail := "admin@telkomsel.co.id"
	if auth := r.Header.Get("Authorization"); auth != "" {
		if claims, err := ValidateToken(stringsTrimBearer(auth)); err == nil {
			operatorEmail = claims.Email
		}
	}

	var snap model.DatabaseSnapshot
	if err := json.NewDecoder(r.Body).Decode(&snap); err != nil {
		writeJSON(w, http.StatusBadRequest, map[string]string{"error": "format berkas cadangan (backup) tidak valid: " + err.Error()})
		return
	}

	if err := h.svc.RestoreSnapshot(snap, operatorEmail); err != nil {
		writeJSON(w, http.StatusBadRequest, map[string]string{"error": err.Error()})
		return
	}

	writeJSON(w, http.StatusOK, map[string]any{
		"success":     true,
		"message":     "Database berhasil dipulihkan dari cadangan.",
		"restored_at": time.Now(),
	})
}

// RunRetention executes automated data retention and pruning.
func (h *APIHandler) RunRetention(w http.ResponseWriter, r *http.Request) {
	operatorEmail := "admin@telkomsel.co.id"
	if auth := r.Header.Get("Authorization"); auth != "" {
		if claims, err := ValidateToken(stringsTrimBearer(auth)); err == nil {
			operatorEmail = claims.Email
		}
	}

	var body struct {
		ThreatDays int `json:"threat_days"`
		AuditDays  int `json:"audit_days"`
		DLQDays    int `json:"dlq_days"`
	}
	_ = json.NewDecoder(r.Body).Decode(&body)

	if body.ThreatDays <= 0 {
		body.ThreatDays = 90
	}
	if body.AuditDays <= 0 {
		body.AuditDays = 180
	}
	if body.DLQDays <= 0 {
		body.DLQDays = 30
	}

	res := h.svc.RunRetentionPolicy(body.ThreatDays, body.AuditDays, body.DLQDays, operatorEmail)

	writeJSON(w, http.StatusOK, map[string]any{
		"success": true,
		"message": "Kebijakan retensi dan pembersihan basis data selesai dijalankan.",
		"data":    res,
	})
}

func stringsTrimBearer(header string) string {
	if len(header) > 7 && header[:7] == "Bearer " {
		return header[7:]
	}
	return header
}
