package com.pfe.backend.controller;

import com.pfe.backend.dto.CompteEpsDTO;
import com.pfe.backend.service.CompteEpsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * CompteEpsController — API REST pour les comptes épargne.
 *
 * <h2>Base URL</h2>
 * <pre>/api/v1/comptes-epargne</pre>
 */
@Tag(name = "Comptes Épargne", description = "Gestion des comptes épargne des membres")
@RestController
@RequestMapping("/api/v1/comptes-epargne")
public class CompteEpsController {

    private final CompteEpsService compteEpsService;

    public CompteEpsController(CompteEpsService compteEpsService) {
        this.compteEpsService = compteEpsService;
    }

    // ── GET /api/v1/comptes-epargne ───────────────────────────────────

    @Operation(summary = "Lister tous les comptes épargne")
    @GetMapping
    @PreAuthorize("hasAuthority('PRIV_VIEW_REPORTS')")
    public ResponseEntity<List<CompteEpsDTO.Response>> listAll() {
        return ResponseEntity.ok(compteEpsService.findAll());
    }

    // ── GET /api/v1/comptes-epargne/{numCompte} ───────────────────────

    @Operation(summary = "Obtenir un compte épargne par son numéro")
    @GetMapping("/{numCompte}")
    @PreAuthorize("hasAuthority('PRIV_VIEW_REPORTS')")
    public ResponseEntity<CompteEpsDTO.Response> getOne(@PathVariable String numCompte) {
        return ResponseEntity.ok(compteEpsService.findById(numCompte));
    }

    // ── GET /api/v1/comptes-epargne/membre/{numMembre} ────────────────

    @Operation(summary = "Lister les comptes épargne d'un membre")
    @GetMapping("/membre/{numMembre}")
    @PreAuthorize("hasAuthority('PRIV_VIEW_REPORTS')")
    public ResponseEntity<List<CompteEpsDTO.Response>> getByMembre(
            @PathVariable String numMembre) {
        return ResponseEntity.ok(compteEpsService.findByMembre(numMembre));
    }

    // ── GET /api/v1/comptes-epargne/agence/{codeAgence} ──────────────

    @Operation(summary = "Lister les comptes épargne d'une agence")
    @GetMapping("/agence/{codeAgence}")
    @PreAuthorize("hasAuthority('PRIV_VIEW_REPORTS')")
    public ResponseEntity<List<CompteEpsDTO.Response>> getByAgence(
            @PathVariable String codeAgence) {
        return ResponseEntity.ok(compteEpsService.findByAgence(codeAgence));
    }

    // ── POST /api/v1/comptes-epargne ──────────────────────────────────

    @Operation(summary = "Ouvrir un nouveau compte épargne")
    @PostMapping
    @PreAuthorize("hasAuthority('PRIV_OPEN_COMPTE_EPS')")
    public ResponseEntity<CompteEpsDTO.Response> create(
            @Valid @RequestBody CompteEpsDTO.CreateRequest req) {
        CompteEpsDTO.Response created = compteEpsService.create(req);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{numCompte}")
                .buildAndExpand(created.numCompte())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    // ── PUT /api/v1/comptes-epargne/{numCompte} ───────────────────────

    @Operation(summary = "Mettre à jour un compte épargne")
    @PutMapping("/{numCompte}")
    @PreAuthorize("hasAuthority('PRIV_OPEN_COMPTE_EPS')")
    public ResponseEntity<CompteEpsDTO.Response> update(
            @PathVariable String numCompte,
            @Valid @RequestBody CompteEpsDTO.UpdateRequest req) {
        return ResponseEntity.ok(compteEpsService.update(numCompte, req));
    }

    // ── DELETE /api/v1/comptes-epargne/{numCompte} ────────────────────

    @Operation(summary = "Supprimer un compte épargne")
    @DeleteMapping("/{numCompte}")
    @PreAuthorize("hasAuthority('PRIV_MANAGE_USERS')")
    public ResponseEntity<Void> delete(@PathVariable String numCompte) {
        compteEpsService.delete(numCompte);
        return ResponseEntity.noContent().build();
    }
}
