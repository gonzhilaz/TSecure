import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/theme/app_colors.dart';
import '../../core/theme/app_typography.dart';
import '../../core/widgets/app_button.dart';
import 'auth_controller.dart';
import 'verifikasi_otp_screen.dart';

class MasukScreen extends StatefulWidget {
  const MasukScreen({super.key});

  @override
  State<MasukScreen> createState() => _MasukScreenState();
}

class _MasukScreenState extends State<MasukScreen> {
  final _phoneController = TextEditingController(text: '081299887766');

  @override
  void dispose() {
    _phoneController.dispose();
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
                      decoration: const BoxDecoration(
                        color: AppColors.surface,
                        borderRadius: BorderRadius.only(
                          topLeft: Radius.circular(24),
                          topRight: Radius.circular(24),
                        ),
                      ),
                      padding: const EdgeInsets.symmetric(
                        horizontal: 24.0,
                        vertical: 20.0,
                      ),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Center(
                            child: Container(
                              width: 44,
                              height: 4,
                              decoration: BoxDecoration(
                                color: AppColors.slateBorder,
                                borderRadius: BorderRadius.circular(2),
                              ),
                            ),
                          ),
                          const SizedBox(height: 24),
                          Text('Masuk', style: AppTypography.headlineLg),
                          const SizedBox(height: 6),
                          Text(
                            'Gunakan nomor MyTelkomsel Anda',
                            style: AppTypography.bodyMd,
                          ),
                          const SizedBox(height: 28),
                          Text('Nomor Ponsel', style: AppTypography.labelMd),
                          const SizedBox(height: 8),
                          _buildPhoneInput(),
                          const SizedBox(height: 10),
                          _buildQuickPresets(),
                          const SizedBox(height: 14),
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
                          const SizedBox(height: 12),
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

  Widget _buildQuickPresets() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          children: [
            const Icon(Icons.science_outlined, size: 13, color: AppColors.slateMuted),
            const SizedBox(width: 4),
            Text(
              'Preset Uji Coba POC:',
              style: AppTypography.labelSm.copyWith(
                color: AppColors.slateMuted,
                fontSize: 11,
              ),
            ),
          ],
        ),
        const SizedBox(height: 6),
        Wrap(
          spacing: 6,
          runSpacing: 4,
          children: [
            _PresetChip(
              label: 'Aktif (30 Hari)',
              color: AppColors.statusSafeEmerald,
              onTap: () => setState(() => _phoneController.text = '081299887766'),
            ),
            _PresetChip(
              label: 'Masa Aktif Habis',
              color: AppColors.statusDanger,
              onTap: () => setState(() => _phoneController.text = '081200000000'),
            ),
            _PresetChip(
              label: 'Belum Terdaftar',
              color: AppColors.slateMuted,
              onTap: () => setState(() => _phoneController.text = '081288880000'),
            ),
          ],
        ),
      ],
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
                ),
              ),
              Text(
                'SECURITY',
                style: AppTypography.labelSm.copyWith(
                  color: AppColors.primary,
                  letterSpacing: 2.0,
                ),
              ),
            ],
          ),
          IconButton(
            onPressed: () {},
            icon: const Icon(Icons.more_horiz, color: AppColors.slateMid),
            style: IconButton.styleFrom(
              backgroundColor: AppColors.slateDivider,
              padding: const EdgeInsets.all(8),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildPhoneInput() {
    return Container(
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.circular(10),
        border: Border.all(color: AppColors.slateBorder),
      ),
      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 4),
      child: Row(
        children: [
          const Text('🇮🇩', style: TextStyle(fontSize: 20)),
          const SizedBox(width: 8),
          Text(
            '+62',
            style: AppTypography.labelLg.copyWith(color: AppColors.textPrimary),
          ),
          Container(
            height: 24,
            width: 1,
            color: AppColors.slateBorder,
            margin: const EdgeInsets.symmetric(horizontal: 12),
          ),
          Expanded(
            child: TextField(
              controller: _phoneController,
              keyboardType: TextInputType.phone,
              style: AppTypography.labelLg.copyWith(
                fontWeight: FontWeight.w600,
                letterSpacing: 0.5,
              ),
              decoration: const InputDecoration(
                border: InputBorder.none,
                enabledBorder: InputBorder.none,
                focusedBorder: InputBorder.none,
                contentPadding: EdgeInsets.zero,
                hintText: '812-3456-7890',
              ),
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
              children: const [
                TextSpan(text: 'Saya menyetujui '),
                TextSpan(
                  text: 'Ketentuan Layanan',
                  style: TextStyle(
                    color: AppColors.primary,
                    fontWeight: FontWeight.w600,
                    decoration: TextDecoration.underline,
                  ),
                ),
                TextSpan(text: ' & '),
                TextSpan(
                  text: 'Kebijakan Privasi',
                  style: TextStyle(
                    color: AppColors.primary,
                    fontWeight: FontWeight.w600,
                    decoration: TextDecoration.underline,
                  ),
                ),
                TextSpan(text: ' Telkomsel (UU PDP No. 27/2022)'),
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
            children: const [
              TextSpan(text: 'Masuk instan tanpa OTP. Butuh bantuan? Hubungi '),
              TextSpan(
                text: 'GraPARI 188',
                style: TextStyle(
                  fontWeight: FontWeight.w700,
                  color: AppColors.textPrimary,
                ),
              ),
              TextSpan(text: ' atau '),
              TextSpan(
                text: 'Veronika',
                style: TextStyle(
                  fontWeight: FontWeight.w700,
                  color: AppColors.primary,
                ),
              ),
            ],
          ),
        ),
      ],
    );
  }
}

class _PresetChip extends StatelessWidget {
  final String label;
  final Color color;
  final VoidCallback onTap;

  const _PresetChip({
    required this.label,
    required this.color,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(12),
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
        decoration: BoxDecoration(
          color: color.withValues(alpha: 0.1),
          borderRadius: BorderRadius.circular(12),
          border: Border.all(color: color.withValues(alpha: 0.3)),
        ),
        child: Text(
          label,
          style: TextStyle(
            fontSize: 11,
            fontWeight: FontWeight.w600,
            color: color,
          ),
        ),
      ),
    );
  }
}
