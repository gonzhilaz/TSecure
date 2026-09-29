import 'package:flutter/material.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_typography.dart';

class MonthlySummaryCard extends StatelessWidget {
  final int totalScans;
  final int threatsFound;

  const MonthlySummaryCard({
    super.key,
    required this.totalScans,
    required this.threatsFound,
  });

  @override
  Widget build(BuildContext context) {
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
          Text(
            'Ringkasan Bulan Ini',
            style: AppTypography.headlineSm.copyWith(
              color: AppColors.navyDeep,
              fontWeight: FontWeight.w700,
            ),
          ),
          const SizedBox(height: 16),
          Row(
            children: [
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text('Total Pindai', style: AppTypography.bodySm),
                    const SizedBox(height: 2),
                    Text(
                      '$totalScans',
                      style: AppTypography.headlineXl.copyWith(
                        fontWeight: FontWeight.w800,
                        fontSize: 26,
                      ),
                    ),
                    const SizedBox(height: 2),
                    Text(
                      '100% Bersih',
                      style: AppTypography.labelSm.copyWith(
                        color: AppColors.statusSafeEmerald,
                      ),
                    ),
                  ],
                ),
              ),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text('Ancaman', style: AppTypography.bodySm),
                    const SizedBox(height: 2),
                    Text(
                      '$threatsFound',
                      style: AppTypography.headlineXl.copyWith(
                        fontWeight: FontWeight.w800,
                        fontSize: 26,
                        color: threatsFound > 0
                            ? AppColors.primary
                            : AppColors.navyDeep,
                      ),
                    ),
                    const SizedBox(height: 2),
                    Text(
                      'Dinonaktifkan',
                      style: AppTypography.labelSm.copyWith(
                        color: AppColors.slateMuted,
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ),
          const SizedBox(height: 14),
          Text(
            'Semua pemindaian & audit jaringan berjalan otomatis',
            style: AppTypography.bodySm.copyWith(
              color: AppColors.slateMuted,
            ),
          ),
        ],
      ),
    );
  }
}
