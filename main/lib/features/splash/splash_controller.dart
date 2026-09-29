import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../../core/constants/app_constants.dart';
import '../../data/services/kaspersky_sdk_bridge.dart';
import '../../data/services/mobile_id_service.dart';
import '../../data/services/telkomsel_backend_service.dart';

enum SplashStatus { initializing, checkingBackend, completed, needsLogin }

class SplashController extends ChangeNotifier {
  final TelkomselBackendService backendService;
  final KasperskySdkBridge kasperskySdk;

  SplashStatus _status = SplashStatus.initializing;
  String _statusMessage = 'Preparing...';
  double _progress = 0.15;
  String? _mobileId;

  SplashController({
    required this.backendService,
    required this.kasperskySdk,
  });

  SplashStatus get status => _status;
  String get statusMessage => _statusMessage;
  double get progress => _progress;
  String? get mobileId => _mobileId;

  Future<void> runInitialization() async {
    _status = SplashStatus.initializing;
    _statusMessage = 'Preparing...';
    _progress = 0.20;
    notifyListeners();

    await Future.delayed(const Duration(milliseconds: 650));

    // Step 1: Provision or retrieve persistent Mobile ID
    _statusMessage = 'System Integrity Checking...';
    _progress = 0.45;
    notifyListeners();

    _mobileId = await MobileIdService.getOrCreateMobileId();
    await Future.delayed(const Duration(milliseconds: 650));

    final prefs = await SharedPreferences.getInstance();
    final savedMsisdn = prefs.getString(AppConstants.keyMsisdn);
    final isLoggedIn = prefs.getBool(AppConstants.keyIsLoggedIn) ?? false;
    final expiryStr = prefs.getString(AppConstants.keySessionExpiry);

    bool isSessionExpired = false;
    if (expiryStr != null) {
      final expiry = DateTime.tryParse(expiryStr);
      if (expiry != null && DateTime.now().isAfter(expiry)) {
        isSessionExpired = true;
      }
    }

    if (!isLoggedIn || savedMsisdn == null || savedMsisdn.isEmpty || isSessionExpired) {
      _statusMessage = 'Authenticating...';
      _progress = 1.0;
      notifyListeners();
      await Future.delayed(const Duration(milliseconds: 700));
      _status = SplashStatus.needsLogin;
      notifyListeners();
      return;
    }

    // Step 2: Cek Masa Aktif (Validation) from Backend
    _status = SplashStatus.checkingBackend;
    _statusMessage = 'Validating...';
    _progress = 0.75;
    notifyListeners();

    await Future.delayed(const Duration(milliseconds: 700));

    try {
      final activePeriod = await backendService.checkActivePeriod(
        msisdn: savedMsisdn,
        mobileId: _mobileId!,
      );

      // Step 3: Initialize Kaspersky SDK only if activePeriod is valid
      if (activePeriod.isValid && !activePeriod.isExpired) {
        _statusMessage = 'Initializing...';
        _progress = 0.90;
        notifyListeners();

        await kasperskySdk.initKasperskySdk(
          mobileId: _mobileId!,
          hasActivePeriod: true,
        );
      }

      _statusMessage = 'Protection Ready!';
      _progress = 1.0;
      notifyListeners();
      await Future.delayed(const Duration(milliseconds: 500));

      _status = SplashStatus.completed;
      notifyListeners();
    } catch (e) {
      _statusMessage = 'Protected Mode is Active.';
      _progress = 1.0;
      _status = SplashStatus.completed;
      notifyListeners();
    }
  }
}
