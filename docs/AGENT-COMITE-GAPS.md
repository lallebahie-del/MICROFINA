# Pages Agent & Comité — Audit des manques

Document de cadrage avant implémentation. Référence : workflow crédit défini dans
[CreditWorkflowController.java](../backend/src/main/java/com/pfe/backend/controller/CreditWorkflowController.java)
(10 étapes : `SAISIE → COMPLETUDE → ANALYSE_FINANCIERE → VISA_RC → COMITE → VISA_SF → DEBLOCAGE_PENDING → DEBLOQUE` ; états terminaux `REJETE` / `CLOTURE`).

---

## 1. État actuel

### 1.1 Page Comité — `/credits/comite` ✅ existe

Fichiers : [credits-comite-page.component.ts](../frontend/src/app/pages/credits/credits-comite-page.component.ts) + [.html](../frontend/src/app/pages/credits/credits-comite-page.component.html)

**Ce qui est en place :**
- Liste des dossiers en attente comité (endpoint `GET /api/v1/credits/workflow/comite/pending`).
- Colonnes : `numCredit`, `statut`, `etapeCourante`.
- Actions : **Détail** (lien `/credits/{id}`), **Workflow** (lien `/credits/{id}/workflow`), **✓ Approuver** + **✗ Rejeter** avec champ commentaire (motif obligatoire pour rejet).

**Endpoint backend exposé :**
```
GET  /api/v1/credits/workflow/comite/pending          → List<Map<idCredit, numCredit, statut, etapeCourante>>
POST /api/v1/credits/{id}/workflow/comite/approuver   → décision OK
POST /api/v1/credits/{id}/workflow/comite/rejeter     → décision KO + motif
```

### 1.2 Page Agent — `/credits/agent` ❌ n'existe pas

Aucune route, aucun composant, aucune entrée menu pour l'agent de crédit.

L'agent doit aujourd'hui passer par :
- `/credits` (liste générale, sans filtre par étape de workflow)
- ou `/credits/{id}/workflow` (page détail qui expose **toutes** les transitions, sans dashboard d'ensemble)

---

## 2. Manques identifiés

### 2.1 Backend — `CreditWorkflowController`

| # | Manque | Détail | Impact |
|---|--------|--------|--------|
| **B1** | Endpoint `GET /workflow/agent/pending` | Aucun équivalent du `comite/pending` pour les étapes agent (`COMPLETUDE`, `ANALYSE_FINANCIERE`). | Bloquant pour la page Agent |
| **B2** | DTO `WorkflowCreditSummary` trop pauvre | Renvoie uniquement `{idCredit, numCredit, statut, etapeCourante}`. Manquent `nomMembre`, `prenomMembre`, `numMembre`, `montantDemande`, `montantAccorde`, `codeAgence`, `nomAgence`, `dateDemande`, `nomAgent`. | Pages Agent et Comité affichent `—` partout |
| **B3** | Pas d'endpoint `GET /workflow/queue/{etape}` générique | Si on veut un dashboard par étape (ex: VISA_RC, VISA_SF, DEBLOCAGE_PENDING) il faudrait un seul endpoint paramétrable. | Optionnel, mais évite la prolifération |
| **B4** | Pas d'endpoint d'agrégat (`GET /workflow/stats`) | Compteurs par étape (X dossiers en complétude, Y en analyse, Z au comité…). | Bonus pour widget dashboard |

### 2.2 Frontend — Page Comité

| # | Manque | Détail |
|---|--------|--------|
| **C1** | Identité du membre | `nomMembre`, `prenomMembre`, `numMembre` non affichés (DTO backend pauvre — voir B2). |
| **C2** | Montant demandé | Critère #1 pour décider — actuellement absent. |
| **C3** | Agence d'origine | Permet au comité de filtrer par agence ou voir d'où vient le dossier. |
| **C4** | Date de soumission au comité | Délai de traitement non visible (priorisation impossible). |
| **C5** | Aperçu de l'analyse financière | L'agent a déjà saisi `revenusMensuels`, `chargesMensuelles`, `totalActif`, `totalPassif`, `commentaire`, `avisAgent` — non lu sur la page comité. Ratio capacité de remboursement non calculé. |
| **C6** | Aperçu des garanties | Liste des garanties attachées au crédit (type, valeur estimée, taux de couverture). |
| **C7** | Timeline / historique | Aucun fil chronologique des décisions précédentes (qui a validé la complétude, qui a saisi l'analyse, qui a fait le visa RC). |
| **C8** | Filtre / recherche | Pas de filtre par agence, par tranche de montant, par date. |
| **C9** | Statistique comité | Pas de compteur (X dossiers en attente, montant cumulé). |
| **C10** | Actions par lot | Approuver/rejeter plusieurs dossiers en une fois (uniquement individuel actuellement). *Optionnel, à confirmer.* |

### 2.3 Frontend — Page Agent (à créer)

| # | À implémenter | Détail |
|---|---------------|--------|
| **A1** | Route `/credits/agent` | + entrée menu sous `CRÉDIT` (lien "Mes dossiers"). |
| **A2** | Sections par étape | Onglets ou sections regroupées : **Complétude** / **Analyse financière** / **Visa RC** *(selon privilèges du user)*. |
| **A3** | Cartes synthèse | Compteur de dossiers par étape + montant cumulé en attente. |
| **A4** | Tableau des dossiers | Colonnes : numCredit, membre, montant demandé, agence, date soumission, étape, jours d'attente. |
| **A5** | Actions inline | Selon l'étape courante : **Valider complétude** / **Saisir analyse** / **Visa RC** / **Rejeter** (avec commentaire). Le formulaire d'analyse financière (revenus/charges/actif/passif) doit être saisissable directement depuis le dashboard agent. |
| **A6** | Filtre / recherche | Par agence (si l'agent gère plusieurs), par membre. |
| **A7** | Timeline | Vue compacte de l'historique pour les dossiers ouverts. |
| **A8** | Lien vers détail | Bouton "Workflow complet" pour chaque dossier (vers `/credits/{id}/workflow`). |
| **A9** | Privilèges | Affichage conditionnel des sections selon `PRIV_COMPLETUDE_DOSSIER`, `PRIV_ANALYSE_FINANCIERE`, `PRIV_VISA_RC`. |

---

## 3. Plan d'implémentation proposé

### 3.1 Backend (priorité — débloque tout le reste)

**Étape 1 — Enrichir le DTO commun**
- Créer un `WorkflowCreditSummaryDTO` avec : `idCredit, numCredit, statut, etapeCourante, nomMembre, prenomMembre, numMembre, montantDemande, codeAgence, nomAgence, dateDemande, joursDansEtape`.
- Refactorer `CreditWorkflowController.creditSummary()` pour renvoyer ce DTO enrichi.
- Adapter `comitePending()` pour utiliser le nouveau DTO.

**Étape 2 — Endpoints Agent**
```
GET /api/v1/credits/workflow/agent/pending
    → dossiers en COMPLETUDE ou ANALYSE_FINANCIERE
    PreAuthorize : PRIV_COMPLETUDE_DOSSIER OR PRIV_ANALYSE_FINANCIERE

GET /api/v1/credits/workflow/queue/{etape}
    → dossiers à n'importe quelle étape (VISA_RC, VISA_SF, DEBLOCAGE_PENDING)
    PreAuthorize : selon étape (lookup table)
```

**Étape 3 — Stats (optionnel)**
```
GET /api/v1/credits/workflow/stats
    → { COMPLETUDE: 5, ANALYSE_FINANCIERE: 3, VISA_RC: 2, COMITE: 4, ... }
```

### 3.2 Frontend Comité — enrichissement

1. Mettre à jour `WorkflowCreditSummary` (model TS) avec les nouveaux champs.
2. Réécrire `credits-comite-page.component.html` :
   - Ajouter colonnes : Membre, Montant demandé, Agence, Date.
   - Bouton **Voir analyse** (popover/section pliable) qui appelle `GET /workflow/{id}/analyse`.
   - Bouton **Voir garanties** (popover) qui appelle `GET /api/v1/garanties/credit/{id}`.
   - Bouton **Timeline** qui appelle `GET /workflow/{id}/timeline`.
3. Filter-bar : recherche par membre, filtre par agence.
4. Bandeau de stats en haut (nb dossiers, montant cumulé en attente).

### 3.3 Frontend Agent — création

1. Nouveau composant `credits-agent-page.component.ts` + `.html`.
2. Route `/credits/agent` dans `app.routes.ts`.
3. Entrée menu dans `sidebar.html` sous CRÉDIT (libellé : "Mes dossiers").
4. Structure :
   - Header avec compteurs par étape.
   - Tableau filtré, regroupé visuellement par étape (badges colorés).
   - Modal d'analyse financière inline (formulaire avec `revenusMensuels`, `chargesMensuelles`, `totalActif`, `totalPassif`, `commentaire`, `avisAgent`).
   - Modal de validation complétude / visa RC (commentaire libre).
   - Modal de rejet (motif obligatoire).

### 3.4 Privilèges affectés

| Privilège | Page Agent | Page Comité |
|-----------|------------|-------------|
| `PRIV_COMPLETUDE_DOSSIER` | ✓ accès + action "Valider complétude" | — |
| `PRIV_ANALYSE_FINANCIERE` | ✓ accès + action "Saisir analyse" | lecture seule |
| `PRIV_VISA_RC` | ✓ accès + action "Visa RC" | — |
| `PRIV_COMITE_CREDIT` | — | ✓ accès + action "Approuver/Rejeter" |
| `PRIV_VIEW_REPORTS` | lecture | lecture |

---

## 4. Estimation effort

| Lot | Description | Effort |
|-----|-------------|--------|
| **B1+B2** | DTO enrichi + endpoint `agent/pending` | ~1h backend |
| **B3** | Endpoint queue paramétrable | ~30min |
| **B4** | Stats agrégées | ~30min |
| **C** | Refonte page Comité (tous items C1→C9) | ~1h30 frontend |
| **A** | Création page Agent complète | ~2h frontend |
| **Tests + validation** | smoke tests sur backend, click-through frontend | ~30min |

**Total estimé : ~6h** pour livrer les 2 pages complètes avec backend enrichi.

---

## 5. Ce qui n'est PAS dans le scope

- Génération de PDF/Word de la fiche dossier (peut être ajoutée via le module `/exports` existant ultérieurement).
- Notifications push / email lors d'un changement d'étape.
- Workflow paramétrable (les étapes sont codées en dur dans `CreditWorkflowService`).
- Délégation / réaffectation entre agents.
- Tableau de bord croisé multi-agents (vue manager).

---

## 6. Validation requise

Merci de répondre :

1. **Périmètre OK** ? (les manques A/B/C reflètent ton besoin réel)
2. **Endpoints backend** : on enrichit le DTO commun (`B1+B2`) ou on garde les pages mockées avec `—` pour l'instant ?
3. **Queue paramétrable** (B3) : utile maintenant, ou on se limite à `agent/pending` + `comite/pending` séparés ?
4. **Stats agrégées** (B4) : nice-to-have ou indispensable pour l'UX dashboard ?
5. **Actions Agent** : on intègre les formulaires (analyse financière) inline dans le dashboard Agent, ou on délègue à la page workflow détail (clic → /credits/{id}/workflow) ?
6. **Filtres / recherche** : pertinents (multi-agence) ou inutiles (un agent = une agence) ?

Une fois validé, j'implémente backend → frontend Comité → frontend Agent → build.
