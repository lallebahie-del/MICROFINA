import 'package:flutter/material.dart';
import 'app_colors.dart';

class AppShadows {
  // --- Soft Shadows ---
  static List<BoxShadow> get soft => [
    BoxShadow(
      color: AppColors.shadowLight,
      blurRadius: 10,
      offset: const Offset(0, 4),
    ),
  ];

  static List<BoxShadow> get softMedium => [
    BoxShadow(
      color: AppColors.shadow,
      blurRadius: 20,
      offset: const Offset(0, 8),
    ),
  ];

  static List<BoxShadow> get softLarge => [
    BoxShadow(
      color: AppColors.shadow,
      blurRadius: 30,
      offset: const Offset(0, 12),
    ),
  ];

  // --- Premium Shadows ---
  static List<BoxShadow> get premium => [
    BoxShadow(
      color: AppColors.secondary.withOpacity(0.1),
      blurRadius: 30,
      offset: const Offset(0, 15),
      spreadRadius: -5,
    ),
  ];

  static List<BoxShadow> get premiumColored => [
    BoxShadow(
      color: AppColors.secondary.withOpacity(0.15),
      blurRadius: 40,
      offset: const Offset(0, 20),
      spreadRadius: -10,
    ),
  ];

  // --- Card Shadows ---
  static List<BoxShadow> get card => [
    BoxShadow(
      color: AppColors.shadowLight,
      blurRadius: 15,
      offset: const Offset(0, 5),
    ),
  ];

  static List<BoxShadow> get cardHover => [
    BoxShadow(
      color: AppColors.shadow,
      blurRadius: 25,
      offset: const Offset(0, 10),
    ),
  ];

  static List<BoxShadow> get cardPressed => [
    BoxShadow(
      color: AppColors.shadowLight,
      blurRadius: 8,
      offset: const Offset(0, 2),
    ),
  ];

  // --- Button Shadows ---
  static List<BoxShadow> get button => [
    BoxShadow(
      color: AppColors.primary.withOpacity(0.2),
      blurRadius: 12,
      offset: const Offset(0, 6),
    ),
  ];

  static List<BoxShadow> get buttonHover => [
    BoxShadow(
      color: AppColors.primary.withOpacity(0.3),
      blurRadius: 16,
      offset: const Offset(0, 8),
    ),
  ];

  static List<BoxShadow> get buttonPressed => [
    BoxShadow(
      color: AppColors.primary.withOpacity(0.1),
      blurRadius: 6,
      offset: const Offset(0, 2),
    ),
  ];

  // --- Status Shadows ---
  static List<BoxShadow> get success => [
    BoxShadow(
      color: AppColors.success.withOpacity(0.2),
      blurRadius: 20,
      offset: const Offset(0, 8),
    ),
  ];

  static List<BoxShadow> get warning => [
    BoxShadow(
      color: AppColors.warning.withOpacity(0.2),
      blurRadius: 20,
      offset: const Offset(0, 8),
    ),
  ];

  static List<BoxShadow> get error => [
    BoxShadow(
      color: AppColors.error.withOpacity(0.2),
      blurRadius: 20,
      offset: const Offset(0, 8),
    ),
  ];

  // --- Inner Shadows ---
  static List<BoxShadow> get inner => [
    BoxShadow(
      color: AppColors.shadowLight,
      blurRadius: 10,
      offset: const Offset(0, 2),
    ),
  ];

  // --- Glow Effects ---
  static List<BoxShadow> get glow => [
    BoxShadow(
      color: AppColors.secondary.withOpacity(0.3),
      blurRadius: 30,
      spreadRadius: 5,
      offset: const Offset(0, 0),
    ),
  ];

  static List<BoxShadow> get glowSuccess => [
    BoxShadow(
      color: AppColors.success.withOpacity(0.3),
      blurRadius: 30,
      spreadRadius: 5,
      offset: const Offset(0, 0),
    ),
  ];

  static List<BoxShadow> get glowError => [
    BoxShadow(
      color: AppColors.error.withOpacity(0.3),
      blurRadius: 30,
      spreadRadius: 5,
      offset: const Offset(0, 0),
    ),
  ];

  // --- Floating Shadows ---
  static List<BoxShadow> get floating => [
    BoxShadow(
      color: AppColors.shadow,
      blurRadius: 40,
      offset: const Offset(0, 20),
    ),
  ];

  static List<BoxShadow> get floatingLarge => [
    BoxShadow(
      color: AppColors.shadowHeavy,
      blurRadius: 60,
      offset: const Offset(0, 30),
    ),
  ];

  // --- No Shadow ---
  static List<BoxShadow> get none => [];

  // --- Custom Shadow Builder ---
  static BoxShadow custom({
    required Color color,
    double blurRadius = 10,
    double spreadRadius = 0,
    Offset offset = Offset.zero,
  }) {
    return BoxShadow(
      color: color,
      blurRadius: blurRadius,
      spreadRadius: spreadRadius,
      offset: offset,
    );
  }
}
