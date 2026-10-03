class ThreatDetailItem {
  final String id;
  final String fileName;
  final String filePath;
  final String virusName;
  final String threatType; // Malware, Trojan, Riskware, Phishing, Adware
  final String severity;   // KRITIS, TINGGI, SEDANG
  String actionTaken;      // DIKARANTINA, DIHAPUS, DIISOLASI, AKTIF
  final DateTime detectedAt;

  ThreatDetailItem({
    required this.id,
    required this.fileName,
    required this.filePath,
    required this.virusName,
    this.threatType = 'Malware',
    this.severity = 'TINGGI',
    this.actionTaken = 'AKTIF',
    DateTime? detectedAt,
  }) : detectedAt = detectedAt ?? DateTime.now();

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'fileName': fileName,
      'filePath': filePath,
      'virusName': virusName,
      'threatType': threatType,
      'severity': severity,
      'actionTaken': actionTaken,
      'detectedAt': detectedAt.toIso8601String(),
    };
  }

  factory ThreatDetailItem.fromJson(Map<String, dynamic> json) {
    return ThreatDetailItem(
      id: json['id'] as String? ?? 'threat-${DateTime.now().millisecondsSinceEpoch}',
      fileName: json['fileName'] as String? ?? 'berkas_terinfeksi',
      filePath: json['filePath'] as String? ?? '',
      virusName: json['virusName'] as String? ?? 'Malware',
      threatType: json['threatType'] as String? ?? 'Malware',
      severity: json['severity'] as String? ?? 'TINGGI',
      actionTaken: json['actionTaken'] as String? ?? 'AKTIF',
      detectedAt: DateTime.tryParse(json['detectedAt'] as String? ?? '') ?? DateTime.now(),
    );
  }
}
