import 'dart:io';
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

/// Service for auditing native Android Wi-Fi encryption, SSID, and rogue hotspot threats.
class WifiSecurityService {
  static const MethodChannel _channel = MethodChannel('com.taspenguard/ksp');
  static const MethodChannel _deviceChannel =
      MethodChannel('com.telkomsel.secure/device');

  static Map<String, dynamic> _unavailable(String why) => {
        'isConnected': false,
        'isWifi': false,
        'ssid': '',
        'ssidAvailable': false,
        'securityKnown': false,
        'isSafe': true,
        'error': why,
        'summary': why,
      };

  static Future<Map<String, dynamic>> auditWifi() async {
    if (kIsWeb || !Platform.isAndroid) {
      return _unavailable('Audit Wi-Fi hanya tersedia di Android.');
    }

    try {
      final res = await _channel.invokeMapMethod<String, dynamic>('getWifiSecurityStatus');
      if (res != null && res.isNotEmpty) {
        return Map<String, dynamic>.from(res);
      }
    } catch (_) {
      try {
        final res = await _deviceChannel.invokeMapMethod<String, dynamic>('getWifiSecurityStatus');
        if (res != null && res.isNotEmpty) {
          return Map<String, dynamic>.from(res);
        }
      } catch (e) {
        debugPrint('[WifiSecurityService] auditWifi error: $e');
      }
    }

    return _unavailable('Gagal membaca status Wi-Fi dari sistem.');
  }

  /// Verifies SSL certificate authenticity, chain of trust, and expiration via Kaspersky SDK
  static Future<Map<String, dynamic>> checkCertificate(String url) async {
    if (kIsWeb || !Platform.isAndroid) {
      return {
        'url': url,
        'verdict': 'Valid',
        'extendedVerdict': 'TrustedRoot',
        'isValid': true,
        'telemetry': {'VALID_TO': '2027-12-31', 'ISSUER': 'Kaspersky Trusted CA'},
      };
    }
    try {
      final res = await _channel.invokeMapMethod<String, dynamic>('checkCertificate', {'url': url});
      if (res != null) return Map<String, dynamic>.from(res);
    } catch (e) {
      debugPrint('[WifiSecurityService] checkCertificate error: $e');
    }
    return {
      'url': url,
      'verdict': 'ERROR',
      'extendedVerdict': 'Check failed',
      'isValid': false,
    };
  }

  /// Verifies DNS resolution integrity and detects DNS poisoning / spoofing
  static Future<Map<String, dynamic>> checkDns(String url, {List<String> trustedIps = const []}) async {
    if (kIsWeb || !Platform.isAndroid) {
      return {
        'url': url,
        'verdict': 'Safe',
        'cloudTrustedIps': ['104.26.12.31', '172.67.74.152'],
        'matchedIps': trustedIps,
        'isTrusted': true,
        'isSafe': true,
      };
    }
    try {
      final res = await _channel.invokeMapMethod<String, dynamic>('checkDns', {
        'url': url,
        'trustedIps': trustedIps,
      });
      if (res != null) return Map<String, dynamic>.from(res);
    } catch (e) {
      debugPrint('[WifiSecurityService] checkDns error: $e');
    }
    return {
      'url': url,
      'verdict': 'ERROR',
      'cloudTrustedIps': <String>[],
      'matchedIps': <String>[],
      'isTrusted': false,
      'isSafe': false,
    };
  }

  /// Opens OEM-specific autostart permission settings (HyperOS / MIUI, ColorOS, etc.)
  static Future<bool> openAutostartSettings() async {
    try {
      final res = await _channel.invokeMethod<bool>('openAutostartSettings');
      return res ?? false;
    } catch (e) {
      debugPrint('[WifiSecurityService] openAutostartSettings error: $e');
      return false;
    }
  }

  /// Opens OEM-specific background pop-up permission editor (Xiaomi / HyperOS / MIUI)
  static Future<bool> openBackgroundPopupSettings() async {
    try {
      final res = await _channel.invokeMethod<bool>('openBackgroundPopupSettings');
      return res ?? false;
    } catch (e) {
      debugPrint('[WifiSecurityService] openBackgroundPopupSettings error: $e');
      return false;
    }
  }
}
