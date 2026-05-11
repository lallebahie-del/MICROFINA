import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import '../../../data/datasources/mock/mock_data.dart';
import '../../../data/models/extra_models.dart';
import 'transaction_event.dart';
import 'transaction_state.dart';

export 'transaction_event.dart';
export 'transaction_state.dart';

class TransactionBloc extends Bloc<TransactionEvent, TransactionState> {
  static const int _pageSize = 20; // Mis à jour à 20 selon Tâche 3.4
  DateTimeRange? _currentDateRange;

  static List<EpargneTransactionModel> _toModels(List<Map<String, dynamic>> batch) {
    return batch.map(EpargneTransactionModel.fromJson).toList();
  }

  TransactionBloc() : super(TransactionInitial()) {
    on<LoadTransactions>(_onLoadTransactions);
    on<LoadMoreTransactions>(_onLoadMoreTransactions);
    on<FilterTransactionsByDate>(_onFilterTransactionsByDate);
  }

  Future<void> _onLoadTransactions(LoadTransactions event, Emitter<TransactionState> emit) async {
    emit(TransactionLoading());
    _currentDateRange = event.dateRange;
    
    final firstBatch = _toModels(await MockData.getPaginatedTransactions(
      accountId: event.accountId,
      page: 0,
      pageSize: _pageSize,
      dateRange: _currentDateRange,
    ));
    
    emit(TransactionLoaded(
      transactions: firstBatch,
      hasReachedMax: firstBatch.length < _pageSize,
    ));
  }

  Future<void> _onFilterTransactionsByDate(FilterTransactionsByDate event, Emitter<TransactionState> emit) async {
    emit(TransactionLoading());
    _currentDateRange = event.dateRange;
    
    final filteredBatch = _toModels(await MockData.getPaginatedTransactions(
      accountId: event.accountId,
      page: 0,
      pageSize: _pageSize,
      dateRange: _currentDateRange,
    ));
    
    emit(TransactionLoaded(
      transactions: filteredBatch,
      hasReachedMax: filteredBatch.length < _pageSize,
    ));
  }

  Future<void> _onLoadMoreTransactions(LoadMoreTransactions event, Emitter<TransactionState> emit) async {
    if (state is! TransactionLoaded) return;
    
    final currentState = state as TransactionLoaded;
    if (currentState.hasReachedMax) return;

    final nextIndex = currentState.transactions.length;
    final page = nextIndex ~/ _pageSize;
    
    final nextBatch = _toModels(await MockData.getPaginatedTransactions(
      accountId: event.accountId,
      page: page,
      pageSize: _pageSize,
      dateRange: _currentDateRange,
    ));
    
    if (nextBatch.isEmpty) {
      emit(TransactionLoaded(
        transactions: currentState.transactions,
        hasReachedMax: true,
      ));
    } else {
      emit(TransactionLoaded(
        transactions: List.of(currentState.transactions)..addAll(nextBatch),
        hasReachedMax: nextBatch.length < _pageSize,
      ));
    }
  }
}
