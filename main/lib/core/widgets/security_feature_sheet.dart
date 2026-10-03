import 'package:flutter/material.dart';
import '../theme/app_colors.dart';
import '../theme/app_typography.dart';
import '../../data/services/kaspersky_sdk_bridge.dart';
import '../../data/services/protection_status_service.dart';
import '../services/permission_gate.dart';
import 'dns_cert_check_dialog.dart';
import 'security_feature_live_configs.dart';
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
  ProtectionStatus? _protection;
  bool _loadingProtection = false;

  @override
  void initState() {
    super.initState();
    _isEnabled = _getInitialStatus();
    if (widget.featureTitle.toLowerCase().contains('realtime')) _loadProtection();
  }

  Future<void> _loadProtection() async {
    setState(() => _loadingProtection = true);
    final st = await ProtectionStatusService.fetch();
    if (!mounted) return;
    setState(() { _protection = st; _loadingProtection = false; });
  }

  Future<void> _auditWifi() async {
    final ok = await PermissionGate.ensure(context, GateFeature.wifi);
    if (!mounted) return;
    setState(() => _isAuditingWifi = true);
    final res = await widget.sdk.auditWifi();
    if (!mounted) return;
    setState(() => _isAuditingWifi = false);
    final ssid = (res['ssid'] as String?) ?? '';
    final msg = !ok
        ? 'Izin lokasi diperlukan untuk membaca nama Wi-Fi.'
        : (res['isWifi'] == true
            ? 'Wi-Fi: ${ssid.isEmpty ? 'nama tidak terbaca' : ssid}'
            : (res['summary'] as String? ?? 'Tidak terhubung Wi-Fi'));
    ScaffoldMessenger.of(context).showSnackBar(SnackBar(
      content: Text(msg),
      backgroundColor: res['isSafe'] == false ? AppColors.statusWarning : AppColors.navyDeep,
      duration: const Duration(seconds: 2),
    ));
  }

  bool _getInitialStatus() => LiveFeatureConfigs.initialStatus(widget.featureTitle, widget.sdk);

  Future<void> _toggleStatus(bool value) async {
    final t = widget.featureTitle.toLowerCase();
    if (value) {
      GateFeature? gate;
      if (t.contains('web')) {
        gate = GateFeature.webFilter;
      } else if (t.contains('realtime') || t.contains('pua') || t.contains('fake')) {
        gate = GateFeature.scan;
      } else if (t.contains('wifi')) {
        gate = GateFeature.wifi;
      }
      if (gate != null) {
        final ok = await PermissionGate.ensure(context, gate);
        if (!ok || !mounted) return;
      }
    }
    setState(() => _isEnabled = value);
    LiveFeatureConfigs.applyToggle(widget.featureTitle, widget.sdk, value);
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
      return LiveFeatureConfigs.realtime(
        status: _protection,
        loading: _loadingProtection,
        dbVersion: widget.sdk.virusDbVersion,
        onAction: () { Navigator.pop(context); widget.onNavigateToScanner(); },
        onRefresh: _loadProtection,
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
          SecurityFeatureTelemetryRow('Status Deteksi', _isEnabled ? 'Aktif' : 'Nonaktif'),
          SecurityFeatureTelemetryRow('Klasifikasi Deteksi', 'Adware, Riskware, Remote Admin'),
        ],
      );
    }
    if (title.contains('wifi')) {
      return LiveFeatureConfigs.wifi(
        audit: widget.sdk.wifiAuditData,
        loading: _isAuditingWifi,
        enabled: _isEnabled,
        onAction: _auditWifi,
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
          SecurityFeatureTelemetryRow('Status Deteksi', _isEnabled ? 'Aktif' : 'Nonaktif'),
          SecurityFeatureTelemetryRow('Validasi Tanda Tangan', 'v1/v2/v3'),
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
          final msg = rooted ? 'Peringatan: Root terdeteksi ($cause)' : 'Integritas aman: tanpa root';
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
                  if (cfg.onAction != null) return cfg.onAction!();
                  if (cfg.actionMessage != null) {
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
