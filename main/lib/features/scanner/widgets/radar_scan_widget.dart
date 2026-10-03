import 'dart:math';
import 'package:flutter/material.dart';
import '../../../core/theme/app_typography.dart';
import '../../../data/models/security_score.dart';

class RadarScanWidget extends StatefulWidget {
  final bool isScanning;
  final double scanProgress;
  final SecurityScore securityScore;

  const RadarScanWidget({
    super.key,
    required this.isScanning,
    required this.scanProgress,
    required this.securityScore,
  });

  @override
  State<RadarScanWidget> createState() => _RadarScanWidgetState();
}

class _RadarScanWidgetState extends State<RadarScanWidget>
    with SingleTickerProviderStateMixin {
  late final AnimationController _rotationController;

  @override
  void initState() {
    super.initState();
    _rotationController = AnimationController(
      vsync: this,
      duration: const Duration(seconds: 3),
    );

    if (widget.isScanning) {
      _rotationController.repeat();
    }
  }

  @override
  void didUpdateWidget(covariant RadarScanWidget oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (widget.isScanning && !_rotationController.isAnimating) {
      _rotationController.repeat();
    } else if (!widget.isScanning && _rotationController.isAnimating) {
      _rotationController.stop();
    }
  }

  @override
  void dispose() {
    _rotationController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final displayScore = widget.isScanning
        ? (widget.scanProgress * 100).round()
        : widget.securityScore.score;

    return Center(
      child: SizedBox(
        width: 290,
        height: 290,
        child: Stack(
          alignment: Alignment.center,
          children: [
            // Radar concentric rings and reticles
            CustomPaint(
              size: const Size(280, 280),
              painter: _RadarReticlePainter(),
            ),
            // Rotating radar beam when scanning
            if (widget.isScanning)
              AnimatedBuilder(
                animation: _rotationController,
                builder: (context, child) {
                  return Transform.rotate(
                    angle: _rotationController.value * 2 * pi,
                    child: CustomPaint(
                      size: const Size(280, 280),
                      painter: _RadarBeamPainter(),
                    ),
                  );
                },
              ),
            // Center circular glowing disc
            Container(
              width: 150,
              height: 150,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                gradient: const RadialGradient(
                  colors: [
                    Color(0xFFE52E42),
                    Color(0xFFBA0A1C),
                  ],
                ),
                boxShadow: [
                  BoxShadow(
                    color: Colors.black.withValues(alpha: 0.25),
                    blurRadius: 18,
                    offset: const Offset(0, 6),
                  ),
                ],
              ),
              child: Center(
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Text(
                      '$displayScore%',
                      style: AppTypography.headlineXl.copyWith(
                        fontSize: 38,
                        fontWeight: FontWeight.w800,
                        color: Colors.white,
                        height: 1.0,
                      ),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      widget.isScanning
                          ? 'MEMINDAI...'
                          : widget.securityScore.statusText.toUpperCase(),
                      textAlign: TextAlign.center,
                      maxLines: 1,
                      style: AppTypography.labelSm.copyWith(
                        color: Colors.white.withValues(alpha: 0.95),
                        letterSpacing: 1.5,
                        fontWeight: FontWeight.w700,
                        fontSize: 11,
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _RadarReticlePainter extends CustomPainter {
  @override
  void paint(Canvas canvas, Size size) {
    final center = Offset(size.width / 2, size.height / 2);
    final paintRing = Paint()
      ..color = Colors.white.withValues(alpha: 0.22)
      ..style = PaintingStyle.stroke
      ..strokeWidth = 1.0;

    // Rings
    canvas.drawCircle(center, size.width * 0.46, paintRing);
    canvas.drawCircle(center, size.width * 0.35, paintRing);

    // Crosshair ticks
    final tickPaint = Paint()
      ..color = Colors.white.withValues(alpha: 0.4)
      ..strokeWidth = 1.5;

    final outerR = size.width * 0.46;
    canvas.drawLine(Offset(center.dx, center.dy - outerR - 6),
        Offset(center.dx, center.dy - outerR + 6), tickPaint);
    canvas.drawLine(Offset(center.dx, center.dy + outerR - 6),
        Offset(center.dx, center.dy + outerR + 6), tickPaint);
    canvas.drawLine(Offset(center.dx - outerR - 6, center.dy),
        Offset(center.dx - outerR + 6, center.dy), tickPaint);
    canvas.drawLine(Offset(center.dx + outerR - 6, center.dy),
        Offset(center.dx + outerR + 6, center.dy), tickPaint);
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => false;
}

class _RadarBeamPainter extends CustomPainter {
  @override
  void paint(Canvas canvas, Size size) {
    final center = Offset(size.width / 2, size.height / 2);
    final radius = size.width * 0.46;

    final sweepGradient = SweepGradient(
      center: Alignment.center,
      colors: [
        Colors.white.withValues(alpha: 0.0),
        Colors.white.withValues(alpha: 0.35),
      ],
      stops: const [0.8, 1.0],
    );

    final beamPaint = Paint()
      ..shader = sweepGradient.createShader(
        Rect.fromCircle(center: center, radius: radius),
      );

    canvas.drawCircle(center, radius, beamPaint);
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => true;
}
