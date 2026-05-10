import 'package:equatable/equatable.dart';

class EpargneModel extends Equatable {
  final String id;
  final String libelle;
  final double solde;
  final String dateOuverture;
  final String typeEpargne;

  const EpargneModel({
    required this.id,
    required this.libelle,
    required this.solde,
    required this.dateOuverture,
    required this.typeEpargne,
  });

  factory EpargneModel.fromJson(Map<String, dynamic> json) {
    return EpargneModel(
      id: json['id'] as String,
      libelle: json['libelle'] as String,
      solde: (json['solde'] as num).toDouble(),
      dateOuverture: json['dateOuverture'] as String,
      typeEpargne: json['typeEpargne'] as String,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'libelle': libelle,
      'solde': solde,
      'dateOuverture': dateOuverture,
      'typeEpargne': typeEpargne,
    };
  }

  @override
  List<Object?> get props => [id, libelle, solde, dateOuverture, typeEpargne];
}
