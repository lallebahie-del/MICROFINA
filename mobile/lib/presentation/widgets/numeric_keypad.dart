import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class NumericKeypad extends StatefulWidget {
  final Function(String) onNumberPressed;
  final VoidCallback onDeletePressed;
  final bool shuffle;

  const NumericKeypad({
    super.key,
    required this.onNumberPressed,
    required this.onDeletePressed,
    this.shuffle = false,
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
    // L'ordre est maintenant constant de 1 à 0
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
            return _buildKey(_numbers[index]);
          } else if (index == 9) {
            return const SizedBox.shrink(); // Case vide à gauche du 0
          } else if (index == 10) {
            return _buildKey(_numbers[9]); // Le chiffre 0
          } else {
            return _buildDeleteKey();
          }
        },
      ),
    );
  }

  Widget _buildKey(String value) {
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
            color: Colors.white,
            borderRadius: BorderRadius.circular(20),
            boxShadow: [
              BoxShadow(
                color: Colors.black.withOpacity(0.04),
                blurRadius: 8,
                offset: const Offset(0, 4),
              ),
            ],
            border: Border.all(color: Colors.grey.withOpacity(0.1)),
          ),
          child: Center(
            child: Text(
              value,
              style: const TextStyle(
                fontSize: 26,
                fontWeight: FontWeight.w700,
                color: Color(0xFF0D47A1),
              ),
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildDeleteKey() {
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
            color: Colors.red.withOpacity(0.05),
            borderRadius: BorderRadius.circular(20),
          ),
          child: const Center(
            child: Icon(Icons.backspace_rounded, color: Colors.red, size: 24),
          ),
        ),
      ),
    );
  }
}
