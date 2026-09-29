import 'package:flutter/material.dart';
import '../../core/widgets/tri_state_view.dart';
import '../../data/models/activity_log.dart';
import '../../data/models/security_score.dart';
import '../../data/services/activity_log_repository.dart';
import '../../data/services/kaspersky_sdk_bridge.dart';
import '../../data/services/security_score_service.dart';

class DashboardController extends ChangeNotifier {
  final KasperskySdkBridge kasperskySdk;
  final ActivityLogRepository logRepository;

  ViewState _state = ViewState.loading;
  late SecurityScore _securityScore;

  DashboardController({
    required this.kasperskySdk,
    required this.logRepository,
  }) {
    kasperskySdk.addListener(_onKasperskyUpdated);
    logRepository.addListener(_onLogsUpdated);
    _securityScore = SecurityScoreService.computeScore(
      kasperskySdk: kasperskySdk,
    );
  }

  ViewState get state => _state;
  ActivityLog? get latestLog => logRepository.latestLog;
  SecurityScore get securityScore => _securityScore;

  void _onKasperskyUpdated() {
    _securityScore = SecurityScoreService.computeScore(
      kasperskySdk: kasperskySdk,
    );
    notifyListeners();
  }

  void _onLogsUpdated() {
    notifyListeners();
  }

  Future<void> loadDashboardData() async {
    _state = ViewState.loading;
    notifyListeners();

    try {
      if (!logRepository.isLoaded) {
        await logRepository.loadLogs();
      }
      _securityScore = SecurityScoreService.computeScore(
        kasperskySdk: kasperskySdk,
      );
      _state = ViewState.success;
      notifyListeners();
    } catch (e) {
      _state = ViewState.error;
      notifyListeners();
    }
  }

  void toggleRealtimeProtection(bool enabled) {
    kasperskySdk.toggleRealtimeProtection(enabled);
    _securityScore = SecurityScoreService.computeScore(
      kasperskySdk: kasperskySdk,
    );
    notifyListeners();
  }

  @override
  void dispose() {
    kasperskySdk.removeListener(_onKasperskyUpdated);
    logRepository.removeListener(_onLogsUpdated);
    super.dispose();
  }
}
