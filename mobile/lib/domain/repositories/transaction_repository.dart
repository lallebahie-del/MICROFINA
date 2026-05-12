import 'package:flutter/material.dart';

import '../../data/models/extra_models.dart';

abstract class TransactionRepository {
  Future<List<EpargneTransactionModel>> getTransactions(String accountId);

  /// Pagination mock (même comportement que [MockData.getPaginatedTransactions]).
  Future<List<EpargneTransactionModel>> getPaginatedTransactions({
    required String accountId,
    required int page,
    required int pageSize,
    DateTimeRange? dateRange,
  });

  Future<bool> transferFunds({
    required String fromAccountId,
    required String toAccountId,
    required double amount,
    required String reason,
  });

  /// Virement vers un compte tiers (autre banque / RIB).
  Future<bool> transferExternalFunds({
    required String fromAccountId,
    required String beneficiaryName,
    required String externalAccountNumber,
    String? beneficiaryBank,
    required double amount,
    required String reason,
  });
}
