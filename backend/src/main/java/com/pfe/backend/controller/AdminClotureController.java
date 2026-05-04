package com.pfe.backend.controller;

import com.pfe.backend.service.ClotureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Admin — Clôture", description = "Clôture mensuelle et annuelle des exercices comptables")
@RestController
@RequestMapping("/api/v1/admin/cloture")
@PreAuthorize("hasAuthority('PRIV_MANAGE_BUDGET')")
public class AdminClotureController {

    private final ClotureService clotureService;

    public AdminClotureController(ClotureService clotureService) {
        this.clotureService = clotureService;
    }

    @Operation(summary = "Clôture mensuelle d'une période comptable")
    @PostMapping("/mensuelle/{annee}/{mois}")
    public ResponseEntity<Map<String, String>> mensuelle(
            @PathVariable int annee, @PathVariable int mois) {
        clotureService.clotureMensuelle(annee, mois);
        return ResponseEntity.ok(Map.of(
            "annee", String.valueOf(annee),
            "mois", String.valueOf(mois),
            "statut", "CLOTURÉ"
        ));
    }

    @Operation(summary = "Clôture annuelle d'un exercice fiscal")
    @PostMapping("/annuelle/{annee}")
    public ResponseEntity<Map<String, String>> annuelle(@PathVariable int annee) {
        clotureService.clotureAnnuelle(annee);
        return ResponseEntity.ok(Map.of("annee", String.valueOf(annee), "statut", "CLOTURÉ"));
    }
}
