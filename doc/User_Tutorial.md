# Panduan Pengguna TelkomSecure (Telkomsel Mobile Security)

Selamat datang di **TelkomSecure**, solusi keamanan seluler komprehensif tingkat korporasi persembahan Telkomsel. Aplikasi ini dirancang untuk melindungi perangkat pintar, identitas digital, dan privasi Anda dari ancaman siber, malware, trojan, phishing, dan intersepsi jaringan secara real-time.

---

## 1. Memulai Aplikasi & Masuk Akun

Aplikasi TelkomSecure menggunakan sistem autentikasi instan jaringan seluler Telkomsel tanpa perlu menunggu kode OTP melalui SMS, menjamin kenyamanan dan kecepatan login yang aman.

![Layar Masuk](file:///d:/DEVELOPMENT/Projects/Riski/TelkomSecure/doc/images/emulator_live_1.png)

### Panduan Interaksi Tombol & Form:
1. **Input Nomor Ponsel**: Masukkan nomor kartu Telkomsel Anda (simPATI, Kartu Halo, atau By.U) pada kolom nomor yang diawali format internasional `+62`.
2. **Persetujuan Regulasi UU PDP**: Centang kotak persetujuan **Ketentuan Layanan & Kebijakan Privasi Telkomsel (UU PDP No. 27/2022)**. Persetujuan ini menjamin data pribadi Anda dilindungi sesuai standar hukum privasi Indonesia.
3. **Tombol "Masuk"**: Tekan tombol merah **Masuk** untuk memvalidasi nomor telepon dan memulai verifikasi lisensi korporasi B2B ke server Telkomsel.
4. **Bantuan GraPARI 188 / Veronika**: Jika Anda mengalami kendala saat masuk jaringan, tautan bantuan di bagian bawah menghubungkan Anda langsung ke asisten virtual Veronika atau panggilan GraPARI 188.

---

## 2. Dasbor Utama & Indikator Reputasi Keamanan

Setelah berhasil masuk, Anda akan diarahkan ke halaman **Dasbor Utama (Dashboard)** yang menyajikan status keamanan perangkat Anda secara seketika.

![Layar Dashboard](file:///d:/DEVELOPMENT/Projects/Riski/TelkomSecure/doc/images/screen_updated_dashboard.png)

### Panduan Fitur & Kontrol Dasbor:
1. **Speedometer Reputasi Perangkat (100% Terlindungi Maksimal)**:
   - Gauge melingkar dinamis dengan gradien warna Telkomsel menampilkan skor reputasi perangkat Anda (0–100%).
   - Indikator hijau menandakan seluruh modul keamanan (antivirus, integritas OS, enkripsi memori) beroperasi optimal.
2. **Kartu Status "Perangkat Sangat Aman"**:
   - Menampilkan waktu pemindaian terakhir.
   - **Tombol "Pindai"**: Tekan tombol ini untuk memicu pemindaian kilat terhadap aplikasi dan file terbaru.
3. **Sakelar "Proteksi Real-Time"**:
   - Sakelar toggle interaktif yang dapat diaktifkan atau dinonaktifkan sesuai kebutuhan. Saat aktif (berwarna hijau), sistem secara otomatis mencegat trojan, ransomware, dan spyware yang mencoba masuk ke perangkat Anda.
4. **Grid Modul Fitur Keamanan**:
   - **Web Filter**: Melindungi aktivitas berselancar di peramban dari situs web penipuan, malware, dan tautan berbahaya.
   - **Realtime Scan**: Modul proteksi file berbasis mesin Kaspersky Mobile B2B SDK.
   - **PUA Scanner**: Menganalisis aplikasi yang berpotensi tidak diinginkan (*Potentially Unwanted Applications*).
   - **Wifi Safety**: Memverifikasi keamanan enkripsi titik akses Wi-Fi (WPA2/WPA3) dari risiko serangan *Man-in-the-Middle* (MitM).

---

## 3. Pemindaian Menyeluruh (Scan Penuh)

Fitur pemindaian radar penuh dirancang untuk melakukan inspeksi mendalam terhadap seluruh partisi sistem operasi, penyimpanan internal, dan direktori aplikasi.

![Layar Pemindaian](file:///d:/DEVELOPMENT/Projects/Riski/TelkomSecure/doc/images/emulator_live_5.png)

### Panduan Operasional Pemindaian:
1. **Radar Visual Concentric Sweep**:
   - Animasi radar berputar memvisualisasikan proses penelusuran tanda tangan virus (*threat signatures*) secara mendalam.
2. **Indikator Status Pemindaian**:
   - Kotak dialog menampilkan persentase progres pemindaian dan jumlah ancaman yang berhasil dinetralisasi.
   - Ikon centang hijau **Semua Sistem Terlindung** mengonfirmasi tidak ada berkas berbahaya yang lolos.
3. **Tombol "Pindai Ulang"**:
   - Tekan tombol putih di bagian bawah untuk menjalankan siklus pemindaian baru kapan saja.
4. **Tombol Navigasi Kembali & Filter Lanjutan**:
   - Tombol panah kembali di kiri atas membawa Anda kembali ke dasbor utama.
   - Tombol filter di kanan atas memungkinkan penyesuaian kedalaman pemindaian (Pindai Kilat, Pindai Memori, atau Pindai Kartu SD).

---

## 4. Audit & Rekomendasi Keamanan Perangkat

Halaman **Perangkat** menyajikan telemetri aktivitas perlindungan berkelanjutan dan daftar rekomendasi konfigurasi keamanan yang perlu diperhatikan.

![Layar Perangkat](file:///d:/DEVELOPMENT/Projects/Riski/TelkomSecure/doc/images/screen_perangkat.png)

### Panduan Elemen & Kontrol:
1. **Kartu "Waktu Terakhir Scan"**:
   - Menampilkan ringkasan waktu inspeksi terakhir (misal: "3 jam yang lalu").
   - Dilengkapi tombol cepat **Pindai Perangkat**.
2. **Kartu "Proteksi Aktif (Realtime Shield 24/7)"**:
   - Dilengkapi diagram batang dinamis 6-fase yang merekam fluktuasi intensitas perlindungan latar belakang.
3. **Daftar Rekomendasi & Status Keamanan**:
   - **Optimalisasi Keamanan**: Tekan tombol **Pindai** untuk mengeksekusi rekomendasi pemeliharaan berkala.
   - **Web Filter & Wi-Fi**: Menampilkan nama jaringan Wi-Fi aktif (`Telkomsel_Orbit_5G`) beserta standar enkripsi (`WPA3`).
   - **Analisis Aplikasi & PUA**: Menginformasikan jumlah total aplikasi yang diaudit dan status ketiadaan aplikasi palsu.
   - **Proteksi Real-time**: Indikator status proteksi latar belakang tanpa jeda.

---

## 5. Riwayat Aktivitas Keamanan

Halaman **Riwayat Aktivitas** menyajikan buku log komprehensif atas seluruh aksi pencegahan, deteksi, dan audit yang telah dieksekusi oleh TelkomSecure.

![Layar Riwayat](file:///d:/DEVELOPMENT/Projects/Riski/TelkomSecure/doc/images/screen_riwayat.png)

### Panduan Interaksi Riwayat:
1. **Kartu "Ringkasan Bulan Ini"**:
   - Memberikan angka agregat pemindaian (misal: 32 pemindaian) dan jumlah ancaman yang dinonaktifkan (0 ancaman).
2. **Bilah Filter Kategori (Chips)**:
   - Pilih tab filter yang diinginkan: **Semua**, **Pemindaian**, **Jaringan & Web**, atau **Aplikasi** untuk memfilter rekaman log secara instan.
3. **Daftar Kronologis Aktivitas**:
   - Log dikelompokkan berdasarkan tanggal kejadian (**HARI INI**, **KEMARIN**).
   - Setiap kartu log menampilkan ikon kategori, ringkasan insiden (contoh: "Pemindaian Cepat Selesai", "Tautan Mencurigakan Diblokir"), detail teknis, dan waktu stempel (WIB).

---

## 6. Profil Pengguna & Pengelolaan Lisensi Korporasi

Halaman **Profil** menampilkan identitas pelanggan, tingkatan loyalitas Telkomsel Halo, dan rincian alokasi lisensi keamanan B2B yang sedang aktif.

![Layar Profil](file:///d:/DEVELOPMENT/Projects/Riski/TelkomSecure/doc/images/screen_profil.png)

### Panduan Fitur Profil:
1. **Banner Avatar & Identitas**:
   - Menampilkan inisial nama pengguna, nama lengkap, nomor ponsel terdaftar, dan status keanggotaan (misal: **Telkomsel Halo Diamond**).
2. **Tombol Aksi Cepat**:
   - Ikon pesan, sunting profil, bagikan status keamanan, dan menu opsi lainnya.
3. **Kartu Lisensi "Mobile Security Ultimate"**:
   - **Masa Berlaku**: Menampilkan batas tanggal kadaluarsa paket lisensi korporasi dan hitungan mundur hari tersisa.
   - **Perangkat Aktif**: Menginformasikan kuota perangkat yang terikat dengan lisensi akun ini.
4. **Informasi Akun & Pengaturan**:
   - Tombol pengaturan di sudut kanan atas memungkinkan pengelolaan preferensi notifikasi, audit biometrik, dan kebijakan pembaruan database virus secara otomatis.

---

*Hak Cipta © 2026 PT Telekomunikasi Selular (Telkomsel). Seluruh hak cipta dilindungi undang-undang.*
