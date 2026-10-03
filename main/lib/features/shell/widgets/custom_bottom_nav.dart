import 'package:flutter/material.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/stitch_icons.dart';

/// Fixed Bottom Navigation Bar matching Stitch design specification:
/// - Exact 64px row height wrapped in SafeArea
/// - Top active indicator bar (w-8 h-0.5 bg-primary rounded-full)
/// - Center elevated scan button (-translate-y-2.5, rounded-2xl with red glow)
/// - Pixel-perfect Stitch SVG icons: 2x2 grid, smartphone, scanner target, clock arrow, user outline
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
      decoration: BoxDecoration(
        color: AppColors.surface,
        border: const Border(
          top: BorderSide(color: Color(0xFFF1F5F9), width: 1),
        ),
        boxShadow: [
          BoxShadow(
            color: const Color(0xFF0B132B).withValues(alpha: 0.05),
            blurRadius: 16,
            offset: const Offset(0, -4),
          ),
        ],
      ),
      child: SafeArea(
        top: false,
        child: SizedBox(
          height: 60,
          child: Row(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              // 1. Beranda (Grid 2x2 outline)
              _buildNavItem(
                index: 0,
                svgIcon: StitchIcons.navDashboard,
                isSelected: currentIndex == 0,
              ),
              // 2. Perangkat (Smartphone outline with dot)
              _buildNavItem(
                index: 1,
                svgIcon: StitchIcons.navDevices,
                isSelected: currentIndex == 1,
              ),
              // 3. Center elevated Scan button
              _buildCenterScanButton(),
              // 4. Riwayat Aktivitas (Clock counter-clockwise)
              _buildNavItem(
                index: 3,
                svgIcon: StitchIcons.navHistory,
                isSelected: currentIndex == 3,
              ),
              // 5. Profil (User outline)
              _buildNavItem(
                index: 4,
                svgIcon: StitchIcons.navProfile,
                isSelected: currentIndex == 4,
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildNavItem({
    required int index,
    required String svgIcon,
    required bool isSelected,
  }) {
    final color = isSelected ? AppColors.primary : AppColors.slateMuted;

    return Expanded(
      child: Material(
        color: Colors.transparent,
        child: InkWell(
          onTap: () => onTabSelected(index),
          splashColor: AppColors.primary.withValues(alpha: 0.08),
          highlightColor: Colors.transparent,
          child: Stack(
            alignment: Alignment.center,
            children: [
              // Top active indicator (Stitch: w-8 h-0.5 bg-brand-red rounded-full)
              Positioned(
                top: 0,
                child: Container(
                  width: 32,
                  height: 2.5,
                  decoration: BoxDecoration(
                    color: isSelected ? AppColors.primary : Colors.transparent,
                    borderRadius: BorderRadius.circular(2),
                  ),
                ),
              ),
              // Tab Icon using exact Stitch SVG
              Center(
                child: StitchSvg(
                  svgString: svgIcon,
                  size: 24,
                  color: color,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildCenterScanButton() {
    final isSelected = currentIndex == 2;
    return Expanded(
      child: Center(
        child: Transform.translate(
          offset: const Offset(0, -10),
          child: GestureDetector(
            onTap: () => onTabSelected(2),
            child: Container(
              width: 52,
              height: 52,
              decoration: BoxDecoration(
                color: AppColors.primary,
                borderRadius: BorderRadius.circular(16),
                boxShadow: [
                  BoxShadow(
                    color: AppColors.primary.withValues(alpha: 0.40),
                    blurRadius: 16,
                    offset: const Offset(0, 6),
                  ),
                ],
                border: isSelected
                    ? Border.all(color: Colors.white, width: 2)
                    : null,
              ),
              child: const Center(
                child: StitchSvg(
                  svgString: StitchIcons.navScan,
                  size: 24,
                  color: Colors.white,
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }
}
