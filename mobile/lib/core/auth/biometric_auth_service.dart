import 'package:local_auth/local_auth.dart';

/// Point unique pour la biométrie (disponibilité + dialogue système).
class BiometricAuthService {
  final LocalAuthentication _localAuth = LocalAuthentication();

  Future<bool> isDeviceReadyForBiometrics() async {
    final canCheck = await _localAuth.canCheckBiometrics;
    final isSupported = await _localAuth.isDeviceSupported();
    return canCheck && isSupported;
  }

  Future<bool> authenticate({
    required String localizedReason,
    bool biometricOnly = true,
    bool stickyAuth = true,
  }) async {
    if (!await isDeviceReadyForBiometrics()) return false;
    return _localAuth.authenticate(
      localizedReason: localizedReason,
      options: AuthenticationOptions(
        biometricOnly: biometricOnly,
        stickyAuth: stickyAuth,
      ),
    );
  }
}
