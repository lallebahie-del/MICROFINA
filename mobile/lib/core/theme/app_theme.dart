import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:google_fonts/google_fonts.dart';
import 'app_colors.dart';
import 'app_text_styles.dart';
import 'app_spacing.dart';
import 'app_shadows.dart';

class AppTheme {
  // --- Light Theme ---
  static ThemeData get lightTheme {
    return ThemeData(
      useMaterial3: true,
      brightness: Brightness.light,

      // Color Scheme
      colorScheme: ColorScheme.light(
        primary: AppColors.primary,
        onPrimary: AppColors.textOnPrimary,
        primaryContainer: AppColors.surfaceVariant,
        onPrimaryContainer: AppColors.textPrimary,
        secondary: AppColors.secondary,
        onSecondary: AppColors.textOnPrimary,
        secondaryContainer: AppColors.surfaceVariant,
        onSecondaryContainer: AppColors.textPrimary,
        tertiary: AppColors.success,
        onTertiary: AppColors.textOnPrimary,
        tertiaryContainer: AppColors.successLight,
        onTertiaryContainer: AppColors.textPrimary,
        error: AppColors.error,
        onError: AppColors.textOnPrimary,
        errorContainer: AppColors.errorLight,
        onErrorContainer: AppColors.textPrimary,
        surface: AppColors.surface,
        onSurface: AppColors.textPrimary,
        onSurfaceVariant: AppColors.textSecondary,
        outline: AppColors.border,
        outlineVariant: AppColors.borderLight,
        surfaceTint: AppColors.secondary,
        inverseSurface: AppColors.primary,
        onInverseSurface: AppColors.textOnPrimary,
        inversePrimary: AppColors.secondary,
        scrim: Colors.black,
        shadow: AppColors.shadow,
      ),

      // Text Theme
      textTheme: _buildTextTheme(AppColors.textPrimary),

      // App Bar Theme
      appBarTheme: AppBarTheme(
        backgroundColor: Colors.transparent,
        elevation: 0,
        scrolledUnderElevation: 0,
        centerTitle: false,
        titleTextStyle: AppTextStyles.headlineSmall.copyWith(
          color: AppColors.textPrimary,
          fontWeight: FontWeight.w700,
        ),
        systemOverlayStyle: SystemUiOverlayStyle.dark,
        iconTheme: const IconThemeData(color: AppColors.textPrimary, size: 24),
        actionsIconTheme: const IconThemeData(
          color: AppColors.textPrimary,
          size: 24,
        ),
      ),

      // Card Theme
      cardTheme: CardThemeData(
        color: AppColors.surface,
        elevation: 0,
        shadowColor: AppColors.shadow,
        surfaceTintColor: AppColors.secondary,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(AppSpacing.radiusLarge),
        ),
        margin: EdgeInsets.zero,
      ),

      // Elevated Button Theme
      elevatedButtonTheme: ElevatedButtonThemeData(
        style: ElevatedButton.styleFrom(
          backgroundColor: AppColors.primary,
          foregroundColor: AppColors.textOnPrimary,
          elevation: 0,
          shadowColor: Colors.transparent,
          surfaceTintColor: Colors.transparent,
          minimumSize: const Size(double.infinity, AppSpacing.buttonHeight),
          maximumSize: const Size(double.infinity, AppSpacing.buttonHeight),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(AppSpacing.radiusLarge),
          ),
          textStyle: AppTextStyles.buttonText,
          padding: const EdgeInsets.symmetric(
            horizontal: AppSpacing.lg,
            vertical: AppSpacing.md,
          ),
        ),
      ),

      // Outlined Button Theme
      outlinedButtonTheme: OutlinedButtonThemeData(
        style: OutlinedButton.styleFrom(
          foregroundColor: AppColors.primary,
          backgroundColor: Colors.transparent,
          elevation: 0,
          shadowColor: Colors.transparent,
          surfaceTintColor: Colors.transparent,
          minimumSize: const Size(double.infinity, AppSpacing.buttonHeight),
          maximumSize: const Size(double.infinity, AppSpacing.buttonHeight),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(AppSpacing.radiusLarge),
            side: const BorderSide(
              color: AppColors.primary,
              width: AppSpacing.strokeNormal,
            ),
          ),
          textStyle: AppTextStyles.buttonText,
          padding: const EdgeInsets.symmetric(
            horizontal: AppSpacing.lg,
            vertical: AppSpacing.md,
          ),
        ),
      ),

      // Text Button Theme
      textButtonTheme: TextButtonThemeData(
        style: TextButton.styleFrom(
          foregroundColor: AppColors.secondary,
          backgroundColor: Colors.transparent,
          elevation: 0,
          shadowColor: Colors.transparent,
          surfaceTintColor: Colors.transparent,
          minimumSize: const Size(0, AppSpacing.buttonHeightSmall),
          maximumSize: const Size(
            double.infinity,
            AppSpacing.buttonHeightSmall,
          ),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(AppSpacing.radiusMedium),
          ),
          textStyle: AppTextStyles.buttonText.copyWith(
            fontSize: 14,
            fontWeight: FontWeight.w600,
          ),
          padding: const EdgeInsets.symmetric(
            horizontal: AppSpacing.md,
            vertical: AppSpacing.sm,
          ),
        ),
      ),

      // Input Decoration Theme
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: AppColors.surface,
        contentPadding: const EdgeInsets.symmetric(
          horizontal: AppSpacing.lg,
          vertical: AppSpacing.inputPadding,
        ),
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(AppSpacing.radiusLarge),
          borderSide: BorderSide.none,
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(AppSpacing.radiusLarge),
          borderSide: const BorderSide(
            color: AppColors.border,
            width: AppSpacing.strokeNormal,
          ),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(AppSpacing.radiusLarge),
          borderSide: const BorderSide(
            color: AppColors.secondary,
            width: AppSpacing.strokeThick,
          ),
        ),
        errorBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(AppSpacing.radiusLarge),
          borderSide: const BorderSide(
            color: AppColors.error,
            width: AppSpacing.strokeNormal,
          ),
        ),
        focusedErrorBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(AppSpacing.radiusLarge),
          borderSide: const BorderSide(
            color: AppColors.error,
            width: AppSpacing.strokeThick,
          ),
        ),
        disabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(AppSpacing.radiusLarge),
          borderSide: BorderSide(
            color: AppColors.border.withOpacity(0.5),
            width: AppSpacing.strokeNormal,
          ),
        ),
        labelStyle: AppTextStyles.inputLabel.copyWith(
          color: AppColors.textSecondary,
        ),
        hintStyle: AppTextStyles.inputHint.copyWith(
          color: AppColors.textTertiary,
        ),
        errorStyle: AppTextStyles.errorText.copyWith(color: AppColors.error),
        helperStyle: AppTextStyles.bodySmall.copyWith(
          color: AppColors.textTertiary,
        ),
        prefixIconColor: AppColors.textSecondary,
        suffixIconColor: AppColors.textSecondary,
        floatingLabelBehavior: FloatingLabelBehavior.auto,
        alignLabelWithHint: true,
      ),

      // Bottom Navigation Bar Theme
      bottomNavigationBarTheme: BottomNavigationBarThemeData(
        backgroundColor: AppColors.surface,
        selectedItemColor: AppColors.primary,
        unselectedItemColor: AppColors.textTertiary,
        selectedLabelStyle: AppTextStyles.navigationActive,
        unselectedLabelStyle: AppTextStyles.navigationLabel,
        type: BottomNavigationBarType.fixed,
        elevation: 10,
        landscapeLayout: BottomNavigationBarLandscapeLayout.centered,
      ),

      // Chip Theme
      chipTheme: ChipThemeData(
        backgroundColor: AppColors.surfaceVariant,
        brightness: Brightness.light,
        labelStyle: AppTextStyles.labelMedium.copyWith(
          color: AppColors.textPrimary,
        ),
        secondaryLabelStyle: AppTextStyles.labelMedium.copyWith(
          color: AppColors.textSecondary,
        ),
        padding: const EdgeInsets.symmetric(
          horizontal: AppSpacing.md,
          vertical: AppSpacing.sm,
        ),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(AppSpacing.radiusFull),
        ),
      ),

      // Dialog Theme
      dialogTheme: DialogThemeData(
        backgroundColor: AppColors.surface,
        elevation: 20,
        shadowColor: AppColors.shadow,
        surfaceTintColor: AppColors.secondary,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(AppSpacing.radiusXXLarge),
        ),
        titleTextStyle: AppTextStyles.headlineSmall.copyWith(
          color: AppColors.textPrimary,
        ),
        contentTextStyle: AppTextStyles.bodyMedium.copyWith(
          color: AppColors.textSecondary,
        ),
        actionsPadding: const EdgeInsets.all(AppSpacing.lg),
      ),

      // Snack Bar Theme
      snackBarTheme: SnackBarThemeData(
        backgroundColor: AppColors.primary,
        contentTextStyle: AppTextStyles.bodyMedium.copyWith(
          color: AppColors.textOnPrimary,
        ),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(AppSpacing.radiusMedium),
        ),
        behavior: SnackBarBehavior.floating,
        elevation: 10,
      ),

      // Progress Indicator Theme
      progressIndicatorTheme: const ProgressIndicatorThemeData(
        color: AppColors.secondary,
        linearTrackColor: AppColors.borderLight,
        circularTrackColor: AppColors.borderLight,
      ),

      // Switch Theme
      switchTheme: SwitchThemeData(
        thumbColor: WidgetStateProperty.resolveWith((states) {
          if (states.contains(WidgetState.selected)) {
            return AppColors.secondary;
          }
          return AppColors.surface;
        }),
        trackColor: WidgetStateProperty.resolveWith((states) {
          if (states.contains(WidgetState.selected)) {
            return AppColors.secondary.withOpacity(0.3);
          }
          return AppColors.border;
        }),
      ),

      // Checkbox Theme
      checkboxTheme: CheckboxThemeData(
        fillColor: WidgetStateProperty.resolveWith((states) {
          if (states.contains(WidgetState.selected)) {
            return AppColors.secondary;
          }
          return AppColors.surface;
        }),
        checkColor: WidgetStateProperty.all(AppColors.textOnPrimary),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(AppSpacing.radiusXSmall),
        ),
      ),

      // Radio Theme
      radioTheme: RadioThemeData(
        fillColor: WidgetStateProperty.resolveWith((states) {
          if (states.contains(WidgetState.selected)) {
            return AppColors.secondary;
          }
          return AppColors.textTertiary;
        }),
      ),

      // Extensions
      extensions: [
        AppColorExtensions(
          primary: AppColors.primary,
          secondary: AppColors.secondary,
          success: AppColors.success,
          warning: AppColors.warning,
          error: AppColors.error,
          surface: AppColors.surface,
          background: AppColors.background,
        ),
        AppTextStylesExtensions(
          heroTitle: AppTextStyles.heroTitle,
          cardTitle: AppTextStyles.cardTitle,
          balanceAmount: AppTextStyles.balanceAmount,
          transactionAmount: AppTextStyles.transactionAmount,
        ),
      ],
    );
  }

  // --- Dark Theme ---
  static ThemeData get darkTheme {
    return ThemeData(
      useMaterial3: true,
      brightness: Brightness.dark,

      // Color Scheme
      colorScheme: ColorScheme.dark(
        primary: AppColors.secondary,
        onPrimary: AppColors.textOnPrimary,
        primaryContainer: AppColors.darkSurfaceVariant,
        onPrimaryContainer: AppColors.darkTextPrimary,
        secondary: AppColors.secondary,
        onSecondary: AppColors.textOnPrimary,
        secondaryContainer: AppColors.darkSurfaceVariant,
        onSecondaryContainer: AppColors.darkTextPrimary,
        tertiary: AppColors.success,
        onTertiary: AppColors.textOnPrimary,
        tertiaryContainer: AppColors.success,
        onTertiaryContainer: AppColors.darkTextPrimary,
        error: AppColors.error,
        onError: AppColors.textOnPrimary,
        errorContainer: AppColors.error,
        onErrorContainer: AppColors.darkTextPrimary,
        surface: AppColors.darkSurface,
        onSurface: AppColors.darkTextPrimary,
        onSurfaceVariant: AppColors.darkTextSecondary,
        outline: AppColors.darkBorder,
        outlineVariant: AppColors.darkBorder,
        surfaceTint: AppColors.secondary,
        inverseSurface: AppColors.surface,
        onInverseSurface: AppColors.textPrimary,
        inversePrimary: AppColors.primary,
        scrim: Colors.black,
        shadow: AppColors.shadowHeavy,
      ),

      // Text Theme
      textTheme: _buildTextTheme(AppColors.darkTextPrimary),

      // App Bar Theme
      appBarTheme: AppBarTheme(
        backgroundColor: Colors.transparent,
        elevation: 0,
        scrolledUnderElevation: 0,
        centerTitle: false,
        titleTextStyle: AppTextStyles.headlineSmall.copyWith(
          color: AppColors.darkTextPrimary,
          fontWeight: FontWeight.w700,
        ),
        systemOverlayStyle: SystemUiOverlayStyle.light,
        iconTheme: const IconThemeData(
          color: AppColors.darkTextPrimary,
          size: 24,
        ),
        actionsIconTheme: const IconThemeData(
          color: AppColors.darkTextPrimary,
          size: 24,
        ),
      ),

      // Card Theme
      cardTheme: CardThemeData(
        color: AppColors.darkSurface,
        elevation: 0,
        shadowColor: AppColors.shadowHeavy,
        surfaceTintColor: AppColors.secondary,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(AppSpacing.radiusLarge),
        ),
        margin: EdgeInsets.zero,
      ),

      // Input Decoration Theme
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: AppColors.darkSurfaceVariant,
        contentPadding: const EdgeInsets.symmetric(
          horizontal: AppSpacing.lg,
          vertical: AppSpacing.inputPadding,
        ),
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(AppSpacing.radiusLarge),
          borderSide: BorderSide.none,
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(AppSpacing.radiusLarge),
          borderSide: const BorderSide(
            color: AppColors.darkBorder,
            width: AppSpacing.strokeNormal,
          ),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(AppSpacing.radiusLarge),
          borderSide: const BorderSide(
            color: AppColors.secondary,
            width: AppSpacing.strokeThick,
          ),
        ),
        errorBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(AppSpacing.radiusLarge),
          borderSide: const BorderSide(
            color: AppColors.error,
            width: AppSpacing.strokeNormal,
          ),
        ),
        focusedErrorBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(AppSpacing.radiusLarge),
          borderSide: const BorderSide(
            color: AppColors.error,
            width: AppSpacing.strokeThick,
          ),
        ),
        disabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(AppSpacing.radiusLarge),
          borderSide: BorderSide(
            color: AppColors.darkBorder.withOpacity(0.5),
            width: AppSpacing.strokeNormal,
          ),
        ),
        labelStyle: AppTextStyles.inputLabel.copyWith(
          color: AppColors.darkTextSecondary,
        ),
        hintStyle: AppTextStyles.inputHint.copyWith(
          color: AppColors.darkTextTertiary,
        ),
        errorStyle: AppTextStyles.errorText.copyWith(color: AppColors.error),
        helperStyle: AppTextStyles.bodySmall.copyWith(
          color: AppColors.darkTextTertiary,
        ),
        prefixIconColor: AppColors.darkTextSecondary,
        suffixIconColor: AppColors.darkTextSecondary,
        floatingLabelBehavior: FloatingLabelBehavior.auto,
        alignLabelWithHint: true,
      ),

      // Bottom Navigation Bar Theme
      bottomNavigationBarTheme: BottomNavigationBarThemeData(
        backgroundColor: AppColors.darkSurface,
        selectedItemColor: AppColors.secondary,
        unselectedItemColor: AppColors.darkTextTertiary,
        selectedLabelStyle: AppTextStyles.navigationActive,
        unselectedLabelStyle: AppTextStyles.navigationLabel,
        type: BottomNavigationBarType.fixed,
        elevation: 10,
        landscapeLayout: BottomNavigationBarLandscapeLayout.centered,
      ),

      // Extensions
      extensions: [
        AppColorExtensions(
          primary: AppColors.darkPrimary,
          secondary: AppColors.secondary,
          success: AppColors.success,
          warning: AppColors.warning,
          error: AppColors.error,
          surface: AppColors.darkSurface,
          background: AppColors.darkBackground,
        ),
        AppTextStylesExtensions(
          heroTitle: AppTextStyles.heroTitle.copyWith(
            color: AppColors.darkTextPrimary,
          ),
          cardTitle: AppTextStyles.cardTitle.copyWith(
            color: AppColors.darkTextPrimary,
          ),
          balanceAmount: AppTextStyles.balanceAmount.copyWith(
            color: AppColors.darkTextPrimary,
          ),
          transactionAmount: AppTextStyles.transactionAmount.copyWith(
            color: AppColors.darkTextPrimary,
          ),
        ),
      ],
    );
  }

  // --- Build Text Theme ---
  static TextTheme _buildTextTheme(Color primaryColor) {
    return TextTheme(
      displayLarge: AppTextStyles.displayLarge.copyWith(color: primaryColor),
      displayMedium: AppTextStyles.displayMedium.copyWith(color: primaryColor),
      displaySmall: AppTextStyles.displaySmall.copyWith(color: primaryColor),
      headlineLarge: AppTextStyles.headlineLarge.copyWith(color: primaryColor),
      headlineMedium: AppTextStyles.headlineMedium.copyWith(
        color: primaryColor,
      ),
      headlineSmall: AppTextStyles.headlineSmall.copyWith(color: primaryColor),
      titleLarge: AppTextStyles.titleLarge.copyWith(color: primaryColor),
      titleMedium: AppTextStyles.titleMedium.copyWith(color: primaryColor),
      titleSmall: AppTextStyles.titleSmall.copyWith(color: primaryColor),
      bodyLarge: AppTextStyles.bodyLarge.copyWith(color: primaryColor),
      bodyMedium: AppTextStyles.bodyMedium.copyWith(color: primaryColor),
      bodySmall: AppTextStyles.bodySmall.copyWith(color: primaryColor),
      labelLarge: AppTextStyles.labelLarge.copyWith(color: primaryColor),
      labelMedium: AppTextStyles.labelMedium.copyWith(color: primaryColor),
      labelSmall: AppTextStyles.labelSmall.copyWith(color: primaryColor),
    );
  }
}
