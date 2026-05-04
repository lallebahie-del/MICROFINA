package com.microfina.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;

/**
 * FAMILLEPARTSOCIALE – families of social shares held by members
 * (e.g. Ordinaire, Privilégiée).
 */
@Entity
@Table(name = "FAMILLEPARTSOCIALE")
@DynamicUpdate
public class FamillePartSociale implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IDFAMILLEPARTSOCIALE", nullable = false)
    private Long idFamillePartSociale;

    @NotBlank
    @Size(max = 255)
    @Column(name = "nomfamille", length = 255, nullable = false)
    private String nomFamille;

    @Column(name = "ACTIF", nullable = false)
    private Boolean actif = true;

    @Version
    @Column(name = "VERSION")
    private Integer version = 0;

    // ── Generated (was Lombok) ──────────────────────────────────────────────

    public FamillePartSociale() {
    }

    public FamillePartSociale(Long idFamillePartSociale, String nomFamille, Boolean actif, Integer version) {
        this.idFamillePartSociale = idFamillePartSociale;
        this.nomFamille = nomFamille;
        this.actif = actif;
        this.version = version;
    }

    public Long getIdFamillePartSociale() {
        return idFamillePartSociale;
    }

    public void setIdFamillePartSociale(Long idFamillePartSociale) {
        this.idFamillePartSociale = idFamillePartSociale;
    }

    public String getNomFamille() {
        return nomFamille;
    }

    public void setNomFamille(String nomFamille) {
        this.nomFamille = nomFamille;
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
        return "FamillePartSociale("
            + "idFamillePartSociale=" + idFamillePartSociale
            + ")";
    }
}
