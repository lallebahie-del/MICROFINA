package com.pfe.backend.dto;

import com.microfina.entity.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * RoleDTO — DTOs pour la gestion des rôles fonctionnels.
 */
public class RoleDTO {

    /**
     * Corps de la requête POST /api/v1/admin/roles.
     */
    public record CreateRequest(
        @NotBlank @Size(max = 50) String codeRole,
        @NotBlank @Size(max = 100) String libelle,
        @Size(max = 500) String description
    ) {}

    /**
     * Corps de la requête PUT /api/v1/admin/roles/{id}.
     * Tous les champs sont optionnels (patch partiel).
     */
    public record UpdateRequest(
        @Size(max = 100) String libelle,
        @Size(max = 500) String description
    ) {}

    /**
     * Réponse renvoyée au client.
     */
    public record Response(
        Long id,
        String codeRole,
        String libelle,
        String description
    ) {
        /**
         * Construit un Response à partir d'une entité Role.
         *
         * @param r entité source
         * @return DTO Response correspondant
         */
        public static Response from(Role r) {
            return new Response(
                r.getId(),
                r.getCodeRole(),
                r.getLibelle(),
                r.getDescription()
            );
        }
    }
}
