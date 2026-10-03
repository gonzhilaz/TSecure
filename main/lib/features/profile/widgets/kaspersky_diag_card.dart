import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_typography.dart';
import '../../../data/services/kaspersky_sdk_bridge.dart';

/// Simplified Kaspersky badge shown in the profile screen.
/// Displays "Powered by Kaspersky Security Engine" with an active/dormant status indicator.
class KasperskyDiagCard extends StatelessWidget {
  const KasperskyDiagCard({super.key});

  @override
  Widget build(BuildContext context) {
    final ksp = context.watch<KasperskySdkBridge>();
    final isActive = ksp.isInitialized;

    return Container(
      width: double.infinity,
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: AppColors.slateBorder),
      ),
      child: Row(
        children: [
          Container(
            padding: const EdgeInsets.all(8),
            decoration: BoxDecoration(
              color: (isActive ? AppColors.statusSafeEmerald : AppColors.slateMuted)
                  .withValues(alpha: 0.12),
              borderRadius: BorderRadius.circular(10),
            ),
            child: Icon(
              isActive ? Icons.verified_user_rounded : Icons.shield_outlined,
              color: isActive ? AppColors.statusSafeEmerald : AppColors.slateMuted,
              size: 20,
            ),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'Powered by Kaspersky Security Engine',
                  style: AppTypography.labelMd.copyWith(
                    color: AppColors.navyDeep,
                    fontWeight: FontWeight.w700,
                  ),
                ),
                const SizedBox(height: 2),
                Text(
                  isActive ? 'Mesin Aktif • Perlindungan Berjalan' : 'Mesin Dorman',
                  style: AppTypography.bodySm.copyWith(
                    color: isActive ? AppColors.statusSafeEmerald : AppColors.slateMuted,
                    fontWeight: FontWeight.w600,
                    fontSize: 11,
                  ),
                ),
              ],
            ),
          ),
          if (!ksp.hasFullStorageAccess)
            IconButton(
              onPressed: () => ksp.requestFullStoragePermission(),
              icon: const Icon(Icons.folder_special_rounded, size: 18),
              tooltip: 'Izinkan Akses Seluruh File',
              color: AppColors.statusWarning,
              style: IconButton.styleFrom(
                backgroundColor: AppColors.statusWarning.withValues(alpha: 0.1),
                padding: const EdgeInsets.all(8),
                minimumSize: const Size(36, 36),
              ),
            ),
        ],
      ),
    );
  }
}
