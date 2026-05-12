import 'package:equatable/equatable.dart';
import '../../../data/models/credit_model.dart';
import '../../../data/models/amortp_model.dart';
import '../../../data/models/extra_models.dart';

enum LoanStatus { initial, loading, success, failure }

class LoanState extends Equatable {
  static const Object _unsetInstallmentPulse = Object();

  final LoanStatus status;
  final List<LoanModel> loans;
  final LoanModel? selectedLoan;
  final List<AmortpModel> amortizationSchedule;
  final List<GarantieModel> guarantees;
  final String? errorMessage;

  /// Pulse UI après paiement d'échéance réussi (évite le faux positif au chargement du détail).
  final bool installmentPaymentSuccessPulse;

  const LoanState({
    this.status = LoanStatus.initial,
    this.loans = const [],
    this.selectedLoan,
    this.amortizationSchedule = const [],
    this.guarantees = const [],
    this.errorMessage,
    this.installmentPaymentSuccessPulse = false,
  });

  LoanState copyWith({
    LoanStatus? status,
    List<LoanModel>? loans,
    LoanModel? selectedLoan,
    List<AmortpModel>? amortizationSchedule,
    List<GarantieModel>? guarantees,
    String? errorMessage,
    Object? installmentPaymentSuccessPulse = _unsetInstallmentPulse,
  }) {
    return LoanState(
      status: status ?? this.status,
      loans: loans ?? this.loans,
      selectedLoan: selectedLoan ?? this.selectedLoan,
      amortizationSchedule: amortizationSchedule ?? this.amortizationSchedule,
      guarantees: guarantees ?? this.guarantees,
      errorMessage: errorMessage ?? this.errorMessage,
      installmentPaymentSuccessPulse:
          identical(installmentPaymentSuccessPulse, _unsetInstallmentPulse)
          ? this.installmentPaymentSuccessPulse
          : installmentPaymentSuccessPulse as bool,
    );
  }

  @override
  List<Object?> get props => [
    status,
    loans,
    selectedLoan,
    amortizationSchedule,
    guarantees,
    errorMessage,
    installmentPaymentSuccessPulse,
  ];
}
