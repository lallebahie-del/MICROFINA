import 'dart:async';
import 'dart:math' as math;

import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:geocoding/geocoding.dart';
import 'package:geolocator/geolocator.dart';
import 'package:google_maps_flutter/google_maps_flutter.dart';

import '../../../core/constants/app_geo.dart';
import '../../../core/di/service_locator.dart';
import '../../../core/storage/secure_storage_service.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../data/datasources/mock/mock_data.dart';
import '../../../data/models/agence_model.dart';
import '../../../domain/repositories/agences_repository.dart';
import '../../blocs/auth/auth_bloc.dart';

class AgenciesScreen extends MapScreen {
  const AgenciesScreen({super.key, super.initialAgenceId});
}

class MapScreen extends StatefulWidget {
  final String? initialAgenceId;

  const MapScreen({super.key, this.initialAgenceId});

  @override
  State<MapScreen> createState() => _MapScreenState();
}

class _MapScreenState extends State<MapScreen> {
  final Completer<GoogleMapController> _mapController =
      Completer<GoogleMapController>();
  List<AgenceModel> _agences = [];
  AgenceModel? _focusedAgence;
  bool _agencesLoading = true;
  String? _agencesError;
  bool _isLocatingUser = true;
  String? _locationMessage;
  LatLng? _userProfileLatLng;
  String? _userProfileAddressLabel;
  /// Position GPS réelle (prioritaire pour « agences les plus proches »).
  LatLng? _deviceGpsLatLng;

  static const LatLng _defaultCenter = AppGeo.headquartersLatLng;

  /// Point de référence pour trier les agences : GPS si dispo, sinon adresse profil.
  LatLng? get _referencePointForProximity =>
      _deviceGpsLatLng ?? _userProfileLatLng;

  static double _toRad(double deg) => deg * math.pi / 180;

  /// Distance a vol d'oiseau (km).
  static double _haversineKm(
    double lat1,
    double lon1,
    double lat2,
    double lon2,
  ) {
    const earthKm = 6371.0;
    final dLat = _toRad(lat2 - lat1);
    final dLon = _toRad(lon2 - lon1);
    final a = math.sin(dLat / 2) * math.sin(dLat / 2) +
        math.cos(_toRad(lat1)) *
            math.cos(_toRad(lat2)) *
            math.sin(dLon / 2) *
            math.sin(dLon / 2);
    final c = 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a));
    return earthKm * c;
  }

  static String _formatDistanceKm(double km) {
    if (km < 1) return '${(km * 1000).round()} m';
    return '${km.toStringAsFixed(1)} km';
  }

  List<AgenceModel> get _agencesByNearest {
    final ref = _referencePointForProximity;
    if (ref == null) return List<AgenceModel>.from(_agences);
    final sorted = List<AgenceModel>.from(_agences);
    sorted.sort((a, b) {
      final da = _haversineKm(ref.latitude, ref.longitude, a.latitude, a.longitude);
      final db = _haversineKm(ref.latitude, ref.longitude, b.latitude, b.longitude);
      return da.compareTo(db);
    });
    return sorted;
  }

  double _distanceToAgenceKm(AgenceModel a) {
    final ref = _referencePointForProximity;
    if (ref == null) return double.infinity;
    return _haversineKm(ref.latitude, ref.longitude, a.latitude, a.longitude);
  }

  @override
  void initState() {
    super.initState();
    _loadAgences();
  }

  Future<void> _loadAgences() async {
    try {
      final list = await sl<AgencesRepository>().fetchAgences();
      if (!mounted) return;
      AgenceModel? focused;
      final initialId = widget.initialAgenceId;
      if (initialId != null) {
        for (final a in list) {
          if (a.id == initialId || a.code == initialId) {
            focused = a;
            break;
          }
        }
      }
      setState(() {
        _agences = list;
        _agencesLoading = false;
        _focusedAgence = focused;
      });
    } catch (e) {
      if (!mounted) return;
      setState(() {
        _agencesError = e.toString();
        _agencesLoading = false;
      });
    }
  }

  CameraPosition get _initialCameraPosition {
    final agence = _focusedAgence;
    return CameraPosition(
      target: agence == null
          ? _defaultCenter
          : LatLng(agence.latitude, agence.longitude),
      zoom: agence == null ? 7 : 16,
    );
  }

  Set<Marker> get _markers {
    final agenceMarkers = _agences.map((agence) {
      return Marker(
        markerId: MarkerId(agence.id),
        position: LatLng(agence.latitude, agence.longitude),
        infoWindow: InfoWindow(title: agence.nom, snippet: agence.ville),
        onTap: () => _onAgenceMarkerTap(agence),
      );
    }).toSet();
    agenceMarkers.add(
      Marker(
        markerId: const MarkerId('microfina_siege_nktt'),
        position: AppGeo.headquartersLatLng,
        infoWindow: InfoWindow(
          title: 'Microfina',
          snippet: AppGeo.headquartersAddress,
        ),
        icon: BitmapDescriptor.defaultMarkerWithHue(BitmapDescriptor.hueAzure),
      ),
    );
    final home = _userProfileLatLng;
    if (home != null) {
      agenceMarkers.add(
        Marker(
          markerId: const MarkerId('user_profile_address'),
          position: home,
          infoWindow: InfoWindow(
            title: 'Mon adresse',
            snippet: _userProfileAddressLabel ?? '',
          ),
          icon: BitmapDescriptor.defaultMarkerWithHue(BitmapDescriptor.hueGreen),
        ),
      );
    }
    return agenceMarkers;
  }

  Future<void> _onMapCreated(GoogleMapController controller) async {
    if (!_mapController.isCompleted) {
      _mapController.complete(controller);
    }

    final deepLinkedAgence = _focusedAgence;
    if (deepLinkedAgence != null) {
      await _focusAgence(deepLinkedAgence, showSheet: true);
      setState(() => _isLocatingUser = false);
      return;
    }

    await _centerMapFromProfileAddressThenGps();
  }

  String _resolveUserPhone() {
    try {
      final auth = context.read<AuthBloc>().state;
      if (auth is AuthSuccess &&
          auth.phone != null &&
          auth.phone!.trim().isNotEmpty) {
        return auth.phone!.trim();
      }
    } catch (_) {}
    return MockData.currentUserPhone;
  }

  /// Cadre les agences mock (utile si GPS / géocodage indisponibles).
  Future<void> _fitCameraToAgences() async {
    if (_agences.isEmpty || !mounted) return;
    try {
      final controller = await _mapController.future;
      if (_agences.length == 1) {
        final a = _agences.first;
        await controller.animateCamera(
          CameraUpdate.newCameraPosition(
            CameraPosition(
              target: LatLng(a.latitude, a.longitude),
              zoom: 13,
            ),
          ),
        );
        return;
      }
      var minLat = _agences.first.latitude;
      var maxLat = minLat;
      var minLng = _agences.first.longitude;
      var maxLng = minLng;
      for (final a in _agences) {
        minLat = math.min(minLat, a.latitude);
        maxLat = math.max(maxLat, a.latitude);
        minLng = math.min(minLng, a.longitude);
        maxLng = math.max(maxLng, a.longitude);
      }
      const pad = 0.02;
      await controller.animateCamera(
        CameraUpdate.newLatLngBounds(
          LatLngBounds(
            southwest: LatLng(minLat - pad, minLng - pad),
            northeast: LatLng(maxLat + pad, maxLng + pad),
          ),
          56,
        ),
      );
    } catch (_) {
      try {
        final c = await _mapController.future;
        await c.animateCamera(
          CameraUpdate.newCameraPosition(
            const CameraPosition(
              target: AppGeo.headquartersLatLng,
              zoom: 12,
            ),
          ),
        );
      } catch (_) {}
    }
  }

  /// Priorité : adresse enregistrée (profil + stockage) → GPS → carte par défaut.
  Future<void> _centerMapFromProfileAddressThenGps() async {
    setState(() {
      _isLocatingUser = true;
      _locationMessage = null;
    });

    final phone = _resolveUserPhone();
    final storage = sl<SecureStorageService>();
    final stored = await storage.getUserAddress(phone);
    final mock = MockData.getAdresseForPhone(phone);
    final mockLine = (mock['adresse'] as String?)?.trim() ?? '';
    final address = stored != null && stored.trim().isNotEmpty
        ? stored.trim()
        : mockLine;

    if (address.length >= 3) {
      if (mounted) {
        setState(() => _locationMessage = 'Recherche de votre adresse…');
      }
      try {
        final locations = await locationFromAddress(address).timeout(
          const Duration(seconds: 12),
          onTimeout: () => <Location>[],
        );
        if (!mounted) return;
        if (locations.isNotEmpty) {
          final loc = locations.first;
          final target = LatLng(loc.latitude, loc.longitude);
          setState(() {
            _userProfileLatLng = target;
            _userProfileAddressLabel = address;
          });
          final controller = await _mapController.future;
          await controller.animateCamera(
            CameraUpdate.newCameraPosition(
              CameraPosition(target: target, zoom: 14),
            ),
          );
          if (mounted) {
            setState(() {
              _isLocatingUser = false;
              _locationMessage =
                  'Carte centrée sur votre adresse — agences triées par proximité.';
            });
          }
          return;
        }
      } catch (_) {
        if (mounted) {
          setState(
            () => _locationMessage =
                'Adresse introuvable sur la carte. Essai par la localisation…',
          );
        }
        await Future<void>.delayed(const Duration(milliseconds: 600));
      }
    }

    await _centerCameraOnDeviceGpsIfPermitted();
  }

  Future<void> _centerCameraOnDeviceGpsIfPermitted() async {
    if (!mounted) return;
    setState(() {
      _isLocatingUser = true;
      _locationMessage = 'Localisation en cours…';
    });

    final serviceEnabled = await Geolocator.isLocationServiceEnabled();
    if (!serviceEnabled) {
      setState(() {
        _isLocatingUser = false;
        _locationMessage =
            'Service de localisation désactivé. Affichage des agences.';
      });
      await _fitCameraToAgences();
      return;
    }

    var permission = await Geolocator.checkPermission();
    if (permission == LocationPermission.denied) {
      permission = await Geolocator.requestPermission();
    }

    if (permission == LocationPermission.denied ||
        permission == LocationPermission.deniedForever) {
      setState(() {
        _isLocatingUser = false;
        _locationMessage =
            'Permission de localisation refusée. Affichage des agences.';
      });
      await _fitCameraToAgences();
      return;
    }

    try {
      final position = await Geolocator.getCurrentPosition();
      final here = LatLng(position.latitude, position.longitude);
      if (mounted) {
        setState(() => _deviceGpsLatLng = here);
      }
      final controller = await _mapController.future;
      await controller.animateCamera(
        CameraUpdate.newCameraPosition(
          CameraPosition(
            target: here,
            zoom: 14,
          ),
        ),
      );

      if (mounted) {
        setState(() {
          _isLocatingUser = false;
          _locationMessage =
              'Carte centrée sur votre position — agences triées par proximité.';
        });
      }
    } catch (_) {
      if (mounted) {
        setState(() {
          _isLocatingUser = false;
          _locationMessage = 'GPS indisponible. Affichage des agences.';
        });
      }
      await _fitCameraToAgences();
    }
  }

  Future<void> _onAgenceMarkerTap(AgenceModel agence) async {
    await _focusAgence(agence, showSheet: true);
  }

  Future<void> _focusAgence(
    AgenceModel agence, {
    required bool showSheet,
  }) async {
    setState(() => _focusedAgence = agence);
    final controller = await _mapController.future;
    await controller.animateCamera(
      CameraUpdate.newCameraPosition(
        CameraPosition(
          target: LatLng(agence.latitude, agence.longitude),
          zoom: 16,
        ),
      ),
    );

    if (showSheet && mounted) {
      _showAgenceSheet(agence);
    }
  }

  void _showAgenceSheet(AgenceModel agence) {
    final ref = _referencePointForProximity;
    final distanceLine = ref == null
        ? null
        : "Environ ${_formatDistanceKm(_distanceToAgenceKm(agence))} (à vol d'oiseau) depuis votre position sur la carte.";

    showModalBottomSheet<void>(
      context: context,
      showDragHandle: true,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(28)),
      ),
      builder: (sheetContext) {
        return Padding(
          padding: EdgeInsets.fromLTRB(
            AppSpacing.lg,
            AppSpacing.sm,
            AppSpacing.lg,
            MediaQuery.paddingOf(sheetContext).bottom + AppSpacing.lg,
          ),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              Text(
                agence.nom,
                style: const TextStyle(
                  color: AppColors.primary,
                  fontSize: 22,
                  fontWeight: FontWeight.w900,
                ),
              ),
              const SizedBox(height: AppSpacing.sm),
              Text(
                '${agence.adresse}, ${agence.ville}',
                style: const TextStyle(
                  color: AppColors.textSecondary,
                  fontSize: 14,
                  fontWeight: FontWeight.w600,
                ),
              ),
              if (distanceLine != null) ...[
                const SizedBox(height: AppSpacing.md),
                Text(
                  distanceLine,
                  style: TextStyle(
                    color: Colors.grey[800],
                    fontSize: 13,
                    fontWeight: FontWeight.w600,
                  ),
                ),
              ],
              const SizedBox(height: AppSpacing.lg),
              FilledButton.icon(
                onPressed: () => Navigator.of(sheetContext).pop(),
                icon: const Icon(Icons.map_rounded),
                label: const Text('Rester sur la carte'),
                style: FilledButton.styleFrom(
                  backgroundColor: AppColors.primary,
                  foregroundColor: Colors.white,
                  padding: const EdgeInsets.symmetric(vertical: 15),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(16),
                  ),
                ),
              ),
            ],
          ),
        );
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF8FAFC),
      appBar: AppBar(
        title: const Text(
          'Agences',
          style: TextStyle(fontWeight: FontWeight.w900),
        ),
        centerTitle: true,
        elevation: 0,
        backgroundColor: Colors.white,
        foregroundColor: AppColors.primary,
      ),
      body: _agencesLoading
          ? const Center(child: CircularProgressIndicator())
          : _agencesError != null
          ? Center(
              child: Padding(
                padding: const EdgeInsets.all(24),
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    Text(_agencesError!, textAlign: TextAlign.center),
                    const SizedBox(height: 16),
                    ElevatedButton(
                      onPressed: _loadAgences,
                      child: const Text('RÉESSAYER'),
                    ),
                  ],
                ),
              ),
            )
          : _agences.isEmpty
          ? _buildEmptyState()
          : Stack(
              children: [
                GoogleMap(
                  initialCameraPosition: _initialCameraPosition,
                  markers: _markers,
                  myLocationButtonEnabled: true,
                  myLocationEnabled: !_isLocatingUser,
                  zoomControlsEnabled: false,
                  mapToolbarEnabled: false,
                  onMapCreated: _onMapCreated,
                ),
                Positioned(
                  left: AppSpacing.md,
                  right: AppSpacing.md,
                  top: AppSpacing.md,
                  child: _buildAgenceSelector(),
                ),
                Positioned(
                  left: AppSpacing.md,
                  right: AppSpacing.md,
                  bottom:
                      MediaQuery.paddingOf(context).bottom + AppSpacing.md,
                  child: Column(
                    mainAxisSize: MainAxisSize.min,
                    crossAxisAlignment: CrossAxisAlignment.stretch,
                    children: [
                      if (_referencePointForProximity != null &&
                          !_isLocatingUser)
                        Padding(
                          padding: const EdgeInsets.only(bottom: AppSpacing.sm),
                          child: _buildNearestAgenciesCard(),
                        ),
                      if (_isLocatingUser || _locationMessage != null)
                        _buildLocationStatus(),
                    ],
                  ),
                ),
              ],
            ),
    );
  }

  Widget _buildNearestAgenciesCard() {
    final ref = _referencePointForProximity;
    if (ref == null || _agences.isEmpty) return const SizedBox.shrink();

    final top = _agencesByNearest.take(3).toList();
    return Material(
      color: Colors.white,
      borderRadius: BorderRadius.circular(18),
      elevation: 4,
      shadowColor: Colors.black12,
      child: Padding(
        padding: const EdgeInsets.fromLTRB(14, 12, 14, 12),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          mainAxisSize: MainAxisSize.min,
          children: [
            Row(
              children: [
                Icon(Icons.near_me_rounded, color: AppColors.primary, size: 20),
                const SizedBox(width: 8),
                const Expanded(
                  child: Text(
                    'Les plus proches de vous',
                    style: TextStyle(
                      color: AppColors.primary,
                      fontWeight: FontWeight.w900,
                      fontSize: 14,
                    ),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 4),
            Text(
              _deviceGpsLatLng != null
                  ? 'Selon votre position GPS'
                  : 'Selon votre adresse enregistrée',
              style: TextStyle(
                color: Colors.grey[600],
                fontSize: 11,
                fontWeight: FontWeight.w600,
              ),
            ),
            const SizedBox(height: 8),
            ...top.map((a) {
              final km = _distanceToAgenceKm(a);
              return InkWell(
                onTap: () => _focusAgence(a, showSheet: true),
                borderRadius: BorderRadius.circular(12),
                child: Padding(
                  padding: const EdgeInsets.symmetric(vertical: 6),
                  child: Row(
                    children: [
                      Expanded(
                        child: Text(
                          a.nom,
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                          style: const TextStyle(
                            fontWeight: FontWeight.w700,
                            fontSize: 13,
                          ),
                        ),
                      ),
                      Text(
                        _formatDistanceKm(km),
                        style: TextStyle(
                          color: Colors.grey[700],
                          fontWeight: FontWeight.w800,
                          fontSize: 13,
                        ),
                      ),
                    ],
                  ),
                ),
              );
            }),
          ],
        ),
      ),
    );
  }

  Widget _buildEmptyState() {
    return const Center(
      child: Text(
        'Aucune agence disponible',
        style: TextStyle(color: AppColors.primary, fontWeight: FontWeight.w900),
      ),
    );
  }

  Widget _buildAgenceSelector() {
    final ordered = _agencesByNearest;
    return SizedBox(
      height: 54,
      child: ListView.separated(
        scrollDirection: Axis.horizontal,
        itemCount: ordered.length,
        separatorBuilder: (context, index) =>
            const SizedBox(width: AppSpacing.sm),
        itemBuilder: (context, index) {
          final agence = ordered[index];
          final selected = agence.id == _focusedAgence?.id;
          return ActionChip(
            avatar: Icon(
              Icons.location_on_rounded,
              color: selected ? Colors.white : AppColors.primary,
              size: 18,
            ),
            label: Text(
              _referencePointForProximity != null
                  ? '${agence.ville} · ${_formatDistanceKm(_distanceToAgenceKm(agence))}'
                  : agence.ville,
            ),
            labelStyle: TextStyle(
              color: selected ? Colors.white : AppColors.primary,
              fontWeight: FontWeight.w800,
            ),
            backgroundColor: selected ? AppColors.primary : Colors.white,
            side: BorderSide(
              color: selected ? AppColors.primary : AppColors.border,
            ),
            elevation: selected ? 4 : 0,
            onPressed: () => _focusAgence(agence, showSheet: true),
          );
        },
      ),
    );
  }

  Widget _buildLocationStatus() {
    return Material(
      color: Colors.white,
      borderRadius: BorderRadius.circular(18),
      elevation: 4,
      shadowColor: Colors.black12,
      child: Padding(
        padding: const EdgeInsets.symmetric(
          horizontal: AppSpacing.md,
          vertical: AppSpacing.sm,
        ),
        child: Row(
          children: [
            if (_isLocatingUser)
              const SizedBox(
                width: 18,
                height: 18,
                child: CircularProgressIndicator(strokeWidth: 2),
              )
            else
              const Icon(Icons.info_outline_rounded, color: AppColors.primary),
            const SizedBox(width: AppSpacing.sm),
            Expanded(
              child: Text(
                _isLocatingUser
                    ? 'Demande de permission de localisation...'
                    : _locationMessage!,
                style: const TextStyle(
                  color: AppColors.primary,
                  fontWeight: FontWeight.w700,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
