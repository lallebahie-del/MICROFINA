import 'dart:async';

/// Signal émis après une opération (virement, paiement) pour rafraîchir le badge notifications.
class NotificationRefreshBroadcaster {
  final _controller = StreamController<void>.broadcast();

  Stream<void> get stream => _controller.stream;

  void broadcast() {
    if (!_controller.isClosed) {
      _controller.add(null);
    }
  }
}
