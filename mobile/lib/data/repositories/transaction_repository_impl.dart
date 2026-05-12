import '../../core/config/app_build_config.dart';
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
    if (AppBuildConfig.allowMockFallback) {
      final batch = await MockData.getPaginatedTransactions(
        accountId: accountId,
        page: page,
        pageSize: pageSize,
        dateRange: dateRange,
      );
      return batch.map(EpargneTransactionModel.fromJson).toList();
    }
    try {
      final all = await getTransactions(accountId);
      DateTime parseD(EpargneTransactionModel t) {
        try {
          return DateTime.parse(t.date);
        } catch (_) {
          return DateTime.fromMillisecondsSinceEpoch(0);
        }
      }

      var filtered = all;
      if (dateRange != null) {
        filtered = all.where((t) {
          final d = parseD(t);
          return !d.isBefore(dateRange.start) && !d.isAfter(dateRange.end);
        }).toList();
      }
      filtered.sort((a, b) => parseD(b).compareTo(parseD(a)));
      final start = (page - 1) * pageSize;
      if (start >= filtered.length) return [];
      final end = (start + pageSize) > filtered.length
          ? filtered.length
          : start + pageSize;
      return filtered.sublist(start, end);
    } catch (_) {
      return [];
    }
  }

  @override
  Future<List<EpargneTransactionModel>> getTransactions(
    String accountId,
  ) async {
    try {
      final response = await _dioClient.get(
        '/accounts/$accountId/transactions',
      );
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
      final response = await _dioClient.post(
        '/transactions/transfer',
        data: {
          'fromAccountId': fromAccountId,
          'toAccountId': toAccountId,
          'amount': amount,
          'reason': reason,
        },
      );
      final code = response.statusCode;
      if (code != null && code >= 200 && code < 300) return true;
    } catch (_) {
      // API indisponible : simulation locale uniquement si le build l'autorise.
    }
    if (AppBuildConfig.allowMockFallback) {
      return MockData.performInternalTransfer(
        fromAccountId: fromAccountId,
        toAccountId: toAccountId,
        amount: amount,
        reason: reason,
      );
    }
    return false;
  }

  @override
  Future<bool> transferExternalFunds({
    required String fromAccountId,
    required String beneficiaryName,
    required String externalAccountNumber,
    String? beneficiaryBank,
    required double amount,
    required String reason,
  }) async {
    try {
      final bank = beneficiaryBank?.trim();
      final response = await _dioClient.post(
        '/transactions/transfer-external',
        data: {
          'fromAccountId': fromAccountId,
          'beneficiaryName': beneficiaryName,
          'externalAccountNumber': externalAccountNumber,
          if (bank != null && bank.isNotEmpty) 'beneficiaryBank': bank,
          'amount': amount,
          'reason': reason,
        },
      );
      final code = response.statusCode;
      if (code != null && code >= 200 && code < 300) return true;
    } catch (_) {}
    if (AppBuildConfig.allowMockFallback) {
      return MockData.performExternalTransfer(
        fromAccountId: fromAccountId,
        beneficiaryName: beneficiaryName,
        externalAccountNumber: externalAccountNumber,
        beneficiaryBank: beneficiaryBank,
        amount: amount,
        reason: reason,
      );
    }
    return false;
  }
}
