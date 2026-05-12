import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'dart:convert';
import 'package:flutter/services.dart';
import '../../models/agence_model.dart';

class MockData {
  static String currentUserPhone = '27123456'; // Défaut pour la démo

  /// Filtre agrégé : toutes les transactions de tous les comptes (tri par date).
  static const String transactionScopeAllAccounts = '__ALL__';

  /// --- GESTION DYNAMIQUE DES COMPTES ---
  static final Map<String, List<Map<String, dynamic>>> _userAccountsMap = {
    /// Démo **Mariem** : connexion avec le numéro `37900112` (PIN selon votre API ou mode démo).
    /// Deux comptes distincts pour tester le **virement interne** ; soldes confortables pour **paiement** / factures.
    '37900112': [
      {
        'id': 'acc_mariem_001',
        'numeroCompte': '3723790011201',
        'libelle': 'Compte courant — Mariem',
        'availableBalance': 2500000.0,
        'blockedBalance': 0.0,
        'accountType': 'EPARGNE',
        'devise': 'FCFA',
        'lastSyncedAt': DateTime.now()
            .subtract(const Duration(minutes: 5))
            .toIso8601String(),
        'isDefaultAccount': true,
        'accountTypeColor': '#1A237E',
      },
      {
        'id': 'acc_mariem_002',
        'numeroCompte': '3723790011202',
        'libelle': 'Épargne projet — Mariem',
        'availableBalance': 850000.0,
        'blockedBalance': 50000.0,
        'accountType': 'GARANTIE',
        'devise': 'FCFA',
        'lastSyncedAt': DateTime.now()
            .subtract(const Duration(hours: 2))
            .toIso8601String(),
        'isDefaultAccount': false,
        'accountTypeColor': '#00C853',
      },
      {
        'id': 'acc_mariem_003',
        'numeroCompte': '3723790011203',
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
    '27123456': [
      {
        'id': 'acc_001',
        'numeroCompte': '3722712345601',
        'libelle': 'Compte Courant Particulier',
        'availableBalance': 1250500.0,
        'blockedBalance': 50000.0,
        'accountType': 'EPARGNE',
        'devise': 'FCFA',
        'lastSyncedAt': DateTime.now()
            .subtract(const Duration(minutes: 15))
            .toIso8601String(),
        'isDefaultAccount': true,
        'accountTypeColor': '#1A237E',
      },
      {
        'id': 'acc_002',
        'numeroCompte': '3722712345602',
        'libelle': 'Compte Épargne Projet',
        'availableBalance': 450000.0,
        'blockedBalance': 200000.0,
        'accountType': 'GARANTIE',
        'devise': 'FCFA',
        'lastSyncedAt': DateTime.now()
            .subtract(const Duration(hours: 1))
            .toIso8601String(),
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
    final accounts =
        _userAccountsMap[currentUserPhone] ??
        getAccountsForPhone(currentUserPhone);
    if (accounts.isEmpty) return 0.0;
    final defaultAcc = accounts.firstWhere(
      (acc) => acc['isDefaultAccount'] == true,
      orElse: () => accounts.first,
    );
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

  /// Simulation d'un virement vers un compte **externe** (autre institution / tiers).
  static Future<bool> performExternalTransfer({
    required String fromAccountId,
    required String beneficiaryName,
    required String externalAccountNumber,
    String? beneficiaryBank,
    required double amount,
    required String reason,
  }) async {
    await Future.delayed(const Duration(seconds: 2));
    if (amount <= 0) return false;
    final name = beneficiaryName.trim();
    final ext = externalAccountNumber.trim().replaceAll(RegExp(r'\s'), '');
    if (name.length < 2 || ext.length < 8) return false;

    Map<String, dynamic>? fromAcc;
    for (final userAccounts in _userAccountsMap.values) {
      for (final acc in userAccounts) {
        if (acc['id'] == fromAccountId) fromAcc = acc;
      }
    }
    if (fromAcc == null || (fromAcc['availableBalance'] as num) < amount) {
      return false;
    }

    fromAcc['availableBalance'] =
        (fromAcc['availableBalance'] as num).toDouble() - amount;

    final bank = beneficiaryBank?.trim();
    final bankSuffix = (bank != null && bank.isNotEmpty) ? ' — $bank' : '';

    mockEpargneTransactions.insert(0, {
      'id': 'tx_ext_${DateTime.now().millisecondsSinceEpoch}',
      'accountId': fromAccountId,
      'date': DateTime.now().toIso8601String(),
      'montant': amount,
      'type': 'DEBIT',
      'libelle':
          'Virement externe vers $name (compte $ext)$bankSuffix — $reason',
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
    final accounts =
        _userAccountsMap[currentUserPhone] ??
        getAccountsForPhone(currentUserPhone);
    if (accounts.isEmpty) return false;
    final account = accounts.firstWhere(
      (acc) => acc['isDefaultAccount'] == true,
      orElse: () => accounts.first,
    );

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
    return getAccountsForPhone(
      currentUserPhone,
    ).map((e) => e['id'] as String).toSet();
  }

  static void _syncTransactionHistoryForCurrentUser() {
    final switched =
        _transactionHistoryOwnerPhone != null &&
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
      filteredTransactions = mockEpargneTransactions
          .where((tx) => tx['accountId'] == accountId)
          .toList();
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
        return txDate.isAfter(dateRange.start) &&
            txDate.isBefore(dateRange.end.add(const Duration(days: 1)));
      }).toList();
    }

    final int start = page * pageSize;
    final int end = start + pageSize;

    if (start >= filteredTransactions.length) return [];

    return filteredTransactions.sublist(
      start,
      end > filteredTransactions.length ? filteredTransactions.length : end,
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
    final fmt = NumberFormat.currency(
      locale: 'fr_FR',
      symbol: 'FCFA',
      decimalDigits: 0,
    );

    final txs = mockEpargneTransactions
        .where((t) => accountIds.contains(t['accountId']))
        .toList();
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
      return Map<String, dynamic>.from(n)
        ..['isRead'] = _readNotificationIds.contains(id);
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
  /// Intégrité : loanId lié à cre_882 — liste mutable pour simulation de paiement.
  static final List<Map<String, dynamic>> mockAmortpList = [
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
  /// Intégrité : loanId lié à cre_882 — [statut] libellé métier (ex. CREDITS / GARANTIES).
  static final List<Map<String, dynamic>> mockGaranties = [
    {
      'id': 'gar_001',
      'loanId': 'cre_882',
      'type': 'Hypothèque',
      'valeur': 15000000.0,
      'description': 'Titre Foncier n°1234/Dakar',
      'statut': 'Validée',
    },
    {
      'id': 'gar_002',
      'loanId': 'cre_882',
      'type': 'Caution solidaire',
      'valeur': 3000000.0,
      'description': 'Cautionnement société partenaire — dossier en cours',
      'statut': 'En cours de constitution',
    },
  ];

  /// Prêts actifs (exclut statut clôturé — MODULE 4 « Mes prêts »).
  static List<Map<String, dynamic>> get activeMockCredits =>
      mockCredits.where((c) => (c['statusCode'] as int) != 3).toList();

  /// Après débit compte réussi : marque l'échéance payée et réduit le capital restant dû.
  static bool registerInstallmentPaid({
    required String loanId,
    required int installmentId,
  }) {
    Map<String, dynamic>? row;
    for (final m in mockAmortpList) {
      if (m['loanId'] == loanId && m['id'] == installmentId) {
        row = m;
        break;
      }
    }
    if (row == null) return false;
    if (row['estPaye'] == true) return true;

    final capital = (row['montantCapital'] as num).toDouble();
    row['estPaye'] = true;

    for (final c in mockCredits) {
      if (c['loanId'] == loanId) {
        final rem = (c['remainingCapital'] as num).toDouble();
        c['remainingCapital'] = (rem - capital).clamp(0.0, double.infinity);
        break;
      }
    }
    return true;
  }

  /// Paiement d'échéance : débit + mise à jour AMORTP / CREDITS.
  static Future<bool> payLoanInstallmentFull({
    required String loanId,
    required int installmentId,
  }) async {
    Map<String, dynamic>? row;
    for (final m in mockAmortpList) {
      if (m['loanId'] == loanId && m['id'] == installmentId) {
        row = m;
        break;
      }
    }
    if (row == null) return false;
    final amount =
        (row['montantCapital'] as num).toDouble() +
        (row['montantInteret'] as num).toDouble() +
        (row['montantTva'] as num).toDouble();

    final debited = await payLoanInstallment(
      loanId: loanId,
      amount: amount,
      installmentId: installmentId,
    );
    if (!debited) return false;
    registerInstallmentPaid(loanId: loanId, installmentId: installmentId);
    return true;
  }

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

  /// --- AGENCE (Avec coordonnées GPS) — surcharge possible via assets/mock/mock_agences.json
  static final List<Map<String, dynamic>> _agenceDefaults = [
    {
      'id': 'ag_001',
      'code': 'HQ001',
      'nom': 'Agence principale — Nouakchott',
      'ville': 'Nouakchott',
      'adresse': 'Tevragh Zeina, avenue Gamal Abdel Nasser',
      'latitude': 18.0735,
      'longitude': -15.9582,
    },
    {
      'id': 'ag_002',
      'code': 'NK002',
      'nom': 'Agence Ksar',
      'ville': 'Nouakchott',
      'adresse': 'Ksar, près du marché central',
      'latitude': 18.0900,
      'longitude': -15.9780,
    },
  ];

  /// Copie modifiable (hydratation). Ne doit pas rester vide : [getAgences] retombe sur [_agenceDefaults].
  static final List<Map<String, dynamic>> mockAgenceList =
      List<Map<String, dynamic>>.from(
        _agenceDefaults.map((e) => Map<String, dynamic>.from(e)),
      );

  static List<AgenceModel> getAgences() {
    final src = mockAgenceList.isEmpty ? _agenceDefaults : mockAgenceList;
    return src.map(AgenceModel.fromJson).toList();
  }

  static AgenceModel? findAgenceByIdOrCode(String value) {
    final normalized = value.toLowerCase();
    for (final agence in getAgences()) {
      final numericSuffix = agence.id.replaceFirst(RegExp(r'^ag_0*'), '');
      if (agence.id.toLowerCase() == normalized ||
          agence.code.toLowerCase() == normalized ||
          numericSuffix == normalized) {
        return agence;
      }
    }
    return null;
  }

  /// --- ADRESSE (coordonnées client) ---
  static final Map<String, Map<String, dynamic>> mockAdresseByPhone = {
    '27123456': {
      'nom': 'Client Microfina',
      'email': 'client@microfina.com',
      'telephone': '27123456',
      'adresse': 'Nouakchott, Mauritanie',
    },
    '37900112': {
      'nom': 'Mariem Ndiaye',
      'email': 'mariem.ndiaye@microfina.com',
      'telephone': '37900112',
      'adresse': 'Nouakchott, Mauritanie',
    },
  };

  static Map<String, dynamic> getAdresseForPhone(String phone) {
    return mockAdresseByPhone[phone] ??
        {
          'nom': 'Utilisateur Microfina',
          'email': 'contact@microfina.com',
          'telephone': phone,
          'adresse': 'Nouakchott, Mauritanie',
        };
  }

  /// --- OPTSMS (préférences de notifications SMS) ---
  static const List<Map<String, dynamic>> mockOptSmsList = [
    {'id': 'mouvement_compte', 'label': 'Mouvement de compte', 'enabled': true},
    {'id': 'rappel_echeance', 'label': "Rappel d'échéance", 'enabled': true},
    {'id': 'securite', 'label': 'Sécurité et connexion', 'enabled': false},
  ];

  /// Hydrate les tables de référence (MODULE 1.4) depuis les JSON statiques sous `assets/mock/`.
  /// Silencieux si les fichiers sont absents ou invalides.
  static Future<void> hydrateReferenceTablesFromAssetsIfPresent() async {
    const prefix = 'assets/mock/';
    try {
      final raw = await rootBundle.loadString('${prefix}mock_agences.json');
      final list = jsonDecode(raw) as List<dynamic>;
      final parsed = <Map<String, dynamic>>[];
      for (final e in list) {
        if (e is Map) {
          try {
            parsed.add(Map<String, dynamic>.from(e));
          } catch (_) {}
        }
      }
      if (parsed.isNotEmpty) {
        mockAgenceList
          ..clear()
          ..addAll(parsed);
      }
    } catch (_) {}

    try {
      final raw = await rootBundle.loadString('${prefix}mock_credits.json');
      final list = jsonDecode(raw) as List<dynamic>;
      mockCredits
        ..clear()
        ..addAll(list.map((e) => Map<String, dynamic>.from(e as Map)));
    } catch (_) {}

    try {
      final raw = await rootBundle.loadString('${prefix}mock_amortp.json');
      final list = jsonDecode(raw) as List<dynamic>;
      mockAmortpList
        ..clear()
        ..addAll(list.map((e) => Map<String, dynamic>.from(e as Map)));
    } catch (_) {}

    try {
      final raw = await rootBundle.loadString(
        '${prefix}compte_eps_by_user.json',
      );
      final map = jsonDecode(raw) as Map<String, dynamic>;
      for (final e in map.entries) {
        final list = e.value as List<dynamic>;
        _userAccountsMap[e.key] = list
            .map((x) => Map<String, dynamic>.from(x as Map))
            .toList();
      }
    } catch (_) {}

    try {
      if (mockEpargneTransactions.isEmpty) {
        final raw = await rootBundle.loadString(
          '${prefix}epargne_transactions.json',
        );
        final list = jsonDecode(raw) as List<dynamic>;
        if (list.isNotEmpty) {
          mockEpargneTransactions.addAll(
            list.map((x) => Map<String, dynamic>.from(x as Map)),
          );
        }
      }
    } catch (_) {}
  }
}
