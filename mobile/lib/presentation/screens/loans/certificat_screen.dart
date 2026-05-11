import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:intl/intl.dart';
import '../../../core/theme/app_theme.dart';
import '../../../core/utils/pdf_generator_service.dart';
import '../../../data/models/extra_models.dart';
import '../../blocs/certificat/certificat_bloc.dart';
import '../../widgets/loan/maturity_countdown.dart';

import '../../../core/widgets/app_shimmer.dart';

class CertificateScreen extends StatefulWidget {
  const CertificateScreen({super.key});

  @override
  State<CertificateScreen> createState() => _CertificateScreenState();
}

class _CertificateScreenState extends State<CertificateScreen> {
  @override
  void initState() {
    super.initState();
    context.read<CertificatBloc>().add(FetchCertificats());
  }

  @override
  Widget build(BuildContext context) {
    final currencyFormat = NumberFormat.currency(locale: 'fr_FR', symbol: 'FCFA', decimalDigits: 0);

    return Scaffold(
      backgroundColor: const Color(0xFFF8FAFC),
      appBar: AppBar(
        title: const Text('Suivi des Placements', style: TextStyle(fontWeight: FontWeight.w800)),
        centerTitle: true,
        elevation: 0,
        backgroundColor: Colors.transparent,
        foregroundColor: AppTheme.primaryBlue,
        leading: IconButton(
          icon: const Icon(Icons.arrow_back_ios_new, size: 20),
          onPressed: () => Navigator.pop(context),
        ),
      ),
      body: BlocBuilder<CertificatBloc, CertificatState>(
        builder: (context, state) {
          if (state.status == CertificatStatus.loading) {
            return _buildSkeleton();
          }

          if (state.status == CertificatStatus.failure) {
            return Center(child: Text('Erreur: ${state.errorMessage}'));
          }

          if (state.certificats.isEmpty) {
            return const Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Icon(Icons.account_balance_wallet_outlined, size: 64, color: Colors.grey),
                  SizedBox(height: 16),
                  Text('Aucun placement à terme trouvé.', style: TextStyle(color: Colors.grey)),
                ],
              ),
            );
          }

          return ListView.builder(
            padding: const EdgeInsets.all(24),
            itemCount: state.certificats.length,
            itemBuilder: (context, index) {
              final cert = state.certificats[index];
              return Container(
                margin: const EdgeInsets.only(bottom: 24),
                decoration: BoxDecoration(
                  color: Colors.white,
                  borderRadius: BorderRadius.circular(32),
                  boxShadow: [
                    BoxShadow(
                      color: Colors.black.withOpacity(0.04),
                      blurRadius: 20,
                      offset: const Offset(0, 10),
                    ),
                  ],
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    // Header de la carte
                    Padding(
                      padding: const EdgeInsets.fromLTRB(24, 24, 24, 16),
                      child: Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(
                                cert.numeroCertificat,
                                style: TextStyle(
                                  color: AppTheme.primaryBlue.withOpacity(0.5),
                                  fontWeight: FontWeight.w800,
                                  fontSize: 12,
                                  letterSpacing: 1,
                                ),
                              ),
                              const SizedBox(height: 4),
                              const Text(
                                'DÉPÔT À TERME (DAT)',
                                style: TextStyle(
                                  fontWeight: FontWeight.w900,
                                  fontSize: 14,
                                  color: AppTheme.primaryBlue,
                                ),
                              ),
                            ],
                          ),
                          Container(
                            padding: const EdgeInsets.all(10),
                            decoration: BoxDecoration(
                              color: AppTheme.successGreen.withOpacity(0.1),
                              shape: BoxShape.circle,
                            ),
                            child: const Icon(Icons.verified_rounded, color: AppTheme.successGreen, size: 20),
                          ),
                        ],
                      ),
                    ),
                    
                    const Divider(height: 1),
                    
                    // Body avec MaturityCountdown
                    Padding(
                      padding: const EdgeInsets.all(24),
                      child: Column(
                        children: [
                          MaturityCountdown(
                            startDate: cert.startDate,
                            maturityDate: cert.maturityDate,
                            size: 160,
                          ),
                          const SizedBox(height: 32),
                          Row(
                            mainAxisAlignment: MainAxisAlignment.spaceBetween,
                            children: [
                              _buildDetailItem('Montant Initial', currencyFormat.format(cert.montantPlacement)),
                              _buildDetailItem('Taux Annuel', '${cert.tauxInteret}%'),
                            ],
                          ),
                          const SizedBox(height: 20),
                          Row(
                            mainAxisAlignment: MainAxisAlignment.spaceBetween,
                            children: [
                              _buildDetailItem('Date de début', DateFormat('dd MMM yyyy').format(cert.startDate)),
                              _buildDetailItem('Échéance', DateFormat('dd MMM yyyy').format(cert.maturityDate)),
                            ],
                          ),
                          const SizedBox(height: 20),
                          Container(
                            width: double.infinity,
                            padding: const EdgeInsets.all(16),
                            decoration: BoxDecoration(
                              color: AppTheme.lightBlue.withOpacity(0.5),
                              borderRadius: BorderRadius.circular(16),
                            ),
                            child: Row(
                              mainAxisAlignment: MainAxisAlignment.spaceBetween,
                              children: [
                                const Text(
                                  'Intérêts attendus',
                                  style: TextStyle(
                                    fontWeight: FontWeight.w700,
                                    color: AppTheme.primaryBlue,
                                    fontSize: 13,
                                  ),
                                ),
                                Text(
                                  '+ ${currencyFormat.format(cert.expectedInterests)}',
                                  style: const TextStyle(
                                    fontWeight: FontWeight.w900,
                                    color: AppTheme.successGreen,
                                    fontSize: 15,
                                  ),
                                ),
                              ],
                            ),
                          ),
                        ],
                      ),
                    ),
                    
                    // Footer Actions
                    Padding(
                      padding: const EdgeInsets.fromLTRB(24, 0, 24, 24),
                      child: SizedBox(
                        width: double.infinity,
                        height: 54,
                        child: ElevatedButton.icon(
                          onPressed: () => PdfGeneratorService.generateAndPreviewCertificate(certificat: cert),
                          icon: const Icon(Icons.file_download_outlined, size: 20),
                          label: const Text('TÉLÉCHARGER LE CERTIFICAT'),
                          style: ElevatedButton.styleFrom(
                            backgroundColor: AppTheme.primaryBlue,
                            foregroundColor: Colors.white,
                            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
                            elevation: 0,
                          ),
                        ),
                      ),
                    ),
                  ],
                ),
              );
            },
          );
        },
      ),
    );
  }

  Widget _buildSkeleton() {
    return ListView.builder(
      padding: const EdgeInsets.all(24),
      itemCount: 3,
      itemBuilder: (context, index) => Padding(
        padding: const EdgeInsets.only(bottom: 24),
        child: AppShimmer(height: 300, width: double.infinity, borderRadius: BorderRadius.circular(32)),
      ),
    );
  }

  Widget _buildDetailItem(String label, String value) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          label.toUpperCase(),
          style: TextStyle(
            color: Colors.grey[400],
            fontSize: 9,
            fontWeight: FontWeight.w900,
            letterSpacing: 1,
          ),
        ),
        const SizedBox(height: 4),
        Text(
          value,
          style: const TextStyle(
            fontWeight: FontWeight.w800,
            fontSize: 14,
            color: AppTheme.primaryBlue,
          ),
        ),
      ],
    );
  }
}
