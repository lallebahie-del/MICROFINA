import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:intl/intl.dart';
import '../../../core/theme/app_theme.dart';
import '../../blocs/transaction/transaction_bloc.dart';
import '../../blocs/transaction/transaction_event.dart';
import '../../blocs/transaction/transaction_state.dart';
import '../../widgets/transaction_shimmer.dart';
import '../../../core/utils/pdf_generator_service.dart';

class TransactionsScreen extends StatefulWidget {
  final String accountId;

  const TransactionsScreen({super.key, required this.accountId});

  @override
  State<TransactionsScreen> createState() => _TransactionsScreenState();
}

class _TransactionsScreenState extends State<TransactionsScreen> {
  final ScrollController _scrollController = ScrollController();
  final currencyFormat = NumberFormat.currency(locale: 'fr_FR', symbol: 'FCFA', decimalDigits: 0);
  DateTimeRange? _selectedDateRange;

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
    if (_scrollController.position.pixels >= _scrollController.position.maxScrollExtent * 0.9) {
      context.read<TransactionBloc>().add(LoadMoreTransactions(widget.accountId));
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
              primary: AppTheme.primaryBlue,
              onPrimary: Colors.white,
              onSurface: AppTheme.primaryBlue,
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
      if (mounted) {
        context.read<TransactionBloc>().add(FilterTransactionsByDate(widget.accountId, picked));
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF0F2F5),
      body: CustomScrollView(
        controller: _scrollController,
        slivers: [
          SliverAppBar(
            expandedHeight: 120,
            floating: false,
            pinned: true,
            elevation: 0,
            backgroundColor: AppTheme.primaryBlue,
            actions: [
              IconButton(
                onPressed: () {
                  final state = context.read<TransactionBloc>().state;
                  if (state is TransactionLoaded) {
                    PdfGeneratorService.generateAndPreviewTransactions(
                      accountName: 'Mon Compte', // À dynamiser si nécessaire
                      accountId: widget.accountId,
                      transactions: state.transactions,
                    );
                  }
                },
                icon: const Icon(Icons.picture_as_pdf_rounded, color: Colors.white),
              ),
              IconButton(
                onPressed: () => _selectDateRange(context),
                icon: Icon(
                  _selectedDateRange != null ? Icons.filter_alt_rounded : Icons.calendar_month_rounded,
                  color: Colors.white,
                ),
              ),
              if (_selectedDateRange != null)
                IconButton(
                  onPressed: () {
                    setState(() {
                      _selectedDateRange = null;
                    });
                    context.read<TransactionBloc>().add(LoadTransactions(widget.accountId));
                  },
                  icon: const Icon(Icons.close_rounded, color: Colors.white),
                ),
              const SizedBox(width: 8),
            ],
            flexibleSpace: FlexibleSpaceBar(
              title: const Text('Historique', style: TextStyle(fontWeight: FontWeight.bold)),
              background: Container(
                decoration: const BoxDecoration(
                  gradient: LinearGradient(
                    colors: [AppTheme.primaryBlue, Color(0xFF1976D2)],
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
                  hintText: 'Rechercher une transaction...',
                  prefixIcon: const Icon(Icons.search_rounded, color: AppTheme.primaryBlue),
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
                padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                color: AppTheme.primaryBlue.withOpacity(0.05),
                child: Row(
                  children: [
                    const Icon(Icons.date_range_rounded, size: 16, color: AppTheme.primaryBlue),
                    const SizedBox(width: 8),
                    Text(
                      'Du ${DateFormat('dd/MM/yyyy').format(_selectedDateRange!.start)} au ${DateFormat('dd/MM/yyyy').format(_selectedDateRange!.end)}',
                      style: const TextStyle(fontSize: 12, fontWeight: FontWeight.bold, color: AppTheme.primaryBlue),
                    ),
                  ],
                ),
              ),
            ),
          
          BlocBuilder<TransactionBloc, TransactionState>(
            builder: (context, state) {
              if (state is TransactionLoading) {
                return const SliverFillRemaining(
                  child: TransactionShimmer(),
                );
              }

              if (state is TransactionLoaded) {
                if (state.transactions.isEmpty) {
                  return const SliverFillRemaining(
                    child: Center(child: Text('Aucune transaction trouvée.')),
                  );
                }

                return SliverPadding(
                  padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 20),
                  sliver: SliverList(
                    delegate: SliverChildBuilderDelegate(
                      (context, index) {
                        if (index >= state.transactions.length) {
                          return const Padding(
                            padding: EdgeInsets.symmetric(vertical: 20),
                            child: Center(child: CircularProgressIndicator(strokeWidth: 2)),
                          );
                        }

                        final tx = state.transactions[index];
                        final isCredit = tx['type'] == 'CREDIT';
                        final DateTime date = DateTime.parse(tx['date']);
                        final String formattedDate = DateFormat('dd MMM yyyy à HH:mm', 'fr_FR').format(date);

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
                                color: isCredit ? Colors.green.withOpacity(0.1) : Colors.red.withOpacity(0.1),
                                borderRadius: BorderRadius.circular(16),
                              ),
                              child: Icon(
                                isCredit ? Icons.arrow_downward_rounded : Icons.arrow_upward_rounded,
                                color: isCredit ? AppTheme.successGreen : AppTheme.errorRed,
                                size: 24,
                              ),
                            ),
                            title: Text(
                              tx['libelle'],
                              style: const TextStyle(fontWeight: FontWeight.w800, fontSize: 16, color: AppTheme.primaryBlue),
                            ),
                            subtitle: Padding(
                              padding: const EdgeInsets.only(top: 4),
                              child: Text(
                                formattedDate,
                                style: TextStyle(fontSize: 12, color: AppTheme.primaryBlue.withOpacity(0.4), fontWeight: FontWeight.w600),
                              ),
                            ),
                            trailing: Text(
                              '${isCredit ? '+' : '-'} ${currencyFormat.format(tx['montant'])}',
                              style: TextStyle(
                                fontWeight: FontWeight.w900,
                                color: isCredit ? AppTheme.successGreen : AppTheme.errorRed,
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
