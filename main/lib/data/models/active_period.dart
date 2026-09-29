class ActivePeriod {
  final String packageName;
  final String packageDescription;
  final DateTime expiryDate;
  final int activeDeviceCount;
  final int maxDeviceAllowed;
  final bool isValid;
  final String statusMessage;
  final String activationStatus;
  final bool isPendingProvisioning;
  final String licenseKey;

  const ActivePeriod({
    required this.packageName,
    required this.packageDescription,
    required this.expiryDate,
    required this.activeDeviceCount,
    required this.maxDeviceAllowed,
    required this.isValid,
    required this.statusMessage,
    this.activationStatus = 'ACTIVATED',
    this.isPendingProvisioning = false,
    this.licenseKey = '',
  });

  int get daysRemaining {
    final diff = expiryDate.difference(DateTime.now()).inDays;
    return diff > 0 ? diff : 0;
  }

  bool get isExpired => DateTime.now().isAfter(expiryDate);
  bool get isPendingKsp => activationStatus == 'ACTIVATION_PENDING_KSP';
  bool get isActivated => activationStatus == 'ACTIVATED';
  bool get isPendingActivation => activationStatus == 'PENDING_ACTIVATION';
}
