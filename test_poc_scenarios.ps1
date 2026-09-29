# ==============================================================================
# SECUREGUARD MOBILE SECURITY - SCRIPT PENGUJIAN OTOMATIS SKENARIO POC
# ==============================================================================
# Deskripsi: Script ini mengeksekusi pengujian otomatis seluruh skenario POC
# terhadap Core Backend Engine (port :8080) dan menyajikan hasil validasi status.
# ==============================================================================

$BackendBaseUrl = "http://localhost:8080"
$PassedCount = 0
$TotalTests = 11

Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "       SECUREGUARD ENTERPRISE POC - AUTOMATED TEST SUITE              " -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "Target Endpoint: $BackendBaseUrl" -ForegroundColor Yellow
$timestamp = Get-Date -Format "dd MMMM yyyy HH:mm:ss WIB"
Write-Host "Waktu Pengujian : $timestamp" -ForegroundColor Yellow
Write-Host "----------------------------------------------------------------------"

function Run-TestCase {
    param (
        [string]$TestId,
        [string]$Title,
        [string]$Method,
        [string]$Uri,
        [string]$Body = "",
        [switch]$ExpectError,
        [scriptblock]$Validator = $null
    )

    Write-Host ""
    Write-Host "[$TestId] $Title" -ForegroundColor White
    Write-Host "  $Method $Uri" -ForegroundColor DarkGray
    if ($Body -ne "") {
        Write-Host "  Payload: $Body" -ForegroundColor DarkGray
    }

    try {
        if ($Method -eq "GET") {
            $resp = Invoke-RestMethod -Uri $Uri -Method Get -TimeoutSec 5
        } else {
            $resp = Invoke-RestMethod -Uri $Uri -Method Post -Body $Body -ContentType "application/json" -TimeoutSec 5
        }

        if ($ExpectError) {
            Write-Host "  -> VERIFIKASI: [FAIL] (Ekspektasi ditolak tetapi request berhasil)" -ForegroundColor Red
            return
        }

        $isValid = $true
        if ($Validator -ne $null) {
            $isValid = & $Validator $resp
        }

        if ($isValid) {
            Write-Host "  -> VERIFIKASI: [PASS] (Berhasil Sesuai Ekspektasi)" -ForegroundColor Green
            $global:PassedCount++
        } else {
            Write-Host "  -> VERIFIKASI: [FAIL] (Respon tidak sesuai kriteria)" -ForegroundColor Red
        }
        $respJson = ($resp | ConvertTo-Json -Compress)
        Write-Host "  Respon: $respJson" -ForegroundColor Gray
    } catch {
        $errBody = ""
        try {
            if ($_.Exception.Response) {
                $stream = $_.Exception.Response.GetResponseStream()
                $reader = New-Object System.IO.StreamReader($stream)
                $errBody = $reader.ReadToEnd()
            }
        } catch {}

        if ($ExpectError) {
            Write-Host "  -> VERIFIKASI: [PASS] (Akses ditolak sesuai aturan Subscription Guard)" -ForegroundColor Green
            Write-Host "  Respon HTTP Guard: $errBody" -ForegroundColor Gray
            $global:PassedCount++
        } else {
            Write-Host "  -> VERIFIKASI: [ERROR] Gagal menghubungi backend: $_" -ForegroundColor Red
        }
    }
}

# --- TEST 1: Health Check ---
Run-TestCase -TestId "TEST-01" `
    -Title "Pemeriksaan Kesehatan Server (Health Check)" `
    -Method "GET" `
    -Uri "$BackendBaseUrl/health" `
    -Validator { param($r) $r.status -eq "healthy" }

# --- TEST 2: Request SMS OTP Pelanggan Aktif ---
$otpReq = @{ msisdn = "081299887766"; mobile_id = "MOB-POC-TEST-01" } | ConvertTo-Json
Run-TestCase -TestId "TEST-02" `
    -Title "Pengiriman SMS OTP Pelanggan Aktif (081299887766)" `
    -Method "POST" `
    -Uri "$BackendBaseUrl/api/v1/auth/request-otp" `
    -Body $otpReq `
    -Validator { param($r) $r.status -eq "SENT" -and $r.mock_otp -eq "123456" }

# --- TEST 3: Verifikasi Kode OTP ---
$verifyReq = @{ msisdn = "081299887766"; mobile_id = "MOB-POC-TEST-01"; otp_code = "123456" } | ConvertTo-Json
Run-TestCase -TestId "TEST-03" `
    -Title "Verifikasi Kode OTP 6-Digit dan Penerbitan Sesi 30 Hari" `
    -Method "POST" `
    -Uri "$BackendBaseUrl/api/v1/auth/verify-otp" `
    -Body $verifyReq `
    -Validator { param($r) $r.success -eq $true -and $r.expires_in_days -eq 30 -and $r.session_token -ne $null }

# --- TEST 4: Validasi Masa Aktif Nomor Normal ---
$t4Uri = $BackendBaseUrl + "/api/v1/subscription/check?msisdn=081299887766" + [char]38 + "mobile_id=MOB-POC-TEST-01"
Run-TestCase -TestId "TEST-04" `
    -Title "Pengecekan Masa Aktif Paket Data (081299887766)" `
    -Method "GET" `
    -Uri $t4Uri `
    -Validator { param($r) $r.is_valid -eq $true -and $r.is_expired -eq $false -and $r.days_remaining -gt 0 }

# --- TEST 5: Aktivasi Tahap 1 - Simulasi Delay Jaringan NDP ---
$stage1Req = @{ msisdn = "081299887766"; mobile_id = "MOB-POC-TEST-01"; simulate_pending_ndp = $true } | ConvertTo-Json
Run-TestCase -TestId "TEST-05" `
    -Title "Aktivasi Tahap 1: Simulasi Delay NDP - Status Dalam Proses..." `
    -Method "POST" `
    -Uri "$BackendBaseUrl/api/v1/auth/activate-license" `
    -Body $stage1Req `
    -Validator { param($r) $r.success -eq $false -and $r.activation_status -eq "PENDING_PROVISIONING" -and $r.status_message -eq "Dalam Proses..." }

# --- TEST 6: Aktivasi Tahap 2 - Simulasi Gangguan Server Kaspersky ---
$stage2Req = @{ msisdn = "081299887766"; mobile_id = "MOB-POC-TEST-01"; simulate_ksp_outage = $true } | ConvertTo-Json
Run-TestCase -TestId "TEST-06" `
    -Title "Aktivasi Tahap 2: Simulasi Gangguan Kaspersky - Status Sinkronisasi Sedang Berjalan" `
    -Method "POST" `
    -Uri "$BackendBaseUrl/api/v1/auth/activate-license" `
    -Body $stage2Req `
    -Validator { param($r) $r.success -eq $true -and $r.activation_status -eq "ACTIVATION_PENDING_KSP" -and $r.status_message -eq "Sinkronisasi sedang berjalan" }

# --- TEST 7: Aktivasi Tahap 3 - Selesai Penuh Normal ---
$stage3Req = @{ msisdn = "081299887766"; mobile_id = "MOB-POC-TEST-01" } | ConvertTo-Json
Run-TestCase -TestId "TEST-07" `
    -Title "Aktivasi Tahap 3: Selesai Penuh Normal - Status Perangkat Berhasil Dilindungi" `
    -Method "POST" `
    -Uri "$BackendBaseUrl/api/v1/auth/activate-license" `
    -Body $stage3Req `
    -Validator { param($r) $r.success -eq $true -and $r.activation_status -eq "ACTIVATED" -and $r.status_message -eq "Perangkat Berhasil Dilindungi" }

# --- TEST 8: Pelanggan dengan Masa Aktif Habis ---
$t8Uri = $BackendBaseUrl + "/api/v1/subscription/check?msisdn=081200000000" + [char]38 + "mobile_id=MOB-EXP-01"
Run-TestCase -TestId "TEST-08" `
    -Title "Pemeriksaan Pelanggan Masa Aktif Kedaluwarsa (081200000000)" `
    -Method "GET" `
    -Uri $t8Uri `
    -Validator { param($r) $r.is_valid -eq $false -and $r.is_expired -eq $true }

# --- TEST 9: Pelanggan Belum Terdaftar (Subscription Guard) ---
$unregReq = @{ msisdn = "081288880000"; mobile_id = "MOB-UNREG-01" } | ConvertTo-Json
Run-TestCase -TestId "TEST-09" `
    -Title "Subscription Guard: Nomor Belum Berlangganan (081288880000)" `
    -Method "POST" `
    -Uri "$BackendBaseUrl/api/v1/auth/request-otp" `
    -Body $unregReq `
    -ExpectError

# --- TEST 10: Ingestion Telemetri Ancaman ke SOC ---
$threatReq = @{
    msisdn = "081299887766"
    mobile_id = "MOB-POC-TEST-01"
    threat_type = "TROJAN_BLOCKED"
    target = "com.suspicious.app.apk"
    severity = "CRITICAL"
    description = "Kaspersky engine mengisolasi Trojan.AndroidOS.Agent.wz"
    action_taken = "QUARANTINED"
} | ConvertTo-Json
Run-TestCase -TestId "TEST-10" `
    -Title "Ingestion Telemetri Ancaman Real-time ke SOC Event Bus" `
    -Method "POST" `
    -Uri "$BackendBaseUrl/api/v1/telemetry/events" `
    -Body $threatReq `
    -Validator { param($r) $true }

# --- TEST 11: Pengecekan Statistik Agregat SOC Dashboard ---
Run-TestCase -TestId "TEST-11" `
    -Title "Pengambilan Agregat KPI Statistik SOC Dashboard" `
    -Method "GET" `
    -Uri "$BackendBaseUrl/api/v1/dashboard/stats" `
    -Validator { param($r) $r.total_subscribers -gt 0 -and $r.kaspersky_quota_total -eq 100 }

# --- HASIL AKHIR ---
$passColor = "Yellow"
$statusColor = "Red"
$statusMsg = "TERDAPAT KEGAGALAN"

if ($PassedCount -eq $TotalTests) {
    $passColor = "Green"
    $statusColor = "Green"
    $statusMsg = "SEMUA SKENARIO LOLOS (PASSED)"
}

$pct = [math]::Round(($PassedCount / $TotalTests) * 100, 2)

Write-Host ""
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "                   RINGKASAN HASIL PENGUJIAN                          " -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "Total Skenario Diuji : $TotalTests" -ForegroundColor White
Write-Host "Total Skenario Lolos : $PassedCount" -ForegroundColor $passColor
Write-Host "Tingkat Keberhasilan : $pct%" -ForegroundColor $passColor
Write-Host "Status Pengujian     : $statusMsg" -ForegroundColor $statusColor
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host ""
