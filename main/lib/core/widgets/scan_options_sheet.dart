import 'package:flutter/material.dart';
import '../theme/app_colors.dart';
import '../theme/app_typography.dart';

class ScanOptionsSheet extends StatelessWidget {
  final void Function(String title, String code) onSelectOption;

  const ScanOptionsSheet({super.key, required this.onSelectOption});

  static void show(
    BuildContext context, {
    required void Function(String title, String code) onSelect,
  }) {
    showModalBottomSheet(
      context: context,
      backgroundColor: Colors.transparent,
      builder: (_) => ScanOptionsSheet(onSelectOption: onSelect),
    );
  }

  @override
  Widget build(BuildContext context) {
    final options = [
      _ScanMode(
        'Pindai Cepat (Quick Scan)',
        'Inspeksi cepat aplikasi terpasang & memori kerja sistem',
        Icons.flash_on_rounded,
        'QUICK',
      ),
      _ScanMode(
        'Pindai Penuh (Full Scan)',
        'Inspeksi menyeluruh semua berkas penyimpanan internal, kartu SD, & aplikasi',
        Icons.phone_android_rounded,
        'FULL',
      ),
      _ScanMode(
        'Pindai Rekomendasi (Recommended)',
        'Inspeksi berkas sistem & partisi rentan rekomendasi Kaspersky',
        Icons.radar_rounded,
        'RECOMMENDED',
      ),
      _ScanMode(
        'Pindai Folder Unduhan (Folder Scan)',
        'Inspeksi berkas APK eksternal & dokumen di folder Download',
        Icons.folder_open_rounded,
        'FOLDER',
      ),
    ];

    return Container(
      decoration: const BoxDecoration(
        color: AppColors.surfaceCard,
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 20),
      child: SafeArea(
        top: false,
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Center(
              child: Container(
                width: 40,
                height: 4,
                decoration: BoxDecoration(
                  color: AppColors.slateBorder,
                  borderRadius: BorderRadius.circular(2),
                ),
              ),
            ),
            const SizedBox(height: 16),
            Text(
              '4 Mode Pemindaian Kaspersky SDK',
              style: AppTypography.headlineSm.copyWith(
                fontWeight: FontWeight.w700,
                color: AppColors.navyDeep,
              ),
            ),
            const SizedBox(height: 6),
            Text(
              'Pilih cakupan pemindaian sesuai kebutuhan keamanan perangkat Anda',
              style: AppTypography.bodySm.copyWith(color: AppColors.slateMuted),
            ),
            const SizedBox(height: 14),
            ...options.map((opt) {
              return ListTile(
                contentPadding: const EdgeInsets.symmetric(vertical: 4),
                leading: Container(
                  padding: const EdgeInsets.all(10),
                  decoration: BoxDecoration(
                    color: AppColors.primary.withValues(alpha: 0.1),
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: Icon(opt.icon, color: AppColors.primary, size: 22),
                ),
                title: Text(
                  opt.title,
                  style: AppTypography.labelMd.copyWith(
                    fontWeight: FontWeight.w700,
                    color: AppColors.navyDeep,
                  ),
                ),
                subtitle: Text(
                  opt.desc,
                  style: AppTypography.bodySm.copyWith(color: AppColors.slateMuted),
                ),
                onTap: () {
                  Navigator.pop(context);
                  onSelectOption(opt.title, opt.code);
                },
              );
            }),
            const SizedBox(height: 10),
          ],
        ),
      ),
    );
  }
}

class _ScanMode {
  final String title;
  final String desc;
  final IconData icon;
  final String code;
  _ScanMode(this.title, this.desc, this.icon, this.code);
}
