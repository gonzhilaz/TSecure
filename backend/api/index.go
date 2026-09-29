package handler

import (
	"encoding/json"
	"net/http"
	"os"
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

	// Root status check
	mux.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusOK)
		_ = json.NewEncoder(w).Encode(map[string]any{
			"status":   "UP",
			"service":  "TelkomSecure Go Backend",
			"version":  "2.4",
			"path":     r.URL.Path,
			"endpoints": []string{
				"/health",
				"/api/v1/subscription/check",
				"/api/v1/telemetry/events",
				"/api/v1/dashboard/stats",
				"/api/v1/dashboard/subscribers",
				"/api/v1/dashboard/threats",
				"/api/v1/ndp/simulate-purchase",
			},
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

	// NDP / MyTelkomsel Simulation Endpoints
	mux.HandleFunc("/api/v1/ndp/simulate-purchase", apiHandler.SimulateNdpPurchase)
	mux.HandleFunc("/api/v1/ndp/simulate-expire", apiHandler.SimulateNdpExpire)

	// SOC Dashboard Endpoints
	mux.HandleFunc("/api/v1/dashboard/stats", apiHandler.GetDashboardStats)
	mux.HandleFunc("/api/v1/dashboard/subscribers", apiHandler.ListSubscribers)
	mux.HandleFunc("/api/v1/dashboard/threats", apiHandler.ListThreats)
	mux.Handle("/api/v1/dashboard/stream", broker)

	// Admin & Customer Care Helpdesk Endpoints
	mux.HandleFunc("/api/v1/admin/subscribers/search", apiHandler.SearchSubscribers)
	mux.HandleFunc("/api/v1/admin/resend-code", apiHandler.ResendActivationCode)
	mux.HandleFunc("/api/v1/admin/resync-license", apiHandler.ResyncLicense)
	mux.HandleFunc("/api/v1/admin/migrate-device", apiHandler.MigrateDevice)

	httpHandler = apiHandler.CORSMiddleware(mux)
}

// Handler is the entrypoint for Vercel Serverless Functions in Go
func Handler(w http.ResponseWriter, r *http.Request) {
	once.Do(initServer)

	// Determine original requested path
	path := r.URL.Path
	if qPath := r.URL.Query().Get("path"); qPath != "" {
		path = qPath
	} else if matched := r.Header.Get("x-matched-path"); matched != "" {
		path = matched
	} else if fwd := r.Header.Get("x-forwarded-uri"); fwd != "" {
		parts := strings.SplitN(fwd, "?", 2)
		path = parts[0]
	}

	// Normalize prefix
	path = strings.TrimPrefix(path, "/api/index.go")
	path = strings.TrimPrefix(path, "/api/index")
	if !strings.HasPrefix(path, "/") {
		path = "/" + path
	}
	r.URL.Path = path

	httpHandler.ServeHTTP(w, r)
}
