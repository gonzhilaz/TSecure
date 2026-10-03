class QuarantineItem {
  final String id;
  final String fileName;
  final String originalPath;
  final String vaultPath;
  final String threatName;
  final String threatType;
  final String severity;
  final int fileSize;
  final String quarantineDate;
  final String status;

  QuarantineItem({
    required this.id,
    required this.fileName,
    required this.originalPath,
    required this.vaultPath,
    required this.threatName,
    required this.threatType,
    required this.severity,
    required this.fileSize,
    required this.quarantineDate,
    required this.status,
  });

  factory QuarantineItem.fromMap(Map<String, dynamic> map) {
    return QuarantineItem(
      id: map['id']?.toString() ?? '',
      fileName: map['fileName']?.toString() ?? 'Berkas Terisolasi',
      originalPath: map['originalPath']?.toString() ?? '',
      vaultPath: map['vaultPath']?.toString() ?? '',
      threatName: map['threatName']?.toString() ?? 'Ancaman Malware',
      threatType: map['threatType']?.toString() ?? 'Trojan',
      severity: map['severity']?.toString() ?? 'TINGGI',
      fileSize: (map['fileSize'] as num?)?.toInt() ?? 0,
      quarantineDate: map['quarantineDate']?.toString() ?? '',
      status: map['status']?.toString() ?? 'ISOLATED',
    );
  }

  Map<String, dynamic> toMap() {
    return {
      'id': id,
      'fileName': fileName,
      'originalPath': originalPath,
      'vaultPath': vaultPath,
      'threatName': threatName,
      'threatType': threatType,
      'severity': severity,
      'fileSize': fileSize,
      'quarantineDate': quarantineDate,
      'status': status,
    };
  }

  String get formattedSize {
    if (fileSize < 1024) return '$fileSize B';
    if (fileSize < 1024 * 1024) {
      return '${(fileSize / 1024).toStringAsFixed(1)} KB';
    }
    return '${(fileSize / (1024 * 1024)).toStringAsFixed(2)} MB';
  }
}
