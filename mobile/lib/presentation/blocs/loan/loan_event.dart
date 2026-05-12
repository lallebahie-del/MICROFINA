import 'package:equatable/equatable.dart';

abstract class LoanEvent extends Equatable {
  const LoanEvent();

  @override
  List<Object?> get props => [];
}

class FetchLoans extends LoanEvent {}

class FetchLoanDetails extends LoanEvent {
  final String loanId;

  const FetchLoanDetails(this.loanId);

  @override
  List<Object?> get props => [loanId];
}

class SubmitLoanRequest extends LoanEvent {
  final double amount;
  final int durationMonths;
  final String purpose;

  const SubmitLoanRequest({
    required this.amount,
    required this.durationMonths,
    required this.purpose,
  });

  @override
  List<Object?> get props => [amount, durationMonths, purpose];
}

class PayInstallment extends LoanEvent {
  final String loanId;
  final int installmentId;

  const PayInstallment({required this.loanId, required this.installmentId});

  @override
  List<Object?> get props => [loanId, installmentId];
}

class ClearInstallmentPaymentPulse extends LoanEvent {
  const ClearInstallmentPaymentPulse();
}
