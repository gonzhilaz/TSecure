import 'package:flutter/material.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_typography.dart';
import '../../../core/widgets/app_button.dart';
import '../../../data/models/security_score.dart';

class ProtectionStatusCard extends StatelessWidget {
  final SecurityScore securityScore;
  final bool isRealtimeActive;
  final String lastScanText;
  final VoidCallback onScanPressed;
  final ValueChanged<bool> onRealtimeToggled;

  const ProtectionStatusCard({
    super.key,
    required this.securityScore,
    required this.isRealtimeActive,
    required this.lastScanText,
    required this.onScanPressed,
    required this.onRealtimeToggled,
  });

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        // Card 1: Scan Overview Card
        Container(
          padding: const EdgeInsets.all(16),
          decoration: BoxDecoration(
            color: AppColors.surfaceCard,
            borderRadius: BorderRadius.circular(16),
            border: Border.all(color: AppColors.slateBorder),
          ),
          child: Row(
            children: [
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Perangkat ${securityScore.statusText}',
                      style: AppTypography.headlineSm.copyWith(
                        color: AppColors.navyDeep,
                        fontWeight: FontWeight.w700,
                      ),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      'Pemindaian terakhir: $lastScanText',
                      style: AppTypography.bodySm.copyWith(
                        color: AppColors.slateMuted,
                      ),
                    ),
                  ],
                ),
              ),
              AppButton(
                label: 'Pindai',
                width: 90,
                height: 38,
                onPressed: onScanPressed,
              ),
            ],
          ),
        ),
        const SizedBox(height: 12),
        // Card 2: Realtime Protection Toggle Card
        Container(
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
          decoration: BoxDecoration(
            color: AppColors.surfaceCard,
            borderRadius: BorderRadius.circular(16),
            border: Border.all(color: AppColors.slateBorder),
          ),
          child: Row(
            children: [
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Proteksi Real-Time',
                      style: AppTypography.headlineSm.copyWith(
                        color: AppColors.navyDeep,
                        fontWeight: FontWeight.w700,
                        fontSize: 15,
                      ),
                    ),
                    const SizedBox(height: 2),
                    Text(
                      'Trojan & ransomware aktif diblokir',
                      style: AppTypography.bodySm.copyWith(
                        color: AppColors.slateMuted,
                      ),
                    ),
                  ],
                ),
              ),
              Switch(
                value: isRealtimeActive,
                activeTrackColor: AppColors.statusSafeEmerald,
                activeThumbColor: Colors.white,
                inactiveThumbColor: Colors.white,
                inactiveTrackColor: AppColors.slateBorder,
                onChanged: onRealtimeToggled,
              ),
            ],
          ),
        ),
      ],
    );
  }
}
