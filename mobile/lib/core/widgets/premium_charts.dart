import 'package:flutter/material.dart';
import 'package:fl_chart/fl_chart.dart';
import 'package:intl/intl.dart';
import '../theme/app_colors.dart';
import '../theme/app_spacing.dart';
import '../theme/app_shadows.dart';
import '../theme/app_text_styles.dart';

class PremiumPieChart extends StatelessWidget {
  final List<PremiumPieSlice> data;
  final String? centerText;
  final double? radius;
  final bool showLegend;
  final Function(int)? onTap;

  const PremiumPieChart({
    super.key,
    required this.data,
    this.centerText,
    this.radius,
    this.showLegend = true,
    this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final currencyFormat = NumberFormat.currency(
      locale: 'fr_FR',
      symbol: 'FCFA',
      decimalDigits: 0,
    );

    return Column(
      children: [
        SizedBox(
          height: radius ?? 200,
          child: PieChart(
            PieChartData(
              pieTouchData: PieTouchData(
                touchCallback: (FlTouchEvent event, pieTouchResponse) {
                  if (event.isInterestedForInteractions &&
                      pieTouchResponse != null &&
                      pieTouchResponse.touchedSection != null) {
                    onTap?.call(
                      pieTouchResponse.touchedSection!.touchedSectionIndex,
                    );
                  }
                },
              ),
              sectionsSpace: 4,
              centerSpaceRadius: (radius ?? 200) * 0.4,
              startDegreeOffset: 270,
              sections: data.asMap().entries.map((entry) {
                final index = entry.key;
                final item = entry.value;
                final isTouched =
                    index == (data.length - 1); // Default touched state

                return PieChartSectionData(
                  value: item.value,
                  color: item.color,
                  title: '',
                  radius: isTouched
                      ? (radius ?? 200) * 0.15
                      : (radius ?? 200) * 0.12,
                  badgeWidget: _buildBadge(
                    context,
                    currencyFormat.format(item.value),
                    item.color,
                    isTouched,
                  ),
                  badgePositionPercentageOffset: isTouched ? 1.3 : 1.2,
                );
              }).toList(),
            ),
          ),
        ),
        if (centerText != null) ...[
          const SizedBox(height: AppSpacing.md),
          Text(
            centerText!,
            style: AppTextStyles.caption.copyWith(
              color: theme.colorScheme.onSurfaceVariant,
            ),
          ),
        ],
        if (showLegend) ...[
          const SizedBox(height: AppSpacing.lg),
          Wrap(
            spacing: AppSpacing.md,
            runSpacing: AppSpacing.sm,
            children: data.map((item) {
              return _LegendItem(
                color: item.color,
                label: item.label,
                value: currencyFormat.format(item.value),
              );
            }).toList(),
          ),
        ],
      ],
    );
  }

  Widget _buildBadge(
    BuildContext context,
    String value,
    Color color,
    bool isTouched,
  ) {
    return AnimatedContainer(
      duration: const Duration(milliseconds: 200),
      padding: EdgeInsets.symmetric(
        horizontal: isTouched ? AppSpacing.sm : AppSpacing.xs,
        vertical: isTouched ? AppSpacing.xs : AppSpacing.micro,
      ),
      decoration: BoxDecoration(
        color: color,
        borderRadius: BorderRadius.circular(AppSpacing.radiusSmall),
      ),
      child: Text(
        value,
        style: AppTextStyles.labelSmall.copyWith(
          color: AppColors.textOnPrimary,
          fontWeight: FontWeight.w700,
          fontSize: isTouched ? 10 : 9,
        ),
      ),
    );
  }
}

class _LegendItem extends StatelessWidget {
  final Color color;
  final String label;
  final String value;

  const _LegendItem({
    required this.color,
    required this.label,
    required this.value,
  });

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        Container(
          width: 12,
          height: 12,
          decoration: BoxDecoration(
            color: color,
            borderRadius: BorderRadius.circular(AppSpacing.radiusXSmall),
          ),
        ),
        const SizedBox(width: AppSpacing.xs),
        Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          mainAxisSize: MainAxisSize.min,
          children: [
            Text(
              label,
              style: AppTextStyles.labelSmall.copyWith(
                fontWeight: FontWeight.w600,
              ),
            ),
            Text(
              value,
              style: AppTextStyles.bodySmall.copyWith(
                color: Theme.of(context).colorScheme.onSurfaceVariant,
              ),
            ),
          ],
        ),
      ],
    );
  }
}

class PremiumLineChart extends StatelessWidget {
  final List<PremiumLineSeries> data;
  final List<String> xLabels;
  final String? title;
  final bool showGrid;
  final bool showDots;
  final Color? lineColor;

  const PremiumLineChart({
    super.key,
    required this.data,
    required this.xLabels,
    this.title,
    this.showGrid = true,
    this.showDots = true,
    this.lineColor,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final currencyFormat = NumberFormat.currency(
      locale: 'fr_FR',
      symbol: 'FCFA',
      decimalDigits: 0,
    );

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        if (title != null) ...[
          Text(
            title!,
            style: AppTextStyles.titleMedium.copyWith(
              fontWeight: FontWeight.w700,
            ),
          ),
          const SizedBox(height: AppSpacing.md),
        ],
        SizedBox(
          height: 200,
          child: LineChart(
            LineChartData(
              gridData: showGrid
                  ? FlGridData(
                      show: true,
                      drawVerticalLine: true,
                      drawHorizontalLine: true,
                      getDrawingVerticalLine: (value) {
                        return FlLine(
                          color: theme.colorScheme.outline.withOpacity(0.2),
                          strokeWidth: 1,
                        );
                      },
                      getDrawingHorizontalLine: (value) {
                        return FlLine(
                          color: theme.colorScheme.outline.withOpacity(0.2),
                          strokeWidth: 1,
                        );
                      },
                    )
                  : const FlGridData(show: false),
              titlesData: FlTitlesData(
                show: true,
                rightTitles: const AxisTitles(
                  sideTitles: SideTitles(showTitles: false),
                ),
                topTitles: const AxisTitles(
                  sideTitles: SideTitles(showTitles: false),
                ),
                bottomTitles: AxisTitles(
                  sideTitles: SideTitles(
                    showTitles: true,
                    reservedSize: 30,
                    interval: 1,
                    getTitlesWidget: (value, meta) {
                      final index = value.toInt();
                      if (index >= 0 && index < xLabels.length) {
                        return SideTitleWidget(
                          meta: meta,
                          child: Text(
                            xLabels[index],
                            style: AppTextStyles.labelSmall.copyWith(
                              color: theme.colorScheme.onSurfaceVariant,
                            ),
                          ),
                        );
                      }
                      return const SizedBox();
                    },
                  ),
                ),
                leftTitles: AxisTitles(
                  sideTitles: SideTitles(
                    showTitles: true,
                    reservedSize: 60,
                    getTitlesWidget: (value, meta) {
                      return SideTitleWidget(
                        meta: meta,
                        child: Text(
                          currencyFormat.format(value),
                          style: AppTextStyles.labelSmall.copyWith(
                            color: theme.colorScheme.onSurfaceVariant,
                          ),
                        ),
                      );
                    },
                  ),
                ),
              ),
              borderData: FlBorderData(
                show: true,
                border: Border.all(
                  color: theme.colorScheme.outline.withOpacity(0.2),
                ),
              ),
              minX: 0,
              maxX: (xLabels.length - 1).toDouble(),
              minY: 0,
              lineBarsData: data.map((lineData) {
                return LineChartBarData(
                  spots: lineData.spots,
                  isCurved: true,
                  gradient: LinearGradient(
                    colors: [
                      lineColor ?? AppColors.secondary,
                      (lineColor ?? AppColors.secondary).withOpacity(0.8),
                    ],
                  ),
                  barWidth: 3,
                  isStrokeCapRound: true,
                  dotData: showDots
                      ? FlDotData(
                          show: true,
                          getDotPainter: (spot, percent, barData, index) {
                            return FlDotCirclePainter(
                              radius: 4,
                              color: lineColor ?? AppColors.secondary,
                              strokeWidth: 2,
                              strokeColor: theme.colorScheme.surface,
                            );
                          },
                        )
                      : const FlDotData(show: false),
                  belowBarData: BarAreaData(
                    show: true,
                    gradient: LinearGradient(
                      colors: [
                        (lineColor ?? AppColors.secondary).withOpacity(0.3),
                        (lineColor ?? AppColors.secondary).withOpacity(0.1),
                      ],
                      begin: Alignment.topCenter,
                      end: Alignment.bottomCenter,
                    ),
                  ),
                );
              }).toList(),
            ),
          ),
        ),
      ],
    );
  }
}

class PremiumBarChart extends StatelessWidget {
  final List<PremiumBarSeries> data;
  final List<String> xLabels;
  final String? title;
  final bool showGrid;
  final bool isHorizontal;

  const PremiumBarChart({
    super.key,
    required this.data,
    required this.xLabels,
    this.title,
    this.showGrid = true,
    this.isHorizontal = false,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final currencyFormat = NumberFormat.currency(
      locale: 'fr_FR',
      symbol: 'FCFA',
      decimalDigits: 0,
    );

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        if (title != null) ...[
          Text(
            title!,
            style: AppTextStyles.titleMedium.copyWith(
              fontWeight: FontWeight.w700,
            ),
          ),
          const SizedBox(height: AppSpacing.md),
        ],
        SizedBox(
          height: isHorizontal ? data.length * 40.0 : 200,
          child: BarChart(
            BarChartData(
              gridData: showGrid
                  ? FlGridData(
                      show: true,
                      drawVerticalLine: !isHorizontal,
                      drawHorizontalLine: isHorizontal,
                      getDrawingVerticalLine: (value) {
                        return FlLine(
                          color: theme.colorScheme.outline.withOpacity(0.2),
                          strokeWidth: 1,
                        );
                      },
                      getDrawingHorizontalLine: (value) {
                        return FlLine(
                          color: theme.colorScheme.outline.withOpacity(0.2),
                          strokeWidth: 1,
                        );
                      },
                    )
                  : const FlGridData(show: false),
              titlesData: FlTitlesData(
                show: true,
                rightTitles: const AxisTitles(
                  sideTitles: SideTitles(showTitles: false),
                ),
                topTitles: const AxisTitles(
                  sideTitles: SideTitles(showTitles: false),
                ),
                bottomTitles: isHorizontal
                    ? const AxisTitles(
                        sideTitles: SideTitles(showTitles: false),
                      )
                    : AxisTitles(
                        sideTitles: SideTitles(
                          showTitles: true,
                          reservedSize: 30,
                          getTitlesWidget: (value, meta) {
                            final index = value.toInt();
                            if (index >= 0 && index < xLabels.length) {
                              return SideTitleWidget(
                                meta: meta,
                                child: Text(
                                  xLabels[index],
                                  style: AppTextStyles.labelSmall.copyWith(
                                    color: theme.colorScheme.onSurfaceVariant,
                                  ),
                                ),
                              );
                            }
                            return const SizedBox();
                          },
                        ),
                      ),
                leftTitles: isHorizontal
                    ? AxisTitles(
                        sideTitles: SideTitles(
                          showTitles: true,
                          reservedSize: 60,
                          getTitlesWidget: (value, meta) {
                            return SideTitleWidget(
                              meta: meta,
                              child: Text(
                                xLabels[value.toInt()],
                                style: AppTextStyles.labelSmall.copyWith(
                                  color: theme.colorScheme.onSurfaceVariant,
                                ),
                              ),
                            );
                          },
                        ),
                      )
                    : AxisTitles(
                        sideTitles: SideTitles(
                          showTitles: true,
                          reservedSize: 60,
                          getTitlesWidget: (value, meta) {
                            return SideTitleWidget(
                              meta: meta,
                              child: Text(
                                currencyFormat.format(value),
                                style: AppTextStyles.labelSmall.copyWith(
                                  color: theme.colorScheme.onSurfaceVariant,
                                ),
                              ),
                            );
                          },
                        ),
                      ),
              ),
              borderData: FlBorderData(
                show: true,
                border: Border.all(
                  color: theme.colorScheme.outline.withOpacity(0.2),
                ),
              ),
              barGroups: data.asMap().entries.map((entry) {
                final index = entry.key;
                final item = entry.value;

                return BarChartGroupData(
                  x: index,
                  barRods: [
                    BarChartRodData(
                      toY: item.value,
                      color: item.color ?? AppColors.secondary,
                      width: isHorizontal ? 20 : 16,
                      borderRadius: isHorizontal
                          ? const BorderRadius.only(
                              topLeft: Radius.circular(4),
                              topRight: Radius.circular(4),
                            )
                          : const BorderRadius.only(
                              topLeft: Radius.circular(4),
                              topRight: Radius.circular(4),
                            ),
                      gradient: LinearGradient(
                        colors: [
                          item.color ?? AppColors.secondary,
                          (item.color ?? AppColors.secondary).withOpacity(0.8),
                        ],
                        begin: isHorizontal
                            ? Alignment.centerLeft
                            : Alignment.bottomCenter,
                        end: isHorizontal
                            ? Alignment.centerRight
                            : Alignment.topCenter,
                      ),
                    ),
                  ],
                );
              }).toList(),
            ),
          ),
        ),
      ],
    );
  }
}

class PremiumPieSlice {
  final double value;
  final String label;
  final Color color;

  PremiumPieSlice({
    required this.value,
    required this.label,
    required this.color,
  });
}

class PremiumLineSeries {
  final List<FlSpot> spots;
  final Color? color;

  PremiumLineSeries({required this.spots, this.color});
}

class PremiumBarSeries {
  final double value;
  final Color? color;

  PremiumBarSeries({required this.value, this.color});
}
