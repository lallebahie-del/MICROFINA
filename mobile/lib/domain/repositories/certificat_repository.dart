import '../../data/models/extra_models.dart';

abstract class CertificatRepository {
  Future<List<CertificatModel>> getCertificats();
}
