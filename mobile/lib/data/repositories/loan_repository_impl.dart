import '../../core/network/dio_client.dart';
import '../../core/network/connectivity_service.dart';
import '../../core/storage/local_cache_service.dart';
import '../../domain/repositories/loan_repository.dart';
import '../models/credit_model.dart';
import '../models/amortp_model.dart';
import '../models/extra_models.dart';

class LoanRepositoryImpl implements LoanRepository {
  final DioClient _dioClient;
  final LocalCacheService _cacheService;
  final ConnectivityService _connectivityService;

  LoanRepositoryImpl(this._dioClient, this._cacheService, this._connectivityService);

  @override
  Future<List<LoanModel>> getLoans() async {
    final isConnected = await _connectivityService.isConnected;
    
    if (!isConnected) {
      final cached = await _cacheService.getCachedLoans();
      return cached.map((json) => LoanModel.fromJson(json)).toList();
    }

    try {
      final response = await _dioClient.get('/loans');
      final List<dynamic> data = response.data;
      final loansJson = data.map((e) => e as Map<String, dynamic>).toList();
      final loans = loansJson.map((json) => LoanModel.fromJson(json)).toList();
      
      await _cacheService.cacheLoans(loansJson);
      return loans;
    } catch (e) {
      final cached = await _cacheService.getCachedLoans();
      if (cached.isNotEmpty) return cached.map((json) => LoanModel.fromJson(json)).toList();
      rethrow;
    }
  }

  @override
  Future<LoanModel> getLoanDetails(String loanId) async {
    try {
      final response = await _dioClient.get('/loans/$loanId');
      return LoanModel.fromJson(response.data);
    } catch (e) {
      rethrow;
    }
  }

  @override
  Future<List<AmortpModel>> getAmortizationSchedule(String loanId) async {
    try {
      final response = await _dioClient.get('/loans/$loanId/schedule');
      return (response.data as List)
          .map((json) => AmortpModel.fromJson(json))
          .toList();
    } catch (e) {
      rethrow;
    }
  }

  @override
  Future<List<GarantieModel>> getGuarantees(String loanId) async {
    try {
      final response = await _dioClient.get('/loans/$loanId/guarantees');
      return (response.data as List)
          .map((json) => GarantieModel.fromJson(json))
          .toList();
    } catch (e) {
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
      final response = await _dioClient.post('/loans/request', data: {
        'amount': amount,
        'duration': duration,
        'purpose': purpose,
      });
      return response.statusCode == 201 || response.statusCode == 200;
    } catch (e) {
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
      final response = await _dioClient.post('/loans/$loanId/pay', data: {
        'installmentId': installmentId,
        'amount': amount,
      });
      return response.statusCode == 200;
    } catch (e) {
      return false;
    }
  }
}
