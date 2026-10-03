import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_typography.dart';
import '../../../../data/services/kaspersky_sdk_bridge.dart';

class SimWatchCard extends StatelessWidget {
  const SimWatchCard({super.key});

  @override
  Widget build(BuildContext context) {
    final sdk = context.watch<KasperskySdkBridge>();

    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: AppColors.surfaceCard,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: AppColors.slateBorder),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Container(
                padding: const EdgeInsets.all(10),
                decoration: BoxDecoration(
                  color: sdk.simWatchEnabled
                      ? AppColors.primary
                      : AppColors.slateDivider,
                  borderRadius: BorderRadius.circular(12),
                ),
                child: Icon(
                  Icons.sim_card_outlined,
                  color: sdk.simWatchEnabled ? Colors.white : AppColors.navyDeep,
                  size: 22,
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Anti-Theft SIM Watch',
                      style: AppTypography.headlineSm.copyWith(
                        color: AppColors.navyDeep,
                        fontWeight: FontWeight.w700,
                        fontSize: 16,
                      ),
                    ),
                    const SizedBox(height: 2),
                    Text(
                      'Proteksi Penggantian Kartu SIM Fisik',
                      style: AppTypography.bodySm.copyWith(
                        color: AppColors.slateMuted,
                        fontSize: 12,
                      ),
                    ),
                  ],
                ),
              ),
              Switch(
                value: sdk.simWatchEnabled,
                activeThumbColor: AppColors.primary,
                onChanged: sdk.toggleSimWatch,
              ),
            ],
          ),
          const SizedBox(height: 14),
          Container(
            padding: const EdgeInsets.all(12),
            decoration: BoxDecoration(
              color: AppColors.surface,
              borderRadius: BorderRadius.circular(12),
              border: Border.all(color: AppColors.slateBorder),
            ),
            child: Column(
              children: [
                _buildInfoRow(
                  label: 'Status Kartu SIM',
                  value: sdk.boundSimSlot,
                  trailing: Container(
                    width: 8,
                    height: 8,
                    decoration: const BoxDecoration(
                      color: AppColors.statusSafeEmerald,
                      shape: BoxShape.circle,
                    ),
                  ),
                ),
                const Divider(color: AppColors.slateBorder, height: 16),
                _buildInfoRow(
                  label: 'Binding Kriptografis',
                  value: sdk.boundIccidMasked,
                ),
                const Divider(color: AppColors.slateBorder, height: 16),
                _buildInfoRow(
                  label: 'Respon Pencurian',
                  value: sdk.simWatchEnabled
                      ? 'Auto-Lock Layar & Sirene'
                      : 'Proteksi Nonaktif',
                ),
                const Divider(color: AppColors.slateBorder, height: 16),
                _buildInfoRow(
                  label: 'Kontak Darurat (SMS)',
                  value: sdk.emergencyContact,
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildInfoRow({
    required String label,
    required String value,
    Widget? trailing,
  }) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        Text(
          label,
          style: AppTypography.bodySm.copyWith(
            color: AppColors.slateMuted,
            fontSize: 12,
          ),
        ),
        const SizedBox(width: 8),
        Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            if (trailing != null) ...[
              trailing,
              const SizedBox(width: 6),
            ],
            Text(
              value,
              style: AppTypography.bodySm.copyWith(
                color: AppColors.navyDeep,
                fontWeight: FontWeight.w600,
                fontSize: 12,
              ),
            ),
          ],
        ),
      ],
    );
  }
}
