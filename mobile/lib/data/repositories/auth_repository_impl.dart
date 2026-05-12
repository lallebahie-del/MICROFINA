import 'dart:async';

import 'package:dio/dio.dart';

import '../../core/network/dio_client.dart';
import '../../core/storage/secure_storage_service.dart';
import '../../domain/models/login_outcome.dart';
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
  Future<LoginOutcome> login(String phone, String pin) async {
    try {
      final response = await _dioClient.post(
        '/auth/login',
        data: {'phone': phone, 'pin': pin},
      );

      if (response.statusCode == 200 && response.data is Map) {
        final data = response.data as Map;
        final token = data['token'];
        final refreshToken = data['refreshToken'];
        if (token is String && refreshToken is String) {
          await _secureStorage.saveToken(token);
          await _secureStorage.saveRefreshToken(refreshToken);
          _authStatusController.add(true);
          return LoginSuccess(token);
        }
      }
      return const LoginFailure(LoginFailureKind.invalidCredentials);
    } on DioException catch (e) {
      if (e.type == DioExceptionType.connectionTimeout ||
          e.type == DioExceptionType.sendTimeout ||
          e.type == DioExceptionType.receiveTimeout ||
          e.type == DioExceptionType.connectionError) {
        return const LoginFailure(LoginFailureKind.network);
      }
      final code = e.response?.statusCode;
      if (code == 401 || code == 403 || code == 400) {
        return const LoginFailure(LoginFailureKind.invalidCredentials);
      }
      return const LoginFailure(LoginFailureKind.unknown);
    } catch (_) {
      return const LoginFailure(LoginFailureKind.unknown);
    }
  }

  @override
  Future<void> logout() async {
    // Déconnexion locale immédiate (l’UI et GoRouter s’appuient dessus tout de suite).
    await _secureStorage.deleteToken();
    await _secureStorage.deleteRefreshToken();
    _authStatusController.add(false);
    // Ne pas bloquer l’émission d’[Unauthenticated] sur l’appel réseau.
    unawaited(_postLogoutRemoteBestEffort());
  }

  Future<void> _postLogoutRemoteBestEffort() async {
    try {
      await _dioClient.post('/auth/logout').timeout(const Duration(seconds: 4));
    } catch (_) {}
  }

  @override
  Future<String?> getToken() async {
    return await _secureStorage.getToken();
  }

  void dispose() {
    _authStatusController.close();
  }
}
