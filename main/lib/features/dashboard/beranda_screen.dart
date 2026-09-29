import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/theme/app_colors.dart';
import '../../core/theme/app_typography.dart';
import '../../core/widgets/advanced_security_hub_sheet.dart';
import '../../core/widgets/notification_center_sheet.dart';
import '../../core/widgets/security_feature_sheet.dart';
import '../../core/widgets/tri_state_view.dart';
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

class _BerandaScreenState extends State<BerandaScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<DashboardController>().loadDashboardData();
    });
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
                const SizedBox(height: 20),
                ProtectionStatusCard(
                  securityScore: controller.securityScore,
                  isRealtimeActive:
                      controller.kasperskySdk.realtimeProtection,
                  lastScanText: 'Hari ini, 09:42 WIB',
                  onScanPressed: widget.onNavigateToScanner,
                  onRealtimeToggled: controller.toggleRealtimeProtection,
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
