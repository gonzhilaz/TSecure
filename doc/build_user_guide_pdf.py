import os
from reportlab.lib.pagesizes import A4
from reportlab.lib import colors
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, Image as RLImage, Table, TableStyle, PageBreak, KeepTogether, HRFlowable
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
            self.drawString(54, 802, "TELKOMSEL SECURE — PANDUAN PENGGUNA RESMI")
            self.drawRightString(541, 802, "VERSI 1.0.0 (2026)")
            self.setStrokeColor(colors.HexColor("#CBD5E1"))
            self.setLineWidth(0.5)
            self.line(54, 796, 541, 796)
            
        # Footer
        self.setFont("Helvetica", 8)
        page_str = f"Halaman {self._pageNumber} dari {page_count}"
        self.drawRightString(541, 34, page_str)
        self.drawString(54, 34, "© 2026 PT Telekomunikasi Selular — Rahasia & Terbatas untuk Pelanggan Telkomsel")
        self.setStrokeColor(colors.HexColor("#CBD5E1"))
        self.setLineWidth(0.5)
        self.line(54, 44, 541, 44)
        self.restoreState()

def create_safe_image(img_path, max_w=200, max_h=280):
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
    pdf_path = os.path.join(base_dir, "Telkomsel_Secure_User_Guide.pdf")
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
    c_emerald = colors.HexColor("#10B981")
    c_border = colors.HexColor("#E2E8F0")

    t_style = ParagraphStyle('DocTitle', parent=styles['Normal'], fontName='Helvetica-Bold', fontSize=22, leading=26, textColor=c_primary, spaceAfter=6)
    sub_style = ParagraphStyle('DocSub', parent=styles['Normal'], fontName='Helvetica-Bold', fontSize=11, leading=15, textColor=c_navy, spaceAfter=14)
    h1_style = ParagraphStyle('H1', parent=styles['Normal'], fontName='Helvetica-Bold', fontSize=14, leading=18, textColor=c_navy, spaceBefore=14, spaceAfter=8, keepWithNext=True)
    h2_style = ParagraphStyle('H2', parent=styles['Normal'], fontName='Helvetica-Bold', fontSize=11, leading=15, textColor=c_primary, spaceBefore=10, spaceAfter=4, keepWithNext=True)
    body_style = ParagraphStyle('Body', parent=styles['Normal'], fontName='Helvetica', fontSize=9.5, leading=14.5, textColor=c_slate, spaceAfter=6)
    bullet_style = ParagraphStyle('Bullet', parent=styles['Normal'], fontName='Helvetica', fontSize=9, leading=13.5, textColor=c_muted, leftIndent=12, firstLineIndent=-8, spaceAfter=3)
    callout_style = ParagraphStyle('Callout', parent=styles['Normal'], fontName='Helvetica-Bold', fontSize=9.5, leading=14, textColor=c_navy)

    story = []

    # Cover Title & Banner
    story.append(Paragraph("TELKOMSEL SECURE MOBILE APP", t_style))
    story.append(Paragraph("Panduan Lengkap Pengguna & Visual User Journey", sub_style))
    story.append(HRFlowable(width="100%", thickness=2.5, color=c_primary, spaceAfter=14))

    intro = ("<b>Telkomsel Secure</b> adalah solusi keamanan perangkat bergerak resmi yang dirancang khusus "
             "untuk pelanggan Telkomsel. Bertenaga integrasi <b>Kaspersky B2B Mobile Security SDK</b> dan kecerdasan "
             "jaringan <b>Telkomsel Network Data Platform (NDP)</b>, aplikasi ini memberikan perlindungan komprehensif "
             "terhadap virus, phishing, Wi-Fi berbahaya, dan kebocoran data privasi.")
    story.append(Paragraph(intro, body_style))
    story.append(Spacer(1, 8))

    # Bab 1: Splash & Sesi 30 Hari
    story.append(Paragraph("1. Memulai Aplikasi: Splash Screen & Sesi Otomatis 30 Hari", h1_style))
    splash_img = create_safe_image(os.path.join(base_dir, "images", "screen_splash_live.png"), 150, 240)
    splash_text = [
        Paragraph("<b>Validasi Cepat pada Splash Screen:</b>", h2_style),
        Paragraph("Saat membuka aplikasi, sistem memeriksa integritas gawai dan status paket langganan secara instan.", body_style),
        Paragraph("<b>Kenyamanan Sesi 30 Hari (MyTelkomsel Standard):</b>", h2_style),
        Paragraph("• Sesi login tersimpan aman selama 30 hari di ponsel Anda.", bullet_style),
        Paragraph("• Tidak perlu meminta dan mengetik kode SMS OTP berulang kali.", bullet_style),
        Paragraph("• Membuka aplikasi langsung membawa Anda ke Dashboard.", bullet_style)
    ]
    if splash_img:
        row1 = [[splash_img, splash_text]]
        t1 = Table(row1, colWidths=[160, 327])
        t1.setStyle(TableStyle([('VALIGN', (0,0), (-1,-1), 'TOP'), ('BOTTOMPADDING', (0,0), (-1,-1), 0)]))
        story.append(t1)
    else:
        for p in splash_text: story.append(p)
    story.append(Spacer(1, 12))

    # Bab 2: Masuk & OTP
    story.append(Paragraph("2. Masuk & Autentikasi SMS OTP 6-Digit", h1_style))
    masuk_img = create_safe_image(os.path.join(base_dir, "images", "screen_masuk_live.png"), 150, 240)
    masuk_text = [
        Paragraph("<b>Langkah Autentikasi Nomor Ponsel:</b>", h2_style),
        Paragraph("1. Masukkan nomor seluler Telkomsel Anda (+62 / 08xx).", bullet_style),
        Paragraph("2. Setujui Ketentuan Layanan & Kebijakan Privasi (UU PDP No. 27/2022).", bullet_style),
        Paragraph("3. Tekan tombol <b>'Masuk'</b> untuk meminta kode SMS OTP.", bullet_style),
        Paragraph("4. Masukkan 6 digit kode OTP yang diterima melalui SMS.", bullet_style),
        Paragraph("5. Tombol 'Kirim Ulang' aktif setelah hitungan mundur 60 detik.", bullet_style),
        Paragraph("6. Tekan <b>'Verifikasi & Lanjutkan'</b>.", bullet_style)
    ]
    if masuk_img:
        row2 = [[masuk_img, masuk_text]]
        t2 = Table(row2, colWidths=[160, 327])
        t2.setStyle(TableStyle([('VALIGN', (0,0), (-1,-1), 'TOP'), ('BOTTOMPADDING', (0,0), (-1,-1), 0)]))
        story.append(t2)
    else:
        for p in masuk_text: story.append(p)
    story.append(Spacer(1, 14))

    # Bab 3: Wizard Aktivasi Layanan 3 Tahap
    story.append(PageBreak())
    story.append(Paragraph("3. Wizard Aktivasi Layanan (3 Tahap Visual)", h1_style))
    wizard_intro = ("Bagi pelanggan yang baru membeli paket atau mengikat perangkat baru, aplikasi secara "
                    "otomatis memandu proses aktivasi dengan penanganan status yang sangat transparan:")
    story.append(Paragraph(wizard_intro, body_style))
    story.append(Spacer(1, 6))

    # Table of 3 stages
    stage_data = [
        [Paragraph("<b>Tahapan Aktivasi</b>", callout_style), Paragraph("<b>Kondisi Normal</b>", callout_style), Paragraph("<b>Kondisi Gangguan / Delay Jaringan</b>", callout_style)],
        [
            Paragraph("<b>Tahap 1:<br/>Verifikasi Paket MyTelkomsel</b>", body_style),
            Paragraph("Paket aktif terdeteksi. Centang hijau muncul dan <b>otomatis lanjut ke Tahap 2</b>.", body_style),
            Paragraph("Sinkronisasi jaringan NDP tertunda: Tampil status <b>'Dalam Proses...'</b> lengkap dengan tombol <b>'Cek Ulang Status'</b>.", body_style)
        ],
        [
            Paragraph("<b>Tahap 2:<br/>Ikat Lisensi Kaspersky B2B</b>", body_style),
            Paragraph("Mobile ID diikat ke lisensi korporasi. Proteksi aktif penuh dan <b>lanjut ke Tahap 3</b>.", body_style),
            Paragraph("Server Kaspersky timeout/gangguan: Status <b>'Sinkronisasi sedang berjalan'</b>. Pengguna <b>tetap diizinkan masuk</b>, dan tombol <b>'Sinkronkan Ulang'</b> tersedia di menu Profil.", body_style)
        ],
        [
            Paragraph("<b>Tahap 3:<br/>Selesai</b>", body_style),
            Paragraph("Menampilkan <b>ikon centang hijau besar</b> dengan headline <b>'Perangkat Berhasil Dilindungi'</b> lalu otomatis masuk ke Dashboard.", body_style),
            Paragraph("Tanpa teks tambahan yang berlebihan, memastikan alur mulus dan bersih bagi pengguna.", body_style)
        ]
    ]
    st_table = Table(stage_data, colWidths=[140, 170, 177])
    st_table.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), colors.HexColor("#F1F5F9")),
        ('GRID', (0,0), (-1,-1), 0.5, c_border),
        ('VALIGN', (0,0), (-1,-1), 'TOP'),
        ('TOPPADDING', (0,0), (-1,-1), 6),
        ('BOTTOMPADDING', (0,0), (-1,-1), 6),
    ]))
    story.append(st_table)
    story.append(Spacer(1, 14))

    # Bab 4: Dashboard Utama & Fitur Perlindungan
    story.append(Paragraph("4. Fitur Utama Dashboard Keamanan", h1_style))
    dash_img = create_safe_image(os.path.join(base_dir, "images", "screen_updated_dashboard.png"), 150, 240)
    dash_text = [
        Paragraph("<b>Indikator Skor Keamanan (0% – 100%):</b>", h2_style),
        Paragraph("• <b>≥ 90% (Hijau)</b>: Perangkat Sangat Aman & optimal.", bullet_style),
        Paragraph("• <b>70% – 89% (Amber)</b>: Tindakan optimalisasi tersedia.", bullet_style),
        Paragraph("• <b>< 70% (Merah)</b>: Perangkat rentan terhadap bahaya.", bullet_style),
        Paragraph("<b>Fitur Proteksi Unggulan:</b>", h2_style),
        Paragraph("• <b>Quick Scan Antivirus</b>: Memindai memori & file dalam 5–15 detik.", bullet_style),
        Paragraph("• <b>Web Protection</b>: Blokir phishing & tautan palsu perbankan.", bullet_style),
        Paragraph("• <b>Wi-Fi Security</b>: Audit enkripsi router publik secara real-time.", bullet_style)
    ]
    if dash_img:
        row3 = [[dash_img, dash_text]]
        t3 = Table(row3, colWidths=[160, 327])
        t3.setStyle(TableStyle([('VALIGN', (0,0), (-1,-1), 'TOP'), ('BOTTOMPADDING', (0,0), (-1,-1), 0)]))
        story.append(t3)
    else:
        for p in dash_text: story.append(p)
    story.append(Spacer(1, 12))

    # Bab 5: Profil & Sinkronisasi Ulang
    story.append(PageBreak())
    story.append(Paragraph("5. Menu Profil & Fitur 'Sinkronkan Ulang'", h1_style))
    prof_img = create_safe_image(os.path.join(base_dir, "images", "screen_profile_live.png"), 150, 240)
    prof_text = [
        Paragraph("<b>Informasi Kartu Lisensi:</b>", h2_style),
        Paragraph("Menyajikan nama paket, sisa hari aktif perlindungan, dan kuota perangkat terikat (1/1 gawai).", body_style),
        Paragraph("<b>Fitur 'Sinkronkan Ulang' di Profil:</b>", h2_style),
        Paragraph("Bila aktivasi awal sempat tertunda akibat server lisensi sibuk:", body_style),
        Paragraph("1. Buka tab <b>Profil</b>.", bullet_style),
        Paragraph("2. Temukan kartu lisensi berstatus <i>'Sinkronisasi sedang berjalan'</i>.", bullet_style),
        Paragraph("3. Tekan tombol <b>'Sinkronkan Ulang'</b>.", bullet_style),
        Paragraph("4. Proteksi Kaspersky aktif seketika tanpa perlu login ulang.", bullet_style),
        Paragraph("<b>Keluar dari Akun (Logout):</b>", h2_style),
        Paragraph("Tombol keluar berada di dasar halaman untuk mengakhiri sesi 30 hari secara aman.", body_style)
    ]
    if prof_img:
        row4 = [[prof_img, prof_text]]
        t4 = Table(row4, colWidths=[160, 327])
        t4.setStyle(TableStyle([('VALIGN', (0,0), (-1,-1), 'TOP'), ('BOTTOMPADDING', (0,0), (-1,-1), 0)]))
        story.append(t4)
    else:
        for p in prof_text: story.append(p)

    doc.build(story, canvasmaker=NumberedCanvas)
    print(f"[PDF BUILDER] User Guide generated: {pdf_path}")

if __name__ == "__main__":
    build_pdf()
