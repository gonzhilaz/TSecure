import 'package:flutter/material.dart';
import '../../data/models/activity_log.dart';
import '../../data/models/threat_detail_item.dart';
import '../../data/services/kaspersky_sdk_bridge.dart';
import '../../data/services/threat_manager_service.dart';
import '../theme/app_colors.dart';
import '../theme/app_typography.dart';
import 'app_button.dart';
import 'quarantine_vault_sheet.dart';

class ScanThreatDetailSheet extends StatefulWidget {
  final ActivityLog log;
  final ThreatManagerService threatManager;
  final KasperskySdkBridge? kasperskySdk;

  const ScanThreatDetailSheet({
    super.key,
    required this.log,
    required this.threatManager,
    this.kasperskySdk,
  });

  static void show(
    BuildContext context, {
    required ActivityLog log,
    required ThreatManagerService threatManager,
    KasperskySdkBridge? kasperskySdk,
  }) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (_) => ScanThreatDetailSheet(
        log: log,
        threatManager: threatManager,
        kasperskySdk: kasperskySdk,
      ),
    );
  }

  @override
  State<ScanThreatDetailSheet> createState() => _ScanThreatDetailSheetState();
}

class _ScanThreatDetailSheetState extends State<ScanThreatDetailSheet> {
  late List<ThreatDetailItem> _threats;
  bool _isProcessing = false;

  @override
  void initState() {
    super.initState();
    if (widget.log.threats.isNotEmpty) {
      _threats = List.from(widget.log.threats);
    } else if (widget.kasperskySdk != null && widget.kasperskySdk!.currentScanThreats.isNotEmpty) {
      _threats = List.from(widget.kasperskySdk!.currentScanThreats);
    } else {
      _threats = [];
    }
  }

  void _showSnack(String msg, {Color bg = AppColors.navyDeep, SnackBarAction? action}) {
    if (!mounted) return;
    ScaffoldMessenger.of(context).hideCurrentSnackBar();
    ScaffoldMessenger.of(context).showSnackBar(SnackBar(
      content: Text(msg),
      backgroundColor: bg,
      behavior: SnackBarBehavior.floating,
      action: action,
    ));
  }

  Future<void> _handleClearAll() async {
    setState(() => _isProcessing = true);
    await widget.threatManager.clearAllThreats(widget.log, localThreats: _threats);
    if (widget.kasperskySdk != null) await widget.kasperskySdk!.resolveAllThreats(quarantine: false);
    if (!mounted) return;
    setState(() => _isProcessing = false);
    final anyFailed = _threats.any((t) => t.actionTaken == 'GAGAL');
    _showSnack(
      anyFailed ? 'Sebagian berkas gagal dimusnahkan.' : 'Semua berkas ancaman berhasil dimusnahkan.',
      bg: anyFailed ? AppColors.statusWarning : AppColors.statusSafeEmerald,
    );
  }

  Future<void> _handleQuarantineAll() async {
    setState(() => _isProcessing = true);
    await widget.threatManager.quarantineAllThreats(widget.log, localThreats: _threats);
    if (widget.kasperskySdk != null) await widget.kasperskySdk!.resolveAllThreats(quarantine: true);
    if (!mounted) return;
    setState(() => _isProcessing = false);
    _showSnack(
      'Semua berkas dipindahkan ke Brankas Karantina.',
      bg: AppColors.statusWarning,
      action: SnackBarAction(label: 'Buka Brankas', textColor: Colors.white, onPressed: () => QuarantineVaultSheet.show(context)),
    );
  }

  Future<void> _handleDeleteAllQuarantined() async {
    setState(() => _isProcessing = true);
    for (final t in _threats.where((item) => item.actionTaken == 'DIKARANTINA')) {
      final ok = await widget.threatManager.deleteQuarantinedByFilePath(t.filePath);
      if (ok) t.actionTaken = 'DIBERSIHKAN';
    }
    if (widget.kasperskySdk != null) await widget.kasperskySdk!.resolveAllThreats(quarantine: false);
    if (widget.threatManager.logRepository != null) await widget.threatManager.logRepository!.updateLog(widget.log);
    if (!mounted) return;
    setState(() => _isProcessing = false);
    _showSnack('Semua berkas di karantina telah dimusnahkan secara permanen.', bg: AppColors.statusSafeEmerald);
  }

  Future<void> _handleQuarantineSingle(ThreatDetailItem t) async {
    await widget.threatManager.quarantineThreat(widget.log, t);
    setState(() {});
    _showSnack('Berkas ${t.fileName} dipindahkan ke karantina.', bg: AppColors.statusWarning);
  }

  Future<void> _handleClearSingle(ThreatDetailItem t) async {
    final ok = await widget.threatManager.clearThreat(widget.log, t);
    setState(() {});
    _showSnack(ok ? 'Berkas ${t.fileName} berhasil dimusnahkan.' : 'Gagal menghapus berkas.', bg: ok ? AppColors.statusSafeEmerald : AppColors.statusDanger);
  }

  Future<void> _handleDeleteQuarantinedSingle(ThreatDetailItem t) async {
    final ok = await widget.threatManager.deleteQuarantinedByFilePath(t.filePath);
    if (ok) {
      setState(() => t.actionTaken = 'DIBERSIHKAN');
      if (widget.threatManager.logRepository != null) await widget.threatManager.logRepository!.updateLog(widget.log);
    }
    _showSnack(ok ? 'Berkas ${t.fileName} dimusnahkan permanen dari brankas.' : 'Gagal memusnahkan berkas.', bg: ok ? AppColors.statusSafeEmerald : AppColors.statusDanger);
  }

  Future<void> _handleRestoreQuarantinedSingle(ThreatDetailItem t) async {
    final ok = await widget.threatManager.restoreQuarantinedByFilePath(t.filePath);
    if (ok) {
      setState(() => t.actionTaken = 'AKTIF');
      if (widget.threatManager.logRepository != null) await widget.threatManager.logRepository!.updateLog(widget.log);
    }
    _showSnack(ok ? 'Berkas ${t.fileName} berhasil dipulihkan ke lokasi semula.' : 'Gagal memulihkan berkas.', bg: ok ? AppColors.statusSafeEmerald : AppColors.statusDanger);
  }

  @override
  Widget build(BuildContext context) {
    final activeCount = _threats.where((t) => t.actionTaken == 'AKTIF').length;
    final quarantinedCount = _threats.where((t) => t.actionTaken == 'DIKARANTINA').length;

    return Container(
      constraints: BoxConstraints(maxHeight: MediaQuery.of(context).size.height * 0.88),
      decoration: const BoxDecoration(color: AppColors.surfaceCard, borderRadius: BorderRadius.vertical(top: Radius.circular(28))),
      padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 16),
      child: SafeArea(
        top: false,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Center(child: Container(width: 44, height: 4, decoration: BoxDecoration(color: AppColors.slateBorder, borderRadius: BorderRadius.circular(2)))),
            const SizedBox(height: 16),
            _buildHeader(activeCount, quarantinedCount),
            const SizedBox(height: 14),
            const Divider(color: AppColors.slateBorder, height: 1),
            const SizedBox(height: 12),
            Expanded(
              child: _threats.isEmpty ? _buildEmptyState() : ListView.separated(
                physics: const BouncingScrollPhysics(),
                itemCount: _threats.length,
                separatorBuilder: (_, _) => const SizedBox(height: 12),
                itemBuilder: (_, idx) => _buildThreatCard(_threats[idx]),
              ),
            ),
            const SizedBox(height: 16),
            _buildActionButtons(activeCount, quarantinedCount),
          ],
        ),
      ),
    );
  }

  Widget _buildEmptyState() {
    return Center(
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Container(
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(color: AppColors.statusSafeEmerald.withValues(alpha: 0.12), shape: BoxShape.circle),
            child: const Icon(Icons.verified_user_rounded, color: AppColors.statusSafeEmerald, size: 40),
          ),
          const SizedBox(height: 12),
          Text('Tidak Ada Ancaman Aktif', style: AppTypography.headlineSm.copyWith(fontWeight: FontWeight.w700, color: AppColors.navyDeep)),
          const SizedBox(height: 4),
          Text('Semua berkas dan sistem telah diverifikasi aman.', style: AppTypography.bodySm.copyWith(color: AppColors.slateMuted), textAlign: TextAlign.center),
        ],
      ),
    );
  }

  Widget _buildHeader(int activeCount, int quarantinedCount) {
    final hasActive = activeCount > 0;
    return Row(
      children: [
        Container(
          width: 44, height: 44,
          decoration: BoxDecoration(
            color: hasActive ? AppColors.statusDanger.withValues(alpha: 0.12) : (quarantinedCount > 0 ? AppColors.statusWarning.withValues(alpha: 0.12) : AppColors.statusSafeEmerald.withValues(alpha: 0.12)),
            borderRadius: BorderRadius.circular(12),
          ),
          child: Icon(
            hasActive ? Icons.bug_report_rounded : (quarantinedCount > 0 ? Icons.lock_clock_rounded : Icons.verified_user_rounded),
            color: hasActive ? AppColors.statusDanger : (quarantinedCount > 0 ? AppColors.statusWarning : AppColors.statusSafeEmerald),
            size: 24,
          ),
        ),
        const SizedBox(width: 14),
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                hasActive
                    ? 'Pemindaian Selesai • ${_threats.length} Ancaman Ditemukan'
                    : (quarantinedCount > 0 ? '$quarantinedCount Berkas Dalam Karantina' : 'Pemindaian Selesai • Berkas Aman'),
                style: AppTypography.headlineSm.copyWith(fontSize: 15, fontWeight: FontWeight.w700, color: AppColors.navyDeep),
              ),
              const SizedBox(height: 2),
              Text('Audit Kaspersky Engine • ${widget.log.time}', style: AppTypography.bodySm.copyWith(color: AppColors.slateMuted, fontSize: 11)),
            ],
          ),
        ),
      ],
    );
  }

  Widget _buildThreatCard(ThreatDetailItem threat) {
    final isCleaned = threat.actionTaken == 'DIBERSIHKAN';
    final isQuar = threat.actionTaken == 'DIKARANTINA';
    final isAktif = threat.actionTaken == 'AKTIF';
    final statusColor = isCleaned ? AppColors.statusSafeEmerald : (isQuar ? AppColors.statusWarning : AppColors.statusDanger);

    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: AppColors.surface, borderRadius: BorderRadius.circular(14),
        border: Border.all(color: isAktif ? AppColors.statusDanger.withValues(alpha: 0.3) : AppColors.slateBorder),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Expanded(child: Text(threat.fileName, style: AppTypography.labelMd.copyWith(fontWeight: FontWeight.w700, color: AppColors.navyDeep), maxLines: 1, overflow: TextOverflow.ellipsis)),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                decoration: BoxDecoration(color: statusColor.withValues(alpha: 0.12), borderRadius: BorderRadius.circular(6)),
                child: Text(threat.actionTaken, style: TextStyle(color: statusColor, fontSize: 10, fontWeight: FontWeight.w800)),
              ),
            ],
          ),
          const SizedBox(height: 4),
          Text(threat.filePath, style: const TextStyle(fontSize: 10, fontFamily: 'monospace', color: AppColors.slateMuted), maxLines: 2, overflow: TextOverflow.ellipsis),
          const SizedBox(height: 8),
          Wrap(
            spacing: 6, runSpacing: 4,
            children: [
              _buildBadge(Icons.coronavirus_outlined, threat.virusName, AppColors.statusDanger),
              _buildBadge(Icons.category_outlined, threat.threatType, AppColors.navyDeep),
              _buildBadge(Icons.warning_amber_rounded, threat.severity, Colors.orange.shade800),
            ],
          ),
          const SizedBox(height: 8),
          const Divider(color: AppColors.slateBorder, height: 1),
          const SizedBox(height: 8),
          if (isAktif) Row(
            children: [
              Expanded(child: OutlinedButton(
                style: OutlinedButton.styleFrom(side: const BorderSide(color: AppColors.statusWarning), visualDensity: VisualDensity.compact),
                onPressed: _isProcessing ? null : () => _handleQuarantineSingle(threat),
                child: const Text('Karantina', style: TextStyle(color: AppColors.statusWarning, fontSize: 11, fontWeight: FontWeight.w700)),
              )),
              const SizedBox(width: 8),
              Expanded(child: ElevatedButton(
                style: ElevatedButton.styleFrom(backgroundColor: AppColors.statusDanger, visualDensity: VisualDensity.compact),
                onPressed: _isProcessing ? null : () => _handleClearSingle(threat),
                child: const Text('Hapus', style: TextStyle(color: Colors.white, fontSize: 11, fontWeight: FontWeight.w700)),
              )),
            ],
          ) else if (isQuar) Row(
            children: [
              Expanded(child: OutlinedButton(
                style: OutlinedButton.styleFrom(side: const BorderSide(color: AppColors.statusSafeEmerald), visualDensity: VisualDensity.compact),
                onPressed: _isProcessing ? null : () => _handleRestoreQuarantinedSingle(threat),
                child: const Text('Pulihkan', style: TextStyle(color: AppColors.statusSafeEmerald, fontSize: 11, fontWeight: FontWeight.w700)),
              )),
              const SizedBox(width: 8),
              Expanded(child: ElevatedButton(
                style: ElevatedButton.styleFrom(backgroundColor: AppColors.statusDanger, visualDensity: VisualDensity.compact),
                onPressed: _isProcessing ? null : () => _handleDeleteQuarantinedSingle(threat),
                child: const Text('Hapus Permanen', style: TextStyle(color: Colors.white, fontSize: 11, fontWeight: FontWeight.w700)),
              )),
            ],
          ) else Row(
            children: const [
              Icon(Icons.check_circle_outline, size: 14, color: AppColors.statusSafeEmerald),
              SizedBox(width: 6),
              Text('Berkas telah dimusnahkan secara permanen', style: TextStyle(color: AppColors.statusSafeEmerald, fontSize: 11, fontWeight: FontWeight.w600)),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildBadge(IconData icon, String text, Color color) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
      decoration: BoxDecoration(color: color.withValues(alpha: 0.08), borderRadius: BorderRadius.circular(4)),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(icon, size: 12, color: color),
          const SizedBox(width: 4),
          Text(text, style: TextStyle(color: color, fontSize: 10, fontWeight: FontWeight.w600)),
        ],
      ),
    );
  }

  Widget _buildActionButtons(int activeCount, int quarantinedCount) {
    if (activeCount > 0) {
      return Row(
        children: [
          Expanded(child: OutlinedButton.icon(
            style: OutlinedButton.styleFrom(padding: const EdgeInsets.symmetric(vertical: 12), side: const BorderSide(color: AppColors.statusWarning, width: 1.5), shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14))),
            icon: const Icon(Icons.lock_clock_rounded, size: 18, color: AppColors.statusWarning),
            label: const Text('Karantina Semua', style: TextStyle(color: AppColors.statusWarning, fontWeight: FontWeight.w700, fontSize: 12)),
            onPressed: _isProcessing ? null : _handleQuarantineAll,
          )),
          const SizedBox(width: 10),
          Expanded(child: ElevatedButton.icon(
            style: ElevatedButton.styleFrom(backgroundColor: AppColors.statusDanger, foregroundColor: Colors.white, elevation: 0, padding: const EdgeInsets.symmetric(vertical: 12), shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14))),
            icon: const Icon(Icons.delete_forever_rounded, size: 18),
            label: const Text('Hapus Semua', style: TextStyle(color: Colors.white, fontWeight: FontWeight.w700, fontSize: 12)),
            onPressed: _isProcessing ? null : _handleClearAll,
          )),
        ],
      );
    }

    if (quarantinedCount > 0) {
      return Row(
        children: [
          Expanded(child: OutlinedButton.icon(
            style: OutlinedButton.styleFrom(padding: const EdgeInsets.symmetric(vertical: 12), side: const BorderSide(color: AppColors.primary, width: 1.5), shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14))),
            icon: const Icon(Icons.archive_outlined, size: 18, color: AppColors.primary),
            label: const Text('Brankas Karantina', style: TextStyle(color: AppColors.primary, fontWeight: FontWeight.w700, fontSize: 12)),
            onPressed: () => QuarantineVaultSheet.show(context),
          )),
          const SizedBox(width: 10),
          Expanded(child: ElevatedButton.icon(
            style: ElevatedButton.styleFrom(backgroundColor: AppColors.statusDanger, foregroundColor: Colors.white, elevation: 0, padding: const EdgeInsets.symmetric(vertical: 12), shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14))),
            icon: const Icon(Icons.delete_forever_rounded, size: 18),
            label: const Text('Hapus Permanen', style: TextStyle(color: Colors.white, fontWeight: FontWeight.w700, fontSize: 12)),
            onPressed: _isProcessing ? null : _handleDeleteAllQuarantined,
          )),
        ],
      );
    }

    return AppButton(
      label: 'Tutup Laporan',
      icon: Icons.check_circle_outline_rounded,
      onPressed: () => Navigator.pop(context),
    );
  }
}
