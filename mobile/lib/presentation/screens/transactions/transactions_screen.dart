import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:intl/intl.dart';
import '../../../core/theme/app_colors.dart';
import '../../../data/models/extra_models.dart';
import '../../../data/datasources/mock/mock_data.dart';
import '../../blocs/transaction/transaction_bloc.dart';
import '../../blocs/account/account_bloc.dart';
import '../../widgets/transaction_shimmer.dart';
import '../../../core/utils/pdf_generator_service.dart';

class TransactionsScreen extends StatefulWidget {
  /// Identifiant du compte, ou [MockData.transactionScopeAllAccounts] pour l’historique agrégé.
  final String accountId;

  const TransactionsScreen({super.key, required this.accountId});

  @override
  State<TransactionsScreen> createState() => _TransactionsScreenState();
}

class _TransactionsScreenState extends State<TransactionsScreen> {
  final ScrollController _scrollController = ScrollController();
  final currencyFormat = NumberFormat.currency(
    locale: 'fr_FR',
    symbol: 'FCFA',
    decimalDigits: 0,
  );
  DateTimeRange? _selectedDateRange;

  bool get _allAccounts =>
      widget.accountId == MockData.transactionScopeAllAccounts;

  @override
  void initState() {
    super.initState();
    _scrollController.addListener(_onScroll);
  }

  @override
  void dispose() {
    _scrollController.removeListener(_onScroll);
    _scrollController.dispose();
    super.dispose();
  }

  void _onScroll() {
    if (_scrollController.position.pixels >=
        _scrollController.position.maxScrollExtent * 0.9) {
      context.read<TransactionBloc>().add(
        LoadMoreTransactions(widget.accountId),
      );
    }
  }

  void _onSearchChanged(String query) {
    // Ici on pourrait ajouter un événement de filtrage par texte au BLoC
    // Pour l'instant, on simule l'interface de recherche
  }

  Future<void> _selectDateRange(BuildContext context) async {
    final DateTimeRange? picked = await showDateRangePicker(
      context: context,
      firstDate: DateTime(2020),
      lastDate: DateTime.now(),
      initialDateRange: _selectedDateRange,
      builder: (context, child) {
        return Theme(
          data: Theme.of(context).copyWith(
            colorScheme: const ColorScheme.light(
              primary: AppColors.primary,
              onPrimary: Colors.white,
              onSurface: AppColors.primary,
            ),
          ),
          child: child!,
        );
      },
    );

    if (picked != null && picked != _selectedDateRange) {
      setState(() {
        _selectedDateRange = picked;
      });
      if (!context.mounted) return;
      context.read<TransactionBloc>().add(
        FilterTransactionsByDate(widget.accountId, picked),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.background,
      body: CustomScrollView(
        controller: _scrollController,
        slivers: [
          SliverAppBar(
            expandedHeight: 120,
            floating: false,
            pinned: true,
            elevation: 0,
            backgroundColor: AppColors.primary,
            foregroundColor: Colors.white,
            actions: [
              IconButton(
                onPressed: () {
                  final state = context.read<TransactionBloc>().state;
                  if (state is TransactionLoaded) {
                    final accs = context
                        .read<AccountBloc>()
                        .state
                        .accounts
                        .where((a) => a.id == widget.accountId);
                    final name = _allAccounts
                        ? 'Tous les comptes'
                        : (accs.isEmpty ? null : accs.first.libelle);
                    PdfGeneratorService.generateAndPreviewTransactions(
                      accountName: name ?? 'Mon compte',
                      accountId: widget.accountId,
                      transactions: state.transactions,
                    );
                  }
                },
                icon: const Icon(
                  Icons.picture_as_pdf_rounded,
                  color: Colors.white,
                ),
              ),
              IconButton(
                onPressed: () => _selectDateRange(context),
                icon: Icon(
                  _selectedDateRange != null
                      ? Icons.filter_alt_rounded
                      : Icons.calendar_month_rounded,
                  color: Colors.white,
                ),
              ),
              if (_selectedDateRange != null)
                IconButton(
                  onPressed: () {
                    setState(() {
                      _selectedDateRange = null;
                    });
                    context.read<TransactionBloc>().add(
                      LoadTransactions(widget.accountId),
                    );
                  },
                  icon: const Icon(Icons.close_rounded, color: Colors.white),
                ),
              const SizedBox(width: 8),
            ],
            flexibleSpace: FlexibleSpaceBar(
              title: const Text(
                'Historique des opérations',
                style: TextStyle(
                  color: Colors.white,
                  fontWeight: FontWeight.bold,
                  fontSize: 16,
                ),
              ),
              background: Container(
                decoration: const BoxDecoration(
                  gradient: LinearGradient(
                    colors: AppColors.primaryGradient,
                    begin: Alignment.topLeft,
                    end: Alignment.bottomRight,
                  ),
                ),
              ),
            ),
          ),

          SliverToBoxAdapter(
            child: Container(
              padding: const EdgeInsets.all(16),
              child: TextField(
                onChanged: _onSearchChanged,
                decoration: InputDecoration(
                  hintText: _allAccounts
                      ? 'Rechercher une opération (tous comptes)…'
                      : 'Rechercher une transaction…',
                  prefixIcon: const Icon(
                    Icons.search_rounded,
                    color: AppColors.primary,
                  ),
                  filled: true,
                  fillColor: Colors.white,
                  border: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(16),
                    borderSide: BorderSide.none,
                  ),
                  contentPadding: const EdgeInsets.symmetric(vertical: 0),
                ),
              ),
            ),
          ),

          if (_selectedDateRange != null)
            SliverToBoxAdapter(
              child: Container(
                padding: const EdgeInsets.symmetric(
                  horizontal: 16,
                  vertical: 8,
                ),
                color: AppColors.primary.withOpacity(0.05),
                child: Row(
                  children: [
                    const Icon(
                      Icons.date_range_rounded,
                      size: 16,
                      color: AppColors.primary,
                    ),
                    const SizedBox(width: 8),
                    Text(
                      'Du ${DateFormat('dd/MM/yyyy').format(_selectedDateRange!.start)} au ${DateFormat('dd/MM/yyyy').format(_selectedDateRange!.end)}',
                      style: const TextStyle(
                        fontSize: 12,
                        fontWeight: FontWeight.bold,
                        color: AppColors.primary,
                      ),
                    ),
                  ],
                ),
              ),
            ),

          BlocBuilder<TransactionBloc, TransactionState>(
            builder: (context, state) {
              if (state is TransactionLoading) {
                return const SliverFillRemaining(child: TransactionShimmer());
              }

              if (state is TransactionLoaded) {
                if (state.transactions.isEmpty) {
                  return SliverFillRemaining(
                    child: Center(
                      child: Padding(
                        padding: const EdgeInsets.symmetric(horizontal: 32),
                        child: Column(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            Icon(
                              Icons.receipt_long_outlined,
                              size: 56,
                              color: Colors.grey[400],
                            ),
                            const SizedBox(height: 16),
                            Text(
                              'Aucune opération enregistrée',
                              textAlign: TextAlign.center,
                              style: TextStyle(
                                fontWeight: FontWeight.w800,
                                fontSize: 17,
                                color: AppColors.primary.withOpacity(0.85),
                              ),
                            ),
                            const SizedBox(height: 8),
                            Text(
                              'Seuls vos virements, paiements et remboursements effectués dans l’app apparaissent ici.',
                              textAlign: TextAlign.center,
                              style: TextStyle(
                                fontSize: 14,
                                height: 1.35,
                                color: Colors.grey[600],
                              ),
                            ),
                          ],
                        ),
                      ),
                    ),
                  );
                }

                return BlocBuilder<AccountBloc, AccountState>(
                  builder: (context, accountState) {
                    return SliverPadding(
                      padding: const EdgeInsets.symmetric(
                        horizontal: 16,
                        vertical: 20,
                      ),
                      sliver: SliverList(
                        delegate: SliverChildBuilderDelegate(
                          (context, index) {
                            if (index >= state.transactions.length) {
                              return const Padding(
                                padding: EdgeInsets.symmetric(vertical: 20),
                                child: Center(
                                  child: CircularProgressIndicator(
                                    strokeWidth: 2,
                                    color: AppColors.primary,
                                  ),
                                ),
                              );
                            }

                            final EpargneTransactionModel tx =
                                state.transactions[index];
                            final isCredit = tx.type == 'CREDIT';
                            final DateTime date = DateTime.parse(tx.date);
                            final String formattedDate = DateFormat(
                              'dd MMM yyyy à HH:mm',
                              'fr_FR',
                            ).format(date);
                            String? compteLabel;
                            if (_allAccounts) {
                              for (final a in accountState.accounts) {
                                if (a.id == tx.accountId) {
                                  compteLabel = a.libelle;
                                  break;
                                }
                              }
                            }

                            return Container(
                              margin: const EdgeInsets.only(bottom: 12),
                              decoration: BoxDecoration(
                                color: Colors.white,
                                borderRadius: BorderRadius.circular(20),
                                boxShadow: [
                                  BoxShadow(
                                    color: Colors.black.withOpacity(0.02),
                                    blurRadius: 15,
                                    offset: const Offset(0, 5),
                                  ),
                                ],
                              ),
                              child: ListTile(
                                contentPadding: const EdgeInsets.all(16),
                                leading: Container(
                                  padding: const EdgeInsets.all(12),
                                  decoration: BoxDecoration(
                                    color: isCredit
                                        ? AppColors.secondary.withOpacity(0.12)
                                        : AppColors.error.withOpacity(0.1),
                                    borderRadius: BorderRadius.circular(16),
                                  ),
                                  child: Icon(
                                    isCredit
                                        ? Icons.arrow_downward_rounded
                                        : Icons.arrow_upward_rounded,
                                    color: isCredit
                                        ? AppColors.secondary
                                        : AppColors.error,
                                    size: 24,
                                  ),
                                ),
                                title: Text(
                                  tx.libelle,
                                  style: const TextStyle(
                                    fontWeight: FontWeight.w800,
                                    fontSize: 16,
                                    color: AppColors.primary,
                                  ),
                                ),
                                subtitle: Padding(
                                  padding: const EdgeInsets.only(top: 4),
                                  child: Column(
                                    crossAxisAlignment:
                                        CrossAxisAlignment.start,
                                    children: [
                                      if (compteLabel != null)
                                        Padding(
                                          padding: const EdgeInsets.only(
                                            bottom: 2,
                                          ),
                                          child: Text(
                                            compteLabel,
                                            style: TextStyle(
                                              fontSize: 11,
                                              fontWeight: FontWeight.w700,
                                              color: AppColors.primary
                                                  .withOpacity(0.55),
                                            ),
                                          ),
                                        ),
                                      Text(
                                        formattedDate,
                                        style: TextStyle(
                                          fontSize: 12,
                                          color: AppColors.primary.withOpacity(
                                            0.4,
                                          ),
                                          fontWeight: FontWeight.w600,
                                        ),
                                      ),
                                    ],
                                  ),
                                ),
                                trailing: Text(
                                  '${isCredit ? '+' : '-'} ${currencyFormat.format(tx.montant)}',
                                  style: TextStyle(
                                    fontWeight: FontWeight.w900,
                                    color: isCredit
                                        ? AppColors.secondary
                                        : AppColors.error,
                                    fontSize: 16,
                                    letterSpacing: -0.5,
                                  ),
                                ),
                              ),
                            );
                          },
                          childCount: state.hasReachedMax
                              ? state.transactions.length
                              : state.transactions.length + 1,
                        ),
                      ),
                    );
                  },
                );
              }

              if (state is TransactionError) {
                return SliverFillRemaining(
                  child: Center(child: Text('Erreur: ${state.message}')),
                );
              }

              return const SliverToBoxAdapter(child: SizedBox.shrink());
            },
          ),
        ],
      ),
    );
  }
}
