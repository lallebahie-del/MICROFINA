import 'dart:async';

import 'package:flutter_bloc/flutter_bloc.dart';
import '../../../core/auth/session_invalidation_broadcaster.dart';
import '../../../data/datasources/mock/mock_data.dart';
import '../../../data/repositories/auth_repository_impl.dart';
import '../../../domain/models/login_outcome.dart';
import '../../../domain/repositories/auth_repository.dart';
import 'auth_event.dart';
import 'auth_state.dart';

export 'auth_event.dart';
export 'auth_state.dart';

String _loginFailureMessage(LoginFailureKind kind) {
  switch (kind) {
    case LoginFailureKind.invalidCredentials:
      return 'Numéro ou code PIN incorrect.';
    case LoginFailureKind.network:
      return 'Impossible de joindre le serveur. Sur téléphone, lancez l\'app avec '
          'l\'IP de votre PC : flutter run --dart-define=API_BASE_URL=http://VOTRE_IP:8080 '
          '(pas localhost). Même Wi-Fi + backend démarré.';
    case LoginFailureKind.unknown:
      return "Erreur d'authentification. Réessayez.";
  }
}

class AuthBloc extends Bloc<AuthEvent, AuthState> {
  final AuthRepository _authRepository;
  final SessionInvalidationBroadcaster _sessionInvalidation;
  late final StreamSubscription<void> _invalidationSub;

  AuthBloc(this._authRepository, this._sessionInvalidation)
    : super(AuthInitial()) {
    on<AppStarted>(_onAppStarted);
    on<LoginRequested>(_onLoginRequested);
    on<RegisterRequested>(_onRegisterRequested);
    on<LogoutRequested>(_onLogoutRequested);
    on<RemoteSessionInvalidated>(_onRemoteSessionInvalidated);

    _invalidationSub = _sessionInvalidation.stream.listen((_) {
      if (!isClosed) add(RemoteSessionInvalidated());
    });
  }

  @override
  Future<void> close() async {
    await _invalidationSub.cancel();
    return super.close();
  }

  Future<void> _onRemoteSessionInvalidated(
    RemoteSessionInvalidated event,
    Emitter<AuthState> emit,
  ) async {
    if (!isClosed && state is! Unauthenticated && state is! AuthInitial) {
      emit(Unauthenticated());
    }
  }

  Future<void> _onAppStarted(AppStarted event, Emitter<AuthState> emit) async {
    final token = await _authRepository.getToken();
    final phone = await (_authRepository as AuthRepositoryImpl).secureStorage
        .getLastPhone();
    if (token != null) {
      if (phone != null) MockData.currentUserPhone = phone;
      emit(AuthSuccess(token, phone: phone));
    } else {
      emit(Unauthenticated());
    }
  }

  Future<void> _onRegisterRequested(
    RegisterRequested event,
    Emitter<AuthState> emit,
  ) async {
    emit(AuthLoading());
    try {
      final outcome = await _authRepository.registerMobile(
        phone: event.phone,
        pin: event.pin,
        nomComplet: event.nomComplet,
        email: event.email,
        address: event.address,
      );
      switch (outcome) {
        case LoginSuccess(:final token):
          MockData.currentUserPhone = event.phone;
          emit(AuthSuccess(token, phone: event.phone));
        case LoginFailure(:final kind):
          emit(AuthFailure(_loginFailureMessage(kind)));
      }
    } catch (e) {
      emit(AuthFailure(e.toString()));
    }
  }

  Future<void> _onLoginRequested(
    LoginRequested event,
    Emitter<AuthState> emit,
  ) async {
    // Logique du PIN de Panique
    if (event.pin == '9999') {
      await _authRepository.logout();
      // On pourrait aussi appeler clearAll() ici si le repo le permet
      emit(Unauthenticated());
      return;
    }

    emit(AuthLoading());
    try {
      final outcome = await _authRepository.login(event.phone, event.pin);
      switch (outcome) {
        case LoginSuccess(:final token):
          MockData.currentUserPhone = event.phone;
          emit(AuthSuccess(token, phone: event.phone));
        case LoginFailure(:final kind):
          emit(AuthFailure(_loginFailureMessage(kind)));
      }
    } catch (e) {
      emit(AuthFailure(e.toString()));
    }
  }

  Future<void> _onLogoutRequested(
    LogoutRequested event,
    Emitter<AuthState> emit,
  ) async {
    try {
      await _authRepository.logout();
    } catch (_) {
      // Réseau ou API : on force quand même la sortie locale (tokens effacés dans le repo).
    }
    emit(Unauthenticated());
  }
}
