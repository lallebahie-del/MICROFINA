package com.pfe.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CompteEpsDTO {

    public record CreateRequest(
        @NotBlank @Size(max = 255) String numCompte,
        @NotBlank String numMembre,
        String codeAgence,
        @Size(max = 20) String produitEpargne,
        @Size(max = 255) String typeEpargne,
        @Size(max = 255) String remarque,
        @DecimalMin("0") BigDecimal montantOuvert,
        BigDecimal tauxInteret,
        LocalDate dateCreation,
        LocalDate dateEcheance,
        Integer duree
    ) {}

    public record MouvementRequest(
        @DecimalMin("0.01") BigDecimal montant,
        @Size(max = 255) String libelle
    ) {}

    public record MouvementResponse(
        String numCompte,
        String type,
        BigDecimal montant,
        BigDecimal soldeApres
    ) {}

    public record UpdateRequest(
        @Size(max = 255) String typeEpargne,
        @Size(max = 255) String remarque,
        String bloque,
        String ferme,
        String exonere,
        BigDecimal montantBloque,
        BigDecimal tauxInteret,
        LocalDate dateEcheance,
        LocalDate dateFermee
    ) {}

    public record Response(
        String numCompte,
        String numMembre,
        String codeAgence,
        String produitEpargne,
        String typeEpargne,
        String bloque,
        String ferme,
        String exonere,
        BigDecimal montantOuvert,
        BigDecimal montantBloque,
        BigDecimal montantDepot,
        BigDecimal tauxInteret,
        LocalDate dateCreation,
        LocalDate dateEcheance,
        LocalDate dateFermee
    ) {
        public static Response from(com.microfina.entity.CompteEps c) {
            return new Response(
                c.getNumCompte(),
                c.getMembre() != null ? c.getMembre().getNumMembre() : null,
                c.getAgence() != null ? c.getAgence().getCodeAgence() : null,
                c.getProduitEpargne(),
                c.getTypeEpargne(),
                c.getBloque(),
                c.getFerme(),
                c.getExonere(),
                c.getMontantOuvert(),
                c.getMontantBloque(),
                c.getMontantDepot(),
                c.getTauxInteret(),
                c.getDateCreation(),
                c.getDateEcheance(),
                c.getDateFermee()
            );
        }
    }
}
