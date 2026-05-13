package com.pfe.backend.controller;

import com.microfina.entity.MouvementBudget;
import com.pfe.backend.service.MouvementBudgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;

/**
 * MouvementBudgetController — saisie du réalisé budgétaire (mouvements).
 *
 * <pre>
 *   GET    /api/v1/budgets/lignes/{ligneId}/mouvements   — mouvements d'une ligne
 *   GET    /api/v1/budgets/{budgetId}/mouvements         — mouvements de tout un budget
 *   GET    /api/v1/budgets/mouvements/{id}               — détail d'un mouvement
 *   POST   /api/v1/budgets/lignes/{ligneId}/mouvements   — saisir un mouvement (PRIV_RECORD_MOVEMENT)
 *   DELETE /api/v1/budgets/mouvements/{id}               — supprimer un mouvement (PRIV_MANAGE_BUDGET)
 * </pre>
 */
@Tag(name = "Budget — Mouvements", description = "Saisie et suivi du réalisé budgétaire")
@RestController
@RequestMapping("/api/v1/budgets")
public class MouvementBudgetController {

    private final MouvementBudgetService service;

    public MouvementBudgetController(MouvementBudgetService service) {
        this.service = service;
    }

    // ── DTO ──────────────────────────────────────────────────────────────────

    public record MouvementBudgetDto(
            Long id,
            Long ligneBudgetId,
            Long idComptabilite,
            LocalDate dateMouvement,
            BigDecimal montant,
            String libelle,
            String utilisateur
    ) {
        public static MouvementBudgetDto from(MouvementBudget m) {
            return new MouvementBudgetDto(
                m.getId(),
                m.getLigneBudget()  != null ? m.getLigneBudget().getId() : null,
                m.getComptabilite() != null ? m.getComptabilite().getIdComptabilite() : null,
                m.getDateMouvement(),
                m.getMontant(),
                m.getLibelle(),
                m.getUtilisateur()
            );
        }
    }

    public record MouvementBudgetWriteRequest(
            Long idComptabilite,
            LocalDate dateMouvement,
            BigDecimal montant,
            String libelle
    ) {}

    // ── Endpoints ───────────────────────────────────────────────────────────

    @Operation(summary = "Mouvements d'une ligne budgétaire")
    @GetMapping("/lignes/{ligneId}/mouvements")
    @PreAuthorize("hasAnyAuthority('PRIV_VIEW_REPORTS','PRIV_MANAGE_BUDGET','PRIV_RECORD_MOVEMENT')")
    public ResponseEntity<List<MouvementBudgetDto>> findByLigne(@PathVariable Long ligneId) {
        return ResponseEntity.ok(
            service.findByLigne(ligneId).stream().map(MouvementBudgetDto::from).toList()
        );
    }

    @Operation(summary = "Mouvements de toutes les lignes d'un budget")
    @GetMapping("/{budgetId}/mouvements")
    @PreAuthorize("hasAnyAuthority('PRIV_VIEW_REPORTS','PRIV_MANAGE_BUDGET','PRIV_RECORD_MOVEMENT')")
    public ResponseEntity<List<MouvementBudgetDto>> findByBudget(@PathVariable Long budgetId) {
        return ResponseEntity.ok(
            service.findByBudget(budgetId).stream().map(MouvementBudgetDto::from).toList()
        );
    }

    @Operation(summary = "Détail d'un mouvement")
    @GetMapping("/mouvements/{id}")
    @PreAuthorize("hasAnyAuthority('PRIV_VIEW_REPORTS','PRIV_MANAGE_BUDGET','PRIV_RECORD_MOVEMENT')")
    public ResponseEntity<MouvementBudgetDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(MouvementBudgetDto.from(service.findById(id)));
    }

    @Operation(summary = "Saisir un mouvement budgétaire (lié à une écriture comptable existante)")
    @PostMapping("/lignes/{ligneId}/mouvements")
    @PreAuthorize("hasAnyAuthority('PRIV_RECORD_MOVEMENT','PRIV_MANAGE_BUDGET')")
    public ResponseEntity<MouvementBudgetDto> create(@PathVariable Long ligneId,
                                                     @RequestBody MouvementBudgetWriteRequest req,
                                                     Authentication auth) {
        String utilisateur = auth != null ? auth.getName() : null;
        MouvementBudget created = service.create(
            ligneId,
            req.idComptabilite(),
            req.dateMouvement(),
            req.montant(),
            req.libelle(),
            utilisateur
        );
        return ResponseEntity
            .created(URI.create("/api/v1/budgets/mouvements/" + created.getId()))
            .body(MouvementBudgetDto.from(created));
    }

    @Operation(summary = "Supprimer un mouvement budgétaire")
    @DeleteMapping("/mouvements/{id}")
    @PreAuthorize("hasAuthority('PRIV_MANAGE_BUDGET')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
