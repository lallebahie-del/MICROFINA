import 'package:flutter/material.dart';
import 'package:intl/intl.dart';

class MockData {
  static String currentUserPhone = '771234567'; // Défaut pour la démo

  /// Filtre agrégé : toutes les transactions de tous les comptes (tri par date).
  static const String transactionScopeAllAccounts = '__ALL__';

  /// --- GESTION DYNAMIQUE DES COMPTES ---
  static final Map<String, List<Map<String, dynamic>>> _userAccountsMap = {
    /// Démo **Mariem** : connexion avec le numéro `779001122` (PIN selon votre API ou mode démo).
    /// Deux comptes distincts pour tester le **virement interne** ; soldes confortables pour **paiement** / factures.
    '779001122': [
      {
        'id': 'acc_mariem_001',
        'numeroCompte': '37277900112201',
        'libelle': 'Compte courant — Mariem',
        'availableBalance': 2500000.0,
        'blockedBalance': 0.0,
        'accountType': 'EPARGNE',
        'devise': 'FCFA',
        'lastSyncedAt': DateTime.now().subtract(const Duration(minutes: 5)).toIso8601String(),
        'isDefaultAccount': true,
        'accountTypeColor': '#1A237E',
      },
      {
        'id': 'acc_mariem_002',
        'numeroCompte': '37277900112202',
        'libelle': 'Épargne projet — Mariem',
        'availableBalance': 850000.0,
        'blockedBalance': 50000.0,
        'accountType': 'GARANTIE',
        'devise': 'FCFA',
        'lastSyncedAt': DateTime.now().subtract(const Duration(hours: 2)).toIso8601String(),
        'isDefaultAccount': false,
        'accountTypeColor': '#00C853',
      },
      {
        'id': 'acc_mariem_003',
        'numeroCompte': '37277900112203',
        'libelle': 'Compte paiements & services',
        'availableBalance': 350000.0,
        'blockedBalance': 0.0,
        'accountType': 'EPARGNE',
        'devise': 'FCFA',
        'lastSyncedAt': DateTime.now().toIso8601String(),
        'isDefaultAccount': false,
        'accountTypeColor': '#FF6D00',
      },
    ],
    '771234567': [
      {
        'id': 'acc_001',
        'numeroCompte': '372001234567',
        'libelle': 'Compte Courant Particulier',
        'availableBalance': 1250500.0,
        'blockedBalance': 50000.0,
        'accountType': 'EPARGNE',
        'devise': 'FCFA',
        'lastSyncedAt': DateTime.now().subtract(const Duration(minutes: 15)).toIso8601String(),
        'isDefaultAccount': true,
        'accountTypeColor': '#1A237E',
      },
      {
        'id': 'acc_002',
        'numeroCompte': '372001987654',
        'libelle': 'Compte Épargne Projet',
        'availableBalance': 450000.0,
        'blockedBalance': 200000.0,
        'accountType': 'GARANTIE',
        'devise': 'FCFA',
        'lastSyncedAt': DateTime.now().subtract(const Duration(hours: 1)).toIso8601String(),
        'isDefaultAccount': false,
        'accountTypeColor': '#00C853',
      },
    ],
  };

  static List<Map<String, dynamic>> getAccountsForPhone(String phone) {
    if (!_userAccountsMap.containsKey(phone)) {
      _userAccountsMap[phone] = [
        {
          'id': 'acc_${phone}_1',
          'numeroCompte': '372${phone}01',
          'libelle': 'Compte Courant',
          'availableBalance': 50000.0,
          'blockedBalance': 0.0,
          'accountType': 'EPARGNE',
          'devise': 'FCFA',
          'lastSyncedAt': DateTime.now().toIso8601String(),
          'isDefaultAccount': true,
          'accountTypeColor': '#1A237E',
        },
        {
          'id': 'acc_${phone}_2',
          'numeroCompte': '372${phone}02',
          'libelle': 'Épargne Sécurité',
          'availableBalance': 10000.0,
          'blockedBalance': 5000.0,
          'accountType': 'GARANTIE',
          'devise': 'FCFA',
          'lastSyncedAt': DateTime.now().toIso8601String(),
          'isDefaultAccount': false,
          'accountTypeColor': '#00C853',
        },
      ];
    }
    return _userAccountsMap[phone]!;
  }

  static double getDefaultAccountBalance() {
    final accounts = _userAccountsMap[currentUserPhone] ?? getAccountsForPhone(currentUserPhone);
    if (accounts.isEmpty) return 0.0;
    final defaultAcc = accounts.firstWhere((acc) => acc['isDefaultAccount'] == true, orElse: () => accounts.first);
    return (defaultAcc['availableBalance'] as num).toDouble();
  }

  /// Simulation d'un virement interne
  static Future<bool> performInternalTransfer({
    required String fromAccountId,
    required String toAccountId,
    required double amount,
    required String reason,
  }) async {
    await Future.delayed(const Duration(seconds: 2));
    if (amount <= 0) return false;

    Map<String, dynamic>? fromAcc;
    Map<String, dynamic>? toAcc;

    // Rechercher dans tous les comptes de tous les utilisateurs
    for (var userAccounts in _userAccountsMap.values) {
      for (var acc in userAccounts) {
        if (acc['id'] == fromAccountId) fromAcc = acc;
        if (acc['id'] == toAccountId) toAcc = acc;
      }
    }

    if (fromAcc == null || toAcc == null) return false;
    if (fromAcc['availableBalance'] < amount) return false;

    // Mise à jour effective
    fromAcc['availableBalance'] -= amount;
    toAcc['availableBalance'] += amount;

    // Historique (les notifications mouvements sont dérivées de cette liste)
    mockEpargneTransactions.insert(0, {
      'id': 'tx_transfer_deb_${DateTime.now().millisecondsSinceEpoch}',
      'accountId': fromAccountId,
      'date': DateTime.now().toIso8601String(),
      'montant': amount,
      'type': 'DEBIT',
      'libelle': 'Virement vers ${toAcc['libelle']} : $reason',
    });

    mockEpargneTransactions.insert(0, {
      'id': 'tx_transfer_cre_${DateTime.now().millisecondsSinceEpoch}',
      'accountId': toAccountId,
      'date': DateTime.now().toIso8601String(),
      'montant': amount,
      'type': 'CREDIT',
      'libelle': 'Virement reçu de ${fromAcc['libelle']} : $reason',
    });
    
    return true;
  }

  /// Simulation d'un paiement d'échéance de prêt
  static Future<bool> payLoanInstallment({
    required String loanId,
    required double amount,
    required int installmentId,
  }) async {
    await Future.delayed(const Duration(seconds: 1));
    
    // On utilise le compte de l'utilisateur courant
    final accounts = _userAccountsMap[currentUserPhone] ?? getAccountsForPhone(currentUserPhone);
    if (accounts.isEmpty) return false;
    final account = accounts.firstWhere((acc) => acc['isDefaultAccount'] == true, orElse: () => accounts.first);

    if (account['availableBalance'] < amount) {
      return false; // Solde insuffisant
    }

    // Débit effectif
    account['availableBalance'] -= amount;

    // Ajouter à l'historique des transactions
    mockEpargneTransactions.insert(0, {
      'id': 'tx_loan_${DateTime.now().millisecondsSinceEpoch}',
      'accountId': account['id'],
      'date': DateTime.now().toIso8601String(),
      'montant': amount,
      'type': 'DEBIT',
      'libelle': 'Remboursement Prêt $loanId - Échéance n°$installmentId',
    });

    return true;
  }

  /// Simulation d'un paiement de service
  static Future<bool> performServicePayment({
    required String accountId,
    required String serviceName,
    required double amount,
    required String reference,
  }) async {
    await Future.delayed(const Duration(seconds: 2));
    if (amount <= 0) return false;

    Map<String, dynamic>? account;
    for (var userAccounts in _userAccountsMap.values) {
      for (var acc in userAccounts) {
        if (acc['id'] == accountId) account = acc;
      }
    }

    if (account == null || account['availableBalance'] < amount) return false;

    // Débit effectif
    account['availableBalance'] -= amount;

    // Ajouter à l'historique
    mockEpargneTransactions.insert(0, {
      'id': 'tx_pay_${DateTime.now().millisecondsSinceEpoch}',
      'accountId': accountId,
      'date': DateTime.now().toIso8601String(),
      'montant': amount,
      'type': 'DEBIT',
      'libelle': 'Paiement facture $serviceName (Réf: $reference)',
    });

    return true;
  }

  static final List<Map<String, dynamic>> mockCompteEpsList = [
    // Conservé pour compatibilité mais non utilisé par getAccountsForPhone désormais
  ];

  /// --- EPARGNE (historique dynamique : uniquement opérations réelles sur vos comptes) ---
  ///
  /// Alimenté par [performInternalTransfer], [performServicePayment], [payLoanInstallment].
  /// Plus de données « seed » : la liste est vidée lors d’un changement d’utilisateur (téléphone).
  static final List<Map<String, dynamic>> mockEpargneTransactions = [];
  static String? _transactionHistoryOwnerPhone;

  static Set<String> _currentUserAccountIds() {
    return getAccountsForPhone(currentUserPhone).map((e) => e['id'] as String).toSet();
  }

  static void _syncTransactionHistoryForCurrentUser() {
    final switched = _transactionHistoryOwnerPhone != null &&
        _transactionHistoryOwnerPhone != currentUserPhone;
    if (switched) {
      mockEpargneTransactions.clear();
      _readNotificationIds.clear();
    }
    _transactionHistoryOwnerPhone = currentUserPhone;
  }

  /// Simulation de pagination avec filtres
  static Future<List<Map<String, dynamic>>> getPaginatedTransactions({
    required String accountId,
    required int page,
    required int pageSize,
    DateTimeRange? dateRange,
  }) async {
    await Future.delayed(const Duration(milliseconds: 400));

    _syncTransactionHistoryForCurrentUser();

    final allowedIds = _currentUserAccountIds();

    List<Map<String, dynamic>> filteredTransactions;
    if (accountId == transactionScopeAllAccounts) {
      filteredTransactions = mockEpargneTransactions
          .where((tx) => allowedIds.contains(tx['accountId'] as String))
          .toList();
    } else {
      if (!allowedIds.contains(accountId)) {
        return [];
      }
      filteredTransactions =
          mockEpargneTransactions.where((tx) => tx['accountId'] == accountId).toList();
    }

    filteredTransactions.sort((a, b) {
      final da = DateTime.parse(a['date'] as String);
      final db = DateTime.parse(b['date'] as String);
      return db.compareTo(da);
    });

    // Appliquer le filtre de date si présent
    if (dateRange != null) {
      filteredTransactions = filteredTransactions.where((tx) {
        final txDate = DateTime.parse(tx['date'] as String);
        return txDate.isAfter(dateRange.start) && txDate.isBefore(dateRange.end.add(const Duration(days: 1)));
      }).toList();
    }

    final int start = page * pageSize;
    final int end = start + pageSize;
    
    if (start >= filteredTransactions.length) return [];
    
    return filteredTransactions.sublist(
      start, 
      end > filteredTransactions.length ? filteredTransactions.length : end
    );
  }

  /// --- GESTION DES BÉNÉFICIAIRES ---
  static final List<Map<String, String>> _beneficiaries = [
    {'id': 'ben_001', 'name': 'Fatima Diop', 'accountNumber': '372009876543'},
    {'id': 'ben_002', 'name': 'Ousmane Sarr', 'accountNumber': '372001122334'},
  ];

  static List<Map<String, String>> getBeneficiaries() => _beneficiaries;

  static void addBeneficiary(String name, String accountNumber) {
    _beneficiaries.add({
      'id': 'ben_${DateTime.now().millisecondsSinceEpoch}',
      'name': name,
      'accountNumber': accountNumber,
    });
  }

  /// --- NOTIFICATIONS (dynamiques : uniquement opérations réellement enregistrées sur vos comptes) ---
  ///
  /// Dérivées de [mockEpargneTransactions] (virements, paiements, remboursements). Pas de rappels fictifs.
  static final Set<String> _readNotificationIds = <String>{};

  static List<Map<String, dynamic>> _composeNotificationFeed() {
    _syncTransactionHistoryForCurrentUser();
    final accounts = getAccountsForPhone(currentUserPhone);
    final accountIds = accounts.map((e) => e['id'] as String).toSet();
    final fmt = NumberFormat.currency(locale: 'fr_FR', symbol: 'FCFA', decimalDigits: 0);

    final txs = mockEpargneTransactions.where((t) => accountIds.contains(t['accountId'])).toList();
    txs.sort((a, b) {
      final da = DateTime.parse(a['date'] as String);
      final db = DateTime.parse(b['date'] as String);
      return db.compareTo(da);
    });

    final feed = <Map<String, dynamic>>[];
    for (final tx in txs.take(30)) {
      final id = tx['id'] as String;
      final isCredit = tx['type'] == 'CREDIT';
      final montant = (tx['montant'] as num).toDouble();
      feed.add({
        'id': 'feed_$id',
        'title': isCredit ? 'Crédit sur compte' : 'Débit sur compte',
        'message': '${tx['libelle']} · ${fmt.format(montant)}',
        'date': tx['date'],
      });
    }
    return feed;
  }

  static List<Map<String, dynamic>> getNotifications() {
    return _composeNotificationFeed().map((n) {
      final id = n['id'] as String;
      return Map<String, dynamic>.from(n)..['isRead'] = _readNotificationIds.contains(id);
    }).toList();
  }

  static void markAllNotificationsAsRead() {
    for (final n in _composeNotificationFeed()) {
      _readNotificationIds.add(n['id'] as String);
    }
  }

  /// --- CREDITS (Prêts) ---
  static final List<Map<String, dynamic>> mockCredits = [
    {
      'loanId': 'cre_882',
      'totalAmount': 6000000.0,
      'remainingCapital': 4500000.0,
      'interestRate': 12.5,
      'endDate': '2026-12-15',
      'statusCode': 1, // Actif
      'nextInstallmentDueDate': '2026-05-15',
    },
    {
      'loanId': 'cre_883',
      'totalAmount': 2000000.0,
      'remainingCapital': 1200000.0,
      'interestRate': 10.0,
      'endDate': '2026-08-10',
      'statusCode': 2, // En retard
      'nextInstallmentDueDate': '2026-05-01',
    },
    {
      'loanId': 'cre_884',
      'totalAmount': 1000000.0,
      'remainingCapital': 0.0,
      'interestRate': 11.0,
      'endDate': '2026-01-20',
      'statusCode': 3, // Clôturé
      'nextInstallmentDueDate': null,
    },
  ];

  static const Map<String, dynamic> mockCredit = {
    'loanId': 'cre_882',
    'totalAmount': 6000000.0,
    'remainingCapital': 4500000.0,
    'interestRate': 12.5,
    'endDate': '2026-12-15',
    'statusCode': 1,
    'nextInstallmentDueDate': '2026-05-15',
  };

  /// --- AMORTP (Échéancier - 6 échéances) ---
  /// Intégrité : loanId lié à cre_882
  static const List<Map<String, dynamic>> mockAmortpList = [
    {
      'id': 1,
      'loanId': 'cre_882',
      'dateEcheance': '2026-02-15',
      'montantCapital': 1000000.0,
      'montantInteret': 62500.0,
      'montantTva': 11250.0,
      'estPaye': true,
    },
    {
      'id': 2,
      'loanId': 'cre_882',
      'dateEcheance': '2026-03-15',
      'montantCapital': 1000000.0,
      'montantInteret': 56250.0,
      'montantTva': 10125.0,
      'estPaye': true,
    },
    {
      'id': 3,
      'loanId': 'cre_882',
      'dateEcheance': '2026-04-15',
      'montantCapital': 1000000.0,
      'montantInteret': 50000.0,
      'montantTva': 9000.0,
      'estPaye': false,
    },
    {
      'id': 4,
      'loanId': 'cre_882',
      'dateEcheance': '2026-05-15',
      'montantCapital': 1000000.0,
      'montantInteret': 43750.0,
      'montantTva': 7875.0,
      'estPaye': false,
    },
    {
      'id': 5,
      'loanId': 'cre_882',
      'dateEcheance': '2026-06-15',
      'montantCapital': 1000000.0,
      'montantInteret': 37500.0,
      'montantTva': 6750.0,
      'estPaye': false,
    },
    {
      'id': 6,
      'loanId': 'cre_882',
      'dateEcheance': '2026-07-15',
      'montantCapital': 1000000.0,
      'montantInteret': 31250.0,
      'montantTva': 5625.0,
      'estPaye': false,
    },
  ];

  /// --- GARANTIES ---
  /// Intégrité : loanId lié à cre_882
  static const List<Map<String, dynamic>> mockGaranties = [
    {
      'id': 'gar_001',
      'loanId': 'cre_882',
      'type': 'Hypothèque',
      'valeur': 15000000.0,
      'description': 'Titre Foncier n°1234/Dakar',
    },
  ];

  /// --- CERTIFICAT (DAT - Dépôt à Terme) ---
  /// Intégrité : accountId lié à acc_001
  static const List<Map<String, dynamic>> mockCertificats = [
    {
      'id': 'cert_001',
      'accountId': 'acc_001',
      'numeroCertificat': 'DAT-2026-001',
      'montantPlacement': 5000000.0,
      'tauxInteret': 5.5,
      'startDate': '2026-01-15',
      'dateEcheance': '2027-01-15',
    },
    {
      'id': 'cert_002',
      'accountId': 'acc_001',
      'numeroCertificat': 'DAT-2026-002',
      'montantPlacement': 2000000.0,
      'tauxInteret': 4.5,
      'startDate': '2026-03-01',
      'dateEcheance': '2026-09-01',
    },
  ];

  /// --- AGENCE (Avec coordonnées GPS) ---
  static const List<Map<String, dynamic>> mockAgenceList = [
    {
      'id': 'ag_001',
      'code': 'HQ001',
      'nom': 'Agence Principale - Dakar',
      'ville': 'Dakar',
      'adresse': 'Avenue Cheikh Anta Diop',
      'latitude': 14.6928,
      'longitude': -17.4467,
    },
    {
      'id': 'ag_002',
      'code': 'TH002',
      'nom': 'Agence Thiès Centre',
      'ville': 'Thiès',
      'adresse': 'Quartier Escale',
      'latitude': 14.7910,
      'longitude': -16.9359,
    },
  ];
}
