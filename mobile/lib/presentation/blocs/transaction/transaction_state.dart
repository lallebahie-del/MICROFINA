import 'package:equatable/equatable.dart';

abstract class TransactionState extends Equatable {
  const TransactionState();

  @override
  List<Object?> get props => [];
}

class TransactionInitial extends TransactionState {}

class TransactionLoading extends TransactionState {}

class TransactionLoaded extends TransactionState {
  final List<Map<String, dynamic>> transactions;
  final bool hasReachedMax;

  const TransactionLoaded({
    required this.transactions,
    required this.hasReachedMax,
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
