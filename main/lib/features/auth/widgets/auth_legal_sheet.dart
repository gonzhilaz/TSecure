import 'package:flutter/material.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_typography.dart';

class AuthLegalSheet extends StatelessWidget {
  final String title;
  final String subtitle;
  final List<Map<String, String>> sections;

  const AuthLegalSheet({
    super.key,
    required this.title,
    required this.subtitle,
    required this.sections,
  });

  static void showTerms(BuildContext context) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (_) => const AuthLegalSheet(
        title: 'Ketentuan Layanan',
        subtitle: 'Syarat & Ketentuan Penggunaan Telkomsel Secure Guard',
        sections: [
          {
            'heading': '1. Ruang Lingkup Layanan',
            'body': 'Telkomsel Secure Guard menyediakan perlindungan keamanan siber seluler bertenaga Kaspersky Mobile Security dan RASP Security untuk pelanggan aktif Telkomsel.',
          },
          {
            'heading': '2. Masa Aktif & Lisensi',
            'body': 'Lisensi keamanan terikat dengan nomor MSISDN dan hardware Mobile ID perangkat Anda. Perpanjangan lisensi mengikuti masa aktif paket data di MyTelkomsel.',
          },
          {
            'heading': '3. Batasan Tanggung Jawab',
            'body': 'Telkomsel dan mitra penyedia mesin antivirus berupaya maksimal mendeteksi dan mencegah ancaman malware, trojan, dan phishing secara real-time.',
          },
        ],
      ),
    );
  }

  static void showPrivacy(BuildContext context) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (_) => const AuthLegalSheet(
        title: 'Kebijakan Privasi',
        subtitle: 'Kepatuhan UU Perlindungan Data Pribadi (UU PDP No. 27/2022)',
        sections: [
          {
            'heading': '1. Pengumpulan Data Telemetri',
            'body': 'Data yang dikumpulkan terbatas pada metadata keamanan, hash integritas aplikasi, model perangkat, serta versi OS untuk mendeteksi ancaman secara akurat.',
          },
          {
            'heading': '2. Perlindungan Kerahasiaan',
            'body': 'Kami tidak membaca pesan pribadi, kontak, foto, atau riwayat penjelajahan privat Anda. Pemindaian berkas dilakukan secara lokal pada perangkat.',
          },
          {
            'heading': '3. Hak Subjek Data',
            'body': 'Sesuai UU PDP No. 27/2022, Anda berhak mencabut persetujuan atau meminta penghapusan riwayat telemetri keamanan kapan pun melalui CS 188.',
          },
        ],
      ),
    );
  }

  static void showSupportDialog(BuildContext context, {required bool isVeronika}) {
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        backgroundColor: Colors.white,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
        title: Row(
          children: [
            Container(
              padding: const EdgeInsets.all(8),
              decoration: BoxDecoration(
                color: AppColors.primary.withValues(alpha: 0.1),
                shape: BoxShape.circle,
              ),
              child: Icon(
                isVeronika ? Icons.support_agent_rounded : Icons.phone_in_talk_rounded,
                color: AppColors.primary,
                size: 24,
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Text(
                isVeronika ? 'Asisten Veronika' : 'GraPARI 188',
                style: AppTypography.headlineSm.copyWith(
                  fontWeight: FontWeight.w700,
                  color: AppColors.navyDeep,
                  fontSize: 18,
                ),
              ),
            ),
          ],
        ),
        content: Text(
          isVeronika
              ? 'Layanan asisten virtual Veronika Telkomsel siap membantu aktivasi paket dan troubleshooting keamanan Anda 24/7.'
              : 'Pusat kontak resmi Telkomsel 188 tersedia 24 jam untuk bantuan teknis dan informasi langganan paket Telkomsel Secure.',
          style: AppTypography.bodySm.copyWith(color: AppColors.textSecondary),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('Tutup'),
          ),
          ElevatedButton(
            onPressed: () {
              Navigator.pop(ctx);
              ScaffoldMessenger.of(context).showSnackBar(
                SnackBar(
                  content: Text(
                    isVeronika
                        ? 'Menghubungkan ke asisten virtual Veronika...'
                        : 'Memulai panggilan ke GraPARI 188...',
                  ),
                  backgroundColor: AppColors.navyDeep,
                  behavior: SnackBarBehavior.floating,
                ),
              );
            },
            style: ElevatedButton.styleFrom(
              backgroundColor: AppColors.primary,
              foregroundColor: Colors.white,
            ),
            child: Text(isVeronika ? 'Mulai Chat' : 'Hubungi 188'),
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      constraints: BoxConstraints(
        maxHeight: MediaQuery.of(context).size.height * 0.85,
      ),
      decoration: const BoxDecoration(
        color: AppColors.surfaceCard,
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 20),
      child: SafeArea(
        top: false,
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Center(
              child: Container(
                width: 44,
                height: 4,
                decoration: BoxDecoration(
                  color: AppColors.slateBorder,
                  borderRadius: BorderRadius.circular(2),
                ),
              ),
            ),
            const SizedBox(height: 18),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        title,
                        style: AppTypography.headlineSm.copyWith(
                          fontWeight: FontWeight.w700,
                          color: AppColors.navyDeep,
                        ),
                      ),
                      const SizedBox(height: 2),
                      Text(
                        subtitle,
                        style: AppTypography.bodySm.copyWith(
                          color: AppColors.slateMuted,
                          fontSize: 11,
                        ),
                      ),
                    ],
                  ),
                ),
                IconButton(
                  icon: const Icon(Icons.close),
                  onPressed: () => Navigator.pop(context),
                ),
              ],
            ),
            const Divider(color: AppColors.slateBorder, height: 20),
            Expanded(
              child: SingleChildScrollView(
                physics: const BouncingScrollPhysics(),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: sections.map((sec) {
                    return Padding(
                      padding: const EdgeInsets.only(bottom: 16),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            sec['heading'] ?? '',
                            style: AppTypography.labelMd.copyWith(
                              fontWeight: FontWeight.w700,
                              color: AppColors.navyDeep,
                            ),
                          ),
                          const SizedBox(height: 6),
                          Text(
                            sec['body'] ?? '',
                            style: AppTypography.bodySm.copyWith(
                              color: AppColors.textSecondary,
                              height: 1.45,
                            ),
                          ),
                        ],
                      ),
                    );
                  }).toList(),
                ),
              ),
            ),
            const SizedBox(height: 16),
            SizedBox(
              height: 46,
              child: ElevatedButton(
                style: ElevatedButton.styleFrom(
                  backgroundColor: AppColors.primary,
                  foregroundColor: Colors.white,
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                ),
                onPressed: () => Navigator.pop(context),
                child: const Text('Saya Mengerti', style: TextStyle(fontWeight: FontWeight.w700)),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
