package handler

import (
	"encoding/json"
	"io"
	"net/http"
	"strings"

	"telkomsecure-backend/internal/model"
)

func getApiKeyFromRequest(r *http.Request) string {
	if key := r.Header.Get("X-TelkomSecure-Key"); key != "" {
		return key
	}
	authHeader := r.Header.Get("Authorization")
	if strings.HasPrefix(authHeader, "Bearer ") {
		return strings.TrimPrefix(authHeader, "Bearer ")
	}
	return ""
}

// IngestNdpBilling processes server-to-server subscription updates from NDP.
func (h *APIHandler) IngestNdpBilling(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		writeJSON(w, http.StatusMethodNotAllowed, map[string]string{"error": "method not allowed"})
		return
	}

	rawBytes, _ := io.ReadAll(r.Body)
	rawStr := string(rawBytes)

	// 1. Authorize API Key
	apiKey := getApiKeyFromRequest(r)
	keyObj, ok := h.svc.VerifyApiKey(apiKey)
	if !ok {
		dlq := h.svc.RecordDeadLetter("NDP_BILLING", rawStr, "Unauthorized: Invalid or inactive API key", "CRITICAL")
		writeJSON(w, http.StatusUnauthorized, map[string]any{
			"error":  "invalid API key",
			"dlq_id": dlq.ID,
		})
		return
	}

	// 2. Parse payload
	var payload model.NdpIngestPayload
	if err := json.Unmarshal(rawBytes, &payload); err != nil {
		dlq := h.svc.RecordDeadLetter("NDP_BILLING", rawStr, "Schema Parse Error: "+err.Error(), "HIGH")
		writeJSON(w, http.StatusBadRequest, map[string]any{
			"error":  "invalid JSON schema: " + err.Error(),
			"dlq_id": dlq.ID,
		})
		return
	}

	if payload.MSISDN == "" {
		dlq := h.svc.RecordDeadLetter("NDP_BILLING", rawStr, "Validation Error: MSISDN cannot be empty", "MEDIUM")
		writeJSON(w, http.StatusBadRequest, map[string]any{
			"error":  "msisdn is required",
			"dlq_id": dlq.ID,
		})
		return
	}

	sub, err := h.svc.ProcessNdpIngest(payload)
	if err != nil {
		dlq := h.svc.RecordDeadLetter("NDP_BILLING", rawStr, "Processing Error: "+err.Error(), "HIGH")
		writeJSON(w, http.StatusInternalServerError, map[string]any{
			"error":  err.Error(),
			"dlq_id": dlq.ID,
		})
		return
	}

	writeJSON(w, http.StatusOK, map[string]any{
		"success":       true,
		"message":       "NDP billing event processed successfully",
		"key_name":      keyObj.Name,
		"subscriber_id": sub.ID,
		"msisdn":        sub.MSISDN,
		"status":        sub.ActivationStatus,
	})
}

// IngestProxyThreats processes bulk telemetry from Telkom proxy/WAF servers.
func (h *APIHandler) IngestProxyThreats(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		writeJSON(w, http.StatusMethodNotAllowed, map[string]string{"error": "method not allowed"})
		return
	}

	rawBytes, _ := io.ReadAll(r.Body)
	rawStr := string(rawBytes)

	apiKey := getApiKeyFromRequest(r)
	keyObj, ok := h.svc.VerifyApiKey(apiKey)
	if !ok {
		dlq := h.svc.RecordDeadLetter("TELCO_PROXY", rawStr, "Unauthorized: Invalid or inactive API key", "CRITICAL")
		writeJSON(w, http.StatusUnauthorized, map[string]any{
			"error":  "invalid API key",
			"dlq_id": dlq.ID,
		})
		return
	}

	var payload model.ProxyThreatIngestPayload
	if err := json.Unmarshal(rawBytes, &payload); err != nil {
		dlq := h.svc.RecordDeadLetter("TELCO_PROXY", rawStr, "Schema Parse Error: "+err.Error(), "HIGH")
		writeJSON(w, http.StatusBadRequest, map[string]any{
			"error":  "invalid JSON schema: " + err.Error(),
			"dlq_id": dlq.ID,
		})
		return
	}

	count, err := h.svc.ProcessProxyThreatIngest(payload)
	if err != nil {
		dlq := h.svc.RecordDeadLetter("TELCO_PROXY", rawStr, "Processing Error: "+err.Error(), "HIGH")
		writeJSON(w, http.StatusInternalServerError, map[string]any{
			"error":  err.Error(),
			"dlq_id": dlq.ID,
		})
		return
	}

	writeJSON(w, http.StatusOK, map[string]any{
		"success":        true,
		"gateway_id":     payload.GatewayID,
		"key_name":       keyObj.Name,
		"threats_ingest": count,
	})
}

// ListApiKeys lists external server integration credentials.
func (h *APIHandler) ListApiKeys(w http.ResponseWriter, r *http.Request) {
	keys := h.svc.ListApiKeys()
	writeJSON(w, http.StatusOK, map[string]any{
		"success": true,
		"data":    keys,
		"count":   len(keys),
	})
}

// CreateApiKey provisions a new external integration key.
func (h *APIHandler) CreateApiKey(w http.ResponseWriter, r *http.Request) {
	var body struct {
		Name   string `json:"name"`
		Source string `json:"source"`
	}
	if err := json.NewDecoder(r.Body).Decode(&body); err != nil {
		writeJSON(w, http.StatusBadRequest, map[string]string{"error": "payload tidak valid"})
		return
	}

	key, err := h.svc.CreateApiKey(body.Name, body.Source)
	if err != nil {
		writeJSON(w, http.StatusInternalServerError, map[string]string{"error": err.Error()})
		return
	}

	writeJSON(w, http.StatusCreated, map[string]any{
		"success": true,
		"data":    key,
	})
}

// ListDLQ returns dead letter error queue records for mitigation.
func (h *APIHandler) ListDLQ(w http.ResponseWriter, r *http.Request) {
	status := r.URL.Query().Get("status")
	records := h.svc.ListDLQRecords(status)
	writeJSON(w, http.StatusOK, map[string]any{
		"success": true,
		"data":    records,
		"count":   len(records),
	})
}

// ReplayDLQ replays or marks resolved a failed ingestion.
func (h *APIHandler) ReplayDLQ(w http.ResponseWriter, r *http.Request) {
	var body struct {
		ID    string `json:"id"`
		Notes string `json:"notes"`
	}
	if err := json.NewDecoder(r.Body).Decode(&body); err != nil {
		writeJSON(w, http.StatusBadRequest, map[string]string{"error": "payload tidak valid"})
		return
	}

	rec, err := h.svc.ResolveDLQRecord(body.ID, "REPLAYED", body.Notes)
	if err != nil {
		writeJSON(w, http.StatusNotFound, map[string]string{"error": err.Error()})
		return
	}

	writeJSON(w, http.StatusOK, map[string]any{
		"success": true,
		"data":    rec,
	})
}

// DiscardDLQ marks a poison message as discarded.
func (h *APIHandler) DiscardDLQ(w http.ResponseWriter, r *http.Request) {
	var body struct {
		ID    string `json:"id"`
		Notes string `json:"notes"`
	}
	if err := json.NewDecoder(r.Body).Decode(&body); err != nil {
		writeJSON(w, http.StatusBadRequest, map[string]string{"error": "payload tidak valid"})
		return
	}

	rec, err := h.svc.ResolveDLQRecord(body.ID, "DISCARDED", body.Notes)
	if err != nil {
		writeJSON(w, http.StatusNotFound, map[string]string{"error": err.Error()})
		return
	}

	writeJSON(w, http.StatusOK, map[string]any{
		"success": true,
		"data":    rec,
	})
}
