import 'package:flutter/material.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_typography.dart';
import '../../../core/widgets/app_button.dart';

class ActivationStageCard extends StatelessWidget {
  final int stageNum;
  final String title;
  final String subtitle;
  final bool isActive;
  final bool isCompleted;
  final bool isWarning;
  final bool isRunning;
  final String? warningBadgeText;
  final Widget? warningContent;
  final Widget? customBody;

  const ActivationStageCard({
    super.key,
    required this.stageNum,
    required this.title,
    required this.subtitle,
    required this.isActive,
    required this.isCompleted,
    required this.isWarning,
    required this.isRunning,
    this.warningBadgeText,
    this.warningContent,
    this.customBody,
  });

  @override
  Widget build(BuildContext context) {
    Color borderColor = AppColors.slateBorder;
    if (isWarning) {
      borderColor = AppColors.statusWarning;
    } else if (isCompleted) {
      borderColor = AppColors.statusSafeEmerald;
    } else if (isActive) {
      borderColor = AppColors.primary;
    }

    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: AppColors.surfaceCard,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: borderColor, width: isActive || isWarning ? 1.5 : 1),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              _buildIcon(),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      title,
                      style: AppTypography.headlineSm.copyWith(
                        fontSize: 15,
                        fontWeight: FontWeight.w700,
                        color: AppColors.navyDeep,
                      ),
                    ),
                    if (subtitle.isNotEmpty) ...[
                      const SizedBox(height: 2),
                      Text(
                        subtitle,
                        style: AppTypography.bodySm.copyWith(color: AppColors.slateMuted),
                      ),
                    ],
                  ],
                ),
              ),
              if (isWarning && warningBadgeText != null)
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                  decoration: BoxDecoration(
                    color: AppColors.statusWarning.withValues(alpha: 0.15),
                    borderRadius: BorderRadius.circular(6),
                  ),
                  child: Text(
                    warningBadgeText!,
                    style: const TextStyle(
                      fontSize: 11,
                      fontWeight: FontWeight.w700,
                      color: AppColors.statusWarning,
                    ),
                  ),
                ),
            ],
          ),
          if (isWarning && warningContent != null) ...[
            const SizedBox(height: 12),
            warningContent!,
          ],
          if (customBody != null) ...[
            const SizedBox(height: 12),
            customBody!,
          ],
        ],
      ),
    );
  }

  Widget _buildIcon() {
    if (isWarning) {
      return Container(
        width: 32,
        height: 32,
        decoration: const BoxDecoration(color: AppColors.statusWarning, shape: BoxShape.circle),
        child: const Icon(Icons.priority_high_rounded, color: Colors.white, size: 18),
      );
    }
    if (isCompleted) {
      return Container(
        width: 32,
        height: 32,
        decoration: const BoxDecoration(color: AppColors.statusSafeEmerald, shape: BoxShape.circle),
        child: const Icon(Icons.check, color: Colors.white, size: 20),
      );
    }
    if (isActive && isRunning) {
      return const SizedBox(
        width: 32,
        height: 32,
        child: CircularProgressIndicator(strokeWidth: 2.5, color: AppColors.primary),
      );
    }
    return Container(
      width: 32,
      height: 32,
      decoration: BoxDecoration(
        color: AppColors.background,
        shape: BoxShape.circle,
        border: Border.all(color: AppColors.slateBorder),
      ),
      child: Center(
        child: Text(
          '$stageNum',
          style: const TextStyle(fontWeight: FontWeight.w700, color: AppColors.slateMuted),
        ),
      ),
    );
  }
}

class Stage1WarningWidget extends StatelessWidget {
  final bool isRunning;
  final VoidCallback onRetry;

  const Stage1WarningWidget({
    super.key,
    required this.isRunning,
    required this.onRetry,
  });

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        Text(
          'Paket Anda sedang disinkronkan oleh sistem MyTelkomsel. Silakan periksa kembali setelah beberapa saat.',
          style: AppTypography.bodySm.copyWith(color: AppColors.navyDeep),
        ),
        const SizedBox(height: 12),
        AppButton(
          label: 'Cek Ulang Status',
          icon: Icons.refresh,
          type: AppButtonType.primary,
          isLoading: isRunning,
          onPressed: onRetry,
        ),
      ],
    );
  }
}

class Stage2WarningWidget extends StatelessWidget {
  final VoidCallback onContinue;

  const Stage2WarningWidget({
    super.key,
    required this.onContinue,
  });

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        Text(
          'Server Kaspersky mengalami antrean penerbitan lisensi. Paket MyTelkomsel Anda valid dan Anda tetap dapat masuk ke aplikasi.',
          style: AppTypography.bodySm.copyWith(color: AppColors.navyDeep),
        ),
        const SizedBox(height: 6),
        Text(
          'Anda dapat menekan "Sinkronkan Ulang" kapan saja melalui menu Profil.',
          style: AppTypography.bodySm.copyWith(
            color: AppColors.slateMuted,
            fontStyle: FontStyle.italic,
          ),
        ),
        const SizedBox(height: 12),
        AppButton(
          label: 'Lanjut ke Aplikasi',
          icon: Icons.arrow_forward,
          type: AppButtonType.primary,
          onPressed: onContinue,
        ),
      ],
    );
  }
}

class Stage3CelebrationWidget extends StatelessWidget {
  const Stage3CelebrationWidget({super.key});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(vertical: 16),
      alignment: Alignment.center,
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.center,
        children: [
          const Icon(
            Icons.check_circle_rounded,
            size: 64,
            color: AppColors.statusSafeEmerald,
          ),
          const SizedBox(height: 12),
          Text(
            'Perangkat Berhasil Dilindungi',
            textAlign: TextAlign.center,
            style: AppTypography.headlineSm.copyWith(
              fontWeight: FontWeight.w800,
              color: AppColors.statusSafeEmerald,
            ),
          ),
        ],
      ),
    );
  }
}

class DemoPresetsBar extends StatelessWidget {
  final VoidCallback onNormal;
  final VoidCallback onDelay;
  final VoidCallback onKspOutage;

  const DemoPresetsBar({
    super.key,
    required this.onNormal,
    required this.onDelay,
    required this.onKspOutage,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
      decoration: const BoxDecoration(
        color: AppColors.surface,
        border: Border(top: BorderSide(color: AppColors.slateBorder)),
      ),
      child: Row(
        children: [
          const Text('POC:', style: TextStyle(fontSize: 11, fontWeight: FontWeight.bold)),
          const SizedBox(width: 8),
          Expanded(
            child: SingleChildScrollView(
              scrollDirection: Axis.horizontal,
              child: Row(
                children: [
                  _chip('Normal', onNormal),
                  const SizedBox(width: 6),
                  _chip('Delay "Dalam Proses..."', onDelay),
                  const SizedBox(width: 6),
                  _chip('Gangguan KSP', onKspOutage),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _chip(String label, VoidCallback onTap) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(16),
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
        decoration: BoxDecoration(
          color: AppColors.background,
          borderRadius: BorderRadius.circular(16),
          border: Border.all(color: AppColors.slateBorder),
        ),
        child: Text(
          label,
          style: const TextStyle(fontSize: 11, fontWeight: FontWeight.w600, color: AppColors.navyDeep),
        ),
      ),
    );
  }
}
