import 'package:flutter/material.dart';
import '../../../core/theme/stitch_icons.dart';
import '../../../data/models/user_session.dart';

/// Informasi Akun Card matching Stitch design:
/// - Section title: INFORMASI AKUN
/// - Phone row with 28x28 light container & Stitch phone SVG
/// - Location row with 28x28 light container & Stitch location SVG
/// - Divider & 24/7 transmission protection description
/// - 3 Tags: Telkomsel Guard, Halo VIP, Prioritas
class AccountInfoCard extends StatelessWidget {
  final UserSession session;

  const AccountInfoCard({
    super.key,
    required this.session,
  });

  String _formatMsisdn(String raw) {
    if (raw.isEmpty) return '-';
    if (raw.contains('-')) return raw;
    if (raw.startsWith('+62') && raw.length >= 12) {
      final prefix = raw.substring(0, 3);
      final mid = raw.substring(3, raw.length - 7);
      final rest = raw.substring(raw.length - 7);
      return '$prefix $mid-$rest';
    }
    return raw;
  }

  @override
  Widget build(BuildContext context) {
    final displayPhone = _formatMsisdn(session.msisdn);
    final displayLocation = session.location.isNotEmpty ? session.location : 'Indonesia';

    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: const Color(0xFFF1F5F9)),
        boxShadow: [
          BoxShadow(
            color: const Color(0xFF0B132B).withValues(alpha: 0.03),
            blurRadius: 20,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Header: INFORMASI AKUN
          const Text(
            'INFORMASI AKUN',
            style: TextStyle(
              fontSize: 12,
              fontWeight: FontWeight.w700,
              color: Color(0xFF94A3B8),
              letterSpacing: 1.0,
            ),
          ),
          const SizedBox(height: 12),

          // Row 1: Phone
          Row(
            children: [
              Container(
                width: 28,
                height: 28,
                decoration: BoxDecoration(
                  color: const Color(0xFFF1F5F9),
                  borderRadius: BorderRadius.circular(8),
                ),
                child: const Center(
                  child: StitchSvg(
                    svgString: StitchIcons.phone,
                    size: 14,
                    color: Color(0xFF64748B),
                  ),
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Text(
                  displayPhone,
                  style: const TextStyle(
                    fontSize: 12,
                    fontWeight: FontWeight.w500,
                    color: Color(0xFF334155),
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 10),

          // Row 2: Location
          Row(
            children: [
              Container(
                width: 28,
                height: 28,
                decoration: BoxDecoration(
                  color: const Color(0xFFF1F5F9),
                  borderRadius: BorderRadius.circular(8),
                ),
                child: const Center(
                  child: StitchSvg(
                    svgString: StitchIcons.location,
                    size: 14,
                    color: Color(0xFF64748B),
                  ),
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Text(
                  displayLocation,
                  style: const TextStyle(
                    fontSize: 12,
                    fontWeight: FontWeight.w500,
                    color: Color(0xFF334155),
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 12),

          // Divider
          const Divider(color: Color(0xFFF1F5F9), height: 1),
          const SizedBox(height: 10),

          // Description
          const Text(
            'Perlindungan identitas & enkripsi transmisi data cloud aktif 24/7 di seluruh jaringan Telkomsel.',
            style: TextStyle(
              fontSize: 11,
              height: 1.5,
              color: Color(0xFF778CA2),
            ),
          ),
          const SizedBox(height: 12),

          // 3 Pill Tags
          Wrap(
            spacing: 6,
            runSpacing: 6,
            children: const [
              _StitchPill(label: 'Telkomsel Guard'),
              _StitchPill(label: 'Halo VIP'),
              _StitchPill(label: 'Prioritas'),
            ],
          ),
        ],
      ),
    );
  }
}

class _StitchPill extends StatelessWidget {
  final String label;

  const _StitchPill({required this.label});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
      decoration: BoxDecoration(
        color: const Color(0xFFF1F5F9),
        borderRadius: BorderRadius.circular(20),
      ),
      child: Text(
        label,
        style: const TextStyle(
          fontSize: 11,
          fontWeight: FontWeight.w600,
          color: Color(0xFF475569),
        ),
      ),
    );
  }
}
