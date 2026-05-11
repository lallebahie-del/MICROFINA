import 'package:flutter_bloc/flutter_bloc.dart';
import '../../../domain/repositories/transaction_repository.dart';
import 'transfer_event.dart';
import 'transfer_state.dart';

export 'transfer_event.dart';
export 'transfer_state.dart';

class TransferBloc extends Bloc<TransferEvent, TransferState> {
  final TransactionRepository _transactionRepository;

  TransferBloc(this._transactionRepository) : super(const TransferState()) {
    on<PerformTransfer>(_onPerformTransfer);
  }

  Future<void> _onPerformTransfer(PerformTransfer event, Emitter<TransferState> emit) async {
    emit(state.copyWith(status: TransferStatus.loading));
    
    try {
      final success = await _transactionRepository.transferFunds(
        fromAccountId: event.fromAccountId,
        toAccountId: event.toAccountId,
        amount: event.amount,
        reason: event.reason,
      );

      if (success) {
        emit(state.copyWith(status: TransferStatus.success));
      } else {
        emit(state.copyWith(
          status: TransferStatus.failure,
          errorMessage: "Le virement a échoué. Veuillez vérifier vos fonds et les informations du compte.",
        ));
      }
    } catch (e) {
      emit(state.copyWith(
        status: TransferStatus.failure,
        errorMessage: "Une erreur inattendue est survenue: $e",
      ));
    }
  }
}
