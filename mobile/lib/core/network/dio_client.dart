import 'package:dio/dio.dart';
import '../storage/secure_storage_service.dart';

class DioClient {
  final Dio _dio;
  final SecureStorageService _secureStorage;

  DioClient(this._dio, this._secureStorage) {
    _dio
      ..options.baseUrl = 'https://api.microfina.com/v1'
      ..options.connectTimeout = const Duration(seconds: 10)
      ..options.receiveTimeout = const Duration(seconds: 10)
      ..options.responseType = ResponseType.json
      ..interceptors.addAll([
        AuthInterceptor(_secureStorage, _dio),
        LogInterceptor(
          requestHeader: true,
          requestBody: true,
          responseHeader: true,
          responseBody: true,
        ),
      ]);
  }

  Future<Response> get(
    String url, {
    Map<String, dynamic>? queryParameters,
    Options? options,
    CancelToken? cancelToken,
    ProgressCallback? onReceiveProgress,
  }) async {
    try {
      return await _dio.get(
        url,
        queryParameters: queryParameters,
        options: options,
        cancelToken: cancelToken,
        onReceiveProgress: onReceiveProgress,
      );
    } catch (e) {
      rethrow;
    }
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
    try {
      return await _dio.post(
        url,
        data: data,
        queryParameters: queryParameters,
        options: options,
        cancelToken: cancelToken,
        onSendProgress: onSendProgress,
        onReceiveProgress: onReceiveProgress,
      );
    } catch (e) {
      rethrow;
    }
  }
}

class AuthInterceptor extends Interceptor {
  final SecureStorageService _secureStorage;
  final Dio _dio;

  AuthInterceptor(this._secureStorage, this._dio);

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
    if (err.response?.statusCode == 401) {
      // Logic for refresh token
      final refreshToken = await _secureStorage.getRefreshToken();
      if (refreshToken != null) {
        try {
          // Attempt to refresh the token
          final response = await _dio.post('/auth/refresh', data: {
            'refreshToken': refreshToken,
          });

          if (response.statusCode == 200) {
            final newToken = response.data['token'];
            final newRefreshToken = response.data['refreshToken'];

            await _secureStorage.saveToken(newToken);
            await _secureStorage.saveRefreshToken(newRefreshToken);

            // Retry the original request with the new token
            err.requestOptions.headers['Authorization'] = 'Bearer $newToken';
            final cloneReq = await _dio.fetch(err.requestOptions);
            return handler.resolve(cloneReq);
          }
        } catch (e) {
          // If refresh fails, logout
          await _secureStorage.deleteToken();
          await _secureStorage.deleteRefreshToken();
          // You might want to trigger a logout event here
        }
      } else {
        await _secureStorage.deleteToken();
      }
    }
    return handler.next(err);
  }
}
