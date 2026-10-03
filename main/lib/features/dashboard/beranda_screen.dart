import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/services/permission_gate.dart';
import '../../core/theme/app_colors.dart';
import '../../core/theme/app_typography.dart';
import '../../core/widgets/advanced_security_hub_sheet.dart';
import '../../core/widgets/notification_center_sheet.dart';
import '../../core/widgets/security_feature_sheet.dart';
import '../../core/widgets/tri_state_view.dart';
import '../../data/services/kaspersky_sdk_bridge.dart';
import 'dashboard_controller.dart';
import 'widgets/protection_status_card.dart';
import 'widgets/recent_activity_preview.dart';
import 'widgets/security_features_grid.dart';
import 'widgets/security_gauge_card.dart';

class BerandaScreen extends StatefulWidget {
  final VoidCallback onNavigateToScanner;
  final VoidCallback onNavigateToHistory;
  final VoidCallback onNavigateToProfile;

  const BerandaScreen({
    super.key,
    required this.onNavigateToScanner,
    required this.onNavigateToHistory,
    required this.onNavigateToProfile,
  });

  @override
  State<BerandaScreen> createState() => _BerandaScreenState();
}

class _BerandaScreenState extends State<BerandaScreen> with WidgetsBindingObserver {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<DashboardController>().loadDashboardData();
      context.read<KasperskySdkBridge>().checkFullStoragePermission();
    });
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.resumed) {
      context.read<KasperskySdkBridge>().checkFullStoragePermission();
      context.read<DashboardController>().loadDashboardData();
    }
  }

  String _formatLastScan(DateTime date) {
    final now = DateTime.now();
    final diff = now.difference(date);
    if (diff.inMinutes < 2) {
      return 'Baru saja';
    } else if (diff.inMinutes < 60) {
      return '${diff.inMinutes} menit yang lalu';
    } else if (diff.inHours < 24 && date.day == now.day) {
      final hour = date.hour.toString().padLeft(2, '0');
      final minute = date.minute.toString().padLeft(2, '0');
      return 'Hari ini, $hour:$minute WIB';
    } else {
      final hour = date.hour.toString().padLeft(2, '0');
      final minute = date.minute.toString().padLeft(2, '0');
      return '${date.day}/${date.month}/${date.year}, $hour:$minute WIB';
    }
  }

  @override
  Widget build(BuildContext context) {
    final controller = context.watch<DashboardController>();

    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: AppBar(
        title: Text('Dashboard', style: AppTypography.headlineSm),
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
        onRetry: controller.loadDashboardData,
        child: RefreshIndicator(
          color: AppColors.primary,
          onRefresh: controller.loadDashboardData,
          child: SingleChildScrollView(
            physics: const AlwaysScrollableScrollPhysics(),
            padding: const EdgeInsets.symmetric(horizontal: 20.0, vertical: 16.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                SecurityGaugeCard(
                  securityScore: controller.securityScore,
                ),
                const SizedBox(height: 16),
                ProtectionStatusCard(
                  securityScore: controller.securityScore,
                  isRealtimeActive:
                      controller.kasperskySdk.realtimeProtection,
                  lastScanText: _formatLastScan(controller.kasperskySdk.lastScanDate),
                  onScanPressed: widget.onNavigateToScanner,
                  onRealtimeToggled: (enabled) async {
                    if (enabled) {
                      final ok = await PermissionGate.ensure(context, GateFeature.realtime);
                      if (!ok || !context.mounted) return;
                    }
                    controller.toggleRealtimeProtection(enabled);
                  },
                ),
                const SizedBox(height: 24),
                SecurityFeaturesGrid(
                  onFeatureTapped: (featureName) {
                    if (featureName == 'Lainnya') {
                      AdvancedSecurityHubSheet.show(
                        context,
                        sdk: controller.kasperskySdk,
                        onNavigateToScanner: widget.onNavigateToScanner,
                        onNavigateToProfile: widget.onNavigateToProfile,
                      );
                    } else {
                      SecurityFeatureSheet.show(
                        context,
                        featureTitle: featureName,
                        sdk: controller.kasperskySdk,
                        onNavigateToScanner: widget.onNavigateToScanner,
                        onNavigateToProfile: widget.onNavigateToProfile,
                      );
                    }
                  },
                ),
                const SizedBox(height: 24),
                RecentActivityPreview(
                  latestLog: controller.latestLog,
                  onViewAll: widget.onNavigateToHistory,
                ),
                const SizedBox(height: 32),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
