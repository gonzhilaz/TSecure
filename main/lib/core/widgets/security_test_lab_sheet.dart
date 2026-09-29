import 'package:flutter/material.dart';
import '../theme/app_colors.dart';
import '../theme/app_typography.dart';
import '../../data/services/kaspersky_sdk_bridge.dart';

class SecurityTestLabSheet extends StatefulWidget {
  final KasperskySdkBridge sdk;
  final int initialTab;

  const SecurityTestLabSheet({
    super.key,
    required this.sdk,
    this.initialTab = 0,
  });

  static void show(
    BuildContext context, {
    required KasperskySdkBridge sdk,
    int initialTab = 0,
  }) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (_) => SecurityTestLabSheet(
        sdk: sdk,
        initialTab: initialTab,
      ),
    );
  }

  @override
  State<SecurityTestLabSheet> createState() => _SecurityTestLabSheetState();
}

class _SecurityTestLabSheetState extends State<SecurityTestLabSheet>
    with SingleTickerProviderStateMixin {
  late TabController _tabController;
  final TextEditingController _urlController = TextEditingController();

  bool _isCheckingUrl = false;
  Map<String, dynamic>? _urlResult;

  bool _isScanningEicar = false;
  Map<String, dynamic>? _eicarResult;

  final List<Map<String, String>> _testUrls = [
    {
      'title': 'Kaspersky Phishing Test',
      'url': 'http://www.kaspersky.com/antiphishing_test',
      'type': 'PHISHING',
      'org': 'Kaspersky Lab Official',
    },
    {
      'title': 'AMTSO Phishing Standard',
      'url': 'https://www.amtso.org/check-desktop-phishing-page/',
      'type': 'PHISHING',
      'org': 'AMTSO Global Standard',
    },
    {
      'title': 'Kaspersky Malicious Web (WMUF)',
      'url': 'http://www.kaspersky.com/test/wmuf',
      'type': 'MALWARE',
      'org': 'Kaspersky Lab Malware Test',
    },
  ];

  @override
  void initState() {
    super.initState();
    _tabController = TabController(
      length: 2,
      vsync: this,
      initialIndex: widget.initialTab,
    );
    widget.sdk.requestNotificationPermission();
  }

  @override
  void dispose() {
    _tabController.dispose();
    _urlController.dispose();
    super.dispose();
  }

  Future<void> _checkTargetUrl(String url) async {
    if (url.trim().isEmpty) return;
    setState(() {
      _isCheckingUrl = true;
      _urlResult = null;
    });

    final res = await widget.sdk.checkUrl(url.trim());

    if (mounted) {
      setState(() {
        _isCheckingUrl = false;
        _urlResult = res;
      });
    }
  }

  Future<void> _runEicarScan() async {
    setState(() {
      _isScanningEicar = true;
      _eicarResult = null;
    });

    await Future.delayed(const Duration(milliseconds: 500));
    final res = await widget.sdk.testScanEicar();

    if (mounted) {
      setState(() {
        _isScanningEicar = false;
        _eicarResult = res;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      height: MediaQuery.of(context).size.height * 0.88,
      decoration: const BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      child: Column(
        children: [
          _buildDragHandle(),
          _buildHeader(),
          _buildTabBar(),
          Expanded(
            child: TabBarView(
              controller: _tabController,
              children: [
                _buildWebFilterTab(),
                _buildEicarTab(),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildDragHandle() {
    return Center(
      child: Container(
        margin: const EdgeInsets.only(top: 12, bottom: 8),
        width: 40,
        height: 4,
        decoration: BoxDecoration(
          color: AppColors.slateBorder,
          borderRadius: BorderRadius.circular(2),
        ),
      ),
    );
  }

  Widget _buildHeader() {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 8),
      child: Row(
        children: [
          Container(
            padding: const EdgeInsets.all(10),
            decoration: BoxDecoration(
              color: AppColors.primary.withValues(alpha: 0.1),
              borderRadius: BorderRadius.circular(12),
            ),
            child: const Icon(Icons.science_outlined, color: AppColors.primary, size: 24),
          ),
          const SizedBox(width: 14),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text('Laboratorium Uji Keamanan', style: AppTypography.headlineMd),
                const SizedBox(height: 2),
                Text(
                  'Validasi Deteksi Kaspersky KSN & AMTSO',
                  style: AppTypography.bodySm.copyWith(color: AppColors.slateMuted),
                ),
              ],
            ),
          ),
          IconButton(
            icon: const Icon(Icons.close, color: AppColors.slateMuted),
            onPressed: () => Navigator.pop(context),
          ),
        ],
      ),
    );
  }

  Widget _buildTabBar() {
    return Container(
      margin: const EdgeInsets.symmetric(horizontal: 20, vertical: 8),
      decoration: BoxDecoration(
        color: AppColors.slateSurface,
        borderRadius: BorderRadius.circular(12),
      ),
      child: TabBar(
        controller: _tabController,
        indicator: BoxDecoration(
          color: AppColors.navyDeep,
          borderRadius: BorderRadius.circular(12),
        ),
        labelColor: Colors.white,
        unselectedLabelColor: AppColors.slateMuted,
        labelStyle: AppTypography.bodySm.copyWith(fontWeight: FontWeight.w600),
        indicatorSize: TabBarIndicatorSize.tab,
        tabs: const [
          Tab(text: 'Uji Web Filter'),
          Tab(text: 'Uji Antivirus EICAR'),
        ],
      ),
    );
  }

  Widget _buildWebFilterTab() {
    return ListView(
      padding: const EdgeInsets.all(20),
      children: [
        Text(
          'Pilih URL Uji Standar AMTSO & Kaspersky:',
          style: AppTypography.labelLg.copyWith(color: AppColors.navyDeep),
        ),
        const SizedBox(height: 10),
        ..._testUrls.map((item) => _buildTestUrlTile(item)),
        const SizedBox(height: 16),
        Text('Atau Masukkan URL Kustom:', style: AppTypography.labelLg),
        const SizedBox(height: 8),
        Row(
          children: [
            Expanded(
              child: TextField(
                controller: _urlController,
                style: AppTypography.bodySm,
                decoration: InputDecoration(
                  hintText: 'https://contoh-domain.com',
                  hintStyle: AppTypography.bodySm.copyWith(color: AppColors.slateMuted),
                  contentPadding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
                  filled: true,
                  fillColor: AppColors.slateSurface,
                  border: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(12),
                    borderSide: const BorderSide(color: AppColors.slateBorder),
                  ),
                ),
              ),
            ),
            const SizedBox(width: 10),
            ElevatedButton(
              style: ElevatedButton.styleFrom(
                backgroundColor: AppColors.navyDeep,
                foregroundColor: Colors.white,
                padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
              ),
              onPressed: _isCheckingUrl ? null : () => _checkTargetUrl(_urlController.text),
              child: _isCheckingUrl
                  ? const SizedBox(width: 18, height: 18, child: CircularProgressIndicator(color: Colors.white, strokeWidth: 2))
                  : const Text('Cek'),
            ),
          ],
        ),
        const SizedBox(height: 20),
        if (_isCheckingUrl) _buildLoadingCard('Memeriksa reputasi URL pada Kaspersky KSN...')
        else if (_urlResult != null) _buildUrlResultCard(_urlResult!),
      ],
    );
  }

  Widget _buildTestUrlTile(Map<String, String> item) {
    final isPhishing = item['type'] == 'PHISHING';
    return Container(
      margin: const EdgeInsets.only(bottom: 10),
      decoration: BoxDecoration(
        color: AppColors.slateSurface,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: AppColors.slateBorder),
      ),
      child: ListTile(
        contentPadding: const EdgeInsets.symmetric(horizontal: 14, vertical: 4),
        leading: Container(
          padding: const EdgeInsets.all(8),
          decoration: BoxDecoration(
            color: (isPhishing ? Colors.amber : Colors.red).withValues(alpha: 0.12),
            shape: BoxShape.circle,
          ),
          child: Icon(
            isPhishing ? Icons.phishing_outlined : Icons.pest_control_outlined,
            color: isPhishing ? Colors.amber[800] : Colors.red[700],
            size: 20,
          ),
        ),
        title: Text(item['title']!, style: AppTypography.bodySm.copyWith(fontWeight: FontWeight.w700)),
        subtitle: Text(
          '${item['org']} • ${item['url']}',
          maxLines: 1,
          overflow: TextOverflow.ellipsis,
          style: AppTypography.bodySm.copyWith(color: AppColors.slateMuted, fontSize: 10),
        ),
        trailing: ElevatedButton(
          style: ElevatedButton.styleFrom(
            backgroundColor: AppColors.primary,
            foregroundColor: Colors.white,
            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
            minimumSize: const Size(60, 32),
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
          ),
          onPressed: _isCheckingUrl ? null : () => _checkTargetUrl(item['url']!),
          child: const Text('Uji', style: TextStyle(fontSize: 12, fontWeight: FontWeight.bold)),
        ),
      ),
    );
  }

  Widget _buildUrlResultCard(Map<String, dynamic> res) {
    final isSafe = res['isSafe'] == true;
    final verdict = res['verdict'] ?? (isSafe ? 'AMAN' : 'BERBAHAYA');
    final score = res['score'] ?? (isSafe ? 100 : 15);
    final desc = res['description'] ?? '';

    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: isSafe ? Colors.green.withValues(alpha: 0.06) : Colors.red.withValues(alpha: 0.06),
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: isSafe ? Colors.green.withValues(alpha: 0.3) : Colors.red.withValues(alpha: 0.3)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(isSafe ? Icons.check_circle : Icons.warning_amber_rounded, color: isSafe ? Colors.green : Colors.red, size: 28),
              const SizedBox(width: 10),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text('Hasil Inspeksi: $verdict', style: AppTypography.labelLg.copyWith(color: isSafe ? Colors.green[800] : Colors.red[800], fontWeight: FontWeight.w800)),
                    Text('Skor Keamanan: $score/100', style: AppTypography.bodySm.copyWith(color: AppColors.slateMuted)),
                  ],
                ),
              ),
            ],
          ),
          const SizedBox(height: 10),
          Text(desc, style: AppTypography.bodySm.copyWith(color: AppColors.navyDeep)),
          const SizedBox(height: 10),
          Container(
            padding: const EdgeInsets.all(10),
            decoration: BoxDecoration(color: Colors.white, borderRadius: BorderRadius.circular(10)),
            child: Row(
              children: [
                const Icon(Icons.notifications_active, color: AppColors.primary, size: 16),
                const SizedBox(width: 8),
                Expanded(
                  child: Text(
                    isSafe ? 'Riwayat dicatat di Aktivitas Terakhir.' : 'Notifikasi Heads-Up terkirim & tercatat di Log Ancaman.',
                    style: AppTypography.bodySm.copyWith(fontWeight: FontWeight.w600, fontSize: 10),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildEicarTab() {
    return ListView(
      padding: const EdgeInsets.all(20),
      children: [
        Container(
          padding: const EdgeInsets.all(16),
          decoration: BoxDecoration(
            color: AppColors.slateSurface,
            borderRadius: BorderRadius.circular(16),
            border: Border.all(color: AppColors.slateBorder),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  const Icon(Icons.security, color: AppColors.primary),
                  const SizedBox(width: 8),
                  Text('Standar Industri EICAR', style: AppTypography.labelLg),
                ],
              ),
              const SizedBox(height: 8),
              Text(
                'EICAR (European Institute for Computer Antivirus Research) adalah berkas uji standar non-destruktif untuk memvalidasi efektivitas mesin antivirus tanpa merusak perangkat.',
                style: AppTypography.bodySm.copyWith(color: AppColors.slateMuted),
              ),
            ],
          ),
        ),
        const SizedBox(height: 20),
        SizedBox(
          width: double.infinity,
          height: 48,
          child: ElevatedButton.icon(
            style: ElevatedButton.styleFrom(
              backgroundColor: AppColors.primary,
              foregroundColor: Colors.white,
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
            ),
            icon: _isScanningEicar
                ? const SizedBox(width: 18, height: 18, child: CircularProgressIndicator(color: Colors.white, strokeWidth: 2))
                : const Icon(Icons.bug_report_outlined),
            label: Text(
              _isScanningEicar ? 'Memindai Berkas EICAR...' : 'Jalankan Uji Deteksi EICAR',
              style: const TextStyle(fontWeight: FontWeight.w700),
            ),
            onPressed: _isScanningEicar ? null : _runEicarScan,
          ),
        ),
        const SizedBox(height: 20),
        if (_isScanningEicar) _buildLoadingCard('Mesin Kaspersky sedang memindai file uji EICAR...')
        else if (_eicarResult != null) _buildEicarResultCard(_eicarResult!),
      ],
    );
  }

  Widget _buildEicarResultCard(Map<String, dynamic> res) {
    final threatName = res['threatName'] ?? 'EICAR-Test-File';
    final threatType = res['threatType'] ?? 'Virus';
    final severity = res['severity'] ?? 'HIGH';
    final desc = res['description'] ?? '';

    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.red.withValues(alpha: 0.06),
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: Colors.red.withValues(alpha: 0.3)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              const Icon(Icons.pest_control_outlined, color: Colors.red, size: 28),
              const SizedBox(width: 10),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text('Ancaman Terdeteksi: $threatName', style: AppTypography.labelLg.copyWith(color: Colors.red[800], fontWeight: FontWeight.w800)),
                    Text('Tipe: $threatType • Bahaya: $severity', style: AppTypography.bodySm.copyWith(color: AppColors.slateMuted)),
                  ],
                ),
              ),
            ],
          ),
          const SizedBox(height: 10),
          Text(desc, style: AppTypography.bodySm.copyWith(color: AppColors.navyDeep)),
          const SizedBox(height: 12),
          Container(
            padding: const EdgeInsets.all(10),
            decoration: BoxDecoration(color: Colors.white, borderRadius: BorderRadius.circular(10)),
            child: Row(
              children: [
                const Icon(Icons.check_circle_outline, color: Colors.green, size: 16),
                const SizedBox(width: 8),
                Expanded(
                  child: Text(
                    'Berkas uji berhasil diisolasi & dicatat ke Aktivitas Terakhir.',
                    style: AppTypography.bodySm.copyWith(fontWeight: FontWeight.w600, fontSize: 10),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildLoadingCard(String message) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          children: [
            const CircularProgressIndicator(color: AppColors.primary),
            const SizedBox(height: 12),
            Text(message, style: AppTypography.bodySm.copyWith(color: AppColors.slateMuted)),
          ],
        ),
      ),
    );
  }
}
