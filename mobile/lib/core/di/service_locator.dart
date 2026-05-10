import 'package:dio/dio.dart';
import 'package:get_it/get_it.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import '../utils/pdf_generator_service.dart';
import '../storage/secure_storage_service.dart';
import '../network/connectivity_service.dart';
import '../network/dio_client.dart';
import '../../domain/repositories/auth_repository.dart';
import '../../domain/repositories/comptes_repository.dart';
import '../../domain/repositories/transactions_repository.dart';
import '../../domain/repositories/operations_repository.dart';
import '../../domain/repositories/notifications_repository.dart';
import '../../domain/repositories/credits_repository.dart';
import '../../data/repositories/auth_repository_impl.dart';
import '../../data/repositories/comptes_repository_impl.dart';
import '../../data/repositories/transactions_repository_impl.dart';
import '../../data/repositories/operations_repository_impl.dart';
import '../../data/repositories/notifications_repository_impl.dart';
import '../../data/repositories/credits_repository_impl.dart';
import '../../presentation/blocs/auth/auth_bloc.dart';
import '../../presentation/blocs/auth/auth_event.dart';

final sl = GetIt.instance;

void setupLocator() {
  // ── External ────────────────────────────────────────────────────────────
  sl.registerLazySingleton(() => const FlutterSecureStorage());
  sl.registerLazySingleton<Dio>(() => Dio());

  // ── Services ────────────────────────────────────────────────────────────
  sl.registerLazySingleton(() => PdfGeneratorService());
  sl.registerLazySingleton(() => SecureStorageService(sl()));
  sl.registerLazySingleton(() => ConnectivityService());
  sl.registerLazySingleton<DioClient>(() => DioClient(sl<Dio>(), sl()));

  // ── Repositories ────────────────────────────────────────────────────────
  sl.registerLazySingleton<AuthRepository>(
    () => AuthRepositoryImpl(sl<DioClient>(), sl()),
  );
  sl.registerLazySingleton<ComptesRepository>(
    () => ComptesRepositoryImpl(sl<DioClient>()),
  );
  sl.registerLazySingleton<TransactionsRepository>(
    () => TransactionsRepositoryImpl(sl<DioClient>()),
  );
  sl.registerLazySingleton<OperationsRepository>(
    () => OperationsRepositoryImpl(sl<DioClient>()),
  );
  sl.registerLazySingleton<NotificationsRepository>(
    () => NotificationsRepositoryImpl(sl<DioClient>()),
  );
  sl.registerLazySingleton<CreditsRepository>(
    () => CreditsRepositoryImpl(sl<DioClient>()),
  );

  // ── Blocs ───────────────────────────────────────────────────────────────
  sl.registerFactory(() => AuthBloc(sl())..add(AppStarted()));
}
