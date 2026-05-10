import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:intl/intl.dart';
import '../../../core/theme/app_theme.dart';
import '../../blocs/transaction/transaction_bloc.dart';
import '../../blocs/transaction/transaction_event.dart';
import '../../blocs/transaction/transaction_state.dart';
import '../../widgets/transaction_shimmer.dart';

class TransactionsScreen extends StatefulWidget {
  final String accountId;

  const TransactionsScreen({super.key, required this.accountId});

  @override
  State<TransactionsScreen> createState() => _TransactionsScreenState();
}

class _TransactionsScreenState extends State<TransactionsScreen> {
  final ScrollController _scrollController = ScrollController();
  final currencyFormat = NumberFormat.currency(locale: 'fr_FR', symbol: 'FCFA', decimalDigits: 0);

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

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF0F2F5),
      body: CustomScrollView(
        controller: _scrollController,
        slivers: [
          SliverAppBar(
            expandedHeight: 100,
            floating: false,
            pinned: true,
            elevation: 0,
            backgroundColor: AppTheme.primaryBlue,
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

                        return Container(
                          margin: const EdgeInsets.only(bottom: 12),
                          decoration: BoxDecoration(
                            color: Colors.white,
                            borderRadius: BorderRadius.circular(15),
                            boxShadow: [
                              BoxShadow(
                                color: Colors.black.withOpacity(0.03),
                                blurRadius: 10,
                                offset: const Offset(0, 4),
                              ),
                            ],
                          ),
                          child: ListTile(
                            contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                            leading: Container(
                              padding: const EdgeInsets.all(10),
                              decoration: BoxDecoration(
                                color: isCredit ? Colors.green.withOpacity(0.1) : Colors.red.withOpacity(0.1),
                                shape: BoxShape.circle,
                              ),
                              child: Icon(
                                isCredit ? Icons.add_rounded : Icons.remove_rounded,
                                color: isCredit ? AppTheme.successGreen : AppTheme.errorRed,
                                size: 24,
                              ),
                            ),
                            title: Text(
                              tx['libelle'],
                              style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 15),
                            ),
                            subtitle: Padding(
                              padding: const EdgeInsets.only(top: 4),
                              child: Text(
                                tx['date'],
                                style: TextStyle(fontSize: 12, color: Colors.grey.shade600),
                              ),
                            ),
                            trailing: Text(
                              '${isCredit ? '+' : '-'} ${currencyFormat.format(tx['montant'])}',
                              style: TextStyle(
                                fontWeight: FontWeight.w900,
                                color: isCredit ? AppTheme.successGreen : AppTheme.errorRed,
                                fontSize: 16,
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
