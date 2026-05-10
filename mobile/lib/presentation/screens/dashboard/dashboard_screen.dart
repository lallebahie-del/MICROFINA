import 'dart:io';
import 'package:flutter/material.dart';
import 'package:fl_chart/fl_chart.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';
import 'package:intl/intl.dart';
import '../../../core/theme/app_theme.dart';
import '../../../data/datasources/mock/mock_data.dart';
import '../../../core/router/app_router.dart';
import '../../blocs/auth/auth_bloc.dart';
import '../../blocs/auth/auth_event.dart';
import '../../blocs/auth/auth_state.dart';

import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import '../../../core/storage/secure_storage_service.dart';

import 'package:shimmer/shimmer.dart';
import '../../../core/network/connectivity_service.dart';

class DashboardScreen extends StatefulWidget {
  const DashboardScreen({super.key});

  @override
  State<DashboardScreen> createState() => _DashboardScreenState();
}

class _DashboardScreenState extends State<DashboardScreen> {
  final _secureStorage = SecureStorageService(const FlutterSecureStorage());
  String? _userName;
  String? _photoPath;
  bool _isLoading = true;
  ConnectivityStatus _connectivityStatus = ConnectivityStatus.online;

  @override
  void initState() {
    super.initState();
    _loadUserData();
    _listenToConnectivity();
  }

  void _listenToConnectivity() {
    context.read<ConnectivityService>().stream.listen((status) {
      if (mounted) {
        setState(() {
          _connectivityStatus = status;
        });
      }
    });
  }

  Future<void> _loadUserData() async {
    // Simuler un chargement pour montrer le Skeleton
    await Future.delayed(const Duration(seconds: 2));
    
    final authState = context.read<AuthBloc>().state;
    if (authState is AuthSuccess && authState.phone != null) {
      final name = await _secureStorage.getUserName(authState.phone!);
      final photo = await _secureStorage.getUserPhoto(authState.phone!);
      if (mounted) {
        setState(() {
          _userName = name;
          _photoPath = photo;
          _isLoading = false;
        });
      }
    }
  }

  void _showLogoutDialog(BuildContext context) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Déconnexion'),
        content: const Text('Voulez-vous vraiment vous déconnecter de votre espace sécurisé ?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('ANNULER'),
          ),
          TextButton(
            onPressed: () {
              Navigator.pop(context);
              context.read<AuthBloc>().add(LogoutRequested());
            },
            child: const Text('DÉCONNEXION', style: TextStyle(color: AppTheme.errorRed)),
          ),
        ],
      ),
    );
  }

  void _showComingSoon(BuildContext context, String feature) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text('$feature bientôt disponible'),
        behavior: SnackBarBehavior.floating,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(15)),
      ),
    );
  }

  void _showAllAccounts(BuildContext context, List<Map<String, dynamic>> accounts, NumberFormat currencyFormat) {
    showModalBottomSheet(
      context: context,
      backgroundColor: Colors.transparent,
      isScrollControlled: true,
      builder: (context) => Container(
        height: MediaQuery.of(context).size.height * 0.8,
        padding: const EdgeInsets.all(32),
        decoration: const BoxDecoration(
          color: AppTheme.bgLight,
          borderRadius: BorderRadius.only(
            topLeft: Radius.circular(40),
            topRight: Radius.circular(40),
          ),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Center(
              child: Container(width: 40, height: 4, decoration: BoxDecoration(color: Colors.grey[300], borderRadius: BorderRadius.circular(2))),
            ),
            const SizedBox(height: 32),
            const Text(
              'Tous mes comptes',
              style: TextStyle(fontSize: 24, fontWeight: FontWeight.w900, color: AppTheme.primaryBlue),
            ),
            const SizedBox(height: 24),
            Expanded(
              child: ListView.builder(
                itemCount: accounts.length,
                itemBuilder: (context, index) {
                  final account = accounts[index];
                  final Color accountColor = account['accountTypeColor'] != null 
                      ? Color(int.parse(account['accountTypeColor'].replaceFirst('#', '0xFF')))
                      : AppTheme.accentBlue;

                  return Container(
                    margin: const EdgeInsets.only(bottom: 16),
                    padding: const EdgeInsets.all(20),
                    decoration: BoxDecoration(
                      color: Colors.white,
                      borderRadius: BorderRadius.circular(24),
                      boxShadow: AppTheme.softShadow,
                    ),
                    child: ListTile(
                      onTap: () {
                        Navigator.pop(context);
                        context.push('${AppRouter.transactions}/${account['id']}');
                      },
                      contentPadding: EdgeInsets.zero,
                      leading: Container(
                        padding: const EdgeInsets.all(12),
                        decoration: BoxDecoration(color: accountColor.withOpacity(0.1), borderRadius: BorderRadius.circular(16)),
                        child: Icon(Icons.account_balance_rounded, color: accountColor, size: 20),
                      ),
                      title: Text(account['libelle'], style: const TextStyle(fontWeight: FontWeight.w800, color: AppTheme.primaryBlue)),
                      subtitle: Text(account['numeroCompte']),
                      trailing: Text(
                        currencyFormat.format(account['availableBalance']),
                        style: TextStyle(fontWeight: FontWeight.w900, color: accountColor, fontSize: 16),
                      ),
                    ),
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
  Widget build(BuildContext context) {
    final currencyFormat = NumberFormat.currency(locale: 'fr_FR', symbol: 'FCFA', decimalDigits: 0);
    
    // Récupérer les infos de l'utilisateur connecté
    final authState = context.read<AuthBloc>().state;
    String currentPhone = '771234567'; // Fallback
    if (authState is AuthSuccess && authState.phone != null) {
      currentPhone = authState.phone!;
    }

    // Récupérer les comptes dynamiquement
    final List<Map<String, dynamic>> userAccounts = MockData.getAccountsForPhone(currentPhone);
    
    // Calculer les soldes réels
    double soldeDisponible = 0;
    double soldeBloque = 0;
    for (var acc in userAccounts) {
      soldeDisponible += acc['availableBalance'];
      soldeBloque += acc['blockedBalance'];
    }
    final double soldeTotal = soldeDisponible + soldeBloque;

    return Scaffold(
      backgroundColor: Theme.of(context).scaffoldBackgroundColor,
      body: Column(
        children: [
          if (_connectivityStatus == ConnectivityStatus.offline)
            Container(
              width: double.infinity,
              color: AppTheme.errorRed,
              padding: const EdgeInsets.symmetric(vertical: 6),
              child: const Text(
                'MODE HORS-LIGNE - DONNÉES LOCALES',
                textAlign: TextAlign.center,
                style: TextStyle(color: Colors.white, fontSize: 10, fontWeight: FontWeight.w900, letterSpacing: 1),
              ),
            ),
          Expanded(
            child: _isLoading ? _buildSkeleton() : CustomScrollView(
              physics: const BouncingScrollPhysics(),
              slivers: [
                // Header Ultra-Premium
                SliverToBoxAdapter(
                  child: Container(
                    padding: const EdgeInsets.fromLTRB(32, 70, 32, 40),
                    decoration: BoxDecoration(
                      color: AppTheme.primaryBlue,
                      borderRadius: const BorderRadius.only(
                        bottomLeft: Radius.circular(48),
                        bottomRight: Radius.circular(48),
                      ),
                      boxShadow: [
                        BoxShadow(
                          color: AppTheme.primaryBlue.withOpacity(0.2),
                          blurRadius: 30,
                          offset: const Offset(0, 15),
                        ),
                      ],
                    ),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text(
                                  'Bienvenue,',
                                  style: TextStyle(color: Colors.white.withOpacity(0.5), fontSize: 16, fontWeight: FontWeight.w500),
                                ),
                                Text(
                                  '${_userName ?? 'Utilisateur'} 👋',
                                  style: const TextStyle(
                                    color: Colors.white,
                                    fontSize: 28,
                                    fontWeight: FontWeight.w900,
                                    letterSpacing: -0.5,
                                  ),
                                ),
                              ],
                            ),
                            GestureDetector(
                              onTap: () => context.go('/profile'),
                              child: Container(
                                padding: const EdgeInsets.all(3),
                                decoration: BoxDecoration(
                                  shape: BoxShape.circle,
                                  border: Border.all(color: AppTheme.accentBlue, width: 2),
                                ),
                                child: CircleAvatar(
                                  radius: 28,
                                  backgroundColor: AppTheme.surfaceDark,
                                  backgroundImage: _photoPath != null ? FileImage(File(_photoPath!)) : null,
                                  child: _photoPath == null 
                                    ? const Icon(Icons.person_outline_rounded, color: Colors.white, size: 30)
                                    : null,
                                ),
                              ),
                            ),
                          ],
                        ),
                        const SizedBox(height: 48),
                        Text(
                          'SOLDE TOTAL',
                          style: TextStyle(
                            color: Colors.white.withOpacity(0.4),
                            fontSize: 12,
                            fontWeight: FontWeight.w800,
                            letterSpacing: 2,
                          ),
                        ),
                        const SizedBox(height: 12),
                        Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          crossAxisAlignment: CrossAxisAlignment.center,
                          children: [
                            Text(
                              currencyFormat.format(soldeTotal),
                              style: const TextStyle(
                                color: Colors.white,
                                fontSize: 40,
                                fontWeight: FontWeight.w900,
                                letterSpacing: -1,
                              ),
                            ),
                            Container(
                              padding: const EdgeInsets.all(12),
                              decoration: BoxDecoration(
                                color: Colors.white.withOpacity(0.1),
                                borderRadius: BorderRadius.circular(16),
                              ),
                              child: const Icon(Icons.show_chart_rounded, color: AppTheme.successGreen, size: 24),
                            ),
                          ],
                        ),
                        const SizedBox(height: 24),
                        Row(
                          children: [
                            Icon(Icons.history_toggle_off_rounded, color: Colors.white.withOpacity(0.3), size: 14),
                            const SizedBox(width: 6),
                            Text(
                              'Actualisé à ${DateFormat('HH:mm', 'fr_FR').format(DateTime.now())}',
                              style: TextStyle(color: Colors.white.withOpacity(0.3), fontSize: 11, fontWeight: FontWeight.w600),
                            ),
                          ],
                        ),
                      ],
                    ),
                  ),
                ),

                SliverToBoxAdapter(
                  child: Padding(
                    padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 32),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        // Quick Actions Grid
                        const Text(
                          'Actions Rapides',
                          style: TextStyle(fontSize: 20, fontWeight: FontWeight.w900, color: AppTheme.primaryBlue),
                        ),
                        const SizedBox(height: 20),
                        GridView.count(
                          shrinkWrap: true,
                          physics: const NeverScrollableScrollPhysics(),
                          crossAxisCount: 4,
                          mainAxisSpacing: 16,
                          crossAxisSpacing: 12,
                          childAspectRatio: 0.75,
                          children: [
                            _buildPremiumQuickAction(
                              Icons.send_rounded, 
                              'Envoi', 
                              const Color(0xFFE0F2FE), 
                              AppTheme.accentBlue,
                              onTap: () => _showComingSoon(context, 'Service d\'Envoi'),
                            ),
                            _buildPremiumQuickAction(
                              Icons.qr_code_scanner_rounded, 
                              'Scan', 
                              const Color(0xFFFEF3C7), 
                              AppTheme.warningOrange,
                              onTap: () => _showComingSoon(context, 'Scanner QR'),
                            ),
                            _buildPremiumQuickAction(
                              Icons.account_balance_wallet_rounded, 
                              'Pay', 
                              const Color(0xFFF3E8FF), 
                              Colors.purple,
                              onTap: () => _showComingSoon(context, 'Paiement'),
                            ),
                            _buildPremiumQuickAction(
                              Icons.add_box_rounded, 
                              'Plus', 
                              const Color(0xFFDCFCE7), 
                              AppTheme.successGreen,
                              onTap: () => _showComingSoon(context, 'Plus d\'options'),
                            ),
                          ],
                        ),
                        
                        const SizedBox(height: 40),
                        
                        Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            const Text(
                              'Mes Comptes',
                              style: TextStyle(fontSize: 20, fontWeight: FontWeight.w900, color: AppTheme.primaryBlue),
                            ),
                            TextButton(
                              onPressed: () => _showAllAccounts(context, userAccounts, currencyFormat),
                              child: const Text('VOIR TOUT', style: TextStyle(fontWeight: FontWeight.w800, fontSize: 11, letterSpacing: 1)),
                            ),
                          ],
                        ),
                        const SizedBox(height: 16),
                        
                        ListView.builder(
                          shrinkWrap: true,
                          padding: EdgeInsets.zero,
                          physics: const NeverScrollableScrollPhysics(),
                          itemCount: userAccounts.length > 3 ? 3 : userAccounts.length,
                          itemBuilder: (context, index) {
                            final account = userAccounts[index];
                            final Color accountColor = account['accountTypeColor'] != null 
                                ? Color(int.parse(account['accountTypeColor'].replaceFirst('#', '0xFF')))
                                : AppTheme.accentBlue;

                            return Container(
                              margin: const EdgeInsets.only(bottom: 20),
                              padding: const EdgeInsets.all(24),
                              decoration: BoxDecoration(
                                color: Colors.white,
                                borderRadius: BorderRadius.circular(32),
                                boxShadow: AppTheme.softShadow,
                                border: Border.all(color: accountColor.withOpacity(0.05), width: 1),
                              ),
                              child: InkWell(
                                onTap: () => context.push('${AppRouter.transactions}/${account['id']}'),
                                child: Row(
                                  children: [
                                    Container(
                                      padding: const EdgeInsets.all(16),
                                      decoration: BoxDecoration(
                                        color: accountColor.withOpacity(0.1),
                                        borderRadius: BorderRadius.circular(20),
                                      ),
                                      child: Icon(Icons.account_balance_rounded, color: accountColor, size: 24),
                                    ),
                                    const SizedBox(width: 20),
                                    Expanded(
                                      child: Column(
                                        crossAxisAlignment: CrossAxisAlignment.start,
                                        children: [
                                          Text(
                                            account['libelle'],
                                            style: const TextStyle(fontWeight: FontWeight.w800, fontSize: 17, color: AppTheme.primaryBlue),
                                          ),
                                          const SizedBox(height: 4),
                                          Text(
                                            account['numeroCompte'],
                                            style: TextStyle(color: AppTheme.primaryBlue.withOpacity(0.4), fontSize: 13, fontWeight: FontWeight.w600),
                                          ),
                                        ],
                                      ),
                                    ),
                                    Column(
                                      crossAxisAlignment: CrossAxisAlignment.end,
                                      children: [
                                        Text(
                                          currencyFormat.format(account['availableBalance']),
                                          style: TextStyle(
                                            fontWeight: FontWeight.w900,
                                            color: accountColor,
                                            fontSize: 18,
                                            letterSpacing: -0.5,
                                          ),
                                        ),
                                        const SizedBox(height: 4),
                                        Container(
                                          padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                                          decoration: BoxDecoration(
                                            color: (account['accountType'] == 'GARANTIE' ? AppTheme.errorRed : AppTheme.successGreen).withOpacity(0.1),
                                            borderRadius: BorderRadius.circular(8),
                                          ),
                                          child: Text(
                                            account['accountType'] == 'GARANTIE' ? 'BLOQUÉ' : 'ACTIF', 
                                            style: TextStyle(
                                              fontSize: 9, 
                                              color: account['accountType'] == 'GARANTIE' ? AppTheme.errorRed : AppTheme.successGreen, 
                                              fontWeight: FontWeight.w900,
                                              letterSpacing: 0.5,
                                            )
                                          ),
                                        ),
                                      ],
                                    ),
                                  ],
                                ),
                              ),
                            );
                          },
                        ),
                        
                        const SizedBox(height: 48),
                        
                        // Analysis Section
                        Container(
                          padding: const EdgeInsets.all(32),
                          decoration: BoxDecoration(
                            color: AppTheme.primaryBlue,
                            borderRadius: BorderRadius.circular(40),
                            image: const DecorationImage(
                              image: NetworkImage('https://www.transparenttextures.com/patterns/carbon-fibre.png'),
                              opacity: 0.05,
                            ),
                          ),
                          child: Column(
                            children: [
                              Row(
                                children: [
                                  const CircleAvatar(
                                    backgroundColor: AppTheme.accentBlue,
                                    radius: 20,
                                    child: Icon(Icons.pie_chart_rounded, color: Colors.white, size: 20),
                                  ),
                                  const SizedBox(width: 16),
                                  const Text(
                                    'Analyse des actifs',
                                    style: TextStyle(color: Colors.white, fontWeight: FontWeight.w800, fontSize: 18),
                                  ),
                                  const Spacer(),
                                  Icon(Icons.arrow_forward_ios_rounded, color: Colors.white.withOpacity(0.3), size: 16),
                                ],
                              ),
                              const SizedBox(height: 40),
                              SizedBox(
                                height: 200,
                                child: PieChart(
                                  PieChartData(
                                    sectionsSpace: 8,
                                    centerSpaceRadius: 60,
                                    sections: [
                                      PieChartSectionData(
                                        value: soldeDisponible,
                                        color: AppTheme.accentBlue,
                                        title: '',
                                        radius: 25,
                                      ),
                                      PieChartSectionData(
                                        value: soldeBloque,
                                        color: AppTheme.successGreen,
                                        title: '',
                                        radius: 18,
                                      ),
                                    ],
                                  ),
                                ),
                              ),
                              const SizedBox(height: 32),
                              Row(
                                mainAxisAlignment: MainAxisAlignment.center,
                                children: [
                                  _buildLegendItem(AppTheme.accentBlue, 'Disponible'),
                                  const SizedBox(width: 24),
                                  _buildLegendItem(AppTheme.successGreen, 'Bloqué'),
                                ],
                              ),
                            ],
                          ),
                        ),
                        const SizedBox(height: 60),
                      ],
                    ),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildPremiumQuickAction(IconData icon, String label, Color bgColor, Color iconColor, {VoidCallback? onTap}) {
    return GestureDetector(
      onTap: onTap,
      child: Column(
        children: [
          AspectRatio(
            aspectRatio: 1,
            child: Container(
              width: double.infinity,
              decoration: BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.circular(24),
                boxShadow: AppTheme.softShadow,
              ),
              child: Icon(icon, color: AppTheme.primaryBlue, size: 28),
            ),
          ),
          const SizedBox(height: 8),
          Text(
            label,
            maxLines: 1,
            overflow: TextOverflow.ellipsis,
            style: const TextStyle(
              fontSize: 12,
              fontWeight: FontWeight.w800,
              color: AppTheme.primaryBlue,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildLegendItem(Color color, String label) {
    return Row(
      children: [
        Container(width: 12, height: 12, decoration: BoxDecoration(color: color, shape: BoxShape.circle)),
        const SizedBox(width: 8),
        Text(label, style: const TextStyle(color: Colors.white70, fontSize: 13, fontWeight: FontWeight.w600)),
      ],
    );
  }

  Widget _buildSkeleton() {
    return Shimmer.fromColors(
      baseColor: Colors.grey[200]!,
      highlightColor: Colors.grey[50]!,
      child: SingleChildScrollView(
        padding: const EdgeInsets.all(32),
        child: Column(
          children: [
            Container(height: 240, width: double.infinity, decoration: BoxDecoration(color: Colors.white, borderRadius: BorderRadius.circular(48))),
            const SizedBox(height: 48),
            GridView.count(
              shrinkWrap: true,
              crossAxisCount: 4,
              mainAxisSpacing: 20,
              crossAxisSpacing: 16,
              children: List.generate(4, (index) => Container(decoration: BoxDecoration(color: Colors.white, borderRadius: BorderRadius.circular(24))))),
            const SizedBox(height: 48),
            ...List.generate(2, (index) => Container(margin: const EdgeInsets.only(bottom: 20), height: 110, width: double.infinity, decoration: BoxDecoration(color: Colors.white, borderRadius: BorderRadius.circular(32)))),
          ],
        ),
      ),
    );
  }
}
