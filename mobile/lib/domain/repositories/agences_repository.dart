import '../../data/models/agence_model.dart';

abstract class AgencesRepository {
  Future<List<AgenceModel>> fetchAgences();
}
