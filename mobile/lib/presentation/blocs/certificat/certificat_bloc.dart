import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:equatable/equatable.dart';
import '../../../domain/repositories/certificat_repository.dart';
import '../../../data/models/extra_models.dart';

// Events
abstract class CertificatEvent extends Equatable {
  const CertificatEvent();
  @override
  List<Object?> get props => [];
}

class FetchCertificats extends CertificatEvent {}

// State
enum CertificatStatus { initial, loading, success, failure }

class CertificatState extends Equatable {
  final CertificatStatus status;
  final List<CertificatModel> certificats;
  final String? errorMessage;

  const CertificatState({
    this.status = CertificatStatus.initial,
    this.certificats = const [],
    this.errorMessage,
  });

  CertificatState copyWith({
    CertificatStatus? status,
    List<CertificatModel>? certificats,
    String? errorMessage,
  }) {
    return CertificatState(
      status: status ?? this.status,
      certificats: certificats ?? this.certificats,
      errorMessage: errorMessage ?? this.errorMessage,
    );
  }

  @override
  List<Object?> get props => [status, certificats, errorMessage];
}

// Bloc
class CertificatBloc extends Bloc<CertificatEvent, CertificatState> {
  final CertificatRepository _certificatRepository;

  CertificatBloc(this._certificatRepository) : super(const CertificatState()) {
    on<FetchCertificats>(_onFetchCertificats);
  }

  Future<void> _onFetchCertificats(
    FetchCertificats event,
    Emitter<CertificatState> emit,
  ) async {
    emit(state.copyWith(status: CertificatStatus.loading));
    try {
      final certificats = await _certificatRepository.getCertificats();
      emit(
        state.copyWith(
          status: CertificatStatus.success,
          certificats: certificats,
        ),
      );
    } catch (e) {
      emit(
        state.copyWith(
          status: CertificatStatus.failure,
          errorMessage: e.toString(),
        ),
      );
    }
  }
}
