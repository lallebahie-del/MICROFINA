import 'package:dio/dio.dart';
import '../../core/network/dio_client.dart';
import '../../domain/repositories/operations_repository.dart';

class OperationsRepositoryImpl implements OperationsRepository {
  final DioClient _dio;

  OperationsRepositoryImpl(this._dio);

  @override
  Future<MobileOperationResult> transfer({
    required String fromAccountNum,
    required String toAccountNum,
    required double montant,
    String? libelle,
  }) async {
    try {
      final Response<dynamic> response = await _dio.post(
        '/api/v1/mobile/me/transfer',
        data: {
          'compteSource':       fromAccountNum,
          'compteDestinataire': toAccountNum,
          'montant':            montant,
          'libelle':            libelle,
        },
      );
      return _result(response.data, defaultMontant: montant);
    } on DioException catch (e) {
      throw Exception(_messageFromDio(e, fallback: 'Échec du virement.'));
    }
  }

  @override
  Future<MobileOperationResult> pay({
    required String fromAccountNum,
    required String serviceName,
    required double montant,
    String? reference,
  }) async {
    try {
      final Response<dynamic> response = await _dio.post(
        '/api/v1/mobile/me/pay',
        data: {
          'compte':    fromAccountNum,
          'service':   serviceName,
          'reference': reference,
          'montant':   montant,
        },
      );
      return _result(response.data, defaultMontant: montant);
    } on DioException catch (e) {
      throw Exception(_messageFromDio(e, fallback: 'Échec du paiement.'));
    }
  }

  MobileOperationResult _result(dynamic data, {required double defaultMontant}) {
    final body = data is Map<String, dynamic> ? data : <String, dynamic>{};
    final num montant = (body['montant'] as num?) ?? defaultMontant;
    final num? solde  = body['soldeApres'] as num? ?? body['soldeSourceApres'] as num?;
    return MobileOperationResult(
      statut:        (body['statut'] as String?) ?? 'OK',
      compteSource:   body['compteSource']  as String?,
      compteDest:     body['compteDest']    as String?,
      service:        body['service']       as String?,
      reference:      body['reference']     as String?,
      montant:        montant.toDouble(),
      soldeApres:     solde?.toDouble(),
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
