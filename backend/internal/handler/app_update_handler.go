package handler

import (
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"os"
	"path/filepath"
	"strconv"

	"telkomsecure-backend/internal/model"
)

// CheckAppUpdate checks client version vs server latest release
func (h *APIHandler) CheckAppUpdate(w http.ResponseWriter, r *http.Request) {
	buildStr := r.URL.Query().Get("build_number")
	clientBuild, _ := strconv.Atoi(buildStr)
	if clientBuild <= 0 {
		clientBuild = 1
	}

	latestVersion := "1.1.0"
	latestBuild := 2
	minSupportedBuild := 1
	releaseNotes := []string{
		"Integrasi Kaspersky Security Engine Enterprise v2026.09",
		"Fitur Pembaruan Otomatis In-App (OTA) Mandiri",
		"Penyempurnaan antarmuka autentikasi jaringan Telkomsel",
		"Peningkatan kecepatan dan efisiensi pemindaian berkas",
	}
	apkSizeMB := 28.0
	publishedAt := "2026-09-30"

	// Support dynamic config file without requiring backend recompile
	for _, cfgPath := range []string{"app_update_config.json", "../app_update_config.json"} {
		if data, err := os.ReadFile(cfgPath); err == nil {
			var cfg struct {
				LatestVersion     string   `json:"latest_version"`
				LatestBuildNumber int      `json:"latest_build_number"`
				MinSupportedBuild int      `json:"min_supported_build"`
				ReleaseNotes      []string `json:"release_notes"`
				ApkSizeMB         float64  `json:"apk_size_mb"`
				PublishedAt       string   `json:"published_at"`
			}
			if err := json.Unmarshal(data, &cfg); err == nil {
				if cfg.LatestVersion != "" {
					latestVersion = cfg.LatestVersion
				}
				if cfg.LatestBuildNumber > 0 {
					latestBuild = cfg.LatestBuildNumber
				}
				if cfg.MinSupportedBuild > 0 {
					minSupportedBuild = cfg.MinSupportedBuild
				}
				if len(cfg.ReleaseNotes) > 0 {
					releaseNotes = cfg.ReleaseNotes
				}
				if cfg.ApkSizeMB > 0 {
					apkSizeMB = cfg.ApkSizeMB
				}
				if cfg.PublishedAt != "" {
					publishedAt = cfg.PublishedAt
				}
				break
			}
		}
	}

	hasUpdate := clientBuild < latestBuild

	scheme := "http"
	if r.TLS != nil || r.Header.Get("X-Forwarded-Proto") == "https" {
		scheme = "https"
	}
	host := r.Host
	if host == "" {
		host = "localhost:8080"
	}
	downloadURL := fmt.Sprintf("%s://%s/api/v1/app/download-latest", scheme, host)

	resp := model.AppUpdateResponse{
		HasUpdate:         hasUpdate,
		LatestVersion:     latestVersion,
		LatestBuildNumber: latestBuild,
		MinSupportedBuild: minSupportedBuild,
		IsMandatory:       clientBuild < minSupportedBuild,
		ReleaseNotes:      releaseNotes,
		DownloadURL:       downloadURL,
		ApkSizeMB:         apkSizeMB,
		PublishedAt:       publishedAt,
	}

	writeJSON(w, http.StatusOK, resp)
}

// DownloadLatestApk streams or downloads the release APK
func (h *APIHandler) DownloadLatestApk(w http.ResponseWriter, r *http.Request) {
	candidatePaths := []string{
		"../releases/TelkomSecure-Release.apk",
		"../releases/TelkomSecure-v1.0.5+6-Release.apk",
		"../releases/TelkomSecure-v1.0.5.apk",
		"../releases/TelkomSecure-v1.0.4+5-Release.apk",
		"../app-arm64-v8a-production-release.apk",
		"../app-shielded.apk",
		"../main/build/app/outputs/flutter-apk/app-release.apk",
		"./TelkomSecure-latest.apk",
		"./app-arm64-v8a-production-release.apk",
		"/tmp/TelkomSecure-latest.apk",
	}

	var apkPath string
	for _, p := range candidatePaths {
		if _, err := os.Stat(p); err == nil {
			apkPath = p
			break
		}
	}

	if apkPath == "" {
		http.Error(w, "File APK pembaruan belum tersedia di server", http.StatusNotFound)
		return
	}

	file, err := os.Open(apkPath)
	if err != nil {
		http.Error(w, "Gagal membuka file APK: "+err.Error(), http.StatusInternalServerError)
		return
	}
	defer file.Close()

	stat, err := file.Stat()
	if err != nil {
		http.Error(w, "Gagal membaca stat APK: "+err.Error(), http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/vnd.android.package-archive")
	w.Header().Set("Content-Disposition", fmt.Sprintf("attachment; filename=\"%s\"", filepath.Base(apkPath)))
	w.Header().Set("Content-Length", strconv.FormatInt(stat.Size(), 10))

	_, _ = io.Copy(w, file)
}
