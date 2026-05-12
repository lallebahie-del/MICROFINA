import 'package:equatable/equatable.dart';

class CompteEpsModel extends Equatable {
  final String id;
  final String numeroCompte;
  final String libelle;
  final double availableBalance;
  final double blockedBalance;
  final String accountType; // ex: 'EPARGNE', 'GARANTIE'
  final String devise;
  final DateTime? lastSyncedAt;
  final bool isDefaultAccount;
  final String? accountTypeColor; // Code Hexadécimal

  const CompteEpsModel({
    required this.id,
    required this.numeroCompte,
    required this.libelle,
    required this.availableBalance,
    required this.blockedBalance,
    required this.accountType,
    required this.devise,
    this.lastSyncedAt,
    this.isDefaultAccount = false,
    this.accountTypeColor,
  });

  double get totalBalance => availableBalance + blockedBalance;

  factory CompteEpsModel.fromJson(Map<String, dynamic> json) {
    return CompteEpsModel(
      id: json['id'] as String,
      numeroCompte: json['numeroCompte'] as String,
      libelle: json['libelle'] as String,
      availableBalance: (json['availableBalance'] as num).toDouble(),
      blockedBalance: (json['blockedBalance'] as num).toDouble(),
      accountType: json['accountType'] as String,
      devise: json['devise'] as String,
      lastSyncedAt: json['lastSyncedAt'] != null
          ? DateTime.parse(json['lastSyncedAt'] as String)
          : null,
      isDefaultAccount: json['isDefaultAccount'] as bool? ?? false,
      accountTypeColor: json['accountTypeColor'] as String?,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'numeroCompte': numeroCompte,
      'libelle': libelle,
      'availableBalance': availableBalance,
      'blockedBalance': blockedBalance,
      'accountType': accountType,
      'devise': devise,
      'lastSyncedAt': lastSyncedAt?.toIso8601String(),
      'isDefaultAccount': isDefaultAccount,
      'accountTypeColor': accountTypeColor,
    };
  }

  @override
  List<Object?> get props => [
    id,
    numeroCompte,
    libelle,
    availableBalance,
    blockedBalance,
    accountType,
    devise,
    lastSyncedAt,
    isDefaultAccount,
    accountTypeColor,
  ];
}
