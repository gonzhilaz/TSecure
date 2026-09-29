import 'package:flutter/material.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_typography.dart';
import '../../../data/models/security_audit_item.dart';

class AuditListTile extends StatelessWidget {
  final SecurityAuditItem item;
  final VoidCallback? onActionPressed;

  const AuditListTile({
    super.key,
    required this.item,
    this.onActionPressed,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.only(bottom: 12),
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
      decoration: BoxDecoration(
        color: AppColors.surfaceCard,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: AppColors.slateBorder),
      ),
      child: Row(
        children: [
          Container(
            width: 44,
            height: 44,
            decoration: BoxDecoration(
              color: AppColors.primary,
              borderRadius: BorderRadius.circular(12),
            ),
            child: Center(
              child: Icon(item.icon, color: Colors.white, size: 22),
            ),
          ),
          const SizedBox(width: 14),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  item.title,
                  style: AppTypography.labelLg.copyWith(
                    fontWeight: FontWeight.w700,
                    color: AppColors.navyDeep,
                  ),
                ),
                const SizedBox(height: 2),
                Text(
                  item.subtitle,
                  style: AppTypography.bodySm.copyWith(
                    color: AppColors.slateMuted,
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(width: 8),
          _buildActionOrCheck(),
        ],
      ),
    );
  }

  Widget _buildActionOrCheck() {
    if (item.actionLabel != null) {
      final isRed = item.actionLabel == 'Pindai';
      return SizedBox(
        height: 32,
        child: ElevatedButton(
          style: ElevatedButton.styleFrom(
            backgroundColor: isRed ? AppColors.primary : AppColors.slateDivider,
            foregroundColor: isRed ? Colors.white : AppColors.navyDeep,
            padding: const EdgeInsets.symmetric(horizontal: 14),
            minimumSize: Size.zero,
            tapTargetSize: MaterialTapTargetSize.shrinkWrap,
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(16),
            ),
            elevation: 0,
          ),
          onPressed: onActionPressed,
          child: Text(
            item.actionLabel!,
            style: AppTypography.labelSm.copyWith(
              fontWeight: FontWeight.w700,
              color: isRed ? Colors.white : AppColors.navyDeep,
            ),
          ),
        ),
      );
    }

    return Container(
      width: 28,
      height: 28,
      decoration: const BoxDecoration(
        color: Color(0xFFFBE9E7),
        shape: BoxShape.circle,
      ),
      child: const Center(
        child: Icon(
          Icons.check,
          color: AppColors.primary,
          size: 18,
        ),
      ),
    );
  }
}
