class MobileUserProfile {

  final String login;

  final String nomComplet;

  final String email;

  final String telephone;

  final String codeAgence;

  final String numMembre;

  final String adresse;

  final String ville;

  final String latitude;

  final String longitude;

  final String numCompteCourant;

  final bool actif;



  const MobileUserProfile({

    required this.login,

    required this.nomComplet,

    required this.email,

    required this.telephone,

    required this.codeAgence,

    required this.numMembre,

    required this.adresse,

    required this.ville,

    required this.latitude,

    required this.longitude,

    required this.numCompteCourant,

    required this.actif,

  });

}



abstract class ProfileRepository {

  Future<MobileUserProfile> getProfile();

}

