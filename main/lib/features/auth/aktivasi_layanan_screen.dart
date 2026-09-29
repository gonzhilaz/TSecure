import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/theme/app_colors.dart';
import '../../core/theme/app_typography.dart';
import '../shell/main_shell_screen.dart';
import 'auth_controller.dart';
import 'widgets/activation_stage_card.dart';

enum WizardStage { stage1Ndp, stage2Kaspersky, stage3Done }

class AktivasiLayananScreen extends StatefulWidget {
  final bool initialSimulateDelay;
  final bool initialSimulateKspOutage;

  const AktivasiLayananScreen({
    super.key,
    this.initialSimulateDelay = false,
    this.initialSimulateKspOutage = false,
  });

  @override
  State<AktivasiLayananScreen> createState() => _AktivasiLayananScreenState();
}

class _AktivasiLayananScreenState extends State<AktivasiLayananScreen> {
  WizardStage _currentStage = WizardStage.stage1Ndp;
  bool _isRunning = false;
  bool _stage1Pending = false;
  bool _stage2PendingKsp = false;
  bool _simulateDelay = false;
  bool _simulateKspOutage = false;

  @override
  void initState() {
    super.initState();
    _simulateDelay = widget.initialSimulateDelay;
    _simulateKspOutage = widget.initialSimulateKspOutage;
    WidgetsBinding.instance.addPostFrameCallback((_) => _startActivationWorkflow());
  }

  Future<void> _startActivationWorkflow() async {
    if (_isRunning) return;
    setState(() {
      _isRunning = true;
      _currentStage = WizardStage.stage1Ndp;
      _stage1Pending = false;
      _stage2PendingKsp = false;
    });

    final auth = context.read<AuthController>();

    // ===== TAHAP 1: Verifikasi Paket MyTelkomsel =====
    await Future.delayed(const Duration(milliseconds: 900));
    if (_simulateDelay) {
      if (!mounted) return;
      setState(() {
        _stage1Pending = true;
        _isRunning = false;
      });
      return;
    }

    // Paket MyTelkomsel Aktif -> Lanjut otomatis ke tahap 2
    if (!mounted) return;
    setState(() {
      _currentStage = WizardStage.stage2Kaspersky;
    });

    // ===== TAHAP 2: Menerbitkan & Mengikat Lisensi Kaspersky B2B =====
    await Future.delayed(const Duration(milliseconds: 1200));
    final res = await auth.activateLicense(
      simulateKspOutage: _simulateKspOutage,
      simulatePendingNdp: false,
    );

    if (!mounted) return;

    if (res['activation_status'] == 'ACTIVATION_PENDING_KSP' || _simulateKspOutage) {
      // Server Kaspersky Gangguan/Timeout -> status ACTIVATION_PENDING_KSP
      setState(() {
        _stage2PendingKsp = true;
        _isRunning = false;
      });
      return;
    }

    // Tahap 2 Berhasil -> Mesin proteksi aktif penuh -> Lanjut Tahap 3
    setState(() {
      _currentStage = WizardStage.stage3Done;
      _isRunning = false;
    });

    // Auto navigate to dashboard after celebration
    await Future.delayed(const Duration(milliseconds: 2200));
    if (!mounted) return;
    _navigateToDashboard();
  }

  void _navigateToDashboard() {
    Navigator.of(context).pushAndRemoveUntil(
      MaterialPageRoute(builder: (_) => const MainShellScreen()),
      (route) => false,
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        elevation: 0,
        title: Text(
          'Aktivasi Layanan',
          style: AppTypography.headlineSm.copyWith(color: AppColors.navyDeep),
        ),
        centerTitle: true,
      ),
      body: SafeArea(
        child: Column(
          children: [
            Expanded(
              child: SingleChildScrollView(
                padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 16),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    Text(
                      'Menghubungkan Perlindungan',
                      style: AppTypography.headlineLg.copyWith(color: AppColors.navyDeep),
                    ),
                    const SizedBox(height: 6),
                    Text(
                      'Mengikat lisensi korporasi Kaspersky B2B ke nomor seluler Anda.',
                      style: AppTypography.bodySm.copyWith(color: AppColors.slateMuted),
                    ),
                    const SizedBox(height: 24),
                    ActivationStageCard(
                      stageNum: 1,
                      title: 'Verifikasi Paket MyTelkomsel',
                      subtitle: _stage1Pending
                          ? 'Status paket memerlukan validasi sinkronisasi'
                          : 'Pemeriksaan status paket data & langganan',
                      isActive: _currentStage == WizardStage.stage1Ndp,
                      isCompleted: _currentStage.index > WizardStage.stage1Ndp.index,
                      isWarning: _stage1Pending,
                      isRunning: _isRunning,
                      warningBadgeText: 'Dalam Proses...',
                      warningContent: Stage1WarningWidget(
                        isRunning: _isRunning,
                        onRetry: () {
                          setState(() => _simulateDelay = false);
                          _startActivationWorkflow();
                        },
                      ),
                    ),
                    const SizedBox(height: 14),
                    ActivationStageCard(
                      stageNum: 2,
                      title: 'Penerbitan Lisensi Kaspersky B2B',
                      subtitle: _stage2PendingKsp
                          ? 'Pengikatan lisensi ke Mobile ID tertunda'
                          : 'Mengikat lisensi & mengaktifkan Mobile ID (1/3)',
                      isActive: _currentStage == WizardStage.stage2Kaspersky,
                      isCompleted: _currentStage.index > WizardStage.stage2Kaspersky.index,
                      isWarning: _stage2PendingKsp,
                      isRunning: _isRunning,
                      warningBadgeText: 'Sinkronisasi sedang berjalan',
                      warningContent: Stage2WarningWidget(
                        onContinue: _navigateToDashboard,
                      ),
                    ),
                    const SizedBox(height: 14),
                    ActivationStageCard(
                      stageNum: 3,
                      title: 'Selesai',
                      subtitle: '',
                      isActive: _currentStage == WizardStage.stage3Done,
                      isCompleted: _currentStage == WizardStage.stage3Done,
                      isWarning: false,
                      isRunning: _isRunning,
                      customBody: _currentStage == WizardStage.stage3Done
                          ? const Stage3CelebrationWidget()
                          : null,
                    ),
                  ],
                ),
              ),
            ),
            DemoPresetsBar(
              onNormal: () {
                _simulateDelay = false;
                _simulateKspOutage = false;
                _startActivationWorkflow();
              },
              onDelay: () {
                _simulateDelay = true;
                _simulateKspOutage = false;
                _startActivationWorkflow();
              },
              onKspOutage: () {
                _simulateDelay = false;
                _simulateKspOutage = true;
                _startActivationWorkflow();
              },
            ),
          ],
        ),
      ),
    );
  }
}
