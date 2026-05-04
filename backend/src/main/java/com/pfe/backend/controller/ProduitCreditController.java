package com.pfe.backend.controller;

import com.pfe.backend.dto.ProduitCreditDTO;
import com.pfe.backend.service.ProduitCreditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * ProduitCreditController – REST API for credit product configuration.
 *
 * <h2>Base URL</h2>
 * <pre>/api/v1/produits-credit</pre>
 *
 * <h2>Endpoints</h2>
 * <pre>
 *   GET    /api/v1/produits-credit            – paginated search           PRIV_VIEW_REPORTS
 *   GET    /api/v1/produits-credit/actifs     – active products (dropdown) PRIV_VIEW_REPORTS
 *   GET    /api/v1/produits-credit/{id}       – single product             PRIV_VIEW_REPORTS
 *   POST   /api/v1/produits-credit            – create product             PRIV_MANAGE_USERS
 *   PUT    /api/v1/produits-credit/{id}       – update product             PRIV_MANAGE_USERS
 *   DELETE /api/v1/produits-credit/{id}       – delete product             PRIV_MANAGE_USERS
 * </pre>
 */
@Tag(name = "Produits crédit", description = "Paramétrage des produits de crédit")
@RestController
@RequestMapping("/api/v1/produits-credit")
public class ProduitCreditController {

    private final ProduitCreditService service;

    public ProduitCreditController(ProduitCreditService service) {
        this.service = service;
    }

    @Operation(summary = "Rechercher les produits de crédit (paginé)")
    @GetMapping
    @PreAuthorize("hasAuthority('PRIV_VIEW_REPORTS')")
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(defaultValue = "")   String  search,
            @RequestParam(required = false)    Integer actif,
            @RequestParam(defaultValue = "0")  int     page,
            @RequestParam(defaultValue = "20") int     size
    ) {
        return ResponseEntity.ok(service.search(search, actif, page, size));
    }

    @Operation(summary = "Lister les produits actifs (pour les menus déroulants)")
    @GetMapping("/actifs")
    @PreAuthorize("hasAuthority('PRIV_VIEW_REPORTS')")
    public ResponseEntity<List<ProduitCreditDTO>> listActifs() {
        return ResponseEntity.ok(service.listActifs());
    }

    @Operation(summary = "Obtenir un produit de crédit par code")
    @GetMapping("/{numProduit}")
    @PreAuthorize("hasAuthority('PRIV_VIEW_REPORTS')")
    public ResponseEntity<ProduitCreditDTO> getOne(@PathVariable String numProduit) {
        return service.findById(numProduit)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Créer un produit de crédit")
    @PostMapping
    @PreAuthorize("hasAuthority('PRIV_MANAGE_USERS')")
    public ResponseEntity<ProduitCreditDTO> create(
            @Valid @RequestBody ProduitCreditDTO.CreateRequest req) {
        return ResponseEntity.status(201).body(service.create(req));
    }

    @Operation(summary = "Modifier un produit de crédit")
    @PutMapping("/{numProduit}")
    @PreAuthorize("hasAuthority('PRIV_MANAGE_USERS')")
    public ResponseEntity<ProduitCreditDTO> update(
            @PathVariable String numProduit,
            @Valid @RequestBody ProduitCreditDTO.UpdateRequest req) {
        return ResponseEntity.ok(service.update(numProduit, req));
    }

    @Operation(summary = "Supprimer un produit de crédit")
    @DeleteMapping("/{numProduit}")
    @PreAuthorize("hasAuthority('PRIV_MANAGE_USERS')")
    public ResponseEntity<Void> delete(@PathVariable String numProduit) {
        return service.delete(numProduit)
            ? ResponseEntity.noContent().build()
            : ResponseEntity.notFound().build();
    }
}
