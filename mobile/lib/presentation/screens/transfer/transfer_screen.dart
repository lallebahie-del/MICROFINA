import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:local_auth/local_auth.dart';
import 'package:intl/intl.dart';
import '../../../core/theme/app_colors.dart';
import '../../../data/models/compte_eps_model.dart';
import '../../blocs/account/account_bloc.dart';
import '../../blocs/transfer/transfer_bloc.dart';

class TransferScreen extends StatefulWidget {
  const TransferScreen({super.key});

  @override
  State<TransferScreen> createState() => _TransferScreenState();
}

class _TransferScreenState extends State<TransferScreen> {
  final _formKey = GlobalKey<FormState>();
  final _amountController = TextEditingController();
  final _reasonController = TextEditingController();
  final LocalAuthentication _auth = LocalAuthentication();
  final currencyFormat = NumberFormat.currency(locale: 'fr_FR', symbol: 'FCFA', decimalDigits: 0);
  
  String? _fromAccountId;
  String? _toAccountId;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (!mounted) return;
      context.read<AccountBloc>().add(FetchAccounts());
    });
  }

  @override
  void dispose() {
    _amountController.dispose();
    _reasonController.dispose();
    super.dispose();
  }

  void _handleTransfer() async {
    if (!_formKey.currentState!.validate()) return;
    if (_fromAccountId == null || _toAccountId == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Veuillez sélectionner les comptes')),
      );
      return;
    }
    if (_fromAccountId == _toAccountId) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Le compte source et destination doivent être différents')),
      );
      return;
    }

    final accounts = context.read<AccountBloc>().state.accounts;
    CompteEpsModel? fromAcc;
    for (final a in accounts) {
      if (a.id == _fromAccountId) fromAcc = a;
    }
    if (fromAcc == null) return;

    final amount = double.parse(_amountController.text);
    if (amount > fromAcc.availableBalance) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(
            'Solde insuffisant sur le compte à débiter (disponible : ${currencyFormat.format(fromAcc.availableBalance)}).',
          ),
        ),
      );
      return;
    }

    // Authentification biométrique
    final bool canCheckBiometrics = await _auth.canCheckBiometrics;
    final bool isDeviceSupported = await _auth.isDeviceSupported();

    if (canCheckBiometrics && isDeviceSupported) {
      try {
        final bool didAuthenticate = await _auth.authenticate(
          localizedReason: 'Veuillez vous authentifier pour valider le virement',
          options: const AuthenticationOptions(
            stickyAuth: true,
            biometricOnly: true,
          ),
        );
        if (didAuthenticate) {
          _executeTransfer();
          return;
        }
      } catch (e) {
        debugPrint('Erreur biométrie: $e');
      }
    }

    _showPinDialog();
  }

  void _showPinDialog() {
    final pinController = TextEditingController();
    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (context) => AlertDialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
        title: const Text('Confirmation Sécurisée', 
          style: TextStyle(fontWeight: FontWeight.bold, color: AppColors.primary)),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const Text('Veuillez saisir votre code secret pour valider le virement.'),
            const SizedBox(height: 20),
            TextField(
              controller: pinController,
              obscureText: true,
              keyboardType: TextInputType.number,
              maxLength: 4,
              textAlign: TextAlign.center,
              style: const TextStyle(
                fontSize: 24,
                letterSpacing: 10,
                fontWeight: FontWeight.bold,
                color: AppColors.primary,
              ),
              decoration: InputDecoration(
                hintText: '****',
                counterText: '',
                filled: true,
                fillColor: Colors.grey[100],
                border: OutlineInputBorder(borderRadius: BorderRadius.circular(12), borderSide: BorderSide.none),
              ),
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('ANNULER', style: TextStyle(color: Colors.grey)),
          ),
          ElevatedButton(
            onPressed: () {
              if (pinController.text.length == 4) {
                Navigator.pop(context);
                _executeTransfer();
              }
            },
            style: ElevatedButton.styleFrom(
              backgroundColor: AppColors.primary,
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
            ),
            child: const Text('VALIDER'),
          ),
        ],
      ),
    );
  }

  void _executeTransfer() {
    context.read<TransferBloc>().add(PerformTransfer(
      fromAccountId: _fromAccountId!,
      toAccountId: _toAccountId!,
      amount: double.parse(_amountController.text),
      reason: _reasonController.text,
    ));
  }

  void _showSuccessPopup(String message) {
    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (context) => AlertDialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(28)),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const SizedBox(height: 10),
            const Icon(Icons.check_circle_outline_rounded, color: AppColors.success, size: 80),
            const SizedBox(height: 24),
            const Text(
              'Félicitations !',
              style: TextStyle(fontSize: 22, fontWeight: FontWeight.w900, color: AppColors.primary),
            ),
            const SizedBox(height: 12),
            Text(
              message,
              textAlign: TextAlign.center,
              style: TextStyle(fontSize: 14, color: Colors.grey[600], fontWeight: FontWeight.w500),
            ),
            const SizedBox(height: 32),
            SizedBox(
              width: double.infinity,
              height: 56,
              child: ElevatedButton(
                onPressed: () {
                  context.pop(); // Fermer le popup
                  context.pop(); // Retour au Dashboard
                },
                style: ElevatedButton.styleFrom(
                  backgroundColor: AppColors.primary,
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
                  elevation: 0,
                ),
                child: const Text('RETOUR À L\'ACCUEIL', style: TextStyle(fontWeight: FontWeight.bold, letterSpacing: 0.5)),
              ),
            ),
          ],
        ),
      ),
    );
  }

  void _showErrorPopup(String message) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(28)),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const SizedBox(height: 10),
            Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: AppColors.error.withOpacity(0.1),
                shape: BoxShape.circle,
              ),
              child: const Icon(Icons.error_rounded, color: AppColors.error, size: 60),
            ),
            const SizedBox(height: 24),
            const Text(
              'Oups !',
              style: TextStyle(fontSize: 22, fontWeight: FontWeight.w900, color: AppColors.primary),
            ),
            const SizedBox(height: 12),
            Text(
              message,
              textAlign: TextAlign.center,
              style: TextStyle(fontSize: 14, color: Colors.grey[600], fontWeight: FontWeight.w500),
            ),
            const SizedBox(height: 32),
            SizedBox(
              width: double.infinity,
              height: 56,
              child: ElevatedButton(
                onPressed: () => Navigator.pop(context),
                style: ElevatedButton.styleFrom(
                  backgroundColor: Colors.grey[200],
                  foregroundColor: AppColors.primary,
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
                  elevation: 0,
                ),
                child: const Text('RÉESSAYER', style: TextStyle(fontWeight: FontWeight.bold)),
              ),
            ),
          ],
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return BlocListener<TransferBloc, TransferState>(
      listener: (context, state) {
        if (state.status == TransferStatus.success) {
          context.read<AccountBloc>().add(FetchAccounts());
          _showSuccessPopup('Virement effectué avec succès !');
        } else if (state.status == TransferStatus.failure) {
          _showErrorPopup(state.errorMessage ?? 'Une erreur est survenue');
        }
      },
      child: Scaffold(
        backgroundColor: const Color(0xFFF8FAFC),
        appBar: AppBar(
          title: const Text('Virement Interne', style: TextStyle(fontWeight: FontWeight.w800)),
          centerTitle: true,
          elevation: 0,
          backgroundColor: Colors.transparent,
          foregroundColor: AppColors.primary,
        ),
        body: BlocBuilder<AccountBloc, AccountState>(
          builder: (context, accountState) {
            final accounts = accountState.accounts;

            if (accounts.isEmpty) {
              return Center(
                child: Padding(
                  padding: const EdgeInsets.all(24),
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Icon(Icons.account_balance_wallet_outlined, size: 56, color: Colors.grey[400]),
                      const SizedBox(height: 16),
                      Text(
                        accountState.status == AccountStatus.loading
                            ? 'Chargement de vos comptes…'
                            : 'Aucun compte disponible pour un virement.',
                        textAlign: TextAlign.center,
                        style: TextStyle(color: Colors.grey[700], fontWeight: FontWeight.w500),
                      ),
                      if (accountState.status != AccountStatus.loading) ...[
                        const SizedBox(height: 20),
                        TextButton(
                          onPressed: () => context.read<AccountBloc>().add(FetchAccounts()),
                          child: const Text('RÉESSAYER'),
                        ),
                      ],
                    ],
                  ),
                ),
              );
            }

            return SingleChildScrollView(
              padding: const EdgeInsets.all(24),
              child: Form(
                key: _formKey,
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text(
                      'Transférer de l\'argent entre vos propres comptes en toute sécurité.',
                      style: TextStyle(fontSize: 15, color: Colors.grey, fontWeight: FontWeight.w500),
                    ),
                    const SizedBox(height: 32),
                    
                    _buildSectionTitle('DEPUIS LE COMPTE'),
                    const SizedBox(height: 12),
                    _buildAccountSelector(
                      value: _fromAccountId,
                      items: accounts,
                      onChanged: (val) => setState(() => _fromAccountId = val),
                      hint: 'Compte à débiter',
                    ),
                    
                    const SizedBox(height: 32),
                    
                    _buildSectionTitle('VERS LE COMPTE'),
                    const SizedBox(height: 12),
                    _buildAccountSelector(
                      value: _toAccountId,
                      items: accounts,
                      onChanged: (val) => setState(() => _toAccountId = val),
                      hint: 'Compte à créditer',
                    ),
                    
                    const SizedBox(height: 32),
                    
                    _buildSectionTitle('MONTANT DU VIREMENT'),
                    const SizedBox(height: 12),
                    TextFormField(
                      controller: _amountController,
                      keyboardType: TextInputType.number,
                      style: const TextStyle(fontWeight: FontWeight.w900, fontSize: 24, color: AppColors.primary),
                      textAlign: TextAlign.center,
                      decoration: InputDecoration(
                        hintText: '0 FCFA',
                        hintStyle: TextStyle(color: Colors.grey[300]),
                        filled: true,
                        fillColor: Colors.white,
                        border: OutlineInputBorder(borderRadius: BorderRadius.circular(20), borderSide: BorderSide.none),
                        contentPadding: const EdgeInsets.all(24),
                      ),
                      validator: (val) {
                        if (val == null || val.isEmpty) return 'Veuillez saisir un montant';
                        if (double.tryParse(val) == null || double.parse(val) <= 0) return 'Montant invalide';
                        return null;
                      },
                    ),
                    
                    const SizedBox(height: 32),
                    
                    _buildSectionTitle('MOTIF DU VIREMENT'),
                    const SizedBox(height: 12),
                    TextFormField(
                      controller: _reasonController,
                      decoration: InputDecoration(
                        hintText: 'Ex: Épargne projet, Loyer...',
                        filled: true,
                        fillColor: Colors.white,
                        border: OutlineInputBorder(borderRadius: BorderRadius.circular(16), borderSide: BorderSide.none),
                      ),
                    ),
                    
                    const SizedBox(height: 48),
                    
                    BlocBuilder<TransferBloc, TransferState>(
                      builder: (context, state) {
                        final isLoading = state.status == TransferStatus.loading;
                        return SizedBox(
                          width: double.infinity,
                          height: 60,
                          child: ElevatedButton(
                            onPressed: isLoading ? null : _handleTransfer,
                            style: ElevatedButton.styleFrom(
                              backgroundColor: AppColors.primary,
                              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
                              elevation: 4,
                              shadowColor: AppColors.primary.withOpacity(0.4),
                            ),
                            child: isLoading 
                              ? const CircularProgressIndicator(color: Colors.white)
                              : const Text(
                                  'CONFIRMER LE VIREMENT',
                                  style: TextStyle(fontWeight: FontWeight.w900, fontSize: 16, letterSpacing: 1),
                                ),
                          ),
                        );
                      },
                    ),
                  ],
                ),
              ),
            );
          },
        ),
      ),
    );
  }

  Widget _buildSectionTitle(String title) {
    return Text(
      title,
      style: TextStyle(fontSize: 11, fontWeight: FontWeight.w900, color: AppColors.primary.withOpacity(0.4), letterSpacing: 1.5),
    );
  }

  Widget _buildAccountSelector({
    required String? value,
    required List<CompteEpsModel> items,
    required Function(String?) onChanged,
    required String hint,
  }) {
    return Container(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(20),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.03),
            blurRadius: 15,
            offset: const Offset(0, 5),
          ),
        ],
      ),
      child: DropdownButtonFormField<String>(
        value: value,
        isExpanded: true,
        isDense: true,
        itemHeight: 72,
        decoration: InputDecoration(
          contentPadding: const EdgeInsets.symmetric(horizontal: 20, vertical: 12),
          border: InputBorder.none,
          hintText: hint,
          hintStyle: TextStyle(color: Colors.grey[400], fontSize: 14, fontWeight: FontWeight.w500),
        ),
        icon: const Icon(Icons.keyboard_arrow_down_rounded, color: AppColors.primary),
        selectedItemBuilder: (BuildContext context) {
          return items.map((account) {
            return Align(
              alignment: Alignment.centerLeft,
              child: Row(
                children: [
                  Container(
                    width: 8,
                    height: 8,
                    decoration: BoxDecoration(
                      color: account.accountTypeColor != null
                          ? Color(int.parse(account.accountTypeColor!.replaceFirst('#', '0xFF')))
                          : AppColors.primary,
                      shape: BoxShape.circle,
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Text(
                      account.libelle,
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                      style: const TextStyle(
                        fontWeight: FontWeight.bold,
                        fontSize: 14,
                        color: AppColors.primary,
                      ),
                    ),
                  ),
                ],
              ),
            );
          }).toList();
        },
        items: items.map((account) {
          return DropdownMenuItem<String>(
            value: account.id,
            child: Row(
              children: [
                Container(
                  width: 8,
                  height: 8,
                  decoration: BoxDecoration(
                    color: account.accountTypeColor != null 
                        ? Color(int.parse(account.accountTypeColor!.replaceFirst('#', '0xFF')))
                        : AppColors.primary,
                    shape: BoxShape.circle,
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    mainAxisSize: MainAxisSize.min,
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Text(
                        account.libelle,
                        maxLines: 2,
                        overflow: TextOverflow.ellipsis,
                        style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 14, color: AppColors.primary),
                      ),
                      Text(
                        'Solde: ${currencyFormat.format(account.availableBalance)}',
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                        style: TextStyle(
                          fontSize: 11,
                          fontWeight: FontWeight.w600,
                          color: AppColors.primary.withValues(alpha: 0.55),
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
          );
        }).toList(),
        onChanged: onChanged,
      ),
    );
  }
}
