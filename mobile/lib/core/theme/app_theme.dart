import 'package:flutter/material.dart';

class AppTheme {
  // --- Couleurs Premium Neo-Bank ---
  static const Color primaryBlue = Color(0xFF1E293B); // Slate 800 - Pro & Moderne
  static const Color accentBlue = Color(0xFF3B82F6);  // Blue 500 - Vibrant
  static const Color lightBlue = Color(0xFFEFF6FF);   // Blue 50
  
  static const Color successGreen = Color(0xFF10B981); // Emerald 500
  static const Color errorRed = Color(0xFFEF4444);    // Red 500
  static const Color warningOrange = Color(0xFFF59E0B); // Amber 500

  static const Color bgLight = Color(0xFFF8FAFC);    // Slate 50
  static const Color surfaceWhite = Colors.white;
  
  // Dark Mode Colors
  static const Color bgDark = Color(0xFF0F172A);      // Slate 900
  static const Color surfaceDark = Color(0xFF1E293B); // Slate 800

  // --- Ombres Custom ---
  static List<BoxShadow> softShadow = [
    BoxShadow(
      color: Colors.black.withOpacity(0.03),
      blurRadius: 20,
      offset: const Offset(0, 10),
    ),
  ];

  static List<BoxShadow> premiumShadow = [
    BoxShadow(
      color: accentBlue.withOpacity(0.1),
      blurRadius: 30,
      offset: const Offset(0, 15),
    ),
  ];

  static final ThemeData lightTheme = ThemeData(
    useMaterial3: true,
    brightness: Brightness.light,
    colorScheme: ColorScheme.fromSeed(
      seedColor: accentBlue,
      primary: primaryBlue,
      secondary: accentBlue,
      surface: surfaceWhite,
      background: bgLight,
    ),
    scaffoldBackgroundColor: bgLight,
    fontFamily: 'Inter', // Assurez-vous d'avoir Inter ou utilisez la police système
    
    appBarTheme: const AppBarTheme(
      backgroundColor: Colors.transparent,
      elevation: 0,
      centerTitle: false,
      iconTheme: IconThemeData(color: primaryBlue),
      titleTextStyle: TextStyle(
        color: primaryBlue,
        fontSize: 24,
        fontWeight: FontWeight.w800,
        letterSpacing: -0.5,
      ),
    ),

    cardTheme: const CardThemeData(
      color: surfaceWhite,
      elevation: 0,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.all(Radius.circular(24))),
    ),

    inputDecorationTheme: InputDecorationTheme(
      filled: true,
      fillColor: surfaceWhite,
      contentPadding: const EdgeInsets.symmetric(horizontal: 24, vertical: 20),
      border: OutlineInputBorder(
        borderRadius: BorderRadius.circular(20),
        borderSide: BorderSide.none,
      ),
      enabledBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(20),
        borderSide: BorderSide.none,
      ),
      focusedBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(20),
        borderSide: const BorderSide(color: accentBlue, width: 1.5),
      ),
      labelStyle: TextStyle(color: primaryBlue.withOpacity(0.5), fontWeight: FontWeight.w500),
      hintStyle: TextStyle(color: primaryBlue.withOpacity(0.3)),
    ),

    elevatedButtonTheme: ElevatedButtonThemeData(
      style: ElevatedButton.styleFrom(
        backgroundColor: primaryBlue,
        foregroundColor: Colors.white,
        minimumSize: const Size(double.infinity, 64),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
        elevation: 0,
        textStyle: const TextStyle(fontSize: 16, fontWeight: FontWeight.w700, letterSpacing: 0.2),
      ),
    ),
  );

  static final ThemeData darkTheme = ThemeData(
    useMaterial3: true,
    brightness: Brightness.dark,
    colorScheme: ColorScheme.fromSeed(
      seedColor: accentBlue,
      brightness: Brightness.dark,
      primary: accentBlue,
      secondary: accentBlue,
      surface: surfaceDark,
      background: bgDark,
    ),
    scaffoldBackgroundColor: bgDark,
    fontFamily: 'Inter',
    
    appBarTheme: const AppBarTheme(
      backgroundColor: Colors.transparent,
      elevation: 0,
      centerTitle: false,
      iconTheme: IconThemeData(color: Colors.white),
      titleTextStyle: TextStyle(
        color: Colors.white,
        fontSize: 24,
        fontWeight: FontWeight.w800,
        letterSpacing: -0.5,
      ),
    ),

    cardTheme: const CardThemeData(
      color: surfaceDark,
      elevation: 0,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.all(Radius.circular(24))),
    ),

    inputDecorationTheme: InputDecorationTheme(
      filled: true,
      fillColor: surfaceDark,
      contentPadding: const EdgeInsets.symmetric(horizontal: 24, vertical: 20),
      border: OutlineInputBorder(
        borderRadius: BorderRadius.circular(20),
        borderSide: BorderSide.none,
      ),
      enabledBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(20),
        borderSide: BorderSide.none,
      ),
      focusedBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(20),
        borderSide: const BorderSide(color: accentBlue, width: 1.5),
      ),
      labelStyle: const TextStyle(color: Colors.white60, fontWeight: FontWeight.w500),
    ),

    elevatedButtonTheme: ElevatedButtonThemeData(
      style: ElevatedButton.styleFrom(
        backgroundColor: accentBlue,
        foregroundColor: Colors.white,
        minimumSize: const Size(double.infinity, 64),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
        elevation: 0,
        textStyle: const TextStyle(fontSize: 16, fontWeight: FontWeight.w700, letterSpacing: 0.2),
      ),
    ),
  );
}
