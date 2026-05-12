package com.pfe.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class CarnetChequeDTO {

    public record CreateRequest(
        @NotBlank @Size(max = 50) String numeroCarnet,
        Long compteBanqueId,
        @NotBlank String numMembre,
        LocalDate dateDemande,
        Integer nombreCheques
    ) {}

    public record UpdateRequest(
        String statut,
        LocalDate dateRemise,
        Integer nombreCheques
    ) {}

    public record Response(
        Long id,
        String numeroCarnet,
        LocalDate dateDemande,
        LocalDate dateRemise,
        Integer nombreCheques,
        String statut,
        Long compteBanqueId,
        String numMembre
    ) {
        public static Response from(com.microfina.entity.CarnetCheque c) {
            return new Response(
                c.getId(),
                c.getNumeroCarnet(),
                c.getDateDemande(),
                c.getDateRemise(),
                c.getNombreCheques(),
                c.getStatut() != null ? c.getStatut().name() : null,
                c.getCompteBanque() != null ? c.getCompteBanque().getId() : null,
                c.getMembre() != null ? c.getMembre().getNumMembre() : null
            );
        }
    }
}
