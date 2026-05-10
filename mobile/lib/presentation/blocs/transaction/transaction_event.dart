import 'package:equatable/equatable.dart';

abstract class TransactionEvent extends Equatable {
  const TransactionEvent();

  @override
  List<Object?> get props => [];
}

class LoadTransactions extends TransactionEvent {
  final String accountId;
  const LoadTransactions(this.accountId);

  @override
  List<Object?> get props => [accountId];
}

class LoadMoreTransactions extends TransactionEvent {
  final String accountId;
  const LoadMoreTransactions(this.accountId);

  @override
  List<Object?> get props => [accountId];
}
