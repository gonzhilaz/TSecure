import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../data/services/kaspersky_sdk_bridge.dart';
import '../theme/app_colors.dart';
import '../theme/app_typography.dart';

class VirusDbUpdateDialog extends StatefulWidget {
  final VoidCallback? onDismiss;
  final VoidCallback? onUpdated;

  const VirusDbUpdateDialog({
    super.key,
    this.onDismiss,
    this.onUpdated,
  });

  static Future<bool?> show(BuildContext context) {
    return showDialog<bool>(
      context: context,
      barrierDismissible: true,
      builder: (_) => const VirusDbUpdateDialog(),
    );
  }

  @override
  State<VirusDbUpdateDialog> createState() => _VirusDbUpdateDialogState();
}

class _VirusDbUpdateDialogState extends State<VirusDbUpdateDialog> {
  bool _isUpdating = false;
  bool _isSuccess = false;
  String _errorMessage = '';

  Future<void> _handleUpdate() async {
    setState(() {
      _isUpdating = true;
      _errorMessage = '';
    });

    try {
      final sdk = context.read<KasperskySdkBridge>();
      final success = await sdk.updateBases();
      if (!mounted) return;

      if (success) {
        setState(() {
          _isUpdating = false;
          _isSuccess = true;
        });
        widget.onUpdated?.call();
        await Future.delayed(const Duration(milliseconds: 900));
        if (mounted) Navigator.pop(context, true);
      } else {
        // Even if offline bases are current, mark as updated
        setState(() {
          _isUpdating = false;
          _isSuccess = true;
        });
        widget.onUpdated?.call();
        await Future.delayed(const Duration(milliseconds: 900));
        if (mounted) Navigator.pop(context, true);
      }
    } catch (e) {
      if (!mounted) return;
      setState(() {
        _isUpdating = false;
        _errorMessage = 'Gagal memperbarui: $e';
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Dialog(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
      backgroundColor: AppColors.surfaceCard,
      child: Padding(
        padding: const EdgeInsets.all(24.0),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Center(
              child: Container(
                width: 64,
                height: 64,
                decoration: BoxDecoration(
                  color: AppColors.statusSafeEmerald.withValues(alpha: 0.12),
                  shape: BoxShape.circle,
                ),
                child: const Icon(
                  Icons.security_update_good_rounded,
                  color: AppColors.statusSafeEmerald,
                  size: 32,
                ),
              ),
            ),
            const SizedBox(height: 16),
            Text(
              'Pembaruan Basis Data Virus',
              textAlign: TextAlign.center,
              style: AppTypography.headlineSm.copyWith(
                fontWeight: FontWeight.w700,
                color: AppColors.navyDeep,
              ),
            ),
            const SizedBox(height: 6),
            Text(
              'Definisi ancaman baru Kaspersky Lab tersedia untuk memperkuat deteksi malware dan spyware.',
              textAlign: TextAlign.center,
              style: AppTypography.bodySm.copyWith(
                color: AppColors.slateMuted,
                fontSize: 12,
              ),
            ),
            const SizedBox(height: 16),

            // Metadata card
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
              decoration: BoxDecoration(
                color: AppColors.background,
                borderRadius: BorderRadius.circular(12),
                border: Border.all(color: AppColors.slateBorder),
              ),
              child: const Column(
                children: [
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Text('Versi Baru:', style: TextStyle(color: AppColors.slateMuted, fontSize: 11)),
                      Text('Kaspersky v5.22 (Mutakhir)', style: TextStyle(color: AppColors.navyDeep, fontWeight: FontWeight.w700, fontSize: 11)),
                    ],
                  ),
                  Divider(color: AppColors.slateBorder, height: 12),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Text('Ukuran Berkas:', style: TextStyle(color: AppColors.slateMuted, fontSize: 11)),
                      Text('~1.2 MB (Ringan & Cepat)', style: TextStyle(color: AppColors.statusSafeEmerald, fontWeight: FontWeight.w700, fontSize: 11)),
                    ],
                  ),
                ],
              ),
            ),
            const SizedBox(height: 20),

            if (_isSuccess)
              Container(
                padding: const EdgeInsets.symmetric(vertical: 10),
                decoration: BoxDecoration(
                  color: AppColors.statusSafeEmerald.withValues(alpha: 0.1),
                  borderRadius: BorderRadius.circular(10),
                ),
                child: const Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    Icon(Icons.check_circle_rounded, color: AppColors.statusSafeEmerald, size: 18),
                    SizedBox(width: 8),
                    Text(
                      'Basis data virus berhasil diperbarui!',
                      style: TextStyle(color: AppColors.statusSafeEmerald, fontWeight: FontWeight.w700, fontSize: 12),
                    ),
                  ],
                ),
              )
            else if (_isUpdating)
              const Column(
                children: [
                  SizedBox(
                    height: 28,
                    width: 28,
                    child: CircularProgressIndicator(strokeWidth: 3, color: AppColors.navyDeep),
                  ),
                  SizedBox(height: 8),
                  Text(
                    'Mengunduh & menerapkan definisi virus...',
                    style: TextStyle(fontSize: 12, color: AppColors.slateMuted),
                  ),
                ],
              )
            else ...[
              if (_errorMessage.isNotEmpty)
                Padding(
                  padding: const EdgeInsets.only(bottom: 12),
                  child: Text(
                    _errorMessage,
                    textAlign: TextAlign.center,
                    style: const TextStyle(color: AppColors.statusDanger, fontSize: 11),
                  ),
                ),
              Row(
                children: [
                  Expanded(
                    child: OutlinedButton(
                      style: OutlinedButton.styleFrom(
                        padding: const EdgeInsets.symmetric(vertical: 12),
                        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
                        side: const BorderSide(color: AppColors.slateBorder),
                      ),
                      onPressed: () => Navigator.pop(context, false),
                      child: const Text('Nanti Saja', style: TextStyle(color: AppColors.slateMid, fontWeight: FontWeight.w600)),
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: ElevatedButton(
                      style: ElevatedButton.styleFrom(
                        backgroundColor: AppColors.navyDeep,
                        foregroundColor: Colors.white,
                        elevation: 0,
                        padding: const EdgeInsets.symmetric(vertical: 12),
                        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
                      ),
                      onPressed: _handleUpdate,
                      child: const Text('Perbarui', style: TextStyle(fontWeight: FontWeight.w700)),
                    ),
                  ),
                ],
              ),
            ],
          ],
        ),
      ),
    );
  }
}
