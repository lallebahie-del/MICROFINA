package com.pfe.backend.controller;

import com.pfe.backend.dto.BanqueDTO;
import com.pfe.backend.service.BanqueService;
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
 * BanqueController — API REST pour la gestion du référentiel bancaire.
 *
 * <h2>Base URL</h2>
 * <pre>/api/v1/banques</pre>
 */
@Tag(name = "Banques", description = "Référentiel des banques partenaires")
@RestController
@RequestMapping("/api/v1/banques")
public class BanqueController {

    private final BanqueService banqueService;

    public BanqueController(BanqueService banqueService) {
        this.banqueService = banqueService;
    }

    // ── GET /api/v1/banques ───────────────────────────────────────────

    @Operation(summary = "Lister toutes les banques")
    @GetMapping
    @PreAuthorize("hasAuthority('PRIV_VIEW_REPORTS')")
    public ResponseEntity<List<BanqueDTO.Response>> listAll() {
        return ResponseEntity.ok(banqueService.findAll());
    }

    // ── GET /api/v1/banques/actives ───────────────────────────────────

    @Operation(summary = "Lister les banques actives")
    @GetMapping("/actives")
    @PreAuthorize("hasAuthority('PRIV_VIEW_REPORTS')")
    public ResponseEntity<List<BanqueDTO.Response>> listActives() {
        return ResponseEntity.ok(banqueService.findActives());
    }

    // ── GET /api/v1/banques/{codeBanque} ──────────────────────────────

    @Operation(summary = "Obtenir une banque par son code")
    @GetMapping("/{codeBanque}")
    @PreAuthorize("hasAuthority('PRIV_VIEW_REPORTS')")
    public ResponseEntity<BanqueDTO.Response> getOne(@PathVariable String codeBanque) {
        return ResponseEntity.ok(banqueService.findById(codeBanque));
    }

    // ── POST /api/v1/banques ──────────────────────────────────────────

    @Operation(summary = "Créer une nouvelle banque")
    @PostMapping
    @PreAuthorize("hasAuthority('PRIV_MANAGE_BANK')")
    public ResponseEntity<BanqueDTO.Response> create(
            @Valid @RequestBody BanqueDTO.CreateRequest req) {
        BanqueDTO.Response created = banqueService.create(req);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{codeBanque}")
                .buildAndExpand(created.codeBanque())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    // ── PUT /api/v1/banques/{codeBanque} ──────────────────────────────

    @Operation(summary = "Mettre à jour une banque")
    @PutMapping("/{codeBanque}")
    @PreAuthorize("hasAuthority('PRIV_MANAGE_BANK')")
    public ResponseEntity<BanqueDTO.Response> update(
            @PathVariable String codeBanque,
            @Valid @RequestBody BanqueDTO.UpdateRequest req) {
        return ResponseEntity.ok(banqueService.update(codeBanque, req));
    }

    // ── DELETE /api/v1/banques/{codeBanque} ───────────────────────────

    @Operation(summary = "Supprimer une banque")
    @DeleteMapping("/{codeBanque}")
    @PreAuthorize("hasAuthority('PRIV_MANAGE_BANK')")
    public ResponseEntity<Void> delete(@PathVariable String codeBanque) {
        banqueService.delete(codeBanque);
        return ResponseEntity.noContent().build();
    }
}
