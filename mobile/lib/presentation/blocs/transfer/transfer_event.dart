import 'package:equatable/equatable.dart';

abstract class TransferEvent extends Equatable {
  const TransferEvent();

  @override
  List<Object?> get props => [];
}

class PerformTransfer extends TransferEvent {
  final String fromAccountId;
  final String toAccountId;
  final double amount;
  final String reason;

  const PerformTransfer({
    required this.fromAccountId,
    required this.toAccountId,
    required this.amount,
    required this.reason,
  });

  @override
  List<Object?> get props => [fromAccountId, toAccountId, amount, reason];
}

class PerformExternalTransfer extends TransferEvent {
  final String fromAccountId;
  final String beneficiaryPhone;
  final double amount;
  final String reason;

  const PerformExternalTransfer({
    required this.fromAccountId,
    required this.beneficiaryPhone,
    required this.amount,
    required this.reason,
  });

  @override
  List<Object?> get props => [fromAccountId, beneficiaryPhone, amount, reason];
}
