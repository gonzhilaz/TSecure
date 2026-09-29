import 'package:flutter/material.dart';

enum AuditStatus { passed, warning, actionRequired }

class SecurityAuditItem {
  final String id;
  final String title;
  final String subtitle;
  final AuditStatus status;
  final IconData icon;
  final String? actionLabel;
  final bool isToggleable;
  final bool isToggled;
  final int scoreWeight;

  const SecurityAuditItem({
    required this.id,
    required this.title,
    required this.subtitle,
    required this.status,
    required this.icon,
    this.actionLabel,
    this.isToggleable = false,
    this.isToggled = true,
    this.scoreWeight = 10,
  });

  SecurityAuditItem copyWith({
    String? id,
    String? title,
    String? subtitle,
    AuditStatus? status,
    IconData? icon,
    String? actionLabel,
    bool? isToggleable,
    bool? isToggled,
    int? scoreWeight,
  }) {
    return SecurityAuditItem(
      id: id ?? this.id,
      title: title ?? this.title,
      subtitle: subtitle ?? this.subtitle,
      status: status ?? this.status,
      icon: icon ?? this.icon,
      actionLabel: actionLabel ?? this.actionLabel,
      isToggleable: isToggleable ?? this.isToggleable,
      isToggled: isToggled ?? this.isToggled,
      scoreWeight: scoreWeight ?? this.scoreWeight,
    );
  }
}
