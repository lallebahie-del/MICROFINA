import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:equatable/equatable.dart';
import 'dart:math';

// Events
abstract class LoanSimulatorEvent extends Equatable {
  const LoanSimulatorEvent();
  @override
  List<Object?> get props => [];
}

class UpdateSimulation extends LoanSimulatorEvent {
  final double amount;
  final int durationMonths;
  final double annualRate;

  const UpdateSimulation({
    required this.amount,
    required this.durationMonths,
    required this.annualRate,
  });

  @override
  List<Object?> get props => [amount, durationMonths, annualRate];
}

// State
class LoanSimulatorState extends Equatable {
  final double amount;
  final int durationMonths;
  final double annualRate;
  final double monthlyPayment;
  final double totalInterest;
  final double totalRepayment;

  const LoanSimulatorState({
    this.amount = 1000000,
    this.durationMonths = 12,
    this.annualRate = 12.0,
    this.monthlyPayment = 0,
    this.totalInterest = 0,
    this.totalRepayment = 0,
  });

  LoanSimulatorState copyWith({
    double? amount,
    int? durationMonths,
    double? annualRate,
    double? monthlyPayment,
    double? totalInterest,
    double? totalRepayment,
  }) {
    return LoanSimulatorState(
      amount: amount ?? this.amount,
      durationMonths: durationMonths ?? this.durationMonths,
      annualRate: annualRate ?? this.annualRate,
      monthlyPayment: monthlyPayment ?? this.monthlyPayment,
      totalInterest: totalInterest ?? this.totalInterest,
      totalRepayment: totalRepayment ?? this.totalRepayment,
    );
  }

  @override
  List<Object?> get props => [amount, durationMonths, annualRate, monthlyPayment, totalInterest, totalRepayment];
}

// Bloc
class LoanSimulatorBloc extends Bloc<LoanSimulatorEvent, LoanSimulatorState> {
  LoanSimulatorBloc() : super(const LoanSimulatorState()) {
    on<UpdateSimulation>(_onUpdateSimulation);
    
    // Simulation initiale
    add(const UpdateSimulation(amount: 1000000, durationMonths: 12, annualRate: 12.0));
  }

  void _onUpdateSimulation(UpdateSimulation event, Emitter<LoanSimulatorState> emit) {
    final double monthlyRate = (event.annualRate / 100) / 12;
    final int n = event.durationMonths;
    
    double monthlyPayment;
    if (monthlyRate == 0) {
      monthlyPayment = event.amount / n;
    } else {
      monthlyPayment = (event.amount * monthlyRate * pow(1 + monthlyRate, n)) / (pow(1 + monthlyRate, n) - 1);
    }
    
    final double totalRepayment = monthlyPayment * n;
    final double totalInterest = totalRepayment - event.amount;

    emit(state.copyWith(
      amount: event.amount,
      durationMonths: event.durationMonths,
      annualRate: event.annualRate,
      monthlyPayment: monthlyPayment,
      totalInterest: totalInterest,
      totalRepayment: totalRepayment,
    ));
  }
}
