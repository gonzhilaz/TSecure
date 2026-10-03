import 'package:flutter/material.dart';
import '../theme/app_colors.dart';
import '../theme/app_typography.dart';

class NotificationCenterSheet extends StatelessWidget {
  const NotificationCenterSheet({super.key});

  static void show(BuildContext context) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (_) => const NotificationCenterSheet(),
    );
  }

  @override
  Widget build(BuildContext context) {
    final notifications = [
      _NotifItem(
        title: 'Database Antivirus Diperbarui',
        desc: 'Tanda tangan virus Kaspersky versi 2026.09.25 sukses terinstal.',
        time: '10 menit yang lalu',
        icon: Icons.system_update_alt_rounded,
        isRead: false,
      ),
      _NotifItem(
        title: 'Wi-Fi Terverifikasi Aman',
        desc: 'Koneksi ke Telkomsel_Orbit_5G dienkripsi protokol WPA3.',
        time: '3 jam yang lalu',
        icon: Icons.wifi_protected_setup_rounded,
        isRead: false,
      ),
      _NotifItem(
        title: 'Lisensi Korporasi Aktif',
        desc: 'Paket Mobile Security Ultimate aktif hingga 14 Okt 2026.',
        time: '1 hari yang lalu',
        icon: Icons.verified_user_rounded,
        isRead: true,
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
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  'Pusat Notifikasi',
                  style: AppTypography.headlineSm.copyWith(
                    fontWeight: FontWeight.w700,
                    color: AppColors.navyDeep,
                  ),
                ),
                TextButton(
                  onPressed: () => Navigator.pop(context),
                  child: const Text('Tutup'),
                ),
              ],
            ),
            const SizedBox(height: 12),
            ...notifications.map((item) {
              return Padding(
                padding: const EdgeInsets.only(bottom: 12),
                child: Material(
                  color: Colors.transparent,
                  child: InkWell(
                    borderRadius: BorderRadius.circular(14),
                    onTap: () {
                      Navigator.pop(context);
                      ScaffoldMessenger.of(context).showSnackBar(
                        SnackBar(
                          content: Text('${item.title}: ${item.desc}'),
                          backgroundColor: AppColors.navyDeep,
                          behavior: SnackBarBehavior.floating,
                          duration: const Duration(seconds: 3),
                        ),
                      );
                    },
                    child: Container(
                      padding: const EdgeInsets.all(14),
                      decoration: BoxDecoration(
                        color: item.isRead
                            ? AppColors.surface
                            : AppColors.primary.withValues(alpha: 0.05),
                        borderRadius: BorderRadius.circular(14),
                        border: Border.all(
                          color: item.isRead
                              ? AppColors.slateBorder
                              : AppColors.primary.withValues(alpha: 0.2),
                        ),
                      ),
                      child: Row(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Container(
                            padding: const EdgeInsets.all(8),
                            decoration: BoxDecoration(
                              color: item.isRead
                                  ? AppColors.slateDivider
                                  : AppColors.primary.withValues(alpha: 0.12),
                              borderRadius: BorderRadius.circular(10),
                            ),
                            child: Icon(
                              item.icon,
                              color: item.isRead
                                  ? AppColors.navyDeep
                                  : AppColors.primary,
                              size: 20,
                            ),
                          ),
                          const SizedBox(width: 12),
                          Expanded(
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text(
                                  item.title,
                                  style: AppTypography.labelMd.copyWith(
                                    fontWeight: FontWeight.w700,
                                    color: AppColors.navyDeep,
                                  ),
                                ),
                                const SizedBox(height: 2),
                                Text(
                                  item.desc,
                                  style: AppTypography.bodySm.copyWith(
                                    color: AppColors.slateMuted,
                                  ),
                                ),
                                const SizedBox(height: 4),
                                Text(
                                  item.time,
                                  style: AppTypography.labelSm.copyWith(
                                    fontSize: 10,
                                    color: AppColors.slateMuted,
                                  ),
                                ),
                              ],
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                ),
              );
            }),
            const SizedBox(height: 8),
          ],
        ),
      ),
    );
  }
}

class _NotifItem {
  final String title;
  final String desc;
  final String time;
  final IconData icon;
  final bool isRead;

  _NotifItem({
    required this.title,
    required this.desc,
    required this.time,
    required this.icon,
    required this.isRead,
  });
}
