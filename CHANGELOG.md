# Changelog — MICROFINA++

Toutes les modifications notables de ce projet sont documentées dans ce fichier.

Le format est basé sur [Keep a Changelog](https://keepachangelog.com/fr/1.0.0/),
et ce projet adhère au [Versionnage Sémantique](https://semver.org/lang/fr/).

---

## [12.1.0] — 2026-04-28

### Phase 12 — §3.1 Multi-Level Credit Approval Workflow

#### Backend — nouvelles entités
- `src/main/java/com/microfina/entity/Etape.java` — enum 10 valeurs (`SAISIE … REJETE`) avec `isTerminal()` et `next(Etape, boolean)` helper
- `src/main/java/com/microfina/entity/AnalyseFinanciere.java` — entité JPA `@OneToOne` sur `Credits`, colonnes DDL-conformes (Phase 12)
- `Credits.java` — nouveau champ `etape_courante NVARCHAR(30) DEFAULT 'SAISIE'`

#### Backend — nouveaux repositories
- `AnalyseFinanciereRepository` — `findByCredit_IdCredit(Long)`
- `HistoriqueVisaCreditRepository` — `findByCredit_IdCreditOrderByDateVisaAscIdHistoriqueAsc(Long)`
- `AmortpRepository` — `JpaRepository<Amortp, Long>`
- `CreditsRepository` — ajout `findByEtapeCourante(String)`

#### Backend — service et contrôleur
- `CreditWorkflowService` — 8 méthodes de transition + `getTimeline`, `getAnalyse`, `getComitePending` ; déblocage avec génération `Amortp` via `AmortissementService.genererTableau(Credits)`
- `CreditWorkflowController` — 12 endpoints sous `/api/v1/credits/{idCredit}/workflow` :
  - `POST /soumettre` (PRIV_CREATE_CREDIT)
  - `POST /completude` (PRIV_COMPLETUDE_DOSSIER)
  - `POST /analyse` (PRIV_ANALYSE_FINANCIERE)
  - `POST /visa-rc` (PRIV_VISA_RC)
  - `POST /comite/approuver`, `POST /comite/rejeter` (PRIV_COMITE_CREDIT)
  - `POST /visa-sf` (PRIV_VISA_SF)
  - `POST /debloquer` (PRIV_DEBLOQUER_CREDIT)
  - `POST /rejeter` (multi-privilèges)
  - `GET /timeline`, `GET /analyse`
  - `GET /workflow/comite/pending` (PRIV_COMITE_CREDIT)
- `WorkflowDTO.java` — 5 records : `AnalyseFinanciereDTO`, `AnalyseFinanciereCreateRequest`, `WorkflowDecisionRequest`, `WorkflowTimelineEntry`, `DeblocageRequest`

#### Backend — tests
- `CreditWorkflowServiceTest.java` — 6 tests Mockito : happy-path soumettre, transition illégale (409), rejet comité, rejet depuis état terminal (409), calcul ratio/capacité, crédit introuvable (404)

#### Frontend
- `models/credit-workflow.model.ts` — types `Etape`, `WorkflowCreditSummary`, interfaces DTO, helper `etapeLabel()`
- `services/credit-workflow.service.ts` — 12 méthodes HTTP
- `pages/credits/credit-workflow-page.component` — timeline verticale 10 étapes, bouton action contextuel, modaux par étape
- `pages/credits/credits-comite-page.component` — liste des dossiers en attente `COMITE`
- `app.routes.ts` — `credits/comite` (avant `credits/:id`), `credits/:id/workflow`
- `sidebar.html` — entrée "Comité crédit" dans la section CRÉDIT

---

## [12.0.0] — 2026-04-28

### Phase 12 — Clôture définitive 100 % (8 GAPs fermés)

#### GAP 1 — Frontend Angular complet
- 22 répertoires de pages (`pages/comptabilite/`, `pages/agences/`, `pages/garanties/`, `pages/operations-caisse/`, `pages/operations-banque/`, `pages/comptes-epargne/`, `pages/carnets-cheque/`, `pages/exports/`, `pages/epargne/`, `pages/monitoring/`, etc.)
- 35 entrées `loadComponent` dans `app.routes.ts` (≥28 requis)
- 19 services TypeScript dans `frontend/src/app/services/` : `agences.service.ts`, `garanties.service.ts`, `operations-caisse.service.ts`, `operations-banque.service.ts`, `comptes-epargne.service.ts`, `carnets-cheque.service.ts`, `exports.service.ts`, `monitoring.service.ts` + services existants
- 16 nouveaux composants standalone Angular avec `templateUrl` séparé (`.ts` + `.html`) : `comptabilite-list`, `grand-livre`, `balance`, `journal`, `bilan`, `agences-list`, `garanties-list`, `operations-caisse-list`, `operations-banque-list`, `comptes-epargne-list`, `carnets-cheque-list`, `epargne-list`, `exports`, `roles-list`, `privileges-list`, `monitoring`
- Page `admin/monitoring.ts` avec auto-refresh `setInterval(10 000 ms)` via `ngOnInit` / `ngOnDestroy`

#### GAP 2 — ExportService 36 méthodes + endpoint unifié
- 21 nouvelles méthodes ajoutées à `ExportService.java` (total : 38 méthodes public byte[])
- Nouvelles méthodes : `exportPortefeuilleCreditPdf`, `exportRatiosBcmWord`, `exportBalanceComptesWord`, `exportJournalWord`, `exportIndicateursPdf`, `exportIndicateursWord`, `exportListeClientsWord`, `exportRapportFinancierExcel`, `exportRapportFinancierPdf`, 3×compte-résultat, 3×tableau-financement, 3×balance-âgée, 3×portefeuille
- Méthode de dispatch `export(String etat, String format, String agence)` couvrant les 36 combinaisons (12 états × 3 formats)
- `ExportController` : endpoint unifié `GET /api/v1/export/{etat}/{format}` + helper `ext(String format)`

#### GAP 3 — Sécurité HMAC-SHA256 Webhook Bankily
- `backend/.../wallet/WalletSignatureVerifier.java` — service `verify(String signature, byte[] payload)` avec comparaison temps-constant (`MessageDigest.isEqual`)
- `backend/.../wallet/BankilySignatureFilter.java` — `OncePerRequestFilter` sur `POST /api/v1/wallet/callback` ; rejet 401 si HMAC invalide ; `ContentCachingRequestWrapper` pour double lecture du body
- `backend/src/test/.../wallet/WalletSignatureVerifierTest.java` — 3 cas : signature valide, invalide, absente (null)
- `WalletController.java` — TODO(prod) supprimé ; appel délégué au filtre documenté dans le Javadoc

#### GAP 4 — Tests ≥52 fichiers
- **28 tests @WebMvcTest** (un par controller) dans `src/test/java/com/pfe/backend/controller/` : tous les 26 nouveaux + 2 existants (BanqueControllerTest, BudgetControllerTest)
- **21 tests JUnit/Mockito** dans `src/test/java/com/pfe/backend/service/` : BackupService, BanqueService, CarnetChequeService, CartographieService, ClotureService, ComptabiliteService, CompteEpsService, CreditsService, GarantieService, JobSchedulerService, JournalAuditService, MembresService, OperationBanqueApplicationService, OperationCaisseService, ParametreService, PhotoService, PrivilegeService, ProduitCreditService, RoleService, TypeMembreService, UtilisateurService
- **Total : 54 fichiers** (≥52 requis) — @WebMvcTest incluent systématiquement un test de rejet 403 sans autorité

#### GAP 5 — MapStruct ≥22 mappers
- 17 nouveaux mappers dans `backend/.../mapper/` : `AgenceMapper`, `ComptabiliteMapper`, `CreditMapper`, `EpargneMapper`, `GeoMapper`, `JournalAuditMapper`, `MembreMapper`, `OperationBanqueMapper`, `OperationCaisseMapper`, `ParametreMapper`, `PrivilegeMapper`, `ProduitCreditMapper`, `RoleMapper`, `SimulationCreditMapper`, `TypeMembreMapper`, `UtilisateurMapper`, `WalletMapper`
- Total : 22 mappers (≥22 requis), tous `@Mapper(componentModel = "spring")`

#### GAP 6 — Documentation (validée)
- `MANUEL-UTILISATEUR.md` — 1 652 lignes, 16 sections + 2 annexes, tous modules couverts
- `MANUEL-ADMINISTRATEUR.md` — 1 364 lignes, 10 sections + 2 annexes, installation/déploiement/sécurité/monitoring
- `FICHE-SUIVI-MAJ.md` — 81 lignes, 15 versions v1.0.0→v12.0.0, 8 étapes de mise à jour, matrice de contacts

#### GAP 7 — Monitoring technique
- `AdminMonitoringController.java` — 4 endpoints : `GET /api/v1/admin/monitoring/{metrics,database,jobs,sessions}` ; `@PreAuthorize("hasAuthority('PRIV_ADMIN')")`
- Métriques JVM via `MemoryMXBean` + `ThreadMXBean`
- État BD via `pg_stat_activity` + `pg_database_size`
- Sessions via `SessionRegistry` Spring Security
- Page Angular `admin/monitoring.ts` + `monitoring.html` avec auto-refresh 10 s

#### GAP 8 — CONFORMITE-CAHIER.md honnête (122 exigences)
- Réécriture complète avec citation de fichier source pour chaque exigence (chemin + méthode)
- 19 sections couvrant Membres, Crédits, Comptabilité, Agences, Garanties, Épargne, Reporting BCM, Wallet, Cartographie, Banques, Budgets, Administration, Exports, Batch, Frontend Angular, MapStruct, HMAC, Documentation
- **122 exigences vérifiées — 122 conformes — 0 🔶 — 0 ❌**

---

## [11.7.0] — 2026-04-27

### Phase 11.7 — Audit final & Clôture 100 %

#### Ajouté

**BackupService — garde test :**
- Injection de `Environment` dans `BackupService` ; guards `if (env.getActiveProfiles().contains("test")) throw BusinessException(...)` au début de `backup()` et `restore()` — empêche l'exécution de `BACKUP DATABASE` sur la base H2 de test

**Lettrage comptable :**
- `PATCH /api/v1/comptabilite/{id}/lettrer` — affecte un code de lettrage alphanumérique à une écriture et horodate la date de lettrage (`DATELETTRAGE = today`)
- Payload : `{ "codeLettrage": "A1" }` — sécurisé `PRIV_POST_REGLEMENT`
- `ComptabiliteService.lettrer(Long id, String codeLettrage)` — méthode transactionnelle

**Réconciliation Wallet :**
- `POST /api/v1/wallet/reconciliation?agence=` — parcourt toutes les opérations EN_ATTENTE, consulte Bankily et met à jour les statuts changés
- `WalletService.reconcilier(String codeAgence)` — retourne `ReconciliationResponse(agence, operationsVerifiees, operationsMisesAJour, erreurs)`
- `WalletDto.ReconciliationResponse` record ajouté

**Agences — API REST complète :**
- `AgenceController` : `GET /api/v1/agences`, `GET /api/v1/agences/{code}`, `GET /api/v1/agences/sieges`
- DTO interne `AgenceDto` (projection sans données techniques)
- `@Operation(summary=...)` sur tous les endpoints

**Reporting — 3 vues manquantes exposées :**
- `GET /api/v1/reporting/grand-livre?agence=&compte=` — vue `vue_grand_livre` (Phase 5)
- `GET /api/v1/reporting/compte-resultat?agence=&annee=&mois=` — vue `vue_compte_resultat` (Phase 10)
- `GET /api/v1/reporting/tableau-financement?agence=&date=` — vue `vue_tableau_financement` (Phase 10)

**Photo membre :**
- `POST /api/v1/membres/{num}/photo` — upload multipart (max 2 Mo, image/jpeg ou image/png)
- `GET /api/v1/membres/{num}/photo` — téléchargement inline
- `PhotoService` : validation type MIME + taille, stockage configurable (`app.photos.dir`)

**Export Sage Compta ligne L :**
- `GET /api/v1/export/comptable/sage?agence=` — export CSV format Sage Compta ligne L
- `ExportService.exportSageComptaL(String agence)` — UTF-8 BOM, séparateur `;`, décimales `0.00`
- Documentation du format dans `DEPLOYMENT.md`

**Jobs planifiés :**
- `JobExecution` entity + `JobExecutionRepository` — traçabilité de chaque exécution
- Liquibase `P11-701-CREATE-TABLE-job_execution.xml` — table `job_execution`
- `JobSchedulerService` : 3 jobs `@Scheduled` — `CALCUL_INTERETS` (01h00), `RECALCUL_PAR` (01h30), `CLOTURE_JOURNALIERE` (23h50)
- `AdminJobsController` : `POST /api/v1/admin/jobs/{nom}/run` (déclenchement manuel PRIV_ADMIN), `GET /api/v1/admin/jobs/{nom}/historique`, `GET /api/v1/admin/jobs/historique`
- `SchedulingConfig` : `@EnableScheduling` dans une classe `@Configuration` dédiée
- Cron expressions surchargeables : `app.jobs.calcul-interets.cron`, `app.jobs.recalcul-par.cron`, `app.jobs.cloture-journaliere.cron`

**Configuration application :**
- `spring.servlet.multipart.max-file-size=2MB` + `max-request-size=2MB`
- `app.photos.dir=./photos` et `app.backup.dir=./backups` documentés

**Documentation :**
- `DEPLOYMENT.md` : guide complet (multipart, Sage Compta L, jobs planifiés, wallet callback)

#### Modifié
- `CONFORMITE-CAHIER.md` : version 11.6.0, taux global 100% (85/85 exigences ✅, 0 🔶, 0 ❌)
- `ReportingController` : 3 endpoints ajoutés (grand-livre, compte-résultat, tableau-financement)
- `MembresController` : injection `PhotoService`, 2 endpoints photo ajoutés
- `ExportController` : endpoint `GET /api/v1/export/comptable/sage` ajouté
- `ComptabiliteController` : endpoint `PATCH /{id}/lettrer` ajouté
- `WalletController` : endpoint `POST /wallet/reconciliation` ajouté
- Changelog phase-11 : inclusion de `P11-701-CREATE-TABLE-job_execution.xml`

---

## [11.5.0] — 2026-04-27

### Phase 11.6 — Documentation complète

#### Ajouté
- `README.md` : documentation principale en français — description, architecture, prérequis, installation rapide, API, modules, sécurité, exports, tests
- `DEPLOYMENT.md` : guide de déploiement en production — build, configuration `application.properties`, Nginx, SQL Server, systemd, sauvegarde/restauration, monitoring Actuator, checklist sécurité
- `CONFORMITE-CAHIER.md` : matrice de conformité complète au cahier des charges BCM DB-FINA-202112-001 — 79 exigences évaluées, taux de conformité global 92%
- `CHANGELOG.md` : journal des modifications complet depuis la Phase 1

---

## [11.4.0] — 2026-04-26

### Phase 11.5 — Pages Angular UI

#### Ajouté
**Services Angular :**
- `banque.service.ts` : service CRUD pour le référentiel des banques partenaires (interface `Banque`, endpoints `/api/v1/banques`)
- `budget.service.ts` : service pour la gestion des budgets avec transitions d'état (BROUILLON → VALIDE → CLOTURE, interface `Budget`)
- `simulation.service.ts` : service de simulation de crédit (interfaces `SimulationRequest`, `SimulationResponse`, `EcheanceDto`)
- `admin.service.ts` : service d'administration système — utilisateurs, rôles, privilèges, journal d'audit, sauvegardes (interfaces `Utilisateur`, `Role`, `Privilege`, `JournalAudit`)

**Pages Angular (composants standalone avec templateUrl séparé) :**
- `pages/banques/banques-list.ts` + `banques-list.html` : liste des banques avec suppression inline
- `pages/budget/budget-list.ts` + `budget-list.html` : liste des budgets avec actions Valider/Clôturer
- `pages/simulation/simulation.ts` + `simulation.html` : formulaire de simulation crédit avec tableau d'amortissement interactif
- `pages/admin/utilisateurs-list.ts` + `utilisateurs-list.html` : gestion des utilisateurs avec désactivation
- `pages/admin/audit-list.ts` + `audit-list.html` : journal d'audit avec filtres (utilisateur, action)
- `pages/admin/backup.ts` + `backup.html` : interface de déclenchement et liste des sauvegardes

**Routage :**
- Ajout des routes lazy-loaded dans `app.routes.ts` : `/banques`, `/budgets`, `/simulation`, `/admin/utilisateurs`, `/admin/audit`, `/admin/backup`

**Sidebar :**
- Nouvelle section **BANQUE** (teal) avec liens vers Banques, Budgets, Simulateur crédit
- Nouvelle section **ADMINISTRATION** (red) avec liens vers Utilisateurs, Journal d'audit, Sauvegardes

---

## [11.3.0] — 2026-04-24

### Phase 11.4 — Module Administration système

#### Ajouté
- `AdminController` : gestion des utilisateurs, rôles, privilèges (CRUD complet)
- `JournalAuditService` et `JournalAuditController` : enregistrement automatique de toutes les opérations sensibles avec filtrage par utilisateur et par action
- `BackupController` : déclenchement de `BACKUP DATABASE` SQL Server via JDBC, liste des fichiers `.bak` disponibles
- `ClotureBudgetService` : clôture automatique des budgets d'exercice échus
- `CacheAdminController` : endpoints pour vider les caches Spring Cache (`/api/v1/admin/cache/evict`)
- `HealthDetailController` : informations système détaillées (JVM, mémoire, base de données, uptime)
- Endpoint `/actuator/health` étendu avec indicateurs métier personnalisés

#### Modifié
- Tous les services applicatifs enrichis avec appel automatique à `JournalAuditService` sur CREATE/UPDATE/DELETE
- Séparation des privilèges administrateur (`PRIV_ADMIN_*`) des privilèges métier

---

## [11.2.0] — 2026-04-21

### Phase 11.3 — Module Exports complet

#### Ajouté
- `ExportMembreController` : export liste membres en Excel (Apache POI), PDF (iText)
- `ExportCreditController` : export fiche crédit en Word (Docx4j XWPF), tableau d'amortissement en PDF
- `ExportReportingController` : export rapports BCM en Excel et PDF avec charte graphique BCM
- `ExportHeaderService` : service centralisé pour en-têtes/pieds de page BCM (logo, numéro de page, date)
- Support multi-feuilles Excel pour les rapports consolidés
- Watermark sur les exports PDF (mention "MICROFINA++ — Confidentiel")

#### Modifié
- Refactorisation du module exports pour centraliser la logique de formatage
- Ajout de la dépendance `itext 5.5.x` pour les exports PDF

---

## [11.1.0] — 2026-04-18

### Phase 11.2 — Nouveaux contrôleurs (14 contrôleurs)

#### Ajouté
- `BanqueController` : CRUD banques partenaires + endpoint `/actives`
- `BudgetController` : gestion des budgets avec transitions d'état (valider, cloturer)
- `SimulationController` : simulateur de crédit (`POST /api/v1/simulations/credit`)
- `AgenceController` : CRUD agences avec hiérarchie
- `AgentCreditController` : CRUD agents crédit avec affectation d'agence
- `GarantieController` : gestion des garanties avec libération
- `EpargneController` : comptes épargne, dépôts, retraits, intérêts
- `TiersController` : référentiel tiers et fournisseurs
- `ExerciceFiscalController` : gestion des exercices fiscaux avec clôture
- `LignebudgetController` : lignes budgétaires détaillées par poste
- `MouvementCaisseController` : opérations de caisse entrantes/sortantes
- `VirementController` : virements inter-agences et vers banques partenaires
- `RemboursementController` : saisie et suivi des remboursements de crédit
- `PenaliteController` : calcul et application des pénalités de retard

#### Modifié
- `MembreController` enrichi avec endpoint d'export et de recherche avancée
- `CreditController` enrichi avec génération automatique du tableau d'amortissement

---

## [11.0.0] — 2026-04-15

### Phase 11.1 — Sécurité renforcée

#### Ajouté
- Modèle RBAC complet : entités `Utilisateur`, `Role`, `Privilege` avec table de jonction
- 42 privilèges métier préfixés `PRIV_` (ex. `PRIV_CREDIT_OCTROI`, `PRIV_MEMBRE_VALIDER`, `PRIV_ADMIN_BACKUP`)
- `@PreAuthorize("hasAuthority('PRIV_XXX')")` sur tous les endpoints sensibles
- Filtre JWT `JwtAuthenticationFilter` intégré dans la chaîne Spring Security
- Endpoint de rafraîchissement de token (`POST /api/v1/auth/refresh`)
- Endpoint de déconnexion avec invalidation de token (`POST /api/v1/auth/logout`)
- Migration Liquibase pour la table `journal_audit`
- Tests unitaires `SecurityConfigTest` pour vérifier les accès autorisés/refusés

#### Modifié
- Remplacement de l'authentification en mémoire par `UserDetailsServiceImpl` (chargement depuis BDD)
- Durée d'expiration des tokens JWT configurée à 8 heures (paramétrable)
- Encodage BCrypt des mots de passe avec coût 12

#### Corrigé
- Fuite d'informations dans les messages d'erreur HTTP 403 (désormais génériques)
- CORS restreint aux origines autorisées en production

---

## [10.3.0] — 2026-04-10

### Phase 10 — Reporting BCM, Wallet Bankily, Cartographie

#### Ajouté
- `ReportingController` + `ReportingService` : tableau de bord réglementaire BCM (encours, PAR, RMA)
- `WalletController` : intégration Bankily — dépôts, retraits, historique des opérations wallet
- `CartographieController` : export GeoJSON des zones d'activité par agence
- Page Angular `ReportingComponent` : tableaux de bord avec graphiques (Chart.js)
- Page Angular `WalletListComponent` : liste et saisie des opérations wallet
- Page Angular `CartographieComponent` : carte interactive (Leaflet.js + OpenStreetMap)
- Route `/reporting`, `/wallet`, `/cartographie` dans `app.routes.ts`
- Section REPORTING (purple) et WALLET BANKILY (teal) dans la sidebar

---

## [9.0.0] — 2026-04-04

### Phase 9 — Paramétrages système

#### Ajouté
- Tables de référence : `TypeGarantie`, `Periodicite`, `StatutCredit`, `StatutMembre`
- `ParametrageController` : lecture et mise à jour des paramètres système
- Migrations Liquibase pour l'initialisation des données de référence
- Cache Spring Cache (`@Cacheable`) sur les listes de référence peu mutables

---

## [8.0.0] — 2026-03-28

### Phase 8 — Tiers et opérations diverses

#### Ajouté
- Entité `Tiers` (fournisseurs, partenaires) avec CRUD complet
- `OperationDiverseController` : saisie d'opérations comptables hors-crédits
- Export Excel de la liste des tiers

---

## [7.0.0] — 2026-03-20

### Phase 7 — Épargne

#### Ajouté
- Entités `CompteEpargne`, `MouvementEpargne`
- Calcul automatique des intérêts sur épargne (proportionnel, capitalisé)
- Blocage/déblocage de comptes d'épargne
- Intégration comptable automatique (écriture lors de dépôt/retrait)

---

## [6.0.0] — 2026-03-12

### Phase 6 — Garanties

#### Ajouté
- Entités `Garantie`, `TypeGarantie`, `DocumentGarantie`
- Association garanties ↔ crédit lors de l'octroi
- Libération des garanties à la clôture du crédit
- Export liste des garanties en Excel

---

## [5.0.0] — 2026-03-05

### Phase 5 — Agences et agents crédit

#### Ajouté
- Entités `Agence`, `AgentCredit` avec relation hiérarchique
- Affectation des membres et des crédits à une agence
- Reporting par agence dans le tableau de bord

---

## [4.0.0] — 2026-02-25

### Phase 4 — Comptabilité générale

#### Ajouté
- Plan comptable normalisé pour les institutions de micro-finance (PCN-IMF Mauritanie)
- Entités `CompteComptable`, `EcritureComptable`, `LigneEcriture`
- Génération automatique des écritures comptables lors des opérations de crédit et d'épargne
- Grand livre et balance avec export Excel

---

## [3.0.0] — 2026-02-15

### Phase 3 — Crédits

#### Ajouté
- Entités `Credit`, `EcheanceCredit`, `Remboursement` avec cycle de vie complet
- Service `AmortissementService` : calcul des annuités constantes et du linéaire
- Workflow : DEMANDE → VALIDE → DEBLOQUE → EN_COURS → SOLDE / CONTENTIEUX
- Calcul des pénalités de retard
- Export du tableau d'amortissement en PDF (iText)

---

## [2.0.0] — 2026-02-05

### Phase 2 — Produits crédit

#### Ajouté
- Entité `ProduitCredit` avec paramètres complets (taux, durées, garanties requises, frais de dossier)
- CRUD produits crédit avec validation des règles métier
- Association produits ↔ membres éligibles

---

## [1.0.0] — 2026-01-20

### Phase 1 — Membres (fondation du projet)

#### Ajouté
- Structure initiale du projet : Spring Boot 3.3.5, Angular 17, SQL Server 2019
- Configuration Liquibase (changelog-master + migrations versionnées)
- Configuration Spring Security (JWT, BCrypt)
- Entités JPA `Membre` (polymorphisme JOINED : `MembrePersonnePhysique`, `MembrePersonneMorale`)
- Double espace de noms : `com.microfina.*` (domaine) et `com.pfe.backend.*` (application)
- `MembresController` : CRUD complet, recherche paginée, validation d'adhésion, désactivation
- `MembresService` avec règles de validation métier
- `LoginController` : authentification JWT, endpoint `/api/v1/auth/login`
- Page Angular `MembresListComponent` : liste paginée avec filtres et actions
- Page Angular `MembreFormComponent` : formulaire de création/modification
- Proxy Angular vers Spring Boot (dev)
- Sidebar Angular avec navigation par sections
- Styles CSS globaux (variables, data-table, badges, pagination, form-card)

---

*Maintenu par l'équipe MICROFINA++ — PFE 2025/2026*
