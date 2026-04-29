# FICHE DE SUIVI DES MISES À JOUR — MICROFINA++

**Banque Centrale de Mauritanie — Direction des Systèmes d'Information**
**Référence :** BCM-DSI-MICROFINA-MAJ
**Dernière mise à jour :** 27 avril 2026

---

## Historique des versions

| Version | Date | Description | Auteur | Statut |
|---------|------|-------------|--------|--------|
| v1.0.0 | 2015-03-01 | Version initiale : gestion des membres, comptes d'épargne de base, authentification utilisateur simple. Déploiement pilote sur 3 IMF. | Équipe DSI BCM | Archivé |
| v2.0.0 | 2015-11-15 | Ajout du module crédit : création de dossiers, tableau d'amortissement, remboursements manuels. Première version du plan comptable intégré. | Équipe DSI BCM | Archivé |
| v3.0.0 | 2016-08-22 | Introduction du calcul automatique des intérêts (job quotidien). Génération des premiers rapports réglementaires BCM au format Excel. | Équipe DSI BCM | Archivé |
| v4.0.0 | 2017-05-10 | Refonte de l'interface utilisateur (migration vers Angular). Ajout de la gestion multi-agences. Amélioration des performances base de données. | Équipe DSI BCM | Archivé |
| v5.0.0 | 2018-02-28 | Implémentation du RBAC (contrôle d'accès basé sur les rôles). Introduction des privilèges PRIV_*. Audit trail complet. Sécurisation BCrypt. | Équipe DSI BCM | Archivé |
| v6.0.0 | 2018-12-03 | Migration vers Spring Boot 2.x et Java 11. Adoption de Liquibase pour le versioning de schéma. Amélioration de la stabilité des jobs batch. | Équipe DSI BCM | Archivé |
| v7.0.0 | 2019-09-17 | Module de sauvegarde automatique intégré (pg_dump). Rotation des sauvegardes configurable. API REST de gestion des backups. | Équipe DSI BCM | Archivé |
| v8.0.0 | 2020-06-30 | Intégration Bankily (paiement mobile) via webhook HMAC-SHA256. Gestion des transactions en temps réel. Conformité BCEAO LBC/FT renforcée. | Équipe DSI BCM | Archivé |
| v9.0.0 | 2021-04-19 | Introduction du module PAR (Portefeuille à Risque) : calcul PAR30/PAR90 automatisé, tableau de bord de risque, alertes automatiques. | Équipe DSI BCM | Archivé |
| v9.5.0 | 2021-11-08 | Ajout de l'export PDF des rapports BCM. Corrections de sécurité critiques (CVE-2021-44228 Log4Shell — migration vers Logback). Patch urgent. | Équipe DSI BCM | Archivé |
| v10.0.0 | 2022-10-12 | Migration vers Spring Boot 3.x et Java 17. Refonte du module de conformité BCM. Introduction du rapport de conformité automatisé signé électroniquement. | Équipe DSI BCM | Archivé |
| v10.2.0 | 2023-03-27 | Extension des fonctionnalités de reporting (export CSV, Excel multi-onglets). Optimisation des requêtes SQL (réduction de 40 % du temps d'exécution des jobs). | Équipe DSI BCM | Archivé |
| v11.0.0 | 2023-12-05 | Migration vers Java 21 (LTS) et PostgreSQL 16. Refonte de l'interface Angular 17. Introduction des tokens JWT avec refresh. Tableau de bord de monitoring des jobs. | Équipe DSI BCM | Archivé |
| v11.3.0 | 2024-09-18 | Ajout de la gestion des dépôts à terme. Amélioration du module LBC/FT (déclarations CENTIF automatisées). Correctifs de sécurité et performance. | Équipe DSI BCM | Archivé |
| v12.0.0 | 2026-04-27 | Version majeure : migration Node.js 20 / Maven 3.9, refonte complète du module membres (photos, KYC avancé), job CLOTURE_JOURNALIERE amélioré, nouvelles API d'administration, conformité Circulaire BCM 2022-003. | Équipe DSI BCM | Actif |

---

## Procédure de mise à jour

La procédure ci-dessous doit être suivie pour toute mise à jour de MICROFINA++, quelle que soit la version cible. Elle s'applique aux environnements de recette et de production.

1. **Planification et communication** : Notifier les utilisateurs et les responsables des IMF concernés au moins 48 heures à l'avance. Fixer la fenêtre de maintenance (de préférence entre 02h00 et 06h00). Créer un ticket de suivi dans le système ITSM de la BCM avec le numéro de version cible et l'impact attendu.

2. **Vérification des prérequis** : S'assurer que toutes les versions logicielles requises (Java, PostgreSQL, Node.js, Maven, Nginx) sont installées et conformes aux spécifications de la version cible. Consulter la section 1.4 du Manuel Administrateur pour la matrice de compatibilité complète.

3. **Sauvegarde complète obligatoire** : Déclencher une sauvegarde complète de la base de données et des fichiers (photos, configuration) via l'API `POST /api/v1/admin/backup` avec le type `COMPLETE`. Attendre la confirmation de succès et noter le chemin du fichier de sauvegarde. Ne pas procéder sans cette étape.

4. **Arrêt du service applicatif** : Arrêter le service MICROFINA++ via `sudo systemctl stop microfina`. Vérifier que toutes les sessions actives sont terminées et qu'aucun job batch n'est en cours d'exécution (consulter le tableau de monitoring avant l'arrêt).

5. **Mise à jour du code source** : Depuis le répertoire `/opt/microfina`, exécuter `git pull origin main` pour récupérer la dernière version. Vérifier le tag de version avec `git describe --tags`. En cas de merge conflict, résoudre en priorité les fichiers de configuration.

6. **Reconstruction de l'application** : Exécuter `mvn clean package -DskipTests` pour recompiler le backend. Vérifier l'absence d'erreurs de compilation. Pour le frontend, exécuter `npm install && ng build --configuration=production` depuis le répertoire `frontend/`.

7. **Démarrage et validation des migrations** : Redémarrer le service avec `sudo systemctl start microfina`. Les migrations Liquibase s'appliquent automatiquement. Surveiller les logs au démarrage (`journalctl -u microfina -f`) pour confirmer que toutes les migrations ont été appliquées avec succès. Effectuer des tests fonctionnels de base (connexion, liste des membres, exécution d'un rapport).

8. **Validation finale et documentation** : Vérifier l'endpoint de santé `GET /api/v1/actuator/health`. Mettre à jour la présente fiche de suivi avec la nouvelle entrée de version. Notifier les utilisateurs de la fin de la maintenance. Archiver le rapport de mise à jour dans le référentiel documentaire de la DSI BCM (`dsi-docs/microfina/rapports-maj/`).

---

## Contacts et escalade

### Support de premier niveau

| Rôle | Nom | Email | Téléphone |
|------|-----|-------|-----------|
| Responsable applicatif MICROFINA++ | Direction DSI BCM | microfina-support@bcm.mr | +222 45 25 22 06 |
| Support technique (heures ouvrées) | Équipe Support DSI | dsi-support@bcm.mr | +222 45 25 22 07 |

### Matrice d'escalade

| Niveau | Déclencheur | Responsable | Délai de réponse |
|--------|-------------|-------------|------------------|
| N1 — Support standard | Anomalie fonctionnelle non bloquante | Équipe Support DSI | 4 heures ouvrées |
| N2 — Incident mineur | Service dégradé, job en erreur isolé | Responsable applicatif | 2 heures ouvrées |
| N3 — Incident majeur | Service indisponible, perte de données potentielle | Directeur DSI BCM | 1 heure (24h/24) |
| N4 — Crise | Faille de sécurité, atteinte à l'intégrité des données | DG BCM + CERT national | Immédiat (24h/24) |

### Contacts fournisseurs externes

| Fournisseur | Service | Email de contact |
|-------------|---------|-----------------|
| Bankily | Intégration webhook paiement mobile | integration@bankily.mr |
| Hébergeur BCM | Infrastructure serveurs | infra@bcm.mr |

---

*Document maintenu par la Direction des Systèmes d'Information — Banque Centrale de Mauritanie*
*Pour toute modification, soumettre une demande de changement via le portail ITSM BCM.*
