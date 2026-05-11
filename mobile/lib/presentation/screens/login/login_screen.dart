import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:go_router/go_router.dart';
import 'package:intl/intl.dart';
import 'package:local_auth/local_auth.dart';
import '../../../core/router/app_router.dart';
import '../../../core/storage/secure_storage_service.dart';
import '../../../core/theme/app_theme.dart';
import '../../blocs/auth/auth_bloc.dart';
import '../../widgets/numeric_keypad.dart';

class LoginScreen extends StatefulWidget {
  const LoginScreen({super.key});

  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  final _phoneController = TextEditingController();
  final _pinController = TextEditingController();
  final _localAuth = LocalAuthentication();
  final _secureStorage = SecureStorageService(const FlutterSecureStorage());
  bool _isPinMode = false;
  bool _isFormValid = false;
  String? _lastPhone;
  DateTime? _lastLoginDate;
  String? _userName;
  List<String> _registeredPhones = [];

  @override
  void initState() {
    super.initState();
    _loadInitialData();
    _phoneController.addListener(_validate);
    _pinController.addListener(_validate);
  }

  Future<void> _loadInitialData() async {
    final phone = await _secureStorage.getLastPhone();
    final lastLogin = await _secureStorage.getLastLoginDate();
    final phones = await _secureStorage.getRegisteredPhones();
    
    String? name;
    if (phone != null) {
      name = await _secureStorage.getUserName(phone);
    }

    if (mounted) {
      setState(() {
        _lastLoginDate = lastLogin;
        _registeredPhones = phones;
        if (phone != null) {
          _lastPhone = phone;
          _userName = name;
          _phoneController.text = phone;
          _isPinMode = true;
        }
      });
    }
  }

  Future<void> _selectAccount(String phone) async {
    final name = await _secureStorage.getUserName(phone);
    setState(() {
      _lastPhone = phone;
      _userName = name;
      _phoneController.text = phone;
      _isPinMode = true;
    });
  }

  void _showAccountSelector() {
    showModalBottomSheet(
      context: context,
      backgroundColor: Colors.transparent,
      builder: (context) => Container(
        padding: const EdgeInsets.all(24),
        decoration: const BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.only(
            topLeft: Radius.circular(30),
            topRight: Radius.circular(30),
          ),
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              'Choisir un compte',
              style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold, color: AppTheme.primaryBlue),
            ),
            const SizedBox(height: 20),
            Flexible(
              child: ListView.builder(
                shrinkWrap: true,
                itemCount: _registeredPhones.length,
                itemBuilder: (context, index) {
                  final phone = _registeredPhones[index];
                  return FutureBuilder<String?>(
                    future: _secureStorage.getUserName(phone),
                    builder: (context, snapshot) {
                      return ListTile(
                        onTap: () {
                          Navigator.pop(context);
                          _selectAccount(phone);
                        },
                        leading: const CircleAvatar(
                          backgroundColor: AppTheme.bgLight,
                          child: Icon(Icons.person_rounded, color: AppTheme.accentBlue),
                        ),
                        title: Text(snapshot.data ?? phone, style: const TextStyle(fontWeight: FontWeight.bold)),
                        subtitle: Text(phone),
                        trailing: phone == _lastPhone 
                          ? const Icon(Icons.check_circle_rounded, color: AppTheme.successGreen)
                          : null,
                      );
                    },
                  );
                },
              ),
            ),
            const SizedBox(height: 20),
          ],
        ),
      ),
    );
  }

  @override
  void dispose() {
    _phoneController.dispose();
    _pinController.dispose();
    super.dispose();
  }

  void _validate() {
    setState(() {
      _isFormValid = _phoneController.text.isNotEmpty && _pinController.text.length >= 4;
    });
  }

  void _onLoginPressed() async {
    if (_isFormValid) {
      await _secureStorage.saveLastPhone(_phoneController.text);
      await _secureStorage.saveLastLoginDate(DateTime.now());
      if (mounted) {
        context.read<AuthBloc>().add(
              LoginRequested(
                phone: _phoneController.text,
                pin: _pinController.text,
              ),
            );
      }
    }
  }

  Future<void> _authenticateBiometrically() async {
    try {
      final canCheck = await _localAuth.canCheckBiometrics;
      final isSupported = await _localAuth.isDeviceSupported();

      if (canCheck && isSupported) {
        final didAuthenticate = await _localAuth.authenticate(
          localizedReason: 'Authentifiez-vous pour accéder à votre espace microCredit',
          options: const AuthenticationOptions(
            biometricOnly: true,
            stickyAuth: true,
          ),
        );

        if (didAuthenticate && mounted) {
          // Simulation succès biométrie
          await _secureStorage.saveToken("eyJhbGciOi...biometric_mock_token");
          
          if (mounted) {
            context.read<AuthBloc>().add(AppStarted());
            context.go(AppRouter.dashboard);
          }
        }
      } else {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('La biométrie n\'est pas configurée sur cet appareil')),
          );
        }
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Erreur biométrie: $e')),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return BlocListener<AuthBloc, AuthState>(
      listener: (context, state) {
        if (state is AuthFailure) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text(state.message),
              backgroundColor: AppTheme.errorRed,
              behavior: SnackBarBehavior.floating,
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(15)),
            ),
          );
        }
      },
      child: Scaffold(
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
            child: SingleChildScrollView(
              physics: const BouncingScrollPhysics(),
              child: Padding(
                padding: const EdgeInsets.symmetric(horizontal: 24.0),
                child: Column(
                  children: [
                    const SizedBox(height: 40),
                    // Logo animée ou stylisée
                    Container(
                      padding: const EdgeInsets.all(20),
                      decoration: BoxDecoration(
                        color: Colors.white,
                        borderRadius: BorderRadius.circular(28),
                        boxShadow: AppTheme.softShadow,
                      ),
                      child: const Icon(
                        Icons.account_balance_wallet_rounded,
                        size: 40,
                        color: AppTheme.accentBlue,
                      ),
                    ),
                    const SizedBox(height: 24),
                    const Text(
                      'microCredit',
                      style: TextStyle(
                        fontSize: 32,
                        fontWeight: FontWeight.w900,
                        color: AppTheme.primaryBlue,
                        letterSpacing: -1,
                      ),
                    ),
                    const SizedBox(height: 8),
                    Text(
                      'Votre banque, partout avec vous.',
                      textAlign: TextAlign.center,
                      style: TextStyle(
                        fontSize: 14,
                        color: AppTheme.primaryBlue.withOpacity(0.5),
                        fontWeight: FontWeight.w500,
                      ),
                    ),
                    const SizedBox(height: 40),
                    
                    if (_lastPhone != null) ...[
                      Container(
                        padding: const EdgeInsets.all(20),
                        decoration: BoxDecoration(
                          color: Colors.white,
                          borderRadius: BorderRadius.circular(24),
                          boxShadow: AppTheme.softShadow,
                          border: Border.all(color: AppTheme.accentBlue.withOpacity(0.1)),
                        ),
                        child: Column(
                          children: [
                            Row(
                              children: [
                                Container(
                                  padding: const EdgeInsets.all(10),
                                  decoration: BoxDecoration(
                                    color: AppTheme.lightBlue,
                                    borderRadius: BorderRadius.circular(14),
                                  ),
                                  child: const Icon(Icons.person_rounded, color: AppTheme.accentBlue),
                                ),
                                const SizedBox(width: 16),
                                Expanded(
                                  child: Column(
                                    crossAxisAlignment: CrossAxisAlignment.start,
                                    children: [
                                      Text(
                                        _userName != null ? 'Heureux de vous revoir,' : 'Bon retour,', 
                                        style: TextStyle(fontSize: 12, color: AppTheme.primaryBlue.withOpacity(0.4), fontWeight: FontWeight.w600)
                                      ),
                                      Text(
                                        _userName ?? _lastPhone!, 
                                        style: const TextStyle(fontWeight: FontWeight.w800, fontSize: 17, color: AppTheme.primaryBlue)
                                      ),
                                    ],
                                  ),
                                ),
                                if (_registeredPhones.length > 1)
                                  IconButton(
                                    onPressed: () => _showAccountSelector(),
                                    icon: const Icon(Icons.swap_horiz_rounded, color: AppTheme.accentBlue),
                                    style: IconButton.styleFrom(backgroundColor: AppTheme.lightBlue),
                                  ),
                              ],
                            ),
                            const SizedBox(height: 15),
                            TextButton(
                              onPressed: () => setState(() {
                                _lastPhone = null;
                                _userName = null;
                                _phoneController.clear();
                                _isPinMode = false;
                              }),
                              child: const Text('Utiliser un autre compte', style: TextStyle(fontWeight: FontWeight.bold, color: AppTheme.accentBlue)),
                            ),
                          ],
                        ),
                      ),
                      const SizedBox(height: 32),
                    ],

                    // Phone Input
                    if (_lastPhone == null)
                      _buildPremiumTextField(
                        controller: _phoneController,
                        label: 'Téléphone',
                        icon: Icons.phone_android_rounded,
                        keyboardType: TextInputType.phone,
                        onTap: () => setState(() => _isPinMode = false),
                      ),
                    
                    if (_lastPhone == null) const SizedBox(height: 20),
                    
                    // PIN Input
                    _buildPremiumTextField(
                      controller: _pinController,
                      label: 'Code PIN Secret',
                      icon: Icons.lock_person_rounded,
                      isPin: true,
                      onTap: () => setState(() => _isPinMode = true),
                      suffixIcon: Container(
                        margin: const EdgeInsets.only(right: 8),
                        child: IconButton(
                          onPressed: _authenticateBiometrically,
                          icon: const Icon(Icons.fingerprint_rounded, size: 28, color: AppTheme.accentBlue),
                          style: IconButton.styleFrom(backgroundColor: AppTheme.lightBlue),
                        ),
                      ),
                    ),
                    
                    const SizedBox(height: 48),
                    
                    // Login Button
                    BlocBuilder<AuthBloc, AuthState>(
                      builder: (context, state) {
                        if (state is AuthLoading) {
                          return const Center(child: CircularProgressIndicator(color: AppTheme.accentBlue));
                        }
                        return Column(
                          children: [
                            ElevatedButton(
                              onPressed: _isFormValid ? _onLoginPressed : null,
                              style: ElevatedButton.styleFrom(
                                backgroundColor: AppTheme.primaryBlue,
                                shadowColor: AppTheme.primaryBlue.withOpacity(0.3),
                                elevation: 12,
                              ),
                              child: const Text('ACCÉDER À MON COMPTE'),
                            ),
                            const SizedBox(height: 32),
                            GestureDetector(
                              onTap: () => context.push(AppRouter.register),
                              child: RichText(
                                text: TextSpan(
                                  text: 'Nouveau ici ? ',
                                  style: TextStyle(color: AppTheme.primaryBlue.withOpacity(0.5), fontWeight: FontWeight.w500),
                                  children: const [
                                    TextSpan(
                                      text: 'Ouvrir un compte',
                                      style: TextStyle(color: AppTheme.accentBlue, fontWeight: FontWeight.w800),
                                    ),
                                  ],
                                ),
                              ),
                            ),
                          ],
                        );
                      },
                    ),
                    
                    if (_isPinMode) ...[
                      const SizedBox(height: 32),
                      NumericKeypad(
                        shuffle: false,
                        onNumberPressed: (val) {
                          if (_pinController.text.length < 4) {
                            _pinController.text += val;
                          }
                        },
                        onDeletePressed: () {
                          if (_pinController.text.isNotEmpty) {
                            _pinController.text = _pinController.text.substring(0, _pinController.text.length - 1);
                          }
                        },
                      ),
                    ],
                    const SizedBox(height: 20),
                  ],
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildPremiumTextField({
    required TextEditingController controller,
    required String label,
    required IconData icon,
    bool isPin = false,
    TextInputType? keyboardType,
    VoidCallback? onTap,
    Widget? suffixIcon,
  }) {
    return Container(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(24),
        boxShadow: AppTheme.softShadow,
      ),
      child: TextFormField(
        controller: controller,
        readOnly: isPin,
        obscureText: isPin,
        keyboardType: keyboardType,
        onTap: onTap,
        style: TextStyle(
          fontWeight: FontWeight.w800,
          letterSpacing: isPin ? 12 : 0.5,
          fontSize: isPin ? 24 : 16,
          color: AppTheme.primaryBlue,
        ),
        decoration: InputDecoration(
          labelText: label,
          prefixIcon: Icon(icon, color: AppTheme.accentBlue.withOpacity(0.5)),
          suffixIcon: suffixIcon,
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
