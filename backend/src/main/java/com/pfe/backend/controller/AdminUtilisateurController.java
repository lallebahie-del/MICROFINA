package com.pfe.backend.controller;

import com.pfe.backend.dto.UtilisateurDTO;
import com.pfe.backend.service.UtilisateurService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * AdminUtilisateurController — gestion des comptes utilisateurs applicatifs.
 */
@Tag(name = "Admin — Utilisateurs", description = "CRUD des comptes utilisateurs")
@RestController
@RequestMapping("/api/v1/admin/utilisateurs")
public class AdminUtilisateurController {

    private final UtilisateurService utilisateurService;

    public AdminUtilisateurController(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    @Operation(summary = "Lister tous les utilisateurs")
    @GetMapping
    @PreAuthorize("hasAuthority('PRIV_MANAGE_USERS')")
    public ResponseEntity<List<UtilisateurDTO.Response>> findAll() {
        return ResponseEntity.ok(utilisateurService.findAll());
    }

    @Operation(summary = "Obtenir un utilisateur par son identifiant")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PRIV_MANAGE_USERS')")
    public ResponseEntity<UtilisateurDTO.Response> findById(@PathVariable Long id) {
        return ResponseEntity.ok(utilisateurService.findById(id));
    }

    @Operation(summary = "Lister les utilisateurs d'une agence")
    @GetMapping("/agence/{codeAgence}")
    @PreAuthorize("hasAuthority('PRIV_MANAGE_USERS')")
    public ResponseEntity<List<UtilisateurDTO.Response>> findByAgence(
            @PathVariable String codeAgence) {
        return ResponseEntity.ok(utilisateurService.findByAgence(codeAgence));
    }

    @Operation(summary = "Créer un nouvel utilisateur")
    @PostMapping
    @PreAuthorize("hasAuthority('PRIV_MANAGE_USERS')")
    public ResponseEntity<UtilisateurDTO.Response> create(
            @Valid @RequestBody UtilisateurDTO.CreateRequest req) {
        UtilisateurDTO.Response created = utilisateurService.create(req);
        return ResponseEntity
            .created(URI.create("/api/v1/admin/utilisateurs/" + created.id()))
            .body(created);
    }

    @Operation(summary = "Mettre à jour un utilisateur")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PRIV_MANAGE_USERS')")
    public ResponseEntity<UtilisateurDTO.Response> update(
            @PathVariable Long id,
            @RequestBody UtilisateurDTO.UpdateRequest req) {
        return ResponseEntity.ok(utilisateurService.update(id, req));
    }

    @Operation(summary = "Désactiver un compte utilisateur")
    @PatchMapping("/{id}/desactiver")
    @PreAuthorize("hasAuthority('PRIV_MANAGE_USERS')")
    public ResponseEntity<Void> desactiver(@PathVariable Long id) {
        utilisateurService.desactiver(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Réinitialiser le mot de passe d'un utilisateur")
    @PatchMapping("/{id}/reinitialiser-mdp")
    @PreAuthorize("hasAuthority('PRIV_MANAGE_USERS')")
    public ResponseEntity<Void> reinitialiserMotDePasse(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String nouveauMotDePasse = body.get("nouveauMotDePasse");
        utilisateurService.reinitialiserMotDePasse(id, nouveauMotDePasse);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Supprimer un utilisateur")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PRIV_MANAGE_USERS')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        utilisateurService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Lister les privilèges directs attribués à l'utilisateur")
    @GetMapping("/{id}/privileges")
    @PreAuthorize("hasAuthority('PRIV_MANAGE_USERS')")
    public ResponseEntity<List<String>> getDirectPrivileges(@PathVariable Long id) {
        return ResponseEntity.ok(utilisateurService.getDirectPrivileges(id));
    }

    @Operation(summary = "Attribuer une liste exacte de privilèges à un utilisateur")
    @PutMapping("/{id}/privileges")
    @PreAuthorize("hasAuthority('PRIV_MANAGE_USERS')")
    public ResponseEntity<UtilisateurDTO.Response> setDirectPrivileges(
            @PathVariable Long id,
            @RequestBody Map<String, List<String>> body) {
        List<String> privileges = body.get("privileges");
        return ResponseEntity.ok(utilisateurService.setDirectPrivileges(id, privileges));
    }
}
