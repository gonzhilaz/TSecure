import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/widgets/app_update_dialog.dart';
import '../../core/widgets/virus_db_update_dialog.dart';
import '../../data/services/app_update_service.dart';
import '../../data/services/clipboard_url_guard_service.dart';
import '../../data/services/kaspersky_sdk_bridge.dart';
import '../dashboard/beranda_screen.dart';
import '../device/perangkat_screen.dart';
import '../history/riwayat_screen.dart';
import '../profile/profil_screen.dart';
import '../scanner/pemindaian_screen.dart';
import 'widgets/custom_bottom_nav.dart';

class MainShellScreen extends StatefulWidget {
  const MainShellScreen({super.key});

  @override
  State<MainShellScreen> createState() => _MainShellScreenState();
}

class _MainShellScreenState extends State<MainShellScreen> with WidgetsBindingObserver {
  int _currentIndex = 0;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _checkAutoUpdate();
      _checkClipboardLink();
    });
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.resumed) {
      _checkClipboardLink();
    }
  }

  Future<void> _checkClipboardLink() async {
    if (!mounted) return;
    try {
      final sdk = context.read<KasperskySdkBridge>();
      await ClipboardUrlGuardService.checkClipboardUrl(sdk);
    } catch (_) {}
  }

  Future<void> _checkAutoUpdate() async {
    // Delay 2s after launch so the shell is fully mounted and rendered
    await Future.delayed(const Duration(seconds: 2));
    if (!mounted) return;
    final service = AppUpdateService();
    final updateInfo = await service.checkForUpdate();
    if (!mounted) return;
    if (updateInfo != null && updateInfo.hasUpdate) {
      AppUpdateDialog.show(context, updateInfo: updateInfo);
      return;
    }

    // Cek ketersediaan pembaruan basis data virus
    final sdk = context.read<KasperskySdkBridge>();
    if (!sdk.isVirusDbUpToDate) {
      VirusDbUpdateDialog.show(context);
    }
  }

  void _onTabSelected(int index) {
    setState(() {
      _currentIndex = index;
    });
  }

  @override
  Widget build(BuildContext context) {
    final screens = [
      BerandaScreen(
        onNavigateToScanner: () => _onTabSelected(2),
        onNavigateToHistory: () => _onTabSelected(3),
        onNavigateToProfile: () => _onTabSelected(4),
      ),
      PerangkatScreen(
        onNavigateToScanner: () => _onTabSelected(2),
        onNavigateToProfile: () => _onTabSelected(4),
      ),
      PemindaianScreen(
        onBack: () => _onTabSelected(0),
      ),
      RiwayatScreen(
        onNavigateToProfile: () => _onTabSelected(4),
      ),
      ProfilScreen(
        onBack: () => _onTabSelected(0),
      ),
    ];

    return Scaffold(
      body: IndexedStack(
        index: _currentIndex,
        children: screens,
      ),
      bottomNavigationBar: CustomBottomNav(
        currentIndex: _currentIndex,
        onTabSelected: _onTabSelected,
      ),
    );
  }
}
