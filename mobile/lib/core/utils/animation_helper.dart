import 'package:flutter/material.dart';

class AnimationHelper {
  // --- Page Transitions ---
  static PageRouteBuilder slideTransition({
    required Widget child,
    SlideDirection direction = SlideDirection.left,
    Duration duration = const Duration(milliseconds: 300),
    Curve curve = Curves.easeInOut,
  }) {
    Offset begin;
    switch (direction) {
      case SlideDirection.left:
        begin = const Offset(1.0, 0.0);
        break;
      case SlideDirection.right:
        begin = const Offset(-1.0, 0.0);
        break;
      case SlideDirection.up:
        begin = const Offset(0.0, 1.0);
        break;
      case SlideDirection.down:
        begin = const Offset(0.0, -1.0);
        break;
    }

    return PageRouteBuilder(
      pageBuilder: (context, animation, secondaryAnimation) => child,
      transitionDuration: duration,
      transitionsBuilder: (context, animation, secondaryAnimation, child) {
        return SlideTransition(
          position: Tween<Offset>(
            begin: begin,
            end: Offset.zero,
          ).animate(CurvedAnimation(
            parent: animation,
            curve: curve,
          )),
          child: child,
        );
      },
    );
  }

  static PageRouteBuilder fadeTransition({
    required Widget child,
    Duration duration = const Duration(milliseconds: 300),
    Curve curve = Curves.easeInOut,
  }) {
    return PageRouteBuilder(
      pageBuilder: (context, animation, secondaryAnimation) => child,
      transitionDuration: duration,
      transitionsBuilder: (context, animation, secondaryAnimation, child) {
        return FadeTransition(
          opacity: Tween<double>(
            begin: 0.0,
            end: 1.0,
          ).animate(CurvedAnimation(
            parent: animation,
            curve: curve,
          )),
          child: child,
        );
      },
    );
  }

  static PageRouteBuilder scaleTransition({
    required Widget child,
    Duration duration = const Duration(milliseconds: 300),
    Curve curve = Curves.elasticOut,
  }) {
    return PageRouteBuilder(
      pageBuilder: (context, animation, secondaryAnimation) => child,
      transitionDuration: duration,
      transitionsBuilder: (context, animation, secondaryAnimation, child) {
        return ScaleTransition(
          scale: Tween<double>(
            begin: 0.0,
            end: 1.0,
          ).animate(CurvedAnimation(
            parent: animation,
            curve: curve,
          )),
          child: child,
        );
      },
    );
  }

  // --- Widget Animations ---
  static Widget slideInFromBottom({
    required Widget child,
    Duration duration = const Duration(milliseconds: 300),
    Curve curve = Curves.easeOut,
    double offset = 100.0,
  }) {
    return TweenAnimationBuilder<double>(
      tween: Tween<double>(begin: offset, end: 0.0),
      duration: duration,
      curve: curve,
      builder: (context, value, child) {
        return Transform.translate(
          offset: Offset(0.0, value),
          child: child,
        );
      },
      child: child,
    );
  }

  static Widget slideInFromLeft({
    required Widget child,
    Duration duration = const Duration(milliseconds: 300),
    Curve curve = Curves.easeOut,
    double offset = 100.0,
  }) {
    return TweenAnimationBuilder<double>(
      tween: Tween<double>(begin: -offset, end: 0.0),
      duration: duration,
      curve: curve,
      builder: (context, value, child) {
        return Transform.translate(
          offset: Offset(value, 0.0),
          child: child,
        );
      },
      child: child,
    );
  }

  static Widget fadeIn({
    required Widget child,
    Duration duration = const Duration(milliseconds: 300),
    Curve curve = Curves.easeIn,
  }) {
    return TweenAnimationBuilder<double>(
      tween: Tween<double>(begin: 0.0, end: 1.0),
      duration: duration,
      curve: curve,
      builder: (context, value, child) {
        return Opacity(
          opacity: value,
          child: child,
        );
      },
      child: child,
    );
  }

  static Widget scaleIn({
    required Widget child,
    Duration duration = const Duration(milliseconds: 300),
    Curve curve = Curves.elasticOut,
    double beginScale = 0.0,
  }) {
    return TweenAnimationBuilder<double>(
      tween: Tween<double>(begin: beginScale, end: 1.0),
      duration: duration,
      curve: curve,
      builder: (context, value, child) {
        return Transform.scale(
          scale: value,
          child: child,
        );
      },
      child: child,
    );
  }

  static Widget staggeredAnimation({
    required List<Widget> children,
    Duration duration = const Duration(milliseconds: 300),
    Duration staggerDelay = const Duration(milliseconds: 100),
    Curve curve = Curves.easeOut,
    AnimationType animationType = AnimationType.slideInFromBottom,
  }) {
    return Column(
      children: children.asMap().entries.map((entry) {
        final index = entry.key;
        final child = entry.value;
        
        return TweenAnimationBuilder<double>(
          tween: Tween<double>(begin: 0.0, end: 1.0),
          duration: duration,
          curve: curve,
          onEnd: () {
            // Animation complete callback if needed
          },
          builder: (context, value, child) {
            switch (animationType) {
              case AnimationType.slideInFromBottom:
                return Transform.translate(
                  offset: Offset(0.0, (1.0 - value) * 50.0),
                  child: Opacity(
                    opacity: value,
                    child: child,
                  ),
                );
              case AnimationType.slideInFromLeft:
                return Transform.translate(
                  offset: Offset((1.0 - value) * 50.0, 0.0),
                  child: Opacity(
                    opacity: value,
                    child: child,
                  ),
                );
              case AnimationType.fadeIn:
                return Opacity(
                  opacity: value,
                  child: child,
                );
              case AnimationType.scaleIn:
                return Transform.scale(
                  scale: value,
                  child: child,
                );
            }
          },
          child: child,
        );
      }).toList(),
    );
  }

  // --- Interactive Animations ---
  static Widget bounceOnPress({
    required Widget child,
    required VoidCallback onPressed,
    Duration duration = const Duration(milliseconds: 100),
    double scaleDown = 0.95,
  }) {
    return GestureDetector(
      onTapDown: (_) {
        // Scale down animation
      },
      onTapUp: (_) {
        onPressed();
      },
      onTapCancel: () {
        // Scale back animation
      },
      child: AnimatedScale(
        scale: 1.0,
        duration: duration,
        child: child,
      ),
    );
  }

  static Widget shimmer({
    required Widget child,
    Color baseColor = Colors.grey,
    Color highlightColor = Colors.white,
    Duration duration = const Duration(milliseconds: 1500),
  }) {
    return TweenAnimationBuilder<double>(
      tween: Tween<double>(begin: -2.0, end: 2.0),
      duration: duration,
      onEnd: () {
        // Loop animation
      },
      builder: (context, value, child) {
        return ShaderMask(
          shaderCallback: (bounds) {
            return LinearGradient(
              colors: [
                baseColor,
                highlightColor,
                baseColor,
              ],
              stops: const [0.0, 0.5, 1.0],
              begin: Alignment(value - 1.0, 0.0),
              end: Alignment(value, 0.0),
            ).createShader(bounds);
          },
          child: child,
        );
      },
      child: child,
    );
  }

  // --- Utility Functions ---
  static Future<void> delay(Duration duration) {
    return Future.delayed(duration);
  }

  static AnimationController createController({
    required TickerProvider vsync,
    Duration? duration,
    double lowerBound = 0.0,
    double upperBound = 1.0,
  }) {
    return AnimationController(
      duration: duration ?? const Duration(milliseconds: 300),
      vsync: vsync,
      lowerBound: lowerBound,
      upperBound: upperBound,
    );
  }

  static CurvedAnimation createCurvedAnimation({
    required AnimationController parent,
    Curve curve = Curves.easeInOut,
  }) {
    return CurvedAnimation(
      parent: parent,
      curve: curve,
    );
  }
}

enum SlideDirection {
  left,
  right,
  up,
  down,
}

enum AnimationType {
  slideInFromBottom,
  slideInFromLeft,
  fadeIn,
  scaleIn,
}

class AnimatedCounter extends StatefulWidget {
  final int value;
  final Duration duration;
  final TextStyle? style;
  final String? prefix;
  final String? suffix;

  const AnimatedCounter({
    super.key,
    required this.value,
    this.duration = const Duration(milliseconds: 1000),
    this.style,
    this.prefix,
    this.suffix,
  });

  @override
  State<AnimatedCounter> createState() => _AnimatedCounterState();
}

class _AnimatedCounterState extends State<AnimatedCounter>
    with SingleTickerProviderStateMixin {
  late AnimationController _controller;
  late Animation<int> _animation;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      duration: widget.duration,
      vsync: this,
    );

    _animation = IntTween(
      begin: 0,
      end: widget.value,
    ).animate(CurvedAnimation(
      parent: _controller,
      curve: Curves.easeOut,
    ));

    _controller.forward();
  }

  @override
  void didUpdateWidget(AnimatedCounter oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.value != widget.value) {
      _animation = IntTween(
        begin: oldWidget.value,
        end: widget.value,
      ).animate(CurvedAnimation(
        parent: _controller,
        curve: Curves.easeOut,
      ));
      _controller.forward(from: 0.0);
    }
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
        return Text(
          '${widget.prefix ?? ''}${_animation.value}${widget.suffix ?? ''}',
          style: widget.style,
        );
      },
    );
  }
}

class PulseWidget extends StatefulWidget {
  final Widget child;
  final Duration duration;
  double minScale;
  double maxScale;

  PulseWidget({
    super.key,
    required this.child,
    this.duration = const Duration(milliseconds: 1000),
    this.minScale = 0.95,
    this.maxScale = 1.05,
  });

  @override
  State<PulseWidget> createState() => _PulseWidgetState();
}

class _PulseWidgetState extends State<PulseWidget>
    with SingleTickerProviderStateMixin {
  late AnimationController _controller;
  late Animation<double> _animation;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      duration: widget.duration,
      vsync: this,
    );

    _animation = Tween<double>(
      begin: widget.minScale,
      end: widget.maxScale,
    ).animate(CurvedAnimation(
      parent: _controller,
      curve: Curves.easeInOut,
    ));

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
        return Transform.scale(
          scale: _animation.value,
          child: child,
        );
      },
      child: widget.child,
    );
  }
}
