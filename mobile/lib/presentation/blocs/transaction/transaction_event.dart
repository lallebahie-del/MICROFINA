import 'package:flutter/material.dart';
import 'package:equatable/equatable.dart';

abstract class TransactionEvent extends Equatable {
  const TransactionEvent();

  @override
  List<Object?> get props => [];
}

class LoadTransactions extends TransactionEvent {
  final String accountId;
  final DateTimeRange? dateRange;
  const LoadTransactions(this.accountId, {this.dateRange});

  @override
  List<Object?> get props => [accountId, dateRange];
}

class FilterTransactionsByDate extends TransactionEvent {
  final String accountId;
  final DateTimeRange dateRange;
  const FilterTransactionsByDate(this.accountId, this.dateRange);

  @override
  List<Object?> get props => [accountId, dateRange];
}

class LoadMoreTransactions extends TransactionEvent {
  final String accountId;
  const LoadMoreTransactions(this.accountId);

  @override
  List<Object?> get props => [accountId];
}
