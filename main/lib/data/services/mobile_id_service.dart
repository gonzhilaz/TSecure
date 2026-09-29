import 'dart:math';
import 'package:shared_preferences/shared_preferences.dart';
import '../../core/constants/app_constants.dart';

class MobileIdService {
  static String? _cachedMobileId;

  /// Retrieves or provisions a persistent Mobile ID unique to this device install.
  /// This ID is bound to the Kaspersky B2B license (+1 license billing guard).
  static Future<String> getOrCreateMobileId() async {
    if (_cachedMobileId != null) {
      return _cachedMobileId!;
    }

    final prefs = await SharedPreferences.getInstance();
    String? storedId = prefs.getString(AppConstants.keyMobileId);

    if (storedId == null || storedId.isEmpty) {
      final random = Random.secure();
      final values = List<int>.generate(8, (i) => random.nextInt(256));
      final hex = values.map((b) => b.toRadixString(16).padLeft(2, '0')).join();
      storedId = 'Mobile ID-$hex'.toUpperCase();
      await prefs.setString(AppConstants.keyMobileId, storedId);
    }

    _cachedMobileId = storedId;
    return storedId;
  }
}
