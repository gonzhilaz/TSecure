import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_typography.dart';
import '../../../core/widgets/activity_detail_sheet.dart';
import '../../../core/widgets/scan_threat_detail_sheet.dart';
import '../../../data/models/activity_log.dart';
import '../../../data/services/kaspersky_sdk_bridge.dart';
import '../../../data/services/threat_manager_service.dart';

class RecentActivityPreview extends StatelessWidget {
  final ActivityLog? latestLog;
  final VoidCallback onViewAll;

  const RecentActivityPreview({
    super.key,
    required this.latestLog,
    required this.onViewAll,
  });

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text(
              'Aktivitas Terakhir',
              style: AppTypography.headlineSm.copyWith(
                color: AppColors.navyDeep,
                fontWeight: FontWeight.w700,
              ),
            ),
            InkWell(
              onTap: onViewAll,
              child: Row(
                children: [
                  Text(
                    'Lihat Semua',
                    style: AppTypography.labelMd.copyWith(
                      color: AppColors.primary,
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                  const SizedBox(width: 4),
                  const Icon(
                    Icons.arrow_forward_ios,
                    size: 12,
                    color: AppColors.primary,
                  ),
                ],
              ),
            ),
          ],
        ),
        const SizedBox(height: 12),
        Material(
          color: Colors.transparent,
          borderRadius: BorderRadius.circular(16),
          child: InkWell(
            borderRadius: BorderRadius.circular(16),
            onTap: () {
              if (latestLog != null) {
                if (!latestLog!.isSafe || latestLog!.threats.isNotEmpty) {
                  ScanThreatDetailSheet.show(
                    context,
                    log: latestLog!,
                    threatManager: context.read<ThreatManagerService>(),
                    kasperskySdk: context.read<KasperskySdkBridge>(),
                  );
                } else {
                  ActivityDetailSheet.show(context, latestLog!);
                }
              }
            },
            child: Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: AppColors.surfaceCard,
                borderRadius: BorderRadius.circular(16),
                border: Border.all(color: AppColors.slateBorder),
              ),
              child: Row(
                children: [
                  Container(
                    width: 42,
                    height: 42,
                    decoration: BoxDecoration(
                      color: (latestLog != null && !latestLog!.isSafe)
                          ? AppColors.statusDanger.withValues(alpha: 0.12)
                          : AppColors.slateDivider,
                      borderRadius: BorderRadius.circular(10),
                    ),
                    child: Center(
                      child: Icon(
                        (latestLog != null && !latestLog!.isSafe)
                            ? Icons.bug_report_rounded
                            : Icons.verified_outlined,
                        color: (latestLog != null && !latestLog!.isSafe)
                            ? AppColors.statusDanger
                            : AppColors.navyDeep,
                        size: 24,
                      ),
                    ),
                  ),
                  const SizedBox(width: 14),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          _formatTitle(latestLog),
                          style: AppTypography.labelLg.copyWith(
                            fontWeight: FontWeight.w700,
                            color: AppColors.navyDeep,
                          ),
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                        ),
                        const SizedBox(height: 2),
                        Text(
                          latestLog?.description ??
                              '0 ancaman ditemukan • 1.420 file diperiksa',
                          style: AppTypography.bodySm.copyWith(
                            color: AppColors.slateMuted,
                          ),
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                        ),
                      ],
                    ),
                  ),
                  const Icon(
                    Icons.arrow_forward_ios_rounded,
                    size: 14,
                    color: AppColors.slateMuted,
                  ),
                ],
              ),
            ),
          ),
        ),
      ],
    );
  }

  String _formatTitle(ActivityLog? log) {
    if (log == null) return 'Pemindaian Sistem Siap';
    final desc = log.description;
    final threatMatch = RegExp(r'(\d+)\s+Ancaman\s+Ditemukan', caseSensitive: false).firstMatch(desc);
    if (threatMatch != null) {
      final count = threatMatch.group(1);
      return 'Pemindaian Selesai • $count Ancaman Ditemukan';
    }
    if (!log.isSafe) {
      final count = log.threats.isNotEmpty ? log.threats.length : 3;
      return 'Pemindaian Selesai • $count Ancaman Ditemukan';
    }
    return log.title.replaceAll('Kaspersky ', '');
  }
}
