import 'dart:math';
import 'package:flutter/material.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_typography.dart';
import '../../../data/models/security_score.dart';

class SecurityGaugeCard extends StatelessWidget {
  final SecurityScore securityScore;

  const SecurityGaugeCard({
    super.key,
    required this.securityScore,
  });

  @override
  Widget build(BuildContext context) {
    return Center(
      child: SizedBox(
        width: 220,
        height: 200,
        child: Stack(
          alignment: Alignment.center,
          children: [
            CustomPaint(
              size: const Size(200, 200),
              painter: _GaugePainter(
                percentage: securityScore.score / 100.0,
                progressColor: securityScore.themeColor,
              ),
            ),
            Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                Row(
                  mainAxisSize: MainAxisSize.min,
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      '${securityScore.score}',
                      style: AppTypography.headlineXl.copyWith(
                        fontSize: 44,
                        fontWeight: FontWeight.w800,
                        color: AppColors.navyDeep,
                        height: 1.0,
                      ),
                    ),
                    Text(
                      '%',
                      style: AppTypography.headlineSm.copyWith(
                        color: AppColors.primary,
                        fontWeight: FontWeight.w700,
                        height: 1.4,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 4),
                Text(
                  'REPUTASI\nPERANGKAT',
                  textAlign: TextAlign.center,
                  style: AppTypography.labelSm.copyWith(
                    letterSpacing: 1.2,
                    color: AppColors.slateMid,
                    fontWeight: FontWeight.w700,
                    height: 1.2,
                  ),
                ),
                const SizedBox(height: 12),
                Container(
                  padding: const EdgeInsets.symmetric(
                    horizontal: 10,
                    vertical: 4,
                  ),
                  decoration: BoxDecoration(
                    color: securityScore.backgroundColor,
                    borderRadius: BorderRadius.circular(16),
                    border: Border.all(
                      color: securityScore.themeColor.withValues(alpha: 0.4),
                      width: 1,
                    ),
                  ),
                  child: Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Container(
                        width: 7,
                        height: 7,
                        decoration: BoxDecoration(
                          color: securityScore.themeColor,
                          shape: BoxShape.circle,
                        ),
                      ),
                      const SizedBox(width: 6),
                      Text(
                        securityScore.recommendationText,
                        style: AppTypography.labelSm.copyWith(
                          color: securityScore.themeColor,
                          fontWeight: FontWeight.w700,
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

class _GaugePainter extends CustomPainter {
  final double percentage;
  final Color progressColor;

  _GaugePainter({
    required this.percentage,
    required this.progressColor,
  });

  @override
  void paint(Canvas canvas, Size size) {
    final center = Offset(size.width / 2, size.height / 2 - 10);
    final radius = size.width / 2 - 12;

    const startAngle = 135 * (pi / 180);
    const sweepAngle = 270 * (pi / 180);

    // Background Arc
    final backgroundPaint = Paint()
      ..color = AppColors.slateDivider
      ..strokeWidth = 14
      ..style = PaintingStyle.stroke
      ..strokeCap = StrokeCap.round;

    canvas.drawArc(
      Rect.fromCircle(center: center, radius: radius),
      startAngle,
      sweepAngle,
      false,
      backgroundPaint,
    );

    // Dynamic Progress Arc
    final activeSweep = sweepAngle * percentage.clamp(0.0, 1.0);
    final progressPaint = Paint()
      ..shader = LinearGradient(
        colors: [
          progressColor,
          progressColor.withValues(alpha: 0.85),
          AppColors.primary,
        ],
      ).createShader(Rect.fromCircle(center: center, radius: radius))
      ..strokeWidth = 14
      ..style = PaintingStyle.stroke
      ..strokeCap = StrokeCap.round;

    canvas.drawArc(
      Rect.fromCircle(center: center, radius: radius),
      startAngle,
      activeSweep,
      false,
      progressPaint,
    );
  }

  @override
  bool shouldRepaint(covariant _GaugePainter oldDelegate) {
    return oldDelegate.percentage != percentage ||
        oldDelegate.progressColor != progressColor;
  }
}
