package com.pfe.backend.dto;

import com.microfina.entity.Utilisateur;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * UtilisateurDTO — DTOs pour la gestion des comptes utilisateurs.
 */
public class UtilisateurDTO {

    /**
     * Corps de la requête POST /api/v1/admin/utilisateurs.
     * motDePasse est en clair — il sera BCrypt-encodé côté service.
     * roles : liste de codes rôle (ex. ["ADMIN","AGENT_CREDIT"]) — null = aucun rôle.
     */
    public record CreateRequest(
        @NotBlank @Size(max = 100) String login,
        @NotBlank String motDePasse,
        @Size(max = 255) String nomComplet,
        String email,
        String telephone,
        Boolean actif,
        LocalDate dateExpirationCompte,
        String codeAgence,
        List<String> roles
    ) {}

    /**
     * Corps de la requête PUT /api/v1/admin/utilisateurs/{id}.
     * Tous les champs sont optionnels (patch partiel).
     * roles : null = ne pas toucher aux rôles ; liste vide = supprimer tous les rôles.
     */
    public record UpdateRequest(
        String nomComplet,
        String email,
        String telephone,
        Boolean actif,
        LocalDate dateExpirationCompte,
        String codeAgence,
        List<String> roles
    ) {}

    /**
     * Réponse renvoyée au client — ne contient jamais le hash du mot de passe.
     */
    public record Response(
        Long id,
        String login,
        String nomComplet,
        String email,
        String telephone,
        Boolean actif,
        LocalDate dateExpirationCompte,
        LocalDateTime derniereConnexion,
        Integer nombreEchecs,
        String codeAgence,
        List<String> roles
    ) {
        public static Response from(Utilisateur u, List<String> roles) {
            return new Response(
                u.getId(),
                u.getLogin(),
                u.getNomComplet(),
                u.getEmail(),
                u.getTelephone(),
                u.getActif(),
                u.getDateExpirationCompte(),
                u.getDerniereConnexion(),
                u.getNombreEchecs(),
                u.getAgence() != null ? u.getAgence().getCodeAgence() : null,
                roles != null ? roles : List.of()
            );
        }

        public static Response from(Utilisateur u) {
            return from(u, List.of());
        }
    }
}
