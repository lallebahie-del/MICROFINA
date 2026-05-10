import 'package:get_it/get_it.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import '../utils/pdf_generator_service.dart';
import '../storage/secure_storage_service.dart';
import '../network/connectivity_service.dart';
import '../../domain/repositories/auth_repository.dart';
import '../../data/repositories/auth_repository_impl.dart';
import '../../presentation/blocs/auth/auth_bloc.dart';
import '../../presentation/blocs/auth/auth_event.dart';

final sl = GetIt.instance;

void setupLocator() {
  // External
  sl.registerLazySingleton(() => const FlutterSecureStorage());

  // Services
  sl.registerLazySingleton(() => PdfGeneratorService());
  sl.registerLazySingleton(() => SecureStorageService(sl()));
  sl.registerLazySingleton(() => ConnectivityService());
  
  // Repositories
  sl.registerLazySingleton<AuthRepository>(() => AuthRepositoryImpl(sl()));

  // Blocs
  sl.registerFactory(() => AuthBloc(sl())..add(AppStarted()));
}
