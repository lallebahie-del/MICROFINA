package com.pfe.backend.dto;

import com.microfina.entity.OperationBanque;
import com.microfina.entity.StatutOperationBanque;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * OperationBanqueDTO – DTOs pour les opérations bancaires.
 *
 * <ul>
 *   <li>{@link CreateRequest} – création d'une opération</li>
 *   <li>{@link UpdateRequest} – mise à jour du statut / utilisateur</li>
 *   <li>{@link Response}      – réponse exposée à l'API</li>
 * </ul>
 */
public final class OperationBanqueDTO {

    private OperationBanqueDTO() { }

    // ── Create ────────────────────────────────────────────────────────────────

    /**
     * Payload de création d'une opération bancaire.
     *
     * @param dateOperation  date de l'opération (obligatoire)
     * @param montant        montant (obligatoire, &gt; 0)
     * @param utilisateur    identifiant utilisateur (nullable)
     * @param compteBanqueId identifiant du compte bancaire (nullable)
     * @param codeAgence     code agence (nullable)
     * @param idComptabilite FK vers l'écriture comptable (obligatoire)
     */
    public record CreateRequest(
            @NotNull  LocalDate dateOperation,
            @NotNull  @DecimalMin("0.01") BigDecimal montant,
                      String utilisateur,
                      Long   compteBanqueId,
                      String codeAgence,
            @NotNull  Long   idComptabilite
    ) { }

    // ── Update ────────────────────────────────────────────────────────────────

    /**
     * Payload de mise à jour partielle d'une opération bancaire.
     *
     * @param statut     nouveau statut (nullable)
     * @param utilisateur nouvel utilisateur (nullable)
     */
    public record UpdateRequest(
            String statut,
            String utilisateur
    ) { }

    // ── Response ──────────────────────────────────────────────────────────────

    /**
     * Réponse renvoyée par l'API.
     *
     * @param id             identifiant technique
     * @param dateOperation  date de l'opération
     * @param montant        montant
     * @param statut         statut courant
     * @param utilisateur    utilisateur
     * @param compteBanqueId identifiant du compte bancaire (nullable)
     * @param codeAgence     code agence (nullable)
     * @param idComptabilite identifiant de l'écriture comptable
     */
    public record Response(
            Long                   id,
            LocalDate              dateOperation,
            BigDecimal             montant,
            StatutOperationBanque  statut,
            String                 utilisateur,
            Long                   compteBanqueId,
            String                 codeAgence,
            Long                   idComptabilite
    ) {
        /**
         * Construit un {@code Response} à partir de l'entité.
         *
         * @param op l'entité {@link OperationBanque}
         * @return le DTO correspondant
         */
        public static Response from(OperationBanque op) {
            return new Response(
                    op.getId(),
                    op.getDateOperation(),
                    op.getMontant(),
                    op.getStatut(),
                    op.getUtilisateur(),
                    op.getCompteBanque() != null ? op.getCompteBanque().getId()              : null,
                    op.getAgence()       != null ? op.getAgence().getCodeAgence()            : null,
                    op.getComptabilite() != null ? op.getComptabilite().getIdComptabilite()  : null
            );
        }
    }
}
