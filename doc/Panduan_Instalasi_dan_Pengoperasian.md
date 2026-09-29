# Panduan Instalasi, Konfigurasi & Pengoperasian
## SecureGuard Enterprise Mobile Security Platform
**Dokumen Resmi Panduan Teknis & Pengujian POC**  
**Versi:** 1.0.0 — Release Ready  
**Klasifikasi:** Internal & Stakeholder Confidential  

---

## 1. Spesifikasi Kebutuhan Sistem (Prerequisites)

Untuk menjalankan seluruh ekosistem **SecureGuard Enterprise** (Backend Engine, SOC Web Dashboard, dan Mobile Application), pastikan workstation/server memenuhi persyaratan minimum berikut:

### 1.1. Perangkat Lunak Inti
| Komponen | Versi Rekomendasi | Keterangan |
| :--- | :--- | :--- |
| **Sistem Operasi** | Windows 10/11 (64-bit) / Ubuntu 22.04 LTS | Lingkungan uji utama: Windows PowerShell |
| **Go Programming Language** | Go 1.22.x atau lebih baru | Kompilator & runtime Core Backend Engine |
| **Node.js & npm** | Node.js v20.x LTS, npm v10.x | Runtime Web SOC Dashboard (Next.js 14) |
| **Flutter SDK** | Flutter 3.22.x (Channel Stable) | Build tools & runtime Mobile App (jika run via source) |
| **Android SDK & JDK** | Android SDK 34, OpenJDK 17 | Diperlukan jika kompilasi APK mandiri |
| **PowerShell** | Windows PowerShell 5.1 / PowerShell 7+ | Runner script otomasi pengujian POC |

---

## 2. Struktur Direktori Proyek

Arsitektur monorepo SecureGuard dirancang modular dengan pemisahan tugas yang jelas:

```text
TelkomSecure/
├── backend/                  # Core Backend Engine (Go HTTP REST + SSE Server)
│   ├── cmd/server/main.go    # Entrypoint server port :8080
│   └── internal/             # Handler, Service, Model, & In-Memory Store
├── web/                      # SOC Security Operations Center Dashboard (Next.js 14)
│   ├── src/app/              # App router (Dashboard, Live Map, Telemetry, NDP)
│   └── package.json          # Dependensi Next.js & Tailwind CSS
├── main/                     # Enterprise Mobile Security Application (Flutter)
│   ├── lib/                  # Dart source code (Screen, Service, State)
│   └── android/              # Native Android wrapper & configuration
├── releases/                 # Hasil build produksi siap pakai
│   └── SecureGuard-MobileSecurity-Release.apk (29.3 MB, Tanpa BlackWall)
├── doc/                      # Dokumentasi Teknis & Panduan
│   ├── SecureGuard_Enterprise_Technical_POC_Report.pdf (Laporan Teknis Lengkap)
│   └── Panduan_Instalasi_dan_Pengoperasian.md (Dokumen ini)
└── test_poc_scenarios.ps1    # Script Otomasi Pengujian 11 Skenario POC
```

---

## 3. Langkah Instalasi & Menjalankan Sistem

### Tahap A: Menjalankan Core Backend Engine (Port :8080)
1. Buka terminal PowerShell pada direktori `backend/`:
   ```powershell
   cd d:\DEVELOPMENT\Projects\Riski\TelkomSecure\backend
   ```
2. Jalankan server menggunakan Go:
   ```powershell
   go run ./cmd/server/main.go
   ```
3. Server aktif ketika muncul log:
   ```text
   [INFO] Starting Enterprise Security Backend on port :8080...
   [INFO] Route registered: /health
   [INFO] Route registered: /api/v1/auth/*
   [INFO] Route registered: /api/v1/dashboard/*
   ```
4. **Validasi Kesehatan Server**:
   Akses `http://localhost:8080/health` pada browser atau via curl/Invoke-RestMethod. Respon valid:
   ```json
   {"service":"TelkomSecure Enterprise Backend","status":"healthy","time":"2026-09-28T18:27:07+07:00"}
   ```

---

### Tahap B: Menjalankan Web SOC Security Dashboard (Port :3005)
1. Buka jendela terminal PowerShell baru pada direktori `web/`:
   ```powershell
   cd d:\DEVELOPMENT\Projects\Riski\TelkomSecure\web
   ```
2. Pasang dependensi Node.js (hanya dilakukan sekali di awal):
   ```powershell
   npm install
   ```
3. Jalankan server Next.js pada port **3005** (untuk menghindari konflik port 3000):
   ```powershell
   npm run dev -- -p 3005
   ```
4. Buka peramban web dan akses:
   **`http://localhost:3005`**
   - Layar ringkasan SOC, metrik KPI, umpan ancaman real-time (SSE), dan simulator provisioning NDP siap digunakan.

---

### Tahap C: Mengoperasikan Aplikasi Mobile Android

Tersedia 2 metode pengoperasian aplikasi Android:

#### Opsi 1: Instalasi File APK Release (Rekomendasi Cepat)
File APK produksi yang telah dikompilasi secara bersih tanpa proteksi BlackWall tersedia di direktori `releases/`:
```powershell
adb install -r d:\DEVELOPMENT\Projects\Riski\TelkomSecure\releases\SecureGuard-MobileSecurity-Release.apk
```
*Atau salin file `SecureGuard-MobileSecurity-Release.apk` langsung ke perangkat fisik/emulator Android dan lakukan instalasi via File Manager.*

#### Opsi 2: Menjalankan dari Source Code (Mode Developer)
1. Buka terminal pada folder `main/`:
   ```powershell
   cd d:\DEVELOPMENT\Projects\Riski\TelkomSecure\main
   ```
2. Unduh dependensi Flutter:
   ```powershell
   flutter pub get
   ```
3. Jalankan aplikasi pada emulator atau perangkat Android yang terhubung:
   ```powershell
   flutter run
   ```

---

## 4. Panduan Eksekusi Script Pengujian Otomatis

Untuk memastikan seluruh alur kerja autentikasi, pengecekan masa aktif paket, penanganan error 3 tahap, dan telemetri SOC berfungsi sempurna, telah disediakan script otomatis:

### Cara Menjalankan Script Uji:
Buka PowerShell di root folder proyek dan eksekusi:
```powershell
cd d:\DEVELOPMENT\Projects\Riski\TelkomSecure
powershell -ExecutionPolicy Bypass -File .\test_poc_scenarios.ps1
```

### Matriks 11 Skenario yang Diuji:
| Kode Uji | Skenario Pengujian | Target Endpoint | Kriteria Lolos (Pass) |
| :--- | :--- | :--- | :--- |
| **TEST-01** | Pemeriksaan Kesehatan Server | `GET /health` | Status bernilai `"healthy"` |
| **TEST-02** | Pengiriman SMS OTP Nomor Aktif | `POST /api/v1/auth/request-otp` | Status `"SENT"`, OTP `"123456"` |
| **TEST-03** | Verifikasi OTP & Sesi 30 Hari | `POST /api/v1/auth/verify-otp` | Session token terbit, 30 hari valid |
| **TEST-04** | Validasi Masa Aktif Paket Normal | `GET /api/v1/subscription/check` | `is_valid: true`, sisa hari > 0 |
| **TEST-05** | Aktivasi Tahap 1: Delay Jaringan NDP | `POST /api/v1/auth/activate-license` | Status `"PENDING_PROVISIONING"`, pesan `"Dalam Proses..."` |
| **TEST-06** | Aktivasi Tahap 2: Gangguan Kaspersky | `POST /api/v1/auth/activate-license` | Status `"ACTIVATION_PENDING_KSP"`, pesan `"Sinkronisasi sedang berjalan"` |
| **TEST-07** | Aktivasi Tahap 3: Selesai Normal | `POST /api/v1/auth/activate-license` | Status `"ACTIVATED"`, pesan `"Perangkat Berhasil Dilindungi"` |
| **TEST-08** | Pelanggan Paket Kedaluwarsa | `GET /api/v1/subscription/check` | `is_valid: false`, `is_expired: true` |
| **TEST-09** | Subscription Guard (Nomor Tak Terdaftar) | `POST /api/v1/auth/request-otp` | Akses ditolak (HTTP 403 Forbidden) |
| **TEST-10** | Ingestion Telemetri Ancaman ke SOC | `POST /api/v1/telemetry/events` | Telemetri tercatat di antrean insiden SOC |
| **TEST-11** | Agregat Statistik SOC Dashboard | `GET /api/v1/dashboard/stats` | Kuota lisensi & metrik perangkat valid |

**Target Tingkat Keberhasilan:** **100% (11/11 PASSED)**.

---

## 5. Daftar Data Uji Coba (Mock Test Accounts)

Gunakan nomor-nomor berikut saat mendemonstrasikan aplikasi mobile:

| MSISDN | Peran / Kasus Uji | Perilaku Sistem | Kode OTP |
| :--- | :--- | :--- | :--- |
| **`081299887766`** | Pelanggan Korporasi Aktif | Berlangganan 30 hari aktif, lisensi Kaspersky siap ikat | `123456` |
| **`081200000000`** | Pelanggan Kedaluwarsa | Peringatan paket habis, akses proteksi terkunci | `123456` |
| **`081288880000`** | Nomor Belum Berlangganan | Diblokir oleh Subscription Guard sebelum OTP terbit | - |
| **`081112223344`** | Simulasi SIM Swap | Peringatan perubahan ICCID pada SOC Dashboard | `123456` |
| **`085233445566`** | Simulasi Delay Provisioning | Menampilkan status *"Dalam Proses..."* pada wizard | `123456` |

---

## 6. Prosedur Troubleshooting & FAQ

1. **Port 8080 atau 3005 Sudah Digunakan (Port Already in Use)**:
   - Cek proses yang menduduki port:
     ```powershell
     Get-NetTCPConnection -LocalPort 8080,3005 -ErrorAction SilentlyContinue | Select-Object LocalPort, OwningProcess
     ```
   - Hentikan proses jika diperlukan atau jalankan pada port alternatif.
2. **Koneksi Mobile App ke Backend Gagal**:
   - Jika menjalankan di emulator Android, alamat localhost host machine adalah `http://10.0.2.2:8080`.
   - Jika menjalankan di perangkat fisik, pastikan perangkat dan PC berada dalam satu jaringan Wi-Fi, lalu arahkan base URL ke IP LAN komputer (misal: `http://192.168.1.100:8080`).
3. **Pemberitahuan Status "Sinkronisasi sedang berjalan"**:
   - Merupakan perilaku normal mitigasi jika server Kaspersky B2B mengalami timeout. Pengguna dapat menekan tombol **"Sinkronkan Ulang"** pada menu Profil aplikasi setelah koneksi server pulih.

---

## 7. Verifikasi Independen Integritas & Eksekusi Genuine Kaspersky SDK

Untuk membuktikan secara transparan dan independen bahwa **Kaspersky Mobile Security SDK (v5.21.0.209)** berjalan secara riil di layer native (bukan simulasi/mock), lakukan langkah audit berikut:

### 7.1. Audit Melalui Antarmuka Aplikasi (Live Diagnostic Card)
1. Buka aplikasi **Telkomsel Secure** dan masuk ke tab **Profil** (ikon orang di pojok kanan bawah).
2. Periksa kartu **"Kaspersky Mobile SDK Native Engine"**:
   - **Versi SDK Engine**: `5.21.0.209` (Versi rilis resmi Kaspersky B2B).
   - **Status Lisensi Native**: `VALID (6KYKJ-65T6T-WMVBD-NNPEG)`.
   - **Basis Virus Engine**: Menampilkan path berkas basis internal (`bases.aac`).
   - **Hardware ID Hash**: Hash hardware unik yang dihitung langsung oleh `KavSdk.getHashOfHardwareId()`.
   - **Installation GUID**: GUID instans unik dari `KavSdk.getInstallationId()`.
3. Tekan tombol **"Buka Lab Uji Keamanan (EICAR & KSN)"** untuk melakukan uji coba realtime.

### 7.2. Audit Melalui ADB Logcat (Pengecekan Tingkat Kernel & Bytecode Native)
Jalankan perintah PowerShell berikut saat smartphone Android terhubung:
```powershell
adb logcat -v time -s KasperskyNativeBridge:I KavSdk:I
```

#### Output Bukti Eksekusi Native yang Dihasilkan:
1. **Saat Inisialisasi SDK**:
   ```text
   I/KasperskyNativeBridge: >>> [KASPERSKY NATIVE] KavSdk.initSafe SUCCESS. Bases path: /data/user/0/com.telkomsel.secure.telkomsel_secure/app_bases
   I/KasperskyNativeBridge: >>> [KASPERSKY NATIVE] Antivirus Engine INITIALIZED SUCCESS.
   ```
2. **Saat Lisensi Diaktifkan**:
   ```text
   I/KasperskyNativeBridge: >>> [KASPERSKY NATIVE] Activating license key: 6KYKJ-65T6T-WMVBD-NNPEG
   I/KasperskyNativeBridge: >>> [KASPERSKY NATIVE] Activation result: isValid=true, expire=1798156799
   ```
3. **Saat Pemindaian Berkas EICAR Berjalan**:
   ```text
   W/KasperskyNativeBridge: >>> [KASPERSKY NATIVE] REAL EICAR DETECTED BY ENGINE: EICAR-Test-File (VIRUS)
   ```
4. **Saat Deteksi Root Berjalan**:
   ```text
   I/KasperskyNativeBridge: >>> [KASPERSKY NATIVE] RootDetector.checkRoot() -> isRooted=false, cause=
   ```
5. **Saat Realtime Protection Aktif**:
   ```text
   I/KasperskyNativeBridge: >>> [KASPERSKY NATIVE] Realtime Monitor is now ACTIVE.
   ```

*Catatan: Jika engine antivirus belum diinisialisasi atau basis belum siap, sistem akan secara jujur melaporkan `isThreat: false` atau pesan error tanpa melakukan rekayasa string tiruan.*

