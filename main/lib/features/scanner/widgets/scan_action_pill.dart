import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../../core/services/permission_gate.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_typography.dart';
import '../../../core/widgets/scan_threat_detail_sheet.dart';
import '../../../data/models/activity_log.dart';
import '../../../data/models/threat_detail_item.dart';
import '../../../data/services/activity_log_repository.dart';
import '../../../data/services/kaspersky_sdk_bridge.dart';
import '../../../data/services/threat_manager_service.dart';

class ScanActionPill extends StatelessWidget {
  final bool isScanning;
  final bool isPaused;
  final bool hasScannedInSession;
  final String scanModeCode;
  final VoidCallback onScanStarted;

  const ScanActionPill({
    super.key,
    required this.isScanning,
    required this.isPaused,
    required this.hasScannedInSession,
    required this.scanModeCode,
    required this.onScanStarted,
  });

  @override
  Widget build(BuildContext context) {
    final sdk = context.watch<KasperskySdkBridge>();
    final activeScan = isScanning || isPaused;

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 24.0),
      child: Column(
        children: [
          if (!activeScan && sdk.threatsDetected > 0) ...[
            SizedBox(
              height: 48,
              width: double.infinity,
              child: ElevatedButton.icon(
                style: ElevatedButton.styleFrom(
                  backgroundColor: Colors.white,
                  foregroundColor: AppColors.statusDanger,
                  elevation: 4,
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(28)),
                ),
                icon: const Icon(Icons.shield_outlined, size: 20, color: AppColors.statusDanger),
                label: Text(
                  'Lihat & Tangani Ancaman (${sdk.threatsDetected})',
                  style: AppTypography.labelLg.copyWith(color: AppColors.statusDanger, fontWeight: FontWeight.w700),
                ),
                onPressed: () {
                  final logRepo = context.read<ActivityLogRepository>();
                  final now = DateTime.now();
                  final log = (logRepo.latestLog != null && !logRepo.latestLog!.isSafe && logRepo.latestLog!.threats.isNotEmpty)
                      ? logRepo.latestLog!
                      : ActivityLog(
                          id: 'scan-${now.millisecondsSinceEpoch}',
                          title: 'Pemindaian Selesai • ${sdk.threatsDetected} Ancaman',
                          description: '${sdk.scannedFiles} Berkas Diperiksa',
                          time: '${now.hour.toString().padLeft(2, '0')}:${now.minute.toString().padLeft(2, '0')} WIB',
                          date: now,
                          icon: Icons.bug_report,
                          category: LogCategory.pemindaian,
                          isSafe: false,
                          threats: List<ThreatDetailItem>.from(sdk.currentScanThreats),
                        );
                  ScanThreatDetailSheet.show(context, log: log, threatManager: context.read<ThreatManagerService>(), kasperskySdk: sdk);
                },
              ),
            ),
            const SizedBox(height: 12),
          ],
          if (activeScan)
            Row(
              children: [
                Expanded(
                  child: SizedBox(
                    height: 50,
                    child: ElevatedButton.icon(
                      style: ElevatedButton.styleFrom(
                        backgroundColor: Colors.white,
                        foregroundColor: AppColors.primary,
                        elevation: 3,
                        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(28)),
                      ),
                      onPressed: () => isPaused ? sdk.resumeScan() : sdk.pauseScan(),
                      icon: Icon(isPaused ? Icons.play_arrow_rounded : Icons.pause_rounded, size: 22, color: AppColors.primary),
                      label: Text(
                        isPaused ? 'Lanjutkan' : 'Jeda',
                        style: AppTypography.labelLg.copyWith(color: AppColors.primary, fontWeight: FontWeight.w700),
                      ),
                    ),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: SizedBox(
                    height: 50,
                    child: OutlinedButton.icon(
                      style: OutlinedButton.styleFrom(
                        side: const BorderSide(color: Colors.white, width: 1.8),
                        foregroundColor: Colors.white,
                        backgroundColor: Colors.black.withValues(alpha: 0.15),
                        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(28)),
                      ),
                      onPressed: () => sdk.stopScan(),
                      icon: const Icon(Icons.stop_rounded, size: 22, color: Colors.white),
                      label: Text(
                        'Berhenti',
                        style: AppTypography.labelLg.copyWith(color: Colors.white, fontWeight: FontWeight.w700),
                      ),
                    ),
                  ),
                ),
              ],
            )
          else
            SizedBox(
              height: 50,
              width: double.infinity,
              child: ElevatedButton(
                style: ElevatedButton.styleFrom(
                  backgroundColor: (sdk.threatsDetected > 0) ? Colors.white.withValues(alpha: 0.2) : Colors.white,
                  foregroundColor: (sdk.threatsDetected > 0) ? Colors.white : AppColors.primary,
                  elevation: (sdk.threatsDetected > 0) ? 0 : 4,
                  side: (sdk.threatsDetected > 0) ? const BorderSide(color: Colors.white, width: 1.5) : BorderSide.none,
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(28)),
                ),
                onPressed: () async {
                  if (!sdk.isInitialized) {
                    await sdk.initKasperskySdk(mobileId: sdk.boundMobileId ?? 'MOBILE ID-DIRECT', hasActivePeriod: true);
                  }
                  if (!sdk.isInitialized) {
                    if (context.mounted) {
                      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(
                        content: Text('Mesin Kaspersky Tidak Aktif: Masa aktif lisensi telah berakhir.'),
                        backgroundColor: AppColors.statusDanger,
                      ));
                    }
                    return;
                  }
                  if (!context.mounted) return;
                  final ok = await PermissionGate.ensure(context, GateFeature.scan);
                  if (!ok || !context.mounted) return;
                  onScanStarted();
                  sdk.runFullScan(scanMode: scanModeCode);
                },
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Icon(
                      hasScannedInSession ? Icons.refresh : Icons.shield_rounded,
                      size: 20,
                      color: (sdk.threatsDetected > 0) ? Colors.white : AppColors.primary,
                    ),
                    const SizedBox(width: 8),
                    Text(
                      hasScannedInSession ? 'Pindai Ulang' : 'Pindai',
                      style: AppTypography.labelLg.copyWith(
                        color: (sdk.threatsDetected > 0) ? Colors.white : AppColors.primary,
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
