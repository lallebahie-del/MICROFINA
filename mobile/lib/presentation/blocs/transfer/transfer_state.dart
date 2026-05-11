import 'package:equatable/equatable.dart';

enum TransferStatus { initial, loading, success, failure }

class TransferState extends Equatable {
  final TransferStatus status;
  final String? errorMessage;

  const TransferState({
    this.status = TransferStatus.initial,
    this.errorMessage,
  });

  TransferState copyWith({
    TransferStatus? status,
    String? errorMessage,
  }) {
    return TransferState(
      status: status ?? this.status,
      errorMessage: errorMessage ?? this.errorMessage,
    );
  }

  @override
  List<Object?> get props => [status, errorMessage];
}
