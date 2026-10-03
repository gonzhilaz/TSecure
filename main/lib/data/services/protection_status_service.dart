import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

/// Status nyata Proteksi Real-Time & Web Filter dari native (bukti berjalan).
class ProtectionStatus {
  final bool rtpRunning, webRunning;
  final int rtpStartedAt, lastActivity, filesChecked, urlsChecked, threats;
  final List<ProtectionEvent> events;

  const ProtectionStatus({
    this.rtpRunning = false, this.webRunning = false,
    this.rtpStartedAt = 0, this.lastActivity = 0,
    this.filesChecked = 0, this.urlsChecked = 0, this.threats = 0,
    this.events = const [],
  });

  factory ProtectionStatus.fromMap(Map<dynamic, dynamic> m) {
    int i(String k) => (m[k] as num?)?.toInt() ?? 0;
    final raw = (m['events'] as List?) ?? const [];
    return ProtectionStatus(
      rtpRunning: m['rtpRunning'] == true,
      webRunning: m['webRunning'] == true,
      rtpStartedAt: i('rtpStartedAt'),
      lastActivity: i('lastActivity'),
      filesChecked: i('filesChecked'),
      urlsChecked: i('urlsChecked'),
      threats: i('threats'),
      events: raw.whereType<Map>().map(ProtectionEvent.fromMap).toList(),
    );
  }
}

class ProtectionEvent {
  final int time;
  final String source, kind, detail;
  const ProtectionEvent(this.time, this.source, this.kind, this.detail);

  factory ProtectionEvent.fromMap(Map<dynamic, dynamic> m) => ProtectionEvent(
        (m['t'] as num?)?.toInt() ?? 0,
        m['src'] as String? ?? '',
        m['kind'] as String? ?? '',
        m['detail'] as String? ?? '',
      );
}

class ProtectionStatusService {
  static const MethodChannel _ch = MethodChannel('com.taspenguard/ksp');

  static Future<ProtectionStatus?> fetch() async {
    try {
      final r = await _ch.invokeMapMethod<dynamic, dynamic>('getProtectionStatus');
      return r == null ? null : ProtectionStatus.fromMap(r);
    } catch (e) {
      debugPrint('[ProtectionStatusService] $e');
      return null;
    }
  }

  static String ago(int epochMs) {
    if (epochMs <= 0) return '-';
    final d = DateTime.now().difference(DateTime.fromMillisecondsSinceEpoch(epochMs));
    if (d.inSeconds < 60) return '${d.inSeconds} dtk lalu';
    if (d.inMinutes < 60) return '${d.inMinutes} mnt lalu';
    if (d.inHours < 24) return '${d.inHours} jam lalu';
    return '${d.inDays} hari lalu';
  }
}
