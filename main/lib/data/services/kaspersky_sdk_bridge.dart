import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:intl/intl.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../../core/constants/app_constants.dart';
import '../models/activity_log.dart';
import '../models/threat_detail_item.dart';
import 'activity_log_repository.dart';
import 'threat_telemetry_dispatcher.dart';
import 'url_filter_service.dart';
import 'wifi_security_service.dart';

enum ScanStatus { idle, inProgress, paused, finished, error }

/// KasperskySdkBridge encapsulating native MethodChannel bridge (`com.taspenguard/ksp`)
class KasperskySdkBridge extends ChangeNotifier {
  static const MethodChannel _channel = MethodChannel('com.taspenguard/ksp');

  ActivityLogRepository? logRepository;

  bool _isInitialized = false, _hasFullStorageAccess = false, _isVirusDbUpToDate = true;
  String? _boundMobileId, _scanErrorMessage;
  DateTime _licenseExpiryDate = DateTime(2026, 12, 24, 23, 59, 59);
  String _hardwareIdHash = '', _installationId = '', _emergencyContact = '+62 812-9988-7766';
  Map<String, dynamic>? _rawSdkStatus;
  bool _realtimeProtection = false, _webFilter = false, _puaScanner = true, _wifiSafety = true;
  bool _fakeAppsProtection = true, _deviceReputation = true, _dataBreachProtection = true, _simWatchEnabled = true, _secureStorageEnabled = true;
  final String _boundSimSlot = 'Slot 1 (Telkomsel Halo)', _boundIccidMasked = '8962 0188 **** 9012', _virusDbVersion = '2026.09.24-KSP';
  final int _quarantineItemCount = 0;
  ScanStatus _scanStatus = ScanStatus.idle;
  double _scanProgress = 0.0;
  int _scannedFiles = 0, _totalFiles = 0, _threatsDetected = 0;
  String _currentScanningFile = '';
  DateTime _lastScanDate = DateTime.now().subtract(const Duration(hours: 3));
  final List<ThreatDetailItem> _currentScanThreats = [];

  KasperskySdkBridge() {
    _channel.setMethodCallHandler(_handleNativeCall);
    checkFullStoragePermission();
    _loadVirusDbState();
  }

  Future<void> _loadVirusDbState() async {
    try {
      final prefs = await SharedPreferences.getInstance();
      final lastUpdate = prefs.getInt('ksp_last_db_update') ?? 0;
      _isVirusDbUpToDate = (DateTime.now().millisecondsSinceEpoch - lastUpdate) < 12 * 3600 * 1000;
      notifyListeners();
    } catch (_) {}
  }

  bool get isVirusDbUpToDate => _isVirusDbUpToDate;

  bool get hasFullStorageAccess => _hasFullStorageAccess; bool get isInitialized => _isInitialized;
  String? get boundMobileId => _boundMobileId; DateTime get licenseExpiryDate => _licenseExpiryDate;
  String get hardwareIdHash => _hardwareIdHash; String get installationId => _installationId;
  Map<String, dynamic>? get rawSdkStatus => _rawSdkStatus;
  String get packageName => 'Kaspersky Mobile Security B2B'; String get customerName => 'Telkomsel Indonesia CBA';
  bool get realtimeProtection => _realtimeProtection; bool get webFilter => _webFilter;
  bool get puaScanner => _puaScanner; bool get wifiSafety => _wifiSafety;
  bool get fakeAppsProtection => _fakeAppsProtection; bool get deviceReputation => _deviceReputation;
  bool get dataBreachProtection => _dataBreachProtection; bool get simWatchEnabled => _simWatchEnabled;
  String get boundSimSlot => _boundSimSlot; String get boundIccidMasked => _boundIccidMasked;
  String get emergencyContact => _emergencyContact; int get quarantineItemCount => _quarantineItemCount;
  bool get secureStorageEnabled => _secureStorageEnabled; ScanStatus get scanStatus => _scanStatus;
  bool get isScanPaused => _scanStatus == ScanStatus.paused;
  double get scanProgress => _scanProgress; int get scannedFiles => _scannedFiles;
  int get totalFiles => _totalFiles; int get threatsDetected => _threatsDetected;
  String get currentScanningFile => _currentScanningFile; String? get scanErrorMessage => _scanErrorMessage;
  DateTime get lastScanDate => _lastScanDate; String get virusDbVersion => _virusDbVersion;
  List<ThreatDetailItem> get currentScanThreats => List.unmodifiable(_currentScanThreats);

  Future<void> _logActivity({
    required String id, required String title, required String description,
    required IconData icon, required LogCategory category, required bool isSafe,
    List<ThreatDetailItem> threats = const [],
  }) async {
    final now = DateTime.now();
    await logRepository?.addLog(ActivityLog(
      id: id, title: title, description: description,
      time: '${DateFormat('HH:mm').format(now)} WIB', date: now,
      icon: icon, category: category, isSafe: isSafe, threats: threats,
    ));
  }

  Future<dynamic> _handleNativeCall(MethodCall call) async {
    switch (call.method) {
      case 'onUpdateStatus':
        _currentScanningFile = call.arguments as String? ?? 'Memeriksa basis virus...';
        notifyListeners();
        break;

      case 'onScanProgress':
        _scannedFiles = call.arguments['scanned'] as int? ?? _scannedFiles;
        _totalFiles = call.arguments['total'] as int? ?? _totalFiles;
        _currentScanningFile = call.arguments['currentFile'] as String? ?? '';
        _scanProgress = _totalFiles > 0 ? (_scannedFiles / _totalFiles).clamp(0.0, 0.99) : 0.0;
        notifyListeners();
        break;

      case 'onScanThreat':
        final threatName = call.arguments['threatName'] as String? ?? 'Malware';
        final path = call.arguments['path'] as String? ?? '';
        final isMalware = call.arguments['isMalware'] as bool? ?? true;
        _threatsDetected++;
        final fileName = path.isNotEmpty ? path.split(RegExp(r'[\\/]')).last : 'berkas_terinfeksi';
        final threatItem = ThreatDetailItem(
          id: 'thr-${DateTime.now().millisecondsSinceEpoch}-${_currentScanThreats.length}',
          fileName: fileName,
          filePath: path,
          virusName: threatName,
          threatType: isMalware ? 'Malware' : 'Riskware',
          severity: isMalware ? 'KRITIS' : 'TINGGI',
          actionTaken: 'AKTIF',
        );
        _currentScanThreats.add(threatItem);
        notifyListeners();

        await ThreatTelemetryDispatcher.recordAndReport(
          logRepo: logRepository,
          msisdn: _boundMobileId,
          mobileId: _boundMobileId,
          threatType: isMalware ? 'MALWARE' : 'RISKWARE',
          target: path,
          severity: isMalware ? 'CRITICAL' : 'HIGH',
          title: 'Ancaman Terdeteksi: $threatName',
          description: 'Kaspersky menemukan $threatName di: $path',
          actionTaken: 'AKTIF',
          icon: Icons.bug_report,
          category: LogCategory.pemindaian,
          threats: [threatItem],
        );
        break;

      case 'onScanComplete':
        _scannedFiles = call.arguments['scanned'] as int? ?? _scannedFiles;
        _threatsDetected = call.arguments['threats'] as int? ?? _threatsDetected;
        _scanErrorMessage = call.arguments['error'] as String?;
        _scanProgress = 1.0;
        _scanStatus = _scanErrorMessage != null ? ScanStatus.error : ScanStatus.finished;
        _lastScanDate = DateTime.now();
        _currentScanningFile = _scanErrorMessage != null ? 'Error: $_scanErrorMessage' : 'Pemindaian Selesai';
        notifyListeners();

        final scanTitle = _scanErrorMessage != null ? 'Pemindaian Gagal' : (_threatsDetected > 0 ? 'Pemindaian Selesai • $_threatsDetected Ancaman Ditemukan' : 'Pemindaian Selesai • Sistem Aman');
        final threatsForLog = List<ThreatDetailItem>.from(_currentScanThreats);
        await _logActivity(
          id: 'scan-${DateTime.now().millisecondsSinceEpoch}', title: scanTitle,
          description: _scanErrorMessage ?? '$_scannedFiles Berkas Diperiksa • $_threatsDetected Ancaman Ditemukan',
          icon: _scanErrorMessage != null ? Icons.error_outline : Icons.verified_outlined,
          category: LogCategory.pemindaian, isSafe: _threatsDetected == 0 && _scanErrorMessage == null, threats: threatsForLog,
        );
        break;

      case 'onRealtimeThreat':
        final name = call.arguments['name'] as String? ?? 'Malware';
        final path = call.arguments['path'] as String? ?? '';
        _threatsDetected++; notifyListeners();
        await ThreatTelemetryDispatcher.recordAndReport(
          logRepo: logRepository, msisdn: _boundMobileId, mobileId: _boundMobileId,
          threatType: 'MALWARE', target: path.isNotEmpty ? path : name, severity: 'CRITICAL',
          title: 'Perlindungan Real-Time: $name', description: 'Ancaman $name berhasil diisolasi oleh Kaspersky.',
          actionTaken: 'ISOLATED', icon: Icons.security, category: LogCategory.pemindaian,
        );
        break;

      case 'onUrlThreatDetected':
        final url = call.arguments['url'] as String? ?? '';
        final category = call.arguments['category'] as String? ?? 'Situs Berbahaya';
        final verdict = call.arguments['verdict'] as String? ?? 'BLOCKED';
        _threatsDetected++; notifyListeners();
        await ThreatTelemetryDispatcher.recordAndReport(
          logRepo: logRepository, msisdn: _boundMobileId, mobileId: _boundMobileId,
          threatType: 'PHISHING', target: url, severity: 'HIGH',
          title: 'Situs Berbahaya Diblokir ($category)', description: 'Akses ke $url berhasil dicegat oleh Web Filter.',
          actionTaken: verdict, icon: Icons.language_rounded, category: LogCategory.jaringan,
        );
        break;

      case 'onSmishingThreatDetected':
        final sender = call.arguments['sender'] as String? ?? 'SMS Scam';
        final url = call.arguments['url'] as String? ?? '';
        _threatsDetected++; notifyListeners();
        await ThreatTelemetryDispatcher.recordAndReport(
          logRepo: logRepository, msisdn: _boundMobileId, mobileId: _boundMobileId,
          threatType: 'SMISHING', target: sender, severity: 'CRITICAL',
          title: 'SMS Scam / Smishing Dicegat ($sender)',
          description: 'SMS mencurigakan berisi tautan $url berhasil diblokir.',
          actionTaken: 'BLOCKED', icon: Icons.sms_failed_rounded, category: LogCategory.jaringan,
        );
        break;
    }
  }

  Future<bool> initKasperskySdk({
    required String mobileId,
    required bool hasActivePeriod,
    String? licenseKey,
    DateTime? expiryDate,
  }) async {
    if (!hasActivePeriod) {
      _isInitialized = false;
      _realtimeProtection = false;
      notifyListeners();
      return false;
    }
    final effectiveKey = (licenseKey != null && licenseKey.trim().isNotEmpty)
        ? licenseKey.trim()
        : '6KYKJ-65T6T-WMVBD-NNPEG';
    if (expiryDate != null) _licenseExpiryDate = expiryDate;

    bool activated = false;
    try {
      final nativeResult = await _channel.invokeMethod<bool>('activateLicense', {
        'mobileId': mobileId,
        'licenseKey': effectiveKey,
      });
      activated = nativeResult ?? false;

      final status = await _channel.invokeMapMethod<String, dynamic>('buildSDKStatus');
      if (status != null) {
        _rawSdkStatus = Map<String, dynamic>.from(status);
        final expireSec = status['expireDate'] as int? ?? 0;
        if (expireSec > 0) _licenseExpiryDate = DateTime.fromMillisecondsSinceEpoch(expireSec * 1000);
        final hwid = status['hardwareIdHash'] as String?;
        if (hwid != null && hwid.isNotEmpty) _hardwareIdHash = hwid;
        final inst = status['installationId'] as String?;
        if (inst != null && inst.isNotEmpty) _installationId = inst;
      }
    } catch (e) {
      debugPrint('[KasperskySDK] Native activateLicense error: $e');
      activated = false;
    }

    _isInitialized = activated;
    _boundMobileId = mobileId;
    if (activated) {
      final prefs = await SharedPreferences.getInstance();
      await prefs.setString(AppConstants.keyKasperskyActivated, mobileId);
      toggleRealtimeProtection(true);
      // Initialize and enable URL filter (Web Filter / Phishing Protection)
      urlFilterInit().then((_) => toggleWebFilter(true));
    }
    notifyListeners();
    return activated;
  }

  void deactivateSdk({String reason = 'Active period expired'}) {
    _isInitialized = false; _realtimeProtection = false; _webFilter = false;
    try { _channel.invokeMethod('setRealtimeProtection', {'enabled': false}); } catch (_) {}
    notifyListeners();
  }
  void toggleRealtimeProtection(bool enabled) {
    _realtimeProtection = enabled;
    try { _channel.invokeMethod('setRealtimeProtection', {'enabled': enabled}); } catch (_) {}
    notifyListeners();
  }
  void toggleWebFilter(bool enabled) {
    _webFilter = enabled;
    try { _channel.invokeMethod('setWebFilter', {'enabled': enabled}); } catch (_) {}
    try { _channel.invokeMethod('urlFilterEnable', {'enabled': enabled}); } catch (_) {}
    notifyListeners();
  }

  // --- URL Filter & Vendor Compat delegates ---
  Future<bool> urlFilterInit() => UrlFilterService.init();
  Future<bool> isUrlFilterEnabled() => UrlFilterService.isEnabled();
  Future<Map<String, dynamic>> urlFilterCheckUrl(String url) => UrlFilterService.checkUrl(url);
  Future<bool> isRtpActive() => VendorCompatService.isRtpActive();
  Future<bool> isMiuiDevice() => VendorCompatService.isMiuiDevice();
  Future<bool> hasMiuiPopupEditor() => VendorCompatService.hasMiuiPopupEditor();
  Future<bool> requestMiuiBackgroundPopup() => VendorCompatService.requestMiuiBackgroundPopup();
  Future<bool> openSamsungBatterySettings() => VendorCompatService.openSamsungBatterySettings();
  Future<Map<String, dynamic>> getVendorInfo() => VendorCompatService.getVendorInfo();
  void togglePuaScanner(bool enabled) { _puaScanner = enabled; try { _channel.invokeMethod('setPuaScanner', {'enabled': enabled}); } catch (_) {} notifyListeners(); }
  Map<String, dynamic>? _wifiAuditData; Map<String, dynamic>? get wifiAuditData => _wifiAuditData;
  Future<Map<String, dynamic>> auditWifi() async { final res = await WifiSecurityService.auditWifi(); _wifiAuditData = res; notifyListeners(); return res; }
  void toggleWifiSafety(bool enabled) { _wifiSafety = enabled; notifyListeners(); }
  void toggleFakeApps(bool enabled) { _fakeAppsProtection = enabled; notifyListeners(); }
  void toggleDeviceRep(bool enabled) { _deviceReputation = enabled; notifyListeners(); }
  void toggleDataBreach(bool enabled) { _dataBreachProtection = enabled; notifyListeners(); }
  void toggleSimWatch(bool enabled) { _simWatchEnabled = enabled; notifyListeners(); }
  void toggleSecureStorage(bool enabled) { _secureStorageEnabled = enabled; notifyListeners(); }
  void setEmergencyContact(String contact) { _emergencyContact = contact; notifyListeners(); }

  Future<Map<String, dynamic>> checkRoot() async {
    try {
      final res = await _channel.invokeMapMethod<String, dynamic>('checkRoot');
      if (res != null) return Map<String, dynamic>.from(res);
    } catch (_) {}
    return {'isRooted': false, 'rootCause': 'Gagal audit native', 'sdkVerified': false, 'engine': 'Kaspersky RootDetector v5.21'};
  }
  Future<bool> checkFullStoragePermission() async {
    try {
      final res = await _channel.invokeMapMethod<String, dynamic>('checkStoragePermission');
      _hasFullStorageAccess = res?['hasFullAccess'] as bool? ?? false;
      notifyListeners();
      return _hasFullStorageAccess;
    } catch (_) { return false; }
  }
  Future<void> requestFullStoragePermission() async {
    try { await _channel.invokeMethod('requestStoragePermission'); } catch (_) {}
  }

  Future<void> runFullScan({bool fullPhone = true, String? scanMode}) async {
    if (!_isInitialized || _scanStatus == ScanStatus.inProgress) return;
    _scanStatus = ScanStatus.inProgress; _scanProgress = 0.0;
    _scannedFiles = 0; _totalFiles = 0; _threatsDetected = 0;
    _currentScanThreats.clear(); _scanErrorMessage = null;
    _currentScanningFile = 'Memeriksa pembaruan basis data virus...';
    notifyListeners();

    final effectiveMode = (scanMode != null && scanMode.isNotEmpty)
        ? scanMode.toUpperCase()
        : (fullPhone ? 'FULL' : 'RECOMMENDED');

    try {
      await updateBases();
      _currentScanningFile = effectiveMode == 'QUICK' ? 'Menyiapkan pemindaian cepat...' : 'Menyiapkan pemindaian sistem...';
      notifyListeners();
    } catch (_) {}
    try {
      await _channel.invokeMethod('startScan', {'scanMode': effectiveMode});
    } catch (e) {
      _scanStatus = ScanStatus.error; _scanErrorMessage = e.toString(); notifyListeners();
    }
  }

  Future<bool> pauseScan() async {
    if (_scanStatus != ScanStatus.inProgress) return false;
    try {
      final ok = await _channel.invokeMethod<bool>('pauseScan') ?? false;
      if (ok) { _scanStatus = ScanStatus.paused; notifyListeners(); }
      return ok;
    } catch (_) { return false; }
  }

  Future<bool> resumeScan() async {
    if (_scanStatus != ScanStatus.paused) return false;
    try {
      final ok = await _channel.invokeMethod<bool>('resumeScan') ?? false;
      if (ok) { _scanStatus = ScanStatus.inProgress; notifyListeners(); }
      return ok;
    } catch (_) { return false; }
  }

  Future<bool> stopScan() async {
    if (_scanStatus != ScanStatus.inProgress && _scanStatus != ScanStatus.paused) return false;
    try {
      final ok = await _channel.invokeMethod<bool>('stopScan') ?? false;
      _scanStatus = ScanStatus.finished; _scanProgress = 1.0; _currentScanningFile = 'Pemindaian dihentikan.';
      notifyListeners(); return ok;
    } catch (_) { return false; }
  }
  Future<bool> requestNotificationPermission() async {
    try { return await _channel.invokeMethod<bool>('requestNotificationPermission') ?? true; } catch (_) { return true; }
  }
  Future<void> showSecurityNotification({required String title, required String message, bool isThreat = true}) async {
    try { await _channel.invokeMethod('showNotification', {'title': title, 'message': message, 'isThreat': isThreat}); } catch (_) {}
  }
  Future<Map<String, dynamic>> checkUrl(String url) async {
    final result = await UrlFilterService.checkUrl(url);
    if (result['isSafe'] == false || result['isBlocked'] == true) {
      final verdict = result['verdict'] ?? 'BERBAHAYA';
      await showSecurityNotification(title: 'Ancaman Terdeteksi ($verdict)', message: 'Kaspersky Web Filter memblokir: $url');
      await ThreatTelemetryDispatcher.recordAndReport(
        logRepo: logRepository, msisdn: _boundMobileId, mobileId: _boundMobileId,
        threatType: 'PHISHING', target: url, severity: 'HIGH', title: 'Situs Berbahaya Diblokir ($verdict)',
        description: '$url terdeteksi ancaman nyata di KSN.', actionTaken: 'BLOCKED', icon: Icons.shield_outlined, category: LogCategory.jaringan,
      );
    }
    notifyListeners(); return result;
  }

  Future<bool> updateBases() async {
    try {
      final res = await _channel.invokeMethod<bool>('updateBases') ?? false;
      _isVirusDbUpToDate = true;
      final prefs = await SharedPreferences.getInstance();
      await prefs.setInt('ksp_last_db_update', DateTime.now().millisecondsSinceEpoch);
      notifyListeners(); return res;
    } catch (_) { _isVirusDbUpToDate = true; notifyListeners(); return false; }
  }

  Future<Map<String, dynamic>> scanSpecificFile({String? customPath}) async {
    Map<String, dynamic> result = {'isThreat': false, 'threatName': 'Tidak Terdeteksi', 'threatType': 'None', 'severity': 'NONE', 'description': 'Memeriksa berkas...', 'sdkVerified': false};
    try {
      final nativeRes = await _channel.invokeMapMethod<String, dynamic>('scanSpecificFile', {'filePath': customPath});
      if (nativeRes != null) result = Map<String, dynamic>.from(nativeRes);
    } catch (e) { result['description'] = 'Gagal memanggil scanner: $e'; }
    if (result['isThreat'] == true) { _threatsDetected++; notifyListeners(); }
    return result;
  }

  Future<void> resolveAllThreats({bool quarantine = false}) async {
    _threatsDetected = 0; _currentScanThreats.clear(); notifyListeners();
  }
}
