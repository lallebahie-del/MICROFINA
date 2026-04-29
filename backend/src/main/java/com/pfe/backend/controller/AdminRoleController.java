package com.pfe.backend.controller;

import com.pfe.backend.dto.RoleDTO;
import com.pfe.backend.service.RoleService;
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
 * AdminRoleController — gestion des rôles fonctionnels.
 */
@Tag(name = "Admin — Rôles", description = "CRUD des rôles fonctionnels")
@RestController
@RequestMapping("/api/v1/admin/roles")
public class AdminRoleController {

    private final RoleService roleService;

    public AdminRoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @Operation(summary = "Lister tous les rôles")
    @GetMapping
    @PreAuthorize("hasAuthority('PRIV_MANAGE_USERS')")
    public ResponseEntity<List<RoleDTO.Response>> findAll() {
        return ResponseEntity.ok(roleService.findAll());
    }

    @Operation(summary = "Obtenir un rôle par son identifiant")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PRIV_MANAGE_USERS')")
    public ResponseEntity<RoleDTO.Response> findById(@PathVariable Long id) {
        return ResponseEntity.ok(roleService.findById(id));
    }

    @Operation(summary = "Obtenir un rôle par son code")
    @GetMapping("/code/{codeRole}")
    @PreAuthorize("hasAuthority('PRIV_MANAGE_USERS')")
    public ResponseEntity<RoleDTO.Response> findByCode(@PathVariable String codeRole) {
        return ResponseEntity.ok(roleService.findByCode(codeRole));
    }

    @Operation(summary = "Créer un nouveau rôle")
    @PostMapping
    @PreAuthorize("hasAuthority('PRIV_MANAGE_USERS')")
    public ResponseEntity<RoleDTO.Response> create(@Valid @RequestBody RoleDTO.CreateRequest req) {
        RoleDTO.Response created = roleService.create(req);
        return ResponseEntity
            .created(URI.create("/api/v1/admin/roles/" + created.id()))
            .body(created);
    }

    @Operation(summary = "Mettre à jour un rôle")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PRIV_MANAGE_USERS')")
    public ResponseEntity<RoleDTO.Response> update(
            @PathVariable Long id,
            @RequestBody RoleDTO.UpdateRequest req) {
        return ResponseEntity.ok(roleService.update(id, req));
    }

    @Operation(summary = "Supprimer un rôle")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PRIV_MANAGE_USERS')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
