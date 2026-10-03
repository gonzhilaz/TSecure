import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/theme/app_typography.dart';
import '../../core/widgets/scan_options_sheet.dart';
import '../../data/services/kaspersky_sdk_bridge.dart';
import '../../data/services/security_score_service.dart';
import 'widgets/radar_scan_widget.dart';
import 'widgets/scan_action_pill.dart';

class PemindaianScreen extends StatefulWidget {
  final VoidCallback? onBack;

  const PemindaianScreen({super.key, this.onBack});

  @override
  State<PemindaianScreen> createState() => _PemindaianScreenState();
}

class _PemindaianScreenState extends State<PemindaianScreen> {
  String _scanModeTitle = 'Pindai Penuh (Full Scan)';
  String _scanModeCode = 'FULL';
  bool _hasScannedInSession = false;

  @override
  Widget build(BuildContext context) {
    final kasperskySdk = context.watch<KasperskySdkBridge>();
    final isScanning = kasperskySdk.scanStatus == ScanStatus.inProgress;
    final isPaused = kasperskySdk.isScanPaused;
    final securityScore = SecurityScoreService.computeScore(
      kasperskySdk: kasperskySdk,
    );

    return Scaffold(
      body: Container(
        decoration: const BoxDecoration(
          gradient: LinearGradient(begin: Alignment.topCenter, end: Alignment.bottomCenter, colors: [Color(0xFFE2001F), Color(0xFF9E0013)]),
        ),
        child: SafeArea(
          child: Column(
            children: [
              _buildTopBar(context, kasperskySdk, isScanning || isPaused),
              const Spacer(),
              RadarScanWidget(
                isScanning: isScanning,
                scanProgress: kasperskySdk.scanProgress,
                securityScore: securityScore,
              ),
              const Spacer(),
              _buildStatusSection(isScanning, isPaused, kasperskySdk),
              const SizedBox(height: 24),
              ScanActionPill(
                isScanning: isScanning,
                isPaused: isPaused,
                hasScannedInSession: _hasScannedInSession,
                scanModeCode: _scanModeCode,
                onScanStarted: () => setState(() => _hasScannedInSession = true),
              ),
              const SizedBox(height: 32),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildTopBar(BuildContext context, KasperskySdkBridge kasperskySdk, bool activeScan) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 8.0),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          IconButton(
            onPressed: () {
              if (widget.onBack != null) {
                widget.onBack!();
              } else if (Navigator.canPop(context)) {
                Navigator.pop(context);
              }
            },
            icon: const Icon(Icons.arrow_back, color: Colors.white),
            style: IconButton.styleFrom(
              backgroundColor: Colors.white.withValues(alpha: 0.15),
              padding: const EdgeInsets.all(10),
            ),
          ),
          Expanded(
            child: Text(
              _scanModeTitle,
              textAlign: TextAlign.center,
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
              style: AppTypography.headlineSm.copyWith(
                color: Colors.white,
                fontWeight: FontWeight.w700,
                fontSize: 16,
              ),
            ),
          ),
          IconButton(
            onPressed: activeScan ? null : () {
              ScanOptionsSheet.show(
                context,
                onSelect: (title, code) {
                  setState(() {
                    _scanModeTitle = title;
                    _scanModeCode = code;
                  });
                  ScaffoldMessenger.of(context).hideCurrentSnackBar();
                  ScaffoldMessenger.of(context).showSnackBar(SnackBar(
                    content: Text('Mode $title dipilih. Tekan tombol Pindai untuk mulai.'),
                    duration: const Duration(seconds: 2),
                    behavior: SnackBarBehavior.floating,
                  ));
                },
              );
            },
            icon: const Icon(Icons.tune, color: Colors.white),
            style: IconButton.styleFrom(
              backgroundColor: Colors.white.withValues(alpha: 0.15),
              padding: const EdgeInsets.all(10),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildStatusSection(bool isScanning, bool isPaused, KasperskySdkBridge sdk) {
    if (isPaused) {
      return Column(
        children: [
          Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              const Icon(Icons.pause_circle_filled_rounded, color: Colors.amberAccent, size: 24),
              const SizedBox(width: 8),
              Text(
                'Pemindaian Dijeda',
                style: AppTypography.headlineSm.copyWith(color: Colors.white, fontWeight: FontWeight.w700),
              ),
            ],
          ),
          const SizedBox(height: 6),
          Text(
            '${sdk.scannedFiles} file diperiksa • Ketuk Lanjutkan untuk meneruskan',
            style: AppTypography.bodySm.copyWith(color: Colors.white.withValues(alpha: 0.9)),
          ),
        ],
      );
    }

    if (isScanning) {
      return Column(
        children: [
          Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              const SizedBox(
                width: 20,
                height: 20,
                child: CircularProgressIndicator(
                  strokeWidth: 2.5,
                  valueColor: AlwaysStoppedAnimation<Color>(Colors.white),
                ),
              ),
              const SizedBox(width: 10),
              Text(
                'Memeriksa Berkas Sistem...',
                style: AppTypography.headlineSm.copyWith(
                  color: Colors.white,
                  fontWeight: FontWeight.w700,
                ),
              ),
            ],
          ),
          const SizedBox(height: 8),
          Text(
            '${sdk.scannedFiles} file diperiksa • Mesin Kaspersky Aktif',
            style: AppTypography.bodySm.copyWith(
              color: Colors.white.withValues(alpha: 0.85),
            ),
          ),
          if (sdk.currentScanningFile.isNotEmpty)
            Padding(
              padding: const EdgeInsets.only(top: 4, left: 20, right: 20),
              child: Text(
                sdk.currentScanningFile,
                style: AppTypography.labelSm.copyWith(
                  color: Colors.white.withValues(alpha: 0.75),
                  fontSize: 11,
                ),
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
                textAlign: TextAlign.center,
              ),
            ),
        ],
      );
    }

    if (sdk.scanStatus == ScanStatus.error) {
      return Column(
        children: [
          Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              const Icon(Icons.warning_amber_rounded, color: Colors.white, size: 24),
              const SizedBox(width: 8),
              Text(
                'Pemindaian Terkendala',
                style: AppTypography.headlineSm.copyWith(
                  color: Colors.white,
                  fontWeight: FontWeight.w700,
                ),
              ),
            ],
          ),
          const SizedBox(height: 8),
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 24),
            child: Text(
              sdk.scanErrorMessage ?? 'Basis data offline belum siap. Memeriksa cloud KSN...',
              textAlign: TextAlign.center,
              style: AppTypography.bodySm.copyWith(
                color: Colors.white.withValues(alpha: 0.85),
              ),
            ),
          ),
        ],
      );
    }

    if (sdk.threatsDetected > 0) {
      return Column(
        children: [
          Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              const Icon(Icons.warning_rounded, color: Colors.amberAccent, size: 28),
              const SizedBox(width: 8),
              Text(
                '${sdk.threatsDetected} Ancaman Terdeteksi!',
                style: AppTypography.headlineSm.copyWith(
                  color: Colors.white,
                  fontWeight: FontWeight.w700,
                ),
              ),
            ],
          ),
          const SizedBox(height: 8),
          Text(
            'Tindakan pembersihan segera direkomendasikan.',
            style: AppTypography.bodySm.copyWith(
              color: Colors.white.withValues(alpha: 0.9),
            ),
          ),
        ],
      );
    }

    if (!_hasScannedInSession && !isScanning) {
      return Column(
        children: [
          Text(
            'Perangkat Siap Dipindai',
            style: AppTypography.headlineSm.copyWith(
              color: Colors.white,
              fontWeight: FontWeight.w700,
            ),
          ),
          const SizedBox(height: 8),
          Text(
            'Mulai sesi pemindaian untuk proteksi dari virus & trojan',
            style: AppTypography.bodySm.copyWith(
              color: Colors.white.withValues(alpha: 0.85),
            ),
          ),
        ],
      );
    }

    return Column(
      children: [
        Text(
          'Sesi Pemindaian Selesai',
          style: AppTypography.headlineSm.copyWith(
            color: Colors.white,
            fontWeight: FontWeight.w700,
          ),
        ),
        const SizedBox(height: 8),
        Text(
          'Perangkat Terlindungi • ${sdk.scannedFiles} Berkas Diperiksa',
          style: AppTypography.bodySm.copyWith(
            color: Colors.white.withValues(alpha: 0.85),
          ),
        ),
      ],
    );
  }
}
