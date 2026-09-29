import 'package:flutter/material.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_typography.dart';
import '../../../core/widgets/app_button.dart';

class TelemetryChartCard extends StatelessWidget {
  final VoidCallback onScanPressed;

  const TelemetryChartCard({
    super.key,
    required this.onScanPressed,
  });

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        // Left Card: Waktu Terakhir Scan
        Expanded(
          child: Container(
            height: 156,
            padding: const EdgeInsets.all(14),
            decoration: BoxDecoration(
              color: AppColors.surfaceCard,
              borderRadius: BorderRadius.circular(16),
              border: Border.all(color: AppColors.slateBorder),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Waktu Terakhir\nScan',
                      style: AppTypography.labelLg.copyWith(
                        fontWeight: FontWeight.w700,
                        color: AppColors.navyDeep,
                        height: 1.2,
                      ),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      '3 jam yang lalu',
                      style: AppTypography.bodySm.copyWith(
                        color: AppColors.slateMuted,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 12),
                AppButton(
                  label: 'Pindai Perangkat',
                  height: 36,
                  onPressed: onScanPressed,
                ),
              ],
            ),
          ),
        ),
        const SizedBox(width: 12),
        // Right Card: Proteksi Aktif with Bar Graph
        Expanded(
          child: Container(
            height: 156,
            padding: const EdgeInsets.all(14),
            decoration: BoxDecoration(
              color: AppColors.surfaceCard,
              borderRadius: BorderRadius.circular(16),
              border: Border.all(color: AppColors.slateBorder),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Proteksi Aktif',
                      style: AppTypography.labelLg.copyWith(
                        fontWeight: FontWeight.w700,
                        color: AppColors.navyDeep,
                      ),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      'Realtime Shield 24/7',
                      style: AppTypography.bodySm.copyWith(
                        color: AppColors.slateMuted,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 12),
                _buildBarTelemetry(),
              ],
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildBarTelemetry() {
    final bars = [
      _BarData(16, const Color(0xFFE8EEF5)),
      _BarData(24, const Color(0xFFE8EEF5)),
      _BarData(32, const Color(0xFFE8EEF5)),
      _BarData(42, const Color(0xFFFFD5D9)),
      _BarData(54, AppColors.primary),
      _BarData(48, AppColors.statusSafeEmerald),
    ];

    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceEvenly,
      crossAxisAlignment: CrossAxisAlignment.end,
      children: bars.map((b) {
        return Container(
          width: 8,
          height: b.height,
          decoration: BoxDecoration(
            color: b.color,
            borderRadius: BorderRadius.circular(4),
          ),
        );
      }).toList(),
    );
  }
}

class _BarData {
  final double height;
  final Color color;
  _BarData(this.height, this.color);
}
