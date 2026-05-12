import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:image_picker/image_picker.dart';

import '../../../core/storage/secure_storage_service.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_shadows.dart';
import '../../../core/theme/app_spacing.dart';
import '../../blocs/auth/auth_bloc.dart';
import '../../widgets/logout_confirm_dialog.dart';

class ProfileScreen extends StatefulWidget {
  const ProfileScreen({super.key});

  @override
  State<ProfileScreen> createState() => _ProfileScreenState();
}

class _ProfileScreenState extends State<ProfileScreen> {
  final _secureStorage = SecureStorageService(const FlutterSecureStorage());
  bool _isSecureMode = true;
  String? _userName;
  String? _userPhone;
  String? _photoPath;
  final ImagePicker _picker = ImagePicker();

  @override
  void initState() {
    super.initState();
    _loadData();
  }

  Future<void> _loadData() async {
    final mode = await _secureStorage.getSecureMode();
    if (!mounted) return;

    final authState = context.read<AuthBloc>().state;
    String? name;
    String? phone;
    String? photo;

    if (authState is AuthSuccess && authState.phone != null) {
      phone = authState.phone;
      name = await _secureStorage.getUserName(phone!);
      photo = await _secureStorage.getUserPhoto(phone);
    }

    if (mounted) {
      setState(() {
        _isSecureMode = mode;
        _userName = name;
        _userPhone = phone;
        _photoPath = photo;
      });
    }
  }

  Future<void> _pickImage() async {
    final XFile? image = await _picker.pickImage(source: ImageSource.gallery);
    if (image != null && _userPhone != null) {
      await _secureStorage.saveUserPhoto(_userPhone!, image.path);
      setState(() {
        _photoPath = image.path;
      });
    }
  }

  Future<void> _toggleSecureMode(bool value) async {
    await _secureStorage.setSecureMode(value);
    setState(() {
      _isSecureMode = value;
    });
    if (mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(
            value ? 'Mode haute sécurité activé' : 'Mode haute sécurité désactivé',
          ),
          behavior: SnackBarBehavior.floating,
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
          margin: const EdgeInsets.all(16),
        ),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final bottomInset = MediaQuery.paddingOf(context).bottom;

    return Scaffold(
      backgroundColor: const Color(0xFFF1F5F9),
      body: CustomScrollView(
        physics: const BouncingScrollPhysics(),
        slivers: [
          SliverAppBar(
            expandedHeight: 268,
            pinned: true,
            stretch: true,
            elevation: 0,
            scrolledUnderElevation: 0,
            backgroundColor: AppColors.primary,
            surfaceTintColor: Colors.transparent,
            iconTheme: const IconThemeData(color: Colors.white),
            title: Text(
              'Profil',
              style: TextStyle(
                color: Colors.white.withOpacity(0.95),
                fontWeight: FontWeight.w800,
                fontSize: 17,
                letterSpacing: 0.2,
              ),
            ),
            centerTitle: true,
            flexibleSpace: FlexibleSpaceBar(
              stretchModes: const [
                StretchMode.blurBackground,
                StretchMode.zoomBackground,
              ],
              background: Container(
                decoration: const BoxDecoration(
                  gradient: LinearGradient(
                    begin: Alignment.topLeft,
                    end: Alignment.bottomRight,
                    colors: [
                      AppColors.primary,
                      Color(0xFF334155),
                    ],
                  ),
                ),
                child: SafeArea(
                  bottom: false,
                  child: Padding(
                    padding: const EdgeInsets.fromLTRB(24, 12, 24, 48),
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.end,
                      children: [
                        Stack(
                          clipBehavior: Clip.none,
                          children: [
                            Container(
                              padding: const EdgeInsets.all(3),
                              decoration: BoxDecoration(
                                shape: BoxShape.circle,
                                border: Border.all(
                                  color: Colors.white.withOpacity(0.45),
                                  width: 2.5,
                                ),
                                boxShadow: [
                                  BoxShadow(
                                    color: Colors.black.withOpacity(0.2),
                                    blurRadius: 20,
                                    offset: const Offset(0, 10),
                                  ),
                                ],
                              ),
                              child: CircleAvatar(
                                radius: 48,
                                backgroundColor: const Color(0xFF475569),
                                backgroundImage:
                                    _photoPath != null ? FileImage(File(_photoPath!)) : null,
                                child: _photoPath == null
                                    ? const Icon(
                                        Icons.person_rounded,
                                        size: 56,
                                        color: Colors.white,
                                      )
                                    : null,
                              ),
                            ),
                            Positioned(
                              bottom: 2,
                              right: 2,
                              child: Material(
                                color: Colors.white,
                                shape: const CircleBorder(),
                                elevation: 4,
                                shadowColor: Colors.black26,
                                child: InkWell(
                                  customBorder: const CircleBorder(),
                                  onTap: _pickImage,
                                  child: Padding(
                                    padding: const EdgeInsets.all(10),
                                    child: Icon(
                                      Icons.camera_alt_rounded,
                                      size: 18,
                                      color: AppColors.primary,
                                    ),
                                  ),
                                ),
                              ),
                            ),
                          ],
                        ),
                        const SizedBox(height: AppSpacing.md),
                        Text(
                          _userName ?? 'Utilisateur',
                          textAlign: TextAlign.center,
                          style: const TextStyle(
                            color: Colors.white,
                            fontSize: 22,
                            fontWeight: FontWeight.w900,
                            letterSpacing: -0.3,
                          ),
                        ),
                        const SizedBox(height: 4),
                        Text(
                          _userPhone ?? '—',
                          textAlign: TextAlign.center,
                          style: TextStyle(
                            color: Colors.white.withOpacity(0.72),
                            fontSize: 14,
                            fontWeight: FontWeight.w600,
                            letterSpacing: 0.3,
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
              ),
            ),
          ),
          SliverToBoxAdapter(
            child: Transform.translate(
              offset: const Offset(0, -28),
              child: Padding(
                padding: const EdgeInsets.symmetric(horizontal: AppSpacing.screenPadding),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    const SizedBox(height: AppSpacing.sm),
                    _sectionHeader('Paramètres du compte'),
                    const SizedBox(height: AppSpacing.md),
                    _buildMenuTile(
                      icon: Icons.person_outline_rounded,
                      title: 'Informations personnelles',
                      subtitle: 'Identité et coordonnées',
                      onTap: () => _showProfileDetail('Informations personnelles'),
                    ),
                    const SizedBox(height: AppSpacing.listItemSpacing),
                    _buildSecurityTile(),
                    const SizedBox(height: AppSpacing.listItemSpacing),
                    _buildMenuTile(
                      icon: Icons.notifications_outlined,
                      title: 'Notifications',
                      subtitle: 'Préférences d’alertes',
                      onTap: () => _showProfileDetail('Notifications'),
                    ),
                    const SizedBox(height: AppSpacing.sectionSpacing),
                    _sectionHeader('Support'),
                    const SizedBox(height: AppSpacing.md),
                    _buildMenuTile(
                      icon: Icons.headset_mic_outlined,
                      title: 'Aide & Support',
                      subtitle: 'FAQ, chat et téléphone',
                      onTap: () => _showProfileDetail('Aide & Support'),
                    ),
                    const SizedBox(height: AppSpacing.listItemSpacing),
                    _buildMenuTile(
                      icon: Icons.info_outline_rounded,
                      title: 'À propos',
                      subtitle: 'Version et mentions légales',
                      onTap: () => _showProfileDetail('À propos'),
                    ),
                    const SizedBox(height: AppSpacing.xxl),
                    SizedBox(
                      width: double.infinity,
                      height: 54,
                      child: FilledButton.icon(
                        onPressed: () => showLogoutConfirmDialog(context),
                        icon: const Icon(Icons.logout_rounded, size: 21),
                        label: const Text(
                          'Déconnexion sécurisée',
                          style: TextStyle(
                            fontWeight: FontWeight.w800,
                            letterSpacing: 0.2,
                          ),
                        ),
                        style: FilledButton.styleFrom(
                          backgroundColor: AppColors.error,
                          foregroundColor: Colors.white,
                          elevation: 0,
                          shadowColor: Colors.transparent,
                          shape: RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(18),
                          ),
                        ),
                      ),
                    ),
                    SizedBox(height: bottomInset + AppSpacing.xl),
                  ],
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _sectionHeader(String title) {
    return Padding(
      padding: const EdgeInsets.only(left: 4),
      child: Text(
        title.toUpperCase(),
        style: TextStyle(
          fontSize: 11,
          fontWeight: FontWeight.w900,
          letterSpacing: 1.4,
          color: AppColors.primary.withOpacity(0.38),
        ),
      ),
    );
  }

  Widget _buildMenuTile({
    required IconData icon,
    required String title,
    required String subtitle,
    required VoidCallback onTap,
  }) {
    return Material(
      color: Colors.white,
      borderRadius: BorderRadius.circular(22),
      clipBehavior: Clip.antiAlias,
      elevation: 0,
      shadowColor: Colors.transparent,
      child: InkWell(
        onTap: onTap,
        child: Ink(
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(22),
            border: Border.all(color: AppColors.borderLight),
            boxShadow: AppShadows.soft,
          ),
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 18, vertical: 14),
            child: Row(
              children: [
                Container(
                  padding: const EdgeInsets.all(12),
                  decoration: BoxDecoration(
                    color: AppColors.primary.withOpacity(0.06),
                    borderRadius: BorderRadius.circular(16),
                  ),
                  child: Icon(icon, color: AppColors.primary, size: 22),
                ),
                const SizedBox(width: 16),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        title,
                        style: const TextStyle(
                          fontWeight: FontWeight.w800,
                          fontSize: 15,
                          color: AppColors.primary,
                          letterSpacing: -0.2,
                        ),
                      ),
                      const SizedBox(height: 2),
                      Text(
                        subtitle,
                        style: TextStyle(
                          fontSize: 12.5,
                          fontWeight: FontWeight.w500,
                          color: AppColors.textSecondary.withOpacity(0.88),
                        ),
                      ),
                    ],
                  ),
                ),
                Icon(
                  Icons.chevron_right_rounded,
                  color: AppColors.textTertiary.withOpacity(0.7),
                  size: 26,
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildSecurityTile() {
    return Container(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(22),
        border: Border.all(color: AppColors.borderLight),
        boxShadow: AppShadows.soft,
      ),
      child: SwitchListTile.adaptive(
        contentPadding: const EdgeInsets.symmetric(horizontal: 18, vertical: 4),
        secondary: Container(
          padding: const EdgeInsets.all(12),
          decoration: BoxDecoration(
            color: AppColors.warning.withOpacity(0.12),
            borderRadius: BorderRadius.circular(16),
          ),
          child: const Icon(Icons.shield_outlined, color: AppColors.warning, size: 22),
        ),
        title: const Text(
          'Sécurité maximale',
          style: TextStyle(
            fontWeight: FontWeight.w800,
            fontSize: 15,
            color: AppColors.primary,
            letterSpacing: -0.2,
          ),
        ),
        subtitle: Text(
          'Anti-capture et floutage de l’interface',
          style: TextStyle(
            fontSize: 12.5,
            fontWeight: FontWeight.w500,
            color: AppColors.textSecondary.withOpacity(0.88),
          ),
        ),
        value: _isSecureMode,
        onChanged: _toggleSecureMode,
        activeThumbColor: Colors.white,
        activeTrackColor: AppColors.primary,
        inactiveThumbColor: Colors.white,
        inactiveTrackColor: AppColors.textTertiary.withOpacity(0.35),
      ),
    );
  }

  void _showProfileDetail(String title) {
    showModalBottomSheet<void>(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (context) => DraggableScrollableSheet(
        initialChildSize: 0.72,
        minChildSize: 0.45,
        maxChildSize: 0.92,
        expand: false,
        builder: (context, scrollController) {
          return Container(
            decoration: const BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.vertical(top: Radius.circular(28)),
              boxShadow: [
                BoxShadow(
                  color: Color(0x14000000),
                  blurRadius: 24,
                  offset: Offset(0, -4),
                ),
              ],
            ),
            child: Column(
              children: [
                const SizedBox(height: 10),
                Container(
                  width: 40,
                  height: 4,
                  decoration: BoxDecoration(
                    color: AppColors.border,
                    borderRadius: BorderRadius.circular(2),
                  ),
                ),
                Padding(
                  padding: const EdgeInsets.fromLTRB(24, 20, 24, 8),
                  child: Row(
                    children: [
                      Expanded(
                        child: Text(
                          title,
                          style: const TextStyle(
                            fontSize: 20,
                            fontWeight: FontWeight.w900,
                            color: AppColors.primary,
                            letterSpacing: -0.3,
                          ),
                        ),
                      ),
                      IconButton.filledTonal(
                        onPressed: () => Navigator.pop(context),
                        style: IconButton.styleFrom(
                          backgroundColor: AppColors.primary.withOpacity(0.08),
                          foregroundColor: AppColors.primary,
                        ),
                        icon: const Icon(Icons.close_rounded, size: 22),
                      ),
                    ],
                  ),
                ),
                Expanded(
                  child: ListView(
                    controller: scrollController,
                    padding: const EdgeInsets.fromLTRB(20, 8, 20, 28),
                    children: _buildDetailContentList(title),
                  ),
                ),
              ],
            ),
          );
        },
      ),
    );
  }

  List<Widget> _buildDetailContentList(String title) {
    switch (title) {
      case 'Informations personnelles':
        return [
          _buildDetailCard(Icons.person_outline_rounded, 'Nom complet', _userName ?? 'Non renseigné'),
          _buildDetailCard(Icons.phone_android_rounded, 'Téléphone', _userPhone ?? 'Non renseigné'),
          _buildDetailCard(Icons.email_outlined, 'E-mail', 'contact@microfina.com'),
          _buildDetailCard(Icons.location_on_outlined, 'Adresse', 'Dakar, Sénégal'),
        ];
      case 'Notifications':
        return [
          _buildDetailCard(Icons.notifications_active_outlined, 'Alertes de compte', 'Activé'),
          _buildDetailCard(Icons.shield_outlined, 'Sécurité', 'Activé'),
          _buildDetailCard(Icons.local_offer_outlined, 'Offres promotionnelles', 'Désactivé'),
        ];
      case 'Aide & Support':
        return [
          _buildDetailCard(Icons.chat_bubble_outline_rounded, 'Chat en direct', 'Disponible 24h/24'),
          _buildDetailCard(Icons.call_outlined, 'Support téléphonique', '+221 33 800 00 00'),
          _buildDetailCard(Icons.help_center_outlined, 'FAQ', 'Questions fréquentes'),
        ];
      case 'À propos':
        return [
          _buildDetailCard(Icons.tag_outlined, 'Version', '1.0.0+1'),
          _buildDetailCard(Icons.description_outlined, 'Conditions d’utilisation', 'Consulter les CGU'),
          _buildDetailCard(Icons.privacy_tip_outlined, 'Confidentialité', 'Politique de protection des données'),
        ];
      default:
        return [
          Padding(
            padding: const EdgeInsets.all(24),
            child: Text(
              'Détails pour $title bientôt disponibles.',
              style: TextStyle(color: Colors.grey[600], fontSize: 15),
            ),
          ),
        ];
    }
  }

  Widget _buildDetailCard(IconData icon, String label, String value) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: Container(
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          color: const Color(0xFFF8FAFC),
          borderRadius: BorderRadius.circular(18),
          border: Border.all(color: AppColors.borderLight),
        ),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Container(
              padding: const EdgeInsets.all(10),
              decoration: BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.circular(14),
                border: Border.all(color: AppColors.borderLight),
              ),
              child: Icon(icon, color: AppColors.primary, size: 22),
            ),
            const SizedBox(width: 14),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    label,
                    style: TextStyle(
                      fontSize: 12,
                      fontWeight: FontWeight.w700,
                      color: AppColors.textSecondary.withOpacity(0.85),
                      letterSpacing: 0.2,
                    ),
                  ),
                  const SizedBox(height: 6),
                  Text(
                    value,
                    style: const TextStyle(
                      fontSize: 16,
                      fontWeight: FontWeight.w800,
                      color: AppColors.primary,
                      height: 1.25,
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
