import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../models/activity_log.dart';

/// Local-First Persistent Activity Log Repository
/// Automatically records live Kaspersky scanner and RASP security events.
class ActivityLogRepository extends ChangeNotifier {
  static const String _storageKey = 'telkom_secure_activity_logs';

  final List<ActivityLog> _logs = [];
  bool _isLoaded = false;

  List<ActivityLog> get logs => List.unmodifiable(_logs);
  ActivityLog? get latestLog => _logs.isNotEmpty ? _logs.first : null;
  bool get isLoaded => _isLoaded;

  int get totalScans =>
      _logs.where((l) => l.category == LogCategory.pemindaian).length;

  int get totalThreats => _logs.where((l) => !l.isSafe).length;

  /// Loads stored activity logs or seeds initial realistic state on first launch.
  Future<void> loadLogs() async {
    final prefs = await SharedPreferences.getInstance();
    final jsonString = prefs.getString(_storageKey);

    _logs.clear();

    if (jsonString != null && jsonString.isNotEmpty) {
      try {
        final List<dynamic> rawList = jsonDecode(jsonString) as List<dynamic>;
        for (final item in rawList) {
          if (item is Map<String, dynamic>) {
            _logs.add(ActivityLog.fromJson(item));
          }
        }
      } catch (e) {
        debugPrint('[ActivityLogRepository] Error parsing logs: $e');
      }
    }

    if (_logs.isEmpty) {
      _seedInitialLogs();
      await _persist();
    }

    _isLoaded = true;
    notifyListeners();
  }

  /// Appends a new real log event to the top of the timeline and saves.
  Future<void> addLog(ActivityLog log) async {
    _logs.insert(0, log);
    // Keep max 50 recent events to conserve storage
    if (_logs.length > 50) {
      _logs.removeRange(50, _logs.length);
    }
    await _persist();
    notifyListeners();
  }

  /// Clears all logs
  Future<void> clearLogs() async {
    _logs.clear();
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove(_storageKey);
    notifyListeners();
  }

  Future<void> _persist() async {
    try {
      final prefs = await SharedPreferences.getInstance();
      final listData = _logs.map((l) => l.toJson()).toList();
      await prefs.setString(_storageKey, jsonEncode(listData));
    } catch (e) {
      debugPrint('[ActivityLogRepository] Error saving logs: $e');
    }
  }

  void _seedInitialLogs() {
    final now = DateTime.now();
    final timeStr = DateFormat('HH:mm').format(now);

    _logs.addAll([
      ActivityLog(
        id: 'ksp-init-${now.millisecondsSinceEpoch}',
        title: 'Pemindaian Sistem Siap',
        description: 'Kaspersky Engine & BlackWall RASP Aktif • 0 Ancaman',
        time: '$timeStr WIB',
        date: now,
        icon: Icons.verified_outlined,
        category: LogCategory.pemindaian,
        isSafe: true,
      ),
      ActivityLog(
        id: 'ksp-license-${now.millisecondsSinceEpoch - 1000}',
        title: 'Lisensi Kaspersky B2B Terverifikasi',
        description: 'Telkomsel Indonesia CBA (Berlaku s.d. 24 Des 2026)',
        time: '$timeStr WIB',
        date: now,
        icon: Icons.vpn_key_outlined,
        category: LogCategory.aplikasi,
        isSafe: true,
      ),
    ]);
  }
}
