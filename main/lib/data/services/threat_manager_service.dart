import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../models/activity_log.dart';
import '../models/quarantine_item.dart';
import '../models/threat_detail_item.dart';
import 'activity_log_repository.dart';

class ThreatManagerService extends ChangeNotifier {
  static const String keyAutoQuarantine = 'ts_auto_quarantine';
  static const String keyAutoClearThreats = 'ts_auto_clear_threats';
  static const MethodChannel _kspChannel = MethodChannel('com.taspenguard/ksp');

  bool _autoQuarantine = true;
  bool _autoClearThreats = false;
  List<QuarantineItem> _quarantinedItems = [];
  bool _isLoadingVault = false;
  ActivityLogRepository? logRepository;

  ThreatManagerService([this.logRepository]) {
    _loadPreferences();
    loadQuarantinedItems();
  }

  bool get autoQuarantine => _autoQuarantine;
  bool get autoClearThreats => _autoClearThreats;
  List<QuarantineItem> get quarantinedItems => _quarantinedItems;
  bool get isLoadingVault => _isLoadingVault;

  Future<void> _loadPreferences() async {
    try {
      final prefs = await SharedPreferences.getInstance();
      _autoQuarantine = prefs.getBool(keyAutoQuarantine) ?? true;
      _autoClearThreats = prefs.getBool(keyAutoClearThreats) ?? false;
      notifyListeners();
    } catch (_) {}
  }

  Future<void> setAutoQuarantine(bool enabled) async {
    _autoQuarantine = enabled;
    notifyListeners();
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool(keyAutoQuarantine, enabled);
  }

  Future<void> setAutoClearThreats(bool enabled) async {
    _autoClearThreats = enabled;
    notifyListeners();
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool(keyAutoClearThreats, enabled);
  }

  /// Loads all currently isolated items from the native Quarantine Vault
  Future<void> loadQuarantinedItems() async {
    _isLoadingVault = true;
    notifyListeners();
    try {
      final rawList = await _kspChannel.invokeListMethod<dynamic>('getAllQuarantined');
      if (rawList != null) {
        _quarantinedItems = rawList
            .map((e) => QuarantineItem.fromMap(Map<String, dynamic>.from(e as Map)))
            .toList();
      }
    } catch (e) {
      debugPrint('[ThreatManager] Error loading quarantined items: $e');
    } finally {
      _isLoadingVault = false;
      notifyListeners();
    }
  }

  /// Restores a quarantined file back to its original location
  Future<bool> restoreQuarantinedItem(String itemId) async {
    try {
      final res = await _kspChannel.invokeMapMethod<String, dynamic>(
        'restoreQuarantinedFile',
        {'itemId': itemId},
      );
      final success = res?['success'] as bool? ?? false;
      if (success) {
        await loadQuarantinedItems();
      }
      return success;
    } catch (e) {
      debugPrint('[ThreatManager] Restore error: $e');
      return false;
    }
  }

  /// Permanently deletes a single item from the quarantine vault
  Future<bool> deleteQuarantinedItem(String itemId) async {
    try {
      final res = await _kspChannel.invokeMapMethod<String, dynamic>(
        'deleteQuarantinedItem',
        {'itemId': itemId},
      );
      final success = res?['success'] as bool? ?? false;
      if (success) {
        await loadQuarantinedItems();
      }
      return success;
    } catch (e) {
      debugPrint('[ThreatManager] Delete quarantined item error: $e');
      return false;
    }
  }

  /// Completely empties the quarantine vault
  Future<bool> clearAllQuarantineVault() async {
    try {
      final res = await _kspChannel.invokeMapMethod<String, dynamic>('clearAllQuarantined');
      final success = res?['success'] as bool? ?? false;
      if (success) {
        _quarantinedItems.clear();
        notifyListeners();
      }
      return success;
    } catch (e) {
      debugPrint('[ThreatManager] Clear vault error: $e');
      return false;
    }
  }

  /// Finds quarantined item matching a given file path or file name
  QuarantineItem? findQuarantinedItem(String filePath) {
    final fileName = filePath.contains('/') ? filePath.split('/').last : filePath;
    try {
      return _quarantinedItems.firstWhere(
        (q) => q.originalPath == filePath || q.fileName == fileName,
      );
    } catch (_) {
      return null;
    }
  }

  /// Deletes a quarantined item by file path or shreds it if still on disk
  Future<bool> deleteQuarantinedByFilePath(String filePath) async {
    final item = findQuarantinedItem(filePath);
    bool ok = false;
    if (item != null) {
      ok = await deleteQuarantinedItem(item.id);
    }
    try {
      final res = await _kspChannel.invokeMapMethod<String, dynamic>('deleteThreatFile', {'filePath': filePath});
      final directOk = res?['success'] as bool? ?? false;
      if (directOk) ok = true;
    } catch (_) {}
    return ok;
  }

  /// Restores a quarantined item by file path
  Future<bool> restoreQuarantinedByFilePath(String filePath) async {
    final item = findQuarantinedItem(filePath);
    if (item != null) {
      return await restoreQuarantinedItem(item.id);
    }
    return false;
  }

  /// Clears/shreds all detected threats natively from disk
  Future<void> clearAllThreats(ActivityLog log, {List<ThreatDetailItem>? localThreats}) async {
    final threatsToProcess = (localThreats != null && localThreats.isNotEmpty)
        ? localThreats
        : (log.threats.isNotEmpty ? log.threats : <ThreatDetailItem>[]);

    for (final t in threatsToProcess) {
      try {
        final res = await _kspChannel.invokeMapMethod<String, dynamic>('deleteThreatFile', {'filePath': t.filePath});
        final success = res?['success'] as bool? ?? false;
        t.actionTaken = success ? 'DIBERSIHKAN' : 'GAGAL';
        if (!success) {
          debugPrint('[ThreatManager] Native shredding error for ${t.filePath}: ${res?['message']}');
        }
      } catch (e) {
        t.actionTaken = 'GAGAL';
        debugPrint('[ThreatManager] Native shredding exception for ${t.filePath}: $e');
      }
    }
    notifyListeners();

    if (logRepository != null) {
      final allSuccess = threatsToProcess.every((t) => t.actionTaken == 'DIBERSIHKAN');
      final updatedLog = log.copyWith(
        isSafe: allSuccess,
        title: allSuccess ? 'Pemindaian Selesai • Berkas Aman' : 'Pemindaian Selesai • Butuh Perhatian',
        description: allSuccess
            ? 'Semua berkas ancaman telah berhasil dimusnahkan secara permanen.'
            : 'Sebagian berkas memerlukan izin penyimpanan khusus untuk dihapus.',
        threats: threatsToProcess,
      );
      await logRepository!.updateLog(updatedLog);
    }
  }

  /// Quarantines all threats into the encrypted sandbox vault
  Future<void> quarantineAllThreats(ActivityLog log, {List<ThreatDetailItem>? localThreats}) async {
    final threatsToProcess = (localThreats != null && localThreats.isNotEmpty)
        ? localThreats
        : (log.threats.isNotEmpty ? log.threats : <ThreatDetailItem>[]);

    for (final t in threatsToProcess) {
      try {
        final res = await _kspChannel.invokeMapMethod<String, dynamic>('quarantineFile', {
          'filePath': t.filePath,
          'threatName': t.virusName,
          'threatType': t.threatType,
          'severity': t.severity,
        });
        final success = res?['success'] as bool? ?? false;
        t.actionTaken = success ? 'DIKARANTINA' : 'GAGAL';
      } catch (e) {
        t.actionTaken = 'GAGAL';
        debugPrint('[ThreatManager] Native quarantine failed for ${t.filePath}: $e');
      }
    }
    await loadQuarantinedItems();
    notifyListeners();
    if (logRepository != null) {
      final allSuccess = threatsToProcess.every((t) => t.actionTaken == 'DIKARANTINA');
      final updatedLog = log.copyWith(
        isSafe: allSuccess,
        title: allSuccess ? 'Pemindaian Selesai • Berkas Aman' : 'Pemindaian Selesai • Butuh Perhatian',
        description: allSuccess
            ? 'Semua berkas ancaman telah diisolasi di Brankas Karantina.'
            : 'Sebagian berkas gagal diisolasi (periksa izin penyimpanan).',
        threats: threatsToProcess,
      );
      await logRepository!.updateLog(updatedLog);
    }
  }

  /// Clears a single threat natively
  Future<bool> clearThreat(ActivityLog log, ThreatDetailItem item) async {
    try {
      final res = await _kspChannel.invokeMapMethod<String, dynamic>('deleteThreatFile', {'filePath': item.filePath});
      final success = res?['success'] as bool? ?? false;
      item.actionTaken = success ? 'DIBERSIHKAN' : 'GAGAL';
      notifyListeners();
      if (logRepository != null) {
        await logRepository!.updateLog(log);
      }
      return success;
    } catch (_) {
      item.actionTaken = 'GAGAL';
      notifyListeners();
      return false;
    }
  }

  /// Quarantines a single threat natively
  Future<bool> quarantineThreat(ActivityLog log, ThreatDetailItem item) async {
    try {
      final res = await _kspChannel.invokeMapMethod<String, dynamic>('quarantineFile', {
        'filePath': item.filePath,
        'threatName': item.virusName,
        'threatType': item.threatType,
        'severity': item.severity,
      });
      final success = res?['success'] as bool? ?? false;
      item.actionTaken = success ? 'DIKARANTINA' : 'GAGAL';
      await loadQuarantinedItems();
      notifyListeners();
      if (logRepository != null) {
        await logRepository!.updateLog(log);
      }
      return success;
    } catch (_) {
      item.actionTaken = 'GAGAL';
      notifyListeners();
      return false;
    }
  }

  static List<ThreatDetailItem> generateFallbackThreats(int count) {
    return const [];
  }

  /// Automatically resolves or quarantines detected scan threats based on preferences.
  static Future<void> resolveScanThreats(List<ThreatDetailItem> threats) async {
    try {
      final prefs = await SharedPreferences.getInstance();
      final autoClear = prefs.getBool(keyAutoClearThreats) ?? false;
      final autoQuar = prefs.getBool(keyAutoQuarantine) ?? true;
      for (final t in threats) {
        if (autoClear) {
          final res = await _kspChannel.invokeMapMethod<String, dynamic>('deleteThreatFile', {'filePath': t.filePath});
          t.actionTaken = (res?['success'] as bool? ?? false) ? 'DIBERSIHKAN' : 'GAGAL';
        } else if (autoQuar) {
          final res = await _kspChannel.invokeMapMethod<String, dynamic>('quarantineFile', {
            'filePath': t.filePath, 'threatName': t.virusName, 'threatType': t.threatType, 'severity': t.severity,
          });
          t.actionTaken = (res?['success'] as bool? ?? false) ? 'DIKARANTINA' : 'AKTIF';
        } else {
          t.actionTaken = 'AKTIF';
        }
      }
    } catch (_) {}
  }
}
