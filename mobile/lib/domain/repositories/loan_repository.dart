import '../../data/models/credit_model.dart';
import '../../data/models/amortp_model.dart';
import '../../data/models/extra_models.dart';

abstract class LoanRepository {
  Future<List<LoanModel>> getLoans();
  Future<LoanModel> getLoanDetails(String loanId);
  Future<List<AmortpModel>> getAmortizationSchedule(String loanId);
  Future<List<GarantieModel>> getGuarantees(String loanId);
  Future<bool> requestLoan({
    required double amount,
    required int duration,
    required String purpose,
  });
  Future<bool> payInstallment({
    required String loanId,
    required int installmentId,
    required double amount,
  });
}
