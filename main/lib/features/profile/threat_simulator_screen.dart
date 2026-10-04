import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/theme/app_colors.dart';
import '../../core/theme/app_typography.dart';
import '../../data/models/activity_log.dart';
import '../../data/services/activity_log_repository.dart';
import '../../data/services/kaspersky_sdk_bridge.dart';
import '../../data/services/threat_telemetry_dispatcher.dart';
import '../../data/services/url_filter_service.dart';

/// In-App Threat Simulator Screen (Lab Simulasi Ancaman).
/// Provides live simulation of cyber threats (SMS Judi Online, APK Smishing,
/// Phishing Web Filter, and EICAR Test Virus) for testing & demonstration.
class ThreatSimulatorScreen extends StatefulWidget {
  const ThreatSimulatorScreen({super.key});

  @override
  State<ThreatSimulatorScreen> createState() => _ThreatSimulatorScreenState();
}

class _ThreatSimulatorScreenState extends State<ThreatSimulatorScreen> {
  bool _isSimulating = false;
  String? _lastResult;

  Future<void> _simulateSmsJudiOnline() async {
    setState(() => _isSimulating = true);
    final ksp = context.read<KasperskySdkBridge>();
    final logRepo = context.read<ActivityLogRepository?>();
    const sender = '+6285712398412';
    const message = 'SLOT GACOR MALAM INI! MAXWIN OLYMPUS DEPO PULSA TANPA POTONGAN klik https://slot-zeus88.xyz';
    const url = 'https://slot-zeus88.xyz';

    await ksp.showSecurityNotification(
      title: '🚨 Peringatan SMS Judi Online Ilegal!',
      message: 'SMS dari $sender terindikasi Promosi Judi Online & Slot Ilegal. Jangan klik tautan!',
      isThreat: true,
    );

    await ThreatTelemetryDispatcher.recordAndReport(
      logRepo: logRepo,
      msisdn: ksp.installationId.isNotEmpty ? ksp.installationId : '081299887766',
      mobileId: ksp.installationId,
      threatType: 'JUDI_ONLINE',
      target: sender,
      severity: 'HIGH',
      title: 'SMS Judi Online Dicegat ($sender)',
      description: 'Pesan promosi slot gacor terdeteksi: "$message". Tautan $url dicegah dari akses.',
      actionTaken: 'BLOCKED',
      icon: Icons.casino_outlined,
      category: LogCategory.jaringan,
    );

    setState(() {
      _isSimulating = false;
      _lastResult = 'Berhasil! SMS Judi Online dicegat & dicatat ke log telemetry.';
    });
    _showResultSnackbar('🚨 Simulasi SMS Judi Online berhasil dicegat!');
  }

  Future<void> _simulateSmsPhishingApk() async {
    setState(() => _isSimulating = true);
    final ksp = context.read<KasperskySdkBridge>();
    final logRepo = context.read<ActivityLogRepository?>();
    const sender = '+6282199001122';
    const message = 'KEPOLISIAN RI: Surat Tilang ETLE No. 9210-B. Unduh berkas tilang_elektronik.apk';
    const url = 'https://etle-polri.top/tilang.apk';

    await ksp.showSecurityNotification(
      title: '🚨 Peringatan SMS Penipuan / APK Malware!',
      message: 'SMS dari $sender terindikasi Modus Rekayasa Sosial APK Malware. Jangan unduh berkas!',
      isThreat: true,
    );

    await ThreatTelemetryDispatcher.recordAndReport(
      logRepo: logRepo,
      msisdn: ksp.installationId.isNotEmpty ? ksp.installationId : '081299887766',
      mobileId: ksp.installationId,
      threatType: 'SMISHING',
      target: sender,
      severity: 'CRITICAL',
      title: 'SMS Malware APK Dicegat ($sender)',
      description: 'Upaya pengiriman malware APK ($message, tautan $url) dicegat oleh Telkomsel Secure.',
      actionTaken: 'BLOCKED',
      icon: Icons.sms_failed_rounded,
      category: LogCategory.jaringan,
    );

    setState(() {
      _isSimulating = false;
      _lastResult = 'Berhasil! SMS Phishing APK berhasil dicegah & dilaporkan.';
    });
    _showResultSnackbar('🛡️ Simulasi SMS APK Malware berhasil dinetralkan!');
  }

  Future<void> _simulateWebFilterJudol() async {
    setState(() => _isSimulating = true);
    final ksp = context.read<KasperskySdkBridge>();
    final logRepo = context.read<ActivityLogRepository?>();
    const testUrl = 'https://slot88-gacor-olympus.xyz/login';

    final verdict = await UrlFilterService.checkUrl(testUrl);
    final isBlocked = verdict['isBlocked'] == true;

    await ksp.showSecurityNotification(
      title: 'Situs Judi Terblokir!',
      message: 'Web Filter Telkomsel Secure memblokir akses ke: $testUrl',
      isThreat: true,
    );

    await ThreatTelemetryDispatcher.recordAndReport(
      logRepo: logRepo,
      msisdn: ksp.installationId.isNotEmpty ? ksp.installationId : '081299887766',
      mobileId: ksp.installationId,
      threatType: 'JUDI_ONLINE',
      target: testUrl,
      severity: 'HIGH',
      title: 'Situs Judi Online Diblokir',
      description: 'Akses ke domain perjudian dicegah di level Web Filter (Status blokir: $isBlocked, kategori: ${verdict["category"]}).',
      actionTaken: 'BLOCKED',
      icon: Icons.language_rounded,
      category: LogCategory.jaringan,
    );

    setState(() {
      _isSimulating = false;
      _lastResult = 'Pengecekan Web Filter: Situs judi berhasil diblokir (Status: ${verdict["category"]}).';
    });
    _showResultSnackbar('🛑 Web Filter berhasil memblokir domain judi!');
  }

  Future<void> _simulateEicarMalware() async {
    setState(() => _isSimulating = true);
    final ksp = context.read<KasperskySdkBridge>();
    final logRepo = context.read<ActivityLogRepository?>();
    const threatName = 'EICAR-Test-File (Standard Antivirus Test)';

    await ksp.showSecurityNotification(
      title: 'Ancaman Berkas Terdeteksi!',
      message: 'Kaspersky Engine mendeteksi berkas $threatName dan mengisolasinya ke Karantina.',
      isThreat: true,
    );

    await ThreatTelemetryDispatcher.recordAndReport(
      logRepo: logRepo,
      msisdn: ksp.installationId.isNotEmpty ? ksp.installationId : '081299887766',
      mobileId: ksp.installationId,
      threatType: 'MALWARE',
      target: '/storage/emulated/0/Download/eicar.com.txt',
      severity: 'CRITICAL',
      title: 'Uji Coba Berkas Virus Terisolasi',
      description: 'Berkas uji $threatName berhasil diamankan di Brankas Karantina AES-256.',
      actionTaken: 'ISOLATED',
      icon: Icons.security_rounded,
      category: LogCategory.pemindaian,
    );

    setState(() {
      _isSimulating = false;
      _lastResult = 'Berhasil! Sampel virus uji coba berhasil diisolasi ke karantina.';
    });
    _showResultSnackbar('☣️ Sampel EICAR berhasil diamankan di Karantina!');
  }

  void _showResultSnackbar(String msg) {
    if (!mounted) return;
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(msg, style: AppTypography.labelMd.copyWith(color: Colors.white)),
        backgroundColor: AppColors.navyDeep,
        behavior: SnackBarBehavior.floating,
        duration: const Duration(seconds: 3),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF7F9FC),
      appBar: AppBar(
        title: Text(
          'Lab Simulasi Ancaman',
          style: AppTypography.headlineSm.copyWith(fontWeight: FontWeight.w700, color: AppColors.navyDeep),
        ),
        backgroundColor: Colors.white,
        elevation: 0,
        centerTitle: false,
        iconTheme: const IconThemeData(color: AppColors.navyDeep),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: const Color(0xFFFFF7ED),
                borderRadius: BorderRadius.circular(14),
                border: Border.all(color: const Color(0xFFFFEDD5)),
              ),
              child: Row(
                children: [
                  const Icon(Icons.science_outlined, color: Color(0xFFEA580C), size: 28),
                  const SizedBox(width: 14),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text('Sandbox Demo & Pengujian',
                            style: AppTypography.labelMd.copyWith(fontWeight: FontWeight.w700, color: const Color(0xFF9A3412))),
                        const SizedBox(height: 2),
                        Text(
                          'Pilih skenario serangan di bawah ini untuk melihat reaksi seketika dari sistem proteksi Telkomsel Secure.',
                          style: AppTypography.bodySm.copyWith(color: const Color(0xFFC2410C), fontSize: 12),
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 20),
            _buildSimulationCard(
              title: 'Simulasi SMS Judi Online / Slot',
              subtitle: 'Menguji deteksi heuristik SMS promosi slot gacor, depo pulsa, & link judi.',
              icon: Icons.casino_outlined,
              iconColor: const Color(0xFFEA580C),
              iconBg: const Color(0xFFFFF7ED),
              actionLabel: 'Picu SMS Judi Online',
              onPressed: _isSimulating ? null : _simulateSmsJudiOnline,
            ),
            const SizedBox(height: 14),
            _buildSimulationCard(
              title: 'Simulasi SMS Phishing APK',
              subtitle: 'Menguji pencegatan SMS rekayasa sosial kurir / surat tilang berbasis APK.',
              icon: Icons.sms_failed_outlined,
              iconColor: AppColors.primary,
              iconBg: const Color(0xFFFFEBEB),
              actionLabel: 'Picu SMS Phishing APK',
              onPressed: _isSimulating ? null : _simulateSmsPhishingApk,
            ),
            const SizedBox(height: 14),
            _buildSimulationCard(
              title: 'Simulasi Web Filter Judi Online',
              subtitle: 'Menguji pencegatan akses browser terhadap domain perjudian ilegal.',
              icon: Icons.language_rounded,
              iconColor: const Color(0xFF0284C7),
              iconBg: const Color(0xFFE0F2FE),
              actionLabel: 'Uji Web Filter Judol',
              onPressed: _isSimulating ? null : _simulateWebFilterJudol,
            ),
            const SizedBox(height: 14),
            _buildSimulationCard(
              title: 'Simulasi File Virus Uji EICAR',
              subtitle: 'Memvalidasi modul karantina brankas enkripsi terhadap sampel EICAR.',
              icon: Icons.security_rounded,
              iconColor: const Color(0xFF7C3AED),
              iconBg: const Color(0xFFF3E8FF),
              actionLabel: 'Uji Isolasi Karantina',
              onPressed: _isSimulating ? null : _simulateEicarMalware,
            ),
            if (_lastResult != null) ...[
              const SizedBox(height: 24),
              Container(
                padding: const EdgeInsets.all(14),
                decoration: BoxDecoration(
                  color: Colors.white,
                  borderRadius: BorderRadius.circular(12),
                  border: Border.all(color: AppColors.slateBorder),
                ),
                child: Row(
                  children: [
                    const Icon(Icons.check_circle_outline, color: AppColors.statusSafeEmerald, size: 20),
                    const SizedBox(width: 10),
                    Expanded(
                      child: Text(_lastResult!, style: AppTypography.bodySm.copyWith(color: AppColors.navyDeep)),
                    ),
                  ],
                ),
              ),
            ],
            const SizedBox(height: 30),
          ],
        ),
      ),
    );
  }

  Widget _buildSimulationCard({
    required String title,
    required String subtitle,
    required IconData icon,
    required Color iconColor,
    required Color iconBg,
    required String actionLabel,
    required VoidCallback? onPressed,
  }) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: AppColors.slateBorder),
        boxShadow: [
          BoxShadow(color: Colors.black.withValues(alpha: 0.03), blurRadius: 8, offset: const Offset(0, 2)),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Container(
                padding: const EdgeInsets.all(10),
                decoration: BoxDecoration(color: iconBg, borderRadius: BorderRadius.circular(12)),
                child: Icon(icon, color: iconColor, size: 22),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(title, style: AppTypography.labelMd.copyWith(fontWeight: FontWeight.w700, color: AppColors.navyDeep)),
                    const SizedBox(height: 2),
                    Text(subtitle, style: AppTypography.bodySm.copyWith(color: AppColors.textSecondary, fontSize: 12)),
                  ],
                ),
              ),
            ],
          ),
          const SizedBox(height: 14),
          SizedBox(
            width: double.infinity,
            child: ElevatedButton(
              onPressed: onPressed,
              style: ElevatedButton.styleFrom(
                backgroundColor: AppColors.navyDeep,
                foregroundColor: Colors.white,
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
                padding: const EdgeInsets.symmetric(vertical: 12),
                elevation: 0,
              ),
              child: Text(actionLabel, style: AppTypography.labelMd.copyWith(color: Colors.white, fontWeight: FontWeight.w600)),
            ),
          ),
        ],
      ),
    );
  }
}
