package com.microfina.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Personne – JOINED subtype of {@link Tiers}.
 *
 * Stores all personal-identity fields for physical persons
 * (gestionnaires, commerciaux, sensibilisateurs, utilisateurs…).
 * The PK {@code CODE_TIERS} is shared with the parent TIERS table.
 *
 * DDL source of truth: P2-002-CREATE-TABLE-personne.xml.
 * Spec p.52-53.
 *
 * Phase-2+ FK columns ({@code profession}, {@code zoneGeographique})
 * are stored as raw Long values until their target tables are created.
 */
@Entity
@Table(name = "personne")
@PrimaryKeyJoinColumn(
    name                = "CODE_TIERS",
    referencedColumnName = "CODE_TIERS",
    foreignKey           = @ForeignKey(name = "FK_personne_TIERS")
)
@DiscriminatorValue("personne")
@DynamicUpdate
public class Personne extends Tiers implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Internal person identifier (AN/48). Not the PK. */
    @Size(max = 48)
    @Column(name = "idPersonne", length = 48)
    private String idPersonne;

    /** Active flag (N type – 1 = active, 0 = inactive). */
    @Column(name = "ACTIF")
    private Integer actif;

    /**
     * FK to zoneGeographique (numeric N type, phase 1).
     * Stored as raw Long; @ManyToOne deferred until phase 1 ZoneGeographique
     * entity relationship is confirmed.
     */
    @Column(name = "zoneGeographique")
    private Long zoneGeographique;

    /** FK to ADRESSE (AN/48). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name                 = "ADRESSE_IDADRESSE",
        referencedColumnName = "IDADRESSE",
        foreignKey           = @ForeignKey(name = "FK_personne_ADRESSE")
    )
    private Adresse adresse;

    // ── Personal identity ──────────────────────────────────────────

    @Column(name = "dateNAIS")
    private LocalDate dateNais;

    @Size(max = 255)
    @Column(name = "lieuNaissance", length = 255)
    private String lieuNaissance;

    @Size(max = 255)
    @Column(name = "nomJeuneFille", length = 255)
    private String nomJeuneFille;

    @Size(max = 255)
    @Column(name = "nomPersonne", length = 255)
    private String nomPersonne;

    @Size(max = 255)
    @Column(name = "prenomPersonne", length = 255)
    private String prenomPersonne;

    /** Sex (AN/255 per spec – free-form label, not a 1-char code). */
    @Size(max = 255)
    @Column(name = "sexe", length = 255)
    private String sexe;

    /**
     * FK to profession table (N type – numeric Long).
     * Constraint deferred to phase 2+.
     */
    @Column(name = "profession")
    private Long profession;

    @Size(max = 255)
    @Column(name = "raison_sociale", length = 255)
    private String raisonSociale;

    @Size(max = 255)
    @Column(name = "NOMPARRAIN", length = 255)
    private String nomParrain;

    // ── Back-links ─────────────────────────────────────────────────

    /** Number of the member this person is linked to (AN/25). */
    @Size(max = 25)
    @Column(name = "membre", length = 25)
    private String membre;

    @Size(max = 25)
    @Column(name = "matriculePersonne", length = 25)
    private String matriculePersonne;

    @Size(max = 25)
    @Column(name = "num_membre_old", length = 25)
    private String numMembreOld;

    // ── Generated (was Lombok) ──────────────────────────────────────────────

    public Personne() {
    }

    public Personne(String idPersonne, Integer actif, Long zoneGeographique, Adresse adresse, LocalDate dateNais, String lieuNaissance, String nomJeuneFille, String nomPersonne, String prenomPersonne, String sexe, Long profession, String raisonSociale, String nomParrain, String membre, String matriculePersonne, String numMembreOld) {
        this.idPersonne = idPersonne;
        this.actif = actif;
        this.zoneGeographique = zoneGeographique;
        this.adresse = adresse;
        this.dateNais = dateNais;
        this.lieuNaissance = lieuNaissance;
        this.nomJeuneFille = nomJeuneFille;
        this.nomPersonne = nomPersonne;
        this.prenomPersonne = prenomPersonne;
        this.sexe = sexe;
        this.profession = profession;
        this.raisonSociale = raisonSociale;
        this.nomParrain = nomParrain;
        this.membre = membre;
        this.matriculePersonne = matriculePersonne;
        this.numMembreOld = numMembreOld;
    }

    public String getIdPersonne() {
        return idPersonne;
    }

    public void setIdPersonne(String idPersonne) {
        this.idPersonne = idPersonne;
    }

    public Integer getActif() {
        return actif;
    }

    public void setActif(Integer actif) {
        this.actif = actif;
    }

    public Long getZoneGeographique() {
        return zoneGeographique;
    }

    public void setZoneGeographique(Long zoneGeographique) {
        this.zoneGeographique = zoneGeographique;
    }

    public Adresse getAdresse() {
        return adresse;
    }

    public void setAdresse(Adresse adresse) {
        this.adresse = adresse;
    }

    public LocalDate getDateNais() {
        return dateNais;
    }

    public void setDateNais(LocalDate dateNais) {
        this.dateNais = dateNais;
    }

    public String getLieuNaissance() {
        return lieuNaissance;
    }

    public void setLieuNaissance(String lieuNaissance) {
        this.lieuNaissance = lieuNaissance;
    }

    public String getNomJeuneFille() {
        return nomJeuneFille;
    }

    public void setNomJeuneFille(String nomJeuneFille) {
        this.nomJeuneFille = nomJeuneFille;
    }

    public String getNomPersonne() {
        return nomPersonne;
    }

    public void setNomPersonne(String nomPersonne) {
        this.nomPersonne = nomPersonne;
    }

    public String getPrenomPersonne() {
        return prenomPersonne;
    }

    public void setPrenomPersonne(String prenomPersonne) {
        this.prenomPersonne = prenomPersonne;
    }

    public String getSexe() {
        return sexe;
    }

    public void setSexe(String sexe) {
        this.sexe = sexe;
    }

    public Long getProfession() {
        return profession;
    }

    public void setProfession(Long profession) {
        this.profession = profession;
    }

    public String getRaisonSociale() {
        return raisonSociale;
    }

    public void setRaisonSociale(String raisonSociale) {
        this.raisonSociale = raisonSociale;
    }

    public String getNomParrain() {
        return nomParrain;
    }

    public void setNomParrain(String nomParrain) {
        this.nomParrain = nomParrain;
    }

    public String getMembre() {
        return membre;
    }

    public void setMembre(String membre) {
        this.membre = membre;
    }

    public String getMatriculePersonne() {
        return matriculePersonne;
    }

    public void setMatriculePersonne(String matriculePersonne) {
        this.matriculePersonne = matriculePersonne;
    }

    public String getNumMembreOld() {
        return numMembreOld;
    }

    public void setNumMembreOld(String numMembreOld) {
        this.numMembreOld = numMembreOld;
    }

    @Override
    public String toString() {
        return "Personne(" + super.toString() + ")";
    }
}
