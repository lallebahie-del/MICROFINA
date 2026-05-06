package com.microfina.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;

/**
 * FAMILLEPRODUITCREDIT – families of credit products
 * (e.g. Microcredit, Habitat, Agriculture, Group credit).
 */
@Entity
@Table(name = "FAMILLEPRODUITCREDIT")
@DynamicUpdate
public class FamilleProduitCredit implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @NotBlank
    @Size(max = 255)
    @Column(name = "CODEFAMILLEPRODUITCREDIT", length = 255, nullable = false)
    private String codeFamilleProduitCredit;

    @NotBlank
    @Size(max = 255)
    @Column(name = "nomfamilleProduitCredit", length = 255, nullable = false)
    private String nomFamilleProduitCredit;

    @Column(name = "ACTIF", nullable = false)
    private Boolean actif = true;

    @Version
    @Column(name = "VERSION")
    private Integer version = 0;

    // ── Generated (was Lombok) ──────────────────────────────────────────────

    public FamilleProduitCredit() {
    }

    public FamilleProduitCredit(String codeFamilleProduitCredit, String nomFamilleProduitCredit, Boolean actif, Integer version) {
        this.codeFamilleProduitCredit = codeFamilleProduitCredit;
        this.nomFamilleProduitCredit = nomFamilleProduitCredit;
        this.actif = actif;
        this.version = version;
    }

    public String getCodeFamilleProduitCredit() {
        return codeFamilleProduitCredit;
    }

    public void setCodeFamilleProduitCredit(String codeFamilleProduitCredit) {
        this.codeFamilleProduitCredit = codeFamilleProduitCredit;
    }

    public String getNomFamilleProduitCredit() {
        return nomFamilleProduitCredit;
    }

    public void setNomFamilleProduitCredit(String nomFamilleProduitCredit) {
        this.nomFamilleProduitCredit = nomFamilleProduitCredit;
    }

    public Boolean getActif() {
        return actif;
    }

    public void setActif(Boolean actif) {
        this.actif = actif;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "FamilleProduitCredit("
            + "codeFamilleProduitCredit=" + codeFamilleProduitCredit
            + ")";
    }
}
