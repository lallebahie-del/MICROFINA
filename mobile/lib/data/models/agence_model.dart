import 'package:equatable/equatable.dart';

class AgenceModel extends Equatable {
  final String id;
  final String code;
  final String nom;
  final String ville;
  final String adresse;

  const AgenceModel({
    required this.id,
    required this.code,
    required this.nom,
    required this.ville,
    required this.adresse,
  });

  factory AgenceModel.fromJson(Map<String, dynamic> json) {
    return AgenceModel(
      id: json['id'] as String,
      code: json['code'] as String,
      nom: json['nom'] as String,
      ville: json['ville'] as String,
      adresse: json['adresse'] as String,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'code': code,
      'nom': nom,
      'ville': ville,
      'adresse': adresse,
    };
  }

  @override
  List<Object?> get props => [id, code, nom, ville, adresse];
}
