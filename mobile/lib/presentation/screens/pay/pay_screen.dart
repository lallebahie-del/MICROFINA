import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:local_auth/local_auth.dart';
import 'package:lottie/lottie.dart';
import '../../../core/theme/app_theme.dart';
import '../../../data/datasources/mock/mock_data.dart';
import '../../blocs/auth/auth_bloc.dart';
import '../../blocs/auth/auth_state.dart';

class PayScreen extends StatefulWidget {
  const PayScreen({super.key});

  @override
  State<PayScreen> createState() => _PayScreenState();
}

class _PayScreenState extends State<PayScreen> {
  final LocalAuthentication _auth = LocalAuthentication();

  @override
  Widget build(BuildContext context) {
    final services = [
      {'icon': Icons.flash_on_rounded, 'label': 'Senelec', 'color': Colors.orange},
      {'icon': Icons.water_drop_rounded, 'label': 'Sen\'Eau', 'color': Colors.blue},
      {'icon': Icons.phone_android_rounded, 'label': 'Crédit Tel', 'color': Colors.green},
      {'icon': Icons.tv_rounded, 'label': 'Canal+', 'color': Colors.black},
    ];

    return Scaffold(
      appBar: AppBar(
        title: const Text('Paiement de services', style: TextStyle(fontWeight: FontWeight.bold)),
        backgroundColor: AppTheme.primaryBlue,
        foregroundColor: Colors.white,
        elevation: 0,
        leading: IconButton(
          icon: const Icon(Icons.arrow_back_ios_rounded),
          onPressed: () => context.pop(),
        ),
      ),
      body: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              'Que souhaitez-vous régler ?',
              style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold, color: AppTheme.primaryBlue),
            ),
            const SizedBox(height: 24),
            Expanded(
              child: GridView.builder(
                gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                  crossAxisCount: 2,
                  crossAxisSpacing: 16,
                  mainAxisSpacing: 16,
                  childAspectRatio: 1.1,
                ),
                itemCount: services.length,
                itemBuilder: (context, index) {
                  final service = services[index];
                  return Container(
                    decoration: BoxDecoration(
                      color: Colors.white,
                      borderRadius: BorderRadius.circular(24),
                      boxShadow: AppTheme.softShadow,
                    ),
                    child: InkWell(
                      onTap: () => _showPaymentForm(context, service['label'] as String, service['icon'] as IconData, service['color'] as Color),
                      borderRadius: BorderRadius.circular(24),
                      child: Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Container(
                            padding: const EdgeInsets.all(16),
                            decoration: BoxDecoration(
                              color: (service['color'] as Color).withOpacity(0.1),
                              shape: BoxShape.circle,
                            ),
                            child: Icon(service['icon'] as IconData, color: service['color'] as Color, size: 30),
                          ),
                          const SizedBox(height: 12),
                          Text(
                            service['label'] as String,
                            style: const TextStyle(fontWeight: FontWeight.bold, color: AppTheme.primaryBlue),
                          ),
                        ],
                      ),
                    ),
                  );
                },
              ),
            ),
            SizedBox(
              width: double.infinity,
              height: 56,
              child: ElevatedButton(
                onPressed: () => context.pop(),
                style: ElevatedButton.styleFrom(
                  backgroundColor: AppTheme.primaryBlue,
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
                ),
                child: const Text('RETOUR AU DASHBOARD', style: TextStyle(fontWeight: FontWeight.bold)),
              ),
            ),
          ],
        ),
      ),
    );
  }

  void _showPaymentForm(BuildContext context, String serviceName, IconData icon, Color color) {
    final amountController = TextEditingController();
    final idController = TextEditingController();

    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (context) => Container(
        height: MediaQuery.of(context).size.height * 0.75,
        decoration: const BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.only(topLeft: Radius.circular(40), topRight: Radius.circular(40)),
        ),
        padding: const EdgeInsets.all(32),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Center(child: Container(width: 40, height: 4, decoration: BoxDecoration(color: Colors.grey[300], borderRadius: BorderRadius.circular(2)))),
            const SizedBox(height: 24),
            Row(
              children: [
                Icon(icon, color: color, size: 32),
                const SizedBox(width: 16),
                Text('Paiement $serviceName', style: const TextStyle(fontSize: 24, fontWeight: FontWeight.bold, color: AppTheme.primaryBlue)),
              ],
            ),
            const SizedBox(height: 32),
            const Text('Référence / Numéro de compteur', style: TextStyle(fontWeight: FontWeight.bold, color: AppTheme.primaryBlue)),
            const SizedBox(height: 8),
            TextField(
              controller: idController,
              decoration: InputDecoration(
                hintText: 'Saisissez la référence',
                filled: true,
                fillColor: Colors.grey[100],
                border: OutlineInputBorder(borderRadius: BorderRadius.circular(16), borderSide: BorderSide.none),
              ),
            ),
            const SizedBox(height: 24),
            const Text('Montant à payer (FCFA)', style: TextStyle(fontWeight: FontWeight.bold, color: AppTheme.primaryBlue)),
            const SizedBox(height: 8),
            TextField(
              controller: amountController,
              keyboardType: TextInputType.number,
              decoration: InputDecoration(
                hintText: '0 FCFA',
                filled: true,
                fillColor: Colors.grey[100],
                border: OutlineInputBorder(borderRadius: BorderRadius.circular(16), borderSide: BorderSide.none),
              ),
            ),
            const Spacer(),
            SizedBox(
              width: double.infinity,
              height: 56,
              child: ElevatedButton(
                onPressed: () async {
                  if (amountController.text.isEmpty || idController.text.isEmpty) return;
                  final double amount = double.parse(amountController.text);
                  final String reference = idController.text;
                  
                  final screenContext = this.context;
                  Navigator.pop(context); // Fermer le formulaire

                  // Proposer d'abord la biométrie
                  final bool canCheckBiometrics = await _auth.canCheckBiometrics;
                  final bool isDeviceSupported = await _auth.isDeviceSupported();

                  if (canCheckBiometrics && isDeviceSupported) {
                    try {
                      final bool didAuthenticate = await _auth.authenticate(
                        localizedReason: 'Authentifiez-vous pour valider le paiement de $amount FCFA',
                        options: const AuthenticationOptions(
                          stickyAuth: true,
                          biometricOnly: true,
                        ),
                      );
                      if (didAuthenticate) {
                        if (mounted) {
                          _executePayment(screenContext, serviceName, amount, reference, color);
                        }
                        return;
                      }
                    } catch (e) {
                      debugPrint('Erreur biométrie: $e');
                    }
                  }

                  // Repli sur le PIN
                  if (mounted) {
                    _showPinDialog(screenContext, serviceName, amount, reference, color);
                  }
                },
                style: ElevatedButton.styleFrom(
                  backgroundColor: color,
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
                ),
                child: const Text('CONFIRMER', style: TextStyle(fontWeight: FontWeight.bold, color: Colors.white)),
              ),
            ),
          ],
        ),
      ),
    );
  }

  void _showPinDialog(BuildContext context, String serviceName, double amount, String reference, Color color) {
    final pinController = TextEditingController();
    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (context) => AlertDialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
        title: const Text('Validation de Paiement', 
          style: TextStyle(fontWeight: FontWeight.bold, color: AppTheme.primaryBlue)),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Text('Saisissez votre code secret pour payer $amount FCFA ($serviceName).'),
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
          TextButton(onPressed: () => Navigator.pop(context), child: const Text('ANNULER')),
          ElevatedButton(
            onPressed: () {
              if (pinController.text.length == 4) {
                final screenContext = this.context;
                Navigator.pop(context);
                if (mounted) {
                  _executePayment(screenContext, serviceName, amount, reference, color);
                }
              }
            },
            style: ElevatedButton.styleFrom(backgroundColor: AppTheme.primaryBlue),
            child: const Text('VALIDER'),
          ),
        ],
      ),
    );
  }

  void _executePayment(BuildContext context, String serviceName, double amount, String reference, Color color) async {
    final authState = context.read<AuthBloc>().state;
    String currentPhone = '771234567';
    if (authState is AuthSuccess && authState.phone != null) {
      currentPhone = authState.phone!;
    }
    
    final accounts = MockData.getAccountsForPhone(currentPhone);
    final defaultAccount = accounts.firstWhere((acc) => acc['isDefaultAccount'], orElse: () => accounts.first);

    final success = await MockData.performServicePayment(
      accountId: defaultAccount['id'],
      serviceName: serviceName,
      amount: amount,
      reference: reference,
    );

    if (mounted) {
      if (success) {
        _showSuccessPopup(context, 'Paiement de $amount FCFA pour $serviceName effectué !');
      } else {
        _showErrorPopup(context, 'Erreur : Solde insuffisant');
      }
    }
  }

  void _showSuccessPopup(BuildContext context, String message) {
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
            const Text('Succès !', style: TextStyle(fontSize: 22, fontWeight: FontWeight.w900, color: AppTheme.primaryBlue)),
            const SizedBox(height: 12),
            Text(message, textAlign: TextAlign.center, style: TextStyle(fontSize: 14, color: Colors.grey[600])),
            const SizedBox(height: 32),
            SizedBox(
              width: double.infinity,
              height: 56,
              child: ElevatedButton(
                onPressed: () {
                  final router = GoRouter.of(context);
                  Navigator.pop(context);
                  router.pop();
                },
                style: ElevatedButton.styleFrom(backgroundColor: AppTheme.primaryBlue, shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16))),
                child: const Text('RETOUR À L\'ACCUEIL', style: TextStyle(fontWeight: FontWeight.bold)),
              ),
            ),
          ],
        ),
      ),
    );
  }

  void _showErrorPopup(BuildContext context, String message) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(28)),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(color: AppTheme.errorRed.withOpacity(0.1), shape: BoxShape.circle),
              child: const Icon(Icons.error_rounded, color: AppTheme.errorRed, size: 60),
            ),
            const SizedBox(height: 24),
            const Text('Oups !', style: TextStyle(fontSize: 22, fontWeight: FontWeight.w900, color: AppTheme.primaryBlue)),
            const SizedBox(height: 12),
            Text(message, textAlign: TextAlign.center, style: TextStyle(fontSize: 14, color: Colors.grey[600])),
            const SizedBox(height: 32),
            SizedBox(
              width: double.infinity,
              height: 56,
              child: ElevatedButton(
                onPressed: () => Navigator.pop(context),
                style: ElevatedButton.styleFrom(backgroundColor: Colors.grey[200], foregroundColor: AppTheme.primaryBlue, shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16))),
                child: const Text('RÉESSAYER', style: TextStyle(fontWeight: FontWeight.bold)),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
