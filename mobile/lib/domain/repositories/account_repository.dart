import '../../data/models/compte_eps_model.dart';

abstract class AccountRepository {
  Future<List<CompteEpsModel>> getAccounts();
  Future<CompteEpsModel> getAccountDetails(String accountId);
}
