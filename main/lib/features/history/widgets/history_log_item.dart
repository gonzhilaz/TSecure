import 'package:flutter/material.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_typography.dart';
import '../../../data/models/activity_log.dart';

class HistoryLogItem extends StatelessWidget {
  final ActivityLog log;
  final VoidCallback? onTap;

  const HistoryLogItem({
    super.key,
    required this.log,
    this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.only(bottom: 12),
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: AppColors.surfaceCard,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: AppColors.slateBorder),
      ),
      child: InkWell(
        onTap: onTap,
        child: Row(
          children: [
            Container(
              width: 44,
              height: 44,
              decoration: BoxDecoration(
                color: log.isSafe
                    ? AppColors.slateDivider
                    : AppColors.statusDangerBg,
                borderRadius: BorderRadius.circular(12),
              ),
              child: Center(
                child: Icon(
                  log.icon,
                  color: log.isSafe ? AppColors.navyDeep : AppColors.primary,
                  size: 22,
                ),
              ),
            ),
            const SizedBox(width: 14),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    log.title,
                    style: AppTypography.labelLg.copyWith(
                      fontWeight: FontWeight.w700,
                      color: AppColors.navyDeep,
                    ),
                  ),
                  const SizedBox(height: 3),
                  Text(
                    log.description,
                    style: AppTypography.bodySm.copyWith(
                      color: AppColors.slateMuted,
                    ),
                    maxLines: 2,
                    overflow: TextOverflow.ellipsis,
                  ),
                  const SizedBox(height: 4),
                  Text(
                    log.time,
                    style: AppTypography.bodySm.copyWith(
                      fontSize: 10,
                      color: AppColors.slateMuted.withValues(alpha: 0.8),
                    ),
                  ),
                ],
              ),
            ),
            const Icon(
              Icons.arrow_forward_ios,
              size: 14,
              color: AppColors.slateMuted,
            ),
          ],
        ),
      ),
    );
  }
}
