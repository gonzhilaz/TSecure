import 'package:flutter/material.dart';
import '../theme/app_colors.dart';
import '../theme/app_typography.dart';

class ScanOptionsSheet extends StatelessWidget {
  final ValueChanged<String> onSelectOption;

  const ScanOptionsSheet({super.key, required this.onSelectOption});

  static void show(BuildContext context, {required ValueChanged<String> onSelect}) {
    showModalBottomSheet(
      context: context,
      backgroundColor: Colors.transparent,
      builder: (_) => ScanOptionsSheet(onSelectOption: onSelect),
    );
  }

  @override
  Widget build(BuildContext context) {
    final options = [
      _ScanMode('Pindai Cepat', 'Inspeksi memori aktif dan aplikasi terinstal', Icons.flash_on_rounded),
      _ScanMode('Pindai Penuh (Rekomendasi)', 'Inspeksi menyeluruh OS, berkas sistem, & partisi internal', Icons.radar_rounded),
      _ScanMode('Pindai Folder Unduhan', 'Inspeksi berkas APK eksternal dan folder Download', Icons.folder_open_rounded),
      _ScanMode('Uji Validasi EICAR (Test Lab)', 'Verifikasi deteksi mesin dengan sampel standar EICAR', Icons.science_rounded),
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
              'Pilihan Mode Pemindaian',
              style: AppTypography.headlineSm.copyWith(
                fontWeight: FontWeight.w700,
                color: AppColors.navyDeep,
              ),
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
                  onSelectOption(opt.title);
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
  _ScanMode(this.title, this.desc, this.icon);
}
