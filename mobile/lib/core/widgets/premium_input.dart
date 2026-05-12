import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../theme/app_colors.dart';
import '../theme/app_spacing.dart';
import '../theme/app_shadows.dart';
import '../theme/app_text_styles.dart';

enum PremiumInputType {
  text,
  email,
  phone,
  password,
  number,
  search,
  multiline,
}

enum PremiumInputSize {
  small,
  medium,
  large,
}

class PremiumInput extends StatefulWidget {
  final String? label;
  final String? hint;
  final String? initialValue;
  final bool obscureText;
  final bool enabled;
  final bool required;
  final bool readOnly;
  final int? maxLines;
  final int? minLines;
  final int? maxLength;
  final TextInputType? keyboardType;
  final List<TextInputFormatter>? inputFormatters;
  final ValueChanged<String>? onChanged;
  final ValueChanged<String>? onSubmitted;
  final VoidCallback? onTap;
  final VoidCallback? onClear;
  final String? Function(String?)? validator;
  final TextEditingController? controller;
  final FocusNode? focusNode;
  final Widget? prefixIcon;
  final Widget? suffixIcon;
  final String? errorText;
  final String? helperText;
  final String? counterText;
  final PremiumInputType type;
  final PremiumInputSize size;
  final Color? fillColor;
  final Color? borderColor;
  /// Bordure au focus / saisie (prioritaire sur [borderColor] quand le champ est actif).
  final Color? focusedBorderColor;
  /// Met en avant la bordure sans focus réel (ex. tap + unfocus pour masquer le clavier).
  final bool emphasizeBorder;
  final bool showClearButton;
  final TextInputAction? textInputAction;
  /// Pas de clavier logiciel : saisie réservée au pavé numérique (ex. code PIN).
  final bool pinPadInput;

  const PremiumInput({
    super.key,
    this.label,
    this.hint,
    this.initialValue,
    this.obscureText = false,
    this.enabled = true,
    this.required = false,
    this.readOnly = false,
    this.maxLines = 1,
    this.minLines,
    this.maxLength,
    this.keyboardType,
    this.inputFormatters,
    this.onChanged,
    this.onSubmitted,
    this.onTap,
    this.onClear,
    this.validator,
    this.controller,
    this.focusNode,
    this.prefixIcon,
    this.suffixIcon,
    this.errorText,
    this.helperText,
    this.counterText,
    this.type = PremiumInputType.text,
    this.size = PremiumInputSize.medium,
    this.fillColor,
    this.borderColor,
    this.focusedBorderColor,
    this.emphasizeBorder = false,
    this.showClearButton = false,
    this.textInputAction,
    this.pinPadInput = false,
  });

  @override
  State<PremiumInput> createState() => _PremiumInputState();
}

class _PremiumInputState extends State<PremiumInput> {
  late TextEditingController _controller;
  late FocusNode _focusNode;
  late bool _obscureText;
  late bool _hasFocus;
  late bool _hasText;

  @override
  void initState() {
    super.initState();
    _controller = widget.controller ?? TextEditingController(text: widget.initialValue);
    _focusNode = widget.focusNode ?? FocusNode();
    _obscureText = widget.obscureText;
    _hasFocus = false;
    _hasText = _controller.text.isNotEmpty;
    
    _controller.addListener(_onTextChanged);
    _focusNode.addListener(_onFocusChanged);
  }

  @override
  void dispose() {
    if (widget.controller == null) {
      _controller.dispose();
    }
    if (widget.focusNode == null) {
      _focusNode.dispose();
    }
    super.dispose();
  }

  @override
  void didUpdateWidget(PremiumInput oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.emphasizeBorder != widget.emphasizeBorder) {
      setState(() {});
    }
  }

  void _onTextChanged() {
    final hasText = _controller.text.isNotEmpty;
    if (hasText != _hasText) {
      setState(() {
        _hasText = hasText;
      });
    }
    widget.onChanged?.call(_controller.text);
  }

  void _onFocusChanged() {
    final hasFocus = _focusNode.hasFocus;
    if (hasFocus != _hasFocus) {
      setState(() {
        _hasFocus = hasFocus;
      });
    }
  }

  void _toggleObscureText() {
    setState(() {
      _obscureText = !_obscureText;
    });
  }

  void _clearText() {
    _controller.clear();
    widget.onClear?.call();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    
    final inputStyle = _getInputStyle();
    final dimensions = _getDimensions();
    final showFieldError = widget.errorText != null && widget.errorText!.trim().isNotEmpty;
    final bool activeBorder = _hasFocus || widget.emphasizeBorder;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      mainAxisSize: MainAxisSize.min,
      children: [
        if (widget.label != null) ...[
          Row(
            children: [
              Text(
                widget.label!,
                style: inputStyle.labelStyle,
              ),
              if (widget.required) ...[
                const SizedBox(width: AppSpacing.xs),
                Text(
                  '*',
                  style: inputStyle.labelStyle.copyWith(
                    color: AppColors.error,
                  ),
                ),
              ],
            ],
          ),
          const SizedBox(height: AppSpacing.xs),
        ],
        Container(
          decoration: BoxDecoration(
            color: inputStyle.fillColor,
            borderRadius: BorderRadius.circular(dimensions.radius),
            border: Border.all(
              color: inputStyle.borderColor,
              width: showFieldError || activeBorder ? 2 : AppSpacing.strokeNormal,
            ),
            boxShadow: inputStyle.shadow,
          ),
          child: TextField(
            controller: _controller,
            focusNode: _focusNode,
            enabled: widget.enabled,
            readOnly: widget.readOnly || widget.pinPadInput,
            obscureText: _obscureText,
            maxLines: widget.maxLines,
            minLines: widget.minLines,
            maxLength: widget.maxLength,
            keyboardType: widget.pinPadInput ? TextInputType.none : _getKeyboardType(),
            enableInteractiveSelection: !widget.pinPadInput,
            contextMenuBuilder: widget.pinPadInput
                ? (BuildContext ctx, EditableTextState state) => const SizedBox.shrink()
                : null,
            inputFormatters: widget.inputFormatters ?? _getInputFormatters(),
            onChanged: widget.onChanged,
            onSubmitted: widget.onSubmitted,
            onTap: widget.onTap,
            style: inputStyle.textStyle,
            decoration: InputDecoration(
              hintText: widget.hint,
              hintStyle: inputStyle.hintStyle,
              prefixIcon: widget.prefixIcon != null 
                ? Padding(
                    padding: const EdgeInsets.all(AppSpacing.md),
                    child: widget.prefixIcon,
                  )
                : null,
              suffixIcon: _buildSuffixIcon(inputStyle),
              border: InputBorder.none,
              enabledBorder: InputBorder.none,
              focusedBorder: InputBorder.none,
              errorBorder: InputBorder.none,
              focusedErrorBorder: InputBorder.none,
              disabledBorder: InputBorder.none,
              contentPadding: dimensions.contentPadding,
              counterText: widget.counterText,
            ),
            textInputAction: widget.textInputAction,
          ),
        ),
        if (showFieldError) ...[
          const SizedBox(height: 6),
          Padding(
            padding: const EdgeInsets.only(left: 4, right: 4),
            child: Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Padding(
                  padding: const EdgeInsets.only(top: 1),
                  child: Icon(
                    Icons.error_outline_rounded,
                    size: 14,
                    color: AppColors.error,
                  ),
                ),
                const SizedBox(width: 6),
                Expanded(
                  child: Text(
                    widget.errorText!.trim(),
                    style: inputStyle.errorStyle,
                  ),
                ),
              ],
            ),
          ),
        ] else if (widget.helperText != null) ...[
          const SizedBox(height: AppSpacing.xs),
          Row(
            children: [
              Icon(
                Icons.info_outline_rounded,
                size: 14,
                color: theme.colorScheme.onSurfaceVariant,
              ),
              const SizedBox(width: AppSpacing.xs),
              Expanded(
                child: Text(
                  widget.helperText!,
                  style: inputStyle.helperStyle,
                ),
              ),
            ],
          ),
        ],
      ],
    );
  }

  Widget _buildSuffixIcon(_InputStyle inputStyle) {
    if (widget.suffixIcon != null) {
      return Padding(
        padding: const EdgeInsets.all(AppSpacing.md),
        child: widget.suffixIcon,
      );
    }

    List<Widget> icons = [];

    if (widget.showClearButton && _hasText && _hasFocus) {
      icons.add(
        IconButton(
          icon: Icon(
            Icons.clear_rounded,
            color: Theme.of(context).colorScheme.onSurfaceVariant,
            size: 20,
          ),
          onPressed: _clearText,
          splashRadius: 20,
          constraints: const BoxConstraints(
            minWidth: 32,
            minHeight: 32,
          ),
        ),
      );
    }

    if (widget.obscureText) {
      icons.add(
        IconButton(
          icon: Icon(
            _obscureText ? Icons.visibility_off_rounded : Icons.visibility_rounded,
            color: Theme.of(context).colorScheme.onSurfaceVariant,
            size: 20,
          ),
          onPressed: _toggleObscureText,
          splashRadius: 20,
          constraints: const BoxConstraints(
            minWidth: 32,
            minHeight: 32,
          ),
        ),
      );
    }

    if (icons.isEmpty) return const SizedBox.shrink();

    return Padding(
      padding: const EdgeInsets.only(right: AppSpacing.sm),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: icons,
      ),
    );
  }

  _InputStyle _getInputStyle() {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;
    final hasError = widget.errorText != null && widget.errorText!.trim().isNotEmpty;
    final isEnabled = widget.enabled;

    Color borderColor;
    Color fillColor;
    List<BoxShadow>? shadow;

    if (hasError) {
      borderColor = AppColors.error;
      fillColor = isDark ? AppColors.error.withOpacity(0.1) : AppColors.errorLight;
      shadow = AppShadows.error;
    } else if (_hasFocus || widget.emphasizeBorder) {
      borderColor = widget.focusedBorderColor ?? widget.borderColor ?? AppColors.secondary;
      fillColor = widget.fillColor ??
        (isDark ? AppColors.darkSurfaceVariant : AppColors.surface);
      shadow = AppShadows.glow;
    } else {
      borderColor = widget.borderColor ?? 
        (isDark ? AppColors.darkBorder : AppColors.border);
      fillColor = widget.fillColor ?? 
        (isDark ? AppColors.darkSurfaceVariant : AppColors.surface);
      shadow = null;
    }

    if (!isEnabled) {
      fillColor = fillColor.withOpacity(0.5);
      borderColor = borderColor.withOpacity(0.5);
    }

    return _InputStyle(
      fillColor: fillColor,
      borderColor: borderColor,
      shadow: shadow,
      textStyle: AppTextStyles.inputText.copyWith(
        color: isEnabled 
          ? theme.colorScheme.onSurface 
          : theme.colorScheme.onSurface.withOpacity(0.5),
      ),
      hintStyle: AppTextStyles.inputHint.copyWith(
        color: theme.colorScheme.onSurfaceVariant,
      ),
      labelStyle: AppTextStyles.inputLabel.copyWith(
        color: hasError 
          ? AppColors.error 
          : theme.colorScheme.onSurface,
      ),
      errorStyle: AppTextStyles.errorText.copyWith(
        color: AppColors.error,
      ),
      helperStyle: AppTextStyles.bodySmall.copyWith(
        color: theme.colorScheme.onSurfaceVariant,
      ),
    );
  }

  _InputDimensions _getDimensions() {
    switch (widget.size) {
      case PremiumInputSize.small:
        return _InputDimensions(
          radius: AppSpacing.radiusMedium,
          contentPadding: const EdgeInsets.symmetric(
            horizontal: AppSpacing.md,
            vertical: AppSpacing.sm,
          ),
        );
      
      case PremiumInputSize.medium:
        return _InputDimensions(
          radius: AppSpacing.radiusLarge,
          contentPadding: const EdgeInsets.symmetric(
            horizontal: AppSpacing.lg,
            vertical: AppSpacing.inputPadding,
          ),
        );
      
      case PremiumInputSize.large:
        return _InputDimensions(
          radius: AppSpacing.radiusXLarge,
          contentPadding: const EdgeInsets.symmetric(
            horizontal: AppSpacing.xl,
            vertical: AppSpacing.lg,
          ),
        );
    }
  }

  TextInputType _getKeyboardType() {
    if (widget.keyboardType != null) return widget.keyboardType!;
    
    switch (widget.type) {
      case PremiumInputType.email:
        return TextInputType.emailAddress;
      case PremiumInputType.phone:
        return TextInputType.phone;
      case PremiumInputType.number:
        return TextInputType.number;
      case PremiumInputType.multiline:
        return TextInputType.multiline;
      case PremiumInputType.search:
        return TextInputType.text;
      default:
        return TextInputType.text;
    }
  }

  List<TextInputFormatter>? _getInputFormatters() {
    if (widget.inputFormatters != null) return widget.inputFormatters!;
    
    switch (widget.type) {
      case PremiumInputType.phone:
        return [
          FilteringTextInputFormatter.digitsOnly,
          LengthLimitingTextInputFormatter(15),
        ];
      case PremiumInputType.number:
        return [
          FilteringTextInputFormatter.digitsOnly,
        ];
      default:
        return null;
    }
  }
}

class _InputStyle {
  final Color fillColor;
  final Color borderColor;
  final List<BoxShadow>? shadow;
  final TextStyle textStyle;
  final TextStyle hintStyle;
  final TextStyle labelStyle;
  final TextStyle errorStyle;
  final TextStyle helperStyle;

  _InputStyle({
    required this.fillColor,
    required this.borderColor,
    this.shadow,
    required this.textStyle,
    required this.hintStyle,
    required this.labelStyle,
    required this.errorStyle,
    required this.helperStyle,
  });
}

class _InputDimensions {
  final double radius;
  final EdgeInsets contentPadding;

  _InputDimensions({
    required this.radius,
    required this.contentPadding,
  });
}
