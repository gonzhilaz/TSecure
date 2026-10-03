import 'package:flutter/services.dart';
import 'kaspersky_sdk_bridge.dart';

/// Enterprise Clipboard URL Guard for TelkomSecure.
/// Scans copied links (from SMS, WhatsApp, social media) for phishing & scam domains
/// via Kaspersky Security Network (KSN).
class ClipboardUrlGuardService {
  static String _lastCheckedUrl = '';
  static int _lastCheckedTimestamp = 0;

  static Future<Map<String, dynamic>?> checkClipboardUrl(KasperskySdkBridge sdk) async {
    if (!sdk.webFilter) return null;

    try {
      final data = await Clipboard.getData(Clipboard.kTextPlain);
      final text = data?.text?.trim();
      if (text == null || text.isEmpty) return null;

      final isUrl = text.startsWith('http://') ||
          text.startsWith('https://') ||
          (text.contains('.') && !text.contains(' ') && text.length > 5);

      if (!isUrl) return null;

      final now = DateTime.now().millisecondsSinceEpoch;
      if (text == _lastCheckedUrl && (now - _lastCheckedTimestamp) < 30000) {
        return null;
      }

      _lastCheckedUrl = text;
      _lastCheckedTimestamp = now;

      final formattedUrl = (text.startsWith('http://') || text.startsWith('https://'))
          ? text
          : 'https://$text';

      final res = await sdk.checkUrl(formattedUrl);
      return res;
    } catch (_) {
      return null;
    }
  }
}
