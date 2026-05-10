import 'package:flutter/material.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:go_router/go_router.dart';
import 'package:local_auth/local_auth.dart';
import '../../../core/di/service_locator.dart';
import '../../../core/storage/secure_storage_service.dart';
import '../../../core/theme/app_theme.dart';
import '../../../domain/repositories/auth_repository.dart';

class RegisterScreen extends StatefulWidget {
  const RegisterScreen({super.key});

  @override
  State<RegisterScreen> createState() => _RegisterScreenState();
}

class _RegisterScreenState extends State<RegisterScreen> {
  final _formKey = GlobalKey<FormState>();
  final _nameController = TextEditingController();
  final _phoneController = TextEditingController();
  final _pinController = TextEditingController();
  final _confirmPinController = TextEditingController();
  final _localAuth = LocalAuthentication();
  final _secureStorage = SecureStorageService(const FlutterSecureStorage());
  bool _isFormValid = false;

  @override
  void initState() {
    super.initState();
    _nameController.addListener(_validate);
    _phoneController.addListener(_validate);
    _pinController.addListener(_validate);
    _confirmPinController.addListener(_validate);
  }

  @override
  void dispose() {
    _nameController.dispose();
    _phoneController.dispose();
    _pinController.dispose();
    _confirmPinController.dispose();
    super.dispose();
  }

  void _validate() {
    setState(() {
      _isFormValid = _nameController.text.length >= 3 &&
          _phoneController.text.isNotEmpty &&
          _pinController.text.length >= 4 &&
          _pinController.text == _confirmPinController.text;
    });
  }

  Future<void> _handleRegister() async {
    if (!_isFormValid) return;

    try {
      // 1. Inscription côté backend (POST /api/v1/auth/register-mobile).
      //    Si le compte existe déjà → erreur 409, on sort sans saisir biométrie.
      try {
        await sl<AuthRepository>().register(
          phone:      _phoneController.text,
          pin:        _pinController.text,
          nomComplet: _nameController.text,
        );
      } catch (e) {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text(e.toString().replaceFirst('Exception: ', '')),
              backgroundColor: AppTheme.errorRed,
            ),
          );
        }
        return;
      }

      // 2. Vérifier la disponibilité de la biométrie (optionnelle après register).
      final canCheck = await _localAuth.canCheckBiometrics;
      final isSupported = await _localAuth.isDeviceSupported();

      bool biometricOk = false;
      if (canCheck && isSupported) {
        try {
          biometricOk = await _localAuth.authenticate(
            localizedReason: 'Configurez votre biométrie pour sécuriser votre compte',
            biometricOnly: true,
            persistAcrossBackgrounding: true,
          );
        } catch (_) {
          biometricOk = false;
        }
      }

      // 3. Persistance locale (utilisée par le LoginScreen pour pré-remplir).
      await _secureStorage.saveAccountInfo(
        phone: _phoneController.text,
        name: _nameController.text,
        biometricEnabled: biometricOk,
      );
      await _secureStorage.saveLastPhone(_phoneController.text);
      await _secureStorage.setSecureMode(true);

      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(biometricOk
                ? 'Compte créé et biométrie configurée avec succès !'
                : 'Compte créé. Biométrie non activée (configurable plus tard).'),
            backgroundColor: AppTheme.successGreen,
          ),
        );
        context.pop();
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Erreur : $e')),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Container(
        width: double.infinity,
        decoration: BoxDecoration(
          gradient: LinearGradient(
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
            colors: [
              AppTheme.bgLight,
              AppTheme.lightBlue.withOpacity(0.5),
            ],
          ),
        ),
        child: SafeArea(
          child: CustomScrollView(
            physics: const BouncingScrollPhysics(),
            slivers: [
              SliverAppBar(
                leading: IconButton(
                  icon: const Icon(Icons.arrow_back_ios_new_rounded, color: AppTheme.primaryBlue),
                  onPressed: () => context.pop(),
                ),
                title: const Text('Créer un compte', style: TextStyle(color: AppTheme.primaryBlue, fontWeight: FontWeight.w900)),
                floating: true,
              ),
              SliverToBoxAdapter(
                child: Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 24.0),
                  child: Form(
                    key: _formKey,
                    child: Column(
                      children: [
                        const SizedBox(height: 24),
                        Container(
                          padding: const EdgeInsets.all(20),
                          decoration: BoxDecoration(
                            color: Colors.white,
                            shape: BoxShape.circle,
                            boxShadow: AppTheme.softShadow,
                          ),
                          child: const Icon(Icons.person_add_rounded, size: 40, color: AppTheme.accentBlue),
                        ),
                        const SizedBox(height: 24),
                        const Text(
                          'Bienvenue chez microCredit',
                          textAlign: TextAlign.center,
                          style: TextStyle(fontSize: 24, fontWeight: FontWeight.w900, color: AppTheme.primaryBlue, letterSpacing: -0.5),
                        ),
                        const SizedBox(height: 8),
                        Text(
                          'Sécurisez vos finances en quelques secondes.',
                          textAlign: TextAlign.center,
                          style: TextStyle(color: AppTheme.primaryBlue.withOpacity(0.5), fontSize: 14, fontWeight: FontWeight.w500),
                        ),
                        const SizedBox(height: 32),
                        
                        _buildPremiumInput(
                          controller: _nameController,
                          label: 'Nom complet',
                          icon: Icons.person_outline_rounded,
                        ),
                        const SizedBox(height: 20),
                        
                        _buildPremiumInput(
                          controller: _phoneController,
                          label: 'Numéro de téléphone',
                          icon: Icons.phone_android_rounded,
                          keyboardType: TextInputType.phone,
                        ),
                        const SizedBox(height: 20),
                        
                        _buildPremiumInput(
                          controller: _pinController,
                          label: 'Définir un Code PIN (4 chiffres)',
                          icon: Icons.lock_outline_rounded,
                          isPin: true,
                          keyboardType: TextInputType.number,
                        ),
                        const SizedBox(height: 20),
                        
                        _buildPremiumInput(
                          controller: _confirmPinController,
                          label: 'Confirmer le Code PIN',
                          icon: Icons.lock_clock_rounded,
                          isPin: true,
                          keyboardType: TextInputType.number,
                        ),
                        
                        const SizedBox(height: 56),
                        
                        ElevatedButton(
                          onPressed: _isFormValid ? _handleRegister : null,
                          style: ElevatedButton.styleFrom(
                            backgroundColor: AppTheme.primaryBlue,
                            shadowColor: AppTheme.primaryBlue.withOpacity(0.3),
                            elevation: 12,
                          ),
                          child: const Text('FINALISER L\'INSCRIPTION'),
                        ),
                        
                        const SizedBox(height: 32),
                        TextButton(
                          onPressed: () => context.pop(),
                          child: RichText(
                            text: TextSpan(
                              text: 'Déjà un compte ? ',
                              style: TextStyle(color: AppTheme.primaryBlue.withOpacity(0.5), fontWeight: FontWeight.w500),
                              children: const [
                                TextSpan(
                                  text: 'Se connecter',
                                  style: TextStyle(color: AppTheme.accentBlue, fontWeight: FontWeight.w800),
                                ),
                              ],
                            ),
                          ),
                        ),
                        const SizedBox(height: 24),
                      ],
                    ),
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildPremiumInput({
    required TextEditingController controller,
    required String label,
    required IconData icon,
    bool isPin = false,
    TextInputType? keyboardType,
  }) {
    return Container(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(24),
        boxShadow: AppTheme.softShadow,
      ),
      child: TextFormField(
        controller: controller,
        obscureText: isPin,
        keyboardType: keyboardType,
        style: const TextStyle(fontWeight: FontWeight.w700, color: AppTheme.primaryBlue),
        decoration: InputDecoration(
          labelText: label,
          prefixIcon: Icon(icon, color: AppTheme.accentBlue.withOpacity(0.5)),
          border: InputBorder.none,
          enabledBorder: InputBorder.none,
          focusedBorder: OutlineInputBorder(
            borderRadius: BorderRadius.circular(24),
            borderSide: const BorderSide(color: AppTheme.accentBlue, width: 2),
          ),
        ),
      ),
    );
  }
}
