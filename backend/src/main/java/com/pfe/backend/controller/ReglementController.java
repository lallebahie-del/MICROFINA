package com.pfe.backend.controller;

import com.pfe.backend.dto.ReglementDTO;
import com.pfe.backend.service.ReglementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Remboursements", description = "Encaissement des remboursements de crédit")
@RestController
@RequestMapping("/api/v1/credits")
public class ReglementController {

    private final ReglementService service;

    public ReglementController(ReglementService service) {
        this.service = service;
    }

    @Operation(summary = "Encaisser un remboursement en caisse")
    @PostMapping("/{idCredit}/remboursements/caisse")
    @PreAuthorize("hasAuthority('PRIV_POST_REGLEMENT') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ReglementDTO.Response> encaisserCaisse(
            @PathVariable Long idCredit,
            @Valid @RequestBody ReglementDTO.RemboursementCaisseRequest req,
            Authentication auth
    ) {
        return ResponseEntity.status(201).body(
                service.encaisserRemboursementCaisse(idCredit, req, auth != null ? auth.getName() : "SYSTEM")
        );
    }
}

