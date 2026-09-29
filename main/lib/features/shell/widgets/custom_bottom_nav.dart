import 'package:flutter/material.dart';
import '../../../core/theme/app_colors.dart';

class CustomBottomNav extends StatelessWidget {
  final int currentIndex;
  final ValueChanged<int> onTabSelected;

  const CustomBottomNav({
    super.key,
    required this.currentIndex,
    required this.onTabSelected,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      height: 78,
      decoration: const BoxDecoration(
        color: AppColors.surface,
        border: Border(
          top: BorderSide(color: AppColors.slateBorder, width: 1),
        ),
      ),
      child: SafeArea(
        top: false,
        child: Row(
          mainAxisAlignment: MainAxisAlignment.spaceAround,
          children: [
            _buildNavItem(
              index: 0,
              icon: Icons.grid_view_rounded,
              isSelected: currentIndex == 0,
            ),
            _buildNavItem(
              index: 1,
              icon: Icons.phone_android_rounded,
              isSelected: currentIndex == 1,
            ),
            _buildCenterScanButton(),
            _buildNavItem(
              index: 3,
              icon: Icons.history_rounded,
              isSelected: currentIndex == 3,
            ),
            _buildNavItem(
              index: 4,
              icon: Icons.person_outline_rounded,
              isSelected: currentIndex == 4,
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildNavItem({
    required int index,
    required IconData icon,
    required bool isSelected,
  }) {
    return InkWell(
      onTap: () => onTabSelected(index),
      borderRadius: BorderRadius.circular(16),
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(
              icon,
              size: 26,
              color: isSelected ? AppColors.primary : AppColors.slateMuted,
            ),
            const SizedBox(height: 4),
            Container(
              height: 2,
              width: 14,
              decoration: BoxDecoration(
                color: isSelected ? AppColors.primary : Colors.transparent,
                borderRadius: BorderRadius.circular(1),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildCenterScanButton() {
    final isSelected = currentIndex == 2;
    return GestureDetector(
      onTap: () => onTabSelected(2),
      child: Container(
        width: 54,
        height: 54,
        decoration: BoxDecoration(
          color: AppColors.primary,
          borderRadius: BorderRadius.circular(16),
          boxShadow: [
            BoxShadow(
              color: AppColors.primary.withValues(alpha: 0.45),
              blurRadius: 16,
              offset: const Offset(0, 4),
            ),
          ],
          border: isSelected
              ? Border.all(color: Colors.white, width: 2)
              : null,
        ),
        child: const Center(
          child: Icon(
            Icons.document_scanner_outlined,
            size: 28,
            color: Colors.white,
          ),
        ),
      ),
    );
  }
}
