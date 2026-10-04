import 'package:flutter/services.dart';

/// Dart-side bridge for the native Kaspersky WebFilterControl (URL blocking)
/// and vendor-specific compatibility helpers (MIUI, Samsung, etc.).
///
/// Separated from [KasperskySdkBridge] to keep modules under 400 lines.
class UrlFilterService {
  static const MethodChannel _channel = MethodChannel('com.taspenguard/ksp');

  // --- URL Filter (Kaspersky WebFilterControl) ---

  /// Initialize the WebFilterControl engine (must be called once after license).
  static Future<bool> init() async {
    try {
      final res = await _channel.invokeMethod<bool>('urlFilterInit');
      return res ?? false;
    } catch (_) {
      return false;
    }
  }

  /// Enable or disable real-time URL filtering via the Accessibility Service.
  static Future<bool> setEnabled(bool enabled) async {
    try {
      final res = await _channel.invokeMethod<bool>(
        'urlFilterEnable',
        {'enabled': enabled},
      );
      return res ?? false;
    } catch (_) {
      return false;
    }
  }

  /// Check whether URL filtering is currently active.
  static Future<bool> isEnabled() async {
    try {
      final res = await _channel.invokeMethod<bool>('isUrlFilterEnabled');
      return res ?? false;
    } catch (_) {
      return false;
    }
  }

  static bool isJudiOnlineUrl(String url) {
    final l = url.toLowerCase();
    return l.contains('slot') || l.contains('gacor') || l.contains('maxwin') ||
           l.contains('pragmatic') || l.contains('olympus') || l.contains('zeus') ||
           l.contains('togel') || l.contains('sbobet') || l.contains('judol') ||
           l.contains('kasino') || l.contains('casino') || l.contains('poker') ||
           l.contains('scatter') || l.contains('mahjong') || l.contains('depopulsa');
  }

  /// Manually check a single URL against Kaspersky cloud and local bases.
  /// Returns verdict map with `isBlocked`, `category`, `verdict`, `url`.
  static Future<Map<String, dynamic>> checkUrl(String url) async {
    try {
      final res = await _channel.invokeMapMethod<String, dynamic>(
        'urlFilterCheckUrl',
        {'url': url},
      );
      if (res != null) return Map<String, dynamic>.from(res);
    } catch (_) {}
    final isJudol = isJudiOnlineUrl(url);
    return {
      'isBlocked': isJudol,
      'category': isJudol ? 'Judi Online & Taruhan Ilegal' : 'Unknown',
      'verdict': isJudol ? 'bad' : 'error',
      'url': url,
    };
  }
}

/// Dart-side bridge for OEM vendor compatibility (MIUI, Samsung, OPPO, etc.)
class VendorCompatService {
  static const MethodChannel _channel = MethodChannel('com.taspenguard/ksp');

  /// Whether the device is a Xiaomi / Redmi / POCO (MIUI/HyperOS).
  static Future<bool> isMiuiDevice() async {
    try {
      final res = await _channel.invokeMethod<bool>('isMiui');
      return res ?? false;
    } catch (_) {
      return false;
    }
  }

  /// Whether MIUI has the background popup permission editor activity.
  static Future<bool> hasMiuiPopupEditor() async {
    try {
      final res = await _channel.invokeMethod<bool>('hasMiuiPopupEditor');
      return res ?? false;
    } catch (_) {
      return false;
    }
  }

  /// Opens the MIUI background popup permission editor for this app.
  /// Required for the web filter block page to appear on Xiaomi phones.
  static Future<bool> requestMiuiBackgroundPopup() async {
    try {
      final res = await _channel.invokeMethod<bool>(
        'requestMiuiBackgroundPopup',
      );
      return res ?? false;
    } catch (_) {
      return false;
    }
  }

  /// Opens Samsung's battery optimization settings.
  /// Helps prevent Samsung from killing the RTP foreground service.
  static Future<bool> openSamsungBatterySettings() async {
    try {
      final res = await _channel.invokeMethod<bool>(
        'openSamsungBatterySettings',
      );
      return res ?? false;
    } catch (_) {
      return false;
    }
  }

  /// Returns vendor info map: manufacturer, brand, model, isMiui, isSamsung, etc.
  static Future<Map<String, dynamic>> getVendorInfo() async {
    try {
      final res = await _channel.invokeMapMethod<String, dynamic>(
        'getVendorInfo',
      );
      if (res != null) return Map<String, dynamic>.from(res);
    } catch (_) {}
    return {
      'manufacturer': 'Unknown',
      'isMiui': false,
      'isSamsung': false,
    };
  }

  /// Whether the current device is a Samsung phone.
  static Future<bool> isSamsungDevice() async {
    final info = await getVendorInfo();
    return info['isSamsung'] as bool? ?? false;
  }

  /// Check if RTP (Real-Time Protection) file monitor is actively running.
  static Future<bool> isRtpActive() async {
    try {
      final res = await _channel.invokeMethod<bool>('isRtpActive');
      return res ?? false;
    } catch (_) {
      return false;
    }
  }
}
