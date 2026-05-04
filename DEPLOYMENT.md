# MICROFINA++ — Guide de déploiement

## 1. Prérequis

| Composant       | Version minimale |
|-----------------|-----------------|
| Java (JDK)      | 21              |
| Node.js         | 18 LTS          |
| SQL Server      | 2019 (Express)  |
| Angular CLI     | 17              |

## 2. Configuration des répertoires

```properties
# application.properties — à adapter en production
app.backup.dir=/opt/microfina/backups   # Sauvegardes SQL Server (.bak)
app.photos.dir=/opt/microfina/photos    # Photos membres (JPG/PNG ≤ 2 Mo)
```

## 3. Multipart / Upload photos

La taille maximale d'upload est configurée dans `application.properties` :

```properties
spring.servlet.multipart.max-file-size=2MB
spring.servlet.multipart.max-request-size=2MB
```

Si le serveur se trouve derrière un reverse proxy Nginx, configurer également :

```nginx
client_max_body_size 2m;
```

## 4. Export Sage Compta — Format Ligne L

### Choix du format

L'export comptable vers Sage utilise le **format ligne L** (aussi appelé « import lignes »), standard de facto pour l'intégration Sage Compta dans l'espace francophone africain.

### Structure du fichier CSV

```
TYPE ; CODE_JOURNAL ; DATE ; NUMPIECE ; COMPTE ; LIBELLE ; DEBIT ; CREDIT ; LETTRAGE ; DATE_ECHEANCE ; CODE_AGENCE
L    ; 10           ; 15/04/2026 ; P202604-001 ; 501100 ; Déblocage crédit M001 ; 5000.00 ; 0.00 ; ; 30/04/2026 ; AG001
```

**Spécifications techniques :**
- Séparateur : `;` (point-virgule)
- Encodage : UTF-8 avec BOM (compatibilité Excel)
- Fin de ligne : CRLF
- Décimales : 2 chiffres, séparateur `.` (point)
- Type toujours `L` (ligne d'écriture)
- Code journal : identifiant numérique du journal comptable MICROFINA

### Import dans Sage Compta i7 / Sage 100

1. Aller dans **Comptabilité → Imports → Lignes comptables**
2. Sélectionner le fichier `.csv` généré
3. Choisir l'encodage **UTF-8 avec BOM**
4. Vérifier le séparateur **;**
5. Valider l'import

### Endpoint REST

```
GET /api/v1/export/comptable/sage?agence=AG001
```

- `agence` (optionnel) : filtre sur le code agence
- Retourne un fichier `sage_compta_L_YYYYMMDD.csv`
- Sécurité : `PRIV_EXPORT_REPORTS`

## 5. Jobs planifiés

| Job                  | Cron par défaut | Description                                |
|----------------------|-----------------|--------------------------------------------|
| CALCUL_INTERETS      | `0 0 1 * * ?`   | Calcul intérêts courus (01h00 quotidien)   |
| RECALCUL_PAR         | `0 30 1 * * ?`  | Recalcul portefeuille à risque (01h30)     |
| CLOTURE_JOURNALIERE  | `0 50 23 * * ?` | Clôture comptable journalière (23h50)      |

Pour surcharger les cron expressions en production :

```properties
app.jobs.calcul-interets.cron=0 0 2 * * ?
app.jobs.recalcul-par.cron=0 0 3 * * ?
app.jobs.cloture-journaliere.cron=0 55 23 * * ?
```

Pour déclencher manuellement (rôle ADMIN) :

```
POST /api/v1/admin/jobs/CALCUL_INTERETS/run
POST /api/v1/admin/jobs/RECALCUL_PAR/run
POST /api/v1/admin/jobs/CLOTURE_JOURNALIERE/run
```

## 6. Wallet Bankily — Réconciliation

La réconciliation des opérations EN_ATTENTE peut être déclenchée manuellement :

```
POST /api/v1/wallet/reconciliation?agence=AG001
```

En production, prévoir un job @Scheduled toutes les heures pour les opérations
EN_ATTENTE de plus de 5 minutes.

## 7. Sécurité de l'endpoint /callback (Bankily webhook)

L'endpoint `POST /api/v1/wallet/callback` ne requiert pas d'authentification JWT.
En production, protéger par :

1. Filtrage IP au niveau Nginx (liste blanche IPs Bankily)
2. Activation du filtre HMAC-SHA256 : `BankilySignatureFilter` (à implémenter)
