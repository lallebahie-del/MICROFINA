package com.pfe.backend.dto;

import com.microfina.entity.Budget;
import com.microfina.entity.StatutBudget;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * BudgetDTO – DTOs pour la gestion budgétaire.
 *
 * <ul>
 *   <li>{@link CreateRequest} – création d'un budget</li>
 *   <li>{@link UpdateRequest} – mise à jour d'un budget</li>
 *   <li>{@link Response}      – réponse exposée à l'API</li>
 * </ul>
 */
public final class BudgetDTO {

    private BudgetDTO() { }

    // ── Create ────────────────────────────────────────────────────────────────

    /**
     * Payload de création d'un budget.
     *
     * @param exerciceFiscal année de l'exercice (obligatoire)
     * @param dateCreation   date de création (obligatoire)
     * @param codeAgence     code agence (nullable)
     * @param utilisateur    identifiant utilisateur (nullable)
     */
    public record CreateRequest(
            @NotNull Integer   exerciceFiscal,
            @NotNull LocalDate dateCreation,
                     String    codeAgence,
                     String    utilisateur
    ) { }

    // ── Update ────────────────────────────────────────────────────────────────

    /**
     * Payload de mise à jour partielle d'un budget.
     *
     * @param statut               nouveau statut (nullable)
     * @param montantTotalRecettes total recettes mis à jour (nullable)
     * @param montantTotalDepenses total dépenses mis à jour (nullable)
     * @param utilisateur          utilisateur ayant effectué la modification (nullable)
     */
    public record UpdateRequest(
            String     statut,
            BigDecimal montantTotalRecettes,
            BigDecimal montantTotalDepenses,
            String     utilisateur
    ) { }

    // ── Response ──────────────────────────────────────────────────────────────

    /**
     * Réponse renvoyée par l'API.
     *
     * @param id                   identifiant technique
     * @param exerciceFiscal       année de l'exercice
     * @param dateCreation         date de création
     * @param dateValidation       date de validation (nullable)
     * @param statut               statut courant
     * @param montantTotalRecettes total recettes
     * @param montantTotalDepenses total dépenses
     * @param utilisateur          dernier utilisateur
     * @param codeAgence           code agence (nullable)
     */
    public record Response(
            Long        id,
            Integer     exerciceFiscal,
            LocalDate   dateCreation,
            LocalDate   dateValidation,
            StatutBudget statut,
            BigDecimal  montantTotalRecettes,
            BigDecimal  montantTotalDepenses,
            String      utilisateur,
            String      codeAgence
    ) {
        /**
         * Construit un {@code Response} à partir de l'entité.
         *
         * @param b l'entité {@link Budget}
         * @return le DTO correspondant
         */
        public static Response from(Budget b) {
            return new Response(
                    b.getId(),
                    b.getExerciceFiscal(),
                    b.getDateCreation(),
                    b.getDateValidation(),
                    b.getStatut(),
                    b.getMontantTotalRecettes(),
                    b.getMontantTotalDepenses(),
                    b.getUtilisateur(),
                    b.getAgence() != null ? b.getAgence().getCodeAgence() : null
            );
        }
    }
}
