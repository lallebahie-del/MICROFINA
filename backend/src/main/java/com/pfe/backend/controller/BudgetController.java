package com.pfe.backend.controller;

import com.pfe.backend.dto.BudgetDTO;
import com.pfe.backend.service.BudgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * BudgetController – API REST pour la gestion budgétaire.
 *
 * <h2>Base URL</h2>
 * <pre>/api/v1/budgets</pre>
 */
@Tag(name = "Budgets", description = "Gestion des budgets annuels par agence")
@RestController
@RequestMapping("/api/v1/budgets")
public class BudgetController {

    private final BudgetService service;

    public BudgetController(BudgetService service) {
        this.service = service;
    }

    // ── GET / ─────────────────────────────────────────────────────────────────

    @Operation(summary = "Lister tous les budgets")
    @GetMapping
    @PreAuthorize("hasAuthority('PRIV_VIEW_REPORTS')")
    public ResponseEntity<List<BudgetDTO.Response>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    // ── GET /{id} ─────────────────────────────────────────────────────────────

    @Operation(summary = "Obtenir un budget par identifiant")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PRIV_VIEW_REPORTS')")
    public ResponseEntity<BudgetDTO.Response> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    // ── GET /exercice/{exercice} ──────────────────────────────────────────────

    @Operation(summary = "Lister les budgets d'un exercice fiscal")
    @GetMapping("/exercice/{exercice}")
    @PreAuthorize("hasAuthority('PRIV_VIEW_REPORTS')")
    public ResponseEntity<List<BudgetDTO.Response>> findByExercice(@PathVariable Integer exercice) {
        return ResponseEntity.ok(service.findByExercice(exercice));
    }

    // ── GET /agence/{codeAgence} ──────────────────────────────────────────────

    @Operation(summary = "Lister les budgets d'une agence")
    @GetMapping("/agence/{codeAgence}")
    @PreAuthorize("hasAuthority('PRIV_VIEW_REPORTS')")
    public ResponseEntity<List<BudgetDTO.Response>> findByAgence(@PathVariable String codeAgence) {
        return ResponseEntity.ok(service.findByAgence(codeAgence));
    }

    // ── POST / ────────────────────────────────────────────────────────────────

    @Operation(summary = "Créer un budget")
    @PostMapping
    @PreAuthorize("hasAuthority('PRIV_MANAGE_BUDGET')")
    public ResponseEntity<BudgetDTO.Response> create(
            @Valid @RequestBody BudgetDTO.CreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    // ── PUT /{id} ─────────────────────────────────────────────────────────────

    @Operation(summary = "Mettre à jour un budget")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PRIV_MANAGE_BUDGET')")
    public ResponseEntity<BudgetDTO.Response> update(
            @PathVariable Long id,
            @Valid @RequestBody BudgetDTO.UpdateRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    // ── PATCH /{id}/valider ───────────────────────────────────────────────────

    @Operation(summary = "Valider un budget (BROUILLON → VALIDE)")
    @PatchMapping("/{id}/valider")
    @PreAuthorize("hasAuthority('PRIV_MANAGE_BUDGET')")
    public ResponseEntity<BudgetDTO.Response> valider(@PathVariable Long id) {
        return ResponseEntity.ok(service.valider(id));
    }

    // ── PATCH /{id}/cloturer ──────────────────────────────────────────────────

    @Operation(summary = "Clôturer un budget (VALIDE → CLOTURE)")
    @PatchMapping("/{id}/cloturer")
    @PreAuthorize("hasAuthority('PRIV_MANAGE_BUDGET')")
    public ResponseEntity<BudgetDTO.Response> cloturer(@PathVariable Long id) {
        return ResponseEntity.ok(service.cloturer(id));
    }
}
