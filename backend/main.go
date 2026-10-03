package main

import (
	"context"
	"encoding/json"
	"log"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	"telkomsecure-backend/internal/handler"
	"telkomsecure-backend/internal/service"
	"telkomsecure-backend/internal/storage"
)

func main() {
	port := os.Getenv("PORT")
	if port == "" {
		port = "8080"
	}

	persistFile := os.Getenv("PERSIST_FILE")
	if persistFile == "" {
		if _, err := os.Stat("data_store.json"); err == nil {
			persistFile = "data_store.json"
		} else {
			persistFile = "/tmp/data_store.json"
		}
	}

	log.Printf("[TELKOMSECURE] Initializing TelkomSecure Backend Engine...")
	store := storage.NewStorage(persistFile)
	subService := service.NewSubscriptionService(store)
	broker := handler.NewEventBroker()
	apiHandler := handler.NewAPIHandler(subService, broker)

	mux := http.NewServeMux()

	// Root status check
	mux.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {
		if r.URL.Path != "/" {
			http.NotFound(w, r)
			return
		}
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusOK)
		_ = json.NewEncoder(w).Encode(map[string]any{
			"status":   "UP",
			"service":  "TelkomSecure Go Backend",
			"version":  "2.4",
			"platform": "Vercel Go Framework",
		})
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

	corsHandler := apiHandler.CORSMiddleware(mux)

	srv := &http.Server{
		Addr:         ":" + port,
		Handler:      corsHandler,
		ReadTimeout:  15 * time.Second,
		WriteTimeout: 0, // 0 for SSE streaming support
		IdleTimeout:  60 * time.Second,
	}

	stopChan := make(chan os.Signal, 1)
	signal.Notify(stopChan, os.Interrupt, syscall.SIGTERM)

	go func() {
		log.Printf("[TELKOMSECURE] Server listening on port :%s", port)
		if err := srv.ListenAndServe(); err != nil && err != http.ErrServerClosed {
			log.Fatalf("[TELKOMSECURE] Server fatal error: %v", err)
		}
	}()

	<-stopChan
	log.Println("[TELKOMSECURE] Shutting down gracefully...")

	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()

	if err := srv.Shutdown(ctx); err != nil {
		log.Printf("[TELKOMSECURE] Shutdown error: %v", err)
	}
	log.Println("[TELKOMSECURE] Server stopped.")
}
