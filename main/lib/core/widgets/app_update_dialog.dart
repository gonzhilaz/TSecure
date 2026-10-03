import 'package:flutter/material.dart';
import '../../data/services/app_update_service.dart';
import '../theme/app_colors.dart';
import '../theme/app_typography.dart';
import 'app_button.dart';

enum UpdateState { prompt, downloading, readyToInstall, error }

class AppUpdateDialog extends StatefulWidget {
  final AppUpdateInfo updateInfo;
  final VoidCallback? onDismiss;

  const AppUpdateDialog({
    super.key,
    required this.updateInfo,
    this.onDismiss,
  });

  static Future<void> show(
    BuildContext context, {
    required AppUpdateInfo updateInfo,
  }) {
    return showDialog(
      context: context,
      barrierDismissible: !updateInfo.isMandatory,
      builder: (_) => AppUpdateDialog(updateInfo: updateInfo),
    );
  }

  @override
  State<AppUpdateDialog> createState() => _AppUpdateDialogState();
}

class _AppUpdateDialogState extends State<AppUpdateDialog> {
  final _service = AppUpdateService();
  UpdateState _state = UpdateState.prompt;
  double _downloadProgress = 0.0;
  String _downloadedPath = '';
  String _errorMessage = '';

  Future<void> _startDownload() async {
    setState(() {
      _state = UpdateState.downloading;
      _downloadProgress = 0.0;
      _errorMessage = '';
    });

    final path = await _service.downloadApk(
      widget.updateInfo.downloadUrl,
      onProgress: (progress, received, total) {
        if (mounted) {
          setState(() {
            _downloadProgress = progress;
          });
        }
      },
    );

    if (!mounted) return;

    if (path != null && path.isNotEmpty) {
      setState(() {
        _downloadedPath = path;
        _state = UpdateState.readyToInstall;
      });
      // Automatically trigger package installer
      await _installApk();
    } else {
      setState(() {
        _state = UpdateState.error;
        _errorMessage = 'Gagal mengunduh berkas APK pembaruan. Silakan periksa koneksi jaringan Anda.';
      });
    }
  }

  Future<void> _installApk() async {
    if (_downloadedPath.isEmpty) return;
    final hasPerm = await _service.checkInstallPermission();
    if (!hasPerm) {
      await _service.requestInstallPermission();
      if (mounted) {
        setState(() {
          _state = UpdateState.readyToInstall;
          _errorMessage = 'Silakan aktifkan izin "Install dari sumber tidak dikenal" pada setelan, lalu tekan Pasang.';
        });
      }
      return;
    }
    final ok = await _service.installApk(_downloadedPath);
    if (!ok && mounted) {
      setState(() {
        _state = UpdateState.readyToInstall;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return PopScope(
      canPop: !widget.updateInfo.isMandatory && _state != UpdateState.downloading,
      child: Dialog(
        backgroundColor: Colors.white,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
        insetPadding: const EdgeInsets.symmetric(horizontal: 24, vertical: 24),
        child: Padding(
          padding: const EdgeInsets.all(22.0),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              _buildHeader(),
              const SizedBox(height: 18),
              if (_state == UpdateState.prompt) _buildPromptBody(),
              if (_state == UpdateState.downloading) _buildDownloadingBody(),
              if (_state == UpdateState.readyToInstall) _buildReadyBody(),
              if (_state == UpdateState.error) _buildErrorBody(),
              const SizedBox(height: 22),
              _buildActions(),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildHeader() {
    return Row(
      children: [
        Container(
          width: 48,
          height: 48,
          decoration: BoxDecoration(
            color: AppColors.primary.withValues(alpha: 0.1),
            borderRadius: BorderRadius.circular(14),
          ),
          child: const Center(
            child: Icon(
              Icons.system_update_rounded,
              color: AppColors.primary,
              size: 26,
            ),
          ),
        ),
        const SizedBox(width: 14),
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                'Pembaruan Tersedia',
                style: AppTypography.headlineSm.copyWith(
                  fontWeight: FontWeight.w700,
                  color: AppColors.navyDeep,
                ),
              ),
              const SizedBox(height: 2),
              Row(
                children: [
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                    decoration: BoxDecoration(
                      color: AppColors.primary,
                      borderRadius: BorderRadius.circular(4),
                    ),
                    child: Text(
                      'v${widget.updateInfo.latestVersion}',
                      style: const TextStyle(
                        color: Colors.white,
                        fontSize: 10,
                        fontWeight: FontWeight.w700,
                      ),
                    ),
                  ),
                  const SizedBox(width: 6),
                  Text(
                    '${widget.updateInfo.apkSizeMb.toStringAsFixed(1)} MB',
                    style: AppTypography.bodySm.copyWith(
                      color: AppColors.slateMuted,
                      fontSize: 11,
                    ),
                  ),
                ],
              ),
            ],
          ),
        ),
      ],
    );
  }

  Widget _buildPromptBody() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          'Versi terbaru Telkomsel Secure telah siap dipasang dengan peningkatan keamanan dan performa:',
          style: AppTypography.bodySm.copyWith(color: AppColors.textSecondary),
        ),
        const SizedBox(height: 12),
        Container(
          padding: const EdgeInsets.all(12),
          decoration: BoxDecoration(
            color: AppColors.background,
            borderRadius: BorderRadius.circular(12),
            border: Border.all(color: AppColors.slateBorder),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: widget.updateInfo.releaseNotes.map((note) {
              return Padding(
                padding: const EdgeInsets.symmetric(vertical: 3.0),
                child: Row(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Padding(
                      padding: EdgeInsets.only(top: 2.0),
                      child: Icon(
                        Icons.check_circle_rounded,
                        size: 14,
                        color: AppColors.statusSafeEmerald,
                      ),
                    ),
                    const SizedBox(width: 8),
                    Expanded(
                      child: Text(
                        note,
                        style: AppTypography.bodySm.copyWith(
                          color: AppColors.navyDeep,
                          fontSize: 12,
                        ),
                      ),
                    ),
                  ],
                ),
              );
            }).toList(),
          ),
        ),
      ],
    );
  }

  Widget _buildDownloadingBody() {
    final pct = (_downloadProgress * 100).toInt();
    return Column(
      children: [
        const SizedBox(height: 8),
        ClipRRect(
          borderRadius: BorderRadius.circular(8),
          child: LinearProgressIndicator(
            value: _downloadProgress > 0 ? _downloadProgress : null,
            backgroundColor: AppColors.slateBorder,
            valueColor: const AlwaysStoppedAnimation<Color>(AppColors.primary),
            minHeight: 8,
          ),
        ),
        const SizedBox(height: 12),
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text(
              'Mengunduh berkas APK...',
              style: AppTypography.bodySm.copyWith(color: AppColors.slateMid),
            ),
            Text(
              '$pct%',
              style: AppTypography.labelMd.copyWith(
                fontWeight: FontWeight.w700,
                color: AppColors.primary,
              ),
            ),
          ],
        ),
        const SizedBox(height: 6),
      ],
    );
  }

  Widget _buildReadyBody() {
    return Column(
      children: [
        const Icon(
          Icons.task_alt_rounded,
          color: AppColors.statusSafeEmerald,
          size: 40,
        ),
        const SizedBox(height: 10),
        Text(
          'Unduhan Selesai',
          style: AppTypography.headlineSm.copyWith(
            fontWeight: FontWeight.w700,
            color: AppColors.navyDeep,
          ),
        ),
        const SizedBox(height: 4),
        Text(
          'Silakan tekan tombol di bawah untuk melanjutkan pemasangan paket aplikasi.',
          textAlign: TextAlign.center,
          style: AppTypography.bodySm.copyWith(color: AppColors.textSecondary),
        ),
      ],
    );
  }

  Widget _buildErrorBody() {
    return Column(
      children: [
        const Icon(
          Icons.error_outline_rounded,
          color: AppColors.statusDanger,
          size: 36,
        ),
        const SizedBox(height: 8),
        Text(
          _errorMessage,
          textAlign: TextAlign.center,
          style: AppTypography.bodySm.copyWith(color: AppColors.statusDanger),
        ),
      ],
    );
  }

  Widget _buildActions() {
    if (_state == UpdateState.downloading) {
      return const SizedBox.shrink();
    }

    if (_state == UpdateState.readyToInstall) {
      return AppButton(
        label: 'Pasang Sekarang',
        icon: Icons.install_mobile_rounded,
        onPressed: _installApk,
      );
    }

    if (_state == UpdateState.error) {
      return Row(
        children: [
          if (!widget.updateInfo.isMandatory)
            Expanded(
              child: TextButton(
                onPressed: () => Navigator.pop(context),
                child: Text('Tutup', style: AppTypography.labelMd.copyWith(color: AppColors.slateMuted)),
              ),
            ),
          Expanded(
            child: AppButton(
              label: 'Coba Lagi',
              icon: Icons.refresh_rounded,
              onPressed: _startDownload,
            ),
          ),
        ],
      );
    }

    // Default prompt actions
    return Row(
      children: [
        if (!widget.updateInfo.isMandatory) ...[
          Expanded(
            child: TextButton(
              onPressed: () => Navigator.pop(context),
              child: Text(
                'Nanti Saja',
                style: AppTypography.labelMd.copyWith(color: AppColors.slateMuted),
              ),
            ),
          ),
          const SizedBox(width: 8),
        ],
        Expanded(
          flex: widget.updateInfo.isMandatory ? 1 : 2,
          child: AppButton(
            label: 'Perbarui Sekarang',
            icon: Icons.download_rounded,
            onPressed: _startDownload,
          ),
        ),
      ],
    );
  }
}
