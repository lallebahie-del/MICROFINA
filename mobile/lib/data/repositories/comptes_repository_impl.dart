import 'package:dio/dio.dart';
import '../../core/network/dio_client.dart';
import '../../domain/repositories/comptes_repository.dart';
import '../models/compte_eps_model.dart';

/// Implémentation Dio du contrat [ComptesRepository].
///
/// Mappe la réponse `CompteEpsDTO.Response` du backend vers
/// [CompteEpsModel] côté mobile :
///   - `numCompte`        → `numeroCompte`
///   - `produitEpargne`   → `accountType` (ou défaut "EPARGNE")
///   - `montantOuvert + montantDepot` → `availableBalance`
///   - `montantBloque`    → `blockedBalance`
class ComptesRepositoryImpl implements ComptesRepository {
  final DioClient _dio;

  ComptesRepositoryImpl(this._dio);

  @override
  Future<List<CompteEpsModel>> getMyComptes() async {
    try {
      final Response<dynamic> response = await _dio.get('/api/v1/mobile/me/comptes');
      final List<dynamic> raw = (response.data as List<dynamic>?) ?? const [];
      return raw
          .whereType<Map<String, dynamic>>()
          .map(_fromBackend)
          .toList();
    } on DioException catch (e) {
      final code = e.response?.statusCode;
      final body = e.response?.data;
      String message = 'Impossible de charger les comptes';
      if (code == 401) {
        message = 'Session expirée. Reconnecte-toi.';
      } else if (code == 403) {
        message = 'Accès refusé.';
      } else if (body is Map && body['message'] is String) {
        message = body['message'] as String;
      } else if (e.type == DioExceptionType.connectionTimeout ||
          e.type == DioExceptionType.connectionError) {
        message = 'Serveur injoignable.';
      }
      throw Exception(message);
    }
  }

  CompteEpsModel _fromBackend(Map<String, dynamic> json) {
    final num montantOuvert  = (json['montantOuvert']  as num?) ?? 0;
    final num montantDepot   = (json['montantDepot']   as num?) ?? 0;
    final num montantBloque  = (json['montantBloque']  as num?) ?? 0;
    final String numCompte   = (json['numCompte']      as String?) ?? '';
    final String produit     = (json['produitEpargne'] as String?) ?? 'EPARGNE';
    final String typeEpargne = (json['typeEpargne']    as String?) ?? produit;
    final String agence      = (json['codeAgence']     as String?) ?? '';

    return CompteEpsModel(
      id: numCompte,
      numeroCompte: numCompte,
      libelle: typeEpargne.isNotEmpty ? typeEpargne : 'Compte $numCompte',
      availableBalance: (montantOuvert + montantDepot).toDouble(),
      blockedBalance: montantBloque.toDouble(),
      accountType: produit,
      devise: 'MRU',
      lastSyncedAt: DateTime.now(),
      isDefaultAccount: false,
      accountTypeColor: agence,
    );
  }
}
