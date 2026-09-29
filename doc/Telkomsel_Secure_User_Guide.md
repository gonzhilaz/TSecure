# Panduan Pengguna Telkomsel Secure Mobile App
**Versi 1.0.0 — Edisi Resmi Pelanggan Telkomsel**

---

## 1. Tentang Telkomsel Secure

**Telkomsel Secure** adalah solusi keamanan perangkat bergerak (*mobile security*) mutakhir yang dirancang khusus untuk melindungi pelanggan Telkomsel dari berbagai ancaman siber modern. Didukung oleh integrasi mesin keamanan kelas korporasi **Kaspersky B2B Mobile Security SDK** dan kecerdasan jaringan **Telkomsel Network Data Platform (NDP)**, aplikasi ini memberikan perlindungan menyeluruh terhadap:
* Serangan virus, spyware, ransomware, dan aplikasi berbahaya (*Trojan/PUA*).
* Situs web penipuan (*phishing*) dan tautan palsu perbankan saat berselancar di internet.
* Jaringan Wi-Fi publik yang tidak aman atau berpotensi menyusupkan data (*Man-in-the-Middle attack*).
* Penyalahgunaan izin privasi pada aplikasi-aplikasi yang terpasang di ponsel cerdas Anda.

---

## 2. Memulai Aplikasi: Splash Screen & Sesi Otomatis 30 Hari

### 2.1 Membuka Aplikasi
Saat pertama kali membuka ikon aplikasi **Telkomsel Secure** di layar ponsel Anda:
1. Layar **Splash Screen** bernuansa *Telkomsel Deep Navy* akan muncul sesaat.
2. Sistem secara otomatis melakukan verifikasi integritas sistem dan status langganan perangkat ke jaringan Telkomsel.

### 2.2 Kemudahan Sesi Persisten 30 Hari (MyTelkomsel Standard)
Aplikasi ini mengadopsi standar kenyamanan akun MyTelkomsel. Setelah Anda berhasil masuk dan memverifikasi nomor ponsel, sesi login Anda akan **tersimpan secara aman selama 30 hari**. 
* Anda tidak perlu berulang kali meminta dan memasukkan kode SMS OTP setiap kali membuka aplikasi.
* Aplikasi akan langsung membuka layar Dashboard utama selama masa aktif sesi masih berlaku.

---

## 3. Masuk & Autentikasi SMS OTP

Jika Anda baru pertama kali memasang aplikasi, berganti perangkat, atau masa sesi 30 hari telah berakhir, Anda akan diarahkan ke layar **Masuk**.

### 3.1 Langkah Memasukkan Nomor Ponsel
1. Pada kolom **Nomor Ponsel**, masukkan nomor seluler Telkomsel Anda (Halo / Prabayar), contoh: `0812-9988-7766` atau `81234567890`.
2. Pastikan tanda centang **Ketentuan Layanan & Kebijakan Privasi (PDP)** telah tercentang sebagai persetujuan perlindungan data pribadi.
3. Tekan tombol **"Masuk"**.

### 3.2 Memasukkan Kode Verifikasi OTP 6-Digit
1. Jaringan Telkomsel akan mengirimkan SMS resmi berisi **6 digit kode verifikasi** ke nomor ponsel Anda.
2. Masukkan 6 digit kode tersebut pada kotak input verifikasi OTP.
   * Kotak input mendukung fitur *auto-focus* dan *paste otomatis*.
3. Jika SMS belum diterima, tunggu hingga penghitung waktu mundur (**60 detik**) selesai, lalu tekan **"Kirim Ulang Kode OTP"**.
4. Tekan tombol **"Verifikasi & Lanjutkan"**.

---

## 4. Wizard Aktivasi Layanan (3 Tahap Visual)

Bagi pelanggan yang baru pertama kali membeli paket perlindungan atau mengikat perangkat baru, aplikasi akan menjalankan **Wizard Aktivasi Layanan** secara otomatis.

Proses aktivasi terdiri dari 3 tahapan yang transparan:

```
[Tahap 1: Verifikasi Paket] ──> [Tahap 2: Ikat Lisensi Kaspersky] ──> [Tahap 3: Selesai]
```

### Tahap 1: Verifikasi Paket MyTelkomsel
* **Kondisi Normal (Aktif):**
  Sistem mendeteksi paket Telkomsel Secure Anda aktif di jaringan. Indikator berubah menjadi centang hijau dan sistem **melanjutkan secara otomatis** ke Tahap 2 dalam waktu kurang dari 1 detik.
* **Kondisi Sinkronisasi Jaringan Tertunda (Delay NDP):**
  Jika Anda baru saja membeli paket beberapa detik sebelumnya dan sistem jaringan masih dalam proses sinkronisasi, akan muncul lencana status amber bertuliskan:
  **`"Dalam Proses..."`**
  * *Tindakan Anda:* Cukup tekan tombol **"Cek Ulang Status"** untuk memeriksa kembali pembaruan paket dari jaringan MyTelkomsel tanpa harus mengulang dari awal.

### Tahap 2: Menerbitkan & Mengikat Lisensi Kaspersky B2B
Aplikasi mengaitkan identitas unik perangkat (*Mobile ID*) ke kuota lisensi Kaspersky korporasi resmi Anda.
* **Kondisi Berhasil:**
  Lisensi langsung terikat pada perangkat ponsel Anda, mesin proteksi aktif penuh, dan layar otomatis beralih ke Tahap 3.
* **Kondisi Gangguan Jaringan / Server Kaspersky Mengalami Antrean:**
  Jika gateway server Kaspersky sedang dalam pemeliharaan atau mengalami pelambatan respons:
  * Sistem mencatat status: **`ACTIVATION_PENDING_KSP`**.
  * **Anda TIDAK ditolak / diblokir**: Karena paket MyTelkomsel Anda telah terverifikasi sah, Anda tetap diberikan akses masuk ke aplikasi dengan status peringatan **`"Sinkronisasi sedang berjalan"`**.
  * Tekan tombol **"Lanjut ke Aplikasi"**. Anda dapat menyelesaikan pengikatan lisensi kapan saja melalui tombol *"Sinkronkan Ulang"* di menu Profil.

### Tahap 3: Selesai
Saat aktivasi berhasil diselesaikan:
* Layar menampilkan **ikon centang hijau besar** dengan tulisan resmi:
  **`"Perangkat Berhasil Dilindungi"`**
* Aplikasi secara otomatis mengarahkan Anda langsung menuju layar **Dashboard Utama**.

---

## 5. Fitur Utama Dashboard Keamanan

Dashboard adalah pusat kendali proteksi perangkat ponsel Anda yang memuat sejumlah modul keamanan terdepan:

### 5.1 Indikator Skor Keamanan (*Security Gauge*)
Di bagian atas Dashboard, terdapat pengukur persentase skor keamanan dinamis (0% – 100%):
* **Skor ≥ 90% (Hijau Emerald):** Perangkat Anda dalam kondisi prima dan terlindungi secara optimal.
* **Skor 70% – 89% (Amber):** Terdapat rekomendasi perlindungan yang dapat dioptimalkan (misal: pemindaian belum dilakukan lebih dari 72 jam).
* **Skor < 70% (Merah Telkomsel):** Perangkat rentan terhadap ancaman atau masa aktif paket telah berakhir. Segera lakukan tindakan perbaikan yang disarankan.

### 5.2 Pemindaian Cepat Antivirus (*Quick Scan*)
* Tekan tombol **"Pindai Sekarang"** untuk memulai pemindaian file aplikasi, unduhan baru, dan sistem memori secara cepat oleh mesin Kaspersky.
* Durasi pemindaian cepat rata-rata berlangsung antara 5 hingga 15 detik tanpa membebani baterai ponsel Anda.

### 5.3 Perlindungan Web & Anti-Phishing (*Web Protection*)
* Menjaga aktivitas penelusuran web Anda dari situs palsu perbankan, link unduhan APK berbahaya, dan penipuan undian bodong.
* Bekerja secara otomatis di latar belakang (*real-time*).

### 5.4 Keamanan Jaringan Wi-Fi (*Wi-Fi Shield*)
* Memeriksa enkripsi router Wi-Fi publik yang Anda hubungkan (memastikan penggunaan WPA2/WPA3).
* Memberi peringatan seketika bila terdeteksi indikasi intersepsi data (*ARP Spoofing* atau *Rogue Hotspot*).

---

## 6. Menu Profil & Sinkronisasi Ulang Lisensi

Menu **Profil** dapat diakses melalui tab paling kanan pada navigasi bawah aplikasi.

### 6.1 Memeriksa Status Kartu Lisensi
Di dalam menu Profil, kartu **Status Lisensi** menyajikan informasi transparan mengenai:
* Nama Paket Aktif (contoh: *Telkomsel Secure Guard 30 Hari*).
* Sisa Masa Aktif Perlindungan (contoh: *26 hari lagi*).
* Jumlah Perangkat Aktif yang terikat (*1 dari 1 perangkat*).

### 6.2 Menggunakan Tombol "Sinkronkan Ulang"
Bila pada saat aktivasi awal server lisensi mengalami gangguan (status *"Sinkronisasi sedang berjalan"*):
1. Buka menu **Profil**.
2. Pada kartu lisensi, Anda akan melihat kartu peringatan khusus dengan tombol **"Sinkronkan Ulang"**.
3. Tekan tombol **"Sinkronkan Ulang"**.
4. Aplikasi akan menghubungkan kembali Mobile ID Anda ke server Kaspersky. Setelah berhasil, notifikasi hijau *"Perangkat Berhasil Dilindungi"* akan muncul dan seluruh proteksi aktif seketika.

### 6.3 Keluar dari Akun (*Logout*)
1. Gulir ke bagian paling bawah menu Profil dan tekan tombol **"Keluar dari Akun"**.
2. Kotak dialog konfirmasi akan muncul.
3. Tekan **"Ya, Keluar"** untuk mengakhiri sesi dan membersihkan kredensial sesi lokal pada perangkat.
