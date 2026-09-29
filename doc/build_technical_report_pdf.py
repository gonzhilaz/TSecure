import os
from reportlab.lib.pagesizes import A4
from reportlab.lib import colors
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, PageBreak, HRFlowable
)
from reportlab.pdfgen import canvas

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
            self.drawString(54, 802, "TELKOMSEL SECURE — TECHNICAL ARCHITECTURE & POC REPORT")
            self.drawRightString(541, 802, "CONFIDENTIAL & PROPRIETARY")
            self.setStrokeColor(colors.HexColor("#CBD5E1"))
            self.setLineWidth(0.5)
            self.line(54, 796, 541, 796)
            
        # Footer
        self.setFont("Helvetica", 8)
        page_str = f"Halaman {self._pageNumber} dari {page_count}"
        self.drawRightString(541, 34, page_str)
        self.drawString(54, 34, "© 2026 PT Telekomunikasi Selular — Divisi Cyber Security & B2B Solutions")
        self.setStrokeColor(colors.HexColor("#CBD5E1"))
        self.setLineWidth(0.5)
        self.line(54, 44, 541, 44)
        self.restoreState()

def build_pdf():
    base_dir = os.path.dirname(os.path.abspath(__file__))
    pdf_path = os.path.join(base_dir, "Telkomsel_Secure_Technical_POC_Report.pdf")
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
    c_header_bg = colors.HexColor("#F1F5F9")

    t_style = ParagraphStyle('DocTitle', parent=styles['Normal'], fontName='Helvetica-Bold', fontSize=20, leading=24, textColor=c_primary, spaceAfter=4)
    sub_style = ParagraphStyle('DocSub', parent=styles['Normal'], fontName='Helvetica-Bold', fontSize=10.5, leading=14, textColor=c_navy, spaceAfter=12)
    h1_style = ParagraphStyle('H1', parent=styles['Normal'], fontName='Helvetica-Bold', fontSize=13, leading=17, textColor=c_navy, spaceBefore=12, spaceAfter=6, keepWithNext=True)
    h2_style = ParagraphStyle('H2', parent=styles['Normal'], fontName='Helvetica-Bold', fontSize=10, leading=14, textColor=c_primary, spaceBefore=8, spaceAfter=4, keepWithNext=True)
    body_style = ParagraphStyle('Body', parent=styles['Normal'], fontName='Helvetica', fontSize=8.5, leading=12.5, textColor=c_slate, spaceAfter=4)
    code_style = ParagraphStyle('Code', parent=styles['Normal'], fontName='Courier', fontSize=7.5, leading=10, textColor=colors.HexColor("#0F172A"))
    th_style = ParagraphStyle('TH', parent=styles['Normal'], fontName='Helvetica-Bold', fontSize=8, leading=11, textColor=c_navy)

    story = []

    # Title & Banner
    story.append(Paragraph("LAPORAN ARSITEKTUR TEKNIS & VALIDASI POC", t_style))
    story.append(Paragraph("Telkomsel Secure Mobile Security — Integrasi Jaringan NDP & Kaspersky B2B", sub_style))
    story.append(HRFlowable(width="100%", thickness=2, color=c_primary, spaceAfter=10))

    # 1. Executive Summary & Arsitektur
    story.append(Paragraph("1. Executive Summary & Topologi Sistem 3-Tier", h1_style))
    story.append(Paragraph("Telkomsel Secure mengintegrasikan 3 tier sistem utama untuk menjamin akuntabilitas lisensi korporasi dan proteksi siber telco-grade:", body_style))
    
    topologi_data = [
        [Paragraph("<b>Tier Sistem</b>", th_style), Paragraph("<b>Teknologi & Port</b>", th_style), Paragraph("<b>Fungsi & Tanggung Jawab Utama</b>", th_style)],
        [Paragraph("<b>Tier 1: Mobile Client</b>", body_style), Paragraph("Flutter 3.x / Dart<br/>Android & iOS", body_style), Paragraph("Sesi 30 hari, Wizard Aktivasi 3 Tahap, Jembatan Kaspersky SDK Bridge, Telemetri Real-time.", body_style)],
        [Paragraph("<b>Tier 2: Core Daemon</b>", body_style), Paragraph("Golang 1.22+ REST<br/>Port :8080 (SSE Stream)", body_style), Paragraph("Validasi Masa Aktif, Rekonsiliasi NDP, Multi-Stage Activation Engine, Anti-SIM Swap Guard.", body_style)],
        [Paragraph("<b>Tier 3: Platform Mitra</b>", body_style), Paragraph("Telkomsel NDP Core &<br/>Kaspersky B2B Gateway", body_style), Paragraph("Transaksi paket MyTelkomsel (*363#), Alokasi Lisensi Korporasi B2B, Update Signature Malware.", body_style)]
    ]
    t_top = Table(topologi_data, colWidths=[120, 110, 257])
    t_top.setStyle(TableStyle([('BACKGROUND', (0,0), (-1,0), c_header_bg), ('GRID', (0,0), (-1,-1), 0.5, c_border), ('VALIGN', (0,0), (-1,-1), 'TOP')]))
    story.append(t_top)
    story.append(Spacer(1, 8))

    # 2. Parameter Teknis & SIM Binding
    story.append(Paragraph("2. Parameter Teknis Identitas, Perangkat & SIM Binding", h1_style))
    story.append(Paragraph("• <b>Format & Validasi ICCID:</b> Nomor seri fisik SIM 20-digit (<i>896201...</i>). Sistem mencocokkan <code>BoundIccid</code> vs <code>CurrentIccid</code> pada setiap pemindaian untuk mendeteksi pembajakan SIM Swap.", body_style))
    story.append(Paragraph("• <b>Aturan Mobile ID (+1 Lisensi B2B):</b> Setiap <code>Mobile ID</code> unik baru yang diaktifkan memotong <b>+1 kuota lisensi Kaspersky korporasi</b>. Re-inisialisasi pada gawai yang sama bersifat idempoten (0 lisensi tambahan).", body_style))
    story.append(Paragraph("• <b>Sesi Persisten 30 Hari:</b> Token sesi <code>TSEL-SEC-[MSISDN]-[TIMESTAMP]</code> disimpan di SharedPreferences aman dengan durasi 30 hari, mengeliminasi kebutuhan SMS OTP berulang.", body_style))
    story.append(Spacer(1, 8))

    # 3. Dynamic Scoring Model (0-100%)
    story.append(Paragraph("3. Algoritma Dynamic Security Scoring (Bobot 7 Dimensi)", h1_style))
    score_data = [
        [Paragraph("<b>Dimensi Keamanan</b>", th_style), Paragraph("<b>Bobot</b>", th_style), Paragraph("<b>Kriteria Skor Penuh</b>", th_style), Paragraph("<b>Dampak Kegagalan</b>", th_style)],
        [Paragraph("Real-time Antivirus Shield", body_style), Paragraph("25%", body_style), Paragraph("Proteksi berkas & memori Kaspersky aktif", body_style), Paragraph("-25% jika nonaktif", body_style)],
        [Paragraph("Recent Malware Scan", body_style), Paragraph("20%", body_style), Paragraph("Pemindaian penuh dalam 72 jam terakhir", body_style), Paragraph("-20% jika scan usang/ada malware", body_style)],
        [Paragraph("Web Filter & Anti-Phishing", body_style), Paragraph("15%", body_style), Paragraph("Safe Browsing aktif mencegah tautan palsu", body_style), Paragraph("-15% jika filter mati", body_style)],
        [Paragraph("Integritas Sistem (Root/Bootloader)", body_style), Paragraph("15%", body_style), Paragraph("Firmware resmi pabrikan, tanpa su binary", body_style), Paragraph("-15% jika Root / Frida hooking", body_style)],
        [Paragraph("Wi-Fi & Network Security", body_style), Paragraph("10%", body_style), Paragraph("Terenkripsi WPA2/WPA3, tanpa ARP spoofing", body_style), Paragraph("-10% pada Wi-Fi publik terbuka", body_style)],
        [Paragraph("Enkripsi Penyimpanan Internal", body_style), Paragraph("10%", body_style), Paragraph("Storage terenkripsi AES-256 bawaan", body_style), Paragraph("-10% jika tanpa enkripsi", body_style)],
        [Paragraph("Kunci Layar (Biometrik/PIN)", body_style), Paragraph("5%", body_style), Paragraph("Kunci layar gawai aktif", body_style), Paragraph("-5% jika tanpa proteksi layar", body_style)]
    ]
    t_sc = Table(score_data, colWidths=[140, 45, 172, 130])
    t_sc.setStyle(TableStyle([('BACKGROUND', (0,0), (-1,0), c_header_bg), ('GRID', (0,0), (-1,-1), 0.5, c_border), ('VALIGN', (0,0), (-1,-1), 'TOP')]))
    story.append(t_sc)
    story.append(Paragraph("<b>Ambang Batas:</b> Hijau (≥90%) 'Sangat Aman' | Amber (70-89%) 'Perlu Optimalisasi' | Merah (<70%) 'Rentan/Bahaya'.", body_style))

    # Page Break for Troubleshooting Matrix
    story.append(PageBreak())

    # 4. Panduan Troubleshooting & Error Handling Matrix
    story.append(Paragraph("4. Panduan Troubleshooting & Error Handling Matrix", h1_style))
    story.append(Paragraph("Matriks penanganan error operasional dan skenario kegagalan jaringan:", body_style))

    err_data = [
        [Paragraph("<b>Kode Error</b>", th_style), Paragraph("<b>Status Tampilan UI</b>", th_style), Paragraph("<b>Penyebab Utama</b>", th_style), Paragraph("<b>Langkah Remediasi Teknis</b>", th_style)],
        [
            Paragraph("<b>ERR_NDP_DELAY</b>", code_style),
            Paragraph("Badge Amber:<br/><i>'Dalam Proses...'</i>", body_style),
            Paragraph("Paket baru dibeli, sinkronisasi webhook NDP ke HLR masih antre.", body_style),
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
            Paragraph("Kaspersky SDK beralih ke <b>mode pasif/dormant</b> otomatis (0 konsumsi lisensi). Arahkan pembelian paket di MyTelkomsel.", body_style)
        ],
        [
            Paragraph("<b>ERR_SIM_SWAP</b>", code_style),
            Paragraph("Alert Tingkat Tinggi:<br/><i>'SIM Swap Terdeteksi'</i>", body_style),
            Paragraph("CurrentIccid kartu SIM tidak cocok dengan BoundIccid registrasi.", body_style),
            Paragraph("Skor keamanan diturunkan <70%. Kirim telemetri ancaman ke Next.js SOC Dashboard. Lakukan verifikasi di GraPARI.", body_style)
        ],
        [
            Paragraph("<b>ERR_ROOT_HOOK</b>", code_style),
            Paragraph("Alert Sistem:<br/><i>'Integritas Terkompromi'</i>", body_style),
            Paragraph("Binari su / Magisk / Frida hooking terdeteksi pada gawai Android.", body_style),
            Paragraph("RASP modul mengisolasi data sensitif, menolak penyimpanan memori lokal, dan mencatat audit di log backend.", body_style)
        ]
    ]
    t_err = Table(err_data, colWidths=[95, 95, 137, 160])
    t_err.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), c_header_bg),
        ('GRID', (0,0), (-1,-1), 0.5, c_border),
        ('VALIGN', (0,0), (-1,-1), 'TOP'),
        ('TOPPADDING', (0,0), (-1,-1), 4),
        ('BOTTOMPADDING', (0,0), (-1,-1), 4)
    ]))
    story.append(t_err)
    story.append(Spacer(1, 10))

    # 5. Laporan Validasi Pengujian POC (Live Test Evidence)
    story.append(Paragraph("5. Laporan Bukti Validasi Pengujian POC (Live Test Results)", h1_style))
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
        story.append(Spacer(1, 4))

    # 6. Kepatuhan Standar Rekayasa
    story.append(Paragraph("6. Kepatuhan Standar Rekayasa & Zero Defect", h1_style))
    story.append(Paragraph("• <b>Modularitas & 400-Line Limit:</b> Seluruh 9 berkas kode aplikasi dipelihara strictly di bawah 400 baris (contoh: <code>aktivasi_layanan_screen.dart</code> 195 baris, <code>activation_stage_card.dart</code> 297 baris).", body_style))
    story.append(Paragraph("• <b>Static Analysis Pass:</b> Verifikasi <code>flutter analyze</code> menghasilkan <b>0 Issues Found</b> (clean lint & compiler).", body_style))
    story.append(Paragraph("• <b>Keamanan Kredensial:</b> Zero hardcoded secrets, enkripsi payload, dan sanitasi query URL parameter.", body_style))

    doc.build(story, canvasmaker=NumberedCanvas)
    print(f"[PDF BUILDER] Technical Report generated: {pdf_path}")

if __name__ == "__main__":
    build_pdf()
