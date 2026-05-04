package com.pfe.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

/**
 * SimulationCreditDTO — DTOs pour la simulation de crédit (amortissement français).
 */
public class SimulationCreditDTO {

    public record SimulationRequest(
        @NotNull @DecimalMin("0.01") BigDecimal montantPrincipal,
        @NotNull @DecimalMin("0") BigDecimal tauxAnnuel,
        @NotNull @Min(1) Integer nombreEcheances,
        @NotBlank String periodicite
    ) {}

    public record EcheanceDto(
        int numero,
        BigDecimal capitalDu,
        BigDecimal interet,
        BigDecimal capitalRembourse,
        BigDecimal echeance
    ) {}

    public record SimulationResponse(
        BigDecimal montantPrincipal,
        BigDecimal tauxAnnuel,
        Integer nombreEcheances,
        String periodicite,
        BigDecimal echeanceMensuelle,
        BigDecimal totalRembourse,
        BigDecimal totalInteret,
        List<EcheanceDto> tableau
    ) {}
}
