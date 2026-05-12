import 'package:flutter_bloc/flutter_bloc.dart';
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
      return 'Problème de connexion. Vérifiez votre réseau et réessayez.';
    case LoginFailureKind.unknown:
      return "Erreur d'authentification. Réessayez.";
  }
}

class AuthBloc extends Bloc<AuthEvent, AuthState> {
  final AuthRepository _authRepository;

  AuthBloc(this._authRepository) : super(AuthInitial()) {
    on<AppStarted>(_onAppStarted);
    on<LoginRequested>(_onLoginRequested);
    on<LogoutRequested>(_onLogoutRequested);
  }

  Future<void> _onAppStarted(AppStarted event, Emitter<AuthState> emit) async {
    final token = await _authRepository.getToken();
    final phone = await (_authRepository as AuthRepositoryImpl).secureStorage.getLastPhone();
    if (token != null) {
      if (phone != null) MockData.currentUserPhone = phone;
      emit(AuthSuccess(token, phone: phone));
    } else {
      emit(Unauthenticated());
    }
  }

  Future<void> _onLoginRequested(LoginRequested event, Emitter<AuthState> emit) async {
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

  Future<void> _onLogoutRequested(LogoutRequested event, Emitter<AuthState> emit) async {
    try {
      await _authRepository.logout();
    } catch (_) {
      // Réseau ou API : on force quand même la sortie locale (tokens effacés dans le repo).
    }
    emit(Unauthenticated());
  }
}
