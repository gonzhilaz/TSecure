package handler

import (
	"encoding/json"
	"net/http"
	"strconv"
	"time"

	"telkomsecure-backend/internal/model"
	"telkomsecure-backend/internal/service"
)

type APIHandler struct {
	svc    *service.SubscriptionService
	broker *EventBroker
}

func NewAPIHandler(svc *service.SubscriptionService, broker *EventBroker) *APIHandler {
	return &APIHandler{
		svc:    svc,
		broker: broker,
	}
}

func (h *APIHandler) Health(w http.ResponseWriter, r *http.Request) {
	writeJSON(w, http.StatusOK, map[string]any{
		"status":  "healthy",
		"service": "TelkomSecure Enterprise Backend",
		"time":    time.Now().Format(time.RFC3339),
	})
}

func (h *APIHandler) CheckActivePeriod(w http.ResponseWriter, r *http.Request) {
	msisdn := r.URL.Query().Get("msisdn")
	mobileID := r.URL.Query().Get("mobile_id")
	deviceModel := r.URL.Query().Get("device_model")
	osVersion := r.URL.Query().Get("os_version")

	if msisdn == "" {
		writeJSON(w, http.StatusBadRequest, map[string]string{"error": "msisdn query parameter is required"})
		return
	}

	resp := h.svc.CheckActivePeriod(msisdn, mobileID, deviceModel, osVersion)
	writeJSON(w, http.StatusOK, resp)
}

func (h *APIHandler) ReportTelemetry(w http.ResponseWriter, r *http.Request) {
	var event model.ThreatEvent
	if err := json.NewDecoder(r.Body).Decode(&event); err != nil {
		writeJSON(w, http.StatusBadRequest, map[string]string{"error": "invalid json payload: " + err.Error()})
		return
	}

	h.svc.RecordThreat(event)
	h.broker.Broadcast("threat_alert", event)

	writeJSON(w, http.StatusCreated, map[string]any{
		"status":  "recorded",
		"id":      event.ID,
		"message": "Threat telemetry safely logged and forwarded to SOC.",
	})
}

func (h *APIHandler) SimulateNdpPurchase(w http.ResponseWriter, r *http.Request) {
	var req model.NdpOrderRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		writeJSON(w, http.StatusBadRequest, map[string]string{"error": "invalid json payload: " + err.Error()})
		return
	}

	if req.MSISDN == "" {
		writeJSON(w, http.StatusBadRequest, map[string]string{"error": "msisdn is required"})
		return
	}
	if req.PackageName == "" {
		req.PackageName = "Telkomsel Secure Guard 30 Hari"
	}
	if req.DurationDays <= 0 {
		req.DurationDays = 30
	}

	sub := h.svc.SimulatePurchase(req)
	h.broker.Broadcast("subscriber_updated", sub)

	writeJSON(w, http.StatusOK, map[string]any{
		"status":     "success",
		"message":    "NDP purchase simulated successfully.",
		"subscriber": sub,
	})
}

func (h *APIHandler) SimulateNdpExpire(w http.ResponseWriter, r *http.Request) {
	msisdn := r.URL.Query().Get("msisdn")
	if msisdn == "" {
		var body struct {
			MSISDN string `json:"msisdn"`
		}
		_ = json.NewDecoder(r.Body).Decode(&body)
		msisdn = body.MSISDN
	}

	if msisdn == "" {
		writeJSON(w, http.StatusBadRequest, map[string]string{"error": "msisdn is required"})
		return
	}

	sub, found := h.svc.SimulateExpire(msisdn)
	if !found {
		writeJSON(w, http.StatusNotFound, map[string]string{"error": "subscriber not found"})
		return
	}

	h.broker.Broadcast("subscriber_updated", sub)
	writeJSON(w, http.StatusOK, map[string]any{
		"status":     "expired",
		"message":    "Subscriber forced into expired state for POC testing.",
		"subscriber": sub,
	})
}

// ResendActivationCode resolves SMS delivery failure.
func (h *APIHandler) ResendActivationCode(w http.ResponseWriter, r *http.Request) {
	var req struct {
		MSISDN string `json:"msisdn"`
	}
	_ = json.NewDecoder(r.Body).Decode(&req)
	if req.MSISDN == "" {
		req.MSISDN = r.URL.Query().Get("msisdn")
	}
	if req.MSISDN == "" {
		writeJSON(w, http.StatusBadRequest, map[string]string{"error": "msisdn is required"})
		return
	}

	sub, err := h.svc.ResendActivationCode(req.MSISDN)
	if err != nil {
		writeJSON(w, http.StatusNotFound, map[string]string{"error": err.Error()})
		return
	}

	h.broker.Broadcast("subscriber_updated", sub)
	writeJSON(w, http.StatusOK, model.AdminActionResponse{
		Status:     "success",
		Message:    "Kode aktivasi berhasil dikirimkan ulang via SMS Gateway Telkomsel ke " + sub.MSISDN,
		Subscriber: sub,
	})
}

// ResyncLicense resolves NDP vs Kaspersky license active period desync.
func (h *APIHandler) ResyncLicense(w http.ResponseWriter, r *http.Request) {
	var req struct {
		MSISDN string `json:"msisdn"`
	}
	_ = json.NewDecoder(r.Body).Decode(&req)
	if req.MSISDN == "" {
		req.MSISDN = r.URL.Query().Get("msisdn")
	}
	if req.MSISDN == "" {
		writeJSON(w, http.StatusBadRequest, map[string]string{"error": "msisdn is required"})
		return
	}

	sub, err := h.svc.ResyncKasperskyLicense(req.MSISDN)
	if err != nil {
		writeJSON(w, http.StatusNotFound, map[string]string{"error": err.Error()})
		return
	}

	h.broker.Broadcast("subscriber_updated", sub)
	writeJSON(w, http.StatusOK, model.AdminActionResponse{
		Status:     "success",
		Message:    "Masa aktif lisensi Kaspersky berhasil disinkronkan kembali penuh sesuai catatan NDP Telkomsel!",
		Subscriber: sub,
	})
}

// MigrateDevice allows customer to swap devices mid-month without license exhaustion.
func (h *APIHandler) MigrateDevice(w http.ResponseWriter, r *http.Request) {
	var req model.DeviceMigrationRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		writeJSON(w, http.StatusBadRequest, map[string]string{"error": "invalid payload: " + err.Error()})
		return
	}
	if req.MSISDN == "" {
		writeJSON(w, http.StatusBadRequest, map[string]string{"error": "msisdn is required"})
		return
	}

	sub, err := h.svc.MigrateDevice(req)
	if err != nil {
		writeJSON(w, http.StatusNotFound, map[string]string{"error": err.Error()})
		return
	}

	h.broker.Broadcast("subscriber_updated", sub)
	writeJSON(w, http.StatusOK, model.AdminActionResponse{
		Status:     "success",
		Message:    "Device binding berhasil dimigrasikan ke perangkat baru tanpa menghabiskan kuota B2B ekstra.",
		Subscriber: sub,
	})
}

// SearchSubscribers filters by query, status, and risk level.
func (h *APIHandler) SearchSubscribers(w http.ResponseWriter, r *http.Request) {
	query := r.URL.Query().Get("q")
	status := r.URL.Query().Get("status")
	risk := r.URL.Query().Get("risk")

	subs := h.svc.SearchSubscribers(query, status, risk)
	writeJSON(w, http.StatusOK, subs)
}

func (h *APIHandler) GetDashboardStats(w http.ResponseWriter, r *http.Request) {
	stats := h.svc.GetStats()
	writeJSON(w, http.StatusOK, stats)
}

func (h *APIHandler) ListSubscribers(w http.ResponseWriter, r *http.Request) {
	subs := h.svc.ListSubscribers()
	writeJSON(w, http.StatusOK, subs)
}

func (h *APIHandler) ListThreats(w http.ResponseWriter, r *http.Request) {
	limit := 20
	if lStr := r.URL.Query().Get("limit"); lStr != "" {
		if l, err := strconv.Atoi(lStr); err == nil && l > 0 {
			limit = l
		}
	}
	threats := h.svc.ListThreats(limit)
	writeJSON(w, http.StatusOK, threats)
}

func (h *APIHandler) RequestOtp(w http.ResponseWriter, r *http.Request) {
	var req model.OtpRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		writeJSON(w, http.StatusBadRequest, map[string]string{"error": "invalid json payload: " + err.Error()})
		return
	}
	if req.MSISDN == "" {
		writeJSON(w, http.StatusBadRequest, map[string]string{"error": "msisdn is required"})
		return
	}

	resp, err := h.svc.RequestOtp(req.MSISDN, req.MobileID)
	if err != nil {
		writeJSON(w, http.StatusBadRequest, map[string]string{"error": err.Error()})
		return
	}

	writeJSON(w, http.StatusOK, resp)
}

func (h *APIHandler) VerifyOtp(w http.ResponseWriter, r *http.Request) {
	var req model.VerifyOtpRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		writeJSON(w, http.StatusBadRequest, map[string]string{"error": "invalid json payload: " + err.Error()})
		return
	}
	if req.MSISDN == "" || req.OtpCode == "" {
		writeJSON(w, http.StatusBadRequest, map[string]string{"error": "msisdn and otp_code are required"})
		return
	}

	resp, err := h.svc.VerifyOtp(req.MSISDN, req.MobileID, req.OtpCode)
	if err != nil {
		writeJSON(w, http.StatusInternalServerError, map[string]string{"error": err.Error()})
		return
	}
	if !resp.Success {
		writeJSON(w, http.StatusUnauthorized, resp)
		return
	}

	writeJSON(w, http.StatusOK, resp)
}

func (h *APIHandler) ActivateLicense(w http.ResponseWriter, r *http.Request) {
	var req model.LicenseActivationRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		writeJSON(w, http.StatusBadRequest, map[string]string{"error": "invalid json payload: " + err.Error()})
		return
	}
	if req.MSISDN == "" {
		writeJSON(w, http.StatusBadRequest, map[string]string{"error": "msisdn is required"})
		return
	}

	resp, err := h.svc.ActivateLicense(req)
	if err != nil {
		writeJSON(w, http.StatusInternalServerError, map[string]string{"error": err.Error()})
		return
	}

	writeJSON(w, http.StatusOK, resp)
}

func (h *APIHandler) CORSMiddleware(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Access-Control-Allow-Origin", "*")
		w.Header().Set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
		w.Header().Set("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With")

		if r.Method == http.MethodOptions {
			w.WriteHeader(http.StatusOK)
			return
		}
		next.ServeHTTP(w, r)
	})
}

func writeJSON(w http.ResponseWriter, code int, payload any) {
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(code)
	_ = json.NewEncoder(w).Encode(payload)
}
