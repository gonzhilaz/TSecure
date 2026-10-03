package handler

import (
	"net/http/httptest"
	"testing"
)

func TestResolvePath(t *testing.T) {
	tests := []struct {
		name         string
		reqURL       string
		fwdURI       string
		invokePath   string
		expectedPath string
		expectedQ    string
	}{
		{
			name:         "Root URL",
			reqURL:       "http://example.com/api/index.go",
			fwdURI:       "/",
			expectedPath: "/",
		},
		{
			name:         "Health check via forwarded URI",
			reqURL:       "http://example.com/api/index.go",
			fwdURI:       "/health",
			expectedPath: "/health",
		},
		{
			name:         "Dashboard stats via forwarded URI",
			reqURL:       "http://example.com/api/index.go",
			fwdURI:       "/api/v1/dashboard/stats",
			expectedPath: "/api/v1/dashboard/stats",
		},
		{
			name:         "Dashboard stream via forwarded URI",
			reqURL:       "http://example.com/api/index.go",
			fwdURI:       "/api/v1/dashboard/stream",
			expectedPath: "/api/v1/dashboard/stream",
		},
		{
			name:         "Threats with query string in forwarded URI",
			reqURL:       "http://example.com/api/index.go",
			fwdURI:       "/api/v1/dashboard/threats?limit=25",
			expectedPath: "/api/v1/dashboard/threats",
			expectedQ:    "limit=25",
		},
		{
			name:         "Path query parameter fallback",
			reqURL:       "http://example.com/api/index.go?path=/api/v1/dashboard/stats",
			expectedPath: "/api/v1/dashboard/stats",
		},
		{
			name:         "Short v1 route normalizes to /api/v1",
			reqURL:       "http://example.com/api/index.go",
			fwdURI:       "/v1/dashboard/stats",
			expectedPath: "/api/v1/dashboard/stats",
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			req := httptest.NewRequest("GET", tt.reqURL, nil)
			if tt.fwdURI != "" {
				req.Header.Set("x-forwarded-uri", tt.fwdURI)
			}
			if tt.invokePath != "" {
				req.Header.Set("x-invoke-path", tt.invokePath)
			}

			resolved := resolvePath(req)
			if resolved != tt.expectedPath {
				t.Errorf("resolvePath() = %v, want %v", resolved, tt.expectedPath)
			}
			if tt.expectedQ != "" && req.URL.RawQuery != tt.expectedQ {
				t.Errorf("r.URL.RawQuery = %v, want %v", req.URL.RawQuery, tt.expectedQ)
			}
		})
	}
}

func TestHandlerExecution(t *testing.T) {
	// 1. Test Root Status Check
	rootReq := httptest.NewRequest("GET", "http://example.com/api/index.go", nil)
	rootReq.Header.Set("x-forwarded-uri", "/")
	rootRec := httptest.NewRecorder()
	Handler(rootRec, rootReq)
	if rootRec.Code != 200 {
		t.Fatalf("expected 200 for root, got %d. Body: %s", rootRec.Code, rootRec.Body.String())
	}

	// 2. Test Health Check
	healthReq := httptest.NewRequest("GET", "http://example.com/api/index.go", nil)
	healthReq.Header.Set("x-forwarded-uri", "/health")
	healthRec := httptest.NewRecorder()
	Handler(healthRec, healthReq)
	if healthRec.Code != 200 {
		t.Fatalf("expected 200 for health, got %d. Body: %s", healthRec.Code, healthRec.Body.String())
	}

	// 3. Test Dashboard Stats
	statsReq := httptest.NewRequest("GET", "http://example.com/api/index.go", nil)
	statsReq.Header.Set("x-forwarded-uri", "/api/v1/dashboard/stats")
	statsRec := httptest.NewRecorder()
	Handler(statsRec, statsReq)
	if statsRec.Code != 200 {
		t.Fatalf("expected 200 for dashboard stats, got %d. Body: %s", statsRec.Code, statsRec.Body.String())
	}
}

