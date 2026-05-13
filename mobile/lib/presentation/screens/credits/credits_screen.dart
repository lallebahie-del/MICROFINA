import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:intl/intl.dart';
import '../../../core/di/service_locator.dart';
import '../../../core/theme/app_theme.dart';
import '../../../domain/repositories/credits_repository.dart';

class CreditsScreen extends StatefulWidget {
  const CreditsScreen({super.key});

  @override
  State<CreditsScreen> createState() => _CreditsScreenState();
}

class _CreditsScreenState extends State<CreditsScreen> {
  final CreditsRepository _repo = sl<CreditsRepository>();
  final _currency = NumberFormat.currency(locale: 'fr_FR', symbol: 'MRU', decimalDigits: 0);

  List<MobileCredit> _items = const [];
  bool _loading = true;
  String? _error;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      final list = await _repo.getMyCredits();
      if (!mounted) return;
      setState(() {
        _items = list;
        _loading = false;
      });
    } catch (e) {
      if (!mounted) return;
      setState(() {
        _error = e.toString().replaceFirst('Exception: ', '');
        _loading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppTheme.bgLight,
      appBar: AppBar(
        title: const Text('Mes prêts', style: TextStyle(fontWeight: FontWeight.bold)),
        backgroundColor: AppTheme.primaryBlue,
        foregroundColor: Colors.white,
        elevation: 0,
        actions: [
          IconButton(
            tooltip: 'Actualiser',
            onPressed: _loading ? null : _load,
            icon: const Icon(Icons.refresh_rounded),
          ),
        ],
      ),
      body: RefreshIndicator(
        onRefresh: _load,
        child: _buildBody(),
      ),
    );
  }

  Widget _buildBody() {
    if (_loading && _items.isEmpty) {
      return const Center(child: CircularProgressIndicator(color: AppTheme.accentBlue));
    }
    if (_error != null) {
      return ListView(children: [
        const SizedBox(height: 80),
        Padding(
          padding: const EdgeInsets.all(24),
          child: Column(children: [
            Icon(Icons.cloud_off_rounded, size: 48, color: Colors.grey[400]),
            const SizedBox(height: 12),
            Text(_error!, textAlign: TextAlign.center, style: TextStyle(color: Colors.grey[700])),
            const SizedBox(height: 16),
            OutlinedButton.icon(
              onPressed: _load,
              icon: const Icon(Icons.refresh_rounded),
              label: const Text('Réessayer'),
            ),
          ]),
        ),
      ]);
    }
    if (_items.isEmpty) {
      return ListView(children: [
        const SizedBox(height: 120),
        Center(
          child: Padding(
            padding: const EdgeInsets.all(24),
            child: Column(children: [
              Icon(Icons.account_balance_rounded, size: 60, color: Colors.grey[300]),
              const SizedBox(height: 12),
              const Text('Aucun prêt en cours.',
                style: TextStyle(color: Colors.grey, fontWeight: FontWeight.w600)),
              const SizedBox(height: 8),
              Text(
                'Tes prêts apparaîtront ici dès que l\'agence aura validé une demande.',
                textAlign: TextAlign.center,
                style: TextStyle(color: Colors.grey[600], fontSize: 13),
              ),
            ]),
          ),
        ),
      ]);
    }

    return ListView.builder(
      padding: const EdgeInsets.all(16),
      itemCount: _items.length,
      itemBuilder: (context, index) => _buildCreditCard(_items[index]),
    );
  }

  Widget _buildCreditCard(MobileCredit c) {
    final color = _statutColor(c.statut);
    final progressPct = c.montantAccorde > 0
        ? (1 - (c.soldeCapital / c.montantAccorde)).clamp(0.0, 1.0)
        : 0.0;

    return Container(
      margin: const EdgeInsets.only(bottom: 14),
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(20),
        boxShadow: AppTheme.softShadow,
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Header
          Row(
            children: [
              Container(
                padding: const EdgeInsets.all(10),
                decoration: BoxDecoration(
                  color: color.withOpacity(0.12),
                  borderRadius: BorderRadius.circular(12),
                ),
                child: Icon(Icons.account_balance_rounded, color: color),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      c.numCredit.isEmpty ? 'Prêt #${c.idCredit}' : c.numCredit,
                      style: const TextStyle(
                        fontSize: 16,
                        fontWeight: FontWeight.w800,
                        color: AppTheme.primaryBlue,
                      ),
                    ),
                    if (c.objetCredit != null && c.objetCredit!.isNotEmpty)
                      Text(c.objetCredit!,
                          style: TextStyle(fontSize: 12, color: Colors.grey[600])),
                  ],
                ),
              ),
              _StatutBadge(label: c.statut, color: color),
            ],
          ),
          const SizedBox(height: 16),

          // Soldes
          Row(
            children: [
              Expanded(
                child: _AmountTile(
                  label: 'Montant accordé',
                  value: _currency.format(c.montantAccorde),
                ),
              ),
              Expanded(
                child: _AmountTile(
                  label: 'Solde restant',
                  value: _currency.format(c.soldeCapital),
                  highlight: true,
                ),
              ),
            ],
          ),
          const SizedBox(height: 12),

          // Progression
          ClipRRect(
            borderRadius: BorderRadius.circular(4),
            child: LinearProgressIndicator(
              value: progressPct,
              minHeight: 6,
              backgroundColor: Colors.grey[200],
              valueColor: AlwaysStoppedAnimation(color),
            ),
          ),
          const SizedBox(height: 4),
          Text(
            '${(progressPct * 100).toStringAsFixed(0)} % remboursé',
            style: TextStyle(fontSize: 11, color: Colors.grey[600]),
          ),

          // Infos secondaires
          const SizedBox(height: 14),
          Wrap(
            spacing: 18,
            runSpacing: 6,
            children: [
              _MetaItem(icon: Icons.percent_rounded,    label: 'Taux',     value: '${c.tauxInteret.toStringAsFixed(2)} %'),
              _MetaItem(icon: Icons.calendar_today_rounded, label: 'Durée', value: '${c.duree} m'),
              _MetaItem(icon: Icons.format_list_numbered_rounded, label: 'Échéances', value: '${c.nombreEcheance}'),
              if (c.dateDeblocage != null)
                _MetaItem(icon: Icons.event_available_rounded, label: 'Décaissé', value: _fmtDate(c.dateDeblocage!)),
              if (c.dateEcheance != null)
                _MetaItem(icon: Icons.event_busy_rounded, label: 'Fin', value: _fmtDate(c.dateEcheance!)),
            ],
          ),
        ],
      ),
    );
  }

  String _fmtDate(String iso) {
    try {
      return DateFormat('dd MMM yyyy').format(DateTime.parse(iso));
    } catch (_) {
      return iso;
    }
  }

  Color _statutColor(String statut) {
    final s = statut.toUpperCase();
    if (s == 'DEBLOQUE')               return AppTheme.successGreen;
    if (s == 'SOLDE' || s == 'CLOTURE')return Colors.grey;
    if (s == 'VALIDE_COMITE' || s == 'VALIDE_AGENT') return AppTheme.accentBlue;
    if (s == 'SOUMIS' || s == 'BROUILLON') return Colors.orange;
    if (s == 'REJETE')                 return AppTheme.errorRed;
    return AppTheme.accentBlue;
  }
}

class _StatutBadge extends StatelessWidget {
  final String label;
  final Color color;
  const _StatutBadge({required this.label, required this.color});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 5),
      decoration: BoxDecoration(
        color: color.withOpacity(0.15),
        borderRadius: BorderRadius.circular(20),
      ),
      child: Text(
        label.isEmpty ? '—' : label,
        style: TextStyle(color: color, fontWeight: FontWeight.w700, fontSize: 11),
      ),
    );
  }
}

class _AmountTile extends StatelessWidget {
  final String label;
  final String value;
  final bool highlight;
  const _AmountTile({required this.label, required this.value, this.highlight = false});

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(label, style: TextStyle(fontSize: 11, color: Colors.grey[600], fontWeight: FontWeight.w600)),
        const SizedBox(height: 2),
        Text(
          value,
          style: TextStyle(
            fontSize: highlight ? 17 : 15,
            color: highlight ? AppTheme.primaryBlue : Colors.black87,
            fontWeight: FontWeight.w800,
          ),
        ),
      ],
    );
  }
}

class _MetaItem extends StatelessWidget {
  final IconData icon;
  final String label;
  final String value;
  const _MetaItem({required this.icon, required this.label, required this.value});

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        Icon(icon, size: 14, color: Colors.grey[500]),
        const SizedBox(width: 5),
        Text('$label : ',
          style: TextStyle(fontSize: 11, color: Colors.grey[600])),
        Text(value,
          style: const TextStyle(fontSize: 11, fontWeight: FontWeight.w700, color: AppTheme.primaryBlue)),
      ],
    );
  }
}
