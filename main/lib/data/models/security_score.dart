import 'package:flutter/material.dart';
import '../../core/theme/app_colors.dart';

class SecurityScore {
  final int score;
  final String statusText;
  final String recommendationText;
  final Color themeColor;
  final Color backgroundColor;

  const SecurityScore({
    required this.score,
    required this.statusText,
    required this.recommendationText,
    required this.themeColor,
    required this.backgroundColor,
  });

  factory SecurityScore.calculate(int scoreValue) {
    final clamped = scoreValue.clamp(0, 100);

    if (clamped >= 90) {
      return SecurityScore(
        score: clamped,
        statusText: 'Sangat Aman',
        recommendationText: 'Terlindungi Maksimal',
        themeColor: AppColors.statusSafeEmerald,
        backgroundColor: AppColors.statusSafeBg,
      );
    } else if (clamped >= 70) {
      return SecurityScore(
        score: clamped,
        statusText: 'Perlu Perhatian',
        recommendationText: 'Optimalisasi Tersedia',
        themeColor: AppColors.statusWarningAmber,
        backgroundColor: AppColors.statusWarningBg,
      );
    } else {
      return SecurityScore(
        score: clamped,
        statusText: 'Rentan Terhadap Ancaman',
        recommendationText: 'Tindakan Diperlukan Segera',
        themeColor: AppColors.statusDanger,
        backgroundColor: AppColors.statusDangerBg,
      );
    }
  }
}
