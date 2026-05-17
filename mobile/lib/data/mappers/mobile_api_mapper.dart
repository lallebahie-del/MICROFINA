import '../models/compte_eps_model.dart';
import '../models/credit_model.dart';
import '../models/extra_models.dart';

/// Mappe les réponses JSON du backend vers les modèles Flutter.
class MobileApiMapper {
  MobileApiMapper._();

  static String? _colorForAccountType(String typeEpargne) {
    final t = typeEpargne.toLowerCase();
    if (t.contains('courant')) return '#1B5E20';
    if (t.contains('épargne') || t.contains('epargne')) return '#1565C0';
    return '#455A64';
  }

  static CompteEpsModel compteFromBackend(Map<String, dynamic> json) {
    final num montantOuvert = (json['montantOuvert'] as num?) ?? 0;
    final num montantDepot = (json['montantDepot'] as num?) ?? 0;
    final num montantBloque = (json['montantBloque'] as num?) ?? 0;
    final String numCompte = (json['numCompte'] as String?) ?? '';
    final String produit = (json['produitEpargne'] as String?) ?? 'EPARGNE';
    final String typeEpargne = (json['typeEpargne'] as String?) ?? produit;
    return CompteEpsModel(
      id: numCompte,
      numeroCompte: numCompte,
      libelle: typeEpargne.isNotEmpty ? typeEpargne : 'Compte $numCompte',
      availableBalance: (montantOuvert + montantDepot).toDouble(),
      blockedBalance: montantBloque.toDouble(),
      accountType: produit,
      devise: 'MRU',
      lastSyncedAt: DateTime.now(),
      isDefaultAccount: false,
      accountTypeColor: _colorForAccountType(typeEpargne),
    );
  }

  static EpargneTransactionModel transactionFromBackend(
    Map<String, dynamic> json,
  ) {
    return EpargneTransactionModel(
      id: json['id']?.toString() ?? '',
      accountId: (json['accountId'] as String?) ?? '',
      date: (json['date'] as String?) ?? '',
      montant: ((json['montant'] as num?) ?? 0).toDouble(),
      type: (json['type'] as String?) ?? 'DEBIT',
      libelle: (json['libelle'] as String?) ?? '',
    );
  }

  static LoanModel loanFromCreditDto(Map<String, dynamic> json) {
    double asDouble(dynamic v) =>
        (v is num) ? v.toDouble() : double.tryParse('$v') ?? 0.0;

    final numCredit = (json['numCredit'] as String?)?.trim();
    final idCredit = json['idCredit'];
    final loanId = (numCredit != null && numCredit.isNotEmpty)
        ? numCredit
        : (idCredit != null ? idCredit.toString() : '');

    final statut = (json['statut'] as String?)?.toUpperCase() ?? '';
    int statusCode;
    if (statut == 'DEBLOQUE') {
      statusCode = 1;
    } else if (statut == 'SOLDE' || statut == 'REJETE') {
      statusCode = 3;
    } else {
      statusCode = 0;
    }

    DateTime endDate = DateTime.now().add(const Duration(days: 365));
    final rawEnd = json['dateEcheance'] as String?;
    if (rawEnd != null && rawEnd.isNotEmpty) {
      try {
        endDate = DateTime.parse(rawEnd);
      } catch (_) {}
    }

    return LoanModel(
      loanId: loanId,
      totalAmount: asDouble(json['montantAccorde'] ?? json['montantDemande']),
      remainingCapital: asDouble(json['soldeCapital']),
      interestRate: asDouble(json['tauxInteret']),
      endDate: endDate,
      statusCode: statusCode,
    );
  }
}
