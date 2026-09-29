import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/theme/app_colors.dart';
import '../../core/theme/app_typography.dart';
import '../../core/widgets/scan_options_sheet.dart';
import '../../core/widgets/security_test_lab_sheet.dart';
import '../../data/services/kaspersky_sdk_bridge.dart';
import '../../data/services/security_score_service.dart';
import 'widgets/radar_scan_widget.dart';

class PemindaianScreen extends StatelessWidget {
  final VoidCallback? onBack;

  const PemindaianScreen({super.key, this.onBack});

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
          gradient: LinearGradient(
            begin: Alignment.topCenter,
            end: Alignment.bottomCenter,
            colors: [
              Color(0xFFE2001F),
              Color(0xFF9E0013),
            ],
          ),
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
              const SizedBox(height: 28),
              _buildActionPill(context, isScanning, kasperskySdk),
              const SizedBox(height: 36),
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
              if (onBack != null) {
                onBack!();
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
          Text(
            'Scan Penuh',
            style: AppTypography.headlineSm.copyWith(
              color: Colors.white,
              fontWeight: FontWeight.w700,
            ),
          ),
          IconButton(
            onPressed: () {
              ScanOptionsSheet.show(
                context,
                onSelect: (mode) {
                  if (mode.contains('EICAR')) {
                    SecurityTestLabSheet.show(context, sdk: kasperskySdk, initialTab: 1);
                  } else {
                    ScaffoldMessenger.of(context).showSnackBar(
                      SnackBar(
                        content: Text('Beralih ke mode: $mode'),
                        duration: const Duration(seconds: 1),
                      ),
                    );
                    kasperskySdk.runFullScan();
                  }
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

    if (!sdk.isInitialized) {
      return Column(
        children: [
          Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              Container(
                padding: const EdgeInsets.all(2),
                decoration: const BoxDecoration(
                  color: AppColors.statusDanger,
                  shape: BoxShape.circle,
                ),
                child: const Icon(Icons.warning_amber_rounded, color: Colors.white, size: 18),
              ),
              const SizedBox(width: 8),
              Text(
                'Proteksi Nonaktif (Dormant)',
                style: AppTypography.headlineSm.copyWith(
                  color: Colors.white,
                  fontWeight: FontWeight.w700,
                ),
              ),
            ],
          ),
          const SizedBox(height: 8),
          Text(
            'Masa aktif paket habis • Mesin Kaspersky dorman',
            style: AppTypography.bodySm.copyWith(
              color: Colors.white.withValues(alpha: 0.85),
            ),
          ),
        ],
      );
    }

    return Column(
      children: [
        Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Container(
              padding: const EdgeInsets.all(2),
              decoration: const BoxDecoration(
                color: AppColors.statusSafeEmerald,
                shape: BoxShape.circle,
              ),
              child: const Icon(Icons.check, color: Colors.white, size: 18),
            ),
            const SizedBox(width: 8),
            Text(
              'Semua Sistem Terlindung',
              style: AppTypography.headlineSm.copyWith(
                color: Colors.white,
                fontWeight: FontWeight.w700,
              ),
            ),
          ],
        ),
        const SizedBox(height: 8),
        Text(
          '${sdk.threatsDetected} ancaman terdeteksi • Terakhir dipindai baru saja',
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
          SizedBox(
            height: 50,
            width: double.infinity,
            child: ElevatedButton(
              style: ElevatedButton.styleFrom(
                backgroundColor: Colors.white,
                foregroundColor: AppColors.primary,
                elevation: 4,
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
                              'Mesin Kaspersky Tidak Aktif: Masa aktif paket telah berakhir. Silakan perpanjang paket di MyTelkomsel.',
                            ),
                            backgroundColor: AppColors.statusDanger,
                          ),
                        );
                        return;
                      }
                      sdk.runFullScan();
                    },
              child: Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Icon(
                    Icons.refresh,
                    size: 22,
                    color: isScanning ? AppColors.slateMuted : AppColors.primary,
                  ),
                  const SizedBox(width: 8),
                  Text(
                    isScanning ? 'Sedang Memindai...' : 'Pindai Ulang',
                    style: AppTypography.labelLg.copyWith(
                      color: isScanning ? AppColors.slateMuted : AppColors.primary,
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 12),
          SizedBox(
            height: 44,
            width: double.infinity,
            child: OutlinedButton.icon(
              style: OutlinedButton.styleFrom(
                foregroundColor: Colors.white,
                side: BorderSide(color: Colors.white.withValues(alpha: 0.4)),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(28),
                ),
              ),
              onPressed: () {
                SecurityTestLabSheet.show(context, sdk: sdk, initialTab: 1);
              },
              icon: const Icon(Icons.science_outlined, size: 18),
              label: const Text(
                'Laboratorium Uji Keamanan',
                style: TextStyle(fontWeight: FontWeight.w600, fontSize: 13),
              ),
            ),
          ),
        ],
      ),
    );
  }
}
