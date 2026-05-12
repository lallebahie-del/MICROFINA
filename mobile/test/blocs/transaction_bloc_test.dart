import 'package:bloc_test/bloc_test.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:micro_credit/data/models/extra_models.dart';
import 'package:micro_credit/domain/repositories/transaction_repository.dart';
import 'package:micro_credit/presentation/blocs/transaction/transaction_bloc.dart';

class _FakeTransactionRepository implements TransactionRepository {
  _FakeTransactionRepository({
    this.firstPageSize = 5,
    this.secondPageSize = 0,
    this.filteredFirstPageSize,
  });

  final int firstPageSize;
  final int secondPageSize;
  final int? filteredFirstPageSize;

  EpargneTransactionModel _tx(String id, String accountId) {
    return EpargneTransactionModel(
      id: id,
      accountId: accountId,
      date: DateTime.utc(2024, 6, 15).toIso8601String(),
      montant: 1000,
      type: 'CREDIT',
      libelle: 'Test $id',
    );
  }

  @override
  Future<List<EpargneTransactionModel>> getPaginatedTransactions({
    required String accountId,
    required int page,
    required int pageSize,
    DateTimeRange? dateRange,
  }) async {
    if (page == 0) {
      final n = dateRange != null ? (filteredFirstPageSize ?? 1) : firstPageSize;
      return List.generate(
        n,
        (i) => _tx('p0_${dateRange != null ? 'f' : 'n'}_$i', accountId),
      );
    }
    if (page == 1) {
      return List.generate(
        secondPageSize,
        (i) => _tx('p1_$i', accountId),
      );
    }
    return [];
  }

  @override
  Future<List<EpargneTransactionModel>> getTransactions(String accountId) async => [];

  @override
  Future<bool> transferFunds({
    required String fromAccountId,
    required String toAccountId,
    required double amount,
    required String reason,
  }) async =>
      false;
}

void main() {
  group('TransactionBloc', () {
    const accountId = 'acc_test';

    blocTest<TransactionBloc, TransactionState>(
      'LoadTransactions emits loading then loaded',
      build: () => TransactionBloc(_FakeTransactionRepository(firstPageSize: 5)),
      act: (bloc) => bloc.add(const LoadTransactions(accountId)),
      expect: () => [
        isA<TransactionLoading>(),
        isA<TransactionLoaded>().having(
          (TransactionLoaded s) => s.transactions.length,
          'count',
          5,
        ).having(
          (TransactionLoaded s) => s.hasReachedMax,
          'hasReachedMax',
          true,
        ),
      ],
    );

    blocTest<TransactionBloc, TransactionState>(
      'LoadMoreTransactions appends next page',
      build: () => TransactionBloc(
        _FakeTransactionRepository(firstPageSize: 20, secondPageSize: 3),
      ),
      act: (bloc) async {
        bloc.add(const LoadTransactions(accountId));
        await bloc.stream.firstWhere((s) => s is TransactionLoaded);
        bloc.add(const LoadMoreTransactions(accountId));
      },
      expect: () => [
        isA<TransactionLoading>(),
        isA<TransactionLoaded>().having(
          (TransactionLoaded s) => s.transactions.length,
          'after first page',
          20,
        ).having(
          (TransactionLoaded s) => s.hasReachedMax,
          'hasReachedMax after first',
          false,
        ),
        isA<TransactionLoaded>().having(
          (TransactionLoaded s) => s.transactions.length,
          'after load more',
          23,
        ).having(
          (TransactionLoaded s) => s.hasReachedMax,
          'hasReachedMax final',
          true,
        ),
      ],
    );

    blocTest<TransactionBloc, TransactionState>(
      'FilterTransactionsByDate reloads first page',
      build: () => TransactionBloc(
        _FakeTransactionRepository(firstPageSize: 2, filteredFirstPageSize: 1),
      ),
      act: (bloc) async {
        bloc.add(const LoadTransactions(accountId));
        await bloc.stream.firstWhere((s) => s is TransactionLoaded);
        final range = DateTimeRange(
          start: DateTime.utc(2024, 1, 1),
          end: DateTime.utc(2024, 12, 31),
        );
        bloc.add(FilterTransactionsByDate(accountId, range));
      },
      expect: () => [
        isA<TransactionLoading>(),
        isA<TransactionLoaded>().having(
          (TransactionLoaded s) => s.transactions.length,
          'unfiltered count',
          2,
        ),
        isA<TransactionLoading>(),
        isA<TransactionLoaded>().having(
          (TransactionLoaded s) => s.transactions.length,
          'filtered count',
          1,
        ),
      ],
    );
  });
}
