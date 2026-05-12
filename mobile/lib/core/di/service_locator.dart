import 'package:get_it/get_it.dart';
import 'package:dio/dio.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import '../auth/biometric_auth_service.dart';
import '../auth/session_invalidation_broadcaster.dart';
import '../utils/pdf_generator_service.dart';
import '../storage/secure_storage_service.dart';
import '../storage/local_cache_service.dart';
import '../network/connectivity_service.dart';
import '../network/dio_client.dart';
import '../../domain/repositories/auth_repository.dart';
import '../../domain/repositories/account_repository.dart';
import '../../domain/repositories/loan_repository.dart';
import '../../domain/repositories/transaction_repository.dart';
import '../../domain/repositories/certificat_repository.dart';
import '../../data/repositories/auth_repository_impl.dart';
import '../../data/repositories/account_repository_impl.dart';
import '../../data/repositories/loan_repository_impl.dart';
import '../../data/repositories/transaction_repository_impl.dart';
import '../../data/repositories/certificat_repository_impl.dart';
import '../../presentation/blocs/auth/auth_bloc.dart';
import '../../presentation/blocs/account/account_bloc.dart';
import '../../presentation/blocs/loan/loan_bloc.dart';
import '../../presentation/blocs/transfer/transfer_bloc.dart';
import '../../presentation/blocs/loan_simulator/loan_simulator_bloc.dart';
import '../../presentation/blocs/certificat/certificat_bloc.dart';
import '../../presentation/blocs/transaction/transaction_bloc.dart';

final sl = GetIt.instance;

void setupLocator() {
  // External
  sl.registerLazySingleton(() => const FlutterSecureStorage());
  sl.registerLazySingleton(() => Dio());

  // Services
  sl.registerLazySingleton(() => SessionInvalidationBroadcaster());
  sl.registerLazySingleton(() => BiometricAuthService());
  sl.registerLazySingleton(() => PdfGeneratorService());
  sl.registerLazySingleton(() => SecureStorageService(sl()));
  sl.registerLazySingleton(() => LocalCacheService());
  sl.registerLazySingleton(() => ConnectivityService());
  sl.registerLazySingleton(() => DioClient(sl(), sl(), sl()));

  // Repositories
  sl.registerLazySingleton<AuthRepository>(
    () => AuthRepositoryImpl(sl(), sl()),
  );
  sl.registerLazySingleton<AccountRepository>(
    () => AccountRepositoryImpl(sl(), sl(), sl()),
  );
  sl.registerLazySingleton<LoanRepository>(
    () => LoanRepositoryImpl(sl(), sl(), sl()),
  );
  sl.registerLazySingleton<TransactionRepository>(
    () => TransactionRepositoryImpl(sl()),
  );
  sl.registerLazySingleton<CertificatRepository>(
    () => CertificatRepositoryImpl(sl(), sl(), sl()),
  );

  // Blocs — AuthBloc doit rester une instance unique (router + Provider + déconnexion).
  sl.registerLazySingleton(() => AuthBloc(sl(), sl())..add(AppStarted()));
  sl.registerFactory(() => AccountBloc(sl()));
  sl.registerFactory(() => LoanBloc(sl()));
  sl.registerFactory(() => TransferBloc(sl()));
  sl.registerFactory(() => LoanSimulatorBloc());
  sl.registerFactory(() => CertificatBloc(sl()));
  sl.registerFactory(() => TransactionBloc(sl()));
}
