import 'package:flutter/services.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';

class SecureStorageService {
  final FlutterSecureStorage _storage;

  SecureStorageService(this._storage);

  static const String _tokenKey = 'auth_token';
  static const String _refreshTokenKey = 'refresh_token';
  static const String _userKey = 'user_data';
  static const String _secureModeKey = 'secure_mode';
  static const String _lastPhoneKey = 'last_phone';
  static const String _lastLoginDateKey = 'last_login_date';
  static const String _accountsListKey = 'registered_accounts_list';
  static const String _userNameKey = 'user_name';
  static const String _userPhotoKey = 'user_photo_path';

  Future<void> saveToken(String token) async {
    try {
      await _storage.write(key: _tokenKey, value: token);
    } on PlatformException catch (e) {
      // Gérer les exceptions spécifiques à la plateforme (ex: Web, Android Backup)
      debugPrint('Erreur lors de la sauvegarde du token: ${e.message}');
      rethrow;
    }
  }

  Future<String?> getToken() async {
    try {
      return await _storage.read(key: _tokenKey);
    } on PlatformException catch (e) {
      debugPrint('Erreur lors de la lecture du token: ${e.message}');
      return null;
    }
  }

  Future<void> deleteToken() async {
    try {
      await _storage.delete(key: _tokenKey);
    } on PlatformException catch (e) {
      debugPrint('Erreur lors de la suppression du token: ${e.message}');
    }
  }

  Future<void> saveRefreshToken(String token) async {
    try {
      await _storage.write(key: _refreshTokenKey, value: token);
    } on PlatformException catch (e) {
      debugPrint('Erreur lors de la sauvegarde du refresh token: ${e.message}');
      rethrow;
    }
  }

  Future<String?> getRefreshToken() async {
    try {
      return await _storage.read(key: _refreshTokenKey);
    } on PlatformException catch (e) {
      debugPrint('Erreur lors de la lecture du refresh token: ${e.message}');
      return null;
    }
  }

  Future<void> deleteRefreshToken() async {
    try {
      await _storage.delete(key: _refreshTokenKey);
    } on PlatformException catch (e) {
      debugPrint(
        'Erreur lors de la suppression du refresh token: ${e.message}',
      );
    }
  }

  Future<void> saveUserData(String userData) async {
    try {
      await _storage.write(key: _userKey, value: userData);
    } on PlatformException catch (e) {
      debugPrint(
        'Erreur lors de la sauvegarde des données utilisateur: ${e.message}',
      );
    }
  }

  Future<String?> getUserData() async {
    try {
      return await _storage.read(key: _userKey);
    } on PlatformException catch (e) {
      debugPrint(
        'Erreur lors de la lecture des données utilisateur: ${e.message}',
      );
      return null;
    }
  }

  Future<void> clearAll() async {
    try {
      await _storage.deleteAll();
    } on PlatformException catch (e) {
      debugPrint('Erreur lors du nettoyage du stockage: ${e.message}');
    }
  }

  Future<void> setSecureMode(bool enabled) async {
    await _storage.write(key: _secureModeKey, value: enabled.toString());
  }

  Future<bool> getSecureMode() async {
    final value = await _storage.read(key: _secureModeKey);
    return value == null || value == 'true'; // Défaut à true pour la sécurité
  }

  Future<void> saveLastPhone(String phone) async {
    await _storage.write(key: _lastPhoneKey, value: phone);
  }

  Future<String?> getLastPhone() async {
    return await _storage.read(key: _lastPhoneKey);
  }

  Future<void> saveLastLoginDate(DateTime date) async {
    await _storage.write(key: _lastLoginDateKey, value: date.toIso8601String());
  }

  Future<DateTime?> getLastLoginDate() async {
    final value = await _storage.read(key: _lastLoginDateKey);
    return value != null ? DateTime.parse(value) : null;
  }

  // --- Gestion Multi-Comptes ---

  Future<void> saveAccountInfo({
    required String phone,
    required String name,
    bool biometricEnabled = true,
  }) async {
    // Sauvegarder le nom spécifique au téléphone
    await _storage.write(key: 'user_name_$phone', value: name);
    await _storage.write(
      key: 'biometric_enabled_$phone',
      value: biometricEnabled.toString(),
    );

    // Mettre à jour la liste des comptes enregistrés
    List<String> accounts = await getRegisteredPhones();
    if (!accounts.contains(phone)) {
      accounts.add(phone);
      await _storage.write(key: _accountsListKey, value: accounts.join(','));
    }
  }

  Future<List<String>> getRegisteredPhones() async {
    final value = await _storage.read(key: _accountsListKey);
    if (value == null || value.isEmpty) return [];
    return value.split(',');
  }

  Future<String?> getUserName(String phone) async {
    return await _storage.read(key: 'user_name_$phone');
  }

  Future<void> saveUserPhoto(String phone, String path) async {
    await _storage.write(key: 'user_photo_$phone', value: path);
  }

  Future<String?> getUserPhoto(String phone) async {
    return await _storage.read(key: 'user_photo_$phone');
  }

  Future<void> saveUserEmail(String phone, String email) async {
    await _storage.write(key: 'user_email_$phone', value: email);
  }

  Future<String?> getUserEmail(String phone) async {
    return await _storage.read(key: 'user_email_$phone');
  }

  Future<void> saveUserContactPhone(String phone, String contactPhone) async {
    await _storage.write(key: 'user_contact_phone_$phone', value: contactPhone);
  }

  Future<String?> getUserContactPhone(String phone) async {
    return await _storage.read(key: 'user_contact_phone_$phone');
  }

  Future<void> saveUserAddress(String phone, String address) async {
    await _storage.write(key: 'user_address_$phone', value: address);
  }

  Future<String?> getUserAddress(String phone) async {
    return await _storage.read(key: 'user_address_$phone');
  }

  Future<void> setSmsAlertsEnabled(String phone, bool enabled) async {
    await _storage.write(key: 'optsms_$phone', value: enabled.toString());
  }

  Future<bool> getSmsAlertsEnabled(String phone) async {
    final value = await _storage.read(key: 'optsms_$phone');
    return value == null || value == 'true';
  }

  Future<bool> isBiometricEnabled(String phone) async {
    final value = await _storage.read(key: 'biometric_enabled_$phone');
    return value == null || value == 'true';
  }

  Future<void> removeAccount(String phone) async {
    await _storage.delete(key: 'user_name_$phone');
    await _storage.delete(key: 'biometric_enabled_$phone');

    List<String> accounts = await getRegisteredPhones();
    accounts.remove(phone);
    await _storage.write(key: _accountsListKey, value: accounts.join(','));
  }
}

// Pour le debugPrint
void debugPrint(String message) {
  // ignore: avoid_print
  print('[SecureStorage] $message');
}
