import 'package:flutter/material.dart';

/// Parse une couleur de compte (#RRGGBB ou 0xFF…) ; retourne null si invalide.
Color? tryParseAccountColor(String? raw) {
  if (raw == null || raw.trim().isEmpty) return null;
  final value = raw.trim();
  if (value.startsWith('#') && value.length >= 7) {
    final hex = value.replaceFirst('#', '0xFF');
    final parsed = int.tryParse(hex);
    if (parsed != null) return Color(parsed);
  }
  if (value.startsWith('0x') || value.startsWith('0X')) {
    final parsed = int.tryParse(value);
    if (parsed != null) return Color(parsed);
  }
  return null;
}
