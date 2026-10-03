import 'package:flutter/material.dart';
import '../theme/app_colors.dart';
import '../theme/app_typography.dart';
import '../../data/services/kaspersky_sdk_bridge.dart';

/// Modal dialog for live testing URLs against Kaspersky Web Filter / KSN Cloud.
class WebFilterTestDialog extends StatefulWidget {
  final KasperskySdkBridge sdk;

  const WebFilterTestDialog({super.key, required this.sdk});

  static Future<void> show(BuildContext context, {required KasperskySdkBridge sdk}) {
    return showDialog(
      context: context,
      builder: (_) => WebFilterTestDialog(sdk: sdk),
    );
  }

  @override
  State<WebFilterTestDialog> createState() => _WebFilterTestDialogState();
}

class _WebFilterTestDialogState extends State<WebFilterTestDialog> {
  final _urlController = TextEditingController(text: 'https://telkomsel.com');
  bool _isLoading = false;
  Map<String, dynamic>? _result;

  @override
  void dispose() {
    _urlController.dispose();
    super.dispose();
  }

  Future<void> _testUrl(String targetUrl) async {
    setState(() {
      _isLoading = true;
      _result = null;
    });

    final res = await widget.sdk.checkUrl(targetUrl);
    if (!mounted) return;

    setState(() {
      _isLoading = false;
      _result = res;
    });
  }

  @override
  Widget build(BuildContext context) {
    final verdict = _result?['verdict'] as String? ?? '';
    final isSafe = _result?['isSafe'] == true;
    final description = _result?['description'] as String? ?? '';

    return Dialog(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
      backgroundColor: AppColors.surfaceCard,
      child: Padding(
        padding: const EdgeInsets.all(20.0),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Container(
                  padding: const EdgeInsets.all(8),
                  decoration: BoxDecoration(
                    color: AppColors.primary.withValues(alpha: 0.1),
                    borderRadius: BorderRadius.circular(10),
                  ),
                  child: const Icon(Icons.language_rounded, color: AppColors.primary, size: 22),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Text('Uji Kaspersky Web Filter', style: AppTypography.headlineSm),
                ),
              ],
            ),
            const SizedBox(height: 14),
            Text(
              'Masukkan URL untuk memverifikasi proteksi anti-phishing dan filter situs berbahaya via Kaspersky KSN:',
              style: AppTypography.bodySm.copyWith(color: AppColors.slateMuted),
            ),
            const SizedBox(height: 14),
            TextField(
              controller: _urlController,
              decoration: InputDecoration(
                hintText: 'https://contoh-situs.com',
                prefixIcon: const Icon(Icons.link_rounded, size: 20),
                contentPadding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
                border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)),
              ),
              keyboardType: TextInputType.url,
            ),
            const SizedBox(height: 12),
            Row(
              children: [
                Expanded(
                  child: OutlinedButton(
                    onPressed: _isLoading ? null : () {
                      _urlController.text = 'http://testsafebrowsing.appspot.com/s/phishing.html';
                      _testUrl(_urlController.text);
                    },
                    style: OutlinedButton.styleFrom(
                      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
                      padding: const EdgeInsets.symmetric(vertical: 8),
                    ),
                    child: const Text('Simulasi Phishing', style: TextStyle(fontSize: 12)),
                  ),
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: ElevatedButton(
                    onPressed: _isLoading ? null : () => _testUrl(_urlController.text.trim()),
                    style: ElevatedButton.styleFrom(
                      backgroundColor: AppColors.primary,
                      foregroundColor: Colors.white,
                      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
                      padding: const EdgeInsets.symmetric(vertical: 8),
                    ),
                    child: _isLoading
                        ? const SizedBox(width: 16, height: 16, child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white))
                        : const Text('Periksa URL', style: TextStyle(fontSize: 12, fontWeight: FontWeight.w700)),
                  ),
                ),
              ],
            ),
            if (_result != null) ...[
              const SizedBox(height: 16),
              Container(
                width: double.infinity,
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: isSafe ? AppColors.statusSafe.withValues(alpha: 0.1) : AppColors.statusDanger.withValues(alpha: 0.1),
                  borderRadius: BorderRadius.circular(12),
                  border: Border.all(
                    color: isSafe ? AppColors.statusSafe : AppColors.statusDanger,
                  ),
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Icon(
                          isSafe ? Icons.check_circle_outline_rounded : Icons.warning_amber_rounded,
                          color: isSafe ? AppColors.statusSafe : AppColors.statusDanger,
                          size: 18,
                        ),
                        const SizedBox(width: 8),
                        Text(
                          'Hasil: $verdict',
                          style: TextStyle(
                            fontWeight: FontWeight.w700,
                            color: isSafe ? AppColors.statusSafe : AppColors.statusDanger,
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 4),
                    Text(
                      description,
                      style: AppTypography.bodySm.copyWith(color: AppColors.navyDeep),
                    ),
                  ],
                ),
              ),
            ],
            const SizedBox(height: 14),
            Align(
              alignment: Alignment.centerRight,
              child: TextButton(
                onPressed: () => Navigator.pop(context),
                child: const Text('Tutup'),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
