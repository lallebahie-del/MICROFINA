# Fonctionnalités du backend MICROFINA

Ce document décrit **l’ensemble des capacités exposées par l’API** du module `backend` (Spring Boot 3.3, Java 21). Les chemins sont relatifs à la base de l’application (ex. `http://localhost:8080`).

**Sécurité.** Presque toutes les routes sous `/api/**` exigent un **JWT** (`Authorization: Bearer <token>`) et des **privilèges** métier (`PRIV_*` ou rôles), sauf mention contraire. Les annotations exactes sont sur chaque méthode des contrôleurs.

**Référence technique.** Contrôleurs Java : `backend/src/main/java/com/pfe/backend/controller/`.

---

## 1. Authentification et session

| Méthode | Chemin | Rôle |
|--------|--------|------|
| POST | `/api/auth/login` | Connexion, obtention du JWT |
| GET | `/api/auth/me` | Utilisateur courant à partir du token |

---

## 2. Santé et supervision (Actuator)

| Méthode | Chemin | Rôle |
|--------|--------|------|
| GET | `/actuator/health` | Santé de l’application (public) |
| GET | `/actuator/` | Redirection vers `/actuator` (contrôleur dédié) |

Détails métier possibles via indicateurs personnalisés (ex. compteurs membres / utilisateurs).

---

## 3. Membres et référentiels membres

### 3.1 Membres (`/api/v1/membres`)

| Méthode | Chemin | Fonctionnalité |
|--------|--------|----------------|
| GET | `/api/v1/membres` | Liste / recherche des membres |
| GET | `/api/v1/membres/{numMembre}` | Détail d’un membre |
| POST | `/api/v1/membres` | Création |
| PUT | `/api/v1/membres/{numMembre}` | Mise à jour complète |
| PATCH | `/api/v1/membres/{numMembre}/desactiver` | Désactivation |
| DELETE | `/api/v1/membres/{numMembre}` | Suppression |
| POST | `/api/v1/membres/{numMembre}/photo` | Upload photo (multipart) |
| GET | `/api/v1/membres/{numMembre}/photo` | Récupération photo |

Stockage disque configuré via `app.photos.dir` (`application.properties`).

### 3.2 Types de membre (`/api/v1/types-membre`)

| Méthode | Chemin | Fonctionnalité |
|--------|--------|----------------|
| GET | `/api/v1/types-membre` | Liste des types |
| GET | `/api/v1/types-membre/{typeMembre}` | Détail |

---

## 4. Agences et banques

### 4.1 Agences (`/api/v1/agences`)

| Méthode | Chemin | Fonctionnalité |
|--------|--------|----------------|
| GET | `/api/v1/agences` | Liste |
| GET | `/api/v1/agences/{code}` | Détail |
| GET | `/api/v1/agences/sieges` | Sièges |

### 4.2 Banques (`/api/v1/banques`)

| Méthode | Chemin | Fonctionnalité |
|--------|--------|----------------|
| GET | `/api/v1/banques` | Liste |
| GET | `/api/v1/banques/actives` | Banques actives |
| GET | `/api/v1/banques/{codeBanque}` | Détail |
| POST | `/api/v1/banques` | Création |
| PUT | `/api/v1/banques/{codeBanque}` | Mise à jour |
| DELETE | `/api/v1/banques/{codeBanque}` | Suppression |

---

## 5. Produits et parts sociales

### 5.1 Produits crédit (`/api/v1/produits-credit`)

| Méthode | Chemin | Fonctionnalité |
|--------|--------|----------------|
| GET | `/api/v1/produits-credit` | Liste |
| GET | `/api/v1/produits-credit/actifs` | Produits actifs |
| GET | `/api/v1/produits-credit/{numProduit}` | Détail |
| POST | `/api/v1/produits-credit` | Création |
| PUT | `/api/v1/produits-credit/{numProduit}` | Mise à jour |
| DELETE | `/api/v1/produits-credit/{numProduit}` | Suppression |

### 5.2 Parts sociales (`/api/v1/parts-sociales`)

| Méthode | Chemin | Fonctionnalité |
|--------|--------|----------------|
| GET | `/api/v1/parts-sociales` | Liste |
| GET | `/api/v1/parts-sociales/actifs` | Actifs |
| GET | `/api/v1/parts-sociales/{id}` | Détail |
| POST | `/api/v1/parts-sociales` | Création |
| PUT | `/api/v1/parts-sociales/{id}` | Mise à jour |
| DELETE | `/api/v1/parts-sociales/{id}` | Suppression |

---

## 6. Crédits (cycle de vie, workflow, règlements)

**Préfixe commun** : `/api/v1/credits` (plusieurs contrôleurs).

### 6.1 CRUD et transitions (`CreditsController`)

| Méthode | Chemin | Fonctionnalité |
|--------|--------|----------------|
| GET | `/api/v1/credits` | Liste |
| GET | `/api/v1/credits/{id}` | Détail |
| POST | `/api/v1/credits` | Création |
| PUT | `/api/v1/credits/{id}` | Mise à jour |
| POST | `/api/v1/credits/{id}/transitionner` | Changement d’état métier |
| DELETE | `/api/v1/credits/{id}` | Suppression |

### 6.2 Workflow (`CreditWorkflowController`)

| Méthode | Chemin | Fonctionnalité |
|--------|--------|----------------|
| POST | `/api/v1/credits/{idCredit}/workflow/soumettre` | Soumission |
| POST | `/api/v1/credits/{idCredit}/workflow/completude` | Complétude |
| POST | `/api/v1/credits/{idCredit}/workflow/analyse` | Analyse |
| POST | `/api/v1/credits/{idCredit}/workflow/visa-rc` | Visa RC |
| POST | `/api/v1/credits/{idCredit}/workflow/comite/approuver` | Comité approuve |
| POST | `/api/v1/credits/{idCredit}/workflow/comite/rejeter` | Comité rejette |
| POST | `/api/v1/credits/{idCredit}/workflow/visa-sf` | Visa SF |
| POST | `/api/v1/credits/{idCredit}/workflow/debloquer` | Déblocage des fonds |
| POST | `/api/v1/credits/{idCredit}/workflow/rejeter` | Rejet |
| GET | `/api/v1/credits/{idCredit}/workflow/timeline` | Frise / historique |
| GET | `/api/v1/credits/{idCredit}/workflow/analyse` | Détail analyse |
| GET | `/api/v1/credits/workflow/comite/pending` | Dossiers en attente comité |

### 6.3 Paiements et amortissement (`CreditPaymentsController`)

| Méthode | Chemin | Fonctionnalité |
|--------|--------|----------------|
| GET | `/api/v1/credits/{idCredit}/amortp` | Plan d’amortissement |
| GET | `/api/v1/credits/{idCredit}/amortissement/preview` | Aperçu amortissement |

### 6.4 Règlements (`ReglementController`)

| Méthode | Chemin | Fonctionnalité |
|--------|--------|----------------|
| POST | `/api/v1/credits/{idCredit}/remboursements/caisse` | Remboursement en caisse |

---

## 7. Simulation crédit

**Base** : `/api/v1/simulations`

| Méthode | Chemin | Fonctionnalité |
|--------|--------|----------------|
| POST | `/api/v1/simulations/credit` | Simulation (échéances / paramètres) |

---

## 8. Garanties et référentiel

### 8.1 Garanties (`/api/v1/garanties`)

| Méthode | Chemin | Fonctionnalité |
|--------|--------|----------------|
| POST | `/api/v1/garanties` | Création |
| GET | `/api/v1/garanties/{id}` | Détail |
| GET | `/api/v1/garanties/credit/{idCredit}` | Garanties d’un crédit |
| GET | `/api/v1/garanties/credit/{idCredit}/couverture` | Taux de couverture |
| PATCH | `/api/v1/garanties/{id}/liberer` | Mainlevée / libération |
| POST | `/api/v1/garanties/{id}/documents` | Upload document (multipart) |
| GET | `/api/v1/garanties/{id}/documents` | Liste des documents |
| GET | `/api/v1/garanties/{id}/documents/{docId}` | Téléchargement document |

### 8.2 Types de garantie (`/api/v1/referentiel/types-garantie`)

| Méthode | Chemin | Fonctionnalité |
|--------|--------|----------------|
| GET | `/api/v1/referentiel/types-garantie` | Liste |
| GET | `/api/v1/referentiel/types-garantie/{code}` | Détail |

---

## 9. Comptes épargne (`/api/v1/comptes-epargne`)

| Méthode | Chemin | Fonctionnalité |
|--------|--------|----------------|
| GET | `/api/v1/comptes-epargne` | Liste |
| GET | `/api/v1/comptes-epargne/{numCompte}` | Détail |
| GET | `/api/v1/comptes-epargne/membre/{numMembre}` | Comptes d’un membre |
| GET | `/api/v1/comptes-epargne/agence/{codeAgence}` | Comptes par agence |
| POST | `/api/v1/comptes-epargne` | Ouverture de compte |
| PUT | `/api/v1/comptes-epargne/{numCompte}` | Mise à jour (flags, taux, etc.) |
| DELETE | `/api/v1/comptes-epargne/{numCompte}` | Suppression |

---

## 10. Opérations de caisse (`/api/v1/operations-caisse`)

| Méthode | Chemin | Fonctionnalité |
|--------|--------|----------------|
| GET | `/api/v1/operations-caisse` | Liste |
| GET | `/api/v1/operations-caisse/{id}` | Détail |
| GET | `/api/v1/operations-caisse/agence/{codeAgence}` | Par agence |
| GET | `/api/v1/operations-caisse/compte/{numCompte}` | Par compte épargne |
| POST | `/api/v1/operations-caisse` | Création |
| PATCH | `/api/v1/operations-caisse/{id}/valider` | Validation |
| PATCH | `/api/v1/operations-caisse/{id}/annuler` | Annulation |

---

## 11. Opérations bancaires (`/api/v1/operations-banque`)

| Méthode | Chemin | Fonctionnalité |
|--------|--------|----------------|
| GET | `/api/v1/operations-banque` | Liste |
| GET | `/api/v1/operations-banque/{id}` | Détail |
| GET | `/api/v1/operations-banque/agence/{codeAgence}` | Par agence |
| GET | `/api/v1/operations-banque/compte-banque/{compteBanqueId}` | Par compte banque |
| POST | `/api/v1/operations-banque` | Création |
| PATCH | `/api/v1/operations-banque/{id}/valider` | Validation |
| PATCH | `/api/v1/operations-banque/{id}/annuler` | Annulation |

---

## 12. Carnets de chèques (`/api/v1/carnets-cheque`)

| Méthode | Chemin | Fonctionnalité |
|--------|--------|----------------|
| GET | `/api/v1/carnets-cheque` | Liste |
| GET | `/api/v1/carnets-cheque/{id}` | Détail |
| GET | `/api/v1/carnets-cheque/membre/{numMembre}` | Par membre |
| POST | `/api/v1/carnets-cheque` | Création / émission |
| PUT | `/api/v1/carnets-cheque/{id}` | Mise à jour |
| DELETE | `/api/v1/carnets-cheque/{id}` | Suppression |

---

## 13. Comptabilité (`/api/v1/comptabilite`)

| Méthode | Chemin | Fonctionnalité |
|--------|--------|----------------|
| GET | `/api/v1/comptabilite` | Liste des écritures / pièces |
| GET | `/api/v1/comptabilite/{id}` | Détail |
| GET | `/api/v1/comptabilite/agence/{codeAgence}` | Par agence |
| PATCH | `/api/v1/comptabilite/{id}/lettrer` | Lettrage |

---

## 14. Budgets (`/api/v1/budgets`)

| Méthode | Chemin | Fonctionnalité |
|--------|--------|----------------|
| GET | `/api/v1/budgets` | Liste |
| GET | `/api/v1/budgets/{id}` | Détail |
| GET | `/api/v1/budgets/exercice/{exercice}` | Par exercice |
| GET | `/api/v1/budgets/agence/{codeAgence}` | Par agence |
| POST | `/api/v1/budgets` | Création |
| PUT | `/api/v1/budgets/{id}` | Mise à jour |
| PATCH | `/api/v1/budgets/{id}/valider` | Validation |
| PATCH | `/api/v1/budgets/{id}/cloturer` | Clôture |

---

## 15. Reporting BCM / financier (`/api/v1/reporting`)

| Méthode | Chemin | Fonctionnalité |
|--------|--------|----------------|
| GET | `/api/v1/reporting/etat-credits` | État des crédits |
| GET | `/api/v1/reporting/ratios-bcm` | Ratios BCM |
| GET | `/api/v1/reporting/indicateurs` | Indicateurs agrégés |
| GET | `/api/v1/reporting/liste-clients` | Liste clients |
| GET | `/api/v1/reporting/balance-comptes` | Balance comptes |
| GET | `/api/v1/reporting/journal` | Journal |
| GET | `/api/v1/reporting/bilan` | Bilan |
| GET | `/api/v1/reporting/grand-livre` | Grand livre |
| GET | `/api/v1/reporting/compte-resultat` | Compte de résultat |
| GET | `/api/v1/reporting/tableau-financement` | Tableau de financement |

Ces endpoints s’appuient sur des requêtes SQL / services métier dédiés (voir `ReportingController`).

---

## 16. Exports fichiers (`/api/v1/export`)

Export Excel, PDF, Word, formats comptables (SAGE), etc. Exemples :

| Méthode | Chemin (extrait) | Fonctionnalité |
|--------|------------------|----------------|
| GET | `/api/v1/export/credits/excel` | Export crédits Excel |
| GET | `/api/v1/export/credits/pdf` | Export crédits PDF |
| GET | `/api/v1/export/credits/word` | Export crédits Word |
| GET | `/api/v1/export/ratios-bcm/excel` | Ratios Excel |
| GET | `/api/v1/export/ratios-bcm/pdf` | Ratios PDF |
| GET | `/api/v1/export/bilan/excel` | Bilan Excel |
| GET | `/api/v1/export/bilan/pdf` | Bilan PDF |
| GET | `/api/v1/export/bilan/word` | Bilan Word |
| GET | `/api/v1/export/rapport-financier/word` | Rapport Word |
| GET | `/api/v1/export/clients/pdf` | Clients PDF |
| GET | `/api/v1/export/indicateurs/excel` | Indicateurs Excel |
| GET | `/api/v1/export/liste-clients/excel` | Liste clients Excel |
| GET | `/api/v1/export/liste-clients/word` | Liste clients Word |
| GET | `/api/v1/export/balance-comptes/excel` | Balance Excel |
| GET | `/api/v1/export/balance-comptes/pdf` | Balance PDF |
| GET | `/api/v1/export/journal/excel` | Journal Excel |
| GET | `/api/v1/export/journal/pdf` | Journal PDF |
| GET | `/api/v1/export/journal/word` | Journal Word |
| GET | `/api/v1/export/grand-livre/excel` | Grand livre Excel |
| GET | `/api/v1/export/grand-livre/pdf` | Grand livre PDF |
| GET | `/api/v1/export/grand-livre/word` | Grand livre Word |
| GET | `/api/v1/export/compte-resultat/excel` | CR Excel |
| GET | `/api/v1/export/compte-resultat/pdf` | CR PDF |
| GET | `/api/v1/export/compte-resultat/word` | CR Word |
| GET | `/api/v1/export/tableau-financement/excel` | TDF Excel |
| GET | `/api/v1/export/tableau-financement/pdf` | TDF PDF |
| GET | `/api/v1/export/tableau-financement/word` | TDF Word |
| GET | `/api/v1/export/balance-agee/excel` | Balance âgée Excel |
| GET | `/api/v1/export/balance-agee/pdf` | Balance âgée PDF |
| GET | `/api/v1/export/balance-agee/word` | Balance âgée Word |
| GET | `/api/v1/export/ratios/excel` | Ratios Excel |
| GET | `/api/v1/export/balance/excel` | Balance (variante) Excel |
| GET | `/api/v1/export/balance/pdf` | Balance PDF |
| GET | `/api/v1/export/balance/word` | Balance Word |
| GET | `/api/v1/export/comptable/sage` | Export format SAGE |
| GET | `/api/v1/export/{etat}/{format}` | Route générique état/format |

---

## 17. Cartographie (`/api/v1/cartographie`)

Réponses **GeoJSON** (ou JSON) pour cartes et analyses spatiales.

| Méthode | Chemin | Fonctionnalité |
|--------|--------|----------------|
| GET | `/api/v1/cartographie/zones` | Zones |
| GET | `/api/v1/cartographie/zones/{id}` | Zone par id |
| GET | `/api/v1/cartographie/agences` | Agences géolocalisées |
| GET | `/api/v1/cartographie/heatmap-par` | Heatmap PAR |

---

## 18. Wallet (Bankily / intégration paiement) (`/api/v1/wallet`)

| Méthode | Chemin | Fonctionnalité |
|--------|--------|----------------|
| POST | `/api/v1/wallet/deblocage` | Déblocage |
| POST | `/api/v1/wallet/remboursement` | Remboursement |
| POST | `/api/v1/wallet/callback` | Callback fournisseur |
| GET | `/api/v1/wallet/operations/{id}` | Détail opération |
| GET | `/api/v1/wallet/operations/{id}/statut` | Statut |
| PATCH | `/api/v1/wallet/operations/{id}/annuler` | Annulation |
| GET | `/api/v1/wallet/membres/{numMembre}` | Opérations par membre |
| GET | `/api/v1/wallet/credits/{idCredit}` | Opérations par crédit |
| GET | `/api/v1/wallet/operations` | Liste des opérations |
| POST | `/api/v1/wallet/reconciliation` | Réconciliation |

---

## 19. Administration

### 19.1 Utilisateurs (`/api/v1/admin/utilisateurs`)

| Méthode | Chemin | Fonctionnalité |
|--------|--------|----------------|
| GET | `/api/v1/admin/utilisateurs` | Liste |
| GET | `/api/v1/admin/utilisateurs/{id}` | Détail |
| GET | `/api/v1/admin/utilisateurs/agence/{codeAgence}` | Par agence |
| POST | `/api/v1/admin/utilisateurs` | Création |
| PUT | `/api/v1/admin/utilisateurs/{id}` | Mise à jour |
| PATCH | `/api/v1/admin/utilisateurs/{id}/desactiver` | Désactivation |
| PATCH | `/api/v1/admin/utilisateurs/{id}/reinitialiser-mdp` | Réinitialisation mot de passe |
| DELETE | `/api/v1/admin/utilisateurs/{id}` | Suppression |

### 19.2 Rôles (`/api/v1/admin/roles`)

| Méthode | Chemin | Fonctionnalité |
|--------|--------|----------------|
| GET | `/api/v1/admin/roles` | Liste |
| GET | `/api/v1/admin/roles/{id}` | Détail |
| GET | `/api/v1/admin/roles/code/{codeRole}` | Par code |
| POST | `/api/v1/admin/roles` | Création |
| PUT | `/api/v1/admin/roles/{id}` | Mise à jour |
| DELETE | `/api/v1/admin/roles/{id}` | Suppression |

### 19.3 Privilèges (`/api/v1/admin/privileges`)

| Méthode | Chemin | Fonctionnalité |
|--------|--------|----------------|
| GET | `/api/v1/admin/privileges` | Liste |
| GET | `/api/v1/admin/privileges/{id}` | Détail |
| GET | `/api/v1/admin/privileges/module/{module}` | Par module |
| POST | `/api/v1/admin/privileges` | Création |
| PUT | `/api/v1/admin/privileges/{id}` | Mise à jour |
| DELETE | `/api/v1/admin/privileges/{id}` | Suppression |

### 19.4 Paramètres (`/api/v1/admin/parametres`)

| Méthode | Chemin | Fonctionnalité |
|--------|--------|----------------|
| GET | `/api/v1/admin/parametres` | Liste |
| GET | `/api/v1/admin/parametres/{id}` | Détail |
| GET | `/api/v1/admin/parametres/agence/{codeAgence}` | Par agence |
| POST | `/api/v1/admin/parametres` | Création |
| PUT | `/api/v1/admin/parametres/{id}` | Mise à jour |

### 19.5 Journal d’audit (`/api/v1/admin/audit`)

| Méthode | Chemin | Fonctionnalité |
|--------|--------|----------------|
| GET | `/api/v1/admin/audit` | Liste |
| GET | `/api/v1/admin/audit/{id}` | Détail |
| GET | `/api/v1/admin/audit/utilisateur/{login}` | Par utilisateur |
| GET | `/api/v1/admin/audit/entite/{entite}` | Par entité |
| GET | `/api/v1/admin/audit/action/{action}` | Par type d’action |

### 19.6 Sauvegardes (`/api/v1/admin/backup`)

| Méthode | Chemin | Fonctionnalité |
|--------|--------|----------------|
| POST | `/api/v1/admin/backup` | Lancer une sauvegarde |
| GET | `/api/v1/admin/backup` | Lister les sauvegardes |
| POST | `/api/v1/admin/backup/restore/{filename}` | Restauration |

Répertoire configuré via `app.backup.dir`.

### 19.7 Monitoring (`/api/v1/admin/monitoring`)

| Méthode | Chemin | Fonctionnalité |
|--------|--------|----------------|
| GET | `/api/v1/admin/monitoring/metrics` | Métriques |
| GET | `/api/v1/admin/monitoring/database` | État base |
| GET | `/api/v1/admin/monitoring/jobs` | Jobs |
| GET | `/api/v1/admin/monitoring/sessions` | Sessions |

### 19.8 Jobs planifiés / manuels (`/api/v1/admin/jobs`)

| Méthode | Chemin | Fonctionnalité |
|--------|--------|----------------|
| POST | `/api/v1/admin/jobs/{nom}/run` | Exécuter un job |
| GET | `/api/v1/admin/jobs/{nom}/historique` | Historique d’un job |
| GET | `/api/v1/admin/jobs/historique` | Historique global |

Les **expressions cron** des jobs automatiques sont dans `application.properties` (`app.jobs.*`).

### 19.9 Clôture périodique (`/api/v1/admin/cloture`)

| Méthode | Chemin | Fonctionnalité |
|--------|--------|----------------|
| POST | `/api/v1/admin/cloture/mensuelle/{annee}/{mois}` | Clôture mensuelle |
| POST | `/api/v1/admin/cloture/annuelle/{annee}` | Clôture annuelle |

---

## 20. Persistance et données

- **Base** : Microsoft SQL Server, schéma géré par **Liquibase** (`backend/src/main/resources/db/changelog/`).
- **JPA** : `ddl-auto=none` (pas de génération automatique du schéma par Hibernate).
- **Fichiers** : photos membres, exports / backups selon répertoires configurés.

---

## 21. Documentation API interactive

Le module inclut **springdoc-openapi** (`springdoc-openapi-starter-webmvc-ui`). En démarrage local, l’exploration des endpoints se fait en général via :

- **Swagger UI** : `http://localhost:8080/swagger-ui/index.html` (ou `/swagger-ui.html` selon la version)
- **OpenAPI JSON** : `http://localhost:8080/v3/api-docs`

Les contrôleurs annotés `@Tag` / `@Operation` décrivent les groupes et opérations dans cette UI.

---

*Document généré à partir du code du dépôt ; en cas d’écart, le code source des contrôleurs fait foi.*
