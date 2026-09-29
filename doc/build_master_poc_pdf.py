import os
from reportlab.lib.pagesizes import A4
from reportlab.lib import colors
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, Image as RLImage, Table, TableStyle, PageBreak, HRFlowable
)
from reportlab.pdfgen import canvas
from PIL import Image as PILImage

class NumberedCanvas(canvas.Canvas):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self._saved_page_states = []

    def showPage(self):
        self._saved_page_states.append(dict(self.__dict__))
        self._startPage()

    def save(self):
        num_pages = len(self._saved_page_states)
        for state in self._saved_page_states:
            self.__dict__.update(state)
            self.draw_page_decorations(num_pages)
            super().showPage()
        super().save()

    def draw_page_decorations(self, page_count):
        self.saveState()
        self.setFont("Helvetica-Bold", 8)
        self.setFillColor(colors.HexColor("#64748B"))
        
        # Header (pages > 1)
        if self._pageNumber > 1:
            self.drawString(54, 802, "SECUREGUARD — TRI-PARTITE ARCHITECTURE & POC VALIDATION MANUAL")
            self.drawRightString(541, 802, "STRICTLY CONFIDENTIAL")
            self.setStrokeColor(colors.HexColor("#CBD5E1"))
            self.setLineWidth(0.5)
            self.line(54, 796, 541, 796)
            
        # Footer
        self.setFont("Helvetica", 8)
        page_str = f"Halaman {self._pageNumber} dari {page_count}"
        self.drawRightString(541, 34, page_str)
        self.drawString(54, 34, "© 2026 Enterprise Mobile Security Platform — Engineering, Architecture & SOC Manual")
        self.setStrokeColor(colors.HexColor("#CBD5E1"))
        self.setLineWidth(0.5)
        self.line(54, 44, 541, 44)
        self.restoreState()

def create_safe_image(img_path, max_w=200, max_h=240):
    if not os.path.exists(img_path):
        return None
    try:
        with PILImage.open(img_path) as im:
            orig_w, orig_h = im.size
        aspect = orig_h / float(orig_w)
        w = min(max_w, 487)
        h = w * aspect
        if h > max_h:
            h = max_h
            w = h / aspect
        return RLImage(img_path, width=w, height=h)
    except Exception as e:
        print(f"Error loading {img_path}: {e}")
        return None

def build_pdf():
    base_dir = os.path.dirname(os.path.abspath(__file__))
    img_dir = os.path.join(base_dir, "images")
    pdf_path = os.path.join(base_dir, "SecureGuard_Enterprise_Technical_POC_Report.pdf")
    doc = SimpleDocTemplate(
        pdf_path,
        pagesize=A4,
        leftMargin=54,
        rightMargin=54,
        topMargin=54,
        bottomMargin=54
    )

    styles = getSampleStyleSheet()
    c_primary = colors.HexColor("#ED0226")
    c_navy = colors.HexColor("#0F172A")
    c_slate = colors.HexColor("#1E293B")
    c_muted = colors.HexColor("#475569")
    c_border = colors.HexColor("#CBD5E1")
    c_bg = colors.HexColor("#F8FAFC")

    t_style = ParagraphStyle('DocTitle', parent=styles['Normal'], fontName='Helvetica-Bold', fontSize=18, leading=22, textColor=c_primary, spaceAfter=4)
    sub_style = ParagraphStyle('DocSub', parent=styles['Normal'], fontName='Helvetica-Bold', fontSize=10, leading=14, textColor=c_navy, spaceAfter=10)
    h1_style = ParagraphStyle('H1', parent=styles['Normal'], fontName='Helvetica-Bold', fontSize=12, leading=16, textColor=c_navy, spaceBefore=10, spaceAfter=5, keepWithNext=True)
    h2_style = ParagraphStyle('H2', parent=styles['Normal'], fontName='Helvetica-Bold', fontSize=9.5, leading=13, textColor=c_primary, spaceBefore=6, spaceAfter=3, keepWithNext=True)
    body_style = ParagraphStyle('Body', parent=styles['Normal'], fontName='Helvetica', fontSize=8, leading=11.5, textColor=c_slate, spaceAfter=3)
    bullet_style = ParagraphStyle('Bullet', parent=styles['Normal'], fontName='Helvetica', fontSize=8, leading=11.5, textColor=c_muted, leftIndent=10, firstLineIndent=-7, spaceAfter=2)
    th_style = ParagraphStyle('TH', parent=styles['Normal'], fontName='Helvetica-Bold', fontSize=7.5, leading=10, textColor=c_navy)
    code_style = ParagraphStyle('Code', parent=styles['Normal'], fontName='Courier', fontSize=7, leading=9, textColor=c_navy)

    story = []

    # Title & Header Banner
    story.append(Paragraph("SECUREGUARD ENTERPRISE MOBILE SECURITY", t_style))
    story.append(Paragraph("Dokumen Spesifikasi Komprehensif Arsitektur, Alur Tri-Partite & Validasi POC", sub_style))
    story.append(HRFlowable(width="100%", thickness=2, color=c_primary, spaceAfter=8))

    # Bab 1: Topologi Tri-Partite
    story.append(Paragraph("1. Executive Summary & Topologi Sistem Tri-Partite", h1_style))
    story.append(Paragraph("Platform SecureGuard mengintegrasikan 3 pilar sistem secara asinkron dan tahan gangguan:", body_style))
    
    top_table = [
        [Paragraph("<b>Pilar Sistem</b>", th_style), Paragraph("<b>Teknologi & Port</b>", th_style), Paragraph("<b>Peran, Tugas & Penjelasan Alur</b>", th_style)],
        [Paragraph("<b>1. Mobile Client</b>", body_style), Paragraph("Flutter 3.x / Dart<br/>Multi-platform", body_style), Paragraph("Sesi 30 hari, Wizard Aktivasi 3 Tahap, Jembatan Kaspersky SDK Bridge, Telemetri Real-time.", body_style)],
        [Paragraph("<b>2. Core Backend</b>", body_style), Paragraph("Golang 1.22+ REST<br/>Port :8080 (SSE)", body_style), Paragraph("Validasi Masa Aktif, Rekonsiliasi NDP, Multi-Stage Activation Engine, Anti-SIM Swap Guard.", body_style)],
        [Paragraph("<b>3. Web SOC Dashboard</b>", body_style), Paragraph("Next.js 16+ Turbopack<br/>Port :3005", body_style), Paragraph("Live Threat Monitoring, Helpdesk Lisensi Pelanggan, Device Integrity & NDP Billing Simulator.", body_style)]
    ]
    t_top = Table(top_table, colWidths=[110, 110, 267])
    t_top.setStyle(TableStyle([('BACKGROUND', (0,0), (-1,0), colors.HexColor("#F1F5F9")), ('GRID', (0,0), (-1,-1), 0.5, c_border), ('VALIGN', (0,0), (-1,-1), 'TOP')]))
    story.append(t_top)
    story.append(Spacer(1, 6))

    # Bab 2: Parameter Teknis Identitas & SIM Binding
    story.append(Paragraph("2. Parameter Teknis Identitas, Perangkat & SIM Binding", h1_style))
    story.append(Paragraph("• <b>Format & Validasi ICCID (Anti-SIM Swap):</b> Nomor seri fisik 20-digit (<i>896201...</i>). Sistem mencocokkan <code>BoundIccid</code> vs <code>CurrentIccid</code> setiap scan untuk mendeteksi pembajakan SIM Swap.", body_style))
    story.append(Paragraph("• <b>Aturan Mobile ID (+1 Lisensi B2B):</b> Setiap <code>Mobile ID</code> unik baru yang diaktifkan memotong <b>+1 kuota lisensi Kaspersky korporasi</b>. Re-inisialisasi pada gawai yang sama bersifat idempoten (0 lisensi tambahan).", body_style))
    story.append(Paragraph("• <b>Sesi Persisten 30 Hari:</b> Token sesi <code>SEC-SESSION-[MSISDN]-[TIMESTAMP]</code> disimpan di SharedPreferences aman dengan durasi 30 hari, mengeliminasi kebutuhan SMS OTP berulang.", body_style))
    story.append(Spacer(1, 6))

    # Bab 3: Scoring Model Table
    story.append(Paragraph("3. Algoritma Dynamic Security Scoring (0–100%)", h1_style))
    sc_table = [
        [Paragraph("<b>Dimensi Keamanan</b>", th_style), Paragraph("<b>Bobot</b>", th_style), Paragraph("<b>Kriteria Skor Penuh</b>", th_style), Paragraph("<b>Dampak Kegagalan</b>", th_style)],
        [Paragraph("Antivirus Real-time Shield", body_style), Paragraph("25%", body_style), Paragraph("Proteksi memori & file Kaspersky aktif", body_style), Paragraph("-25% jika nonaktif", body_style)],
        [Paragraph("Recent Malware Scan", body_style), Paragraph("20%", body_style), Paragraph("Scan penuh dalam 72 jam terakhir", body_style), Paragraph("-20% jika scan usang", body_style)],
        [Paragraph("Web Protection & Anti-Phishing", body_style), Paragraph("15%", body_style), Paragraph("Safe Browsing aktif mencegah tautan palsu", body_style), Paragraph("-15% jika filter mati", body_style)],
        [Paragraph("Integritas Sistem (Root/Bootloader)", body_style), Paragraph("15%", body_style), Paragraph("Firmware resmi pabrikan, tanpa su binary", body_style), Paragraph("-15% jika Root / Hooking", body_style)],
        [Paragraph("Wi-Fi & Network Security", body_style), Paragraph("10%", body_style), Paragraph("Terenkripsi WPA2/WPA3, tanpa ARP spoofing", body_style), Paragraph("-10% pada Wi-Fi publik", body_style)],
        [Paragraph("Enkripsi Penyimpanan Gawai", body_style), Paragraph("10%", body_style), Paragraph("Internal storage terenkripsi AES-256", body_style), Paragraph("-10% jika tanpa enkripsi", body_style)],
        [Paragraph("Kunci Layar (Biometrik/PIN)", body_style), Paragraph("5%", body_style), Paragraph("Kunci layar gawai aktif", body_style), Paragraph("-5% tanpa kunci layar", body_style)]
    ]
    t_sc = Table(sc_table, colWidths=[140, 45, 172, 130])
    t_sc.setStyle(TableStyle([('BACKGROUND', (0,0), (-1,0), colors.HexColor("#F1F5F9")), ('GRID', (0,0), (-1,-1), 0.5, c_border), ('VALIGN', (0,0), (-1,-1), 'TOP')]))
    story.append(t_sc)
    story.append(Spacer(1, 4))
    story.append(Paragraph("<b>Ambang Batas Status:</b> Hijau (≥90%) 'Perangkat Sangat Aman' | Amber (70-89%) 'Perlu Optimalisasi' | Merah (<70%) 'Rentan Terhadap Bahaya'.", body_style))

    # Bab 4: Bedah Lengkap Layar Mobile Client (Screenshot & Penjelasan)
    story.append(PageBreak())
    story.append(Paragraph("4. Bedah Lengkap Seluruh Layar Mobile Client (Screenshot & Anatomi UI)", h1_style))
    story.append(Paragraph("Berikut adalah analisis teknis mendalam terhadap setiap layar, tombol, input, dan badge pada aplikasi mobile:", body_style))
    story.append(Spacer(1, 4))

    mobile_screens = [
        ("4.1 Layar Masuk (Login Screen)", "screen_masuk_live.png", [
            "<b>Kolom Input Nomor Ponsel (TextField):</b> Field nomor seluler berawalan kode negara +62. Menerapkan validasi sanitasi spasi dan tanda minus.",
            "<b>Checkbox Persetujuan PDP:</b> Persetujuan Ketentuan Layanan & Privasi (UU PDP). Menjadi prasyarat aktifnya tombol Masuk.",
            "<b>Tombol 'Masuk' (AppButton):</b> Memicu POST /api/v1/auth/request-otp. Menampilkan spinner saat loading.",
            "<b>Chips Preset Uji Coba POC:</b> Tiga tombol cepat uji coba: [Aktif 30 Hari], [Masa Aktif Habis], dan [Belum Terdaftar]."
        ]),
        ("4.2 Wizard Aktivasi Layanan (3 Tahap Visual)", "screen_splash_live.png", [
            "<b>Tahap 1 (Verifikasi Paket NDP):</b> Jika aktif -> auto ke Tahap 2. Jika pending -> status amber <i>'Dalam Proses...'</i> + tombol <b>'Cek Ulang Status'</b>.",
            "<b>Tahap 2 (Ikat Lisensi Kaspersky B2B):</b> Jika sukses -> auto ke Tahap 3. Jika gateway timeout -> status <b>'Sinkronisasi sedang berjalan'</b> + tombol <b>'Lanjut ke Aplikasi'</b> (Graceful Entry).",
            "<b>Tahap 3 (Selesai):</b> Menampilkan <b>ikon centang hijau besar</b> + headline <b>'Perangkat Berhasil Dilindungi'</b> lalu otomatis masuk ke Dashboard.",
            "<b>Toolbar Presets POC:</b> Tombol uji coba interaktif di bagian bawah: [Normal], [Delay 'Dalam Proses...'], dan [Gangguan KSP]."
        ]),
        ("4.3 Dashboard Utama Keamanan", "screen_updated_dashboard.png", [
            "<b>Speedometer Skor Keamanan:</b> Indikator melingkar dinamis persentase keamanan gawai (0–100%) dengan status teks.",
            "<b>Tombol 'Pindai Sekarang':</b> Menjalankan pemindaian kilat antivirus seketika pada partisi memori sistem.",
            "<b>Sakelar Real-Time Protection:</b> Switch toggle untuk menyalakan/mematikan pemantauan file dan aplikasi 24/7.",
            "<b>Grid Fitur Keamanan:</b> Kartu modul Web Protection, Wi-Fi Security, App Lock, dan Antivirus PUA Scanner."
        ]),
        ("4.4 Pemindaian Antivirus (Radar Sweep)", "emulator_live_5.png", [
            "<b>Radar Concentric Sweep:</b> Animasi radar sapuan lingkaran saat memeriksa partisi memori dan berkas internal.",
            "<b>Metrik Item Terpindai:</b> Jumlah file yang telah diperiksa (misal: 1.840 item) dan jumlah ancaman terdeteksi (0 ancaman).",
            "<b>Tombol 'Pindai Ulang':</b> Memulai kembali siklus inspeksi komprehensif gawai."
        ]),
        ("4.5 Audit & Rekomendasi Perangkat", "screen_perangkat.png", [
            "<b>Status Bootloader & Root:</b> Memverifikasi keaslian firmware gawai dan memastikan tidak ada binary su / Magisk.",
            "<b>Enkripsi Penyimpanan:</b> Mengonfirmasi penyimpanan internal terenkripsi perangkat keras AES-256.",
            "<b>Checklist Rekomendasi:</b> Daftar perbaikan konfigurasi keamanan yang perlu dioptimalkan pengguna."
        ]),
        ("4.6 Menu Profil & Sinkronisasi Ulang", "screen_profile_live.png", [
            "<b>Kartu Status Lisensi:</b> Nama paket aktif, sisa masa berlaku (hari), dan kuota gawai terhubung (1/1 gawai).",
            "<b>Tombol 'Sinkronkan Ulang':</b> Muncul khusus saat akun berstatus ACTIVATION_PENDING_KSP untuk memulihkan lisensi.",
            "<b>Tombol 'Keluar dari Akun':</b> Menampilkan dialog konfirmasi untuk membersihkan sesi login 30 hari secara aman."
        ])
    ]

    for title, img_name, bullets in mobile_screens:
        story.append(Paragraph(f"<b>{title}</b>", h2_style))
        img_obj = create_safe_image(os.path.join(img_dir, img_name), 130, 190)
        t_bullets = [Paragraph(f"• {b}", bullet_style) for b in bullets]
        if img_obj:
            t_row = [[img_obj, t_bullets]]
            t_table = Table(t_row, colWidths=[140, 347])
            t_table.setStyle(TableStyle([('VALIGN', (0,0), (-1,-1), 'TOP'), ('BOTTOMPADDING', (0,0), (-1,-1), 0)]))
            story.append(t_table)
        else:
            for b in t_bullets: story.append(b)
        story.append(Spacer(1, 6))

    # Bab 5: Bedah Lengkap Web SOC Dashboard (Screenshot & Penjelasan)
    story.append(PageBreak())
    story.append(Paragraph("5. Bedah Lengkap Web SOC & Admin Dashboard (Screenshot & Anatomi UI)", h1_style))
    story.append(Paragraph("Web SOC Dashboard (:3005) menyajikan visibilitas penuh bagi operator NOC/SOC dan Customer Care Desk:", body_style))
    story.append(Spacer(1, 4))

    dash_tabs = [
        ("5.1 Tab 1: SOC & Threat Monitoring", "soc_dashboard_overview.png", [
            "<b>Live Threat Feed (Panel Kanan):</b> Aliran telemetri ancaman siber yang dicegat Kaspersky secara real-time via SSE Stream.",
            "<b>Tabel Pelanggan Terdaftar (Panel Kiri):</b> Daftar gawai aktif, nomor seluler, model ponsel, dan status proteksi.",
            "<b>Kartu Metrik KPI (Stats Cards):</b> Total Subscribers, Active, Expired, Quota Lisensi Kaspersky (misal: 4/100), Rooted, dan SIM Swap."
        ]),
        ("5.2 Tab 2: Customer Care & License Helpdesk Desk", "soc_dashboard_helpdesk.png", [
            "<b>Kotak Pencarian Multi-Kriteria:</b> Pencarian cepat berdasarkan MSISDN, ID Pelanggan, atau Kode Aktivasi.",
            "<b>Filter Status Tab (Chips):</b> Penyaringan status: ALL, ACTIVATED, PENDING, EXPIRED, DESYNC, dan SMS_FAILED.",
            "<b>Aksi Interaktif Baris:</b> Mengklik baris pelanggan akan membuka Modal Detail Pelanggan untuk audit mendalam."
        ]),
        ("5.3 Modal Detail Pelanggan (Customer Detail Modal)", "soc_dashboard_customer_detail.png", [
            "<b>Tab Overview:</b> Audit nomor fisik kartu SIM (BoundIccid vs CurrentIccid), model gawai, dan versi OS.",
            "<b>Tab Diagnostik:</b> Tombol 'Kirim Ulang Kode SMS' dan tombol 'Sinkronkan Ulang Lisensi Kaspersky'.",
            "<b>Tab Riwayat Pembelian & Log Scan:</b> Memuat daftar order paket data dan 10 riwayat log scan Kaspersky terakhir."
        ]),
        ("5.4 Tab 3: Device Integrity & SIM Watch Desk", "soc_dashboard_device_integrity.png", [
            "<b>Audit SIM Swap:</b> Daftar gawai dengan perbedaan BoundIccid dan CurrentIccid sebagai deteksi pembajakan nomor.",
            "<b>Root & Hooking Watch:</b> Panel pemantau gawai dengan binary superuser atau injeksi hooking Frida.",
            "<b>Panel Karantina:</b> File malware yang berhasil diisolasi oleh modul RASP / Kaspersky."
        ]),
        ("5.5 Tab 4: Telecom NDP & Billing Simulator Desk", "soc_dashboard_ndp_simulator.png", [
            "<b>Formulir Order Paket:</b> Input MSISDN, pilihan paket proteksi (30 Hari / 1 Tahun), dan durasi masa aktif.",
            "<b>Tombol 'Proses Pembelian (NDP Order)':</b> Mengirim webhook simulasi ke backend untuk memperpanjang masa aktif.",
            "<b>Tombol 'Simulasikan Paket Kedaluwarsa':</b> Mengubah tanggal kedaluwarsa ke masa lampau untuk menguji mode pasif SDK.",
            "<b>Tombol 'Reset Database ke Seed Awal':</b> Mengembalikan database pengujian ke kondisi awal pabrikan."
        ])
    ]

    for title, img_name, bullets in dash_tabs:
        story.append(Paragraph(f"<b>{title}</b>", h2_style))
        img_obj = create_safe_image(os.path.join(img_dir, img_name), 260, 160)
        t_bullets = [Paragraph(f"• {b}", bullet_style) for b in bullets]
        if img_obj:
            t_row = [[img_obj, t_bullets]]
            t_table = Table(t_row, colWidths=[270, 217])
            t_table.setStyle(TableStyle([('VALIGN', (0,0), (-1,-1), 'TOP'), ('BOTTOMPADDING', (0,0), (-1,-1), 0)]))
            story.append(t_table)
        else:
            for b in t_bullets: story.append(b)
        story.append(Spacer(1, 6))

    # Bab 6: Panduan Troubleshooting & Error Handling Matrix
    story.append(PageBreak())
    story.append(Paragraph("6. Panduan Troubleshooting & Matriks Penanganan Error (Error Handling Matrix)", h1_style))
    story.append(Paragraph("Matriks penanganan error operasional dan skenario kegagalan jaringan:", body_style))

    err_matrix = [
        [Paragraph("<b>Kode Error</b>", th_style), Paragraph("<b>Tampilan UI Mobile</b>", th_style), Paragraph("<b>Penyebab Utama</b>", th_style), Paragraph("<b>Langkah Remediasi Teknis</b>", th_style)],
        [
            Paragraph("<b>ERR_NDP_DELAY</b>", code_style),
            Paragraph("Badge Amber:<br/><i>'Dalam Proses...'</i>", body_style),
            Paragraph("Paket baru dibeli, sinkronisasi webhook NDP ke database backend belum tuntas.", body_style),
            Paragraph("Pengguna menekan tombol <b>'Cek Ulang Status'</b>. Sistem melakukan query ulang status paket ke endpoint /subscription/check.", body_style)
        ],
        [
            Paragraph("<b>ERR_KSP_TIMEOUT</b>", code_style),
            Paragraph("Badge Amber:<br/><i>'Sinkronisasi sedang berjalan'</i>", body_style),
            Paragraph("Gateway Kaspersky B2B mengalami timeout / antrean penerbitan lisensi.", body_style),
            Paragraph("Pengguna <b>tetap diizinkan masuk</b> (Graceful Entry). Menu Profil menyediakan tombol <b>'Sinkronkan Ulang'</b> untuk retry tanpa re-login.", body_style)
        ],
        [
            Paragraph("<b>ERR_OTP_INVALID</b>", code_style),
            Paragraph("SnackBar Merah:<br/><i>'Kode OTP salah'</i>", body_style),
            Paragraph("Salah ketik kode angka OTP 6-digit.", body_style),
            Paragraph("Ketik ulang kode benar (mode POC: <code>123456</code>). Batas kirim ulang terkunci selama 60 detik.", body_style)
        ],
        [
            Paragraph("<b>ERR_SUB_EXPIRED</b>", code_style),
            Paragraph("Status:<br/><i>'Masa Aktif Habis'</i>", body_style),
            Paragraph("Masa berlaku paket pelanggan telah melewati tanggal jatuh tempo.", body_style),
            Paragraph("Kaspersky SDK beralih ke <b>mode pasif/dormant</b> otomatis (0 konsumsi lisensi). Arahkan pembelian paket di portal seluler.", body_style)
        ],
        [
            Paragraph("<b>ERR_SIM_SWAP</b>", code_style),
            Paragraph("Alert Tingkat Tinggi:<br/><i>'SIM Swap Terdeteksi'</i>", body_style),
            Paragraph("CurrentIccid kartu SIM tidak cocok dengan BoundIccid registrasi.", body_style),
            Paragraph("Skor keamanan diturunkan <70%. Kirim telemetri ancaman ke Next.js SOC Dashboard. Lakukan verifikasi identitas ke Service Desk.", body_style)
        ],
        [
            Paragraph("<b>ERR_ROOT_HOOK</b>", code_style),
            Paragraph("Alert Sistem:<br/><i>'Integritas Terkompromi'</i>", body_style),
            Paragraph("Binari su / Magisk / Frida hooking terdeteksi pada gawai Android.", body_style),
            Paragraph("RASP modul mengisolasi data sensitif, menolak penyimpanan memori lokal, dan mencatat audit di log backend.", body_style)
        ]
    ]
    t_err = Table(err_matrix, colWidths=[90, 95, 142, 160])
    t_err.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), colors.HexColor("#F1F5F9")),
        ('GRID', (0,0), (-1,-1), 0.5, c_border),
        ('VALIGN', (0,0), (-1,-1), 'TOP'),
        ('TOPPADDING', (0,0), (-1,-1), 4),
        ('BOTTOMPADDING', (0,0), (-1,-1), 4)
    ]))
    story.append(t_err)
    story.append(Spacer(1, 8))

    # Bab 7: Laporan Bukti Validasi Pengujian POC (Live Test Results)
    story.append(Paragraph("7. Laporan Bukti Validasi Pengujian POC (Live Test Results)", h1_style))
    story.append(Paragraph("Hasil eksekusi riil pada Golang Core Daemon (:8080) dan Flutter Client:", body_style))

    test_cases = [
        ("Test Case 1: Aktivasi Normal Pelanggan Aktif (081299887766)",
         "POST /api/v1/auth/activate-license -> {'msisdn':'081299887766', 'mobile_id':'TS-MOB-TEST-01'}",
         "{'success':true, 'activation_status':'ACTIVATED', 'stage':3, 'status_message':'Perangkat Berhasil Dilindungi'}",
         "PASS: Ikon centang hijau besar muncul, direct masuk Dashboard."),
        
        ("Test Case 2: Simulasi Delay NDP ('Dalam Proses...')",
         "POST /api/v1/auth/activate-license -> {'simulate_pending_ndp':true}",
         "{'success':false, 'activation_status':'PENDING_PROVISIONING', 'stage':1, 'status_message':'Dalam Proses...'}",
         "PASS: Tampil badge amber 'Dalam Proses...' dan tombol 'Cek Ulang Status'."),
        
        ("Test Case 3: Simulasi Gangguan Server Kaspersky (ACTIVATION_PENDING_KSP)",
         "POST /api/v1/auth/activate-license -> {'simulate_ksp_outage':true}",
         "{'success':true, 'activation_status':'ACTIVATION_PENDING_KSP', 'stage':2, 'status_message':'Sinkronisasi sedang berjalan'}",
         "PASS: Graceful entry aktif. Tombol 'Sinkronkan Ulang' tersedia di Profil."),
        
        ("Test Case 4: Pelanggan Kedaluwarsa (081200000000)",
         "GET /api/v1/subscription/check?msisdn=081200000000",
         "{'is_valid':false, 'is_expired':true, 'message':'Masa aktif telah berakhir'}",
         "PASS: Kaspersky SDK dormant mode, bebas kebocoran lisensi.")
    ]

    for title, req, res, verdict in test_cases:
        story.append(Paragraph(f"<b>{title}</b>", h2_style))
        tc_box = [
            [Paragraph("<b>Request Payload</b>", th_style), Paragraph(f"<code>{req}</code>", code_style)],
            [Paragraph("<b>Response Payload</b>", th_style), Paragraph(f"<code>{res}</code>", code_style)],
            [Paragraph("<b>Verifikasi Hasil</b>", th_style), Paragraph(f"<b>{verdict}</b>", body_style)]
        ]
        t_tc = Table(tc_box, colWidths=[100, 387])
        t_tc.setStyle(TableStyle([('BACKGROUND', (0,0), (0,-1), colors.HexColor("#F8FAFC")), ('GRID', (0,0), (-1,-1), 0.5, c_border), ('VALIGN', (0,0), (-1,-1), 'TOP')]))
        story.append(t_tc)
        story.append(Spacer(1, 3))

    # Bab 8: Kepatuhan Standar Rekayasa
    story.append(Paragraph("8. Kepatuhan Standar Rekayasa & Keamanan", h1_style))
    story.append(Paragraph("• <b>Modularitas & 400-Line Limit:</b> Seluruh 9 berkas kode aplikasi dipelihara strictly di bawah 400 baris (contoh: <code>aktivasi_layanan_screen.dart</code> 195 baris, <code>activation_stage_card.dart</code> 297 baris).", body_style))
    story.append(Paragraph("• <b>Static Analysis Pass:</b> Verifikasi <code>flutter analyze</code> menghasilkan <b>0 Issues Found</b> (clean lint & compiler).", body_style))
    story.append(Paragraph("• <b>Keamanan Kredensial:</b> Zero hardcoded secrets, enkripsi payload, dan sanitasi query URL parameter.", body_style))

    doc.build(story, canvasmaker=NumberedCanvas)
    print(f"[MASTER PDF BUILDER] Master Technical POC Report generated: {pdf_path}")

if __name__ == "__main__":
    build_pdf()
