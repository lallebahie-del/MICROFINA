import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import '../../../core/theme/app_colors.dart';
import '../../blocs/loan/loan_bloc.dart';

class NewLoanRequestScreen extends StatefulWidget {
  const NewLoanRequestScreen({super.key});

  @override
  State<NewLoanRequestScreen> createState() => _NewLoanRequestScreenState();
}

class _NewLoanRequestScreenState extends State<NewLoanRequestScreen> {
  final _formKey = GlobalKey<FormState>();
  final _amountController = TextEditingController();
  String? _selectedPurpose;
  int _selectedDuration = 12;
  bool _awaitingSubmitResult = false;
  final List<int> _durations = [6, 12, 18, 24, 36, 48, 60];
  final List<String> _purposes = [
    "Achat de matériel",
    "Besoin de trésorerie",
    "Investissement immobilier",
    "Consommation",
    "Autre",
  ];

  @override
  void dispose() {
    _amountController.dispose();
    super.dispose();
  }

  void _submitRequest() {
    if (_formKey.currentState!.validate()) {
      setState(() => _awaitingSubmitResult = true);
      context.read<LoanBloc>().add(
        SubmitLoanRequest(
          amount: double.parse(_amountController.text),
          durationMonths: _selectedDuration,
          purpose: _selectedPurpose ?? "Non spécifié",
        ),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return BlocListener<LoanBloc, LoanState>(
      listenWhen: (prev, curr) =>
          _awaitingSubmitResult &&
          (prev.status != curr.status) &&
          (curr.status == LoanStatus.success ||
              curr.status == LoanStatus.failure),
      listener: (context, state) {
        if (!_awaitingSubmitResult) return;
        if (state.status == LoanStatus.success) {
          _awaitingSubmitResult = false;
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('Demande envoyée avec succès !')),
          );
          Navigator.pop(context);
        } else if (state.status == LoanStatus.failure) {
          _awaitingSubmitResult = false;
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text('Erreur: ${state.errorMessage}')),
          );
        }
      },
      child: Scaffold(
        backgroundColor: const Color(0xFFF8FAFC),
        appBar: AppBar(
          title: const Text(
            'Demande de Crédit',
            style: TextStyle(fontWeight: FontWeight.w800),
          ),
          centerTitle: true,
          elevation: 0,
          backgroundColor: Colors.transparent,
          foregroundColor: AppColors.primary,
        ),
        body: SingleChildScrollView(
          padding: const EdgeInsets.all(24),
          child: Form(
            key: _formKey,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text(
                  'Remplissez ce formulaire pour soumettre votre demande de financement en ligne.',
                  style: TextStyle(
                    fontSize: 15,
                    color: Colors.grey,
                    fontWeight: FontWeight.w500,
                  ),
                ),
                const SizedBox(height: 32),

                _buildSectionTitle('MONTANT SOUHAITÉ (FCFA)'),
                const SizedBox(height: 12),
                TextFormField(
                  controller: _amountController,
                  keyboardType: TextInputType.number,
                  style: const TextStyle(
                    fontWeight: FontWeight.w900,
                    fontSize: 24,
                    color: AppColors.primary,
                  ),
                  textAlign: TextAlign.center,
                  decoration: InputDecoration(
                    hintText: '0 FCFA',
                    filled: true,
                    fillColor: Colors.white,
                    border: OutlineInputBorder(
                      borderRadius: BorderRadius.circular(20),
                      borderSide: BorderSide.none,
                    ),
                    contentPadding: const EdgeInsets.all(24),
                  ),
                  validator: (val) {
                    if (val == null || val.isEmpty)
                      return 'Veuillez saisir un montant';
                    if (double.tryParse(val) == null ||
                        double.parse(val) < 100000)
                      return 'Minimum 100 000 FCFA';
                    return null;
                  },
                ),

                const SizedBox(height: 32),

                _buildSectionTitle('DURÉE DU CRÉDIT'),
                const SizedBox(height: 12),
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 16),
                  decoration: BoxDecoration(
                    color: Colors.white,
                    borderRadius: BorderRadius.circular(16),
                  ),
                  child: DropdownButtonHideUnderline(
                    child: DropdownButton<int>(
                      value: _selectedDuration,
                      isExpanded: true,
                      items: _durations.map((d) {
                        return DropdownMenuItem<int>(
                          value: d,
                          child: Text('$d mois'),
                        );
                      }).toList(),
                      onChanged: (val) =>
                          setState(() => _selectedDuration = val!),
                    ),
                  ),
                ),

                const SizedBox(height: 32),

                _buildSectionTitle('OBJET DU CRÉDIT'),
                const SizedBox(height: 12),
                DropdownButtonFormField<String>(
                  value: _selectedPurpose,
                  items: _purposes.map((p) {
                    return DropdownMenuItem<String>(value: p, child: Text(p));
                  }).toList(),
                  onChanged: (val) => setState(() => _selectedPurpose = val),
                  decoration: InputDecoration(
                    hintText: 'Sélectionnez le motif',
                    filled: true,
                    fillColor: Colors.white,
                    border: OutlineInputBorder(
                      borderRadius: BorderRadius.circular(16),
                      borderSide: BorderSide.none,
                    ),
                  ),
                  validator: (val) {
                    if (val == null || val.isEmpty)
                      return 'Veuillez sélectionner un motif';
                    return null;
                  },
                ),

                const SizedBox(height: 48),

                BlocBuilder<LoanBloc, LoanState>(
                  builder: (context, state) {
                    final isLoading = state.status == LoanStatus.loading;
                    return SizedBox(
                      width: double.infinity,
                      height: 60,
                      child: ElevatedButton(
                        onPressed: isLoading ? null : _submitRequest,
                        style: ElevatedButton.styleFrom(
                          backgroundColor: AppColors.primary,
                          shape: RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(20),
                          ),
                          elevation: 4,
                          shadowColor: AppColors.primary.withOpacity(0.4),
                        ),
                        child: isLoading
                            ? const CircularProgressIndicator(
                                color: Colors.white,
                              )
                            : const Text(
                                'SOUMETTRE LA DEMANDE',
                                style: TextStyle(
                                  fontWeight: FontWeight.w900,
                                  fontSize: 16,
                                  letterSpacing: 1,
                                ),
                              ),
                      ),
                    );
                  },
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildSectionTitle(String title) {
    return Text(
      title,
      style: TextStyle(
        fontSize: 11,
        fontWeight: FontWeight.w900,
        color: AppColors.primary.withOpacity(0.4),
        letterSpacing: 1.5,
      ),
    );
  }
}
