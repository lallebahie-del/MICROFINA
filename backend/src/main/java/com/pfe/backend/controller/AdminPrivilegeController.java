package com.pfe.backend.controller;

import com.pfe.backend.dto.PrivilegeDTO;
import com.pfe.backend.service.PrivilegeService;
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

import java.net.URI;
import java.util.List;

/**
 * AdminPrivilegeController — gestion des privilèges fonctionnels granulaires.
 */
@Tag(name = "Admin — Privilèges", description = "CRUD des privilèges fonctionnels")
@RestController
@RequestMapping("/api/v1/admin/privileges")
public class AdminPrivilegeController {

    private final PrivilegeService privilegeService;

    public AdminPrivilegeController(PrivilegeService privilegeService) {
        this.privilegeService = privilegeService;
    }

    @Operation(summary = "Lister tous les privilèges")
    @GetMapping
    @PreAuthorize("hasAuthority('PRIV_MANAGE_USERS')")
    public ResponseEntity<List<PrivilegeDTO.Response>> findAll() {
        return ResponseEntity.ok(privilegeService.findAll());
    }

    @Operation(summary = "Obtenir un privilège par son identifiant")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PRIV_MANAGE_USERS')")
    public ResponseEntity<PrivilegeDTO.Response> findById(@PathVariable Long id) {
        return ResponseEntity.ok(privilegeService.findById(id));
    }

    @Operation(summary = "Lister les privilèges d'un module")
    @GetMapping("/module/{module}")
    @PreAuthorize("hasAuthority('PRIV_MANAGE_USERS')")
    public ResponseEntity<List<PrivilegeDTO.Response>> findByModule(@PathVariable String module) {
        return ResponseEntity.ok(privilegeService.findByModule(module));
    }

    @Operation(summary = "Créer un nouveau privilège")
    @PostMapping
    @PreAuthorize("hasAuthority('PRIV_MANAGE_USERS')")
    public ResponseEntity<PrivilegeDTO.Response> create(
            @Valid @RequestBody PrivilegeDTO.CreateRequest req) {
        PrivilegeDTO.Response created = privilegeService.create(req);
        return ResponseEntity
            .created(URI.create("/api/v1/admin/privileges/" + created.id()))
            .body(created);
    }

    @Operation(summary = "Mettre à jour un privilège")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PRIV_MANAGE_USERS')")
    public ResponseEntity<PrivilegeDTO.Response> update(
            @PathVariable Long id,
            @RequestBody PrivilegeDTO.UpdateRequest req) {
        return ResponseEntity.ok(privilegeService.update(id, req));
    }

    @Operation(summary = "Supprimer un privilège")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PRIV_MANAGE_USERS')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        privilegeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
