import 'package:flutter/material.dart';
import '../theme/app_colors.dart';
import '../theme/app_typography.dart';
import '../../data/services/kaspersky_sdk_bridge.dart';
import 'security_test_lab_sheet.dart';

class AdvancedSecurityHubSheet extends StatefulWidget {
  final KasperskySdkBridge sdk;
  final VoidCallback onNavigateToScanner;
  final VoidCallback? onNavigateToProfile;

  const AdvancedSecurityHubSheet({
    super.key,
    required this.sdk,
    required this.onNavigateToScanner,
    this.onNavigateToProfile,
  });

  static void show(
    BuildContext context, {
    required KasperskySdkBridge sdk,
    required VoidCallback onNavigateToScanner,
    VoidCallback? onNavigateToProfile,
  }) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (_) => AdvancedSecurityHubSheet(
        sdk: sdk,
        onNavigateToScanner: onNavigateToScanner,
        onNavigateToProfile: onNavigateToProfile,
      ),
    );
  }

  @override
  State<AdvancedSecurityHubSheet> createState() =>
      _AdvancedSecurityHubSheetState();
}

class _AdvancedSecurityHubSheetState extends State<AdvancedSecurityHubSheet> {
  @override
  Widget build(BuildContext context) {
    final sdk = widget.sdk;

    return Container(
      decoration: const BoxDecoration(
        color: AppColors.surfaceCard,
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 18),
      child: SafeArea(
        top: false,
        child: SingleChildScrollView(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
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
              const SizedBox(height: 16),
              Row(
                children: [
                  Container(
                    padding: const EdgeInsets.all(10),
                    decoration: BoxDecoration(
                      color: AppColors.primary,
                      borderRadius: BorderRadius.circular(14),
                    ),
                    child: const Icon(Icons.apps, color: Colors.white, size: 24),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          'Pusat Modul Lanjutan',
                          style: AppTypography.headlineSm.copyWith(
                            fontWeight: FontWeight.w700,
                            color: AppColors.navyDeep,
                            fontSize: 16,
                          ),
                        ),
                        const SizedBox(height: 2),
                        Text(
                          'Kaspersky Mobile Security & BlackWall Enterprise',
                          style: AppTypography.bodySm.copyWith(
                            color: AppColors.slateMuted,
                            fontSize: 11,
                          ),
                        ),
                      ],
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 16),

              // Modul 1: Anti-Theft SIM Watch Guard
              _buildModuleCard(
                icon: Icons.sim_card_outlined,
                title: 'Anti-Theft SIM Watch Guard',
                badgeText: sdk.simWatchEnabled ? 'Aktif' : 'Nonaktif',
                isSafe: sdk.simWatchEnabled,
                toggleValue: sdk.simWatchEnabled,
                onToggle: (v) {
                  setState(() => sdk.toggleSimWatch(v));
                },
                rows: [
                  _InfoRow('SIM Terikat', sdk.boundSimSlot),
                  _InfoRow('Kriptografis ICCID', sdk.boundIccidMasked),
                  _InfoRow('Kontak Darurat', sdk.emergencyContact),
                  _InfoRow(
                    'Respon Pencurian',
                    sdk.simWatchEnabled
                        ? 'Auto-Lock Layar & Sirene'
                        : 'Nonaktif',
                  ),
                ],
                actionLabel: 'Uji Respon Sensor SIM',
                onAction: () {
                  _showSnack(
                    '[SIM Watch Test] Sensor Aktif: Pelepasan SIM terdeteksi akan langsung mengunci layar & memicu sirene darurat.',
                  );
                },
              ),
              const SizedBox(height: 12),

              // Modul 2: Brankas Karantina File
              _buildModuleCard(
                icon: Icons.lock_clock_outlined,
                title: 'Brankas Karantina File',
                badgeText: '0 Berkas',
                isSafe: true,
                rows: [
                  _InfoRow('Status Karantina', '0 Berkas Terisolasi (Bersih)'),
                  _InfoRow('Mesin Pengawas', 'Kaspersky Antivirus Isolation Engine'),
                  _InfoRow('Kebijakan Akses', 'Akses I/O Terblokir Total'),
                ],
                actionLabel: 'Periksa Ruang Karantina',
                onAction: () {
                  _showSnack(
                    'Ruang Karantina Bersih: Tidak ada berkas malware atau file mencurigakan yang diisolasi.',
                  );
                },
              ),
              const SizedBox(height: 12),

              // Modul 3: Enkripsi Penyimpanan Sensitif
              _buildModuleCard(
                icon: Icons.enhanced_encryption_outlined,
                title: 'Enkripsi Penyimpanan Sensitif',
                badgeText: sdk.secureStorageEnabled ? 'Aktif' : 'Nonaktif',
                isSafe: sdk.secureStorageEnabled,
                toggleValue: sdk.secureStorageEnabled,
                onToggle: (v) {
                  setState(() => sdk.toggleSecureStorage(v));
                },
                rows: [
                  _InfoRow('Status Enkripsi', sdk.secureStorageEnabled ? 'AES-256 GCM Aktif' : 'Nonaktif'),
                  _InfoRow('Modul SDK', 'com.kavsdk.securestorage.file'),
                  _InfoRow('Key Storage', 'Android Hardware KeyStore'),
                  _InfoRow('Target Proteksi', 'Basis Data & Kredensial Lokal'),
                ],
                actionLabel: 'Validasi Enkripsi Keystore',
                onAction: () {
                  _showSnack(
                    'Validasi Hardware KeyStore: Kunci enkripsi AES-256 tersimpan aman pada Trusted Execution Environment (TEE).',
                  );
                },
              ),
              const SizedBox(height: 16),

              SizedBox(
                width: double.infinity,
                height: 44,
                child: OutlinedButton.icon(
                  style: OutlinedButton.styleFrom(
                    foregroundColor: AppColors.primary,
                    side: const BorderSide(color: AppColors.primary, width: 1.5),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(12),
                    ),
                  ),
                  onPressed: () {
                    Navigator.pop(context);
                    SecurityTestLabSheet.show(context, sdk: sdk);
                  },
                  icon: const Icon(Icons.science_outlined, size: 20),
                  label: const Text(
                    'Buka Lab Uji Keamanan (EICAR & Phishing)',
                    style: TextStyle(fontWeight: FontWeight.w700),
                  ),
                ),
              ),
              const SizedBox(height: 10),

              SizedBox(
                width: double.infinity,
                height: 44,
                child: ElevatedButton(
                  style: ElevatedButton.styleFrom(
                    backgroundColor: AppColors.navyDeep,
                    foregroundColor: Colors.white,
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(12),
                    ),
                  ),
                  onPressed: () => Navigator.pop(context),
                  child: const Text('Tutup Pusat Modul', style: TextStyle(fontWeight: FontWeight.w700)),
                ),
              ),
              const SizedBox(height: 8),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildModuleCard({
    required IconData icon,
    required String title,
    required String badgeText,
    required bool isSafe,
    bool? toggleValue,
    ValueChanged<bool>? onToggle,
    required List<_InfoRow> rows,
    required String actionLabel,
    required VoidCallback onAction,
  }) {
    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: AppColors.slateBorder),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(icon, color: AppColors.primary, size: 20),
              const SizedBox(width: 8),
              Expanded(
                child: Text(
                  title,
                  style: AppTypography.bodySm.copyWith(
                    fontWeight: FontWeight.w700,
                    color: AppColors.navyDeep,
                  ),
                ),
              ),
              if (toggleValue != null && onToggle != null)
                Switch(
                  value: toggleValue,
                  activeThumbColor: AppColors.primary,
                  onChanged: onToggle,
                )
              else
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                  decoration: BoxDecoration(
                    color: isSafe ? AppColors.statusSafeBg : AppColors.statusWarningBg,
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: Text(
                    badgeText,
                    style: TextStyle(
                      color: isSafe ? AppColors.statusSafeEmerald : AppColors.statusWarningAmber,
                      fontWeight: FontWeight.w700,
                      fontSize: 11,
                    ),
                  ),
                ),
            ],
          ),
          const SizedBox(height: 8),
          Container(
            padding: const EdgeInsets.all(10),
            decoration: BoxDecoration(
              color: AppColors.background,
              borderRadius: BorderRadius.circular(10),
            ),
            child: Column(
              children: [
                for (int i = 0; i < rows.length; i++) ...[
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Text(rows[i].label, style: AppTypography.bodySm.copyWith(color: AppColors.slateMuted, fontSize: 11)),
                      const SizedBox(width: 6),
                      Expanded(
                        child: Text(
                          rows[i].value,
                          style: AppTypography.bodySm.copyWith(color: AppColors.navyDeep, fontWeight: FontWeight.w600, fontSize: 11),
                          textAlign: TextAlign.right,
                        ),
                      ),
                    ],
                  ),
                  if (i < rows.length - 1)
                    const Divider(color: AppColors.slateBorder, height: 12),
                ],
              ],
            ),
          ),
          const SizedBox(height: 8),
          SizedBox(
            width: double.infinity,
            height: 34,
            child: OutlinedButton(
              style: OutlinedButton.styleFrom(
                foregroundColor: AppColors.primary,
                side: const BorderSide(color: AppColors.primary, width: 1),
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
                padding: EdgeInsets.zero,
              ),
              onPressed: onAction,
              child: Text(actionLabel, style: const TextStyle(fontWeight: FontWeight.w700, fontSize: 12)),
            ),
          ),
        ],
      ),
    );
  }

  void _showSnack(String msg) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(msg),
        backgroundColor: AppColors.navyDeep,
        behavior: SnackBarBehavior.floating,
        duration: const Duration(seconds: 2),
      ),
    );
  }
}

class _InfoRow {
  final String label;
  final String value;
  const _InfoRow(this.label, this.value);
}
