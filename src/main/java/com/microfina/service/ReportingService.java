package com.microfina.service;

import com.microfina.entity.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * ReportingService – génération des états financiers et rapports BCM.
 *
 * ══════════════════════════════════════════════════════════════════════
 * Architecture d'export
 * ══════════════════════════════════════════════════════════════════════
 * Chaque méthode d'export retourne un tableau d'octets (byte[]) prêt
 * à être servi via un endpoint HTTP (Content-Disposition: attachment).
 *
 * Les implémentations actuelles produisent du CSV UTF-8 simulant le
 * format des rapports. En Phase 6, ces méthodes seront remplacées par
 * des générateurs Apache POI (Excel) et iText / OpenPDF (PDF).
 *
 * ══════════════════════════════════════════════════════════════════════
 * Taxes incluses (P4-013)
 * ══════════════════════════════════════════════════════════════════════
 * Toutes les méthodes de synthèse incluent MONTANT_TAXE / TAXE dans le
 * calcul des revenus totaux, conformément à la réglementation fiscale
 * mauritanienne et aux exigences BCM.
 *
 * ══════════════════════════════════════════════════════════════════════
 * Rapports disponibles
 * ══════════════════════════════════════════════════════════════════════
 *
 *  exporterTableauAmortissement   – Échéancier détaillé d'un crédit
 *  exporterEtatBCM                – Portefeuille à risque (PAR) BCM
 *  exporterGrandLivre             – Grand Livre comptable par période
 *  exporterPortefeuilleCredits    – Synthèse encours + arriérés
 *  exporterRevenusJournaliers     – Flux revenus journaliers par agence
 */
@Service
@Transactional(readOnly = true)
public class ReportingService {

    // Séparateur CSV et format date
    private static final String SEP  = ";";
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @PersistenceContext
    private EntityManager em;

    // ══════════════════════════════════════════════════════════════
    // 1. Tableau d'amortissement d'un crédit
    // ══════════════════════════════════════════════════════════════

    /**
     * Exporte le tableau d'amortissement complet ({@link Amortp}) d'un crédit.
     *
     * Colonnes exportées (ordre BCM) :
     *   N° Échéance, Date Échéance, Capital Dû, Intérêt Dû,
     *   Assurance, Commission, Taxe, Total Échéance,
     *   Capital Remboursé, Intérêt Remboursé, Pénalités,
     *   Taxe Réglée, Solde Capital, Statut
     *
     * @param idCredit identifiant du crédit ({@link Credits#getIdCredit()})
     * @return octets CSV UTF-8 ; tableau vide si aucune ligne trouvée
     */
    public byte[] exporterTableauAmortissement(Long idCredit) {
        List<Amortp> lignes = em.createQuery(
                "SELECT a FROM Amortp a WHERE a.credit.idCredit = :id " +
                "ORDER BY a.numEcheance",
                Amortp.class)
            .setParameter("id", idCredit)
            .getResultList();

        StringBuilder sb = new StringBuilder();

        // En-tête
        sb.append("N_ECHEANCE").append(SEP)
          .append("DATE_ECHEANCE").append(SEP)
          .append("CAPITAL").append(SEP)
          .append("INTERET").append(SEP)
          .append("ASSURANCE").append(SEP)
          .append("COMMISSION").append(SEP)
          .append("TAXE").append(SEP)
          .append("TOTAL_ECHEANCE").append(SEP)
          .append("CAPITAL_REMBOURSE").append(SEP)
          .append("INTERET_REMBOURSE").append(SEP)
          .append("PENALITE_REGLEE").append(SEP)
          .append("TAXE_REGLEE").append(SEP)
          .append("SOLDE_CAPITAL").append(SEP)
          .append("STATUT_ECHEANCE")
          .append("\n");

        for (Amortp a : lignes) {
            sb.append(a.getNumEcheance()).append(SEP)
              .append(formatDate(a.getDateEcheance())).append(SEP)
              .append(a.getCapital()).append(SEP)
              .append(a.getInteret()).append(SEP)
              .append(a.getAssurance()).append(SEP)
              .append(a.getCommission()).append(SEP)
              .append(a.getTaxe()).append(SEP)
              .append(a.getTotalEcheance()).append(SEP)
              .append(a.getCapitalRembourse()).append(SEP)
              .append(a.getInteretRembourse()).append(SEP)
              .append(a.getPenaliteReglee()).append(SEP)
              .append(a.getTaxeReglee()).append(SEP)
              .append(a.getSoldeCapital()).append(SEP)
              .append(a.getStatutEcheance())
              .append("\n");
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    // ══════════════════════════════════════════════════════════════
    // 2. État BCM – Portefeuille à risque (PAR)
    // ══════════════════════════════════════════════════════════════

    /**
     * Exporte l'état BCM du portefeuille à risque pour une date donnée.
     *
     * La requête lit directement la vue {@code vue_par_bcm} via JPQL natif
     * afin d'éviter la duplication de la logique d'agrégation PAR.
     *
     * Colonnes exportées (format État BCM Mauritanie) :
     *   NUMCREDIT, NOM_MEMBRE, PRENOM_MEMBRE, SEXE,
     *   CODE_AGENCE, DATE_DEBLOCAGE, DATE_ECHEANCE_FINALE,
     *   ENCOURS_BRUT, SOLDE_CAPITAL,
     *   NB_ECHEANCES_RETARD, CAPITAL_RETARD, INTERET_RETARD,
     *   PENALITE_RETARD, TAXE_RETARD, TOTAL_ARRIERES,
     *   MAX_JOURS_RETARD, CATEGORIE_PAR
     *
     * @param dateRapport date de référence pour le rapport (en général la date du jour)
     * @return octets CSV UTF-8
     */
    public byte[] exporterEtatBCM(LocalDate dateRapport) {
        // Lecture native de la vue via SQL brut
        // (vue_par_bcm = résultat de la jointure Credits/Membres/Amortp)
        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery(
                "SELECT " +
                "  NUMCREDIT, nom_membre, prenom_membre, SEXE, " +
                "  CODE_AGENCE, DATE_DEBLOCAGE, date_echeance_finale, " +
                "  encours_brut, SOLDE_CAPITAL, " +
                "  nb_echeances_retard, capital_retard, interet_retard, " +
                "  penalite_retard, taxe_retard, total_arrieres, " +
                "  max_jours_retard, categorie_par " +
                "FROM vue_par_bcm " +
                "ORDER BY categorie_par DESC, max_jours_retard DESC")
            .getResultList();

        StringBuilder sb = new StringBuilder();

        // En-tête (colonnes BCM officielles)
        sb.append("NUMCREDIT").append(SEP)
          .append("NOM_MEMBRE").append(SEP)
          .append("PRENOM_MEMBRE").append(SEP)
          .append("SEXE").append(SEP)
          .append("CODE_AGENCE").append(SEP)
          .append("DATE_DEBLOCAGE").append(SEP)
          .append("DATE_ECHEANCE_FINALE").append(SEP)
          .append("ENCOURS_BRUT").append(SEP)
          .append("SOLDE_CAPITAL").append(SEP)
          .append("NB_ECHEANCES_RETARD").append(SEP)
          .append("CAPITAL_RETARD").append(SEP)
          .append("INTERET_RETARD").append(SEP)
          .append("PENALITE_RETARD").append(SEP)
          .append("TAXE_RETARD").append(SEP)
          .append("TOTAL_ARRIERES").append(SEP)
          .append("MAX_JOURS_RETARD").append(SEP)
          .append("CATEGORIE_PAR")
          .append("\n");

        for (Object[] r : rows) {
            for (int i = 0; i < r.length; i++) {
                if (i > 0) sb.append(SEP);
                sb.append(r[i] == null ? "" : r[i].toString());
            }
            sb.append("\n");
        }

        // Métadonnées de clôture du rapport
        sb.append("\n# État BCM généré le : ").append(dateRapport.format(DATE_FMT))
          .append(" – MICROFINA++\n");

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    // ══════════════════════════════════════════════════════════════
    // 3. Grand Livre comptable
    // ══════════════════════════════════════════════════════════════

    /**
     * Exporte le Grand Livre (vue_grand_livre) sur une plage de dates.
     *
     * Colonnes exportées :
     *   IDCOMPTABILITE, DATEOPERATION, NUMPIECE, LIBELLE,
     *   DEBIT, CREDIT, NUM_COMPTE, COMPTE_AUXILIAIRE,
     *   SENS, REF_CREDIT_COMPTABLE, AGENCE_ECRITURE,
     *   IDREGLEMENT, DATE_REGLEMENT, MONTANT_TOTAL,
     *   MONTANT_CAPITAL, MONTANT_INTERET, MONTANT_COMMISSION,
     *   MONTANT_TAXE, MODE_PAIEMENT, NUM_CREDIT, TOTAL_REVENUS_ECRITURE
     *
     * @param debut date de début (inclusive) de la plage
     * @param fin   date de fin   (inclusive) de la plage
     * @return octets CSV UTF-8
     */
    public byte[] exporterGrandLivre(LocalDate debut, LocalDate fin) {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery(
                "SELECT " +
                "  IDCOMPTABILITE, DATEOPERATION, num_piece_comptable, LIBELLE, " +
                "  DEBIT, CREDIT, num_compte, compte_auxiliaire, " +
                "  sens, ref_credit_comptable, agence_ecriture, " +
                "  IDREGLEMENT, DATE_REGLEMENT, MONTANT_TOTAL, " +
                "  MONTANT_CAPITAL, MONTANT_INTERET, MONTANT_COMMISSION, " +
                "  MONTANT_TAXE, MODE_PAIEMENT, num_credit, total_revenus_ecriture " +
                "FROM vue_grand_livre " +
                "WHERE DATEOPERATION BETWEEN :debut AND :fin " +
                "ORDER BY DATEOPERATION, IDCOMPTABILITE")
            .setParameter("debut", debut)
            .setParameter("fin",   fin)
            .getResultList();

        StringBuilder sb = new StringBuilder();

        sb.append("IDCOMPTABILITE").append(SEP)
          .append("DATE_OPERATION").append(SEP)
          .append("NUM_PIECE").append(SEP)
          .append("LIBELLE").append(SEP)
          .append("DEBIT").append(SEP)
          .append("CREDIT").append(SEP)
          .append("NUM_COMPTE").append(SEP)
          .append("COMPTE_AUXILIAIRE").append(SEP)
          .append("SENS").append(SEP)
          .append("REF_CREDIT").append(SEP)
          .append("AGENCE").append(SEP)
          .append("IDREGLEMENT").append(SEP)
          .append("DATE_REGLEMENT").append(SEP)
          .append("MONTANT_TOTAL").append(SEP)
          .append("MONTANT_CAPITAL").append(SEP)
          .append("MONTANT_INTERET").append(SEP)
          .append("MONTANT_COMMISSION").append(SEP)
          .append("MONTANT_TAXE").append(SEP)
          .append("MODE_PAIEMENT").append(SEP)
          .append("NUM_CREDIT").append(SEP)
          .append("TOTAL_REVENUS")
          .append("\n");

        for (Object[] r : rows) {
            for (int i = 0; i < r.length; i++) {
                if (i > 0) sb.append(SEP);
                sb.append(r[i] == null ? "" : r[i].toString());
            }
            sb.append("\n");
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    // ══════════════════════════════════════════════════════════════
    // 4. Synthèse du portefeuille de crédits
    // ══════════════════════════════════════════════════════════════

    /**
     * Exporte la synthèse des encours par agence et par statut crédit.
     *
     * Colonnes : CODE_AGENCE, STATUT_CREDIT, NB_CREDITS,
     *            TOTAL_ENCOURS, TOTAL_SOLDE_CAPITAL,
     *            TOTAL_SOLDE_INTERET, TOTAL_SOLDE_PENALITE
     *
     * @return octets CSV UTF-8
     */
    public byte[] exporterPortefeuilleCredits() {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery(
                "SELECT " +
                "  c.agence              AS CODE_AGENCE, " +
                "  c.STATUT             AS STATUT_CREDIT, " +
                "  COUNT(*)             AS NB_CREDITS, " +
                "  SUM(c.MONTANT_DEBLOQUER) AS TOTAL_ENCOURS, " +
                "  SUM(c.SOLDE_CAPITAL)  AS TOTAL_SOLDE_CAPITAL, " +
                "  SUM(c.SOLDE_INTERET)  AS TOTAL_SOLDE_INTERET, " +
                "  SUM(c.SOLDE_PENALITE) AS TOTAL_SOLDE_PENALITE " +
                "FROM Credits c " +
                "GROUP BY c.agence, c.STATUT " +
                "ORDER BY c.agence, c.STATUT")
            .getResultList();

        StringBuilder sb = new StringBuilder();

        sb.append("CODE_AGENCE").append(SEP)
          .append("STATUT_CREDIT").append(SEP)
          .append("NB_CREDITS").append(SEP)
          .append("TOTAL_ENCOURS").append(SEP)
          .append("TOTAL_SOLDE_CAPITAL").append(SEP)
          .append("TOTAL_SOLDE_INTERET").append(SEP)
          .append("TOTAL_SOLDE_PENALITE")
          .append("\n");

        BigDecimal grandTotalEncours = BigDecimal.ZERO;
        for (Object[] r : rows) {
            for (int i = 0; i < r.length; i++) {
                if (i > 0) sb.append(SEP);
                sb.append(r[i] == null ? "" : r[i].toString());
            }
            sb.append("\n");
            if (r[3] != null) {
                grandTotalEncours = grandTotalEncours.add(
                        new BigDecimal(r[3].toString()));
            }
        }

        sb.append("\n# TOTAL ENCOURS BRUT : ").append(grandTotalEncours).append("\n");

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    // ══════════════════════════════════════════════════════════════
    // 5. Revenus journaliers (intérêts + commissions + taxes)
    // ══════════════════════════════════════════════════════════════

    /**
     * Exporte les revenus journaliers d'une plage de dates,
     * ventilés par agence et par type de revenu.
     *
     * Inclut MONTANT_TAXE conformément à P4-013.
     *
     * Colonnes : DATE_REGLEMENT, CODE_AGENCE,
     *            NB_REGLEMENTS, TOTAL_CAPITAL,
     *            TOTAL_INTERET, TOTAL_COMMISSION,
     *            TOTAL_TAXE, TOTAL_PENALITE, TOTAL_ASSURANCE,
     *            TOTAL_REVENUS (= intérêt + commission + taxe)
     *
     * @param debut date de début (inclusive)
     * @param fin   date de fin   (inclusive)
     * @return octets CSV UTF-8
     */
    public byte[] exporterRevenusJournaliers(LocalDate debut, LocalDate fin) {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery(
                "SELECT " +
                "  r.DATE_REGLEMENT, " +
                "  r.agence                      AS CODE_AGENCE, " +
                "  COUNT(*)                      AS NB_REGLEMENTS, " +
                "  SUM(r.MONTANT_CAPITAL)        AS TOTAL_CAPITAL, " +
                "  SUM(r.MONTANT_INTERET)        AS TOTAL_INTERET, " +
                "  SUM(r.MONTANT_COMMISSION)     AS TOTAL_COMMISSION, " +
                "  SUM(r.MONTANT_TAXE)           AS TOTAL_TAXE, " +
                "  SUM(r.MONTANT_PENALITE)       AS TOTAL_PENALITE, " +
                "  SUM(r.MONTANT_ASSURANCE)      AS TOTAL_ASSURANCE, " +
                "  SUM(r.MONTANT_INTERET " +
                "      + r.MONTANT_COMMISSION " +
                "      + r.MONTANT_TAXE)         AS TOTAL_REVENUS " +
                "FROM Reglement r " +
                "WHERE r.STATUT = 'VALIDE' " +
                "  AND r.DATE_REGLEMENT BETWEEN :debut AND :fin " +
                "GROUP BY r.DATE_REGLEMENT, r.agence " +
                "ORDER BY r.DATE_REGLEMENT, r.agence")
            .setParameter("debut", debut)
            .setParameter("fin",   fin)
            .getResultList();

        StringBuilder sb = new StringBuilder();

        sb.append("DATE_REGLEMENT").append(SEP)
          .append("CODE_AGENCE").append(SEP)
          .append("NB_REGLEMENTS").append(SEP)
          .append("TOTAL_CAPITAL").append(SEP)
          .append("TOTAL_INTERET").append(SEP)
          .append("TOTAL_COMMISSION").append(SEP)
          .append("TOTAL_TAXE").append(SEP)
          .append("TOTAL_PENALITE").append(SEP)
          .append("TOTAL_ASSURANCE").append(SEP)
          .append("TOTAL_REVENUS")
          .append("\n");

        for (Object[] r : rows) {
            for (int i = 0; i < r.length; i++) {
                if (i > 0) sb.append(SEP);
                sb.append(r[i] == null ? "" : r[i].toString());
            }
            sb.append("\n");
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    // ══════════════════════════════════════════════════════════════
    // Utilitaires internes
    // ══════════════════════════════════════════════════════════════

    private String formatDate(java.time.LocalDate d) {
        return d == null ? "" : d.format(DATE_FMT);
    }
}
