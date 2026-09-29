import 'package:flutter/material.dart';
import '../theme/app_colors.dart';
import '../theme/app_typography.dart';

enum AppButtonType { primary, secondary, outline }

class AppButton extends StatelessWidget {
  final String label;
  final VoidCallback? onPressed;
  final AppButtonType type;
  final bool isLoading;
  final IconData? icon;
  final double? width;
  final double height;

  const AppButton({
    super.key,
    required this.label,
    required this.onPressed,
    this.type = AppButtonType.primary,
    this.isLoading = false,
    this.icon,
    this.width,
    this.height = 48,
  });

  @override
  Widget build(BuildContext context) {
    final effectiveWidth = width ?? double.infinity;

    if (type == AppButtonType.outline) {
      return SizedBox(
        width: effectiveWidth,
        height: height,
        child: OutlinedButton(
          style: OutlinedButton.styleFrom(
            minimumSize: Size.zero,
            tapTargetSize: MaterialTapTargetSize.shrinkWrap,
          ),
          onPressed: isLoading ? null : onPressed,
          child: _buildChild(AppColors.navyDeep),
        ),
      );
    }

    final isPrimary = type == AppButtonType.primary;
    return SizedBox(
      width: effectiveWidth,
      height: height,
      child: ElevatedButton(
        style: ElevatedButton.styleFrom(
          backgroundColor: isPrimary ? AppColors.primary : AppColors.navySurface,
          foregroundColor: AppColors.textOnPrimary,
          minimumSize: Size.zero,
          tapTargetSize: MaterialTapTargetSize.shrinkWrap,
        ),
        onPressed: isLoading ? null : onPressed,
        child: _buildChild(AppColors.textOnPrimary),
      ),
    );
  }

  Widget _buildChild(Color textColor) {
    if (isLoading) {
      return SizedBox(
        width: 20,
        height: 20,
        child: CircularProgressIndicator(
          strokeWidth: 2,
          valueColor: AlwaysStoppedAnimation<Color>(textColor),
        ),
      );
    }

    if (icon != null) {
      return Row(
        mainAxisSize: MainAxisSize.min,
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Text(
            label,
            style: AppTypography.labelLg.copyWith(color: textColor),
          ),
          const SizedBox(width: 8),
          Icon(icon, size: 18, color: textColor),
        ],
      );
    }

    return Text(
      label,
      style: AppTypography.labelLg.copyWith(color: textColor),
    );
  }
}
