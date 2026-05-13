import 'dart:async';

/// Petit bus d'événements de session, utilisé pour découpler
/// le [AuthInterceptor] (qui détecte un token périmé) du `AuthBloc`
/// (qui réagit en déclenchant le retour à l'écran de login).
///
/// Le `AuthInterceptor` n'a pas accès au `AuthRepository` (cycle de
/// dépendances avec DioClient), donc on passe par un singleton léger.
class SessionEvents {
  SessionEvents._();
  static final SessionEvents instance = SessionEvents._();

  final StreamController<SessionEvent> _ctrl =
      StreamController<SessionEvent>.broadcast();

  Stream<SessionEvent> get stream => _ctrl.stream;

  void emit(SessionEvent event) => _ctrl.add(event);
}

enum SessionEvent {
  /// Le serveur a refusé l'authentification (401/403 avec header présent).
  /// Le token a déjà été supprimé du SecureStorage par l'interceptor.
  expired,
}
