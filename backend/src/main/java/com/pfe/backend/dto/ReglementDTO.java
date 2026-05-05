package com.pfe.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ReglementDTO {

    public record RemboursementCaisseRequest(
            @NotNull @DecimalMin("0.01") BigDecimal montant,
            LocalDate dateReglement,
            @Size(max = 50) String numPiece,
            @Size(max = 30) String modePaiement,
            @Size(max = 25) String codeAgence
    ) {}

    public record Response(
            Long idReglement,
            Long idCredit,
            LocalDate dateReglement,
            BigDecimal montantTotal,
            Long idComptabilite,
            String statut
    ) {}
}

