import 'package:equatable/equatable.dart';

class AmortpModel extends Equatable {
  final int id;
  final String loanId;
  final String dateEcheance;
  final double montantCapital;
  final double montantInteret;
  final double montantTva;
  final bool estPaye;

  const AmortpModel({
    required this.id,
    required this.loanId,
    required this.dateEcheance,
    required this.montantCapital,
    required this.montantInteret,
    required this.montantTva,
    required this.estPaye,
  });

  factory AmortpModel.fromJson(Map<String, dynamic> json) {
    return AmortpModel(
      id: json['id'] as int,
      loanId: json['loanId'] as String? ?? '',
      dateEcheance: json['dateEcheance'] as String,
      montantCapital: (json['montantCapital'] as num).toDouble(),
      montantInteret: (json['montantInteret'] as num).toDouble(),
      montantTva: (json['montantTva'] as num).toDouble(),
      estPaye: json['estPaye'] as bool,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'loanId': loanId,
      'dateEcheance': dateEcheance,
      'montantCapital': montantCapital,
      'montantInteret': montantInteret,
      'montantTva': montantTva,
      'estPaye': estPaye,
    };
  }

  double get montantTotal => montantCapital + montantInteret + montantTva;

  @override
  List<Object?> get props => [
        id,
        loanId,
        dateEcheance,
        montantCapital,
        montantInteret,
        montantTva,
        estPaye,
      ];
}
