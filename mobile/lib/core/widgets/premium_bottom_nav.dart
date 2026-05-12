import 'package:flutter/material.dart';
import '../theme/app_colors.dart';
import '../theme/app_spacing.dart';
import '../theme/app_shadows.dart';
import '../theme/app_text_styles.dart';

class PremiumBottomNav extends StatelessWidget {
  final int currentIndex;
  final ValueChanged<int> onTap;
  final List<PremiumNavItem> items;
  final bool isFloating;
  final Color? backgroundColor;
  final Color? selectedItemColor;
  final Color? unselectedItemColor;

  const PremiumBottomNav({
    super.key,
    required this.currentIndex,
    required this.onTap,
    required this.items,
    this.isFloating = false,
    this.backgroundColor,
    this.selectedItemColor,
    this.unselectedItemColor,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;

    if (isFloating) {
      return _buildFloatingNav(context, isDark);
    }

    return _buildStandardNav(context, isDark);
  }

  Widget _buildStandardNav(BuildContext context, bool isDark) {
    return Container(
      decoration: BoxDecoration(
        color:
            backgroundColor ??
            (isDark ? AppColors.darkSurface : AppColors.surface),
        boxShadow: AppShadows.soft,
      ),
      child: SafeArea(
        child: Padding(
          padding: const EdgeInsets.symmetric(
            horizontal: AppSpacing.md,
            vertical: AppSpacing.xs,
          ),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceAround,
            children: items.asMap().entries.map((entry) {
              final index = entry.key;
              final item = entry.value;
              final isSelected = index == currentIndex;

              return Expanded(
                child: _buildNavItem(
                  context,
                  item,
                  isSelected,
                  isDark,
                  () => onTap(index),
                ),
              );
            }).toList(),
          ),
        ),
      ),
    );
  }

  Widget _buildFloatingNav(BuildContext context, bool isDark) {
    return Container(
      margin: const EdgeInsets.all(AppSpacing.lg),
      decoration: BoxDecoration(
        color:
            backgroundColor ??
            (isDark ? AppColors.darkSurface : AppColors.surface),
        borderRadius: BorderRadius.circular(AppSpacing.radiusXXLarge),
        boxShadow: AppShadows.floating,
      ),
      child: SafeArea(
        child: Padding(
          padding: const EdgeInsets.symmetric(
            horizontal: AppSpacing.md,
            vertical: AppSpacing.sm,
          ),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceAround,
            children: items.asMap().entries.map((entry) {
              final index = entry.key;
              final item = entry.value;
              final isSelected = index == currentIndex;

              return Expanded(
                child: _buildNavItem(
                  context,
                  item,
                  isSelected,
                  isDark,
                  () => onTap(index),
                ),
              );
            }).toList(),
          ),
        ),
      ),
    );
  }

  Widget _buildNavItem(
    BuildContext context,
    PremiumNavItem item,
    bool isSelected,
    bool isDark,
    VoidCallback onTap,
  ) {
    final selectedColor = selectedItemColor ?? AppColors.primary;
    final unselectedColor =
        unselectedItemColor ??
        (isDark ? AppColors.darkTextTertiary : AppColors.textTertiary);

    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(AppSpacing.radiusLarge),
      child: Container(
        padding: const EdgeInsets.symmetric(
          vertical: AppSpacing.sm,
          horizontal: AppSpacing.xs,
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            AnimatedContainer(
              duration: const Duration(milliseconds: 200),
              padding: isSelected
                  ? const EdgeInsets.all(AppSpacing.sm)
                  : EdgeInsets.zero,
              decoration: BoxDecoration(
                color: isSelected
                    ? selectedColor.withValues(alpha: 0.1)
                    : Colors.transparent,
                borderRadius: BorderRadius.circular(AppSpacing.radiusMedium),
              ),
              child: Icon(
                item.icon,
                size: 24,
                color: isSelected ? selectedColor : unselectedColor,
              ),
            ),
            const SizedBox(height: AppSpacing.xs),
            AnimatedDefaultTextStyle(
              duration: const Duration(milliseconds: 200),
              style: isSelected
                  ? AppTextStyles.navigationActive.copyWith(
                      color: selectedColor,
                      fontWeight: FontWeight.w700,
                    )
                  : AppTextStyles.navigationLabel.copyWith(
                      color: unselectedColor,
                      fontWeight: FontWeight.w500,
                    ),
              child: Text(
                item.label,
                textAlign: TextAlign.center,
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class PremiumNavItem {
  final IconData icon;
  final String label;
  final String? badge;

  const PremiumNavItem({required this.icon, required this.label, this.badge});
}

class PremiumBottomNavWithFab extends StatelessWidget {
  final int currentIndex;
  final ValueChanged<int> onTap;
  final List<PremiumNavItem> items;
  final Widget fab;
  final FloatingActionButtonLocation fabLocation;
  final Color? backgroundColor;
  final Color? selectedItemColor;
  final Color? unselectedItemColor;

  const PremiumBottomNavWithFab({
    super.key,
    required this.currentIndex,
    required this.onTap,
    required this.items,
    required this.fab,
    this.fabLocation = FloatingActionButtonLocation.centerDocked,
    this.backgroundColor,
    this.selectedItemColor,
    this.unselectedItemColor,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;

    return Container(
      decoration: BoxDecoration(
        color:
            backgroundColor ??
            (isDark ? AppColors.darkSurface : AppColors.surface),
        boxShadow: AppShadows.soft,
      ),
      child: SafeArea(
        child: Row(
          children: [
            // Left side nav items
            Expanded(
              flex: 2,
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                children: items.take(2).toList().asMap().entries.map((entry) {
                  final index = entry.key;
                  final item = entry.value;
                  final isSelected = index == currentIndex;

                  return Expanded(
                    child: _buildNavItem(
                      context,
                      item,
                      isSelected,
                      isDark,
                      () => onTap(index),
                    ),
                  );
                }).toList(),
              ),
            ),

            // FAB space
            const SizedBox(width: 56),

            // Right side nav items
            Expanded(
              flex: 2,
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                children: items.skip(2).take(2).toList().asMap().entries.map((
                  entry,
                ) {
                  final index = entry.key + 2;
                  final item = entry.value;
                  final isSelected = index == currentIndex;

                  return Expanded(
                    child: _buildNavItem(
                      context,
                      item,
                      isSelected,
                      isDark,
                      () => onTap(index),
                    ),
                  );
                }).toList(),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildNavItem(
    BuildContext context,
    PremiumNavItem item,
    bool isSelected,
    bool isDark,
    VoidCallback onTap,
  ) {
    final selectedColor = selectedItemColor ?? AppColors.primary;
    final unselectedColor =
        unselectedItemColor ??
        (isDark ? AppColors.darkTextTertiary : AppColors.textTertiary);

    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(AppSpacing.radiusLarge),
      child: Container(
        padding: const EdgeInsets.symmetric(
          vertical: AppSpacing.sm,
          horizontal: AppSpacing.xs,
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            AnimatedContainer(
              duration: const Duration(milliseconds: 200),
              padding: isSelected
                  ? const EdgeInsets.all(AppSpacing.sm)
                  : EdgeInsets.zero,
              decoration: BoxDecoration(
                color: isSelected
                    ? selectedColor.withValues(alpha: 0.1)
                    : Colors.transparent,
                borderRadius: BorderRadius.circular(AppSpacing.radiusMedium),
              ),
              child: Icon(
                item.icon,
                size: 24,
                color: isSelected ? selectedColor : unselectedColor,
              ),
            ),
            const SizedBox(height: AppSpacing.xs),
            AnimatedDefaultTextStyle(
              duration: const Duration(milliseconds: 200),
              style: isSelected
                  ? AppTextStyles.navigationActive.copyWith(
                      color: selectedColor,
                      fontWeight: FontWeight.w700,
                    )
                  : AppTextStyles.navigationLabel.copyWith(
                      color: unselectedColor,
                      fontWeight: FontWeight.w500,
                    ),
              child: Text(
                item.label,
                textAlign: TextAlign.center,
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
