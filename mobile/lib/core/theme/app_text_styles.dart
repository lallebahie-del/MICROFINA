import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';

class AppTextStyles {
  // --- Display Styles ---
  static TextStyle get displayLarge => GoogleFonts.inter(
    fontSize: 57,
    fontWeight: FontWeight.w800,
    letterSpacing: -1.5,
    height: 1.1,
  );

  static TextStyle get displayMedium => GoogleFonts.inter(
    fontSize: 45,
    fontWeight: FontWeight.w800,
    letterSpacing: -1.0,
    height: 1.2,
  );

  static TextStyle get displaySmall => GoogleFonts.inter(
    fontSize: 36,
    fontWeight: FontWeight.w700,
    letterSpacing: -0.5,
    height: 1.2,
  );

  // --- Headline Styles ---
  static TextStyle get headlineLarge => GoogleFonts.inter(
    fontSize: 32,
    fontWeight: FontWeight.w700,
    letterSpacing: -0.25,
    height: 1.3,
  );

  static TextStyle get headlineMedium => GoogleFonts.inter(
    fontSize: 28,
    fontWeight: FontWeight.w600,
    letterSpacing: 0,
    height: 1.3,
  );

  static TextStyle get headlineSmall => GoogleFonts.inter(
    fontSize: 24,
    fontWeight: FontWeight.w600,
    letterSpacing: 0,
    height: 1.3,
  );

  // --- Title Styles ---
  static TextStyle get titleLarge => GoogleFonts.inter(
    fontSize: 22,
    fontWeight: FontWeight.w600,
    letterSpacing: 0,
    height: 1.4,
  );

  static TextStyle get titleMedium => GoogleFonts.inter(
    fontSize: 16,
    fontWeight: FontWeight.w500,
    letterSpacing: 0.15,
    height: 1.4,
  );

  static TextStyle get titleSmall => GoogleFonts.inter(
    fontSize: 14,
    fontWeight: FontWeight.w500,
    letterSpacing: 0.1,
    height: 1.4,
  );

  // --- Body Styles ---
  static TextStyle get bodyLarge => GoogleFonts.inter(
    fontSize: 16,
    fontWeight: FontWeight.w400,
    letterSpacing: 0.5,
    height: 1.5,
  );

  static TextStyle get bodyMedium => GoogleFonts.inter(
    fontSize: 14,
    fontWeight: FontWeight.w400,
    letterSpacing: 0.25,
    height: 1.4,
  );

  static TextStyle get bodySmall => GoogleFonts.inter(
    fontSize: 12,
    fontWeight: FontWeight.w400,
    letterSpacing: 0.4,
    height: 1.3,
  );

  // --- Label Styles ---
  static TextStyle get labelLarge => GoogleFonts.inter(
    fontSize: 14,
    fontWeight: FontWeight.w500,
    letterSpacing: 0.1,
    height: 1.3,
  );

  static TextStyle get labelMedium => GoogleFonts.inter(
    fontSize: 12,
    fontWeight: FontWeight.w500,
    letterSpacing: 0.5,
    height: 1.3,
  );

  static TextStyle get labelSmall => GoogleFonts.inter(
    fontSize: 11,
    fontWeight: FontWeight.w500,
    letterSpacing: 0.5,
    height: 1.3,
  );

  // --- Custom Fintech Styles ---
  static TextStyle get heroTitle => GoogleFonts.inter(
    fontSize: 32,
    fontWeight: FontWeight.w900,
    letterSpacing: -1.0,
    height: 1.2,
  );

  static TextStyle get cardTitle => GoogleFonts.inter(
    fontSize: 18,
    fontWeight: FontWeight.w700,
    letterSpacing: -0.2,
    height: 1.3,
  );

  static TextStyle get cardSubtitle => GoogleFonts.inter(
    fontSize: 14,
    fontWeight: FontWeight.w500,
    letterSpacing: 0.1,
    height: 1.3,
  );

  static TextStyle get balanceAmount => GoogleFonts.inter(
    fontSize: 32,
    fontWeight: FontWeight.w800,
    letterSpacing: -0.5,
    height: 1.2,
  );

  static TextStyle get transactionAmount => GoogleFonts.inter(
    fontSize: 16,
    fontWeight: FontWeight.w700,
    letterSpacing: -0.2,
    height: 1.2,
  );

  static TextStyle get buttonText => GoogleFonts.inter(
    fontSize: 16,
    fontWeight: FontWeight.w700,
    letterSpacing: 0.2,
    height: 1.2,
  );

  static TextStyle get caption => GoogleFonts.inter(
    fontSize: 10,
    fontWeight: FontWeight.w600,
    letterSpacing: 1.0,
    height: 1.2,
  );

  static TextStyle get overline => GoogleFonts.inter(
    fontSize: 10,
    fontWeight: FontWeight.w700,
    letterSpacing: 1.5,
    height: 1.3,
  );

  // --- Input Styles ---
  static TextStyle get inputLabel => GoogleFonts.inter(
    fontSize: 14,
    fontWeight: FontWeight.w500,
    letterSpacing: 0.1,
    height: 1.3,
  );

  static TextStyle get inputText => GoogleFonts.inter(
    fontSize: 16,
    fontWeight: FontWeight.w600,
    letterSpacing: 0.2,
    height: 1.4,
  );

  static TextStyle get inputHint => GoogleFonts.inter(
    fontSize: 16,
    fontWeight: FontWeight.w400,
    letterSpacing: 0.2,
    height: 1.4,
  );

  // --- Navigation Styles ---
  static TextStyle get navigationLabel => GoogleFonts.inter(
    fontSize: 12,
    fontWeight: FontWeight.w600,
    letterSpacing: 0.4,
    height: 1.2,
  );

  static TextStyle get navigationActive => GoogleFonts.inter(
    fontSize: 12,
    fontWeight: FontWeight.w700,
    letterSpacing: 0.4,
    height: 1.2,
  );

  // --- Status Styles ---
  static TextStyle get statusBadge => GoogleFonts.inter(
    fontSize: 10,
    fontWeight: FontWeight.w700,
    letterSpacing: 0.8,
    height: 1.2,
  );

  static TextStyle get errorText => GoogleFonts.inter(
    fontSize: 12,
    fontWeight: FontWeight.w500,
    letterSpacing: 0.4,
    height: 1.3,
  );

  static TextStyle get successText => GoogleFonts.inter(
    fontSize: 12,
    fontWeight: FontWeight.w500,
    letterSpacing: 0.4,
    height: 1.3,
  );
}

class AppTextStylesExtensions extends ThemeExtension<AppTextStylesExtensions> {
  final TextStyle? heroTitle;
  final TextStyle? cardTitle;
  final TextStyle? balanceAmount;
  final TextStyle? transactionAmount;

  const AppTextStylesExtensions({
    this.heroTitle,
    this.cardTitle,
    this.balanceAmount,
    this.transactionAmount,
  });

  @override
  AppTextStylesExtensions copyWith({
    TextStyle? heroTitle,
    TextStyle? cardTitle,
    TextStyle? balanceAmount,
    TextStyle? transactionAmount,
  }) {
    return AppTextStylesExtensions(
      heroTitle: heroTitle ?? this.heroTitle,
      cardTitle: cardTitle ?? this.cardTitle,
      balanceAmount: balanceAmount ?? this.balanceAmount,
      transactionAmount: transactionAmount ?? this.transactionAmount,
    );
  }

  @override
  AppTextStylesExtensions lerp(
    ThemeExtension<AppTextStylesExtensions>? other,
    double t,
  ) {
    if (other is! AppTextStylesExtensions) return this;

    return AppTextStylesExtensions(
      heroTitle: TextStyle.lerp(heroTitle, other.heroTitle, t),
      cardTitle: TextStyle.lerp(cardTitle, other.cardTitle, t),
      balanceAmount: TextStyle.lerp(balanceAmount, other.balanceAmount, t),
      transactionAmount: TextStyle.lerp(
        transactionAmount,
        other.transactionAmount,
        t,
      ),
    );
  }
}
