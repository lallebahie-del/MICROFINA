import 'package:equatable/equatable.dart';

abstract class AccountEvent extends Equatable {
  const AccountEvent();
  @override
  List<Object?> get props => [];
}

class FetchAccounts extends AccountEvent {}
class SelectAccount extends AccountEvent {
  final String accountId;
  const SelectAccount(this.accountId);
  @override
  List<Object?> get props => [accountId];
}
