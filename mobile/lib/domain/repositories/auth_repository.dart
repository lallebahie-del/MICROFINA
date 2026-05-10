abstract class AuthRepository {
  Future<String?> login(String phone, String pin);

  /// Inscription self-service (POST /api/v1/auth/register-mobile).
  /// Retourne le JWT du nouveau compte (auto-login après inscription).
  Future<String?> register({
    required String phone,
    required String pin,
    String? nomComplet,
    String? email,
  });

  Future<void> logout();
  Future<String?> getToken();
  Stream<bool> get authStatus;
}
