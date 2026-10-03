import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../../core/constants/app_constants.dart';
import '../../data/services/app_update_service.dart';
import '../../data/services/kaspersky_sdk_bridge.dart';
import '../../data/services/threat_manager_service.dart';
import '../theme/app_colors.dart';
import '../theme/app_typography.dart';
import 'app_update_dialog.dart';
import 'permission_center_sheet.dart';
import 'quarantine_vault_sheet.dart';
import 'virus_db_update_dialog.dart';

class SettingsSheet extends StatefulWidget {
  const SettingsSheet({super.key});

  static void show(BuildContext context) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (_) => const SettingsSheet(),
    );
  }

  @override
  State<SettingsSheet> createState() => _SettingsSheetState();
}

class _SettingsSheetState extends State<SettingsSheet> {
  bool _autoUpdateDb = true;
  bool _biometricLock = true;
  bool _pushAlerts = true;
  bool _autoQuarantine = true;
  bool _autoClearThreats = false;
  bool _checkingUpdate = false;
  bool _isLoading = true;
  bool _isAppUpdated = true;
  String _appVersion = 'v1.0.0 (Build 1)';

  @override
  void initState() {
    super.initState();
    _loadSettings();
  }

  Future<void> _loadSettings() async {
    try {
      final threatManager = context.read<ThreatManagerService>();
      final prefs = await SharedPreferences.getInstance();
      final ver = await AppUpdateService().getCurrentVersion();
      final vName = ver['versionName'] ?? '1.0.0';
      final vCode = ver['versionCode'] ?? 1;

      if (!mounted) return;

      setState(() {
        _autoUpdateDb = prefs.getBool(AppConstants.keyAutoUpdateDb) ?? true;
        _biometricLock = prefs.getBool(AppConstants.keyBiometricLock) ?? true;
        _pushAlerts = prefs.getBool(AppConstants.keyPushAlerts) ?? true;
        _autoQuarantine = threatManager.autoQuarantine;
        _autoClearThreats = threatManager.autoClearThreats;
        _appVersion = 'v$vName (Build $vCode)';
        _isLoading = false;
      });
    } catch (_) {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  Future<void> _saveSetting(String key, bool value) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool(key, value);
  }

  void _showFeedback(String message) {
    ScaffoldMessenger.of(context).hideCurrentSnackBar();
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text(message), backgroundColor: AppColors.navyDeep, behavior: SnackBarBehavior.floating, duration: const Duration(seconds: 2)),
    );
  }

  Widget _buildSwitchTile(String title, String subtitle, bool value, ValueChanged<bool> onChanged) {
    return SwitchListTile(
      contentPadding: EdgeInsets.zero,
      activeThumbColor: AppColors.primary,
      title: Text(title, style: AppTypography.labelMd.copyWith(fontWeight: FontWeight.w600)),
      subtitle: Text(subtitle, style: AppTypography.bodySm),
      value: value,
      onChanged: onChanged,
    );
  }

  Widget _buildUpdatedBadge() {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
      decoration: BoxDecoration(
        color: AppColors.statusSafeEmerald.withValues(alpha: 0.12),
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: AppColors.statusSafeEmerald.withValues(alpha: 0.3)),
      ),
      child: const Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(Icons.check_circle_rounded, size: 12, color: AppColors.statusSafeEmerald),
          SizedBox(width: 4),
          Text('UPDATED', style: TextStyle(color: AppColors.statusSafeEmerald, fontSize: 10, fontWeight: FontWeight.w800, letterSpacing: 0.5)),
        ],
      ),
    );
  }

  Widget _buildUpdateButton(String label, Color color) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
      decoration: BoxDecoration(color: color, borderRadius: BorderRadius.circular(8)),
      child: Text(label, style: const TextStyle(color: Colors.white, fontSize: 11, fontWeight: FontWeight.w700)),
    );
  }

  @override
  Widget build(BuildContext context) {
    final threatManager = context.read<ThreatManagerService>();
    final sdk = context.watch<KasperskySdkBridge>();

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
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            // Drag handle
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

            // Header Row
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 20),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text(
                    'Pengaturan Keamanan',
                    style: AppTypography.headlineSm.copyWith(
                      fontWeight: FontWeight.w700,
                      color: AppColors.navyDeep,
                    ),
                  ),
                  IconButton(
                    icon: const Icon(Icons.close),
                    onPressed: () => Navigator.pop(context),
                  ),
                ],
              ),
            ),
            const Divider(color: AppColors.slateBorder, height: 1),

            // Scrollable Content
            Expanded(
              child: _isLoading
                  ? const Center(child: CircularProgressIndicator(color: AppColors.primary))
                  : SingleChildScrollView(
                      physics: const BouncingScrollPhysics(),
                      padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 12),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.stretch,
                        children: [
                          // 1. Basis Data Virus Card
                          ListTile(
                            contentPadding: const EdgeInsets.symmetric(horizontal: 14, vertical: 2),
                            shape: RoundedRectangleBorder(
                              borderRadius: BorderRadius.circular(14),
                              side: const BorderSide(color: AppColors.slateBorder),
                            ),
                            tileColor: AppColors.background,
                            leading: Container(
                              width: 38,
                              height: 38,
                              decoration: BoxDecoration(
                                color: AppColors.statusSafeEmerald.withValues(alpha: 0.12),
                                borderRadius: BorderRadius.circular(10),
                              ),
                              child: const Icon(Icons.security_rounded, color: AppColors.statusSafeEmerald, size: 20),
                            ),
                            title: Text(
                              'Basis Data Virus',
                              style: AppTypography.labelMd.copyWith(fontWeight: FontWeight.w700),
                              maxLines: 1,
                              overflow: TextOverflow.ellipsis,
                            ),
                            subtitle: Text(
                              sdk.isVirusDbUpToDate ? 'Kaspersky v5.21 • Versi Mutakhir' : 'Definisi v5.22 Tersedia • Perbarui',
                              style: AppTypography.bodySm.copyWith(color: AppColors.slateMuted, fontSize: 11),
                              maxLines: 1,
                              overflow: TextOverflow.ellipsis,
                            ),
                            trailing: sdk.isVirusDbUpToDate ? _buildUpdatedBadge() : _buildUpdateButton('Perbarui', AppColors.navyDeep),
                            onTap: () async {
                              if (sdk.isVirusDbUpToDate) {
                                _showFeedback('Basis data virus Kaspersky sudah versi terkini (UPDATED).');
                              } else {
                                await VirusDbUpdateDialog.show(context);
                              }
                            },
                          ),
                          const SizedBox(height: 8),
                          _buildSwitchTile('Pembaruan Otomatis Virus DB', 'Unduh definisi ancaman terbaru sebelum pemindaian', _autoUpdateDb, (v) async {
                            setState(() => _autoUpdateDb = v);
                            await _saveSetting(AppConstants.keyAutoUpdateDb, v);
                            _showFeedback(v ? 'Pembaruan otomatis virus DB aktif.' : 'Pembaruan otomatis virus DB dinonaktifkan.');
                          }),
                          const Divider(color: AppColors.slateBorder, height: 1),
                          _buildSwitchTile('Kunci Biometrik Aplikasi', 'Gunakan sidik jari atau Face Unlock untuk membuka aplikasi', _biometricLock, (v) async {
                            setState(() => _biometricLock = v);
                            await _saveSetting(AppConstants.keyBiometricLock, v);
                            _showFeedback(v ? 'Autentikasi biometrik aplikasi aktif.' : 'Autentikasi biometrik dinonaktifkan.');
                          }),
                          const Divider(color: AppColors.slateBorder, height: 1),
                          _buildSwitchTile('Karantina Otomatis (Auto Quarantine)', 'Otomatis isolasi & enkripsi berkas berbahaya saat ditemukan', _autoQuarantine, (v) async {
                            setState(() => _autoQuarantine = v);
                            await threatManager.setAutoQuarantine(v);
                            _showFeedback(v ? 'Karantina otomatis berkas berbahaya aktif.' : 'Karantina otomatis dinonaktifkan.');
                          }),
                          const Divider(color: AppColors.slateBorder, height: 1),
                          _buildSwitchTile('Pembersihan Otomatis (Auto Clear Threats)', 'Hapus malware otomatis seketika tanpa perlu konfirmasi manual', _autoClearThreats, (v) async {
                            setState(() => _autoClearThreats = v);
                            await threatManager.setAutoClearThreats(v);
                            _showFeedback(v ? 'Pembersihan otomatis malware aktif.' : 'Pembersihan otomatis malware dinonaktifkan.');
                          }),
                          const Divider(color: AppColors.slateBorder, height: 1),
                          _buildSwitchTile('Notifikasi Peringatan Instan', 'Dapatkan peringatan seketika saat situs phishing diblokir', _pushAlerts, (v) async {
                            setState(() => _pushAlerts = v);
                            await _saveSetting(AppConstants.keyPushAlerts, v);
                            _showFeedback(v ? 'Notifikasi peringatan instan aktif.' : 'Notifikasi peringatan dinonaktifkan.');
                          }),
                          const SizedBox(height: 14),

                          // Pusat Perizinan Card
                          ListTile(
                            contentPadding: const EdgeInsets.symmetric(horizontal: 14, vertical: 2),
                            shape: RoundedRectangleBorder(
                              borderRadius: BorderRadius.circular(14),
                              side: const BorderSide(color: AppColors.slateBorder),
                            ),
                            tileColor: AppColors.background,
                            leading: Container(
                              width: 38,
                              height: 38,
                              decoration: BoxDecoration(
                                color: AppColors.primary.withValues(alpha: 0.1),
                                borderRadius: BorderRadius.circular(10),
                              ),
                              child: const Icon(Icons.verified_user_outlined, color: AppColors.primary, size: 20),
                            ),
                            title: Text('Pusat Perizinan Proteksi', style: AppTypography.labelMd.copyWith(fontWeight: FontWeight.w700)),
                            subtitle: Text('Kelola izin penyimpanan, lokasi Wi-Fi, dan filter', style: AppTypography.bodySm.copyWith(color: AppColors.slateMuted, fontSize: 11)),
                            trailing: const Icon(Icons.arrow_forward_ios_rounded, size: 14, color: AppColors.slateMid),
                            onTap: () => PermissionCenterSheet.show(context),
                          ),
                          const SizedBox(height: 10),

                          // Brankas Karantina Card
                          ListTile(
                            contentPadding: const EdgeInsets.symmetric(horizontal: 14, vertical: 2),
                            shape: RoundedRectangleBorder(
                              borderRadius: BorderRadius.circular(14),
                              side: const BorderSide(color: AppColors.slateBorder),
                            ),
                            tileColor: AppColors.background,
                            leading: Container(
                              width: 38,
                              height: 38,
                              decoration: BoxDecoration(
                                color: AppColors.statusDanger.withValues(alpha: 0.1),
                                borderRadius: BorderRadius.circular(10),
                              ),
                              child: const Icon(Icons.archive_outlined, color: AppColors.statusDanger, size: 20),
                            ),
                            title: Text('Brankas Karantina', style: AppTypography.labelMd.copyWith(fontWeight: FontWeight.w700)),
                            subtitle: Text('${threatManager.quarantinedItems.length} berkas terisolasi & terenkripsi', style: AppTypography.bodySm.copyWith(color: AppColors.slateMuted, fontSize: 11)),
                            trailing: const Icon(Icons.arrow_forward_ios_rounded, size: 14, color: AppColors.slateMid),
                            onTap: () => QuarantineVaultSheet.show(context),
                          ),
                          const SizedBox(height: 10),

                          // Pembaruan Aplikasi Card
                          ListTile(
                            contentPadding: const EdgeInsets.symmetric(horizontal: 14, vertical: 2),
                            shape: RoundedRectangleBorder(
                              borderRadius: BorderRadius.circular(14),
                              side: const BorderSide(color: AppColors.slateBorder),
                            ),
                            tileColor: AppColors.background,
                            leading: Container(
                              width: 38,
                              height: 38,
                              decoration: BoxDecoration(
                                color: AppColors.primary.withValues(alpha: 0.1),
                                borderRadius: BorderRadius.circular(10),
                              ),
                              child: const Icon(Icons.system_update_rounded, color: AppColors.primary, size: 20),
                            ),
                            title: Text(
                              'Pembaruan Aplikasi',
                              style: AppTypography.labelMd.copyWith(fontWeight: FontWeight.w700),
                              maxLines: 1,
                              overflow: TextOverflow.ellipsis,
                            ),
                            subtitle: Text(
                              _isAppUpdated ? 'Versi $_appVersion • Versi Terkini' : 'Versi baru tersedia • Ketuk untuk unduh',
                              style: AppTypography.bodySm.copyWith(color: AppColors.slateMuted, fontSize: 11),
                              maxLines: 1,
                              overflow: TextOverflow.ellipsis,
                            ),
                            trailing: _checkingUpdate
                                ? const SizedBox(width: 22, height: 22, child: CircularProgressIndicator(strokeWidth: 2, color: AppColors.primary))
                                : (_isAppUpdated ? _buildUpdatedBadge() : _buildUpdateButton('Update', AppColors.primary)),
                            onTap: _checkingUpdate ? null : _handleManualCheckUpdate,
                          ),
                          const SizedBox(height: 16),
                        ],
                      ),
                    ),
            ),
          ],
        ),
      ),
    );
  }

  Future<void> _handleManualCheckUpdate() async {
    setState(() => _checkingUpdate = true);
    final service = AppUpdateService();
    final updateInfo = await service.checkForUpdate();
    if (!mounted) return;
    setState(() => _checkingUpdate = false);

    if (updateInfo == null) {
      setState(() => _isAppUpdated = true);
      _showFeedback('Aplikasi TelkomSecure $_appVersion sudah aktif & mutakhir.');
      return;
    }
    if (updateInfo.hasUpdate) {
      setState(() => _isAppUpdated = false);
      Navigator.pop(context);
      AppUpdateDialog.show(context, updateInfo: updateInfo);
    } else {
      setState(() => _isAppUpdated = true);
      _showFeedback('Aplikasi TelkomSecure sudah menggunakan versi terbaru (UPDATED).');
    }
  }
}
