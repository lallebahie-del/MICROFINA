package com.pfe.backend.controller;

import com.pfe.backend.dto.SimulationCreditDTO;
import com.pfe.backend.service.AmortissementCalculator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * SimulationCreditController — calcul d'amortissement français (annuités constantes).
 */
@Tag(name = "Simulation crédit", description = "Calcul d'amortissement français")
@RestController
@RequestMapping("/api/v1/simulations")
public class SimulationCreditController {

    private final AmortissementCalculator calculator;

    public SimulationCreditController(AmortissementCalculator calculator) {
        this.calculator = calculator;
    }

    @Operation(summary = "Simuler un crédit — tableau d'amortissement français")
    @PostMapping("/credit")
    @PreAuthorize("hasAuthority('PRIV_CREATE_CREDIT')")
    public ResponseEntity<SimulationCreditDTO.SimulationResponse> simuler(
            @Valid @RequestBody SimulationCreditDTO.SimulationRequest req) {
        return ResponseEntity.ok(calculator.calculer(req));
    }
}
