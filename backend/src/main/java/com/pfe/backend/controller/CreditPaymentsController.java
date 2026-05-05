package com.pfe.backend.controller;

import com.pfe.backend.dto.CreditPaymentsDTO;
import com.pfe.backend.service.CreditPaymentsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Crédits — Suivi paiements", description = "Échéancier (Amortp) + retards / balance âgée")
@RestController
@RequestMapping("/api/v1/credits")
public class CreditPaymentsController {

    private final CreditPaymentsService service;

    public CreditPaymentsController(CreditPaymentsService service) {
        this.service = service;
    }

    @Operation(summary = "Suivi paiements par crédit (échéances Amortp + balance âgée)")
    @GetMapping("/{idCredit}/amortp")
    @PreAuthorize("hasAuthority('PRIV_VIEW_REPORTS') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<CreditPaymentsDTO.Response> getSuivi(@PathVariable Long idCredit) {
        return ResponseEntity.ok(service.getAmortpSuivi(idCredit));
    }

    @Operation(summary = "Prévisualisation échéancier (sans persistance Amortp) — ex. après comité")
    @GetMapping("/{idCredit}/amortissement/preview")
    @PreAuthorize("hasAuthority('PRIV_VIEW_REPORTS') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<CreditPaymentsDTO.PreviewResponse> getAmortissementPreview(
            @PathVariable Long idCredit) {
        return ResponseEntity.ok(service.getAmortissementPreview(idCredit));
    }
}

