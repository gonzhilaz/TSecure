import 'package:flutter/material.dart';
import '../models/activity_log.dart';

class MockActivityLogs {
  static List<ActivityLog> getLogs() {
    final now = DateTime.now();
    return [
      ActivityLog(
        id: 'log-1',
        title: 'Pemindaian Cepat Selesai',
        description: '84 Aplikasi • 0 Ancaman Ditemukan',
        time: '10:45 WIB',
        date: now,
        icon: Icons.verified_outlined,
        category: LogCategory.pemindaian,
        isSafe: true,
      ),
      ActivityLog(
        id: 'log-2',
        title: 'Tautan Mencurigakan Diblokir',
        description: 'Phishing terdeteksi di browser: bit.ly/promo-telkomsel-palsu',
        time: '08:12 WIB',
        date: now,
        icon: Icons.language_outlined,
        category: LogCategory.jaringan,
        isSafe: false,
      ),
      ActivityLog(
        id: 'log-3',
        title: 'Audit Keamanan Wi-Fi',
        description: "Terhubung ke 'Telkomsel_Orbit_5G' • WPA3",
        time: '19:30 WIB',
        date: now.subtract(const Duration(days: 1)),
        icon: Icons.wifi,
        category: LogCategory.jaringan,
        isSafe: true,
      ),
      ActivityLog(
        id: 'log-4',
        title: 'Pembaruan Definisi Virus',
        description: 'Versi 2026.09.24-KSP berhasil diunduh',
        time: '14:15 WIB',
        date: now.subtract(const Duration(days: 1)),
        icon: Icons.sync,
        category: LogCategory.pemindaian,
        isSafe: true,
      ),
      ActivityLog(
        id: 'log-5',
        title: 'Analisis Instalasi Aplikasi',
        description: "APK 'MyTelkomsel' terverifikasi resmi tanda tangan digital",
        time: '11:05 WIB',
        date: now.subtract(const Duration(days: 2)),
        icon: Icons.system_update_alt_outlined,
        category: LogCategory.aplikasi,
        isSafe: true,
      ),
    ];
  }
}
