import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import '../theme/app_colors.dart';
import '../theme/app_spacing.dart';
import '../theme/app_shadows.dart';
import '../theme/app_text_styles.dart';

enum PremiumCardType {
  standard,
  elevated,
  outlined,
  glass,
  gradient,
}

enum PremiumCardSize {
  small,
  medium,
  large,
}

class PremiumCard extends StatelessWidget {
  final Widget child;
  final PremiumCardType type;
  final PremiumCardSize size;
  final EdgeInsetsGeometry? padding;
  final EdgeInsetsGeometry? margin;
  final VoidCallback? onTap;
  final Color? backgroundColor;
  final Color? borderColor;
  final List<BoxShadow>? customShadow;
  final bool isInteractive;
  final double? width;
  final double? height;
  final Widget? header;
  final Widget? footer;
  final CrossAxisAlignment alignment;

  const PremiumCard({
    super.key,
    required this.child,
    this.type = PremiumCardType.standard,
    this.size = PremiumCardSize.medium,
    this.padding,
    this.margin,
    this.onTap,
    this.backgroundColor,
    this.borderColor,
    this.customShadow,
    this.isInteractive = false,
    this.width,
    this.height,
    this.header,
    this.footer,
    this.alignment = CrossAxisAlignment.start,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;
    
    final cardStyle = _getCardStyle(isDark);
    final dimensions = _getDimensions();
    
    Widget cardChild = Column(
      crossAxisAlignment: alignment,
      mainAxisSize: MainAxisSize.min,
      children: [
        if (header != null) ...[
          header!,
          SizedBox(height: AppSpacing.md),
        ],
        Flexible(child: child),
        if (footer != null) ...[
          SizedBox(height: AppSpacing.md),
          footer!,
        ],
      ],
    );

    if (isInteractive || onTap != null) {
      cardChild = Material(
        color: Colors.transparent,
        child: InkWell(
          onTap: onTap,
          borderRadius: BorderRadius.circular(dimensions.radius),
          splashColor: cardStyle.splashColor,
          highlightColor: cardStyle.highlightColor,
          child: cardChild,
        ),
      );
    }

    return Container(
      width: width,
      height: height,
      margin: margin ?? dimensions.margin,
      decoration: BoxDecoration(
        color: cardStyle.backgroundColor,
        borderRadius: BorderRadius.circular(dimensions.radius),
        border: cardStyle.border,
        boxShadow: cardStyle.shadow,
        gradient: cardStyle.gradient,
      ),
      child: Padding(
        padding: padding ?? dimensions.padding,
        child: cardChild,
      ),
    );
  }

  _CardStyle _getCardStyle(bool isDark) {
    switch (type) {
      case PremiumCardType.standard:
        return _CardStyle(
          backgroundColor: backgroundColor ?? 
            (isDark ? AppColors.darkSurface : AppColors.surface),
          shadow: customShadow ?? AppShadows.card,
          border: null,
          gradient: null,
          splashColor: Colors.transparent,
          highlightColor: Colors.transparent,
        );
      
      case PremiumCardType.elevated:
        return _CardStyle(
          backgroundColor: backgroundColor ?? 
            (isDark ? AppColors.darkSurface : AppColors.surface),
          shadow: customShadow ?? AppShadows.floating,
          border: null,
          gradient: null,
          splashColor: Colors.transparent,
          highlightColor: Colors.transparent,
        );
      
      case PremiumCardType.outlined:
        return _CardStyle(
          backgroundColor: backgroundColor ?? 
            (isDark ? AppColors.darkSurface : AppColors.surface),
          shadow: AppShadows.none,
          border: Border.all(
            color: borderColor ?? 
              (isDark ? AppColors.darkBorder : AppColors.border),
            width: AppSpacing.strokeNormal,
          ),
          gradient: null,
          splashColor: Colors.transparent,
          highlightColor: Colors.transparent,
        );
      
      case PremiumCardType.glass:
        return _CardStyle(
          backgroundColor: (backgroundColor ?? AppColors.glass),
          shadow: customShadow ?? AppShadows.soft,
          border: Border.all(
            color: AppColors.glassBorder,
            width: AppSpacing.strokeThin,
          ),
          gradient: LinearGradient(
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
            colors: [
              Colors.white.withOpacity(0.1),
              Colors.white.withOpacity(0.05),
            ],
          ),
          splashColor: Colors.white.withOpacity(0.1),
          highlightColor: Colors.white.withOpacity(0.05),
        );
      
      case PremiumCardType.gradient:
        return _CardStyle(
          backgroundColor: backgroundColor ?? AppColors.primary,
          shadow: customShadow ?? AppShadows.premium,
          border: null,
          gradient: LinearGradient(
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
            colors: backgroundColor != null 
              ? [backgroundColor!, backgroundColor!.withOpacity(0.8)]
              : AppColors.primaryGradient,
          ),
          splashColor: Colors.white.withOpacity(0.1),
          highlightColor: Colors.white.withOpacity(0.05),
        );
    }
  }

  _CardDimensions _getDimensions() {
    switch (size) {
      case PremiumCardSize.small:
        return _CardDimensions(
          padding: const EdgeInsets.all(AppSpacing.md),
          margin: const EdgeInsets.all(AppSpacing.xs),
          radius: AppSpacing.radiusMedium,
        );
      
      case PremiumCardSize.medium:
        return _CardDimensions(
          padding: const EdgeInsets.all(AppSpacing.lg),
          margin: const EdgeInsets.all(AppSpacing.sm),
          radius: AppSpacing.radiusLarge,
        );
      
      case PremiumCardSize.large:
        return _CardDimensions(
          padding: const EdgeInsets.all(AppSpacing.xl),
          margin: const EdgeInsets.all(AppSpacing.md),
          radius: AppSpacing.radiusXXLarge,
        );
    }
  }
}

class _CardStyle {
  final Color backgroundColor;
  final List<BoxShadow> shadow;
  final BoxBorder? border;
  final Gradient? gradient;
  final Color splashColor;
  final Color highlightColor;

  _CardStyle({
    required this.backgroundColor,
    required this.shadow,
    this.border,
    this.gradient,
    required this.splashColor,
    required this.highlightColor,
  });
}

class _CardDimensions {
  final EdgeInsetsGeometry padding;
  final EdgeInsetsGeometry margin;
  final double radius;

  _CardDimensions({
    required this.padding,
    required this.margin,
    required this.radius,
  });
}

// --- Specialized Card Components ---

class AccountCard extends StatelessWidget {
  final String accountName;
  final String accountNumber;
  final double balance;
  final String accountType;
  final Color? accountColor;
  final VoidCallback? onTap;
  final bool showBalance;

  const AccountCard({
    super.key,
    required this.accountName,
    required this.accountNumber,
    required this.balance,
    required this.accountType,
    this.accountColor,
    this.onTap,
    this.showBalance = true,
  });

  @override
  Widget build(BuildContext context) {
    final currencyFormat = NumberFormat.currency(
      locale: 'fr_FR',
      symbol: 'FCFA',
      decimalDigits: 0,
    );

    return PremiumCard(
      type: PremiumCardType.elevated,
      size: PremiumCardSize.medium,
      onTap: onTap,
      isInteractive: true,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Container(
                width: 4,
                height: 32,
                decoration: BoxDecoration(
                  color: accountColor ?? AppColors.accountChecking,
                  borderRadius: BorderRadius.circular(2),
                ),
              ),
              const SizedBox(width: AppSpacing.md),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      accountName,
                      style: AppTextStyles.cardTitle,
                    ),
                    const SizedBox(height: AppSpacing.xs),
                    Text(
                      accountNumber,
                      style: AppTextStyles.bodySmall.copyWith(
                        color: Theme.of(context).colorScheme.onSurfaceVariant,
                      ),
                    ),
                  ],
                ),
              ),
              Container(
                padding: const EdgeInsets.symmetric(
                  horizontal: AppSpacing.sm,
                  vertical: AppSpacing.xs,
                ),
                decoration: BoxDecoration(
                  color: (accountColor ?? AppColors.accountChecking).withOpacity(0.1),
                  borderRadius: BorderRadius.circular(AppSpacing.radiusFull),
                ),
                child: Text(
                  accountType,
                  style: AppTextStyles.labelSmall.copyWith(
                    color: accountColor ?? AppColors.accountChecking,
                    fontWeight: FontWeight.w700,
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: AppSpacing.md),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                'Solde disponible',
                style: AppTextStyles.bodySmall.copyWith(
                  color: Theme.of(context).colorScheme.onSurfaceVariant,
                ),
              ),
              Text(
                showBalance ? currencyFormat.format(balance) : '••••••',
                style: AppTextStyles.transactionAmount.copyWith(
                  color: accountColor ?? AppColors.accountChecking,
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}

class TransactionCard extends StatelessWidget {
  final String title;
  final String description;
  final double amount;
  final DateTime date;
  final String category;
  final Color? categoryColor;
  final bool isCredit;
  final VoidCallback? onTap;

  const TransactionCard({
    super.key,
    required this.title,
    required this.description,
    required this.amount,
    required this.date,
    required this.category,
    this.categoryColor,
    this.isCredit = false,
    this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final currencyFormat = NumberFormat.currency(
      locale: 'fr_FR',
      symbol: 'FCFA',
      decimalDigits: 0,
    );
    final dateFormat = DateFormat('dd MMM yyyy', 'fr_FR');

    return PremiumCard(
      type: PremiumCardType.standard,
      size: PremiumCardSize.small,
      onTap: onTap,
      isInteractive: true,
      padding: const EdgeInsets.all(AppSpacing.md),
      child: Row(
        children: [
          Container(
            width: 40,
            height: 40,
            decoration: BoxDecoration(
              color: (categoryColor ?? AppColors.categoryOther).withOpacity(0.1),
              borderRadius: BorderRadius.circular(AppSpacing.radiusMedium),
            ),
            child: Icon(
              _getCategoryIcon(category),
              color: categoryColor ?? AppColors.categoryOther,
              size: 20,
            ),
          ),
          const SizedBox(width: AppSpacing.md),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  title,
                  style: AppTextStyles.titleSmall,
                ),
                const SizedBox(height: AppSpacing.xs),
                Text(
                  description,
                  style: AppTextStyles.bodySmall.copyWith(
                    color: Theme.of(context).colorScheme.onSurfaceVariant,
                  ),
                ),
              ],
            ),
          ),
          Column(
            crossAxisAlignment: CrossAxisAlignment.end,
            children: [
              Text(
                '${isCredit ? '+' : '-'}${currencyFormat.format(amount.abs())}',
                style: AppTextStyles.transactionAmount.copyWith(
                  color: isCredit ? AppColors.success : AppColors.textPrimary,
                ),
              ),
              const SizedBox(height: AppSpacing.xs),
              Text(
                dateFormat.format(date),
                style: AppTextStyles.bodySmall.copyWith(
                  color: Theme.of(context).colorScheme.onSurfaceVariant,
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  IconData _getCategoryIcon(String category) {
    switch (category.toLowerCase()) {
      case 'food':
      case 'nourriture':
        return Icons.restaurant_rounded;
      case 'transport':
        return Icons.directions_car_rounded;
      case 'shopping':
      case 'achats':
        return Icons.shopping_bag_rounded;
      case 'entertainment':
      case 'divertissement':
        return Icons.movie_rounded;
      case 'bills':
      case 'factures':
        return Icons.receipt_long_rounded;
      case 'health':
      case 'santé':
        return Icons.medical_services_rounded;
      case 'education':
        return Icons.school_rounded;
      default:
        return Icons.more_horiz_rounded;
    }
  }
}
