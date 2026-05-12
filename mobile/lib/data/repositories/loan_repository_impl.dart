import '../../core/config/app_build_config.dart';
import '../../core/network/dio_client.dart';
import '../../core/network/connectivity_service.dart';
import '../../core/storage/local_cache_service.dart';
import '../../domain/repositories/loan_repository.dart';
import '../datasources/mock/mock_data.dart';
import '../models/credit_model.dart';
import '../models/amortp_model.dart';
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
      final response = await _dioClient.get('/loans');
      final List<dynamic> data = response.data;
      final loansJson = data.map((e) => e as Map<String, dynamic>).toList();
      final loans = loansJson
          .map((json) => LoanModel.fromJson(json))
          .where((l) => l.statusCode != 3)
          .toList();

      if (loans.isEmpty) {
        if (AppBuildConfig.allowMockFallback) {
          return _mockActiveLoans();
        }
        return [];
      }
      await _cacheService.cacheLoans(loansJson);
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
    try {
      final response = await _dioClient.get('/loans/$loanId');
      return LoanModel.fromJson(response.data);
    } catch (e) {
      if (AppBuildConfig.allowMockFallback) {
        final fallback = _mockLoanById(loanId);
        if (fallback != null) return fallback;
      }
      rethrow;
    }
  }

  @override
  Future<List<AmortpModel>> getAmortizationSchedule(String loanId) async {
    try {
      final response = await _dioClient.get('/loans/$loanId/schedule');
      return (response.data as List)
          .map((json) => AmortpModel.fromJson(json as Map<String, dynamic>))
          .toList();
    } catch (e) {
      if (AppBuildConfig.allowMockFallback) {
        return _mockSchedule(loanId);
      }
      rethrow;
    }
  }

  @override
  Future<List<GarantieModel>> getGuarantees(String loanId) async {
    try {
      final response = await _dioClient.get('/loans/$loanId/guarantees');
      return (response.data as List)
          .map((json) => GarantieModel.fromJson(json as Map<String, dynamic>))
          .toList();
    } catch (e) {
      if (AppBuildConfig.allowMockFallback) {
        return _mockGuarantees(loanId);
      }
      rethrow;
    }
  }

  @override
  Future<bool> requestLoan({
    required double amount,
    required int duration,
    required String purpose,
  }) async {
    try {
      final response = await _dioClient.post(
        '/loans/request',
        data: {'amount': amount, 'duration': duration, 'purpose': purpose},
      );
      return response.statusCode == 201 || response.statusCode == 200;
    } catch (e) {
      if (AppBuildConfig.allowMockFallback) {
        await Future.delayed(const Duration(milliseconds: 600));
        return true;
      }
      return false;
    }
  }

  @override
  Future<bool> payInstallment({
    required String loanId,
    required int installmentId,
    required double amount,
  }) async {
    try {
      final response = await _dioClient.post(
        '/loans/$loanId/pay',
        data: {'installmentId': installmentId, 'amount': amount},
      );
      return response.statusCode == 200;
    } catch (e) {
      if (AppBuildConfig.allowMockFallback) {
        return MockData.payLoanInstallmentFull(
          loanId: loanId,
          installmentId: installmentId,
        );
      }
      return false;
    }
  }
}
