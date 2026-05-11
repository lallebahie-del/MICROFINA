import '../../data/models/extra_models.dart';

abstract class TransactionRepository {
  Future<List<EpargneTransactionModel>> getTransactions(String accountId);
  Future<bool> transferFunds({
    required String fromAccountId,
    required String toAccountId,
    required double amount,
    required String reason,
  });
}
