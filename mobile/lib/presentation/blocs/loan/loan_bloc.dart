import 'package:flutter_bloc/flutter_bloc.dart';
import '../../../data/models/amortp_model.dart';
import '../../../data/models/credit_model.dart';
import '../../../data/models/extra_models.dart';
import '../../../domain/repositories/loan_repository.dart';
import 'loan_event.dart';
import 'loan_state.dart';

export 'loan_event.dart';
export 'loan_state.dart';

class LoanBloc extends Bloc<LoanEvent, LoanState> {
  final LoanRepository _loanRepository;
  bool _awaitingInstallmentPaymentRefresh = false;

  LoanBloc(this._loanRepository) : super(const LoanState()) {
    on<FetchLoans>(_onFetchLoans);
    on<FetchLoanDetails>(_onFetchLoanDetails);
    on<SubmitLoanRequest>(_onSubmitLoanRequest);
    on<PayInstallment>(_onPayInstallment);
    on<ClearInstallmentPaymentPulse>(_onClearInstallmentPaymentPulse);
  }

  void _onClearInstallmentPaymentPulse(
    ClearInstallmentPaymentPulse event,
    Emitter<LoanState> emit,
  ) {
    emit(state.copyWith(installmentPaymentSuccessPulse: false));
  }

  Future<void> _onPayInstallment(
    PayInstallment event,
    Emitter<LoanState> emit,
  ) async {
    emit(
      state.copyWith(
        status: LoanStatus.loading,
        installmentPaymentSuccessPulse: false,
      ),
    );
    try {
      final installment = state.amortizationSchedule.firstWhere(
        (i) => i.id == event.installmentId,
      );

      final bool success = await _loanRepository.payInstallment(
        loanId: event.loanId,
        amount: installment.montantTotal,
        installmentId: event.installmentId,
      );

      if (!success) {
        emit(
          state.copyWith(
            status: LoanStatus.failure,
            errorMessage:
                "Solde insuffisant ou erreur lors du règlement de l'échéance.",
          ),
        );
        return;
      }

      _awaitingInstallmentPaymentRefresh = true;
      add(FetchLoanDetails(event.loanId));
    } catch (e) {
      emit(
        state.copyWith(status: LoanStatus.failure, errorMessage: e.toString()),
      );
    }
  }

  Future<void> _onSubmitLoanRequest(
    SubmitLoanRequest event,
    Emitter<LoanState> emit,
  ) async {
    emit(state.copyWith(status: LoanStatus.loading));
    try {
      final success = await _loanRepository.requestLoan(
        amount: event.amount,
        duration: event.durationMonths,
        purpose: event.purpose,
      );

      if (success) {
        emit(state.copyWith(status: LoanStatus.success));
      } else {
        emit(
          state.copyWith(
            status: LoanStatus.failure,
            errorMessage: "Échec de la demande de prêt",
          ),
        );
      }
    } catch (e) {
      emit(
        state.copyWith(status: LoanStatus.failure, errorMessage: e.toString()),
      );
    }
  }

  Future<void> _onFetchLoans(FetchLoans event, Emitter<LoanState> emit) async {
    emit(state.copyWith(status: LoanStatus.loading));
    try {
      final loans = await _loanRepository.getLoans();

      emit(state.copyWith(status: LoanStatus.success, loans: loans));
    } catch (e) {
      emit(
        state.copyWith(status: LoanStatus.failure, errorMessage: e.toString()),
      );
    }
  }

  Future<void> _onFetchLoanDetails(
    FetchLoanDetails event,
    Emitter<LoanState> emit,
  ) async {
    emit(
      state.copyWith(
        status: LoanStatus.loading,
        installmentPaymentSuccessPulse: false,
      ),
    );
    try {
      final results = await Future.wait([
        _loanRepository.getLoanDetails(event.loanId),
        _loanRepository.getAmortizationSchedule(event.loanId),
        _loanRepository.getGuarantees(event.loanId),
      ]);

      final showPaymentPulse = _awaitingInstallmentPaymentRefresh;
      if (_awaitingInstallmentPaymentRefresh) {
        _awaitingInstallmentPaymentRefresh = false;
      }

      emit(
        state.copyWith(
          status: LoanStatus.success,
          selectedLoan: results[0] as LoanModel,
          amortizationSchedule: results[1] as List<AmortpModel>,
          guarantees: results[2] as List<GarantieModel>,
          installmentPaymentSuccessPulse: showPaymentPulse,
        ),
      );
    } catch (e) {
      emit(
        state.copyWith(status: LoanStatus.failure, errorMessage: e.toString()),
      );
    }
  }
}
