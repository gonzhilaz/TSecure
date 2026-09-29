# Dokumen Spesifikasi Komprehensif Arsitektur, Alur Kerja Tri-Partite & Laporan Pengujian POC
**SecureGuard Mobile Security & Enterprise Threat Management Platform**
*Bertenaga Integrasi Telecom Core Network (NDP) & Kaspersky B2B Mobile Security SDK*
*Dokumen Resmi: System Analyst, Technical Writer, Solutions Architect & Engineering Team*

---

## 1. Executive Summary & Arsitektur Tri-Partite (Mobile, Backend, Dashboard)

### 1.1 Visi Solusi & Nilai Bisnis
**SecureGuard Mobile Security Platform** adalah ekosistem keamanan perangkat bergerak tingkat korporasi (*telco-grade mobile threat management*) yang menghubungkan secara terpadu:
1. **Aplikasi Klien Mobile (Flutter)** terpasang pada gawai cerdas pengguna akhir.
2. **Core Backend Engine (Golang REST & SSE Daemon)** sebagai orkestrator lisensi, otentikasi sesi, dan agregator telemetri ancaman.
3. **Web SOC & Admin Dashboard (Next.js)** sebagai pusat kendali operasional *Customer Care Desk*, analisis insiden keamanan siber, dan simulator transaksi *Network Data Platform (NDP)*.

Solusi ini memecahkan tantangan kritis operator telekomunikasi: **menjamin kuota lisensi B2B hanya terikat pada pelanggan dengan masa aktif paket data yang sah**, menghentikan kebocoran lisensi (*license leakage*), mendeteksi pembajakan nomor seluler (*SIM Swap*), dan memberikan pengalaman pengguna yang tangguh terhadap gangguan jaringan (*fault-tolerant*).

### 1.2 Diagram Arsitektur Tri-Partite & Alur Interaksi Terpadu

```
  ┌────────────────────────────────────────────────────────────────────────┐
  │                    1. MOBILE CLIENT (FLUTTER APP)                      │
  │  • Sesi Persisten 30 Hari       • Wizard Aktivasi Layanan 3 Tahap      │
  │  • Kaspersky SDK Engine Bridge  • Dynamic Security Scoring (0–100%)    │
  └──────────────────┬────────────────────────────────▲────────────────────┘
                     │                                │
        1. Request   │                                │ 4. Status Lisensi
        SMS OTP &    │                                │    & Sesi 30 Hari
        Aktivasi     │                                │
                     ▼                                │
  ┌───────────────────────────────────────────────────┴────────────────────┐
  │                 2. CORE BACKEND ENGINE (GOLANG :8080)                  │
  │  • Subscription Validator (:8080/api/v1/subscription/check)            │
  │  • Multi-Stage Activation Engine (:8080/api/v1/auth/activate-license)  │
  │  • Anti-SIM Swap Engine (Audit BoundIccid vs CurrentIccid)             │
  │  • Telemetry Ingestion Hub (:8080/api/v1/telemetry/events)             │
  │  • Live SSE Event Bus (:8080/api/v1/dashboard/stream)                  │
  └──────────────┬───────────────────────────────────┬─────────────────────┘
                 │                                   │
    2. Webhook   │ Transaksi Paket                   │ 3. Telemetri Real-time
       Order     │ & Validasi HLR                    │    & Status Pelanggan
                 ▼                                   ▼
  ┌──────────────────────────────┐   ┌─────────────────────────────────────┐
  │   3A. TELECOM CORE NETWORK   │   │  3B. WEB SOC & ADMIN DASHBOARD      │
  │     (NDP / BSS PLATFORM)     │   │         (NEXT.JS :3005)             │
  │ • Paket Data & Transaksi     │   │ • Live Threat Monitor & SSE Feed    │
  │ • USSD *363# & Portal Web    │   │ • Customer Care & License Helpdesk  │
  │ • Sinkronisasi Asinkron      │   │ • Device Integrity & SIM Swap Desk  │
  │                              │   │ • NDP & Billing Order Simulator     │
  └──────────────────────────────┘   └─────────────────────────────────────┘
```

---

## 2. Parameter Teknis Identitas, Perangkat & SIM Binding

Sistem menerapkan validasi identitas tri-aksial (*three-axis identity validation*) untuk mencegah kecurangan akun, duplikasi lisensi, dan pencurian identitas seluler:

### 2.1 Format & Validasi ICCID (Mesin Deteksi Anti-SIM Swap)
* **Definisi:** Nomor seri identitas fisik unik pada chip kartu SIM seluler sepanjang **20 digit angka**.
* **Format Baku:** `89620101XXXXXXXXXXXX`
  * `89`: Pengidentifikasi industri telekomunikasi internasional (*Telecom Major Industry*).
  * `62`: Kode nomor negara Indonesia (*Country Code*).
  * `01`: Kode jaringan operator seluler (*Mobile Network Code / MNC*).
  * `12 Digit Terakhir`: Nomor seri unik kartu fisik SIM.
* **Mekanisme Anti-SIM Swap:**
  1. Pada registrasi awal, `MSISDN` diikat permanen dengan `BoundIccid`.
  2. Setiap pemindaian rutin membaca nilai `CurrentIccid` yang aktif pada slot kartu perangkat.
  3. Bila `BoundIccid != CurrentIccid`, sistem mendeteksi **SIM Swap**, memotong skor keamanan gawai hingga ke zona bahaya (<70%), dan menyiarkan insiden berprioritas tinggi ke SOC Dashboard.

### 2.2 Aturan Pengikatan Lisensi Kaspersky B2B (`Mobile ID += 1`)
* **Format Mobile ID:** UUID perangkat unik berbasis *hardware signature* gawai (contoh: `MOB-77A4B190288EFA11`).
* **Aturan Bisnis Konsumsi Lisensi:**
  * **Aturan 1 (Konsumsi Kuota Korporasi):** Setiap inisialisasi pada gawai dengan `Mobile ID` baru secara permanen memotong kuota lisensi korporasi sebesar **+1 lisensi**.
  * **Aturan 2 (Idempotensi):** Re-inisialisasi pada perangkat yang sama dengan `Mobile ID` yang telah terdaftar tidak memotong lisensi baru (*zero additional license consumption*).
  * **Aturan 3 (Subscription Guard):** Fungsi `initKasperskySdk()` tidak diizinkan dieksekusi sebelum backend memverifikasi bahwa paket pelanggan aktif dan sah.

### 2.3 Manajemen Sesi Persisten 30 Hari
* **Token Sesi:** Diterbitkan oleh Backend setelah verifikasi OTP berhasil dengan format `SEC-SESSION-<MSISDN>-<TIMESTAMP>`.
* **Penyimpanan:** Disimpan di `SharedPreferences` terenkripsi lokal perangkat bersama stempel waktu `keySessionExpiry`.
* **Perilaku:** Saat aplikasi dibuka kembali, `SplashController` mengevaluasi masa aktif sesi lokal. Jika masih berada dalam rentang 30 hari, aplikasi langsung menampilkan Dashboard tanpa meminta SMS OTP berulang kali.

---

## 3. Algoritma Dynamic Security Scoring (0–100%)

Skor keamanan pada Dashboard dihitung secara deterministik dan dinamis secara *real-time* oleh **Security Scoring Engine**:

$$\text{Total Score} = \sum_{i=1}^{7} W_i \times S_i \quad (\text{Skor Maksimal: } 100\%)$$

| No | Dimensi Keamanan | Bobot ($W_i$) | Kriteria Skor Penuh | Dampak Kegagalan ($S_i = 0$) |
| :---: | :--- | :---: | :--- | :--- |
| 1 | **Antivirus Real-Time Shield** | **25%** | Proteksi file & memori Kaspersky aktif | **-25%** jika mesin antivirus nonaktif |
| 2 | **Recent Malware Scan** | **20%** | Scan penuh dilakukan dalam 72 jam terakhir | **-20%** jika scan usang / ada malware |
| 3 | **Web Protection & Anti-Phishing** | **15%** | Filter Safe Browsing aktif mencegah tautan palsu | **-15%** jika filter web dimatikan |
| 4 | **Integritas Sistem (Root/Bootloader)** | **15%** | Firmware resmi, tanpa binari su / Magisk | **-15%** jika terdeteksi Root / Frida hooking |
| 5 | **Keamanan Jaringan Wi-Fi** | **10%** | Terhubung ke Wi-Fi WPA2/WPA3 tanpa ARP spoofing | **-10%** jika terhubung ke Wi-Fi publik terbuka |
| 6 | **Enkripsi Penyimpanan Gawai** | **10%** | Memori internal terenkripsi perangkat keras AES-256 | **-10%** jika enkripsi internal tidak aktif |
| 7 | **Kunci Layar (Biometrik/PIN)** | **5%** | PIN, pola, atau sidik jari gawai aktif | **-5%** jika gawai tanpa kunci pengaman |

### Ambang Batas Status & Warna:
* **Skor ≥ 90% (Hijau Emerald `#10B981`):** Status `"Perangkat Sangat Aman"` — seluruh perisai proteksi aktif.
* **Skor 70% – 89% (Amber Warning `#F59E0B`):** Status `"Perlu Optimalisasi"` — pemindaian berkala direkomendasikan.
* **Skor < 70% (Brand Red `#ED0226`):** Status `"Rentan Terhadap Bahaya"` — terdeteksi ancaman aktif atau masa aktif habis.

---

## 4. Bedah Lengkap Seluruh Layar Mobile Client (Screenshot & Anatomi UI)

Setiap layar mobile dirancang dengan estetika modern, ramah pengguna, dan responsif.

### 4.1 Layar Splash Screen
* **Tangkapan Layar:** `doc/images/screen_splash_live.png`
* **Tujuan:** Gerbang pembuka aplikasi untuk inisialisasi modul lokal dan pengecekan sesi 30 hari.
* **Anatomi Elemen UI:**
  * **Logo & Teks Brand:** Menampilkan lambang perisai keamanan dan judul platform.
  * **Indikator Loading:** Animasi pemutar halus saat aplikasi membaca token sesi lokal.
  * **Logika Sistem:** Jika token sesi valid (< 30 hari), sistem memvalidasi masa aktif di latar belakang dan langsung membuka Dashboard. Jika sesi kosong/kedaluwarsa, layar berganti ke Layar Masuk.

### 4.2 Layar Masuk (Login)
* **Tangkapan Layar:** `doc/images/screen_masuk_live.png`
* **Tujuan:** Otentikasi identitas nomor seluler pelanggan sebelum memasuki jaringan proteksi.
* **Anatomi Elemen UI:**
  * **Kolom Input Nomor Ponsel (`TextField`):** Field teks berformat nomor seluler diawali kode negara `+62`.
  * **Kotak Persetujuan PDP (`Checkbox`):** Kotak persetujuan Ketentuan Layanan & Kebijakan Privasi (UU Perlindungan Data Pribadi). Wajib dicentang untuk mengaktifkan tombol Masuk.
  * **Tombol "Masuk" (`AppButton`):** Tombol merah utama untuk memicu pemanggilan API `POST /api/v1/auth/request-otp`. Menampilkan indikator berputar (*spinner*) saat proses berlangsung.
  * **Preset Uji Coba POC (`Chips`):** Tiga tombol cepat untuk pengujian:
    * `[Aktif (30 Hari)]`: Mengisi otomatis nomor pelanggan aktif (`081299887766`).
    * `[Masa Aktif Habis]`: Mengisi otomatis nomor pelanggan kedaluwarsa (`081200000000`).
    * `[Belum Terdaftar]`: Mengisi otomatis nomor belum berlangganan (`081288880000`).

### 4.3 Layar Verifikasi SMS OTP
* **Tujuan:** Memvalidasi kepemilikan nomor seluler melalui kode 6 digit rahasia.
* **Anatomi Elemen UI:**
  * **6 Kotak Digit Input OTP:** Kotak individual dengan format angka saja, mendukung *auto-focus* ke kotak berikutnya dan *paste* otomatis 6 digit sekaligus.
  * **Chip Bantuan POC Gateway:** Menampilkan kode uji coba instan (`123456`) dan tombol **"Isi Cepat"**.
  * **Penghitung Waktu Mundur 60 Detik:** Menampilkan waktu tunggu kirim ulang SMS (`Kirim ulang kode dalam 45d`).
  * **Tombol "Kirim Ulang Kode OTP":** Menjadi aktif setelah 60 detik berlalu untuk meminta kode baru.
  * **Tombol "Verifikasi & Lanjutkan":** Mengirim kode ke endpoint `POST /api/v1/auth/verify-otp`. Jika akun memerlukan aktivasi baru, rute dialihkan ke Wizard Aktivasi.

### 4.4 Wizard Aktivasi Layanan 3 Tahap
* **Tujuan:** Proses transparan pengikatan lisensi korporasi Kaspersky ke Mobile ID perangkat.
* **Anatomi 3 Tahapan:**
  * **Tahap 1: Verifikasi Paket Core Network:**
    * *Jika Aktif:* Ikon centang hijau muncul dan **otomatis lanjut ke Tahap 2**.
    * *Jika Delay Jaringan NDP:* Muncul status amber **`"Dalam Proses..."`** dengan penjelasan bahwa sistem jaringan sedang memproses data, dilengkapi tombol **`"Cek Ulang Status"`**.
  * **Tahap 2: Menerbitkan & Mengikat Lisensi Kaspersky B2B:**
    * *Jika Berhasil:* Lisensi terikat pada Mobile ID gawai, proteksi aktif penuh, dan lanjut ke Tahap 3.
    * *Jika Gateway Kaspersky Gangguan/Timeout:* Status dicatat **`ACTIVATION_PENDING_KSP`**. Muncul badge amber **`"Sinkronisasi sedang berjalan"`** dan tombol **`"Lanjut ke Aplikasi"`**. Pengguna tetap diberikan akses penuh ke aplikasi, dan tombol **`"Sinkronkan Ulang"`** disiapkan di menu Profil.
  * **Tahap 3: Selesai:**
    * Menampilkan **ikon centang hijau besar** dengan headline **`"Perangkat Berhasil Dilindungi"`** (tanpa teks tambahan yang berlebihan), lalu aplikasi secara otomatis mengarahkan pengguna masuk ke Dashboard Utama.
  * **Toolbar Preset POC di Bagian Bawah:** Menyediakan tombol simulasi cepat: `[Normal]`, `[Delay "Dalam Proses..."]`, dan `[Gangguan KSP]` untuk demonstrasi interaktif.

### 4.5 Dashboard Utama
* **Tangkapan Layar:** `doc/images/screen_updated_dashboard.png`
* **Tujuan:** Pusat monitoring keamanan gawai dan akses ke seluruh fitur perlindungan.
* **Anatomi Elemen UI:**
  * **Speedometer Skor Keamanan:** Indikator melingkar dinamis persentase keamanan gawai (0% – 100%) dengan status teks (*"100% Terlindungi"*).
  * **Tombol "Pindai Sekarang":** Menjalankan pemindaian kilat antivirus seketika.
  * **Sakelar Real-Time Protection (`Switch`):** Tombol geser untuk mengaktifkan/menonaktifkan perisai antivirus di memori sistem.
  * **Grid Fitur Keamanan:** Kartu navigasi ke Web Protection, Wi-Fi Security, App Lock, dan Antivirus PUA Scanner.

### 4.6 Layar Pemindaian Antivirus (Radar Sweep)
* **Tangkapan Layar:** `doc/images/emulator_live_5.png`
* **Tujuan:** Memeriksa seluruh file sistem dari infeksi malware dan trojan.
* **Anatomi Elemen UI:**
  * **Radar Concentric Sweep:** Animasi radar sapuan lingkaran saat memindai partisi memori.
  * **Metrik Pemindaian:** Jumlah file yang telah diperiksa (misal: *1.840 item*) dan jumlah ancaman terdeteksi (*0 ancaman*).
  * **Tombol "Pindai Ulang":** Memulai kembali siklus inspeksi menyeluruh.

### 4.7 Layar Audit & Rekomendasi Perangkat
* **Tangkapan Layar:** `doc/images/screen_perangkat.png`
* **Tujuan:** Memeriksa kepatuhan konfigurasi keamanan sistem operasi gawai.
* **Anatomi Elemen UI:**
  * **Status Root & Bootloader:** Memverifikasi integritas firmware gawai resmi.
  * **Enkripsi Penyimpanan:** Mengonfirmasi bahwa penyimpanan internal terenkripsi perangkat keras.
  * **Diagram Batang Metrik:** Visualisasi kestabilan perlindungan 6-fase.

### 4.8 Layar Riwayat Aktivitas Keamanan
* **Tangkapan Layar:** `doc/images/screen_riwayat.png`
* **Tujuan:** Buku log catatan seluruh kejadian keamanan dan pemindaian yang pernah dilakukan.
* **Anatomi Elemen UI:**
  * **Ringkasan Bulanan:** Menampilkan total pemindaian bulan berjalan dan ancaman berhasil dinetralkan.
  * **Filter Kategori (Chips):** Tombol penyaring riwayat: *Semua*, *Pemindaian*, *Jaringan & Web*, dan *Aplikasi*.
  * **Daftar Kronologis:** Catatan kejadian lengkap dengan stempel waktu, modul pendeteksi, dan status penanganan.

### 4.9 Layar Profil & Kartu Lisensi
* **Tangkapan Layar:** `doc/images/screen_profile_live.png`
* **Tujuan:** Mengelola akun pelanggan, status lisensi, dan pemulihan aktivasi.
* **Anatomi Elemen UI:**
  * **Kartu Status Lisensi (`LicenseStatusCard`):** Menampilkan nama paket aktif, sisa hari aktif (*26 hari lagi*), dan kuota perangkat (*1 perangkat aktif*).
  * **Tombol "Sinkronkan Ulang" (`OutlinedButton`):** Muncul khusus saat akun berstatus `ACTIVATION_PENDING_KSP`. Menghubungkan ulang Mobile ID ke gateway lisensi tanpa perlu login ulang.
  * **Tombol "Keluar dari Akun" (`AppButton`):** Tombol outline di bagian dasar layar untuk mengakhiri sesi login.

### 4.10 Dialog Konfirmasi Logout
* **Tangkapan Layar:** `doc/images/screen_logout_dialog.png`
* **Tujuan:** Mencegah pengguna keluar secara tidak sengaja.
* **Anatomi Elemen UI:** Tombol **"Batal"** untuk kembali ke aplikasi, dan tombol merah **"Ya, Keluar"** untuk menghapus token sesi lokal.

---

## 5. Bedah Lengkap Seluruh Tab Web SOC & Admin Dashboard (Screenshot & Anatomi UI)

Web SOC Dashboard (Next.js :3005) menyajikan visibilitas penuh bagi tim analis keamanan dan administrator jaringan:

### 5.1 Tab 1: SOC & Threat Monitoring
* **Tangkapan Layar:** `doc/images/soc_dashboard_overview.png`
* **Tujuan:** Monitoring telemetri ancaman siber yang dicegat di seluruh perangkat pelanggan secara real-time.
* **Anatomi Elemen UI:**
  * **Header Status Koneksi SSE:** Menampilkan status *Connected* dengan lampu indikator hijau dan tombol *Refresh Manual*.
  * **Kartu KPI (Stats Cards):** Total Subscribers, Active, Expired, Quota Lisensi Kaspersky Terpakai (misal: *4/100*), Desync Warnings, Rooted Devices, dan SIM Swap Alerts.
  * **Live Threat Feed (Panel Kanan):** Daftar serangan malware, phishing, dan trojan yang baru saja diblokir oleh Kaspersky SDK pada gawai pengguna, lengkap dengan tingkat keparahan (*High*, *Medium*, *Critical*) dan stempel waktu WIB.
  * **Tabel Pelanggan Terdaftar (Panel Kiri):** Ringkasan pelanggan seluler dengan kolom MSISDN, Model Gawai, dan Status Proteksi.

### 5.2 Tab 2: Customer Care & License Helpdesk Desk
* **Tangkapan Layar:** `doc/images/soc_dashboard_helpdesk.png`
* **Tujuan:** Pusat penanganan kendala pelanggan oleh tim Customer Support dan Helpdesk Lisensi.
* **Anatomi Elemen UI:**
  * **Kotak Pencarian Cepat:** Pencarian berdasarkan Nomor Ponsel, ID Pelanggan, atau Kode Aktivasi.
  * **Filter Status Lisensi:** Tombol saring status: *ALL*, *ACTIVATED*, *PENDING*, *EXPIRED*, *DESYNC*, dan *SMS_FAILED*.
  * **Tabel Pelanggan Terperinci:** Menampilkan MSISDN, Mobile ID, Nama Paket, Tanggal Kedaluwarsa, Sisa Hari, dan Lencana Status.
  * **Baris Interaktif:** Mengklik salah satu baris akan membuka **Modal Detail Pelanggan**.

### 5.3 Modal Detail Pelanggan (Customer Detail Modal)
* **Tangkapan Layar:** `doc/images/soc_dashboard_customer_detail.png`
* **Tujuan:** Audit mendalam identitas kartu SIM gawai dan penanganan perbaikan lisensi.
* **Anatomi Elemen UI:**
  * **Tab Overview:** Rincian teknis `BoundIccid`, `CurrentIccid`, Slot SIM, Model Gawai, dan Versi OS.
  * **Tab Diagnostik:**
    * Tombol **"Kirim Ulang Kode Aktivasi (SMS)"**: Memaksa pengiriman ulang kode aktivasi jika SMS gateway sempat terganggu.
    * Tombol **"Sinkronkan Ulang Lisensi Kaspersky"**: Merekonsiliasi tanggal kedaluwarsa Kaspersky dengan tanggal paket NDP bila terjadi desync.
  * **Tab Riwayat Pembelian (Purchase History):** Riwayat transaksi paket data (ID Order, Tanggal Beli, Durasi, Harga, Saluran Pembelian).
  * **Tab Log Pemindaian (Scan Logs):** 10 catatan riwayat pemindaian terakhir mesin Kaspersky gawai tersebut.

### 5.4 Tab 3: Device Integrity & SIM Watch Desk
* **Tangkapan Layar:** `doc/images/soc_dashboard_device_integrity.png`
* **Tujuan:** Mengawasi pelanggaran integritas gawai dan potensi serangan pembajakan nomor seluler (*SIM Swap*).
* **Anatomi Elemen UI:**
  * **Panel Audit SIM Swap:** Daftar gawai yang kartu SIM fisiknya terdeteksi berbeda dari data registrasi awal.
  * **Panel Root & Jailbreak Watch:** Daftar gawai pelanggan yang terdeteksi memiliki binari superuser atau modifikasi kernel berisiko.
  * **Panel Karantina:** Ringkasan file malware yang sedang diisolasi oleh modul RASP / Kaspersky.

### 5.5 Tab 4: Telecom NDP & Billing Simulator Desk
* **Tangkapan Layar:** `doc/images/soc_dashboard_ndp_simulator.png`
* **Tujuan:** Alat simulasi bagi tim QA dan pengembang untuk menyimulasikan transaksi pembelian paket tanpa kartu kredit nyata.
* **Anatomi Elemen UI:**
  * **Formulir Simulasi Pembelian:**
    * Kolom Input MSISDN (Nomor Ponsel).
    * Pilihan Paket Proteksi (Paket 30 Hari, Paket Korporasi 1 Tahun).
    * Durasi Masa Aktif (Hari).
  * **Tombol "Proses Pembelian Paket (NDP Order)":** Mengirim webhook simulasi ke backend untuk memperpanjang masa aktif seketika.
  * **Tombol "Simulasikan Paket Kedaluwarsa":** Mengubah tanggal kedaluwarsa akun menjadi masa lampau untuk menguji perilaku pasif Kaspersky SDK.
  * **Tombol "Reset Database ke Seed Awal":** Mengembalikan seluruh data simulasi ke kondisi awal pengujian.

---

## 6. Panduan Troubleshooting & Matriks Penanganan Error (Error Handling Matrix)

| Kode Error | Tampilan UI Mobile | Penyebab Utama | Alur Sistem Backend | Langkah Remediasi Teknis |
| :--- | :--- | :--- | :--- | :--- |
| **`ERR_NDP_DELAY`** | Status amber:<br/>**`"Dalam Proses..."`** | Pembelian paket baru berhasil, sinkronisasi webhook NDP ke database backend belum selesai. | Endpoint aktivasi memeriksa status `PENDING_PROVISIONING`. Akun ditahan sementara pada Tahap 1. | Pengguna menekan tombol **"Cek Ulang Status"**. Backend melakukan query ulang ke NDP core. |
| **`ERR_KSP_TIMEOUT`** | Status amber:<br/>**`"Sinkronisasi sedang berjalan"`** | Gateway penerbitan lisensi Kaspersky B2B mengalami timeout / antrean server. | Status akun disimpan sebagai `ACTIVATION_PENDING_KSP`. Backend mengembalikan `success: true` agar pengguna tidak terkunci. | Pengguna menekan **"Lanjut ke Aplikasi"**. Saat koneksi gateway pulih, tekan **"Sinkronkan Ulang"** di Profil. |
| **`ERR_OTP_INVALID`** | SnackBar merah:<br/>*"Kode OTP salah"* | Pengguna salah memasukkan 6 digit angka kode verifikasi SMS. | Backend menolak kode yang tidak cocok dengan memori OTP. | Masukkan kode yang benar (pada mode pengujian POC, gunakan kode universal `123456`). |
| **`ERR_OTP_EXPIRED`** | Pesan error:<br/>*"Kode OTP kedaluwarsa"* | Pengguna memasukkan kode setelah batas waktu 5 menit terlampaui. | Stempel waktu OTP melewati jendela validitas 300 detik. | Tekan tombol **"Kirim Ulang Kode OTP"** setelah hitungan mundur 60 detik selesai. |
| **`ERR_SUB_EXPIRED`** | Layar login menolak masuk / Dashboard proteksi mati | Masa aktif paket pelanggan telah melewati tanggal jatuh tempo (*EndDate*). | Endpoint `/subscription/check` mengembalikan `is_valid: false, is_expired: true`. | Kaspersky SDK otomatis masuk ke mode pasif (*dormant*). Pengguna diarahkan membeli paket baru. |
| **`ERR_SIM_SWAP`** | Peringatan risiko tinggi pada Dashboard | Nilai `CurrentIccid` kartu fisik SIM berbeda dengan `BoundIccid` pendaftaran. | Backend membandingkan nilai ICCID telemetri dengan data registrasi gawai. | Tim SOC menerima alert otomatis. Pengguna diarahkan menghubungi Layanan Pelanggan 188. |
| **`ERR_ROOT_DETECTED`** | Status merah:<br/>*"Integritas Terkompromi"* | Terdeteksi binari `su`, Magisk, atau hooking Frida pada sistem Android. | Modul RASP mendeteksi modifikasi lingkungan eksekusi gawai. | Aplikasi mengisolasi penyimpanan sensitif dan menyarankan penggunaan firmware pabrikan resmi. |
| **`ERR_NET_OFFLINE`** | Pesan SnackBar:<br/>*"Jaringan offline"* | Ponsel dalam mode pesawat atau sinyal seluler/Wi-Fi terputus. | Klien Flutter menangani timeout jaringan (3 detik) dengan fallback cache aman. | Aktifkan koneksi data seluler atau Wi-Fi dan tekan tombol aksi ulang (*Retry*). |

---

## 7. Laporan Bukti Validasi Pengujian POC (Live Test Results)

Validasi teknis telah dieksekusi secara langsung pada daemon backend Golang (:8080) dan Klien Mobile:

### Test Case 1: Aktivasi Normal Pelanggan Aktif (`081299887766`)
* **Request:** `POST /api/v1/auth/activate-license`
  ```json
  {"msisdn": "081299887766", "mobile_id": "TS-MOB-TEST-01"}
  ```
* **Response (Status 200 OK):**
  ```json
  {
    "success": true,
    "activation_status": "ACTIVATED",
    "stage": 3,
    "status_message": "Perangkat Berhasil Dilindungi",
    "license_key": "6KYKJ-65T6T-WMVBD-NNPEG"
  }
  ```
* **Verifikasi:** **PASS**. Ikon centang hijau besar muncul dan pengguna otomatis dialihkan ke Dashboard.

### Test Case 2: Simulasi Delay Jaringan NDP (*"Dalam Proses..."*)
* **Request:**
  ```json
  {"msisdn": "081299887766", "mobile_id": "TS-MOB-TEST-01", "simulate_pending_ndp": true}
  ```
* **Response (Status 200 OK):**
  ```json
  {
    "success": false,
    "activation_status": "PENDING_PROVISIONING",
    "stage": 1,
    "status_message": "Dalam Proses..."
  }
  ```
* **Verifikasi:** **PASS**. Muncul status amber *"Dalam Proses..."* dan tombol *"Cek Ulang Status"*.

### Test Case 3: Simulasi Gangguan Server Kaspersky (*"Sinkronisasi sedang berjalan"*)
* **Request:**
  ```json
  {"msisdn": "081299887766", "mobile_id": "TS-MOB-TEST-01", "simulate_ksp_outage": true}
  ```
* **Response (Status 200 OK):**
  ```json
  {
    "success": true,
    "activation_status": "ACTIVATION_PENDING_KSP",
    "stage": 2,
    "status_message": "Sinkronisasi sedang berjalan"
  }
  ```
* **Verifikasi:** **PASS**. Pengguna tetap diberikan akses masuk ke aplikasi, dan tombol *"Sinkronkan Ulang"* aktif di menu Profil.

### Test Case 4: Pelanggan Kedaluwarsa (`081200000000`)
* **Verifikasi:** Endpoint `/api/v1/subscription/check` mengembalikan `is_valid: false, is_expired: true`.
* **Verifikasi:** **PASS**. Mesin Kaspersky beralih ke mode pasif tanpa mengonsumsi kuota lisensi baru.

### Test Case 5: Pelanggan Belum Terdaftar (`081288880000`)
* **Verifikasi:** Request OTP ditolak dengan pesan: *"Nomor ponsel belum berlangganan paket"*.
* **Verifikasi:** **PASS**. Mencegah celah eksploitasi *auto-provisioning* liar.

---

## 8. Kepatuhan Standar Rekayasa & Keamanan

1. **Aturan Modularitas (< 400 Baris per File):**
   * Seluruh kode sumber dipelihara strictly di bawah 400 baris per berkas untuk kemudahan pemeliharaan (*maintainability*).
2. **Defensive Null-Safety:**
   * Seluruh parsing payload REST JSON menerapkan operator *safe null coalescing* (`??`) untuk mencegah crash saat runtime.
3. **Kepatuhan Static Analysis:**
   * Perintah `flutter analyze` menghasilkan **0 Issues Found** (0 error, 0 warning).
4. **Keamanan Kredensial (Zero Hardcoded Secrets):**
   * Tidak ada API key, token rahasia, atau kata sandi yang dikomit ke repositori kode.
