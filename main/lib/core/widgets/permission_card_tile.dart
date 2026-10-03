import 'package:flutter/material.dart';
import '../theme/app_colors.dart';
import '../theme/app_typography.dart';

class PermissionCardTile extends StatelessWidget {
  final IconData icon;
  final String title;
  final String description;
  final bool isGranted;
  final VoidCallback onActivate;
  final String? activeLabel;
  final String? inactiveLabel;

  const PermissionCardTile({
    super.key,
    required this.icon,
    required this.title,
    required this.description,
    required this.isGranted,
    required this.onActivate,
    this.activeLabel,
    this.inactiveLabel,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: AppColors.background,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(
          color: isGranted
              ? AppColors.statusSafeEmerald.withValues(alpha: 0.3)
              : AppColors.slateBorder,
        ),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Container(
                width: 36,
                height: 36,
                decoration: BoxDecoration(
                  color: isGranted
                      ? AppColors.statusSafeEmerald.withValues(alpha: 0.12)
                      : AppColors.primary.withValues(alpha: 0.1),
                  borderRadius: BorderRadius.circular(10),
                ),
                child: Icon(
                  icon,
                  color: isGranted ? AppColors.statusSafeEmerald : AppColors.primary,
                  size: 20,
                ),
              ),
              const SizedBox(width: 10),
              Expanded(
                child: Text(
                  title,
                  style: AppTypography.labelMd.copyWith(fontWeight: FontWeight.w700),
                ),
              ),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                decoration: BoxDecoration(
                  color: isGranted
                      ? AppColors.statusSafeEmerald.withValues(alpha: 0.12)
                      : AppColors.slateDivider,
                  borderRadius: BorderRadius.circular(6),
                ),
                child: Text(
                  isGranted ? (activeLabel ?? 'Aktif') : (inactiveLabel ?? 'Nonaktif'),
                  style: TextStyle(
                    color: isGranted ? AppColors.statusSafeEmerald : AppColors.slateMid,
                    fontSize: 10,
                    fontWeight: FontWeight.w700,
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 8),
          Text(
            description,
            style: AppTypography.bodySm.copyWith(color: AppColors.slateMuted, fontSize: 11),
          ),
          const SizedBox(height: 12),
          Align(
            alignment: Alignment.centerRight,
            child: SizedBox(
              height: 34,
              child: isGranted
                  ? OutlinedButton.icon(
                      onPressed: onActivate,
                      icon: const Icon(Icons.settings, size: 14),
                      label: const Text('Kelola', style: TextStyle(fontSize: 11)),
                      style: OutlinedButton.styleFrom(
                        foregroundColor: AppColors.slateMid,
                        side: const BorderSide(color: AppColors.slateBorder),
                        padding: const EdgeInsets.symmetric(horizontal: 12),
                        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
                      ),
                    )
                  : ElevatedButton.icon(
                      onPressed: onActivate,
                      icon: const Icon(Icons.arrow_forward_rounded, size: 14),
                      label: const Text('Buka Pengaturan', style: TextStyle(fontSize: 11)),
                      style: ElevatedButton.styleFrom(
                        backgroundColor: AppColors.primary,
                        foregroundColor: Colors.white,
                        elevation: 0,
                        padding: const EdgeInsets.symmetric(horizontal: 14),
                        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
                      ),
                    ),
            ),
          ),
        ],
      ),
    );
  }
}
