import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/theme/app_colors.dart';
import '../../core/theme/app_typography.dart';
import '../../core/widgets/activity_detail_sheet.dart';
import '../../core/widgets/notification_center_sheet.dart';
import '../../core/widgets/scan_threat_detail_sheet.dart';
import '../../core/widgets/tri_state_view.dart';
import '../../data/services/kaspersky_sdk_bridge.dart';
import '../../data/services/threat_manager_service.dart';
import 'history_controller.dart';
import 'widgets/history_log_item.dart';
import 'widgets/monthly_summary_card.dart';

class RiwayatScreen extends StatefulWidget {
  final VoidCallback onNavigateToProfile;

  const RiwayatScreen({
    super.key,
    required this.onNavigateToProfile,
  });

  @override
  State<RiwayatScreen> createState() => _RiwayatScreenState();
}

class _RiwayatScreenState extends State<RiwayatScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<HistoryController>().loadLogs();
    });
  }

  @override
  Widget build(BuildContext context) {
    final controller = context.watch<HistoryController>();

    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: AppBar(
        title: Text('Riwayat Aktivitas', style: AppTypography.headlineSm),
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
        onRetry: controller.loadLogs,
        child: RefreshIndicator(
          color: AppColors.primary,
          onRefresh: controller.loadLogs,
          child: SingleChildScrollView(
            physics: const AlwaysScrollableScrollPhysics(),
            padding: const EdgeInsets.symmetric(horizontal: 20.0, vertical: 16.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                MonthlySummaryCard(
                  totalScans: controller.totalScans,
                  threatsFound: controller.totalThreats,
                ),
                const SizedBox(height: 20),
                _buildFilterRow(controller),
                const SizedBox(height: 24),
                if (controller.groupedLogs.isEmpty)
                  Center(
                    child: Padding(
                      padding: const EdgeInsets.symmetric(vertical: 40.0),
                      child: Text(
                        'Belum ada aktivitas tercatat',
                        style: AppTypography.bodyMd.copyWith(color: AppColors.slateMuted),
                      ),
                    ),
                  )
                else
                  ...controller.groupedLogs.entries.map((entry) {
                    final dateLabel = entry.key;
                    final logList = entry.value;
                    return Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        _buildSectionHeader(dateLabel, '${logList.length} Aktivitas'),
                        const SizedBox(height: 12),
                        ...logList.map((log) => HistoryLogItem(
                              log: log,
                              onTap: () {
                                if (!log.isSafe || log.threats.isNotEmpty) {
                                  ScanThreatDetailSheet.show(
                                    context,
                                    log: log,
                                    threatManager: context.read<ThreatManagerService>(),
                                    kasperskySdk: context.read<KasperskySdkBridge>(),
                                  );
                                } else {
                                  ActivityDetailSheet.show(context, log);
                                }
                              },
                            )),
                        const SizedBox(height: 20),
                      ],
                    );
                  }),
                const SizedBox(height: 24),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildFilterRow(HistoryController controller) {
    return SingleChildScrollView(
      scrollDirection: Axis.horizontal,
      child: Row(
        children: controller.filterOptions.map((filter) {
          final isSelected = controller.selectedFilter == filter;
          return Padding(
            padding: const EdgeInsets.only(right: 8.0),
            child: ChoiceChip(
              label: Text(
                filter,
                style: AppTypography.labelMd.copyWith(
                  color: isSelected ? Colors.white : AppColors.navyDeep,
                  fontWeight: FontWeight.w600,
                ),
              ),
              selected: isSelected,
              selectedColor: AppColors.primary,
              backgroundColor: AppColors.surface,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(20),
                side: BorderSide(
                  color: isSelected ? AppColors.primary : AppColors.slateBorder,
                ),
              ),
              showCheckmark: false,
              onSelected: (_) => controller.setFilter(filter),
            ),
          );
        }).toList(),
      ),
    );
  }

  Widget _buildSectionHeader(String title, String count) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        Text(
          title,
          style: AppTypography.labelSm.copyWith(
            letterSpacing: 1.0,
            color: AppColors.slateMid,
            fontWeight: FontWeight.w700,
          ),
        ),
        Text(
          count,
          style: AppTypography.bodySm.copyWith(
            color: AppColors.slateMuted,
          ),
        ),
      ],
    );
  }
}
