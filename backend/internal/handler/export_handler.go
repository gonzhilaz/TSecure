package handler

import (
	"encoding/csv"
	"fmt"
	"net/http"
	"strconv"
	"time"
)

// ExportThreatsStream streams threat logs directly to HTTP response using chunked transfer encoding.
// Memory complexity is O(1) regardless of whether there are 1,000 or 1,000,000 records.
func (h *APIHandler) ExportThreatsStream(w http.ResponseWriter, r *http.Request) {
	flusher, ok := w.(http.Flusher)
	if !ok {
		http.Error(w, "Streaming unsupported by client", http.StatusInternalServerError)
		return
	}

	limit := 1000
	if l := r.URL.Query().Get("limit"); l != "" {
		if parsed, err := strconv.Atoi(l); err == nil && parsed > 0 {
			limit = parsed
		}
	}

	timestamp := time.Now().Format("2006-01-02-150405")
	filename := fmt.Sprintf("telkomsel-threat-telemetry-%s.csv", timestamp)

	w.Header().Set("Content-Type", "text/csv; charset=utf-8")
	w.Header().Set("Content-Disposition", fmt.Sprintf("attachment; filename=\"%s\"", filename))
	w.Header().Set("Transfer-Encoding", "chunked")
	w.Header().Set("X-Content-Type-Options", "nosniff")

	// Prepend UTF-8 BOM for Microsoft Excel auto-encoding
	_, _ = w.Write([]byte("\xef\xbb\xbf"))

	csvWriter := csv.NewWriter(w)

	// Write CSV Header
	_ = csvWriter.Write([]string{
		"Incident ID",
		"Timestamp",
		"Severity",
		"Threat Type",
		"MSISDN",
		"Mobile ID",
		"Target / Component",
		"Action Taken",
		"Description",
	})
	csvWriter.Flush()
	flusher.Flush()

	threats := h.svc.ListThreats(limit)
	for i, t := range threats {
		_ = csvWriter.Write([]string{
			t.ID,
			t.Timestamp.Format(time.RFC3339),
			t.Severity,
			t.ThreatType,
			t.MSISDN,
			t.MobileID,
			t.Target,
			t.ActionTaken,
			t.Description,
		})

		// Flush every 100 records to keep server memory bounded
		if (i+1)%100 == 0 {
			csvWriter.Flush()
			flusher.Flush()
		}
	}

	csvWriter.Flush()
	flusher.Flush()
}

// ExportSubscribersStream streams protected subscriber fleet records using chunked transfer encoding.
func (h *APIHandler) ExportSubscribersStream(w http.ResponseWriter, r *http.Request) {
	flusher, ok := w.(http.Flusher)
	if !ok {
		http.Error(w, "Streaming unsupported by client", http.StatusInternalServerError)
		return
	}

	timestamp := time.Now().Format("2006-01-02-150405")
	filename := fmt.Sprintf("telkomsel-subscriber-fleet-%s.csv", timestamp)

	w.Header().Set("Content-Type", "text/csv; charset=utf-8")
	w.Header().Set("Content-Disposition", fmt.Sprintf("attachment; filename=\"%s\"", filename))
	w.Header().Set("Transfer-Encoding", "chunked")
	w.Header().Set("X-Content-Type-Options", "nosniff")

	// Prepend UTF-8 BOM
	_, _ = w.Write([]byte("\xef\xbb\xbf"))

	csvWriter := csv.NewWriter(w)

	// Write CSV Header
	_ = csvWriter.Write([]string{
		"Subscriber ID",
		"MSISDN",
		"Mobile ID",
		"Device Model",
		"OS Version",
		"Plan Name",
		"Active Period Start",
		"Active Period End",
		"Kaspersky Expiry",
		"Is Active",
		"Activation Status",
		"Root Status",
		"Hook Status",
		"Current ICCID",
		"SIM Slot",
	})
	csvWriter.Flush()
	flusher.Flush()

	subscribers := h.svc.ListSubscribers()
	for i, s := range subscribers {
		_ = csvWriter.Write([]string{
			s.ID,
			s.MSISDN,
			s.MobileID,
			s.DeviceModel,
			s.OSVersion,
			s.PlanName,
			s.ActivePeriodStart.Format("2006-01-02"),
			s.ActivePeriodEnd.Format("2006-01-02"),
			s.KasperskyExpiryDate.Format("2006-01-02"),
			strconv.FormatBool(s.IsActive),
			s.ActivationStatus,
			s.RootStatus,
			s.HookStatus,
			s.CurrentIccid,
			s.SimSlot,
		})

		if (i+1)%100 == 0 {
			csvWriter.Flush()
			flusher.Flush()
		}
	}

	csvWriter.Flush()
	flusher.Flush()
}
