import 'package:flutter/foundation.dart';

/// Données de démo / repli mock : actives en debug, ou en release si
/// `flutter run --dart-define=ALLOW_MOCK_FALLBACK=true`.
class AppBuildConfig {
  AppBuildConfig._();

  static const bool _allowMockFromDefine = bool.fromEnvironment(
    'ALLOW_MOCK_FALLBACK',
    defaultValue: false,
  );

  /// Repli sur JSON mock quand l’API est vide / en erreur (hors cache).
  static bool get allowMockFallback => kDebugMode || _allowMockFromDefine;
}
