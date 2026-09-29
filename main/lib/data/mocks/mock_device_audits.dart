import 'package:flutter/material.dart';
import '../models/security_audit_item.dart';

class MockDeviceAudits {
  static List<SecurityAuditItem> getInitialAuditItems() {
    return const [
      SecurityAuditItem(
        id: 'opt_schedule',
        title: 'Optimalisasi Keamanan',
        subtitle: 'Jadwal deep scan berkala aktif',
        status: AuditStatus.warning,
        icon: Icons.phone_android,
        actionLabel: 'Pindai',
        scoreWeight: 10,
      ),
      SecurityAuditItem(
        id: 'web_filter',
        title: 'Web Filter & Wi-Fi',
        subtitle: 'Telkomsel_Orbit_5G • WPA3',
        status: AuditStatus.passed,
        icon: Icons.wifi,
        scoreWeight: 15,
      ),
      SecurityAuditItem(
        id: 'app_pua',
        title: 'Analisis Aplikasi & PUA',
        subtitle: '84 Aplikasi • 0 Fake Apps',
        status: AuditStatus.passed,
        icon: Icons.verified_user_outlined,
        scoreWeight: 15,
      ),
      SecurityAuditItem(
        id: 'realtime_protection',
        title: 'Proteksi Real-time',
        subtitle: 'Pemantauan ancaman 24/7',
        status: AuditStatus.passed,
        icon: Icons.shield_outlined,
        scoreWeight: 25,
      ),
      SecurityAuditItem(
        id: 'bootloader_root',
        title: 'Audit Bootloader & Root',
        subtitle: 'Integritas Knox & SELinux Enforcing',
        status: AuditStatus.passed,
        icon: Icons.system_security_update_good_outlined,
        scoreWeight: 15,
      ),
      SecurityAuditItem(
        id: 'password_strength',
        title: 'Audit Kekuatan Sandi',
        subtitle: 'Periksa kebocoran kata sandi akun',
        status: AuditStatus.warning,
        icon: Icons.lock_outline,
        actionLabel: 'Cek',
        scoreWeight: 10,
      ),
      SecurityAuditItem(
        id: 'storage_encryption',
        title: 'Enkripsi Penyimpanan',
        subtitle: 'Data internal terenkripsi AES-256',
        status: AuditStatus.passed,
        icon: Icons.security_update_warning_outlined,
        scoreWeight: 10,
      ),
    ];
  }
}
