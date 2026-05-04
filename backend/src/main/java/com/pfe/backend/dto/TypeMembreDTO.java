package com.pfe.backend.dto;

import com.microfina.entity.ProduitCreditTypeMembre;

/**
 * TypeMembreDTO — DTOs pour la consultation des types membres éligibles aux produits crédit.
 */
public class TypeMembreDTO {

    /**
     * Vue simple d'un type membre avec ses informations de produit associé.
     */
    public record TypeMembre(
        String typeMembre,
        String libelleTypeMembre,
        Integer actif,
        String numProduit
    ) {}

    /**
     * Réponse renvoyée au client.
     */
    public record Response(
        String typeMembre,
        String libelleTypeMembre,
        Integer actif,
        String numProduit
    ) {
        /**
         * Construit un Response à partir d'une entité ProduitCreditTypeMembre.
         *
         * @param p entité source
         * @return DTO Response correspondant
         */
        public static Response from(ProduitCreditTypeMembre p) {
            return new Response(
                p.getId() != null ? p.getId().getTypeMembre() : null,
                p.getLibelleTypeMembre(),
                p.getActif(),
                p.getId() != null ? p.getId().getNumProduit() : null
            );
        }
    }
}
