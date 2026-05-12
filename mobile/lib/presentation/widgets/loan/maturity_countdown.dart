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
    final today = DateTime(now.year, now.month, now.day);
    final maturityDay = DateTime(
      maturityDate.year,
      maturityDate.month,
      maturityDate.day,
    );
    final daysLeft = maturityDay.difference(today).inDays;

    final startDay = DateTime(startDate.year, startDate.month, startDate.day);
    final totalDays = maturityDay.difference(startDay).inDays;
    final elapsedDays = today.difference(startDay).inDays;
    final double progress = totalDays <= 0
        ? 1.0
        : (elapsedDays / totalDays).clamp(0.0, 1.0);

    final String mainLabel;
    final String subLabel;
    final String? tertiary;
    if (daysLeft < 0) {
      mainLabel = '0';
      subLabel = 'ÉCHU';
      tertiary = null;
    } else if (daysLeft == 0) {
      mainLabel = '0';
      subLabel = 'ÉCHÉANCE';
      tertiary = "AUJOURD'HUI";
    } else {
      mainLabel = '$daysLeft';
      subLabel = 'JOURS';
      tertiary = 'RESTANTS';
    }

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
                value: daysLeft < 0 ? 1.0 : progress,
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
                mainLabel,
                style: TextStyle(
                  fontSize: size * 0.25,
                  fontWeight: FontWeight.w900,
                  color: const Color(0xFF1E293B),
                  letterSpacing: -1,
                ),
              ),
              Text(
                subLabel,
                style: TextStyle(
                  fontSize: size * 0.08,
                  fontWeight: FontWeight.w900,
                  color: Colors.grey[400],
                  letterSpacing: 2,
                ),
              ),
              if (tertiary != null)
                Text(
                  tertiary,
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
