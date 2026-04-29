package com.pfe.backend.dto;

import com.microfina.entity.ActionAudit;
import com.microfina.entity.JournalAudit;

import java.time.LocalDateTime;

/**
 * JournalAuditDTO — DTOs pour la consultation du journal d'audit (lecture seule).
 */
public class JournalAuditDTO {

    /**
     * Réponse renvoyée au client — ne contient pas les informations de taille maximale.
     */
    public record Response(
        Long id,
        LocalDateTime dateAction,
        String utilisateur,
        ActionAudit action,
        String entite,
        String idEntite,
        String ancienneValeur,
        String nouvelleValeur,
        String adresseIp
    ) {
        /**
         * Construit un Response à partir d'une entité JournalAudit.
         *
         * @param j entité source
         * @return DTO Response correspondant
         */
        public static Response from(JournalAudit j) {
            return new Response(
                j.getId(),
                j.getDateAction(),
                j.getUtilisateur(),
                j.getAction(),
                j.getEntite(),
                j.getIdEntite(),
                j.getAncienneValeur(),
                j.getNouvelleValeur(),
                j.getAdresseIp()
            );
        }
    }
}
