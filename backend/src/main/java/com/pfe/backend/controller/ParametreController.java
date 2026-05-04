package com.pfe.backend.controller;

import com.pfe.backend.dto.ParametreDTO;
import com.pfe.backend.service.ParametreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/**
 * ParametreController — gestion des paramètres de configuration par agence.
 */
@Tag(name = "Admin — Paramètres", description = "Gestion des paramètres de configuration par agence")
@RestController
@RequestMapping("/api/v1/admin/parametres")
public class ParametreController {

    private final ParametreService parametreService;

    public ParametreController(ParametreService parametreService) {
        this.parametreService = parametreService;
    }

    @Operation(summary = "Lister tous les paramètres")
    @GetMapping
    @PreAuthorize("hasAuthority('PRIV_MANAGE_PARAMS')")
    public ResponseEntity<List<ParametreDTO.Response>> findAll() {
        return ResponseEntity.ok(parametreService.findAll());
    }

    @Operation(summary = "Obtenir un paramètre par son identifiant")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PRIV_MANAGE_PARAMS')")
    public ResponseEntity<ParametreDTO.Response> findById(@PathVariable Long id) {
        return ResponseEntity.ok(parametreService.findById(id));
    }

    @Operation(summary = "Obtenir le paramètre d'une agence")
    @GetMapping("/agence/{codeAgence}")
    @PreAuthorize("hasAuthority('PRIV_MANAGE_PARAMS')")
    public ResponseEntity<ParametreDTO.Response> findByAgence(@PathVariable String codeAgence) {
        return ResponseEntity.ok(parametreService.findByAgence(codeAgence));
    }

    @Operation(summary = "Créer un nouveau paramètre")
    @PostMapping
    @PreAuthorize("hasAuthority('PRIV_MANAGE_PARAMS')")
    public ResponseEntity<ParametreDTO.Response> create(
            @RequestBody ParametreDTO.CreateRequest req) {
        ParametreDTO.Response created = parametreService.create(req);
        return ResponseEntity
            .created(URI.create("/api/v1/admin/parametres/" + created.idParametre()))
            .body(created);
    }

    @Operation(summary = "Mettre à jour un paramètre")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PRIV_MANAGE_PARAMS')")
    public ResponseEntity<ParametreDTO.Response> update(
            @PathVariable Long id,
            @RequestBody ParametreDTO.UpdateRequest req) {
        return ResponseEntity.ok(parametreService.update(id, req));
    }
}
