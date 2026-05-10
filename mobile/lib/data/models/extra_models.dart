import 'package:equatable/equatable.dart';

class GarantieModel extends Equatable {
  final String id;
  final String loanId;
  final String type; // e.g., 'Hypothèque', 'Caution', 'Nantissement'
  final double valeur;
  final String description;

  const GarantieModel({
    required this.id,
    required this.loanId,
    required this.type,
    required this.valeur,
    required this.description,
  });

  factory GarantieModel.fromJson(Map<String, dynamic> json) {
    return GarantieModel(
      id: json['id'] as String,
      loanId: json['loanId'] as String,
      type: json['type'] as String,
      valeur: (json['valeur'] as num).toDouble(),
      description: json['description'] as String,
    );
  }

  @override
  List<Object?> get props => [id, loanId, type, valeur, description];
}

class CertificatModel extends Equatable {
  final String id;
  final String accountId;
  final String numeroCertificat;
  final double montantPlacement;
  final double tauxInteret;
  final String dateEcheance;

  const CertificatModel({
    required this.id,
    required this.accountId,
    required this.numeroCertificat,
    required this.montantPlacement,
    required this.tauxInteret,
    required this.dateEcheance,
  });

  factory CertificatModel.fromJson(Map<String, dynamic> json) {
    return CertificatModel(
      id: json['id'] as String,
      accountId: json['accountId'] as String,
      numeroCertificat: json['numeroCertificat'] as String,
      montantPlacement: (json['montantPlacement'] as num).toDouble(),
      tauxInteret: (json['tauxInteret'] as num).toDouble(),
      dateEcheance: json['dateEcheance'] as String,
    );
  }

  @override
  List<Object?> get props => [id, accountId, numeroCertificat, montantPlacement, tauxInteret, dateEcheance];
}

class EpargneTransactionModel extends Equatable {
  final String id;
  final String accountId;
  final String date;
  final double montant;
  final String type; // 'CREDIT' ou 'DEBIT'
  final String libelle;

  const EpargneTransactionModel({
    required this.id,
    required this.accountId,
    required this.date,
    required this.montant,
    required this.type,
    required this.libelle,
  });

  factory EpargneTransactionModel.fromJson(Map<String, dynamic> json) {
    return EpargneTransactionModel(
      id: json['id'] as String,
      accountId: json['accountId'] as String,
      date: json['date'] as String,
      montant: (json['montant'] as num).toDouble(),
      type: json['type'] as String,
      libelle: json['libelle'] as String,
    );
  }

  @override
  List<Object?> get props => [id, accountId, date, montant, type, libelle];
}
