import '../../core/config/api_config.dart';
import '../../core/config/app_build_config.dart';
import '../../core/network/dio_client.dart';
import '../../core/network/connectivity_service.dart';
import '../../core/storage/local_cache_service.dart';
import '../../domain/repositories/account_repository.dart';
import '../datasources/mock/mock_data.dart';
import '../mappers/mobile_api_mapper.dart';
import '../models/compte_eps_model.dart';

class AccountRepositoryImpl implements AccountRepository {
  final DioClient _dioClient;
  final LocalCacheService _cacheService;
  final ConnectivityService _connectivityService;

  AccountRepositoryImpl(
    this._dioClient,
    this._cacheService,
    this._connectivityService,
  );

  List<CompteEpsModel> _mockAccountsForCurrentUser() {
    return MockData.getAccountsForPhone(
      MockData.currentUserPhone,
    ).map(CompteEpsModel.fromJson).toList();
  }

  @override
  Future<List<CompteEpsModel>> getAccounts() async {
    final isConnected = await _connectivityService.isConnected;

    if (!isConnected) {
      final cached = await _cacheService.getCachedAccounts();
      if (cached.isNotEmpty) {
        return cached.map((json) => CompteEpsModel.fromJson(json)).toList();
      }
      if (AppBuildConfig.allowMockFallback) {
        return _mockAccountsForCurrentUser();
      }
      return [];
    }

    try {
      final response = await _dioClient.get(ApiConfig.mobileComptes());
      final List<dynamic> raw = (response.data as List<dynamic>?) ?? const [];
      final accounts = raw
          .whereType<Map<String, dynamic>>()
          .map(MobileApiMapper.compteFromBackend)
          .toList();

      if (accounts.isEmpty) {
        if (AppBuildConfig.allowMockFallback) {
          return _mockAccountsForCurrentUser();
        }
        return [];
      }

      await _cacheService.cacheAccounts(
        accounts.map((c) => c.toJson()).toList(),
      );
      return accounts;
    } catch (e) {
      final cached = await _cacheService.getCachedAccounts();
      if (cached.isNotEmpty) {
        return cached.map((json) => CompteEpsModel.fromJson(json)).toList();
      }
      if (AppBuildConfig.allowMockFallback) {
        return _mockAccountsForCurrentUser();
      }
      return [];
    }
  }

  @override
  Future<CompteEpsModel> getAccountDetails(String accountId) async {
    final accounts = await getAccounts();
    return accounts.firstWhere(
      (a) => a.id == accountId || a.numeroCompte == accountId,
      orElse: () => throw Exception('Compte introuvable: $accountId'),
    );
  }
}
