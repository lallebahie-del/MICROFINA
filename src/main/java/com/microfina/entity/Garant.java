package com.microfina.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Garant – personal guarantor pledging to cover a credit if the
 * primary borrower defaults.
 *
 * A guarantor may or may not be a registered member (membre nullable).
 * FK to membres is optional; NOM_GARANT / PRENOM_GARANT cover
 * external guarantors.
 *
 * DDL source of truth: P4-006-CREATE-TABLE-garant.xml.
 * Spec: Phase 4, Section 11 (Garanties).
 */
@Entity
@Table(name = "garant")
@DynamicUpdate
public class Garant implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IDGARANT", nullable = false)
    private Long idGarant;

    @Size(max = 255)
    @Column(name = "NOM_GARANT", length = 255)
    private String nomGarant;

    @Size(max = 255)
    @Column(name = "PRENOM_GARANT", length = 255)
    private String prenomGarant;

    /** PP=Personne Physique, PM=Personne Morale, MEMBRE=registered member. */
    @Size(max = 50)
    @Column(name = "TYPE_GARANT", length = 50)
    private String typeGarant;

    @Size(max = 30)
    @Column(name = "TELEPHONE", length = 30)
    private String telephone;

    @Size(max = 255)
    @Column(name = "ADRESSE", length = 255)
    private String adresse;

    @Size(max = 50)
    @Column(name = "NUMPIECE_IDENTITE", length = 50)
    private String numPieceIdentite;

    /** Monthly income used for guarantor risk assessment. */
    @Column(name = "REVENU_MENSUEL", precision = 19, scale = 4)
    private BigDecimal revenuMensuel;

    @Version
    @Column(name = "VERSION")
    private Integer version = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name                 = "idcredit",
        referencedColumnName = "IDCREDIT",
        foreignKey           = @ForeignKey(name = "FK_garant_Credits")
    )
    private Credits credit;

    /** Optional: guarantor is also a registered member. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name                 = "nummembre",
        referencedColumnName = "NUM_MEMBRE",
        foreignKey           = @ForeignKey(name = "FK_garant_membres")
    )
    private Membres membre;

    // ── Generated (was Lombok) ──────────────────────────────────────────────

    public Garant() {
    }

    public Garant(Long idGarant, String nomGarant, String prenomGarant, String typeGarant, String telephone, String adresse, String numPieceIdentite, BigDecimal revenuMensuel, Integer version, Credits credit, Membres membre) {
        this.idGarant = idGarant;
        this.nomGarant = nomGarant;
        this.prenomGarant = prenomGarant;
        this.typeGarant = typeGarant;
        this.telephone = telephone;
        this.adresse = adresse;
        this.numPieceIdentite = numPieceIdentite;
        this.revenuMensuel = revenuMensuel;
        this.version = version;
        this.credit = credit;
        this.membre = membre;
    }

    public Long getIdGarant() {
        return idGarant;
    }

    public void setIdGarant(Long idGarant) {
        this.idGarant = idGarant;
    }

    public String getNomGarant() {
        return nomGarant;
    }

    public void setNomGarant(String nomGarant) {
        this.nomGarant = nomGarant;
    }

    public String getPrenomGarant() {
        return prenomGarant;
    }

    public void setPrenomGarant(String prenomGarant) {
        this.prenomGarant = prenomGarant;
    }

    public String getTypeGarant() {
        return typeGarant;
    }

    public void setTypeGarant(String typeGarant) {
        this.typeGarant = typeGarant;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getNumPieceIdentite() {
        return numPieceIdentite;
    }

    public void setNumPieceIdentite(String numPieceIdentite) {
        this.numPieceIdentite = numPieceIdentite;
    }

    public BigDecimal getRevenuMensuel() {
        return revenuMensuel;
    }

    public void setRevenuMensuel(BigDecimal revenuMensuel) {
        this.revenuMensuel = revenuMensuel;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Credits getCredit() {
        return credit;
    }

    public void setCredit(Credits credit) {
        this.credit = credit;
    }

    public Membres getMembre() {
        return membre;
    }

    public void setMembre(Membres membre) {
        this.membre = membre;
    }

    @Override
    public String toString() {
        return "Garant("
            + "idGarant=" + idGarant
            + ")";
    }
}
