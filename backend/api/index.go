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
	once       sync.Once
	apiHandler *handler.APIHandler
	broker     *handler.EventBroker
)

func initServer() {
	persistFile := os.Getenv("PERSIST_FILE")
	if persistFile == "" {
		persistFile = "/tmp/data_store.json"
	}

	store := storage.NewStorage(persistFile)
	subService := service.NewSubscriptionService(store)
	broker = handler.NewEventBroker()
	apiHandler = handler.NewAPIHandler(subService, broker)
}

// Handler is the entrypoint for Vercel Serverless Functions in Go
func Handler(w http.ResponseWriter, r *http.Request) {
	// CORS Headers
	w.Header().Set("Access-Control-Allow-Origin", "*")
	w.Header().Set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
	w.Header().Set("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With")

	if r.Method == http.MethodOptions {
		w.WriteHeader(http.StatusOK)
		return
	}

	once.Do(initServer)

	// Resolve the real request path from Vercel headers or URL
	path := r.Header.Get("x-matched-path")
	if path == "" || path == "/api" || path == "/api/index.go" {
		path = r.Header.Get("x-forwarded-uri")
	}
	if path == "" {
		path = r.URL.Path
	}

	// Strip query string if present in path
	if idx := strings.Index(path, "?"); idx != -1 {
		path = path[:idx]
	}

	// Normalize path
	path = strings.TrimPrefix(path, "/api/index.go")
	path = strings.TrimPrefix(path, "/api/index")
	if path != "/" {
		path = strings.TrimSuffix(path, "/")
	}
	if path == "" {
		path = "/"
	}

	// Direct Dispatcher (bypasses any ServeMux matching issues)
	switch path {
	case "/", "/api":
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusOK)
		_ = json.NewEncoder(w).Encode(map[string]any{
			"status":   "UP",
			"service":  "TelkomSecure Go Backend",
			"version":  "2.4",
			"platform": "Vercel Go Serverless",
			"path":     path,
		})
		return

	case "/health", "/api/health":
		apiHandler.Health(w, r)
		return

	case "/api/v1/subscription/check", "/v1/subscription/check":
		apiHandler.CheckActivePeriod(w, r)
		return

	case "/api/v1/auth/request-otp", "/v1/auth/request-otp":
		apiHandler.RequestOtp(w, r)
		return

	case "/api/v1/auth/verify-otp", "/v1/auth/verify-otp":
		apiHandler.VerifyOtp(w, r)
		return

	case "/api/v1/auth/activate-license", "/v1/auth/activate-license":
		apiHandler.ActivateLicense(w, r)
		return

	case "/api/v1/telemetry/events", "/v1/telemetry/events":
		apiHandler.ReportTelemetry(w, r)
		return

	case "/api/v1/ndp/simulate-purchase", "/v1/ndp/simulate-purchase":
		apiHandler.SimulateNdpPurchase(w, r)
		return

	case "/api/v1/ndp/simulate-expire", "/v1/ndp/simulate-expire":
		apiHandler.SimulateNdpExpire(w, r)
		return

	case "/api/v1/dashboard/stats", "/v1/dashboard/stats":
		apiHandler.GetDashboardStats(w, r)
		return

	case "/api/v1/dashboard/subscribers", "/v1/dashboard/subscribers":
		apiHandler.ListSubscribers(w, r)
		return

	case "/api/v1/dashboard/threats", "/v1/dashboard/threats":
		apiHandler.ListThreats(w, r)
		return

	case "/api/v1/dashboard/stream", "/v1/dashboard/stream":
		broker.ServeHTTP(w, r)
		return

	case "/api/v1/admin/subscribers/search", "/v1/admin/subscribers/search":
		apiHandler.SearchSubscribers(w, r)
		return

	case "/api/v1/admin/resend-code", "/v1/admin/resend-code":
		apiHandler.ResendActivationCode(w, r)
		return

	case "/api/v1/admin/resync-license", "/v1/admin/resync-license":
		apiHandler.ResyncLicense(w, r)
		return

	case "/api/v1/admin/migrate-device", "/v1/admin/migrate-device":
		apiHandler.MigrateDevice(w, r)
		return

	default:
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusNotFound)
		_ = json.NewEncoder(w).Encode(map[string]any{
			"error":        "Not Found",
			"path":         path,
			"url_path":     r.URL.Path,
			"matched_path": r.Header.Get("x-matched-path"),
		})
	}
}
