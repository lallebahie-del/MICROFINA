package com.pfe.backend.controller;

import com.pfe.backend.dto.OperationCaisseDTO;
import com.pfe.backend.service.OperationCaisseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * OperationCaisseController – API REST pour les opérations de caisse.
 *
 * <h2>Base URL</h2>
 * <pre>/api/v1/operations-caisse</pre>
 */
@Tag(name = "Opérations de caisse", description = "Gestion des opérations de caisse (dépôts, retraits, frais)")
@RestController
@RequestMapping("/api/v1/operations-caisse")
public class OperationCaisseController {

    private final OperationCaisseService service;

    public OperationCaisseController(OperationCaisseService service) {
        this.service = service;
    }

    // ── GET / ─────────────────────────────────────────────────────────────────

    @Operation(summary = "Lister toutes les opérations de caisse")
    @GetMapping
    @PreAuthorize("hasAnyAuthority('PRIV_VIEW_REPORTS','PRIV_MANAGE_CASH')")
    public ResponseEntity<List<OperationCaisseDTO.Response>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    // ── GET /{id} ─────────────────────────────────────────────────────────────

    @Operation(summary = "Obtenir une opération de caisse par identifiant")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PRIV_VIEW_REPORTS','PRIV_MANAGE_CASH')")
    public ResponseEntity<OperationCaisseDTO.Response> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    // ── GET /agence/{codeAgence} ──────────────────────────────────────────────

    @Operation(summary = "Lister les opérations de caisse d'une agence")
    @GetMapping("/agence/{codeAgence}")
    @PreAuthorize("hasAnyAuthority('PRIV_VIEW_REPORTS','PRIV_MANAGE_CASH')")
    public ResponseEntity<List<OperationCaisseDTO.Response>> findByAgence(
            @PathVariable String codeAgence) {
        return ResponseEntity.ok(service.findByAgence(codeAgence));
    }

    // ── GET /compte/{numCompte} ───────────────────────────────────────────────

    @Operation(summary = "Lister les opérations de caisse d'un compte épargne")
    @GetMapping("/compte/{numCompte}")
    @PreAuthorize("hasAnyAuthority('PRIV_VIEW_REPORTS','PRIV_MANAGE_CASH')")
    public ResponseEntity<List<OperationCaisseDTO.Response>> findByCompte(
            @PathVariable String numCompte) {
        return ResponseEntity.ok(service.findByCompte(numCompte));
    }

    // ── POST / ────────────────────────────────────────────────────────────────

    @Operation(summary = "Créer une opération de caisse")
    @PostMapping
    @PreAuthorize("hasAuthority('PRIV_MANAGE_CASH')")
    public ResponseEntity<OperationCaisseDTO.Response> create(
            @Valid @RequestBody OperationCaisseDTO.CreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    // ── PATCH /{id}/valider ───────────────────────────────────────────────────

    @Operation(summary = "Valider une opération de caisse")
    @PatchMapping("/{id}/valider")
    @PreAuthorize("hasAuthority('PRIV_MANAGE_CASH')")
    public ResponseEntity<OperationCaisseDTO.Response> valider(@PathVariable Long id) {
        return ResponseEntity.ok(service.valider(id));
    }

    // ── PATCH /{id}/annuler ───────────────────────────────────────────────────

    @Operation(summary = "Annuler une opération de caisse")
    @PatchMapping("/{id}/annuler")
    @PreAuthorize("hasAuthority('PRIV_MANAGE_CASH')")
    public ResponseEntity<OperationCaisseDTO.Response> annuler(@PathVariable Long id) {
        return ResponseEntity.ok(service.annuler(id));
    }
}
