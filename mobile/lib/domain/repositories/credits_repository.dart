/// Représentation d'un crédit côté UI mobile.
class MobileCredit {
  final int     idCredit;
  final String  numCredit;
  final String  statut;
  final double  montantDemande;
  final double  montantAccorde;
  final double  soldeCapital;
  final double  soldeInteret;
  final double  tauxInteret;
  final int     duree;
  final int     nombreEcheance;
  final String? periodicite;
  final String? dateDemande;
  final String? dateAccord;
  final String? dateDeblocage;
  final String? dateEcheance;
  final String? objetCredit;
  final String? membreNum;
  final String? membreNom;
  final String? membrePrenom;
  final String? produitNom;
  final String? agenceCode;

  const MobileCredit({
    required this.idCredit,
    required this.numCredit,
    required this.statut,
    required this.montantDemande,
    required this.montantAccorde,
    required this.soldeCapital,
    required this.soldeInteret,
    required this.tauxInteret,
    required this.duree,
    required this.nombreEcheance,
    this.periodicite,
    this.dateDemande,
    this.dateAccord,
    this.dateDeblocage,
    this.dateEcheance,
    this.objetCredit,
    this.membreNum,
    this.membreNom,
    this.membrePrenom,
    this.produitNom,
    this.agenceCode,
  });
}

abstract class CreditsRepository {
  Future<List<MobileCredit>> getMyCredits();
}
