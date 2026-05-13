package com.pfe.backend.controller;

import com.microfina.entity.LigneBudget;
import com.microfina.entity.TypeLigneBudget;
import com.pfe.backend.service.LigneBudgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

/**
 * LigneBudgetController — gestion des lignes (recettes / dépenses) d'un budget.
 *
 * <pre>
 *   GET    /api/v1/budgets/{budgetId}/lignes        — lister les lignes d'un budget
 *   GET    /api/v1/budgets/lignes/{id}              — détail d'une ligne
 *   POST   /api/v1/budgets/{budgetId}/lignes        — ajouter une ligne (PRIV_MANAGE_BUDGET, budget BROUILLON uniquement)
 *   PUT    /api/v1/budgets/lignes/{id}              — modifier une ligne (idem)
 *   DELETE /api/v1/budgets/lignes/{id}              — supprimer une ligne (idem)
 * </pre>
 */
@Tag(name = "Budget — Lignes", description = "Gestion des lignes budgétaires (recettes / dépenses)")
@RestController
@RequestMapping("/api/v1/budgets")
public class LigneBudgetController {

    private final LigneBudgetService service;

    public LigneBudgetController(LigneBudgetService service) {
        this.service = service;
    }

    // ── DTO ──────────────────────────────────────────────────────────────────

    public record LigneBudgetDto(
            Long id,
            Long budgetId,
            String codeRubrique,
            String libelle,
            TypeLigneBudget typeLigne,
            BigDecimal montantPrevu,
            BigDecimal montantRealise,
            String compte
    ) {
        public static LigneBudgetDto from(LigneBudget l) {
            return new LigneBudgetDto(
                    l.getId(),
                    l.getBudget() != null ? l.getBudget().getId() : null,
                    l.getCodeRubrique(),
                    l.getLibelle(),
                    l.getTypeLigne(),
                    l.getMontantPrevu(),
                    l.getMontantRealise(),
                    l.getCompte()
            );
        }
    }

    public record LigneBudgetWriteRequest(
            String codeRubrique,
            String libelle,
            TypeLigneBudget typeLigne,
            BigDecimal montantPrevu,
            String compte
    ) {}

    // ── Endpoints ───────────────────────────────────────────────────────────

    @Operation(summary = "Lister les lignes d'un budget")
    @GetMapping("/{budgetId}/lignes")
    @PreAuthorize("hasAnyAuthority('PRIV_VIEW_REPORTS','PRIV_MANAGE_BUDGET')")
    public ResponseEntity<List<LigneBudgetDto>> findByBudget(@PathVariable Long budgetId) {
        return ResponseEntity.ok(
            service.findByBudget(budgetId).stream().map(LigneBudgetDto::from).toList()
        );
    }

    @Operation(summary = "Détail d'une ligne budgétaire")
    @GetMapping("/lignes/{id}")
    @PreAuthorize("hasAnyAuthority('PRIV_VIEW_REPORTS','PRIV_MANAGE_BUDGET')")
    public ResponseEntity<LigneBudgetDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(LigneBudgetDto.from(service.findById(id)));
    }

    @Operation(summary = "Ajouter une ligne à un budget BROUILLON")
    @PostMapping("/{budgetId}/lignes")
    @PreAuthorize("hasAuthority('PRIV_MANAGE_BUDGET')")
    public ResponseEntity<LigneBudgetDto> create(@PathVariable Long budgetId,
                                                 @RequestBody LigneBudgetWriteRequest req) {
        LigneBudget created = service.create(
            budgetId,
            req.codeRubrique(),
            req.libelle(),
            req.typeLigne(),
            req.montantPrevu(),
            req.compte()
        );
        return ResponseEntity
            .created(URI.create("/api/v1/budgets/lignes/" + created.getId()))
            .body(LigneBudgetDto.from(created));
    }

    @Operation(summary = "Mettre à jour une ligne (budget BROUILLON uniquement)")
    @PutMapping("/lignes/{id}")
    @PreAuthorize("hasAuthority('PRIV_MANAGE_BUDGET')")
    public ResponseEntity<LigneBudgetDto> update(@PathVariable Long id,
                                                 @RequestBody LigneBudgetWriteRequest req) {
        LigneBudget updated = service.update(
            id,
            req.codeRubrique(),
            req.libelle(),
            req.typeLigne(),
            req.montantPrevu(),
            req.compte()
        );
        return ResponseEntity.ok(LigneBudgetDto.from(updated));
    }

    @Operation(summary = "Supprimer une ligne (budget BROUILLON uniquement)")
    @DeleteMapping("/lignes/{id}")
    @PreAuthorize("hasAuthority('PRIV_MANAGE_BUDGET')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
