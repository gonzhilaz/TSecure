import 'package:flutter/material.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_typography.dart';

class BentoSecurityFeaturesGrid extends StatelessWidget {
  final ValueChanged<String> onFeatureTapped;

  const BentoSecurityFeaturesGrid({
    super.key,
    required this.onFeatureTapped,
  });

  @override
  Widget build(BuildContext context) {
    final features = [
      _BentoFeature(
        title: 'Anti-Malware',
        subtitle: 'KavSDK File Guard',
        featureKey: 'Realtime Scanner',
        icon: Icons.security_rounded,
        badgeText: 'Aktif',
        badgeColor: const Color(0xFF10B981),
        accentBg: AppColors.primary.withValues(alpha: 0.08),
        iconColor: AppColors.primary,
      ),
      _BentoFeature(
        title: 'Web Filter',
        subtitle: 'Anti-Phishing URL',
        featureKey: 'Web Filter',
        icon: Icons.language_rounded,
        badgeText: 'Aktif',
        badgeColor: const Color(0xFF10B981),
        accentBg: const Color(0xFF007EB4).withValues(alpha: 0.08),
        iconColor: const Color(0xFF007EB4),
      ),
      _BentoFeature(
        title: 'Wi-Fi Safety',
        subtitle: 'Audit Jaringan',
        featureKey: 'Wifi Safety',
        icon: Icons.wifi_lock_rounded,
        badgeText: 'Aman',
        badgeColor: const Color(0xFF10B981),
        accentBg: const Color(0xFF6366F1).withValues(alpha: 0.08),
        iconColor: const Color(0xFF6366F1),
      ),
      _BentoFeature(
        title: 'Device Risk',
        subtitle: 'Root & OS Guard',
        featureKey: 'Device Reputation',
        icon: Icons.phonelink_lock_rounded,
        badgeText: 'Aman',
        badgeColor: const Color(0xFF10B981),
        accentBg: const Color(0xFFF59E0B).withValues(alpha: 0.08),
        iconColor: const Color(0xFFF59E0B),
      ),
    ];

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text(
              'Modul Keamanan',
              style: AppTypography.headlineSm.copyWith(
                color: AppColors.navyDeep,
                fontWeight: FontWeight.w700,
                fontSize: 16,
              ),
            ),
            InkWell(
              onTap: () => onFeatureTapped('Lainnya'),
              borderRadius: BorderRadius.circular(8),
              child: Padding(
                padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 4),
                child: Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Text(
                      'Semua Modul',
                      style: AppTypography.labelSm.copyWith(
                        color: AppColors.primary,
                        fontWeight: FontWeight.w700,
                      ),
                    ),
                    const SizedBox(width: 4),
                    const Icon(
                      Icons.arrow_forward_ios_rounded,
                      size: 11,
                      color: AppColors.primary,
                    ),
                  ],
                ),
              ),
            ),
          ],
        ),
        const SizedBox(height: 12),
        // 2x2 Bento Grid
        GridView.builder(
          shrinkWrap: true,
          physics: const NeverScrollableScrollPhysics(),
          itemCount: features.length,
          gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
            crossAxisCount: 2,
            crossAxisSpacing: 12,
            mainAxisSpacing: 12,
            childAspectRatio: 1.55,
          ),
          itemBuilder: (context, index) {
            final item = features[index];
            return Material(
              color: AppColors.surfaceCard,
              borderRadius: BorderRadius.circular(16),
              child: InkWell(
                onTap: () => onFeatureTapped(item.featureKey),
                borderRadius: BorderRadius.circular(16),
                child: Container(
                  padding: const EdgeInsets.all(12),
                  decoration: BoxDecoration(
                    borderRadius: BorderRadius.circular(16),
                    border: Border.all(color: AppColors.slateBorder),
                  ),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          Container(
                            width: 34,
                            height: 34,
                            decoration: BoxDecoration(
                              color: item.accentBg,
                              borderRadius: BorderRadius.circular(10),
                            ),
                            child: Icon(
                              item.icon,
                              color: item.iconColor,
                              size: 18,
                            ),
                          ),
                          Container(
                            padding: const EdgeInsets.symmetric(
                              horizontal: 6,
                              vertical: 2,
                            ),
                            decoration: BoxDecoration(
                              color: item.badgeColor.withValues(alpha: 0.12),
                              borderRadius: BorderRadius.circular(8),
                            ),
                            child: Row(
                              mainAxisSize: MainAxisSize.min,
                              children: [
                                Container(
                                  width: 5,
                                  height: 5,
                                  decoration: BoxDecoration(
                                    color: item.badgeColor,
                                    shape: BoxShape.circle,
                                  ),
                                ),
                                const SizedBox(width: 4),
                                Text(
                                  item.badgeText,
                                  style: AppTypography.labelSm.copyWith(
                                    color: item.badgeColor,
                                    fontWeight: FontWeight.w700,
                                    fontSize: 9,
                                  ),
                                ),
                              ],
                            ),
                          ),
                        ],
                      ),
                      Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            item.title,
                            style: AppTypography.headlineSm.copyWith(
                              fontSize: 13,
                              fontWeight: FontWeight.w700,
                              color: AppColors.navyDeep,
                            ),
                            maxLines: 1,
                            overflow: TextOverflow.ellipsis,
                          ),
                          const SizedBox(height: 1),
                          Text(
                            item.subtitle,
                            style: AppTypography.bodySm.copyWith(
                              fontSize: 10,
                              color: AppColors.slateMuted,
                            ),
                            maxLines: 1,
                            overflow: TextOverflow.ellipsis,
                          ),
                        ],
                      ),
                    ],
                  ),
                ),
              ),
            );
          },
        ),
      ],
    );
  }
}

class _BentoFeature {
  final String title;
  final String subtitle;
  final String featureKey;
  final IconData icon;
  final String badgeText;
  final Color badgeColor;
  final Color accentBg;
  final Color iconColor;

  const _BentoFeature({
    required this.title,
    required this.subtitle,
    required this.featureKey,
    required this.icon,
    required this.badgeText,
    required this.badgeColor,
    required this.accentBg,
    required this.iconColor,
  });
}
