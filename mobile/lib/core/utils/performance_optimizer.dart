import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class PerformanceOptimizer {
  static void optimizeApp() {
    // Set preferred orientations
    SystemChrome.setPreferredOrientations([
      DeviceOrientation.portraitUp,
      DeviceOrientation.portraitDown,
    ]);

    // Set system UI overlay style
    SystemChrome.setSystemUIOverlayStyle(
      const SystemUiOverlayStyle(
        statusBarColor: Colors.transparent,
        statusBarIconBrightness: Brightness.dark,
        systemNavigationBarColor: Colors.transparent,
        systemNavigationBarIconBrightness: Brightness.dark,
      ),
    );

    // Enable performance overlays for debug mode
    if (kDebugMode) {
      // Uncomment to enable performance overlays
      // WidgetsApp.showPerformanceOverlay = true;
    }
  }

  // Preload images for better performance
  static Future<void> preloadImages(
    BuildContext context,
    List<String> imagePaths,
  ) async {
    for (final path in imagePaths) {
      try {
        await precacheImage(AssetImage(path), context);
      } catch (e) {
        debugPrint('Error preloading image $path: $e');
      }
    }
  }

  // Optimize image loading with placeholder
  static Widget optimizedImage({
    required String image,
    required Widget placeholder,
    double? width,
    double? height,
    BoxFit fit = BoxFit.cover,
    BorderRadius? borderRadius,
  }) {
    return ClipRRect(
      borderRadius: borderRadius ?? BorderRadius.zero,
      child: Image.network(
        image,
        width: width,
        height: height,
        fit: fit,
        loadingBuilder: (context, child, loadingProgress) {
          if (loadingProgress == null) return child;
          return placeholder;
        },
        errorBuilder: (context, error, stackTrace) {
          return Container(
            width: width,
            height: height,
            color: Colors.grey[300],
            child: const Icon(Icons.error_outline_rounded),
          );
        },
      ),
    );
  }

  // Memory management helper
  static void clearImageCache() {
    PaintingBinding.instance.imageCache.clear();
    PaintingBinding.instance.imageCache.clearLiveImages();
  }

  // Optimize list performance with caching
  static Widget optimizedListView({
    required int itemCount,
    required IndexedWidgetBuilder itemBuilder,
    ScrollController? controller,
    EdgeInsets? padding,
    ScrollPhysics? physics,
  }) {
    return ListView.builder(
      controller: controller,
      padding: padding,
      physics: physics ?? const BouncingScrollPhysics(),
      itemCount: itemCount,
      cacheExtent: 250.0, // Optimize for smooth scrolling
      itemBuilder: (context, index) {
        return RepaintBoundary(
          key: ValueKey(index),
          child: itemBuilder(context, index),
        );
      },
    );
  }

  // Performance monitoring
  static void monitorPerformance() {
    if (kDebugMode) {
      WidgetsBinding.instance.addPostFrameCallback((_) {
        debugPrint('Frame rendered');
      });
    }
  }

  // Optimize rebuilds with const widgets
  static Widget constWrapper(Widget child) {
    return RepaintBoundary(child: child);
  }
}

class OptimizedScrollController extends ChangeNotifier {
  final ScrollController _controller;
  final VoidCallback? onScrollEnd;
  Timer? _scrollEndTimer;

  OptimizedScrollController({ScrollController? controller, this.onScrollEnd})
    : _controller = controller ?? ScrollController() {
    _controller.addListener(_onScroll);
  }

  void _onScroll() {
    _scrollEndTimer?.cancel();
    _scrollEndTimer = Timer(const Duration(milliseconds: 150), () {
      onScrollEnd?.call();
    });
  }

  ScrollController get controller => _controller;

  @override
  void dispose() {
    _scrollEndTimer?.cancel();
    _controller.dispose();
    super.dispose();
  }
}

class OptimizedAnimationController extends AnimationController {
  OptimizedAnimationController({
    required super.duration,
    required TickerProvider vsync,
    super.lowerBound = 0.0,
    super.upperBound = 1.0,
    super.animationBehavior = AnimationBehavior.normal,
  }) : super(vsync: vsync);

  @override
  void dispose() {
    stop();
    super.dispose();
  }
}

class PerformanceAwareBuilder extends StatefulWidget {
  final Widget Function(BuildContext context, bool isLowPerformance) builder;
  final Widget? child;

  const PerformanceAwareBuilder({super.key, required this.builder, this.child});

  @override
  State<PerformanceAwareBuilder> createState() =>
      _PerformanceAwareBuilderState();
}

class _PerformanceAwareBuilderState extends State<PerformanceAwareBuilder> {
  bool _isLowPerformance = false;

  @override
  void initState() {
    super.initState();
    _checkPerformance();
  }

  void _checkPerformance() {
    // Simple performance check based on device info
    // In a real app, you might want to use more sophisticated metrics
    WidgetsBinding.instance.addPostFrameCallback((_) {
      final isLowEnd = _isLowEndDevice();
      setState(() {
        _isLowPerformance = isLowEnd;
      });
    });
  }

  bool _isLowEndDevice() {
    // This is a simplified check
    // In production, you might want to use device_info_plus or similar
    return false; // Assume high performance for now
  }

  @override
  Widget build(BuildContext context) {
    return widget.builder(context, _isLowPerformance);
  }
}

class LazyLoadBuilder extends StatefulWidget {
  final Widget Function(BuildContext context) builder;
  final double? triggerOffset;
  final Widget? placeholder;

  const LazyLoadBuilder({
    super.key,
    required this.builder,
    this.triggerOffset,
    this.placeholder,
  });

  @override
  State<LazyLoadBuilder> createState() => _LazyLoadBuilderState();
}

class _LazyLoadBuilderState extends State<LazyLoadBuilder> {
  bool _isLoaded = false;
  final ScrollController _scrollController = ScrollController();

  @override
  void initState() {
    super.initState();
    _scrollController.addListener(_checkVisibility);
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _checkVisibility();
    });
  }

  @override
  void dispose() {
    _scrollController.dispose();
    super.dispose();
  }

  void _checkVisibility() {
    if (_isLoaded) return;

    final triggerOffset = widget.triggerOffset ?? 200.0;
    final position = _scrollController.position;

    if (position.pixels >= position.maxScrollExtent - triggerOffset) {
      setState(() {
        _isLoaded = true;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    if (_isLoaded) {
      return widget.builder(context);
    }

    return widget.placeholder ?? const SizedBox();
  }
}
