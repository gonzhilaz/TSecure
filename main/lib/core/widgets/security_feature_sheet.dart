import 'package:flutter/material.dart';
import '../theme/app_colors.dart';
import '../theme/app_typography.dart';
import '../../data/services/kaspersky_sdk_bridge.dart';
import 'security_test_lab_sheet.dart';

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
        featureTitle: featureTitle,
        sdk: sdk,
        onNavigateToScanner: onNavigateToScanner,
        onNavigateToProfile: onNavigateToProfile,
      ),
    );
  }

  @override
  State<SecurityFeatureSheet> createState() => _SecurityFeatureSheetState();
}

class _SecurityFeatureSheetState extends State<SecurityFeatureSheet> {
  late bool _isEnabled;

  @override
  void initState() {
    super.initState();
    _isEnabled = _getInitialStatus();
  }

  bool _getInitialStatus() {
    final title = widget.featureTitle.toLowerCase();
    if (title.contains('web')) return widget.sdk.webFilter;
    if (title.contains('realtime')) return widget.sdk.realtimeProtection;
    if (title.contains('pua')) return widget.sdk.puaScanner;
    if (title.contains('wifi')) return widget.sdk.wifiSafety;
    if (title.contains('fake')) return widget.sdk.fakeAppsProtection;
    if (title.contains('device')) return widget.sdk.deviceReputation;
    if (title.contains('data')) return widget.sdk.dataBreachProtection;
    return true;
  }

  void _onToggle(bool value) {
    setState(() => _isEnabled = value);
    final title = widget.featureTitle.toLowerCase();
    if (title.contains('web')) widget.sdk.toggleWebFilter(value);
    if (title.contains('realtime')) widget.sdk.toggleRealtimeProtection(value);
    if (title.contains('pua')) widget.sdk.togglePuaScanner(value);
    if (title.contains('wifi')) widget.sdk.toggleWifiSafety(value);
    if (title.contains('fake')) widget.sdk.toggleFakeApps(value);
    if (title.contains('device')) widget.sdk.toggleDeviceRep(value);
    if (title.contains('data')) widget.sdk.toggleDataBreach(value);
  }

  _FeatureConfig _getConfig() {
    final title = widget.featureTitle.toLowerCase();
    if (title.contains('web')) {
      return _FeatureConfig(
        title: 'Web Filter',
        icon: Icons.language,
        engine: 'Kaspersky URL Filter SDK v5.21',
        description: 'Blokir real-time terhadap situs berbahaya, web phishing, dan skrip pelacak berbahaya.',
        actionLabel: 'Buka Lab Uji Web Filter (Phishing & Malware)',
        onAction: () {
          Navigator.pop(context);
          SecurityTestLabSheet.show(context, sdk: widget.sdk, initialTab: 0);
        },
        telemetry: [
          _Row('Status URL Guard', _isEnabled ? 'Aktif & Melindungi' : 'Nonaktif'),
          _Row('Deteksi Phishing', 'Kaspersky Security Network (KSN)'),
          _Row('Situs Berbahaya Dicegat', '14 Domain Terblokir'),
          _Row('Protokol Didukung', 'HTTP, HTTPS, TLS Inspection'),
        ],
      );
    }
    if (title.contains('realtime')) {
      return _FeatureConfig(
        title: 'Realtime Scanner',
        icon: Icons.shield,
        engine: 'Kaspersky Anti-Malware Core',
        description: 'Pemantauan I/O file system secara terus menerus untuk mencegah trojan dan ransomware.',
        actionLabel: 'Mulai Pemindaian Sekarang',
        onAction: () {
          Navigator.pop(context);
          widget.onNavigateToScanner();
        },
        telemetry: [
          _Row('Status Pemantauan', _isEnabled ? 'Aktif 24/7' : 'Nonaktif'),
          _Row('Kecepatan Analisis', '< 50 ms per file'),
          _Row('Database Virus', widget.sdk.virusDbVersion),
          _Row('Heuristik Malware', 'Deep AI Behavior Analysis'),
        ],
      );
    }
    if (title.contains('pua')) {
      return _FeatureConfig(
        title: 'PUA Scanner',
        icon: Icons.security,
        engine: 'Kaspersky Heuristic Adware Engine',
        description: 'Mendeteksi Potentially Unwanted Applications, adware agresif, dan utilitas pengawasan tersembunyi.',
        actionLabel: 'Pindai Aplikasi Terpasang',
        onAction: () {
          Navigator.pop(context);
          widget.onNavigateToScanner();
        },
        telemetry: [
          _Row('Status Deteksi', _isEnabled ? 'Aktif Melindungi' : 'Nonaktif'),
          _Row('Klasifikasi Deteksi', 'Adware, Riskware, Remote Admin'),
          _Row('Aplikasi Dianalisis', '84 Aplikasi Sistem & Pengguna'),
          _Row('Tingkat Kebersihan', '100% Bebas PUA'),
        ],
      );
    }
    if (title.contains('wifi')) {
      return _FeatureConfig(
        title: 'Wifi Safety',
        icon: Icons.wifi,
        engine: 'TelkomSecure Network Guard',
        description: 'Audit enkripsi hotspot, integritas DNS resolver, dan perlindungan dari serangan ARP Spoofing.',
        actionLabel: 'Audit Ulang Jaringan Wi-Fi',
        actionMessage: 'Audit Wi-Fi Selesai: Enkripsi WPA3 & DNS Crypt aman!',
        telemetry: [
          _Row('Jaringan Terhubung', 'Telkomsel_Orbit_5G'),
          _Row('Protokol Keamanan', 'WPA3 Personal (AES-256)'),
          _Row('DNS Spoofing Guard', _isEnabled ? 'Aktif (DoH Enkripsi)' : 'Nonaktif'),
          _Row('Status Man-in-the-Middle', 'Aman (0 Anomali)'),
        ],
      );
    }
    if (title.contains('fake')) {
      return _FeatureConfig(
        title: 'Fake Apps Detector',
        icon: Icons.warning_amber_rounded,
        engine: 'BlackWall RASP + Kaspersky AppControl',
        description: 'Memverifikasi sertifikat cryptographic APK dan mencegah aplikasi tiruan/repackaged.',
        actionLabel: 'Pindai Integritas APK',
        onAction: () {
          Navigator.pop(context);
          widget.onNavigateToScanner();
        },
        telemetry: [
          _Row('Integritas Paket', _isEnabled ? 'Terverifikasi Utuh' : 'Nonaktif'),
          _Row('Injeksi DEX / Hook', '0 Modifikasi Terdeteksi'),
          _Row('Anti-Tampering', 'BlackWall Hardening Aktif'),
          _Row('Validasi Tanda Tangan', 'v1/v2/v3 Scheme Resmi'),
        ],
      );
    }
    if (title.contains('device')) {
      return _FeatureConfig(
        title: 'Device Reputation',
        icon: Icons.phone_android,
        engine: 'Kaspersky Integrity & SELinux Auditor',
        description: 'Menilai kesehatan dan integritas firmware sistem operasi dari eksploitasi root atau debugging ilegal.',
        actionLabel: 'Cek Integritas Perangkat',
        actionMessage: 'Hasil Integritas: Perangkat 100% murni tanpa root!',
        telemetry: [
          _Row('Skor Integritas', '100% Terlindungi Maksimal'),
          _Row('Status Root / Magisk', 'Tidak Ditemukan (Bersih)'),
          _Row('Status SELinux', 'Enforcing (Aktif)'),
          _Row('Patch Keamanan OS', 'September 2026'),
        ],
      );
    }
    if (title.contains('data')) {
      return _FeatureConfig(
        title: 'Data Breach Monitor',
        icon: Icons.dns_outlined,
        engine: 'Telkomsel Cyber Threat Intelligence',
        description: 'Pemantauan dark web dan kebocoran basis data untuk nomor telepon dan kredensial akun terikat.',
        actionLabel: 'Periksa Kebocoran Sekarang',
        actionMessage: 'Pemeriksaan Selesai: Kredensial akun Telkomsel Anda aman!',
        telemetry: [
          _Row('Akun Terpantau', '+62 82-141414-875 (Halo)'),
          _Row('Cakupan Database', 'Dark Web & Public Breach Dumps'),
          _Row('Status Kebocoran', '0 Insiden Kebocoran Ditemukan'),
          _Row('Frekuensi Audit', 'Otomatis Berkala 24 Jam'),
        ],
      );
    }
    return _FeatureConfig(
      title: widget.featureTitle,
      icon: Icons.shield_outlined,
      engine: 'Kaspersky Security Engine',
      description: 'Modul perlindungan aktif untuk integritas sistem.',
      actionLabel: 'Pindai Perangkat',
      onAction: () {
        Navigator.pop(context);
        widget.onNavigateToScanner();
      },
      telemetry: const [
        _Row('Status Modul', 'Aktif & Melindungi'),
        _Row('Mesin SDK', 'Kaspersky v5.21.0'),
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
      child: SafeArea(
        top: false,
        child: Column(
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
                  padding: const EdgeInsets.all(12),
                  decoration: BoxDecoration(
                    color: _isEnabled
                        ? AppColors.primary
                        : AppColors.slateDivider,
                    borderRadius: BorderRadius.circular(16),
                  ),
                  child: Icon(
                    cfg.icon,
                    color: _isEnabled ? Colors.white : AppColors.navyDeep,
                    size: 26,
                  ),
                ),
                const SizedBox(width: 14),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        cfg.title,
                        style: AppTypography.headlineSm.copyWith(
                          fontWeight: FontWeight.w700,
                          color: AppColors.navyDeep,
                        ),
                      ),
                      const SizedBox(height: 2),
                      Text(
                        cfg.engine,
                        style: AppTypography.bodySm.copyWith(
                          color: AppColors.slateMuted,
                          fontSize: 12,
                        ),
                      ),
                    ],
                  ),
                ),
                Switch(
                  value: _isEnabled,
                  activeThumbColor: AppColors.primary,
                  onChanged: _onToggle,
                ),
              ],
            ),
            const SizedBox(height: 14),
            Text(
              cfg.description,
              style: AppTypography.bodySm.copyWith(color: AppColors.slateMid),
            ),
            const SizedBox(height: 18),
            Container(
              padding: const EdgeInsets.all(14),
              decoration: BoxDecoration(
                color: AppColors.surface,
                borderRadius: BorderRadius.circular(14),
                border: Border.all(color: AppColors.slateBorder),
              ),
              child: Column(
                children: [
                  for (int i = 0; i < cfg.telemetry.length; i++) ...[
                    _buildRow(cfg.telemetry[i].label, cfg.telemetry[i].value),
                    if (i < cfg.telemetry.length - 1)
                      const Divider(color: AppColors.slateBorder, height: 16),
                  ],
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
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(14),
                  ),
                ),
                onPressed: () {
                  if (cfg.onAction != null) {
                    cfg.onAction!();
                  } else if (cfg.actionMessage != null) {
                    Navigator.pop(context);
                    ScaffoldMessenger.of(context).showSnackBar(
                      SnackBar(
                        content: Text(cfg.actionMessage!),
                        backgroundColor: AppColors.navyDeep,
                        behavior: SnackBarBehavior.floating,
                        duration: const Duration(seconds: 2),
                      ),
                    );
                  }
                },
                child: Text(
                  cfg.actionLabel,
                  style: const TextStyle(fontWeight: FontWeight.w700),
                ),
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
          child: Text(
            value,
            style: AppTypography.bodySm.copyWith(
              fontWeight: FontWeight.w600,
              color: AppColors.navyDeep,
            ),
            textAlign: TextAlign.right,
          ),
        ),
      ],
    );
  }
}

class _FeatureConfig {
  final String title, engine, description, actionLabel;
  final IconData icon;
  final String? actionMessage;
  final VoidCallback? onAction;
  final List<_Row> telemetry;

  _FeatureConfig({
    required this.title, required this.icon, required this.engine,
    required this.description, required this.actionLabel,
    this.actionMessage, this.onAction, required this.telemetry,
  });
}

class _Row {
  final String label, value;
  const _Row(this.label, this.value);
}
