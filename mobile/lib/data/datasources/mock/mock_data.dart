class MockData {
  /// --- COMPTEEPS (Génère des comptes spécifiques pour un utilisateur) ---
  static List<Map<String, dynamic>> getAccountsForPhone(String phone) {
    // Si c'est l'utilisateur par défaut (Moussa)
    if (phone == '771234567' || phone == 'acc_001') {
      return mockCompteEpsList;
    }

    // Sinon générer des comptes "frais" pour le nouvel utilisateur
    return [
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

  static final List<Map<String, dynamic>> mockCompteEpsList = [
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
      'accountTypeColor': '#1A237E', // Navy
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
      'accountTypeColor': '#00C853', // Green
    },
  ];

  /// --- EPARGNE (Transactions - 30 entrées) ---
  /// Intégrité : accountId lié à acc_001 ou acc_002
  static final List<Map<String, dynamic>> mockEpargneTransactions = List.generate(30, (index) {
    final bool isEven = index % 2 == 0;
    final String accountId = index < 15 ? 'acc_001' : 'acc_002';
    return {
      'id': 'tx_${index.toString().padLeft(3, '0')}',
      'accountId': accountId,
      'date': '2026-04-${(index % 28 + 1).toString().padLeft(2, '0')}',
      'montant': (index + 1) * 5000.0,
      'type': isEven ? 'CREDIT' : 'DEBIT',
      'libelle': isEven ? 'Dépôt Espèces' : 'Retrait GAB',
    };
  });

  /// --- CREDITS (Prêts) ---
  static const Map<String, dynamic> mockCredit = {
    'id': 'cre_882',
    'capitalRestantDu': 4500000.0,
    'taux': 12.5,
    'statut': 'ACTIF',
    'dateDeblocage': '2026-01-15',
    'montantInitial': 6000000.0,
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
      'dateEcheance': '2027-01-15',
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
