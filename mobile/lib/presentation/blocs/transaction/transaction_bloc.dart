import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import '../../../domain/repositories/transactions_repository.dart';
import 'transaction_event.dart';
import 'transaction_state.dart';

/// TransactionBloc — branché sur l'API mobile
/// `GET /api/v1/mobile/me/transactions/{numCompte}?page=...&size=...`.
///
/// Chaque transaction est exposée à l'UI sous forme de
/// `Map<String, dynamic>` (clés : `id`, `accountId`, `date`, `montant`,
/// `type`, `libelle`) afin de rester compatible avec les widgets existants
/// qui consomment ces champs directement.
///
/// Le filtre par plage de dates est appliqué **côté client** sur la page
/// chargée (le backend ne supporte pas encore de filtre date — à brancher
/// dans une itération ultérieure si besoin).
class TransactionBloc extends Bloc<TransactionEvent, TransactionState> {
  static const int _pageSize = 20;
  final TransactionsRepository _repo;
  DateTimeRange? _currentDateRange;

  TransactionBloc(this._repo) : super(TransactionInitial()) {
    on<LoadTransactions>(_onLoadTransactions);
    on<LoadMoreTransactions>(_onLoadMoreTransactions);
    on<FilterTransactionsByDate>(_onFilterTransactionsByDate);
  }

  Future<void> _onLoadTransactions(
    LoadTransactions event,
    Emitter<TransactionState> emit,
  ) async {
    emit(TransactionLoading());
    _currentDateRange = event.dateRange;
    try {
      final page = await _repo.fetch(numCompte: event.accountId, page: 0, size: _pageSize);
      final items = _filterByDate(page.items, _currentDateRange);
      emit(TransactionLoaded(
        transactions: items.map(_toMap).toList(),
        hasReachedMax: items.length < _pageSize || page.totalPages <= 1,
      ));
    } catch (e) {
      emit(TransactionError(e.toString().replaceFirst('Exception: ', '')));
    }
  }

  Future<void> _onFilterTransactionsByDate(
    FilterTransactionsByDate event,
    Emitter<TransactionState> emit,
  ) async {
    emit(TransactionLoading());
    _currentDateRange = event.dateRange;
    try {
      final page = await _repo.fetch(numCompte: event.accountId, page: 0, size: _pageSize);
      final items = _filterByDate(page.items, _currentDateRange);
      emit(TransactionLoaded(
        transactions: items.map(_toMap).toList(),
        hasReachedMax: items.length < _pageSize || page.totalPages <= 1,
      ));
    } catch (e) {
      emit(TransactionError(e.toString().replaceFirst('Exception: ', '')));
    }
  }

  Future<void> _onLoadMoreTransactions(
    LoadMoreTransactions event,
    Emitter<TransactionState> emit,
  ) async {
    if (state is! TransactionLoaded) return;
    final currentState = state as TransactionLoaded;
    if (currentState.hasReachedMax) return;

    final nextIndex = currentState.transactions.length;
    final pageIndex = nextIndex ~/ _pageSize;
    try {
      final page = await _repo.fetch(numCompte: event.accountId, page: pageIndex, size: _pageSize);
      final items = _filterByDate(page.items, _currentDateRange);
      if (items.isEmpty) {
        emit(TransactionLoaded(
          transactions: currentState.transactions,
          hasReachedMax: true,
        ));
      } else {
        emit(TransactionLoaded(
          transactions: List.of(currentState.transactions)
            ..addAll(items.map(_toMap)),
          hasReachedMax: pageIndex + 1 >= page.totalPages,
        ));
      }
    } catch (e) {
      emit(TransactionError(e.toString().replaceFirst('Exception: ', '')));
    }
  }

  // ── Helpers ────────────────────────────────────────────────────────────

  List<MobileTransaction> _filterByDate(
    List<MobileTransaction> items,
    DateTimeRange? range,
  ) {
    if (range == null) return items;
    return items.where((t) {
      if (t.date == null) return false;
      return t.date!.isAfter(range.start) &&
             t.date!.isBefore(range.end.add(const Duration(days: 1)));
    }).toList();
  }

  Map<String, dynamic> _toMap(MobileTransaction t) {
    return {
      'id':         t.id,
      'accountId':  t.accountId,
      'date':       t.date?.toIso8601String() ?? '',
      'montant':    t.montant,
      'type':       t.type,
      'libelle':    t.libelle,
      'numPiece':   t.numPiece,
      'soldeApres': t.soldeApres,
    };
  }
}
