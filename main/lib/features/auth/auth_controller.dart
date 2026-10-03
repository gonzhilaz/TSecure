import 'dart:async';
import 'package:flutter/foundation.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../../core/constants/app_constants.dart';
import '../../data/models/active_period.dart';
import '../../data/models/user_session.dart';
import '../../data/services/kaspersky_sdk_bridge.dart';
import '../../data/services/mobile_id_service.dart';
import '../../data/services/telkomsel_backend_service.dart';

class AuthController extends ChangeNotifier {
  final TelkomselBackendService backendService;
  final KasperskySdkBridge kasperskySdk;

  bool _isAgreementChecked = true;
  bool _isLoading = false;
  String? _errorMessage;
  String? _pendingMsisdn;
  String? _lastMockOtp;
  int _resendCountdown = 0;
  Timer? _countdownTimer;
  ActivePeriod? _activePeriod;
  UserSession? _userSession;
  bool _needsActivation = false;
  String _activationStatus = 'ACTIVATED';
  String? _activationMessage;

  AuthController({
    required this.backendService,
    required this.kasperskySdk,
  });

  bool get isAgreementChecked => _isAgreementChecked;
  bool get isLoading => _isLoading;
  String? get errorMessage => _errorMessage;
  String? get pendingMsisdn => _pendingMsisdn;
  String? get lastMockOtp => _lastMockOtp;
  int get resendCountdown => _resendCountdown;
  ActivePeriod? get activePeriod => _activePeriod;
  UserSession? get userSession => _userSession;
  bool get needsActivation => _needsActivation;
  String get activationStatus => _activationStatus;
  String? get activationMessage => _activationMessage;

  void toggleAgreement(bool? value) {
    _isAgreementChecked = value ?? false;
    notifyListeners();
  }

  void clearError() {
    _errorMessage = null;
    notifyListeners();
  }

  String formatMsisdn(String rawPhone) {
    var clean = rawPhone.trim().replaceAll(RegExp(r'[\s\-]'), '');
    if (clean.startsWith('+62')) clean = clean.substring(3);
    if (clean.startsWith('62')) clean = clean.substring(2);
    while (clean.startsWith('0')) {
      clean = clean.substring(1);
    }
    return '+62$clean';
  }

  /// Step 1: Request SMS OTP and validate active subscription
  Future<bool> requestOtp(String rawPhone) async {
    final cleanPhone = rawPhone.trim().replaceAll(RegExp(r'[\s\-]'), '');
    if (cleanPhone.isEmpty) {
      _errorMessage = 'Nomor ponsel MyTelkomsel wajib diisi.';
      notifyListeners();
      return false;
    }

    if (!_isAgreementChecked) {
      _errorMessage = 'Harap setujui Ketentuan Layanan & Kebijakan Privasi.';
      notifyListeners();
      return false;
    }

    _isLoading = true;
    _errorMessage = null;
    notifyListeners();

    try {
      final mobileId = await MobileIdService.getOrCreateMobileId();
      final fullMsisdn = formatMsisdn(cleanPhone);
      _pendingMsisdn = fullMsisdn;

      final res = await backendService.requestOtp(
        msisdn: fullMsisdn,
        mobileId: mobileId,
      );

      if (res['success'] != true) {
        _errorMessage = res['error'] ?? 'Gagal meminta kode OTP.';
        _isLoading = false;
        notifyListeners();
        return false;
      }

      _lastMockOtp = kDebugMode ? (res['mock_otp'] ?? '123456') : null;
      _startResendTimer(res['resend_timeout_seconds'] ?? 60);

      _isLoading = false;
      notifyListeners();
      return true;
    } catch (e) {
      _errorMessage = 'Gagal memproses autentikasi jaringan: $e';
      _isLoading = false;
      notifyListeners();
      return false;
    }
  }

  /// Step 2: Verify SMS OTP and establish 30-day persistent session
  Future<bool> verifyOtp(String otpCode) async {
    if (_pendingMsisdn == null || _pendingMsisdn!.isEmpty) {
      _errorMessage = 'Sesi verifikasi tidak valid. Masukkan nomor kembali.';
      notifyListeners();
      return false;
    }

    if (otpCode.length < 6) {
      _errorMessage = 'Kode OTP harus terdiri dari 6 digit.';
      notifyListeners();
      return false;
    }

    _isLoading = true;
    _errorMessage = null;
    notifyListeners();

    try {
      final mobileId = await MobileIdService.getOrCreateMobileId();
      final res = await backendService.verifyOtp(
        msisdn: _pendingMsisdn!,
        mobileId: mobileId,
        otpCode: otpCode,
      );

      if (res['success'] != true) {
        _errorMessage = res['error'] ?? 'Kode OTP salah atau tidak valid.';
        _isLoading = false;
        notifyListeners();
        return false;
      }

      final sessionToken = res['session_token'] as String? ?? 'TSEL-SESSION-${DateTime.now().millisecondsSinceEpoch}';

      // 1. Cek Masa Aktif (Validation)
      final period = await backendService.checkActivePeriod(
        msisdn: _pendingMsisdn!,
        mobileId: mobileId,
      );
      _activePeriod = period;

      final bool needsActivation = res['needs_activation'] == true ||
          period.isPendingProvisioning ||
          period.activationStatus != 'ACTIVATED';
      _needsActivation = needsActivation;
      _activationStatus = period.activationStatus;

      // 2. Fetch User Profile
      _userSession = await backendService.fetchUserProfile(
        msisdn: _pendingMsisdn!,
        mobileId: mobileId,
      );

      // 3. Initialize Kaspersky SDK if already activated
      if (!needsActivation && period.isValid && !period.isExpired && !period.isPendingActivation) {
        await kasperskySdk.initKasperskySdk(
          mobileId: mobileId,
          hasActivePeriod: true,
          expiryDate: period.expiryDate,
          licenseKey: period.licenseKey,
        );
      } else {
        kasperskySdk.deactivateSdk(
          reason: period.isPendingActivation
              ? 'Perangkat belum diaktivasi di NDP'
              : 'Masa aktif Telkomsel habis di NDP',
        );
      }

      // 4. Save persistent session (MyTelkomsel 30-day session)
      final prefs = await SharedPreferences.getInstance();
      await prefs.setString(AppConstants.keyMsisdn, _pendingMsisdn!);
      await prefs.setString(AppConstants.keySessionToken, sessionToken);
      await prefs.setString(
        AppConstants.keySessionExpiry,
        DateTime.now().add(const Duration(days: 30)).toIso8601String(),
      );
      await prefs.setBool(AppConstants.keyIsLoggedIn, true);

      _countdownTimer?.cancel();
      _isLoading = false;
      notifyListeners();
      return true;
    } catch (e) {
      _errorMessage = 'Verifikasi OTP gagal: $e';
      _isLoading = false;
      notifyListeners();
      return false;
    }
  }

  /// Activates or synchronizes Kaspersky B2B license
  Future<Map<String, dynamic>> activateLicense({
    bool simulateKspOutage = false,
    bool simulatePendingNdp = false,
  }) async {
    final msisdn = _pendingMsisdn ?? _userSession?.msisdn;
    if (msisdn == null || msisdn.isEmpty) {
      return {'success': false, 'status_message': 'Nomor ponsel tidak ditemukan'};
    }

    _isLoading = true;
    _errorMessage = null;
    notifyListeners();

    try {
      final mobileId = await MobileIdService.getOrCreateMobileId();
      final res = await backendService.activateLicense(
        msisdn: msisdn,
        mobileId: mobileId,
        simulateKspOutage: simulateKspOutage,
        simulatePendingNdp: simulatePendingNdp,
      );

      final status = res['activation_status'] as String? ?? 'ACTIVATED';
      _activationStatus = status;
      _activationMessage = res['status_message'] as String? ?? '';

      if (status == 'ACTIVATED') {
        _needsActivation = false;
        final period = await backendService.checkActivePeriod(
          msisdn: msisdn,
          mobileId: mobileId,
        );
        _activePeriod = period;
        final String resolvedKey = (period.licenseKey.isNotEmpty)
            ? period.licenseKey
            : (res['license_key'] as String? ?? '');

        await kasperskySdk.initKasperskySdk(
          mobileId: mobileId,
          hasActivePeriod: true,
          expiryDate: period.expiryDate,
          licenseKey: resolvedKey,
        );
      } else if (status == 'ACTIVATION_PENDING_KSP') {
        _needsActivation = false;
        if (_activePeriod != null) {
          _activePeriod = ActivePeriod(
            packageName: _activePeriod!.packageName,
            packageDescription: _activePeriod!.packageDescription,
            expiryDate: _activePeriod!.expiryDate,
            activeDeviceCount: _activePeriod!.activeDeviceCount,
            maxDeviceAllowed: _activePeriod!.maxDeviceAllowed,
            isValid: true,
            statusMessage: 'Sinkronisasi sedang berjalan',
            activationStatus: 'ACTIVATION_PENDING_KSP',
          );
        }
      }

      _isLoading = false;
      notifyListeners();
      return res;
    } catch (e) {
      _isLoading = false;
      _errorMessage = 'Aktivasi lisensi gagal: $e';
      notifyListeners();
      return {'success': false, 'status_message': 'Gagal menghubungi server: $e'};
    }
  }

  /// Resends SMS OTP code
  Future<bool> resendOtp() async {
    if (_pendingMsisdn == null || _resendCountdown > 0) return false;
    return requestOtp(_pendingMsisdn!);
  }

  void _startResendTimer(int seconds) {
    _countdownTimer?.cancel();
    _resendCountdown = seconds;
    notifyListeners();

    _countdownTimer = Timer.periodic(const Duration(seconds: 1), (timer) {
      if (_resendCountdown <= 1) {
        _resendCountdown = 0;
        timer.cancel();
      } else {
        _resendCountdown--;
      }
      notifyListeners();
    });
  }

  @override
  void dispose() {
    _countdownTimer?.cancel();
    super.dispose();
  }
}
