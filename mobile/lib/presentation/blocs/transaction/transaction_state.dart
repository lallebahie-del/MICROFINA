import 'package:equatable/equatable.dart';
import '../../../data/models/extra_models.dart';

abstract class TransactionState extends Equatable {
  const TransactionState();

  @override
  List<Object?> get props => [];

  // Add these common getters to avoid casting in UI
  List<EpargneTransactionModel> get transactions => [];
  bool get hasReachedMax => false;
}

class TransactionInitial extends TransactionState {}

class TransactionLoading extends TransactionState {}

class TransactionLoaded extends TransactionState {
  @override
  final List<EpargneTransactionModel> transactions;
  @override
  final bool hasReachedMax;

  const TransactionLoaded({
    required this.transactions,
    this.hasReachedMax = false,
  });

  @override
  List<Object?> get props => [transactions, hasReachedMax];
}

class TransactionError extends TransactionState {
  final String message;

  const TransactionError(this.message);

  @override
  List<Object?> get props => [message];
}
