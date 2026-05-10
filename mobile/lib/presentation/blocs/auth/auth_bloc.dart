import 'package:flutter_bloc/flutter_bloc.dart';
import '../../../domain/repositories/auth_repository.dart';
import '../../../data/repositories/auth_repository_impl.dart';
import 'auth_event.dart';
import 'auth_state.dart';

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
      final token = await _authRepository.login(event.phone, event.pin);
      if (token != null) {
        emit(AuthSuccess(token, phone: event.phone));
      } else {
        emit(const AuthFailure("Erreur d'authentification"));
      }
    } catch (e) {
      emit(AuthFailure(e.toString()));
    }
  }

  Future<void> _onLogoutRequested(LogoutRequested event, Emitter<AuthState> emit) async {
    await _authRepository.logout();
    emit(Unauthenticated());
  }
}
