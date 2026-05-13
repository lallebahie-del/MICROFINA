-- P10-410 — FERME / BLOQUE sont NVARCHAR ('N'/'O'), pas des entiers.
-- Appliqué au démarrage (SQL Server) en complément de Liquibase pour bases déjà migrées.

CREATE OR ALTER VIEW dbo.vue_indicateurs_performance AS
SELECT
    ag.CODE_AGENCE,
    ag.NOMAGENCE                                    AS nom_agence,
    COALESCE(crd.nb_credits_total,      0)         AS nb_credits_total,
    COALESCE(crd.nb_credits_actifs,     0)         AS nb_credits_actifs,
    COALESCE(crd.nb_credits_soldes,     0)         AS nb_credits_soldes,
    COALESCE(crd.montant_encours,       0)         AS montant_encours,
    COALESCE(crd.montant_debloque_total,0)         AS montant_debloque_total,
    COALESCE(ret.nb_credits_retard,     0)         AS nb_credits_retard,
    CASE
        WHEN COALESCE(crd.nb_credits_actifs, 0) > 0
        THEN CAST(COALESCE(ret.nb_credits_retard, 0) AS FLOAT)
             / CAST(crd.nb_credits_actifs AS FLOAT)
        ELSE 0
    END                                             AS taux_credits_retard,
    COALESCE(ret.nb_echeances_en_retard,0)         AS nb_echeances_en_retard,
    COALESCE(ret.montant_arrieres,      0)         AS montant_arrieres,
    COALESCE(remb.montant_rembourse_total,  0)     AS montant_rembourse_total,
    COALESCE(remb.montant_interet_percu,    0)     AS montant_interet_percu,
    COALESCE(remb.montant_commission_percu, 0)     AS montant_commission_percu,
    COALESCE(remb.nb_reglements,            0)     AS nb_reglements,
    COALESCE(amort.total_echu,              0)     AS total_echu,
    CASE
        WHEN COALESCE(amort.total_echu, 0) > 0
        THEN CAST(COALESCE(remb.montant_rembourse_total, 0) AS FLOAT)
             / CAST(amort.total_echu AS FLOAT)
        ELSE 0
    END                                             AS taux_remboursement_global,
    COALESCE(eps.nb_comptes_actifs,     0)         AS nb_comptes_epargne_actifs,
    COALESCE(eps.total_epargne,         0)         AS total_epargne,
    COALESCE(eps.total_epargne_bloquee, 0)         AS total_epargne_bloquee,
    COALESCE(mbr.nb_membres_actifs,     0)         AS nb_membres_actifs,
    COALESCE(empr.nb_membres_emprunteurs,0)        AS nb_membres_emprunteurs
FROM AGENCE ag
LEFT JOIN (
    SELECT
        c.agence,
        COUNT(*)                                    AS nb_credits_total,
        SUM(CASE WHEN c.STATUT = 'DEBLOQUE' THEN 1 ELSE 0 END) AS nb_credits_actifs,
        SUM(CASE WHEN c.STATUT = 'SOLDE'    THEN 1 ELSE 0 END) AS nb_credits_soldes,
        SUM(CASE WHEN c.STATUT = 'DEBLOQUE' THEN c.SOLDE_CAPITAL ELSE 0 END) AS montant_encours,
        SUM(COALESCE(c.MONTANT_DEBLOQUER, 0))      AS montant_debloque_total
    FROM Credits c
    GROUP BY c.agence
) crd ON crd.agence = ag.CODE_AGENCE
LEFT JOIN (
    SELECT
        c2.agence,
        COUNT(DISTINCT a.idcredit)                 AS nb_credits_retard,
        COUNT(*)                                    AS nb_echeances_en_retard,
        SUM(a.CAPITAL    - a.CAPITAL_REMBOURSE
          + a.INTERET    - a.INTERET_REMBOURSE
          + a.PENALITE   - a.PENALITE_REGLEE)      AS montant_arrieres
    FROM Amortp a
    JOIN Credits c2 ON c2.IDCREDIT = a.idcredit
    WHERE a.STATUT_ECHEANCE IN ('EN_RETARD','PARTIELLEMENT_REGLE')
    GROUP BY c2.agence
) ret ON ret.agence = ag.CODE_AGENCE
LEFT JOIN (
    SELECT
        r.agence,
        SUM(r.MONTANT_TOTAL)                       AS montant_rembourse_total,
        SUM(r.MONTANT_INTERET)                     AS montant_interet_percu,
        SUM(r.MONTANT_COMMISSION)                  AS montant_commission_percu,
        COUNT(r.IDREGLEMENT)                       AS nb_reglements
    FROM Reglement r
    WHERE r.STATUT = 'VALIDE'
    GROUP BY r.agence
) remb ON remb.agence = ag.CODE_AGENCE
LEFT JOIN (
    SELECT
        c3.agence,
        SUM(a2.TOTAL_ECHEANCE)                     AS total_echu
    FROM Amortp a2
    JOIN Credits c3 ON c3.IDCREDIT = a2.idcredit
    WHERE a2.STATUT_ECHEANCE IN ('EN_RETARD','PARTIELLEMENT_REGLE','REGLE')
    GROUP BY c3.agence
) amort ON amort.agence = ag.CODE_AGENCE
LEFT JOIN (
    SELECT
        m2.agence                                  AS agence,
        COUNT(*)                                   AS nb_comptes_actifs,
        SUM(ce.MONTANTDEPOT)                       AS total_epargne,
        SUM(ce.MONTANTBLOQUE)                      AS total_epargne_bloquee
    FROM COMPTEEPS ce
    JOIN membres m2 ON m2.NUM_MEMBRE = ce.code_membre
    WHERE (ce.FERME IS NULL OR UPPER(LTRIM(RTRIM(ce.FERME))) <> N'O')
      AND (ce.BLOQUE IS NULL OR UPPER(LTRIM(RTRIM(ce.BLOQUE))) <> N'O')
    GROUP BY m2.agence
) eps ON eps.agence = ag.CODE_AGENCE
LEFT JOIN (
    SELECT
        m3.agence                                  AS agence,
        COUNT(*)                                   AS nb_membres_actifs
    FROM membres m3
    WHERE m3.STATUT = 'ACTIF'
    GROUP BY m3.agence
) mbr ON mbr.agence = ag.CODE_AGENCE
LEFT JOIN (
    SELECT
        c4.agence,
        COUNT(DISTINCT c4.nummembre)               AS nb_membres_emprunteurs
    FROM Credits c4
    WHERE c4.STATUT = 'DEBLOQUE'
    GROUP BY c4.agence
) empr ON empr.agence = ag.CODE_AGENCE
WHERE ag.ACTIF = 1;

CREATE OR ALTER VIEW dbo.vue_liste_clients AS
SELECT
    m.NUM_MEMBRE,
    m.NOM,
    m.PRENOM,
    m.SEXE,
    m.DATENAISSANCE,
    m.STATUT                                        AS statut_membre,
    m.ETAT                                          AS etat_membre,
    m.DATECREATIONUSER                              AS date_adhesion,
    m.agence                                        AS code_agence,
    ag.NOMAGENCE                                    AS nom_agence,
    COALESCE(eps.nb_comptes_epargne,    0)          AS nb_comptes_epargne,
    COALESCE(eps.total_epargne,         0)          AS total_epargne,
    COALESCE(eps.total_epargne_bloquee, 0)          AS total_epargne_bloquee,
    COALESCE(crd.nb_credits_total,      0)          AS nb_credits_total,
    COALESCE(crd.nb_credits_actifs,     0)          AS nb_credits_actifs,
    COALESCE(crd.montant_total_accorde, 0)          AS montant_total_accorde,
    COALESCE(crd.encours_capital,       0)          AS encours_capital,
    COALESCE(ret.nb_credits_retard,     0)          AS nb_credits_retard,
    CASE
        WHEN COALESCE(par.max_jours_retard, 0) > 180 THEN 'PAR180_PLUS'
        WHEN COALESCE(par.max_jours_retard, 0) > 90  THEN 'PAR180'
        WHEN COALESCE(par.max_jours_retard, 0) > 30  THEN 'PAR90'
        WHEN COALESCE(par.max_jours_retard, 0) > 0   THEN 'PAR30'
        ELSE 'SAIN'
    END                                             AS categorie_par_pire,
    COALESCE(par.max_jours_retard,      0)          AS max_jours_retard,
    COALESCE(par.total_arrieres,        0)          AS total_arrieres,
    COALESCE(gar.nb_garanties,          0)          AS nb_garanties,
    COALESCE(gar.total_garanties,       0)          AS total_garanties
FROM membres m
LEFT JOIN AGENCE ag ON ag.CODE_AGENCE = m.agence
LEFT JOIN (
    SELECT
        ce.code_membre,
        COUNT(*)                        AS nb_comptes_epargne,
        SUM(CASE WHEN (ce.FERME IS NULL OR UPPER(LTRIM(RTRIM(ce.FERME))) <> N'O')
                   AND (ce.BLOQUE IS NULL OR UPPER(LTRIM(RTRIM(ce.BLOQUE))) <> N'O')
                 THEN ce.MONTANTDEPOT  ELSE 0 END)  AS total_epargne,
        SUM(CASE WHEN (ce.FERME IS NULL OR UPPER(LTRIM(RTRIM(ce.FERME))) <> N'O')
                   AND (ce.BLOQUE IS NULL OR UPPER(LTRIM(RTRIM(ce.BLOQUE))) <> N'O')
                 THEN ce.MONTANTBLOQUE ELSE 0 END)  AS total_epargne_bloquee
    FROM COMPTEEPS ce
    GROUP BY ce.code_membre
) eps ON eps.code_membre = m.NUM_MEMBRE
LEFT JOIN (
    SELECT
        c.nummembre,
        COUNT(*)                                    AS nb_credits_total,
        SUM(CASE WHEN c.STATUT = 'DEBLOQUE' THEN 1 ELSE 0 END) AS nb_credits_actifs,
        SUM(COALESCE(c.MONTANT_ACCORDE, 0))        AS montant_total_accorde,
        SUM(CASE WHEN c.STATUT = 'DEBLOQUE' THEN c.SOLDE_CAPITAL ELSE 0 END) AS encours_capital
    FROM Credits c
    GROUP BY c.nummembre
) crd ON crd.nummembre = m.NUM_MEMBRE
LEFT JOIN (
    SELECT
        c2.nummembre,
        COUNT(DISTINCT a.idcredit)                 AS nb_credits_retard
    FROM Amortp a
    JOIN Credits c2 ON c2.IDCREDIT = a.idcredit
    WHERE a.STATUT_ECHEANCE IN ('EN_RETARD','PARTIELLEMENT_REGLE')
    GROUP BY c2.nummembre
) ret ON ret.nummembre = m.NUM_MEMBRE
LEFT JOIN (
    SELECT
        p.NUM_MEMBRE,
        MAX(p.max_jours_retard)                    AS max_jours_retard,
        SUM(p.total_arrieres)                      AS total_arrieres
    FROM vue_par_bcm p
    GROUP BY p.NUM_MEMBRE
) par ON par.NUM_MEMBRE = m.NUM_MEMBRE
LEFT JOIN (
    SELECT
        g.num_membre,
        COUNT(*)                                   AS nb_garanties,
        SUM(g.valeur_estimee)                      AS total_garanties
    FROM vue_garanties_unifiee g
    WHERE COALESCE(g.statut, 'ACTIF') = 'ACTIF'
      AND g.num_membre IS NOT NULL
    GROUP BY g.num_membre
) gar ON gar.num_membre = m.NUM_MEMBRE;
