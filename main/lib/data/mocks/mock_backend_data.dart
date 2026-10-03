import '../models/active_period.dart';
import '../models/user_session.dart';

class MockBackendData {
  static UserSession defaultUserSession(String mobileId) {
    return UserSession(
      msisdn: '',
      name: '',
      tier: 'Pelanggan Telkomsel',
      location: 'Indonesia',
      mobileId: mobileId,
      isKasperskyInitialized: false,
    );
  }

  static ActivePeriod activeSubscription() {
    return ActivePeriod(
      packageName: 'Mobile Security Ultimate',
      packageDescription: 'Lisensi Korporasi & Perlindungan Data',
      expiryDate: DateTime.now().add(const Duration(days: 20)),
      activeDeviceCount: 1,
      maxDeviceAllowed: 1,
      isValid: true,
      statusMessage: 'Perpanjangan mengikuti paket My Telkomsel',
    );
  }

  static ActivePeriod expiredSubscription() {
    return ActivePeriod(
      packageName: 'Mobile Security Ultimate',
      packageDescription: 'Lisensi Korporasi & Perlindungan Data',
      expiryDate: DateTime.now().subtract(const Duration(days: 2)),
      activeDeviceCount: 1,
      maxDeviceAllowed: 1,
      isValid: false,
      statusMessage: 'Masa aktif paket telah berakhir. Silakan perpanjang di My Telkomsel.',
    );
  }
}
