package com.pfe.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * GarantieDTO — DTO de réponse (lecture) pour l'entité {@code Garantie}.
 *
 * <p>Aplati : les associations {@code TypeGarantie} et {@code Credits} sont
 * représentées par leurs identifiants et libellés uniquement.</p>
 */
public record GarantieDTO(

        Long      idGarantie,

        // ── Type ──────────────────────────────────────────────────────────
        String    codeTypeGarantie,
        String    libelleTypeGarantie,

        // ── Crédit couvert ────────────────────────────────────────────────
        Long      idCredit,
        String    numCredit,

        // ── Garant (membre, optionnel) ────────────────────────────────────
        String    numMembreGarant,

        // ── Valorisation ─────────────────────────────────────────────────
        BigDecimal valeurEstimee,
        BigDecimal tauxCouverture,
        LocalDate  dateEvaluation,

        // ── Suivi de vie ──────────────────────────────────────────────────
        String    statut,
        LocalDate dateMainlevee,
        String    referenceDocument,
        String    observations,

        // ── Traçabilité ───────────────────────────────────────────────────
        String    utilisateur,
        LocalDate dateSaisie
) {

    // ── Requête de création ───────────────────────────────────────────────

    /**
     * Corps de la requête POST /api/v1/garanties.
     */
    public record CreationRequest(
            @NotBlank String    codeTypeGarantie,
            @NotNull  Long      idCredit,
            String              numMembreGarant,
            @NotNull @DecimalMin("0.00") BigDecimal valeurEstimee,
            LocalDate           dateEvaluation,
            String              referenceDocument,
            String              observations
    ) {}

    /**
     * Corps de la requête PATCH /api/v1/garanties/{id}/liberer.
     */
    public record MainleveeRequest(
            @NotNull LocalDate dateMainlevee,
            String             observations
    ) {}

    /**
     * Réponse synthétique de couverture d'un crédit.
     */
    public record CouvertureDTO(
            Long       idCredit,
            String     numCredit,
            BigDecimal montantCredit,
            BigDecimal totalGaranties,
            Double     tauxCouverturePct,
            int        nbGaranties
    ) {}
}
