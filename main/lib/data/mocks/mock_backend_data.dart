import '../models/active_period.dart';
import '../models/user_session.dart';

class MockBackendData {
  static UserSession defaultUserSession(String mobileId) {
    return UserSession(
      msisdn: '+62 812-3456-7890',
      name: 'R. Aryandi',
      tier: 'Telkomsel Halo Diamond',
      location: 'Bandung, Jawa Barat',
      mobileId: mobileId,
      isKasperskyInitialized: false,
    );
  }

  static ActivePeriod activeSubscription() {
    return ActivePeriod(
      packageName: 'Mobile Security Ultimate (11GB)',
      packageDescription: 'Lisensi Korporasi & Perlindungan Data',
      expiryDate: DateTime.now().add(const Duration(days: 20)),
      activeDeviceCount: 1,
      maxDeviceAllowed: 1,
      isValid: true,
      statusMessage: 'Perpanjangan mengikuti paket MyTelkomsel',
    );
  }

  static ActivePeriod expiredSubscription() {
    return ActivePeriod(
      packageName: 'Mobile Security Ultimate (11GB)',
      packageDescription: 'Lisensi Korporasi & Perlindungan Data',
      expiryDate: DateTime.now().subtract(const Duration(days: 2)),
      activeDeviceCount: 1,
      maxDeviceAllowed: 1,
      isValid: false,
      statusMessage: 'Masa aktif paket telah berakhir. Silakan perpanjang di MyTelkomsel.',
    );
  }
}
