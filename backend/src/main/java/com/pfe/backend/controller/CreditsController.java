package com.pfe.backend.controller;

import com.pfe.backend.dto.CreditDTO;
import com.pfe.backend.service.CreditsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * CreditsController – REST API for loan management.
 *
 * <h2>Base URL</h2>
 * <pre>/api/v1/credits</pre>
 *
 * <h2>Endpoints</h2>
 * <pre>
 *   GET    /api/v1/credits                    – paginated search           PRIV_VIEW_REPORTS
 *   GET    /api/v1/credits/{id}               – single credit              PRIV_VIEW_REPORTS
 *   POST   /api/v1/credits                    – create (BROUILLON)         PRIV_CREATE_CREDIT
 *   PUT    /api/v1/credits/{id}               – update (BROUILLON only)    PRIV_CREATE_CREDIT
 *   POST   /api/v1/credits/{id}/transitionner – lifecycle move             PRIV_VALIDATE_CREDIT | PRIV_DISBURSE_CREDIT | PRIV_REJECT_CREDIT
 *   DELETE /api/v1/credits/{id}               – delete (BROUILLON only)    PRIV_CREATE_CREDIT
 * </pre>
 */
@Tag(name = "Crédits", description = "Gestion du portefeuille crédit")
@RestController
@RequestMapping("/api/v1/credits")
public class CreditsController {

    private final CreditsService service;

    public CreditsController(CreditsService service) {
        this.service = service;
    }

    @Operation(summary = "Rechercher les crédits (paginé)")
    @GetMapping
    @PreAuthorize("hasAuthority('PRIV_VIEW_REPORTS')")
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(defaultValue = "")   String search,
            @RequestParam(defaultValue = "")   String statut,
            @RequestParam(defaultValue = "")   String numMembre,
            @RequestParam(defaultValue = "0")  int    page,
            @RequestParam(defaultValue = "20") int    size
    ) {
        return ResponseEntity.ok(service.search(search, statut, numMembre, page, size));
    }

    @Operation(summary = "Obtenir un crédit par ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PRIV_VIEW_REPORTS')")
    public ResponseEntity<CreditDTO> getOne(@PathVariable Long id) {
        return service.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Créer un nouveau crédit (statut BROUILLON)")
    @PostMapping
    @PreAuthorize("hasAuthority('PRIV_CREATE_CREDIT')")
    public ResponseEntity<CreditDTO> create(@Valid @RequestBody CreditDTO.CreateRequest req) {
        return ResponseEntity.status(201).body(service.create(req));
    }

    @Operation(summary = "Modifier un crédit (BROUILLON uniquement)")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PRIV_CREATE_CREDIT')")
    public ResponseEntity<CreditDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody CreditDTO.UpdateRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    /**
     * Cycle de vie — corps : { "statut": "SOUMIS" }
     * Transitions autorisées :
     *   BROUILLON → SOUMIS               (PRIV_CREATE_CREDIT)
     *   SOUMIS → VALIDE_AGENT            (PRIV_VALIDATE_CREDIT)
     *   VALIDE_AGENT → VALIDE_COMITE     (PRIV_VALIDATE_CREDIT)
     *   VALIDE_COMITE → DEBLOQUE         (PRIV_DISBURSE_CREDIT)
     *   Tout → REJETE                    (PRIV_REJECT_CREDIT)
     */
    @Operation(summary = "Effectuer une transition de statut")
    @PostMapping("/{id}/transitionner")
    @PreAuthorize("hasAnyAuthority('PRIV_CREATE_CREDIT','PRIV_VALIDATE_CREDIT','PRIV_DISBURSE_CREDIT','PRIV_REJECT_CREDIT')")
    public ResponseEntity<CreditDTO> transitionner(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String statut = body.get("statut");
        if (statut == null || statut.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(service.transitionner(id, statut));
    }

    @Operation(summary = "Supprimer un crédit (BROUILLON uniquement)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PRIV_CREATE_CREDIT')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return service.delete(id)
            ? ResponseEntity.noContent().build()
            : ResponseEntity.notFound().build();
    }
}
