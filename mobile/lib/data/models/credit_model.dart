import 'package:equatable/equatable.dart';

class CreditModel extends Equatable {
  final String id;
  final double capitalRestantDu;
  final double taux;
  final String statut; // e.g., 'ACTIF', 'SOLDE', 'EN_RETARD'
  final String dateDeblocage;
  final double montantInitial;

  const CreditModel({
    required this.id,
    required this.capitalRestantDu,
    required this.taux,
    required this.statut,
    required this.dateDeblocage,
    required this.montantInitial,
  });

  factory CreditModel.fromJson(Map<String, dynamic> json) {
    return CreditModel(
      id: json['id'] as String,
      capitalRestantDu: (json['capitalRestantDu'] as num).toDouble(),
      taux: (json['taux'] as num).toDouble(),
      statut: json['statut'] as String,
      dateDeblocage: json['dateDeblocage'] as String,
      montantInitial: (json['montantInitial'] as num).toDouble(),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'capitalRestantDu': capitalRestantDu,
      'taux': taux,
      'statut': statut,
      'dateDeblocage': dateDeblocage,
      'montantInitial': montantInitial,
    };
  }

  @override
  List<Object?> get props => [
        id,
        capitalRestantDu,
        taux,
        statut,
        dateDeblocage,
        montantInitial,
      ];
}
