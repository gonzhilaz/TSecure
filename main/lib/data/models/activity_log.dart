import 'package:flutter/material.dart';
import 'threat_detail_item.dart';

enum LogCategory { pemindaian, jaringan, aplikasi }

class ActivityLog {
  final String id;
  final String title;
  final String description;
  final String time;
  final DateTime date;
  final IconData icon;
  final LogCategory category;
  final bool isSafe;
  final List<ThreatDetailItem> threats;

  const ActivityLog({
    required this.id,
    required this.title,
    required this.description,
    required this.time,
    required this.date,
    required this.icon,
    required this.category,
    this.isSafe = true,
    this.threats = const [],
  });

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'title': title,
      'description': description,
      'time': time,
      'date': date.toIso8601String(),
      'iconCodePoint': icon.codePoint,
      'category': category.name,
      'isSafe': isSafe,
      'threats': threats.map((t) => t.toJson()).toList(),
    };
  }

  factory ActivityLog.fromJson(Map<String, dynamic> json) {
    final rawThreats = json['threats'] as List<dynamic>?;
    final parsedThreats = rawThreats != null
        ? rawThreats.map((t) => ThreatDetailItem.fromJson(t as Map<String, dynamic>)).toList()
        : <ThreatDetailItem>[];

    return ActivityLog(
      id: json['id'] as String? ?? 'log-${DateTime.now().millisecondsSinceEpoch}',
      title: json['title'] as String? ?? 'Aktivitas Keamanan',
      description: json['description'] as String? ?? '',
      time: json['time'] as String? ?? '',
      date: DateTime.tryParse(json['date'] as String? ?? '') ?? DateTime.now(),
      icon: _resolveIcon(json['iconCodePoint'] as int?),
      category: LogCategory.values.firstWhere(
        (c) => c.name == (json['category'] as String?),
        orElse: () => LogCategory.pemindaian,
      ),
      isSafe: json['isSafe'] as bool? ?? true,
      threats: parsedThreats,
    );
  }

  ActivityLog copyWith({
    String? id,
    String? title,
    String? description,
    String? time,
    DateTime? date,
    IconData? icon,
    LogCategory? category,
    bool? isSafe,
    List<ThreatDetailItem>? threats,
  }) {
    return ActivityLog(
      id: id ?? this.id,
      title: title ?? this.title,
      description: description ?? this.description,
      time: time ?? this.time,
      date: date ?? this.date,
      icon: icon ?? this.icon,
      category: category ?? this.category,
      isSafe: isSafe ?? this.isSafe,
      threats: threats ?? this.threats,
    );
  }

  static IconData _resolveIcon(int? codePoint) {
    if (codePoint == Icons.vpn_key_outlined.codePoint) {
      return Icons.vpn_key_outlined;
    }
    if (codePoint == Icons.wifi_protected_setup_rounded.codePoint) {
      return Icons.wifi_protected_setup_rounded;
    }
    if (codePoint == Icons.warning_amber_rounded.codePoint) {
      return Icons.warning_amber_rounded;
    }
    if (codePoint == Icons.shield_outlined.codePoint) {
      return Icons.shield_outlined;
    }
    if (codePoint == Icons.security_rounded.codePoint) {
      return Icons.security_rounded;
    }
    return Icons.verified_outlined;
  }
}
