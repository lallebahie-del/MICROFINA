import '../../core/network/dio_client.dart';
import '../../domain/repositories/transaction_repository.dart';
import '../models/extra_models.dart';

class TransactionRepositoryImpl implements TransactionRepository {
  final DioClient _dioClient;

  TransactionRepositoryImpl(this._dioClient);

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
      return response.statusCode == 200;
    } catch (e) {
      return false;
    }
  }
}
