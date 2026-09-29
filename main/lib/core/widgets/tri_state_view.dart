import 'package:flutter/material.dart';
import '../theme/app_colors.dart';
import '../theme/app_typography.dart';
import 'app_button.dart';
import 'security_shimmer.dart';

enum ViewState { loading, empty, error, success }

class TriStateView extends StatelessWidget {
  final ViewState state;
  final Widget child;
  final Widget? loadingWidget;
  final String emptyTitle;
  final String emptySubtitle;
  final IconData emptyIcon;
  final String errorMessage;
  final VoidCallback? onRetry;

  const TriStateView({
    super.key,
    required this.state,
    required this.child,
    this.loadingWidget,
    this.emptyTitle = 'Belum Ada Data',
    this.emptySubtitle = 'Data akan muncul secara otomatis saat aktivitas tercatat.',
    this.emptyIcon = Icons.inbox_outlined,
    this.errorMessage = 'Terjadi kendala saat memuat data keamanan.',
    this.onRetry,
  });

  @override
  Widget build(BuildContext context) {
    switch (state) {
      case ViewState.loading:
        return loadingWidget ?? _buildDefaultLoading();
      case ViewState.empty:
        return _buildEmpty();
      case ViewState.error:
        return _buildError();
      case ViewState.success:
        return child;
    }
  }

  Widget _buildDefaultLoading() {
    return Padding(
      padding: const EdgeInsets.all(16.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: const [
          SecurityShimmer(width: double.infinity, height: 120),
          SizedBox(height: 16),
          SecurityShimmer(width: double.infinity, height: 80),
          SizedBox(height: 16),
          SecurityShimmer(width: double.infinity, height: 80),
        ],
      ),
    );
  }

  Widget _buildEmpty() {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(24.0),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: AppColors.slateDivider,
                shape: BoxShape.circle,
              ),
              child: Icon(emptyIcon, size: 40, color: AppColors.slateMuted),
            ),
            const SizedBox(height: 16),
            Text(
              emptyTitle,
              style: AppTypography.headlineSm,
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 8),
            Text(
              emptySubtitle,
              style: AppTypography.bodyMd,
              textAlign: TextAlign.center,
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildError() {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(24.0),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: AppColors.statusDangerBg,
                shape: BoxShape.circle,
              ),
              child: const Icon(
                Icons.error_outline,
                size: 40,
                color: AppColors.statusDanger,
              ),
            ),
            const SizedBox(height: 16),
            Text(
              'Kendala Sistem',
              style: AppTypography.headlineSm,
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 8),
            Text(
              errorMessage,
              style: AppTypography.bodyMd,
              textAlign: TextAlign.center,
            ),
            if (onRetry != null) ...[
              const SizedBox(height: 20),
              AppButton(
                label: 'Coba Lagi',
                width: 140,
                height: 42,
                type: AppButtonType.primary,
                onPressed: onRetry,
              ),
            ],
          ],
        ),
      ),
    );
  }
}
