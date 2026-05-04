package com.pfe.backend.dto;

import com.microfina.entity.Comptabilite;

/**
 * ComptabiliteDTO – DTO de lecture pour les écritures comptables.
 *
 * <p>La comptabilité est en lecture seule via l'API REST ;
 * les écritures sont générées par les autres modules (caisse, banque, etc.).</p>
 */
public final class ComptabiliteDTO {

    private ComptabiliteDTO() { }

    // ── Response ──────────────────────────────────────────────────────────────

    /**
     * Réponse minimale renvoyée par l'API.
     *
     * @param idComptabilite identifiant technique de l'écriture
     * @param codeAgence     code de l'agence rattachée à l'écriture (nullable)
     */
    public record Response(
            Long   idComptabilite,
            String codeAgence
    ) {
        /**
         * Construit un {@code Response} à partir de l'entité.
         *
         * <p>Note : {@link Comptabilite#getCodeAgence()} est un champ String direct
         * (colonne CODE_AGENCE). La relation JPA {@code agence} (FK vers AGENCE) est
         * distincte et utilisée uniquement si disponible.</p>
         *
         * @param c l'entité {@link Comptabilite}
         * @return le DTO correspondant
         */
        public static Response from(Comptabilite c) {
            // Priorité à la relation JPA agence, fallback sur la colonne codeAgence (String)
            String agenceCode = c.getAgence() != null
                    ? c.getAgence().getCodeAgence()
                    : c.getCodeAgence();

            return new Response(
                    c.getIdComptabilite(),
                    agenceCode
            );
        }
    }
}
