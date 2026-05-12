/// Règle métier : numéro mobile sur **8 chiffres** ; le premier chiffre est **2, 3 ou 4** uniquement.
class PhoneNumberPolicy {
  PhoneNumberPolicy._();

  static final RegExp mobileRegex = RegExp(r'^[234][0-9]{7}$');

  /// Ne garde que les chiffres (espaces ignorés).
  static String normalize(String raw) =>
      raw.replaceAll(RegExp(r'\D'), '');

  static bool isValid(String raw) => mobileRegex.hasMatch(normalize(raw));

  static const String validationMessage =
      '8 chiffres requis ; le premier doit être 2, 3 ou 4.';
}
