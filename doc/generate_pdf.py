import os
from reportlab.lib.pagesizes import letter, A4
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
        self.setFont("Helvetica", 9)
        self.setFillColor(colors.HexColor("#6B7280"))
        
        # Header (pages > 1)
        if self._pageNumber > 1:
            self.drawString(54, 800, "TelkomSecure — Panduan Pengguna Resmi")
            self.setStrokeColor(colors.HexColor("#E5E7EB"))
            self.setLineWidth(0.5)
            self.line(54, 792, 541, 792)
            
        # Footer
        page_str = f"Halaman {self._pageNumber} dari {page_count}"
        self.drawRightString(541, 36, page_str)
        self.drawString(54, 36, "© 2026 PT Telekomunikasi Selular — Rahasia & Terbatas")
        self.setStrokeColor(colors.HexColor("#E5E7EB"))
        self.setLineWidth(0.5)
        self.line(54, 48, 541, 48)
        self.restoreState()

def build_pdf():
    pdf_path = os.path.abspath(r"d:\DEVELOPMENT\Projects\Riski\TelkomSecure\doc\User_Tutorial.pdf")
    doc = SimpleDocTemplate(
        pdf_path,
        pagesize=A4,
        leftMargin=54,
        rightMargin=54,
        topMargin=54,
        bottomMargin=54
    )

    styles = getSampleStyleSheet()
    primary_color = colors.HexColor("#EE2E24")
    navy_color = colors.HexColor("#001A41")
    slate_dark = colors.HexColor("#1F2937")
    slate_muted = colors.HexColor("#4B5563")

    title_style = ParagraphStyle(
        'DocTitle',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=24,
        leading=28,
        textColor=primary_color,
        spaceAfter=8
    )

    subtitle_style = ParagraphStyle(
        'DocSubtitle',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=12,
        leading=16,
        textColor=navy_color,
        spaceAfter=20
    )

    h1_style = ParagraphStyle(
        'Heading1Custom',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=16,
        leading=20,
        textColor=navy_color,
        spaceBefore=14,
        spaceAfter=8,
        keepWithNext=True
    )

    body_style = ParagraphStyle(
        'BodyCustom',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=10,
        leading=15,
        textColor=slate_dark,
        spaceAfter=8
    )

    bullet_style = ParagraphStyle(
        'BulletCustom',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=9.5,
        leading=14,
        textColor=slate_muted,
        leftIndent=15,
        firstLineIndent=-10,
        spaceAfter=4
    )

    story = []

    # Title & Header Banner
    story.append(Paragraph("TelkomSecure (Telkomsel Mobile Security)", title_style))
    story.append(Paragraph("Panduan Pengguna & Pengoperasian Fitur Keamanan Seluler Korporasi", subtitle_style))
    story.append(HRFlowable(width="100%", thickness=2, color=primary_color, spaceAfter=18))

    intro_p = ("TelkomSecure menghadirkan proteksi tingkat korporasi untuk perangkat pintar Android Anda. "
               "Dilengkapi mesin antivirus terdepan Kaspersky B2B Mobile SDK, verifikasi lisensi real-time, "
               "serta perlindungan ancaman jaringan, web filter, dan integritas sistem operasi tanpa mengorbankan performa.")
    story.append(Paragraph(intro_p, body_style))
    story.append(Spacer(1, 10))

    sections = [
        {
            "num": "1. Memulai Aplikasi & Masuk Akun",
            "desc": ("Aplikasi menggunakan sistem autentikasi instan jaringan seluler Telkomsel tanpa perlu menunggu kode OTP melalui SMS, menjamin kenyamanan dan kecepatan login yang aman."),
            "image": r"d:\DEVELOPMENT\Projects\Riski\TelkomSecure\doc\images\emulator_live_1.png",
            "bullets": [
                "<b>Input Nomor Ponsel</b>: Masukkan nomor kartu Telkomsel Anda (+62) pada kolom nomor ponsel.",
                "<b>Persetujuan Regulasi UU PDP</b>: Centang kotak persetujuan Ketentuan Layanan & Kebijakan Privasi Telkomsel (UU PDP No. 27/2022).",
                "<b>Tombol Masuk</b>: Tekan tombol merah 'Masuk' untuk memulai validasi akun ke server Telkomsel B2B.",
                "<b>Bantuan GraPARI 188 / Veronika</b>: Tautan cepat di bagian bawah jika memerlukan bantuan layanan pelanggan."
            ]
        },
        {
            "num": "2. Dasbor Utama & Indikator Reputasi",
            "desc": ("Halaman Dasbor Utama menyajikan visibilitas seketika mengenai tingkat reputasi keamanan gawai dan modul proteksi aktif."),
            "image": r"d:\DEVELOPMENT\Projects\Riski\TelkomSecure\doc\images\screen_updated_dashboard.png",
            "bullets": [
                "<b>Speedometer Reputasi (100% Terlindungi)</b>: Menampilkan status kesehatan sistem dengan visualisasi gauge melingkar gradien merah-hijau.",
                "<b>Status Perangkat & Tombol Pindai</b>: Memberikan ringkasan audit terakhir serta tombol pintas pemindaian kilat.",
                "<b>Sakelar Proteksi Real-Time</b>: Toggle proteksi pencegatan trojan, ransomware, dan malware 24/7.",
                "<b>Grid Fitur Keamanan</b>: Akses cepat ke Web Filter, Realtime Scan, PUA Scanner, dan Keamanan Wi-Fi."
            ]
        },
        {
            "num": "3. Pemindaian Menyeluruh (Scan Penuh)",
            "desc": ("Melakukan inspeksi mendalam terhadap seluruh partisi sistem operasi, penyimpanan internal, dan direktori berkas aplikasi."),
            "image": r"d:\DEVELOPMENT\Projects\Riski\TelkomSecure\doc\images\emulator_live_5.png",
            "bullets": [
                "<b>Radar Concentric Sweep</b>: Visualisasi pemindaian radar penuh dengan sapuan target melingkar merah khas Telkomsel.",
                "<b>Indikator 100% Sangat Aman</b>: Memberikan konfirmasi bahwa seluruh modul sistem bersih dari tanda tangan virus.",
                "<b>Tombol Pindai Ulang</b>: Menjalankan kembali siklus inspeksi berkas terbaru secara komprehensif.",
                "<b>Filter & Navigasi</b>: Opsi kustomisasi cakupan pemindaian pada sudut kanan atas."
            ]
        },
        {
            "num": "4. Audit & Rekomendasi Keamanan Perangkat",
            "desc": ("Halaman Perangkat merekam telemetri aktivitas perlindungan berkelanjutan dan daftar rekomendasi konfigurasi keamanan yang perlu diperhatikan."),
            "image": r"d:\DEVELOPMENT\Projects\Riski\TelkomSecure\doc\images\screen_perangkat.png",
            "bullets": [
                "<b>Waktu Terakhir Scan</b>: Kartu status riwayat audit disertai tombol 'Pindai Perangkat'.",
                "<b>Proteksi Aktif & Diagram Batang</b>: Diagram batang 6-fase menampilkan metrik kestabilan pengawasan sistem secara visual.",
                "<b>Daftar Rekomendasi & Status</b>: Checklist rekomendasi keamanan mulai dari audit bootloader, verifikasi enkripsi data, hingga audit sandi akun.",
                "<b>Tombol Tindakan Pindai/Cek</b>: Mengeksekusi penanganan rekomendasi secara mandiri dalam satu ketukan."
            ]
        },
        {
            "num": "5. Riwayat Aktivitas Keamanan",
            "desc": ("Buku log komprehensif atas seluruh aksi pencegahan, deteksi, dan audit yang telah dieksekusi oleh TelkomSecure."),
            "image": r"d:\DEVELOPMENT\Projects\Riski\TelkomSecure\doc\images\screen_riwayat.png",
            "bullets": [
                "<b>Ringkasan Bulan Ini</b>: Menampilkan metrik agregat pemindaian bulanan dan efektivitas eliminasi ancaman (0 ancaman).",
                "<b>Filter Kategori (Chips)</b>: Kemudahan menyaring aktivitas berdasarkan kategori: Semua, Pemindaian, Jaringan & Web, atau Aplikasi.",
                "<b>Pengelompokan Kronologis</b>: Riwayat terstruktur berdasarkan urutan hari (Hari Ini, Kemarin) lengkap dengan stempel waktu WIB."
            ]
        },
        {
            "num": "6. Profil Pengguna & Pengelolaan Lisensi Korporasi",
            "desc": ("Menampilkan identitas pelanggan, tingkatan loyalitas Telkomsel Halo, dan rincian alokasi lisensi keamanan B2B yang sedang aktif."),
            "image": r"d:\DEVELOPMENT\Projects\Riski\TelkomSecure\doc\images\screen_profil.png",
            "bullets": [
                "<b>Banner Avatar & Identitas</b>: Menampilkan inisial, nama lengkap pengguna, dan status keanggotaan Halo Diamond.",
                "<b>Kartu Lisensi Mobile Security Ultimate</b>: Masa berlaku paket, sisa hari aktif, serta kuota perangkat yang terhubung.",
                "<b>Pengaturan & Keamanan Akun</b>: Konfigurasi notifikasi penting, proteksi biometrik, dan sinkronisasi server korporasi."
            ]
        }
    ]

    for sec in sections:
        story.append(KeepTogether([
            Paragraph(sec["num"], h1_style),
            Paragraph(sec["desc"], body_style),
            Spacer(1, 4)
        ]))

        # Image and Bullets in 2 columns
        img_path = sec["image"]
        if os.path.exists(img_path):
            # Scale image proportionally (target height ~ 220)
            with PILImage.open(img_path) as pi:
                w, h = pi.size
                target_h = 220
                target_w = (w / h) * target_h
            img = RLImage(img_path, width=target_w, height=target_h)
        else:
            img = Paragraph("[Gambar tidak ditemukan]", body_style)

        bullet_paragraphs = [Paragraph(f"• {b}", bullet_style) for b in sec["bullets"]]

        layout_table = Table(
            [[img, bullet_paragraphs]],
            colWidths=[target_w + 14, 487 - (target_w + 14)]
        )
        layout_table.setStyle(TableStyle([
            ('VALIGN', (0,0), (-1,-1), 'TOP'),
            ('LEFTPADDING', (0,0), (-1,-1), 0),
            ('RIGHTPADDING', (0,0), (-1,-1), 0),
            ('BOTTOMPADDING', (0,0), (-1,-1), 0),
            ('TOPPADDING', (0,0), (-1,-1), 0),
        ]))

        story.append(KeepTogether([
            layout_table,
            Spacer(1, 14),
            HRFlowable(width="100%", thickness=0.5, color=colors.HexColor("#E5E7EB"), spaceAfter=14)
        ]))

    doc.build(story, canvasmaker=NumberedCanvas)
    print(f"[SUCCESS] PDF generated at: {pdf_path}")

if __name__ == "__main__":
    build_pdf()
