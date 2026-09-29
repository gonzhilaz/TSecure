import 'package:flutter/material.dart';
import '../../../core/theme/app_colors.dart';

/// Fixed Bottom Navigation Bar matching Stitch design specification:
/// - Exact 64px row height (h-[64px]) wrapped in SafeArea so it's never squished
/// - Top active indicator bar (h-[3px] bg-primary rounded-b-sm)
/// - Center elevated scan button (-translate-y-2.5, 48x48 rounded-2xl)
/// - Exact Stitch icons: grid_view, smartphone, document_scanner, history, person
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
          top: BorderSide(color: AppColors.slateBorder, width: 1),
        ),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.04),
            blurRadius: 20,
            offset: const Offset(0, -4),
          ),
        ],
      ),
      child: SafeArea(
        top: false,
        child: SizedBox(
          height: 64, // Exact Stitch design: h-[64px]
          child: Row(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              // 1. Beranda (Active indicator on top)
              _buildNavItem(
                index: 0,
                icon: Icons.grid_view_rounded,
                isSelected: currentIndex == 0,
              ),
              // 2. Perangkat
              _buildNavItem(
                index: 1,
                icon: Icons.phone_android_rounded,
                isSelected: currentIndex == 1,
              ),
              // 3. Center elevated Scan button (-translate-y-2.5)
              _buildCenterScanButton(),
              // 4. Riwayat Aktivitas
              _buildNavItem(
                index: 3,
                icon: Icons.history_rounded,
                isSelected: currentIndex == 3,
              ),
              // 5. Profil
              _buildNavItem(
                index: 4,
                icon: Icons.person_rounded,
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
    required IconData icon,
    required bool isSelected,
  }) {
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
              // Top active indicator (Stitch: h-[3px] bg-primary rounded-b-sm left-4 right-4)
              Positioned(
                top: 0,
                left: 12,
                right: 12,
                child: Container(
                  height: 3,
                  decoration: BoxDecoration(
                    color: isSelected ? AppColors.primary : Colors.transparent,
                    borderRadius: const BorderRadius.vertical(
                      bottom: Radius.circular(2),
                    ),
                  ),
                ),
              ),
              // Tab Icon (Stitch: text-[24px])
              Icon(
                icon,
                size: 24,
                color: isSelected ? AppColors.primary : AppColors.slateMuted,
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
          offset: const Offset(0, -10), // Stitch: -translate-y-2.5 (~10px elevated)
          child: GestureDetector(
            onTap: () => onTabSelected(2),
            child: Container(
              width: 48, // Stitch: w-12 (48px)
              height: 48, // Stitch: h-12 (48px)
              decoration: BoxDecoration(
                color: AppColors.primary,
                borderRadius: BorderRadius.circular(16), // Stitch: rounded-2xl
                boxShadow: [
                  BoxShadow(
                    color: AppColors.primary.withValues(alpha: 0.38),
                    blurRadius: 16,
                    offset: const Offset(0, 6),
                  ),
                ],
                border: isSelected
                    ? Border.all(color: Colors.white, width: 2)
                    : null,
              ),
              child: const Center(
                child: Icon(
                  Icons.document_scanner_rounded, // Stitch: document_scanner text-[26px]
                  size: 26,
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
