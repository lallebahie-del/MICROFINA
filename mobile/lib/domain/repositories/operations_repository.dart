/// Résultat d'une opération mobile (transfer ou paiement).
class MobileOperationResult {
  final String statut;          // 'OK' typiquement
  final String? compteSource;
  final String? compteDest;
  final String? service;
  final String? reference;
  final double  montant;
  final double? soldeApres;

  const MobileOperationResult({
    required this.statut,
    required this.montant,
    this.compteSource,
    this.compteDest,
    this.service,
    this.reference,
    this.soldeApres,
  });
}

abstract class OperationsRepository {
  /// Virement entre deux comptes du même utilisateur (ou de la même agence).
  Future<MobileOperationResult> transfer({
    required String fromAccountNum,
    required String toAccountNum,
    required double montant,
    String? libelle,
  });

  /// Paiement d'une facture (Senelec, Sen'Eau, Canal+, Crédit téléphonique…).
  Future<MobileOperationResult> pay({
    required String fromAccountNum,
    required String serviceName,
    required double montant,
    String? reference,
  });
}
