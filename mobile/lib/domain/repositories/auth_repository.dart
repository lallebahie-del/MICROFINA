import '../models/login_outcome.dart';

abstract class AuthRepository {
  Future<LoginOutcome> login(String phone, String pin);
  Future<void> logout();
  Future<String?> getToken();
  Stream<bool> get authStatus;
}
