import 'package:dio/dio.dart';
import '../../core/network/dio_client.dart';
import '../../domain/repositories/transactions_repository.dart';

class TransactionsRepositoryImpl implements TransactionsRepository {
  final DioClient _dio;

  TransactionsRepositoryImpl(this._dio);

  @override
  Future<TransactionPage> fetch({
    required String numCompte,
    int page = 0,
    int size = 20,
  }) async {
    try {
      final Response<dynamic> response = await _dio.get(
        '/api/v1/mobile/me/transactions/$numCompte',
        queryParameters: {'page': page, 'size': size},
      );
      final body = response.data as Map<String, dynamic>?;
      if (body == null) {
        return const TransactionPage(items: [], page: 0, totalPages: 0, totalElements: 0);
      }

      final List<dynamic> raw = (body['content'] as List<dynamic>?) ?? const [];
      final items = raw
          .whereType<Map<String, dynamic>>()
          .map(_fromBackend)
          .toList();

      return TransactionPage(
        items: items,
        page:           (body['page']          as num?)?.toInt() ?? page,
        totalPages:     (body['totalPages']    as num?)?.toInt() ?? 0,
        totalElements:  (body['totalElements'] as num?)?.toInt() ?? items.length,
      );
    } on DioException catch (e) {
      throw Exception(_messageFromDio(e, fallback: 'Impossible de charger les transactions.'));
    }
  }

  MobileTransaction _fromBackend(Map<String, dynamic> json) {
    final num montant = (json['montant'] as num?) ?? 0;
    final num? solde  = json['soldeApres'] as num?;
    DateTime? date;
    final rawDate = json['date'] as String?;
    if (rawDate != null && rawDate.isNotEmpty) {
      try { date = DateTime.parse(rawDate); } catch (_) { date = null; }
    }
    return MobileTransaction(
      id:        (json['id']?.toString() ?? ''),
      accountId: (json['accountId'] as String?) ?? '',
      date:      date,
      montant:   montant.toDouble(),
      type:      (json['type'] as String?) ?? 'DEBIT',
      libelle:   (json['libelle'] as String?) ?? '',
      numPiece:  json['numPiece'] as String?,
      soldeApres: solde?.toDouble(),
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
