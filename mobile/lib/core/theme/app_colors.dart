import 'package:flutter/material.dart';

class AppColors {
  // --- Primary Brand Colors ---
  static const Color primary = Color(0xFF1E293B); // Slate 800
  static const Color primaryVariant = Color(0xFF0F172A); // Slate 900
  static const Color secondary = Color(0xFF3B82F6); // Blue 500
  static const Color secondaryVariant = Color(0xFF2563EB); // Blue 600
  
  // --- Surface Colors ---
  static const Color surface = Color(0xFFFFFFFF);
  static const Color surfaceVariant = Color(0xFFF8FAFC); // Slate 50
  static const Color background = Color(0xFFF8FAFC); // Slate 50
  
  // --- Status Colors ---
  static const Color success = Color(0xFF10B981); // Emerald 500
  static const Color successLight = Color(0xFFD1FAE5); // Emerald 100
  static const Color warning = Color(0xFFF59E0B); // Amber 500
  static const Color warningLight = Color(0xFFFEF3C7); // Amber 100
  static const Color error = Color(0xFFEF4444); // Red 500
  static const Color errorLight = Color(0xFFFEE2E2); // Red 100
  static const Color info = Color(0xFF06B6D4); // Cyan 500
  static const Color infoLight = Color(0xFFCFFAFE); // Cyan 100
  
  // --- Text Colors ---
  static const Color textPrimary = Color(0xFF1E293B); // Slate 800
  static const Color textSecondary = Color(0xFF64748B); // Slate 500
  static const Color textTertiary = Color(0xFF94A3B8); // Slate 400
  static const Color textDisabled = Color(0xFFCBD5E1); // Slate 300
  static const Color textOnPrimary = Color(0xFFFFFFFF);
  
  // --- Border & Divider Colors ---
  static const Color border = Color(0xFFE2E8F0); // Slate 200
  static const Color borderLight = Color(0xFFF1F5F9); // Slate 100
  static const Color divider = Color(0xFFE2E8F0); // Slate 200
  
  // --- Shadow Colors ---
  static Color shadow = Colors.black.withOpacity(0.08);
  static Color shadowLight = Colors.black.withOpacity(0.04);
  static Color shadowHeavy = Colors.black.withOpacity(0.16);
  
  // --- Gradient Colors ---
  static const List<Color> primaryGradient = [
    Color(0xFF1E293B), // Slate 800
    Color(0xFF334155), // Slate 700
  ];
  
  static const List<Color> secondaryGradient = [
    Color(0xFF3B82F6), // Blue 500
    Color(0xFF2563EB), // Blue 600
  ];
  
  static const List<Color> successGradient = [
    Color(0xFF10B981), // Emerald 500
    Color(0xFF059669), // Emerald 600
  ];
  
  static const List<Color> cardGradient = [
    Color(0xFFFFFFFF),
    Color(0xFFF8FAFC),
  ];
  
  // --- Glass Effect Colors ---
  static Color glass = Colors.white.withOpacity(0.1);
  static Color glassBorder = Colors.white.withOpacity(0.2);
  
  // --- Dark Theme Colors ---
  static const Color darkPrimary = Color(0xFF0F172A); // Slate 900
  static const Color darkSurface = Color(0xFF1E293B); // Slate 800
  static const Color darkSurfaceVariant = Color(0xFF334155); // Slate 700
  static const Color darkBackground = Color(0xFF0F172A); // Slate 900
  static const Color darkBorder = Color(0xFF475569); // Slate 600
  static const Color darkTextPrimary = Color(0xFFF8FAFC); // Slate 50
  static const Color darkTextSecondary = Color(0xFFCBD5E1); // Slate 300
  static const Color darkTextTertiary = Color(0xFF94A3B8); // Slate 400
  
  // --- Account Type Colors ---
  static const Color accountChecking = Color(0xFF3B82F6); // Blue 500
  static const Color accountSavings = Color(0xFF10B981); // Emerald 500
  static const Color accountLoan = Color(0xFFF59E0B); // Amber 500
  static const Color accountInvestment = Color(0xFF8B5CF6); // Violet 500
  static const Color accountBusiness = Color(0xFFEC4899); // Pink 500
  
  // --- Transaction Category Colors ---
  static const Color categoryFood = Color(0xFFEF4444); // Red 500
  static const Color categoryTransport = Color(0xFF3B82F6); // Blue 500
  static const Color categoryShopping = Color(0xFF8B5CF6); // Violet 500
  static const Color categoryEntertainment = Color(0xFFEC4899); // Pink 500
  static const Color categoryBills = Color(0xFFF59E0B); // Amber 500
  static const Color categoryHealth = Color(0xFF10B981); // Emerald 500
  static const Color categoryEducation = Color(0xFF06B6D4); // Cyan 500
  static const Color categoryOther = Color(0xFF64748B); // Slate 500
  
  // --- Opacity Helpers ---
  static Color primaryWithOpacity(double opacity) => primary.withOpacity(opacity);
  static Color secondaryWithOpacity(double opacity) => secondary.withOpacity(opacity);
  static Color successWithOpacity(double opacity) => success.withOpacity(opacity);
  static Color errorWithOpacity(double opacity) => error.withOpacity(opacity);
  static Color warningWithOpacity(double opacity) => warning.withOpacity(opacity);
}

class AppColorExtensions extends ThemeExtension<AppColorExtensions> {
  final Color? primary;
  final Color? secondary;
  final Color? success;
  final Color? warning;
  final Color? error;
  final Color? surface;
  final Color? background;
  
  const AppColorExtensions({
    this.primary,
    this.secondary,
    this.success,
    this.warning,
    this.error,
    this.surface,
    this.background,
  });

  @override
  AppColorExtensions copyWith({
    Color? primary,
    Color? secondary,
    Color? success,
    Color? warning,
    Color? error,
    Color? surface,
    Color? background,
  }) {
    return AppColorExtensions(
      primary: primary ?? this.primary,
      secondary: secondary ?? this.secondary,
      success: success ?? this.success,
      warning: warning ?? this.warning,
      error: error ?? this.error,
      surface: surface ?? this.surface,
      background: background ?? this.background,
    );
  }

  @override
  AppColorExtensions lerp(ThemeExtension<AppColorExtensions>? other, double t) {
    if (other is! AppColorExtensions) return this;
    
    return AppColorExtensions(
      primary: Color.lerp(primary, other.primary, t),
      secondary: Color.lerp(secondary, other.secondary, t),
      success: Color.lerp(success, other.success, t),
      warning: Color.lerp(warning, other.warning, t),
      error: Color.lerp(error, other.error, t),
      surface: Color.lerp(surface, other.surface, t),
      background: Color.lerp(background, other.background, t),
    );
  }
}
