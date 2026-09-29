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

	register := func(pattern string, fn http.HandlerFunc) {
		mux.HandleFunc(pattern, fn)
		if strings.HasPrefix(pattern, "/api") {
			mux.HandleFunc(strings.TrimPrefix(pattern, "/api"), fn)
		} else {
			mux.HandleFunc("/api"+pattern, fn)
		}
	}

	// Root status check
	rootHandler := func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusOK)
		_ = json.NewEncoder(w).Encode(map[string]any{
			"status":  "UP",
			"service": "TelkomSecure Go Backend",
			"version": "2.4",
			"path":    r.URL.Path,
		})
	}
	mux.HandleFunc("/", rootHandler)
	mux.HandleFunc("/api", rootHandler)
	mux.HandleFunc("/api/", rootHandler)

	// Health check
	register("/health", apiHandler.Health)

	// Mobile App Endpoints
	register("/api/v1/auth/request-otp", apiHandler.RequestOtp)
	register("/api/v1/auth/verify-otp", apiHandler.VerifyOtp)
	register("/api/v1/auth/activate-license", apiHandler.ActivateLicense)
	register("/api/v1/subscription/check", apiHandler.CheckActivePeriod)
	register("/api/v1/telemetry/events", apiHandler.ReportTelemetry)

	// NDP / MyTelkomsel Simulation Endpoints
	register("/api/v1/ndp/simulate-purchase", apiHandler.SimulateNdpPurchase)
	register("/api/v1/ndp/simulate-expire", apiHandler.SimulateNdpExpire)

	// SOC Dashboard Endpoints
	register("/api/v1/dashboard/stats", apiHandler.GetDashboardStats)
	register("/api/v1/dashboard/subscribers", apiHandler.ListSubscribers)
	register("/api/v1/dashboard/threats", apiHandler.ListThreats)
	mux.Handle("/api/v1/dashboard/stream", broker)
	mux.Handle("/v1/dashboard/stream", broker)

	// Admin & Customer Care Helpdesk Endpoints
	register("/api/v1/admin/subscribers/search", apiHandler.SearchSubscribers)
	register("/api/v1/admin/resend-code", apiHandler.ResendActivationCode)
	register("/api/v1/admin/resync-license", apiHandler.ResyncLicense)
	register("/api/v1/admin/migrate-device", apiHandler.MigrateDevice)

	httpHandler = apiHandler.CORSMiddleware(mux)
}

// Handler is the entrypoint for Vercel Serverless Functions in Go
func Handler(w http.ResponseWriter, r *http.Request) {
	once.Do(initServer)

	target := r.URL.Path
	if matched := r.Header.Get("x-matched-path"); matched != "" && !strings.Contains(matched, "index.go") {
		target = matched
	} else if fwd := r.Header.Get("x-forwarded-uri"); fwd != "" {
		target = strings.SplitN(fwd, "?", 2)[0]
	}

	target = strings.TrimPrefix(target, "/api/index.go")
	target = strings.TrimPrefix(target, "/api/index")
	if !strings.HasPrefix(target, "/") {
		target = "/" + target
	}
	r.URL.Path = target

	httpHandler.ServeHTTP(w, r)
}
