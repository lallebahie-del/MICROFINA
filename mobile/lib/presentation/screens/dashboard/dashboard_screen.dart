import 'dart:io';
import 'package:flutter/material.dart';
import 'package:fl_chart/fl_chart.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';
import 'package:intl/intl.dart';
import '../../../core/theme/app_theme.dart';
import '../../../core/router/app_router.dart';
import '../../../core/di/service_locator.dart';
import '../../../data/datasources/mock/mock_data.dart';
import '../../../domain/repositories/comptes_repository.dart';
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
  final ComptesRepository _comptesRepo = sl<ComptesRepository>();

  String? _userName;
  String? _photoPath;
  bool _isLoading = true;
  bool _isBalanceVisible = true;
  ConnectivityStatus _connectivityStatus = ConnectivityStatus.online;
  int? _touchedIndex;

  /// Comptes chargés depuis le backend (mappés en `Map<String,dynamic>` pour
  /// rester compatible avec l'UI existante qui consomme `account['xxx']`).
  List<Map<String, dynamic>> _userAccounts = const [];
  String? _comptesError;

  @override
  void initState() {
    super.initState();
    _listenToConnectivity();
    _loadUserData();
    _loadComptes();
  }

  Future<void> _loadComptes() async {
    try {
      final list = await _comptesRepo.getMyComptes();
      if (!mounted) return;
      setState(() {
        _userAccounts = list
            .map((c) => {
                  'id':                c.id,
                  'libelle':           c.libelle,
                  'numeroCompte':      c.numeroCompte,
                  'availableBalance':  c.availableBalance,
                  'blockedBalance':    c.blockedBalance,
                  'accountType':       c.accountType,
                  'devise':            c.devise,
                  'accountTypeColor':  null,
                })
            .toList();
        _comptesError = null;
      });
    } catch (e) {
      if (!mounted) return;
      // Fallback : on conserve les mocks pour ne pas bloquer l'UI en cas d'erreur réseau.
      final authState = context.read<AuthBloc>().state;
      String currentPhone = '771234567';
      if (authState is AuthSuccess && authState.phone != null) {
        currentPhone = authState.phone!;
      }
      setState(() {
        _userAccounts = MockData.getAccountsForPhone(currentPhone);
        _comptesError = e.toString().replaceFirst('Exception: ', '');
      });
    }
  }

  void _listenToConnectivity() {
    if (!mounted) return;
    final connectivityService = context.read<ConnectivityService>();
    connectivityService.stream.listen((status) {
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
    
    if (!mounted) return;
    
    final authBloc = context.read<AuthBloc>();
    final authState = authBloc.state;
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
              final authBloc = context.read<AuthBloc>();
              Navigator.pop(context);
              authBloc.add(LogoutRequested());
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
                        final router = GoRouter.of(context);
                        Navigator.pop(context);
                        router.push('${AppRouter.transactions}/${account['id']}');
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
                        _isBalanceVisible ? currencyFormat.format(account['availableBalance']) : '••••••',
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
    
    // Comptes chargés depuis le backend via ComptesRepository (cf. _loadComptes()).
    final List<Map<String, dynamic>> userAccounts = _userAccounts;

    // Calculer les soldes réels
    double soldeDisponible = 0;
    double soldeBloque = 0;
    for (var acc in userAccounts) {
      soldeDisponible += (acc['availableBalance'] as num?)?.toDouble() ?? 0;
      soldeBloque     += (acc['blockedBalance']    as num?)?.toDouble() ?? 0;
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
                    padding: const EdgeInsets.fromLTRB(24, 60, 24, 40),
                    decoration: const BoxDecoration(
                      color: AppTheme.primaryBlue,
                      borderRadius: BorderRadius.only(
                        bottomLeft: Radius.circular(40),
                        bottomRight: Radius.circular(40),
                      ),
                    ),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            Row(
                              children: [
                                GestureDetector(
                                  onTap: () => context.go('/profile'),
                                  child: Container(
                                    padding: const EdgeInsets.all(2),
                                    decoration: BoxDecoration(
                                      shape: BoxShape.circle,
                                      border: Border.all(color: Colors.white.withOpacity(0.2), width: 1),
                                    ),
                                    child: CircleAvatar(
                                      radius: 22,
                                      backgroundColor: AppTheme.surfaceDark,
                                      backgroundImage: _photoPath != null ? FileImage(File(_photoPath!)) : null,
                                      child: _photoPath == null 
                                        ? const Icon(Icons.person_rounded, color: Colors.white, size: 22)
                                        : null,
                                    ),
                                  ),
                                ),
                                const SizedBox(width: 12),
                                Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    Text(
                                      'Bonjour,',
                                      style: TextStyle(color: Colors.white.withOpacity(0.5), fontSize: 13, fontWeight: FontWeight.w500),
                                    ),
                                    Text(
                                      _userName ?? 'Utilisateur',
                                      style: const TextStyle(
                                        color: Colors.white,
                                        fontSize: 18,
                                        fontWeight: FontWeight.w700,
                                      ),
                                    ),
                                  ],
                                ),
                              ],
                            ),
                            _buildHeaderIconButton(
                              icon: Icons.notifications_rounded,
                              hasBadge: true,
                              onTap: () => context.push(AppRouter.notifications),
                            ),
                          ],
                        ),
                        const SizedBox(height: 40),
                        Text(
                          'SOLDE TOTAL DISPONIBLE',
                          style: TextStyle(
                            color: Colors.white.withOpacity(0.4),
                            fontSize: 11,
                            fontWeight: FontWeight.w600,
                            letterSpacing: 1.2,
                          ),
                        ),
                        const SizedBox(height: 8),
                        Row(
                          crossAxisAlignment: CrossAxisAlignment.center,
                          children: [
                            Text(
                              _isBalanceVisible ? currencyFormat.format(soldeTotal) : '•••••••• FCFA',
                              style: const TextStyle(
                                color: Colors.white,
                                fontSize: 32,
                                fontWeight: FontWeight.w800,
                                letterSpacing: -0.5,
                              ),
                            ),
                            const SizedBox(width: 12),
                            IconButton(
                              onPressed: () => setState(() => _isBalanceVisible = !_isBalanceVisible),
                              icon: Icon(
                                _isBalanceVisible ? Icons.visibility_off_rounded : Icons.visibility_rounded,
                                color: Colors.white.withOpacity(0.4),
                                size: 18,
                              ),
                              constraints: const BoxConstraints(),
                              padding: EdgeInsets.zero,
                            ),
                          ],
                        ),
                        const SizedBox(height: 20),
                        Row(
                          children: [
                            Container(
                              padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                              decoration: BoxDecoration(
                                color: Colors.white.withOpacity(0.1),
                                borderRadius: BorderRadius.circular(8),
                              ),
                              child: Row(
                                children: [
                                  const Icon(Icons.check_circle_rounded, color: AppTheme.successGreen, size: 12),
                                  const SizedBox(width: 6),
                                  Text(
                                    'Compte vérifié',
                                    style: TextStyle(color: Colors.white.withOpacity(0.7), fontSize: 10, fontWeight: FontWeight.w600),
                                  ),
                                ],
                              ),
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
                        Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            const Text(
                              'Actions Rapides',
                              style: TextStyle(fontSize: 20, fontWeight: FontWeight.w900, color: AppTheme.primaryBlue),
                            ),
                            Container(
                              padding: const EdgeInsets.all(8),
                              decoration: BoxDecoration(
                                color: AppTheme.primaryBlue.withOpacity(0.05), 
                                borderRadius: BorderRadius.circular(10),
                              ),
                              child: const Icon(Icons.auto_awesome_rounded, color: AppTheme.primaryBlue, size: 14),
                            ),
                          ],
                        ),
                        const SizedBox(height: 24),
                        Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            _buildPremiumQuickAction(
                              Icons.send_rounded, 
                              'Envoi', 
                              onTap: () async {
                                await context.push(AppRouter.transfer);
                                if (mounted) {
                                  setState(() {});
                                }
                              },
                            ),
                            _buildPremiumQuickAction(
                              Icons.qr_code_scanner_rounded, 
                              'Scan', 
                              onTap: () => context.push(AppRouter.scan),
                            ),
                            _buildPremiumQuickAction(
                              Icons.account_balance_wallet_rounded, 
                              'Pay', 
                              onTap: () async {
                                await context.push(AppRouter.pay);
                                if (mounted) {
                                  setState(() {});
                                }
                              },
                            ),
                            _buildPremiumQuickAction(
                              Icons.more_horiz_rounded, 
                              'Plus', 
                              onTap: () => _showComingSoon(context, 'Plus d\'options'),
                            ),
                          ],
                        ),
                        
                        const SizedBox(height: 40),
                        
                        // Analyse Section (Graphique Donut)
                        Container(
                          padding: const EdgeInsets.all(24),
                          decoration: BoxDecoration(
                            color: Colors.white,
                            borderRadius: BorderRadius.circular(32),
                            boxShadow: AppTheme.softShadow,
                          ),
                          child: Column(
                            children: [
                              Row(
                                children: [
                                  Container(
                                    padding: const EdgeInsets.all(8),
                                    decoration: BoxDecoration(
                                      color: AppTheme.primaryBlue.withOpacity(0.1),
                                      borderRadius: BorderRadius.circular(12),
                                    ),
                                    child: const Icon(Icons.pie_chart_rounded, color: AppTheme.primaryBlue, size: 20),
                                  ),
                                  const SizedBox(width: 12),
                                  const Text(
                                    'Répartition des avoirs',
                                    style: TextStyle(color: AppTheme.primaryBlue, fontWeight: FontWeight.w800, fontSize: 16),
                                  ),
                                ],
                              ),
                              const SizedBox(height: 24),
                              SizedBox(
                                height: 180,
                                child: Stack(
                                  alignment: Alignment.center,
                                  children: [
                                    PieChart(
                                      PieChartData(
                                        pieTouchData: PieTouchData(
                                          touchCallback: (FlTouchEvent event, pieTouchResponse) {
                                            setState(() {
                                              if (!event.isInterestedForInteractions ||
                                                  pieTouchResponse == null ||
                                                  pieTouchResponse.touchedSection == null) {
                                                _touchedIndex = -1;
                                                return;
                                              }
                                              _touchedIndex = pieTouchResponse.touchedSection!.touchedSectionIndex;
                                            });
                                          },
                                        ),
                                        sectionsSpace: 4,
                                        centerSpaceRadius: 55,
                                        sections: [
                                          PieChartSectionData(
                                            value: soldeDisponible,
                                            color: AppTheme.accentBlue,
                                            title: _touchedIndex == 0 
                                                ? currencyFormat.format(soldeDisponible) 
                                                : soldeTotal > 0 
                                                    ? '${((soldeDisponible / soldeTotal) * 100).toStringAsFixed(0)}%'
                                                    : '0%',
                                            radius: _touchedIndex == 0 ? 60 : 50,
                                            titleStyle: TextStyle(
                                              fontSize: _touchedIndex == 0 ? 14 : 12, 
                                              fontWeight: FontWeight.bold, 
                                              color: Colors.white,
                                            ),
                                          ),
                                          PieChartSectionData(
                                            value: soldeBloque,
                                            color: AppTheme.successGreen,
                                            title: _touchedIndex == 1 
                                                ? currencyFormat.format(soldeBloque) 
                                                : soldeTotal > 0 
                                                    ? '${((soldeBloque / soldeTotal) * 100).toStringAsFixed(0)}%'
                                                    : '0%',
                                            radius: _touchedIndex == 1 ? 50 : 40,
                                            titleStyle: TextStyle(
                                              fontSize: _touchedIndex == 1 ? 12 : 10, 
                                              fontWeight: FontWeight.bold, 
                                              color: Colors.white,
                                            ),
                                          ),
                                        ],
                                      ),
                                    ),
                                    Column(
                                      mainAxisSize: MainAxisSize.min,
                                      children: [
                                        Text(
                                          'TOTAL',
                                          style: TextStyle(
                                            color: AppTheme.primaryBlue.withOpacity(0.5),
                                            fontSize: 10,
                                            fontWeight: FontWeight.bold,
                                            letterSpacing: 1,
                                          ),
                                        ),
                                        Text(
                                          _isBalanceVisible ? currencyFormat.format(soldeTotal) : '••••••',
                                          style: const TextStyle(
                                            color: AppTheme.primaryBlue,
                                            fontSize: 14,
                                            fontWeight: FontWeight.w900,
                                          ),
                                        ),
                                      ],
                                    ),
                                  ],
                                ),
                              ),
                              const SizedBox(height: 20),
                              Row(
                                mainAxisAlignment: MainAxisAlignment.center,
                                children: [
                                  _buildLegendItem(AppTheme.accentBlue, 'Disponible', isDark: false),
                                  const SizedBox(width: 24),
                                  _buildLegendItem(AppTheme.successGreen, 'Bloqué', isDark: false),
                                ],
                              ),
                            ],
                          ),
                        ),

                        const SizedBox(height: 40),
                        
                        Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            const Text(
                              'Mes Comptes',
                              style: TextStyle(fontSize: 20, fontWeight: FontWeight.w900, color: AppTheme.primaryBlue),
                            ),
                            Row(
                              children: [
                                TextButton(
                                  onPressed: () => _showAllAccounts(context, userAccounts, currencyFormat),
                                  child: const Text('VOIR TOUT', style: TextStyle(fontWeight: FontWeight.w800, fontSize: 11, letterSpacing: 1)),
                                ),
                                const SizedBox(width: 4),
                                Container(
                                  padding: const EdgeInsets.all(6),
                                  decoration: BoxDecoration(
                                    color: AppTheme.primaryBlue.withOpacity(0.05),
                                    shape: BoxShape.circle,
                                  ),
                                  child: const Icon(Icons.account_balance_rounded, color: AppTheme.primaryBlue, size: 12),
                                ),
                              ],
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
                              margin: const EdgeInsets.only(bottom: 12),
                              decoration: BoxDecoration(
                                color: Colors.white,
                                borderRadius: BorderRadius.circular(16),
                                border: Border.all(color: Colors.grey.withOpacity(0.05)),
                                boxShadow: [
                                  BoxShadow(
                                    color: Colors.black.withOpacity(0.02),
                                    blurRadius: 10,
                                    offset: const Offset(0, 4),
                                  ),
                                ],
                              ),
                              child: InkWell(
                                onTap: () => context.push('${AppRouter.transactions}/${account['id']}'),
                                borderRadius: BorderRadius.circular(16),
                                child: Padding(
                                  padding: const EdgeInsets.all(16),
                                  child: Row(
                                    children: [
                                      Container(
                                        width: 4,
                                        height: 40,
                                        decoration: BoxDecoration(
                                          color: accountColor,
                                          borderRadius: BorderRadius.circular(2),
                                        ),
                                      ),
                                      const SizedBox(width: 16),
                                      Expanded(
                                        child: Column(
                                          crossAxisAlignment: CrossAxisAlignment.start,
                                          children: [
                                            Text(
                                              account['libelle'],
                                              style: const TextStyle(fontWeight: FontWeight.w600, fontSize: 14, color: AppTheme.primaryBlue),
                                            ),
                                            const SizedBox(height: 2),
                                            Text(
                                              account['numeroCompte'],
                                              style: TextStyle(color: Colors.grey[500], fontSize: 12, fontWeight: FontWeight.w500),
                                            ),
                                          ],
                                        ),
                                      ),
                                      Column(
                                        crossAxisAlignment: CrossAxisAlignment.end,
                                        children: [
                                          Text(
                                            _isBalanceVisible 
                                              ? currencyFormat.format(account['availableBalance'])
                                              : '••••••',
                                            style: TextStyle(
                                              fontWeight: FontWeight.w700,
                                              color: AppTheme.primaryBlue,
                                              fontSize: 15,
                                            ),
                                          ),
                                          Text(
                                            'Solde disponible',
                                            style: TextStyle(fontSize: 10, color: Colors.grey[400], fontWeight: FontWeight.w500),
                                          ),
                                        ],
                                      ),
                                      const SizedBox(width: 8),
                                      Icon(Icons.chevron_right_rounded, color: Colors.grey[300], size: 20),
                                    ],
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
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildPremiumQuickAction(IconData icon, String label, {VoidCallback? onTap}) {
    return GestureDetector(
      onTap: onTap,
      child: Column(
        children: [
          Container(
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(24),
              border: Border.all(color: AppTheme.primaryBlue.withOpacity(0.05)),
              boxShadow: [
                BoxShadow(
                  color: AppTheme.primaryBlue.withOpacity(0.03),
                  blurRadius: 15,
                  offset: const Offset(0, 5),
                ),
              ],
            ),
            child: Icon(icon, color: AppTheme.primaryBlue, size: 26),
          ),
          const SizedBox(height: 10),
          Text(
            label,
            style: const TextStyle(
              fontSize: 12,
              fontWeight: FontWeight.w700,
              color: AppTheme.primaryBlue,
              letterSpacing: 0.1,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildHeaderIconButton({required IconData icon, required VoidCallback onTap, bool hasBadge = false}) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.all(12),
        decoration: BoxDecoration(
          color: Colors.white.withOpacity(0.08),
          shape: BoxShape.circle,
        ),
        child: Stack(
          children: [
            Icon(icon, color: Colors.white, size: 24),
            if (hasBadge)
              Positioned(
                right: 0,
                top: 0,
                child: Container(
                  width: 10,
                  height: 10,
                  decoration: BoxDecoration(
                    color: AppTheme.errorRed,
                    shape: BoxShape.circle,
                    border: Border.all(color: AppTheme.primaryBlue, width: 2),
                  ),
                ),
              ),
          ],
        ),
      ),
    );
  }

  Widget _buildLegendItem(Color color, String label, {bool isDark = true}) {
    return Row(
      children: [
        Container(width: 10, height: 10, decoration: BoxDecoration(color: color, shape: BoxShape.circle)),
        const SizedBox(width: 8),
        Text(label, style: TextStyle(color: isDark ? Colors.white70 : AppTheme.primaryBlue.withOpacity(0.6), fontSize: 12, fontWeight: FontWeight.w600)),
      ],
    );
  }

  Widget _buildSkeleton() {
    return Shimmer.fromColors(
      baseColor: Colors.grey[200]!,
      highlightColor: Colors.grey[50]!,
      child: SingleChildScrollView(
        padding: const EdgeInsets.all(24),
        child: Column(
          children: [
            Container(height: 200, width: double.infinity, decoration: BoxDecoration(color: Colors.white, borderRadius: BorderRadius.circular(40))),
            const SizedBox(height: 32),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: List.generate(4, (index) => Container(height: 80, width: 70, decoration: BoxDecoration(color: Colors.white, borderRadius: BorderRadius.circular(16)))),
            ),
            const SizedBox(height: 32),
            ...List.generate(3, (index) => Container(margin: const EdgeInsets.only(bottom: 12), height: 80, width: double.infinity, decoration: BoxDecoration(color: Colors.white, borderRadius: BorderRadius.circular(16)))),
          ],
        ),
      ),
    );
  }
}
