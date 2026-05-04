package com.pfe.backend.dto;

import com.microfina.entity.Privilege;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * PrivilegeDTO — DTOs pour la gestion des privilèges fonctionnels.
 */
public class PrivilegeDTO {

    /**
     * Corps de la requête POST /api/v1/admin/privileges.
     */
    public record CreateRequest(
        @NotBlank @Size(max = 100) String codePrivilege,
        @NotBlank @Size(max = 200) String libelle,
        @NotBlank @Size(max = 50) String module
    ) {}

    /**
     * Corps de la requête PUT /api/v1/admin/privileges/{id}.
     * Tous les champs sont optionnels (patch partiel).
     */
    public record UpdateRequest(
        @Size(max = 200) String libelle,
        @Size(max = 50) String module
    ) {}

    /**
     * Réponse renvoyée au client.
     */
    public record Response(
        Long id,
        String codePrivilege,
        String libelle,
        String module
    ) {
        /**
         * Construit un Response à partir d'une entité Privilege.
         *
         * @param p entité source
         * @return DTO Response correspondant
         */
        public static Response from(Privilege p) {
            return new Response(
                p.getId(),
                p.getCodePrivilege(),
                p.getLibelle(),
                p.getModule()
            );
        }
    }
}
