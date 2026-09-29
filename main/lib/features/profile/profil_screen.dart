import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../../core/constants/app_constants.dart';
import '../../core/theme/app_colors.dart';
import '../../core/theme/app_typography.dart';
import '../../core/widgets/app_button.dart';
import '../../core/widgets/settings_sheet.dart';
import '../../core/widgets/tri_state_view.dart';
import '../splash/splash_screen.dart';
import 'profile_controller.dart';
import 'widgets/account_info_card.dart';
import 'widgets/kaspersky_diag_card.dart';
import 'widgets/license_status_card.dart';

class ProfilScreen extends StatefulWidget {
  final VoidCallback? onBack;

  const ProfilScreen({super.key, this.onBack});

  @override
  State<ProfilScreen> createState() => _ProfilScreenState();
}

class _ProfilScreenState extends State<ProfilScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<ProfileController>().loadProfileData();
    });
  }

  void _showLogoutDialog(BuildContext context) {
    showDialog(
      context: context,
      builder: (dialogCtx) => AlertDialog(
        backgroundColor: Colors.white,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
        title: Text(
          'Keluar dari Akun?',
          style: AppTypography.headlineSm.copyWith(
            fontWeight: FontWeight.w700,
            color: AppColors.navyDeep,
          ),
        ),
        content: Text(
          'Anda akan keluar dari sesi Telkomsel Secure. Lisensi keamanan akan diverifikasi ulang saat masuk kembali.',
          style: AppTypography.bodySm.copyWith(color: AppColors.textSecondary),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(dialogCtx),
            child: Text(
              'Batal',
              style: AppTypography.labelMd.copyWith(color: AppColors.slateMuted),
            ),
          ),
          ElevatedButton(
            onPressed: () async {
              Navigator.pop(dialogCtx);
              final prefs = await SharedPreferences.getInstance();
              await prefs.setBool(AppConstants.keyIsLoggedIn, false);
              await prefs.remove(AppConstants.keySessionToken);
              await prefs.remove(AppConstants.keySessionExpiry);
              if (!context.mounted) return;
              Navigator.of(context).pushAndRemoveUntil(
                MaterialPageRoute(builder: (_) => const SplashScreen()),
                (route) => false,
              );
            },
            style: ElevatedButton.styleFrom(
              backgroundColor: AppColors.primary,
              foregroundColor: Colors.white,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(8),
              ),
            ),
            child: const Text('Ya, Keluar'),
          ),
        ],
      ),
    );
  }

  void _showInfoSnackbar(BuildContext context, String message) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message),
        backgroundColor: AppColors.navyDeep,
        behavior: SnackBarBehavior.floating,
        duration: const Duration(seconds: 2),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final controller = context.watch<ProfileController>();

    return Scaffold(
      backgroundColor: AppColors.background,
      body: TriStateView(
        state: controller.state,
        onRetry: controller.loadProfileData,
        child: RefreshIndicator(
          color: AppColors.primary,
          onRefresh: controller.loadProfileData,
          child: SingleChildScrollView(
            physics: const AlwaysScrollableScrollPhysics(),
            child: Column(
              children: [
                _buildTopHeader(context),
                Transform.translate(
                  offset: const Offset(0, -50),
                  child: Padding(
                    padding: const EdgeInsets.symmetric(horizontal: 20.0),
                    child: Column(
                      children: [
                        _buildAvatar(),
                        const SizedBox(height: 14),
                        Text(
                          controller.userSession?.name ?? 'R. Aryandi',
                          style: AppTypography.headlineLg.copyWith(
                            fontWeight: FontWeight.w800,
                            color: AppColors.navyDeep,
                          ),
                        ),
                        const SizedBox(height: 4),
                        Row(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            Text(
                              '${controller.userSession?.msisdn ?? "+62 82-141414-875"} • ',
                              style: AppTypography.bodySm.copyWith(
                                color: AppColors.slateMuted,
                              ),
                            ),
                            Text(
                              controller.userSession?.tier ?? 'Telkomsel Halo Diamond',
                              style: AppTypography.bodySm.copyWith(
                                color: AppColors.primary,
                                fontWeight: FontWeight.w700,
                              ),
                            ),
                          ],
                        ),
                      const SizedBox(height: 18),
                      _buildActionButtons(context),
                      const SizedBox(height: 24),
                      if (controller.activePeriod != null)
                        LicenseStatusCard(
                          activePeriod: controller.activePeriod!,
                          onRetrySync: () async {
                            final success = await controller.retryActivation();
                            if (!context.mounted) return;
                            ScaffoldMessenger.of(context).showSnackBar(
                              SnackBar(
                                content: Text(
                                  success
                                      ? 'Perangkat Berhasil Dilindungi'
                                      : 'Sinkronisasi masih dalam proses. Silakan coba lagi.',
                                ),
                                backgroundColor: success
                                    ? AppColors.statusSafeEmerald
                                    : AppColors.statusWarning,
                              ),
                            );
                          },
                        ),
                      const SizedBox(height: 16),
                      const KasperskyDiagCard(),
                      const SizedBox(height: 16),
                      if (controller.userSession != null)
                        AccountInfoCard(
                          session: controller.userSession!,
                        ),
                      const SizedBox(height: 24),
                      AppButton(
                        label: 'Keluar dari Akun',
                        type: AppButtonType.outline,
                        icon: Icons.logout_rounded,
                        onPressed: () => _showLogoutDialog(context),
                      ),
                      const SizedBox(height: 32),
                    ],
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    ),
  );
}

  Widget _buildTopHeader(BuildContext context) {
    return Container(
      height: 150,
      decoration: const BoxDecoration(
        color: AppColors.primary,
      ),
      child: SafeArea(
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 8.0),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              IconButton(
                icon: const Icon(Icons.arrow_back, color: Colors.white),
                onPressed: () {
                  if (widget.onBack != null) {
                    widget.onBack!();
                  } else if (Navigator.canPop(context)) {
                    Navigator.pop(context);
                  }
                },
                style: IconButton.styleFrom(
                  backgroundColor: Colors.white.withValues(alpha: 0.15),
                ),
              ),
              IconButton(
                icon: const Icon(Icons.settings_outlined, color: Colors.white),
                onPressed: () => SettingsSheet.show(context),
                style: IconButton.styleFrom(
                  backgroundColor: Colors.white.withValues(alpha: 0.15),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildAvatar() {
    return Stack(
      children: [
        Container(
          width: 96,
          height: 96,
          decoration: BoxDecoration(
            color: AppColors.primary,
            shape: BoxShape.circle,
            border: Border.all(color: Colors.white, width: 4),
            boxShadow: [
              BoxShadow(
                color: Colors.black.withValues(alpha: 0.12),
                blurRadius: 16,
                offset: const Offset(0, 4),
              ),
            ],
          ),
          child: const Center(
            child: Text(
              'RA',
              style: TextStyle(
                color: Colors.white,
                fontSize: 32,
                fontWeight: FontWeight.w800,
              ),
            ),
          ),
        ),
        Positioned(
          bottom: 4,
          right: 4,
          child: Container(
            width: 18,
            height: 18,
            decoration: BoxDecoration(
              color: AppColors.statusSafeEmerald,
              shape: BoxShape.circle,
              border: Border.all(color: Colors.white, width: 3),
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildActionButtons(BuildContext context) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        _buildCircleButton(
          Icons.chat_bubble_outline,
          'Bantuan Veronika',
          () => _showInfoSnackbar(context, 'Membuka layanan Veronika Telkomsel AI Assistant...'),
        ),
        const SizedBox(width: 16),
        _buildCircleButton(
          Icons.edit_outlined,
          'Ubah Profil',
          () => _showInfoSnackbar(context, 'Fitur ubah profil disinkronkan dengan MyTelkomsel ID.'),
        ),
        const SizedBox(width: 16),
        _buildCircleButton(
          Icons.share_outlined,
          'Bagikan Status',
          () => _showInfoSnackbar(context, 'Mempersiapkan laporan status keamanan untuk dibagikan...'),
        ),
        const SizedBox(width: 16),
        _buildCircleButton(
          Icons.more_horiz,
          'Lainnya',
          () => SettingsSheet.show(context),
        ),
      ],
    );
  }

  Widget _buildCircleButton(IconData icon, String tooltip, VoidCallback onTap) {
    return Material(
      color: Colors.transparent,
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(22),
        child: Container(
          width: 44,
          height: 44,
          decoration: BoxDecoration(
            color: AppColors.surface,
            shape: BoxShape.circle,
            border: Border.all(color: AppColors.slateBorder),
          ),
          child: Center(
            child: Icon(icon, color: AppColors.navyDeep, size: 20),
          ),
        ),
      ),
    );
  }
}
