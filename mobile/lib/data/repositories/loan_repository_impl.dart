import '../../core/config/api_config.dart';
import '../../core/config/app_build_config.dart';
import '../../core/network/dio_client.dart';
import '../../core/network/connectivity_service.dart';
import '../../core/storage/local_cache_service.dart';
import '../../domain/repositories/loan_repository.dart';
import '../datasources/mock/mock_data.dart';
import '../mappers/mobile_api_mapper.dart';
import '../models/amortp_model.dart';
import '../models/credit_model.dart';
import '../models/extra_models.dart';

class LoanRepositoryImpl implements LoanRepository {
  final DioClient _dioClient;
  final LocalCacheService _cacheService;
  final ConnectivityService _connectivityService;

  LoanRepositoryImpl(
    this._dioClient,
    this._cacheService,
    this._connectivityService,
  );

  List<LoanModel> _mockActiveLoans() {
    return MockData.activeMockCredits
        .map((json) => LoanModel.fromJson(Map<String, dynamic>.from(json)))
        .toList();
  }

  LoanModel? _mockLoanById(String loanId) {
    for (final m in MockData.mockCredits) {
      if (m['loanId'] == loanId) {
        return LoanModel.fromJson(Map<String, dynamic>.from(m));
      }
    }
    return null;
  }

  List<AmortpModel> _mockSchedule(String loanId) {
    return MockData.mockAmortpList
        .where((row) => row['loanId'] == loanId)
        .map((row) => AmortpModel.fromJson(Map<String, dynamic>.from(row)))
        .toList();
  }

  List<GarantieModel> _mockGuarantees(String loanId) {
    return MockData.mockGaranties
        .where((row) => row['loanId'] == loanId)
        .map((row) => GarantieModel.fromJson(Map<String, dynamic>.from(row)))
        .toList();
  }

  @override
  Future<List<LoanModel>> getLoans() async {
    final isConnected = await _connectivityService.isConnected;

    if (!isConnected) {
      final cached = await _cacheService.getCachedLoans();
      if (cached.isNotEmpty) {
        return cached
            .map((json) => LoanModel.fromJson(json))
            .where((l) => l.statusCode != 3)
            .toList();
      }
      if (AppBuildConfig.allowMockFallback) {
        return _mockActiveLoans();
      }
      return [];
    }

    try {
      final response = await _dioClient.get(ApiConfig.mobileCredits());
      final List<dynamic> raw = (response.data as List<dynamic>?) ?? const [];
      final loans = raw
          .whereType<Map<String, dynamic>>()
          .map(MobileApiMapper.loanFromCreditDto)
          .where((l) => l.statusCode != 3)
          .toList();

      if (loans.isEmpty) {
        if (AppBuildConfig.allowMockFallback) {
          return _mockActiveLoans();
        }
        return [];
      }

      await _cacheService.cacheLoans(
        loans.map((l) => l.toJson()).toList(),
      );
      return loans;
    } catch (e) {
      final cached = await _cacheService.getCachedLoans();
      if (cached.isNotEmpty) {
        return cached
            .map((json) => LoanModel.fromJson(json))
            .where((l) => l.statusCode != 3)
            .toList();
      }
      if (AppBuildConfig.allowMockFallback) {
        return _mockActiveLoans();
      }
      return [];
    }
  }

  @override
  Future<LoanModel> getLoanDetails(String loanId) async {
    final loans = await getLoans();
    final match = loans.where((l) => l.loanId == loanId);
    if (match.isNotEmpty) return match.first;

    if (AppBuildConfig.allowMockFallback) {
      final fallback = _mockLoanById(loanId);
      if (fallback != null) return fallback;
    }
    throw Exception('Crédit introuvable: $loanId');
  }

  @override
  Future<List<AmortpModel>> getAmortizationSchedule(String loanId) async {
    if (AppBuildConfig.allowMockFallback) {
      return _mockSchedule(loanId);
    }
    return [];
  }

  @override
  Future<List<GarantieModel>> getGuarantees(String loanId) async {
    if (AppBuildConfig.allowMockFallback) {
      return _mockGuarantees(loanId);
    }
    return [];
  }

  @override
  Future<bool> requestLoan({
    required double amount,
    required int duration,
    required String purpose,
  }) async {
    if (AppBuildConfig.allowMockFallback) {
      await Future.delayed(const Duration(milliseconds: 600));
      return true;
    }
    return false;
  }

  @override
  Future<bool> payInstallment({
    required String loanId,
    required int installmentId,
    required double amount,
  }) async {
    if (AppBuildConfig.allowMockFallback) {
      return MockData.payLoanInstallmentFull(
        loanId: loanId,
        installmentId: installmentId,
      );
    }
    return false;
  }
}
