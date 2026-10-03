import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../data/models/quarantine_item.dart';
import '../../data/services/threat_manager_service.dart';
import '../theme/app_colors.dart';
import '../theme/app_typography.dart';
import 'app_button.dart';

class QuarantineVaultSheet extends StatelessWidget {
  const QuarantineVaultSheet({super.key});

  static void show(BuildContext context) {
    context.read<ThreatManagerService>().loadQuarantinedItems();
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (_) => const QuarantineVaultSheet(),
    );
  }

  @override
  Widget build(BuildContext context) {
    final threatManager = context.watch<ThreatManagerService>();
    final items = threatManager.quarantinedItems;

    return Container(
      constraints: BoxConstraints(
        maxHeight: MediaQuery.of(context).size.height * 0.88,
      ),
      decoration: const BoxDecoration(
        color: AppColors.surfaceCard,
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      child: SafeArea(
        top: false,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            const SizedBox(height: 12),
            Center(
              child: Container(
                width: 40,
                height: 4,
                decoration: BoxDecoration(
                  color: AppColors.slateBorder,
                  borderRadius: BorderRadius.circular(2),
                ),
              ),
            ),
            const SizedBox(height: 12),
            _buildHeader(context, items.length),
            const Divider(color: AppColors.slateBorder, height: 1),
            Expanded(
              child: threatManager.isLoadingVault
                  ? const Center(child: CircularProgressIndicator(color: AppColors.primary))
                  : items.isEmpty
                      ? _buildEmptyState()
                      : _buildItemList(context, items, threatManager),
            ),
            if (items.isNotEmpty) ...[
              const Divider(color: AppColors.slateBorder, height: 1),
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 12),
                child: AppButton(
                  label: 'Kosongkan Semua Brankas',
                  type: AppButtonType.secondary,
                  onPressed: () => _confirmClearAll(context, threatManager),
                ),
              ),
            ],
          ],
        ),
      ),
    );
  }

  Widget _buildHeader(BuildContext context, int count) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 6),
      child: Row(
        children: [
          Container(
            width: 40,
            height: 40,
            decoration: BoxDecoration(
              color: AppColors.primary.withValues(alpha: 0.1),
              borderRadius: BorderRadius.circular(10),
            ),
            child: const Icon(Icons.archive_outlined, color: AppColors.primary, size: 22),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'Brankas Karantina',
                  style: AppTypography.headlineSm.copyWith(
                    fontWeight: FontWeight.w700,
                    color: AppColors.navyDeep,
                  ),
                ),
                Text(
                  '$count berkas terisolasi & dienkripsi aman',
                  style: AppTypography.bodySm.copyWith(color: AppColors.slateMuted, fontSize: 12),
                ),
              ],
            ),
          ),
          IconButton(
            icon: const Icon(Icons.close),
            onPressed: () => Navigator.pop(context),
          ),
        ],
      ),
    );
  }

  Widget _buildEmptyState() {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(32.0),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Container(
              padding: const EdgeInsets.all(20),
              decoration: BoxDecoration(
                color: AppColors.statusSafeEmerald.withValues(alpha: 0.12),
                shape: BoxShape.circle,
              ),
              child: const Icon(Icons.shield_outlined, color: AppColors.statusSafeEmerald, size: 48),
            ),
            const SizedBox(height: 16),
            Text(
              'Brankas Bersih & Aman',
              style: AppTypography.headlineSm.copyWith(
                fontWeight: FontWeight.w700,
                color: AppColors.navyDeep,
              ),
            ),
            const SizedBox(height: 6),
            Text(
              'Tidak ada berkas malware yang sedang dikarantina. Sistem Anda terlindung dengan baik.',
              textAlign: TextAlign.center,
              style: AppTypography.bodySm.copyWith(color: AppColors.slateMuted),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildItemList(
    BuildContext context,
    List<QuarantineItem> items,
    ThreatManagerService manager,
  ) {
    return ListView.separated(
      padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 16),
      itemCount: items.length,
      separatorBuilder: (context, index) => const SizedBox(height: 12),
      itemBuilder: (ctx, index) {
        final item = items[index];
        return _buildItemCard(ctx, item, manager);
      },
    );
  }

  Widget _buildItemCard(
    BuildContext context,
    QuarantineItem item,
    ThreatManagerService manager,
  ) {
    return Container(
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: AppColors.background,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: AppColors.slateBorder),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Container(
                padding: const EdgeInsets.all(8),
                decoration: BoxDecoration(
                  color: AppColors.statusDanger.withValues(alpha: 0.1),
                  borderRadius: BorderRadius.circular(8),
                ),
                child: const Icon(Icons.lock_outline, color: AppColors.statusDanger, size: 18),
              ),
              const SizedBox(width: 10),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      item.fileName,
                      style: AppTypography.labelMd.copyWith(fontWeight: FontWeight.w700),
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                    ),
                    Text(
                      item.threatName,
                      style: AppTypography.bodySm.copyWith(
                        color: AppColors.statusDanger,
                        fontSize: 11,
                        fontWeight: FontWeight.w600,
                      ),
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                    ),
                  ],
                ),
              ),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                decoration: BoxDecoration(
                  color: AppColors.primary.withValues(alpha: 0.1),
                  borderRadius: BorderRadius.circular(6),
                ),
                child: Text(
                  item.severity,
                  style: const TextStyle(
                    color: AppColors.primary,
                    fontSize: 10,
                    fontWeight: FontWeight.w700,
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 8),
          Text(
            item.originalPath,
            style: AppTypography.bodySm.copyWith(color: AppColors.slateMuted, fontSize: 11),
            maxLines: 1,
            overflow: TextOverflow.ellipsis,
          ),
          const SizedBox(height: 6),
          Row(
            children: [
              Text(
                'Ukuran: ${item.formattedSize}',
                style: AppTypography.labelSm.copyWith(color: AppColors.slateMid, fontSize: 11),
              ),
              const Spacer(),
              Text(
                item.quarantineDate,
                style: AppTypography.labelSm.copyWith(color: AppColors.slateMid, fontSize: 10),
              ),
            ],
          ),
          const SizedBox(height: 12),
          Row(
            children: [
              Expanded(
                child: OutlinedButton.icon(
                  onPressed: () => _confirmRestore(context, item, manager),
                  icon: const Icon(Icons.restore_page_outlined, size: 16),
                  label: const Text('Pulihkan', style: TextStyle(fontSize: 12)),
                  style: OutlinedButton.styleFrom(
                    foregroundColor: AppColors.statusSafeEmerald,
                    side: const BorderSide(color: AppColors.statusSafeEmerald),
                    padding: const EdgeInsets.symmetric(vertical: 8),
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
                  ),
                ),
              ),
              const SizedBox(width: 10),
              Expanded(
                child: OutlinedButton.icon(
                  onPressed: () => _confirmDelete(context, item, manager),
                  icon: const Icon(Icons.delete_forever_outlined, size: 16),
                  label: const Text('Hapus Permanen', style: TextStyle(fontSize: 12)),
                  style: OutlinedButton.styleFrom(
                    foregroundColor: AppColors.statusDanger,
                    side: const BorderSide(color: AppColors.statusDanger),
                    padding: const EdgeInsets.symmetric(vertical: 8),
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
                  ),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  void _confirmRestore(BuildContext context, QuarantineItem item, ThreatManagerService manager) {
    showDialog(
      context: context,
      builder: (dialogCtx) => AlertDialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
        title: const Text('Pulihkan Berkas?'),
        content: Text('Berkas ${item.fileName} akan dikembalikan ke lokasi semula (${item.originalPath}). Pastikan berkas ini aman.'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(dialogCtx),
            child: const Text('Batal'),
          ),
          ElevatedButton(
            style: ElevatedButton.styleFrom(backgroundColor: AppColors.statusSafeEmerald),
            onPressed: () async {
              Navigator.pop(dialogCtx);
              final ok = await manager.restoreQuarantinedItem(item.id);
              if (context.mounted) {
                ScaffoldMessenger.of(context).showSnackBar(SnackBar(
                  content: Text(ok ? 'Berkas berhasil dipulihkan.' : 'Gagal memulihkan berkas.'),
                  backgroundColor: ok ? AppColors.statusSafeEmerald : AppColors.statusDanger,
                ));
              }
            },
            child: const Text('Pulihkan', style: TextStyle(color: Colors.white)),
          ),
        ],
      ),
    );
  }

  void _confirmDelete(BuildContext context, QuarantineItem item, ThreatManagerService manager) {
    showDialog(
      context: context,
      builder: (dialogCtx) => AlertDialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
        title: const Text('Hapus Permanen?'),
        content: Text('Berkas ${item.fileName} akan dimusnahkan secara permanen dari penyimpanan perangkat.'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(dialogCtx),
            child: const Text('Batal'),
          ),
          ElevatedButton(
            style: ElevatedButton.styleFrom(backgroundColor: AppColors.statusDanger),
            onPressed: () async {
              Navigator.pop(dialogCtx);
              final ok = await manager.deleteQuarantinedItem(item.id);
              if (context.mounted) {
                ScaffoldMessenger.of(context).showSnackBar(SnackBar(
                  content: Text(ok ? 'Berkas berhasil dimusnahkan.' : 'Gagal menghapus berkas.'),
                  backgroundColor: ok ? AppColors.navyDeep : AppColors.statusDanger,
                ));
              }
            },
            child: const Text('Musnahkan', style: TextStyle(color: Colors.white)),
          ),
        ],
      ),
    );
  }

  void _confirmClearAll(BuildContext context, ThreatManagerService manager) {
    showDialog(
      context: context,
      builder: (dialogCtx) => AlertDialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
        title: const Text('Kosongkan Semua Brankas?'),
        content: const Text('Semua berkas yang berada di brankas karantina akan dihapus secara permanen dan tidak dapat dipulihkan lagi.'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(dialogCtx),
            child: const Text('Batal'),
          ),
          ElevatedButton(
            style: ElevatedButton.styleFrom(backgroundColor: AppColors.statusDanger),
            onPressed: () async {
              Navigator.pop(dialogCtx);
              await manager.clearAllQuarantineVault();
              if (context.mounted) {
                ScaffoldMessenger.of(context).showSnackBar(const SnackBar(
                  content: Text('Seluruh isi brankas karantina telah dibersihkan.'),
                  backgroundColor: AppColors.navyDeep,
                ));
              }
            },
            child: const Text('Kosongkan', style: TextStyle(color: Colors.white)),
          ),
        ],
      ),
    );
  }
}
