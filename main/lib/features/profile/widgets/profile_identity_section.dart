import 'package:flutter/material.dart';
import '../../../core/theme/stitch_icons.dart';
import '../../../core/widgets/settings_sheet.dart';
import '../../../data/models/user_session.dart';

/// User identity card section displaying avatar with online indicator,
/// formatted phone number, subscription tier, and quick squircle action buttons.
class ProfileIdentitySection extends StatelessWidget {
  final UserSession? session;
  final void Function(String message)? onActionNotice;

  const ProfileIdentitySection({
    super.key,
    this.session,
    this.onActionNotice,
  });

  String _formatMsisdn(String? raw) {
    if (raw == null || raw.isEmpty) return '-';
    if (raw.contains('-')) return raw;
    if (raw.startsWith('+62') && raw.length >= 12) {
      final prefix = raw.substring(0, 3);
      final mid = raw.substring(3, raw.length - 7);
      final rest = raw.substring(raw.length - 7);
      return '$prefix $mid-$rest';
    }
    return raw;
  }

  String _resolveDisplayName(UserSession? session) {
    final rawName = session?.name.trim();
    if (rawName != null && rawName.isNotEmpty) {
      return rawName;
    }
    // Jika belum ada nama, gunakan User_<uniqueID>
    final mobileId = session?.mobileId.replaceAll('-', '').trim() ?? '';
    if (mobileId.isNotEmpty) {
      final suffix = mobileId.length >= 4 ? mobileId.substring(mobileId.length - 4).toUpperCase() : mobileId;
      return 'User_$suffix';
    }
    final cleanPhone = session?.msisdn.replaceAll(RegExp(r'\D'), '') ?? '';
    if (cleanPhone.length >= 4) {
      return 'User_${cleanPhone.substring(cleanPhone.length - 4)}';
    }
    return 'User_${(session?.hashCode ?? 1001).abs() % 10000}';
  }

  String _getInitials(String? name) {
    if (name == null || name.trim().isEmpty) return 'U';
    final parts = name.trim().split(RegExp(r'[\s_]+'));
    if (parts.length >= 2) {
      final first = parts[0].isNotEmpty ? parts[0][0] : '';
      final second = parts[1].isNotEmpty ? parts[1][0] : '';
      return '$first$second'.toUpperCase();
    }
    return name.substring(0, name.length >= 2 ? 2 : 1).toUpperCase();
  }

  @override
  Widget build(BuildContext context) {
    final displayName = _resolveDisplayName(session);
    final formattedPhone = _formatMsisdn(session?.msisdn);
    final tier = (session?.tier != null && session!.tier.isNotEmpty)
        ? session!.tier
        : 'Pelanggan Telkomsel';
    final initials = _getInitials(displayName);

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        // Profile Avatar with Online Dot (Left aligned matching Stitch)
        Stack(
          clipBehavior: Clip.none,
          children: [
            Container(
              width: 80,
              height: 80,
              decoration: BoxDecoration(
                color: const Color(0xFFED0226),
                shape: BoxShape.circle,
                border: Border.all(color: Colors.white, width: 4),
                boxShadow: [
                  BoxShadow(
                    color: const Color(0xFF0B132B).withValues(alpha: 0.12),
                    blurRadius: 16,
                    offset: const Offset(0, 4),
                  ),
                ],
              ),
              child: Center(
                child: Text(
                  initials,
                  style: const TextStyle(
                    color: Colors.white,
                    fontSize: 22,
                    fontWeight: FontWeight.w700,
                    letterSpacing: -0.5,
                  ),
                ),
              ),
            ),
            Positioned(
              bottom: 2,
              right: 2,
              child: Container(
                width: 16,
                height: 16,
                decoration: BoxDecoration(
                  color: const Color(0xFF10B981),
                  shape: BoxShape.circle,
                  border: Border.all(color: Colors.white, width: 2.5),
                ),
              ),
            ),
          ],
        ),
        const SizedBox(height: 12),

        // User Name (Left aligned)
        Text(
          displayName,
          textAlign: TextAlign.left,
          style: const TextStyle(
            fontSize: 20,
            fontWeight: FontWeight.w700,
            color: Color(0xFF0B132B),
            letterSpacing: -0.3,
          ),
        ),
        const SizedBox(height: 4),

        // Subtitle (Phone + Telkomsel Halo Diamond, Left aligned)
        RichText(
          textAlign: TextAlign.left,
          text: TextSpan(
            style: const TextStyle(
              fontSize: 12,
              fontWeight: FontWeight.w500,
              color: Color(0xFF778CA2),
            ),
            children: [
              TextSpan(text: '$formattedPhone • '),
              TextSpan(
                text: tier,
                style: const TextStyle(
                  color: Color(0xFFED0226),
                  fontWeight: FontWeight.w600,
                ),
              ),
            ],
          ),
        ),
        const SizedBox(height: 14),

        // 4 Action Buttons (Left aligned matching Stitch)
        Row(
          mainAxisAlignment: MainAxisAlignment.start,
          children: [
            _buildSquircleButton(
              svgIcon: StitchIcons.actionMessage,
              tooltip: 'Kirim Pesan',
              onTap: () => _showVeronikaDialog(context),
            ),
            const SizedBox(width: 10),
            _buildSquircleButton(
              svgIcon: StitchIcons.actionEdit,
              tooltip: 'Edit Profil',
              onTap: () => _showEditProfileDialog(context),
            ),
            const SizedBox(width: 10),
            _buildSquircleButton(
              svgIcon: StitchIcons.actionShare,
              tooltip: 'Bagikan',
              onTap: () => _showShareDialog(context, displayName, formattedPhone, tier),
            ),
            const SizedBox(width: 10),
            _buildSquircleButton(
              svgIcon: StitchIcons.actionMore,
              tooltip: 'Pilihan Lainnya',
              onTap: () => SettingsSheet.show(context),
            ),
          ],
        ),
      ],
    );
  }

  void _showVeronikaDialog(BuildContext context) {
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        backgroundColor: Colors.white,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
        title: const Row(
          children: [
            Icon(Icons.support_agent_rounded, color: Color(0xFFED0226), size: 24),
            SizedBox(width: 10),
            Text(
              'Veronika AI Assistant',
              style: TextStyle(fontSize: 16, fontWeight: FontWeight.w700, color: Color(0xFF0B132B)),
            ),
          ],
        ),
        content: const Text(
          'Layanan Asisten Virtual Veronika Telkomsel siap membantu seputar paket Telkomsel Secure, verifikasi lisensi Kaspersky, dan keamanan digital.',
          style: TextStyle(fontSize: 13, color: Color(0xFF64748B)),
        ),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('Tutup')),
          ElevatedButton(
            style: ElevatedButton.styleFrom(
              backgroundColor: const Color(0xFFED0226),
              foregroundColor: Colors.white,
            ),
            onPressed: () {
              Navigator.pop(ctx);
              onActionNotice?.call('Menghubungkan ke layanan Veronika Telkomsel...');
            },
            child: const Text('Mulai Chat'),
          ),
        ],
      ),
    );
  }

  void _showEditProfileDialog(BuildContext context) {
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        backgroundColor: Colors.white,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
        title: const Row(
          children: [
            Icon(Icons.badge_outlined, color: Color(0xFFED0226), size: 22),
            SizedBox(width: 10),
            Text(
              'Sinkronisasi Profil',
              style: TextStyle(fontSize: 16, fontWeight: FontWeight.w700, color: Color(0xFF0B132B)),
            ),
          ],
        ),
        content: const Text(
          'Data identitas nama, tier Telkomsel Halo, dan lokasi terhubung secara terenkripsi dengan profil MyTelkomsel ID Anda.',
          style: TextStyle(fontSize: 13, color: Color(0xFF64748B)),
        ),
        actions: [
          ElevatedButton(
            style: ElevatedButton.styleFrom(
              backgroundColor: const Color(0xFF0B132B),
              foregroundColor: Colors.white,
            ),
            onPressed: () => Navigator.pop(ctx),
            child: const Text('Mengerti'),
          ),
        ],
      ),
    );
  }

  void _showShareDialog(BuildContext context, String name, String phone, String tier) {
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        backgroundColor: Colors.white,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
        title: const Row(
          children: [
            Icon(Icons.verified_rounded, color: Color(0xFF10B981), size: 24),
            SizedBox(width: 10),
            Text(
              'Sertifikat Keamanan',
              style: TextStyle(fontSize: 16, fontWeight: FontWeight.w700, color: Color(0xFF0B132B)),
            ),
          ],
        ),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              'Status Perangkat: Terlindungi Penuh\nMesin Antivirus: Kaspersky Mobile Security SDK v5.21\nProteksi RASP: Aktif 24/7',
              style: TextStyle(fontSize: 13, color: Color(0xFF334155), height: 1.5),
            ),
            const SizedBox(height: 12),
            Container(
              padding: const EdgeInsets.all(10),
              decoration: BoxDecoration(
                color: const Color(0xFFF8FAFC),
                borderRadius: BorderRadius.circular(8),
                border: Border.all(color: const Color(0xFFE2E8F0)),
              ),
              child: Text(
                '$name • $phone\n$tier',
                style: const TextStyle(fontSize: 11, color: Color(0xFF64748B)),
              ),
            ),
          ],
        ),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('Tutup')),
          ElevatedButton.icon(
            style: ElevatedButton.styleFrom(
              backgroundColor: const Color(0xFFED0226),
              foregroundColor: Colors.white,
            ),
            icon: const Icon(Icons.share_rounded, size: 16),
            label: const Text('Bagikan'),
            onPressed: () {
              Navigator.pop(ctx);
              onActionNotice?.call('Tautan sertifikat keamanan siap dibagikan.');
            },
          ),
        ],
      ),
    );
  }

  Widget _buildSquircleButton({
    required String svgIcon,
    required String tooltip,
    required VoidCallback onTap,
  }) {
    return Material(
      color: Colors.transparent,
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(12),
        child: Container(
          width: 36,
          height: 36,
          decoration: BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.circular(12),
            border: Border.all(color: const Color(0xFFE2E8F0)),
            boxShadow: [
              BoxShadow(
                color: const Color(0xFF0B132B).withValues(alpha: 0.04),
                blurRadius: 4,
                offset: const Offset(0, 1),
              ),
            ],
          ),
          child: Center(
            child: StitchSvg(
              svgString: svgIcon,
              size: 16,
              color: const Color(0xFF475569),
            ),
          ),
        ),
      ),
    );
  }
}
