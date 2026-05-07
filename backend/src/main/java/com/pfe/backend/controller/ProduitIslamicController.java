package com.pfe.backend.controller;

import com.microfina.entity.ProduitIslamic;
import com.pfe.backend.service.ProduitIslamicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * ProduitIslamicController — référentiel des produits de finance islamique
 * (Mourabaha, Moucharaka, Ijara) utilisés par les contrats {@link com.microfina.entity.ProduitCredit}.
 */
@Tag(name = "Produits islamiques", description = "Référentiel des produits de finance islamique")
@RestController
@RequestMapping("/api/v1/produits-islamic")
public class ProduitIslamicController {

    private final ProduitIslamicService service;

    public ProduitIslamicController(ProduitIslamicService service) {
        this.service = service;
    }

    @Operation(summary = "Liste tous les produits islamiques")
    @GetMapping
    @PreAuthorize("hasAnyAuthority('PRIV_VIEW_REPORTS','PRIV_MANAGE_PARAMS')")
    public List<ProduitIslamic> findAll() {
        return service.findAll();
    }

    @Operation(summary = "Liste les produits islamiques actifs")
    @GetMapping("/actifs")
    @PreAuthorize("hasAnyAuthority('PRIV_VIEW_REPORTS','PRIV_MANAGE_PARAMS')")
    public List<ProduitIslamic> findActifs() {
        return service.findActifs();
    }

    @Operation(summary = "Récupère un produit islamique par son code")
    @GetMapping("/{code}")
    @PreAuthorize("hasAnyAuthority('PRIV_VIEW_REPORTS','PRIV_MANAGE_PARAMS')")
    public ResponseEntity<ProduitIslamic> findById(@PathVariable String code) {
        return ResponseEntity.ok(service.findById(code));
    }

    @Operation(summary = "Crée un produit islamique")
    @PostMapping
    @PreAuthorize("hasAuthority('PRIV_MANAGE_PARAMS')")
    public ResponseEntity<ProduitIslamic> create(@RequestBody ProduitIslamic input) {
        ProduitIslamic created = service.create(input);
        return ResponseEntity
            .created(URI.create("/api/v1/produits-islamic/" + created.getCodeProduit()))
            .body(created);
    }

    @Operation(summary = "Met à jour un produit islamique")
    @PutMapping("/{code}")
    @PreAuthorize("hasAuthority('PRIV_MANAGE_PARAMS')")
    public ResponseEntity<ProduitIslamic> update(@PathVariable String code,
                                                 @RequestBody ProduitIslamic input) {
        return ResponseEntity.ok(service.update(code, input));
    }

    @Operation(summary = "Supprime un produit islamique")
    @DeleteMapping("/{code}")
    @PreAuthorize("hasAuthority('PRIV_MANAGE_PARAMS')")
    public ResponseEntity<Void> delete(@PathVariable String code) {
        service.delete(code);
        return ResponseEntity.noContent().build();
    }
}
