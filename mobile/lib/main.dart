import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:go_router/go_router.dart';
import 'package:intl/date_symbol_data_local.dart';
import 'core/router/app_router.dart';
import 'core/theme/app_theme.dart';
import 'core/widgets/secure_app.dart';
import 'core/storage/secure_storage_service.dart';
import 'core/network/connectivity_service.dart';
import 'domain/repositories/auth_repository.dart';
import 'data/repositories/auth_repository_impl.dart';
import 'presentation/blocs/auth/auth_bloc.dart';
import 'presentation/blocs/auth/auth_event.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  // Initialisation des données de localisation pour les dates
  await initializeDateFormatting('fr_FR', null);

  // Initialisation du stockage sécurisé
  final secureStorage = SecureStorageService(const FlutterSecureStorage());

  // Initialisation des services
  final connectivityService = ConnectivityService();

  // Initialisation des repositories
  final authRepository = AuthRepositoryImpl(secureStorage);

  // Initialisation du Bloc d'authentification
  final authBloc = AuthBloc(authRepository)..add(AppStarted());

  runApp(
    MultiRepositoryProvider(
      providers: [
        RepositoryProvider<AuthRepository>(create: (_) => authRepository),
        RepositoryProvider<SecureStorageService>(create: (_) => secureStorage),
        RepositoryProvider<ConnectivityService>(create: (_) => connectivityService),
      ],
      child: MultiBlocProvider(
        providers: [
          BlocProvider<AuthBloc>.value(value: authBloc),
        ],
        child: MyApp(authBloc: authBloc),
      ),
    ),
  );
}

class MyApp extends StatefulWidget {
  final AuthBloc authBloc;

  const MyApp({super.key, required this.authBloc});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  late final GoRouter _router;

  @override
  void initState() {
    super.initState();
    _router = AppRouter.createRouter(widget.authBloc);
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp.router(
      title: 'Micro Credit',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.lightTheme,
      darkTheme: AppTheme.darkTheme,
      themeMode: ThemeMode.system,
      routerConfig: _router,
      builder: (context, child) => SecureApp(child: child!),
    );
  }
}
