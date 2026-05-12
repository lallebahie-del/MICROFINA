import 'package:flutter/material.dart';
import 'package:secure_application/secure_application.dart';
import '../di/service_locator.dart';
import '../storage/secure_storage_service.dart';

class SecureApp extends StatefulWidget {
  final Widget child;

  const SecureApp({super.key, required this.child});

  @override
  State<SecureApp> createState() => _SecureAppState();
}

class _SecureAppState extends State<SecureApp> {
  late final SecureStorageService _secureStorage = sl<SecureStorageService>();
  bool _isSecureMode = true;

  @override
  void initState() {
    super.initState();
    _checkSecureMode();
  }

  Future<void> _checkSecureMode() async {
    final mode = await _secureStorage.getSecureMode();
    if (mounted) {
      setState(() {
        _isSecureMode = mode;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    if (!_isSecureMode) return widget.child;

    return Directionality(
      textDirection: TextDirection.ltr,
      child: SecureApplication(
        nativeRemoveDelay: 100,
        autoUnlockNative: true,
        child: SecureGate(
          blurr: 20,
          opacity: 0.6,
          lockedBuilder: (context, secureNotifier) => Scaffold(
            body: Container(
              decoration: const BoxDecoration(
                gradient: LinearGradient(
                  colors: [Color(0xFF0D47A1), Color(0xFF1976D2)],
                  begin: Alignment.topLeft,
                  end: Alignment.bottomRight,
                ),
              ),
              child: const Center(
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    Icon(Icons.lock_rounded, color: Colors.white, size: 80),
                    SizedBox(height: 24),
                    Text(
                      'Espace Sécurisé',
                      style: TextStyle(
                        color: Colors.white,
                        fontSize: 24,
                        fontWeight: FontWeight.bold,
                        letterSpacing: 1.2,
                      ),
                    ),
                    SizedBox(height: 8),
                    Text(
                      'microCredit protège vos données',
                      style: TextStyle(color: Colors.white70, fontSize: 16),
                    ),
                  ],
                ),
              ),
            ),
          ),
          child: widget.child,
        ),
      ),
    );
  }
}
