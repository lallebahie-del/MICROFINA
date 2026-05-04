# MANUEL UTILISATEUR — MICROFINA++
**Version 12.0.0 | Banque Centrale de Mauritanie**

---

> Document officiel — Usage interne BCM  
> Date de révision : Avril 2026  
> Statut : Approuvé  
> Référence : BCM-MF-MU-12.0.0

---

## Table des matières

1. [Introduction](#1-introduction)
2. [Connexion et authentification](#2-connexion-et-authentification)
3. [Tableau de bord](#3-tableau-de-bord)
4. [Gestion des membres](#4-gestion-des-membres)
5. [Produits de crédit](#5-produits-de-crédit)
6. [Gestion des crédits](#6-gestion-des-crédits)
7. [Épargne et comptes épargne](#7-épargne-et-comptes-épargne)
8. [Garanties](#8-garanties)
9. [Comptabilité](#9-comptabilité)
10. [Opérations de caisse et bancaires](#10-opérations-de-caisse-et-bancaires)
11. [Wallet Bankily](#11-wallet-bankily)
12. [Reporting BCM](#12-reporting-bcm)
13. [Exports](#13-exports)
14. [Administration](#14-administration-réservé-aux-administrateurs)
15. [Cartographie](#15-cartographie)
16. [FAQ et dépannage](#16-faq-et-dépannage)
- [Annexe A : Glossaire](#annexe-a--glossaire)
- [Annexe B : Contacts support](#annexe-b--contacts-support)

---

## 1. Introduction

### 1.1 Présentation du système MICROFINA++

MICROFINA++ est le système d'information de gestion des institutions de microfinance (SIGIM) déployé par la Banque Centrale de Mauritanie (BCM) pour l'ensemble des institutions de microfinance (IMF) opérant sur le territoire mauritanien. Il constitue la plateforme officielle de supervision, de gestion opérationnelle et de reporting réglementaire.

MICROFINA++ version 12.0.0 regroupe les fonctionnalités suivantes :

- **Gestion des membres** : personnes physiques (PP) et personnes morales (PM), avec gestion du cycle de vie complet (inscription, validation, désactivation).
- **Gestion des crédits** : origination des demandes, instruction, déblocage, suivi des remboursements et gestion des impayés.
- **Épargne** : ouverture de comptes épargne, dépôts, retraits et blocage.
- **Garanties** : enregistrement, association aux crédits et mainlevée.
- **Comptabilité intégrée** : plan comptable SYSCOHADA adapté microfinance, journal, grand livre, balance et bilan simplifié.
- **Caisse et opérations bancaires** : gestion des entrées/sorties de caisse, virements bancaires et carnets de chèques.
- **Wallet Bankily** : intégration avec la plateforme de monnaie mobile nationale pour les décaissements et remboursements dématérialisés.
- **Reporting réglementaire BCM** : génération automatisée du Rapport Mensuel d'Activité (RMA), tableau de bord prudentiel et calcul du Portefeuille à Risque (PAR).
- **Exports** : extraction des données en formats Excel, PDF, Word et Sage Compta Ligne L.
- **Administration** : gestion des utilisateurs, des rôles, des privilèges et journalisation des audits.
- **Cartographie** : visualisation géographique des membres et zones d'activité.

MICROFINA++ est une application web accessible via un navigateur moderne (Google Chrome 110+, Mozilla Firefox 110+, Microsoft Edge 110+). Elle est hébergée localement sur l'infrastructure de chaque IMF et connectée au serveur central de la BCM pour la remontée réglementaire des données.

### 1.2 Public cible

Ce manuel s'adresse aux profils d'utilisateurs suivants :

| Profil | Rôle principal |
|---|---|
| Agent de crédit | Saisie des demandes de crédit, suivi des remboursements |
| Caissier | Opérations de caisse, dépôts et retraits épargne |
| Comptable | Consultation et lettrage des écritures, exports comptables |
| Responsable d'agence | Validation des dossiers, consultation du tableau de bord |
| Auditeur interne | Consultation en lecture seule, accès au journal d'audit |
| Administrateur système | Gestion complète : utilisateurs, rôles, sauvegardes, monitoring |
| Superviseur BCM | Accès aux reportings réglementaires et exports BCM |

Chaque profil dispose d'un ensemble de droits définis dans le module d'administration. Les fonctionnalités décrites dans ce manuel peuvent ne pas être toutes accessibles selon le rôle attribué à l'utilisateur connecté.

### 1.3 Conventions du document

Le présent manuel utilise les conventions visuelles et typographiques suivantes :

**Champs et libellés d'interface**

Les noms des champs, boutons, onglets, menus et labels affichés dans l'interface sont indiqués en **gras** ou entre guillemets « » selon le contexte.

**Chemins de navigation**

Les chemins de navigation dans les menus sont indiqués avec le séparateur `>`, par exemple :  
`Menu principal > Membres > Nouveau membre`

**Captures d'écran**

Les captures d'écran sont identifiées par un cadre gris et une légende numérotée au format `[Fig. X.Y]`. Les zones importantes sont entourées d'un cadre rouge sur l'image.

**Icônes récurrentes**

| Icône | Signification |
|---|---|
| Loupe | Rechercher / Filtrer |
| Plus (+) | Créer un nouvel enregistrement |
| Crayon | Modifier un enregistrement existant |
| Corbeille | Supprimer un enregistrement |
| Œil | Consulter le détail (lecture seule) |
| Téléchargement | Exporter / Télécharger un fichier |
| Verrou | Bloquer / Verrouiller un compte ou dossier |
| Déverrou | Débloquer / Libérer |
| Flèche verte | Valider / Approuver |
| Croix rouge | Rejeter / Annuler |

**Notes, avertissements et conseils**

> **NOTE :** Information complémentaire utile mais non critique.

> **ATTENTION :** Action irréversible ou pouvant affecter des données importantes.

> **CONSEIL :** Bonne pratique recommandée.

---

## 2. Connexion et authentification

### 2.1 Accès à l'application

MICROFINA++ est accessible depuis n'importe quel poste connecté au réseau local de l'institution. Ouvrez votre navigateur web et saisissez l'adresse suivante dans la barre d'URL :

```
http://localhost:4200
```

Si l'application est déployée sur un serveur centralisé accessible depuis plusieurs postes, l'URL sera de la forme :

```
http://<adresse-ip-serveur>:4200
```

Exemple : `http://192.168.1.100:4200`

Consultez votre administrateur système pour connaître l'URL exacte configurée dans votre établissement.

> **NOTE :** Pour une utilisation optimale, utilisez Google Chrome en version 110 ou supérieure. Désactivez les extensions susceptibles de bloquer les requêtes JavaScript (ad-blockers, etc.) si vous rencontrez des problèmes d'affichage.

### 2.2 Page de connexion

La page de connexion s'affiche au chargement de l'URL. Elle présente les champs suivants :

**Champ « Identifiant »**  
Saisissez votre nom d'utilisateur fourni par votre administrateur système. L'identifiant est sensible à la casse.

**Champ « Mot de passe »**  
Saisissez votre mot de passe personnel. Les caractères saisis sont masqués (affichés sous forme de points). Cliquez sur l'icône œil à droite du champ pour afficher temporairement le mot de passe saisi.

**Bouton « Se connecter »**  
Cliquez sur ce bouton ou appuyez sur la touche `Entrée` pour valider votre connexion.

**Procédure de connexion étape par étape :**

1. Ouvrez votre navigateur web.
2. Accédez à l'URL `http://localhost:4200`.
3. La page de connexion MICROFINA++ s'affiche avec le logo de la BCM.
4. Dans le champ **Identifiant**, saisissez votre nom d'utilisateur (exemple : `agent.credit01`).
5. Dans le champ **Mot de passe**, saisissez votre mot de passe.
6. Cliquez sur le bouton **Se connecter**.
7. Si les identifiants sont corrects, vous êtes redirigé vers le tableau de bord principal.

**Gestion des erreurs de connexion :**

- Si l'identifiant ou le mot de passe est incorrect, un message d'erreur s'affiche : _« Identifiant ou mot de passe incorrect. »_
- Après **5 tentatives échouées consécutives**, le compte est automatiquement verrouillé pendant 30 minutes. Contactez votre administrateur pour un déverrouillage immédiat.
- Si votre compte a été désactivé par l'administrateur, le message _« Ce compte est désactivé. Contactez votre administrateur. »_ s'affiche.

### 2.3 Déconnexion

Il est impératif de se déconnecter de l'application après chaque session de travail afin de protéger les données confidentielles.

**Procédure de déconnexion :**

1. Cliquez sur votre **nom d'utilisateur** affiché en haut à droite de l'écran.
2. Un menu déroulant s'affiche avec les options : **Profil**, **Changer le mot de passe**, **Déconnexion**.
3. Cliquez sur **Déconnexion**.
4. Vous êtes redirigé vers la page de connexion.

> **ATTENTION :** Une session inactive pendant plus de **30 minutes** est automatiquement déconnectée par le système. Toute saisie non enregistrée sera perdue. Pensez à sauvegarder régulièrement votre travail.

### 2.4 Gestion du mot de passe

#### 2.4.1 Changer son mot de passe

Il est fortement recommandé de changer le mot de passe fourni par l'administrateur lors de votre première connexion.

1. Cliquez sur votre **nom d'utilisateur** en haut à droite.
2. Sélectionnez **Changer le mot de passe**.
3. Renseignez les champs suivants :
   - **Ancien mot de passe** : votre mot de passe actuel.
   - **Nouveau mot de passe** : votre nouveau mot de passe (minimum 8 caractères, dont au moins une majuscule, un chiffre et un caractère spécial).
   - **Confirmer le nouveau mot de passe** : ressaisir le nouveau mot de passe.
4. Cliquez sur **Enregistrer**.
5. Un message de confirmation s'affiche : _« Mot de passe modifié avec succès. »_

#### 2.4.2 Mot de passe oublié

Si vous avez oublié votre mot de passe :

1. Sur la page de connexion, cliquez sur le lien **Mot de passe oublié ?**
2. Saisissez votre **adresse e-mail professionnelle** enregistrée dans le système.
3. Cliquez sur **Envoyer**.
4. Un e-mail contenant un lien de réinitialisation valable **24 heures** vous est envoyé.
5. Cliquez sur le lien reçu par e-mail.
6. Définissez un nouveau mot de passe en respectant les critères de sécurité.

> **NOTE :** Si vous n'avez pas reçu l'e-mail dans les 5 minutes, vérifiez votre dossier courrier indésirable. Si le problème persiste, contactez votre administrateur système.

---

## 3. Tableau de bord

Le tableau de bord est la première page affichée après la connexion. Il offre une vue synthétique et en temps réel de l'activité de l'institution.

### 3.1 Widgets principaux

Le tableau de bord est composé de plusieurs widgets (blocs d'information) disposés en grille. Le contenu affiché dépend du profil de l'utilisateur connecté.

#### 3.1.1 Widget « Encours de crédit »

Affiche le montant total de l'encours brut du portefeuille de crédit de l'institution, exprimé en Ouguiya mauritanienne (MRU).

- **Encours brut total** : somme des capitaux restants dus sur l'ensemble des crédits actifs.
- **Évolution mensuelle** : variation en pourcentage par rapport au mois précédent, indiquée par une flèche verte (hausse) ou rouge (baisse).
- **Cliquez sur le widget** pour accéder à la liste des crédits actifs.

#### 3.1.2 Widget « Membres actifs »

Affiche le nombre total de membres actifs dans le système.

- **Total membres** : nombre cumulé de membres dont le statut est « Actif ».
- **Nouveaux ce mois** : nombre de nouvelles adhésions validées au cours du mois en cours.
- **Répartition PP/PM** : graphique en secteurs distinguant les personnes physiques et les personnes morales.

#### 3.1.3 Widget « PAR (Portefeuille à Risque) »

Le PAR (Portfolio at Risk) est l'indicateur prudentiel central en microfinance. Il mesure la proportion du portefeuille de crédit présentant des impayés.

- **PAR > 30 jours** : pourcentage du capital restant dû sur les crédits ayant au moins une échéance impayée depuis plus de 30 jours.
- **PAR > 90 jours** : indicateur de risque élevé (crédits en souffrance).
- **Couleur d'alerte** :
  - Vert : PAR ≤ 5%
  - Orange : 5% < PAR ≤ 10%
  - Rouge : PAR > 10%

#### 3.1.4 Widget « Épargne totale »

Affiche le solde cumulé de tous les comptes épargne actifs.

#### 3.1.5 Widget « Opérations du jour »

Résumé des opérations saisies dans la journée en cours :
- Nombre de remboursements encaissés.
- Nombre de nouveaux crédits débloqués.
- Nombre de dépôts et retraits épargne.

#### 3.1.6 Widget « Alertes »

Liste des alertes système nécessitant une action :
- Dossiers de crédit en attente de validation.
- Adhésions en attente d'approbation.
- Garanties arrivant à échéance.
- Alertes de sécurité (tentatives de connexion échouées, etc.).

### 3.2 Navigation dans le menu latéral

Le menu latéral (sidebar) est positionné sur la gauche de l'écran. Il est organisé en sections correspondant aux modules du système. Cliquez sur une section pour la développer et afficher ses sous-menus.

**Structure du menu latéral :**

```
Tableau de bord
├── Membres
│   ├── Liste des membres
│   ├── Nouveau membre
│   └── Adhésions en attente
├── Crédits
│   ├── Liste des crédits
│   ├── Nouvelle demande
│   ├── Remboursements
│   └── Simulateur
├── Épargne
│   ├── Comptes épargne
│   └── Opérations épargne
├── Garanties
│   ├── Liste des garanties
│   └── Nouvelle garantie
├── Comptabilité
│   ├── Écritures comptables
│   ├── Grand livre
│   ├── Balance
│   ├── Journal
│   └── Bilan
├── Caisse & Banque
│   ├── Opérations de caisse
│   ├── Opérations bancaires
│   └── Carnets de chèques
├── Wallet Bankily
│   ├── Opérations wallet
│   └── Réconciliation
├── Reporting BCM
│   ├── RMA
│   ├── Tableau de bord BCM
│   └── Export CSV BCM
├── Exports
│   ├── Centre d'exports
│   └── Export Sage
├── Cartographie
│   ├── Zones d'activité
│   └── Localisation membres
└── Administration (admin uniquement)
    ├── Utilisateurs
    ├── Rôles & Privilèges
    ├── Journal d'audit
    ├── Sauvegarde
    └── Monitoring
```

Pour **réduire** le menu latéral et agrandir la zone de travail, cliquez sur l'icône hamburger (≡) en haut du menu. Le menu se réduit à des icônes uniquement. Cliquez à nouveau pour le déployer.

---

## 4. Gestion des membres

### 4.1 Recherche d'un membre

La recherche de membres est accessible depuis `Menu > Membres > Liste des membres`.

**Filtres de recherche disponibles :**

| Champ | Description |
|---|---|
| Numéro de membre | Identifiant unique attribué automatiquement à l'inscription |
| Nom / Prénom | Recherche partielle (minimum 3 caractères) |
| Numéro CNI / NIF | Numéro national d'identité ou numéro d'identification fiscale |
| Téléphone | Numéro de téléphone mobile |
| Statut | Actif, Inactif, En attente, Rejeté |
| Type | Personne physique (PP) ou Personne morale (PM) |
| Agence | Filtrer par agence d'appartenance |
| Date d'adhésion | Plage de dates (du … au …) |

**Procédure de recherche :**

1. Accédez à `Menu > Membres > Liste des membres`.
2. Renseignez un ou plusieurs critères dans le panneau de filtres en haut du tableau.
3. Cliquez sur le bouton **Rechercher** (icône loupe).
4. Les résultats s'affichent dans le tableau en dessous.
5. Cliquez sur le bouton **Détail** (icône œil) sur la ligne souhaitée pour ouvrir la fiche du membre.

> **CONSEIL :** Pour rechercher rapidement un membre, utilisez la barre de recherche rapide en haut à droite du tableau (raccourci clavier : `Ctrl + F`). Elle effectue une recherche simultanée sur le nom, le numéro de membre et le numéro CNI.

### 4.2 Créer un nouveau membre

La création d'un membre est accessible depuis `Menu > Membres > Nouveau membre`.

#### 4.2.1 Personne physique (PP)

Remplissez les onglets du formulaire dans l'ordre :

**Onglet « Informations personnelles » :**

| Champ | Obligatoire | Description |
|---|---|---|
| Civilité | Oui | M., Mme, Dr, etc. |
| Nom | Oui | Nom de famille en majuscules |
| Prénom(s) | Oui | Prénom(s) usuel(s) |
| Date de naissance | Oui | Format JJ/MM/AAAA |
| Lieu de naissance | Non | Ville ou commune de naissance |
| Nationalité | Oui | Mauritanienne par défaut |
| Numéro CNI | Oui | Numéro de la carte nationale d'identité |
| Date d'expiration CNI | Oui | Date d'expiration de la pièce d'identité |
| Genre | Oui | Masculin / Féminin |
| Situation matrimoniale | Non | Célibataire, Marié(e), Divorcé(e), Veuf/Veuve |
| Nombre de personnes à charge | Non | Entier positif |

**Onglet « Coordonnées » :**

| Champ | Obligatoire | Description |
|---|---|---|
| Téléphone mobile | Oui | Format +222 XXXXXXXX |
| Téléphone fixe | Non | |
| E-mail | Non | Adresse électronique |
| Wilaya | Oui | Wilaya de résidence (liste déroulante) |
| Moughataa | Oui | Moughataa de résidence |
| Commune | Non | |
| Adresse complète | Non | Quartier, rue, numéro |

**Onglet « Activité économique » :**

| Champ | Obligatoire | Description |
|---|---|---|
| Secteur d'activité | Oui | Commerce, Agriculture, Élevage, Services, etc. |
| Sous-secteur | Non | Précision du secteur |
| Revenus mensuels estimés | Non | Montant en MRU |
| Source de revenus | Non | Salarié, Indépendant, Agriculture, etc. |

**Onglet « Adhésion » :**

| Champ | Obligatoire | Description |
|---|---|---|
| Agence | Oui | Agence d'rattachement |
| Agent responsable | Oui | Agent de crédit référent |
| Date d'adhésion | Oui | Date de la demande d'adhésion (auto-remplie) |
| Droit d'adhésion | Non | Montant du droit d'entrée si applicable |
| Part sociale souscrite | Non | Nombre de parts sociales |

**Onglet « Documents » :**

Téléversez les documents justificatifs (formats acceptés : PDF, JPG, PNG, taille max : 5 Mo par fichier) :
- Copie de la CNI (recto/verso).
- Justificatif de domicile.
- Photo d'identité (voir section 4.5).
- Tout autre document requis par la politique interne.

Cliquez sur **Enregistrer le brouillon** pour sauvegarder sans soumettre, ou sur **Soumettre l'adhésion** pour envoyer le dossier en validation.

#### 4.2.2 Personne morale (PM)

Pour créer une personne morale, sélectionnez le type **Personne morale** au début du formulaire. Les onglets sont légèrement différents :

**Onglet « Informations entreprise » :**

| Champ | Obligatoire | Description |
|---|---|---|
| Raison sociale | Oui | Dénomination officielle de l'entreprise |
| Forme juridique | Oui | SA, SARL, GIE, Association, Coopérative, etc. |
| NIF (Numéro d'Identification Fiscale) | Oui | Numéro fiscal de l'entreprise |
| Numéro RCCM | Oui | Registre du Commerce et du Crédit Mobilier |
| Date de création | Oui | Date de constitution de l'entité |
| Capital social | Non | Montant du capital en MRU |
| Secteur d'activité | Oui | |

**Onglet « Représentant légal » :**

Renseignez les informations du représentant légal habilité à contracter (nom, prénom, CNI, fonction, téléphone, e-mail).

### 4.3 Valider ou rejeter une adhésion

Les adhésions soumises par les agents apparaissent dans la file d'attente accessible depuis `Menu > Membres > Adhésions en attente`.

**Procédure de validation :**

1. Accédez à `Menu > Membres > Adhésions en attente`.
2. Consultez la liste des dossiers en attente.
3. Cliquez sur le bouton **Détail** (icône œil) pour ouvrir le dossier complet.
4. Vérifiez l'ensemble des informations et des pièces justificatives.
5. Pour **valider** : cliquez sur le bouton **Approuver** (icône flèche verte). Saisissez un commentaire si nécessaire, puis confirmez. Le membre passe au statut **Actif** et un numéro de membre est attribué automatiquement.
6. Pour **rejeter** : cliquez sur le bouton **Rejeter** (icône croix rouge). Saisissez obligatoirement le **motif du rejet** dans le champ prévu, puis confirmez. Le dossier est renvoyé à l'agent avec le motif de rejet.

> **NOTE :** Le motif de rejet est visible par l'agent qui a créé le dossier. Il peut corriger le dossier et le soumettre à nouveau.

### 4.4 Désactiver un membre

La désactivation d'un membre est une action irréversible depuis l'interface standard (seul un administrateur peut réactiver un compte désactivé).

**Prérequis avant désactivation :**

- Le membre ne doit avoir aucun crédit actif (capital restant dû > 0).
- Le membre ne doit avoir aucun compte épargne avec solde positif.
- Toutes les garanties associées doivent être libérées.

**Procédure :**

1. Ouvrez la fiche du membre à désactiver.
2. Cliquez sur le bouton **Actions** (menu déroulant en haut à droite de la fiche).
3. Sélectionnez **Désactiver le membre**.
4. Une boîte de dialogue s'affiche listant les prérequis vérifiés. Si des obstacles sont détectés (crédit actif, épargne non nulle), le système bloque l'opération et indique les actions correctives nécessaires.
5. Si tous les prérequis sont satisfaits, saisissez le **motif de désactivation** et confirmez.
6. Le statut du membre passe à **Inactif**.

> **ATTENTION :** Un membre désactivé ne peut plus accéder aux services de l'institution. Cette action est journalisée dans le journal d'audit.

### 4.5 Upload de la photo du membre

L'ajout d'une photo d'identité est requis pour chaque membre.

**Procédure :**

1. Ouvrez la fiche du membre.
2. Dans la zone de la photo (coin supérieur gauche de la fiche), cliquez sur l'icône **Appareil photo** ou sur le cadre de photo vide.
3. Une fenêtre de sélection de fichier s'ouvre. Sélectionnez une image au format JPG ou PNG (taille recommandée : 300×300 pixels minimum, taille max : 2 Mo).
4. Après sélection, un outil de recadrage (crop) s'affiche pour ajuster l'image.
5. Ajustez le cadrage et cliquez sur **Valider le recadrage**.
6. Cliquez sur **Enregistrer** pour sauvegarder la photo dans le profil du membre.

> **CONSEIL :** Utilisez une photo nette, sur fond uni, avec le visage bien visible et centré. Évitez les photos avec lunettes de soleil ou couvre-chef.

---

## 5. Produits de crédit

### 5.1 Consulter le référentiel des produits

Le référentiel des produits de crédit est accessible depuis `Menu > Produits > Référentiel crédits`.

La liste affiche tous les produits de crédit configurés, avec pour chaque produit :
- **Code produit** : identifiant unique.
- **Libellé** : nom commercial du produit.
- **Type** : Individuel, Solidaire, PME, etc.
- **Durée min/max** : en mois.
- **Taux d'intérêt** : taux nominal annuel en %.
- **Statut** : Actif / Inactif.

Cliquez sur un produit pour consulter son détail complet.

### 5.2 Créer un produit de crédit

Accessible uniquement aux utilisateurs disposant du privilège **Gestion des produits** (généralement le directeur ou l'administrateur).

Chemin : `Menu > Produits > Nouveau produit de crédit`

**Formulaire de création — Onglet « Informations générales » :**

| Champ | Obligatoire | Description |
|---|---|---|
| Code produit | Oui | Code unique (auto-généré ou saisi manuellement) |
| Libellé | Oui | Nom du produit (ex : « Crédit Commerce ») |
| Description | Non | Description détaillée du produit |
| Type de crédit | Oui | Individuel / Solidaire / PME / Agricole |
| Devise | Oui | MRU (Ouguiya) par défaut |
| Statut | Oui | Actif / Inactif |

### 5.3 Paramétrer les taux et durées

**Onglet « Taux et durées » :**

| Champ | Description |
|---|---|
| Taux d'intérêt nominal (annuel) | Taux en % appliqué au capital (ex : 24%) |
| Méthode de calcul | Dégressif (sur capital restant dû) ou Linéaire (sur capital initial) |
| Durée minimale | Durée minimale du crédit en mois |
| Durée maximale | Durée maximale du crédit en mois |
| Montant minimum | Montant plancher du crédit en MRU |
| Montant maximum | Montant plafond du crédit en MRU |
| Fréquence de remboursement | Mensuelle, Bimestrielle, Trimestrielle, In fine |
| Période de grâce | Nombre de mois sans remboursement en capital (0 par défaut) |

**Onglet « Frais et commissions » :**

| Champ | Description |
|---|---|
| Frais de dossier | Montant fixe ou pourcentage du capital accordé |
| Assurance | Taux d'assurance (optionnel) |
| Droit de timbre | Montant fixe si applicable |
| Pénalité de retard | Taux de pénalité journalier sur échéances impayées |

**Onglet « Comptabilisation » :**

Associez les comptes du plan comptable aux différentes natures de flux du produit :
- Compte de capital (actif).
- Compte d'intérêts courus.
- Compte de produits d'intérêts.
- Compte de provisions.

Cliquez sur **Enregistrer** pour créer le produit. Il est immédiatement disponible pour la saisie de nouvelles demandes de crédit si son statut est **Actif**.

---

## 6. Gestion des crédits

### 6.1 Saisir une demande de crédit

Chemin : `Menu > Crédits > Nouvelle demande`

**Étape 1 — Sélection du membre**

1. Dans le champ **Rechercher un membre**, saisissez le nom ou le numéro du membre.
2. Sélectionnez le membre dans la liste déroulante de résultats.
3. Le système vérifie automatiquement l'éligibilité du membre (statut actif, pas de crédit impayé en cours selon la politique configurée).
4. Un récapitulatif de la fiche membre s'affiche à droite (nom, numéro, photo, encours existants).

**Étape 2 — Paramètres du crédit**

| Champ | Obligatoire | Description |
|---|---|---|
| Produit de crédit | Oui | Sélectionner dans la liste déroulante |
| Montant demandé | Oui | Montant en MRU (doit être dans la plage min/max du produit) |
| Durée (mois) | Oui | Durée souhaitée (dans la plage autorisée par le produit) |
| Objet du crédit | Oui | Description de l'utilisation du financement |
| Garantie(s) proposée(s) | Non | Sélection dans le module Garanties |
| Date de première échéance | Oui | Date de la première mensualité |
| Déblocage via wallet | Non | Cocher si le décaissement sera effectué via Bankily |

**Étape 3 — Calcul du tableau d'amortissement**

Après avoir renseigné les paramètres, cliquez sur **Calculer** pour générer le tableau d'amortissement prévisionnel. Le tableau affiche pour chaque échéance :
- Numéro d'échéance.
- Date d'échéance.
- Capital remboursé.
- Intérêts.
- Assurance (si applicable).
- Total échéance.
- Capital restant dû.

**Étape 4 — Pièces justificatives**

Téléversez les documents requis pour l'instruction du dossier :
- Plan d'affaires ou devis.
- Justificatifs de revenus.
- Contrat de garantie signé.
- Tout document complémentaire.

**Étape 5 — Soumission**

Cliquez sur **Soumettre la demande** pour envoyer le dossier en validation. Un numéro de dossier est attribué automatiquement (format : `CR-AAAA-XXXXXXXX`).

### 6.2 Valider et débloquer un crédit

**Processus de validation (workflow) :**

1. **Analyse** : l'agent de crédit examine le dossier et émet un avis.
2. **Validation de premier niveau** : le responsable d'agence approuve ou rejette.
3. **Validation de deuxième niveau** (si montant > seuil configuré) : directeur ou comité de crédit.
4. **Déblocage** : le caissier ou le comptable procède au décaissement.

**Procédure de validation :**

1. Accédez à `Menu > Crédits > Liste des crédits`, filtrez par statut **En attente de validation**.
2. Ouvrez le dossier concerné.
3. Consultez le tableau d'amortissement, les pièces justificatives et les informations du membre.
4. Cliquez sur **Valider** (saisir un commentaire) ou **Rejeter** (motif obligatoire).

**Procédure de déblocage :**

1. Les crédits validés apparaissent avec le statut **Approuvé — En attente de déblocage**.
2. Ouvrez le dossier.
3. Vérifiez le mode de décaissement (caisse, virement bancaire ou wallet Bankily).
4. Cliquez sur **Débloquer le crédit**.
5. Saisissez la **date de déblocage effective** et le **mode de décaissement**.
6. Confirmez l'opération.
7. Le système génère automatiquement les écritures comptables de déblocage et le tableau d'amortissement définitif.
8. Le crédit passe au statut **Actif — En cours**.

> **ATTENTION :** Le déblocage génère des écritures comptables irréversibles. Vérifiez attentivement les informations avant de confirmer.

### 6.3 Consulter le tableau d'amortissement

1. Ouvrez la fiche d'un crédit actif.
2. Cliquez sur l'onglet **Tableau d'amortissement**.
3. Le tableau affiche l'ensemble des échéances avec leur statut :
   - **Payée** (fond vert) : échéance remboursée intégralement.
   - **Partiellement payée** (fond orange) : remboursement partiel reçu.
   - **En retard** (fond rouge) : échéance dépassée non payée.
   - **À venir** (fond blanc) : échéance future.
4. Le bouton **Exporter en PDF** permet de télécharger le tableau au format PDF.
5. Le bouton **Exporter en Excel** permet de télécharger le tableau au format XLSX.

### 6.4 Saisir un remboursement

Chemin : `Menu > Crédits > Remboursements > Nouveau remboursement`

1. **Rechercher le crédit** : saisissez le numéro de crédit ou le nom du membre.
2. Le système affiche la ou les échéances dues à la date du jour.
3. **Montant encaissé** : saisissez le montant réellement perçu du client (peut être différent du montant théorique en cas de remboursement partiel ou de paiement anticipé).
4. **Mode de paiement** : Espèces, Chèque, Virement, Wallet Bankily.
5. **Référence** : numéro de reçu, numéro de chèque ou référence de transaction Bankily.
6. **Date de paiement** : date effective de l'encaissement (par défaut : date du jour).
7. Cliquez sur **Valider le remboursement**.
8. Le système ventile automatiquement le montant encaissé entre les pénalités, les intérêts et le capital, selon la règle d'imputation configurée dans le produit.
9. Un reçu de paiement est automatiquement généré et peut être imprimé.

> **NOTE :** En cas de paiement anticipé total (solde du crédit), le système recalcule les intérêts selon la méthode de clôture anticipée définie dans le produit (intérêts compensateurs si applicable).

### 6.5 Simulateur de crédit

Le simulateur est accessible depuis `Menu > Crédits > Simulateur` ou depuis la page d'accueil via le widget **Simuler un crédit**.

Il permet de calculer les paramètres d'un crédit sans créer de dossier.

**Champs à renseigner :**

| Champ | Description |
|---|---|
| Produit | Sélectionner le produit de crédit |
| Montant | Montant souhaité en MRU |
| Durée | Durée en mois |
| Date de début | Date de premier déblocage |

Après avoir renseigné ces informations, cliquez sur **Simuler**. Le système affiche :
- La mensualité théorique.
- Le coût total du crédit (intérêts + frais).
- Le tableau d'amortissement complet.

Cliquez sur **Exporter la simulation en PDF** pour générer un document remettable au client.

---

## 7. Épargne et comptes épargne

### 7.1 Ouvrir un compte épargne

Chemin : `Menu > Épargne > Comptes épargne > Nouveau compte`

**Prérequis :** le membre doit exister et avoir le statut **Actif**.

**Formulaire d'ouverture :**

| Champ | Obligatoire | Description |
|---|---|---|
| Membre | Oui | Rechercher et sélectionner le membre |
| Type de compte | Oui | Épargne ordinaire, Épargne à terme, Épargne projet, etc. |
| Devise | Oui | MRU par défaut |
| Dépôt initial | Non | Montant du premier versement à l'ouverture |
| Taux d'intérêt créditeur | Non | Renseigné automatiquement selon le type de compte |
| Date d'ouverture | Oui | Date effective d'ouverture |
| Agence | Oui | Agence gestionnaire du compte |

Cliquez sur **Ouvrir le compte**. Un numéro de compte est attribué automatiquement (format : `EP-AAAA-XXXXXXXXX`).

### 7.2 Effectuer un dépôt

Chemin : `Menu > Épargne > Opérations épargne > Dépôt`

1. **Rechercher le compte épargne** : saisir le numéro de compte ou le nom du membre.
2. Renseignez les champs :
   - **Montant du dépôt** : montant en MRU.
   - **Mode de paiement** : Espèces, Chèque, Virement.
   - **Référence** : référence du versement.
   - **Date de l'opération** : date effective.
   - **Commentaire** : note optionnelle.
3. Cliquez sur **Valider le dépôt**.
4. Le solde du compte est mis à jour en temps réel.
5. Un reçu de dépôt est généré et peut être imprimé.

### 7.3 Effectuer un retrait

Chemin : `Menu > Épargne > Opérations épargne > Retrait`

1. **Rechercher le compte épargne**.
2. Le système affiche le solde disponible (qui peut différer du solde total si une partie est bloquée).
3. Renseignez les champs :
   - **Montant du retrait** : ne peut pas dépasser le solde disponible.
   - **Mode de décaissement** : Espèces, Chèque, Virement.
   - **Référence**.
   - **Date de l'opération**.
4. Cliquez sur **Valider le retrait**.
5. Un reçu de retrait est généré.

> **ATTENTION :** Si le compte est de type **Épargne à terme**, des pénalités de retrait anticipé peuvent s'appliquer conformément aux conditions du produit.

### 7.4 Bloquer un compte épargne

Le blocage d'un compte interdit toute opération de retrait sur celui-ci (les dépôts restent possibles sauf blocage total).

**Procédure :**

1. Ouvrez la fiche du compte épargne.
2. Cliquez sur **Actions > Bloquer le compte**.
3. Renseignez :
   - **Motif du blocage** : Saisie judiciaire, Demande du client, Garantie de crédit, etc.
   - **Date de début du blocage**.
   - **Date de fin prévue** (optionnel).
   - **Montant bloqué** : si blocage partiel, indiquer le montant ; laisser vide pour blocage total.
4. Cliquez sur **Confirmer le blocage**.

Pour **débloquer** un compte bloqué :
1. Ouvrez la fiche du compte.
2. Cliquez sur **Actions > Débloquer le compte**.
3. Saisissez le motif de levée du blocage et confirmez.

---

## 8. Garanties

### 8.1 Associer une garantie à un crédit

Chemin : `Menu > Garanties > Nouvelle garantie`

**Types de garanties gérés par MICROFINA++ :**

- Garantie réelle mobilière (nantissement de matériel, véhicule, etc.).
- Garantie réelle immobilière (hypothèque).
- Garantie personnelle (caution solidaire).
- Garantie financière (dépôt de garantie, blocage épargne).
- Aval ou cautionnement institutionnel.

**Formulaire de saisie de garantie :**

| Champ | Obligatoire | Description |
|---|---|---|
| Type de garantie | Oui | Sélectionner dans la liste |
| Description | Oui | Description précise du bien ou de la personne garante |
| Valeur estimée | Oui | Valeur en MRU à la date d'évaluation |
| Date d'évaluation | Oui | |
| Évaluateur | Non | Nom de l'évaluateur ou de l'expert |
| Crédit(s) associé(s) | Non | Lier à un ou plusieurs crédits |
| Documents | Non | Titre foncier, contrat, photos, etc. |

**Pour associer une garantie à un crédit lors de la saisie :**

Dans le formulaire de demande de crédit (étape 2), cliquez sur **Ajouter une garantie**, puis recherchez et sélectionnez une garantie existante, ou cliquez sur **Créer une nouvelle garantie** pour saisir une nouvelle garantie directement depuis le formulaire de crédit.

### 8.2 Libérer une garantie

La libération d'une garantie s'effectue lorsque le crédit associé est entièrement remboursé, ou sur décision de la direction.

**Procédure :**

1. Accédez à `Menu > Garanties > Liste des garanties`.
2. Recherchez la garantie à libérer.
3. Ouvrez la fiche de la garantie.
4. Vérifiez que le ou les crédits associés ont le statut **Soldé** (capital restant dû = 0).
5. Cliquez sur **Actions > Libérer la garantie**.
6. Saisissez :
   - **Date de libération**.
   - **Motif** (remboursement intégral, décision de gestion, etc.).
   - **Commentaire** éventuel.
7. Confirmez.

> **NOTE :** Si la garantie est associée à un crédit encore actif, le système bloque la libération et affiche un message d'erreur. Un administrateur peut forcer la libération dans des cas exceptionnels via le menu d'administration.

---

## 9. Comptabilité

### 9.1 Consulter les écritures comptables

Chemin : `Menu > Comptabilité > Écritures comptables`

La liste des écritures offre les filtres suivants :

| Filtre | Description |
|---|---|
| Numéro de pièce | Référence unique de l'écriture |
| Journal | Journal de saisie (Caisse, Banque, Opérations, etc.) |
| Compte | Numéro de compte du plan comptable |
| Libellé | Recherche textuelle |
| Période | Plage de dates (du … au …) |
| Statut | Lettré / Non lettré / Tous |
| Sens | Débit / Crédit / Tous |

Chaque ligne affiche : numéro de pièce, date, journal, libellé, numéro de compte, débit, crédit, statut de lettrage.

### 9.2 Lettrer une écriture

Le lettrage permet de rapprocher les écritures débitrices et créditrices d'un même compte pour identifier les transactions soldées.

**Procédure :**

1. Filtrez les écritures sur le compte à lettrer (ex : compte client d'un membre).
2. Sélectionnez les écritures à lettrer en cochant les cases à gauche.
3. Vérifiez que la **somme des débits** est égale à la **somme des crédits** sélectionnées (la balance de sélection est affichée en bas de page).
4. Cliquez sur **Lettrer la sélection**.
5. Saisissez une **référence de lettrage** (optionnel).
6. Confirmez.
7. Les écritures lettrées sont identifiées par une icône de lien et une référence de lettrage commune.

Pour **délettrer** un groupe d'écritures lettrées :
1. Ouvrez l'une des écritures du groupe.
2. Cliquez sur **Actions > Délettrer**.
3. Confirmez.

### 9.3 Grand livre

Chemin : `Menu > Comptabilité > Grand livre`

Le grand livre présente l'historique complet des mouvements pour chaque compte.

**Paramètres de génération :**

| Paramètre | Description |
|---|---|
| Compte(s) | Sélectionner un compte ou une plage de comptes |
| Période | Du … au … |
| Afficher les écritures lettrées | Oui / Non |
| Format d'export | PDF, Excel |

Le grand livre affiche pour chaque compte :
- Solde d'ouverture de la période.
- Liste chronologique des mouvements avec date, libellé, débit, crédit.
- Solde progressif après chaque mouvement.
- Solde de clôture de la période.

### 9.4 Balance des comptes

Chemin : `Menu > Comptabilité > Balance`

La balance affiche pour chaque compte du plan comptable :
- **Solde initial** (début de la période ou début d'exercice).
- **Mouvements de la période** : total débit et total crédit.
- **Solde final** : solde à la date de fin de la période.

**Types de balance disponibles :**

- Balance générale (tous les comptes).
- Balance par nature (actif, passif, produits, charges).
- Balance analytique (par agence, par agent, par produit).

Cliquez sur un compte dans la balance pour naviguer directement vers son grand livre.

### 9.5 Journal comptable

Chemin : `Menu > Comptabilité > Journal`

Le journal comptable présente les écritures groupées par journal :

- **Journal de caisse** (JC) : toutes les opérations d'encaissement et de décaissement en espèces.
- **Journal de banque** (JB) : mouvements des comptes bancaires.
- **Journal des opérations de crédit** (JK) : déblocages, remboursements.
- **Journal des opérations d'épargne** (JE) : dépôts et retraits épargne.
- **Journal des opérations diverses** (JOD) : écritures manuelles, régularisations.

**Paramètres :** sélectionnez le journal souhaité et la période, puis cliquez sur **Afficher**. Exportez en PDF ou Excel.

### 9.6 Bilan simplifié

Chemin : `Menu > Comptabilité > Bilan`

Le bilan simplifié est généré à la date sélectionnée et présente :

**ACTIF :**
- Trésorerie (caisse + banque).
- Portefeuille de crédits net (encours brut – provisions).
- Immobilisations nettes.
- Autres actifs.

**PASSIF :**
- Épargne des membres.
- Emprunts et ressources extérieures.
- Fonds propres.
- Résultat de l'exercice.

Le bilan est exportable en PDF avec en-tête institutionnel.

---

## 10. Opérations de caisse et bancaires

### 10.1 Opérations de caisse

Chemin : `Menu > Caisse & Banque > Opérations de caisse`

#### 10.1.1 Ouverture de caisse

Chaque journée de travail commence par une ouverture de caisse :

1. Accédez à `Menu > Caisse & Banque > Opérations de caisse`.
2. Cliquez sur **Ouvrir la caisse**.
3. Saisissez le **solde d'ouverture** (fonds de caisse du matin).
4. Confirmez. La caisse est désormais ouverte pour la journée.

#### 10.1.2 Saisir une entrée de caisse

1. Cliquez sur **Nouvelle opération de caisse**.
2. Sélectionnez le **type d'opération** : Remboursement de crédit, Dépôt épargne, Droit d'adhésion, Autre recette.
3. Renseignez le montant, la référence et le bénéficiaire/payeur.
4. Cliquez sur **Valider**.

#### 10.1.3 Saisir une sortie de caisse

1. Cliquez sur **Nouvelle opération de caisse**.
2. Sélectionnez le **type** : Décaissement de crédit, Retrait épargne, Dépense opérationnelle, etc.
3. Renseignez le montant, la référence et le destinataire.
4. Cliquez sur **Valider**.

#### 10.1.4 Fermeture de caisse

En fin de journée :

1. Cliquez sur **Fermer la caisse**.
2. Le système affiche le **solde théorique** calculé à partir des opérations de la journée.
3. Saisissez le **solde physique réel** (après comptage des espèces).
4. Si un **écart** est constaté, saisissez un commentaire explicatif.
5. Confirmez la fermeture.
6. Un **arrêté de caisse journalier** est généré automatiquement, prêt à être imprimé.

### 10.2 Opérations bancaires (virements)

Chemin : `Menu > Caisse & Banque > Opérations bancaires`

#### 10.2.1 Saisir un virement sortant

1. Cliquez sur **Nouveau virement**.
2. Renseignez :
   - **Compte bancaire débiteur** : sélectionner le compte de l'institution.
   - **Bénéficiaire** : nom et coordonnées bancaires (RIB, IBAN).
   - **Banque du bénéficiaire** : sélectionner dans la liste.
   - **Montant** : en MRU.
   - **Motif** : description de l'opération.
   - **Date d'exécution souhaitée**.
   - **Référence externe** : numéro de virement de la banque si disponible.
3. Cliquez sur **Enregistrer**.
4. Le virement est soumis à une validation par un deuxième utilisateur autorisé (principe des quatre yeux).
5. Après validation, le virement est transmis à la banque.

#### 10.2.2 Rapprochement bancaire

1. Accédez à `Menu > Caisse & Banque > Rapprochement bancaire`.
2. Importez le **relevé bancaire** au format CSV ou saisissez les lignes manuellement.
3. Le système effectue un rapprochement automatique en comparant les mouvements du relevé avec les opérations enregistrées.
4. Les opérations non rapprochées sont listées pour traitement manuel.
5. Pour rapprocher manuellement : sélectionnez l'opération bancaire et l'opération comptable correspondante, puis cliquez sur **Rapprocher**.

### 10.3 Carnets de chèques

Chemin : `Menu > Caisse & Banque > Carnets de chèques`

#### 10.3.1 Enregistrer un nouveau carnet de chèques

1. Cliquez sur **Nouveau carnet**.
2. Renseignez :
   - **Compte bancaire** associé.
   - **Numéro de premier chèque** et **numéro de dernier chèque**.
   - **Date de réception** du carnet.
3. Enregistrez. Le carnet est créé avec l'ensemble des chèques en statut **Disponible**.

#### 10.3.2 Émettre un chèque

1. Lors de la saisie d'une opération bancaire, sélectionnez **Chèque** comme mode de paiement.
2. Le système affiche le prochain numéro de chèque disponible dans le carnet sélectionné.
3. Confirmez l'utilisation du chèque. Son statut passe à **Émis**.

#### 10.3.3 Marquer un chèque comme encaissé ou rejeté

Depuis la liste des chèques émis :
- Cliquez sur **Encaissé** pour marquer le chèque comme débité du compte.
- Cliquez sur **Rejeté** pour signaler le rejet (chèque sans provision, erreur, etc.) et saisir le motif.

---

## 11. Wallet Bankily

### 11.1 Présentation de l'intégration Bankily

Bankily est la plateforme de monnaie mobile nationale en Mauritanie, opérée par Mauritel. MICROFINA++ intègre l'API Bankily pour permettre :
- Le décaissement des crédits directement vers le wallet Bankily du membre.
- L'encaissement des remboursements depuis le wallet Bankily.
- Les dépôts et retraits épargne via Bankily.

**Prérequis :**

- Le membre doit avoir renseigné un numéro de téléphone Bankily actif dans sa fiche.
- L'institution doit avoir signé une convention avec Bankily et configuré ses paramètres d'API dans le module d'administration (`Administration > Paramètres > Bankily`).

### 11.2 Initier un déblocage via wallet

1. Lors du déblocage d'un crédit (section 6.2), sélectionnez **Wallet Bankily** comme mode de décaissement.
2. Le champ **Numéro Bankily** est pré-rempli avec le numéro du membre ; vérifiez-le.
3. Cliquez sur **Débloquer via Bankily**.
4. Le système appelle l'API Bankily (`POST /api/v1/transactions/transfer`) et attend la confirmation.
5. Une fois la transaction confirmée par Bankily, le statut du crédit passe à **Actif — En cours** et la transaction Bankily est enregistrée avec sa référence unique.
6. En cas d'échec (numéro invalide, wallet inactif, erreur réseau), un message d'erreur s'affiche avec le code retour Bankily. Corrigez les informations et réessayez, ou basculez vers un autre mode de décaissement.

### 11.3 Suivi des opérations wallet

Chemin : `Menu > Wallet Bankily > Opérations wallet`

La liste des opérations Bankily affiche :
- **Référence Bankily** : identifiant unique de la transaction côté Bankily.
- **Référence interne** : numéro de crédit, de remboursement ou d'opération épargne.
- **Type** : Déblocage, Remboursement, Dépôt, Retrait.
- **Montant** : en MRU.
- **Numéro wallet** : numéro Bankily du membre.
- **Statut** : En attente, Succès, Échoué, En cours de réconciliation.
- **Date/heure** : horodatage de l'opération.

Utilisez les filtres (date, statut, type) pour affiner la recherche. Cliquez sur une opération pour voir son détail complet.

### 11.4 Réconciliation Bankily

Chemin : `Menu > Wallet Bankily > Réconciliation`

La réconciliation permet de comparer les transactions enregistrées dans MICROFINA++ avec le relevé officiel fourni par Bankily.

**Procédure :**

1. Importez le fichier de relevé Bankily au format CSV (fourni par Bankily sur demande ou téléchargeable depuis le portail partenaire Bankily).
2. Sélectionnez la **période de réconciliation**.
3. Cliquez sur **Lancer la réconciliation**.
4. Le système compare les transactions et identifie :
   - **Transactions rapprochées** : présentes des deux côtés avec les mêmes montants et références.
   - **Transactions en attente** : présentes dans MICROFINA++ mais pas dans le relevé Bankily (en cours de traitement).
   - **Transactions orphelines** : présentes dans le relevé Bankily mais pas dans MICROFINA++ (à traiter manuellement).
   - **Écarts de montant** : références identiques mais montants différents.
5. Traitez les anomalies identifiées : saisie manuelle, correction ou signalement au support Bankily.
6. Cliquez sur **Valider la réconciliation** pour clôturer la session de réconciliation.

---

## 12. Reporting BCM

### 12.1 Rapport Mensuel d'Activité (RMA)

Le Rapport Mensuel d'Activité est le document réglementaire transmis mensuellement à la Banque Centrale de Mauritanie par chaque IMF.

Chemin : `Menu > Reporting BCM > RMA`

**Générer le RMA :**

1. Sélectionnez le **mois** et l'**année** du rapport.
2. Cliquez sur **Générer le RMA**.
3. Le système compile automatiquement les données des différents modules.
4. Le rapport est divisé en plusieurs tableaux réglementaires :
   - **Tableau 1** : données sur le portefeuille de crédits (encours, décaissements, remboursements, provisions).
   - **Tableau 2** : données sur l'épargne (soldes, mouvements).
   - **Tableau 3** : indicateurs de performance (PAR, taux de remboursement, rendement de portefeuille).
   - **Tableau 4** : données sur les membres (actifs, nouveaux, sorties).
   - **Tableau 5** : données financières (résultat, fonds propres, ratios prudentiels).
5. Vérifiez les données générées. Si des corrections sont nécessaires, retournez dans les modules concernés pour corriger les données sources, puis régénérez le RMA.
6. Cliquez sur **Valider le RMA** pour le verrouiller.
7. Cliquez sur **Transmettre à la BCM** pour envoyer le rapport au système central BCM (connexion API requise).
8. Téléchargez le rapport au format **PDF** ou **Excel** pour archivage interne.

> **ATTENTION :** Un RMA validé et transmis à la BCM ne peut plus être modifié depuis l'interface standard. Contactez la BCM pour toute correction nécessaire après transmission.

### 12.2 Tableau de bord BCM

Chemin : `Menu > Reporting BCM > Tableau de bord BCM`

Ce tableau de bord est une version épurée du tableau de bord principal, présentant exclusivement les indicateurs requis par la réglementation BCM :

- **Nombre de membres actifs** (total, PP, PM).
- **Encours brut de crédits**.
- **PAR > 30 jours** et **PAR > 90 jours**.
- **Taux de remboursement** (12 derniers mois).
- **Solde total épargne**.
- **Ratio d'adéquation des fonds propres**.
- **Résultat net de l'exercice**.
- **Nombre de crédits décaissés** (cumul de l'exercice).
- **Montant total décaissé** (cumul de l'exercice).

La période d'affichage est sélectionnable (mensuelle, trimestrielle, annuelle).

### 12.3 Calcul et analyse du PAR

Chemin : `Menu > Reporting BCM > PAR`

Le module PAR permet une analyse détaillée du portefeuille à risque.

**Paramètres :**

- **Date de calcul** : le PAR est calculé à cette date.
- **Seuil(s) de retard** : PAR > 1, PAR > 30, PAR > 60, PAR > 90, PAR > 180 jours.

**Résultats affichés :**

- Encours total à la date sélectionnée.
- Nombre et montant des crédits à risque par seuil de retard.
- PAR en pourcentage pour chaque seuil.
- Graphique d'évolution du PAR sur les 12 derniers mois.
- Détail des crédits à risque avec nom du membre, montant en retard, nombre de jours de retard.

**Export :** la liste des crédits à risque est exportable en Excel pour les actions de recouvrement.

### 12.4 Export CSV format BCM

Chemin : `Menu > Reporting BCM > Export CSV BCM`

Ce module génère des fichiers CSV dans le format standardisé requis par la BCM pour l'alimentation de sa base de données de supervision.

**Fichiers disponibles :**

| Fichier | Contenu |
|---|---|
| `membres_AAAAMM.csv` | Liste exhaustive des membres et leurs caractéristiques |
| `credits_AAAAMM.csv` | Portefeuille de crédits avec détail des échéances |
| `epargne_AAAAMM.csv` | Comptes épargne et soldes |
| `operations_AAAAMM.csv` | Toutes les opérations de la période |
| `indicateurs_AAAAMM.csv` | Indicateurs de performance agrégés |

**Procédure :**

1. Sélectionnez le mois et l'année.
2. Cochez les fichiers à générer.
3. Cliquez sur **Générer les fichiers CSV**.
4. Téléchargez les fichiers et transmettez-les à la BCM selon les instructions du superviseur.

---

## 13. Exports

### 13.1 Centre d'exports

Chemin : `Menu > Exports > Centre d'exports`

Le centre d'exports centralise toutes les fonctions d'extraction de données de MICROFINA++.

**Catalogue des exports disponibles :**

| Rapport | Formats disponibles | Description |
|---|---|---|
| Liste des membres | Excel, PDF | Annuaire complet des membres avec filtres |
| Fiches membres individuelles | PDF | Fiche détaillée par membre |
| Liste des crédits | Excel, PDF | Portefeuille avec statuts et encours |
| Tableau d'amortissement | PDF, Excel | Par crédit individuel ou en masse |
| État des impayés | Excel, PDF | Crédits en retard avec ancienneté |
| Liste des épargnes | Excel | Comptes et soldes par membre |
| Relevé de compte épargne | PDF | Par compte sur une période |
| Grand livre | Excel, PDF | Par compte sur une période |
| Balance comptable | Excel, PDF | Générale ou analytique |
| Journal comptable | Excel, PDF | Par journal et par période |
| Arrêtés de caisse | PDF | Journaliers ou par période |
| Rapport PAR | Excel, PDF | Analyse portefeuille à risque |
| RMA | PDF, Excel | Rapport mensuel BCM |
| Statistiques membres | Excel | Analyses démographiques et géographiques |

**Procédure d'export :**

1. Dans le centre d'exports, cliquez sur le rapport souhaité.
2. Renseignez les paramètres (période, filtre par agence, format, etc.).
3. Cliquez sur **Générer**.
4. Une barre de progression s'affiche pendant la génération (quelques secondes à quelques minutes selon le volume).
5. Lorsque la génération est terminée, un lien de téléchargement apparaît.
6. Cliquez sur le lien pour télécharger le fichier.

> **NOTE :** Les exports volumineux (plus de 10 000 lignes) sont traités en mode asynchrone. Une notification apparaît dans la barre d'alertes lorsque le fichier est prêt.

### 13.2 Export Sage Compta Ligne L

Chemin : `Menu > Exports > Export Sage`

Cet export permet de transférer les écritures comptables de MICROFINA++ vers le logiciel de comptabilité **Sage Compta** en utilisant le format d'import **Ligne L** (format texte délimité reconnu par Sage).

**Paramètres de l'export :**

| Paramètre | Description |
|---|---|
| Journal(ux) | Sélectionner le ou les journaux à exporter |
| Période | Du … au … |
| Plan comptable Sage | Sélectionner le mapping de comptes configuré |
| Encode du fichier | ANSI (Windows-1252) ou UTF-8 selon la version Sage |
| Séparateur | Virgule ou point-virgule |

**Procédure :**

1. Renseignez les paramètres.
2. Cliquez sur **Générer l'export Sage**.
3. Téléchargez le fichier `.txt` ou `.csv` généré.
4. Importez ce fichier dans Sage Compta via le menu `Traitements > Import/Export > Import d'écritures > Format Ligne L`.

**Mapping de comptes :**

Le mapping entre les comptes MICROFINA++ (plan comptable SYSCOHADA microfinance) et les comptes Sage est configurable depuis `Administration > Paramètres > Mapping Sage`. Consultez votre administrateur système pour la configuration initiale.

---

## 14. Administration (réservé aux administrateurs)

Ce module est exclusivement accessible aux utilisateurs ayant le rôle **Administrateur système** ou **Super Administrateur**.

### 14.1 Gestion des utilisateurs

Chemin : `Menu > Administration > Utilisateurs`

**Créer un nouvel utilisateur :**

1. Cliquez sur **Nouvel utilisateur**.
2. Renseignez :
   - **Prénom et Nom**.
   - **Identifiant de connexion** (login).
   - **Adresse e-mail professionnelle**.
   - **Téléphone**.
   - **Rôle** : sélectionner parmi les rôles disponibles.
   - **Agence(s)** : affecter l'utilisateur à une ou plusieurs agences.
   - **Statut** : Actif / Inactif.
3. Cliquez sur **Créer**. Un e-mail est automatiquement envoyé à l'utilisateur avec ses identifiants de connexion et un lien pour définir son mot de passe initial.

**Modifier un utilisateur existant :**

1. Recherchez l'utilisateur dans la liste.
2. Cliquez sur l'icône crayon.
3. Modifiez les informations souhaitées.
4. Cliquez sur **Enregistrer**.

**Désactiver un utilisateur :**

1. Ouvrez la fiche de l'utilisateur.
2. Changez le **statut** de Actif à Inactif.
3. Enregistrez.

> **NOTE :** La désactivation d'un utilisateur ne supprime pas ses données historiques. Toutes ses opérations passées restent traçables dans le journal d'audit.

**Réinitialiser le mot de passe d'un utilisateur :**

1. Ouvrez la fiche de l'utilisateur.
2. Cliquez sur **Actions > Réinitialiser le mot de passe**.
3. Un e-mail avec un lien de réinitialisation est envoyé à l'adresse e-mail de l'utilisateur.

### 14.2 Gestion des rôles

Chemin : `Menu > Administration > Rôles`

Les rôles sont des profils prédéfinis regroupant un ensemble de privilèges.

**Rôles standards livrés avec MICROFINA++ :**

| Rôle | Description |
|---|---|
| Super Administrateur | Accès complet sans restriction |
| Administrateur | Gestion des utilisateurs, paramètres, sauvegardes |
| Directeur | Accès à tous les modules en consultation + validation de niveau 2 |
| Responsable d'agence | Validation de niveau 1, consultation reporting |
| Agent de crédit | Saisie des demandes, remboursements, membres |
| Caissier | Opérations de caisse, épargne |
| Comptable | Comptabilité, exports, rapprochements |
| Auditeur | Accès lecture seule à tous les modules + journal d'audit |
| Superviseur BCM | Accès lecture + reporting BCM + exports BCM |

**Créer un rôle personnalisé :**

1. Cliquez sur **Nouveau rôle**.
2. Donnez un nom et une description au rôle.
3. Dans l'onglet **Privilèges**, cochez les privilèges à accorder (voir section 14.3).
4. Cliquez sur **Enregistrer**.

### 14.3 Gestion des privilèges

Chemin : `Menu > Administration > Rôles > [Nom du rôle] > Privilèges`

Les privilèges sont organisés par module. Pour chaque module, les actions disponibles sont :

| Action | Description |
|---|---|
| Voir | Accès en lecture seule |
| Créer | Créer de nouveaux enregistrements |
| Modifier | Modifier les enregistrements existants |
| Supprimer | Supprimer des enregistrements |
| Valider | Approuver des workflows (adhésions, crédits, etc.) |
| Exporter | Générer et télécharger des exports |
| Administrer | Accès complet au module y compris les paramètres |

Pour accorder ou retirer un privilège : cochez ou décochez la case correspondante dans la matrice de privilèges, puis cliquez sur **Enregistrer les privilèges**.

### 14.4 Journal d'audit

Chemin : `Menu > Administration > Journal d'audit`

Le journal d'audit enregistre toutes les actions réalisées dans le système, avec horodatage et identification de l'utilisateur.

**Informations enregistrées pour chaque entrée :**

- Date et heure (timestamp UTC).
- Utilisateur (login + nom).
- Adresse IP du poste.
- Module concerné.
- Action réalisée (Connexion, Création, Modification, Suppression, Validation, Export, etc.).
- Identifiant de l'objet modifié (numéro de membre, de crédit, etc.).
- Données avant modification (snapshot before).
- Données après modification (snapshot after).

**Filtres disponibles :**

- Utilisateur.
- Action.
- Module.
- Plage de dates.
- Adresse IP.

Le journal d'audit est en **lecture seule** et ne peut pas être modifié ou supprimé depuis l'interface. Il peut être exporté en Excel ou PDF pour les audits externes.

### 14.5 Sauvegarde de la base de données

Chemin : `Menu > Administration > Sauvegarde`

> **ATTENTION :** La sauvegarde régulière de la base de données est une obligation critique. Une sauvegarde doit être effectuée au minimum une fois par jour en fin de journée.

**Sauvegarde manuelle :**

1. Accédez à `Menu > Administration > Sauvegarde`.
2. Cliquez sur **Lancer une sauvegarde maintenant**.
3. Sélectionnez le **type de sauvegarde** :
   - **Complète** : sauvegarde intégrale de la base de données (recommandée en fin de semaine).
   - **Différentielle** : sauvegarde des modifications depuis la dernière sauvegarde complète.
4. Sélectionnez le **destination** de sauvegarde (disque local, partage réseau, serveur FTP).
5. Cliquez sur **Démarrer**.
6. Une barre de progression s'affiche. À la fin, un message de confirmation avec la taille et l'heure de la sauvegarde s'affiche.

**Sauvegarde automatique planifiée :**

La sauvegarde automatique est configurée depuis `Administration > Sauvegarde > Planification`. Paramétrez :
- **Fréquence** : Quotidienne, Hebdomadaire.
- **Heure d'exécution** : heure à laquelle la sauvegarde doit être déclenchée (recommandé : en dehors des heures ouvrables).
- **Rétention** : nombre de jours de conservation des sauvegardes (recommandé : 30 jours minimum).

**Restauration :**

La restauration de la base de données ne peut être effectuée que par un technicien qualifié. Contactez le support technique MICROFINA++ pour toute opération de restauration.

### 14.6 Monitoring système

Chemin : `Menu > Administration > Monitoring`

Le module de monitoring offre une vue en temps réel de la santé du système :

**Indicateurs disponibles :**

| Indicateur | Description |
|---|---|
| CPU | Utilisation du processeur du serveur en % |
| Mémoire RAM | Utilisation de la mémoire vive en % et en Go |
| Espace disque | Espace utilisé / espace total du serveur |
| Connexions actives | Nombre d'utilisateurs connectés en ce moment |
| Transactions/minute | Charge transactionnelle en temps réel |
| Latence API | Temps de réponse moyen des appels API (ms) |
| Statut base de données | Connecté / Dégradé / Hors ligne |
| Statut API Bankily | Disponible / Indisponible |
| Dernière sauvegarde | Date, heure et statut de la dernière sauvegarde |
| Erreurs système | Nombre d'erreurs dans les 24 dernières heures |

Le monitoring affiche également les **10 dernières erreurs système** avec leur message et leur horodatage. Cliquez sur une erreur pour voir la trace complète (stack trace).

---

## 15. Cartographie

### 15.1 Zones d'activité

Chemin : `Menu > Cartographie > Zones d'activité`

Le module de cartographie permet de visualiser la distribution géographique de l'activité de l'institution sur une carte interactive de la Mauritanie.

**Fonctionnalités :**

- **Carte choroplèthe** : coloration des Wilayas selon des indicateurs sélectionnés (nombre de membres, encours de crédits, PAR, etc.).
- **Heatmap** : densité de membres ou de crédits par zone géographique.
- **Filtres** :
  - Indicateur à afficher.
  - Période de référence.
  - Agence.
  - Type de membre (PP/PM).
  - Produit de crédit.

**Navigation sur la carte :**

- Cliquez sur une Wilaya pour afficher les statistiques de la zone.
- Utilisez les boutons + et – pour zoomer, ou la molette de la souris.
- Cliquez et faites glisser pour déplacer la carte.

**Export de la carte :**

Cliquez sur **Exporter** pour télécharger la carte en cours d'affichage au format PNG ou PDF.

### 15.2 Localisation des membres

Chemin : `Menu > Cartographie > Localisation membres`

Cette vue affiche la position géographique des membres sur la carte, sous forme de points ou de clusters.

**Prérequis :** les membres doivent avoir des coordonnées GPS enregistrées dans leur fiche (champs **Latitude** et **Longitude** dans l'onglet **Coordonnées**). Ces coordonnées peuvent être saisies manuellement ou capturées via l'application mobile de terrain.

**Filtres disponibles :**

- Statut du membre (Actif, Inactif, etc.).
- Type (PP / PM).
- Agence.
- Secteur d'activité.
- Statut de crédit (avec crédit actif, sans crédit, en retard).

En cliquant sur un point sur la carte, une fenêtre popup affiche le nom du membre, son numéro, son statut et un lien vers sa fiche complète.

---

## 16. FAQ et dépannage

### 16.1 Problèmes de connexion courants

**Problème : la page de connexion ne s'affiche pas**

*Symptôme :* Le navigateur affiche « Impossible d'afficher la page » ou « ERR_CONNECTION_REFUSED ».

*Solutions :*
1. Vérifiez que le serveur MICROFINA++ est bien démarré. Contactez votre administrateur système.
2. Vérifiez l'URL saisie dans le navigateur (http://localhost:4200 ou l'adresse IP du serveur).
3. Vérifiez la connexion réseau entre votre poste et le serveur.
4. Vérifiez qu'aucun pare-feu ne bloque le port 4200.

**Problème : message « Identifiant ou mot de passe incorrect »**

*Solutions :*
1. Vérifiez que la touche Verr. Maj. n'est pas activée.
2. Ressaisissez vos identifiants soigneusement.
3. Si le problème persiste après 3 tentatives, contactez votre administrateur pour vérifier votre compte.

**Problème : compte verrouillé**

*Solution :* Contactez votre administrateur système qui pourra déverrouiller votre compte depuis `Administration > Utilisateurs > [votre compte] > Actions > Déverrouiller`.

**Problème : session expirée en cours de saisie**

*Solution :* MICROFINA++ déconnecte automatiquement les sessions inactives après 30 minutes. Si vous êtes redirigé vers la page de connexion en cours de saisie, reconnectez-vous. Les données non enregistrées seront perdues. Pensez à utiliser le bouton **Enregistrer le brouillon** régulièrement lors de la saisie de longs formulaires.

**Problème : la page s'affiche de manière incorrecte (éléments déformés, fonctionnalités manquantes)**

*Solutions :*
1. Videz le cache du navigateur (Ctrl + Maj + Suppr dans Chrome/Firefox).
2. Essayez d'ouvrir l'application en mode navigation privée/incognito.
3. Vérifiez que vous utilisez un navigateur compatible (Chrome 110+, Firefox 110+, Edge 110+).
4. Désactivez les extensions du navigateur susceptibles d'interférer.

### 16.2 Erreurs fréquentes et solutions

**Erreur : « Vous n'avez pas les droits nécessaires pour effectuer cette action »**

*Cause :* Votre rôle ne dispose pas du privilège requis pour l'action que vous tentez d'effectuer.  
*Solution :* Contactez votre administrateur système pour qu'il vérifie et ajuste vos droits si nécessaire.

**Erreur : « Ce membre a déjà un crédit actif »**

*Cause :* La politique de crédit configurée ne permet pas d'accorder un nouveau crédit à un membre ayant déjà un crédit en cours.  
*Solution :* Attendez que le crédit en cours soit soldé, ou obtenez une dérogation de la direction pour accorder un crédit supplémentaire (fonctionnalité disponible selon configuration).

**Erreur : « Montant hors limites du produit »**

*Cause :* Le montant saisi est inférieur au montant minimum ou supérieur au montant maximum défini dans le produit de crédit sélectionné.  
*Solution :* Vérifiez les plafonds du produit (`Menu > Produits > Référentiel crédits`) et ajustez le montant en conséquence, ou sélectionnez un produit adapté.

**Erreur lors de l'upload d'un document : « Format de fichier non supporté »**

*Cause :* Le fichier que vous tentez de téléverser n'est pas dans un format accepté (PDF, JPG ou PNG uniquement).  
*Solution :* Convertissez le document au format PDF et réessayez. Des outils gratuits de conversion sont disponibles en ligne (PDF24, ILovePDF, etc.).

**Erreur : « Impossible de se connecter au service Bankily »**

*Cause :* Le service Bankily est temporairement indisponible, ou les paramètres d'API ne sont pas correctement configurés.  
*Solution :*
1. Vérifiez la connexion Internet du serveur.
2. Consultez l'état du service Bankily sur le portail partenaire.
3. Vérifiez les paramètres API Bankily dans `Administration > Paramètres > Bankily`.
4. Contactez le support Bankily si le problème persiste.

**Erreur lors de la transmission du RMA : « Connexion au serveur BCM impossible »**

*Cause :* Le serveur central de la BCM est temporairement indisponible, ou les paramètres de connexion ne sont pas corrects.  
*Solution :*
1. Vérifiez la connexion Internet du serveur.
2. Consultez les paramètres de connexion BCM dans `Administration > Paramètres > Connexion BCM`.
3. Contactez le support BCM (voir Annexe B).

**Erreur : « La balance n'est pas équilibrée »**

*Cause :* Une ou plusieurs écritures comptables ont été saisies avec un déséquilibre (total débit ≠ total crédit).  
*Solution :* Accédez au journal comptable de la période concernée, identifiez les écritures déséquilibrées (elles sont signalées par une icône d'alerte rouge) et corrigez-les.

**Problème : un export Excel génère un fichier corrompu ou vide**

*Solutions :*
1. Vérifiez que les critères de filtre ne donnent pas un résultat vide (aucune donnée pour la période sélectionnée).
2. Réduisez la période ou les filtres pour diminuer le volume de données.
3. Contactez le support technique si le problème persiste avec des paramètres valides.

**Problème : les performances de l'application sont lentes**

*Causes possibles :* Surcharge du serveur, connexion réseau lente, base de données non optimisée.  
*Solutions :*
1. Vérifiez le monitoring système (`Administration > Monitoring`) pour identifier la ressource saturée.
2. Évitez de lancer des exports volumineux aux heures de pointe.
3. Contactez le support technique pour une analyse des performances et une éventuelle optimisation de la base de données.

---

## Annexe A : Glossaire

| Terme | Définition |
|---|---|
| **Adhésion** | Processus par lequel une personne physique ou morale devient membre d'une IMF |
| **Amortissement** | Remboursement progressif du capital d'un crédit selon un échéancier défini |
| **Arriéré** | Montant dû mais non payé à la date d'échéance convenue |
| **BCM** | Banque Centrale de Mauritanie — autorité de tutelle et de supervision du secteur financier mauritanien |
| **Bilan** | Document comptable présentant l'actif et le passif d'une institution à une date donnée |
| **Caution solidaire** | Garantie par laquelle un tiers s'engage à rembourser le crédit si le débiteur principal est défaillant |
| **CNI** | Carte Nationale d'Identité |
| **Crédit en souffrance** | Crédit dont une ou plusieurs échéances sont impayées depuis plus de 90 jours |
| **Déblocage** | Opération de décaissement des fonds d'un crédit accordé au bénéficiaire |
| **Dégressif** | Méthode de calcul des intérêts sur le capital restant dû (intérêts diminuent au fil des remboursements) |
| **Encours** | Montant total du capital restant dû sur l'ensemble du portefeuille de crédits à un instant donné |
| **Épargne à terme** | Compte d'épargne dont les fonds sont bloqués pour une durée déterminée moyennant un taux d'intérêt supérieur |
| **GIE** | Groupement d'Intérêt Économique — forme juridique fréquente pour les groupements de microentrepreneurs |
| **Grand livre** | Document comptable regroupant l'ensemble des mouvements de chaque compte sur une période |
| **IMF** | Institution de Microfinance — établissement financier agréé par la BCM pour exercer des activités de microfinance |
| **In fine** | Mode de remboursement dans lequel le capital est remboursé en totalité à la dernière échéance |
| **Journal d'audit** | Registre informatique enregistrant toutes les actions des utilisateurs dans le système |
| **Lettrage** | Procédure comptable consistant à rapprocher des écritures débitrices et créditrices d'un même compte |
| **Linéaire** | Méthode de calcul des intérêts sur le capital initial (intérêts constants) |
| **Moughataa** | Subdivision administrative mauritanienne de niveau 2 (équivalent à un département) |
| **MRU** | Ouguiya mauritanienne — devise officielle de la République Islamique de Mauritanie |
| **NIF** | Numéro d'Identification Fiscale |
| **PAR** | Portfolio at Risk (Portefeuille à Risque) — indicateur mesurant la proportion du portefeuille ayant des impayés |
| **Période de grâce** | Période initiale d'un crédit pendant laquelle seuls les intérêts sont dus, sans remboursement en capital |
| **Personne morale (PM)** | Entité juridique (entreprise, association, coopérative) pouvant contracter en son nom propre |
| **Personne physique (PP)** | Individu, être humain identifié comme client de l'IMF |
| **Plan comptable** | Liste normalisée des comptes utilisés pour enregistrer les opérations comptables |
| **Provision** | Charge comptabilisée en anticipation d'une perte probable sur un crédit à risque |
| **RCCM** | Registre du Commerce et du Crédit Mobilier |
| **RIB** | Relevé d'Identité Bancaire |
| **RMA** | Rapport Mensuel d'Activité — document réglementaire transmis mensuellement à la BCM |
| **Sage Compta** | Logiciel de comptabilité développé par Sage, utilisé par certaines IMF pour leur comptabilité générale |
| **SYSCOHADA** | Système Comptable OHADA — référentiel comptable officiel en usage dans les États membres de l'OHADA |
| **Taux d'intérêt nominal** | Taux d'intérêt exprimé sur une base annuelle, avant prise en compte des frais annexes |
| **Taux effectif global (TEG)** | Taux reflétant le coût total du crédit (intérêts + frais + assurance) |
| **Virement** | Transfert de fonds d'un compte bancaire à un autre par voie scripturale |
| **Wallet Bankily** | Porte-monnaie électronique de la plateforme de monnaie mobile Bankily (Mauritel) |
| **Wilaya** | Subdivision administrative mauritanienne de niveau 1 (équivalent à une région) |

---

## Annexe B : Contacts support

### Support technique MICROFINA++

En cas de problème technique (bug, erreur système, panne), contactez le support technique MICROFINA++ :

| Canal | Coordonnées |
|---|---|
| E-mail support | support@microfina.bcm.mr |
| Téléphone (heures ouvrables) | +222 45 25 22 00 |
| Portail de tickets | http://support.microfina.bcm.mr |
| Astreinte (urgences hors heures ouvrables) | +222 36 XX XX XX |

**Heures d'ouverture du support :**
- Dimanche au jeudi : 08h00 – 17h00 (heure de Nouakchott, UTC+0)
- Vendredi et samedi : fermé

**Informations à fournir lors d'une demande de support :**

Pour un traitement rapide de votre demande, préparez les informations suivantes :
1. Nom de votre institution (IMF).
2. Version de MICROFINA++ (visible dans `Menu > À propos`).
3. Nom et prénom de l'utilisateur concerné (login).
4. Description détaillée du problème (quelle action, quel écran, quel message d'erreur).
5. Capture d'écran du message d'erreur si possible.
6. Heure et date à laquelle le problème s'est produit.

### Direction de la Supervision de la Microfinance — BCM

Pour les questions réglementaires, le reporting BCM et les demandes d'accès au système central :

| Contact | Coordonnées |
|---|---|
| Direction Supervision Microfinance | supervision.mf@bcm.mr |
| Adresse | Avenue de l'Indépendance, Nouakchott, Mauritanie |
| Téléphone | +222 45 22 62 06 |
| Site web BCM | https://www.bcm.mr |

### Contacts d'urgence pour les opérations Bankily

En cas d'anomalie sur les transactions Bankily (fonds débités mais non crédités, transactions bloquées) :

| Contact | Coordonnées |
|---|---|
| Support Bankily Partenaires | bankily.partners@mauritel.mr |
| Téléphone | +222 22 XX XX XX |

> **NOTE :** Conservez toujours les références de transaction (numéro de transaction Bankily) et les horodatages lors de tout signalement d'anomalie sur les opérations Bankily.

---

*Fin du document — MANUEL UTILISATEUR MICROFINA++ v12.0.0*

*© Banque Centrale de Mauritanie — Tous droits réservés*  
*Document confidentiel — Usage interne aux IMF agréées BCM*  
*Toute reproduction partielle ou totale sans autorisation écrite de la BCM est interdite.*

---
