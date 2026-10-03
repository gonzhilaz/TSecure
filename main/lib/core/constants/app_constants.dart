class AppConstants {
  AppConstants._();

  static const String appName = 'Telkomsel Secure';
  static const String appTagline = 'Perlindungan Menyeluruh Jaringan & Perangkat';
  static const String defaultBackendUrl = String.fromEnvironment(
    'BACKEND_URL',
    defaultValue: 'https://backend-i3wy.vercel.app',
  );

  // Storage Keys
  static const String keyMobileId = 'ts_mobile_id';
  static const String keyMsisdn = 'ts_user_msisdn';
  static const String keyIsLoggedIn = 'ts_is_logged_in';
  static const String keySessionToken = 'ts_session_token';
  static const String keySessionExpiry = 'ts_session_expiry';
  static const String keyKasperskyActivated = 'ts_kaspersky_activated';
  static const String keyKasperskyLicenseKey = 'ts_kaspersky_license_key';
  static const String keyLastActiveCheck = 'ts_last_active_check';
  static const String keyAutoUpdateDb = 'ts_auto_update_db';
  static const String keyBiometricLock = 'ts_biometric_lock';
  static const String keyPushAlerts = 'ts_push_alerts';

  // Support contacts
  static const String grapariSupport = '188';
  static const String veronikaUrl = 'https://tsel.id/veronika';
}
