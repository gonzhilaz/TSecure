import 'package:flutter/material.dart';
import '../../data/services/protection_status_service.dart';
import 'security_feature_models.dart';

/// Konfigurasi fitur yang hanya menampilkan data NYATA dari perangkat.
class LiveFeatureConfigs {
  static String _hm(int ms) {
    final d = DateTime.fromMillisecondsSinceEpoch(ms);
    String two(int v) => v.toString().padLeft(2, '0');
    return '${two(d.hour)}:${two(d.minute)}:${two(d.second)}';
  }

  static SecurityFeatureConfig realtime({
    required ProtectionStatus? status,
    required bool loading,
    required String dbVersion,
    required VoidCallback onAction,
    required VoidCallback onRefresh,
  }) {
    final st = status;
    final running = st?.rtpRunning == true;
    final rows = <SecurityFeatureTelemetryRow>[
      SecurityFeatureTelemetryRow(
        'Status Pemantauan',
        st == null
            ? (loading ? 'Memeriksa...' : 'Tidak terbaca')
            : (running ? 'Berjalan sejak ${_hm(st.rtpStartedAt)}' : 'Tidak berjalan'),
      ),
      SecurityFeatureTelemetryRow('Berkas Diperiksa', '${st?.filesChecked ?? 0}'),
      SecurityFeatureTelemetryRow('Ancaman Dicegat', '${st?.threats ?? 0}'),
      SecurityFeatureTelemetryRow('Aktivitas Terakhir', ProtectionStatusService.ago(st?.lastActivity ?? 0)),
      SecurityFeatureTelemetryRow('Database Virus', dbVersion),
    ];
    final recent = (st?.events ?? const <ProtectionEvent>[]).where((e) => e.source == 'RTP').take(3);
    for (final e in recent) {
      rows.add(SecurityFeatureTelemetryRow('${_hm(e.time)} ${e.kind}', e.detail));
    }
    return SecurityFeatureConfig(
      title: 'Realtime Scanner',
      icon: Icons.shield,
      engine: 'Kaspersky Anti-Malware Core',
      description: 'Memantau berkas baru/diubah di perangkat. Angka di bawah dibaca langsung dari layanan proteksi.',
      actionLabel: 'Muat Ulang Status',
      onAction: onRefresh,
      telemetry: rows,
    );
  }

  static String _ssidText(Map<String, dynamic>? w, bool loading) {
    if (loading) return 'Memeriksa...';
    if (w == null) return 'Belum diaudit';
    if (w['isWifi'] != true) return 'Tidak terhubung Wi-Fi';
    final ssid = (w['ssid'] as String?) ?? '';
    if (ssid.isNotEmpty) return ssid;
    if (w['hasLocationPermission'] == false) return 'Izin lokasi belum aktif';
    if (w['isGpsEnabled'] == false) return 'GPS mati';
    return 'Nama tidak terbaca';
  }

  static SecurityFeatureConfig wifi({
    required Map<String, dynamic>? audit,
    required bool loading,
    required bool enabled,
    required VoidCallback onAction,
  }) {
    final w = audit;
    final connected = w?['isWifi'] == true;
    String val(String k, {String empty = '-'}) {
      final v = w?[k];
      return (v is String && v.isNotEmpty) ? v : empty;
    }

    final proto = connected
        ? (w?['securityKnown'] == true ? val('securityProtocol') : 'Tidak diketahui')
        : '-';
    final encryption = !connected
        ? '-'
        : (w?['isOpenNetwork'] == true ? 'Terbuka (tidak aman)' : 'Terenkripsi');
    return SecurityFeatureConfig(
      title: 'Wifi Safety',
      icon: Icons.wifi_rounded,
      engine: 'TelkomSecure Network Guard',
      description: 'Membaca jaringan Wi-Fi yang sedang dipakai dan memeriksa enkripsinya.',
      actionLabel: loading ? 'Memeriksa...' : 'Audit Wi-Fi',
      onAction: onAction,
      telemetry: [
        SecurityFeatureTelemetryRow('Jaringan', _ssidText(w, loading)),
        SecurityFeatureTelemetryRow('Keamanan', proto),
        SecurityFeatureTelemetryRow('Enkripsi', enabled ? encryption : 'Nonaktif'),
        SecurityFeatureTelemetryRow('Sinyal', connected ? val('signalLevel') : '-'),
        SecurityFeatureTelemetryRow('Gateway', connected ? val('gateway') : '-'),
        SecurityFeatureTelemetryRow('DNS', connected ? val('dnsResolver') : '-'),
      ],
    );
  }

  static bool initialStatus(String title, dynamic sdk) {
    final t = title.toLowerCase();
    if (t.contains('web')) return sdk.webFilter;
    if (t.contains('realtime')) return sdk.realtimeProtection;
    if (t.contains('pua')) return sdk.puaScanner;
    if (t.contains('wifi')) return sdk.wifiSafety;
    if (t.contains('fake')) return sdk.fakeAppsProtection;
    if (t.contains('device')) return sdk.deviceReputation;
    return sdk.dataBreachProtection;
  }

  static void applyToggle(String title, dynamic sdk, bool value) {
    final t = title.toLowerCase();
    if (t.contains('web')) {
      sdk.toggleWebFilter(value);
    } else if (t.contains('realtime')) {
      sdk.toggleRealtimeProtection(value);
    } else if (t.contains('pua')) {
      sdk.togglePuaScanner(value);
    } else if (t.contains('wifi')) {
      sdk.toggleWifiSafety(value);
    } else if (t.contains('fake')) {
      sdk.toggleFakeApps(value);
    } else if (t.contains('device')) {
      sdk.toggleDeviceRep(value);
    } else if (t.contains('data')) {
      sdk.toggleDataBreach(value);
    }
  }
}
