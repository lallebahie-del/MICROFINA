import 'package:flutter/material.dart';

import '../../core/network/dio_client.dart';
import '../../domain/repositories/transaction_repository.dart';
import '../datasources/mock/mock_data.dart';
import '../models/extra_models.dart';

class TransactionRepositoryImpl implements TransactionRepository {
  final DioClient _dioClient;

  TransactionRepositoryImpl(this._dioClient);

  @override
  Future<List<EpargneTransactionModel>> getPaginatedTransactions({
    required String accountId,
    required int page,
    required int pageSize,
    DateTimeRange? dateRange,
  }) async {
    final batch = await MockData.getPaginatedTransactions(
      accountId: accountId,
      page: page,
      pageSize: pageSize,
      dateRange: dateRange,
    );
    return batch.map(EpargneTransactionModel.fromJson).toList();
  }

  @override
  Future<List<EpargneTransactionModel>> getTransactions(String accountId) async {
    try {
      final response = await _dioClient.get('/accounts/$accountId/transactions');
      return (response.data as List)
          .map((json) => EpargneTransactionModel.fromJson(json))
          .toList();
    } catch (e) {
      rethrow;
    }
  }

  @override
  Future<bool> transferFunds({
    required String fromAccountId,
    required String toAccountId,
    required double amount,
    required String reason,
  }) async {
    try {
      final response = await _dioClient.post('/transactions/transfer', data: {
        'fromAccountId': fromAccountId,
        'toAccountId': toAccountId,
        'amount': amount,
        'reason': reason,
      });
      final code = response.statusCode;
      if (code != null && code >= 200 && code < 300) return true;
    } catch (_) {
      // API indisponible : simulation locale (mêmes règles que MockData).
    }
    return MockData.performInternalTransfer(
      fromAccountId: fromAccountId,
      toAccountId: toAccountId,
      amount: amount,
      reason: reason,
    );
  }
}
