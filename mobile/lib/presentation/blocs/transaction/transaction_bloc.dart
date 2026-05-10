import 'package:flutter_bloc/flutter_bloc.dart';
import '../../../data/datasources/mock/mock_data.dart';
import 'transaction_event.dart';
import 'transaction_state.dart';

class TransactionBloc extends Bloc<TransactionEvent, TransactionState> {
  static const int _pageSize = 15;

  TransactionBloc() : super(TransactionInitial()) {
    on<LoadTransactions>(_onLoadTransactions);
    on<LoadMoreTransactions>(_onLoadMoreTransactions);
  }

  Future<void> _onLoadTransactions(LoadTransactions event, Emitter<TransactionState> emit) async {
    emit(TransactionLoading());
    
    // Simulation du délai de chargement initial (pour voir le Shimmer)
    await Future.delayed(const Duration(milliseconds: 1500));
    
    final allTransactions = MockData.mockEpargneTransactions
        .where((tx) => tx['accountId'] == event.accountId)
        .toList();
    
    final firstBatch = allTransactions.take(_pageSize).toList();
    
    emit(TransactionLoaded(
      transactions: firstBatch,
      hasReachedMax: firstBatch.length >= allTransactions.length,
    ));
  }

  Future<void> _onLoadMoreTransactions(LoadMoreTransactions event, Emitter<TransactionState> emit) async {
    if (state is! TransactionLoaded) return;
    
    final currentState = state as TransactionLoaded;
    if (currentState.hasReachedMax) return;

    // Simulation d'un petit délai réseau pour le load more
    await Future.delayed(const Duration(milliseconds: 500));

    final allTransactions = MockData.mockEpargneTransactions
        .where((tx) => tx['accountId'] == event.accountId)
        .toList();
    
    final nextIndex = currentState.transactions.length;
    final nextBatch = allTransactions.skip(nextIndex).take(_pageSize).toList();
    
    emit(TransactionLoaded(
      transactions: List.of(currentState.transactions)..addAll(nextBatch),
      hasReachedMax: (nextIndex + nextBatch.length) >= allTransactions.length,
    ));
  }
}
