package com.pfe.backend.controller;

import com.pfe.backend.dto.OperationBanqueDTO;
import com.pfe.backend.service.OperationBanqueApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * OperationBanqueController – API REST pour les opérations bancaires.
 *
 * <h2>Base URL</h2>
 * <pre>/api/v1/operations-banque</pre>
 */
@Tag(name = "Opérations bancaires", description = "Gestion des opérations bancaires (virements, dépôts, retraits)")
@RestController
@RequestMapping("/api/v1/operations-banque")
public class OperationBanqueController {

    private final OperationBanqueApplicationService service;

    public OperationBanqueController(OperationBanqueApplicationService service) {
        this.service = service;
    }

    // ── GET / ─────────────────────────────────────────────────────────────────

    @Operation(summary = "Lister toutes les opérations bancaires")
    @GetMapping
    @PreAuthorize("hasAuthority('PRIV_VIEW_REPORTS')")
    public ResponseEntity<List<OperationBanqueDTO.Response>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    // ── GET /{id} ─────────────────────────────────────────────────────────────

    @Operation(summary = "Obtenir une opération bancaire par identifiant")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PRIV_VIEW_REPORTS')")
    public ResponseEntity<OperationBanqueDTO.Response> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    // ── GET /agence/{codeAgence} ──────────────────────────────────────────────

    @Operation(summary = "Lister les opérations bancaires d'une agence")
    @GetMapping("/agence/{codeAgence}")
    @PreAuthorize("hasAuthority('PRIV_VIEW_REPORTS')")
    public ResponseEntity<List<OperationBanqueDTO.Response>> findByAgence(
            @PathVariable String codeAgence) {
        return ResponseEntity.ok(service.findByAgence(codeAgence));
    }

    // ── GET /compte-banque/{compteBanqueId} ───────────────────────────────────

    @Operation(summary = "Lister les opérations bancaires d'un compte bancaire")
    @GetMapping("/compte-banque/{compteBanqueId}")
    @PreAuthorize("hasAuthority('PRIV_VIEW_REPORTS')")
    public ResponseEntity<List<OperationBanqueDTO.Response>> findByCompteBanque(
            @PathVariable Long compteBanqueId) {
        return ResponseEntity.ok(service.findByCompteBanque(compteBanqueId));
    }

    // ── POST / ────────────────────────────────────────────────────────────────

    @Operation(summary = "Créer une opération bancaire")
    @PostMapping
    @PreAuthorize("hasAuthority('PRIV_MANAGE_BANK')")
    public ResponseEntity<OperationBanqueDTO.Response> create(
            @Valid @RequestBody OperationBanqueDTO.CreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    // ── PATCH /{id}/valider ───────────────────────────────────────────────────

    @Operation(summary = "Valider une opération bancaire")
    @PatchMapping("/{id}/valider")
    @PreAuthorize("hasAuthority('PRIV_MANAGE_BANK')")
    public ResponseEntity<OperationBanqueDTO.Response> valider(@PathVariable Long id) {
        return ResponseEntity.ok(service.valider(id));
    }

    // ── PATCH /{id}/annuler ───────────────────────────────────────────────────

    @Operation(summary = "Annuler une opération bancaire")
    @PatchMapping("/{id}/annuler")
    @PreAuthorize("hasAuthority('PRIV_MANAGE_BANK')")
    public ResponseEntity<OperationBanqueDTO.Response> annuler(@PathVariable Long id) {
        return ResponseEntity.ok(service.annuler(id));
    }
}
