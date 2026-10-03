package handler

import (
	"encoding/json"
	"fmt"
	"net/http"
	"time"

	"telkomsecure-backend/internal/model"
)

// ListPhishing returns filtered phishing intelligence records.
func (h *APIHandler) ListPhishing(w http.ResponseWriter, r *http.Request) {
	brand := r.URL.Query().Get("brand")
	status := r.URL.Query().Get("status")
	query := r.URL.Query().Get("q")

	records := h.svc.ListPhishingRecords(brand, status, query)
	writeJSON(w, http.StatusOK, map[string]any{
		"success": true,
		"data":    records,
		"count":   len(records),
	})
}

// GetPhishingStats returns real-time intelligence summary metrics.
func (h *APIHandler) GetPhishingStats(w http.ResponseWriter, r *http.Request) {
	stats := h.svc.GetPhishingStats()
	writeJSON(w, http.StatusOK, map[string]any{
		"success": true,
		"data":    stats,
	})
}

// UpdatePhishingStatus handles analyst triage status updates.
func (h *APIHandler) UpdatePhishingStatus(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost && r.Method != http.MethodPut {
		writeJSON(w, http.StatusMethodNotAllowed, map[string]string{"error": "method not allowed"})
		return
	}

	var req model.PhishingStatusUpdateRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		writeJSON(w, http.StatusBadRequest, map[string]string{"error": "invalid payload: " + err.Error()})
		return
	}

	if req.ID == "" || req.Status == "" {
		writeJSON(w, http.StatusBadRequest, map[string]string{"error": "id and status are required"})
		return
	}

	updated, err := h.svc.UpdatePhishingStatus(req.ID, req.Status, req.Notes)
	if err != nil {
		writeJSON(w, http.StatusNotFound, map[string]string{"error": err.Error()})
		return
	}

	h.broker.Broadcast("phishing_status_update", updated)

	writeJSON(w, http.StatusOK, map[string]any{
		"success": true,
		"message": "Phishing threat status updated successfully",
		"data":    updated,
	})
}

// ExportPhishingCSV streams RFC-4180 CSV for CSIRT / Kominfo reporting.
func (h *APIHandler) ExportPhishingCSV(w http.ResponseWriter, r *http.Request) {
	csvBytes, err := h.svc.ExportPhishingCSV()
	if err != nil {
		writeJSON(w, http.StatusInternalServerError, map[string]string{"error": "failed to generate CSV: " + err.Error()})
		return
	}

	filename := fmt.Sprintf("telkomsecure_phishing_intel_%s.csv", time.Now().Format("20060102_150405"))
	w.Header().Set("Content-Type", "text/csv; charset=utf-8")
	w.Header().Set("Content-Disposition", fmt.Sprintf("attachment; filename=\"%s\"", filename))
	w.WriteHeader(http.StatusOK)
	_, _ = w.Write(csvBytes)
}
