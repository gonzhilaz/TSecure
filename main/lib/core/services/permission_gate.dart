import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../theme/app_colors.dart';
import '../widgets/web_filter_guide_dialog.dart';

/// Fitur yang membutuhkan izin sistem.
enum GateFeature { scan, realtime, webFilter, wifi }

class _Need {
  final String key; // key dari getAllPermissionsStatus
  final String title;
  final String reason;
  final String request; // method native
  final bool required;
  const _Need(this.key, this.title, this.reason, this.request, {this.required = true});
}

/// Meminta izin yang dibutuhkan sebuah fitur secara otomatis, tepat saat
/// fitur dipakai. Mengembalikan true jika semua izin WAJIB sudah diberikan.
class PermissionGate {
  static const MethodChannel _ch = MethodChannel('com.taspenguard/ksp');

  static const _storage = _Need(
    'storage', 'Akses Penyimpanan',
    'Diperlukan untuk memindai berkas dan aplikasi.', 'requestStoragePermission');
  static const _notif = _Need(
    'notifications', 'Notifikasi',
    'Untuk memberi peringatan saat ancaman terdeteksi.', 'requestNotificationPermission',
    required: false);
  static const _location = _Need(
    'location', 'Izin Lokasi',
    'Android mewajibkannya untuk membaca nama Wi-Fi (SSID).', 'requestLocationPermission');
  static const _a11y = _Need(
    'accessibility', 'Aksesibilitas',
    'Untuk memeriksa tautan di browser.', 'requestAccessibilityPermission');

  static List<_Need> _needs(GateFeature f) {
    switch (f) {
      case GateFeature.scan:
      case GateFeature.realtime:
        return [_storage, _notif];
      case GateFeature.webFilter:
        return [_a11y, _notif];
      case GateFeature.wifi:
        return [_location];
    }
  }

  static Future<Map<String, dynamic>> status() async {
    try {
      final r = await _ch.invokeMapMethod<String, dynamic>('getAllPermissionsStatus');
      return r == null ? {} : Map<String, dynamic>.from(r);
    } catch (_) {
      return {};
    }
  }

  static Future<bool> ensure(BuildContext context, GateFeature feature) async {
    var st = await status();
    var allRequired = true;
    for (final need in _needs(feature)) {
      if (st[need.key] == true) continue;
      if (!context.mounted) return false;
      final ok = await _ask(context, need);
      if (!ok) {
        if (need.required) allRequired = false;
        continue;
      }
      st = await status();
      if (st[need.key] != true && need.required) allRequired = false;
    }
    if (feature == GateFeature.wifi && allRequired && st['locationEnabled'] == false) {
      if (context.mounted) await _askGps(context);
    }
    return allRequired;
  }

  static Future<bool> _ask(BuildContext context, _Need need) async {
    final go = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: Text('Izinkan ${need.title}?'),
        content: Text(need.reason),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx, false), child: const Text('Nanti')),
          TextButton(
            onPressed: () => Navigator.pop(ctx, true),
            child: const Text('Izinkan', style: TextStyle(color: AppColors.primary, fontWeight: FontWeight.w700)),
          ),
        ],
      ),
    );
    if (go != true || !context.mounted) return false;

    if (need.key == 'accessibility') {
      await WebFilterGuideDialog.show(
        context,
        onOpenAccessibility: () => _ch.invokeMethod('requestAccessibilityPermission'),
        onOpenAppSettings: () => _ch.invokeMethod('openAppSettings'),
      );
    } else {
      try { await _ch.invokeMethod(need.request); } catch (_) {}
    }
    return _waitGranted(need.key);
  }

  /// Menunggu hingga izin aktif (maks 2 menit; user bisa berada di Pengaturan).
  static Future<bool> _waitGranted(String key) async {
    for (var i = 0; i < 120; i++) {
      await Future<void>.delayed(const Duration(seconds: 1));
      if ((await status())[key] == true) return true;
    }
    return false;
  }

  static Future<void> _askGps(BuildContext context) async {
    final go = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Aktifkan Lokasi (GPS)?'),
        content: const Text('Tanpa GPS aktif, nama Wi-Fi tidak dapat dibaca oleh Android.'),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx, false), child: const Text('Lewati')),
          TextButton(onPressed: () => Navigator.pop(ctx, true), child: const Text('Buka Pengaturan')),
        ],
      ),
    );
    if (go == true) {
      try { await _ch.invokeMethod('openLocationSettings'); } catch (_) {}
      for (var i = 0; i < 60; i++) {
        await Future<void>.delayed(const Duration(seconds: 1));
        if ((await status())['locationEnabled'] == true) break;
      }
    }
  }
}
