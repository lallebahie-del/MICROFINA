package com.pfe.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * WalletDto — DTOs du module Wallet Bankily.
 *
 * <h2>Hiérarchie</h2>
 * <ul>
 *   <li>{@link DeblocageRequest}    — déclencher un déblocage via wallet</li>
 *   <li>{@link RemboursementRequest} — initier un remboursement via wallet</li>
 *   <li>{@link CallbackRequest}     — payload webhook entrant de Bankily</li>
 *   <li>{@link OperationResponse}   — réponse standard (lecture/initiation)</li>
 *   <li>{@link StatutResponse}      — réponse rafraîchissement statut</li>
 * </ul>
 */
public final class WalletDto {

    private WalletDto() {}

    // ── Requête de déblocage ──────────────────────────────────────────

    /**
     * Demande de décaissement d'un crédit vers le wallet Bankily du membre.
     */
    public record DeblocageRequest(

            @NotNull(message = "L'identifiant du crédit est obligatoire")
            Long idCredit,

            @NotBlank(message = "Le numéro de téléphone est obligatoire")
            @Pattern(regexp = "^\\+?[0-9]{8,15}$", message = "Format de téléphone invalide")
            String numeroTelephone,

            @Size(max = 255)
            String motif
    ) {}

    // ── Requête de remboursement ──────────────────────────────────────

    /**
     * Demande de collecte d'un remboursement depuis le wallet Bankily du membre.
     */
    public record RemboursementRequest(

            @NotNull(message = "L'identifiant du crédit est obligatoire")
            Long idCredit,

            @NotBlank(message = "Le numéro de téléphone est obligatoire")
            @Pattern(regexp = "^\\+?[0-9]{8,15}$", message = "Format de téléphone invalide")
            String numeroTelephone,

            @NotNull(message = "Le montant est obligatoire")
            @DecimalMin(value = "0.01", message = "Le montant doit être positif")
            BigDecimal montant,

            @Size(max = 255)
            String motif
    ) {}

    // ── Callback Bankily ─────────────────────────────────────────────

    /**
     * Payload du webhook de notification Bankily.
     * Bankily appelle {@code POST /api/v1/wallet/callback} avec ce corps JSON.
     */
    public record CallbackRequest(

            /** Référence de la transaction chez Bankily. */
            @NotBlank
            String referenceBankily,

            /** Statut Bankily : SUCCESS | FAILED | EXPIRED | CANCELLED. */
            @NotBlank
            String statut,

            /** Code retour opérateur. */
            String codeRetour,

            /** Message opérateur (en clair). */
            String message,

            /** Signature HMAC-SHA256 pour vérification d'authenticité. */
            String signature
    ) {}

    // ── Réponse opération ─────────────────────────────────────────────

    /**
     * Réponse standardisée pour toute opération wallet (initiation ou lecture).
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record OperationResponse(

            Long          id,
            String        referenceMfi,
            String        referenceBankily,
            String        numeroTelephone,
            BigDecimal    montant,
            String        typeOperation,
            String        statut,
            LocalDate     dateOperation,
            LocalDateTime dateConfirmation,
            String        motif,
            String        codeRetour,
            String        messageRetour,

            // Liens métier
            String        numMembre,
            Long          idCredit,
            String        codeAgence,

            // Audit
            String        utilisateur
    ) {}

    // ── Réponse rafraîchissement statut ──────────────────────────────

    /**
     * Réponse de la consultation du statut temps-réel auprès de Bankily.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record StatutResponse(
            String  referenceMfi,
            String  referenceBankily,
            String  statutLocal,        // statut en base avant refresh
            String  statutBankily,      // statut retourné par l'API Bankily
            boolean miseAJourEffectuee, // true si le statut local a changé
            String  message
    ) {}

    // ── Réponse Bankily (interne — retour du BankilyClient) ──────────

    /**
     * Réponse brute de l'API Bankily lors de l'initiation d'une transaction.
     * Usage interne uniquement (non exposé via REST).
     */
    public record BankilyInitResponse(
            boolean  succes,
            String   referenceBankily,
            String   codeRetour,
            String   message
    ) {}

    /**
     * Réponse brute de l'API Bankily lors de la consultation d'une transaction.
     * Usage interne uniquement (non exposé via REST).
     */
    public record BankilyStatutResponse(
            String  referenceBankily,
            String  statut,      // SUCCESS | PENDING | FAILED | EXPIRED | CANCELLED
            String  codeRetour,
            String  message
    ) {}

    /**
     * Résultat d'une réconciliation Bankily.
     *
     * @param agence          code agence traité (ou "TOUTES" si aucun filtre)
     * @param operationsVerifiees  nombre d'opérations EN_ATTENTE vérifiées
     * @param operationsMisesAJour nombre d'opérations dont le statut a changé
     * @param erreurs         liste des références MFI ayant échoué lors du polling
     */
    public record ReconciliationResponse(
            String       agence,
            int          operationsVerifiees,
            int          operationsMisesAJour,
            java.util.List<String> erreurs
    ) {}
}
