package com.pfe.backend.controller;

import com.pfe.backend.dto.CarnetChequeDTO;
import com.pfe.backend.service.CarnetChequeService;
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
 * CarnetChequeController — API REST pour la gestion des carnets de chèques.
 *
 * <h2>Base URL</h2>
 * <pre>/api/v1/carnets-cheque</pre>
 */
@Tag(name = "Carnets de Chèque", description = "Gestion des carnets de chèques remis aux membres")
@RestController
@RequestMapping("/api/v1/carnets-cheque")
public class CarnetChequeController {

    private final CarnetChequeService carnetChequeService;

    public CarnetChequeController(CarnetChequeService carnetChequeService) {
        this.carnetChequeService = carnetChequeService;
    }

    // ── GET /api/v1/carnets-cheque ────────────────────────────────────

    @Operation(summary = "Lister tous les carnets de chèques")
    @GetMapping
    @PreAuthorize("hasAuthority('PRIV_VIEW_REPORTS')")
    public ResponseEntity<List<CarnetChequeDTO.Response>> listAll() {
        return ResponseEntity.ok(carnetChequeService.findAll());
    }

    // ── GET /api/v1/carnets-cheque/{id} ───────────────────────────────

    @Operation(summary = "Obtenir un carnet de chèques par son identifiant")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PRIV_VIEW_REPORTS')")
    public ResponseEntity<CarnetChequeDTO.Response> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(carnetChequeService.findById(id));
    }

    // ── GET /api/v1/carnets-cheque/membre/{numMembre} ─────────────────

    @Operation(summary = "Lister les carnets de chèques d'un membre")
    @GetMapping("/membre/{numMembre}")
    @PreAuthorize("hasAuthority('PRIV_VIEW_REPORTS')")
    public ResponseEntity<List<CarnetChequeDTO.Response>> getByMembre(
            @PathVariable String numMembre) {
        return ResponseEntity.ok(carnetChequeService.findByMembre(numMembre));
    }

    // ── POST /api/v1/carnets-cheque ───────────────────────────────────

    @Operation(summary = "Créer un nouveau carnet de chèques")
    @PostMapping
    @PreAuthorize("hasAuthority('PRIV_MANAGE_BANK')")
    public ResponseEntity<CarnetChequeDTO.Response> create(
            @Valid @RequestBody CarnetChequeDTO.CreateRequest req) {
        CarnetChequeDTO.Response created = carnetChequeService.create(req);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    // ── PUT /api/v1/carnets-cheque/{id} ───────────────────────────────

    @Operation(summary = "Mettre à jour un carnet de chèques")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PRIV_MANAGE_BANK')")
    public ResponseEntity<CarnetChequeDTO.Response> update(
            @PathVariable Long id,
            @Valid @RequestBody CarnetChequeDTO.UpdateRequest req) {
        return ResponseEntity.ok(carnetChequeService.update(id, req));
    }

    // ── DELETE /api/v1/carnets-cheque/{id} ────────────────────────────

    @Operation(summary = "Supprimer un carnet de chèques")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PRIV_MANAGE_BANK')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        carnetChequeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
