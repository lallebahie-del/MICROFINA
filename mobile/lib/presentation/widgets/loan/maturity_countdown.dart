import 'package:flutter/material.dart';
import 'dart:math' as math;

class MaturityCountdown extends StatelessWidget {
  final DateTime startDate;
  final DateTime maturityDate;
  final double size;

  const MaturityCountdown({
    super.key,
    required this.startDate,
    required this.maturityDate,
    this.size = 140,
  });

  @override
  Widget build(BuildContext context) {
    final now = DateTime.now();
    final totalDuration = maturityDate.difference(startDate).inSeconds;
    final elapsedDuration = now.difference(startDate).inSeconds;
    
    // Calcul du pourcentage de temps écoulé
    final double progress = (elapsedDuration / totalDuration).clamp(0.0, 1.0);
    
    // Calcul des jours restants
    final int remainingDays = maturityDate.difference(now).inDays;
    final displayDays = remainingDays < 0 ? 0 : remainingDays;

    return Center(
      child: Stack(
        alignment: Alignment.center,
        children: [
          SizedBox(
            width: size,
            height: size,
            child: ShaderMask(
              shaderCallback: (rect) {
                return const SweepGradient(
                  startAngle: -math.pi / 2,
                  endAngle: 3 * math.pi / 2,
                  colors: [Colors.blue, Colors.teal, Colors.green],
                  stops: [0.0, 0.5, 1.0],
                ).createShader(rect);
              },
              child: CircularProgressIndicator(
                value: progress,
                strokeWidth: 12,
                backgroundColor: Colors.grey[200]!.withOpacity(0.3),
                strokeCap: StrokeCap.round,
              ),
            ),
          ),
          Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Text(
                '$displayDays',
                style: TextStyle(
                  fontSize: size * 0.25,
                  fontWeight: FontWeight.w900,
                  color: const Color(0xFF1E293B), // AppTheme.primaryBlue
                  letterSpacing: -1,
                ),
              ),
              Text(
                'JOURS',
                style: TextStyle(
                  fontSize: size * 0.08,
                  fontWeight: FontWeight.w900,
                  color: Colors.grey[400],
                  letterSpacing: 2,
                ),
              ),
              Text(
                'RESTANTS',
                style: TextStyle(
                  fontSize: size * 0.07,
                  fontWeight: FontWeight.w800,
                  color: Colors.grey[400],
                  letterSpacing: 1,
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}
