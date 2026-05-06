package com.pfe.backend.controller;

import com.microfina.entity.ProduitPartSociale;
import com.pfe.backend.exception.ResourceNotFoundException;
import com.pfe.backend.service.ProduitPartSocialeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Parts Sociales", description = "Gestion des produits de parts sociales")
@RestController
@RequestMapping("/api/v1/parts-sociales")
public class ProduitPartSocialeController {

    private final ProduitPartSocialeService service;

    public ProduitPartSocialeController(ProduitPartSocialeService service) {
        this.service = service;
    }

    @Operation(summary = "Liste tous les produits de parts sociales")
    @GetMapping
    @PreAuthorize("hasAnyAuthority('PRIV_VIEW_REPORTS','PRIV_MANAGE_USERS')")
    public List<ProduitPartSociale> findAll() {
        return service.findAll();
    }

    @Operation(summary = "Liste les produits de parts sociales actifs")
    @GetMapping("/actifs")
    @PreAuthorize("hasAnyAuthority('PRIV_VIEW_REPORTS','PRIV_MANAGE_USERS')")
    public List<ProduitPartSociale> findActifs() {
        return service.findActifs();
    }

    @Operation(summary = "Récupère un produit par son code")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PRIV_VIEW_REPORTS','PRIV_MANAGE_USERS')")
    public ResponseEntity<ProduitPartSociale> findById(@PathVariable String id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("ProduitPartSociale", id));
    }

    @Operation(summary = "Crée un nouveau produit de parts sociales")
    @PostMapping
    @PreAuthorize("hasAuthority('PRIV_MANAGE_USERS')")
    public ResponseEntity<ProduitPartSociale> create(@RequestBody ProduitPartSociale produit) {
        return ResponseEntity.status(201).body(service.save(produit));
    }

    @Operation(summary = "Met à jour un produit de parts sociales")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PRIV_MANAGE_USERS')")
    public ResponseEntity<ProduitPartSociale> update(@PathVariable String id,
                                                     @RequestBody ProduitPartSociale req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @Operation(summary = "Supprime un produit de parts sociales")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PRIV_MANAGE_USERS')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        if (!service.delete(id)) throw new ResourceNotFoundException("ProduitPartSociale", id);
        return ResponseEntity.noContent().build();
    }
}
