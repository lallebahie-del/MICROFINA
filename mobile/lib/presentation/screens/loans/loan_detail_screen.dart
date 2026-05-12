import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';
import 'package:intl/intl.dart';
import 'package:lottie/lottie.dart';
import '../../../core/theme/app_theme.dart';
import '../../../core/theme/app_colors.dart';
import '../../../data/models/credit_model.dart';
import '../../blocs/loan/loan_bloc.dart';
import '../../widgets/loan/amortization_timeline.dart';

class LoanDetailScreen extends StatefulWidget {
  final String loanId;

  const LoanDetailScreen({super.key, required this.loanId});

  @override
  State<LoanDetailScreen> createState() => _LoanDetailScreenState();
}

class _LoanDetailScreenState extends State<LoanDetailScreen> {
  bool _isPaying = false;

  @override
  void initState() {
    super.initState();
    context.read<LoanBloc>().add(FetchLoanDetails(widget.loanId));
  }

  void _showInsufficientFundsDialog(BuildContext context, String message) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
        title: const Row(
          children: [
            Icon(Icons.warning_amber_rounded, color: Colors.orange, size: 28),
            SizedBox(width: 12),
            Text("Solde Insuffisant", style: TextStyle(fontWeight: FontWeight.w900, color: AppColors.primary)),
          ],
        ),
        content: Text(
          message,
          style: TextStyle(color: Colors.grey[600], fontSize: 14),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text("COMPRIS", style: TextStyle(fontWeight: FontWeight.bold, color: AppColors.primary)),
          ),
          ElevatedButton(
            onPressed: () {
              Navigator.pop(context);
              // Optionnel: Rediriger vers l'écran de dépôt/recharge
            },
            style: ElevatedButton.styleFrom(
              backgroundColor: AppColors.primary,
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
            ),
            child: const Text("RECHARGER", style: TextStyle(fontWeight: FontWeight.bold, color: Colors.white)),
          ),
        ],
      ),
    );
  }

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
            width: 220,
            height: 220,
            padding: const EdgeInsets.all(24),
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(40),
              boxShadow: [
                BoxShadow(
                  color: Colors.black.withOpacity(0.1),
                  blurRadius: 30,
                  offset: const Offset(0, 15),
                ),
              ],
            ),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                Expanded(
                  child: Lottie.network(
                    'https://assets10.lottiefiles.com/packages/lf20_af7p8v6v.json',
                    repeat: false,
                  ),
                ),
                const Text(
                  "Paiement Réussi !",
                  style: TextStyle(
                    fontWeight: FontWeight.w900,
                    color: AppColors.success,
                    fontSize: 16,
                  ),
                ),
              ],
            ),
          ),
        );
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: BlocListener<LoanBloc, LoanState>(
        listener: (context, state) {
          if (state.status == LoanStatus.loading) {
            _isPaying = true;
          }

          if (state.status == LoanStatus.success && _isPaying) {
            _isPaying = false;
            // On vérifie si c'est un succès de paiement (le capital a diminué ou une échéance a été payée)
            _showSuccessAnimation(context);
          }

          if (state.status == LoanStatus.failure && state.errorMessage != null) {
            _isPaying = false;
            if (state.errorMessage!.contains("Solde insuffisant")) {
              _showInsufficientFundsDialog(context, state.errorMessage!);
            } else {
              ScaffoldMessenger.of(context).showSnackBar(
                SnackBar(
                  content: Text(state.errorMessage!),
                  backgroundColor: Colors.red,
                  behavior: SnackBarBehavior.floating,
                ),
              );
            }
          }
        },
        child: BlocBuilder<LoanBloc, LoanState>(
          builder: (context, state) {
          if (state.status == LoanStatus.loading) {
            return const Center(child: CircularProgressIndicator());
          }

          if (state.status == LoanStatus.failure && state.selectedLoan == null) {
            return Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  const Icon(Icons.error_outline, size: 60, color: Colors.red),
                  const SizedBox(height: 16),
                  Text("Erreur: ${state.errorMessage}"),
                  const SizedBox(height: 24),
                  ElevatedButton(
                    onPressed: () => context.read<LoanBloc>().add(FetchLoanDetails(widget.loanId)),
                    child: const Text("Réessayer"),
                  ),
                ],
              ),
            );
          }

          final loan = state.selectedLoan;
          if (loan == null) {
            return const Center(child: Text("Prêt non trouvé"));
          }

          final String endDateStr = DateFormat('dd/MM/yyyy').format(loan.endDate);

          return CustomScrollView(
            slivers: [
              // Sticky Header
              SliverAppBar(
                expandedHeight: 220.0,
                floating: false,
                pinned: true,
                elevation: 0,
                backgroundColor: AppColors.primary,
                leading: IconButton(
                  icon: const Icon(Icons.arrow_back_ios_new, color: Colors.white, size: 20),
                  onPressed: () => context.pop(),
                ),
                flexibleSpace: FlexibleSpaceBar(
                  centerTitle: true,
                  title: Text(
                    "Prêt ${loan.loanId}",
                    style: const TextStyle(
                      color: Colors.white,
                      fontWeight: FontWeight.bold,
                      fontSize: 16,
                    ),
                  ),
                  background: Container(
                    padding: const EdgeInsets.only(top: 40),
                    decoration: BoxDecoration(
                      gradient: LinearGradient(
                        begin: Alignment.topCenter,
                        end: Alignment.bottomCenter,
                        colors: [
                          AppColors.primary,
                          AppColors.primary.withOpacity(0.8),
                        ],
                      ),
                    ),
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Text(
                          NumberFormat.currency(symbol: 'FCFA', decimalDigits: 0)
                              .format(loan.totalAmount),
                          style: const TextStyle(
                            color: Colors.white,
                            fontSize: 32,
                            fontWeight: FontWeight.w900,
                          ),
                        ),
                        const Text(
                          "Montant Initial Emprunté",
                          style: TextStyle(color: Colors.white70, fontSize: 13, fontWeight: FontWeight.w500),
                        ),
                        const SizedBox(height: 25),
                        Padding(
                          padding: const EdgeInsets.symmetric(horizontal: 20),
                          child: Row(
                            mainAxisAlignment: MainAxisAlignment.spaceAround,
                            children: [
                              _buildHeaderStat("Taux", "${loan.interestRate}%"),
                              _buildHeaderStat("Échéance", endDateStr),
                              _buildHeaderStat("Statut", loan.statusLabel),
                            ],
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
              ),

              // Résumé du Capital Restant Dû
              SliverToBoxAdapter(
                child: Padding(
                  padding: const EdgeInsets.all(24.0),
                  child: Container(
                    padding: const EdgeInsets.all(24),
                    decoration: BoxDecoration(
                      color: Colors.white,
                      borderRadius: BorderRadius.circular(24),
                      boxShadow: [
                        BoxShadow(
                          color: Colors.black.withOpacity(0.05),
                          blurRadius: 20,
                          offset: const Offset(0, 10),
                        ),
                      ],
                    ),
                    child: Column(
                      children: [
                        const Text(
                          "Capital Restant Dû",
                          style: TextStyle(fontSize: 14, color: Colors.grey, fontWeight: FontWeight.w600),
                        ),
                        const SizedBox(height: 12),
                        Text(
                          NumberFormat.currency(symbol: 'FCFA', decimalDigits: 0)
                              .format(loan.remainingCapital),
                          style: TextStyle(
                            fontSize: 28,
                            fontWeight: FontWeight.w900,
                            color: AppColors.primary,
                          ),
                        ),
                        const SizedBox(height: 20),
                        ClipRRect(
                          borderRadius: BorderRadius.circular(12),
                          child: LinearProgressIndicator(
                            value: loan.progressPercentage / 100,
                            backgroundColor: Colors.grey[100],
                            valueColor: AlwaysStoppedAnimation<Color>(loan.isLate ? Colors.red : Colors.green),
                            minHeight: 10,
                          ),
                        ),
                        const SizedBox(height: 12),
                        Text(
                          "Remboursé à ${loan.progressPercentage.toInt()}%",
                          style: TextStyle(color: Colors.grey[600], fontSize: 12, fontWeight: FontWeight.bold),
                        ),
                      ],
                    ),
                  ),
                ),
              ),

              // Section Garanties
              SliverToBoxAdapter(
                child: Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 24.0),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const Text(
                        "GARANTIES",
                        style: TextStyle(fontSize: 11, fontWeight: FontWeight.w900, color: Colors.grey, letterSpacing: 1.5),
                      ),
                      const SizedBox(height: 16),
                      ...state.guarantees.map((g) => Container(
                            margin: const EdgeInsets.only(bottom: 12),
                            decoration: BoxDecoration(
                              color: Colors.white,
                              borderRadius: BorderRadius.circular(20),
                              border: Border.all(color: Colors.grey[100]!),
                            ),
                            child: ListTile(
                              contentPadding: const EdgeInsets.symmetric(horizontal: 20, vertical: 8),
                              leading: Container(
                                padding: const EdgeInsets.all(10),
                                decoration: BoxDecoration(
                                  color: Colors.blue.withOpacity(0.1),
                                  shape: BoxShape.circle,
                                ),
                                child: const Icon(Icons.security, color: Colors.blue, size: 20),
                              ),
                              title: Text(g.type, style: const TextStyle(fontWeight: FontWeight.bold)),
                              subtitle: Text(g.description, style: TextStyle(color: Colors.grey[600], fontSize: 12)),
                              trailing: Text(
                                NumberFormat.currency(symbol: 'FCFA', decimalDigits: 0).format(g.valeur),
                                style: TextStyle(fontWeight: FontWeight.w900, color: AppColors.primary),
                              ),
                            ),
                          )),
                      if (state.guarantees.isEmpty)
                        Container(
                          width: double.infinity,
                          padding: const EdgeInsets.all(20),
                          decoration: BoxDecoration(
                            color: Colors.grey[50],
                            borderRadius: BorderRadius.circular(20),
                          ),
                          child: const Center(
                            child: Text("Aucune garantie enregistrée", style: TextStyle(color: Colors.grey, fontSize: 13)),
                          ),
                        ),
                      const SizedBox(height: 32),
                    ],
                  ),
                ),
              ),

              // Section Échéancier (Timeline)
              SliverToBoxAdapter(
                child: Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 24.0),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const Text(
                        "ÉCHÉANCIER DE REMBOURSEMENT",
                        style: TextStyle(fontSize: 11, fontWeight: FontWeight.w900, color: Colors.grey, letterSpacing: 1.5),
                      ),
                      const SizedBox(height: 24),
                      AmortizationTimeline(schedule: state.amortizationSchedule),
                      const SizedBox(height: 40),
                    ],
                  ),
                ),
              ),
            ],
          );
        },
      ),
    ),
  );
}

  Widget _buildHeaderStat(String label, String value) {
    return Column(
      children: [
        Text(
          value,
          style: const TextStyle(
            color: Colors.white,
            fontWeight: FontWeight.bold,
            fontSize: 16,
          ),
        ),
        Text(
          label,
          style: const TextStyle(color: Colors.white70, fontSize: 12),
        ),
      ],
    );
  }
}
