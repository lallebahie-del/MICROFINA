import 'package:equatable/equatable.dart';

class GarantieModel extends Equatable {
  final String id;
  final String loanId;
  final String type; // e.g., 'Hypothèque', 'Caution', 'Nantissement'
  final double valeur;
  final String description;

  /// Statut métier (GARANTIES) : ex. Validée, En cours de constitution, Libérée.
  final String statut;

  const GarantieModel({
    required this.id,
    required this.loanId,
    required this.type,
    required this.valeur,
    required this.description,
    this.statut = 'Validée',
  });

  factory GarantieModel.fromJson(Map<String, dynamic> json) {
    return GarantieModel(
      id: json['id'] as String,
      loanId: json['loanId'] as String,
      type: json['type'] as String,
      valeur: (json['valeur'] as num).toDouble(),
      description: json['description'] as String,
      statut: json['statut'] as String? ?? 'Validée',
    );
  }

  @override
  List<Object?> get props => [id, loanId, type, valeur, description, statut];
}

class CertificatModel extends Equatable {
  final String id;
  final String accountId;
  final String numeroCertificat;
  final double montantPlacement;
  final double tauxInteret;
  final DateTime startDate;
  final DateTime maturityDate;

  const CertificatModel({
    required this.id,
    required this.accountId,
    required this.numeroCertificat,
    required this.montantPlacement,
    required this.tauxInteret,
    required this.startDate,
    required this.maturityDate,
  });

  factory CertificatModel.fromJson(Map<String, dynamic> json) {
    return CertificatModel(
      id: json['id'] as String,
      accountId: json['accountId'] as String,
      numeroCertificat: json['numeroCertificat'] as String,
      montantPlacement: (json['montantPlacement'] as num).toDouble(),
      tauxInteret: (json['tauxInteret'] as num).toDouble(),
      startDate: DateTime.parse(
        json['startDate'] ??
            DateTime.now().subtract(const Duration(days: 30)).toIso8601String(),
      ),
      maturityDate: DateTime.parse(
        json['dateEcheance'] ?? json['maturityDate'],
      ),
    );
  }

  double get progress {
    final now = DateTime.now();
    final total = maturityDate.difference(startDate).inSeconds;
    final elapsed = now.difference(startDate).inSeconds;
    if (total <= 0) return 1.0;
    return (elapsed / total).clamp(0.0, 1.0);
  }

  int get remainingDays {
    final diff = maturityDate.difference(DateTime.now()).inDays;
    return diff < 0 ? 0 : diff;
  }

  double get expectedInterests {
    final totalDays = maturityDate.difference(startDate).inDays;
    if (totalDays <= 0) return 0;
    // Formule simplifiée : (Capital * Taux * (Jours / 365)) / 100
    return (montantPlacement * tauxInteret * (totalDays / 365)) / 100;
  }

  @override
  List<Object?> get props => [
    id,
    accountId,
    numeroCertificat,
    montantPlacement,
    tauxInteret,
    startDate,
    maturityDate,
  ];
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
