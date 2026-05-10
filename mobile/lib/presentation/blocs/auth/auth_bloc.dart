import 'dart:async';
import 'package:flutter_bloc/flutter_bloc.dart';
import '../../../core/network/session_events.dart';
import '../../../domain/repositories/auth_repository.dart';
import '../../../data/repositories/auth_repository_impl.dart';
import 'auth_event.dart';
import 'auth_state.dart';

class AuthBloc extends Bloc<AuthEvent, AuthState> {
  final AuthRepository _authRepository;
  StreamSubscription<SessionEvent>? _sessionSub;

  AuthBloc(this._authRepository) : super(AuthInitial()) {
    on<AppStarted>(_onAppStarted);
    on<LoginRequested>(_onLoginRequested);
    on<LogoutRequested>(_onLogoutRequested);

    // Lorsqu'un appel HTTP révèle un token périmé (401/403), l'AuthInterceptor
    // émet ici → on déclenche un logout côté bloc, ce qui force le GoRouter à
    // rediriger vers /login.
    _sessionSub = SessionEvents.instance.stream.listen((event) {
      if (event == SessionEvent.expired) {
        add(LogoutRequested());
      }
    });
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

  @override
  Future<void> close() {
    _sessionSub?.cancel();
    return super.close();
  }
}
