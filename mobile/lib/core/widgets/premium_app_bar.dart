import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../theme/app_colors.dart';
import '../theme/app_spacing.dart';
import '../theme/app_shadows.dart';
import '../theme/app_text_styles.dart';

enum PremiumAppBarType { standard, transparent, glass, gradient, floating }

class PremiumAppBar extends StatelessWidget implements PreferredSizeWidget {
  final String? title;
  final List<Widget>? actions;
  final Widget? leading;
  final bool automaticallyImplyLeading;
  final bool centerTitle;
  final PreferredSizeWidget? bottom;
  final double? elevation;
  final PremiumAppBarType type;
  final Color? backgroundColor;
  final Color? foregroundColor;
  final Widget? flexibleSpace;
  final double? toolbarHeight;
  final bool showBackButton;
  final VoidCallback? onBackPressed;
  final Widget? titleWidget;
  final bool showNotificationBadge;
  final int notificationCount;

  const PremiumAppBar({
    super.key,
    this.title,
    this.actions,
    this.leading,
    this.automaticallyImplyLeading = true,
    this.centerTitle = false,
    this.bottom,
    this.elevation,
    this.type = PremiumAppBarType.standard,
    this.backgroundColor,
    this.foregroundColor,
    this.flexibleSpace,
    this.toolbarHeight,
    this.showBackButton = true,
    this.onBackPressed,
    this.titleWidget,
    this.showNotificationBadge = false,
    this.notificationCount = 0,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;

    final appBarStyle = _getAppBarStyle(context, isDark);

    Widget appBar = AppBar(
      title:
          titleWidget ??
          (title != null ? Text(title!, style: appBarStyle.titleStyle) : null),
      actions: _buildActions(context, appBarStyle),
      leading: leading ?? _buildLeading(context, appBarStyle),
      automaticallyImplyLeading: automaticallyImplyLeading,
      centerTitle: centerTitle,
      bottom: bottom,
      elevation:
          elevation ?? (type == PremiumAppBarType.transparent ? 0 : null),
      backgroundColor: appBarStyle.backgroundColor,
      foregroundColor: appBarStyle.foregroundColor,
      flexibleSpace: flexibleSpace,
      toolbarHeight: toolbarHeight,
      systemOverlayStyle: appBarStyle.systemOverlayStyle,
      surfaceTintColor: Colors.transparent,
      shape: appBarStyle.shape,
    );

    if (type == PremiumAppBarType.floating) {
      appBar = Container(
        margin: const EdgeInsets.all(AppSpacing.md),
        decoration: BoxDecoration(
          color: appBarStyle.backgroundColor,
          borderRadius: BorderRadius.circular(AppSpacing.radiusXXLarge),
          boxShadow: AppShadows.floating,
        ),
        child: ClipRRect(
          borderRadius: BorderRadius.circular(AppSpacing.radiusXXLarge),
          child: appBar,
        ),
      );
    }

    return appBar;
  }

  Widget? _buildLeading(BuildContext context, _AppBarStyle appBarStyle) {
    if (!showBackButton) return null;

    final navigator = Navigator.of(context);
    final canPop = navigator.canPop();

    if (!canPop && !automaticallyImplyLeading) return null;

    return Container(
      margin: const EdgeInsets.all(AppSpacing.xs),
      decoration: BoxDecoration(
        color: Colors.transparent,
        borderRadius: BorderRadius.circular(AppSpacing.radiusMedium),
      ),
      child: InkWell(
        onTap:
            onBackPressed ??
            () {
              if (canPop) {
                navigator.pop();
              }
            },
        borderRadius: BorderRadius.circular(AppSpacing.radiusMedium),
        child: Container(
          padding: const EdgeInsets.all(AppSpacing.sm),
          decoration: BoxDecoration(
            color: appBarStyle.iconBackgroundColor,
            borderRadius: BorderRadius.circular(AppSpacing.radiusMedium),
          ),
          child: Icon(
            Icons.arrow_back_ios_new_rounded,
            color: appBarStyle.iconColor,
            size: 20,
          ),
        ),
      ),
    );
  }

  List<Widget>? _buildActions(BuildContext context, _AppBarStyle appBarStyle) {
    if (actions == null) return null;

    return actions!.map((action) {
      if (action is _NotificationIconButton) {
        return _NotificationIconButton(
          icon: action.icon,
          showBadge: action.showBadge,
          count: action.count,
          onTap: action.onTap,
          iconColor: appBarStyle.iconColor,
          iconBackgroundColor: appBarStyle.iconBackgroundColor,
        );
      }
      return action;
    }).toList();
  }

  _AppBarStyle _getAppBarStyle(BuildContext context, bool isDark) {
    final theme = Theme.of(context);

    switch (type) {
      case PremiumAppBarType.standard:
        return _AppBarStyle(
          backgroundColor: backgroundColor ?? Colors.transparent,
          foregroundColor: foregroundColor ?? theme.colorScheme.onSurface,
          titleStyle: AppTextStyles.headlineSmall.copyWith(
            color: foregroundColor ?? theme.colorScheme.onSurface,
            fontWeight: FontWeight.w700,
          ),
          iconColor: foregroundColor ?? theme.colorScheme.onSurface,
          iconBackgroundColor: Colors.transparent,
          systemOverlayStyle: SystemUiOverlayStyle.dark,
          shape: null,
        );

      case PremiumAppBarType.transparent:
        return _AppBarStyle(
          backgroundColor: backgroundColor ?? Colors.transparent,
          foregroundColor: foregroundColor ?? theme.colorScheme.onSurface,
          titleStyle: AppTextStyles.headlineSmall.copyWith(
            color: foregroundColor ?? theme.colorScheme.onSurface,
            fontWeight: FontWeight.w700,
          ),
          iconColor: foregroundColor ?? theme.colorScheme.onSurface,
          iconBackgroundColor: Colors.transparent,
          systemOverlayStyle: SystemUiOverlayStyle.dark,
          shape: null,
        );

      case PremiumAppBarType.glass:
        return _AppBarStyle(
          backgroundColor: AppColors.glass,
          foregroundColor: foregroundColor ?? theme.colorScheme.onSurface,
          titleStyle: AppTextStyles.headlineSmall.copyWith(
            color: foregroundColor ?? theme.colorScheme.onSurface,
            fontWeight: FontWeight.w700,
          ),
          iconColor: foregroundColor ?? theme.colorScheme.onSurface,
          iconBackgroundColor: AppColors.glass,
          systemOverlayStyle: SystemUiOverlayStyle.dark,
          shape: null,
        );

      case PremiumAppBarType.gradient:
        return _AppBarStyle(
          backgroundColor: backgroundColor ?? AppColors.primary,
          foregroundColor: foregroundColor ?? AppColors.textOnPrimary,
          titleStyle: AppTextStyles.headlineSmall.copyWith(
            color: foregroundColor ?? AppColors.textOnPrimary,
            fontWeight: FontWeight.w700,
          ),
          iconColor: foregroundColor ?? AppColors.textOnPrimary,
          iconBackgroundColor: Colors.white.withOpacity(0.2),
          systemOverlayStyle: SystemUiOverlayStyle.light,
          shape: null,
        );

      case PremiumAppBarType.floating:
        return _AppBarStyle(
          backgroundColor:
              backgroundColor ??
              (isDark ? AppColors.darkSurface : AppColors.surface),
          foregroundColor: foregroundColor ?? theme.colorScheme.onSurface,
          titleStyle: AppTextStyles.headlineSmall.copyWith(
            color: foregroundColor ?? theme.colorScheme.onSurface,
            fontWeight: FontWeight.w700,
          ),
          iconColor: foregroundColor ?? theme.colorScheme.onSurface,
          iconBackgroundColor: Colors.transparent,
          systemOverlayStyle: isDark
              ? SystemUiOverlayStyle.light
              : SystemUiOverlayStyle.dark,
          shape: null,
        );
    }
  }

  @override
  Size get preferredSize => Size.fromHeight(toolbarHeight ?? kToolbarHeight);
}

class _AppBarStyle {
  final Color backgroundColor;
  final Color foregroundColor;
  final TextStyle titleStyle;
  final Color iconColor;
  final Color iconBackgroundColor;
  final SystemUiOverlayStyle systemOverlayStyle;
  final ShapeBorder? shape;

  _AppBarStyle({
    required this.backgroundColor,
    required this.foregroundColor,
    required this.titleStyle,
    required this.iconColor,
    required this.iconBackgroundColor,
    required this.systemOverlayStyle,
    this.shape,
  });
}

class NotificationIconButton extends StatelessWidget {
  final IconData icon;
  final bool showBadge;
  final int count;
  final VoidCallback? onTap;

  const NotificationIconButton({
    super.key,
    this.icon = Icons.notifications_rounded,
    this.showBadge = false,
    this.count = 0,
    this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return _NotificationIconButton(
      icon: icon,
      showBadge: showBadge,
      count: count,
      onTap: onTap,
      iconColor: Theme.of(context).colorScheme.onSurface,
      iconBackgroundColor: Colors.transparent,
    );
  }
}

class _NotificationIconButton extends StatelessWidget {
  final IconData icon;
  final bool showBadge;
  final int count;
  final VoidCallback? onTap;
  final Color iconColor;
  final Color iconBackgroundColor;

  const _NotificationIconButton({
    required this.icon,
    required this.showBadge,
    required this.count,
    this.onTap,
    required this.iconColor,
    required this.iconBackgroundColor,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.all(AppSpacing.xs),
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(AppSpacing.radiusMedium),
        child: Container(
          padding: const EdgeInsets.all(AppSpacing.sm),
          decoration: BoxDecoration(
            color: iconBackgroundColor,
            borderRadius: BorderRadius.circular(AppSpacing.radiusMedium),
          ),
          child: Stack(
            children: [
              Icon(icon, color: iconColor, size: 24),
              if (showBadge && count > 0)
                Positioned(
                  right: 0,
                  top: 0,
                  child: Container(
                    width: 16,
                    height: 16,
                    decoration: BoxDecoration(
                      color: AppColors.error,
                      borderRadius: BorderRadius.circular(
                        AppSpacing.radiusFull,
                      ),
                      border: Border.all(color: iconBackgroundColor, width: 2),
                    ),
                    child: Center(
                      child: Text(
                        count > 99 ? '99+' : count.toString(),
                        style: AppTextStyles.labelSmall.copyWith(
                          color: AppColors.textOnPrimary,
                          fontSize: 10,
                          fontWeight: FontWeight.w700,
                        ),
                      ),
                    ),
                  ),
                ),
            ],
          ),
        ),
      ),
    );
  }
}
