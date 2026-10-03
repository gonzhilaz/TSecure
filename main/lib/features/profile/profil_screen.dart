import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../../core/constants/app_constants.dart';
import '../../core/theme/app_colors.dart';
import '../../core/theme/app_typography.dart';
import '../../core/widgets/app_button.dart';
import '../../core/widgets/tri_state_view.dart';
import '../../data/models/active_period.dart';
import '../../data/models/user_session.dart';
import '../splash/splash_screen.dart';
import 'profile_controller.dart';
import 'widgets/account_info_card.dart';
import 'widgets/kaspersky_diag_card.dart';
import 'widgets/kaspersky_engine_footer.dart';
import 'widgets/license_status_card.dart';
import 'widgets/profile_header_banner.dart';
import 'widgets/profile_identity_section.dart';

/// Profil Screen pixel-precisely aligned with Stitch specification:
/// - Gradient banner with rounded-xl translucent back & settings buttons
/// - Circular RA avatar overlapping banner with emerald online indicator
/// - 4 rounded-xl squircle action buttons (Message, Edit, Share, More)
/// - Clean Mobile Security Ultimate subscription card
/// - Informasi Akun card with phone, location, note & 3 pill tags
/// - Subtle Kaspersky Security Engine footer at the very bottom
///
/// Modularized architecture with all components under 400 lines.
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
      backgroundColor: const Color(0xFFF7F9FC),
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
                ProfileHeaderBanner(onBack: widget.onBack),
                Transform.translate(
                  offset: const Offset(0, -40),
                  child: Padding(
                    padding: const EdgeInsets.symmetric(horizontal: 20.0),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.stretch,
                      children: [
                        ProfileIdentitySection(
                          session: controller.userSession,
                          onActionNotice: (msg) => _showInfoSnackbar(context, msg),
                        ),
                        const SizedBox(height: 20),
                        LicenseStatusCard(
                          activePeriod: controller.activePeriod ??
                              ActivePeriod(
                                packageName: 'Mobile Security Ultimate',
                                packageDescription: 'Lisensi Korporasi & Perlindungan Data',
                                expiryDate: DateTime(2026, 10, 14),
                                activeDeviceCount: 1,
                                maxDeviceAllowed: 1,
                                isValid: true,
                                statusMessage: 'Perangkat Dilindungi',
                                activationStatus: 'ACTIVATED',
                              ),
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
                        const SizedBox(height: 14),
                        AccountInfoCard(
                          session: controller.userSession ??
                              UserSession(
                                msisdn: '',
                                name: '',
                                tier: 'Pelanggan Telkomsel',
                                location: 'Indonesia',
                                mobileId: controller.kasperskySdk.installationId,
                              ),
                        ),
                        const SizedBox(height: 14),
                        const KasperskyDiagCard(),
                        const SizedBox(height: 16),

                        // Subtle Security Engine Footer
                        const KasperskyEngineFooter(),
                        const SizedBox(height: 16),

                        // Logout Action Button
                        AppButton(
                          label: 'Keluar dari Akun',
                          type: AppButtonType.outline,
                          icon: Icons.logout_rounded,
                          onPressed: () => _showLogoutDialog(context),
                        ),
                        const SizedBox(height: 28),
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
}
