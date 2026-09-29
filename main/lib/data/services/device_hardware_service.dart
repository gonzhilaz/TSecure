import 'dart:io';
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

class DeviceHardwareService {
  static const MethodChannel _blackwallChannel =
      MethodChannel('com.telkomsel.secure/blackwall');

  static String? _cachedDeviceModel;
  static String? _cachedOsVersion;

  /// Fetches real device hardware model (e.g. "Samsung SM-S918B", "Xiaomi 23078PND5G")
  static Future<String> getDeviceModel() async {
    if (_cachedDeviceModel != null) return _cachedDeviceModel!;
    await _initDeviceInfo();
    return _cachedDeviceModel ?? 'Smartphone Android';
  }

  /// Fetches real OS version (e.g. "Android 14")
  static Future<String> getOsVersion() async {
    if (_cachedOsVersion != null) return _cachedOsVersion!;
    await _initDeviceInfo();
    return _cachedOsVersion ?? 'Android 14';
  }

  static Future<void> _initDeviceInfo() async {
    if (kIsWeb) {
      _cachedDeviceModel = 'Web Browser Client';
      _cachedOsVersion = 'Web Dashboard';
      return;
    }

    if (Platform.isAndroid) {
      try {
        final result = await _blackwallChannel.invokeMapMethod<String, dynamic>('getDeviceInfo');
        if (result != null) {
          _cachedDeviceModel = result['deviceModel'] as String?;
          _cachedOsVersion = result['osVersion'] as String?;
        }
      } catch (e) {
        debugPrint('[DeviceHardwareService] Error fetching native device info: $e');
      }
    }

    _cachedDeviceModel ??= Platform.operatingSystem.toUpperCase();
    _cachedOsVersion ??= 'Android ${Platform.operatingSystemVersion}';
  }
}
