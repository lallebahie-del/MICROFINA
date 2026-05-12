import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:go_router/go_router.dart';
import 'package:intl/date_symbol_data_local.dart';
import 'core/router/app_router.dart';
import 'core/theme/app_theme.dart';
import 'core/widgets/secure_app.dart';
import 'core/network/connectivity_service.dart';
import 'presentation/blocs/auth/auth_bloc.dart';
import 'presentation/blocs/loan/loan_bloc.dart';
import 'presentation/blocs/transfer/transfer_bloc.dart';
import 'presentation/blocs/loan_simulator/loan_simulator_bloc.dart';
import 'presentation/blocs/certificat/certificat_bloc.dart';
import 'core/di/service_locator.dart';
import 'core/utils/bloc_observer.dart';

import 'package:hive_flutter/hive_flutter.dart';

import 'presentation/blocs/account/account_bloc.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await Hive.initFlutter();
  
  await initializeDateFormatting('fr_FR', null);
  setupLocator();
  Bloc.observer = SimpleBlocObserver();
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  late final GoRouter _router;
  late final AuthBloc _authBloc;

  @override
  void initState() {
    super.initState();
    _authBloc = sl<AuthBloc>();
    _router = AppRouter.createRouter(_authBloc);
  }

  @override
  Widget build(BuildContext context) {
    return MultiBlocProvider(
      providers: [
        BlocProvider.value(value: _authBloc),
        BlocProvider(create: (context) => sl<AccountBloc>()..add(FetchAccounts())),
        BlocProvider(create: (context) => sl<LoanBloc>()),
        BlocProvider(create: (context) => sl<TransferBloc>()),
        BlocProvider(create: (context) => sl<LoanSimulatorBloc>()),
        BlocProvider(create: (context) => sl<CertificatBloc>()),
      ],
        child: RepositoryProvider(
        create: (context) => sl<ConnectivityService>(),
        child: MaterialApp.router(
          title: 'Micro Credit',
          debugShowCheckedModeBanner: false,
          theme: AppTheme.lightTheme.copyWith(
            textTheme: GoogleFonts.interTextTheme(Theme.of(context).textTheme),
          ),
          darkTheme: AppTheme.darkTheme,
          themeMode: ThemeMode.system,
          routerConfig: _router,
          builder: (context, child) {
            return BlocListener<AuthBloc, AuthState>(
              listenWhen: (prev, curr) =>
                  curr is Unauthenticated && prev is! Unauthenticated,
              listener: (context, state) {
                final loc = GoRouter.of(context).state.uri.path;
                if (loc != AppRouter.login && loc != AppRouter.register) {
                  GoRouter.of(context).go(AppRouter.login);
                }
              },
              child: SecureApp(child: child!),
            );
          },
        ),
      ),
    );
  }
}
