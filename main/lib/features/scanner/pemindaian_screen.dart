import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/theme/app_colors.dart';
import '../../core/theme/app_typography.dart';
import '../../core/widgets/scan_options_sheet.dart';
import '../../core/widgets/scan_threat_detail_sheet.dart';
import '../../data/models/activity_log.dart';
import '../../data/models/threat_detail_item.dart';
import '../../data/services/activity_log_repository.dart';
import '../../data/services/kaspersky_sdk_bridge.dart';
import '../../data/services/security_score_service.dart';
import '../../data/services/threat_manager_service.dart';
import 'widgets/radar_scan_widget.dart';

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
              _buildTopBar(context, kasperskySdk),
              const Spacer(),
              RadarScanWidget(
                isScanning: isScanning,
                scanProgress: kasperskySdk.scanProgress,
                securityScore: securityScore,
              ),
              const Spacer(),
              _buildStatusSection(isScanning, kasperskySdk),
              const SizedBox(height: 24),
              _buildActionPill(context, isScanning, kasperskySdk),
              const SizedBox(height: 32),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildTopBar(BuildContext context, KasperskySdkBridge kasperskySdk) {
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
            onPressed: () {
              ScanOptionsSheet.show(
                context,
                onSelect: (title, code) {
                  setState(() {
                    _scanModeTitle = title;
                    _scanModeCode = code;
                    _hasScannedInSession = true;
                  });
                  if (code == 'FULL' || code == 'FOLDER') {
                    if (!kasperskySdk.hasFullStorageAccess) {
                      kasperskySdk.requestFullStoragePermission();
                    }
                  }
                  kasperskySdk.runFullScan(scanMode: code);
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

  Widget _buildStatusSection(bool isScanning, KasperskySdkBridge sdk) {
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

  Widget _buildActionPill(
      BuildContext context, bool isScanning, KasperskySdkBridge sdk) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 32.0),
      child: Column(
        children: [
          if (!isScanning && sdk.threatsDetected > 0) ...[
            SizedBox(
              height: 48,
              width: double.infinity,
              child: ElevatedButton.icon(
                style: ElevatedButton.styleFrom(
                  backgroundColor: Colors.white,
                  foregroundColor: AppColors.statusDanger,
                  elevation: 4,
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(28),
                  ),
                ),
                icon: const Icon(Icons.shield_outlined, size: 20, color: AppColors.statusDanger),
                label: Text(
                  'Lihat & Tangani Ancaman (${sdk.threatsDetected})',
                  style: AppTypography.labelLg.copyWith(
                    color: AppColors.statusDanger,
                    fontWeight: FontWeight.w700,
                  ),
                ),
                onPressed: () {
                  final logRepo = context.read<ActivityLogRepository>();
                  final now = DateTime.now();
                  final log = (logRepo.latestLog != null && !logRepo.latestLog!.isSafe && logRepo.latestLog!.threats.isNotEmpty)
                      ? logRepo.latestLog!
                      : ActivityLog(
                          id: 'scan-${now.millisecondsSinceEpoch}',
                          title: '🚨 Pemindaian Selesai • ${sdk.threatsDetected} Ancaman',
                          description: '${sdk.scannedFiles} Berkas Diperiksa',
                          time: '${now.hour.toString().padLeft(2, '0')}:${now.minute.toString().padLeft(2, '0')} WIB',
                          date: now, icon: Icons.bug_report, category: LogCategory.pemindaian, isSafe: false,
                          threats: List<ThreatDetailItem>.from(sdk.currentScanThreats),
                        );
                  ScanThreatDetailSheet.show(context, log: log, threatManager: context.read<ThreatManagerService>(), kasperskySdk: sdk);
                },
              ),
            ),
            const SizedBox(height: 12),
          ],
          SizedBox(
            height: 50,
            width: double.infinity,
            child: ElevatedButton(
              style: ElevatedButton.styleFrom(
                backgroundColor: (sdk.threatsDetected > 0 && !isScanning)
                    ? Colors.white.withValues(alpha: 0.2)
                    : Colors.white,
                foregroundColor: (sdk.threatsDetected > 0 && !isScanning)
                    ? Colors.white
                    : AppColors.primary,
                elevation: (sdk.threatsDetected > 0 && !isScanning) ? 0 : 4,
                side: (sdk.threatsDetected > 0 && !isScanning)
                    ? const BorderSide(color: Colors.white, width: 1.5)
                    : BorderSide.none,
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(28),
                ),
              ),
              onPressed: isScanning
                  ? null
                  : () {
                      if (!sdk.isInitialized) {
                        ScaffoldMessenger.of(context).showSnackBar(
                          const SnackBar(
                            content: Text(
                              'Mesin Kaspersky Tidak Aktif: Masa aktif lisensi telah berakhir.',
                            ),
                            backgroundColor: AppColors.statusDanger,
                          ),
                        );
                        return;
                      }
                      setState(() => _hasScannedInSession = true);
                      sdk.runFullScan(scanMode: _scanModeCode);
                    },
              child: Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Icon(
                    isScanning
                        ? Icons.hourglass_top_rounded
                        : (_hasScannedInSession ? Icons.refresh : Icons.shield_rounded),
                    size: 22,
                    color: isScanning
                        ? AppColors.slateMuted
                        : ((sdk.threatsDetected > 0) ? Colors.white : AppColors.primary),
                  ),
                  const SizedBox(width: 8),
                  Text(
                    isScanning
                        ? 'Sedang Memindai...'
                        : (_hasScannedInSession
                            ? 'Pindai Ulang ($_scanModeTitle)'
                            : 'Mulai Pemindaian'),
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: AppTypography.labelLg.copyWith(
                      color: isScanning
                          ? AppColors.slateMuted
                          : ((sdk.threatsDetected > 0) ? Colors.white : AppColors.primary),
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}
