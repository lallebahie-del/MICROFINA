import '../models/login_outcome.dart';

abstract class AuthRepository {
  Future<LoginOutcome> login(String phone, String pin);

  Future<LoginOutcome> registerMobile({
    required String phone,
    required String pin,
    String? nomComplet,
    String? email,
    Map<String, String>? address,
  });

  Future<void> logout();
  Future<String?> getToken();
  Stream<bool> get authStatus;
}
