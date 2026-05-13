package com.pfe.backend.controller;

import com.microfina.entity.TypeGarantie;
import com.pfe.backend.exception.ResourceNotFoundException;
import com.pfe.backend.repository.TypeGarantieRepository;
import com.pfe.backend.service.TypeGarantieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * TypeGarantieController — référentiel des types de garantie (lecture + administration).
 *
 * <pre>
 *   GET    /api/v1/referentiel/types-garantie        — liste actifs (dropdowns)
 *   GET    /api/v1/referentiel/types-garantie/all    — liste complète (admin)
 *   GET    /api/v1/referentiel/types-garantie/{code} — détail
 *   POST   /api/v1/referentiel/types-garantie        — création (PRIV_MANAGE_PARAMS)
 *   PUT    /api/v1/referentiel/types-garantie/{code} — mise à jour (PRIV_MANAGE_PARAMS)
 *   DELETE /api/v1/referentiel/types-garantie/{code} — suppression (PRIV_MANAGE_PARAMS)
 * </pre>
 */
@Tag(name = "Référentiel — Types de garantie", description = "Liste des types de garantie acceptés")
@RestController
@RequestMapping("/api/v1/referentiel/types-garantie")
public class TypeGarantieController {

    private final TypeGarantieRepository repo;
    private final TypeGarantieService service;

    public TypeGarantieController(TypeGarantieRepository repo, TypeGarantieService service) {
        this.repo = repo;
        this.service = service;
    }

    @Operation(summary = "Lister les types de garantie actifs")
    @GetMapping
    @PreAuthorize("hasAnyAuthority('PRIV_VIEW_REPORTS','PRIV_MANAGE_PARAMS')")
    public ResponseEntity<List<TypeGarantie>> listActifs() {
        return ResponseEntity.ok(repo.findAllActifsOrdonnes());
    }

    @Operation(summary = "Lister tous les types de garantie (actifs + archivés)")
    @GetMapping("/all")
    @PreAuthorize("hasAuthority('PRIV_MANAGE_PARAMS')")
    public ResponseEntity<List<TypeGarantie>> listAll() {
        return ResponseEntity.ok(repo.findAll());
    }

    @Operation(summary = "Obtenir un type de garantie par code")
    @GetMapping("/{code}")
    @PreAuthorize("hasAnyAuthority('PRIV_VIEW_REPORTS','PRIV_MANAGE_PARAMS')")
    public ResponseEntity<TypeGarantie> getByCode(@PathVariable String code) {
        return repo.findById(code)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("TypeGarantie", code));
    }

    @Operation(summary = "Créer un type de garantie")
    @PostMapping
    @PreAuthorize("hasAuthority('PRIV_MANAGE_PARAMS')")
    public ResponseEntity<TypeGarantie> create(@RequestBody TypeGarantie input) {
        TypeGarantie created = service.create(input);
        return ResponseEntity
            .created(URI.create("/api/v1/referentiel/types-garantie/" + created.getCode()))
            .body(created);
    }

    @Operation(summary = "Mettre à jour un type de garantie")
    @PutMapping("/{code}")
    @PreAuthorize("hasAuthority('PRIV_MANAGE_PARAMS')")
    public ResponseEntity<TypeGarantie> update(@PathVariable String code,
                                               @RequestBody TypeGarantie input) {
        return ResponseEntity.ok(service.update(code, input));
    }

    @Operation(summary = "Supprimer un type de garantie")
    @DeleteMapping("/{code}")
    @PreAuthorize("hasAuthority('PRIV_MANAGE_PARAMS')")
    public ResponseEntity<Void> delete(@PathVariable String code) {
        service.delete(code);
        return ResponseEntity.noContent().build();
    }
}
