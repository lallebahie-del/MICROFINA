package com.pfe.backend.controller;

import com.pfe.backend.dto.TypeMembreDTO;
import com.pfe.backend.service.TypeMembreService;
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
 * TypeMembreController — consultation des types membres éligibles aux produits crédit.
 */
@Tag(name = "Types Membre", description = "Consultation des types membres éligibles aux produits crédit")
@RestController
@RequestMapping("/api/v1/types-membre")
public class TypeMembreController {

    private final TypeMembreService typeMembreService;

    public TypeMembreController(TypeMembreService typeMembreService) {
        this.typeMembreService = typeMembreService;
    }

    @Operation(summary = "Lister tous les types membres distincts")
    @GetMapping
    @PreAuthorize("hasAuthority('PRIV_VIEW_REPORTS')")
    public ResponseEntity<List<String>> findAll() {
        return ResponseEntity.ok(typeMembreService.findAll());
    }

    @Operation(summary = "Lister les produits éligibles pour un type membre")
    @GetMapping("/{typeMembre}")
    @PreAuthorize("hasAuthority('PRIV_VIEW_REPORTS')")
    public ResponseEntity<List<TypeMembreDTO.Response>> findByType(
            @PathVariable String typeMembre) {
        return ResponseEntity.ok(typeMembreService.findByType(typeMembre));
    }
}
