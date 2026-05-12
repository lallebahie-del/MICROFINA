import 'package:dio/dio.dart';
import 'package:flutter/foundation.dart';
import '../auth/session_invalidation_broadcaster.dart';
import '../storage/secure_storage_service.dart';

class DioClient {
  final Dio _dio;
  final SecureStorageService _secureStorage;
  final SessionInvalidationBroadcaster _sessionInvalidation;

  DioClient(this._dio, this._secureStorage, this._sessionInvalidation) {
    const baseUrl = String.fromEnvironment(
      'API_BASE_URL',
      defaultValue: 'https://api.microfina.com/v1',
    );
    final interceptors = <Interceptor>[
      AuthInterceptor(
        _secureStorage,
        _dio,
        _sessionInvalidation,
        baseUrl: baseUrl,
      ),
    ];
    if (kDebugMode) {
      interceptors.add(
        LogInterceptor(
          requestHeader: true,
          requestBody: true,
          responseHeader: true,
          responseBody: true,
        ),
      );
    }
    _dio
      ..options.baseUrl = baseUrl
      ..options.connectTimeout = const Duration(seconds: 10)
      ..options.receiveTimeout = const Duration(seconds: 10)
      ..options.responseType = ResponseType.json
      ..interceptors.addAll(interceptors);
  }

  Future<Response> get(
    String url, {
    Map<String, dynamic>? queryParameters,
    Options? options,
    CancelToken? cancelToken,
    ProgressCallback? onReceiveProgress,
  }) async {
    return _dio.get(
      url,
      queryParameters: queryParameters,
      options: options,
      cancelToken: cancelToken,
      onReceiveProgress: onReceiveProgress,
    );
  }

  Future<Response> post(
    String url, {
    dynamic data,
    Map<String, dynamic>? queryParameters,
    Options? options,
    CancelToken? cancelToken,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    return _dio.post(
      url,
      data: data,
      queryParameters: queryParameters,
      options: options,
      cancelToken: cancelToken,
      onSendProgress: onSendProgress,
      onReceiveProgress: onReceiveProgress,
    );
  }
}

class AuthInterceptor extends Interceptor {
  AuthInterceptor(
    this._secureStorage,
    this._dio,
    this._sessionInvalidation, {
    required String baseUrl,
  }) : _baseUrl = baseUrl;

  final SecureStorageService _secureStorage;
  final Dio _dio;
  final SessionInvalidationBroadcaster _sessionInvalidation;
  final String _baseUrl;

  @override
  Future<void> onRequest(
    RequestOptions options,
    RequestInterceptorHandler handler,
  ) async {
    final token = await _secureStorage.getToken();
    if (token != null) {
      options.headers['Authorization'] = 'Bearer $token';
    }
    handler.next(options);
  }

  Dio _plainRefreshClient() {
    return Dio(
      BaseOptions(
        baseUrl: _baseUrl,
        connectTimeout: const Duration(seconds: 10),
        receiveTimeout: const Duration(seconds: 10),
        responseType: ResponseType.json,
      ),
    );
  }

  Future<void> _clearSessionAndNotify() async {
    await _secureStorage.deleteToken();
    await _secureStorage.deleteRefreshToken();
    _sessionInvalidation.broadcast();
  }

  @override
  Future<void> onError(
    DioException err,
    ErrorInterceptorHandler handler,
  ) async {
    final path = err.requestOptions.path;
    if (path.contains('/auth/refresh')) {
      await _clearSessionAndNotify();
      return handler.next(err);
    }

    if (err.response?.statusCode != 401) {
      return handler.next(err);
    }

    final refreshToken = await _secureStorage.getRefreshToken();
    if (refreshToken == null) {
      await _clearSessionAndNotify();
      return handler.next(err);
    }

    try {
      final refreshClient = _plainRefreshClient();
      final response = await refreshClient.post(
        '/auth/refresh',
        data: {'refreshToken': refreshToken},
      );

      if (response.statusCode == 200 && response.data is Map) {
        final data = response.data as Map;
        final newToken = data['token'];
        final newRefresh = data['refreshToken'];
        if (newToken is String && newRefresh is String) {
          await _secureStorage.saveToken(newToken);
          await _secureStorage.saveRefreshToken(newRefresh);
          err.requestOptions.headers['Authorization'] = 'Bearer $newToken';
          final cloneReq = await _dio.fetch(err.requestOptions);
          return handler.resolve(cloneReq);
        }
      }
    } catch (_) {
      // refresh impossible
    }

    await _clearSessionAndNotify();
    return handler.next(err);
  }
}
