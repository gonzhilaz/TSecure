import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../../core/constants/app_constants.dart';
import '../../core/widgets/tri_state_view.dart';
import '../../data/mocks/mock_backend_data.dart';
import '../../data/models/active_period.dart';
import '../../data/models/user_session.dart';
import '../../data/services/kaspersky_sdk_bridge.dart';
import '../../data/services/mobile_id_service.dart';
import '../../data/services/telkomsel_backend_service.dart';

class ProfileController extends ChangeNotifier {
  final KasperskySdkBridge kasperskySdk;
  final TelkomselBackendService? backendService;

  ViewState _state = ViewState.success;
  UserSession? _userSession;
  ActivePeriod? _activePeriod;

  ProfileController({
    required this.kasperskySdk,
    this.backendService,
  }) {
    kasperskySdk.addListener(_onSdkUpdated);
    loadProfileData();
  }

  ViewState get state => _state;
  UserSession? get userSession => _userSession;
  ActivePeriod? get activePeriod => _activePeriod;

  void _onSdkUpdated() {
    notifyListeners();
  }

  void _fallbackSyncActivePeriod() {
    final inst = kasperskySdk.installationId;
    final shortId = inst.length >= 8 ? inst.substring(0, 8) : inst;
    _activePeriod = ActivePeriod(
      packageName: kasperskySdk.packageName,
      packageDescription:
          'Lisensi Korporasi & Perlindungan Data (${kasperskySdk.customerName})',
      expiryDate: kasperskySdk.licenseExpiryDate,
      activeDeviceCount: 1,
      maxDeviceAllowed: 1,
      isValid: kasperskySdk.isInitialized,
      statusMessage: 'Perangkat terikat: $shortId... (Aktif)',
    );
  }

  Future<void> loadProfileData() async {
    _state = ViewState.loading;
    notifyListeners();

    try {
      final mobileId = await MobileIdService.getOrCreateMobileId();
      final prefs = await SharedPreferences.getInstance();
      final msisdn = prefs.getString(AppConstants.keyMsisdn) ?? '';

      _userSession = MockBackendData.defaultUserSession(mobileId).copyWith(
        msisdn: msisdn,
      );

      if (backendService != null) {
        final period = await backendService!.checkActivePeriod(
          msisdn: msisdn,
          mobileId: mobileId,
        );
        _activePeriod = period;

        if (period.isPendingActivation) {
          // Scenario 1: Belum Aktif -> SDK harus dormant/deactivated
          kasperskySdk.deactivateSdk(
            reason: 'Perangkat belum melakukan aktivasi lisensi NDP',
          );
        } else if (!period.isValid || period.isExpired) {
          // Scenario 3: Masa Aktif Habis (Lisensi Ada) -> Policy Guard deactivates SDK
          kasperskySdk.deactivateSdk(
            reason: 'Masa aktif paket telah berakhir di NDP Telkomsel',
          );
        } else if (period.isActivated) {
          // Scenario 2: Masa Aktif Ada -> Initialize genuine native SDK
          await kasperskySdk.initKasperskySdk(
            mobileId: mobileId,
            hasActivePeriod: true,
            expiryDate: period.expiryDate,
            licenseKey: period.licenseKey,
          );
        }
      } else {
        _fallbackSyncActivePeriod();
      }

      _state = ViewState.success;
      notifyListeners();
    } catch (e) {
      _state = ViewState.error;
      notifyListeners();
    }
  }

  /// Sinkronkan Ulang / Prosedur Aktivasi lisensi Kaspersky B2B dari menu Profil
  Future<bool> retryActivation() async {
    _state = ViewState.loading;
    notifyListeners();

    try {
      final mobileId = await MobileIdService.getOrCreateMobileId();
      final prefs = await SharedPreferences.getInstance();
      final msisdn = prefs.getString(AppConstants.keyMsisdn) ?? '+62 812-9988-7766';

      if (backendService != null) {
        final res = await backendService!.activateLicense(
          msisdn: msisdn,
          mobileId: mobileId,
          simulateKspOutage: false,
          simulatePendingNdp: false,
        );

        if (res['success'] == true && res['activation_status'] == 'ACTIVATED') {
          final period = await backendService!.checkActivePeriod(
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
          _state = ViewState.success;
          notifyListeners();
          return true;
        }
      }

      await loadProfileData();
      return _activePeriod?.isActivated == true;
    } catch (e) {
      _state = ViewState.success;
      notifyListeners();
      return false;
    }
  }

  @override
  void dispose() {
    kasperskySdk.removeListener(_onSdkUpdated);
    super.dispose();
  }
}
