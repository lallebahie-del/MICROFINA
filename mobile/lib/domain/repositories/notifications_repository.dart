import 'dart:io';

class MobileNotification {
  final int id;
  final String titre;
  final String message;
  final String type;
  final bool lu;
  final DateTime? dateCreation;
  final DateTime? dateLecture;
  final String? lien;

  const MobileNotification({
    required this.id,
    required this.titre,
    required this.message,
    required this.type,
    required this.lu,
    this.dateCreation,
    this.dateLecture,
    this.lien,
  });
}

class NotificationsPage {
  final List<MobileNotification> items;
  final int totalElements;
  final int totalPages;
  final int page;
  final int unread;

  const NotificationsPage({
    required this.items,
    required this.totalElements,
    required this.totalPages,
    required this.page,
    required this.unread,
  });
}

abstract class NotificationsRepository {
  Future<NotificationsPage> fetch({int page = 0, int size = 20});
  Future<void> markAsRead(int id);
  Future<int> markAllAsRead();
  Future<String> uploadProfilePhoto(File file);
}
