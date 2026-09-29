import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/theme/app_colors.dart';
import '../../core/theme/app_typography.dart';
import '../../core/widgets/notification_center_sheet.dart';
import '../../core/widgets/tri_state_view.dart';
import 'device_controller.dart';
import 'widgets/audit_list_tile.dart';
import 'widgets/sim_watch_card.dart';
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
            icon: const Icon(Icons.notifications_none_rounded),
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
              const SizedBox(height: 20),
              const SimWatchCard(),
              const SizedBox(height: 24),
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
                (item) => AuditListTile(
                  item: item,
                  onActionPressed: () {
                    if (item.actionLabel == 'Pindai') {
                      widget.onNavigateToScanner();
                    } else {
                      controller.resolveAuditItem(item.id);
                      ScaffoldMessenger.of(context).showSnackBar(
                        SnackBar(
                          content: Text('${item.title} selesai diverifikasi.'),
                          duration: const Duration(seconds: 1),
                        ),
                      );
                    }
                  },
                ),
              ),
              const SizedBox(height: 24),
            ],
          ),
        ),
      ),
    );
  }
}
