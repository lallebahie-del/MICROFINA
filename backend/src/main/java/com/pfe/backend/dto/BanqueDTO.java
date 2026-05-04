package com.pfe.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class BanqueDTO {

    public record CreateRequest(
        @NotBlank @Size(max = 20) String codeBanque,
        @NotBlank @Size(max = 255) String nom,
        @Size(max = 11) String swiftBic,
        @Size(max = 500) String adresse,
        @Size(max = 100) String pays,
        Boolean actif
    ) {}

    public record UpdateRequest(
        @NotBlank @Size(max = 255) String nom,
        @Size(max = 11) String swiftBic,
        @Size(max = 500) String adresse,
        @Size(max = 100) String pays,
        Boolean actif
    ) {}

    public record Response(
        String codeBanque,
        String nom,
        String swiftBic,
        String adresse,
        String pays,
        Boolean actif
    ) {
        public static Response from(com.microfina.entity.Banque b) {
            return new Response(
                b.getCodeBanque(),
                b.getNom(),
                b.getSwiftBic(),
                b.getAdresse(),
                b.getPays(),
                b.getActif()
            );
        }
    }
}
