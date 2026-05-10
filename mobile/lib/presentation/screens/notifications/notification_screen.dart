import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import '../../../core/di/service_locator.dart';
import '../../../core/theme/app_theme.dart';
import '../../../domain/repositories/notifications_repository.dart';

class NotificationScreen extends StatefulWidget {
  const NotificationScreen({super.key});

  @override
  State<NotificationScreen> createState() => _NotificationScreenState();
}

class _NotificationScreenState extends State<NotificationScreen> {
  final NotificationsRepository _repo = sl<NotificationsRepository>();

  List<MobileNotification> _items = const [];
  bool _loading = true;
  String? _error;
  int _unread = 0;

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
      final page = await _repo.fetch(page: 0, size: 50);
      if (!mounted) return;
      setState(() {
        _items = page.items;
        _unread = page.unread;
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

  Future<void> _markAsRead(MobileNotification n) async {
    if (n.lu) return;
    try {
      await _repo.markAsRead(n.id);
      _load();
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(e.toString().replaceFirst('Exception: ', ''))),
      );
    }
  }

  Future<void> _markAllAsRead() async {
    try {
      final n = await _repo.markAllAsRead();
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('$n notification(s) marquée(s) comme lue(s).')),
      );
      _load();
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(e.toString().replaceFirst('Exception: ', ''))),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Row(children: [
          const Text(
            'Notifications',
            style: TextStyle(
              fontWeight: FontWeight.bold,
              color: Colors.white,
            ),
          ),
          if (_unread > 0)
            Padding(
              padding: const EdgeInsets.only(left: 10),
              child: Container(
                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                decoration: BoxDecoration(
                  color: AppTheme.errorRed,
                  borderRadius: BorderRadius.circular(12),
                ),
                child: Text('$_unread',
                  style: const TextStyle(color: Colors.white, fontSize: 12, fontWeight: FontWeight.bold)),
              ),
            ),
        ]),
        backgroundColor: AppTheme.primaryBlue,
        foregroundColor: Colors.white,
        elevation: 0,
        iconTheme: const IconThemeData(color: Colors.white),
        actionsIconTheme: const IconThemeData(color: Colors.white),
        titleTextStyle: const TextStyle(
          color: Colors.white,
          fontSize: 20,
          fontWeight: FontWeight.bold,
        ),
        actions: [
          if (_unread > 0)
            IconButton(
              tooltip: 'Tout marquer comme lu',
              onPressed: _markAllAsRead,
              icon: const Icon(Icons.done_all_rounded, color: Colors.white),
            ),
          IconButton(
            tooltip: 'Actualiser',
            onPressed: _loading ? null : _load,
            icon: const Icon(Icons.refresh_rounded, color: Colors.white),
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
          child: Column(children: [
            Icon(Icons.notifications_off_rounded, size: 60, color: Colors.grey[300]),
            const SizedBox(height: 12),
            const Text('Aucune notification pour le moment.',
              style: TextStyle(color: Colors.grey)),
          ]),
        ),
      ]);
    }
    return ListView.builder(
      padding: const EdgeInsets.all(16),
      itemCount: _items.length,
      itemBuilder: (context, index) => _buildItem(_items[index]),
    );
  }

  Widget _buildItem(MobileNotification n) {
    final iconData = _iconForType(n.type, n.titre);
    final color = n.lu ? Colors.grey : AppTheme.accentBlue;

    return Container(
      margin: const EdgeInsets.only(bottom: 12),
      decoration: BoxDecoration(
        color: n.lu ? Colors.white : AppTheme.primaryBlue.withOpacity(0.05),
        borderRadius: BorderRadius.circular(20),
        boxShadow: AppTheme.softShadow,
      ),
      child: ListTile(
        contentPadding: const EdgeInsets.all(16),
        onTap: () => _markAsRead(n),
        leading: CircleAvatar(
          backgroundColor: n.lu ? Colors.grey[200] : color.withOpacity(0.2),
          child: Icon(iconData, color: color),
        ),
        title: Text(
          n.titre,
          style: TextStyle(
            fontWeight: n.lu ? FontWeight.w600 : FontWeight.w900,
            color: AppTheme.primaryBlue,
          ),
        ),
        subtitle: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const SizedBox(height: 4),
            if (n.message.isNotEmpty)
              Text(n.message, style: TextStyle(color: Colors.grey[700])),
            const SizedBox(height: 8),
            Text(
              n.dateCreation != null
                  ? DateFormat('dd MMM yyyy à HH:mm').format(n.dateCreation!)
                  : '',
              style: TextStyle(fontSize: 11, color: Colors.grey[500]),
            ),
          ],
        ),
      ),
    );
  }

  IconData _iconForType(String type, String titre) {
    final t = type.toUpperCase();
    if (t == 'ALERTE')   return Icons.warning_amber_rounded;
    if (t == 'OPERATION' || t == 'TRANSFER' || t == 'PAY') return Icons.swap_horiz_rounded;
    if (t == 'COMITE')   return Icons.gavel_rounded;
    if (titre.toLowerCase().contains('sécurité') || titre.toLowerCase().contains('securite')) {
      return Icons.security_rounded;
    }
    return Icons.notifications_rounded;
  }
}
