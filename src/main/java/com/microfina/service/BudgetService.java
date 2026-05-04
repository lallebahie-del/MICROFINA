package com.microfina.service;

import com.microfina.entity.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Renommé en "metierBudgetService" pour cohabiter avec com.pfe.backend.service.BudgetService
// (couche applicative). Référence ce bean explicitement via @Qualifier("metierBudgetService") si besoin.

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * BudgetService – service métier pour la gestion des budgets annuels.
 *
 * <p>Couvre les flux cahier §3.1.1 :
 * <ul>
 *   <li>{@link #creerBudget} – initialise un budget vierge pour un exercice fiscal.</li>
 *   <li>{@link #ajouterLigne} – ajoute une ligne (recette ou dépense) à un budget BROUILLON.</li>
 *   <li>{@link #validerBudget} – verrouille le budget (BROUILLON → VALIDE).</li>
 *   <li>{@link #enregistrerMouvement} – impute un mouvement réel sur une ligne validée,
 *       met à jour {@code montantRealise} et génère une écriture {@link Comptabilite}.</li>
 * </ul>
 *
 * <p>Règle transversale : toute opération monétaire
 * ({@link #enregistrerMouvement}) crée une écriture {@link Comptabilite}
 * dans la <strong>même transaction</strong>.</p>
 *
 * DDL source of truth : P8-001 à P8-003.
 * Spec : cahier §3.1.1 (Saisie du budget et de ses modifications).
 */
@Service("metierBudgetService")
@Transactional
public class BudgetService {

    @PersistenceContext
    private EntityManager em;

    // ═══════════════════════════════════════════════════════════════
    // CRÉATION DU BUDGET
    // ═══════════════════════════════════════════════════════════════

    /**
     * Crée un budget vierge pour l'exercice fiscal donné.
     *
     * <p>Le budget est créé au statut {@link StatutBudget#BROUILLON}.
     * Il ne peut pas y avoir deux budgets pour la même agence et le même exercice
     * (la contrainte est à vérifier en amont par le contrôleur).</p>
     *
     * @param agence        agence propriétaire du budget
     * @param exercice      année fiscale (ex. 2024)
     * @param utilisateur   login du créateur
     * @return le {@link Budget} persisté
     */
    public Budget creerBudget(Agence agence, int exercice, String utilisateur) {
        if (exercice < 2000 || exercice > 2100) {
            throw new IllegalArgumentException(
                "Exercice fiscal invalide : " + exercice + ". Attendu entre 2000 et 2100."
            );
        }

        Budget budget = new Budget();
        budget.setAgence(agence);
        budget.setExerciceFiscal(exercice);
        budget.setDateCreation(LocalDate.now());
        budget.setStatut(StatutBudget.BROUILLON);
        budget.setMontantTotalRecettes(BigDecimal.ZERO);
        budget.setMontantTotalDepenses(BigDecimal.ZERO);
        budget.setUtilisateur(utilisateur);
        em.persist(budget);

        return budget;
    }

    // ═══════════════════════════════════════════════════════════════
    // AJOUT D'UNE LIGNE BUDGÉTAIRE
    // ═══════════════════════════════════════════════════════════════

    /**
     * Ajoute une ligne budgétaire (recette ou dépense) à un budget.
     *
     * <p>Seuls les budgets au statut {@link StatutBudget#BROUILLON} acceptent
     * de nouvelles lignes. Après validation, le budget est verrouillé.</p>
     *
     * <p>À chaque ajout, les totaux du budget ({@code montantTotalRecettes} ou
     * {@code montantTotalDepenses}) sont recalculés automatiquement.</p>
     *
     * @param budget        budget cible (doit être BROUILLON)
     * @param codeRubrique  code comptable de la rubrique (ex. "6101")
     * @param libelle       intitulé de la ligne
     * @param type          {@link TypeLigneBudget#RECETTE} ou {@link TypeLigneBudget#DEPENSE}
     * @param montantPrevu  montant prévisionnel (≥ 0)
     * @param compte        code du compte comptable rattaché (nullable)
     * @return la {@link LigneBudget} persistée
     * @throws IllegalStateException si le budget n'est pas BROUILLON
     */
    public LigneBudget ajouterLigne(Budget budget,
                                     String codeRubrique,
                                     String libelle,
                                     TypeLigneBudget type,
                                     BigDecimal montantPrevu,
                                     String compte) {
        exigerStatut(budget, StatutBudget.BROUILLON, "Impossible d'ajouter une ligne");

        if (montantPrevu == null || montantPrevu.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                "Le montant prévu doit être ≥ 0. Reçu : " + montantPrevu
            );
        }

        LigneBudget ligne = new LigneBudget();
        ligne.setBudget(budget);
        ligne.setCodeRubrique(codeRubrique);
        ligne.setLibelle(libelle);
        ligne.setTypeLigne(type != null ? type : TypeLigneBudget.RECETTE);
        ligne.setMontantPrevu(montantPrevu);
        ligne.setMontantRealise(BigDecimal.ZERO);
        ligne.setCompte(compte);
        em.persist(ligne);

        // Recalcul des totaux du budget
        if (TypeLigneBudget.RECETTE.equals(type)) {
            budget.setMontantTotalRecettes(
                budget.getMontantTotalRecettes().add(montantPrevu)
            );
        } else {
            budget.setMontantTotalDepenses(
                budget.getMontantTotalDepenses().add(montantPrevu)
            );
        }
        em.merge(budget);

        return ligne;
    }

    // ═══════════════════════════════════════════════════════════════
    // VALIDATION DU BUDGET
    // ═══════════════════════════════════════════════════════════════

    /**
     * Valide un budget : BROUILLON → VALIDE.
     *
     * <p>Après validation, plus aucune ligne ne peut être ajoutée ou supprimée.
     * Les mouvements réels peuvent en revanche être enregistrés.</p>
     *
     * @param budget      budget à valider (doit être BROUILLON)
     * @param utilisateur login du validateur
     * @return le {@link Budget} mis à jour
     * @throws IllegalStateException si le budget n'est pas BROUILLON
     */
    public Budget validerBudget(Budget budget, String utilisateur) {
        exigerStatut(budget, StatutBudget.BROUILLON, "Impossible de valider");

        budget.setStatut(StatutBudget.VALIDE);
        budget.setDateValidation(LocalDate.now());
        budget.setUtilisateur(utilisateur);

        return em.merge(budget);
    }

    // ═══════════════════════════════════════════════════════════════
    // ENREGISTREMENT D'UN MOUVEMENT RÉEL
    // ═══════════════════════════════════════════════════════════════

    /**
     * Impute un mouvement réel sur une ligne budgétaire et génère l'écriture comptable.
     *
     * <p>Flux atomique :
     * <ol>
     *   <li>Vérifier que le budget est {@link StatutBudget#VALIDE}.</li>
     *   <li>Créer et persister l'écriture {@link Comptabilite}.</li>
     *   <li>Créer et persister le {@link MouvementBudget}.</li>
     *   <li>Incrémenter {@link LigneBudget#getMontantRealise()} += montant.</li>
     *   <li>Recalculer les totaux du {@link Budget}.</li>
     * </ol>
     *
     * @param ligneBudget ligne budgétaire impactée (budget doit être VALIDE)
     * @param montant     montant réel (&gt; 0)
     * @param libelle     description de l'opération
     * @param utilisateur login de l'opérateur
     * @return le {@link MouvementBudget} persisté
     * @throws IllegalStateException    si le budget n'est pas VALIDE
     * @throws IllegalArgumentException si le montant est nul ou négatif
     */
    public MouvementBudget enregistrerMouvement(LigneBudget ligneBudget,
                                                  BigDecimal montant,
                                                  String libelle,
                                                  String utilisateur) {
        Budget budget = ligneBudget.getBudget();
        exigerStatut(budget, StatutBudget.VALIDE, "Impossible d'enregistrer un mouvement");
        validerMontantPositif(montant);

        // 1. Écriture comptable
        Comptabilite ecriture = creerEcritureComptable(montant, libelle, utilisateur, ligneBudget);
        em.persist(ecriture);

        // 2. Mouvement budgétaire
        MouvementBudget mouvement = new MouvementBudget();
        mouvement.setLigneBudget(ligneBudget);
        mouvement.setDateMouvement(LocalDate.now());
        mouvement.setMontant(montant);
        mouvement.setLibelle(libelle);
        mouvement.setUtilisateur(utilisateur);
        mouvement.setComptabilite(ecriture);
        em.persist(mouvement);

        // 3. Mise à jour montantRealise sur la ligne
        BigDecimal ancienRealise = ligneBudget.getMontantRealise() != null
            ? ligneBudget.getMontantRealise() : BigDecimal.ZERO;
        ligneBudget.setMontantRealise(ancienRealise.add(montant));
        em.merge(ligneBudget);

        // 4. Recalcul des totaux du budget parent
        mettreAJourTotalsBudget(budget, ligneBudget.getTypeLigne(), montant);

        return mouvement;
    }

    // ═══════════════════════════════════════════════════════════════
    // CLÔTURE DU BUDGET
    // ═══════════════════════════════════════════════════════════════

    /**
     * Clôture un budget validé : VALIDE → CLOTURE.
     *
     * <p>Un budget clôturé ne peut plus recevoir de mouvements.</p>
     *
     * @param budget      budget à clôturer (doit être VALIDE)
     * @param utilisateur login du clôturant
     * @return le {@link Budget} clôturé
     */
    public Budget cloturerBudget(Budget budget, String utilisateur) {
        exigerStatut(budget, StatutBudget.VALIDE, "Impossible de clôturer");
        budget.setStatut(StatutBudget.CLOTURE);
        budget.setUtilisateur(utilisateur);
        return em.merge(budget);
    }

    // ═══════════════════════════════════════════════════════════════
    // MÉTHODES PRIVÉES
    // ═══════════════════════════════════════════════════════════════

    /** Vérifie que le budget est dans le statut attendu ; lève une exception sinon. */
    private void exigerStatut(Budget budget, StatutBudget attendu, String operation) {
        if (!attendu.equals(budget.getStatut())) {
            throw new IllegalStateException(
                operation + " : statut attendu " + attendu
                + ", statut actuel " + budget.getStatut()
                + " (budget id=" + budget.getId() + ", exercice=" + budget.getExerciceFiscal() + ")"
            );
        }
    }

    /** Lève une exception si le montant est nul ou négatif. */
    private void validerMontantPositif(BigDecimal montant) {
        if (montant == null || montant.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                "Le montant doit être strictement positif. Reçu : " + montant
            );
        }
    }

    /**
     * Construit une écriture comptable pour un mouvement budgétaire.
     * Le sens débit/crédit dépend du type RECETTE vs DEPENSE.
     */
    private Comptabilite creerEcritureComptable(BigDecimal montant,
                                                  String libelle,
                                                  String utilisateur,
                                                  LigneBudget ligne) {
        Comptabilite c = new Comptabilite();
        c.setDateOperation(LocalDate.now());
        c.setLibelle(libelle != null ? libelle : "Mouvement budget " + ligne.getCodeRubrique());
        c.setReference("BUDGET_" + ligne.getTypeLigne().name());
        c.setCodeEmp(utilisateur);
        c.setEtat("VALIDE");

        if (TypeLigneBudget.RECETTE.equals(ligne.getTypeLigne())) {
            c.setCredit(montant);
            c.setDebit(BigDecimal.ZERO);
        } else {
            c.setDebit(montant);
            c.setCredit(BigDecimal.ZERO);
        }

        if (ligne.getCompte() != null) {
            c.setCompte1(ligne.getCompte());
        }

        return c;
    }

    /**
     * Incrémente le total des réalisations du budget parent
     * selon le type de la ligne (RECETTE ou DEPENSE).
     */
    private void mettreAJourTotalsBudget(Budget budget,
                                          TypeLigneBudget type,
                                          BigDecimal montant) {
        if (TypeLigneBudget.RECETTE.equals(type)) {
            budget.setMontantTotalRecettes(
                budget.getMontantTotalRecettes().add(montant)
            );
        } else {
            budget.setMontantTotalDepenses(
                budget.getMontantTotalDepenses().add(montant)
            );
        }
        em.merge(budget);
    }
}
