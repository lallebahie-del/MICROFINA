import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:intl/intl.dart';
import '../../../core/theme/app_theme.dart';
import '../../../data/datasources/mock/mock_data.dart';

class NotificationScreen extends StatefulWidget {
  const NotificationScreen({super.key});

  @override
  State<NotificationScreen> createState() => _NotificationScreenState();
}

class _NotificationScreenState extends State<NotificationScreen> {
  @override
  Widget build(BuildContext context) {
    final notifications = MockData.getNotifications();
    final hasUnread = notifications.any((n) => n['isRead'] == false);

    return Scaffold(
      backgroundColor: const Color(0xFFF8FAFC),
      appBar: AppBar(
        title: const Text('Notifications', style: TextStyle(fontWeight: FontWeight.w800)),
        centerTitle: true,
        elevation: 0,
        backgroundColor: Colors.transparent,
        foregroundColor: AppTheme.primaryBlue,
        actions: [
          if (notifications.isNotEmpty && hasUnread)
            TextButton(
              onPressed: () {
                setState(() {
                  MockData.markAllNotificationsAsRead();
                });
              },
              child: const Text('Tout lire', style: TextStyle(fontWeight: FontWeight.bold)),
            ),
          const SizedBox(width: 8),
        ],
      ),
      body: notifications.isEmpty
          ? _buildEmptyState()
          : ListView.builder(
              padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 10),
              itemCount: notifications.length,
              itemBuilder: (context, index) {
                final notif = notifications[index];
                final bool isRead = notif['isRead'] ?? false;
                final date = DateTime.parse(notif['date']);
                
                return _buildNotificationCard(notif, isRead, date);
              },
            ),
    );
  }

  Widget _buildEmptyState() {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Container(
            padding: const EdgeInsets.all(32),
            decoration: BoxDecoration(
              color: AppTheme.primaryBlue.withOpacity(0.05),
              shape: BoxShape.circle,
            ),
            child: Icon(Icons.notifications_none_rounded, size: 64, color: AppTheme.primaryBlue.withOpacity(0.4)),
          ),
          const SizedBox(height: 24),
          const Text(
            'Tout est calme ici',
            style: TextStyle(fontSize: 18, fontWeight: FontWeight.w800, color: AppTheme.primaryBlue),
          ),
          const SizedBox(height: 8),
          Text(
            'Vous n\'avez aucune nouvelle notification.',
            style: TextStyle(color: Colors.grey[600], fontSize: 14),
          ),
        ],
      ),
    );
  }

  Widget _buildNotificationCard(Map<String, dynamic> notif, bool isRead, DateTime date) {
    final category = _getCategory(notif['title']);
    
    return Container(
      margin: const EdgeInsets.only(bottom: 16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(24),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(isRead ? 0.02 : 0.05),
            blurRadius: 15,
            offset: const Offset(0, 4),
          ),
        ],
        border: isRead ? null : Border.all(color: category.color.withOpacity(0.1), width: 1),
      ),
      child: ClipRRect(
        borderRadius: BorderRadius.circular(24),
        child: IntrinsicHeight(
          child: Row(
            children: [
              // Barre latérale de couleur pour les non-lus
              if (!isRead)
                Container(
                  width: 5,
                  color: category.color,
                ),
              Expanded(
                child: Padding(
                  padding: const EdgeInsets.all(20),
                  child: Row(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      // Icône de catégorie
                      Container(
                        padding: const EdgeInsets.all(12),
                        decoration: BoxDecoration(
                          color: category.color.withOpacity(0.1),
                          borderRadius: BorderRadius.circular(16),
                        ),
                        child: Icon(category.icon, color: category.color, size: 24),
                      ),
                      const SizedBox(width: 16),
                      // Contenu
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Row(
                              mainAxisAlignment: MainAxisAlignment.spaceBetween,
                              children: [
                                Expanded(
                                  child: Text(
                                    notif['title'],
                                    style: TextStyle(
                                      fontWeight: isRead ? FontWeight.w700 : FontWeight.w900,
                                      fontSize: 15,
                                      color: AppTheme.primaryBlue,
                                    ),
                                  ),
                                ),
                                if (!isRead)
                                  Container(
                                    width: 8,
                                    height: 8,
                                    decoration: BoxDecoration(
                                      color: category.color,
                                      shape: BoxShape.circle,
                                    ),
                                  ),
                              ],
                            ),
                            const SizedBox(height: 6),
                            Text(
                              notif['message'],
                              style: TextStyle(
                                color: isRead ? Colors.grey[600] : Colors.black87,
                                fontSize: 13,
                                height: 1.4,
                              ),
                            ),
                            const SizedBox(height: 12),
                            Text(
                              _formatDate(date),
                              style: TextStyle(
                                fontSize: 11,
                                fontWeight: FontWeight.w600,
                                color: Colors.grey[400],
                              ),
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  _NotificationCategory _getCategory(String title) {
    final t = title.toLowerCase();
    if (t.contains('sécurité')) {
      return _NotificationCategory(Icons.security_rounded, Colors.orange);
    } else if (t.contains('paiement') || t.contains('virement')) {
      return _NotificationCategory(Icons.account_balance_wallet_rounded, Colors.green);
    } else if (t.contains('rappel')) {
      return _NotificationCategory(Icons.event_note_rounded, Colors.blue);
    }
    return _NotificationCategory(Icons.notifications_rounded, AppTheme.primaryBlue);
  }

  String _formatDate(DateTime date) {
    final now = DateTime.now();
    final difference = now.difference(date);

    if (difference.inMinutes < 60) {
      return 'Il y a ${difference.inMinutes} min';
    } else if (difference.inHours < 24) {
      return 'Il y a ${difference.inHours} h';
    } else if (difference.inDays == 1) {
      return 'Hier';
    } else {
      return DateFormat('dd MMM à HH:mm').format(date);
    }
  }
}

class _NotificationCategory {
  final IconData icon;
  final Color color;

  _NotificationCategory(this.icon, this.color);
}
