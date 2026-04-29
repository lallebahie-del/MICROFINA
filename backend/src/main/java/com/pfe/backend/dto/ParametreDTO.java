package com.pfe.backend.dto;

import com.microfina.entity.Parametre;

/**
 * ParametreDTO — DTOs pour la gestion des paramètres de configuration par agence.
 */
public class ParametreDTO {

    /**
     * Corps de la requête POST /api/v1/admin/parametres.
     */
    public record CreateRequest(
        Integer maxiJourOuvert,
        String prefixe,
        String suffixe,
        String useMultidevise,
        String codeAgence
    ) {}

    /**
     * Corps de la requête PUT /api/v1/admin/parametres/{id}.
     * Tous les champs sont optionnels (patch partiel).
     */
    public record UpdateRequest(
        Integer maxiJourOuvert,
        String prefixe,
        String suffixe,
        String useMultidevise,
        String codeAgence
    ) {}

    /**
     * Réponse renvoyée au client.
     */
    public record Response(
        Long idParametre,
        Integer maxiJourOuvert,
        String prefixe,
        String suffixe,
        String useMultidevise,
        String codeAgence
    ) {
        /**
         * Construit un Response à partir d'une entité Parametre.
         *
         * @param p entité source
         * @return DTO Response correspondant
         */
        public static Response from(Parametre p) {
            return new Response(
                p.getIdParametre(),
                p.getMaxiJourOuvert(),
                p.getPrefixe(),
                p.getSuffixe(),
                p.getUseMultidevise(),
                p.getAgence() != null ? p.getAgence().getCodeAgence() : null
            );
        }
    }
}
