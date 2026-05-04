package com.microfina.service;

import com.microfina.entity.Agence;
import com.microfina.entity.ClotureJournaliere;
import com.microfina.entity.Reglement;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ClotureService – gestion de la clôture de journée comptable.
 *
 * ══════════════════════════════════════════════════════════════════════
 * Workflow de clôture
 * ══════════════════════════════════════════════════════════════════════
 *
 *  1. {@link #verifierJournee} – contrôles d'intégrité préalables :
 *       a. Aucune clôture n'existe déjà pour (date, agence).
 *       b. Chaque {@link Reglement} VALIDE de la journée a un
 *          {@code idComptabilite} non nul et référence une écriture
 *          existante dans la table {@code comptabilite}.
 *       c. Aucun Reglement ne se trouve en statut EN_ATTENTE
 *          (= décaissement non encore comptabilisé).
 *
 *  2. {@link #cloturerJournee} – enregistrement de la clôture :
 *       a. Agrège les totaux (capital, intérêts, commissions, taxes…)
 *          depuis les Reglements VALIDE de la journée.
 *       b. Crée et persiste un enregistrement {@link ClotureJournaliere}.
 *       c. Tout échec lève une {@link ClotureException} ; la transaction
 *          est automatiquement rollbackée (@Transactional).
 *
 *  3. {@link #reouvrir} – réouverture exceptionnelle (superviseur) :
 *       Passe le statut de la clôture existante à REOUVERT.
 *       Nécessite que la journée suivante ne soit pas encore clôturée.
 *
 * ══════════════════════════════════════════════════════════════════════
 * Règle mauritanienne (Banque Centrale de Mauritanie)
 * ══════════════════════════════════════════════════════════════════════
 * Tout Reglement VALIDE DOIT avoir une écriture comptabilite associée
 * avant la clôture (P4-013 : idcomptabilite NOT NULL). La présente
 * vérification ajoute un contrôle applicatif en plus de la contrainte DDL.
 */
@Service("metierClotureService")
public class ClotureService {

    @PersistenceContext
    private EntityManager em;

    // ══════════════════════════════════════════════════════════════
    // Verification – contrôles pré-clôture (lecture seule)
    // ══════════════════════════════════════════════════════════════

    /**
     * Vérifie l'intégrité de la journée avant clôture.
     *
     * @param date       journée à contrôler
     * @param codeAgence code de l'agence (null = clôture centrale)
     * @throws ClotureException si un contrôle échoue
     */
    @Transactional(readOnly = true)
    public void verifierJournee(LocalDate date, String codeAgence) {

        // Contrôle 1 – La journée n'est pas déjà clôturée
        verifierNonDejaCloturee(date, codeAgence);

        // Contrôle 2 – Tous les Reglements VALIDE ont une écriture comptable
        verifierComptabilisationComplete(date, codeAgence);

        // Contrôle 3 – Aucun Reglement EN_ATTENTE ne subsiste
        verifierAucunReglementEnAttente(date, codeAgence);
    }

    private void verifierNonDejaCloturee(LocalDate date, String codeAgence) {
        String jpql = codeAgence == null
            ? "SELECT COUNT(c) FROM ClotureJournaliere c " +
              "WHERE c.dateCloture = :date AND c.agence IS NULL AND c.statut = 'CLOTURE'"
            : "SELECT COUNT(c) FROM ClotureJournaliere c " +
              "WHERE c.dateCloture = :date AND c.agence.codeAgence = :agence AND c.statut = 'CLOTURE'";

        var query = em.createQuery(jpql, Long.class).setParameter("date", date);
        if (codeAgence != null) query.setParameter("agence", codeAgence);

        long nb = query.getSingleResult();
        if (nb > 0) {
            throw new ClotureException(
                "La journée " + date + " est déjà clôturée" +
                (codeAgence != null ? " pour l'agence " + codeAgence : "") + ".");
        }
    }

    private void verifierComptabilisationComplete(LocalDate date, String codeAgence) {
        // Cherche les Reglements VALIDE sans écriture comptable associée.
        // Deux cas signalent un problème :
        //   a. idComptabilite NULL (ne devrait pas arriver après P4-013)
        //   b. idComptabilite pointe vers un IDCOMPTABILITE inexistant
        String jpql;
        if (codeAgence == null) {
            jpql = "SELECT r FROM Reglement r " +
                   "WHERE r.dateReglement = :date " +
                   "  AND r.statut = 'VALIDE' " +
                   "  AND (r.idComptabilite IS NULL " +
                   "       OR r.idComptabilite = 0 " +
                   "       OR NOT EXISTS (" +
                   "           SELECT 1 FROM Comptabilite c " +
                   "           WHERE c.idComptabilite = r.idComptabilite))";
        } else {
            jpql = "SELECT r FROM Reglement r " +
                   "WHERE r.dateReglement = :date " +
                   "  AND r.statut = 'VALIDE' " +
                   "  AND r.agence.codeAgence = :agence " +
                   "  AND (r.idComptabilite IS NULL " +
                   "       OR r.idComptabilite = 0 " +
                   "       OR NOT EXISTS (" +
                   "           SELECT 1 FROM Comptabilite c " +
                   "           WHERE c.idComptabilite = r.idComptabilite))";
        }

        var query = em.createQuery(jpql, Reglement.class).setParameter("date", date);
        if (codeAgence != null) query.setParameter("agence", codeAgence);
        List<Reglement> nonComptabilises = query.getResultList();

        if (!nonComptabilises.isEmpty()) {
            String ids = nonComptabilises.stream()
                .map(r -> String.valueOf(r.getIdReglement()))
                .reduce((a, b) -> a + ", " + b)
                .orElse("?");
            throw new ClotureException(
                nonComptabilises.size() + " règlement(s) du " + date +
                " n'ont pas d'écriture comptable associée. " +
                "IDs Reglement : [" + ids + "]. " +
                "Comptabilisez ces règlements avant de clôturer.");
        }
    }

    private void verifierAucunReglementEnAttente(LocalDate date, String codeAgence) {
        String jpql = codeAgence == null
            ? "SELECT COUNT(r) FROM Reglement r " +
              "WHERE r.dateReglement = :date AND r.statut = 'EN_ATTENTE'"
            : "SELECT COUNT(r) FROM Reglement r " +
              "WHERE r.dateReglement = :date " +
              "  AND r.statut = 'EN_ATTENTE' " +
              "  AND r.agence.codeAgence = :agence";

        var query = em.createQuery(jpql, Long.class).setParameter("date", date);
        if (codeAgence != null) query.setParameter("agence", codeAgence);

        long nb = query.getSingleResult();
        if (nb > 0) {
            throw new ClotureException(
                nb + " règlement(s) EN_ATTENTE subsistent pour le " + date +
                ". Validez ou annulez ces règlements avant de clôturer.");
        }
    }

    // ══════════════════════════════════════════════════════════════
    // Clôture – enregistrement de la fermeture de journée
    // ══════════════════════════════════════════════════════════════

    /**
     * Clôture une journée comptable après vérification préalable.
     *
     * Appelle {@link #verifierJournee} puis agrège les Reglements VALIDE
     * pour construire et persister un {@link ClotureJournaliere}.
     *
     * @param date        journée à clôturer
     * @param codeAgence  agence (null = clôture centrale)
     * @param utilisateur login de l'agent déclenchant la clôture
     * @return l'entité {@link ClotureJournaliere} persistée
     * @throws ClotureException si un contrôle d'intégrité échoue
     */
    @Transactional
    public ClotureJournaliere cloturerJournee(LocalDate date,
                                               String codeAgence,
                                               String utilisateur) {
        // Pré-vérification
        verifierJournee(date, codeAgence);

        // Agrégation des Reglements de la journée
        String aggJpql = codeAgence == null
            ? "SELECT " +
              "  COUNT(r),              SUM(r.montantTotal), " +
              "  SUM(r.montantCapital), SUM(r.montantInteret), " +
              "  SUM(r.montantPenalite),SUM(r.montantAssurance), " +
              "  SUM(r.montantCommission), SUM(r.montantTaxe) " +
              "FROM Reglement r " +
              "WHERE r.dateReglement = :date AND r.statut = 'VALIDE'"
            : "SELECT " +
              "  COUNT(r),              SUM(r.montantTotal), " +
              "  SUM(r.montantCapital), SUM(r.montantInteret), " +
              "  SUM(r.montantPenalite),SUM(r.montantAssurance), " +
              "  SUM(r.montantCommission), SUM(r.montantTaxe) " +
              "FROM Reglement r " +
              "WHERE r.dateReglement = :date " +
              "  AND r.statut = 'VALIDE' " +
              "  AND r.agence.codeAgence = :agence";

        var aggQuery = em.createQuery(aggJpql, Object[].class)
                         .setParameter("date", date);
        if (codeAgence != null) aggQuery.setParameter("agence", codeAgence);

        Object[] agg = aggQuery.getSingleResult();

        // Construction de la clôture
        ClotureJournaliere cloture = new ClotureJournaliere();
        cloture.setDateCloture(date);
        cloture.setDateHeureCloture(LocalDateTime.now());
        cloture.setUtilisateur(utilisateur);
        cloture.setStatut("CLOTURE");

        cloture.setNbReglements(toLong(agg[0]).intValue());
        cloture.setMontantTotal(toBd(agg[1]));
        cloture.setMontantCapital(toBd(agg[2]));
        cloture.setMontantInteret(toBd(agg[3]));
        cloture.setMontantPenalite(toBd(agg[4]));
        cloture.setMontantAssurance(toBd(agg[5]));
        cloture.setMontantCommission(toBd(agg[6]));
        cloture.setMontantTaxe(toBd(agg[7]));

        // Lier l'agence si précisée
        if (codeAgence != null) {
            Agence agence = em.find(Agence.class, codeAgence);
            if (agence == null) {
                throw new ClotureException("Agence introuvable : " + codeAgence);
            }
            cloture.setAgence(agence);
        }

        em.persist(cloture);
        return cloture;
    }

    // ══════════════════════════════════════════════════════════════
    // Réouverture exceptionnelle
    // ══════════════════════════════════════════════════════════════

    /**
     * Réouvre exceptionnellement une journée déjà clôturée.
     *
     * Conditions :
     *   – La clôture ciblée existe et est à l'état CLOTURE.
     *   – La journée suivante n'est pas encore clôturée (séquentialité).
     *
     * @param date        journée à réouvrir
     * @param codeAgence  agence (null = clôture centrale)
     * @param motif       motif de réouverture (obligatoire)
     * @param utilisateur superviseur déclenchant la réouverture
     * @throws ClotureException si les conditions ne sont pas remplies
     */
    @Transactional
    public void reouvrir(LocalDate date, String codeAgence,
                         String motif,   String utilisateur) {
        if (motif == null || motif.isBlank()) {
            throw new ClotureException("Un motif de réouverture est obligatoire.");
        }

        // Retrouver la clôture existante
        String jpql = codeAgence == null
            ? "SELECT c FROM ClotureJournaliere c " +
              "WHERE c.dateCloture = :date AND c.agence IS NULL AND c.statut = 'CLOTURE'"
            : "SELECT c FROM ClotureJournaliere c " +
              "WHERE c.dateCloture = :date " +
              "  AND c.agence.codeAgence = :agence AND c.statut = 'CLOTURE'";

        var q = em.createQuery(jpql, ClotureJournaliere.class).setParameter("date", date);
        if (codeAgence != null) q.setParameter("agence", codeAgence);
        List<ClotureJournaliere> clotureList = q.getResultList();

        if (clotureList.isEmpty()) {
            throw new ClotureException(
                "Aucune clôture CLOTURE trouvée pour le " + date +
                (codeAgence != null ? " / agence " + codeAgence : "") + ".");
        }

        // Vérifier que la journée suivante n'est pas encore clôturée
        LocalDate lendemain = date.plusDays(1);
        String nextJpql = codeAgence == null
            ? "SELECT COUNT(c) FROM ClotureJournaliere c " +
              "WHERE c.dateCloture = :len AND c.agence IS NULL AND c.statut = 'CLOTURE'"
            : "SELECT COUNT(c) FROM ClotureJournaliere c " +
              "WHERE c.dateCloture = :len " +
              "  AND c.agence.codeAgence = :agence AND c.statut = 'CLOTURE'";
        var nextQ = em.createQuery(nextJpql, Long.class).setParameter("len", lendemain);
        if (codeAgence != null) nextQ.setParameter("agence", codeAgence);

        if (nextQ.getSingleResult() > 0) {
            throw new ClotureException(
                "Impossible de réouvrir le " + date +
                " : la journée du " + lendemain + " est déjà clôturée.");
        }

        ClotureJournaliere cloture = clotureList.get(0);
        cloture.setStatut("REOUVERT");
        cloture.setObservations("Réouverture par " + utilisateur + " : " + motif);
    }

    // ══════════════════════════════════════════════════════════════
    // Exception métier
    // ══════════════════════════════════════════════════════════════

    /**
     * Exception levée lorsqu'un contrôle de clôture échoue.
     *
     * Étend RuntimeException pour déclencher le rollback automatique
     * de la transaction Spring (@Transactional).
     */
    public static class ClotureException extends RuntimeException {
        public ClotureException(String message) {
            super(message);
        }
    }

    // ══════════════════════════════════════════════════════════════
    // Helpers internes
    // ══════════════════════════════════════════════════════════════

    /** Convertit un résultat de COUNT() (Long ou null) en Long. */
    private Long toLong(Object o) {
        if (o == null) return 0L;
        if (o instanceof Long l) return l;
        return ((Number) o).longValue();
    }

    /** Convertit un résultat de SUM() (BigDecimal ou null) en BigDecimal. */
    private BigDecimal toBd(Object o) {
        if (o == null) return BigDecimal.ZERO;
        if (o instanceof BigDecimal bd) return bd;
        return new BigDecimal(o.toString());
    }
}
