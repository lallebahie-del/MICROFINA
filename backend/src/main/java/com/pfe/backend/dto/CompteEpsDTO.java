package com.pfe.backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CompteEpsDTO {

    static String resolveCodeAgence(com.microfina.entity.CompteEps c) {
        if (c.getAgence() != null && c.getAgence().getCodeAgence() != null
                && !c.getAgence().getCodeAgence().isBlank()) {
            return c.getAgence().getCodeAgence();
        }
        if (c.getMembre() != null && c.getMembre().getAgence() != null
                && c.getMembre().getAgence().getCodeAgence() != null
                && !c.getMembre().getAgence().getCodeAgence().isBlank()) {
            return c.getMembre().getAgence().getCodeAgence();
        }
        return null;
    }

    public record CreateRequest(
        @NotBlank @Size(max = 255) String numCompte,
        @NotBlank String numMembre,
        @JsonAlias("agence")
        String codeAgence,
        @Size(max = 20) String produitEpargne,
        @Size(max = 255) String typeEpargne,
        @Size(max = 255) String remarque,
        @JsonAlias("solde")
        @DecimalMin("0") BigDecimal montantOuvert,
        BigDecimal tauxInteret,
        @JsonAlias("dateOuverture")
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
                resolveCodeAgence(c),
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
