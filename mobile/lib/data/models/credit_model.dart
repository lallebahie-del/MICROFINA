import 'package:equatable/equatable.dart';

class LoanModel extends Equatable {
  final String loanId;
  final double totalAmount;
  final double remainingCapital;
  final double interestRate;
  final DateTime endDate;
  final int statusCode;
  final DateTime? nextInstallmentDueDate;

  const LoanModel({
    required this.loanId,
    required this.totalAmount,
    required this.remainingCapital,
    required this.interestRate,
    required this.endDate,
    required this.statusCode,
    this.nextInstallmentDueDate,
  });

  factory LoanModel.fromJson(Map<String, dynamic> json) {
    return LoanModel(
      loanId: json['loanId'] as String,
      totalAmount: (json['totalAmount'] as num).toDouble(),
      remainingCapital: (json['remainingCapital'] as num).toDouble(),
      interestRate: (json['interestRate'] as num).toDouble(),
      endDate: DateTime.parse(json['endDate'] as String),
      statusCode: json['statusCode'] as int,
      nextInstallmentDueDate: json['nextInstallmentDueDate'] != null
          ? DateTime.parse(json['nextInstallmentDueDate'] as String)
          : null,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'loanId': loanId,
      'totalAmount': totalAmount,
      'remainingCapital': remainingCapital,
      'interestRate': interestRate,
      'endDate': endDate.toIso8601String(),
      'statusCode': statusCode,
      'nextInstallmentDueDate': nextInstallmentDueDate?.toIso8601String(),
    };
  }

  // Améliorations Fonctionnelles
  double get progressPercentage =>
      totalAmount > 0 ? ((totalAmount - remainingCapital) / totalAmount) * 100 : 0;

  String get statusLabel {
    switch (statusCode) {
      case 1:
        return 'Actif';
      case 2:
        return 'En retard';
      case 3:
        return 'Clôturé';
      default:
        return 'Inconnu';
    }
  }

  bool get isLate => statusCode == 2;

  @override
  List<Object?> get props => [
        loanId,
        totalAmount,
        remainingCapital,
        interestRate,
        endDate,
        statusCode,
        nextInstallmentDueDate,
      ];
}
