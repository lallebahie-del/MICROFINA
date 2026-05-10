/// Une transaction telle que consommée par l'UI mobile (TransactionBloc).
class MobileTransaction {
  final String id;
  final String accountId;
  final DateTime? date;
  final double montant;
  final String type; // 'CREDIT' | 'DEBIT'
  final String libelle;
  final String? numPiece;
  final double? soldeApres;

  const MobileTransaction({
    required this.id,
    required this.accountId,
    required this.montant,
    required this.type,
    required this.libelle,
    this.date,
    this.numPiece,
    this.soldeApres,
  });
}

class TransactionPage {
  final List<MobileTransaction> items;
  final int page;
  final int totalPages;
  final int totalElements;

  const TransactionPage({
    required this.items,
    required this.page,
    required this.totalPages,
    required this.totalElements,
  });
}

abstract class TransactionsRepository {
  Future<TransactionPage> fetch({
    required String numCompte,
    int page = 0,
    int size = 20,
  });
}
