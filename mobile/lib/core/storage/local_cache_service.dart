import 'package:hive_flutter/hive_flutter.dart';

class LocalCacheService {
  static const String _accountsBox = 'accounts_box';
  static const String _loansBox = 'loans_box';
  static const String _certificatsBox = 'certificats_box';

  Future<void> cacheListData(
    String boxName,
    List<Map<String, dynamic>> data,
  ) async {
    final box = await Hive.openBox(boxName);
    await box.clear();
    await box.addAll(data);
  }

  Future<List<Map<String, dynamic>>> getCachedListData(String boxName) async {
    final box = await Hive.openBox(boxName);
    return box.values.map((e) => Map<String, dynamic>.from(e as Map)).toList();
  }

  // Specific helpers
  Future<void> cacheAccounts(List<Map<String, dynamic>> accounts) =>
      cacheListData(_accountsBox, accounts);
  Future<List<Map<String, dynamic>>> getCachedAccounts() =>
      getCachedListData(_accountsBox);

  Future<void> cacheLoans(List<Map<String, dynamic>> loans) =>
      cacheListData(_loansBox, loans);
  Future<List<Map<String, dynamic>>> getCachedLoans() =>
      getCachedListData(_loansBox);

  Future<void> cacheCertificats(List<Map<String, dynamic>> certificats) =>
      cacheListData(_certificatsBox, certificats);
  Future<List<Map<String, dynamic>>> getCachedCertificats() =>
      getCachedListData(_certificatsBox);
}
