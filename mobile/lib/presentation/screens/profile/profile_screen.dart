import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';

import '../../../core/di/service_locator.dart';
import '../../../core/router/app_router.dart';
import '../../../core/storage/secure_storage_service.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../core/utils/phone_number_policy.dart';
import '../../../data/datasources/mock/mock_data.dart';
import '../../../domain/repositories/profile_repository.dart';
import '../../blocs/auth/auth_bloc.dart';
import '../../widgets/logout_confirm_dialog.dart';

class ProfileScreen extends StatefulWidget {
  const ProfileScreen({super.key});

  @override
  State<ProfileScreen> createState() => _ProfileScreenState();
}

class _ProfileScreenState extends State<ProfileScreen> {
  final _formKey = GlobalKey<FormState>();
  final _secureStorage = sl<SecureStorageService>();
  final _nameController = TextEditingController();
  final _emailController = TextEditingController();
  final _phoneController = TextEditingController();
  final _addressController = TextEditingController();

  bool _isEditing = false;
  bool _isFormValid = false;
  String? _authPhone;
  String _numCompteCourant = '';

  static final RegExp _emailRegex = RegExp(
    r'^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$',
  );
  static final RegExp _phoneRegex = PhoneNumberPolicy.mobileRegex;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) => _loadProfileData());
    _emailController.addListener(_validateForm);
    _phoneController.addListener(_validateForm);
    _addressController.addListener(_validateForm);
  }

  /// Données mock + surcharge du nom / email / téléphone / adresse issus du
  /// stockage sécurisé (ex. compte créé via inscription « Mariem »).
  Future<void> _loadProfileData() async {
    final authState = context.read<AuthBloc>().state;
    final phone = authState is AuthSuccess && authState.phone != null
        ? authState.phone!
        : MockData.currentUserPhone;

    final adresse = <String, dynamic>{
      'nom': '',
      'email': '',
      'telephone': phone,
      'adresse': '',
    };

    try {
      final profile = await sl<ProfileRepository>().getProfile();
      adresse['nom'] = profile.nomComplet;
      adresse['email'] = profile.email;
      adresse['telephone'] = profile.telephone.isNotEmpty
          ? profile.telephone
          : phone;
      if (profile.adresse.isNotEmpty) {
        adresse['adresse'] = profile.adresse;
      } else if (profile.ville.isNotEmpty) {
        adresse['adresse'] = profile.ville;
      }
      _numCompteCourant = profile.numCompteCourant;
    } catch (_) {
      adresse.addAll(MockData.getAdresseForPhone(phone));
    }

    final storedName = await _secureStorage.getUserName(phone);
    if (storedName != null && storedName.trim().isNotEmpty) {
      adresse['nom'] = storedName.trim();
    }
    final storedEmail = await _secureStorage.getUserEmail(phone);
    if (storedEmail != null && storedEmail.trim().isNotEmpty) {
      adresse['email'] = storedEmail.trim();
    }
    final storedContact = await _secureStorage.getUserContactPhone(phone);
    if (storedContact != null && storedContact.trim().isNotEmpty) {
      adresse['telephone'] = storedContact.trim();
    }
    final storedAddress = await _secureStorage.getUserAddress(phone);
    if (storedAddress != null && storedAddress.trim().isNotEmpty) {
      adresse['adresse'] = storedAddress.trim();
    }

    if (!mounted) return;
    setState(() {
      _authPhone = phone;
      _nameController.text = adresse['nom'] as String;
      _emailController.text = adresse['email'] as String;
      _phoneController.text = adresse['telephone'] as String;
      _addressController.text = adresse['adresse'] as String;
    });
    _validateForm();
  }

  void _validateForm() {
    final email = _emailController.text.trim();
    final phone = _phoneController.text.trim();
    final address = _addressController.text.trim();
    final valid =
        _emailRegex.hasMatch(email) &&
        _phoneRegex.hasMatch(PhoneNumberPolicy.normalize(phone)) &&
        address.length >= 3;

    if (valid != _isFormValid && mounted) {
      setState(() => _isFormValid = valid);
    }
  }

  Future<void> _saveProfile() async {
    if (!_isEditing || !_isFormValid || _authPhone == null) return;
    if (!(_formKey.currentState?.validate() ?? false)) return;

    final updated = {
      'nom': _nameController.text.trim(),
      'email': _emailController.text.trim(),
      'telephone': _phoneController.text.trim(),
      'adresse': _addressController.text.trim(),
    };
    MockData.mockAdresseByPhone[_authPhone!] = updated;

    await _secureStorage.saveAccountInfo(
      phone: _authPhone!,
      name: updated['nom']!,
      biometricEnabled: await _secureStorage.isBiometricEnabled(_authPhone!),
    );
    await _secureStorage.saveUserEmail(_authPhone!, updated['email']!);
    await _secureStorage.saveUserContactPhone(
      _authPhone!,
      updated['telephone']!,
    );
    await _secureStorage.saveUserAddress(_authPhone!, updated['adresse']!);

    if (!mounted) return;
    setState(() => _isEditing = false);
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: const Text('Profil sauvegardé'),
        behavior: SnackBarBehavior.floating,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
        margin: const EdgeInsets.all(16),
      ),
    );
  }

  @override
  void dispose() {
    _nameController.dispose();
    _emailController.dispose();
    _phoneController.dispose();
    _addressController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF8FAFC),
      appBar: AppBar(
        title: const Text(
          'Mon Profil',
          style: TextStyle(fontWeight: FontWeight.w900),
        ),
        centerTitle: true,
        elevation: 0,
        backgroundColor: Colors.white,
        foregroundColor: AppColors.primary,
        actions: [
          TextButton(
            onPressed: () => setState(() => _isEditing = !_isEditing),
            child: Text(_isEditing ? 'Annuler' : 'Modifier'),
          ),
        ],
      ),
      body: Form(
        key: _formKey,
        autovalidateMode: AutovalidateMode.onUserInteraction,
        child: ListView(
          padding: const EdgeInsets.all(AppSpacing.screenPadding),
          children: [
            _buildHeaderCard(),
            const SizedBox(height: AppSpacing.lg),
            _buildField(
              controller: _nameController,
              label: 'Nom',
              icon: Icons.person_outline_rounded,
              enabled: false,
            ),
            const SizedBox(height: AppSpacing.md),
            _buildField(
              controller: _emailController,
              label: 'Email',
              icon: Icons.email_outlined,
              enabled: _isEditing,
              keyboardType: TextInputType.emailAddress,
              validator: (value) {
                final email = value?.trim() ?? '';
                if (!_emailRegex.hasMatch(email)) {
                  return 'Email invalide';
                }
                return null;
              },
            ),
            const SizedBox(height: AppSpacing.md),
            _buildField(
              controller: _phoneController,
              label: 'Téléphone',
              icon: Icons.phone_android_rounded,
              enabled: _isEditing,
              keyboardType: TextInputType.phone,
              validator: (value) {
                final phone = value?.trim() ?? '';
                if (!_phoneRegex.hasMatch(PhoneNumberPolicy.normalize(phone))) {
                  return '8 chiffres ; le premier doit être 2, 3 ou 4';
                }
                return null;
              },
            ),
            const SizedBox(height: AppSpacing.md),
            _buildField(
              controller: _addressController,
              label: 'Adresse',
              icon: Icons.location_on_outlined,
              enabled: _isEditing,
              minLines: 2,
              maxLines: 3,
              validator: (value) {
                if ((value?.trim() ?? '').length < 3) {
                  return 'Adresse obligatoire';
                }
                return null;
              },
            ),
            const SizedBox(height: AppSpacing.lg),
            FilledButton.icon(
              onPressed: _isEditing && _isFormValid ? _saveProfile : null,
              icon: const Icon(Icons.save_rounded),
              label: const Text('Sauvegarder'),
              style: FilledButton.styleFrom(
                backgroundColor: AppColors.primary,
                foregroundColor: Colors.white,
                disabledBackgroundColor: AppColors.textDisabled,
                padding: const EdgeInsets.symmetric(vertical: 15),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(16),
                ),
              ),
            ),
            const SizedBox(height: AppSpacing.xl),
            _buildNavigationTile(
              icon: Icons.notifications_outlined,
              title: 'Gestion des alertes',
              subtitle: 'Préférences OPTSMS',
              onTap: () => context.push(AppRouter.alerts),
            ),
            const SizedBox(height: AppSpacing.listItemSpacing),
            _buildNavigationTile(
              icon: Icons.location_on_outlined,
              title: 'Agences',
              subtitle: 'Carte, marqueurs et itinéraire',
              onTap: () => context.push(AppRouter.agencies),
            ),
            const SizedBox(height: AppSpacing.xl),
            OutlinedButton.icon(
              onPressed: () => showLogoutConfirmDialog(context),
              icon: const Icon(Icons.logout_rounded),
              label: const Text('Déconnexion'),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildHeaderCard() {
    return Container(
      padding: const EdgeInsets.all(AppSpacing.lg),
      decoration: BoxDecoration(
        color: AppColors.primary,
        borderRadius: BorderRadius.circular(AppSpacing.radiusXXLarge),
      ),
      child: Row(
        children: [
          const CircleAvatar(
            radius: 28,
            backgroundColor: Colors.white24,
            child: Icon(Icons.person_rounded, color: Colors.white, size: 34),
          ),
          const SizedBox(width: AppSpacing.md),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  _nameController.text.trim().isEmpty
                      ? 'Mon profil'
                      : _nameController.text.trim(),
                  style: const TextStyle(
                    color: Colors.white,
                    fontSize: 18,
                    fontWeight: FontWeight.w900,
                  ),
                ),
                if (_numCompteCourant.isNotEmpty) ...[
                  const SizedBox(height: 4),
                  Text(
                    'Compte courant · $_numCompteCourant',
                    style: TextStyle(
                      color: Colors.white.withValues(alpha: 0.85),
                      fontSize: 12,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                ],
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildField({
    required TextEditingController controller,
    required String label,
    required IconData icon,
    required bool enabled,
    TextInputType? keyboardType,
    int minLines = 1,
    int maxLines = 1,
    String? Function(String?)? validator,
  }) {
    return TextFormField(
      controller: controller,
      enabled: enabled,
      keyboardType: keyboardType,
      minLines: minLines,
      maxLines: maxLines,
      validator: validator,
      decoration: InputDecoration(
        labelText: label,
        prefixIcon: Icon(icon),
        filled: true,
        fillColor: enabled ? Colors.white : const Color(0xFFEFF3F8),
        border: OutlineInputBorder(borderRadius: BorderRadius.circular(18)),
      ),
    );
  }

  Widget _buildNavigationTile({
    required IconData icon,
    required String title,
    required String subtitle,
    required VoidCallback onTap,
  }) {
    return ListTile(
      onTap: onTap,
      tileColor: Colors.white,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(18)),
      leading: Icon(icon, color: AppColors.primary),
      title: Text(title, style: const TextStyle(fontWeight: FontWeight.w900)),
      subtitle: Text(subtitle),
      trailing: const Icon(Icons.chevron_right_rounded),
    );
  }
}
