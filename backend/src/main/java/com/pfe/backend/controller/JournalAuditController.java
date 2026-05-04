package com.pfe.backend.controller;

import com.pfe.backend.dto.JournalAuditDTO;
import com.pfe.backend.service.JournalAuditService;
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
 * JournalAuditController — consultation du journal d'audit (lecture seule).
 */
@Tag(name = "Admin — Journal d'audit", description = "Consultation du journal d'audit système")
@RestController
@RequestMapping("/api/v1/admin/audit")
public class JournalAuditController {

    private final JournalAuditService journalAuditService;

    public JournalAuditController(JournalAuditService journalAuditService) {
        this.journalAuditService = journalAuditService;
    }

    @Operation(summary = "Lister toutes les entrées du journal d'audit")
    @GetMapping
    @PreAuthorize("hasAuthority('PRIV_VIEW_AUDIT')")
    public ResponseEntity<List<JournalAuditDTO.Response>> findAll() {
        return ResponseEntity.ok(journalAuditService.findAll());
    }

    @Operation(summary = "Obtenir une entrée du journal par son identifiant")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PRIV_VIEW_AUDIT')")
    public ResponseEntity<JournalAuditDTO.Response> findById(@PathVariable Long id) {
        return ResponseEntity.ok(journalAuditService.findById(id));
    }

    @Operation(summary = "Lister les entrées du journal d'un utilisateur")
    @GetMapping("/utilisateur/{login}")
    @PreAuthorize("hasAuthority('PRIV_VIEW_AUDIT')")
    public ResponseEntity<List<JournalAuditDTO.Response>> findByUtilisateur(
            @PathVariable String login) {
        return ResponseEntity.ok(journalAuditService.findByUtilisateur(login));
    }

    @Operation(summary = "Lister les entrées du journal pour une entité")
    @GetMapping("/entite/{entite}")
    @PreAuthorize("hasAuthority('PRIV_VIEW_AUDIT')")
    public ResponseEntity<List<JournalAuditDTO.Response>> findByEntite(
            @PathVariable String entite) {
        return ResponseEntity.ok(journalAuditService.findByEntite(entite));
    }

    @Operation(summary = "Lister les entrées du journal par type d'action")
    @GetMapping("/action/{action}")
    @PreAuthorize("hasAuthority('PRIV_VIEW_AUDIT')")
    public ResponseEntity<List<JournalAuditDTO.Response>> findByAction(
            @PathVariable String action) {
        return ResponseEntity.ok(journalAuditService.findByAction(action));
    }
}
