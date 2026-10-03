import 'package:flutter/material.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_typography.dart';

/// Security Features Row matching Option B (/design/dashboard.jpeg):
/// 4 curated security items in a single horizontal row inside a clean card.
class SecurityFeaturesGrid extends StatelessWidget {
  final ValueChanged<String> onFeatureTapped;

  const SecurityFeaturesGrid({
    super.key,
    required this.onFeatureTapped,
  });

  @override
  Widget build(BuildContext context) {
    final features = [
      _FeatureItem(
        title: 'Anti Malware',
        featureKey: 'Realtime Scanner',
        icon: Icons.shield_outlined,
        bgColor: AppColors.primary,
        iconColor: Colors.white,
      ),
      _FeatureItem(
        title: 'Web Filter',
        featureKey: 'Web Filter',
        icon: Icons.language_rounded,
        bgColor: const Color(0xFFF1F5F9),
        iconColor: AppColors.navyDeep,
      ),
      _FeatureItem(
        title: 'Wi-Fi Safety',
        featureKey: 'Wifi Safety',
        icon: Icons.wifi_rounded,
        bgColor: const Color(0xFFF1F5F9),
        iconColor: AppColors.navyDeep,
      ),
      _FeatureItem(
        title: 'Device Risk',
        featureKey: 'Device Reputation',
        icon: Icons.screen_lock_portrait_rounded,
        bgColor: const Color(0xFFF1F5F9),
        iconColor: AppColors.navyDeep,
      ),
    ];

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 16),
      decoration: BoxDecoration(
        color: AppColors.surfaceCard,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: AppColors.slateBorder),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.03),
            blurRadius: 10,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Fitur Keamanan',
            style: AppTypography.headlineSm.copyWith(
              color: AppColors.navyDeep,
              fontWeight: FontWeight.w700,
            ),
          ),
          const SizedBox(height: 14),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: features.map((item) {
              return Expanded(
                child: InkWell(
                  onTap: () => onFeatureTapped(item.featureKey),
                  borderRadius: BorderRadius.circular(14),
                  child: Padding(
                    padding: const EdgeInsets.symmetric(horizontal: 4.0),
                    child: Column(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Container(
                          width: 50,
                          height: 50,
                          decoration: BoxDecoration(
                            color: item.bgColor,
                            borderRadius: BorderRadius.circular(14),
                            boxShadow: item.bgColor == AppColors.primary
                                ? [
                                    BoxShadow(
                                      color: AppColors.primary.withValues(alpha: 0.28),
                                      blurRadius: 8,
                                      offset: const Offset(0, 3),
                                    ),
                                  ]
                                : null,
                          ),
                          child: Center(
                            child: Icon(item.icon, color: item.iconColor, size: 24),
                          ),
                        ),
                        const SizedBox(height: 8),
                        Text(
                          item.title,
                          style: AppTypography.bodySm.copyWith(
                            color: AppColors.navyDeep,
                            fontWeight: FontWeight.w600,
                            fontSize: 11,
                          ),
                          textAlign: TextAlign.center,
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                        ),
                      ],
                    ),
                  ),
                ),
              );
            }).toList(),
          ),
        ],
      ),
    );
  }
}

class _FeatureItem {
  final String title;
  final String featureKey;
  final IconData icon;
  final Color bgColor;
  final Color iconColor;

  const _FeatureItem({
    required this.title,
    required this.featureKey,
    required this.icon,
    required this.bgColor,
    required this.iconColor,
  });
}
