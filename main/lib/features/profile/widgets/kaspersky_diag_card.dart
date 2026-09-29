import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:provider/provider.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_typography.dart';
import '../../../../core/widgets/security_test_lab_sheet.dart';
import '../../../data/services/kaspersky_sdk_bridge.dart';

import '../profile_controller.dart';

class KasperskyDiagCard extends StatefulWidget {
  const KasperskyDiagCard({super.key});

  @override
  State<KasperskyDiagCard> createState() => _KasperskyDiagCardState();
}

class _KasperskyDiagCardState extends State<KasperskyDiagCard> {
  bool _isLoading = false;
  Map<String, dynamic>? _liveDiagData;

  @override
  void initState() {
    super.initState();
    _fetchLiveDiag();
  }

  Future<void> _fetchLiveDiag() async {
    setState(() => _isLoading = true);
    try {
      final profileController = context.read<ProfileController>();
      await profileController.loadProfileData();
    } catch (_) {}
    if (mounted) {
      final bridge = context.read<KasperskySdkBridge>();
      setState(() {
        _isLoading = false;
        _liveDiagData = bridge.rawSdkStatus;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    final ksp = context.watch<KasperskySdkBridge>();
    final isInitialized = ksp.isInitialized;
    final hwid = ksp.hardwareIdHash.isNotEmpty ? ksp.hardwareIdHash : (_liveDiagData?['hardwareIdHash'] as String? ?? '-');
    final instId = ksp.installationId.isNotEmpty ? ksp.installationId : (_liveDiagData?['installationId'] as String? ?? '-');
    final sdkVersion = _liveDiagData?['sdkVersion'] as String? ?? '5.21.0.209';
    final bases = _liveDiagData?['virusDbPath'] as String? ?? 'bases.aac (Internal Bundle)';

    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: AppColors.slateBorder),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.04),
            blurRadius: 10,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Container(
                padding: const EdgeInsets.all(8),
                decoration: BoxDecoration(
                  color: (isInitialized ? AppColors.statusSafeEmerald : AppColors.statusWarning)
                      .withValues(alpha: 0.12),
                  borderRadius: BorderRadius.circular(10),
                ),
                child: Icon(
                  isInitialized ? Icons.verified_user_rounded : Icons.warning_amber_rounded,
                  color: isInitialized ? AppColors.statusSafeEmerald : AppColors.statusWarning,
                  size: 20,
                ),
              ),
              const SizedBox(width: 10),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text('Kaspersky Mobile SDK Native Engine', style: AppTypography.labelLg),
                    Text(
                      isInitialized ? 'Engine Aktif & Terlindungi' : 'Menunggu Aktivasi Native',
                      style: AppTypography.bodySm.copyWith(
                        color: isInitialized ? AppColors.statusSafeEmerald : AppColors.statusWarning,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                  ],
                ),
              ),
              IconButton(
                icon: _isLoading
                    ? const SizedBox(
                        width: 16,
                        height: 16,
                        child: CircularProgressIndicator(strokeWidth: 2, color: AppColors.primary),
                      )
                    : const Icon(Icons.refresh, size: 20, color: AppColors.slateMuted),
                onPressed: _isLoading ? null : _fetchLiveDiag,
                tooltip: 'Audit Ulang Native SDK',
              ),
            ],
          ),
          const SizedBox(height: 14),
          const Divider(height: 1, color: AppColors.slateBorder),
          const SizedBox(height: 12),
          _buildRow('Versi SDK Engine', sdkVersion),
          _buildRow('Status Lisensi Native', isInitialized ? 'AKTIF (Terverifikasi NDP)' : 'DINONAKTIFKAN / BELUM AKTIF'),
          _buildRow('Kedaluwarsa Lisensi', isInitialized ? '${DateFormat('dd MMM yyyy, HH:mm').format(ksp.licenseExpiryDate)} WIB' : '-'),
          _buildRow('Basis Virus Engine', bases),
          _buildRow('Hardware ID Hash', hwid),
          _buildRow('Installation GUID', instId),
          const SizedBox(height: 14),
          SizedBox(
            width: double.infinity,
            height: 40,
            child: OutlinedButton.icon(
              style: OutlinedButton.styleFrom(
                foregroundColor: AppColors.primary,
                side: const BorderSide(color: AppColors.primary),
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
              ),
              icon: const Icon(Icons.science_outlined, size: 18),
              label: const Text('Buka Lab Uji Keamanan (EICAR & KSN)', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 13)),
              onPressed: () => SecurityTestLabSheet.show(context, sdk: ksp),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildRow(String label, String value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4.0),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(
            width: 135,
            child: Text(
              label,
              style: AppTypography.bodySm.copyWith(color: AppColors.slateMuted, fontSize: 12),
            ),
          ),
          const SizedBox(width: 8),
          Expanded(
            child: Text(
              value,
              textAlign: TextAlign.right,
              maxLines: 2,
              overflow: TextOverflow.ellipsis,
              style: AppTypography.bodySm.copyWith(
                fontWeight: FontWeight.w600,
                color: AppColors.navyDeep,
                fontSize: 12,
              ),
            ),
          ),
        ],
      ),
    );
  }
}
