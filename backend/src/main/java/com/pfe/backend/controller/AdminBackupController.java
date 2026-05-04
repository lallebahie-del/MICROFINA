package com.pfe.backend.controller;

import com.pfe.backend.service.BackupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Admin — Backup", description = "Sauvegarde et restauration SQL Server")
@RestController
@RequestMapping("/api/v1/admin/backup")
public class AdminBackupController {

    private final BackupService backupService;

    public AdminBackupController(BackupService backupService) {
        this.backupService = backupService;
    }

    @Operation(summary = "Déclencher une sauvegarde complète SQL Server")
    @PostMapping
    @PreAuthorize("hasAuthority('PRIV_BACKUP_DB')")
    public ResponseEntity<Map<String, String>> backup() {
        String path = backupService.backup();
        return ResponseEntity.ok(Map.of("fichier", path, "statut", "OK"));
    }

    @Operation(summary = "Lister les fichiers de sauvegarde disponibles")
    @GetMapping
    @PreAuthorize("hasAuthority('PRIV_BACKUP_DB')")
    public ResponseEntity<List<String>> lister() {
        return ResponseEntity.ok(backupService.listerSauvegardes());
    }

    @Operation(summary = "Restaurer la base depuis un fichier .bak (ATTENTION : destructif)")
    @PostMapping("/restore/{filename}")
    @PreAuthorize("hasAuthority('PRIV_RESTORE_DB')")
    public ResponseEntity<Map<String, String>> restore(@PathVariable String filename) {
        backupService.restore(filename);
        return ResponseEntity.ok(Map.of("fichier", filename, "statut", "RESTAURÉ"));
    }
}
