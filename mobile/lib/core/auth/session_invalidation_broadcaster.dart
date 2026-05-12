import 'dart:async';

/// Signal émis quand la session est invalidée hors flux UI (ex. refresh token échoué).
class SessionInvalidationBroadcaster {
  final _controller = StreamController<void>.broadcast();

  Stream<void> get stream => _controller.stream;

  void broadcast() {
    if (!_controller.isClosed) {
      _controller.add(null);
    }
  }
}
