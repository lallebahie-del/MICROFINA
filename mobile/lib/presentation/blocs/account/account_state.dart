import 'package:equatable/equatable.dart';
import '../../../data/models/compte_eps_model.dart';

enum AccountStatus { initial, loading, success, failure }

class AccountState extends Equatable {
  final AccountStatus status;
  final List<CompteEpsModel> accounts;
  final CompteEpsModel? selectedAccount;
  final String? errorMessage;

  const AccountState({
    this.status = AccountStatus.initial,
    this.accounts = const [],
    this.selectedAccount,
    this.errorMessage,
  });

  AccountState copyWith({
    AccountStatus? status,
    List<CompteEpsModel>? accounts,
    CompteEpsModel? selectedAccount,
    String? errorMessage,
  }) {
    return AccountState(
      status: status ?? this.status,
      accounts: accounts ?? this.accounts,
      selectedAccount: selectedAccount ?? this.selectedAccount,
      errorMessage: errorMessage ?? this.errorMessage,
    );
  }

  @override
  List<Object?> get props => [status, accounts, selectedAccount, errorMessage];
}
