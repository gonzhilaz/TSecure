package handler

import (
	"encoding/json"
	"net/http"
	"os"
	"path"
	"strings"
	"sync"

	"telkomsecure-backend/internal/handler"
	"telkomsecure-backend/internal/service"
	"telkomsecure-backend/internal/storage"
)

var (
	once        sync.Once
	httpHandler http.Handler
)

func initServer() {
	persistFile := os.Getenv("PERSIST_FILE")
	if persistFile == "" {
		persistFile = "/tmp/data_store.json"
	}

	store := storage.NewStorage(persistFile)
	subService := service.NewSubscriptionService(store)
	broker := handler.NewEventBroker()
	apiHandler := handler.NewAPIHandler(subService, broker)

	mux := http.NewServeMux()

	// Root & Status Check
	mux.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {
		if r.URL.Path == "/" || r.URL.Path == "/api" || r.URL.Path == "/api/" || r.URL.Path == "/api/index.go" || r.URL.Path == "/api/index" {
			w.Header().Set("Content-Type", "application/json")
			w.WriteHeader(http.StatusOK)
			_ = json.NewEncoder(w).Encode(map[string]any{
				"status":  "UP",
				"service": "TelkomSecure Go Backend",
				"version": "2.4.clean",
			})
			return
		}
		http.NotFound(w, r)
	})

	// Health check
	mux.HandleFunc("/health", apiHandler.Health)
	mux.HandleFunc("/api/health", apiHandler.Health)

	// Mobile App Endpoints
	mux.HandleFunc("/api/v1/auth/request-otp", apiHandler.RequestOtp)
	mux.HandleFunc("/api/v1/auth/verify-otp", apiHandler.VerifyOtp)
	mux.HandleFunc("/api/v1/auth/activate-license", apiHandler.ActivateLicense)
	mux.HandleFunc("/api/v1/subscription/check", apiHandler.CheckActivePeriod)
	mux.HandleFunc("/api/v1/telemetry/events", apiHandler.ReportTelemetry)
	mux.HandleFunc("/api/v1/app/check-update", apiHandler.CheckAppUpdate)
	mux.HandleFunc("/api/v1/app/download-latest", apiHandler.DownloadLatestApk)

	// NDP / MyTelkomsel Simulation Endpoints
	mux.HandleFunc("/api/v1/ndp/simulate-purchase", apiHandler.SimulateNdpPurchase)
	mux.HandleFunc("/api/v1/ndp/simulate-expire", apiHandler.SimulateNdpExpire)
	mux.HandleFunc("/api/v1/ndp/simulate-unactivated", apiHandler.SimulateNdpUnactivated)

	// SOC Dashboard Endpoints
	mux.HandleFunc("/api/v1/dashboard/stats", apiHandler.GetDashboardStats)
	mux.HandleFunc("/api/v1/dashboard/subscribers", apiHandler.ListSubscribers)
	mux.HandleFunc("/api/v1/dashboard/threats", apiHandler.ListThreats)
	mux.HandleFunc("/api/v1/dashboard/clear", apiHandler.ClearDashboardData)
	mux.HandleFunc("/api/v1/admin/clear-threats", apiHandler.ClearDashboardData)
	mux.Handle("/api/v1/dashboard/stream", broker)

	// Big Data Streaming Export Endpoints (Chunked Transfer)
	mux.HandleFunc("/api/v1/export/threats", apiHandler.ExportThreatsStream)
	mux.HandleFunc("/api/v1/export/subscribers", apiHandler.ExportSubscribersStream)
	mux.HandleFunc("/api/v1/export/phishing", apiHandler.ExportPhishingCSV)

	// Phishing Threat Intelligence Endpoints
	mux.HandleFunc("/api/v1/phishing/records", apiHandler.ListPhishing)
	mux.HandleFunc("/api/v1/phishing/stats", apiHandler.GetPhishingStats)
	mux.HandleFunc("/api/v1/phishing/status", apiHandler.UpdatePhishingStatus)

	// Admin & Customer Care Helpdesk Endpoints
	mux.HandleFunc("/api/v1/admin/subscribers/search", apiHandler.SearchSubscribers)
	mux.HandleFunc("/api/v1/admin/resend-code", apiHandler.ResendActivationCode)
	mux.HandleFunc("/api/v1/admin/resync-license", apiHandler.ResyncLicense)
	mux.HandleFunc("/api/v1/admin/migrate-device", apiHandler.MigrateDevice)

	// SOC Authentication & RBAC User Management Endpoints
	mux.HandleFunc("/api/v1/auth/soc/login", apiHandler.LoginSOC)
	mux.HandleFunc("/api/v1/auth/soc/logout", apiHandler.LogoutSOC)
	mux.HandleFunc("/api/v1/auth/soc/me", apiHandler.GetSOCMe)
	mux.HandleFunc("/api/v1/admin/operators", func(w http.ResponseWriter, r *http.Request) {
		if r.Method == http.MethodPost {
			apiHandler.CreateOperator(w, r)
		} else {
			apiHandler.ListOperators(w, r)
		}
	})
	mux.HandleFunc("/api/v1/admin/operators/status", apiHandler.ToggleOperatorStatus)
	mux.HandleFunc("/api/v1/admin/audit-logs", apiHandler.ListAuditLogs)

	// External Server Ingestion Gateway Endpoints (NDP, Proxy, Syslog)
	mux.HandleFunc("/api/v1/ingest/ndp", apiHandler.IngestNdpBilling)
	mux.HandleFunc("/api/v1/ingest/proxy-threats", apiHandler.IngestProxyThreats)
	mux.HandleFunc("/api/v1/admin/ingest/keys", func(w http.ResponseWriter, r *http.Request) {
		if r.Method == http.MethodPost {
			apiHandler.CreateApiKey(w, r)
		} else {
			apiHandler.ListApiKeys(w, r)
		}
	})
	mux.HandleFunc("/api/v1/admin/ingest/dlq", apiHandler.ListDLQ)
	mux.HandleFunc("/api/v1/admin/ingest/dlq/replay", apiHandler.ReplayDLQ)
	mux.HandleFunc("/api/v1/admin/ingest/dlq/discard", apiHandler.DiscardDLQ)

	// Database Maintenance & Lifecycle Endpoints
	mux.HandleFunc("/api/v1/admin/maintenance/stats", apiHandler.GetMaintenanceStats)
	mux.HandleFunc("/api/v1/admin/maintenance/backup", apiHandler.ExportSnapshot)
	mux.HandleFunc("/api/v1/admin/maintenance/restore", apiHandler.RestoreSnapshot)
	mux.HandleFunc("/api/v1/admin/maintenance/retention/run", apiHandler.RunRetention)

	httpHandler = apiHandler.CORSMiddleware(mux)
}

// resolvePath normalizes incoming paths from Vercel Serverless Function rewrites and headers
func resolvePath(r *http.Request) string {
	var target string

	// 1. Check x-forwarded-uri (in Vercel, this reliably holds the original client request URI)
	if fwd := r.Header.Get("x-forwarded-uri"); fwd != "" {
		parts := strings.SplitN(fwd, "?", 2)
		cleanFwd := parts[0]
		if cleanFwd != "" && cleanFwd != "/api/index.go" && cleanFwd != "/api/index" && cleanFwd != "/api" {
			target = cleanFwd
			if len(parts) > 1 && r.URL.RawQuery == "" {
				r.URL.RawQuery = parts[1]
			}
		}
	}

	// 2. Check "path" query parameter (from rewrite destination or direct invocation)
	if target == "" {
		if p := r.URL.Query().Get("path"); p != "" {
			target = p
		}
	}

	// 3. Check x-invoke-path
	if target == "" {
		if invoke := r.Header.Get("x-invoke-path"); invoke != "" && invoke != "/api/index.go" && invoke != "/api/index" && invoke != "/api" {
			target = invoke
		}
	}

	// 4. Check r.URL.Path
	if target == "" {
		if p := r.URL.Path; p != "" && p != "/api/index.go" && p != "/api/index" && p != "/api" {
			target = p
		}
	}

	if target == "" {
		target = "/"
	}

	if !strings.HasPrefix(target, "/") {
		target = "/" + target
	}

	cleaned := path.Clean(target)

	// If request starts with /v1/, normalize to /api/v1/
	if strings.HasPrefix(cleaned, "/v1/") {
		cleaned = "/api" + cleaned
	}

	return cleaned
}

// Handler is the entrypoint for Vercel Serverless Functions in Go
func Handler(w http.ResponseWriter, r *http.Request) {
	once.Do(initServer)

	r.URL.Path = resolvePath(r)

	httpHandler.ServeHTTP(w, r)
}
