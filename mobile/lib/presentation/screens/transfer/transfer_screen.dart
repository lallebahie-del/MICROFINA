import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:lottie/lottie.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:local_auth/local_auth.dart';
import '../../../core/theme/app_theme.dart';
import '../../../data/datasources/mock/mock_data.dart';
import '../../blocs/auth/auth_bloc.dart';
import '../../blocs/auth/auth_state.dart';

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
  
  String? _fromAccountId;
  String? _toAccountId;
  bool _isLoading = false;

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
        const SnackBar(content: Text('Villez sélectionner les comptes')),
      );
      return;
    }
    if (_fromAccountId == _toAccountId) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Le compte source et destination doivent être différents')),
      );
      return;
    }

    // Proposer d'abord la biométrie
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

    // Repli sur le PIN si la biométrie échoue ou n'est pas disponible
    if (mounted) {
      _showPinDialog();
    }
  }

  void _showPinDialog() {
    final pinController = TextEditingController();
    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (context) => AlertDialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
        title: const Text('Confirmation Sécurisée', 
          style: TextStyle(fontWeight: FontWeight.bold, color: AppTheme.primaryBlue)),
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
              style: const TextStyle(fontSize: 24, letterSpacing: 10, fontWeight: FontWeight.bold),
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
              backgroundColor: AppTheme.primaryBlue,
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
            ),
            child: const Text('VALIDER'),
          ),
        ],
      ),
    );
  }

  void _executeTransfer() async {
    setState(() => _isLoading = true);

    final success = await MockData.performInternalTransfer(
      fromAccountId: _fromAccountId!,
      toAccountId: _toAccountId!,
      amount: double.parse(_amountController.text),
      reason: _reasonController.text,
    );

    if (mounted) {
      setState(() => _isLoading = false);
      if (success) {
        _showSuccessPopup('Virement effectué avec succès !');
      } else {
        _showErrorPopup('Erreur lors du virement (Solde insuffisant)');
      }
    }
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
            Lottie.network(
              'https://assets10.lottiefiles.com/packages/lf20_pqnfmone.json', // Animation de succès
              width: 150,
              height: 150,
              repeat: false,
            ),
            const SizedBox(height: 24),
            const Text(
              'Félicitations !',
              style: TextStyle(fontSize: 22, fontWeight: FontWeight.w900, color: AppTheme.primaryBlue),
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
                  final router = GoRouter.of(context);
                  Navigator.pop(context); // Fermer le popup
                  router.pop(); // Retour au Dashboard
                },
                style: ElevatedButton.styleFrom(
                  backgroundColor: AppTheme.primaryBlue,
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
                color: AppTheme.errorRed.withOpacity(0.1),
                shape: BoxShape.circle,
              ),
              child: const Icon(Icons.error_rounded, color: AppTheme.errorRed, size: 60),
            ),
            const SizedBox(height: 24),
            const Text(
              'Oups !',
              style: TextStyle(fontSize: 22, fontWeight: FontWeight.w900, color: AppTheme.primaryBlue),
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
                  foregroundColor: AppTheme.primaryBlue,
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
    final authState = context.read<AuthBloc>().state;
    String currentPhone = '771234567';
    if (authState is AuthSuccess && authState.phone != null) {
      currentPhone = authState.phone!;
    }
    final accounts = MockData.getAccountsForPhone(currentPhone);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Virement Interne', style: TextStyle(fontWeight: FontWeight.bold)),
        backgroundColor: AppTheme.primaryBlue,
        foregroundColor: Colors.white,
        elevation: 0,
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(24),
        child: Form(
          key: _formKey,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text(
                'Transférer de l\'argent entre vos comptes',
                style: TextStyle(fontSize: 16, color: Colors.grey, fontWeight: FontWeight.w500),
              ),
              const SizedBox(height: 32),
              
              _buildSectionTitle('Compte source'),
              const SizedBox(height: 12),
              _buildAccountDropdown(
                value: _fromAccountId,
                items: accounts,
                onChanged: (val) => setState(() => _fromAccountId = val),
                hint: 'Sélectionner le compte à débiter',
              ),
              
              const SizedBox(height: 24),
              
              _buildSectionTitle('Compte destination'),
              const SizedBox(height: 12),
              _buildAccountDropdown(
                value: _toAccountId,
                items: accounts,
                onChanged: (val) => setState(() => _toAccountId = val),
                hint: 'Sélectionner le compte à créditer',
              ),
              
              const SizedBox(height: 24),
              
              _buildSectionTitle('Montant (FCFA)'),
              const SizedBox(height: 12),
              TextFormField(
                controller: _amountController,
                keyboardType: TextInputType.number,
                style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 18),
                decoration: _buildInputDecoration('Ex: 50 000', Icons.attach_money_rounded),
                validator: (val) {
                  if (val == null || val.isEmpty) return 'Veuillez saisir un montant';
                  if (double.tryParse(val) == null || double.parse(val) <= 0) return 'Montant invalide';
                  return null;
                },
              ),
              
              const SizedBox(height: 24),
              
              _buildSectionTitle('Motif (Optionnel)'),
              const SizedBox(height: 12),
              TextFormField(
                controller: _reasonController,
                decoration: _buildInputDecoration('Ex: Épargne projet', Icons.description_rounded),
              ),
              
              const SizedBox(height: 48),
              
              SizedBox(
                width: double.infinity,
                height: 56,
                child: ElevatedButton(
                  onPressed: _isLoading ? null : _handleTransfer,
                  style: ElevatedButton.styleFrom(
                    backgroundColor: AppTheme.primaryBlue,
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
                  ),
                  child: _isLoading 
                    ? const CircularProgressIndicator(color: Colors.white)
                    : const Text(
                        'CONFIRMER LE VIREMENT',
                        style: TextStyle(fontWeight: FontWeight.bold, letterSpacing: 1),
                      ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildSectionTitle(String title) {
    return Text(
      title,
      style: const TextStyle(fontSize: 14, fontWeight: FontWeight.bold, color: AppTheme.primaryBlue),
    );
  }

  Widget _buildAccountDropdown({
    required String? value,
    required List<Map<String, dynamic>> items,
    required Function(String?) onChanged,
    required String hint,
  }) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
        boxShadow: AppTheme.softShadow,
      ),
      child: DropdownButtonHideUnderline(
        child: DropdownButton<String>(
          value: value,
          isExpanded: true,
          hint: Text(hint, style: TextStyle(color: Colors.grey.shade400)),
          items: items.map((acc) {
            return DropdownMenuItem<String>(
              value: acc['id'],
              child: Text('${acc['libelle']} (${acc['numeroCompte']})'),
            );
          }).toList(),
          onChanged: onChanged,
        ),
      ),
    );
  }

  InputDecoration _buildInputDecoration(String hint, IconData icon) {
    return InputDecoration(
      hintText: hint,
      prefixIcon: Icon(icon, color: AppTheme.primaryBlue.withOpacity(0.5)),
      filled: true,
      fillColor: Colors.white,
      border: OutlineInputBorder(
        borderRadius: BorderRadius.circular(16),
        borderSide: BorderSide.none,
      ),
      enabledBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(16),
        borderSide: BorderSide.none,
      ),
      focusedBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(16),
        borderSide: const BorderSide(color: AppTheme.primaryBlue, width: 2),
      ),
    );
  }
}
