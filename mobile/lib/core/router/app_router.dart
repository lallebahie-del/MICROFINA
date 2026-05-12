import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';
import '../../core/di/service_locator.dart';
import '../../presentation/blocs/auth/auth_bloc.dart';
import '../../presentation/blocs/transaction/transaction_bloc.dart';
import '../../presentation/screens/login/login_screen.dart';
import '../../presentation/screens/register/register_screen.dart';
import '../../presentation/screens/dashboard/dashboard_screen.dart';
import '../../presentation/screens/transactions/transactions_screen.dart';
import '../../presentation/screens/profile/profile_screen.dart';
import '../../presentation/screens/transfer/transfer_screen.dart';
import '../../presentation/screens/scan/scan_screen.dart';
import '../../presentation/screens/pay/pay_screen.dart';
import '../../presentation/screens/notifications/notification_screen.dart';
import '../../presentation/screens/dashboard/main_navigation_wrapper.dart';
import '../../presentation/screens/loans/loans_screen.dart';
import '../../presentation/screens/loans/loan_detail_screen.dart';
import '../../presentation/screens/loans/loan_simulator_screen.dart';
import '../../presentation/screens/loans/certificat_screen.dart';
import '../../presentation/screens/loans/loan_request_screen.dart';
import '../../presentation/screens/accounts/accounts_screen.dart';
import '../../data/datasources/mock/mock_data.dart';

class AppRouter {
  static const String root = '/';
  static const String login = '/login';
  static const String register = '/register';
  static const String dashboard = '/dashboard';
  static const String accounts = '/accounts';
  static const String transactions = '/transactions';
  static const String transfer = '/transfer';
  static const String scan = '/scan';
  static const String pay = '/pay';
  static const String notifications = '/notifications';
  static const String profile = '/profile';
  static const String loans = '/loans';
  static const String loanDetail = '/loan-detail/:loan_id';
  static const String loanSimulator = '/loan_simulator';
  static const String loanRequest = '/loan_request';
  static const String certificates = '/certificates';

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
              path: accounts,
              builder: (context, state) => const AccountsScreen(),
            ),
            GoRoute(
              path: '$accounts/:accountId',
              builder: (context, state) {
                final accountId = state.pathParameters['accountId']!;
                return AccountDetailScreen(accountId: accountId);
              },
            ),
            GoRoute(
              path: transactions,
              builder: (context, state) => BlocProvider(
                create: (context) => sl<TransactionBloc>()
                  ..add(LoadTransactions(MockData.transactionScopeAllAccounts)),
                child: TransactionsScreen(
                  accountId: MockData.transactionScopeAllAccounts,
                ),
              ),
            ),
            GoRoute(
              path: '$transactions/:accountId',
              builder: (context, state) {
                final accountId = state.pathParameters['accountId']!;
                return BlocProvider(
                  create: (context) => sl<TransactionBloc>()..add(LoadTransactions(accountId)),
                  child: TransactionsScreen(accountId: accountId),
                );
              },
            ),
            GoRoute(
              path: profile,
              builder: (context, state) => const ProfileScreen(),
            ),
            GoRoute(
              path: loans,
              builder: (context, state) => const LoansScreen(),
            ),
            GoRoute(
              path: loanDetail,
              builder: (context, state) {
                final loanId = state.pathParameters['loan_id']!;
                return LoanDetailScreen(loanId: loanId);
              },
            ),
            GoRoute(
              path: loanSimulator,
              builder: (context, state) => const SimulatorScreen(),
            ),
            GoRoute(
              path: loanRequest,
              builder: (context, state) => const NewLoanRequestScreen(),
            ),
            GoRoute(
              path: certificates,
              builder: (context, state) => const CertificateScreen(),
            ),
            GoRoute(
              path: transfer,
              builder: (context, state) => const TransferScreen(),
            ),
            GoRoute(
              path: scan,
              builder: (context, state) => const ScanScreen(),
            ),
            GoRoute(
              path: pay,
              builder: (context, state) => const PayScreen(),
            ),
            GoRoute(
              path: notifications,
              builder: (context, state) => const NotificationScreen(),
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
