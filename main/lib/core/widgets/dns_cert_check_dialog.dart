import 'package:flutter/material.dart';
import '../theme/app_colors.dart';
import '../theme/app_typography.dart';
import '../../data/services/wifi_security_service.dart';

/// Modal dialog for interactive SSL Certificate and DNS Resolution audits via Kaspersky SDK
class DnsCertCheckDialog extends StatefulWidget {
  final int initialTabIndex;

  const DnsCertCheckDialog({
    super.key,
    this.initialTabIndex = 0,
  });

  static void show(BuildContext context, {int initialTabIndex = 0}) {
    showDialog(
      context: context,
      barrierDismissible: true,
      builder: (_) => DnsCertCheckDialog(initialTabIndex: initialTabIndex),
    );
  }

  @override
  State<DnsCertCheckDialog> createState() => _DnsCertCheckDialogState();
}

class _DnsCertCheckDialogState extends State<DnsCertCheckDialog>
    with SingleTickerProviderStateMixin {
  late TabController _tabController;

  final TextEditingController _certUrlController =
      TextEditingController(text: 'https://telkomsel.com');
  bool _certLoading = false;
  Map<String, dynamic>? _certResult;

  final TextEditingController _dnsUrlController =
      TextEditingController(text: 'telkomsel.com');
  final TextEditingController _trustedIpController = TextEditingController();
  final List<String> _trustedIps = ['104.26.12.31', '172.67.74.152'];
  bool _dnsLoading = false;
  Map<String, dynamic>? _dnsResult;

  @override
  void initState() {
    super.initState();
    _tabController = TabController(
      length: 2,
      vsync: this,
      initialIndex: widget.initialTabIndex,
    );
  }

  @override
  void dispose() {
    _tabController.dispose();
    _certUrlController.dispose();
    _dnsUrlController.dispose();
    _trustedIpController.dispose();
    super.dispose();
  }

  Future<void> _runCertCheck() async {
    final text = _certUrlController.text.trim();
    if (text.isEmpty) return;

    setState(() {
      _certLoading = true;
      _certResult = null;
    });

    final res = await WifiSecurityService.checkCertificate(text);
    if (!mounted) return;
    setState(() {
      _certLoading = false;
      _certResult = res;
    });
  }

  Future<void> _runDnsCheck() async {
    final text = _dnsUrlController.text.trim();
    if (text.isEmpty) return;

    setState(() {
      _dnsLoading = true;
      _dnsResult = null;
    });

    final res = await WifiSecurityService.checkDns(
      text,
      trustedIps: _trustedIps,
    );
    if (!mounted) return;
    setState(() {
      _dnsLoading = false;
      _dnsResult = res;
    });
  }

  void _addTrustedIp() {
    final ip = _trustedIpController.text.trim();
    if (ip.isNotEmpty && !_trustedIps.contains(ip)) {
      setState(() {
        _trustedIps.add(ip);
        _trustedIpController.clear();
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Dialog(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
      backgroundColor: AppColors.surfaceCard,
      insetPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 24),
      child: ConstrainedBox(
        constraints: const BoxConstraints(maxWidth: 420, maxHeight: 620),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Padding(
              padding: const EdgeInsets.fromLTRB(20, 18, 12, 0),
              child: Row(
                children: [
                  Container(
                    width: 36,
                    height: 36,
                    decoration: BoxDecoration(
                      color: AppColors.primary.withValues(alpha: 0.1),
                      borderRadius: BorderRadius.circular(10),
                    ),
                    child: const Icon(Icons.verified_user_outlined,
                        color: AppColors.primary, size: 20),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text('Audit Keamanan Jaringan',
                            style: AppTypography.headlineSm
                                .copyWith(fontWeight: FontWeight.w700)),
                        Text('Kaspersky SSL & DNS Inspection Engine',
                            style: AppTypography.bodySm.copyWith(
                                color: AppColors.slateMuted, fontSize: 11)),
                      ],
                    ),
                  ),
                  IconButton(
                    icon: const Icon(Icons.close, size: 20),
                    onPressed: () => Navigator.pop(context),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 12),
            TabBar(
              controller: _tabController,
              labelColor: AppColors.primary,
              unselectedLabelColor: AppColors.slateMid,
              indicatorColor: AppColors.primary,
              indicatorWeight: 3,
              labelStyle:
                  AppTypography.labelMd.copyWith(fontWeight: FontWeight.w700),
              tabs: const [
                Tab(text: 'Sertifikat SSL'),
                Tab(text: 'Resolusi DNS'),
              ],
            ),
            Expanded(
              child: TabBarView(
                controller: _tabController,
                children: [
                  _buildCertificateTab(),
                  _buildDnsTab(),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildCertificateTab() {
    final isValid = _certResult?['isValid'] == true;
    final verdict = _certResult?['verdict'] as String? ?? '';
    final extVerdict = _certResult?['extendedVerdict'] as String? ?? '';

    return ListView(
      padding: const EdgeInsets.all(20),
      children: [
        Text(
          'Periksa integritas rantai sertifikat TLS/HTTPS dari pemalsuan man-in-the-middle atau kadaluarsa.',
          style: AppTypography.bodySm
              .copyWith(color: AppColors.slateMuted, fontSize: 12),
        ),
        const SizedBox(height: 14),
        TextField(
          controller: _certUrlController,
          decoration: InputDecoration(
            labelText: 'URL Target',
            hintText: 'https://domain.com',
            prefixIcon: const Icon(Icons.lock_outline, size: 20),
            border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)),
            contentPadding:
                const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
          ),
        ),
        const SizedBox(height: 12),
        ElevatedButton.icon(
          onPressed: _certLoading ? null : _runCertCheck,
          icon: _certLoading
              ? const SizedBox(
                  width: 16,
                  height: 16,
                  child: CircularProgressIndicator(
                      strokeWidth: 2, color: Colors.white))
              : const Icon(Icons.search, size: 18),
          label: Text(_certLoading ? 'Memeriksa...' : 'Validasi Sertifikat SSL'),
          style: ElevatedButton.styleFrom(
            backgroundColor: AppColors.primary,
            foregroundColor: Colors.white,
            padding: const EdgeInsets.symmetric(vertical: 12),
            shape:
                RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
          ),
        ),
        if (_certResult != null) ...[
          const SizedBox(height: 18),
          Container(
            padding: const EdgeInsets.all(14),
            decoration: BoxDecoration(
              color: isValid
                  ? AppColors.statusSafeEmerald.withValues(alpha: 0.08)
                  : AppColors.statusDanger.withValues(alpha: 0.08),
              borderRadius: BorderRadius.circular(14),
              border: Border.all(
                color: isValid
                    ? AppColors.statusSafeEmerald.withValues(alpha: 0.3)
                    : AppColors.statusDanger.withValues(alpha: 0.3),
              ),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Icon(
                      isValid
                          ? Icons.check_circle_rounded
                          : Icons.cancel_rounded,
                      color: isValid
                          ? AppColors.statusSafeEmerald
                          : AppColors.statusDanger,
                      size: 20,
                    ),
                    const SizedBox(width: 8),
                    Text(
                      isValid ? 'Sertifikat Valid & Aman' : 'Peringatan Sertifikat',
                      style: AppTypography.labelMd.copyWith(
                        fontWeight: FontWeight.w700,
                        color: isValid
                            ? AppColors.statusSafeEmerald
                            : AppColors.statusDanger,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 8),
                Text('Verdict: $verdict',
                    style: const TextStyle(
                        fontSize: 12, fontWeight: FontWeight.w600)),
                if (extVerdict.isNotEmpty)
                  Text('Status Rinci: $extVerdict',
                      style: const TextStyle(
                          fontSize: 11, color: AppColors.slateMuted)),
              ],
            ),
          ),
        ],
      ],
    );
  }

  Widget _buildDnsTab() {
    final isSafe = _dnsResult?['isSafe'] == true;
    final verdict = _dnsResult?['verdict'] as String? ?? '';
    final cloudIps =
        (_dnsResult?['cloudTrustedIps'] as List<dynamic>?)?.cast<String>() ?? [];

    return ListView(
      padding: const EdgeInsets.all(20),
      children: [
        Text(
          'Deteksi spoofing DNS resolver dan verifikasi kecocokan IP cloud terhadap basis reputasi Kaspersky.',
          style: AppTypography.bodySm
              .copyWith(color: AppColors.slateMuted, fontSize: 12),
        ),
        const SizedBox(height: 14),
        TextField(
          controller: _dnsUrlController,
          decoration: InputDecoration(
            labelText: 'Domain Target',
            hintText: 'telkomsel.com',
            prefixIcon: const Icon(Icons.dns_rounded, size: 20),
            border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)),
            contentPadding:
                const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
          ),
        ),
        const SizedBox(height: 10),
        Row(
          children: [
            Expanded(
              child: TextField(
                controller: _trustedIpController,
                decoration: InputDecoration(
                  labelText: 'Tambah IP Tepercaya',
                  hintText: 'e.g. 104.26.12.31',
                  border: OutlineInputBorder(
                      borderRadius: BorderRadius.circular(10)),
                  contentPadding:
                      const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
                ),
              ),
            ),
            const SizedBox(width: 8),
            IconButton.filled(
              icon: const Icon(Icons.add, size: 20),
              onPressed: _addTrustedIp,
              style: IconButton.styleFrom(backgroundColor: AppColors.slateBorder),
            ),
          ],
        ),
        if (_trustedIps.isNotEmpty) ...[
          const SizedBox(height: 8),
          Wrap(
            spacing: 6,
            runSpacing: 4,
            children: _trustedIps
                .map((ip) => Chip(
                      label: Text(ip, style: const TextStyle(fontSize: 10)),
                      onDeleted: () => setState(() => _trustedIps.remove(ip)),
                      padding: EdgeInsets.zero,
                      materialTapTargetSize: MaterialTapTargetSize.shrinkWrap,
                    ))
                .toList(),
          ),
        ],
        const SizedBox(height: 12),
        ElevatedButton.icon(
          onPressed: _dnsLoading ? null : _runDnsCheck,
          icon: _dnsLoading
              ? const SizedBox(
                  width: 16,
                  height: 16,
                  child: CircularProgressIndicator(
                      strokeWidth: 2, color: Colors.white))
              : const Icon(Icons.verified_outlined, size: 18),
          label: Text(_dnsLoading ? 'Memeriksa...' : 'Verifikasi Resolusi DNS'),
          style: ElevatedButton.styleFrom(
            backgroundColor: AppColors.primary,
            foregroundColor: Colors.white,
            padding: const EdgeInsets.symmetric(vertical: 12),
            shape:
                RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
          ),
        ),
        if (_dnsResult != null) ...[
          const SizedBox(height: 16),
          Container(
            padding: const EdgeInsets.all(14),
            decoration: BoxDecoration(
              color: isSafe
                  ? AppColors.statusSafeEmerald.withValues(alpha: 0.08)
                  : AppColors.statusDanger.withValues(alpha: 0.08),
              borderRadius: BorderRadius.circular(14),
              border: Border.all(
                color: isSafe
                    ? AppColors.statusSafeEmerald.withValues(alpha: 0.3)
                    : AppColors.statusDanger.withValues(alpha: 0.3),
              ),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Icon(
                      isSafe
                          ? Icons.check_circle_rounded
                          : Icons.warning_rounded,
                      color: isSafe
                          ? AppColors.statusSafeEmerald
                          : AppColors.statusDanger,
                      size: 20,
                    ),
                    const SizedBox(width: 8),
                    Text(
                      isSafe ? 'Resolusi DNS Aman' : 'Potensi DNS Spoofing',
                      style: AppTypography.labelMd.copyWith(
                        fontWeight: FontWeight.w700,
                        color: isSafe
                            ? AppColors.statusSafeEmerald
                            : AppColors.statusDanger,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 6),
                Text('Status KSN: $verdict',
                    style: const TextStyle(
                        fontSize: 12, fontWeight: FontWeight.w600)),
                if (cloudIps.isNotEmpty) ...[
                  const SizedBox(height: 4),
                  Text('Cloud IPs: ${cloudIps.join(', ')}',
                      style: const TextStyle(
                          fontSize: 11, color: AppColors.slateMid)),
                ],
              ],
            ),
          ),
        ],
      ],
    );
  }
}
