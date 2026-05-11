import 'package:flutter_bloc/flutter_bloc.dart';
import '../../../domain/repositories/account_repository.dart';
import 'account_event.dart';
import 'account_state.dart';

export 'account_event.dart';
export 'account_state.dart';

class AccountBloc extends Bloc<AccountEvent, AccountState> {
  final AccountRepository _accountRepository;

  AccountBloc(this._accountRepository) : super(const AccountState()) {
    on<FetchAccounts>(_onFetchAccounts);
    on<SelectAccount>(_onSelectAccount);
  }

  Future<void> _onFetchAccounts(FetchAccounts event, Emitter<AccountState> emit) async {
    emit(state.copyWith(status: AccountStatus.loading));
    try {
      final accounts = await _accountRepository.getAccounts();
      final selectedAccount = accounts.firstWhere(
        (acc) => acc.isDefaultAccount,
        orElse: () => accounts.first,
      );
      emit(state.copyWith(
        status: AccountStatus.success,
        accounts: accounts,
        selectedAccount: selectedAccount,
      ));
    } catch (e) {
      emit(state.copyWith(status: AccountStatus.failure, errorMessage: e.toString()));
    }
  }

  void _onSelectAccount(SelectAccount event, Emitter<AccountState> emit) {
    final selectedAccount = state.accounts.firstWhere((acc) => acc.id == event.accountId);
    emit(state.copyWith(selectedAccount: selectedAccount));
  }
}
