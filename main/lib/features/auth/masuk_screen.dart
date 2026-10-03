import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/theme/app_colors.dart';
import '../../core/theme/app_typography.dart';
import '../../core/widgets/app_button.dart';
import '../../core/widgets/settings_sheet.dart';
import 'auth_controller.dart';
import 'verifikasi_otp_screen.dart';
import 'widgets/auth_legal_sheet.dart';
import 'widgets/phone_input_field.dart';

class MasukScreen extends StatefulWidget {
  const MasukScreen({super.key});

  @override
  State<MasukScreen> createState() => _MasukScreenState();
}

class _MasukScreenState extends State<MasukScreen> {
  final _phoneController = TextEditingController();
  late final TapGestureRecognizer _termsRecognizer;
  late final TapGestureRecognizer _privacyRecognizer;
  late final TapGestureRecognizer _grapariRecognizer;
  late final TapGestureRecognizer _veronikaRecognizer;

  @override
  void initState() {
    super.initState();
    _termsRecognizer = TapGestureRecognizer()
      ..onTap = () => AuthLegalSheet.showTerms(context);
    _privacyRecognizer = TapGestureRecognizer()
      ..onTap = () => AuthLegalSheet.showPrivacy(context);
    _grapariRecognizer = TapGestureRecognizer()
      ..onTap = () => AuthLegalSheet.showSupportDialog(context, isVeronika: false);
    _veronikaRecognizer = TapGestureRecognizer()
      ..onTap = () => AuthLegalSheet.showSupportDialog(context, isVeronika: true);
  }

  @override
  void dispose() {
    _phoneController.dispose();
    _termsRecognizer.dispose();
    _privacyRecognizer.dispose();
    _grapariRecognizer.dispose();
    _veronikaRecognizer.dispose();
    super.dispose();
  }

  void _onLoginPressed() async {
    final controller = context.read<AuthController>();
    final success = await controller.requestOtp(_phoneController.text);

    if (!mounted) return;

    if (success) {
      Navigator.of(context).push(
        MaterialPageRoute(
          builder: (_) => VerifikasiOtpScreen(
            msisdn: controller.pendingMsisdn ?? _phoneController.text,
          ),
        ),
      );
    } else if (controller.errorMessage != null) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(controller.errorMessage!),
          backgroundColor: AppColors.statusDanger,
        ),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final authController = context.watch<AuthController>();

    return Scaffold(
      backgroundColor: AppColors.background,
      body: SafeArea(
        child: SingleChildScrollView(
          child: ConstrainedBox(
            constraints: BoxConstraints(
              minHeight: MediaQuery.of(context).size.height -
                  MediaQuery.of(context).padding.top -
                  MediaQuery.of(context).padding.bottom,
            ),
            child: IntrinsicHeight(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  const SizedBox(height: 16),
                  _buildHeader(),
                  const SizedBox(height: 32),
                  Expanded(
                    child: Container(
                      decoration: BoxDecoration(
                        color: Colors.white,
                        borderRadius: const BorderRadius.only(
                          topLeft: Radius.circular(36),
                          topRight: Radius.circular(36),
                        ),
                        boxShadow: [
                          BoxShadow(
                            color: Colors.black.withValues(alpha: 0.06),
                            blurRadius: 20,
                            offset: const Offset(0, -6),
                          ),
                        ],
                      ),
                      padding: const EdgeInsets.symmetric(
                        horizontal: 24.0,
                        vertical: 16.0,
                      ),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Center(
                            child: Container(
                              width: 48,
                              height: 4,
                              decoration: BoxDecoration(
                                color: const Color(0xFFCBD5E1),
                                borderRadius: BorderRadius.circular(2),
                              ),
                            ),
                          ),
                          const SizedBox(height: 20),
                          Text(
                            'Masuk',
                            style: AppTypography.headlineLg.copyWith(
                              fontWeight: FontWeight.w700,
                              color: AppColors.navyDeep,
                            ),
                          ),
                          const SizedBox(height: 4),
                          Text(
                            'Gunakan nomor MyTelkomsel Anda',
                            style: AppTypography.bodyMd.copyWith(
                              color: AppColors.slateMuted,
                            ),
                          ),
                          const SizedBox(height: 12),
                          const Divider(color: Color(0xFFF1F5F9), height: 1, thickness: 1),
                          const SizedBox(height: 20),
                          Text(
                            'Nomor Ponsel',
                            style: AppTypography.labelMd.copyWith(
                              color: AppColors.slateMid,
                              fontWeight: FontWeight.w500,
                            ),
                          ),
                          const SizedBox(height: 8),
                          PhoneInputField(controller: _phoneController),
                          const SizedBox(height: 16),
                          _buildPdpAgreement(authController),
                          const Spacer(),
                          AppButton(
                            label: 'Masuk',
                            icon: Icons.arrow_forward,
                            isLoading: authController.isLoading,
                            onPressed: _onLoginPressed,
                          ),
                          const SizedBox(height: 16),
                          _buildFooterNotes(),
                          const SizedBox(height: 14),
                          Center(
                            child: Container(
                              width: 120,
                              height: 4,
                              decoration: BoxDecoration(
                                color: AppColors.navyDeep.withValues(alpha: 0.8),
                                borderRadius: BorderRadius.circular(2),
                              ),
                            ),
                          ),
                          const SizedBox(height: 4),
                        ],
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildHeader() {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 24.0),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          const SizedBox(width: 40),
          Column(
            children: [
              Text(
                'Telkomsel',
                style: AppTypography.headlineSm.copyWith(
                  fontWeight: FontWeight.w800,
                  color: AppColors.navyDeep,
                  letterSpacing: -0.5,
                ),
              ),
              const SizedBox(height: 2),
              Text(
                'SECURE',
                style: AppTypography.labelSm.copyWith(
                  color: AppColors.primary,
                  fontWeight: FontWeight.w800,
                  letterSpacing: 2.2,
                  fontSize: 10,
                ),
              ),
            ],
          ),
          Container(
            width: 40,
            height: 40,
            decoration: BoxDecoration(
              color: Colors.white,
              shape: BoxShape.circle,
              border: Border.all(color: const Color(0xFFE2E8F0)),
              boxShadow: [
                BoxShadow(
                  color: Colors.black.withValues(alpha: 0.04),
                  blurRadius: 4,
                  offset: const Offset(0, 1),
                ),
              ],
            ),
            child: IconButton(
              onPressed: () => SettingsSheet.show(context),
              icon: const Icon(Icons.more_horiz, size: 20, color: AppColors.slateMid),
              padding: EdgeInsets.zero,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildPdpAgreement(AuthController controller) {
    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        SizedBox(
          width: 24,
          height: 24,
          child: Checkbox(
            value: controller.isAgreementChecked,
            activeColor: AppColors.primary,
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(4),
            ),
            onChanged: controller.toggleAgreement,
          ),
        ),
        const SizedBox(width: 10),
        Expanded(
          child: RichText(
            text: TextSpan(
              style: AppTypography.bodySm.copyWith(color: AppColors.slateMid),
              children: [
                const TextSpan(text: 'Saya menyetujui '),
                TextSpan(
                  text: 'Ketentuan Layanan',
                  recognizer: _termsRecognizer,
                  style: const TextStyle(
                    color: AppColors.primary,
                    fontWeight: FontWeight.w600,
                    decoration: TextDecoration.underline,
                  ),
                ),
                const TextSpan(text: ' & '),
                TextSpan(
                  text: 'Kebijakan Privasi',
                  recognizer: _privacyRecognizer,
                  style: const TextStyle(
                    color: AppColors.primary,
                    fontWeight: FontWeight.w600,
                    decoration: TextDecoration.underline,
                  ),
                ),
                const TextSpan(text: ' Telkomsel (UU PDP No. 27/2022)'),
              ],
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildFooterNotes() {
    return Column(
      children: [
        Text(
          'Autentikasi Instan Jaringan Telkomsel • Tanpa Kode OTP',
          style: AppTypography.bodySm.copyWith(
            color: AppColors.slateMuted,
            fontSize: 11,
          ),
          textAlign: TextAlign.center,
        ),
        const SizedBox(height: 6),
        RichText(
          textAlign: TextAlign.center,
          text: TextSpan(
            style: AppTypography.bodySm.copyWith(
              color: AppColors.slateMuted,
              fontSize: 11,
            ),
            children: [
              const TextSpan(text: 'Masuk instan tanpa OTP. Butuh bantuan? Hubungi '),
              TextSpan(
                text: 'GraPARI 188',
                recognizer: _grapariRecognizer,
                style: const TextStyle(
                  fontWeight: FontWeight.w700,
                  color: AppColors.textPrimary,
                  decoration: TextDecoration.underline,
                ),
              ),
              const TextSpan(text: ' atau '),
              TextSpan(
                text: 'Veronika',
                recognizer: _veronikaRecognizer,
                style: const TextStyle(
                  fontWeight: FontWeight.w700,
                  color: AppColors.primary,
                  decoration: TextDecoration.underline,
                ),
              ),
            ],
          ),
        ),
      ],
    );
  }
}
