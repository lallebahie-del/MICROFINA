import 'package:flutter/material.dart';
import 'package:screen_protector/screen_protector.dart';

/// MODULE 2.6 : désactive les captures d'écran sur la portion d'UI enveloppée
/// (complémentaire au floutage App Switcher via [SecureApp]).
class SensitiveScreenGuard extends StatefulWidget {
  final Widget child;

  const SensitiveScreenGuard({super.key, required this.child});

  @override
  State<SensitiveScreenGuard> createState() => _SensitiveScreenGuardState();
}

class _SensitiveScreenGuardState extends State<SensitiveScreenGuard> {
  @override
  void initState() {
    super.initState();
    _enable();
  }

  @override
  void dispose() {
    _disable();
    super.dispose();
  }

  Future<void> _enable() async {
    try {
      await ScreenProtector.preventScreenshotOn();
    } catch (_) {}
  }

  Future<void> _disable() async {
    try {
      await ScreenProtector.preventScreenshotOff();
    } catch (_) {}
  }

  @override
  Widget build(BuildContext context) => widget.child;
}
