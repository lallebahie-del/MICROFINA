# Fonctionnalités non implémentées ou incomplètes — MICROFINA

Document de référence par rapport au cahier des charges type **SI intégré IMF** et à l’état du code (backend Spring Boot, frontend Angular).  
**Référence API détaillée :** [BACKEND-FONCTIONNALITES.md](./BACKEND-FONCTIONNALITES.md).

Les éléments ci-dessous complètent ce qui est **déjà en place** (membres, crédits, workflow, caisse, banque, épargne, garanties côté API, reporting, exports Excel/PDF/Word, Wallet Bankily, cartographie, admin utilisateurs/rôles/privilèges, audit, sauvegardes, monitoring, etc.).

---

## 1. Backend

### 1.1 Comptabilité — saisie manuelle des écritures

| Exigence type CDC | État |
|-------------------|------|
| Saisie / modification / suppression d’écritures comptables avec traçabilité | **Non implémenté** en API métier dédiée |

**Constat :** `ComptabiliteController` est documenté comme **lecture seule** ; les écritures sont attendues comme **générées par les modules fonctionnels**. Seul le **lettrage** (`PATCH .../lettrer`) est exposé.

**Piste :** module « pièces comptables » / OD manuelles (validation d’équilibre débit-crédit, numérotation, rattachement agence, audit).

---

### 1.2 Budget — lignes budgétaires et exécution (réalisé)

| Exigence type CDC | État |
|-------------------|------|
| Saisie du budget et ses modifications (ventilation recettes / dépenses) | **Partiel** |
| Mise à jour des recettes et dépenses (suivi d’exécution) | **Partiel** |

**Constat :** la couche service (`BudgetService`) prévoit des notions de **lignes** et **mouvements** budgétaires, mais `BudgetController` n’expose que la **tête** de budget : liste, détail, création, mise à jour des totaux, validation, clôture. **Aucun endpoint REST** public pour :

- ajouter / modifier / supprimer des **lignes** (recettes / dépenses) ;
- enregistrer les **mouvements** sur lignes (réalisé vs prévu).

---

### 1.3 Référentiels — administration (CRUD API)

Plusieurs référentiels sont exposés en **consultation seule** alors qu’un CDC « paramétrage » attend souvent une **maintenance applicative** sécurisée.

| Référentiel | API actuelle | Manque typique |
|-------------|--------------|----------------|
| Agences | GET (liste, détail, sièges) | POST / PUT / DELETE **non exposés** |
| Types de garantie | GET | POST / PUT / DELETE **non exposés** |
| Types de membre | GET (liste, éligibilité produits) | CRUD référentiel **non exposé** |
| Produits islamiques (`produit_islamic`) | Données en base / lien `ProduitCredit` | **Pas** de contrôleur REST dédié comme pour `produits-credit` |

---

### 1.4 Sécurité / organisation — groupes d’utilisateurs

| Exigence type CDC | État |
|-------------------|------|
| Groupes d’utilisateurs distincts des rôles | **Non identifié** dans le modèle exposé |

**Constat :** gestion **utilisateurs**, **rôles**, **privilèges** ; pas de couche **groupes** explicite dans les contrôleurs analysés.

---

### 1.5 Paramétrage avancé

| Exigence | État |
|----------|------|
| Paramétrage des **formulaires** métier (champs dynamiques, masques) | **Non implémenté** (modèle figé en code / BDD) |
| Configuration générique des **flux** entrants/sortants (connecteurs, files, mapping) | **Non implémenté** ; intégrations **ponctuelles** (ex. Bankily, exports) |

---

### 1.6 Interfaçage comptable / « Wallet » au sens ERP

| Exigence | État |
|----------|------|
| Interfaçage avec un **logiciel comptable tiers** au protocole du MOA | **Partiel** : exports (ex. SAGE), pas de bus d’échange générique documenté |

**Note terminologique :** dans le dépôt, **Wallet** correspond à **Bankily** (mobile money), pas nécessairement à un ERP nommé « Wallet » dans le CDC.

---

### 1.7 Traçabilité exhaustive

| Exigence | État |
|----------|------|
| Historisation / traçabilité de **toutes** les opérations sensibles sur la BDD | **Partiel** : journal d’audit admin existe ; **couverture par entité** à cartographier et compléter si besoin réglementaire |

---

### 1.8 Architecture technique (hors code applicatif)

| Exigence CDC | État |
|--------------|------|
| Architecture **distribuée**, **HA**, montée en charge multi-site | **À dimensionner en infrastructure** (déploiement, LB, BDD, PRA) |

---

## 2. Frontend

### 2.1 Budget

| Fonction | État |
|----------|------|
| Création / édition détaillée d’un budget | **Non** (écran principalement liste + valider + clôturer) |
| Gestion des **lignes** recettes / dépenses | **Non** (aligné sur l’absence d’API) |
| Saisie du **réalisé** budgétaire | **Non** |

---

### 2.2 Comptabilité

| Fonction | État |
|----------|------|
| Saisie manuelle d’écritures / OD | **Non** |
| Consultation + lettrage | **Oui** (partiel selon besoins métier) |

---

### 2.3 Parts sociales

| Fonction | État |
|----------|------|
| Accès menu + route | **Manquant** : composant / service présents dans le projet mais **pas** déclaré dans `app.routes.ts` ni dans le menu latéral |

---

### 2.4 Référentiels et paramètres système

| Fonction | État |
|----------|------|
| Écrans d’administration des agences (CRUD) | **Non** (liste consultative si l’API reste lecture seule) |
| Écrans types de garantie / types de membre / produits islamiques | **Non** ou très incomplet |
| Écran **paramètres** (`/api/v1/admin/parametres`) | **Non** dans le menu admin actuel |

---

### 2.5 Administration avancée

| Fonction | État |
|----------|------|
| Interface **jobs** planifiés / manuels (`/api/v1/admin/jobs`) | **Non** |
| Interface **clôture** mensuelle / annuelle (`/api/v1/admin/cloture`) | **Non** |

---

### 2.6 États financiers — consultation à l’écran

| État | Consultation dédiée | Export fichier |
|------|---------------------|----------------|
| Journal, grand livre, balance, bilan | **Oui** (routes comptabilité) | **Oui** |
| Compte de résultat | **Principalement via exports** | **Oui** |
| Tableau de financement | **Principalement via exports** | **Oui** |
| Balance âgée | **Principalement via exports** | **Oui** |

**Manque relatif au CDC :** vues **interactives** et tableaux de bord **paramétrables** pour tous les états (au-delà de la page reporting actuelle et des exports).

---

### 2.7 Aide à la décision « à la demande »

| Exigence | État |
|----------|------|
| Outil type **concepteur d’états** / requêtes métier paramétrables | **Non** (rapports et exports prédéfinis) |

---

### 2.8 Fichiers en cours d’intégration

Des éléments (ex. listes **comptes épargne**, **garanties**, **épargne**) peuvent exister en local mais **non suivis par Git** : les traiter comme **non stabilisés** jusqu’à intégration complète (routes, menu, droits, tests).

---

## 3. Synthèse des priorités suggérées

1. **Budget** : exposer lignes + mouvements en API, puis écrans associés.  
2. **Comptabilité** : trancher besoin d’**OD manuelles** ; si oui, API + UI + règles + audit.  
3. **Référentiels** : CRUD sécurisé + UI pour agences, types garantie / membre, produits islamiques, paramètres.  
4. **Frontend** : rattacher **parts sociales** ; pages admin **jobs** et **clôture**.  
5. **Reporting** : renforcer **consultation** CR, TDF, balance âgée si le CDC l’exige au-delà des exports.  
6. **Clarification MOA** : sens de « Wallet », périmètre **groupes**, et niveau de **traçabilité** obligatoire.

---

## 4. Tests de contrat liés à ce document

Des tests automatiques **reflètent l’état actuel des manques** documentés ci-dessus. Lorsqu’une fonctionnalité est livrée, **mettre à jour** le test correspondant et cette section pour éviter une dette de spécification.

### 4.1 Backend (Spring Boot)

| Fichier | Rôle |
|---------|------|
| [backend/src/test/java/com/pfe/backend/contract/DocumentedGapApiSurfaceTest.java](../backend/src/test/java/com/pfe/backend/contract/DocumentedGapApiSurfaceTest.java) | Vérifie l’absence d’API budget « lignes / mouvements », comptabilité en lecture seule (hors lettrage), référentiels agences / types garantie en lecture seule, absence de `ProduitIslamicController`. |

**Exécution** (à la racine du dépôt ou dans `backend/`) :

```bash
cd backend
./mvnw.cmd test
```

Sous Unix : `./mvnw test`.

Les règles métier budget (lignes, mouvements) sont aussi couvertes au niveau service dans [backend/src/test/java/com/microfina/service/BudgetServiceTest.java](../backend/src/test/java/com/microfina/service/BudgetServiceTest.java) — la **couche REST** reste en retard sur ce périmètre, d’où le test de contrat API.

### 4.2 Frontend (Angular + Vitest)

| Fichier | Rôle |
|---------|------|
| [frontend/src/app/documented-gaps.routes.spec.ts](../frontend/src/app/documented-gaps.routes.spec.ts) | Vérifie l’absence de routes pour parts sociales, admin jobs, admin clôture, admin paramètres ; présence des routes budgets / comptabilité. |
| [frontend/vitest.config.ts](../frontend/vitest.config.ts) | Vitest 4 : `maxWorkers` / `minWorkers` à **1** pour limiter les erreurs de workers sous Windows. |

**Exécution** :

```bash
cd frontend
npx ng test --watch=false
```

### 4.3 Build applicatif (hors tests)

```bash
cd backend && ./mvnw.cmd -q -DskipTests package
cd frontend && npm run build
```

---

*Document généré pour faciliter la planification et la recette ; le code source et [BACKEND-FONCTIONNALITES.md](./BACKEND-FONCTIONNALITES.md) font foi en cas d’évolution.*
