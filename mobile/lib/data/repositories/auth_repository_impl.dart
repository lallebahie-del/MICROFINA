import 'dart:async';
import 'package:dio/dio.dart';
import '../../core/network/dio_client.dart';
import '../../core/storage/secure_storage_service.dart';
import '../../domain/repositories/auth_repository.dart';

/// Implémentation Dio-backée du contrat [AuthRepository].
///
/// **Mapping des champs UI → backend** :
///   - le champ "phone" du `LoginScreen` est envoyé en tant que `username`
///     vers `POST /api/auth/login` (le backend ignore le format).
///   - le champ "PIN" est envoyé en tant que `password`.
///
/// Cela permet au login mobile actuel (téléphone + 4 chiffres) de se brancher
/// au backend Spring sans modifier l'UX. Si tu seedes des utilisateurs avec
/// `login = numéro de téléphone` et `password = code à 4 chiffres`, le flow
/// fonctionne tel quel. Sinon, l'utilisateur peut saisir son login + mot de
/// passe Spring dans les deux champs.
class AuthRepositoryImpl implements AuthRepository {
  final DioClient _dio;
  final SecureStorageService _secureStorage;
  final _authStatusController = StreamController<bool>.broadcast();

  AuthRepositoryImpl(this._dio, this._secureStorage);

  SecureStorageService get secureStorage => _secureStorage;

  @override
  Stream<bool> get authStatus => _authStatusController.stream;

  @override
  Future<String?> login(String phone, String pin) async {
    try {
      final Response<dynamic> response = await _dio.post(
        '/api/auth/login',
        data: {
          'username': phone,
          'password': pin,
        },
      );

      final data = response.data as Map<String, dynamic>?;
      final token = data?['token'] as String?;
      if (token == null || token.isEmpty) {
        throw Exception('Réponse backend invalide : token manquant.');
      }

      await _secureStorage.saveToken(token);
      _authStatusController.add(true);
      return token;
    } on DioException catch (e) {
      final code = e.response?.statusCode;
      final body = e.response?.data;
      String message = 'Erreur de connexion';
      if (code == 401) {
        message = 'Identifiants incorrects';
      } else if (body is Map && body['message'] is String) {
        message = body['message'] as String;
      } else if (body is Map && body['error'] is String) {
        message = body['error'] as String;
      } else if (e.type == DioExceptionType.connectionTimeout ||
          e.type == DioExceptionType.connectionError) {
        message = 'Serveur injoignable. Vérifie ta connexion.';
      }
      throw Exception(message);
    }
  }

  @override
  Future<String?> register({
    required String phone,
    required String pin,
    String? nomComplet,
    String? email,
  }) async {
    try {
      final Response<dynamic> response = await _dio.post(
        '/api/v1/auth/register-mobile',
        data: {
          'phone':      phone,
          'pin':        pin,
          'nomComplet': nomComplet,
          'email':      email,
        },
      );
      final data = response.data as Map<String, dynamic>?;
      final token = data?['token'] as String?;
      if (token == null || token.isEmpty) {
        throw Exception('Réponse backend invalide : token manquant.');
      }
      await _secureStorage.saveToken(token);
      _authStatusController.add(true);
      return token;
    } on DioException catch (e) {
      final code = e.response?.statusCode;
      final body = e.response?.data;
      String message = 'Échec de l\'inscription';
      if (code == 409) {
        message = 'Un compte existe déjà pour ce numéro.';
      } else if (body is Map && body['error']   is String) {
        message = body['error']   as String;
      } else if (body is Map && body['message'] is String) {
        message = body['message'] as String;
      } else if (e.type == DioExceptionType.connectionTimeout ||
          e.type == DioExceptionType.connectionError) {
        message = 'Serveur injoignable. Vérifie ta connexion.';
      }
      throw Exception(message);
    }
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
