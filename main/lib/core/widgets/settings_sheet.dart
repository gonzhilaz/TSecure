import 'package:flutter/material.dart';
import '../theme/app_colors.dart';
import '../theme/app_typography.dart';

class SettingsSheet extends StatefulWidget {
  const SettingsSheet({super.key});

  static void show(BuildContext context) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (_) => const SettingsSheet(),
    );
  }

  @override
  State<SettingsSheet> createState() => _SettingsSheetState();
}

class _SettingsSheetState extends State<SettingsSheet> {
  bool _autoUpdateDb = true;
  bool _biometricLock = true;
  bool _pushAlerts = true;

  @override
  Widget build(BuildContext context) {
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
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  'Pengaturan Keamanan',
                  style: AppTypography.headlineSm.copyWith(
                    fontWeight: FontWeight.w700,
                    color: AppColors.navyDeep,
                  ),
                ),
                IconButton(
                  icon: const Icon(Icons.close),
                  onPressed: () => Navigator.pop(context),
                ),
              ],
            ),
            const SizedBox(height: 12),
            SwitchListTile(
              contentPadding: EdgeInsets.zero,
              activeThumbColor: AppColors.primary,
              title: Text('Pembaruan Otomatis Virus DB', style: AppTypography.labelMd.copyWith(fontWeight: FontWeight.w600)),
              subtitle: Text('Unduh definisi ancaman terbaru di latar belakang', style: AppTypography.bodySm),
              value: _autoUpdateDb,
              onChanged: (v) => setState(() => _autoUpdateDb = v),
            ),
            const Divider(color: AppColors.slateBorder),
            SwitchListTile(
              contentPadding: EdgeInsets.zero,
              activeThumbColor: AppColors.primary,
              title: Text('Kunci Biometrik Aplikasi', style: AppTypography.labelMd.copyWith(fontWeight: FontWeight.w600)),
              subtitle: Text('Gunakan sidik jari atau Face Unlock untuk membuka aplikasi', style: AppTypography.bodySm),
              value: _biometricLock,
              onChanged: (v) => setState(() => _biometricLock = v),
            ),
            const Divider(color: AppColors.slateBorder),
            SwitchListTile(
              contentPadding: EdgeInsets.zero,
              activeThumbColor: AppColors.primary,
              title: Text('Notifikasi Peringatan Instan', style: AppTypography.labelMd.copyWith(fontWeight: FontWeight.w600)),
              subtitle: Text('Dapatkan peringatan seketika saat situs phishing diblokir', style: AppTypography.bodySm),
              value: _pushAlerts,
              onChanged: (v) => setState(() => _pushAlerts = v),
            ),
            const SizedBox(height: 16),
          ],
        ),
      ),
    );
  }
}
