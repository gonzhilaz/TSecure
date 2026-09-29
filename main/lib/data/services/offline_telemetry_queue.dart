import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:shared_preferences/shared_preferences.dart';

class OfflineTelemetryQueue {
  static const String _keyPendingThreats = 'tsel_pending_telemetry_queue';

  /// Adds a threat event to persistent local storage when device is offline
  static Future<void> enqueueThreat({
    required String msisdn,
    required String mobileId,
    required String threatType,
    required String target,
    required String severity,
    required String description,
    required String actionTaken,
  }) async {
    try {
      final prefs = await SharedPreferences.getInstance();
      final list = prefs.getStringList(_keyPendingThreats) ?? [];

      final item = {
        'msisdn': msisdn,
        'mobile_id': mobileId,
        'threat_type': threatType,
        'target': target,
        'severity': severity,
        'description': description,
        'action_taken': actionTaken,
        'queued_at': DateTime.now().toIso8601String(),
      };

      list.add(jsonEncode(item));
      // Bounded queue: keep max 50 recent offline events to avoid memory bloat
      if (list.length > 50) {
        list.removeAt(0);
      }

      await prefs.setStringList(_keyPendingThreats, list);
      debugPrint('[TelemetryQueue] Threat queued offline: $threatType on $target');
    } catch (e) {
      debugPrint('[TelemetryQueue] Error queueing threat: $e');
    }
  }

  /// Retrieves and clears all pending threats from local storage
  static Future<List<Map<String, dynamic>>> dequeueAll() async {
    try {
      final prefs = await SharedPreferences.getInstance();
      final list = prefs.getStringList(_keyPendingThreats) ?? [];
      if (list.isEmpty) return [];

      final result = <Map<String, dynamic>>[];
      for (final s in list) {
        try {
          result.add(jsonDecode(s) as Map<String, dynamic>);
        } catch (_) {}
      }

      await prefs.remove(_keyPendingThreats);
      return result;
    } catch (e) {
      debugPrint('[TelemetryQueue] Error dequeuing threats: $e');
      return [];
    }
  }
}
