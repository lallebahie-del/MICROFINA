abstract class AuthRepository {
  Future<String?> login(String phone, String pin);
  Future<void> logout();
  Future<String?> getToken();
  Stream<bool> get authStatus;
}
