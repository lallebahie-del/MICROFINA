import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:local_auth/local_auth.dart';
import 'package:lottie/lottie.dart';
import '../../../data/datasources/mock/mock_data.dart';
import '../../../data/models/amortp_model.dart';
import 'package:intl/intl.dart';
import '../../blocs/loan/loan_bloc.dart';
import '../../../core/theme/app_theme.dart';
import '../../../core/theme/app_colors.dart';

class AmortizationTimeline extends StatelessWidget {
  final List<AmortpModel> schedule;
  final LocalAuthentication _auth = LocalAuthentication();

  AmortizationTimeline({
    super.key,
    required this.schedule,
  });

  void _showSuccessAnimation(BuildContext context) {
    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (context) {
        Future.delayed(const Duration(seconds: 2), () {
          if (context.mounted) Navigator.pop(context);
        });
        return Center(
          child: Container(
            width: 200,
            height: 200,
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(32),
            ),
            child: Lottie.network(
              'https://assets10.lottiefiles.com/packages/lf20_af7p8v6v.json', // Animation de succès
              repeat: false,
              errorBuilder: (context, error, stackTrace) => const Icon(Icons.check_circle, size: 100, color: AppColors.success),
            ),
          ),
        );
      },
    );
  }

  Future<void> _handlePayment(BuildContext context, AmortpModel item) async {
    // 1. Vérification dynamique du solde avant toute action
    final currentBalance = MockData.getDefaultAccountBalance();
    
    if (currentBalance < item.montantTotal) {
      if (context.mounted) {
        // Déclencher l'erreur de solde insuffisant via le Bloc pour afficher la pop-up
        context.read<LoanBloc>().add(PayInstallment(
          loanId: item.loanId,
          installmentId: item.id,
        ));
      }
      return;
    }

    // 2. Authentification biométrique seulement si le solde est suffisant
    try {
      final bool canCheckBiometrics = await _auth.canCheckBiometrics;
      final bool isDeviceSupported = await _auth.isDeviceSupported();

      bool authenticated = false;
      if (canCheckBiometrics && isDeviceSupported) {
        authenticated = await _auth.authenticate(
          localizedReason: "Authentifiez-vous pour valider le paiement de ${NumberFormat.currency(symbol: 'FCFA', decimalDigits: 0).format(item.montantTotal)}",
          options: const AuthenticationOptions(
            stickyAuth: true,
            biometricOnly: false, // Permet le repli sur PIN/Schéma si FaceID/Empreinte échoue
          ),
        );
      } else {
        // Si pas de biométrie, on pourrait demander un PIN personnalisé ici
        // Pour ce sprint, on simule une validation réussie si l'appareil ne supporte pas la biométrie
        authenticated = true; 
      }

      if (authenticated && context.mounted) {
        context.read<LoanBloc>().add(PayInstallment(
              loanId: item.loanId,
              installmentId: item.id,
            ));
      }
    } catch (e) {
      debugPrint('Erreur d\'authentification: $e');
    }
  }

  void _showPaymentConfirmation(BuildContext context, AmortpModel item) {
    showModalBottomSheet(
      context: context,
      backgroundColor: Colors.transparent,
      builder: (bottomSheetContext) => Container(
        padding: const EdgeInsets.all(32),
        decoration: const BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.vertical(top: Radius.circular(32)),
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Container(
              width: 40,
              height: 4,
              decoration: BoxDecoration(
                color: Colors.grey[300],
                borderRadius: BorderRadius.circular(2),
              ),
            ),
            const SizedBox(height: 32),
            const Text(
              "Confirmer le Paiement",
              style: TextStyle(fontSize: 20, fontWeight: FontWeight.w900, color: AppColors.primary),
            ),
            const SizedBox(height: 16),
            Text(
              "Voulez-vous régler l'échéance n°${item.id} d'un montant de :",
              textAlign: TextAlign.center,
              style: TextStyle(color: Colors.grey[600], fontSize: 14),
            ),
            const SizedBox(height: 12),
            Text(
              NumberFormat.currency(symbol: 'FCFA', decimalDigits: 0).format(item.montantTotal),
              style: const TextStyle(fontSize: 28, fontWeight: FontWeight.w900, color: AppColors.primary),
            ),
            const SizedBox(height: 40),
            Row(
              children: [
                Expanded(
                  child: OutlinedButton(
                    onPressed: () => Navigator.pop(bottomSheetContext),
                    style: OutlinedButton.styleFrom(
                      padding: const EdgeInsets.symmetric(vertical: 16),
                      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
                      side: BorderSide(color: Colors.grey[300]!),
                    ),
                    child: const Text("ANNULER", style: TextStyle(color: Colors.grey, fontWeight: FontWeight.bold)),
                  ),
                ),
                const SizedBox(width: 16),
                Expanded(
                  child: ElevatedButton(
                    onPressed: () {
                      Navigator.pop(bottomSheetContext);
                      _handlePayment(context, item);
                    },
                    style: ElevatedButton.styleFrom(
                      backgroundColor: AppColors.primary,
                      padding: const EdgeInsets.symmetric(vertical: 16),
                      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
                      elevation: 0,
                    ),
                    child: const Text("PAYER MAINTENANT", style: TextStyle(fontWeight: FontWeight.bold)),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 16),
          ],
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    if (schedule.isEmpty) {
      return const Center(
        child: Padding(
          padding: EdgeInsets.all(20.0),
          child: Text("Aucune échéance trouvée."),
        ),
      );
    }

    final now = DateTime.now();

    return ListView.builder(
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      itemCount: schedule.length,
      itemBuilder: (context, index) {
        final item = schedule[index];
        final isLast = index == schedule.length - 1;
        final DateTime dueDate = DateTime.parse(item.dateEcheance);

        // Logique de statut stricte selon la demande
        late final String status;
        if (item.estPaye) {
          status = "PAYE";
        } else if (dueDate.isBefore(now)) {
          status = "IMPAYE";
        } else {
          status = "A_VENIR";
        }

        return IntrinsicHeight(
          child: Row(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              // Timeline avec CustomPainter
              CustomPaint(
                size: const Size(40, double.infinity),
                painter: TimelinePainter(
                  status: status,
                  isLast: isLast,
                  isFirst: index == 0,
                ),
              ),
              const SizedBox(width: 12),
              // Contenu
              Expanded(
                child: Padding(
                  padding: const EdgeInsets.only(bottom: 32.0),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          Text(
                            "Échéance n°${item.id}",
                            style: TextStyle(
                              fontWeight: FontWeight.w800,
                              fontSize: 14,
                              color: status == "PAYE" ? Colors.grey : Colors.black87,
                            ),
                          ),
                          Text(
                            DateFormat('dd MMM yyyy').format(dueDate),
                            style: TextStyle(
                              fontSize: 12,
                              color: Colors.grey[500],
                              fontWeight: FontWeight.w500,
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 6),
                      Text(
                        NumberFormat.currency(symbol: 'FCFA', decimalDigits: 0).format(item.montantTotal),
                        style: TextStyle(
                          fontSize: 16,
                          fontWeight: status == "IMPAYE" ? FontWeight.w900 : FontWeight.w700,
                          color: status == "PAYE" 
                              ? Colors.grey 
                              : (status == "IMPAYE" ? Colors.red : Colors.black87),
                          decoration: status == "PAYE" ? TextDecoration.lineThrough : null,
                        ),
                      ),
                      if (status == "IMPAYE") ...[
                        const SizedBox(height: 4),
                        const Text(
                          "IMPAYÉ - À RÉGLER IMMÉDIATEMENT",
                          style: TextStyle(color: Colors.red, fontSize: 10, fontWeight: FontWeight.bold),
                        ),
                      ],
                      if (status != "PAYE") ...[
                        const SizedBox(height: 12),
                        SizedBox(
                          height: 36,
                          child: ElevatedButton(
                            onPressed: () => _showPaymentConfirmation(context, item),
                            style: ElevatedButton.styleFrom(
                              backgroundColor: status == "IMPAYE" ? Colors.red : AppColors.primary,
                              foregroundColor: Colors.white,
                              elevation: 0,
                              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
                              padding: const EdgeInsets.symmetric(horizontal: 16),
                            ),
                            child: const Text("PAYER", style: TextStyle(fontSize: 12, fontWeight: FontWeight.bold)),
                          ),
                        ),
                      ],
                    ],
                  ),
                ),
              ),
            ],
          ),
        );
      },
    );
  }
}

class TimelinePainter extends CustomPainter {
  final String status;
  final bool isLast;
  final bool isFirst;

  TimelinePainter({
    required this.status,
    required this.isLast,
    required this.isFirst,
  });

  @override
  void paint(Canvas canvas, Size size) {
    final center = size.width / 2;
    final paint = Paint()
      ..color = Colors.grey.withOpacity(0.2)
      ..strokeWidth = 2;

    // Ligne verticale
    if (!isLast) {
      canvas.drawLine(
        Offset(center, 20),
        Offset(center, size.height),
        paint,
      );
    }

    // Dessin du cercle
    final circlePaint = Paint()..style = PaintingStyle.fill;
    final borderPaint = Paint()
      ..style = PaintingStyle.stroke
      ..strokeWidth = 2;

    if (status == "PAYE") {
      circlePaint.color = Colors.green;
      canvas.drawCircle(Offset(center, 10), 10, circlePaint);
      
      // Petit check blanc
      final checkPaint = Paint()
        ..color = Colors.white
        ..style = PaintingStyle.stroke
        ..strokeWidth = 2;
      final path = Path()
        ..moveTo(center - 4, 10)
        ..lineTo(center - 1, 13)
        ..lineTo(center + 4, 7);
      canvas.drawPath(path, checkPaint);
    } else if (status == "IMPAYE") {
      circlePaint.color = Colors.red;
      canvas.drawCircle(Offset(center, 10), 10, circlePaint);
    } else {
      // A_VENIR : Cercle orange vide
      circlePaint.color = Colors.white;
      borderPaint.color = Colors.orange;
      canvas.drawCircle(Offset(center, 10), 10, circlePaint);
      canvas.drawCircle(Offset(center, 10), 10, borderPaint);
    }
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => false;
}
