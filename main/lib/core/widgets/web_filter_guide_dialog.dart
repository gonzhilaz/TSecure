import 'package:flutter/material.dart';
import '../theme/app_colors.dart';
import '../theme/app_typography.dart';

/// Modal dialog panduan step-by-step aktivasi izin Web Filter (Aksesibilitas)
/// Memandu pengguna secara visual agar tidak bingung mencari menu di Android / HyperOS / MIUI.
class WebFilterGuideDialog extends StatelessWidget {
  final VoidCallback onOpenAccessibility;
  final VoidCallback onOpenAppSettings;

  const WebFilterGuideDialog({
    super.key,
    required this.onOpenAccessibility,
    required this.onOpenAppSettings,
  });

  static Future<void> show(
    BuildContext context, {
    required VoidCallback onOpenAccessibility,
    required VoidCallback onOpenAppSettings,
  }) {
    return showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (_) => WebFilterGuideDialog(
        onOpenAccessibility: onOpenAccessibility,
        onOpenAppSettings: onOpenAppSettings,
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: const BoxDecoration(
        color: AppColors.surfaceCard,
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      padding: const EdgeInsets.fromLTRB(20, 12, 20, 24),
      child: SafeArea(
        top: false,
        child: SingleChildScrollView(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.stretch,
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

              // Header
              Row(
                children: [
                  Container(
                    padding: const EdgeInsets.all(10),
                    decoration: BoxDecoration(
                      color: AppColors.primary.withValues(alpha: 0.1),
                      borderRadius: BorderRadius.circular(12),
                    ),
                    child: const Icon(Icons.language_rounded, color: AppColors.primary, size: 24),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          'Aktivasi Web Filter Browser',
                          style: AppTypography.headlineSm.copyWith(
                            fontSize: 17,
                            fontWeight: FontWeight.w700,
                          ),
                        ),
                        Text(
                          'Layanan: TelkomSecure - Proteksi Web Filter',
                          style: AppTypography.bodySm.copyWith(
                            color: AppColors.primary,
                            fontWeight: FontWeight.w600,
                          ),
                        ),
                      ],
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 16),
              const Divider(color: AppColors.slateBorder, height: 1),
              const SizedBox(height: 16),

              Text(
                'LANGKAH-LANGKAH PADA MENU AKSESIBILITAS:',
                style: AppTypography.labelSm.copyWith(
                  letterSpacing: 1.0,
                  color: AppColors.slateMid,
                  fontWeight: FontWeight.w700,
                ),
              ),
              const SizedBox(height: 12),

              _buildStepTile(
                step: '1',
                icon: Icons.download_rounded,
                title: 'Pilih "Aplikasi terinstal" / "Downloaded apps"',
                subtitle: 'Di menu Aksesibilitas (tab Umum), gulir ke bawah dan ketuk "Downloaded apps" (Aplikasi yang didownload).',
                highlight: true,
              ),
              const SizedBox(height: 10),
              _buildStepTile(
                step: '2',
                icon: Icons.shield_outlined,
                title: 'Pilih "TelkomSecure - Proteksi Web Filter"',
                subtitle: 'Temukan layanan TelkomSecure di dalam daftar aplikasi terinstal tersebut.',
                highlight: false,
              ),
              const SizedBox(height: 10),
              _buildStepTile(
                step: '3',
                icon: Icons.toggle_on_rounded,
                title: 'Aktifkan Sakelar (Gunakan Layanan)',
                subtitle: 'Nyalakan sakelar agar peramban (Chrome, Edge, dll) terlindungi dari phishing.',
                highlight: false,
              ),

              const SizedBox(height: 16),

              // Banner HyperOS / Xiaomi / Android 13+ Restricted Settings
              Container(
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: const Color(0xFFFFF8E1),
                  borderRadius: BorderRadius.circular(12),
                  border: Border.all(color: const Color(0xFFFFD54F)),
                ),
                child: Row(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Icon(Icons.info_outline_rounded, color: Color(0xFFF57C00), size: 20),
                    const SizedBox(width: 10),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            'Khusus Xiaomi (HyperOS/MIUI) & Android 13+:',
                            style: AppTypography.labelMd.copyWith(
                              fontWeight: FontWeight.w700,
                              color: const Color(0xFFB78103),
                            ),
                          ),
                          const SizedBox(height: 4),
                          Text(
                            'Jika sakelar tidak bisa diklik ("Setelan dibatasi" / "Restricted setting"): Tekan "Buka Info Aplikasi" di bawah, lalu ketuk menu titik tiga (⋮) di pojok kanan atas dan pilih "Izinkan setelan terbatas".',
                            style: AppTypography.bodySm.copyWith(
                              color: const Color(0xFF5D4037),
                              fontSize: 12,
                              height: 1.35,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              ),

              const SizedBox(height: 20),

              // Action Buttons
              OutlinedButton.icon(
                onPressed: () {
                  Navigator.pop(context);
                  onOpenAppSettings();
                },
                icon: const Icon(Icons.settings_outlined, size: 18),
                label: const Text('Buka Info Aplikasi (Buka Pembatasan)'),
                style: OutlinedButton.styleFrom(
                  foregroundColor: AppColors.textPrimary,
                  side: const BorderSide(color: AppColors.slateBorder),
                  padding: const EdgeInsets.symmetric(vertical: 12),
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                ),
              ),
              const SizedBox(height: 8),
              ElevatedButton.icon(
                onPressed: () {
                  Navigator.pop(context);
                  onOpenAccessibility();
                },
                icon: const Icon(Icons.arrow_forward_rounded, size: 18),
                label: const Text('Buka Pengaturan Aksesibilitas'),
                style: ElevatedButton.styleFrom(
                  backgroundColor: AppColors.primary,
                  foregroundColor: Colors.white,
                  padding: const EdgeInsets.symmetric(vertical: 14),
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                  elevation: 0,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildStepTile({
    required String step,
    required IconData icon,
    required String title,
    required String subtitle,
    required bool highlight,
  }) {
    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: highlight ? AppColors.primary.withValues(alpha: 0.05) : AppColors.slateSurface,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(
          color: highlight ? AppColors.primary.withValues(alpha: 0.3) : AppColors.slateBorder,
        ),
      ),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          CircleAvatar(
            radius: 12,
            backgroundColor: highlight ? AppColors.primary : AppColors.slateMid,
            child: Text(
              step,
              style: const TextStyle(
                color: Colors.white,
                fontSize: 12,
                fontWeight: FontWeight.w700,
              ),
            ),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Icon(icon, size: 16, color: highlight ? AppColors.primary : AppColors.textPrimary),
                    const SizedBox(width: 6),
                    Expanded(
                      child: Text(
                        title,
                        style: AppTypography.labelMd.copyWith(
                          fontWeight: FontWeight.w700,
                          color: highlight ? AppColors.primary : AppColors.textPrimary,
                        ),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 4),
                Text(
                  subtitle,
                  style: AppTypography.bodySm.copyWith(
                    color: AppColors.slateMid,
                    fontSize: 12,
                    height: 1.3,
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
