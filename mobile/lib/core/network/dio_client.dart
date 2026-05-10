import 'dart:io' show Platform;
import 'package:dio/dio.dart';
import 'package:flutter/foundation.dart' show kDebugMode, kIsWeb;
import '../storage/secure_storage_service.dart';
import 'session_events.dart';

/// Base URL du backend Spring Boot.
///
/// - **Web** (Chrome via `flutter run -d chrome`)        : `http://localhost:8080`
/// - **Android emulator** (AVD)                          : `http://10.0.2.2:8080`
///   (10.0.2.2 = adresse spéciale = host machine depuis l'AVD)
/// - **iOS simulator** / desktop                         : `http://localhost:8080`
/// - **Device physique sur LAN** : remplacer par l'IP locale
///   du serveur Spring (ex: 192.168.1.42:8080).
String _resolveBaseUrl() {
  if (kIsWeb) return 'http://localhost:8080';
  try {
    if (Platform.isAndroid) return 'http://10.0.2.2:8080';
  } catch (_) {
    /* Platform indisponible. */
  }
  return 'http://localhost:8080';
}

/// Wrapper Dio centralisé. Expose l'instance brute via [dio] pour les
/// repositories qui veulent leurs propres options (multipart, timeouts, etc).
class DioClient {
  final Dio _dio;
  final SecureStorageService _secureStorage;

  DioClient(this._dio, this._secureStorage) {
    _dio
      ..options.baseUrl        = _resolveBaseUrl()
      ..options.connectTimeout = const Duration(seconds: 15)
      ..options.receiveTimeout = const Duration(seconds: 15)
      ..options.responseType   = ResponseType.json
      ..interceptors.addAll([
        AuthInterceptor(_secureStorage),
        if (kDebugMode)
          LogInterceptor(
            requestHeader: false,
            requestBody:   true,
            responseHeader: false,
            responseBody:  true,
            error: true,
          ),
      ]);
  }

  /// Accès à l'instance Dio configurée pour les usages avancés.
  Dio get dio => _dio;

  Future<Response> get(
    String url, {
    Map<String, dynamic>? queryParameters,
    Options? options,
    CancelToken? cancelToken,
    ProgressCallback? onReceiveProgress,
  }) =>
      _dio.get(
        url,
        queryParameters: queryParameters,
        options: options,
        cancelToken: cancelToken,
        onReceiveProgress: onReceiveProgress,
      );

  Future<Response> post(
    String url, {
    dynamic data,
    Map<String, dynamic>? queryParameters,
    Options? options,
    CancelToken? cancelToken,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) =>
      _dio.post(
        url,
        data: data,
        queryParameters: queryParameters,
        options: options,
        cancelToken: cancelToken,
        onSendProgress: onSendProgress,
        onReceiveProgress: onReceiveProgress,
      );

  Future<Response> put(String url, {dynamic data, Options? options}) =>
      _dio.put(url, data: data, options: options);

  Future<Response> delete(String url, {Options? options}) =>
      _dio.delete(url, options: options);

  Future<Response> patch(String url, {dynamic data, Options? options}) =>
      _dio.patch(url, data: data, options: options);
}

/// Attache automatiquement le JWT et déclenche un nettoyage local
/// si le serveur indique que la session est invalide (401 / 403).
///
/// Spring Security renvoie :
///   - 401 quand aucune Authentication n'est trouvée pour un endpoint requis
///   - 403 quand le JwtAuthenticationFilter n'a pas pu valider le token
///     (user supprimé, token expiré silencieusement, etc.)
/// Dans les deux cas, le token local est invalide → on le supprime pour
/// forcer un re-login propre au prochain démarrage de l'app.
class AuthInterceptor extends Interceptor {
  final SecureStorageService _secureStorage;

  AuthInterceptor(this._secureStorage);

  @override
  void onRequest(RequestOptions options, RequestInterceptorHandler handler) async {
    final token = await _secureStorage.getToken();
    if (token != null) {
      options.headers['Authorization'] = 'Bearer $token';
    }
    return handler.next(options);
  }

  @override
  void onError(DioException err, ErrorInterceptorHandler handler) async {
    final code = err.response?.statusCode;
    final hadAuthHeader =
        err.requestOptions.headers['Authorization']?.toString().startsWith('Bearer ') ?? false;

    // 401 = pas de header / token invalide.
    // 403 = header présent mais Authentication context non établi (token stale)
    //       — on ne purge le token QUE si on l'avait envoyé.
    if (code == 401 || (code == 403 && hadAuthHeader)) {
      await _secureStorage.deleteToken();
      // Notifie le AuthBloc pour qu'il bascule en Unauthenticated et que le
      // GoRouter redirige vers /login.
      SessionEvents.instance.emit(SessionEvent.expired);
    }
    return handler.next(err);
  }
}
