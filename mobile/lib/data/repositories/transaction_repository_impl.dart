import 'package:flutter/material.dart';

import '../../core/config/api_config.dart';
import '../../core/config/app_build_config.dart';
import '../../core/utils/phone_number_policy.dart';
import '../../core/network/dio_client.dart';
import '../../domain/repositories/transaction_repository.dart';
import '../datasources/mock/mock_data.dart';
import '../mappers/mobile_api_mapper.dart';
import '../models/extra_models.dart';

class TransactionRepositoryImpl implements TransactionRepository {
  final DioClient _dioClient;

  TransactionRepositoryImpl(this._dioClient);

  Future<List<EpargneTransactionModel>> _fetchPageFromApi({
    required String numCompte,
    required int page,
    required int pageSize,
  }) async {
    final response = await _dioClient.get(
      ApiConfig.mobileTransactions(numCompte),
      queryParameters: {'page': page, 'size': pageSize},
    );
    final body = response.data as Map<String, dynamic>?;
    final List<dynamic> raw = (body?['content'] as List<dynamic>?) ?? const [];
    return raw
        .whereType<Map<String, dynamic>>()
        .map(MobileApiMapper.transactionFromBackend)
        .toList();
  }

  Future<List<String>> _accountNumbers() async {
    final response = await _dioClient.get(ApiConfig.mobileComptes());
    final List<dynamic> raw = (response.data as List<dynamic>?) ?? const [];
    return raw
        .whereType<Map<String, dynamic>>()
        .map((j) => (j['numCompte'] as String?) ?? '')
        .where((n) => n.isNotEmpty)
        .toList();
  }

  Future<List<EpargneTransactionModel>> _fetchAllAccountsMerged({
    required int page,
    required int pageSize,
    DateTimeRange? dateRange,
  }) async {
    final nums = await _accountNumbers();
    final merged = <EpargneTransactionModel>[];
    for (final numCompte in nums) {
      final batch = await _fetchPageFromApi(
        numCompte: numCompte,
        page: 0,
        pageSize: 100,
      );
      merged.addAll(batch);
    }

    DateTime parseD(EpargneTransactionModel t) {
      try {
        return DateTime.parse(t.date);
      } catch (_) {
        return DateTime.fromMillisecondsSinceEpoch(0);
      }
    }

    var filtered = merged;
    if (dateRange != null) {
      filtered = merged.where((t) {
        final d = parseD(t);
        return !d.isBefore(dateRange.start) && !d.isAfter(dateRange.end);
      }).toList();
    }
    filtered.sort((a, b) => parseD(b).compareTo(parseD(a)));

    final start = page * pageSize;
    if (start >= filtered.length) return [];
    final end = (start + pageSize) > filtered.length
        ? filtered.length
        : start + pageSize;
    return filtered.sublist(start, end);
  }

  @override
  Future<List<EpargneTransactionModel>> getPaginatedTransactions({
    required String accountId,
    required int page,
    required int pageSize,
    DateTimeRange? dateRange,
  }) async {
    if (AppBuildConfig.allowMockFallback) {
      try {
        final batch = await _fetchFromApiOrEmpty(
          accountId: accountId,
          page: page,
          pageSize: pageSize,
          dateRange: dateRange,
        );
        if (batch.isNotEmpty) return batch;
      } catch (_) {}
      final mock = await MockData.getPaginatedTransactions(
        accountId: accountId,
        page: page,
        pageSize: pageSize,
        dateRange: dateRange,
      );
      return mock.map(EpargneTransactionModel.fromJson).toList();
    }

    return _fetchFromApiOrEmpty(
      accountId: accountId,
      page: page,
      pageSize: pageSize,
      dateRange: dateRange,
    );
  }

  Future<List<EpargneTransactionModel>> _fetchFromApiOrEmpty({
    required String accountId,
    required int page,
    required int pageSize,
    DateTimeRange? dateRange,
  }) async {
    try {
      if (accountId == ApiConfig.allAccountsTransactionScope) {
        return await _fetchAllAccountsMerged(
          page: page,
          pageSize: pageSize,
          dateRange: dateRange,
        );
      }

      final batch = await _fetchPageFromApi(
        numCompte: accountId,
        page: page,
        pageSize: pageSize,
      );

      if (dateRange == null) return batch;

      DateTime parseD(EpargneTransactionModel t) {
        try {
          return DateTime.parse(t.date);
        } catch (_) {
          return DateTime.fromMillisecondsSinceEpoch(0);
        }
      }

      return batch.where((t) {
        final d = parseD(t);
        return !d.isBefore(dateRange.start) && !d.isAfter(dateRange.end);
      }).toList();
    } catch (_) {
      return [];
    }
  }

  @override
  Future<List<EpargneTransactionModel>> getTransactions(
    String accountId,
  ) async {
    return getPaginatedTransactions(
      accountId: accountId,
      page: 0,
      pageSize: 100,
    );
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
        ApiConfig.mobileTransfer(),
        data: {
          'compteSource': fromAccountId,
          'compteDestinataire': toAccountId,
          'montant': amount,
          'libelle': reason,
        },
      );
      final code = response.statusCode;
      if (code != null && code >= 200 && code < 300) {
        final body = response.data;
        if (body is Map) {
          final statut = body['statut'];
          if (statut is String && statut.toUpperCase() != 'OK') {
            return false;
          }
        }
        return true;
      }
    } catch (_) {
      if (AppBuildConfig.allowMockFallback) {
        return MockData.performInternalTransfer(
          fromAccountId: fromAccountId,
          toAccountId: toAccountId,
          amount: amount,
          reason: reason,
        );
      }
    }
    return false;
  }

  @override
  Future<bool> transferExternalFunds({
    required String fromAccountId,
    required String beneficiaryPhone,
    required double amount,
    required String reason,
  }) async {
    final phone = PhoneNumberPolicy.normalize(beneficiaryPhone);
    try {
      final response = await _dioClient.post(
        ApiConfig.mobileTransferExternal(),
        data: {
          'compteSource': fromAccountId,
          'telephoneBeneficiaire': phone,
          'montant': amount,
          'libelle': reason,
        },
      );
      final code = response.statusCode;
      if (code != null && code >= 200 && code < 300) {
        final body = response.data;
        if (body is Map) {
          final statut = body['statut'];
          if (statut is String && statut.toUpperCase() != 'OK') {
            return false;
          }
        }
        return true;
      }
    } catch (_) {
      if (AppBuildConfig.allowMockFallback) {
        return MockData.performExternalTransfer(
          fromAccountId: fromAccountId,
          beneficiaryPhone: phone,
          amount: amount,
          reason: reason,
        );
      }
    }
    return false;
  }
}
