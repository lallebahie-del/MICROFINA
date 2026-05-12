import '../../core/config/app_build_config.dart';
import '../../core/network/dio_client.dart';
import '../../core/network/connectivity_service.dart';
import '../../core/storage/local_cache_service.dart';
import '../../domain/repositories/certificat_repository.dart';
import '../datasources/mock/mock_data.dart';
import '../models/extra_models.dart';

class CertificatRepositoryImpl implements CertificatRepository {
  final DioClient _dioClient;
  final LocalCacheService _cacheService;
  final ConnectivityService _connectivityService;

  CertificatRepositoryImpl(
    this._dioClient,
    this._cacheService,
    this._connectivityService,
  );

  List<CertificatModel> _mockCertificatsForDemo() {
    return MockData.mockCertificats
        .map((row) => CertificatModel.fromJson(Map<String, dynamic>.from(row)))
        .toList();
  }

  @override
  Future<List<CertificatModel>> getCertificats() async {
    final isConnected = await _connectivityService.isConnected;

    if (!isConnected) {
      final cached = await _cacheService.getCachedCertificats();
      if (cached.isNotEmpty) {
        return cached.map((json) => CertificatModel.fromJson(json)).toList();
      }
      if (AppBuildConfig.allowMockFallback) {
        return _mockCertificatsForDemo();
      }
      return [];
    }

    try {
      final response = await _dioClient.get('/certificats');
      final List<dynamic> data = response.data;
      final certificatsJson = data
          .map((e) => e as Map<String, dynamic>)
          .toList();
      final certificats = certificatsJson
          .map((json) => CertificatModel.fromJson(json))
          .toList();

      if (certificats.isEmpty) {
        if (AppBuildConfig.allowMockFallback) {
          return _mockCertificatsForDemo();
        }
        return [];
      }
      await _cacheService.cacheCertificats(certificatsJson);
      return certificats;
    } catch (e) {
      final cached = await _cacheService.getCachedCertificats();
      if (cached.isNotEmpty)
        return cached.map((json) => CertificatModel.fromJson(json)).toList();
      if (AppBuildConfig.allowMockFallback) {
        return _mockCertificatsForDemo();
      }
      return [];
    }
  }
}
