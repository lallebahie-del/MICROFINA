import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:intl/intl.dart';
import '../../../core/theme/app_theme.dart';
import '../../../data/datasources/mock/mock_data.dart';

class NotificationScreen extends StatelessWidget {
  const NotificationScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final notifications = MockData.getNotifications();

    return Scaffold(
      appBar: AppBar(
        title: const Text('Notifications', style: TextStyle(fontWeight: FontWeight.bold)),
        backgroundColor: AppTheme.primaryBlue,
        foregroundColor: Colors.white,
        elevation: 0,
      ),
      body: notifications.isEmpty
          ? const Center(child: Text('Aucune notification pour le moment.'))
          : ListView.builder(
              padding: const EdgeInsets.all(16),
              itemCount: notifications.length,
              itemBuilder: (context, index) {
                final notif = notifications[index];
                final date = DateTime.parse(notif['date']);
                
                return Container(
                  margin: const EdgeInsets.only(bottom: 12),
                  decoration: BoxDecoration(
                    color: notif['isRead'] ? Colors.white : AppTheme.primaryBlue.withOpacity(0.05),
                    borderRadius: BorderRadius.circular(20),
                    boxShadow: AppTheme.softShadow,
                  ),
                  child: ListTile(
                    contentPadding: const EdgeInsets.all(16),
                    leading: CircleAvatar(
                      backgroundColor: notif['isRead'] ? Colors.grey[200] : AppTheme.accentBlue.withOpacity(0.2),
                      child: Icon(
                        notif['title'].toString().contains('Sécurité') ? Icons.security_rounded : Icons.notifications_rounded,
                        color: notif['isRead'] ? Colors.grey : AppTheme.accentBlue,
                      ),
                    ),
                    title: Text(
                      notif['title'],
                      style: TextStyle(
                        fontWeight: notif['isRead'] ? FontWeight.w600 : FontWeight.w900,
                        color: AppTheme.primaryBlue,
                      ),
                    ),
                    subtitle: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const SizedBox(height: 4),
                        Text(notif['message'], style: TextStyle(color: Colors.grey[700])),
                        const SizedBox(height: 8),
                        Text(
                          DateFormat('dd MMM yyyy à HH:mm').format(date),
                          style: TextStyle(fontSize: 11, color: Colors.grey[500]),
                        ),
                      ],
                    ),
                  ),
                );
              },
            ),
    );
  }
}
