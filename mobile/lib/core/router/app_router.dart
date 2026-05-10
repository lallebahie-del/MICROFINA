import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';
import '../../presentation/blocs/auth/auth_bloc.dart';
import '../../presentation/blocs/auth/auth_state.dart';
import '../../presentation/blocs/transaction/transaction_bloc.dart';
import '../../presentation/blocs/transaction/transaction_event.dart';
import '../../presentation/screens/login/login_screen.dart';
import '../../presentation/screens/register/register_screen.dart';
import '../../presentation/screens/dashboard/dashboard_screen.dart';
import '../../presentation/screens/transactions/transactions_screen.dart';
import '../../presentation/screens/profile/profile_screen.dart';
import '../../presentation/screens/dashboard/main_navigation_wrapper.dart';

class AppRouter {
  static const String root = '/';
  static const String login = '/login';
  static const String register = '/register';
  static const String dashboard = '/dashboard';
  static const String transactions = '/transactions';
  static const String profile = '/profile';

  static final GlobalKey<NavigatorState> _rootNavigatorKey = GlobalKey<NavigatorState>();
  static final GlobalKey<NavigatorState> _shellNavigatorKey = GlobalKey<NavigatorState>();

  static GoRouter createRouter(AuthBloc authBloc) {
    return GoRouter(
      navigatorKey: _rootNavigatorKey,
      initialLocation: login,
      refreshListenable: GoRouterRefreshStream(authBloc.stream),
      redirect: (context, state) {
        final authState = authBloc.state;
        final isLoggingIn = state.matchedLocation == login;
        final isRegistering = state.matchedLocation == register;

        if (authState is AuthInitial || authState is AuthLoading) {
          return null;
        }

        if (authState is Unauthenticated || authState is AuthFailure) {
          if (isLoggingIn || isRegistering) return null;
          return login;
        }

        if (authState is AuthSuccess) {
          if (isLoggingIn || isRegistering) return dashboard;
          return null;
        }

        return null;
      },
      routes: [
        GoRoute(
          path: login,
          builder: (context, state) => const LoginScreen(),
        ),
        GoRoute(
          path: register,
          builder: (context, state) => const RegisterScreen(),
        ),
        
        // ShellRoute pour la navigation persistante
        ShellRoute(
          navigatorKey: _shellNavigatorKey,
          builder: (context, state, child) {
            return MainNavigationWrapper(child: child);
          },
          routes: [
            GoRoute(
              path: dashboard,
              builder: (context, state) => const DashboardScreen(),
            ),
            GoRoute(
              path: '$transactions/:accountId',
              builder: (context, state) {
                final accountId = state.pathParameters['accountId']!;
                return BlocProvider(
                  create: (context) => TransactionBloc()..add(LoadTransactions(accountId)),
                  child: TransactionsScreen(accountId: accountId),
                );
              },
            ),
            GoRoute(
              path: profile,
              builder: (context, state) => const ProfileScreen(),
            ),
          ],
        ),
      ],
    );
  }
}

class GoRouterRefreshStream extends ChangeNotifier {
  GoRouterRefreshStream(Stream<dynamic> stream) {
    _subscription = stream.asBroadcastStream().listen(
          (dynamic _) => notifyListeners(),
        );
  }

  late final StreamSubscription<dynamic> _subscription;

  @override
  void dispose() {
    _subscription.cancel();
    super.dispose();
  }
}
