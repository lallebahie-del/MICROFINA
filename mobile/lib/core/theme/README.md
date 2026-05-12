# 🎨 Premium Design System - Microfina

Un système de design moderne et premium pour l'application mobile Microfina, construit avec Material 3 et les meilleures pratiques Flutter.

## 📁 Structure

```
lib/core/theme/
├── app_colors.dart          # Palette de couleurs complète
├── app_text_styles.dart     # Typographie et styles de texte
├── app_spacing.dart         # Espacement et dimensions
├── app_shadows.dart         # Ombres et effets visuels
├── app_theme.dart           # Thème Material 3 complet
└── README.md              # Documentation

lib/core/widgets/
├── premium_button.dart      # Boutons premium
├── premium_card.dart        # Cartes modernes
├── premium_input.dart       # Champs de saisie
├── premium_app_bar.dart     # Barres d'application
├── premium_bottom_nav.dart  # Navigation inférieure
├── premium_loading.dart     # Indicateurs de chargement
└── premium_charts.dart     # Graphiques premium
```

## 🎨 Couleurs

### Palette Principale
- **Primary**: `#1E293B` (Slate 800) - Pro & Moderne
- **Secondary**: `#3B82F6` (Blue 500) - Vibrant & Interactif
- **Success**: `#10B981` (Emerald 500) - Validation & Succès
- **Warning**: `#F59E0B` (Amber 500) - Alertes & Attention
- **Error**: `#EF4444` (Red 500) - Erreurs & Danger

### Couleurs de Surface
- **Surface**: `#FFFFFF` (White) - Fond principal
- **Background**: `#F8FAFC` (Slate 50) - Arrière-plan
- **Border**: `#E2E8F0` (Slate 200) - Contours

### Mode Sombre
- **Dark Primary**: `#0F172A` (Slate 900)
- **Dark Surface**: `#1E293B` (Slate 800)
- **Dark Background**: `#0F172A` (Slate 900)

## 📝 Typographie

### Hiérarchie Typographique
- **Display Large**: 57px, FontWeight.w800
- **Headline Large**: 32px, FontWeight.w700
- **Title Large**: 22px, FontWeight.w600
- **Body Large**: 16px, FontWeight.w400
- **Label Large**: 14px, FontWeight.w500

### Styles Spécialisés
- **Hero Title**: 32px, FontWeight.w900, LetterSpacing: -1.0
- **Card Title**: 18px, FontWeight.w700, LetterSpacing: -0.2
- **Balance Amount**: 32px, FontWeight.w800, LetterSpacing: -0.5
- **Button Text**: 16px, FontWeight.w700, LetterSpacing: 0.2

## 📏 Espacement

### Échelle d'Espacement
- **xs**: 4px
- **sm**: 8px
- **md**: 16px
- **lg**: 24px
- **xl**: 32px
- **xxl**: 48px
- **xxxl**: 64px

### Bordures
- **Small**: 4px
- **Medium**: 8px
- **Large**: 16px
- **XLarge**: 20px
- **XXLarge**: 24px
- **XXXLarge**: 32px

## 🎭 Ombres

### Types d'Ombres
- **Soft**: Ombre légère pour cartes simples
- **Premium**: Ombre colorée pour éléments premium
- **Card**: Ombre optimisée pour cartes
- **Floating**: Ombre élevée pour éléments flottants
- **Glow**: Effet de lueur pour éléments interactifs

## 🧩 Composants Premium

### PremiumButton
```dart
PremiumButton(
  text: 'Action Principale',
  type: PremiumButtonType.primary,
  size: PremiumButtonSize.large,
  onPressed: () => print('Action!'),
)
```

**Types**: `primary`, `secondary`, `outline`, `ghost`, `success`, `warning`, `error`
**Tailles**: `small`, `medium`, `large`

### PremiumCard
```dart
PremiumCard(
  type: PremiumCardType.elevated,
  size: PremiumCardSize.medium,
  child: Text('Contenu de la carte'),
)
```

**Types**: `standard`, `elevated`, `outlined`, `glass`, `gradient`
**Tailles**: `small`, `medium`, `large`

### PremiumInput
```dart
PremiumInput(
  label: 'Email',
  hint: 'Entrez votre email',
  type: PremiumInputType.email,
  prefixIcon: Icon(Icons.email),
)
```

**Types**: `text`, `email`, `phone`, `password`, `number`, `search`, `multiline`
**Tailles**: `small`, `medium`, `large`

### PremiumAppBar
```dart
PremiumAppBar(
  title: 'Microfina',
  type: PremiumAppBarType.transparent,
  actions: [
    NotificationIconButton(showBadge: true),
  ],
)
```

**Types**: `standard`, `transparent`, `glass`, `gradient`, `floating`

### PremiumBottomNav
```dart
PremiumBottomNav(
  currentIndex: 0,
  onTap: (index) => print('Navigation: $index'),
  items: [
    PremiumNavItem(icon: Icons.home, label: 'Accueil'),
    PremiumNavItem(icon: Icons.person, label: 'Profil'),
  ],
  isFloating: true,
)
```

## 📊 Graphiques Premium

### PremiumPieChart
```dart
PremiumPieChart(
  data: [
    PieChartData(value: 1000, label: 'Épargne', color: AppColors.success),
    PieChartData(value: 500, label: 'Prêts', color: AppColors.warning),
  ],
  centerText: 'TOTAL',
)
```

### PremiumLineChart
```dart
PremiumLineChart(
  data: [
    LineChartData(spots: [
      FlSpot(0, 100),
      FlSpot(1, 150),
      FlSpot(2, 120),
    ]),
  ],
  xLabels: ['Jan', 'Fév', 'Mar'],
)
```

### PremiumBarChart
```dart
PremiumBarChart(
  data: [
    BarChartData(value: 1000, color: AppColors.success),
    BarChartData(value: 800, color: AppColors.secondary),
  ],
  xLabels: ['Mois 1', 'Mois 2'],
)
```

## ⚡ Chargement & États

### PremiumLoading
```dart
PremiumLoading(
  type: PremiumLoadingType.spinner,
  message: 'Chargement...',
  isOverlay: true,
)
```

**Types**: `spinner`, `dots`, `pulse`, `shimmer`, `lottie`

### PremiumSkeleton
```dart
PremiumSkeleton(
  width: double.infinity,
  height: 20,
)
```

### PremiumSkeletonCard
```dart
PremiumSkeletonCard(
  isLoading: true,
  child: ActualContent(),
)
```

## 🎭 Animations & Transitions

### AnimationHelper
```dart
// Transitions de page
Navigator.push(
  context,
  AnimationHelper.slideTransition(child: NextPage()),
);

// Animations de widgets
AnimationHelper.slideInFromBottom(
  child: Card(),
  duration: Duration(milliseconds: 300),
);
```

### Widgets Animés
- **AnimatedCounter**: Compteur animé
- **PulseWidget**: Effet de pulsation
- **StaggeredAnimation**: Animation séquentielle

## 🚀 Performance

### PerformanceOptimizer
```dart
// Optimisation au démarrage
PerformanceOptimizer.optimizeApp();

// Préchargement d'images
PerformanceOptimizer.preloadImages(context, imagePaths);

// ListView optimisé
PerformanceOptimizer.optimizedListView(
  itemCount: items.length,
  itemBuilder: (context, index) => ItemWidget(items[index]),
);
```

### Lazy Loading
```dart
LazyLoadBuilder(
  builder: (context) => ExpensiveWidget(),
  placeholder: LoadingWidget(),
)
```

## 🌙 Thème Sombre

Le design system supporte complètement le thème sombre avec :

- Adaptation automatique des couleurs
- Ombres optimisées pour le mode sombre
- Contrastes respectés pour l'accessibilité
- Transitions fluides entre thèmes

## 🎯 Bonnes Pratiques

### 1. Utilisation des Couleurs
```dart
// ✅ Bon
Theme.of(context).colorScheme.primary
AppColors.success
Theme.of(context).extension<AppColorExtensions>()?.success

// ❌ Éviter
Colors.blue
Color(0xFF3B82F6)
```

### 2. Utilisation des Styles
```dart
// ✅ Bon
AppTextStyles.cardTitle
Theme.of(context).textTheme.titleLarge

// ❌ Éviter
TextStyle(fontSize: 18, fontWeight: FontWeight.bold)
```

### 3. Espacement
```dart
// ✅ Bon
AppSpacing.md
EdgeInsets.all(AppSpacing.lg)

// ❌ Éviter
EdgeInsets.all(16.0)
EdgeInsets.all(24.0)
```

## 🔄 Migration

Pour migrer les composants existants :

1. **Remplacer les couleurs** par `AppColors.*`
2. **Utiliser les styles** de `AppTextStyles.*`
3. **Appliquer l'espacement** avec `AppSpacing.*`
4. **Utiliser les composants premium** au lieu des widgets standards
5. **Ajouter les animations** avec `AnimationHelper`

## 📱 Accessibilité

Le design system intègre :

- Contrastes WCAG AA minimum
- Tailles de police adaptatives
- Zones tactiles suffisantes (44px minimum)
- Support des lecteurs d'écran
- Navigation au clavier

## 🎨 Personnalisation

### Extension du Thème
```dart
Theme(
  data: Theme.of(context).copyWith(
    extensions: [
      AppColorExtensions(
        primary: customColor,
      ),
    ],
  ),
  child: MyApp(),
)
```

### Personnalisation des Composants
Les composants premium acceptent des paramètres de personnalisation :

- `backgroundColor`: Couleur de fond personnalisée
- `customColor`: Couleur principale personnalisée
- `customShadow`: Ombre personnalisée
- `customWidth/Height`: Dimensions personnalisées

## 📚 Ressources

- [Material 3 Design System](https://m3.material.io/)
- [Flutter Theming Guide](https://flutter.dev/docs/cookbook/design/themes)
- [Accessibility Guidelines](https://flutter.dev/docs/development/accessibility-and-localization/accessibility)

---

**Créé avec ❤️ pour Microfina - Design System Premium v1.0**
