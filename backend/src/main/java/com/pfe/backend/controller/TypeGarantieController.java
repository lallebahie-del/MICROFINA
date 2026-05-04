package com.pfe.backend.controller;

import com.microfina.entity.TypeGarantie;
import com.pfe.backend.repository.TypeGarantieRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * TypeGarantieController — référentiel des types de garantie en lecture seule.
 *
 * <pre>
 *   GET /api/v1/referentiel/types-garantie        — liste des types actifs (pour les dropdowns)
 *   GET /api/v1/referentiel/types-garantie/{code} — détail d'un type
 * </pre>
 *
 * <p>Aucune écriture exposée : le référentiel est géré par Liquibase (P10-001b).</p>
 */
@Tag(name = "Référentiel — Types de garantie", description = "Liste des types de garantie acceptés")
@RestController
@RequestMapping("/api/v1/referentiel/types-garantie")
@PreAuthorize("hasAuthority('PRIV_VIEW_REPORTS')")
public class TypeGarantieController {

    private final TypeGarantieRepository repo;

    public TypeGarantieController(TypeGarantieRepository repo) {
        this.repo = repo;
    }

    /**
     * Retourne tous les types de garantie actifs triés par libellé.
     * Utilisé pour alimenter les dropdowns du formulaire de saisie de garantie.
     */
    @Operation(summary = "Lister les types de garantie actifs")
    @GetMapping
    public ResponseEntity<List<TypeGarantie>> listActifs() {
        return ResponseEntity.ok(repo.findAllActifsOrdonnes());
    }

    /**
     * Retourne le détail d'un type de garantie par son code.
     */
    @Operation(summary = "Obtenir un type de garantie par code")
    @GetMapping("/{code}")
    public ResponseEntity<TypeGarantie> getByCode(@PathVariable String code) {
        return repo.findById(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
