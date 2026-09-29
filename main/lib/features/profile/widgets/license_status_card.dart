import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_typography.dart';
import '../../../data/models/active_period.dart';

class LicenseStatusCard extends StatelessWidget {
  final ActivePeriod activePeriod;
  final VoidCallback? onRetrySync;

  const LicenseStatusCard({
    super.key,
    required this.activePeriod,
    this.onRetrySync,
  });

  @override
  Widget build(BuildContext context) {
    final formattedDate =
        DateFormat('dd MMMM yyyy').format(activePeriod.expiryDate);

    return Container(
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(
        color: AppColors.surfaceCard,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: AppColors.slateBorder),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Expanded(
                child: Text(
                  activePeriod.packageName,
                  style: AppTypography.headlineSm.copyWith(
                    color: AppColors.navyDeep,
                    fontWeight: FontWeight.w700,
                  ),
                ),
              ),
              if (activePeriod.isPendingActivation)
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                  decoration: BoxDecoration(
                    color: AppColors.statusWarning.withValues(alpha: 0.15),
                    borderRadius: BorderRadius.circular(6),
                  ),
                  child: Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      const Icon(Icons.schedule_rounded, size: 12, color: AppColors.statusWarning),
                      const SizedBox(width: 4),
                      Text(
                        'Wajib Aktivasi',
                        style: AppTypography.labelSm.copyWith(
                          color: AppColors.statusWarning,
                          fontWeight: FontWeight.w700,
                        ),
                      ),
                    ],
                  ),
                )
              else if (activePeriod.isPendingKsp)
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                  decoration: BoxDecoration(
                    color: AppColors.statusWarning.withValues(alpha: 0.15),
                    borderRadius: BorderRadius.circular(6),
                  ),
                  child: Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      const Icon(Icons.sync, size: 12, color: AppColors.statusWarning),
                      const SizedBox(width: 4),
                      Text(
                        'Sinkronisasi',
                        style: AppTypography.labelSm.copyWith(
                          color: AppColors.statusWarning,
                          fontWeight: FontWeight.w700,
                        ),
                      ),
                    ],
                  ),
                )
              else if (!activePeriod.isValid || activePeriod.isExpired)
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                  decoration: BoxDecoration(
                    color: AppColors.statusDanger.withValues(alpha: 0.15),
                    borderRadius: BorderRadius.circular(6),
                  ),
                  child: Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      const Icon(Icons.cancel_rounded, size: 12, color: AppColors.statusDanger),
                      const SizedBox(width: 4),
                      Text(
                        'Masa Aktif Habis',
                        style: AppTypography.labelSm.copyWith(
                          color: AppColors.statusDanger,
                          fontWeight: FontWeight.w700,
                        ),
                      ),
                    ],
                  ),
                )
              else
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                  decoration: BoxDecoration(
                    color: AppColors.statusSafeEmerald.withValues(alpha: 0.15),
                    borderRadius: BorderRadius.circular(6),
                  ),
                  child: Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      const Icon(Icons.verified_rounded, size: 12, color: AppColors.statusSafeEmerald),
                      const SizedBox(width: 4),
                      Text(
                        'Aktif & Terlindungi',
                        style: AppTypography.labelSm.copyWith(
                          color: AppColors.statusSafeEmerald,
                          fontWeight: FontWeight.w700,
                        ),
                      ),
                    ],
                  ),
                ),
            ],
          ),
          const SizedBox(height: 2),
          Text(
            activePeriod.packageDescription,
            style: AppTypography.bodySm.copyWith(
              color: AppColors.slateMuted,
            ),
          ),
          const SizedBox(height: 16),
          Row(
            children: [
              Expanded(
                child: Container(
                  padding: const EdgeInsets.all(12),
                  decoration: BoxDecoration(
                    color: AppColors.background,
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        'MASA BERLAKU',
                        style: AppTypography.labelSm.copyWith(
                          color: AppColors.slateMuted,
                          letterSpacing: 0.8,
                        ),
                      ),
                      const SizedBox(height: 4),
                      Text(
                        activePeriod.isPendingActivation ? 'Belum Diaktivasi' : formattedDate,
                        style: AppTypography.labelLg.copyWith(
                          fontWeight: FontWeight.w700,
                          color: AppColors.navyDeep,
                        ),
                      ),
                      const SizedBox(height: 2),
                      Text(
                        activePeriod.isPendingActivation
                            ? '0 hari tersisa'
                            : (!activePeriod.isValid || activePeriod.isExpired
                                ? 'Masa aktif habis'
                                : '${activePeriod.daysRemaining} hari lagi'),
                        style: AppTypography.bodySm.copyWith(
                          color: activePeriod.isPendingActivation
                              ? AppColors.statusWarning
                              : (!activePeriod.isValid || activePeriod.isExpired
                                  ? AppColors.statusDanger
                                  : AppColors.statusSafeEmerald),
                          fontWeight: FontWeight.w600,
                        ),
                      ),
                    ],
                  ),
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Container(
                  padding: const EdgeInsets.all(12),
                  decoration: BoxDecoration(
                    color: AppColors.background,
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        'PERANGKAT AKTIF',
                        style: AppTypography.labelSm.copyWith(
                          color: AppColors.slateMuted,
                          letterSpacing: 0.8,
                        ),
                      ),
                      const SizedBox(height: 4),
                      Text(
                        activePeriod.isPendingActivation ? '0' : '${activePeriod.activeDeviceCount}',
                        style: AppTypography.labelLg.copyWith(
                          fontWeight: FontWeight.w700,
                          color: AppColors.navyDeep,
                        ),
                      ),
                      const SizedBox(height: 2),
                      Text(
                        activePeriod.isPendingActivation
                            ? 'Menunggu aktivasi'
                            : 'Lisensi terikat',
                        style: AppTypography.bodySm.copyWith(
                          color: AppColors.slateMuted,
                        ),
                      ),
                    ],
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 14),
          Text(
            activePeriod.statusMessage,
            style: AppTypography.bodySm.copyWith(
              color: AppColors.slateMuted,
            ),
          ),
          if (activePeriod.isPendingActivation) ...[
            const SizedBox(height: 12),
            Container(
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: AppColors.statusWarning.withValues(alpha: 0.08),
                borderRadius: BorderRadius.circular(12),
                border: Border.all(color: AppColors.statusWarning.withValues(alpha: 0.3)),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      const Icon(Icons.info_outline_rounded, size: 18, color: AppColors.statusWarning),
                      const SizedBox(width: 8),
                      Text(
                        'Prosedur Aktivasi Diperlukan',
                        style: AppTypography.labelMd.copyWith(
                          color: AppColors.statusWarning,
                          fontWeight: FontWeight.w700,
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 4),
                  Text(
                    'Paket terdaftar di NDP Telkomsel. Silakan selesaikan prosedur aktivasi untuk menerbitkan lisensi perangkat.',
                    style: AppTypography.bodySm.copyWith(color: AppColors.navyDeep),
                  ),
                  const SizedBox(height: 10),
                  SizedBox(
                    width: double.infinity,
                    child: ElevatedButton.icon(
                      onPressed: onRetrySync,
                      icon: const Icon(Icons.flash_on_rounded, size: 16, color: Colors.white),
                      label: const Text(
                        'Aktivasi Sekarang',
                        style: TextStyle(fontWeight: FontWeight.w700, color: Colors.white),
                      ),
                      style: ElevatedButton.styleFrom(
                        backgroundColor: AppColors.primary,
                        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ] else if (activePeriod.isPendingKsp) ...[
            const SizedBox(height: 12),
            Container(
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: AppColors.statusWarning.withValues(alpha: 0.08),
                borderRadius: BorderRadius.circular(12),
                border: Border.all(color: AppColors.statusWarning.withValues(alpha: 0.3)),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      const Icon(Icons.sync_problem_rounded, size: 18, color: AppColors.statusWarning),
                      const SizedBox(width: 8),
                      Text(
                        'Sinkronisasi sedang berjalan',
                        style: AppTypography.labelMd.copyWith(
                          color: AppColors.statusWarning,
                          fontWeight: FontWeight.w700,
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 4),
                  Text(
                    'Paket MyTelkomsel valid. Menunggu pengikatan lisensi proteksi Kaspersky B2B.',
                    style: AppTypography.bodySm.copyWith(color: AppColors.navyDeep),
                  ),
                  const SizedBox(height: 10),
                  SizedBox(
                    width: double.infinity,
                    child: OutlinedButton.icon(
                      onPressed: onRetrySync,
                      icon: const Icon(Icons.sync, size: 16, color: AppColors.primary),
                      label: const Text(
                        'Sinkronkan Ulang',
                        style: TextStyle(fontWeight: FontWeight.w700, color: AppColors.primary),
                      ),
                      style: OutlinedButton.styleFrom(
                        side: const BorderSide(color: AppColors.primary),
                        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ] else if (!activePeriod.isValid || activePeriod.isExpired) ...[
            const SizedBox(height: 12),
            Container(
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: AppColors.statusDanger.withValues(alpha: 0.08),
                borderRadius: BorderRadius.circular(12),
                border: Border.all(color: AppColors.statusDanger.withValues(alpha: 0.3)),
              ),
              child: Row(
                children: [
                  const Icon(Icons.gpp_bad_rounded, size: 20, color: AppColors.statusDanger),
                  const SizedBox(width: 10),
                  Expanded(
                    child: Text(
                      'Policy Guard: Masa aktif habis di NDP. Proteksi Kaspersky dinonaktifkan.',
                      style: AppTypography.bodySm.copyWith(
                        color: AppColors.statusDanger,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ] else if (activePeriod.isActivated) ...[
            const SizedBox(height: 12),
            Row(
              children: [
                const Icon(Icons.verified_rounded, size: 16, color: AppColors.statusSafeEmerald),
                const SizedBox(width: 6),
                Text(
                  'Perangkat Berhasil Dilindungi (Kaspersky B2B Aktif)',
                  style: AppTypography.labelSm.copyWith(
                    color: AppColors.statusSafeEmerald,
                    fontWeight: FontWeight.w700,
                  ),
                ),
              ],
            ),
          ],
        ],
      ),
    );
  }
}
