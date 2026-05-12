import 'package:flutter/material.dart';
import '../theme/app_colors.dart';
import '../theme/app_spacing.dart';
import '../theme/app_shadows.dart';
import '../theme/app_text_styles.dart';

enum PremiumButtonType {
  primary,
  secondary,
  outline,
  ghost,
  success,
  warning,
  error,
}

enum PremiumButtonSize { small, medium, large }

class PremiumButton extends StatelessWidget {
  final String text;
  final VoidCallback? onPressed;
  final PremiumButtonType type;
  final PremiumButtonSize size;
  final bool isLoading;
  final bool isFullWidth;
  final Widget? icon;
  final Widget? child;
  final Color? customColor;
  final double? customWidth;
  final double? customHeight;

  const PremiumButton({
    super.key,
    required this.text,
    this.onPressed,
    this.type = PremiumButtonType.primary,
    this.size = PremiumButtonSize.medium,
    this.isLoading = false,
    this.isFullWidth = false,
    this.icon,
    this.child,
    this.customColor,
    this.customWidth,
    this.customHeight,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;

    // Get colors based on type
    final colors = _getButtonColors(isDark);
    final dimensions = _getDimensions();

    return SizedBox(
      width: isFullWidth ? double.infinity : customWidth,
      height: customHeight ?? dimensions.height,
      child: Material(
        color: colors.backgroundColor,
        borderRadius: BorderRadius.circular(dimensions.radius),
        elevation: 0,
        child: InkWell(
          onTap: isLoading ? null : onPressed,
          borderRadius: BorderRadius.circular(dimensions.radius),
          splashColor: colors.splashColor,
          highlightColor: colors.highlightColor,
          child: Container(
            decoration: BoxDecoration(
              color: colors.backgroundColor,
              borderRadius: BorderRadius.circular(dimensions.radius),
              border: colors.border,
              boxShadow: colors.shadow,
            ),
            child: Center(
              child: isLoading
                  ? _buildLoadingSpinner(colors.textColor)
                  : child ?? _buildButtonContent(colors.textColor, dimensions),
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildButtonContent(Color textColor, _ButtonDimensions dimensions) {
    if (icon != null) {
      return Row(
        mainAxisSize: MainAxisSize.min,
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          icon!,
          SizedBox(width: AppSpacing.sm),
          Text(
            text,
            style: dimensions.textStyle.copyWith(color: textColor),
            textAlign: TextAlign.center,
          ),
        ],
      );
    }

    return Text(
      text,
      style: dimensions.textStyle.copyWith(color: textColor),
      textAlign: TextAlign.center,
    );
  }

  Widget _buildLoadingSpinner(Color color) {
    return SizedBox(
      width: 20,
      height: 20,
      child: CircularProgressIndicator(
        strokeWidth: 2,
        valueColor: AlwaysStoppedAnimation<Color>(color),
      ),
    );
  }

  _ButtonColors _getButtonColors(bool isDark) {
    switch (type) {
      case PremiumButtonType.primary:
        return _ButtonColors(
          backgroundColor: customColor ?? AppColors.primary,
          textColor: AppColors.textOnPrimary,
          splashColor: AppColors.textOnPrimary.withOpacity(0.1),
          highlightColor: AppColors.textOnPrimary.withOpacity(0.05),
          shadow: AppShadows.button,
          border: null,
        );

      case PremiumButtonType.secondary:
        return _ButtonColors(
          backgroundColor: customColor ?? AppColors.secondary,
          textColor: AppColors.textOnPrimary,
          splashColor: AppColors.textOnPrimary.withOpacity(0.1),
          highlightColor: AppColors.textOnPrimary.withOpacity(0.05),
          shadow: AppShadows.premium,
          border: null,
        );

      case PremiumButtonType.outline:
        return _ButtonColors(
          backgroundColor: Colors.transparent,
          textColor: customColor ?? AppColors.primary,
          splashColor: (customColor ?? AppColors.primary).withOpacity(0.1),
          highlightColor: (customColor ?? AppColors.primary).withOpacity(0.05),
          shadow: null,
          border: Border.all(
            color: customColor ?? AppColors.primary,
            width: AppSpacing.strokeNormal,
          ),
        );

      case PremiumButtonType.ghost:
        return _ButtonColors(
          backgroundColor: Colors.transparent,
          textColor: customColor ?? AppColors.textPrimary,
          splashColor: (customColor ?? AppColors.textPrimary).withOpacity(0.1),
          highlightColor: (customColor ?? AppColors.textPrimary).withOpacity(
            0.05,
          ),
          shadow: null,
          border: null,
        );

      case PremiumButtonType.success:
        return _ButtonColors(
          backgroundColor: AppColors.success,
          textColor: AppColors.textOnPrimary,
          splashColor: AppColors.textOnPrimary.withOpacity(0.1),
          highlightColor: AppColors.textOnPrimary.withOpacity(0.05),
          shadow: AppShadows.success,
          border: null,
        );

      case PremiumButtonType.warning:
        return _ButtonColors(
          backgroundColor: AppColors.warning,
          textColor: AppColors.textOnPrimary,
          splashColor: AppColors.textOnPrimary.withOpacity(0.1),
          highlightColor: AppColors.textOnPrimary.withOpacity(0.05),
          shadow: AppShadows.warning,
          border: null,
        );

      case PremiumButtonType.error:
        return _ButtonColors(
          backgroundColor: AppColors.error,
          textColor: AppColors.textOnPrimary,
          splashColor: AppColors.textOnPrimary.withOpacity(0.1),
          highlightColor: AppColors.textOnPrimary.withOpacity(0.05),
          shadow: AppShadows.error,
          border: null,
        );
    }
  }

  _ButtonDimensions _getDimensions() {
    switch (size) {
      case PremiumButtonSize.small:
        return _ButtonDimensions(
          height: AppSpacing.buttonHeightSmall,
          radius: AppSpacing.radiusMedium,
          textStyle: AppTextStyles.buttonText.copyWith(
            fontSize: 14,
            fontWeight: FontWeight.w600,
          ),
        );

      case PremiumButtonSize.medium:
        return _ButtonDimensions(
          height: AppSpacing.buttonHeight,
          radius: AppSpacing.radiusLarge,
          textStyle: AppTextStyles.buttonText,
        );

      case PremiumButtonSize.large:
        return _ButtonDimensions(
          height: AppSpacing.buttonHeightLarge,
          radius: AppSpacing.radiusXLarge,
          textStyle: AppTextStyles.buttonText.copyWith(
            fontSize: 18,
            fontWeight: FontWeight.w800,
          ),
        );
    }
  }
}

class _ButtonColors {
  final Color backgroundColor;
  final Color textColor;
  final Color splashColor;
  final Color highlightColor;
  final List<BoxShadow>? shadow;
  final BoxBorder? border;

  _ButtonColors({
    required this.backgroundColor,
    required this.textColor,
    required this.splashColor,
    required this.highlightColor,
    this.shadow,
    this.border,
  });
}

class _ButtonDimensions {
  final double height;
  final double radius;
  final TextStyle textStyle;

  _ButtonDimensions({
    required this.height,
    required this.radius,
    required this.textStyle,
  });
}
