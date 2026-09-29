import 'package:flutter/material.dart';
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

class _MainShellScreenState extends State<MainShellScreen> {
  int _currentIndex = 0;

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
