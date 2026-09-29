import 'package:flutter/material.dart';
import '../theme/app_colors.dart';
import '../theme/app_typography.dart';
import '../../data/models/activity_log.dart';

class ActivityDetailSheet extends StatelessWidget {
  final ActivityLog log;

  const ActivityDetailSheet({super.key, required this.log});

  static void show(BuildContext context, ActivityLog log) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (_) => ActivityDetailSheet(log: log),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: const BoxDecoration(
        color: AppColors.surfaceCard,
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      padding: const EdgeInsets.symmetric(horizontal: 22, vertical: 20),
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
            const SizedBox(height: 18),
            Row(
              children: [
                Container(
                  padding: const EdgeInsets.all(12),
                  decoration: BoxDecoration(
                    color: log.isSafe
                        ? AppColors.statusSafeEmerald.withValues(alpha: 0.12)
                        : AppColors.statusDangerBg,
                    shape: BoxShape.circle,
                  ),
                  child: Icon(
                    log.icon,
                    color: log.isSafe
                        ? AppColors.statusSafeEmerald
                        : AppColors.primary,
                    size: 26,
                  ),
                ),
                const SizedBox(width: 14),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        log.title,
                        style: AppTypography.headlineSm.copyWith(
                          fontWeight: FontWeight.w700,
                          color: AppColors.navyDeep,
                        ),
                      ),
                      const SizedBox(height: 2),
                      Text(
                        log.time,
                        style: AppTypography.bodySm.copyWith(
                          color: AppColors.slateMuted,
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
            const SizedBox(height: 20),
            Container(
              padding: const EdgeInsets.all(14),
              decoration: BoxDecoration(
                color: AppColors.surface,
                borderRadius: BorderRadius.circular(14),
                border: Border.all(color: AppColors.slateBorder),
              ),
              child: Column(
                children: [
                  _buildRow('Deskripsi', log.description),
                  const Divider(color: AppColors.slateBorder, height: 16),
                  _buildRow('Status Audit', log.isSafe ? 'Bersih & Aman' : 'Dicegat & Dinonaktifkan'),
                  const Divider(color: AppColors.slateBorder, height: 16),
                  _buildRow('Mesin Deteksi', 'Kaspersky Mobile Engine'),
                  const Divider(color: AppColors.slateBorder, height: 16),
                  _buildRow('Aksi Pengamanan', 'Otomatis oleh Realtime Shield'),
                ],
              ),
            ),
            const SizedBox(height: 20),
            SizedBox(
              width: double.infinity,
              height: 44,
              child: ElevatedButton(
                style: ElevatedButton.styleFrom(
                  backgroundColor: AppColors.primary,
                  foregroundColor: Colors.white,
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(14),
                  ),
                ),
                onPressed: () => Navigator.pop(context),
                child: const Text('Tutup Detail', style: TextStyle(fontWeight: FontWeight.w700)),
              ),
            ),
            const SizedBox(height: 10),
          ],
        ),
      ),
    );
  }

  Widget _buildRow(String label, String value) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        Text(label, style: AppTypography.bodySm.copyWith(color: AppColors.slateMuted)),
        const SizedBox(width: 8),
        Expanded(
          child: Text(
            value,
            style: AppTypography.bodySm.copyWith(
              fontWeight: FontWeight.w600,
              color: AppColors.navyDeep,
            ),
            textAlign: TextAlign.right,
          ),
        ),
      ],
    );
  }
}
