import 'package:flutter/material.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_typography.dart';

class SecurityFeaturesGrid extends StatelessWidget {
  final ValueChanged<String> onFeatureTapped;

  const SecurityFeaturesGrid({
    super.key,
    required this.onFeatureTapped,
  });

  @override
  Widget build(BuildContext context) {
    final features = [
      _FeatureItem('Web Filter', Icons.language, AppColors.slateDivider, AppColors.navyDeep),
      _FeatureItem('Realtime Sc...', Icons.shield, AppColors.primary, Colors.white),
      _FeatureItem('PUA Scanner', Icons.security, AppColors.slateDivider, AppColors.navyDeep),
      _FeatureItem('Wifi Safety', Icons.wifi, AppColors.slateDivider, AppColors.navyDeep),
      _FeatureItem('Fake Apps', Icons.warning_amber_rounded, AppColors.slateDivider, AppColors.navyDeep),
      _FeatureItem('Device Rep', Icons.phone_android, AppColors.slateDivider, AppColors.navyDeep),
      _FeatureItem('Data Breach', Icons.dns_outlined, AppColors.slateDivider, AppColors.navyDeep),
      _FeatureItem('Lainnya', Icons.apps, AppColors.slateDivider, AppColors.navyDeep),
    ];

    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: AppColors.surfaceCard,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: AppColors.slateBorder),
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
          const SizedBox(height: 16),
          GridView.builder(
            itemCount: features.length,
            shrinkWrap: true,
            physics: const NeverScrollableScrollPhysics(),
            gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
              crossAxisCount: 4,
              mainAxisSpacing: 16,
              crossAxisSpacing: 8,
              childAspectRatio: 0.8,
            ),
            itemBuilder: (context, index) {
              final item = features[index];
              return InkWell(
                onTap: () => onFeatureTapped(item.title),
                borderRadius: BorderRadius.circular(12),
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Container(
                      width: 52,
                      height: 52,
                      decoration: BoxDecoration(
                        color: item.bgColor,
                        borderRadius: BorderRadius.circular(14),
                      ),
                      child: Center(
                        child: Icon(item.icon, color: item.iconColor, size: 26),
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
              );
            },
          ),
        ],
      ),
    );
  }
}

class _FeatureItem {
  final String title;
  final IconData icon;
  final Color bgColor;
  final Color iconColor;

  _FeatureItem(this.title, this.icon, this.bgColor, this.iconColor);
}
