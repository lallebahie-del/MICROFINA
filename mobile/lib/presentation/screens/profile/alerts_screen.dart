import 'package:flutter/material.dart';

import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../data/datasources/mock/mock_data.dart';

class AlertsScreen extends StatefulWidget {
  const AlertsScreen({super.key});

  @override
  State<AlertsScreen> createState() => _AlertsScreenState();
}

class _AlertsScreenState extends State<AlertsScreen> {
  late final List<Map<String, dynamic>> _preferences;

  @override
  void initState() {
    super.initState();
    _preferences = MockData.mockOptSmsList
        .map((item) => Map<String, dynamic>.from(item))
        .toList();
  }

  void _togglePreference(int index, bool value) {
    setState(() => _preferences[index]['enabled'] = value);
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: const Text('Préférence enregistrée'),
        behavior: SnackBarBehavior.floating,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
        margin: const EdgeInsets.all(16),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF8FAFC),
      appBar: AppBar(
        title: const Text(
          'Gestion des alertes',
          style: TextStyle(fontWeight: FontWeight.w900),
        ),
        centerTitle: true,
        elevation: 0,
        backgroundColor: Colors.white,
        foregroundColor: AppColors.primary,
      ),
      body: ListView(
        padding: const EdgeInsets.all(AppSpacing.screenPadding),
        children: [
          _buildHeader(),
          const SizedBox(height: AppSpacing.lg),
          ...List.generate(_preferences.length, (index) {
            final item = _preferences[index];
            return Padding(
              padding: const EdgeInsets.only(bottom: AppSpacing.md),
              child: SwitchListTile.adaptive(
                value: item['enabled'] as bool,
                onChanged: (value) => _togglePreference(index, value),
                tileColor: Colors.white,
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(20),
                ),
                contentPadding: const EdgeInsets.symmetric(
                  horizontal: AppSpacing.md,
                  vertical: AppSpacing.sm,
                ),
                secondary: const Icon(
                  Icons.sms_outlined,
                  color: AppColors.primary,
                ),
                title: Text(
                  item['label'] as String,
                  style: const TextStyle(
                    color: AppColors.primary,
                    fontWeight: FontWeight.w900,
                  ),
                ),
                subtitle: Text(
                  (item['enabled'] as bool)
                      ? 'OPTSMS activé'
                      : 'OPTSMS désactivé',
                  style: const TextStyle(fontWeight: FontWeight.w600),
                ),
                activeThumbColor: Colors.white,
                activeTrackColor: AppColors.success,
              ),
            );
          }),
        ],
      ),
    );
  }

  Widget _buildHeader() {
    return Container(
      padding: const EdgeInsets.all(AppSpacing.lg),
      decoration: BoxDecoration(
        color: AppColors.primary,
        borderRadius: BorderRadius.circular(AppSpacing.radiusXXLarge),
      ),
      child: const Row(
        children: [
          Icon(
            Icons.notifications_active_rounded,
            color: Colors.white,
            size: 32,
          ),
          SizedBox(width: AppSpacing.md),
          Expanded(
            child: Text(
              'Préférences OPTSMS',
              style: TextStyle(
                color: Colors.white,
                fontSize: 18,
                fontWeight: FontWeight.w900,
              ),
            ),
          ),
        ],
      ),
    );
  }
}
