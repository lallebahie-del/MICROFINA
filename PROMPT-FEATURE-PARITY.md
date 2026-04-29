# Prompt — Bring Microfina++ to Feature Parity with the Legacy MICROFINA

> Use this prompt to drive a rebuild of the new application so it matches the feature scope of the legacy one. Paste the whole document into a fresh Claude session (or feed it section by section).

---

## 0 — Mission

You are continuing the migration of a Mauritanian microfinance core-banking system from a legacy Java EE / JSF monolith (`MICROFINA-OLD/MicroCredit`) onto a modern stack (Spring Boot 3 + Angular 21).

The new app exists at:

- **Backend:** `MICROFINA/backend/` — Spring Boot 3.3.5, Java 21, JPA, JWT, Liquibase, SQL Server
- **Frontend:** `MICROFINA/frontend/` — Angular 21, standalone components, lazy-loaded routes
- **DB migrations:** `MICROFINA/src/main/resources/db/changelog/` — organized into phases 1–11

The legacy app exists at:

- `MICROFINA/MICROFINA-OLD/MicroCredit/` — JSF + EJB + JPA monolith, ~6,800 Java files, ~6,000 .xhtml pages

**You will not copy files.** The two stacks are incompatible at the file level (JSF managed beans cannot be pasted into Angular; legacy DAOs cannot be pasted into Spring Data). You will use the legacy app as a *specification* — you read its entities, services, and screens to understand the rules, then you re-implement those rules in the new architecture.

**Out-of-scope packages — ignore them entirely:** `net.mediasoft.grhpaie` (HR/payroll), `net.mediasoft.immo` (real estate), `net.mediasoft.stock` (inventory). The new app is microfinance-only.

---

## 1 — Architectural Rules

When you port a feature:

1. **Backend:** Spring Boot REST controller + service + Spring Data JPA repository + Liquibase changeset for any new tables. New tables go in a new phase folder (next sequential number after the existing `phase-11-*`). Never modify a Liquibase changeset that has already been applied — only add new ones.
2. **Frontend:** standalone Angular component, lazy-loaded route in `app.routes.ts`, sidebar entry in `sidebar.html`, French labels, the existing CSS look-and-feel.
3. **Auth:** every controller method must carry `@PreAuthorize("hasAuthority('PRIV_*')")` with an existing privilege from the `Privilege` table, or you must add a new privilege via Liquibase.
4. **Audit:** any CREATE / UPDATE / DELETE on business data must trigger the existing `JournalAudit` aspect — annotate with `@Audit` if needed.
5. **i18n:** keep French labels (the canonical names from the legacy app). The legacy app had multi-language; we are deferring i18n to a later phase, but do not introduce English-only labels.
6. **Naming:** mirror the French entity names from the legacy app (Tontine, Carnet, Cycle, Sinistre, Lettrage, etc.) — they are the domain language.
7. **Money:** use `BigDecimal`, never `double`. Currency is MRU by default; multi-currency support comes from the `DeviseLocal` entity.
8. **Validation:** use Bean Validation (`@NotNull`, `@Positive`, `@DecimalMin`) on all DTOs.
9. **No business logic in controllers** — controllers only orchestrate; rules live in services.

---

## 2 — What's Already Built (Do NOT Re-Do)

The new app, as of v12, already has the following — confirm with the file paths before re-implementing anything:

### Already implemented modules

- **Authentication & users:** JWT login, roles, privileges (7 roles × 42 privileges), audit journal, user CRUD, role/privilege management — see `AuthController`, `AdminUtilisateurController`, `JournalAuditController`.
- **Membres (basic):** create / edit / list / search / soft-delete, photo upload — `MembresController`, `pages/membres/`.
- **Produits crédit:** product catalog with rate, durée, mode de calcul d'intérêt, eligibility — `ProduitCreditController`.
- **Crédits (basic lifecycle):** BROUILLON → DEBLOQUE → SOLDE/CONTENTIEUX, amortization (annuity/linear), simulator — `CreditsController`, `SimulationCreditController`, `AmortissementService`.
- **Comptabilité (basic):** écriture posting, journal, grand-livre, balance, bilan — `ComptabiliteController`, `pages/comptabilite/*`.
- **Épargne (basic):** comptes épargne CRUD, dépôt / retrait — `CompteEpsController`.
- **Carnets de chèques (basic):** issuance, listing — `CarnetChequeController`.
- **Garanties (basic):** caution / hypothèque / nantissement attachment, libération — `GarantieController`.
- **Agences:** CRUD, siège flag — `AgenceController`.
- **Banques:** partner bank registry — `BanqueController`.
- **Opérations caisse / banque:** basic CRUD — `OperationCaisseController`, `OperationBanqueController`.
- **Wallet Bankily:** transaction list, basic reconciliation, HMAC-SHA256 webhook signature verification — `WalletController`.
- **Cartographie:** GeoJSON map per zone — `CartographieController`.
- **Reporting BCM:** PAR, RMA, ratios via SQL views — `ReportingController`.
- **Exports:** 12 report types × 3 formats (Excel/PDF/Word) + Sage Compta CSV — `ExportController`, `ExportService`.
- **Budgets (fiscal year):** simple BROUILLON → VALIDE → CLOTURE — `BudgetController`. NOTE: this is *not* the legacy "Plan Budgétaire" with cost centers and donors; that's a gap — see §3.7.
- **Admin tooling:** monitoring (JVM, DB, jobs, sessions), backup/restore, scheduled jobs (calcul-intérêts, recalcul-PAR, clôture) — `AdminMonitoringController`, `AdminBackupController`, `JobSchedulerService`.

### Existing Liquibase phases (do NOT modify these — only add new phases)

| Phase | Purpose |
|-------|---------|
| 1 | Core config: agence, agent_credit, devise, parametre, taxonomies, fee setup |
| 2 | Members: tiers, personne, membres (PP/PM JOINED), savings accounts |
| 3 | Products: produit_credit, produit_islamic, produit_epargne, produit_part_sociale |
| 4 | Transactions: credits, amortp, garanties, reglement |
| 5 | BCM views, clôture journalière |
| 6 | Bank operations, check books, virements |
| 7 | Cash operations, savings deposits/withdrawals, frais d'adhésion |
| 8 | Budgets (simple) |
| 9 | Security: roles, privilèges, utilisateur, journal_audit |
| 10 | Type garanties, reporting views, wallet, cartographie |
| 11 | Privilege expansion, job_execution table |

**Therefore your new changesets go in `phase-12-*`, `phase-13-*`, etc.**

---

## 3 — The Gap: What's Missing & Must Be Built

The features below exist in the legacy app and are missing — or only partially present — in the new app. They are listed in roughly **descending priority** (most-impactful microfinance gaps first).

For each module: **Goal**, **Legacy reference**, **Data model**, **Backend API**, **Frontend screens**, **Business rules**.

---

### 3.1 — Multi-level Credit Approval Workflow ⚠️ PRIORITY

**Goal.** Replace the simple `transitionner` lifecycle with the formal visa workflow: dossier complétude → analyse financière → visa responsable crédit → comité de crédit → service financier → déblocage. Each step records who, when, and the decision.

**Legacy reference.**
- Beans: `web/src/main/java/net/mediasoft/microfina/view/managedbeans/credit/VisaCommiteCredit*.java`, `VisaResponsableCredit*.java`, `VisaServiceFinancier*.java`
- Services: `service/impl/credit/ControleCreditServiceBean.java`
- Entity: `dao/.../entities/HistoriqueVisaCredit.java` (already exists in the new app — but the workflow on top of it is missing)

**Data model (already partly exists).**
- `historique_visa_credit` table exists in Phase 4. Add columns if missing: `etape` (DOSSIER / ANALYSE / VISA_RC / COMITE / VISA_SF / DEBLOCAGE), `decision` (APPROUVE / REJETE / RENVOYE), `commentaire`, `id_utilisateur`, `date_decision`.
- Add `etape_courante` column to `credits` table indicating where the credit is in the workflow.
- Add `analyse_financiere` table: id, credit_id, revenus_mensuels, charges_mensuelles, ratio_endettement, capacite_remboursement, pieces_jointes (JSON array of file paths), commentaire.

**Backend.**
- `CreditWorkflowService` with methods: `soumettreDossier(creditId)`, `validerComplétude(creditId, decision)`, `enregistrerAnalyseFinanciere(creditId, dto)`, `viserResponsableCredit(creditId, decision)`, `passerAuComite(creditId)`, `decisionComite(creditId, decision)`, `viserServiceFinancier(creditId, decision)`, `débloquer(creditId, montant)`.
- Each method: check current `etape_courante` is the expected one, check user has the right privilege, record in `historique_visa_credit`, advance `etape_courante`, audit.
- New privileges: `PRIV_COMPLETUDE_DOSSIER`, `PRIV_ANALYSE_FINANCIERE`, `PRIV_VISA_RC`, `PRIV_COMITE_CREDIT` (rename from `PRIV_VALIDATE_CREDIT`), `PRIV_VISA_SF`, `PRIV_DEBLOQUER` (rename from `PRIV_DISBURSE_CREDIT`).

**Frontend.**
- New page `/credits/:id/workflow` — vertical timeline of stages with the action button for the current stage (only enabled if user has the right privilege and credit is at that stage).
- `Analyse Financière` modal / sub-form inside the workflow page.
- `Comité de Crédit` page `/credits/comite` — list of credits awaiting committee decision, batch approve/reject.

**Business rules.**
- A credit cannot skip stages.
- A rejected credit at any stage returns to `BROUILLON` with the rejection comment recorded.
- Only credits at `etape_courante = DEBLOCAGE_PENDING` can be disbursed.
- Disbursement triggers automatic accounting entry (debit caisse / credit prêts à recevoir) and the existing amortization generation.

---

### 3.2 — Tontine Module (Community Savings Circles) ⚠️ ENTIRELY MISSING

**Goal.** Implement the rotating savings & credit association feature — totally absent from the new app.

**Legacy reference.**
- Beans: `web/src/main/java/net/mediasoft/microfina/view/managedbeans/tontine/`
- Services: `service/impl/tontine/`
- Entities (legacy package `net.mediasoft.microfina.entities.tontine.*`): `TTontine`, `TCycle`, `TCarnet`, `TMembre`, `TEpargne`, `TCollecteur`, `TVersementCollecteur`, `TCommission`, `TCommissionNegociee`, `TCommissionCaisseTmp`, `TRegulation`, `TRegulationTmp`, `TSuspends`, `TInitierRetrait`, `TBornePaieCollecteur`, `TParametrePaieCollecteur`, `HistoriqueChangementCollecteur`, `HistoriqueChangementEtatTMembre`, `TVenteCarnet`, `ListeVireBanque`, `ZoneActivite`.

**Data model — new Liquibase phase `phase-12-tontine/`:**

| Table | Purpose |
|-------|---------|
| `tontine` | Pool definition: nom, contribution_periodique, periodicite, nb_membres_max, frais_adhesion, agence_id, statut (ACTIVE/CLOTUREE) |
| `cycle_tontine` | One cycle per tontine: numero, date_debut, date_fin, ordre_distribution (JSON), statut |
| `membre_tontine` | Member-tontine link: membre_id, tontine_id, ordre_dans_cycle, date_adhesion, statut (ACTIF/SUSPENDU/RETIRE) |
| `carnet_tontine` | Collection book per member per cycle: codes_paiement_attendus (JSON), montant_par_case |
| `collecteur` | Collection agent: nom, telephone, zone_id, taux_commission, statut |
| `versement_collecteur` | Collector remittance: collecteur_id, date, montant_collecte, montant_commission, montant_remis_caisse |
| `commission_tontine` | Commission record per cycle |
| `regulation_tontine` | Final settlement & payout when a member's turn arrives |
| `historique_changement_collecteur` | Audit trail of collector reassignments |
| `zone_activite` | Geographic / activity grouping for collectors |

**Backend.**
- `TontineService`, `CycleTontineService`, `CarnetService`, `CollecteurService`, `RegulationService`.
- REST: `/api/v1/tontines`, `/api/v1/tontines/{id}/cycles`, `/api/v1/cycles/{id}/carnets`, `/api/v1/collecteurs`, `/api/v1/cycles/{id}/regulation`.
- Privileges: `PRIV_TONTINE_GERER`, `PRIV_TONTINE_COLLECTER`, `PRIV_TONTINE_REGULER`.

**Frontend — new sidebar section "TONTINE" (color: amber):**
- `/tontines` — list of tontines
- `/tontines/nouveau` — create
- `/tontines/:id` — detail with member list, current cycle, payouts
- `/tontines/:id/cycles` — manage cycles
- `/tontines/:id/cycles/:cycleId/carnets` — collection book per member
- `/collecteurs` — collector roster
- `/collecteurs/:id/versements` — record remittances
- `/tontines/regulation` — finalize cycle settlements

**Business rules.**
- Members pay a fixed contribution per period (weekly, monthly, configurable).
- Collector visits members, records cash in their carnet, then deposits to the institution.
- Commission is either fixed (`TCommission`) or negotiated per member (`TCommissionNegociee`).
- When a member's turn (per `ordre_dans_cycle`) arrives, the accumulated balance — minus all commissions — is paid out via `regulation_tontine`.
- Members in arrears can be suspended (`statut = SUSPENDU`); reinstatement requires a comment trail.
- Audit every collector reassignment (`historique_changement_collecteur`) and every member-status change.

---

### 3.3 — Mobile Money / Bankily — Full Integration ⚠️ EXTEND EXISTING

**Goal.** Extend the basic `/wallet` page into a full mobile-money operations module with operator management, prefix routing, SMS notifications, batch file import, and mobile-initiated loan repayment.

**Legacy reference.**
- Package: `net.mediasoft.microfina.mobileMoney.*` and `net.mediasoft.microfina.dao.mobileMoney.*`.
- Entities: `Transaction`, `TransactionImported`, `SouscriptionRembMobile`, `OperateurMobile`, `NumMobileMoney`, `PrefixeMobile`, `SMS`, `TypeOperationMobileMoney`, `ParamFraisMobileMoney`, `TracesOperationsMobileMoney`.

**Data model — phase `phase-13-mobile-money/`:**

| Table | Purpose |
|-------|---------|
| `operateur_mobile` | Bankily, Sedad, Masrvi, Bimbank etc. with API URL, credentials encrypted |
| `prefixe_mobile` | Phone prefix → operator mapping (e.g. `2222`, `2223`, …) |
| `numero_mobile` | Phone numbers linked to a member/account |
| `transaction_mobile_money` | Already partly exists as `operation_wallet` — extend with operator_id, num_mobile, ref_externe |
| `transaction_importee` | Staging area for batch imports before posting |
| `souscription_remb_mobile` | Loan repayment via mobile, linking transaction → credit |
| `sms_notification` | SMS log: destinataire, contenu, statut (SENT/FAILED), date_envoi |
| `param_frais_mobile_money` | Fee per operator × type d'opération |
| `type_operation_mobile_money` | RETRAIT, DEPOT, PAIEMENT, TRANSFERT, REMBOURSEMENT |

**Backend.**
- `OperateurMobileService`, `NumMobileService`, `SouscriptionRembService`, `SmsService`, `BatchImportMobileService`.
- Endpoints: `/api/v1/operateurs-mobile`, `/api/v1/numeros-mobile`, `/api/v1/transactions-mobile`, `/api/v1/souscriptions-mobile`, `/api/v1/transactions-mobile/import` (multipart file upload — CSV from operator settlement file).
- Reconciliation job: nightly compare imported file vs. posted transactions, flag mismatches.
- SMS dispatch: hook into a generic `SmsGateway` interface — for now stub the implementation, but wire the contract.

**Frontend — extend WALLET BANKILY sidebar section:**
- Existing `/wallet` page — keep as transaction list
- `/wallet/operateurs` — operator management
- `/wallet/numeros` — phone-number registry per member
- `/wallet/import` — batch file import + reconciliation viewer
- `/wallet/souscriptions` — loan-repayment-via-mobile log
- `/wallet/parametres-frais` — fee parametrization per operator

**Business rules.**
- Phone numbers must be validated against `prefixe_mobile` (must match a registered operator).
- A repayment transaction matched to a credit triggers the existing `Reglement` posting workflow (debit account, credit credit principal/interest per the amortization schedule).
- Failed reconciliations become an `Anomalie` flag on the transaction — manual resolution required.
- All transactions audited.

---

### 3.4 — Micro-Assurance ⚠️ ENTIRELY MISSING

**Goal.** Credit insurance: collect premium at disbursement, manage claims (sinistres), settle with insurer.

**Legacy reference.**
- Package: implied via entities `AssureurCredit`, `AssuranceType`, `AssuranceCollecte`, `Sinistre`, `BaseAssuranceCredit`.
- Reports: `web/src/main/java/net/mediasoft/microfina/report/micro_assurance/`.

**Data model — phase `phase-14-assurance/`:**

| Table | Purpose |
|-------|---------|
| `assureur` | Insurance company: nom, agrement_bcm, contact, statut |
| `produit_assurance` | Type of cover: deces, invalidite, perte_emploi; rate or fixed amount |
| `police_assurance` | Policy attached to a credit: credit_id, produit_assurance_id, prime, date_debut, date_fin |
| `sinistre` | Claim: police_id, type_sinistre, date_survenance, montant_reclame, statut (DECLARE/EN_COURS/REGLE/REFUSE), justificatifs (JSON) |
| `reglement_sinistre` | Insurer payout to institution / borrower |
| `collecte_prime` | Premium accruals over loan life |

**Backend.**
- `AssureurService`, `PoliceService`, `SinistreService`.
- Endpoints: `/api/v1/assureurs`, `/api/v1/polices`, `/api/v1/sinistres`, `/api/v1/sinistres/{id}/reglement`.
- Privileges: `PRIV_ASSURANCE_GERER`, `PRIV_SINISTRE_TRAITER`.

**Frontend — new section "ASSURANCE":**
- `/assureurs` — insurer registry
- `/produits-assurance` — insurance product catalog
- `/polices` — policy list (filter by credit, by insurer)
- `/sinistres` — claim workflow (DECLARE → EN_COURS → REGLE/REFUSE)
- Integrate into `/credits/:id` — show attached police if any.

**Business rules.**
- Premium can be a percentage of loan amount or fixed; computed at disbursement and either added to principal (`prime_dans_principal = true`) or paid separately.
- Claim filing requires justifying documents (PDF upload).
- Approved claim triggers debit to insurer receivable, credit to credit balance (reduces outstanding) or credit to member account, depending on policy type.

---

### 3.5 — Plan Comptable & Lettrage & Rapprochement Bancaire

**Goal.** Replace ad-hoc account list with a managed `plan_comptable` (PCN-IMF compliant), add manual + automatic lettrage (entry reconciliation), and bank statement rapprochement.

**Legacy reference.**
- Beans: `PlanComptable*`, `LettrageManagedBean`, `LettrageAutoManagedBean`, `RapprochementBancaireManagedBean`.
- Entities: `PlanComptable`, `Lettrage`, `RapprochementBancaire`.

**Data model — phase `phase-15-comptabilite-avancee/`:**

| Table | Purpose |
|-------|---------|
| `plan_comptable` | num_compte, libelle, classe (1–8), parent_id, lettrable (bool), actif, devise |
| `lettrage` | Reconciliation: code_lettrage, date_lettrage, montant_total, statut |
| `lettrage_ligne` | Linked entries: lettrage_id, ecriture_id |
| `rapprochement_bancaire` | Statement period: banque_id, date_debut, date_fin, solde_releve, solde_compta, ecart |
| `releve_bancaire_ligne` | Imported statement lines |

**Backend.**
- `PlanComptableService`, `LettrageService` (with `proposerLettrageAutomatique(compteId)` matching same-amount opposite entries), `RapprochementService`.
- Endpoints: `/api/v1/plan-comptable`, `/api/v1/lettrage`, `/api/v1/rapprochement-bancaire`.

**Frontend — extend COMPTABILITÉ section:**
- `/comptabilite/plan-comptable` — tree view + CRUD
- `/comptabilite/lettrage` — choose account → see open entries → tick to lettrer
- `/comptabilite/lettrage-auto` — propose matches, accept/reject batch
- `/comptabilite/rapprochement-bancaire` — import statement, match to GL, resolve écarts

**Business rules.**
- Only `lettrable = true` accounts can be reconciled.
- A lettrage can only close if débit total = crédit total.
- A rapprochement can only close if `ecart = 0` or with documented justification.

---

### 3.6 — Compte de Résultat, Bilan détaillé & Annexes BCEAO

**Goal.** Full statutory reporting suite (annexes 2–14) per BCEAO/BCM rules.

**Legacy reference.**
- `web/src/main/java/net/mediasoft/microfina/report/compta/EtatAnnexe*.java`.

**Backend.**
- New views (or service-level queries) for each annex; one endpoint per annex.
- Wire into existing `/exports` — add 12 new export entries (one per annex) in Excel + PDF.

**Frontend.**
- New `/comptabilite/etats-annexes` page listing all annexes with date-range picker, agence selector, and "Generate" / "Download" buttons.

---

### 3.7 — Plan Budgétaire Complet (Beyond Fiscal Budget)

**Goal.** Replace the simple `Budget` entity with a full budget-management module: plan budgétaire, lignes budgétaires, sources de financement (bailleurs), centres de coût, prévisions, suivi.

**Legacy reference.**
- Beans: `PlanbudgetaireManagedBean`, `LigneBudgetaireManagedBean`, `BailleurBudgetManagedBean`, `CentreDeCoutManagedBean`, `PrevisionBudgetaireManagedBean`, `ConsomationParCentreDeCoutManagedBean`, `GrandLivreBudgetaireManagedBean`.

**Data model — phase `phase-16-budget-avance/`:**

| Table | Purpose |
|-------|---------|
| `plan_budgetaire` | Top-level budget plan, exercise, agence, statut |
| `ligne_budgetaire` | plan_id, num_compte, libelle, montant_alloue, centre_cout_id, bailleur_id |
| `centre_cout` | Cost center: code, libelle, type_centre_cout_id, parent_id |
| `type_centre_cout` | Operationnel, Administratif, Projet |
| `bailleur` | Donor / lender: nom, type, contact |
| `ligne_financement` | Funding source line per bailleur per ligne_budgetaire |
| `affectation_budget` | Allocation tracking |
| `mouvement_budget` (exists) | Extend with `centre_cout_id`, `ligne_budgetaire_id` |
| `prevision_budgetaire` | Forecast revisions |

**Frontend — extend BANQUE/BUDGET section:**
- `/plans-budgetaires` — list + create
- `/plans-budgetaires/:id/lignes` — manage lines
- `/centres-cout` — cost-center hierarchy
- `/bailleurs` — donor registry
- `/budget/suivi` — variance dashboard (budgété vs. consommé vs. disponible)
- `/budget/grand-livre` — budget posting ledger

---

### 3.8 — Member Lifecycle: Visa Adhésion, Signature, Personne Liée, Consentement

**Goal.** Add member-onboarding workflow: a new member is `EN_ATTENTE` until visa'd; track signature image, related persons, consent forms.

**Legacy reference.**
- Beans: `ValidationMembreManagedBean`, `ConsentementMembreManagedBean`, `PhotoSignatureMembreManagedBean`, `PersonneLieeManagedBean`.

**Data model — phase `phase-17-membre-avance/`:**

- Add to `membres` table: `statut_adhesion` (EN_ATTENTE / VISE / REJETE), `date_adhesion`, `date_visa`, `id_visa_par`, `signature_path`.
- New table `personne_liee`: membre_id, nom, prenom, lien (CONJOINT / ENFANT / GARANT / AUTRE), telephone, document_identite.
- New table `consentement`: membre_id, type_consentement, fichier_path, date_signature.

**Backend.**
- Extend `MembresService` with `viserAdhesion(membreId, decision, commentaire)`, `attacherPersonneLiee`, `enregistrerConsentement`.
- Restrict credit creation to `statut_adhesion = VISE` members.

**Frontend.**
- Extend `/membres/:numMembre/edit` with tabs: Informations / Photo & Signature / Personnes liées / Consentements / Visa adhésion.
- New page `/membres/visa` — list of members awaiting visa.

---

### 3.9 — Engagement, Compte DAT, Affectation Guichet, Billetage, Clôture Journalière

**Goal.** Round out the cashier / day-to-day operations: term deposits (DAT), formal commitments, teller-window assignment, cash-count register, end-of-day procedure.

**Legacy reference.**
- Beans: `CompteDATManagedBean`, `EngagementManagedBean`, `AffectGuichetToUserManagedBean`, `BilletageCaisseManagedBean`, `ClosureManagedBean`, `OuvertureGuichetManagedBean`, `FermetureGuichetManagedBean`.

**Data model — phase `phase-18-caisse-avance/`:**

| Table | Purpose |
|-------|---------|
| `compte_dat` | Term deposit: membre_id, montant, taux, date_debut, date_echeance, capitalise (bool) |
| `engagement` | Formal commitment (caution, garantie morale): membre_id, type, montant, date_debut, date_fin |
| `guichet` | Teller window: code, agence_id, statut (OUVERT/FERME) |
| `affectation_guichet_user` | utilisateur_id, guichet_id, date_debut, date_fin |
| `billetage` | Cash count: guichet_id, date, denominations (JSON: 1000×qty, 500×qty…), total, statut (BROUILLON/VALIDE) |
| `cloture_journee` | Already exists as `cloture_journaliere` — extend with cash-count linkage |

**Backend.**
- `CompteDatService` (with maturity-day interest capitalization job).
- `GuichetService`, `BilletageService`.
- Extend the existing `cloture` job to require: (a) every guichet has a validated `billetage`, (b) GL is in balance, (c) all today's transactions posted.

**Frontend — extend CLIENT/CAISSE section:**
- `/comptes-dat` — DAT list + open/close
- `/engagements` — engagement registry
- `/guichets` — teller windows + open/close
- `/billetage` — cash-count form (denomination grid)
- `/cloture-journee` — wizard: vérifier guichets → vérifier billetage → vérifier GL → clôturer

---

### 3.10 — Exercice & Période, Fermeture Annuelle

**Goal.** Manage fiscal years (exercice) with formal opening / closing routines and inter-period locks.

**Legacy reference.**
- Beans: `ExerciceManagedBean`, `OuvertureExerciceManagedBean`, `FermetureExerciceManagedBean`, `PeriodeManagedBean`.

**Data model — phase `phase-19-exercice/`:**

| Table | Purpose |
|-------|---------|
| `exercice` | annee, date_debut, date_fin, statut (OUVERT / EN_CLOTURE / CLOTURE) |
| `periode` | exercice_id, mois, statut |
| `cloture_exercice_log` | Per-step log of the year-end procedure |

**Backend.**
- `ExerciceService` with `ouvrirExercice(annee)`, `fermerExercice(annee)`.
- Closing exercice runs: all écritures balanced, comptes de résultat reportés à nouveau, generates opening balances of next exercice.
- All postings refuse if their date falls in a `CLOTURE` period.

**Frontend.**
- `/admin/exercices` — list + open/close.

---

### 3.11 — Référentiels Étendus

**Goal.** Add the missing reference tables that power dropdowns across the app.

**Tables to add (phase `phase-20-referentiels/`):**

- `profession` (libelle, secteur_id)
- `secteur_activite` (libelle)
- `type_pret` (libelle, code)
- `filiere` (libelle, secteur_id)
- `type_structure_administrative`
- `organe`, `type_organe`
- `taxe` (already exists — verify columns: taux, base, exonération)
- `nature_frais`, `valeur_frais` extensions

**Backend.**
- One generic `ReferentielController` with `/api/v1/referentiels/{table}` (read-only) + admin CRUD endpoints under `/api/v1/admin/referentiels/{table}`.

**Frontend.**
- `/admin/referentiels` — single page with tabs (or sub-routes) per referentiel.

---

### 3.12 — Provisioning Automatique CL1/CL2/CL3 & Reclassification

**Goal.** Automated daily job that classifies credits into CL1 (sain) / CL2 (surveillé, 25–50% provision) / CL3 (douteux, 100% provision) based on days-in-arrears, and posts the provision écritures.

**Legacy reference.**
- DAO: `ProvisionDaoBean.java`
- Entities: `Provision`, `Situationdeclasse`, `DetailModeleDeclassement`.

**Data model — phase `phase-21-provisioning/`:**

| Table | Purpose |
|-------|---------|
| `regle_declassement` | seuil_jours_min, seuil_jours_max, classe (CL1/CL2/CL3), taux_provision |
| `provision` | credit_id, classe, montant_provision, date_calcul, ecriture_id |
| `historique_classement` | Audit of classification changes |

**Backend.**
- `ProvisioningService.recalculerToutesClasses()` — invoked by a new scheduled job (daily 02:00).
- Post écriture: débit charge de provision / crédit compte de provision.
- Reclassification log per credit.

**Frontend.**
- `/credits/declassement` — view classified credits, filter by classe.
- `/admin/regles-declassement` — manage thresholds (admin-only).

---

### 3.13 — Balance Agée Détaillée, États de Suivi Crédit

**Goal.** The legacy app has at least 6 different aged-balance views (développée, synthèse, par agent, par produit, par concentration, par secteur). The new app has only one basic PAR view.

**Backend.**
- New views: `vue_balance_agee_detaillee`, `vue_balance_agee_synthese`, `vue_balance_agee_par_agent`, `vue_balance_agee_par_produit`, `vue_balance_agee_par_secteur`.
- Extend `ReportingService` with one method per view.

**Frontend.**
- New page `/reporting/balance-agee` with tabs for each variant.
- Add Excel/PDF export entries to `/exports`.

---

### 3.14 — Foreign Exchange (Module Changes)

**Goal.** Multi-currency operations: buy/sell foreign currency, inter-institution transfers, exchange-rate parametrization.

**Legacy reference.**
- Beans: `ChgeDeviseBean`, `ChgeInstitutionDeviseBean`, `ChgeOperationsAchatVenteBean`.
- Package: `net.mediasoft.microfina.dao.changes`, `net.mediasoft.microfina.entities.changes`.

**Data model — phase `phase-22-changes/`:**

| Table | Purpose |
|-------|---------|
| `taux_change` | devise_id, date, taux_achat, taux_vente |
| `operation_change` | type (ACHAT/VENTE), devise_id, montant_devise, montant_local, taux_applique, date, agence_id |
| `transfert_inter_institution` | institution_emettrice, institution_receptrice, devise, montant, statut, ref_externe |

**Backend & Frontend** — new section "CHANGES" with rates, achats/ventes, transferts.

---

### 3.15 — Multi-Critère Search & LazyDataModel

**Goal.** Replace simple search inputs with multi-critère search forms (the legacy app has them on every list page) and lazy server-side pagination for large lists.

**Backend.**
- Extend list endpoints to accept a `criteria` query DTO (DTO-per-domain) with fields like `nom`, `dateDebut`, `dateFin`, `statut`, `agenceId`, `produitId` etc., and a `Pageable`.
- Use Spring Data `Specification` to build dynamic queries.

**Frontend.**
- Generic `<app-recherche-multicritere>` component receiving a `fields` config and emitting a `criteria` object.
- Server-side pagination (`p-table` `lazy="true"` if you adopt PrimeNG, or rebuild the existing table with pagination).

Apply to: membres, crédits, comptabilité, opérations caisse/banque, garanties.

---

### 3.16 — Internationalisation (i18n)

**Goal.** The legacy app supports French (default) + Arabic (RTL). Wire up Angular i18n with French as default and Arabic as a switchable locale, even if Arabic strings come later.

**Frontend.**
- Adopt `@ngx-translate/core` or built-in Angular i18n.
- Move all hardcoded French labels into `assets/i18n/fr.json` and `ar.json`.
- Add a language picker in the navbar.
- For RTL: toggle `dir="rtl"` on `<body>` when `ar` is selected.

**Defer for v2** if scope is too tight — but at minimum extract strings into a single bundle.

---

### 3.17 — Suspense, Encaissement Chèque, Demande / Suspension Chéquier

**Goal.** Round out check-management beyond simple CRUD: customers can request check books, books can be suspended, individual checks can be encashed against an account.

**Backend & Frontend.**
- Extend `CarnetChequeController`: endpoints for `demanderChequier`, `suspendreChequier`, `commanderChequier` (envoi commande à la banque imprimeur), `encaisserCheque(numChequeEntrant, compte)`.
- New screens under CLIENT/CAISSE: "Demandes chéquiers", "Suspensions chéquiers", "Encaissement chèque".

---

## 4 — How to Use This Prompt

You don't have to do everything at once. Pick a section, work it end-to-end, then move on.

For each section above, the workflow is:

1. **Read the legacy reference files** I cited (entities, services, .xhtml screens) to confirm the data model and rules.
2. **Add a new Liquibase phase folder** with the schema (tables, indexes, FKs, and seed data for new privileges/roles if any).
3. **Generate the JPA entities** matching the new tables; place them under `backend/src/main/java/com/microfina/entity/<module>/`.
4. **Write the Spring Data repositories** under `repository/<module>/`.
5. **Write the service layer** under `service/<module>/` — this is where the legacy business rules go.
6. **Write the REST controller** under `controller/<module>/` with `@PreAuthorize` and DTO-based request/response.
7. **Write a unit test** for each service method (the project uses JUnit + Mockito).
8. **Add the Angular service** under `frontend/src/app/services/<module>.service.ts`.
9. **Add the Angular page(s)** under `frontend/src/app/pages/<module>/` as standalone components.
10. **Register the route** in `app.routes.ts` with `loadComponent` and `canActivate: [authGuard]`.
11. **Add the sidebar entry** in `sidebar.html` (correct color section per the legacy mapping).
12. **Run `mvn test` + `ng build` + manual smoke test.**
13. **Update `CHANGELOG.md`** with what was added.

For each session, tell me:
- Which section number from §3 you want to tackle.
- Whether you want it built end-to-end or stage-by-stage (entity → service → controller → frontend).

I'll then read the legacy reference files, design the schema, and ship the code.

---

## 5 — Priority Order (My Recommendation)

If you build them in this order, each step unblocks the next:

1. **§3.1 Multi-level credit approval workflow** — biggest functional gap
2. **§3.8 Member visa adhésion** — credits depend on it
3. **§3.5 Plan comptable + lettrage** — accounting depth
4. **§3.12 Provisioning automatique** — regulatory depth on top of accounting
5. **§3.9 Caisse / billetage / clôture journalière** — daily ops
6. **§3.10 Exercice / période** — locks the books
7. **§3.6 Annexes BCEAO** — finishes regulatory reporting
8. **§3.7 Plan budgétaire complet** — replaces the toy budget
9. **§3.13 Balance agée variants** — risk-management deepening
10. **§3.2 Tontine** — large self-contained module
11. **§3.3 Mobile money extensions** — extending what exists
12. **§3.4 Micro-assurance** — large self-contained module
13. **§3.14 Foreign exchange** — niche but valuable
14. **§3.11 Référentiels étendus** — quality-of-life
15. **§3.15 Multi-critère search & lazy pagination** — UX polish
16. **§3.17 Cheque advanced workflows** — completes caisse
17. **§3.16 i18n** — release polish

---

## 6 — Things to Confirm Before Starting

Decisions the human (lalle) needs to make before you build:

- [ ] **i18n scope:** French only for v2, or French + Arabic from day one?
- [ ] **Tontine scope:** all features (commission négociée, suspensions, zones) or MVP (basic pool + carnet + payout)?
- [ ] **Mobile money operators:** which ones to support officially? (Bankily, Sedad, Masrvi, Bimbank?)
- [ ] **Assurance partners:** is there a real insurer plugged in or is this internal-only at first?
- [ ] **PCN-IMF:** import the official BCM chart of accounts as seed data, or maintain manually?
- [ ] **Reporting periodicity:** annexes generated on demand or auto-archived monthly?

Once those are answered, you have a clean path to v2 feature parity.

---

## §3.1 — DONE (2026-04-28)

### Multi-Level Credit Approval Workflow

**Status:** ✅ Implemented and code-reviewed.

**What was built:**
- State machine: `SAISIE → COMPLETUDE → ANALYSE_FINANCIERE → VISA_RC → COMITE → VISA_SF → DEBLOCAGE_PENDING → DEBLOQUE` (+ `REJETE` from any non-terminal step)
- `etape_courante NVARCHAR(30)` column on `Credits`; DDL P12-001 backfills legacy rows to `SAISIE`
- `analyse_financiere` table (DDL P12-002) as 1-1 FK on `Credits`, stores ratio + capacité auto-computed
- 6 new privileges seeded (P12-003): `COMPLETUDE_DOSSIER`, `ANALYSE_FINANCIERE`, `VISA_RC`, `COMITE_CREDIT`, `VISA_SF`, `DEBLOQUER_CREDIT`
- `CreditWorkflowService` — 8 transition methods + `getTimeline`, `getAnalyse`, `getComitePending`; déblocage generates `Amortp` via `AmortissementService.genererTableau()`
- `CreditWorkflowController` — 12 endpoints at `/api/v1/credits/{idCredit}/workflow/**`, all `@PreAuthorize` secured
- `HistoriqueVisaCredit` row appended per transition (ETAPE, STATUT_AVANT, STATUT_APRES, DATE_VISA, DECISION, UTILISATEUR)
- Angular: `CreditWorkflowPageComponent` (vertical timeline + per-étape action modals), `CreditsComitePageComponent` (list pending), lazy routes, sidebar "Comité crédit" entry

**TODOs left open (tracked with `// TODO §3.1.x` in code):**
- Post comptabilité entries on disbursement (`CreditWorkflowService.debloquer()`)
- Lock guarantees on disbursement (same method)
- Client-side privilege guard (AuthService returns only username/role — enforcement is server-side only)

---

*End of prompt.*
