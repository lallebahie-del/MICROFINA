import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';
import '../../../core/router/app_router.dart';
import '../../../core/auth/biometric_auth_service.dart';
import '../../../core/di/service_locator.dart';
import '../../../core/storage/secure_storage_service.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_shadows.dart';
import '../../../core/location/registration_location_service.dart';
import '../../../core/utils/phone_number_policy.dart';
import '../../blocs/auth/auth_bloc.dart';
import '../../widgets/numeric_keypad.dart';

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
  final BiometricAuthService _biometric = sl<BiometricAuthService>();
  final SecureStorageService _secureStorage = sl<SecureStorageService>();
  final RegistrationLocationService _locationService =
      RegistrationLocationService();
  bool _isFormValid = false;
  bool _isRegistering = false;
  String? _lastRegisteredAddress;

  /// 0 = PIN principal, 1 = confirmation (cible du pavé numérique).
  int _activePinField = 0;
  String? _nameError;
  String? _phoneError;
  String? _pinError;
  String? _confirmPinError;

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
      if (_nameController.text.trim().length >= 3) _nameError = null;
      final raw = _phoneController.text.trim();
      if (raw.isNotEmpty) {
        _phoneError = PhoneNumberPolicy.isValid(raw)
            ? null
            : PhoneNumberPolicy.validationMessage;
      } else {
        _phoneError = null;
      }
      if (_pinController.text.length >= 4) _pinError = null;
      if (_confirmPinController.text.length >= 4 &&
          _confirmPinController.text == _pinController.text) {
        _confirmPinError = null;
      }
      _isFormValid =
          _nameController.text.trim().length >= 3 &&
          raw.isNotEmpty &&
          PhoneNumberPolicy.isValid(raw) &&
          _pinController.text.length >= 4 &&
          _pinController.text == _confirmPinController.text;
    });
  }

  void _validateFieldsForSubmit() {
    final raw = _phoneController.text.trim();
    setState(() {
      _nameError = _nameController.text.trim().length < 3
          ? 'Au moins 3 caractères pour le nom'
          : null;
      _phoneError = raw.isEmpty
          ? 'Numéro de téléphone requis'
          : (!PhoneNumberPolicy.isValid(raw)
                ? PhoneNumberPolicy.validationMessage
                : null);
      _pinError = _pinController.text.length < 4
          ? 'Le code PIN doit comporter 4 chiffres'
          : null;
      _confirmPinError = _confirmPinController.text.length < 4
          ? 'Confirmez les 4 chiffres du code'
          : (_pinController.text != _confirmPinController.text
                ? 'Les deux codes PIN doivent être identiques'
                : null);
    });
  }

  Future<void> _handleRegister() async {
    _validateFieldsForSubmit();
    if (_nameError != null ||
        _phoneError != null ||
        _pinError != null ||
        _confirmPinError != null) {
      return;
    }
    if (!_isFormValid) return;

    try {
      // 1. Vérifier la disponibilité de la biométrie
      if (!await _biometric.isDeviceReadyForBiometrics()) {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(
              content: Text(
                'La biométrie est obligatoire pour créer un compte.',
              ),
            ),
          );
        }
        return;
      }

      // 2. Forcer l'authentification biométrique pour l'enrôlement
      final didAuthenticate = await _biometric.authenticate(
        localizedReason:
            'Veuillez configurer votre biométrie pour sécuriser votre compte',
        biometricOnly: true,
        stickyAuth: true,
      );

      if (didAuthenticate && mounted) {
        setState(() => _isRegistering = true);

        final phone = PhoneNumberPolicy.normalize(_phoneController.text.trim());
        final pin = _pinController.text;

        await _secureStorage.saveAccountInfo(
          phone: phone,
          name: _nameController.text.trim(),
          biometricEnabled: true,
        );
        await _secureStorage.setSecureMode(true);

        RegistrationAddress? address;
        try {
          address = await _locationService.captureCurrentAddress();
        } catch (_) {
          address = null;
        }

        if (!mounted) return;
        final addressLine = address?.adresse ??
            address?.toApiPayload()['adresse'];
        setState(() {
          _isRegistering = false;
          _lastRegisteredAddress = addressLine;
        });

        context.read<AuthBloc>().add(
          RegisterRequested(
            phone: phone,
            pin: pin,
            nomComplet: _nameController.text.trim(),
            address: address?.hasData == true ? address!.toApiPayload() : null,
          ),
        );
      }
    } catch (e) {
      if (mounted) {
        setState(() => _isRegistering = false);
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Erreur lors de la configuration biométrique: $e'),
          ),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return BlocListener<AuthBloc, AuthState>(
      listener: (context, state) async {
        if (state is AuthSuccess) {
          final phone = state.phone;
          if (phone != null && phone.isNotEmpty) {
            final addr = _lastRegisteredAddress;
            if (addr != null && addr.isNotEmpty) {
              await _secureStorage.saveUserAddress(phone, addr);
            }
          }
          if (!context.mounted) return;
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(
              content: Text('Compte créé avec succès !'),
              backgroundColor: AppColors.success,
            ),
          );
          context.go(AppRouter.dashboard);
        } else if (state is AuthFailure) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text(state.message), backgroundColor: AppColors.error),
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
              AppColors.background,
              AppColors.background.withOpacity(0.5),
            ],
          ),
        ),
        child: SafeArea(
          child: CustomScrollView(
            physics: const BouncingScrollPhysics(),
            slivers: [
              SliverAppBar(
                leading: IconButton(
                  icon: const Icon(
                    Icons.arrow_back_ios_new_rounded,
                    color: AppColors.primary,
                  ),
                  onPressed: () => context.pop(),
                ),
                title: const Text(
                  'Créer un compte',
                  style: TextStyle(
                    color: AppColors.primary,
                    fontWeight: FontWeight.w900,
                  ),
                ),
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
                            boxShadow: AppShadows.soft,
                          ),
                          child: const Icon(
                            Icons.person_add_rounded,
                            size: 40,
                            color: AppColors.primary,
                          ),
                        ),
                        const SizedBox(height: 24),
                        const Text(
                          'Bienvenue chez microCredit',
                          textAlign: TextAlign.center,
                          style: TextStyle(
                            fontSize: 24,
                            fontWeight: FontWeight.w900,
                            color: AppColors.primary,
                            letterSpacing: -0.5,
                          ),
                        ),
                        const SizedBox(height: 8),
                        Text(
                          'Sécurisez vos finances en quelques secondes.',
                          textAlign: TextAlign.center,
                          style: TextStyle(
                            color: AppColors.primary.withOpacity(0.5),
                            fontSize: 14,
                            fontWeight: FontWeight.w500,
                          ),
                        ),
                        const SizedBox(height: 32),

                        _buildPremiumInput(
                          controller: _nameController,
                          label: 'Nom complet',
                          icon: Icons.person_outline_rounded,
                          errorText: _nameError,
                        ),
                        const SizedBox(height: 20),

                        _buildPremiumInput(
                          controller: _phoneController,
                          label: 'Numéro de téléphone',
                          icon: Icons.phone_android_rounded,
                          keyboardType: TextInputType.phone,
                          errorText: _phoneError,
                        ),
                        const SizedBox(height: 4),
                        Text(
                          PhoneNumberPolicy.validationMessage,
                          style: TextStyle(
                            fontSize: 12,
                            color: AppColors.textSecondary,
                          ),
                        ),
                        const SizedBox(height: 20),

                        _buildPremiumInput(
                          controller: _pinController,
                          label: 'Définir un Code PIN (4 chiffres)',
                          icon: Icons.lock_outline_rounded,
                          isPin: true,
                          errorText: _pinError,
                          onPinPadFocus: () =>
                              setState(() => _activePinField = 0),
                        ),
                        const SizedBox(height: 20),

                        _buildPremiumInput(
                          controller: _confirmPinController,
                          label: 'Confirmer le Code PIN',
                          icon: Icons.lock_clock_rounded,
                          isPin: true,
                          errorText: _confirmPinError,
                          onPinPadFocus: () =>
                              setState(() => _activePinField = 1),
                        ),
                        const SizedBox(height: 16),
                        Text(
                          'Saisissez votre code avec le pavé numérique (pas de clavier téléphone).',
                          textAlign: TextAlign.center,
                          style: TextStyle(
                            fontSize: 12,
                            fontWeight: FontWeight.w600,
                            color: AppColors.primary.withOpacity(0.55),
                          ),
                        ),
                        const SizedBox(height: 12),
                        Container(
                          padding: const EdgeInsets.all(12),
                          decoration: BoxDecoration(
                            color: Colors.white,
                            borderRadius: BorderRadius.circular(24),
                            boxShadow: AppShadows.soft,
                          ),
                          child: NumericKeypad(
                            key: ValueKey(_activePinField),
                            digitColor: AppColors.primary,
                            shuffle: true,
                            onNumberPressed: (val) {
                              final c = _activePinField == 0
                                  ? _pinController
                                  : _confirmPinController;
                              if (c.text.length < 4) {
                                setState(() => c.text += val);
                              }
                            },
                            onDeletePressed: () {
                              final c = _activePinField == 0
                                  ? _pinController
                                  : _confirmPinController;
                              if (c.text.isNotEmpty) {
                                setState(
                                  () => c.text = c.text.substring(
                                    0,
                                    c.text.length - 1,
                                  ),
                                );
                              }
                            },
                          ),
                        ),

                        const SizedBox(height: 56),

                        BlocBuilder<AuthBloc, AuthState>(
                          builder: (context, state) {
                            final busy =
                                _isRegistering || state is AuthLoading;
                            return ElevatedButton(
                              onPressed: _isFormValid && !busy
                                  ? _handleRegister
                                  : null,
                              style: ElevatedButton.styleFrom(
                                backgroundColor: AppColors.primary,
                                shadowColor: AppColors.primary.withOpacity(0.3),
                                elevation: 12,
                              ),
                              child: busy
                                  ? const SizedBox(
                                      height: 22,
                                      width: 22,
                                      child: CircularProgressIndicator(
                                        strokeWidth: 2,
                                        color: Colors.white,
                                      ),
                                    )
                                  : const Text('FINALISER L\'INSCRIPTION'),
                            );
                          },
                        ),

                        const SizedBox(height: 32),
                        TextButton(
                          onPressed: () => context.pop(),
                          child: RichText(
                            text: TextSpan(
                              text: 'Déjà un compte ? ',
                              style: TextStyle(
                                color: AppColors.primary.withOpacity(0.5),
                                fontWeight: FontWeight.w500,
                              ),
                              children: const [
                                TextSpan(
                                  text: 'Se connecter',
                                  style: TextStyle(
                                    color: AppColors.primary,
                                    fontWeight: FontWeight.w800,
                                  ),
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
    ),
    );
  }

  Widget _buildPremiumInput({
    required TextEditingController controller,
    required String label,
    required IconData icon,
    bool isPin = false,
    TextInputType? keyboardType,
    String? errorText,
    VoidCallback? onPinPadFocus,
  }) {
    final hasErr = errorText != null && errorText.trim().isNotEmpty;
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Container(
          decoration: BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.circular(24),
            border: Border.all(
              color: hasErr ? AppColors.error : AppColors.border,
              width: hasErr ? 2 : 1,
            ),
            boxShadow: AppShadows.soft,
          ),
          child: TextFormField(
            controller: controller,
            obscureText: isPin,
            readOnly: isPin,
            keyboardType: isPin
                ? TextInputType.none
                : (keyboardType ?? TextInputType.text),
            enableInteractiveSelection: !isPin,
            contextMenuBuilder: isPin
                ? (BuildContext ctx, EditableTextState state) =>
                      const SizedBox.shrink()
                : null,
            maxLength: isPin ? 4 : null,
            onTap: isPin && onPinPadFocus != null
                ? () {
                    FocusManager.instance.primaryFocus?.unfocus();
                    onPinPadFocus();
                  }
                : null,
            style: const TextStyle(
              fontWeight: FontWeight.w700,
              color: AppColors.primary,
            ),
            decoration: InputDecoration(
              labelText: label,
              prefixIcon: Icon(
                icon,
                color: AppColors.primary.withValues(alpha: 0.5),
              ),
              border: InputBorder.none,
              enabledBorder: InputBorder.none,
              focusedBorder: OutlineInputBorder(
                borderRadius: BorderRadius.circular(24),
                borderSide: BorderSide(
                  color: hasErr ? AppColors.error : AppColors.primary,
                  width: hasErr ? 2 : 2,
                ),
              ),
              counterText: isPin ? '' : null,
            ),
          ),
        ),
        if (hasErr) ...[
          const SizedBox(height: 6),
          Padding(
            padding: const EdgeInsets.only(left: 6, right: 4),
            child: Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Padding(
                  padding: EdgeInsets.only(top: 1),
                  child: Icon(
                    Icons.error_outline_rounded,
                    size: 14,
                    color: AppColors.error,
                  ),
                ),
                const SizedBox(width: 6),
                Expanded(
                  child: Text(
                    errorText.trim(),
                    style: const TextStyle(
                      color: AppColors.error,
                      fontSize: 12,
                      fontWeight: FontWeight.w600,
                      height: 1.3,
                    ),
                  ),
                ),
              ],
            ),
          ),
        ],
      ],
    );
  }
}
