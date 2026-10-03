import 'package:flutter/material.dart';
import '../../../core/theme/stitch_icons.dart';
import '../../../core/widgets/settings_sheet.dart';

/// Top gradient header banner for Profile screen with back and settings buttons.
class ProfileHeaderBanner extends StatelessWidget {
  final VoidCallback? onBack;

  const ProfileHeaderBanner({super.key, this.onBack});

  @override
  Widget build(BuildContext context) {
    return Container(
      height: 120,
      decoration: const BoxDecoration(
        gradient: LinearGradient(
          begin: Alignment.topCenter,
          end: Alignment.bottomCenter,
          colors: [
            Color(0xFFCC0220),
            Color(0xFFED0226),
            Color(0xFFED0226),
          ],
        ),
      ),
      child: SafeArea(
        bottom: false,
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 20.0, vertical: 4.0),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Back Button Squircle
              GestureDetector(
                onTap: () {
                  if (onBack != null) {
                    onBack!();
                  } else if (Navigator.canPop(context)) {
                    Navigator.pop(context);
                  }
                },
                child: Container(
                  width: 36,
                  height: 36,
                  decoration: BoxDecoration(
                    color: Colors.white.withValues(alpha: 0.20),
                    borderRadius: BorderRadius.circular(12),
                    border: Border.all(color: Colors.white.withValues(alpha: 0.20)),
                  ),
                  child: const Center(
                    child: StitchSvg(
                      svgString: StitchIcons.chevronLeft,
                      size: 20,
                      color: Colors.white,
                    ),
                  ),
                ),
              ),
              // Right Settings Button Squircle
              GestureDetector(
                onTap: () => SettingsSheet.show(context),
                child: Container(
                  width: 36,
                  height: 36,
                  decoration: BoxDecoration(
                    color: Colors.white.withValues(alpha: 0.20),
                    borderRadius: BorderRadius.circular(12),
                    border: Border.all(color: Colors.white.withValues(alpha: 0.20)),
                  ),
                  child: const Center(
                    child: StitchSvg(
                      svgString: StitchIcons.settings,
                      size: 20,
                      color: Colors.white,
                    ),
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
