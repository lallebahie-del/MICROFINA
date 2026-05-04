# §3.1 — Multi-Level Credit Approval Workflow — Implementation Prompt

> **Context for the new session:** You are picking up work on the **Microfina++** full-stack microfinance application (Spring Boot 3.3.5 + Java 21 backend, Angular 21 + TypeScript 5.9 frontend, SQL Server 2022 + Liquibase 4.27). The previous session designed the parity gap (`PROMPT-FEATURE-PARITY.md`) and wrote the Phase 12 Liquibase schema. Your job is to finish §3.1 — the multi-level credit approval workflow — by building the backend, the frontend, and the verification.
>
> Repo root on the user's machine: `C:\Users\PC\Desktop\MICROFINA`
> Backend lives in two places (legacy split — keep using it):
> - **Domain entities:** `src/main/java/com/microfina/entity/`
> - **Backend services / controllers / repos / DTOs:** `backend/src/main/java/com/pfe/backend/`
>
> Frontend: `frontend/src/app/`

---

## 0. What the previous session already finished

✅ Phase 12 Liquibase schema is **written and wired into the master changelog**:

```
src/main/resources/db/changelog/phase-12-credit-workflow/
├── P12-001-ALTER-TABLE-Credits-add-etape.xml          # adds etape_courante NVARCHAR(30) + index, backfills legacy rows
├── P12-002-CREATE-TABLE-analyse_financiere.xml        # new 1-1 table keyed by credit_id
├── P12-003-INSERT-privileges-workflow.xml             # 6 new privileges (idempotent)
├── P12-004-INSERT-role-privileges-workflow.xml        # role↔privilege mappings for AGENT_CREDIT, COMITE_CREDIT, COMPTABLE, CAISSIER, SUPERVISEUR, AUDITEUR + ADMIN catch-up
└── changelog-phase-12.xml                             # phase wrapper
```

`db.changelog-master.xml` already has the Phase 12 `<include>` line. **Do not modify these files** unless you find a real bug.

The new column and table:
- `Credits.etape_courante NVARCHAR(30)` default `SAISIE`, valid values: `SAISIE | COMPLETUDE | ANALYSE_FINANCIERE | VISA_RC | COMITE | VISA_SF | DEBLOCAGE_PENDING | DEBLOQUE | CLOTURE | REJETE`
- `analyse_financiere(ID_ANALYSE PK, credit_id FK UNIQUE, TYPE_ANALYSE, REVENUS_MENSUELS, CHARGES_MENSUELLES, CAPACITE_REMBOURSEMENT, RATIO_ENDETTEMENT, TOTAL_ACTIF, TOTAL_PASSIF, INDICATEURS_JSON, COMMENTAIRE, AVIS_AGENT, DATE_ANALYSE, UTILISATEUR, VERSION)`
- New privileges: `COMPLETUDE_DOSSIER`, `ANALYSE_FINANCIERE`, `VISA_RC`, `COMITE_CREDIT`, `VISA_SF`, `DEBLOQUER_CREDIT`

The existing `CreditStatut` enum (`BROUILLON, SOUMIS, VALIDE_AGENT, VALIDE_COMITE, DEBLOQUE, SOLDE, REJETE`) and the existing `historique_visa_credit` table (`IDHISTORIQUE, ETAPE, STATUT_AVANT, STATUT_APRES, DATE_VISA, DECISION, COMMENTAIRE, UTILISATEUR, VERSION, idcredit FK`) are reused as-is — do **not** modify them.

The existing `CreditsService.transitionner(Long, String)` and its private `validateTransition()` should remain as a low-level, status-only transition (used internally by the new workflow service). **Don't break it.**

---

## 1. Architectural rules (non-negotiable)

1. **Spring Boot package convention:** new entities go under `com.microfina.entity`, new repos/services/controllers/DTOs go under `com.pfe.backend.{repository,service,controller,dto}`.
2. **Validation:** use `jakarta.validation` (`@NotNull`, `@Positive`, `@Size`, `@Valid`).
3. **Security:** every controller method gets `@PreAuthorize("hasAuthority('PRIV_<code>') or hasAuthority('ROLE_ADMIN')")` where `<code>` is the privilege code (e.g. `PRIV_VISA_RC`). The Spring Security setup already prefixes seeded privilege codes with `PRIV_`. Confirm by reading `MicrofinaUserDetailsService.java` before writing the first `@PreAuthorize`.
4. **Error model:** throw `ResponseStatusException(HttpStatus.X, "message")` for business-rule violations (404, 409 conflict on illegal transition, 422 on missing analyse). The existing global handler already catches these.
5. **Audit:** every workflow action **must** append a row to `historique_visa_credit` (one row per transition) with `ETAPE`, `STATUT_AVANT`, `STATUT_APRES`, `DATE_VISA = LocalDate.now()`, `DECISION`, `COMMENTAIRE`, `UTILISATEUR = SecurityContextHolder.getContext().getAuthentication().getName()`.
6. **Optimistic locking:** all entity writes go through JPA `save()` so the `@Version` column does its job.
7. **No new dependencies in `pom.xml` or `package.json`** — everything you need is already there.
8. **French naming for domain methods** (the codebase mixes French + English — match the surrounding code: services and method names French, technical infra English).
9. **DTOs are records** (Java 21) — see `LoginRequest.java` and `LoginResponse.java` for style.

---

## 2. State machine to implement

The workflow has **two parallel state variables** on `Credits`:

| Step                 | `STATUT` (legacy)  | `etape_courante` (new) | Required privilege   | Required role             |
|----------------------|--------------------|-----------------------|----------------------|---------------------------|
| Create               | `BROUILLON`        | `SAISIE`              | `CREATE_CREDIT`      | AGENT_CREDIT              |
| Submit dossier       | `SOUMIS`           | `COMPLETUDE`          | `CREATE_CREDIT`      | AGENT_CREDIT              |
| Validate completeness| `SOUMIS`           | `ANALYSE_FINANCIERE`  | `COMPLETUDE_DOSSIER` | COMITE_CREDIT             |
| Submit analysis      | `SOUMIS`           | `VISA_RC`             | `ANALYSE_FINANCIERE` | AGENT_CREDIT              |
| Visa RC              | `VALIDE_AGENT`     | `COMITE`              | `VISA_RC`            | COMITE_CREDIT             |
| Decision comité APPROVE | `VALIDE_COMITE` | `VISA_SF`             | `COMITE_CREDIT`      | COMITE_CREDIT             |
| Decision comité REJECT  | `REJETE`        | `REJETE`              | `COMITE_CREDIT`      | COMITE_CREDIT             |
| Visa SF              | `VALIDE_COMITE`    | `DEBLOCAGE_PENDING`   | `VISA_SF`            | COMPTABLE                 |
| Débloquer            | `DEBLOQUE`         | `DEBLOQUE`            | `DEBLOQUER_CREDIT`   | CAISSIER                  |
| Clôturer (existing)  | `SOLDE`            | `CLOTURE`             | (existing `transitionner`) | —                   |

**Reject is allowed at any non-terminal step** — sets `STATUT = REJETE`, `etape_courante = REJETE`, requires the privilege of the *current* step.

**Disbursement (`débloquer`)** is the only transition that has side-effects beyond status: it must, in this order:
1. Re-fetch credit with pessimistic lock to avoid double-disbursement.
2. Set `MONTANT_DEBLOQUER`, `DATE_DEBLOCAGE`, `SOLDE_CAPITAL = MONTANT_DEBLOQUER`, `SOLDE_INTERET = 0`, `SOLDE_PENALITE = 0`.
3. Generate the `Amortp` schedule via the **existing** `AmortissementCalculator` — find the right method by reading that class. Do not duplicate annuity logic.
4. Append the historique row.
5. *Skip* the comptabilité posting and guarantee-locking for now — the user has not connected `ComptabiliteService` to disbursement yet. Leave a `// TODO §3.1.x: post comptabilité entries` comment so it's findable.

---

## 3. Backend deliverables (Task #22)

### 3.1 Entity — `AnalyseFinanciere`

`src/main/java/com/microfina/entity/AnalyseFinanciere.java`

- `@Entity @Table(name = "analyse_financiere")`
- `@Id @GeneratedValue(IDENTITY) Long idAnalyse` mapped to `ID_ANALYSE`
- `@OneToOne(fetch = LAZY) @JoinColumn(name = "credit_id", referencedColumnName = "IDCREDIT", unique = true) Credits credit`
- `@Enumerated(STRING)` for `TypeAnalyse { INFERIEURE, SUPERIEURE }` and `AvisAgent { FAVORABLE, DEFAVORABLE, RESERVE }` (nullable)
- `BigDecimal` for all amount fields
- `LocalDateTime dateAnalyse` (column `DATE_ANALYSE`)
- `@Version Integer version`
- Mirror the column list from `P12-002-CREATE-TABLE-analyse_financiere.xml` exactly.

### 3.2 Extend `Credits`

Add a single field:
```java
@Column(name = "etape_courante", length = 30, nullable = false)
private String etapeCourante = "SAISIE";
```
Plus getter/setter. **Do not** introduce a new enum on the Java side yet — string is fine. (The frontend will validate the values; introducing the enum is a separate refactor that can come in §3.1 follow-up.)

### 3.3 Enum — `Etape`

`src/main/java/com/microfina/entity/Etape.java`

A pure enum with the 10 values listed above plus a static `next(Etape current, boolean approve)` helper. Do **not** annotate `Credits.etapeCourante` with this enum yet (string column for now); the workflow service will use the enum internally and translate.

### 3.4 Repository — `AnalyseFinanciereRepository`

`backend/src/main/java/com/pfe/backend/repository/AnalyseFinanciereRepository.java`

- `extends JpaRepository<AnalyseFinanciere, Long>`
- `Optional<AnalyseFinanciere> findByCredit_IdCredit(Long idCredit);`

### 3.5 Repository — extend `HistoriqueVisaCreditRepository`

If it doesn't exist, create:
`backend/src/main/java/com/pfe/backend/repository/HistoriqueVisaCreditRepository.java`
- `extends JpaRepository<HistoriqueVisaCredit, Long>`
- `List<HistoriqueVisaCredit> findByCredit_IdCreditOrderByDateVisaAscIdHistoriqueAsc(Long idCredit);`

### 3.6 DTOs (records)

`backend/src/main/java/com/pfe/backend/dto/`

- `AnalyseFinanciereDTO(Long idAnalyse, Long creditId, String typeAnalyse, BigDecimal revenusMensuels, BigDecimal chargesMensuelles, BigDecimal capaciteRemboursement, BigDecimal ratioEndettement, BigDecimal totalActif, BigDecimal totalPassif, String indicateursJson, String commentaire, String avisAgent, LocalDateTime dateAnalyse, String utilisateur, Integer version)`
- `AnalyseFinanciereCreateRequest(@NotNull @Positive BigDecimal revenusMensuels, @NotNull @Positive BigDecimal chargesMensuelles, BigDecimal totalActif, BigDecimal totalPassif, String indicateursJson, String commentaire, String avisAgent)`
- `WorkflowDecisionRequest(@Size(max=500) String commentaire)` — used by all approve/reject endpoints
- `WorkflowTimelineEntry(String etape, String statutAvant, String statutApres, LocalDate dateVisa, String decision, String commentaire, String utilisateur)` — projection used by the timeline endpoint
- `DeblocageRequest(@NotNull @Positive BigDecimal montantDeblocage, @NotNull LocalDate datePremiereEcheance, @NotNull String periodicite, @NotNull @Positive Integer nombreEcheance, Integer delaiGrace)`

### 3.7 Service — `CreditWorkflowService`

`backend/src/main/java/com/pfe/backend/service/CreditWorkflowService.java`

`@Service @Transactional` — eight public methods, one per transition:

```java
Credits soumettreDossier(Long idCredit);                                              // SAISIE → COMPLETUDE
Credits validerCompletude(Long idCredit, WorkflowDecisionRequest req);                // COMPLETUDE → ANALYSE_FINANCIERE
AnalyseFinanciere enregistrerAnalyseFinanciere(Long idCredit, AnalyseFinanciereCreateRequest req);  // ANALYSE_FINANCIERE → VISA_RC, computes ratio + capacite
Credits viserResponsableCredit(Long idCredit, WorkflowDecisionRequest req);           // VISA_RC → COMITE
Credits decisionComite(Long idCredit, boolean approuve, WorkflowDecisionRequest req); // COMITE → VISA_SF or REJETE
Credits viserServiceFinancier(Long idCredit, WorkflowDecisionRequest req);            // VISA_SF → DEBLOCAGE_PENDING
Credits debloquer(Long idCredit, DeblocageRequest req);                               // DEBLOCAGE_PENDING → DEBLOQUE (with side effects, see §2)
Credits rejeter(Long idCredit, WorkflowDecisionRequest req);                          // any → REJETE (sets both STATUT and etape_courante)
List<WorkflowTimelineEntry> getTimeline(Long idCredit);                                // read historique_visa_credit
```

Internal helpers:
- `private void appendHistorique(Credits c, String etape, CreditStatut avant, CreditStatut apres, String decision, String commentaire)`
- `private void requireEtape(Credits c, String expectedEtape)` — throws 409 if wrong
- `private String currentUser()` — pulls from `SecurityContextHolder`

The ratio computation in `enregistrerAnalyseFinanciere`:
```java
ratio = charges / revenus     // null-safe, BigDecimal.divide(revenus, 4, HALF_UP)
capacite = revenus - charges
```

### 3.8 Controller — `CreditWorkflowController`

`backend/src/main/java/com/pfe/backend/controller/CreditWorkflowController.java`

Mount at `/api/v1/credits/{idCredit}/workflow`. Endpoints:

| Method | Path                  | Privilege            | Body                              |
|--------|-----------------------|----------------------|-----------------------------------|
| POST   | `/soumettre`          | `CREATE_CREDIT`      | none                              |
| POST   | `/completude`         | `COMPLETUDE_DOSSIER` | `WorkflowDecisionRequest`         |
| POST   | `/analyse`            | `ANALYSE_FINANCIERE` | `AnalyseFinanciereCreateRequest`  |
| POST   | `/visa-rc`            | `VISA_RC`            | `WorkflowDecisionRequest`         |
| POST   | `/comite/approuver`   | `COMITE_CREDIT`      | `WorkflowDecisionRequest`         |
| POST   | `/comite/rejeter`     | `COMITE_CREDIT`      | `WorkflowDecisionRequest`         |
| POST   | `/visa-sf`            | `VISA_SF`            | `WorkflowDecisionRequest`         |
| POST   | `/debloquer`          | `DEBLOQUER_CREDIT`   | `DeblocageRequest`                |
| POST   | `/rejeter`            | (any of the above; use SpEL expression `hasAnyAuthority('PRIV_COMPLETUDE_DOSSIER','PRIV_VISA_RC','PRIV_COMITE_CREDIT','PRIV_VISA_SF') or hasAuthority('ROLE_ADMIN')`) | `WorkflowDecisionRequest` |
| GET    | `/timeline`           | authenticated        | —                                 |
| GET    | `/analyse`            | authenticated        | — returns the latest `AnalyseFinanciereDTO` or 404 |
| GET    | `/comite/pending`     | `COMITE_CREDIT`      | — list of credits where `etape_courante = 'COMITE'` (use `CreditsRepository`) |

Add `@Tag(name = "Credit Workflow", description = "...")` and `@Operation(summary = "...")` on each method (springdoc).

### 3.9 Tests — `CreditWorkflowServiceTest`

`backend/src/test/java/com/pfe/backend/service/CreditWorkflowServiceTest.java`

`@SpringBootTest @Transactional` (let each test roll back). Cover at minimum:

1. **Happy path:** create credit → soumettre → completude → analyse → visa-rc → comité approve → visa-sf → débloquer. Assert final `STATUT == DEBLOQUE`, `etape_courante == DEBLOQUE`, 8 rows in `historique_visa_credit`, 1 row in `analyse_financiere`, `Amortp` rows generated.
2. **Reject at comité:** advance to COMITE, call `decisionComite(false, ...)`, assert `STATUT == REJETE`, `etape_courante == REJETE`, terminal.
3. **Illegal transition:** call `viserResponsableCredit` while `etape_courante == COMPLETUDE` → expect `ResponseStatusException(409)`.
4. **Missing analysis:** advance to VISA_RC without calling `enregistrerAnalyseFinanciere` → if you choose to enforce this in `viserResponsableCredit`, test it (otherwise skip).
5. **Backfill check:** insert a credit with legacy `STATUT = VALIDE_AGENT` and `etape_courante = SAISIE` (simulating pre-Phase-12 row before backfill), assert that the workflow service refuses to advance it without first running the backfill. *(Optional — skip if it complicates setup.)*

Use `@WithMockUser(authorities = {"PRIV_..."})` to exercise authorization on at least one happy-path and one denied case.

---

## 4. Frontend deliverables (Task #23)

### 4.1 Service

`frontend/src/app/services/credit-workflow.service.ts`

`@Injectable({ providedIn: 'root' })` with one method per backend endpoint, returning `Observable<...>`. Use the existing `HttpClient` injection pattern from `frontend/src/app/services/credits.service.ts` (read it first to match the auth interceptor style).

### 4.2 Models

`frontend/src/app/models/credit-workflow.model.ts`

TypeScript interfaces matching the DTOs above. Define a string-literal union `Etape` and a helper `etapeLabel(e: Etape): string` returning the French label (`Saisie`, `Complétude`, `Analyse financière`, etc.).

### 4.3 Pages

1. **`/credits/:id/workflow`** — `credit-workflow-page.component.ts` (standalone). Layout:
   - Top: credit summary (number, member, requested amount, current `etape_courante` badge).
   - Middle: vertical timeline showing the 10 etapes; the current one highlighted, completed ones with a checkmark and the historique row's date/user/comment.
   - Bottom: a single "Action" button that switches based on `etape_courante` and the user's privileges. Show *only* the action available to the current user (read `auth.service.ts` for the privilege list helper).
   - Each action opens a modal with the relevant form (decision-comment for visa actions, full analyse-financiere form for ANALYSE_FINANCIERE, full deblocage form for DEBLOCAGE_PENDING).
2. **`/credits/comite`** — `credits-comite-page.component.ts` — table of credits where `etape_courante == 'COMITE'` (call `GET /workflow/comite/pending`), columns: numéro, membre, montant demandé, date soumission, agent. Row click navigates to `/credits/:id/workflow`.

### 4.4 Routes

Register both routes lazily in `frontend/src/app/app.routes.ts` following the existing pattern. Wrap with `canActivate: [authGuard]`. The comité route additionally needs a privilege guard — if `privilegeGuard('COMITE_CREDIT')` exists use it, otherwise add the privilege check inside the component.

### 4.5 Sidebar entry

Add a **"Comité crédit"** entry in `frontend/src/app/layout/sidebar/sidebar.component.ts` (or whatever the sidebar file is — read it first), pointing at `/credits/comite`. Show only when the user has `COMITE_CREDIT`.

Also add a **"Workflow"** action button on the existing credit detail/list page that links to `/credits/:id/workflow`.

### 4.6 Tests (light)

If there are existing `*.spec.ts` files, add one for `credit-workflow.service.ts` (mocked HttpClient) and one smoke test for `credit-workflow-page.component.ts` rendering. If no Angular test infra is wired up, skip — note it in the verification report.

---

## 5. Verification (Task #24)

Run, in order:

1. `cd backend && ./mvnw -DskipTests=false test` — must be green.
2. `./mvnw spring-boot:run` (let it start), then in another terminal:
   - `curl -s -X POST http://localhost:8080/api/auth/login -H 'Content-Type: application/json' -d '{"username":"admin","password":"Admin@1234"}'` → grab token.
   - Smoke-test the happy path with curl: create credit (existing endpoint), then walk through `POST /api/v1/credits/{id}/workflow/soumettre`, `…/completude`, …, `…/debloquer`. Save the responses.
3. `cd frontend && npm run build` — must succeed.
4. Update `CHANGELOG.md` at the repo root with a new "Phase 12 — Multi-level Credit Approval Workflow" section listing the new endpoints, entities, and frontend pages.
5. Append a "§3.1 — DONE" entry to `PROMPT-FEATURE-PARITY.md` under §5 Priority Order.

If any step fails: stop, report the exact error, and propose a fix — don't paper over it.

---

## 6. Reference files to read before writing code

Read these in this order to absorb conventions before producing any new file:

1. `src/main/java/com/microfina/entity/Credits.java` — to see the field naming style and how it interacts with FKs.
2. `src/main/java/com/microfina/entity/HistoriqueVisaCredit.java` — already a pure-Java entity (no Lombok); match its style exactly.
3. `src/main/java/com/microfina/entity/CreditStatut.java` — for `isEditable()`, `isTerminal()`, etc. helpers.
4. `backend/src/main/java/com/pfe/backend/service/CreditsService.java` — for `transitionner()` / `validateTransition()` style; reuse where possible.
5. `backend/src/main/java/com/pfe/backend/controller/CreditsController.java` — for `@PreAuthorize`, `@Operation`, response wrapping.
6. `backend/src/main/java/com/pfe/backend/service/AmortissementCalculator.java` — find the method that produces `Amortp` rows from a `Credits` entity. Use it in `debloquer`.
7. `backend/src/main/java/com/microfina/security/MicrofinaUserDetailsService.java` — confirm whether privileges are exposed as `PRIV_X` or just `X` so `@PreAuthorize` strings match.
8. `frontend/src/app/services/credits.service.ts` and `frontend/src/app/app.routes.ts` — frontend conventions.

---

## 7. Acceptance criteria (exit gate)

- [ ] Liquibase applies cleanly on a fresh DB (`./mvnw liquibase:update`) and on an existing one with rows.
- [ ] All 8 workflow service methods + 11 controller endpoints exist and pass their unit tests.
- [ ] Backend `mvn test` is green.
- [ ] Frontend `ng build` (or `npm run build`) succeeds with zero errors.
- [ ] A logged-in **AGENT_CREDIT** user can advance a credit from SAISIE → VISA_RC.
- [ ] A logged-in **COMITE_CREDIT** user can advance VISA_RC → COMITE → VISA_SF, or reject.
- [ ] A logged-in **COMPTABLE** user can advance VISA_SF → DEBLOCAGE_PENDING.
- [ ] A logged-in **CAISSIER** user can disburse, and an `Amortp` row set is generated.
- [ ] `historique_visa_credit` has one row per transition with the correct `STATUT_AVANT`/`STATUT_APRES`/`UTILISATEUR`.
- [ ] `CHANGELOG.md` and `PROMPT-FEATURE-PARITY.md` are updated.
- [ ] No new dependencies were added.
- [ ] No existing tests were modified — only added.

---

## 8. Out of scope for this prompt (do **not** start them)

- §3.2 Tontine, §3.3 Mobile Money, §3.4 Micro-Assurance, …, §3.17 Cheque advanced workflows.
- The 6 product decisions still pending with the supervisor (i18n scope, tontine scope, mobile money operators, assurance partners, PCN-IMF source, reporting periodicity).
- Connecting the disbursement to comptabilité posting and guarantee-locking — leave the `// TODO §3.1.x` comment.
- Refactoring `Credits.etapeCourante` from String to enum — schedule as a follow-up.

When §3.1 is complete and verified, post a short summary and stop. The user will then either supply supervisor answers or pick the next §3.x to tackle.
