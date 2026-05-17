/// Données de démo / repli mock si
/// `flutter run --dart-define=ALLOW_MOCK_FALLBACK=true`.
class AppBuildConfig {
  AppBuildConfig._();

  static const bool _allowMockFromDefine = bool.fromEnvironment(
    'ALLOW_MOCK_FALLBACK',
    defaultValue: false,
  );

  /// Repli mock uniquement si `flutter run --dart-define=ALLOW_MOCK_FALLBACK=true`.
  static bool get allowMockFallback => _allowMockFromDefine;
}
