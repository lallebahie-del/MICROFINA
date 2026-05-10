import 'dart:io';
import 'package:dio/dio.dart';
import '../../core/network/dio_client.dart';
import '../../domain/repositories/notifications_repository.dart';

class NotificationsRepositoryImpl implements NotificationsRepository {
  final DioClient _dio;

  NotificationsRepositoryImpl(this._dio);

  @override
  Future<NotificationsPage> fetch({int page = 0, int size = 20}) async {
    try {
      final Response<dynamic> response = await _dio.get(
        '/api/v1/mobile/me/notifications',
        queryParameters: {'page': page, 'size': size},
      );
      final body = response.data as Map<String, dynamic>?;
      if (body == null) {
        return const NotificationsPage(items: [], totalElements: 0, totalPages: 0, page: 0, unread: 0);
      }

      final List<dynamic> raw = (body['content'] as List<dynamic>?) ?? const [];
      final items = raw
          .whereType<Map<String, dynamic>>()
          .map(_fromBackend)
          .toList();

      return NotificationsPage(
        items: items,
        totalElements: (body['totalElements'] as num?)?.toInt() ?? items.length,
        totalPages:    (body['totalPages']    as num?)?.toInt() ?? 0,
        page:          (body['page']          as num?)?.toInt() ?? page,
        unread:        (body['unread']        as num?)?.toInt() ?? 0,
      );
    } on DioException catch (e) {
      throw Exception(_messageFromDio(e, fallback: 'Impossible de charger les notifications.'));
    }
  }

  @override
  Future<void> markAsRead(int id) async {
    try {
      await _dio.patch('/api/v1/mobile/me/notifications/$id/read');
    } on DioException catch (e) {
      throw Exception(_messageFromDio(e, fallback: 'Échec du marquage en lu.'));
    }
  }

  @override
  Future<int> markAllAsRead() async {
    try {
      final r = await _dio.patch('/api/v1/mobile/me/notifications/read-all');
      final body = r.data as Map<String, dynamic>?;
      return (body?['marquees'] as num?)?.toInt() ?? 0;
    } on DioException catch (e) {
      throw Exception(_messageFromDio(e, fallback: 'Échec du marquage groupé.'));
    }
  }

  @override
  Future<String> uploadProfilePhoto(File file) async {
    try {
      final mime = file.path.toLowerCase().endsWith('.png')
          ? 'image/png' : 'image/jpeg';
      final form = FormData.fromMap({
        'file': await MultipartFile.fromFile(
          file.path,
          contentType: DioMediaType.parse(mime),
        ),
      });
      final r = await _dio.post(
        '/api/v1/mobile/me/photo',
        data: form,
        options: Options(contentType: 'multipart/form-data'),
      );
      final body = r.data as Map<String, dynamic>?;
      return (body?['path'] as String?) ?? '';
    } on DioException catch (e) {
      throw Exception(_messageFromDio(e, fallback: 'Échec de l\'upload photo.'));
    }
  }

  MobileNotification _fromBackend(Map<String, dynamic> json) {
    DateTime? parse(String? s) {
      if (s == null || s.isEmpty) return null;
      try { return DateTime.parse(s); } catch (_) { return null; }
    }
    return MobileNotification(
      id:           (json['id'] as num).toInt(),
      titre:        (json['titre']        as String?) ?? '',
      message:      (json['message']      as String?) ?? '',
      type:         (json['type']         as String?) ?? 'INFO',
      lu:            json['lu'] == true,
      dateCreation: parse(json['dateCreation'] as String?),
      dateLecture:  parse(json['dateLecture']  as String?),
      lien:          json['lien'] as String?,
    );
  }

  String _messageFromDio(DioException e, {required String fallback}) {
    final code = e.response?.statusCode;
    final body = e.response?.data;
    if (code == 401) return 'Session expirée. Reconnecte-toi.';
    if (code == 403) return 'Accès refusé.';
    if (body is Map && body['message'] is String) return body['message'] as String;
    if (body is Map && body['error']   is String) return body['error']   as String;
    if (e.type == DioExceptionType.connectionTimeout ||
        e.type == DioExceptionType.connectionError) {
      return 'Serveur injoignable.';
    }
    return fallback;
  }
}
