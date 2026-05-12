import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import '../../../core/widgets/premium_bottom_nav.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../core/theme/app_shadows.dart';
import '../../../core/router/app_router.dart';

class MainNavigationWrapper extends StatefulWidget {
  final Widget child;
  final bool showFab;
  final Widget? fab;
  final FloatingActionButtonLocation? fabLocation;

  const MainNavigationWrapper({
    super.key,
    required this.child,
    this.showFab = false,
    this.fab,
    this.fabLocation,
  });

  @override
  State<MainNavigationWrapper> createState() => _MainNavigationWrapperState();
}

class _MainNavigationWrapperState extends State<MainNavigationWrapper> {
  int _currentIndex = 0;

  final List<PremiumNavItem> _navItems = [
    const PremiumNavItem(
      icon: Icons.home_rounded,
      label: 'Accueil',
    ),
    const PremiumNavItem(
      icon: Icons.account_balance_rounded,
      label: 'Comptes',
    ),
    const PremiumNavItem(
      icon: Icons.send_rounded,
      label: 'Transfert',
    ),
    const PremiumNavItem(
      icon: Icons.history_rounded,
      label: 'Historique',
    ),
    const PremiumNavItem(
      icon: Icons.person_rounded,
      label: 'Profil',
    ),
  ];

  @override
  Widget build(BuildContext context) {
    final router = GoRouter.of(context);
    final location = router.routeInformationProvider.value.uri.path;

    // Update current index based on route
    _updateCurrentIndex(location);

    return Scaffold(
      body: widget.child,
      floatingActionButton: widget.showFab ? widget.fab : null,
      floatingActionButtonLocation: widget.fabLocation,
      bottomNavigationBar: widget.showFab
          ? PremiumBottomNavWithFab(
              currentIndex: _currentIndex,
              onTap: _onNavItemTapped,
              items: _navItems,
              fab: widget.fab!,
            )
          : PremiumBottomNav(
              currentIndex: _currentIndex,
              onTap: _onNavItemTapped,
              items: _navItems,
              isFloating: true,
            ),
    );
  }

  void _updateCurrentIndex(String location) {
    int newIndex = 0;
    
    if (location.contains('/dashboard') || location == '/') {
      newIndex = 0;
    } else if (location.contains(AppRouter.accounts)) {
      newIndex = 1;
    } else if (location.contains('/transfer') || location.contains('/scan') || location.contains('/pay')) {
      newIndex = 2;
    } else if (location.contains(AppRouter.transactions)) {
      newIndex = 3;
    } else if (location.contains('/profile')) {
      newIndex = 4;
    }

    if (newIndex != _currentIndex) {
      setState(() {
        _currentIndex = newIndex;
      });
    }
  }

  void _onNavItemTapped(int index) {
    final router = GoRouter.of(context);
    String route;

    switch (index) {
      case 0:
        route = '/dashboard';
        break;
      case 1:
        route = AppRouter.accounts;
        break;
      case 2:
        route = '/transfer';
        break;
      case 3:
        route = AppRouter.transactions;
        break;
      case 4:
        route = '/profile';
        break;
      default:
        route = '/dashboard';
    }

    if (router.canPop() == false || index != _currentIndex) {
      router.go(route);
    }
  }
}

class PremiumFab extends StatelessWidget {
  final VoidCallback onPressed;
  final IconData icon;
  final String label;
  final Color? backgroundColor;
  final Color? foregroundColor;

  const PremiumFab({
    super.key,
    required this.onPressed,
    required this.icon,
    required this.label,
    this.backgroundColor,
    this.foregroundColor,
  });

  @override
  Widget build(BuildContext context) {
    return FloatingActionButton.extended(
      onPressed: onPressed,
      icon: Icon(icon),
      label: Text(label),
      backgroundColor: backgroundColor ?? AppColors.secondary,
      foregroundColor: foregroundColor ?? AppColors.textOnPrimary,
      elevation: 8,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(AppSpacing.radiusXXLarge),
      ),
    );
  }
}

class QuickActionFab extends StatelessWidget {
  final VoidCallback onPressed;
  final IconData icon;
  final String tooltip;
  final Color? backgroundColor;

  const QuickActionFab({
    super.key,
    required this.onPressed,
    required this.icon,
    required this.tooltip,
    this.backgroundColor,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        color: backgroundColor ?? AppColors.secondary,
        borderRadius: BorderRadius.circular(AppSpacing.radiusFull),
        boxShadow: AppShadows.floating,
      ),
      child: Material(
        color: Colors.transparent,
        child: InkWell(
          onTap: onPressed,
          borderRadius: BorderRadius.circular(AppSpacing.radiusFull),
          child: Container(
            width: 56,
            height: 56,
            child: Icon(
              icon,
              color: AppColors.textOnPrimary,
              size: 24,
            ),
          ),
        ),
      ),
    );
  }
}
