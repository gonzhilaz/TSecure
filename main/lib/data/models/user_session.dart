class UserSession {
  final String msisdn;
  final String name;
  final String tier;
  final String location;
  final String mobileId;
  final bool isKasperskyInitialized;

  const UserSession({
    required this.msisdn,
    required this.name,
    required this.tier,
    required this.location,
    required this.mobileId,
    this.isKasperskyInitialized = false,
  });

  UserSession copyWith({
    String? msisdn,
    String? name,
    String? tier,
    String? location,
    String? mobileId,
    bool? isKasperskyInitialized,
  }) {
    return UserSession(
      msisdn: msisdn ?? this.msisdn,
      name: name ?? this.name,
      tier: tier ?? this.tier,
      location: location ?? this.location,
      mobileId: mobileId ?? this.mobileId,
      isKasperskyInitialized:
          isKasperskyInitialized ?? this.isKasperskyInitialized,
    );
  }
}
