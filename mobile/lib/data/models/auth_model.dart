import 'package:equatable/equatable.dart';

class AuthModel extends Equatable {
  final String phoneNumber;
  final String accessToken;
  final String refreshToken;
  final DateTime expiresAt;
  final bool isBiometricEnabled;
  final DateTime? lastLoginDate;

  const AuthModel({
    required this.phoneNumber,
    required this.accessToken,
    required this.refreshToken,
    required this.expiresAt,
    this.isBiometricEnabled = false,
    this.lastLoginDate,
  });

  factory AuthModel.fromJson(Map<String, dynamic> json) {
    return AuthModel(
      phoneNumber: json['phoneNumber'] as String,
      accessToken: json['accessToken'] as String,
      refreshToken: json['refreshToken'] as String,
      expiresAt: DateTime.parse(json['expiresAt'] as String),
      isBiometricEnabled: json['isBiometricEnabled'] as bool? ?? false,
      lastLoginDate: json['lastLoginDate'] != null
          ? DateTime.parse(json['lastLoginDate'] as String)
          : null,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'phoneNumber': phoneNumber,
      'accessToken': accessToken,
      'refreshToken': refreshToken,
      'expiresAt': expiresAt.toIso8601String(),
      'isBiometricEnabled': isBiometricEnabled,
      'lastLoginDate': lastLoginDate?.toIso8601String(),
    };
  }

  @override
  List<Object?> get props => [
    phoneNumber,
    accessToken,
    refreshToken,
    expiresAt,
    isBiometricEnabled,
    lastLoginDate,
  ];

  AuthModel copyWith({
    String? phoneNumber,
    String? accessToken,
    String? refreshToken,
    DateTime? expiresAt,
    bool? isBiometricEnabled,
    DateTime? lastLoginDate,
  }) {
    return AuthModel(
      phoneNumber: phoneNumber ?? this.phoneNumber,
      accessToken: accessToken ?? this.accessToken,
      refreshToken: refreshToken ?? this.refreshToken,
      expiresAt: expiresAt ?? this.expiresAt,
      isBiometricEnabled: isBiometricEnabled ?? this.isBiometricEnabled,
      lastLoginDate: lastLoginDate ?? this.lastLoginDate,
    );
  }
}
