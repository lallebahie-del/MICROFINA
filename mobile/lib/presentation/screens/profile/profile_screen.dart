import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import '../../../core/theme/app_theme.dart';
import '../../../core/storage/secure_storage_service.dart';
import '../../blocs/auth/auth_bloc.dart';
import '../../blocs/auth/auth_event.dart';

import '../../blocs/auth/auth_state.dart';

import 'dart:io';
import 'package:image_picker/image_picker.dart';

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
    
    // Récupérer les infos de l'utilisateur
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
          content: Text(value ? 'Mode haute sécurité activé' : 'Mode haute sécurité désactivé'),
          duration: const Duration(seconds: 2),
        ),
      );
    }
  }

  void _showLogoutDialog(BuildContext context) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Déconnexion'),
        content: const Text('Voulez-vous vraiment vous déconnecter de votre espace sécurisé ?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('ANNULER'),
          ),
          TextButton(
            onPressed: () {
              Navigator.pop(context);
              context.read<AuthBloc>().add(LogoutRequested());
            },
            child: const Text('DÉCONNEXION', style: TextStyle(color: AppTheme.errorRed)),
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Theme.of(context).scaffoldBackgroundColor,
      body: CustomScrollView(
        physics: const BouncingScrollPhysics(),
        slivers: [
          SliverAppBar(
            expandedHeight: 240,
            pinned: true,
            stretch: true,
            flexibleSpace: FlexibleSpaceBar(
              stretchModes: const [StretchMode.blurBackground, StretchMode.zoomBackground],
              background: Container(
                decoration: const BoxDecoration(
                  gradient: LinearGradient(
                    begin: Alignment.topLeft,
                    end: Alignment.bottomRight,
                    colors: [AppTheme.primaryBlue, Color(0xFF334155)],
                  ),
                ),
                child: Center(
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      const SizedBox(height: 40),
                      Stack(
                        children: [
                          Container(
                            padding: const EdgeInsets.all(4),
                            decoration: BoxDecoration(
                              shape: BoxShape.circle,
                              border: Border.all(color: AppTheme.accentBlue, width: 3),
                            ),
                            child: CircleAvatar(
                              radius: 50,
                              backgroundColor: AppTheme.surfaceDark,
                              backgroundImage: _photoPath != null ? FileImage(File(_photoPath!)) : null,
                              child: _photoPath == null 
                                ? const Icon(Icons.person_rounded, size: 60, color: Colors.white)
                                : null,
                            ),
                          ),
                          Positioned(
                            bottom: 0,
                            right: 0,
                            child: GestureDetector(
                              onTap: _pickImage,
                              child: Container(
                                padding: const EdgeInsets.all(8),
                                decoration: const BoxDecoration(
                                  color: AppTheme.accentBlue,
                                  shape: BoxShape.circle,
                                ),
                                child: const Icon(Icons.camera_alt_rounded, size: 18, color: Colors.white),
                              ),
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 16),
                      Text(
                        _userName ?? 'Utilisateur',
                        style: const TextStyle(color: Colors.white, fontSize: 22, fontWeight: FontWeight.w900),
                      ),
                      Text(
                        _userPhone ?? '',
                        style: TextStyle(color: Colors.white.withOpacity(0.6), fontSize: 14, fontWeight: FontWeight.w500),
                      ),
                    ],
                  ),
                ),
              ),
            ),
          ),
          SliverToBoxAdapter(
            child: Padding(
              padding: const EdgeInsets.all(32.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text(
                    'Paramètres du compte',
                    style: TextStyle(fontSize: 18, fontWeight: FontWeight.w900, color: AppTheme.primaryBlue),
                  ),
                  const SizedBox(height: 24),
                  _buildPremiumProfileItem(
                    icon: Icons.person_outline_rounded,
                    title: 'Informations personnelles',
                    color: Colors.blue,
                    onTap: () => _showProfileDetail('Informations personnelles'),
                  ),
                  const SizedBox(height: 16),
                  Container(
                    padding: const EdgeInsets.all(8),
                    decoration: BoxDecoration(
                      color: Colors.white,
                      borderRadius: BorderRadius.circular(24),
                      boxShadow: AppTheme.softShadow,
                    ),
                    child: SwitchListTile(
                      secondary: Container(
                        padding: const EdgeInsets.all(10),
                        decoration: BoxDecoration(color: Colors.orange.withOpacity(0.1), borderRadius: BorderRadius.circular(12)),
                        child: const Icon(Icons.security_rounded, color: Colors.orange, size: 20),
                      ),
                      title: const Text('Sécurité maximale', style: TextStyle(fontWeight: FontWeight.w800, fontSize: 15, color: AppTheme.primaryBlue)),
                      subtitle: const Text('Anti-capture & Floutage', style: TextStyle(fontSize: 12, fontWeight: FontWeight.w500)),
                      value: _isSecureMode,
                      onChanged: _toggleSecureMode,
                      activeColor: AppTheme.accentBlue,
                    ),
                  ),
                  const SizedBox(height: 16),
                  _buildPremiumProfileItem(
                    icon: Icons.notifications_none_rounded,
                    title: 'Notifications',
                    color: Colors.purple,
                    onTap: () => _showProfileDetail('Notifications'),
                  ),
                  const SizedBox(height: 40),
                  const Text(
                    'Support',
                    style: TextStyle(fontSize: 18, fontWeight: FontWeight.w900, color: AppTheme.primaryBlue),
                  ),
                  const SizedBox(height: 24),
                  _buildPremiumProfileItem(
                    icon: Icons.help_outline_rounded,
                    title: 'Aide & Support',
                    color: Colors.green,
                    onTap: () => _showProfileDetail('Aide & Support'),
                  ),
                  const SizedBox(height: 16),
                  _buildPremiumProfileItem(
                    icon: Icons.info_outline_rounded,
                    title: 'À propos',
                    color: Colors.grey,
                    onTap: () => _showProfileDetail('À propos'),
                  ),
                  const SizedBox(height: 48),
                  ElevatedButton.icon(
                    onPressed: () => _showLogoutDialog(context),
                    icon: const Icon(Icons.logout_rounded),
                    label: const Text('DÉCONNEXION SÉCURISÉE'),
                    style: ElevatedButton.styleFrom(
                      backgroundColor: Colors.white,
                      foregroundColor: AppTheme.errorRed,
                      side: const BorderSide(color: AppTheme.errorRed, width: 2),
                    ),
                  ),
                  const SizedBox(height: 30),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  void _showProfileDetail(String title) {
    showModalBottomSheet(
      context: context,
      backgroundColor: Colors.transparent,
      isScrollControlled: true,
      builder: (context) => Container(
        height: MediaQuery.of(context).size.height * 0.7,
        padding: const EdgeInsets.all(32),
        decoration: const BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.only(
            topLeft: Radius.circular(40),
            topRight: Radius.circular(40),
          ),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Center(
              child: Container(
                width: 40,
                height: 4,
                decoration: BoxDecoration(
                  color: Colors.grey[300],
                  borderRadius: BorderRadius.circular(2),
                ),
              ),
            ),
            const SizedBox(height: 32),
            Text(
              title,
              style: const TextStyle(fontSize: 24, fontWeight: FontWeight.w900, color: AppTheme.primaryBlue),
            ),
            const SizedBox(height: 24),
            Expanded(
              child: _buildDetailContent(title),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildDetailContent(String title) {
    switch (title) {
      case 'Informations personnelles':
        return ListView(
          children: [
            _buildDetailTile(Icons.person_outline_rounded, 'Nom Complet', _userName ?? 'Non renseigné'),
            _buildDetailTile(Icons.phone_android_rounded, 'Téléphone', _userPhone ?? 'Non renseigné'),
            _buildDetailTile(Icons.email_rounded, 'Email', 'contact@microfina.com'),
            _buildDetailTile(Icons.location_on_rounded, 'Adresse', 'Dakar, Sénégal'),
          ],
        );
      case 'Notifications':
        return ListView(
          children: [
            _buildDetailTile(Icons.notifications_active_rounded, 'Alertes de compte', 'Activé'),
            _buildDetailTile(Icons.security_rounded, 'Sécurité', 'Activé'),
            _buildDetailTile(Icons.campaign_rounded, 'Offres promotionnelles', 'Désactivé'),
          ],
        );
      case 'Aide & Support':
        return ListView(
          children: [
            _buildDetailTile(Icons.chat_bubble_rounded, 'Chat en direct', 'Disponible 24/7'),
            _buildDetailTile(Icons.call_rounded, 'Appeler le support', '+221 33 800 00 00'),
            _buildDetailTile(Icons.help_center_rounded, 'FAQ', 'Consulter les questions fréquentes'),
          ],
        );
      case 'À propos':
        return ListView(
          children: [
            _buildDetailTile(Icons.info_rounded, 'Version', '1.0.0+1'),
            _buildDetailTile(Icons.description_rounded, 'Conditions d\'utilisation', 'Lire les CGU'),
            _buildDetailTile(Icons.privacy_tip_rounded, 'Politique de confidentialité', 'Lire la politique'),
          ],
        );
      default:
        return Center(child: Text('Détails pour $title bientôt disponibles.'));
    }
  }

  Widget _buildDetailTile(IconData icon, String label, String value) {
    return ListTile(
      leading: Icon(icon, color: AppTheme.accentBlue),
      title: Text(label, style: const TextStyle(fontSize: 14, color: Colors.grey)),
      subtitle: Text(value, style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold, color: AppTheme.primaryBlue)),
      contentPadding: const EdgeInsets.symmetric(vertical: 8),
    );
  }

  Widget _buildPremiumProfileItem({
    required IconData icon,
    required String title,
    required Color color,
    required VoidCallback onTap,
  }) {
    return Container(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(24),
        boxShadow: AppTheme.softShadow,
      ),
      child: ListTile(
        onTap: onTap,
        contentPadding: const EdgeInsets.symmetric(horizontal: 20, vertical: 8),
        leading: Container(
          padding: const EdgeInsets.all(10),
          decoration: BoxDecoration(
            color: color.withOpacity(0.1),
            borderRadius: BorderRadius.circular(14),
          ),
          child: Icon(icon, color: color, size: 22),
        ),
        title: Text(title, style: const TextStyle(fontWeight: FontWeight.w800, fontSize: 15, color: AppTheme.primaryBlue)),
        trailing: const Icon(Icons.arrow_forward_ios_rounded, size: 14, color: Colors.grey),
      ),
    );
  }
}
