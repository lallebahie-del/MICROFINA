import 'package:flutter/material.dart';
import 'package:lottie/lottie.dart';
import '../theme/app_colors.dart';
import '../theme/app_spacing.dart';
import '../theme/app_text_styles.dart';

enum PremiumLoadingType { spinner, dots, pulse, shimmer, lottie }

class PremiumLoading extends StatelessWidget {
  final PremiumLoadingType type;
  final String? message;
  final Color? color;
  final double? size;
  final String? lottieAsset;
  final bool isOverlay;

  const PremiumLoading({
    super.key,
    this.type = PremiumLoadingType.spinner,
    this.message,
    this.color,
    this.size,
    this.lottieAsset,
    this.isOverlay = false,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final loadingColor = color ?? theme.colorScheme.primary;
    final loadingSize = size ?? 40.0;

    Widget loadingWidget = _buildLoadingWidget(loadingColor, loadingSize);

    if (message != null) {
      loadingWidget = Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          loadingWidget,
          const SizedBox(height: AppSpacing.md),
          Text(
            message!,
            style: AppTextStyles.bodyMedium.copyWith(
              color: theme.colorScheme.onSurface,
            ),
            textAlign: TextAlign.center,
          ),
        ],
      );
    }

    if (isOverlay) {
      return Container(
        color: Colors.black.withOpacity(0.3),
        child: Center(child: loadingWidget),
      );
    }

    return Center(child: loadingWidget);
  }

  Widget _buildLoadingWidget(Color color, double size) {
    switch (type) {
      case PremiumLoadingType.spinner:
        return _buildSpinner(color, size);
      case PremiumLoadingType.dots:
        return _buildDots(color, size);
      case PremiumLoadingType.pulse:
        return _buildPulse(color, size);
      case PremiumLoadingType.shimmer:
        return _buildShimmer(color, size);
      case PremiumLoadingType.lottie:
        return _buildLottie(size);
    }
  }

  Widget _buildSpinner(Color color, double size) {
    return SizedBox(
      width: size,
      height: size,
      child: CircularProgressIndicator(
        strokeWidth: 3,
        valueColor: AlwaysStoppedAnimation<Color>(color),
        backgroundColor: color.withOpacity(0.1),
      ),
    );
  }

  Widget _buildDots(Color color, double size) {
    return SizedBox(
      width: size * 3,
      height: size,
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceEvenly,
        children: List.generate(3, (index) {
          return _AnimatedDot(
            color: color,
            size: size / 3,
            delay: Duration(milliseconds: index * 200),
          );
        }),
      ),
    );
  }

  Widget _buildPulse(Color color, double size) {
    return _PulsingCircle(color: color, size: size);
  }

  Widget _buildShimmer(Color color, double size) {
    return _ShimmerLoading(color: color, size: size);
  }

  Widget _buildLottie(double size) {
    return Lottie.asset(
      lottieAsset ?? 'assets/animations/loading.json',
      width: size,
      height: size,
      repeat: true,
      animate: true,
    );
  }
}

class _AnimatedDot extends StatefulWidget {
  final Color color;
  final double size;
  final Duration delay;

  const _AnimatedDot({
    required this.color,
    required this.size,
    required this.delay,
  });

  @override
  State<_AnimatedDot> createState() => _AnimatedDotState();
}

class _AnimatedDotState extends State<_AnimatedDot>
    with SingleTickerProviderStateMixin {
  late AnimationController _controller;
  late Animation<double> _animation;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      duration: const Duration(milliseconds: 1200),
      vsync: this,
    );

    _animation = Tween<double>(
      begin: 0.3,
      end: 1.0,
    ).animate(CurvedAnimation(parent: _controller, curve: Curves.easeInOut));

    Future.delayed(widget.delay, () {
      if (mounted) {
        _controller.repeat(reverse: true);
      }
    });
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: _animation,
      builder: (context, child) {
        return Container(
          width: widget.size,
          height: widget.size,
          decoration: BoxDecoration(
            color: widget.color.withOpacity(_animation.value),
            shape: BoxShape.circle,
          ),
        );
      },
    );
  }
}

class _PulsingCircle extends StatefulWidget {
  final Color color;
  final double size;

  const _PulsingCircle({required this.color, required this.size});

  @override
  State<_PulsingCircle> createState() => _PulsingCircleState();
}

class _PulsingCircleState extends State<_PulsingCircle>
    with SingleTickerProviderStateMixin {
  late AnimationController _controller;
  late Animation<double> _animation;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      duration: const Duration(milliseconds: 1500),
      vsync: this,
    );

    _animation = Tween<double>(
      begin: 0.8,
      end: 1.2,
    ).animate(CurvedAnimation(parent: _controller, curve: Curves.easeInOut));

    _controller.repeat(reverse: true);
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: _animation,
      builder: (context, child) {
        return Container(
          width: widget.size * _animation.value,
          height: widget.size * _animation.value,
          decoration: BoxDecoration(
            color: widget.color.withOpacity(0.3),
            shape: BoxShape.circle,
            border: Border.all(color: widget.color, width: 2),
          ),
        );
      },
    );
  }
}

class _ShimmerLoading extends StatefulWidget {
  final Color color;
  final double size;

  const _ShimmerLoading({required this.color, required this.size});

  @override
  State<_ShimmerLoading> createState() => _ShimmerLoadingState();
}

class _ShimmerLoadingState extends State<_ShimmerLoading>
    with SingleTickerProviderStateMixin {
  late AnimationController _controller;
  late Animation<double> _animation;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      duration: const Duration(milliseconds: 2000),
      vsync: this,
    );

    _animation = Tween<double>(
      begin: -1.0,
      end: 2.0,
    ).animate(CurvedAnimation(parent: _controller, curve: Curves.easeInOut));

    _controller.repeat();
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: _animation,
      builder: (context, child) {
        return Container(
          width: widget.size,
          height: widget.size,
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(widget.size / 2),
            gradient: LinearGradient(
              begin: Alignment.centerLeft,
              end: Alignment.centerRight,
              colors: [
                widget.color.withOpacity(0.1),
                widget.color.withOpacity(0.8),
                widget.color.withOpacity(0.1),
              ],
              stops: [0.0, _animation.value.clamp(0.0, 1.0), 1.0],
            ),
          ),
        );
      },
    );
  }
}

class PremiumSkeleton extends StatelessWidget {
  final double width;
  final double height;
  final BorderRadius? borderRadius;

  const PremiumSkeleton({
    super.key,
    required this.width,
    required this.height,
    this.borderRadius,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      width: width,
      height: height,
      decoration: BoxDecoration(
        borderRadius:
            borderRadius ?? BorderRadius.circular(AppSpacing.radiusSmall),
        gradient: LinearGradient(
          begin: Alignment.centerLeft,
          end: Alignment.centerRight,
          colors: [
            Theme.of(context).colorScheme.surfaceVariant.withOpacity(0.3),
            Theme.of(context).colorScheme.surfaceVariant.withOpacity(0.6),
            Theme.of(context).colorScheme.surfaceVariant.withOpacity(0.3),
          ],
        ),
      ),
    );
  }
}

class PremiumSkeletonCard extends StatelessWidget {
  final Widget child;
  final bool isLoading;

  const PremiumSkeletonCard({
    super.key,
    required this.child,
    this.isLoading = false,
  });

  @override
  Widget build(BuildContext context) {
    if (!isLoading) return child;

    return Container(
      padding: const EdgeInsets.all(AppSpacing.lg),
      decoration: BoxDecoration(
        color: Theme.of(context).colorScheme.surface,
        borderRadius: BorderRadius.circular(AppSpacing.radiusLarge),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              PremiumSkeleton(
                width: 48,
                height: 48,
                borderRadius: BorderRadius.circular(AppSpacing.radiusFull),
              ),
              const SizedBox(width: AppSpacing.md),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    PremiumSkeleton(
                      width: double.infinity,
                      height: 16,
                      borderRadius: BorderRadius.circular(
                        AppSpacing.radiusXSmall,
                      ),
                    ),
                    const SizedBox(height: AppSpacing.xs),
                    PremiumSkeleton(
                      width: 100,
                      height: 12,
                      borderRadius: BorderRadius.circular(
                        AppSpacing.radiusXSmall,
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ),
          const SizedBox(height: AppSpacing.md),
          PremiumSkeleton(
            width: double.infinity,
            height: 20,
            borderRadius: BorderRadius.circular(AppSpacing.radiusXSmall),
          ),
          const SizedBox(height: AppSpacing.xs),
          PremiumSkeleton(
            width: 150,
            height: 16,
            borderRadius: BorderRadius.circular(AppSpacing.radiusXSmall),
          ),
        ],
      ),
    );
  }
}
