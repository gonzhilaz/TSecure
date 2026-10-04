import 'package:flutter/material.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_typography.dart';

class ThreatTelemetryStrip extends StatelessWidget {
  final bool isRealtimeActive;
  final ValueChanged<bool> onRealtimeToggled;

  const ThreatTelemetryStrip({
    super.key,
    required this.isRealtimeActive,
    required this.onRealtimeToggled,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      decoration: BoxDecoration(
        color: AppColors.surfaceCard,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: AppColors.slateBorder),
      ),
      child: Row(
        children: [
          // Telemetry Icon
          Container(
            width: 36,
            height: 36,
            decoration: BoxDecoration(
              color: isRealtimeActive
                  ? const Color(0xFF10B981).withValues(alpha: 0.12)
                  : AppColors.slateDivider,
              borderRadius: BorderRadius.circular(10),
            ),
            child: Icon(
              isRealtimeActive ? Icons.shield_rounded : Icons.shield_outlined,
              color: isRealtimeActive ? const Color(0xFF10B981) : AppColors.slateMuted,
              size: 20,
            ),
          ),
          const SizedBox(width: 12),
          // Info
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Text(
                      'Proteksi Real-Time',
                      style: AppTypography.headlineSm.copyWith(
                        fontSize: 14,
                        fontWeight: FontWeight.w700,
                        color: AppColors.navyDeep,
                      ),
                    ),
                    const SizedBox(width: 6),
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 1.5),
                      decoration: BoxDecoration(
                        color: isRealtimeActive
                            ? const Color(0xFF10B981).withValues(alpha: 0.15)
                            : AppColors.slateDivider,
                        borderRadius: BorderRadius.circular(6),
                      ),
                      child: Text(
                        isRealtimeActive ? 'AKTIF' : 'NONAKTIF',
                        style: AppTypography.labelSm.copyWith(
                          fontSize: 9,
                          fontWeight: FontWeight.w800,
                          color: isRealtimeActive
                              ? const Color(0xFF10B981)
                              : AppColors.slateMuted,
                        ),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 2),
                Text(
                  isRealtimeActive
                      ? 'Kaspersky Mobile Security Engine Aktif'
                      : 'Aktifkan untuk memblokir malware instan',
                  style: AppTypography.bodySm.copyWith(
                    fontSize: 11,
                    color: AppColors.slateMuted,
                  ),
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                ),
              ],
            ),
          ),
          // Clean Switch
          Switch(
            value: isRealtimeActive,
            onChanged: onRealtimeToggled,
            activeThumbColor: Colors.white,
            activeTrackColor: const Color(0xFF10B981),
            inactiveThumbColor: Colors.white,
            inactiveTrackColor: AppColors.slateDivider,
            materialTapTargetSize: MaterialTapTargetSize.shrinkWrap,
          ),
        ],
      ),
    );
  }
}
