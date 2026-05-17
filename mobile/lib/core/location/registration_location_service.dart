import 'package:geocoding/geocoding.dart';
import 'package:geolocator/geolocator.dart';

/// Adresse et coordonnées capturées à l'inscription mobile.
class RegistrationAddress {
  const RegistrationAddress({
    this.adresse,
    this.adresse1,
    this.rueMaison,
    this.ville,
    this.latitude,
    this.longitude,
  });

  final String? adresse;
  final String? adresse1;
  final String? rueMaison;
  final String? ville;
  final String? latitude;
  final String? longitude;

  bool get hasData =>
      (adresse != null && adresse!.isNotEmpty) ||
      (adresse1 != null && adresse1!.isNotEmpty) ||
      (rueMaison != null && rueMaison!.isNotEmpty) ||
      (ville != null && ville!.isNotEmpty) ||
      (latitude != null && latitude!.isNotEmpty) ||
      (longitude != null && longitude!.isNotEmpty);

  Map<String, String> toApiPayload() {
    final map = <String, String>{};
    if (adresse != null && adresse!.isNotEmpty) map['adresse'] = adresse!;
    if (adresse1 != null && adresse1!.isNotEmpty) map['adresse1'] = adresse1!;
    if (rueMaison != null && rueMaison!.isNotEmpty) {
      map['rueMaison'] = rueMaison!;
    }
    if (ville != null && ville!.isNotEmpty) map['ville'] = ville!;
    if (latitude != null && latitude!.isNotEmpty) {
      map['latitude'] = latitude!;
    }
    if (longitude != null && longitude!.isNotEmpty) {
      map['longitude'] = longitude!;
    }
    return map;
  }
}

/// Récupère la position GPS et la convertit en adresse lisible.
class RegistrationLocationService {
  Future<RegistrationAddress?> captureCurrentAddress() async {
    final enabled = await Geolocator.isLocationServiceEnabled();
    if (!enabled) return null;

    var permission = await Geolocator.checkPermission();
    if (permission == LocationPermission.denied) {
      permission = await Geolocator.requestPermission();
    }
    if (permission == LocationPermission.denied ||
        permission == LocationPermission.deniedForever) {
      return null;
    }

    try {
      final position = await Geolocator.getCurrentPosition(
        locationSettings: const LocationSettings(
          accuracy: LocationAccuracy.medium,
          timeLimit: Duration(seconds: 12),
        ),
      );

      final lat = position.latitude.toStringAsFixed(6);
      final lng = position.longitude.toStringAsFixed(6);

      String? street;
      String? locality;
      String? subLocality;
      String? formatted;

      final placemarks = await placemarkFromCoordinates(
        position.latitude,
        position.longitude,
      );
      if (placemarks.isNotEmpty) {
        final p = placemarks.first;
        street = _joinNonEmpty([p.street, p.subThoroughfare]);
        subLocality = p.subLocality;
        locality = _joinNonEmpty([p.locality, p.administrativeArea]);
        formatted = _joinNonEmpty([
          street,
          subLocality,
          locality,
          p.country,
        ]);
      }

      return RegistrationAddress(
        adresse: formatted,
        adresse1: subLocality,
        rueMaison: street,
        ville: locality,
        latitude: lat,
        longitude: lng,
      );
    } catch (_) {
      return null;
    }
  }

  static String? _joinNonEmpty(List<String?> parts) {
    final items = parts
        .where((e) => e != null && e.trim().isNotEmpty)
        .map((e) => e!.trim())
        .toList();
    if (items.isEmpty) return null;
    return items.join(', ');
  }
}
