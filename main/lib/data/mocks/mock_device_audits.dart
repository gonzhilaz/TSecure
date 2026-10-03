import 'package:flutter/material.dart';
import '../models/security_audit_item.dart';

class MockDeviceAudits {
  static List<SecurityAuditItem> getInitialAuditItems() {
    return const [
      SecurityAuditItem(
        id: 'realtime_malware',
        title: 'Anti Malware',
        subtitle: 'Proteksi Real-time pasif • Mesin Kaspersky',
        status: AuditStatus.passed,
        icon: Icons.shield_outlined,
        actionLabel: 'Pindai',
        scoreWeight: 30,
      ),
      SecurityAuditItem(
        id: 'web_filter',
        title: 'Web Filter & Anti Phising',
        subtitle: 'Kaspersky KSN • Blokir situs berbahaya',
        status: AuditStatus.passed,
        icon: Icons.language_rounded,
        actionLabel: 'Uji URL',
        scoreWeight: 25,
      ),
      SecurityAuditItem(
        id: 'wifi_safety',
        title: 'Keamanan Wi-Fi',
        subtitle: 'Audit enkripsi WPA2/WPA3 & integritas jaringan',
        status: AuditStatus.passed,
        icon: Icons.wifi_rounded,
        actionLabel: 'Audit',
        scoreWeight: 25,
      ),
      SecurityAuditItem(
        id: 'device_root',
        title: 'Integritas Perangkat',
        subtitle: 'Kaspersky RootDetector • Integritas OS & Bootloader',
        status: AuditStatus.passed,
        icon: Icons.system_security_update_good_outlined,
        actionLabel: 'Cek Root',
        scoreWeight: 20,
      ),
      SecurityAuditItem(
        id: 'dns_cert_check',
        title: 'Validasi DNS & SSL',
        subtitle: 'Kaspersky DnsChecker & CertificateCheckService',
        status: AuditStatus.passed,
        icon: Icons.domain_verification_rounded,
        actionLabel: 'Audit SSL',
        scoreWeight: 20,
      ),
    ];
  }
}
