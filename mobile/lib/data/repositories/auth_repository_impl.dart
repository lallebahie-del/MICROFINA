import 'dart:async';
import '../../core/network/dio_client.dart';
import '../../core/storage/secure_storage_service.dart';
import '../../domain/repositories/auth_repository.dart';

class AuthRepositoryImpl implements AuthRepository {
  final DioClient _dioClient;
  final SecureStorageService _secureStorage;
  final _authStatusController = StreamController<bool>.broadcast();

  AuthRepositoryImpl(this._dioClient, this._secureStorage);

  SecureStorageService get secureStorage => _secureStorage;

  @override
  Stream<bool> get authStatus => _authStatusController.stream;

  @override
  Future<String?> login(String phone, String pin) async {
    try {
      final response = await _dioClient.post('/auth/login', data: {
        'phone': phone,
        'pin': pin,
      });

      if (response.statusCode == 200) {
        final String token = response.data['token'];
        final String refreshToken = response.data['refreshToken'];

        await _secureStorage.saveToken(token);
        await _secureStorage.saveRefreshToken(refreshToken);
        
        _authStatusController.add(true);
        return token;
      }
      return null;
    } catch (e) {
      return null;
    }
  }

  @override
  Future<void> logout() async {
    try {
      await _dioClient.post('/auth/logout');
    } finally {
      await _secureStorage.deleteToken();
      await _secureStorage.deleteRefreshToken();
      _authStatusController.add(false);
    }
  }

  @override
  Future<String?> getToken() async {
    return await _secureStorage.getToken();
  }

  void dispose() {
    _authStatusController.close();
  }
}
