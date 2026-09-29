import '../models/security_score.dart';
import 'kaspersky_sdk_bridge.dart';

class SecurityScoreService {
  /// Dynamically computes the mobile security health score (0 - 100%)
  /// based on active Kaspersky engine components and system audit checks.
  static SecurityScore computeScore({
    required KasperskySdkBridge kasperskySdk,
    bool isRooted = false,
    bool isStorageEncrypted = true,
    bool isLockScreenProtected = true,
    bool isWifiSecured = true,
  }) {
    int total = 100;

    // Deduct if Kaspersky engine is dormant / active period expired
    if (!kasperskySdk.isInitialized) {
      total -= 55;
    }

    // Deduct if real-time shield is off (major threat)
    if (!kasperskySdk.realtimeProtection) {
      total -= 28;
    }

    // Deduct if web filter is off
    if (!kasperskySdk.webFilter) {
      total -= 14;
    }

    // Deduct if PUA / App scan is off
    if (!kasperskySdk.puaScanner) {
      total -= 10;
    }

    // Scan recency check: if last scan was over 24 hours ago, slight degradation
    final hoursSinceLastScan =
        DateTime.now().difference(kasperskySdk.lastScanDate).inHours;
    if (hoursSinceLastScan > 24) {
      total -= 2; // e.g. 100 - 2 = 98% for recent scan!
    }

    // Deduct for detected threats
    if (kasperskySdk.threatsDetected > 0) {
      total -= (kasperskySdk.threatsDetected * 20);
    }

    // System level audits
    if (isRooted) total -= 30;
    if (!isStorageEncrypted) total -= 10;
    if (!isLockScreenProtected) total -= 10;
    if (!isWifiSecured) total -= 8;

    return SecurityScore.calculate(total);
  }
}
