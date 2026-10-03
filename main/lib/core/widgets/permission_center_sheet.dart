import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../theme/app_colors.dart';
import '../theme/app_typography.dart';
import '../../data/services/wifi_security_service.dart';
import 'permission_card_tile.dart';
import 'web_filter_guide_dialog.dart';

class PermissionCenterSheet extends StatefulWidget {
  const PermissionCenterSheet({super.key});

  static void show(BuildContext context) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (_) => const PermissionCenterSheet(),
    );
  }

  @override
  State<PermissionCenterSheet> createState() => _PermissionCenterSheetState();
}

class _PermissionCenterSheetState extends State<PermissionCenterSheet> with WidgetsBindingObserver {
  static const MethodChannel _kspChannel = MethodChannel('com.taspenguard/ksp');

  bool _loading = true;
  bool _storageGranted = false;
  bool _notifGranted = false;
  bool _locationGranted = false;
  bool _accessibilityGranted = false;
  bool _installGranted = false;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    _refreshPermissions();
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.resumed) {
      _refreshPermissions();
    }
  }

  Future<void> _refreshPermissions() async {
    setState(() => _loading = true);
    try {
      final res = await _kspChannel.invokeMapMethod<String, dynamic>('getAllPermissionsStatus');
      if (res != null && mounted) {
        setState(() {
          _storageGranted = res['storage'] as bool? ?? false;
          _notifGranted = res['notifications'] as bool? ?? false;
          _locationGranted = res['location'] as bool? ?? false;
          _accessibilityGranted = res['accessibility'] as bool? ?? false;
          _installGranted = res['installPackages'] as bool? ?? false;
          _loading = false;
        });
        return;
      }
    } catch (_) {}
    if (mounted) setState(() => _loading = false);
  }

  @override
  Widget build(BuildContext context) {
    final grantedCount = [
      _storageGranted,
      _notifGranted,
      _locationGranted,
      _accessibilityGranted,
      _installGranted,
    ].where((e) => e).length;

    return Container(
      constraints: BoxConstraints(
        maxHeight: MediaQuery.of(context).size.height * 0.88,
      ),
      decoration: const BoxDecoration(
        color: AppColors.surfaceCard,
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      child: SafeArea(
        top: false,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            const SizedBox(height: 12),
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
            const SizedBox(height: 12),
            _buildHeader(grantedCount),
            const Divider(color: AppColors.slateBorder, height: 1),
            Expanded(
              child: _loading
                  ? const Center(child: CircularProgressIndicator(color: AppColors.primary))
                  : ListView(
                      padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 16),
                      children: [
                        _buildSummaryCard(grantedCount),
                        const SizedBox(height: 16),
                        Text(
                          'DAFTAR PERIZINAN SISTEM',
                          style: AppTypography.labelSm.copyWith(
                            letterSpacing: 1.2,
                            color: AppColors.slateMid,
                            fontWeight: FontWeight.w700,
                          ),
                        ),
                        const SizedBox(height: 12),
                        PermissionCardTile(
                          icon: Icons.folder_open_rounded,
                          title: 'Akses Berkas & Penyimpanan Penuh',
                          description: 'Diperlukan untuk memindai berkas unduhan, APK, dan media dari ancaman virus/trojan.',
                          isGranted: _storageGranted,
                          onActivate: () async {
                            await _kspChannel.invokeMethod('requestStoragePermission');
                            await _refreshPermissions();
                          },
                        ),
                        const SizedBox(height: 12),
                        PermissionCardTile(
                          icon: Icons.notifications_active_outlined,
                          title: 'Notifikasi Peringatan Instan',
                          description: 'Memberikan peringatan seketika saat malware atau situs phishing terdeteksi.',
                          isGranted: _notifGranted,
                          onActivate: () async {
                            await _kspChannel.invokeMethod('requestNotificationPermission');
                            await _refreshPermissions();
                          },
                        ),
                        const SizedBox(height: 12),
                        PermissionCardTile(
                          icon: Icons.wifi_find_rounded,
                          title: 'Deteksi Jaringan Wi-Fi & Lokasi',
                          description: 'Diwajibkan sistem Android untuk membaca nama hotspot (SSID) demi mendeteksi Rogue Wi-Fi.',
                          isGranted: _locationGranted,
                          onActivate: () async {
                            await _kspChannel.invokeMethod('requestLocationPermission');
                            await _refreshPermissions();
                          },
                        ),
                        const SizedBox(height: 12),
                        PermissionCardTile(
                          icon: Icons.language_rounded,
                          title: 'Web Filter Browser (Aksesibilitas)',
                          description: 'Memeriksa URL di Chrome, Edge & browser lain untuk memblokir tautan scam/phishing.',
                          isGranted: _accessibilityGranted,
                          onActivate: () async {
                            await WebFilterGuideDialog.show(
                              context,
                              onOpenAccessibility: () async {
                                await _kspChannel.invokeMethod('requestAccessibilityPermission');
                                await _refreshPermissions();
                              },
                              onOpenAppSettings: () async {
                                await _kspChannel.invokeMethod('openAppSettings');
                                await _refreshPermissions();
                              },
                            );
                            await _refreshPermissions();
                          },
                        ),
                        const SizedBox(height: 12),
                        PermissionCardTile(
                          icon: Icons.system_update_rounded,
                          title: 'Pasang Pembaruan Aplikasi',
                          description: 'Mengizinkan pembaruan versi mandiri TelkomSecure langsung tanpa repot.',
                          isGranted: _installGranted,
                          onActivate: () async {
                            await _kspChannel.invokeMethod('requestInstallPermission');
                          },
                        ),
                        const SizedBox(height: 12),
                        PermissionCardTile(
                          icon: Icons.play_circle_outline_rounded,
                          title: 'Mulai Otomatis (Autostart)',
                          description: 'Wajib diaktifkan di Xiaomi (MIUI/HyperOS) & ColorOS agar mesin proteksi Kaspersky tetap siaga setelah HP dinyalakan ulang.',
                          isGranted: true,
                          activeLabel: 'Buka Menu',
                          onActivate: () async {
                            await WifiSecurityService.openAutostartSettings();
                          },
                        ),
                        const SizedBox(height: 12),
                        PermissionCardTile(
                          icon: Icons.picture_in_picture_alt_rounded,
                          title: 'Izin Pop-Up Latar Belakang',
                          description: 'Diperlukan pada Xiaomi & Android agar dialog peringatan malware/phishing dapat muncul seketika saat aplikasi di latar belakang.',
                          isGranted: true,
                          activeLabel: 'Buka Menu',
                          onActivate: () async {
                            await WifiSecurityService.openBackgroundPopupSettings();
                          },
                        ),
                      ],
                    ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildHeader(int grantedCount) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 6),
      child: Row(
        children: [
          Container(
            width: 40,
            height: 40,
            decoration: BoxDecoration(
              color: AppColors.primary.withValues(alpha: 0.1),
              borderRadius: BorderRadius.circular(10),
            ),
            child: const Icon(Icons.security, color: AppColors.primary, size: 22),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'Pusat Perizinan Proteksi',
                  style: AppTypography.headlineSm.copyWith(
                    fontWeight: FontWeight.w700,
                    color: AppColors.navyDeep,
                  ),
                ),
                Text(
                  '$grantedCount dari 5 proteksi aktif',
                  style: AppTypography.bodySm.copyWith(color: AppColors.slateMuted, fontSize: 12),
                ),
              ],
            ),
          ),
          IconButton(
            icon: const Icon(Icons.close),
            onPressed: () => Navigator.pop(context),
          ),
        ],
      ),
    );
  }

  Widget _buildSummaryCard(int count) {
    final allActive = count == 5;
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: allActive
            ? AppColors.statusSafeEmerald.withValues(alpha: 0.1)
            : AppColors.primary.withValues(alpha: 0.08),
        borderRadius: BorderRadius.circular(16),
        border: Border.all(
          color: allActive
              ? AppColors.statusSafeEmerald.withValues(alpha: 0.3)
              : AppColors.primary.withValues(alpha: 0.2),
        ),
      ),
      child: Row(
        children: [
          Icon(
            allActive ? Icons.check_circle_outline_rounded : Icons.info_outline_rounded,
            color: allActive ? AppColors.statusSafeEmerald : AppColors.primary,
            size: 26,
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  allActive ? 'Proteksi Menyeluruh Aktif' : 'Tingkatkan Perlindungan Perangkat',
                  style: AppTypography.labelMd.copyWith(
                    fontWeight: FontWeight.w700,
                    color: allActive ? AppColors.statusSafeEmerald : AppColors.navyDeep,
                  ),
                ),
                const SizedBox(height: 2),
                Text(
                  allActive
                      ? 'Semua izin keamanan Android telah diberikan secara optimal.'
                      : 'Aktifkan seluruh izin agar fitur Web Filter, Wi-Fi Guard, dan Deep Scan bekerja maksimal.',
                  style: AppTypography.bodySm.copyWith(color: AppColors.textSecondary, fontSize: 11),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

}
