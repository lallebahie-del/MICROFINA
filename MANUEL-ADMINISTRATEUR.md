# MANUEL ADMINISTRATEUR — MICROFINA++
**Version 12.0.0 | Banque Centrale de Mauritanie**

---

> Document officiel à usage interne. Toute reproduction ou diffusion sans autorisation préalable de la Direction des Systèmes d'Information de la BCM est interdite.

**Référence :** BCM-DSI-MICROFINA-ADM-v12.0.0
**Date de révision :** 27 avril 2026
**Statut :** Approuvé
**Contact support :** microfina-support@bcm.mr

---

## Table des matières

1. [Introduction et prérequis système](#1-introduction-et-prérequis-système)
2. [Installation et déploiement](#2-installation-et-déploiement)
3. [Configuration de l'application](#3-configuration-de-lapplication)
4. [Gestion des utilisateurs et des accès](#4-gestion-des-utilisateurs-et-des-accès)
5. [Gestion des sauvegardes](#5-gestion-des-sauvegardes)
6. [Jobs planifiés et monitoring](#6-jobs-planifiés-et-monitoring)
7. [Sécurité](#7-sécurité)
8. [Maintenance et mises à jour](#8-maintenance-et-mises-à-jour)
9. [Troubleshooting](#9-troubleshooting)
10. [Conformité BCM](#10-conformité-bcm)
- [Annexe A : Structure de la base de données](#annexe-a--structure-de-la-base-de-données)
- [Annexe B : Endpoints API récapitulatif](#annexe-b--endpoints-api-récapitulatif)

---

## 1. Introduction et prérequis système

### 1.1 Présentation de MICROFINA++

MICROFINA++ est le système de gestion intégré des institutions de microfinance (IMF) placées sous la supervision de la Banque Centrale de Mauritanie (BCM). Il constitue la plateforme centrale de collecte, de traitement et de reporting des données financières relatives aux activités de microcrédit, d'épargne et de transfert de fonds opérées par les IMF agréées sur le territoire mauritanien.

Le système couvre l'intégralité du cycle de vie d'une opération de microfinance :

- Enregistrement et gestion des membres (personnes physiques et morales)
- Gestion des comptes d'épargne et de dépôt à terme
- Octroi, suivi et recouvrement des crédits
- Calcul automatique des intérêts et des pénalités
- Intégration avec la plateforme de paiement mobile **Bankily** via webhook sécurisé HMAC-SHA256
- Génération des rapports réglementaires BCM (formulaires standardisés)
- Audit trail complet de toutes les opérations

MICROFINA++ est développé selon une architecture trois-tiers :

| Couche | Technologie | Description |
|--------|-------------|-------------|
| Backend | Java 21 / Spring Boot 3.x | API REST, logique métier, sécurité |
| Base de données | PostgreSQL 16 | Persistance, transactions ACID |
| Frontend | Angular 17 / Node.js 20 | Interface utilisateur web responsive |
| Build | Maven 3.9 | Gestion des dépendances et compilation |
| Migration BDD | Liquibase | Versioning du schéma de base de données |
| Serveur web | Nginx | Reverse proxy et service des fichiers statiques |

### 1.2 Architecture logique

```
[Navigateur / Client Angular]
          |
          v
    [Nginx - Port 80/443]
     /           \
[Frontend SPA]  [Reverse Proxy /api/*]
                      |
                      v
           [Backend Spring Boot - Port 8080]
                      |
              ________|________
             |                 |
        [PostgreSQL 16]   [Bankily Webhook]
           Port 5432        (HTTPS externe)
```

### 1.3 Prérequis matériels recommandés (production)

| Ressource | Minimum | Recommandé |
|-----------|---------|------------|
| CPU | 4 cœurs | 8 cœurs |
| RAM | 8 Go | 16 Go |
| Disque OS | 50 Go SSD | 100 Go SSD |
| Disque données (PostgreSQL) | 200 Go | 500 Go SSD |
| Disque sauvegardes | 500 Go | 1 To |
| Réseau | 100 Mbps | 1 Gbps |
| OS | Ubuntu 22.04 LTS | Ubuntu 22.04 LTS |

### 1.4 Prérequis logiciels

Avant toute installation, vérifier la présence et la version correcte des composants suivants :

| Logiciel | Version requise | Commande de vérification |
|----------|-----------------|--------------------------|
| Java (JDK) | 21.x (LTS) | `java -version` |
| PostgreSQL | 16.x | `psql --version` |
| Node.js | 20.x (LTS) | `node --version` |
| npm | 10.x | `npm --version` |
| Maven | 3.9.x | `mvn --version` |
| Nginx | 1.24+ | `nginx -v` |
| Git | 2.40+ | `git --version` |

### 1.5 Ports réseau nécessaires

| Port | Service | Protocole | Direction |
|------|---------|-----------|-----------|
| 80 | HTTP (Nginx) | TCP | Entrant |
| 443 | HTTPS (Nginx) | TCP | Entrant |
| 8080 | Backend Spring Boot | TCP | Interne uniquement |
| 5432 | PostgreSQL | TCP | Interne uniquement |
| 22 | SSH administration | TCP | Entrant (IP restreinte) |

---

## 2. Installation et déploiement

### 2.1 Clonage du dépôt

```bash
# Se connecter en tant qu'utilisateur applicatif
sudo useradd -m -s /bin/bash microfina
sudo su - microfina

# Cloner le dépôt
git clone https://git.bcm.mr/microfina/microfina-plus-plus.git /opt/microfina
cd /opt/microfina

# Vérifier la branche (toujours utiliser main en production)
git checkout main
git pull origin main
```

### 2.2 Prérequis logiciels — Installation sur Ubuntu 22.04 LTS

#### Java 21

```bash
sudo apt update
sudo apt install -y openjdk-21-jdk
# Vérification
java -version
# Résultat attendu : openjdk version "21.x.x" ...

# Définir JAVA_HOME
echo 'export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64' >> ~/.bashrc
echo 'export PATH=$JAVA_HOME/bin:$PATH' >> ~/.bashrc
source ~/.bashrc
```

#### PostgreSQL 16

```bash
# Ajout du dépôt officiel PostgreSQL
sudo sh -c 'echo "deb https://apt.postgresql.org/pub/repos/apt $(lsb_release -cs)-pgdg main" > /etc/apt/sources.list.d/pgdg.list'
wget --quiet -O - https://www.postgresql.org/media/keys/ACCC4CF8.asc | sudo apt-key add -
sudo apt update
sudo apt install -y postgresql-16 postgresql-client-16

# Démarrage du service
sudo systemctl start postgresql
sudo systemctl enable postgresql

# Vérification
psql --version
```

#### Node.js 20 (LTS)

```bash
# Via NodeSource
curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
sudo apt install -y nodejs
# Vérification
node --version   # v20.x.x
npm --version    # 10.x.x
```

#### Maven 3.9

```bash
# Téléchargement
cd /tmp
wget https://dlcdn.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz
sudo tar xzf apache-maven-3.9.6-bin.tar.gz -C /opt
sudo ln -s /opt/apache-maven-3.9.6 /opt/maven

# Variables d'environnement
echo 'export M2_HOME=/opt/maven' >> ~/.bashrc
echo 'export PATH=$M2_HOME/bin:$PATH' >> ~/.bashrc
source ~/.bashrc

# Vérification
mvn --version
```

### 2.3 Création de la base de données PostgreSQL

```sql
-- Se connecter en tant que postgres
sudo -u postgres psql

-- Création de l'utilisateur applicatif
CREATE USER microfina_user WITH PASSWORD 'VotreMotDePasseSecurise!2026';

-- Création de la base de données
CREATE DATABASE microfina_db
    WITH OWNER = microfina_user
    ENCODING = 'UTF8'
    LC_COLLATE = 'fr_FR.UTF-8'
    LC_CTYPE = 'fr_FR.UTF-8'
    TEMPLATE = template0;

-- Attribution des privilèges
GRANT ALL PRIVILEGES ON DATABASE microfina_db TO microfina_user;

-- Connexion à la base et attribution des droits sur le schéma public
\c microfina_db
GRANT ALL ON SCHEMA public TO microfina_user;

\q
```

### 2.4 Variables d'environnement

Les variables d'environnement critiques doivent être définies avant le démarrage de l'application. Il est fortement recommandé de ne jamais coder en dur les secrets dans les fichiers de configuration.

Créer le fichier `/etc/microfina/microfina.env` :

```bash
sudo mkdir -p /etc/microfina
sudo chmod 750 /etc/microfina
sudo nano /etc/microfina/microfina.env
```

Contenu du fichier :

```properties
# =============================================
# MICROFINA++ — Variables d'environnement
# /etc/microfina/microfina.env
# NE PAS VERSIONNER CE FICHIER
# =============================================

# Base de données PostgreSQL
DB_URL=jdbc:postgresql://localhost:5432/microfina_db
DB_USER=microfina_user
DB_PASSWORD=VotreMotDePasseSecurise!2026

# Sécurité JWT
JWT_SECRET=UneChaineDe256BitsMinimumGenereeAleatoirement==

# Intégration Bankily (paiement mobile)
app.bankily.hmac-secret=SecretHMACPartagéAvecBankily64Caracteres

# Profil Spring actif
SPRING_PROFILES_ACTIVE=prod

# Répertoires
APP_PHOTOS_DIR=/var/microfina/photos
APP_BACKUP_DIR=/var/microfina/backups
APP_LOG_DIR=/var/log/microfina
```

```bash
# Sécurisation des permissions
sudo chmod 640 /etc/microfina/microfina.env
sudo chown root:microfina /etc/microfina/microfina.env
```

**Description des variables critiques :**

| Variable | Description | Exemple |
|----------|-------------|---------|
| `DB_URL` | URL JDBC de connexion à PostgreSQL | `jdbc:postgresql://localhost:5432/microfina_db` |
| `DB_USER` | Identifiant de l'utilisateur PostgreSQL | `microfina_user` |
| `DB_PASSWORD` | Mot de passe PostgreSQL (ne jamais laisser vide) | *(secret)* |
| `JWT_SECRET` | Clé secrète de signature des tokens JWT (256 bits min.) | *(secret base64)* |
| `app.bankily.hmac-secret` | Secret HMAC partagé avec Bankily pour validation des webhooks | *(secret 64 chars)* |

### 2.5 Construction de l'application (Maven)

```bash
cd /opt/microfina

# Nettoyage et compilation (sans les tests en production initiale)
mvn clean package -DskipTests

# Résultat attendu dans le répertoire target :
# backend/target/backend-12.0.0.jar
ls -lh backend/target/backend-*.jar
```

> **Note :** En environnement CI/CD, il est recommandé d'exécuter `mvn clean verify` (avec les tests) pour valider l'intégrité du build avant déploiement.

### 2.6 Migration Liquibase

La migration de base de données est **automatique** au démarrage de l'application. Liquibase applique les changesets manquants dans l'ordre chronologique défini dans `src/main/resources/db/changelog/`.

Pour vérifier l'état des migrations avant démarrage :

```bash
mvn liquibase:status \
    -Dliquibase.url=jdbc:postgresql://localhost:5432/microfina_db \
    -Dliquibase.username=microfina_user \
    -Dliquibase.password=VotreMotDePasseSecurise!2026
```

### 2.7 Démarrage du backend

```bash
# Démarrage manuel (pour test)
java -jar backend/target/backend-*.jar \
    --spring.config.additional-location=file:/etc/microfina/ \
    --spring.profiles.active=prod
```

Pour un démarrage en service systemd (recommandé en production) :

```bash
sudo nano /etc/systemd/system/microfina.service
```

```ini
[Unit]
Description=MICROFINA++ Backend Service
After=network.target postgresql.service
Requires=postgresql.service

[Service]
Type=simple
User=microfina
Group=microfina
WorkingDirectory=/opt/microfina
EnvironmentFile=/etc/microfina/microfina.env
ExecStart=/usr/bin/java -Xms512m -Xmx2048m \
    -jar /opt/microfina/backend/target/backend-12.0.0.jar \
    --spring.config.additional-location=file:/etc/microfina/
ExecStop=/bin/kill -SIGTERM $MAINPID
Restart=on-failure
RestartSec=10
StandardOutput=append:/var/log/microfina/microfina.log
StandardError=append:/var/log/microfina/microfina-error.log

[Install]
WantedBy=multi-user.target
```

```bash
sudo systemctl daemon-reload
sudo systemctl enable microfina
sudo systemctl start microfina
sudo systemctl status microfina
```

### 2.8 Déploiement du frontend (Angular + Nginx)

#### Construction Angular

```bash
cd /opt/microfina/frontend

# Installation des dépendances npm
npm install

# Build de production
ng build --configuration=production
# Les fichiers compilés se trouvent dans : frontend/dist/microfina-frontend/
```

#### Configuration Nginx

```bash
sudo nano /etc/nginx/sites-available/microfina
```

```nginx
server {
    listen 80;
    server_name microfina.bcm.mr;

    # Redirection vers HTTPS
    return 301 https://$host$request_uri;
}

server {
    listen 443 ssl http2;
    server_name microfina.bcm.mr;

    ssl_certificate     /etc/ssl/bcm/microfina.crt;
    ssl_certificate_key /etc/ssl/bcm/microfina.key;
    ssl_protocols       TLSv1.2 TLSv1.3;
    ssl_ciphers         HIGH:!aNULL:!MD5;

    # Fichiers statiques Angular
    root /opt/microfina/frontend/dist/microfina-frontend/browser;
    index index.html;

    # Support du routing Angular (HTML5 History API)
    location / {
        try_files $uri $uri/ /index.html;
    }

    # Proxy vers l'API backend
    location /api/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_read_timeout 300;
        proxy_connect_timeout 60;
    }

    # Logs
    access_log /var/log/nginx/microfina-access.log;
    error_log  /var/log/nginx/microfina-error.log;
}
```

```bash
sudo ln -s /etc/nginx/sites-available/microfina /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

---

## 3. Configuration de l'application

### 3.1 Fichier application.properties

Le fichier principal de configuration se trouve à `backend/src/main/resources/application.properties`. Les valeurs sensibles doivent être externalisées via les variables d'environnement (voir section 2.4).

```properties
# =============================================
# MICROFINA++ — Configuration principale
# =============================================

# Informations application
spring.application.name=microfina-plus-plus
app.version=12.0.0
app.nom=MICROFINA++
app.institution=Banque Centrale de Mauritanie

# Base de données
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000

# JPA / Hibernate
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=false

# Liquibase
spring.liquibase.change-log=classpath:db/changelog/db.changelog-master.xml
spring.liquibase.enabled=true

# Sécurité JWT
app.jwt.secret=${JWT_SECRET}
app.jwt.expiration-ms=86400000
app.jwt.refresh-expiration-ms=604800000

# Bankily
app.bankily.hmac-secret=${app.bankily.hmac-secret}
app.bankily.webhook-url=/api/v1/bankily/webhook

# Fichiers et répertoires
app.photos.dir=${APP_PHOTOS_DIR:/var/microfina/photos}
app.backup.dir=${APP_BACKUP_DIR:/var/microfina/backups}

# Jobs planifiés (cron expressions)
app.jobs.calcul-interets.cron=0 0 1 * * *
app.jobs.recalcul-par.cron=0 30 1 * * *
app.jobs.cloture-journaliere.cron=0 50 23 * * *

# Limites upload
spring.servlet.multipart.max-file-size=5MB
spring.servlet.multipart.max-request-size=10MB

# Actuator (monitoring)
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=when-authorized

# Logs
logging.level.root=WARN
logging.level.mr.bcm.microfina=INFO
logging.file.name=${APP_LOG_DIR:/var/log/microfina}/microfina.log
logging.logback.rollingpolicy.max-file-size=50MB
logging.logback.rollingpolicy.max-history=30
```

### 3.2 Profils Spring

MICROFINA++ supporte trois profils de déploiement :

#### Profil `dev`

Activé par `--spring.profiles.active=dev`

- Base H2 en mémoire ou PostgreSQL local
- Logs de niveau DEBUG
- CORS ouvert sur `http://localhost:4200`
- Swagger UI activé (`/swagger-ui.html`)
- Données de test pré-chargées via Liquibase context `dev`

#### Profil `test`

Activé automatiquement lors des tests unitaires et d'intégration Maven.

- Base H2 embarquée
- Désactivation des jobs planifiés
- Mocks pour les services externes (Bankily)

#### Profil `prod`

Activé par `--spring.profiles.active=prod` (valeur par défaut en production)

- PostgreSQL 16 via variables d'environnement
- Logs de niveau INFO/WARN uniquement
- CORS restreint aux domaines autorisés
- Swagger UI désactivé
- Actuator restreint

### 3.3 Configuration des jobs batch (cron)

Les expressions cron suivent le format Spring (6 champs : secondes, minutes, heures, jour du mois, mois, jour de la semaine).

| Propriété | Valeur par défaut | Signification |
|-----------|-------------------|---------------|
| `app.jobs.calcul-interets.cron` | `0 0 1 * * *` | Tous les jours à 01h00 |
| `app.jobs.recalcul-par.cron` | `0 30 1 * * *` | Tous les jours à 01h30 |
| `app.jobs.cloture-journaliere.cron` | `0 50 23 * * *` | Tous les jours à 23h50 |

> **Important :** En cas de modification des expressions cron, un redémarrage du service est nécessaire. Ne pas planifier deux jobs de manière simultanée pour éviter les conflits de transactions.

### 3.4 Répertoire des photos de membres

Le répertoire défini par `app.photos.dir` doit être accessible en lecture et écriture par l'utilisateur système `microfina`.

```bash
sudo mkdir -p /var/microfina/photos
sudo chown microfina:microfina /var/microfina/photos
sudo chmod 750 /var/microfina/photos
```

Les photos sont stockées au format JPEG, nommées selon l'identifiant unique du membre : `{uuid-membre}.jpg`. La taille maximale par fichier est de 5 Mo.

### 3.5 Répertoire des sauvegardes

```bash
sudo mkdir -p /var/microfina/backups
sudo chown microfina:microfina /var/microfina/backups
sudo chmod 750 /var/microfina/backups
```

Le répertoire de sauvegarde doit être monté sur un volume distinct du disque système (voir section 5).

---

## 4. Gestion des utilisateurs et des accès

### 4.1 Modèle de sécurité RBAC

MICROFINA++ implémente un contrôle d'accès basé sur les rôles (Role-Based Access Control). Chaque utilisateur se voit attribuer un ou plusieurs rôles. Chaque rôle regroupe un ensemble de privilèges. Les privilèges contrôlent l'accès aux endpoints API et aux fonctionnalités de l'interface.

```
Utilisateur → possède → Rôles
Rôle        → contient → Privilèges
Privilèges  → autorisent → Actions sur les ressources
```

### 4.2 Rôles disponibles

| Rôle | Description | Accès typique |
|------|-------------|---------------|
| `ROLE_ADMIN` | Administrateur système | Accès complet à toutes les fonctionnalités |
| `ROLE_AGENT` | Agent de guichet / chargé de clientèle | Opérations courantes : membres, crédits, épargne |
| `ROLE_REPORT` | Responsable reporting | Consultation et export des rapports uniquement |

### 4.3 Privilèges disponibles (PRIV_*)

| Privilège | Description |
|-----------|-------------|
| `PRIV_ADMIN` | Accès complet à la console d'administration |
| `PRIV_BANK_OPERATION` | Exécution des opérations bancaires (dépôts, retraits, remboursements) |
| `PRIV_EXPORT_REPORTS` | Export des rapports en PDF, Excel, CSV |
| `PRIV_MANAGE_MEMBERS` | Création, modification et archivage des membres |
| `PRIV_MANAGE_LOANS` | Création et gestion des dossiers de crédit |
| `PRIV_MANAGE_SAVINGS` | Gestion des comptes d'épargne |
| `PRIV_VIEW_REPORTS` | Consultation des rapports et tableaux de bord |
| `PRIV_MANAGE_USERS` | Gestion des comptes utilisateurs du système |
| `PRIV_SYSTEM_CONFIG` | Modification de la configuration système |
| `PRIV_AUDIT_LOGS` | Consultation des journaux d'audit |
| `PRIV_BACKUP_RESTORE` | Exécution des sauvegardes et restaurations |
| `PRIV_RUN_JOBS` | Déclenchement manuel des jobs planifiés |
| `PRIV_MANAGE_AGENCIES` | Gestion des agences et caisses |
| `PRIV_VALIDATE_LOANS` | Validation finale des demandes de crédit |

### 4.4 Matrice rôles / privilèges par défaut

| Privilège | ROLE_ADMIN | ROLE_AGENT | ROLE_REPORT |
|-----------|:----------:|:----------:|:-----------:|
| PRIV_ADMIN | ✓ | | |
| PRIV_BANK_OPERATION | ✓ | ✓ | |
| PRIV_EXPORT_REPORTS | ✓ | | ✓ |
| PRIV_MANAGE_MEMBERS | ✓ | ✓ | |
| PRIV_MANAGE_LOANS | ✓ | ✓ | |
| PRIV_MANAGE_SAVINGS | ✓ | ✓ | |
| PRIV_VIEW_REPORTS | ✓ | ✓ | ✓ |
| PRIV_MANAGE_USERS | ✓ | | |
| PRIV_SYSTEM_CONFIG | ✓ | | |
| PRIV_AUDIT_LOGS | ✓ | | |
| PRIV_BACKUP_RESTORE | ✓ | | |
| PRIV_RUN_JOBS | ✓ | | |
| PRIV_MANAGE_AGENCIES | ✓ | | |
| PRIV_VALIDATE_LOANS | ✓ | | |

### 4.5 Création du premier utilisateur administrateur

Lors du premier démarrage, aucun utilisateur n'existe en base. Utiliser l'endpoint d'initialisation (disponible uniquement si la table `utilisateurs` est vide) :

```bash
curl -X POST https://microfina.bcm.mr/api/v1/admin/utilisateurs \
  -H "Content-Type: application/json" \
  -d '{
    "nom": "Administrateur",
    "prenom": "Système",
    "email": "admin@bcm.mr",
    "login": "admin",
    "motDePasse": "MotDePasseInitial@2026!",
    "role": "ROLE_ADMIN",
    "agenceId": null
  }'
```

> **Sécurité :** Changer impérativement le mot de passe de ce compte dès la première connexion. Le mot de passe doit respecter la politique de complexité : 12 caractères minimum, majuscules, minuscules, chiffres et caractères spéciaux.

**Réponse attendue :**

```json
{
  "id": "uuid-generé",
  "login": "admin",
  "email": "admin@bcm.mr",
  "role": "ROLE_ADMIN",
  "actif": true,
  "createdAt": "2026-04-27T10:00:00Z"
}
```

### 4.6 Gestion des utilisateurs via l'API

**Lister les utilisateurs :**

```bash
GET /api/v1/admin/utilisateurs
Authorization: Bearer {jwt-token}
```

**Modifier un utilisateur :**

```bash
PUT /api/v1/admin/utilisateurs/{id}
Authorization: Bearer {jwt-token}
Content-Type: application/json

{
  "role": "ROLE_AGENT",
  "actif": true
}
```

**Désactiver un utilisateur :**

```bash
PATCH /api/v1/admin/utilisateurs/{id}/desactiver
Authorization: Bearer {jwt-token}
```

**Réinitialiser un mot de passe :**

```bash
POST /api/v1/admin/utilisateurs/{id}/reset-password
Authorization: Bearer {jwt-token}
Content-Type: application/json

{
  "nouveauMotDePasse": "NouveauMotDePasse@2026!"
}
```

---

## 5. Gestion des sauvegardes

### 5.1 Stratégie de sauvegarde

MICROFINA++ implémente une stratégie de sauvegarde à trois niveaux :

| Niveau | Fréquence | Type | Rétention |
|--------|-----------|------|-----------|
| Quotidienne | Tous les jours à 02h00 | Complète (pg_dump) | 30 jours |
| Hebdomadaire | Dimanche à 03h00 | Complète + photos | 12 semaines |
| Mensuelle | 1er du mois à 04h00 | Complète + photos + logs | 12 mois |

### 5.2 Sauvegarde manuelle via l'API

Pour déclencher une sauvegarde immédiate :

```bash
curl -X POST https://microfina.bcm.mr/api/v1/admin/backup \
  -H "Authorization: Bearer {jwt-token}" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "COMPLETE",
    "commentaire": "Sauvegarde avant mise à jour v12.0.0"
  }'
```

**Réponse :**

```json
{
  "id": "backup-20260427-143022",
  "type": "COMPLETE",
  "statut": "EN_COURS",
  "cheminFichier": "/var/microfina/backups/backup-20260427-143022.tar.gz",
  "debutAt": "2026-04-27T14:30:22Z"
}
```

### 5.3 Lister les sauvegardes disponibles

```bash
GET /api/v1/admin/backup
Authorization: Bearer {jwt-token}
```

### 5.4 Restauration d'une sauvegarde

> **Attention :** La restauration écrase toutes les données actuelles. Cette opération est irréversible. Effectuer une sauvegarde préalable avant toute restauration.

```bash
curl -X POST https://microfina.bcm.mr/api/v1/admin/backup/restore \
  -H "Authorization: Bearer {jwt-token}" \
  -H "Content-Type: application/json" \
  -d '{
    "backupId": "backup-20260427-143022",
    "confirmation": "CONFIRMER_RESTAURATION"
  }'
```

Le champ `confirmation` doit contenir exactement la chaîne `CONFIRMER_RESTAURATION` pour valider l'opération.

### 5.5 Restauration manuelle via pg_restore

En cas d'indisponibilité de l'API :

```bash
# Arrêt de l'application
sudo systemctl stop microfina

# Restauration PostgreSQL
sudo -u postgres pg_restore \
    --clean \
    --if-exists \
    --dbname=microfina_db \
    /var/microfina/backups/backup-20260427-143022/microfina_db.dump

# Redémarrage
sudo systemctl start microfina
```

### 5.6 Rotation et nettoyage automatique

Le service de sauvegarde effectue un nettoyage automatique selon les règles de rétention définies. En cas de saturation du disque, une alerte est envoyée par email aux administrateurs configurés dans `app.notifications.admin-emails`.

---

## 6. Jobs planifiés et monitoring

### 6.1 Description des jobs

#### CALCUL_INTERETS — Calcul quotidien des intérêts

- **Déclenchement :** Tous les jours à **01h00** (`0 0 1 * * *`)
- **Fonction :** Calcule les intérêts courus sur tous les crédits actifs et les dépôts à terme. Met à jour les soldes des comptes. Génère les écritures comptables correspondantes.
- **Durée estimée :** 5 à 30 minutes selon le volume de données
- **Tables impactées :** `credits`, `echeanciers`, `comptes`, `ecritures_comptables`

#### RECALCUL_PAR — Recalcul du Portefeuille à Risque

- **Déclenchement :** Tous les jours à **01h30** (`0 30 1 * * *`)
- **Fonction :** Recalcule les indicateurs PAR30 et PAR90 (Portefeuille à Risque à 30 et 90 jours) pour chaque agence et pour l'institution globale. Met à jour le tableau de bord de risque.
- **Durée estimée :** 2 à 10 minutes
- **Tables impactées :** `indicateurs_risque`, `credits`
- **Prérequis :** Ce job doit s'exécuter après `CALCUL_INTERETS`

#### CLOTURE_JOURNALIERE — Clôture comptable de fin de journée

- **Déclenchement :** Tous les jours à **23h50** (`0 50 23 * * *`)
- **Fonction :** Effectue la clôture comptable de la journée (J). Consolide les écritures, génère le journal quotidien, verrouille les transactions de la journée et prépare l'ouverture de J+1.
- **Durée estimée :** 1 à 5 minutes
- **Tables impactées :** `journees_comptables`, `ecritures_comptables`, `balances`

### 6.2 Déclenchement manuel d'un job

Un administrateur peut forcer l'exécution immédiate d'un job via l'API :

```bash
curl -X POST https://microfina.bcm.mr/api/v1/admin/jobs/CALCUL_INTERETS/run \
  -H "Authorization: Bearer {jwt-token}" \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2026-04-27",
    "forcer": true
  }'
```

Noms de jobs acceptés : `CALCUL_INTERETS`, `RECALCUL_PAR`, `CLOTURE_JOURNALIERE`.

**Réponse :**

```json
{
  "jobNom": "CALCUL_INTERETS",
  "executionId": "exec-20260427-150000",
  "statut": "DEMARRE",
  "debutAt": "2026-04-27T15:00:00Z"
}
```

### 6.3 Consultation du statut d'un job

```bash
GET /api/v1/admin/jobs/{nom}/status
Authorization: Bearer {jwt-token}
```

**Statuts possibles :**

| Statut | Description |
|--------|-------------|
| `JAMAIS_EXECUTE` | Le job n'a jamais été lancé |
| `EN_COURS` | Exécution en cours |
| `SUCCES` | Dernière exécution réussie |
| `ECHEC` | Dernière exécution en erreur |
| `IGNORE` | Job ignoré (déjà exécuté aujourd'hui) |

### 6.4 Tableau de bord de monitoring

L'interface de monitoring est accessible depuis le menu **Administration → Monitoring** (`/admin/monitoring`).

Ce tableau affiche :

- Statut en temps réel de chaque job
- Heure de dernière exécution et durée
- Nombre d'enregistrements traités
- Derniers messages d'erreur
- Historique des 30 derniers jours d'exécution sous forme de graphique

### 6.5 Alertes automatiques

En cas d'échec d'un job, une notification est envoyée automatiquement :

- Par email aux administrateurs (`app.notifications.admin-emails`)
- Dans le journal d'audit (consultable depuis l'interface)
- Via webhook si configuré (`app.notifications.webhook-url`)

---

## 7. Sécurité

### 7.1 Authentification JWT

MICROFINA++ utilise JSON Web Tokens (JWT) pour l'authentification sans état des sessions utilisateur.

**Processus d'authentification :**

```
1. L'utilisateur envoie ses identifiants : POST /api/v1/auth/login
2. Le serveur vérifie les credentials (BCrypt)
3. Si valides : génération d'un access token (courte durée) + refresh token
4. L'access token est inclus dans chaque requête : Header "Authorization: Bearer {token}"
5. Le refresh token permet d'obtenir un nouvel access token sans re-saisie
```

**Configuration de la durée de vie :**

```properties
# Durée de vie de l'access token (en millisecondes)
app.jwt.expiration-ms=86400000        # 24 heures

# Durée de vie du refresh token
app.jwt.refresh-expiration-ms=604800000  # 7 jours
```

**Renouvellement du token :**

```bash
POST /api/v1/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Révocation (déconnexion) :**

```bash
POST /api/v1/auth/logout
Authorization: Bearer {access-token}
```

Les tokens révoqués sont ajoutés à une liste noire en base de données jusqu'à leur expiration naturelle.

### 7.2 Hachage des mots de passe (BCrypt)

Tous les mots de passe utilisateurs sont hachés avec l'algorithme **BCrypt** (force 12) avant stockage. Le mot de passe en clair n'est jamais persisté.

```java
// Exemple de configuration (non modifiable sans recompilation)
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
}
```

La politique de mots de passe applique les contraintes suivantes :

- Longueur minimale : 12 caractères
- Au moins 1 majuscule, 1 minuscule, 1 chiffre, 1 caractère spécial
- Pas de répétition des 5 derniers mots de passe
- Expiration après 90 jours

### 7.3 Sécurité des webhooks Bankily (HMAC-SHA256)

Les notifications de paiement envoyées par Bankily sont authentifiées via une signature HMAC-SHA256. Pour chaque webhook reçu :

1. Bankily calcule `HMAC-SHA256(corps_requête, secret_partagé)` et l'envoie dans l'en-tête HTTP `X-Bankily-Signature`.
2. MICROFINA++ recalcule la signature côté serveur.
3. Si les signatures correspondent, la transaction est traitée. Sinon, la requête est rejetée (HTTP 401).

```bash
# En-tête attendu dans le webhook entrant :
X-Bankily-Signature: sha256=abc123def456...

# La clé partagée est définie dans :
app.bankily.hmac-secret=SecretHMACPartagéAvecBankily
```

### 7.4 Configuration CORS

En production (`SPRING_PROFILES_ACTIVE=prod`), CORS est restreint aux origines explicitement autorisées :

```properties
app.cors.allowed-origins=https://microfina.bcm.mr
app.cors.allowed-methods=GET,POST,PUT,PATCH,DELETE,OPTIONS
app.cors.allowed-headers=Authorization,Content-Type,X-Requested-With
app.cors.max-age=3600
```

### 7.5 Audit trail

MICROFINA++ maintient un journal d'audit complet de toutes les actions effectuées dans le système. Chaque entrée d'audit contient :

| Champ | Description |
|-------|-------------|
| `id` | Identifiant unique de l'événement |
| `timestamp` | Horodatage précis (UTC) |
| `utilisateur` | Login de l'utilisateur ayant effectué l'action |
| `ip_address` | Adresse IP source |
| `action` | Type d'action (`CREATE`, `UPDATE`, `DELETE`, `LOGIN`, `EXPORT`, etc.) |
| `entite` | Type d'entité concernée (ex. : `CREDIT`, `MEMBRE`) |
| `entite_id` | Identifiant de l'entité |
| `details` | Données avant/après modification (JSON) |

Les journaux d'audit sont immuables : aucune suppression ou modification n'est possible via l'API ou l'interface. La rétention est de 5 ans conformément aux obligations réglementaires BCM.

### 7.6 Bonnes pratiques de sécurité

- Ne jamais exposer le port 8080 (backend) directement sur Internet.
- Renouveler le `JWT_SECRET` annuellement (provoque l'invalidation de tous les tokens actifs).
- Renouveler le `app.bankily.hmac-secret` en coordination avec l'équipe Bankily.
- Activer les mises à jour automatiques de sécurité du système d'exploitation.
- Effectuer des audits de sécurité pénétration annuels.

---

## 8. Maintenance et mises à jour

### 8.1 Processus de mise à jour standard

La procédure de mise à jour doit être effectuée en dehors des heures de pointe (de préférence entre 02h00 et 06h00).

```bash
# 1. Notification aux utilisateurs (depuis l'interface d'administration)
#    Administration → Notifications → Maintenance programmée

# 2. Sauvegarde préalable obligatoire
curl -X POST https://microfina.bcm.mr/api/v1/admin/backup \
  -H "Authorization: Bearer {jwt-token}" \
  -d '{"type": "COMPLETE", "commentaire": "Avant MAJ v12.x.x"}'

# 3. Attendre la fin de la sauvegarde et vérifier son intégrité
# 4. Arrêt du service
sudo systemctl stop microfina

# 5. Mise à jour du code source
cd /opt/microfina
git pull origin main

# 6. Reconstruction
mvn clean package -DskipTests

# 7. Les migrations Liquibase seront appliquées automatiquement au démarrage
# 8. Redémarrage
sudo systemctl start microfina

# 9. Vérification de santé
curl https://microfina.bcm.mr/api/v1/actuator/health

# 10. Reconstruction du frontend si nécessaire
cd /opt/microfina/frontend
npm install
ng build --configuration=production
```

### 8.2 Gestion des migrations Liquibase

Liquibase versionne le schéma de la base de données. Chaque migration est un changeset identifié par un `id` unique et un `author`. Les migrations appliquées sont tracées dans la table `databasechangelog`.

**Vérifier l'état des migrations :**

```bash
mvn liquibase:status \
    -Dliquibase.url=jdbc:postgresql://localhost:5432/microfina_db \
    -Dliquibase.username=microfina_user \
    -Dliquibase.password=${DB_PASSWORD}
```

**Afficher l'historique complet :**

```bash
mvn liquibase:history \
    -Dliquibase.url=jdbc:postgresql://localhost:5432/microfina_db \
    -Dliquibase.username=microfina_user \
    -Dliquibase.password=${DB_PASSWORD}
```

### 8.3 Rollback

En cas de problème critique après mise à jour :

**Rollback Liquibase (N changements) :**

```bash
mvn liquibase:rollback \
    -Dliquibase.rollbackCount=1 \
    -Dliquibase.url=jdbc:postgresql://localhost:5432/microfina_db \
    -Dliquibase.username=microfina_user \
    -Dliquibase.password=${DB_PASSWORD}
```

**Rollback complet depuis une sauvegarde :**

```bash
# Arrêt du service
sudo systemctl stop microfina

# Revenir à la version précédente dans Git
git log --oneline -10
git checkout <commit-hash-version-precedente>
mvn clean package -DskipTests

# Restaurer la sauvegarde de base de données
sudo -u postgres pg_restore --clean --dbname=microfina_db \
    /var/microfina/backups/backup-avant-maj.tar.gz

sudo systemctl start microfina
```

---

## 9. Troubleshooting

### 9.1 Consultation des logs applicatifs

```bash
# Logs en temps réel
sudo journalctl -u microfina -f

# Logs du fichier applicatif
tail -f /var/log/microfina/microfina.log

# Recherche d'erreurs spécifiques
grep -n "ERROR\|FATAL" /var/log/microfina/microfina.log | tail -50

# Logs Nginx
tail -f /var/log/nginx/microfina-error.log
```

### 9.2 Erreurs de connexion à la base de données

**Symptôme :** L'application ne démarre pas, message `Connection refused` ou `FATAL: password authentication failed`.

**Diagnostic :**

```bash
# Vérifier que PostgreSQL est démarré
sudo systemctl status postgresql

# Tester la connexion manuellement
psql -h localhost -U microfina_user -d microfina_db -c "SELECT 1;"

# Vérifier les paramètres dans le fichier d'environnement
cat /etc/microfina/microfina.env | grep DB_
```

**Solutions fréquentes :**

| Erreur | Cause probable | Solution |
|--------|----------------|----------|
| `Connection refused` | PostgreSQL non démarré | `sudo systemctl start postgresql` |
| `password authentication failed` | Mot de passe incorrect | Vérifier `DB_PASSWORD` dans l'env |
| `database does not exist` | Base non créée | Exécuter le script SQL de création |
| `too many connections` | Pool de connexions saturé | Augmenter `hikari.maximum-pool-size` |

### 9.3 Problèmes JWT

**Symptôme :** L'API retourne HTTP 401 sur toutes les requêtes authentifiées.

**Diagnostic :**

```bash
# Vérifier que JWT_SECRET est défini
printenv JWT_SECRET

# Décoder un token pour inspection (sans validation)
# Utiliser https://jwt.io en environnement de dev uniquement
```

**Causes fréquentes :**

- `JWT_SECRET` vide ou non chargé depuis le fichier d'environnement → vérifier `EnvironmentFile` dans le service systemd
- Token expiré → vérifier `app.jwt.expiration-ms` et l'horloge serveur
- Changement du `JWT_SECRET` depuis la génération du token → tous les tokens précédents sont invalidés (comportement attendu)

### 9.4 Jobs en erreur

**Symptôme :** Un job apparaît avec le statut `ECHEC` dans le tableau de monitoring.

**Diagnostic :**

```bash
# Consulter les logs du job concerné
grep "CALCUL_INTERETS\|JobExecutionException" /var/log/microfina/microfina.log | tail -100

# Vérifier via l'API
curl https://microfina.bcm.mr/api/v1/admin/jobs/CALCUL_INTERETS/status \
  -H "Authorization: Bearer {jwt-token}"
```

**Causes fréquentes et solutions :**

| Symptôme | Cause | Solution |
|----------|-------|----------|
| Job bloqué en `EN_COURS` | Processus bloqué, redémarrage non propre | Redémarrer l'application, relancer le job manuellement |
| `CALCUL_INTERETS` en erreur | Données corrompues en base | Consulter les logs détaillés, corriger les données, relancer |
| `CLOTURE_JOURNALIERE` échouée | `CALCUL_INTERETS` non terminé | Vérifier l'ordre d'exécution, relancer dans l'ordre |
| Timeout | Charge base de données trop élevée | Augmenter les ressources, décaler les plages horaires |

### 9.5 Problèmes de performance

Si l'application est lente :

```bash
# Vérifier l'utilisation mémoire JVM
sudo journalctl -u microfina | grep "OutOfMemoryError\|GC overhead"

# Augmenter la mémoire JVM si nécessaire (dans le service systemd)
ExecStart=/usr/bin/java -Xms1g -Xmx4g ...

# Vérifier les requêtes SQL lentes dans PostgreSQL
sudo -u postgres psql -d microfina_db -c "
  SELECT query, calls, total_exec_time, mean_exec_time
  FROM pg_stat_statements
  ORDER BY mean_exec_time DESC
  LIMIT 20;"
```

---

## 10. Conformité BCM

### 10.1 Cadre réglementaire

MICROFINA++ est conçu pour respecter le cadre réglementaire mauritanien applicable aux institutions de microfinance, notamment :

- **Loi n° 2007-006** relative aux coopératives d'épargne et de crédit
- **Règlement BCM n° 2020-001** sur la supervision des IMF
- **Circulaire BCM n° 2022-003** relative aux obligations de reporting des IMF
- **Règlement BCEAO** sur la lutte contre le blanchiment de capitaux (LBC/FT)

### 10.2 Obligations réglementaires couvertes

| Obligation | Description | Module MICROFINA++ |
|------------|-------------|-------------------|
| Reporting mensuel | Transmission des états financiers à la BCM | Module Rapports BCM |
| Déclarations KYC | Identification et vérification des membres | Module Membres |
| Suivi PAR | Calcul et reporting du Portefeuille à Risque | Job RECALCUL_PAR |
| Taux d'intérêt | Respect des plafonds légaux de taux | Validation crédit |
| LBC/FT | Déclarations de soupçons, gel d'avoirs | Module Conformité |
| Conservation des données | Archivage 10 ans minimum | Audit trail / Archives |
| Audit interne | Journal d'audit complet et immuable | Module Audit |

### 10.3 Rapport de conformité

L'administrateur peut générer un rapport de conformité depuis l'interface :

**Administration → Conformité → Générer rapport de conformité**

Ou via l'API :

```bash
curl -X POST https://microfina.bcm.mr/api/v1/admin/compliance/rapport \
  -H "Authorization: Bearer {jwt-token}" \
  -H "Content-Type: application/json" \
  -d '{
    "periode": "2026-03",
    "format": "PDF"
  }'
```

Le rapport couvre :

- Liste des IMF déclarantes
- Indicateurs clés (PAR30, PAR90, taux de remboursement)
- Anomalies détectées
- Statut de transmission des déclarations réglementaires
- Certificat de conformité signé électroniquement

### 10.4 Rétention et archivage des données

Conformément à la réglementation BCM, les données sont conservées selon les durées suivantes :

| Type de donnée | Durée de rétention |
|----------------|-------------------|
| Données membres | 10 ans après clôture du compte |
| Historique des crédits | 10 ans après remboursement intégral |
| Journal d'audit | 5 ans glissants |
| États financiers | 10 ans |
| Sauvegardes quotidiennes | 30 jours |
| Sauvegardes mensuelles | 12 mois |
| Sauvegardes annuelles | 10 ans |

---

## Annexe A : Structure de la base de données

### A.1 Principales tables

| Table | Description | Colonnes clés |
|-------|-------------|---------------|
| `utilisateurs` | Comptes utilisateurs du système | `id`, `login`, `email`, `mot_de_passe_hash`, `role`, `actif` |
| `membres` | Membres des IMF (personnes physiques/morales) | `id`, `numero_membre`, `nom`, `prenom`, `cni`, `telephone`, `photo_url` |
| `agences` | Agences et caisses des IMF | `id`, `code`, `nom`, `imf_id`, `actif` |
| `comptes` | Comptes d'épargne et courants | `id`, `numero`, `type`, `solde`, `membre_id`, `agence_id`, `actif` |
| `credits` | Dossiers de crédit | `id`, `numero`, `montant`, `taux_interet`, `duree_mois`, `statut`, `membre_id` |
| `echeanciers` | Tableau d'amortissement des crédits | `id`, `credit_id`, `numero_echeance`, `date_echeance`, `montant_principal`, `montant_interet`, `statut` |
| `transactions` | Mouvements financiers | `id`, `type`, `montant`, `date`, `compte_id`, `reference_externe` |
| `ecritures_comptables` | Plan comptable | `id`, `compte_comptable`, `debit`, `credit`, `date`, `libelle` |
| `journees_comptables` | Clôtures journalières | `id`, `date`, `statut`, `cloture_at`, `cloture_par` |
| `indicateurs_risque` | PAR et ratios de risque | `id`, `date_calcul`, `par30`, `par90`, `agence_id` |
| `audit_logs` | Journal d'audit | `id`, `timestamp`, `utilisateur`, `ip_address`, `action`, `entite`, `entite_id`, `details_json` |
| `backup_operations` | Historique des sauvegardes | `id`, `type`, `chemin`, `taille_octets`, `statut`, `debut_at`, `fin_at` |
| `job_executions` | Historique des jobs | `id`, `job_nom`, `statut`, `debut_at`, `fin_at`, `message_erreur`, `enregistrements_traites` |
| `blacklisted_tokens` | Tokens JWT révoqués | `id`, `token_hash`, `expiration_at` |
| `parametres_systeme` | Configuration métier dynamique | `cle`, `valeur`, `description`, `modifie_par`, `modifie_at` |

### A.2 Conventions de nommage

- Identifiants : UUID v4 (type `uuid` PostgreSQL)
- Horodatages : `timestamp with time zone` (UTC)
- Montants monétaires : `numeric(15,2)` (en MRU — Ouguiya mauritanien)
- Taux d'intérêt : `numeric(5,4)` (ex. : `0.2400` pour 24%)
- Statuts : `varchar(20)` avec contrainte CHECK

---

## Annexe B : Endpoints API récapitulatif

### B.1 Authentification

| Méthode | Endpoint | Description | Auth |
|---------|----------|-------------|------|
| POST | `/api/v1/auth/login` | Connexion, obtention des tokens | Non |
| POST | `/api/v1/auth/refresh` | Renouvellement de l'access token | Non |
| POST | `/api/v1/auth/logout` | Déconnexion, révocation du token | Oui |
| GET | `/api/v1/auth/me` | Profil de l'utilisateur connecté | Oui |
| POST | `/api/v1/auth/change-password` | Changement de mot de passe | Oui |

### B.2 Administration

| Méthode | Endpoint | Description | Privilège requis |
|---------|----------|-------------|-----------------|
| GET | `/api/v1/admin/utilisateurs` | Liste des utilisateurs | PRIV_MANAGE_USERS |
| POST | `/api/v1/admin/utilisateurs` | Créer un utilisateur | PRIV_MANAGE_USERS |
| PUT | `/api/v1/admin/utilisateurs/{id}` | Modifier un utilisateur | PRIV_MANAGE_USERS |
| PATCH | `/api/v1/admin/utilisateurs/{id}/desactiver` | Désactiver un utilisateur | PRIV_MANAGE_USERS |
| POST | `/api/v1/admin/backup` | Déclencher une sauvegarde | PRIV_BACKUP_RESTORE |
| GET | `/api/v1/admin/backup` | Lister les sauvegardes | PRIV_BACKUP_RESTORE |
| POST | `/api/v1/admin/backup/restore` | Restaurer une sauvegarde | PRIV_BACKUP_RESTORE |
| POST | `/api/v1/admin/jobs/{nom}/run` | Déclencher un job manuellement | PRIV_RUN_JOBS |
| GET | `/api/v1/admin/jobs/{nom}/status` | Statut d'un job | PRIV_ADMIN |
| GET | `/api/v1/admin/audit-logs` | Consulter les logs d'audit | PRIV_AUDIT_LOGS |
| POST | `/api/v1/admin/compliance/rapport` | Générer un rapport de conformité | PRIV_ADMIN |

### B.3 Membres

| Méthode | Endpoint | Description | Privilège requis |
|---------|----------|-------------|-----------------|
| GET | `/api/v1/membres` | Liste des membres (paginée) | PRIV_MANAGE_MEMBERS |
| POST | `/api/v1/membres` | Créer un membre | PRIV_MANAGE_MEMBERS |
| GET | `/api/v1/membres/{id}` | Détails d'un membre | PRIV_MANAGE_MEMBERS |
| PUT | `/api/v1/membres/{id}` | Modifier un membre | PRIV_MANAGE_MEMBERS |
| POST | `/api/v1/membres/{id}/photo` | Uploader la photo | PRIV_MANAGE_MEMBERS |

### B.4 Crédits et épargne

| Méthode | Endpoint | Description | Privilège requis |
|---------|----------|-------------|-----------------|
| GET | `/api/v1/credits` | Liste des crédits | PRIV_MANAGE_LOANS |
| POST | `/api/v1/credits` | Créer un dossier de crédit | PRIV_MANAGE_LOANS |
| POST | `/api/v1/credits/{id}/valider` | Valider un crédit | PRIV_VALIDATE_LOANS |
| POST | `/api/v1/credits/{id}/decaisser` | Décaissement du crédit | PRIV_BANK_OPERATION |
| POST | `/api/v1/credits/{id}/rembourser` | Enregistrer un remboursement | PRIV_BANK_OPERATION |
| GET | `/api/v1/comptes` | Liste des comptes d'épargne | PRIV_MANAGE_SAVINGS |
| POST | `/api/v1/comptes/{id}/depot` | Effectuer un dépôt | PRIV_BANK_OPERATION |
| POST | `/api/v1/comptes/{id}/retrait` | Effectuer un retrait | PRIV_BANK_OPERATION |

### B.5 Rapports

| Méthode | Endpoint | Description | Privilège requis |
|---------|----------|-------------|-----------------|
| GET | `/api/v1/rapports/par` | Rapport PAR30 / PAR90 | PRIV_VIEW_REPORTS |
| GET | `/api/v1/rapports/bilan` | Bilan financier | PRIV_VIEW_REPORTS |
| POST | `/api/v1/rapports/export` | Export PDF / Excel / CSV | PRIV_EXPORT_REPORTS |
| GET | `/api/v1/rapports/bcm` | Rapports réglementaires BCM | PRIV_EXPORT_REPORTS |

---

*Fin du Manuel Administrateur MICROFINA++ v12.0.0*
*Document généré le 27 avril 2026 — Direction des Systèmes d'Information, Banque Centrale de Mauritanie*
