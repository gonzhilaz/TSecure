import 'package:flutter/material.dart';
import '../models/activity_log.dart';
import '../models/threat_detail_item.dart';
import 'activity_log_repository.dart';
import 'telkomsel_backend_service.dart';

/// Centralized dispatcher that logs security threats locally to [ActivityLogRepository]
/// and immediately forwards real-time telemetry to the Telkomsel SOC Backend & Dashboard.
class ThreatTelemetryDispatcher {
  static final TelkomselBackendService _backend = TelkomselBackendService();

  /// Logs a security threat locally and forwards it to the SOC Dashboard in real-time.
  static Future<void> recordAndReport({
    required ActivityLogRepository? logRepo,
    required String? msisdn,
    required String? mobileId,
    required String threatType,
    required String target,
    required String severity,
    required String title,
    required String description,
    required String actionTaken,
    required IconData icon,
    required LogCategory category,
    List<ThreatDetailItem> threats = const [],
  }) async {
    final now = DateTime.now();

    // 1. Local user-visible activity history
    final logId = 'thr-${now.millisecondsSinceEpoch}';
    final timeStr = '${now.hour.toString().padLeft(2, '0')}:${now.minute.toString().padLeft(2, '0')} WIB';
    await logRepo?.addLog(ActivityLog(
      id: logId,
      title: title,
      description: description,
      time: timeStr,
      date: now,
      icon: icon,
      category: category,
      isSafe: false,
      threats: threats,
    ));

    // 2. Real-time telemetry to Golang Backend & SOC Dashboard
    final effectiveMsisdn = (msisdn != null && msisdn.isNotEmpty) ? msisdn : '081299887766';
    final effectiveMobileId = (mobileId != null && mobileId.isNotEmpty) ? mobileId : effectiveMsisdn;

    _backend.reportThreatTelemetry(
      msisdn: effectiveMsisdn,
      mobileId: effectiveMobileId,
      threatType: threatType,
      target: target,
      severity: severity,
      description: description,
      actionTaken: actionTaken,
    );
  }
}
