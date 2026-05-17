import 'package:dio/dio.dart';



import '../../core/config/api_config.dart';

import '../../core/network/dio_client.dart';

import '../../domain/repositories/profile_repository.dart';



class ProfileRepositoryImpl implements ProfileRepository {

  final DioClient _dio;



  ProfileRepositoryImpl(this._dio);



  @override

  Future<MobileUserProfile> getProfile() async {

    try {

      final response = await _dio.get(ApiConfig.mobileProfile());

      final body = response.data as Map<String, dynamic>? ?? {};

      return MobileUserProfile(

        login: (body['login'] as String?) ?? '',

        nomComplet: (body['nomComplet'] as String?) ?? '',

        email: (body['email'] as String?) ?? '',

        telephone: (body['telephone'] as String?) ?? '',

        codeAgence: (body['codeAgence'] as String?) ?? '',

        numMembre: (body['numMembre'] as String?) ?? '',

        adresse: (body['adresse'] as String?) ?? '',

        ville: (body['ville'] as String?) ?? '',

        latitude: (body['latitude'] as String?) ?? '',

        longitude: (body['longitude'] as String?) ?? '',

        numCompteCourant: (body['numCompteCourant'] as String?) ?? '',

        actif: body['actif'] == true,

      );

    } on DioException catch (e) {

      throw Exception(_messageFromDio(e));

    }

  }



  String _messageFromDio(DioException e) {

    final code = e.response?.statusCode;

    if (code == 401) return 'Session expirée. Reconnecte-toi.';

    if (e.type == DioExceptionType.connectionError) {

      return 'Serveur injoignable.';

    }

    return 'Impossible de charger le profil.';

  }

}

