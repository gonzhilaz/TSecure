import 'package:flutter/material.dart';

class SecurityFeatureTelemetryRow {
  final String label;
  final String value;

  const SecurityFeatureTelemetryRow(this.label, this.value);
}

class SecurityFeatureConfig {
  final String title;
  final String engine;
  final String description;
  final String actionLabel;
  final IconData icon;
  final String? actionMessage;
  final VoidCallback? onAction;
  final List<SecurityFeatureTelemetryRow> telemetry;

  SecurityFeatureConfig({
    required this.title,
    required this.icon,
    required this.engine,
    required this.description,
    required this.actionLabel,
    this.actionMessage,
    this.onAction,
    required this.telemetry,
  });
}
