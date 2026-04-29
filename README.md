# MICROFINA++ — Système de Gestion de Micro-Finance

![Java](https://img.shields.io/badge/Java-21-007396?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-6DB33F?logo=springboot)
![Angular](https://img.shields.io/badge/Angular-17-DD0031?logo=angular)
![SQL Server](https://img.shields.io/badge/SQL%20Server-2019-CC2927?logo=microsoftsqlserver)
![Liquibase](https://img.shields.io/badge/Liquibase-4.x-2962FF)

> Plateforme de gestion de micro-finance conforme au cahier des charges BCM **DB-FINA-202112-001 R1.0.0** (Banque Centrale de Mauritanie). MICROFINA++ couvre l'intégralité du cycle de vie d'une institution de micro-finance : adhésion des membres, octroi et suivi des crédits, comptabilité générale, reporting réglementaire BCM, intégration wallet Bankily, cartographie des zones d'activité et administration système.

---

## Architecture

```
┌──────────────────────────────────────────────────────────┐
│                     Angular 17 SPA                        │
│        (standalone components · signals · lazy routes)    │
└────────────────────────┬─────────────────────────────────┘
                         │ HTTP/JSON  (proxy /api/v1)
┌────────────────────────▼─────────────────────────────────┐
│              Spring Boot 3.3.5  (Java 21)                 │
│   REST Controllers · Spring Security (JWT)                │
│   Spring Data JPA · Liquibase migrations                  │
│   Apache POI · iText · OpenPDF · Docx4j (exports)        │
└────────────────────────┬─────────────────────────────────┘
                         │ JDBC (jTDS / mssql-jdbc)
┌────────────────────────▼─────────────────────────────────┐
│            SQL Server 2019  (collation French_CI_AS)      │
└──────────────────────────────────────────────────────────┘
```

### Double espace de noms

Le projet maintient deux espaces de noms Java distincts afin de séparer clairement le domaine métier de la couche applicative :

| Package | Rôle |
|---|---|
| `com.microfina.*` | Entités JPA, DTOs métier, interfaces repository — domaine pur |
| `com.pfe.backend.*` | Controllers REST, services applicatifs, configuration Spring, sécurité |

Cette séparation facilite la réutilisation des entités dans d'autres contextes (batch, tests d'intégration) sans coupler le domaine au framework.

---

## Prérequis

| Composant | Version minimale |
|---|---|
| JDK | 21 (LTS) |
| Maven | 3.9+ |
| Node.js | 20 LTS |
| npm | 10+ |
| SQL Server | 2019 (Express ou supérieur) |
| Système d'exploitation | Windows Server 2019 / Ubuntu 22.04 |

---

## Installation rapide

### 1. Base de données

```sql
-- Créer la base (collation obligatoire)
CREATE DATABASE microfina
  COLLATE French_CI_AS;

-- Créer un compte applicatif dédié
CREATE LOGIN microfina_app WITH PASSWORD = 'MotDePasse@Securise1';
USE microfina;
CREATE USER microfina_app FOR LOGIN microfina_app;
ALTER ROLE db_owner ADD MEMBER microfina_app;
```

### 2. Backend (Spring Boot)

```bash
cd backend

# Copier et adapter le fichier de configuration
cp src/main/resources/application.properties.example \
   src/main/resources/application.properties

# Éditer application.properties :
#   spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=microfina
#   spring.datasource.username=microfina_app
#   spring.datasource.password=MotDePasse@Securise1
#   app.jwt.secret=<chaine-aleatoire-256-bits>
#   app.backup.dir=C:/microfina/backups

# Lancer (Liquibase applique les migrations automatiquement)
mvn spring-boot:run
```

Le backend démarre sur `http://localhost:8080`.

### 3. Frontend (Angular)

```bash
cd frontend
npm install
npm start          # http://localhost:4200  (proxy → :8080)
```

### 4. Build de production

```bash
# Backend
cd backend
mvn package -DskipTests
java -jar target/backend-*.jar

# Frontend
cd frontend
npm run build      # dist/ à servir via Nginx
```

---

## Documentation API (Swagger / OpenAPI)

| URL | Description |
|---|---|
| `http://localhost:8080/swagger-ui.html` | Interface Swagger UI interactive |
| `http://localhost:8080/v3/api-docs` | Spécification OpenAPI 3.0 (JSON) |
| `http://localhost:8080/v3/api-docs.yaml` | Spécification OpenAPI 3.0 (YAML) |

Chemin de base de toutes les routes REST : `/api/v1/`

---

## Modules et phases de développement

| Phase | Module | Description |
|---|---|---|
| 1 | Membres (PP/PM) | Inscription, validation, gestion des personnes physiques et morales |
| 2 | Produits crédit | Référentiel des produits (taux, durées, garanties) |
| 3 | Crédits | Octroi, déblocage, remboursement, tableau d'amortissement |
| 4 | Comptabilité | Plan comptable, écritures, grand livre, balance |
| 5 | Agences & agents | Réseau d'agences, affectation des agents crédit |
| 6 | Garanties | Cautions, hypothèques, nantissements |
| 7 | Épargne | Comptes d'épargne, dépôts, retraits |
| 8 | Tiers & fournisseurs | Référentiel tiers, opérations diverses |
| 9 | Paramétrages | Configuration système, tables de référence |
| 10 | Reporting BCM | Tableaux réglementaires, wallet Bankily, cartographie GeoJSON |
| 11 | Banque & Administration | Banques partenaires, budgets, simulateur crédit, administration système |

---

## Sécurité

MICROFINA++ utilise **JWT (JSON Web Token)** pour l'authentification sans état :

- Les tokens sont signés avec HMAC-SHA256 (secret configurable dans `application.properties`).
- Durée de validité : **8 heures** (configurable via `app.jwt.expiration`).
- Les autorités sont préfixées `PRIV_` (ex. `PRIV_CREDIT_OCTROI`, `PRIV_ADMIN_BACKUP`).
- Chaque endpoint REST est sécurisé par `@PreAuthorize("hasAuthority('PRIV_XXX')")`.
- Les mots de passe sont hachés avec **BCrypt** (coût 12).
- Un journal d'audit (`JournalAudit`) enregistre toutes les opérations sensibles (CREATE, UPDATE, DELETE, LOGIN, LOGOUT).

### Modèle RBAC

```
Utilisateur → 1..N Rôles → 1..N Privilèges (PRIV_*)
```

---

## Module Export

MICROFINA++ propose des exports multi-formats depuis les contrôleurs REST dédiés (`/api/v1/exports/`) :

| Format | Bibliothèque | Endpoint exemple |
|---|---|---|
| Excel (.xlsx) | Apache POI 5.x | `GET /api/v1/exports/membres/excel` |
| Word (.docx) | Docx4j / Apache POI XWPF | `GET /api/v1/exports/credits/word` |
| PDF | iText / OpenPDF | `GET /api/v1/exports/reporting/pdf` |

Les exports respectent la charte graphique BCM (en-têtes, pied de page, logo).

---

## Tests

```bash
# Tests unitaires et d'intégration backend
cd backend
mvn test

# Rapport de couverture (JaCoCo)
mvn verify
# Rapport généré dans : target/site/jacoco/index.html

# Tests unitaires frontend (Karma + Jasmine)
cd frontend
npm test

# Lint Angular
npm run lint
```

---

## Variables d'environnement clés (application.properties)

| Propriété | Description | Exemple |
|---|---|---|
| `spring.datasource.url` | URL JDBC SQL Server | `jdbc:sqlserver://localhost:1433;databaseName=microfina` |
| `spring.datasource.username` | Compte SQL Server | `microfina_app` |
| `spring.datasource.password` | Mot de passe SQL Server | `***` |
| `app.jwt.secret` | Secret HMAC-SHA256 (min. 256 bits) | `<random-base64>` |
| `app.jwt.expiration` | Durée token en ms | `28800000` (8h) |
| `app.backup.dir` | Répertoire des sauvegardes .bak | `C:/microfina/backups` |
| `spring.liquibase.change-log` | Chemin du changelog Liquibase | `classpath:db/changelog/db.changelog-master.xml` |

---

## Conformité réglementaire

Le système est conçu en conformité avec le cahier des charges **BCM DB-FINA-202112-001 R1.0.0**. Voir [CONFORMITE-CAHIER.md](CONFORMITE-CAHIER.md) pour la matrice complète de conformité.

---

## Licence

Projet académique — Mémoire de fin d'études (PFE). Tous droits réservés.
