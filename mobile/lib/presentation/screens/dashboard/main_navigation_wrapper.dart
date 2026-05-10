import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import '../../../core/di/service_locator.dart';
import '../../../core/theme/app_theme.dart';
import '../../../domain/repositories/comptes_repository.dart';

class MainNavigationWrapper extends StatefulWidget {
  final Widget child;

  const MainNavigationWrapper({super.key, required this.child});

  @override
  State<MainNavigationWrapper> createState() => _MainNavigationWrapperState();
}

class _MainNavigationWrapperState extends State<MainNavigationWrapper> {
  final ComptesRepository _comptesRepo = sl<ComptesRepository>();
  String? _cachedAccountId;
  bool _loadingComptes = false;

  int _calculateSelectedIndex(BuildContext context) {
    final String location = GoRouterState.of(context).matchedLocation;
    if (location.startsWith('/dashboard')) return 0;
    if (location.startsWith('/transactions')) return 1;
    if (location.startsWith('/credits')) return 2;
    if (location.startsWith('/profile')) return 3;
    return 0;
  }

  Future<void> _onItemTapped(int index, BuildContext context) async {
    switch (index) {
      case 0:
        context.go('/dashboard');
        break;
      case 1:
        await _goToActivity(context);
        break;
      case 2:
        context.go('/credits');
        break;
      case 3:
        context.go('/profile');
        break;
    }
  }

  Future<void> _goToActivity(BuildContext context) async {
    // Cache pour éviter de re-fetch à chaque tap.
    if (_cachedAccountId != null) {
      context.go('/transactions/$_cachedAccountId');
      return;
    }
    if (_loadingComptes) return;

    setState(() => _loadingComptes = true);
    try {
      final comptes = await _comptesRepo.getMyComptes();
      if (!mounted) return;
      if (comptes.isEmpty) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Aucun compte disponible pour afficher l\'activité.')),
        );
        return;
      }
      _cachedAccountId = comptes.first.numeroCompte;
      if (!mounted) return;
      context.go('/transactions/$_cachedAccountId');
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('Erreur : ${e.toString().replaceFirst('Exception: ', '')}'),
          backgroundColor: AppTheme.errorRed,
        ),
      );
    } finally {
      if (mounted) setState(() => _loadingComptes = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: widget.child,
      bottomNavigationBar: Container(
        decoration: BoxDecoration(
          boxShadow: [
            BoxShadow(
              color: Colors.black.withOpacity(0.1),
              blurRadius: 20,
              offset: const Offset(0, -5),
            ),
          ],
        ),
        child: BottomNavigationBar(
          currentIndex: _calculateSelectedIndex(context),
          onTap: (index) => _onItemTapped(index, context),
          selectedItemColor: AppTheme.primaryBlue,
          unselectedItemColor: Colors.grey,
          showUnselectedLabels: true,
          type: BottomNavigationBarType.fixed,
          items: const [
            BottomNavigationBarItem(
              icon: Icon(Icons.dashboard_rounded),
              activeIcon: Icon(Icons.dashboard_rounded),
              label: 'Accueil',
            ),
            BottomNavigationBarItem(
              icon: Icon(Icons.receipt_long_rounded),
              activeIcon: Icon(Icons.receipt_long_rounded),
              label: 'Activité',
            ),
            BottomNavigationBarItem(
              icon: Icon(Icons.account_balance_rounded),
              label: 'Prêts',
            ),
            BottomNavigationBarItem(
              icon: Icon(Icons.settings_rounded),
              label: 'Profil',
            ),
          ],
        ),
      ),
    );
  }
}
