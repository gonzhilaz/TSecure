import 'dart:convert';
import 'dart:io';
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import '../../core/constants/app_constants.dart';

class AppUpdateInfo {
  final bool hasUpdate;
  final String latestVersion;
  final int latestBuildNumber;
  final int minSupportedBuild;
  final bool isMandatory;
  final List<String> releaseNotes;
  final String downloadUrl;
  final double apkSizeMb;
  final String publishedAt;

  AppUpdateInfo({
    required this.hasUpdate,
    required this.latestVersion,
    required this.latestBuildNumber,
    required this.minSupportedBuild,
    required this.isMandatory,
    required this.releaseNotes,
    required this.downloadUrl,
    required this.apkSizeMb,
    required this.publishedAt,
  });

  factory AppUpdateInfo.fromJson(Map<String, dynamic> json) {
    return AppUpdateInfo(
      hasUpdate: json['has_update'] as bool? ?? false,
      latestVersion: json['latest_version'] as String? ?? '1.0.0',
      latestBuildNumber: json['latest_build_number'] as int? ?? 1,
      minSupportedBuild: json['min_supported_build'] as int? ?? 1,
      isMandatory: json['is_mandatory'] as bool? ?? false,
      releaseNotes: (json['release_notes'] as List<dynamic>?)
              ?.map((e) => e.toString())
              .toList() ??
          [],
      downloadUrl: json['download_url'] as String? ?? '',
      apkSizeMb: (json['apk_size_mb'] as num?)?.toDouble() ?? 28.0,
      publishedAt: json['published_at'] as String? ?? '',
    );
  }
}

class AppUpdateService {
  static const _channel = MethodChannel('com.telkomsel.secure/device');
  final String _baseUrl;

  AppUpdateService({String? baseUrl})
      : _baseUrl = baseUrl ?? AppConstants.defaultBackendUrl;

  /// Fetches native app version and build code
  Future<Map<String, dynamic>> getCurrentVersion() async {
    try {
      final res = await _channel.invokeMapMethod<String, dynamic>('getAppVersion');
      return res ?? {'versionName': '1.0.0', 'versionCode': 1};
    } catch (_) {
      return {'versionName': '1.0.0', 'versionCode': 1};
    }
  }

  /// Checks server if a newer build is available
  Future<AppUpdateInfo?> checkForUpdate() async {
    try {
      final cur = await getCurrentVersion();
      final code = cur['versionCode'] ?? 1;
      final name = cur['versionName'] ?? '1.0.0';

      final client = HttpClient()..connectionTimeout = const Duration(seconds: 5);
      final uri = Uri.parse(
        '$_baseUrl/api/v1/app/check-update?build_number=$code&version=$name',
      );
      final req = await client.getUrl(uri);
      final resp = await req.close();

      if (resp.statusCode == 200) {
        final body = await resp.transform(utf8.decoder).join();
        final json = jsonDecode(body) as Map<String, dynamic>;
        return AppUpdateInfo.fromJson(json);
      }
      return null;
    } catch (e) {
      debugPrint('[AppUpdateService] Check update error: $e');
      return null;
    }
  }

  /// Streams and downloads the APK with realtime progress reporting
  Future<String?> downloadApk(
    String downloadUrl, {
    required void Function(double progress, int received, int total) onProgress,
  }) async {
    try {
      Directory tempDir;
      try {
        tempDir = Directory.systemTemp;
      } catch (_) {
        tempDir = Directory('/data/local/tmp');
      }
      final savePath = '${tempDir.path}/TelkomSecure-Update.apk';

      final targetFile = File(savePath);
      if (await targetFile.exists()) {
        try {
          await targetFile.delete();
        } catch (_) {}
      }

      final client = HttpClient()..connectionTimeout = const Duration(seconds: 15);
      final uri = Uri.parse(downloadUrl.startsWith('http') ? downloadUrl : '$_baseUrl$downloadUrl');
      final req = await client.getUrl(uri);
      final resp = await req.close();

      if (resp.statusCode != 200) {
        debugPrint('[AppUpdateService] Download failed: status ${resp.statusCode}');
        return null;
      }

      final totalBytes = resp.contentLength > 0 ? resp.contentLength : (28 * 1024 * 1024);
      int receivedBytes = 0;
      final sink = targetFile.openWrite();

      await for (final chunk in resp) {
        sink.add(chunk);
        receivedBytes += chunk.length;
        final progress = (receivedBytes / totalBytes).clamp(0.0, 1.0);
        onProgress(progress, receivedBytes, totalBytes);
      }

      await sink.flush();
      await sink.close();

      debugPrint('[AppUpdateService] APK downloaded successfully to: $savePath');
      return savePath;
    } catch (e) {
      debugPrint('[AppUpdateService] Download error: $e');
      return null;
    }
  }

  /// Triggers Android package installer for the downloaded APK
  Future<bool> installApk(String filePath) async {
    try {
      final success = await _channel.invokeMethod<bool>('installApk', {
        'filePath': filePath,
      });
      return success ?? false;
    } catch (e) {
      debugPrint('[AppUpdateService] Install APK error: $e');
      return false;
    }
  }

  /// Checks if the app has permission to request package installation
  Future<bool> checkInstallPermission() async {
    try {
      const kspChannel = MethodChannel('com.taspenguard/ksp');
      final res = await kspChannel.invokeMethod<bool>('checkInstallPermission');
      return res ?? true;
    } catch (_) {
      return true;
    }
  }

  /// Opens system settings to allow installing unknown apps
  Future<bool> requestInstallPermission() async {
    try {
      const kspChannel = MethodChannel('com.taspenguard/ksp');
      final res = await kspChannel.invokeMethod<bool>('requestInstallPermission');
      return res ?? true;
    } catch (_) {
      return false;
    }
  }
}
