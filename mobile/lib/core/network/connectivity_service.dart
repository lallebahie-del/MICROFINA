import 'dart:async';
import 'package:connectivity_plus/connectivity_plus.dart';

enum ConnectivityStatus { online, offline }

class ConnectivityService {
  final Connectivity _connectivity = Connectivity();
  final StreamController<ConnectivityStatus> _controller = StreamController<ConnectivityStatus>.broadcast();

  Stream<ConnectivityStatus> get stream => _controller.stream;

  ConnectivityService() {
    _connectivity.onConnectivityChanged.listen((List<ConnectivityResult> results) {
      _controller.add(_getStatusFromResult(results));
    });
  }

  ConnectivityStatus _getStatusFromResult(List<ConnectivityResult> results) {
    if (results.contains(ConnectivityResult.none)) {
      return ConnectivityStatus.offline;
    }
    return ConnectivityStatus.online;
  }

  Future<bool> get isConnected async {
    final results = await _connectivity.checkConnectivity();
    return !results.contains(ConnectivityResult.none);
  }
}
