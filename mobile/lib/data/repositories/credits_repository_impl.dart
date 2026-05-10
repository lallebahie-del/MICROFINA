import 'package:dio/dio.dart';
import '../../core/network/dio_client.dart';
import '../../domain/repositories/credits_repository.dart';

class CreditsRepositoryImpl implements CreditsRepository {
  final DioClient _dio;

  CreditsRepositoryImpl(this._dio);

  @override
  Future<List<MobileCredit>> getMyCredits() async {
    try {
      final Response<dynamic> response = await _dio.get('/api/v1/mobile/me/credits');
      final List<dynamic> raw = (response.data as List<dynamic>?) ?? const [];
      return raw
          .whereType<Map<String, dynamic>>()
          .map(_fromBackend)
          .toList();
    } on DioException catch (e) {
      throw Exception(_messageFromDio(e, fallback: 'Impossible de charger les crédits.'));
    }
  }

  MobileCredit _fromBackend(Map<String, dynamic> json) {
    double asDouble(dynamic v) =>
        (v is num) ? v.toDouble() : (v == null ? 0.0 : double.tryParse(v.toString()) ?? 0.0);

    return MobileCredit(
      idCredit:        (json['idCredit'] as num?)?.toInt() ?? 0,
      numCredit:       (json['numCredit'] as String?) ?? '',
      statut:          (json['statut']    as String?) ?? '',
      montantDemande:  asDouble(json['montantDemande']),
      montantAccorde:  asDouble(json['montantAccorde']),
      soldeCapital:    asDouble(json['soldeCapital']),
      soldeInteret:    asDouble(json['soldeInteret']),
      tauxInteret:     asDouble(json['tauxInteret']),
      duree:           (json['duree']          as num?)?.toInt() ?? 0,
      nombreEcheance:  (json['nombreEcheance'] as num?)?.toInt() ?? 0,
      periodicite:     json['periodicite']  as String?,
      dateDemande:     json['dateDemande']  as String?,
      dateAccord:      json['dateAccord']   as String?,
      dateDeblocage:   json['dateDeblocage']as String?,
      dateEcheance:    json['dateEcheance'] as String?,
      objetCredit:     json['objetCredit']  as String?,
      membreNum:       json['membreNum']    as String?,
      membreNom:       json['membreNom']    as String?,
      membrePrenom:    json['membrePrenom'] as String?,
      produitNom:      json['produitNom']   as String?,
      agenceCode:      json['agenceCode']   as String?,
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
