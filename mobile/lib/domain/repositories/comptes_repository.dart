import '../../data/models/compte_eps_model.dart';

/// Contrat pour récupérer les comptes épargne accessibles à l'utilisateur courant.
abstract class ComptesRepository {
  /// Comptes du membre/agence rattaché au JWT actuel.
  Future<List<CompteEpsModel>> getMyComptes();
}
