import 'dart:async';
import '../../core/storage/secure_storage_service.dart';
import '../../domain/repositories/auth_repository.dart';

class AuthRepositoryImpl implements AuthRepository {
  final SecureStorageService _secureStorage;
  final _authStatusController = StreamController<bool>.broadcast();

  AuthRepositoryImpl(this._secureStorage);

  SecureStorageService get secureStorage => _secureStorage;

  @override
  Stream<bool> get authStatus => _authStatusController.stream;

  @override
  Future<String?> login(String phone, String pin) async {
    // Simulation d'un délai de 1.5 seconde
    await Future.delayed(const Duration(milliseconds: 1500));

    // Simulation d'un succès (pour le mock, on accepte tout)
    const String mockToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.mock_payload.mock_signature";
    
    await _secureStorage.saveToken(mockToken);
    _authStatusController.add(true);
    
    return mockToken;
  }

  @override
  Future<void> logout() async {
    await _secureStorage.deleteToken();
    _authStatusController.add(false);
  }

  @override
  Future<String?> getToken() async {
    return await _secureStorage.getToken();
  }

  void dispose() {
    _authStatusController.close();
  }
}
