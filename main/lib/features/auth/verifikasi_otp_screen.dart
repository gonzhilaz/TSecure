import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:provider/provider.dart';
import '../../core/theme/app_colors.dart';
import '../../core/theme/app_typography.dart';
import '../../core/widgets/app_button.dart';
import '../shell/main_shell_screen.dart';
import 'aktivasi_layanan_screen.dart';
import 'auth_controller.dart';

class VerifikasiOtpScreen extends StatefulWidget {
  final String msisdn;

  const VerifikasiOtpScreen({
    super.key,
    required this.msisdn,
  });

  @override
  State<VerifikasiOtpScreen> createState() => _VerifikasiOtpScreenState();
}

class _VerifikasiOtpScreenState extends State<VerifikasiOtpScreen> {
  final List<TextEditingController> _controllers =
      List.generate(6, (_) => TextEditingController());
  final List<FocusNode> _focusNodes = List.generate(6, (_) => FocusNode());

  @override
  void dispose() {
    for (final c in _controllers) {
      c.dispose();
    }
    for (final f in _focusNodes) {
      f.dispose();
    }
    super.dispose();
  }

  String get _otpCode => _controllers.map((c) => c.text).join();

  void _onDigitChanged(int index, String value) {
    if (value.isNotEmpty) {
      if (value.length > 1) {
        // Handle paste full OTP
        final clean = value.replaceAll(RegExp(r'\D'), '');
        for (int i = 0; i < 6 && i < clean.length; i++) {
          _controllers[i].text = clean[i];
        }
        if (clean.length >= 6) {
          _focusNodes[5].unfocus();
          _onVerify();
          return;
        } else {
          _focusNodes[clean.length.clamp(0, 5)].requestFocus();
        }
      } else {
        if (index < 5) {
          _focusNodes[index + 1].requestFocus();
        } else {
          _focusNodes[index].unfocus();
          _onVerify();
        }
      }
    }
    setState(() {});
  }

  void _fillMockOtp(String code) {
    for (int i = 0; i < 6 && i < code.length; i++) {
      _controllers[i].text = code[i];
    }
    setState(() {});
    _onVerify();
  }

  Future<void> _onVerify() async {
    final code = _otpCode;
    if (code.length < 6) return;

    final controller = context.read<AuthController>();
    final success = await controller.verifyOtp(code);

    if (!mounted) return;

    if (success) {
      if (controller.needsActivation) {
        Navigator.of(context).pushReplacement(
          MaterialPageRoute(builder: (_) => const AktivasiLayananScreen()),
        );
      } else {
        Navigator.of(context).pushAndRemoveUntil(
          MaterialPageRoute(builder: (_) => const MainShellScreen()),
          (route) => false,
        );
      }
    } else if (controller.errorMessage != null) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(controller.errorMessage!),
          backgroundColor: AppColors.statusDanger,
        ),
      );
    }
  }

  Future<void> _onResend() async {
    final controller = context.read<AuthController>();
    final success = await controller.resendOtp();
    if (!mounted) return;

    if (success) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Kode OTP baru telah dikirim via SMS.'),
          backgroundColor: AppColors.statusSafeEmerald,
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
    final mockOtp = authController.lastMockOtp ?? '123456';
    final countdown = authController.resendCountdown;

    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        elevation: 0,
        leading: IconButton(
          icon: const Icon(Icons.arrow_back_ios_new, size: 20, color: AppColors.navyDeep),
          onPressed: () => Navigator.of(context).pop(),
        ),
        title: Text('Verifikasi OTP', style: AppTypography.headlineSm.copyWith(color: AppColors.navyDeep)),
        centerTitle: true,
      ),
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.symmetric(horizontal: 24.0, vertical: 16.0),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              const SizedBox(height: 12),
              _buildHeaderInfo(),
              const SizedBox(height: 28),
              _buildOtpInputs(),
              const SizedBox(height: 20),
              _buildDemoAssistantChip(mockOtp),
              const SizedBox(height: 36),
              AppButton(
                label: 'Verifikasi & Lanjutkan',
                icon: Icons.check_circle_outline,
                isLoading: authController.isLoading,
                onPressed: _otpCode.length == 6 && !authController.isLoading ? _onVerify : null,
              ),
              const SizedBox(height: 24),
              _buildResendSection(countdown, authController.isLoading),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildHeaderInfo() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text('Masukkan Kode Verifikasi', style: AppTypography.headlineLg),
        const SizedBox(height: 8),
        RichText(
          text: TextSpan(
            style: AppTypography.bodyMd.copyWith(color: AppColors.slateMuted),
            children: [
              const TextSpan(text: 'Kode 6-digit telah dikirim melalui SMS ke nomor '),
              TextSpan(
                text: widget.msisdn,
                style: const TextStyle(fontWeight: FontWeight.w700, color: AppColors.navyDeep),
              ),
              const TextSpan(text: '. Pastikan nomor Anda aktif.'),
            ],
          ),
        ),
      ],
    );
  }

  Widget _buildOtpInputs() {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: List.generate(6, (index) {
        return SizedBox(
          width: 48,
          height: 56,
          child: TextField(
            controller: _controllers[index],
            focusNode: _focusNodes[index],
            keyboardType: TextInputType.number,
            textAlign: TextAlign.center,
            maxLength: 1,
            style: AppTypography.headlineSm.copyWith(
              color: AppColors.navyDeep,
              fontWeight: FontWeight.w800,
            ),
            decoration: InputDecoration(
              counterText: '',
              filled: true,
              fillColor: AppColors.surface,
              contentPadding: EdgeInsets.zero,
              border: OutlineInputBorder(
                borderRadius: BorderRadius.circular(12),
                borderSide: const BorderSide(color: AppColors.slateBorder),
              ),
              enabledBorder: OutlineInputBorder(
                borderRadius: BorderRadius.circular(12),
                borderSide: BorderSide(
                  color: _controllers[index].text.isNotEmpty
                      ? AppColors.primary
                      : AppColors.slateBorder,
                  width: _controllers[index].text.isNotEmpty ? 1.5 : 1.0,
                ),
              ),
              focusedBorder: OutlineInputBorder(
                borderRadius: BorderRadius.circular(12),
                borderSide: const BorderSide(color: AppColors.primary, width: 2.0),
              ),
            ),
            inputFormatters: [FilteringTextInputFormatter.digitsOnly],
            onChanged: (val) => _onDigitChanged(index, val),
          ),
        );
      }),
    );
  }

  Widget _buildDemoAssistantChip(String mockOtp) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
      decoration: BoxDecoration(
        color: AppColors.primary.withValues(alpha: 0.08),
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: AppColors.primary.withValues(alpha: 0.2)),
      ),
      child: Row(
        children: [
          const Icon(Icons.mark_email_unread_outlined, size: 20, color: AppColors.primary),
          const SizedBox(width: 10),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'Simulasi SMS Gateway Telkomsel',
                  style: TextStyle(
                    fontSize: 11,
                    fontWeight: FontWeight.w700,
                    color: AppColors.primary,
                  ),
                ),
                Text(
                  'Gunakan kode POC: $mockOtp',
                  style: const TextStyle(fontSize: 12, color: AppColors.navyDeep),
                ),
              ],
            ),
          ),
          InkWell(
            onTap: () => _fillMockOtp(mockOtp),
            borderRadius: BorderRadius.circular(8),
            child: Container(
              padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
              decoration: BoxDecoration(
                color: AppColors.primary,
                borderRadius: BorderRadius.circular(8),
              ),
              child: const Text(
                'Isi Cepat',
                style: TextStyle(
                  color: Colors.white,
                  fontSize: 11,
                  fontWeight: FontWeight.w700,
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildResendSection(int countdown, bool isLoading) {
    return Center(
      child: countdown > 0
          ? Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                const Icon(Icons.timer_outlined, size: 16, color: AppColors.slateMuted),
                const SizedBox(width: 6),
                Text(
                  'Kirim ulang kode dalam ${countdown}d',
                  style: AppTypography.bodySm.copyWith(color: AppColors.slateMuted),
                ),
              ],
            )
          : TextButton.icon(
              onPressed: isLoading ? null : _onResend,
              icon: const Icon(Icons.refresh, size: 16, color: AppColors.primary),
              label: Text(
                'Kirim Ulang Kode OTP',
                style: AppTypography.labelMd.copyWith(
                  color: AppColors.primary,
                  fontWeight: FontWeight.w700,
                ),
              ),
            ),
    );
  }
}
