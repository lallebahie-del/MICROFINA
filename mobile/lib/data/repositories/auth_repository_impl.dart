import 'dart:async';

import 'package:dio/dio.dart';

import '../../core/config/api_config.dart';
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
        ApiConfig.authLogin,
        data: {'username': phone, 'password': pin},
      );

      if (response.statusCode == 200 && response.data is Map) {
        final data = response.data as Map;
        final token = data['token'];
        if (token is String && token.isNotEmpty) {
          await _secureStorage.saveToken(token);
          await _secureStorage.saveLastPhone(phone);
          await _secureStorage.saveLastLoginDate(DateTime.now());
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
  Future<LoginOutcome> registerMobile({
    required String phone,
    required String pin,
    String? nomComplet,
    String? email,
    Map<String, String>? address,
  }) async {
    try {
      final response = await _dioClient.post(
        ApiConfig.authRegisterMobile,
        data: {
          'phone': phone,
          'pin': pin,
          if (nomComplet != null && nomComplet.isNotEmpty)
            'nomComplet': nomComplet,
          if (email != null && email.isNotEmpty) 'email': email,
          if (address != null) ...address,
        },
      );

      final ok = response.statusCode == 201 || response.statusCode == 200;
      if (ok && response.data is Map) {
        final data = response.data as Map;
        final token = data['token'];
        if (token is String && token.isNotEmpty) {
          await _secureStorage.saveToken(token);
          await _secureStorage.saveLastPhone(phone);
          await _secureStorage.saveLastLoginDate(DateTime.now());
          _authStatusController.add(true);
          return LoginSuccess(token);
        }
      }
      return const LoginFailure(LoginFailureKind.unknown);
    } on DioException catch (e) {
      if (e.response?.statusCode == 409) {
        return const LoginFailure(LoginFailureKind.invalidCredentials);
      }
      if (e.type == DioExceptionType.connectionTimeout ||
          e.type == DioExceptionType.connectionError) {
        return const LoginFailure(LoginFailureKind.network);
      }
      return const LoginFailure(LoginFailureKind.unknown);
    } catch (_) {
      return const LoginFailure(LoginFailureKind.unknown);
    }
  }

  @override
  Future<void> logout() async {
    await _secureStorage.deleteToken();
    await _secureStorage.deleteRefreshToken();
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
