import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';
import 'package:intl/intl.dart';
import '../../../core/auth/biometric_auth_service.dart';
import '../../../core/di/service_locator.dart';
import '../../../core/router/app_router.dart';
import '../../../core/storage/secure_storage_service.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../core/theme/app_text_styles.dart';
import '../../../core/utils/phone_number_policy.dart';
import '../../../core/widgets/premium_button.dart';
import '../../../core/widgets/premium_input.dart';
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
  final BiometricAuthService _biometric = sl<BiometricAuthService>();
  final SecureStorageService _secureStorage = sl<SecureStorageService>();
  bool _isPinMode = false;
  int _pinKeypadEpoch = 0;
  bool _isFormValid = false;
  String? _phoneError;
  String? _pinError;
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
          _pinKeypadEpoch++;
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
      _pinKeypadEpoch++;
    });
  }

  void _showAccountSelector() {
    final sheetTheme = Theme.of(context);
    final maxListHeight = MediaQuery.of(context).size.height * 0.45;
    showModalBottomSheet<void>(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (context) => Container(
        padding: EdgeInsets.fromLTRB(
          AppSpacing.lg,
          AppSpacing.sm,
          AppSpacing.lg,
          AppSpacing.lg + MediaQuery.of(context).padding.bottom,
        ),
        decoration: BoxDecoration(
          color: sheetTheme.colorScheme.surface,
          borderRadius: const BorderRadius.vertical(
            top: Radius.circular(AppSpacing.radiusLarge),
          ),
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Text(
              'Choisir un compte',
              style: sheetTheme.textTheme.titleLarge?.copyWith(
                fontWeight: FontWeight.w700,
                color: AppColors.primary,
              ),
            ),
            const SizedBox(height: AppSpacing.sm),
            Text(
              'Sélectionnez le profil à utiliser sur cet appareil.',
              style: AppTextStyles.bodySmall.copyWith(
                color: sheetTheme.colorScheme.onSurfaceVariant,
              ),
            ),
            const SizedBox(height: AppSpacing.md),
            ConstrainedBox(
              constraints: BoxConstraints(maxHeight: maxListHeight),
              child: ListView.separated(
                shrinkWrap: true,
                itemCount: _registeredPhones.length,
                separatorBuilder: (context, _) =>
                    const SizedBox(height: AppSpacing.xs),
                itemBuilder: (context, index) {
                  final phone = _registeredPhones[index];
                  return FutureBuilder<String?>(
                    future: _secureStorage.getUserName(phone),
                    builder: (context, snapshot) {
                      final selected = phone == _lastPhone;
                      return Material(
                        color: selected
                            ? AppColors.primary.withValues(alpha: 0.1)
                            : sheetTheme.colorScheme.surfaceContainerHighest
                                  .withValues(alpha: 0.35),
                        borderRadius: BorderRadius.circular(
                          AppSpacing.radiusLarge,
                        ),
                        child: InkWell(
                          onTap: () {
                            Navigator.pop(context);
                            _selectAccount(phone);
                          },
                          borderRadius: BorderRadius.circular(
                            AppSpacing.radiusLarge,
                          ),
                          child: Padding(
                            padding: const EdgeInsets.symmetric(
                              horizontal: AppSpacing.md,
                              vertical: AppSpacing.sm + 2,
                            ),
                            child: Row(
                              children: [
                                CircleAvatar(
                                  radius: 22,
                                  backgroundColor: AppColors.primary.withValues(
                                    alpha: 0.12,
                                  ),
                                  child: const Icon(
                                    Icons.person_rounded,
                                    color: AppColors.primary,
                                  ),
                                ),
                                const SizedBox(width: AppSpacing.md),
                                Expanded(
                                  child: Column(
                                    crossAxisAlignment:
                                        CrossAxisAlignment.start,
                                    children: [
                                      Text(
                                        snapshot.data ?? phone,
                                        style: AppTextStyles.titleSmall
                                            .copyWith(
                                              color: sheetTheme
                                                  .colorScheme
                                                  .onSurface,
                                              fontWeight: FontWeight.w700,
                                            ),
                                      ),
                                      const SizedBox(height: 2),
                                      Text(
                                        phone,
                                        style: AppTextStyles.bodySmall.copyWith(
                                          color: sheetTheme
                                              .colorScheme
                                              .onSurfaceVariant,
                                        ),
                                      ),
                                    ],
                                  ),
                                ),
                                if (selected)
                                  const Icon(
                                    Icons.check_circle_rounded,
                                    color: AppColors.success,
                                    size: 26,
                                  ),
                              ],
                            ),
                          ),
                        ),
                      );
                    },
                  );
                },
              ),
            ),
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
      final raw = _phoneController.text.trim();
      if (raw.isNotEmpty) {
        _phoneError = PhoneNumberPolicy.isValid(raw)
            ? null
            : PhoneNumberPolicy.validationMessage;
      } else {
        _phoneError = null;
      }
      if (_pinController.text.length >= 4) {
        _pinError = null;
      }
      _isFormValid =
          raw.isNotEmpty &&
          PhoneNumberPolicy.isValid(raw) &&
          _pinController.text.length >= 4;
    });
  }

  void _onLoginPressed() async {
    setState(() {
      _phoneError = null;
      _pinError = null;
    });
    final phoneRaw = _phoneController.text.trim();
    if (phoneRaw.isEmpty) {
      setState(() => _phoneError = 'Veuillez saisir votre numéro de téléphone');
      return;
    }
    if (!PhoneNumberPolicy.isValid(phoneRaw)) {
      setState(() => _phoneError = PhoneNumberPolicy.validationMessage);
      return;
    }
    final phone = PhoneNumberPolicy.normalize(phoneRaw);
    if (_pinController.text.length < 4) {
      setState(() => _pinError = 'Le code PIN doit comporter 4 chiffres');
      return;
    }
    if (!_isFormValid) return;

    await _secureStorage.saveLastPhone(phone);
    await _secureStorage.saveLastLoginDate(DateTime.now());
    if (mounted) {
      context.read<AuthBloc>().add(
        LoginRequested(phone: phone, pin: _pinController.text),
      );
    }
  }

  Future<void> _authenticateBiometrically() async {
    try {
      if (await _biometric.isDeviceReadyForBiometrics()) {
        final didAuthenticate = await _biometric.authenticate(
          localizedReason:
              'Authentifiez-vous pour accéder à votre espace microCredit',
          biometricOnly: true,
          stickyAuth: true,
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
            const SnackBar(
              content: Text(
                'La biométrie n\'est pas configurée sur cet appareil',
              ),
            ),
          );
        }
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(
          context,
        ).showSnackBar(SnackBar(content: Text('Erreur biométrie: $e')));
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return BlocListener<AuthBloc, AuthState>(
      listener: (context, state) {
        if (state is AuthFailure) {
          final msg = state.message;
          final lower = msg.toLowerCase();
          setState(() {
            if (_lastPhone != null) {
              _phoneError = null;
              _pinError = msg;
            } else if (lower.contains('téléphone') ||
                lower.contains('telephone') ||
                lower.contains('phone')) {
              _phoneError = msg;
              _pinError = null;
            } else {
              _pinError = msg;
              _phoneError = null;
            }
          });
        } else if (state is AuthSuccess) {
          setState(() {
            _phoneError = null;
            _pinError = null;
          });
        }
      },
      child: Scaffold(
        backgroundColor: Theme.of(context).colorScheme.surface,
        body: SafeArea(
          child: SingleChildScrollView(
            physics: const ClampingScrollPhysics(),
            padding: const EdgeInsets.symmetric(
              horizontal: AppSpacing.screenPadding,
              vertical: AppSpacing.md,
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                const SizedBox(height: AppSpacing.lg),
                Icon(
                  Icons.account_balance_wallet_rounded,
                  size: 48,
                  color: AppColors.primary,
                ),
                const SizedBox(height: AppSpacing.md),
                Text(
                  'microCredit',
                  textAlign: TextAlign.center,
                  style: AppTextStyles.headlineSmall.copyWith(
                    color: AppColors.primary,
                    fontWeight: FontWeight.w800,
                  ),
                ),
                const SizedBox(height: AppSpacing.xs),
                Text(
                  'Connexion à votre compte',
                  textAlign: TextAlign.center,
                  style: AppTextStyles.bodyMedium.copyWith(
                    color: Theme.of(context).colorScheme.onSurfaceVariant,
                  ),
                ),
                const SizedBox(height: AppSpacing.xl),
                if (_lastPhone != null) ...[
                  Row(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      CircleAvatar(
                        radius: 22,
                        backgroundColor: AppColors.primary.withValues(
                          alpha: 0.12,
                        ),
                        child: const Icon(
                          Icons.person_rounded,
                          color: AppColors.primary,
                        ),
                      ),
                      const SizedBox(width: AppSpacing.md),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              _userName != null
                                  ? 'Heureux de vous revoir,'
                                  : 'Bon retour,',
                              style: Theme.of(context).textTheme.bodySmall
                                  ?.copyWith(
                                    color: Theme.of(
                                      context,
                                    ).colorScheme.onSurfaceVariant,
                                  ),
                            ),
                            Text(
                              _userName ?? _lastPhone!,
                              style: Theme.of(context).textTheme.titleMedium
                                  ?.copyWith(
                                    fontWeight: FontWeight.w700,
                                    color: Theme.of(
                                      context,
                                    ).colorScheme.onSurface,
                                  ),
                            ),
                          ],
                        ),
                      ),
                      if (_registeredPhones.length > 1)
                        IconButton(
                          tooltip: 'Changer de compte',
                          onPressed: _showAccountSelector,
                          icon: const Icon(
                            Icons.swap_horiz_rounded,
                            color: AppColors.primary,
                          ),
                        ),
                    ],
                  ),
                  Align(
                    alignment: Alignment.centerLeft,
                    child: TextButton(
                      style: TextButton.styleFrom(
                        foregroundColor: AppColors.primary,
                      ),
                      onPressed: () => setState(() {
                        _lastPhone = null;
                        _userName = null;
                        _phoneController.clear();
                        _isPinMode = false;
                      }),
                      child: const Text('Utiliser un autre compte'),
                    ),
                  ),
                  Divider(
                    height: AppSpacing.xl,
                    color: AppColors.primary.withValues(alpha: 0.12),
                  ),
                ],
                if (_lastPhone == null)
                  Padding(
                    padding: const EdgeInsets.only(bottom: AppSpacing.md),
                    child: Text(
                      'Saisissez votre numéro et un code PIN à 4 chiffres.',
                      style: AppTextStyles.bodySmall.copyWith(
                        color: Theme.of(context).colorScheme.onSurfaceVariant,
                      ),
                    ),
                  ),
                if (_lastPhone == null)
                  PremiumInput(
                    label: 'Téléphone',
                    hint: '8 chiffres — commence par 2, 3 ou 4',
                    controller: _phoneController,
                    type: PremiumInputType.phone,
                    errorText: _phoneError,
                    focusedBorderColor: AppColors.primary,
                    emphasizeBorder: !_isPinMode,
                    prefixIcon: const Icon(
                      Icons.phone_android_rounded,
                      color: AppColors.primary,
                    ),
                    onTap: () {
                      FocusManager.instance.primaryFocus?.unfocus();
                      setState(() => _isPinMode = false);
                    },
                  ),
                if (_lastPhone == null)
                  const SizedBox(height: AppSpacing.inputSpacing),
                PremiumInput(
                  label: 'Code PIN',
                  hint: '4 chiffres — pavé numérique',
                  controller: _pinController,
                  type: PremiumInputType.password,
                  obscureText: true,
                  pinPadInput: true,
                  maxLength: 4,
                  errorText: _pinError,
                  focusedBorderColor: AppColors.primary,
                  emphasizeBorder: _isPinMode,
                  prefixIcon: const Icon(
                    Icons.lock_person_rounded,
                    color: AppColors.primary,
                  ),
                  suffixIcon: IconButton(
                    tooltip: 'Connexion biométrique',
                    onPressed: _authenticateBiometrically,
                    icon: const Icon(
                      Icons.fingerprint_rounded,
                      size: 26,
                      color: AppColors.primary,
                    ),
                  ),
                  onTap: () {
                    FocusManager.instance.primaryFocus?.unfocus();
                    setState(() {
                      _isPinMode = true;
                      _pinKeypadEpoch++;
                    });
                  },
                ),
                const SizedBox(height: AppSpacing.lg),
                BlocBuilder<AuthBloc, AuthState>(
                  builder: (context, state) {
                    final loading = state is AuthLoading;
                    return Column(
                      crossAxisAlignment: CrossAxisAlignment.stretch,
                      children: [
                        PremiumButton(
                          text: 'Se connecter',
                          type: PremiumButtonType.primary,
                          size: PremiumButtonSize.large,
                          isFullWidth: true,
                          isLoading: loading,
                          onPressed: (!loading && _isFormValid)
                              ? _onLoginPressed
                              : null,
                        ),
                        const SizedBox(height: AppSpacing.md),
                        Row(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            Text(
                              'Pas encore de compte ?',
                              style: AppTextStyles.bodySmall.copyWith(
                                color: Theme.of(
                                  context,
                                ).colorScheme.onSurfaceVariant,
                              ),
                            ),
                            TextButton(
                              style: TextButton.styleFrom(
                                foregroundColor: AppColors.primary,
                              ),
                              onPressed: () => context.push(AppRouter.register),
                              child: const Text('Créer un compte'),
                            ),
                          ],
                        ),
                      ],
                    );
                  },
                ),
                if (_isPinMode) ...[
                  const SizedBox(height: AppSpacing.lg),
                  Text(
                    'Pavé numérique',
                    style: AppTextStyles.titleSmall.copyWith(
                      color: AppColors.primary,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                  const SizedBox(height: AppSpacing.sm),
                  NumericKeypad(
                    key: ValueKey(_pinKeypadEpoch),
                    shuffle: true,
                    onNumberPressed: (val) {
                      if (_pinController.text.length < 4) {
                        _pinController.text += val;
                      }
                    },
                    onDeletePressed: () {
                      if (_pinController.text.isNotEmpty) {
                        _pinController.text = _pinController.text.substring(
                          0,
                          _pinController.text.length - 1,
                        );
                      }
                    },
                  ),
                ],
                const SizedBox(height: AppSpacing.lg),
                _LoginFooter(
                  lastLogin: _lastLoginDate,
                  onSurfaceVariant: Theme.of(
                    context,
                  ).colorScheme.onSurfaceVariant,
                  accentColor: AppColors.primary,
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}

class _LoginFooter extends StatelessWidget {
  const _LoginFooter({
    required this.lastLogin,
    required this.onSurfaceVariant,
    required this.accentColor,
  });

  final DateTime? lastLogin;
  final Color onSurfaceVariant;
  final Color accentColor;

  @override
  Widget build(BuildContext context) {
    final dateLine = lastLogin != null
        ? 'Dernière connexion · ${DateFormat('dd/MM/yyyy · HH:mm').format(lastLogin!)}'
        : null;

    return Column(
      children: [
        if (dateLine != null) ...[
          Text(
            dateLine,
            textAlign: TextAlign.center,
            style: AppTextStyles.labelSmall.copyWith(
              color: onSurfaceVariant,
              height: 1.2,
            ),
          ),
          const SizedBox(height: AppSpacing.xs),
        ],
        Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(Icons.shield_outlined, size: 16, color: accentColor),
            const SizedBox(width: AppSpacing.sm),
            Flexible(
              child: Text(
                'Connexion sécurisée.',
                textAlign: TextAlign.center,
                style: AppTextStyles.labelSmall.copyWith(
                  color: accentColor.withValues(alpha: 0.85),
                  height: 1.25,
                ),
              ),
            ),
          ],
        ),
      ],
    );
  }
}
