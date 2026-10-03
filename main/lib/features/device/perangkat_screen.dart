import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/theme/app_colors.dart';
import '../../core/theme/app_typography.dart';
import '../../core/widgets/notification_center_sheet.dart';
import '../../core/widgets/security_feature_sheet.dart';
import '../../core/widgets/permission_center_sheet.dart';
import '../../core/widgets/quarantine_vault_sheet.dart';
import '../../core/widgets/tri_state_view.dart';
import '../../core/widgets/web_filter_test_dialog.dart';
import '../../core/widgets/dns_cert_check_dialog.dart';
import '../../data/services/kaspersky_sdk_bridge.dart';
import 'device_controller.dart';
import 'widgets/audit_list_tile.dart';
import 'widgets/telemetry_chart_card.dart';

class PerangkatScreen extends StatefulWidget {
  final VoidCallback onNavigateToScanner;
  final VoidCallback onNavigateToProfile;

  const PerangkatScreen({
    super.key,
    required this.onNavigateToScanner,
    required this.onNavigateToProfile,
  });

  @override
  State<PerangkatScreen> createState() => _PerangkatScreenState();
}

class _PerangkatScreenState extends State<PerangkatScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<DeviceController>().loadDeviceAuditData();
    });
  }

  @override
  Widget build(BuildContext context) {
    final controller = context.watch<DeviceController>();
    debugPrint('=== [TELKOM_PERANGKAT] build() called! state=${controller.state}, items=${controller.auditItems.length} ===');

    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: AppBar(
        title: Text('Perangkat', style: AppTypography.headlineSm),
        leading: Padding(
          padding: const EdgeInsets.only(left: 16.0),
          child: IconButton(
            icon: Stack(
              clipBehavior: Clip.none,
              children: [
                const Icon(Icons.notifications_none_rounded, size: 22),
                Positioned(
                  top: 1,
                  right: 1,
                  child: Container(
                    width: 8,
                    height: 8,
                    decoration: BoxDecoration(
                      color: AppColors.primary,
                      shape: BoxShape.circle,
                      border: Border.all(color: Colors.white, width: 1.5),
                    ),
                  ),
                ),
              ],
            ),
            onPressed: () => NotificationCenterSheet.show(context),
            style: IconButton.styleFrom(
              backgroundColor: AppColors.slateDivider,
              padding: const EdgeInsets.all(8),
            ),
          ),
        ),
        actions: [
          Padding(
            padding: const EdgeInsets.only(right: 16.0),
            child: IconButton(
              icon: const Icon(Icons.person_outline_rounded),
              onPressed: widget.onNavigateToProfile,
              style: IconButton.styleFrom(
                backgroundColor: AppColors.slateDivider,
                padding: const EdgeInsets.all(8),
              ),
            ),
          ),
        ],
      ),
      body: TriStateView(
        state: controller.state,
        onRetry: () => controller.loadDeviceAuditData(showLoading: true),
        child: RefreshIndicator(
          color: AppColors.primary,
          onRefresh: () => controller.loadDeviceAuditData(showLoading: true),
          child: ListView(
            physics: const AlwaysScrollableScrollPhysics(),
            padding: const EdgeInsets.symmetric(horizontal: 20.0, vertical: 16.0),
            children: [
              TelemetryChartCard(
                onScanPressed: widget.onNavigateToScanner,
              ),
              const SizedBox(height: 14),
              Row(
                children: [
                  Expanded(
                    child: InkWell(
                      onTap: () => PermissionCenterSheet.show(context),
                      borderRadius: BorderRadius.circular(16),
                      child: Container(
                        padding: const EdgeInsets.all(14),
                        decoration: BoxDecoration(
                          color: AppColors.surfaceCard,
                          borderRadius: BorderRadius.circular(16),
                          border: Border.all(color: AppColors.slateBorder),
                        ),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Container(
                              width: 34,
                              height: 34,
                              decoration: BoxDecoration(
                                color: AppColors.primary.withValues(alpha: 0.1),
                                borderRadius: BorderRadius.circular(8),
                              ),
                              child: const Icon(Icons.verified_user_outlined, color: AppColors.primary, size: 18),
                            ),
                            const SizedBox(height: 10),
                            Text('Pusat Izin', style: AppTypography.labelMd.copyWith(fontWeight: FontWeight.w700)),
                            const SizedBox(height: 2),
                            Text('5 proteksi sistem', style: AppTypography.bodySm.copyWith(color: AppColors.slateMuted, fontSize: 11)),
                          ],
                        ),
                      ),
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: InkWell(
                      onTap: () => QuarantineVaultSheet.show(context),
                      borderRadius: BorderRadius.circular(16),
                      child: Container(
                        padding: const EdgeInsets.all(14),
                        decoration: BoxDecoration(
                          color: AppColors.surfaceCard,
                          borderRadius: BorderRadius.circular(16),
                          border: Border.all(color: AppColors.slateBorder),
                        ),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Container(
                              width: 34,
                              height: 34,
                              decoration: BoxDecoration(
                                color: AppColors.statusDanger.withValues(alpha: 0.1),
                                borderRadius: BorderRadius.circular(8),
                              ),
                              child: const Icon(Icons.archive_outlined, color: AppColors.statusDanger, size: 18),
                            ),
                            const SizedBox(height: 10),
                            Text('Brankas Karantina', style: AppTypography.labelMd.copyWith(fontWeight: FontWeight.w700)),
                            const SizedBox(height: 2),
                            Text('Isolasi berkas virus', style: AppTypography.bodySm.copyWith(color: AppColors.slateMuted, fontSize: 11)),
                          ],
                        ),
                      ),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 20),
              Text(
                'REKOMENDASI & STATUS',
                style: AppTypography.labelSm.copyWith(
                  letterSpacing: 1.2,
                  color: AppColors.slateMid,
                  fontWeight: FontWeight.w700,
                ),
              ),
              const SizedBox(height: 12),
              ...controller.auditItems.map(
                (item) {
                  final sdk = context.read<KasperskySdkBridge>();
                  final featureTitle = _getFeatureTitle(item.id);
                  return AuditListTile(
                    item: item,
                    onTap: () {
                      SecurityFeatureSheet.show(
                        context,
                        featureTitle: featureTitle,
                        sdk: sdk,
                        onNavigateToScanner: widget.onNavigateToScanner,
                        onNavigateToProfile: widget.onNavigateToProfile,
                      );
                    },
                    onActionPressed: () async {
                      if (item.actionLabel == 'Pindai') {
                        widget.onNavigateToScanner();
                      } else if (item.actionLabel == 'Uji URL') {
                        WebFilterTestDialog.show(context, sdk: sdk);
                      } else if (item.actionLabel == 'Audit SSL' || item.id == 'dns_cert_check') {
                        DnsCertCheckDialog.show(context);
                      } else if (item.actionLabel == 'Audit') {
                        final res = await sdk.auditWifi();
                        final ssid = res['ssid'] ?? 'Wi-Fi';
                        final hasLocation = res['hasLocationPermission'] != false;
                        final isGpsEnabled = res['isGpsEnabled'] != false;
                        if (!context.mounted) return;
                        if (!hasLocation) {
                          ScaffoldMessenger.of(context).showSnackBar(
                            SnackBar(
                              content: const Text('Aktifkan izin Lokasi agar nama Wi-Fi terdeteksi presisi.'),
                              backgroundColor: AppColors.statusWarning,
                              action: SnackBarAction(
                                label: 'Buka Izin',
                                textColor: Colors.white,
                                onPressed: () => PermissionCenterSheet.show(context),
                              ),
                            ),
                          );
                        } else if (!isGpsEnabled) {
                          ScaffoldMessenger.of(context).showSnackBar(
                            const SnackBar(
                              content: Text('Nyalakan GPS / Lokasi perangkat agar nama SSID Wi-Fi dapat terbaca oleh sistem Android.'),
                              backgroundColor: AppColors.statusWarning,
                            ),
                          );
                        } else {
                          ScaffoldMessenger.of(context).showSnackBar(
                            SnackBar(
                              content: Text('Audit Wi-Fi Selesai: $ssid (${res['securityProtocol'] ?? 'Aman'})'),
                              backgroundColor: AppColors.statusSafe,
                            ),
                          );
                        }
                      } else if (item.actionLabel == 'Cek Root') {
                        final res = await sdk.checkRoot();
                        final rooted = res['isRooted'] == true;
                        final cause = res['rootCause'] ?? 'Bersih';
                        if (!context.mounted) return;
                        ScaffoldMessenger.of(context).showSnackBar(
                          SnackBar(
                            content: Text(rooted ? '🚨 Terdeteksi Root: $cause' : '✅ Perangkat Bersih Tanpa Root'),
                            backgroundColor: rooted ? AppColors.statusDanger : AppColors.statusSafe,
                          ),
                        );
                      } else {
                        controller.resolveAuditItem(item.id);
                      }
                    },
                  );
                },
              ),
              const SizedBox(height: 24),
            ],
          ),
        ),
      ),
    );
  }

  String _getFeatureTitle(String id) {
    switch (id) {
      case 'realtime_malware':
        return 'Realtime Scanner';
      case 'web_filter':
        return 'Web Filter';
      case 'wifi_safety':
        return 'Wifi Safety';
      case 'dns_cert_check':
        return 'DNS & Certificate Guard';
      case 'device_root':
      default:
        return 'Device Reputation';
    }
  }
}
