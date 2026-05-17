import 'dart:io';
import 'package:dio/dio.dart';
import '../../core/config/api_config.dart';
import '../../core/config/app_build_config.dart';
import '../../core/network/dio_client.dart';
import '../../domain/repositories/notifications_repository.dart';
import '../datasources/mock/mock_data.dart';

class NotificationsRepositoryImpl implements NotificationsRepository {
  final DioClient _dio;

  NotificationsRepositoryImpl(this._dio);

  @override
  Future<NotificationsPage> fetch({int page = 0, int size = 20}) async {
    try {
      final fromApi = await _fetchFromNotificationsApi(page: page, size: size);
      if (fromApi.items.isNotEmpty) {
        return fromApi;
      }

      final fromTx = await _fetchFromTransactionsApi(page: page, size: size);
      if (fromTx.items.isNotEmpty) {
        return fromTx;
      }
    } catch (e) {
      if (!AppBuildConfig.allowMockFallback) {
        rethrow;
      }
    }

    if (AppBuildConfig.allowMockFallback) {
      return _fetchFromMock(page: page, size: size);
    }

    return const NotificationsPage(
      items: [],
      totalElements: 0,
      totalPages: 0,
      page: 0,
      unread: 0,
    );
  }

  Future<NotificationsPage> _fetchFromNotificationsApi({
    required int page,
    required int size,
  }) async {
    final Response<dynamic> response = await _dio.get(
      ApiConfig.mobileNotifications(),
      queryParameters: {'page': page, 'size': size},
    );
    return _parseNotificationsPage(response.data, page: page);
  }

  /// Repli : même source que l'historique Transactions (toujours peuplé après virement/paiement).
  Future<NotificationsPage> _fetchFromTransactionsApi({
    required int page,
    required int size,
  }) async {
    final comptesResp = await _dio.get(ApiConfig.mobileComptes());
    final List<dynamic> comptesRaw = (comptesResp.data as List<dynamic>?) ?? const [];

    final accountNums = <String>[];
    for (final item in comptesRaw) {
      if (item is! Map) continue;
      final map = Map<String, dynamic>.from(item);
      final num = (map['numCompte'] as String?)?.trim();
      if (num != null && num.isNotEmpty) {
        accountNums.add(num);
      }
    }

    if (accountNums.isEmpty) {
      return const NotificationsPage(
        items: [],
        totalElements: 0,
        totalPages: 0,
        page: 0,
        unread: 0,
      );
    }

    final allTx = <Map<String, dynamic>>[];
    for (final numCompte in accountNums) {
      final Response<dynamic> txResp = await _dio.get(
        ApiConfig.mobileTransactions(numCompte),
        queryParameters: {'page': 0, 'size': 50},
      );
      final body = txResp.data;
      if (body is! Map) continue;
      final map = Map<String, dynamic>.from(body);
      final List<dynamic> raw = (map['content'] as List<dynamic>?) ?? const [];
      for (final row in raw) {
        if (row is Map) {
          allTx.add(Map<String, dynamic>.from(row));
        }
      }
    }

    allTx.sort((a, b) {
      final da = (a['date'] as String?) ?? '';
      final db = (b['date'] as String?) ?? '';
      return db.compareTo(da);
    });

    final start = page * size;
    if (start >= allTx.length) {
      return NotificationsPage(
        items: const [],
        totalElements: allTx.length,
        totalPages: (allTx.length / size).ceil(),
        page: page,
        unread: allTx.length,
      );
    }
    final end = (start + size) > allTx.length ? allTx.length : start + size;
    final slice = allTx.sublist(start, end);
    final items = slice.map(_fromTransaction).toList();

    return NotificationsPage(
      items: items,
      totalElements: allTx.length,
      totalPages: (allTx.length / size).ceil(),
      page: page,
      unread: allTx.length,
    );
  }

  NotificationsPage _parseNotificationsPage(dynamic data, {required int page}) {
    if (data is! Map) {
      return const NotificationsPage(
        items: [],
        totalElements: 0,
        totalPages: 0,
        page: 0,
        unread: 0,
      );
    }
    final body = Map<String, dynamic>.from(data);
    final List<dynamic> raw = (body['content'] as List<dynamic>?) ?? const [];
    final items = <MobileNotification>[];
    for (final row in raw) {
      if (row is Map) {
        items.add(_fromBackend(Map<String, dynamic>.from(row)));
      }
    }

    return NotificationsPage(
      items: items,
      totalElements: (body['totalElements'] as num?)?.toInt() ?? items.length,
      totalPages: (body['totalPages'] as num?)?.toInt() ?? 0,
      page: (body['page'] as num?)?.toInt() ?? page,
      unread: (body['unread'] as num?)?.toInt() ?? items.where((n) => !n.lu).length,
    );
  }

  NotificationsPage _fetchFromMock({required int page, required int size}) {
    final all = MockData.getNotifications();
    final start = page * size;
    if (start >= all.length) {
      return NotificationsPage(
        items: const [],
        totalElements: all.length,
        totalPages: (all.length / size).ceil(),
        page: page,
        unread: all.where((n) => n['isRead'] != true).length,
      );
    }
    final end = (start + size) > all.length ? all.length : start + size;
    final slice = all.sublist(start, end);
    final items = slice.map(_fromMock).toList();
    final unread = all.where((n) => n['isRead'] != true).length;

    return NotificationsPage(
      items: items,
      totalElements: all.length,
      totalPages: (all.length / size).ceil(),
      page: page,
      unread: unread,
    );
  }

  @override
  Future<void> markAsRead(int id) async {
    try {
      await _dio.patch('/api/v1/mobile/me/notifications/$id/read');
    } on DioException catch (e) {
      if (AppBuildConfig.allowMockFallback) {
        MockData.markNotificationAsRead(id);
        return;
      }
      throw Exception(_messageFromDio(e, fallback: 'Échec du marquage en lu.'));
    }
  }

  @override
  Future<int> markAllAsRead() async {
    try {
      final r = await _dio.patch('/api/v1/mobile/me/notifications/read-all');
      final body = r.data;
      if (body is Map) {
        return (Map<String, dynamic>.from(body)['marquees'] as num?)?.toInt() ?? 0;
      }
      return 0;
    } on DioException catch (e) {
      if (AppBuildConfig.allowMockFallback) {
        MockData.markAllNotificationsAsRead();
        return MockData.getNotifications().length;
      }
      throw Exception(_messageFromDio(e, fallback: 'Échec du marquage groupé.'));
    }
  }

  @override
  Future<String> uploadProfilePhoto(File file) async {
    try {
      final mime = file.path.toLowerCase().endsWith('.png')
          ? 'image/png'
          : 'image/jpeg';
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
      final body = r.data;
      if (body is Map) {
        return (Map<String, dynamic>.from(body)['path'] as String?) ?? '';
      }
      return '';
    } on DioException catch (e) {
      throw Exception(_messageFromDio(e, fallback: 'Échec de l\'upload photo.'));
    }
  }

  MobileNotification _fromBackend(Map<String, dynamic> json) {
    DateTime? parse(String? s) {
      if (s == null || s.isEmpty) return null;
      try {
        return DateTime.parse(s);
      } catch (_) {
        return null;
      }
    }

    final idRaw = json['id'];
    final int id = idRaw is int
        ? idRaw
        : (idRaw is num ? idRaw.toInt() : idRaw.hashCode);

    return MobileNotification(
      id: id,
      titre: (json['titre'] as String?) ?? '',
      message: (json['message'] as String?) ?? '',
      type: (json['type'] as String?) ?? 'INFO',
      lu: json['lu'] == true,
      dateCreation: parse(json['dateCreation'] as String?),
      dateLecture: parse(json['dateLecture'] as String?),
      lien: json['lien'] as String?,
    );
  }

  MobileNotification _fromTransaction(Map<String, dynamic> json) {
    final type = ((json['type'] as String?) ?? 'DEBIT').toUpperCase();
    final isCredit = type == 'CREDIT';
    final montant = (json['montant'] as num?) ?? 0;
    final libelle = (json['libelle'] as String?) ?? '';

    DateTime? parseDate(String? s) {
      if (s == null || s.isEmpty) return null;
      try {
        return DateTime.parse(s);
      } catch (_) {
        return null;
      }
    }

    final idRaw = json['id'];
    final int id = idRaw is int
        ? idRaw
        : (idRaw is num
            ? idRaw.toInt()
            : int.tryParse('$idRaw') ?? '$idRaw'.hashCode);

    return MobileNotification(
      id: id,
      titre: isCredit ? 'Crédit sur compte' : 'Débit sur compte',
      message: '$libelle · ${montant.toStringAsFixed(0)} FCFA',
      type: 'OPERATION',
      lu: false,
      dateCreation: parseDate(json['date'] as String?),
      dateLecture: null,
      lien: 'epargne:$id',
    );
  }

  MobileNotification _fromMock(Map<String, dynamic> json) {
    final idRaw = json['id'];
    final int id = idRaw is int
        ? idRaw
        : (idRaw is String ? idRaw.hashCode : 0);
    DateTime? parseDate(dynamic v) {
      if (v is String && v.isNotEmpty) {
        try {
          return DateTime.parse(v);
        } catch (_) {
          return null;
        }
      }
      return null;
    }

    return MobileNotification(
      id: id,
      titre: (json['title'] as String?) ?? '',
      message: (json['message'] as String?) ?? '',
      type: 'OPERATION',
      lu: json['isRead'] == true,
      dateCreation: parseDate(json['date']),
      dateLecture: null,
      lien: null,
    );
  }

  String _messageFromDio(DioException e, {required String fallback}) {
    final code = e.response?.statusCode;
    final body = e.response?.data;
    if (code == 401) return 'Session expirée. Reconnecte-toi.';
    if (code == 403) return 'Accès refusé.';
    if (body is Map && body['message'] is String) return body['message'] as String;
    if (body is Map && body['error'] is String) return body['error'] as String;
    if (e.type == DioExceptionType.connectionTimeout ||
        e.type == DioExceptionType.connectionError) {
      return 'Serveur injoignable.';
    }
    return fallback;
  }
}
