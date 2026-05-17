/// Chemins et constantes partagés pour l'API backend MICROFINA.
class ApiConfig {
  ApiConfig._();

  /// Historique agrégé (tous les comptes du client).
  static const String allAccountsTransactionScope = '__ALL__';

  static const String authLogin = '/api/auth/login';
  static const String authRegisterMobile = '/api/v1/auth/register-mobile';

  static const String mobileMePrefix = '/api/v1/mobile/me';
  static String mobileComptes() => '$mobileMePrefix/comptes';
  static String mobileProfile() => '$mobileMePrefix/profile';
  static String mobileCredits() => '$mobileMePrefix/credits';
  static String mobileTransactions(String numCompte) =>
      '$mobileMePrefix/transactions/$numCompte';
  static String mobileTransfer() => '$mobileMePrefix/transfer';
  static String mobileTransferExternal() => '$mobileMePrefix/transfer-external';
  static String mobilePay() => '$mobileMePrefix/pay';
  static String mobileNotifications() => '$mobileMePrefix/notifications';

  static const String agencesList = '/api/v1/agences';
}
