import 'package:flutter/material.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_typography.dart';
import '../../../data/models/user_session.dart';

class AccountInfoCard extends StatelessWidget {
  final UserSession session;

  const AccountInfoCard({
    super.key,
    required this.session,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(
        color: AppColors.surfaceCard,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: AppColors.slateBorder),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'INFORMASI AKUN',
            style: AppTypography.labelSm.copyWith(
              color: AppColors.slateMuted,
              fontWeight: FontWeight.w700,
              letterSpacing: 1.0,
            ),
          ),
          const SizedBox(height: 16),
          Row(
            children: [
              Container(
                width: 38,
                height: 38,
                decoration: BoxDecoration(
                  color: AppColors.slateDivider,
                  borderRadius: BorderRadius.circular(10),
                ),
                child: const Icon(
                  Icons.phone_outlined,
                  size: 20,
                  color: AppColors.navyDeep,
                ),
              ),
              const SizedBox(width: 14),
              Expanded(
                child: Text(
                  session.msisdn,
                  style: AppTypography.labelLg.copyWith(
                    color: AppColors.navyDeep,
                    fontWeight: FontWeight.w600,
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 14),
          Row(
            children: [
              Container(
                width: 38,
                height: 38,
                decoration: BoxDecoration(
                  color: AppColors.slateDivider,
                  borderRadius: BorderRadius.circular(10),
                ),
                child: const Icon(
                  Icons.location_on_outlined,
                  size: 20,
                  color: AppColors.navyDeep,
                ),
              ),
              const SizedBox(width: 14),
              Expanded(
                child: Text(
                  session.location,
                  style: AppTypography.labelLg.copyWith(
                    color: AppColors.navyDeep,
                    fontWeight: FontWeight.w600,
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 16),
          const Divider(color: AppColors.slateDivider, height: 1),
          const SizedBox(height: 14),
          Text(
            'Perlindungan identitas & enkripsi transmisi data cloud aktif 24/7 di seluruh jaringan Telkomsel.',
            style: AppTypography.bodySm.copyWith(
              color: AppColors.slateMuted,
            ),
          ),
          const SizedBox(height: 16),
          Wrap(
            spacing: 8,
            runSpacing: 8,
            children: const [
              _BadgePill(label: 'Telkomsel Guard'),
              _BadgePill(label: 'Halo VIP'),
              _BadgePill(label: 'Prioritas'),
            ],
          ),
        ],
      ),
    );
  }
}

class _BadgePill extends StatelessWidget {
  final String label;

  const _BadgePill({required this.label});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 6),
      decoration: BoxDecoration(
        color: AppColors.slateDivider,
        borderRadius: BorderRadius.circular(20),
      ),
      child: Text(
        label,
        style: AppTypography.labelSm.copyWith(
          color: AppColors.navyDeep,
          fontWeight: FontWeight.w600,
        ),
      ),
    );
  }
}
