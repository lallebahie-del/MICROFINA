package com.pfe.backend.dto;

import com.microfina.entity.ModePaiementCaisse;
import com.microfina.entity.OperationCaisse;
import com.microfina.entity.StatutOperationCaisse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * OperationCaisseDTO – DTOs pour les opérations de caisse.
 *
 * <ul>
 *   <li>{@link CreateRequest} – création d'une opération</li>
 *   <li>{@link UpdateRequest} – mise à jour du statut / motif</li>
 *   <li>{@link Response}      – réponse exposée à l'API</li>
 * </ul>
 */
public final class OperationCaisseDTO {

    private OperationCaisseDTO() { }

    // ── Create ────────────────────────────────────────────────────────────────

    /**
     * Payload de création d'une opération de caisse.
     *
     * @param numPiece        numéro de pièce comptable (obligatoire, max 30)
     * @param dateOperation   date de l'opération (obligatoire)
     * @param montant         montant (obligatoire, positif)
     * @param modePaiement    mode de paiement (ESPECES / CHEQUE / VIREMENT / MOBILE_MONEY)
     * @param motif           motif libre (max 500, nullable)
     * @param utilisateur     identifiant utilisateur (nullable)
     * @param numCompte       numéro de compte épargne (nullable)
     * @param codeAgence      code agence (nullable – proxy résolu côté service)
     * @param idComptabilite  FK vers l'écriture comptable (obligatoire)
     */
    public record CreateRequest(
            @NotBlank String numPiece,
            @NotNull  LocalDate dateOperation,
            @NotNull  BigDecimal montant,
                      String modePaiement,
                      String motif,
                      String utilisateur,
                      String numCompte,
                      String codeAgence,
            @NotNull  Long idComptabilite
    ) { }

    // ── Update ────────────────────────────────────────────────────────────────

    /**
     * Payload de mise à jour partielle d'une opération de caisse.
     *
     * @param statut  nouveau statut (VALIDE ou ANNULE) – nullable
     * @param motif   nouveau motif – nullable
     */
    public record UpdateRequest(
            String statut,
            String motif
    ) { }

    // ── Response ──────────────────────────────────────────────────────────────

    /**
     * Réponse renvoyée par l'API.
     *
     * @param id              identifiant technique
     * @param numPiece        numéro de pièce
     * @param dateOperation   date de l'opération
     * @param montant         montant
     * @param modePaiement    mode de paiement
     * @param motif           motif
     * @param utilisateur     utilisateur
     * @param statut          statut courant
     * @param numCompte       numéro de compte épargne (nullable)
     * @param codeAgence      code agence (nullable)
     * @param idComptabilite  identifiant de l'écriture comptable
     */
    public record Response(
            Long                   id,
            String                 numPiece,
            LocalDate              dateOperation,
            BigDecimal             montant,
            ModePaiementCaisse     modePaiement,
            String                 motif,
            String                 utilisateur,
            StatutOperationCaisse  statut,
            String                 numCompte,
            String                 codeAgence,
            Long                   idComptabilite
    ) {
        /**
         * Construit un {@code Response} à partir de l'entité.
         *
         * @param op l'entité {@link OperationCaisse}
         * @return le DTO correspondant
         */
        public static Response from(OperationCaisse op) {
            return new Response(
                    op.getId(),
                    op.getNumPiece(),
                    op.getDateOperation(),
                    op.getMontant(),
                    op.getModePaiement(),
                    op.getMotif(),
                    op.getUtilisateur(),
                    op.getStatut(),
                    op.getCompteEps() != null ? op.getCompteEps().getNumCompte() : null,
                    op.getAgence()    != null ? op.getAgence().getCodeAgence()   : null,
                    op.getComptabilite() != null ? op.getComptabilite().getIdComptabilite() : null
            );
        }
    }
}
