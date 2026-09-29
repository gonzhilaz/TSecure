package main

import (
	"context"
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
		persistFile = "data_store.json"
	}

	log.Printf("[TELKOMSECURE] Initializing TelkomSecure Backend Engine...")
	store := storage.NewStorage(persistFile)
	subService := service.NewSubscriptionService(store)
	broker := handler.NewEventBroker()
	apiHandler := handler.NewAPIHandler(subService, broker)

	mux := http.NewServeMux()

	// Health check
	mux.HandleFunc("/health", apiHandler.Health)

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
		log.Printf("[TELKOMSECURE] Endpoints ready: /health, /api/v1/subscription/check, /api/v1/telemetry/events, /api/v1/dashboard/stream")
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
