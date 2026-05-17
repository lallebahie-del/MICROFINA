import 'package:dio/dio.dart';

import '../../core/config/api_config.dart';
import '../../core/config/app_build_config.dart';
import '../../core/network/dio_client.dart';
import '../../domain/repositories/agences_repository.dart';
import '../datasources/mock/mock_data.dart';
import '../models/agence_model.dart';

class AgencesRepositoryImpl implements AgencesRepository {
  final DioClient _dio;

  AgencesRepositoryImpl(this._dio);

  @override
  Future<List<AgenceModel>> fetchAgences() async {
    try {
      final response = await _dio.get(ApiConfig.agencesList);
      final List<dynamic> raw = (response.data as List<dynamic>?) ?? const [];
      return raw
          .whereType<Map<String, dynamic>>()
          .map(_fromBackend)
          .where((a) => a.latitude != 0 || a.longitude != 0)
          .toList();
    } on DioException catch (_) {
      if (AppBuildConfig.allowMockFallback) {
        return MockData.getAgences();
      }
      rethrow;
    }
  }

  AgenceModel _fromBackend(Map<String, dynamic> json) {
    final code = (json['codeAgence'] as String?) ?? '';
    final lat = (json['latitude'] as num?)?.toDouble() ?? 0.0;
    final lng = (json['longitude'] as num?)?.toDouble() ?? 0.0;
    return AgenceModel(
      id: code,
      code: code,
      nom: (json['nomAgence'] as String?) ?? code,
      ville: (json['nomCourt'] as String?) ?? '',
      adresse: (json['institution'] as String?) ?? '',
      latitude: lat,
      longitude: lng,
    );
  }
}
