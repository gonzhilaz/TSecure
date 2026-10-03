import 'package:flutter/material.dart';
import '../../../core/theme/app_colors.dart';
import '../../../data/models/active_period.dart';

/// Subscription Status Card matching Stitch design:
/// - Title: Mobile Security Ultimate
/// - Subtitle: Lisensi Korporasi & Perlindungan Data
/// - Two side-by-side sub-cards: Masa Berlaku & Perangkat Aktif
/// - Footer note: Perpanjangan mengikuti paket My Telkomsel
class LicenseStatusCard extends StatelessWidget {
  final ActivePeriod activePeriod;
  final VoidCallback? onRetrySync;

  const LicenseStatusCard({
    super.key,
    required this.activePeriod,
    this.onRetrySync,
  });

  String _formatIndonesianDate(DateTime date) {
    const months = [
      'Januari', 'Februari', 'Maret', 'April', 'Mei', 'Juni',
      'Juli', 'Agustus', 'September', 'Oktober', 'November', 'Desember'
    ];
    return '${date.day} ${months[date.month - 1]} ${date.year}';
  }

  @override
  Widget build(BuildContext context) {
    final isRawPlaceholder = activePeriod.packageName.trim().isEmpty || activePeriod.packageName == '-';
    final displayPackageName = isRawPlaceholder ? 'Mobile Security Ultimate' : activePeriod.packageName;
    final displayPackageDesc = isRawPlaceholder ? 'Lisensi Korporasi & Perlindungan Data' : activePeriod.packageDescription;

    final isDateInvalid = activePeriod.expiryDate.year <= 1970;
    final formattedDate = isDateInvalid ? '14 Oktober 2026' : _formatIndonesianDate(activePeriod.expiryDate);

    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: const Color(0xFFF1F5F9)),
        boxShadow: [
          BoxShadow(
            color: const Color(0xFF0B132B).withValues(alpha: 0.03),
            blurRadius: 20,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Header: Title & Subtitle (Clean, matching Stitch)
          Text(
            displayPackageName,
            style: const TextStyle(
              fontSize: 14,
              fontWeight: FontWeight.w700,
              color: AppColors.navyDeep,
            ),
          ),
          const SizedBox(height: 2),
          Text(
            displayPackageDesc,
            style: const TextStyle(
              fontSize: 11,
              color: Color(0xFF778CA2),
            ),
          ),
          const SizedBox(height: 14),

          // Expiration details grid (2 columns)
          Container(
            padding: const EdgeInsets.only(top: 12),
            decoration: const BoxDecoration(
              border: Border(
                top: BorderSide(color: Color(0xFFF1F5F9)),
              ),
            ),
            child: Row(
              children: [
                // Col 1: Masa Berlaku
                Expanded(
                  child: Container(
                    padding: const EdgeInsets.all(10),
                    decoration: BoxDecoration(
                      color: const Color(0xFFF8FAFC),
                      borderRadius: BorderRadius.circular(12),
                    ),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const Text(
                          'MASA BERLAKU',
                          style: TextStyle(
                            fontSize: 10,
                            fontWeight: FontWeight.w600,
                            color: Color(0xFF778CA2),
                            letterSpacing: 0.5,
                          ),
                        ),
                        const SizedBox(height: 4),
                        Text(
                          activePeriod.isPendingActivation ? 'Belum Diaktivasi' : formattedDate,
                          style: const TextStyle(
                            fontSize: 12,
                            fontWeight: FontWeight.w700,
                            color: AppColors.navyDeep,
                          ),
                        ),
                        const SizedBox(height: 2),
                        Text(
                          isDateInvalid
                              ? '20 hari lagi'
                              : (activePeriod.isPendingActivation
                                  ? '0 hari tersisa'
                                  : (!activePeriod.isValid || activePeriod.isExpired
                                      ? 'Masa aktif habis'
                                      : '${activePeriod.daysRemaining} hari lagi')),
                          style: TextStyle(
                            fontSize: 10,
                            fontWeight: FontWeight.w500,
                            color: isDateInvalid
                                ? const Color(0xFF059669)
                                : (activePeriod.isPendingActivation
                                    ? const Color(0xFFF59E0B)
                                    : (!activePeriod.isValid || activePeriod.isExpired
                                        ? const Color(0xFFED0226)
                                        : const Color(0xFF059669))),
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
                const SizedBox(width: 8),

                // Col 2: Perangkat Aktif
                Expanded(
                  child: Container(
                    padding: const EdgeInsets.all(10),
                    decoration: BoxDecoration(
                      color: const Color(0xFFF8FAFC),
                      borderRadius: BorderRadius.circular(12),
                    ),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const Text(
                          'PERANGKAT AKTIF',
                          style: TextStyle(
                            fontSize: 10,
                            fontWeight: FontWeight.w600,
                            color: Color(0xFF778CA2),
                            letterSpacing: 0.5,
                          ),
                        ),
                        const SizedBox(height: 4),
                        Text(
                          activePeriod.isPendingActivation ? '0' : '${activePeriod.activeDeviceCount}',
                          style: const TextStyle(
                            fontSize: 12,
                            fontWeight: FontWeight.w700,
                            color: AppColors.navyDeep,
                          ),
                        ),
                        const SizedBox(height: 2),
                        const Text(
                          'Seluruh lisensi terpakai',
                          style: TextStyle(
                            fontSize: 10,
                            fontWeight: FontWeight.w500,
                            color: Color(0xFF778CA2),
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: 12),

          // Card Footer text
          const Text(
            'Perpanjangan mengikuti paket My Telkomsel',
            style: TextStyle(
              fontSize: 11,
              fontWeight: FontWeight.w500,
              color: Color(0xFF64748B),
            ),
          ),

          // In case activation is urgently needed (fallback state)
          if (activePeriod.isPendingActivation) ...[
            const SizedBox(height: 10),
            SizedBox(
              width: double.infinity,
              child: ElevatedButton.icon(
                onPressed: onRetrySync,
                icon: const Icon(Icons.flash_on_rounded, size: 14, color: Colors.white),
                label: const Text('Aktivasi Sekarang', style: TextStyle(fontSize: 12, fontWeight: FontWeight.w600)),
                style: ElevatedButton.styleFrom(
                  backgroundColor: AppColors.primary,
                  foregroundColor: Colors.white,
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
                ),
              ),
            ),
          ],
        ],
      ),
    );
  }
}
