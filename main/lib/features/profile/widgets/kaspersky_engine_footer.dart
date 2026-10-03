import 'package:flutter/material.dart';

/// Clean and subtle footer indicating active Kaspersky Security Engine status.
class KasperskyEngineFooter extends StatelessWidget {
  const KasperskyEngineFooter({super.key});

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.center,
      children: const [
        Icon(Icons.shield_outlined, size: 14, color: Color(0xFF94A3B8)),
        SizedBox(width: 6),
        Text(
          'Powered by Kaspersky Security Engine • Mesin Aktif',
          style: TextStyle(
            fontSize: 11,
            fontWeight: FontWeight.w500,
            color: Color(0xFF94A3B8),
          ),
        ),
      ],
    );
  }
}
