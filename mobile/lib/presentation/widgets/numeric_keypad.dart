import 'dart:math';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../../core/theme/app_colors.dart';

class NumericKeypad extends StatefulWidget {
  final Function(String) onNumberPressed;
  final VoidCallback onDeletePressed;
  final bool shuffle;

  /// Couleur des chiffres (défaut : [AppColors.primary], aligné login / marque).
  final Color? digitColor;

  const NumericKeypad({
    super.key,
    required this.onNumberPressed,
    required this.onDeletePressed,
    this.shuffle = false,
    this.digitColor,
  });

  @override
  State<NumericKeypad> createState() => _NumericKeypadState();
}

class _NumericKeypadState extends State<NumericKeypad> {
  late List<String> _numbers;

  @override
  void initState() {
    super.initState();
    _initializeNumbers();
  }

  void _initializeNumbers() {
    _numbers = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '0'];
    if (widget.shuffle) {
      _numbers = List<String>.from(_numbers)..shuffle(Random());
    }
  }

  @override
  void didUpdateWidget(covariant NumericKeypad oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (widget.shuffle != oldWidget.shuffle) {
      _initializeNumbers();
    }
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(vertical: 5),
      child: GridView.builder(
        shrinkWrap: true,
        physics: const NeverScrollableScrollPhysics(),
        gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
          crossAxisCount: 3,
          mainAxisSpacing: 10,
          crossAxisSpacing: 10,
          childAspectRatio: 1.8,
        ),
        itemCount: 12,
        itemBuilder: (context, index) {
          if (index < 9) {
            return _buildKey(context, _numbers[index]);
          } else if (index == 9) {
            return const SizedBox.shrink(); // Case vide à gauche du 0
          } else if (index == 10) {
            return _buildKey(context, _numbers[9]); // Le chiffre 0
          } else {
            return _buildDeleteKey();
          }
        },
      ),
    );
  }

  Widget _buildKey(BuildContext context, String value) {
    final color = widget.digitColor ?? AppColors.primary;
    return Material(
      color: Colors.transparent,
      child: InkWell(
        onTap: () {
          HapticFeedback.lightImpact();
          widget.onNumberPressed(value);
        },
        borderRadius: BorderRadius.circular(20),
        child: Container(
          decoration: BoxDecoration(
            color: Theme.of(context).colorScheme.surface,
            borderRadius: BorderRadius.circular(20),
            boxShadow: [
              BoxShadow(
                color: Colors.black.withValues(alpha: 0.04),
                blurRadius: 8,
                offset: const Offset(0, 4),
              ),
            ],
            border: Border.all(color: color.withValues(alpha: 0.18)),
          ),
          child: Center(
            child: Text(
              value,
              style: TextStyle(
                fontSize: 26,
                fontWeight: FontWeight.w700,
                color: color,
              ),
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildDeleteKey() {
    final color = widget.digitColor ?? AppColors.primary;
    return Material(
      color: Colors.transparent,
      child: InkWell(
        onTap: () {
          HapticFeedback.mediumImpact();
          widget.onDeletePressed();
        },
        borderRadius: BorderRadius.circular(20),
        child: Container(
          decoration: BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.circular(20),
            boxShadow: [
              BoxShadow(
                color: Colors.black.withValues(alpha: 0.04),
                blurRadius: 8,
                offset: const Offset(0, 4),
              ),
            ],
            border: Border.all(color: color.withValues(alpha: 0.18)),
          ),
          child: const Center(
            child: Icon(
              Icons.backspace_rounded,
              color: AppColors.error,
              size: 24,
            ),
          ),
        ),
      ),
    );
  }
}
