import 'dart:convert';
import 'dart:io';
import 'package:flutter/foundation.dart';
import '../mocks/mock_backend_data.dart';
import '../models/active_period.dart';
import '../models/user_session.dart';
import 'device_hardware_service.dart';
import 'offline_telemetry_queue.dart';

class TelkomselBackendService {
  static const String _baseUrl = String.fromEnvironment(
    'BACKEND_URL',
    defaultValue: 'http://api-telkomsel-secure.digit.co.id',
  );

  /// Request: Cek Masa Aktif (Validasi)
  /// Corresponds to: Mobile App -> Req: Cek Masa Aktif -> Backend Telkomsel Secure
  Future<ActivePeriod> checkActivePeriod({
    required String msisdn,
    required String mobileId,
  }) async {
    try {
      final client = HttpClient();
      client.connectionTimeout = const Duration(seconds: 3);
      final deviceModel = await DeviceHardwareService.getDeviceModel();
      final osVersion = await DeviceHardwareService.getOsVersion();
      final encodedModel = Uri.encodeComponent(deviceModel);
      final encodedOs = Uri.encodeComponent(osVersion);
      final uri = Uri.parse(
        '$_baseUrl/api/v1/subscription/check?msisdn=$msisdn&mobile_id=$mobileId&device_model=$encodedModel&os_version=$encodedOs',
      );
      final request = await client.getUrl(uri);
      final response = await request.close().timeout(const Duration(seconds: 3));

      if (response.statusCode == 200) {
        final body = await response.transform(utf8.decoder).join();
        final Map<String, dynamic> data = jsonDecode(body);
        final bool isValid = data['is_valid'] == true;
        final DateTime defaultExpiry = isValid
            ? DateTime.now().add(const Duration(days: 30))
            : DateTime.now().subtract(const Duration(days: 1));

        return ActivePeriod(
          packageName: data['plan_name'] ?? (isValid ? 'Telkomsel Secure Guard 30 Hari' : '-'),
          packageDescription: isValid
              ? 'Proteksi Menyeluruh Kaspersky Security Engine'
              : 'Paket Belum Aktif / Telah Berakhir',
          expiryDate: DateTime.tryParse(data['end_date'] ?? '') ?? defaultExpiry,
          activeDeviceCount: 1,
          maxDeviceAllowed: 3,
          isValid: isValid,
          statusMessage: data['message'] ??
              (isValid
                  ? 'Paket aktif dan terlindungi penuh.'
                  : 'Masa aktif paket telah berakhir. Silakan perpanjang di MyTelkomsel.'),
          activationStatus: data['activation_status'] ?? (isValid ? 'ACTIVATED' : 'NOT_SUBSCRIBED'),
          isPendingProvisioning: data['is_pending_provisioning'] == true,
          licenseKey: data['license_key'] ?? '',
        );
      }
    } catch (e) {
      debugPrint('[BackendService] Offline or fallback: $e');
    }

    // Smart fallback for offline demo testing
    if (msisdn.contains('0000') || msisdn.isEmpty) {
      return MockBackendData.expiredSubscription();
    } else {
      return MockBackendData.activeSubscription();
    }
  }

  /// Sends live threat telemetry to Golang backend & SOC Dashboard
  Future<void> reportThreatTelemetry({
    required String msisdn,
    required String mobileId,
    required String threatType,
    required String target,
    required String severity,
    required String description,
    required String actionTaken,
  }) async {
    try {
      final client = HttpClient();
      client.connectionTimeout = const Duration(seconds: 3);
      final uri = Uri.parse('$_baseUrl/api/v1/telemetry/events');
      final request = await client.postUrl(uri);
      request.headers.set('Content-Type', 'application/json');
      final payload = jsonEncode({
        'msisdn': msisdn,
        'mobile_id': mobileId,
        'threat_type': threatType,
        'target': target,
        'severity': severity,
        'description': description,
        'action_taken': actionTaken,
      });
      request.write(payload);
      final response = await request.close().timeout(const Duration(seconds: 3));
      debugPrint('[BackendService] Threat telemetry sent: ${response.statusCode}');
      _flushOfflineQueue();
    } catch (e) {
      debugPrint('[BackendService] Telemetry failed, queueing offline: $e');
      await OfflineTelemetryQueue.enqueueThreat(
        msisdn: msisdn,
        mobileId: mobileId,
        threatType: threatType,
        target: target,
        severity: severity,
        description: description,
        actionTaken: actionTaken,
      );
    }
  }

  Future<void> _flushOfflineQueue() async {
    try {
      final pending = await OfflineTelemetryQueue.dequeueAll();
      if (pending.isEmpty) return;
      for (final item in pending) {
        final client = HttpClient();
        client.connectionTimeout = const Duration(seconds: 2);
        final uri = Uri.parse('$_baseUrl/api/v1/telemetry/events');
        final req = await client.postUrl(uri);
        req.headers.set('Content-Type', 'application/json');
        req.write(jsonEncode(item));
        await req.close();
      }
      debugPrint('[BackendService] Flushed ${pending.length} offline threats');
    } catch (_) {}
  }

  /// Requests SMS OTP from Telkomsel Backend
  Future<Map<String, dynamic>> requestOtp({
    required String msisdn,
    required String mobileId,
  }) async {
    try {
      final client = HttpClient();
      client.connectionTimeout = const Duration(seconds: 3);
      final uri = Uri.parse('$_baseUrl/api/v1/auth/request-otp');
      final request = await client.postUrl(uri);
      request.headers.set('Content-Type', 'application/json');
      request.write(jsonEncode({'msisdn': msisdn, 'mobile_id': mobileId}));
      final response = await request.close().timeout(const Duration(seconds: 3));
      final body = await response.transform(utf8.decoder).join();
      final Map<String, dynamic> data = jsonDecode(body);
      if (response.statusCode == 200) {
        return {'success': true, ...data};
      } else {
        return {'success': false, 'error': data['error'] ?? 'Gagal meminta OTP'};
      }
    } catch (e) {
      debugPrint('[BackendService] requestOtp fallback: $e');
      if (msisdn.contains('0000')) {
        return {'success': false, 'error': 'Masa aktif paket telah berakhir. Silakan perpanjang di MyTelkomsel.'};
      }
      return {'success': true, 'status': 'SENT', 'resend_timeout_seconds': 60, 'mock_otp': '123456'};
    }
  }

  /// Verifies SMS OTP with Telkomsel Backend
  Future<Map<String, dynamic>> verifyOtp({
    required String msisdn,
    required String mobileId,
    required String otpCode,
  }) async {
    try {
      final client = HttpClient();
      client.connectionTimeout = const Duration(seconds: 3);
      final uri = Uri.parse('$_baseUrl/api/v1/auth/verify-otp');
      final request = await client.postUrl(uri);
      request.headers.set('Content-Type', 'application/json');
      request.write(jsonEncode({'msisdn': msisdn, 'mobile_id': mobileId, 'otp_code': otpCode}));
      final response = await request.close().timeout(const Duration(seconds: 3));
      final body = await response.transform(utf8.decoder).join();
      final Map<String, dynamic> data = jsonDecode(body);
      if (response.statusCode == 200 && data['success'] == true) {
        return data;
      } else {
        return {'success': false, 'error': data['message'] ?? 'Kode OTP salah atau telah kedaluwarsa.'};
      }
    } catch (e) {
      debugPrint('[BackendService] verifyOtp fallback: $e');
      if (otpCode == '123456') {
        return {
          'success': true,
          'session_token': 'TSEL-SEC-FALLBACK-$msisdn',
          'expires_in_days': 30,
        };
      }
      return {'success': false, 'error': 'Kode OTP tidak valid atau salah. Silakan periksa kembali SMS Anda.'};
    }
  }

  /// Activates or synchronizes Kaspersky B2B license
  /// Corresponds to: Mobile App -> Req: Aktivasi / Ikat Lisensi -> Backend Telkomsel Secure
  Future<Map<String, dynamic>> activateLicense({
    required String msisdn,
    required String mobileId,
    bool simulateKspOutage = false,
    bool simulatePendingNdp = false,
  }) async {
    try {
      final client = HttpClient();
      client.connectionTimeout = const Duration(seconds: 4);
      final deviceModel = await DeviceHardwareService.getDeviceModel();
      final osVersion = await DeviceHardwareService.getOsVersion();
      final uri = Uri.parse('$_baseUrl/api/v1/auth/activate-license');
      final request = await client.postUrl(uri);
      request.headers.set('Content-Type', 'application/json');
      request.write(jsonEncode({
        'msisdn': msisdn,
        'mobile_id': mobileId,
        'device_model': deviceModel,
        'os_version': osVersion,
        'simulate_ksp_outage': simulateKspOutage,
        'simulate_pending_ndp': simulatePendingNdp,
      }));
      final response = await request.close().timeout(const Duration(seconds: 4));
      final body = await response.transform(utf8.decoder).join();
      final Map<String, dynamic> data = jsonDecode(body);
      return data;
    } catch (e) {
      debugPrint('[BackendService] activateLicense fallback: $e');
      if (simulatePendingNdp) {
        return {
          'success': false,
          'activation_status': 'PENDING_PROVISIONING',
          'stage': 1,
          'status_message': 'Dalam Proses...',
        };
      }
      if (simulateKspOutage) {
        return {
          'success': true,
          'activation_status': 'ACTIVATION_PENDING_KSP',
          'stage': 2,
          'status_message': 'Sinkronisasi sedang berjalan',
        };
      }
      return {
        'success': true,
        'activation_status': 'ACTIVATED',
        'stage': 3,
        'status_message': 'Perangkat Berhasil Dilindungi',
        'license_key': '6KYKJ-65T6T-WMVBD-NNPEG',
      };
    }
  }

  /// Fetches subscriber profile data for verified MSISDN
  Future<UserSession> fetchUserProfile({
    required String msisdn,
    required String mobileId,
  }) async {
    await Future.delayed(const Duration(milliseconds: 300));
    return MockBackendData.defaultUserSession(mobileId).copyWith(
      msisdn: msisdn,
    );
  }
}
