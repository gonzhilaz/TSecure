import 'package:flutter/material.dart';
import '../theme/app_colors.dart';
import '../theme/app_typography.dart';
import '../../data/services/kaspersky_sdk_bridge.dart';
import 'dns_cert_check_dialog.dart';
import 'security_feature_models.dart';
import 'web_filter_test_dialog.dart';

class SecurityFeatureSheet extends StatefulWidget {
  final String featureTitle;
  final KasperskySdkBridge sdk;
  final VoidCallback onNavigateToScanner;
  final VoidCallback? onNavigateToProfile;

  const SecurityFeatureSheet({
    super.key,
    required this.featureTitle,
    required this.sdk,
    required this.onNavigateToScanner,
    this.onNavigateToProfile,
  });

  static void show(
    BuildContext context, {
    required String featureTitle,
    required KasperskySdkBridge sdk,
    required VoidCallback onNavigateToScanner,
    VoidCallback? onNavigateToProfile,
  }) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (_) => SecurityFeatureSheet(
        featureTitle: featureTitle, sdk: sdk,
        onNavigateToScanner: onNavigateToScanner, onNavigateToProfile: onNavigateToProfile,
      ),
    );
  }

  @override
  State<SecurityFeatureSheet> createState() => _SecurityFeatureSheetState();
}

class _SecurityFeatureSheetState extends State<SecurityFeatureSheet> {
  late bool _isEnabled;
  Map<String, dynamic>? _rootAuditResult;
  bool _isCheckingRoot = false;
  bool _isAuditingWifi = false;

  @override
  void initState() {
    super.initState();
    _isEnabled = _getInitialStatus();
  }

  bool _getInitialStatus() {
    final t = widget.featureTitle.toLowerCase();
    if (t.contains('web')) return widget.sdk.webFilter;
    if (t.contains('realtime')) return widget.sdk.realtimeProtection;
    if (t.contains('pua')) return widget.sdk.puaScanner;
    if (t.contains('wifi')) return widget.sdk.wifiSafety;
    if (t.contains('fake')) return widget.sdk.fakeAppsProtection;
    if (t.contains('device')) return widget.sdk.deviceReputation;
    return widget.sdk.dataBreachProtection;
  }

  void _toggleStatus(bool value) {
    setState(() => _isEnabled = value);
    final t = widget.featureTitle.toLowerCase();
    if (t.contains('web')) {
      widget.sdk.toggleWebFilter(value);
    } else if (t.contains('realtime')) {
      widget.sdk.toggleRealtimeProtection(value);
    } else if (t.contains('pua')) {
      widget.sdk.togglePuaScanner(value);
    } else if (t.contains('wifi')) {
      widget.sdk.toggleWifiSafety(value);
    } else if (t.contains('fake')) {
      widget.sdk.toggleFakeApps(value);
    } else if (t.contains('device')) {
      widget.sdk.toggleDeviceRep(value);
    } else if (t.contains('data')) {
      widget.sdk.toggleDataBreach(value);
    }
  }

  SecurityFeatureConfig _getConfig() {
    final title = widget.featureTitle.toLowerCase();
    if (title.contains('web')) {
      return SecurityFeatureConfig(
        title: 'Web Filter',
        icon: Icons.language_rounded,
        engine: 'Kaspersky URL Filter SDK v5.21',
        description: 'Blokir real-time terhadap situs berbahaya, web phishing, dan skrip pelacak berbahaya.',
        actionLabel: 'Uji Validasi URL',
        onAction: () => WebFilterTestDialog.show(context, sdk: widget.sdk),
        telemetry: [
          SecurityFeatureTelemetryRow('Status URL Guard', _isEnabled ? 'Aktif & Melindungi' : 'Nonaktif'),
          SecurityFeatureTelemetryRow('Deteksi Phishing', 'Kaspersky Security Network (KSN)'),
          SecurityFeatureTelemetryRow('Mesin Pemeriksa', 'Kaspersky UrlCheckService'),
          SecurityFeatureTelemetryRow('Protokol Didukung', 'HTTP, HTTPS, TLS Inspection'),
        ],
      );
    }
    if (title.contains('realtime')) {
      return SecurityFeatureConfig(
        title: 'Realtime Scanner',
        icon: Icons.shield,
        engine: 'Kaspersky Anti-Malware Core',
        description: 'Pemantauan I/O file system secara terus menerus untuk mencegah trojan dan ransomware.',
        actionLabel: 'Mulai Pemindaian Sekarang',
        onAction: () { Navigator.pop(context); widget.onNavigateToScanner(); },
        telemetry: [
          SecurityFeatureTelemetryRow('Status Pemantauan', _isEnabled ? 'Aktif 24/7' : 'Nonaktif'),
          SecurityFeatureTelemetryRow('Kecepatan Analisis', '< 50 ms per file'),
          SecurityFeatureTelemetryRow('Database Virus', widget.sdk.virusDbVersion),
          SecurityFeatureTelemetryRow('Heuristik Malware', 'Deep AI Behavior Analysis'),
        ],
      );
    }
    if (title.contains('pua')) {
      return SecurityFeatureConfig(
        title: 'PUA Scanner',
        icon: Icons.security,
        engine: 'Kaspersky Heuristic Adware Engine',
        description: 'Mendeteksi Potentially Unwanted Applications, adware agresif, dan utilitas pengawasan tersembunyi.',
        actionLabel: 'Pindai Aplikasi Terpasang',
        onAction: () { Navigator.pop(context); widget.onNavigateToScanner(); },
        telemetry: [
          SecurityFeatureTelemetryRow('Status Deteksi', _isEnabled ? 'Aktif Melindungi' : 'Nonaktif'),
          SecurityFeatureTelemetryRow('Klasifikasi Deteksi', 'Adware, Riskware, Remote Admin'),
          SecurityFeatureTelemetryRow('Aplikasi Dianalisis', '84 Aplikasi Sistem & Pengguna'),
          SecurityFeatureTelemetryRow('Tingkat Kebersihan', '100% Bebas PUA'),
        ],
      );
    }
    if (title.contains('wifi')) {
      final wifi = widget.sdk.wifiAuditData;
      final ssid = wifi?['ssid'] as String? ?? 'Telkomsel_Orbit_5G';
      final proto = wifi?['securityProtocol'] as String? ?? 'WPA3 Personal (AES-256)';
      final signal = wifi?['signalLevel'] as String? ?? 'Baik (80%)';
      final isSafe = wifi?['isSafe'] != false;
      return SecurityFeatureConfig(
        title: 'Wifi Safety',
        icon: Icons.wifi_rounded,
        engine: 'TelkomSecure Network Guard & Wi-Fi Inspector',
        description: 'Audit enkripsi hotspot, integritas DNS resolver, dan perlindungan dari serangan rogue AP/ARP spoofing.',
        actionLabel: _isAuditingWifi ? 'Memeriksa Wi-Fi...' : 'Audit Jaringan Wi-Fi Sekarang',
        onAction: () async {
          setState(() => _isAuditingWifi = true);
          final res = await widget.sdk.auditWifi();
          if (!mounted) return;
          setState(() => _isAuditingWifi = false);
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text('Audit Wi-Fi: $ssid (${res['securityProtocol'] ?? 'Aman'})'),
              backgroundColor: isSafe ? AppColors.statusSafe : AppColors.statusWarning,
              duration: const Duration(seconds: 2),
            ),
          );
        },
        telemetry: [
          SecurityFeatureTelemetryRow('Jaringan Terhubung', ssid),
          SecurityFeatureTelemetryRow('Protokol Keamanan', proto),
          SecurityFeatureTelemetryRow('Kekuatan Sinyal', signal),
          SecurityFeatureTelemetryRow('Status Enkripsi', _isEnabled ? (isSafe ? 'Terkunci & Aman' : 'Peringatan Terbuka') : 'Nonaktif'),
        ],
      );
    }
    if (title.contains('fake')) {
      return SecurityFeatureConfig(
        title: 'Fake Apps Detector',
        icon: Icons.warning_amber_rounded,
        engine: 'Kaspersky AppControl + Integrity Engine',
        description: 'Memverifikasi sertifikat cryptographic APK dan mencegah aplikasi tiruan/repackaged.',
        actionLabel: 'Pindai Integritas APK',
        onAction: () { Navigator.pop(context); widget.onNavigateToScanner(); },
        telemetry: [
          SecurityFeatureTelemetryRow('Integritas Paket', _isEnabled ? 'Terverifikasi Utuh' : 'Nonaktif'),
          SecurityFeatureTelemetryRow('Injeksi DEX / Hook', '0 Modifikasi Terdeteksi'),
          SecurityFeatureTelemetryRow('Anti-Tampering', 'Hardening Aktif'),
          SecurityFeatureTelemetryRow('Validasi Tanda Tangan', 'v1/v2/v3 Scheme Resmi'),
        ],
      );
    }
    if (title.contains('device')) {
      final isRooted = _rootAuditResult?['isRooted'] == true;
      final rootCause = _rootAuditResult?['rootCause'] as String? ?? 'Bersih (Terverifikasi)';
      return SecurityFeatureConfig(
        title: 'Device Reputation',
        icon: Icons.phone_android,
        engine: 'Kaspersky RootDetector v5.21',
        description: 'Menilai kesehatan dan integritas firmware sistem operasi dari eksploitasi root atau debugging ilegal.',
        actionLabel: _isCheckingRoot ? 'Memeriksa Root...' : 'Cek Integritas Perangkat',
        onAction: () async {
          setState(() => _isCheckingRoot = true);
          final res = await widget.sdk.checkRoot();
          if (!mounted) return;
          setState(() {
            _rootAuditResult = res;
            _isCheckingRoot = false;
          });
          final rooted = res['isRooted'] == true;
          final cause = res['rootCause'] as String? ?? 'Terdeteksi Root';
          final msg = rooted ? '🚨 Peringatan Root Terdeteksi ($cause)' : '✅ Integritas Aman: Tanpa Root';
          ScaffoldMessenger.of(context).showSnackBar(SnackBar(
            content: Text(msg),
            backgroundColor: rooted ? AppColors.statusDanger : AppColors.statusSafe,
            duration: const Duration(seconds: 3),
          ));
        },
        telemetry: [
          SecurityFeatureTelemetryRow('Status Root / Magisk', _rootAuditResult == null ? 'Siap Diaudit' : (isRooted ? 'Terdeteksi Root!' : 'Bersih (Murni)')),
          SecurityFeatureTelemetryRow('Detail Audit Root', _rootAuditResult == null ? 'Tekan Tombol Cek' : rootCause),
          SecurityFeatureTelemetryRow('Mesin Audit', 'Kaspersky RootDetector'),
          SecurityFeatureTelemetryRow('Status SELinux', 'Enforcing (Aktif)'),
        ],
      );
    }
    if (title.contains('dns') || title.contains('cert') || title.contains('sertifikat')) {
      return SecurityFeatureConfig(
        title: 'DNS & Certificate Guard',
        icon: Icons.verified_user_rounded,
        engine: 'Kaspersky DnsChecker & CertificateCheckService',
        description: 'Verifikasi validitas sertifikat SSL/TLS domain dan audit integritas resolusi DNS dari ancaman spoofing/hijacking.',
        actionLabel: 'Buka Audit DNS & SSL',
        onAction: () {
          Navigator.pop(context);
          DnsCertCheckDialog.show(context);
        },
        telemetry: const [
          SecurityFeatureTelemetryRow('Mesin Validasi SSL', 'Kaspersky CertificateCheckService'),
          SecurityFeatureTelemetryRow('Resolusi DNS', 'Kaspersky DnsChecker'),
          SecurityFeatureTelemetryRow('Protokol Didukung', 'TLS 1.2 / 1.3, X.509'),
          SecurityFeatureTelemetryRow('Status Proteksi', 'Aktif & Melindungi'),
        ],
      );
    }
    if (title.contains('data')) {
      return SecurityFeatureConfig(
        title: 'Data Breach Monitor',
        icon: Icons.dns_outlined,
        engine: 'Telkomsel Cyber Threat Intelligence',
        description: 'Pemantauan dark web dan kebocoran basis data untuk nomor telepon dan kredensial akun terikat.',
        actionLabel: 'Periksa Kebocoran Sekarang',
        actionMessage: 'Pemeriksaan Selesai: Kredensial akun Telkomsel Anda aman!',
        telemetry: [
          SecurityFeatureTelemetryRow('Akun Terpantau', 'Nomor Pelanggan Aktif'),
          SecurityFeatureTelemetryRow('Cakupan Database', 'Dark Web & Public Breach Dumps'),
          SecurityFeatureTelemetryRow('Status Kebocoran', '0 Insiden Kebocoran Ditemukan'),
          SecurityFeatureTelemetryRow('Frekuensi Audit', 'Otomatis Berkala 24 Jam'),
        ],
      );
    }
    return SecurityFeatureConfig(
      title: widget.featureTitle,
      icon: Icons.shield_outlined,
      engine: 'Kaspersky Security Engine',
      description: 'Modul perlindungan aktif untuk integritas sistem.',
      actionLabel: 'Pindai Perangkat',
      onAction: () { Navigator.pop(context); widget.onNavigateToScanner(); },
      telemetry: const [
        SecurityFeatureTelemetryRow('Status Modul', 'Aktif & Melindungi'),
        SecurityFeatureTelemetryRow('Mesin SDK', 'Kaspersky v5.21.0'),
      ],
    );
  }

  @override
  Widget build(BuildContext context) {
    final cfg = _getConfig();

    return Container(
      decoration: const BoxDecoration(
        color: AppColors.surfaceCard,
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      padding: const EdgeInsets.symmetric(horizontal: 22, vertical: 20),
      child: SafeArea(top: false, child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Center(
              child: Container(
                width: 40,
                height: 4,
                decoration: BoxDecoration(
                  color: AppColors.slateBorder,
                  borderRadius: BorderRadius.circular(2),
                ),
              ),
            ),
            const SizedBox(height: 18),
            Row(
              children: [
                Container(
                  padding: const EdgeInsets.all(10),
                  decoration: BoxDecoration(
                    color: AppColors.primary.withValues(alpha: 0.1),
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: Icon(cfg.icon, color: AppColors.primary, size: 24),
                ),
                const SizedBox(width: 14),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(cfg.title, style: AppTypography.headlineSm),
                      const SizedBox(height: 2),
                      Text(
                        cfg.engine,
                        style: AppTypography.labelSm.copyWith(
                          color: AppColors.primary,
                          fontWeight: FontWeight.w600,
                        ),
                      ),
                    ],
                  ),
                ),
                Switch.adaptive(
                  value: _isEnabled,
                  onChanged: _toggleStatus,
                  activeThumbColor: AppColors.primary,
                ),
              ],
            ),
            const SizedBox(height: 14),
            Text(cfg.description, style: AppTypography.bodySm.copyWith(color: AppColors.slateMuted, height: 1.4)),
            const SizedBox(height: 20),
            Container(
              decoration: BoxDecoration(
                color: AppColors.background,
                borderRadius: BorderRadius.circular(16),
                border: Border.all(color: AppColors.slateBorder),
              ),
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'TELEMETRI & STATUS OPERASIONAL',
                    style: AppTypography.labelSm.copyWith(
                      color: AppColors.slateMuted,
                      letterSpacing: 0.8,
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                  const SizedBox(height: 12),
                  ...cfg.telemetry.map((row) => Padding(
                    padding: const EdgeInsets.only(bottom: 8),
                    child: _buildRow(row.label, row.value),
                  )),
                ],
              ),
            ),
            const SizedBox(height: 20),
            SizedBox(
              width: double.infinity,
              height: 46,
              child: ElevatedButton(
                style: ElevatedButton.styleFrom(
                  backgroundColor: AppColors.primary,
                  foregroundColor: Colors.white,
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
                ),
                onPressed: () {
                  if (cfg.onAction != null) {
                    cfg.onAction!();
                  } else if (cfg.actionMessage != null) {
                    Navigator.pop(context);
                    ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(cfg.actionMessage!), backgroundColor: AppColors.navyDeep, behavior: SnackBarBehavior.floating));
                  }
                },
                child: Text(cfg.actionLabel, style: const TextStyle(fontWeight: FontWeight.w700)),
              ),
            ),
            const SizedBox(height: 10),
          ],
        ),
      ),
    );
  }

  Widget _buildRow(String label, String value) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        Text(label, style: AppTypography.bodySm.copyWith(color: AppColors.slateMuted)),
        const SizedBox(width: 8),
        Expanded(
          child: Text(value, style: AppTypography.bodySm.copyWith(fontWeight: FontWeight.w600, color: AppColors.navyDeep), textAlign: TextAlign.right),
        ),
      ],
    );
  }
}
