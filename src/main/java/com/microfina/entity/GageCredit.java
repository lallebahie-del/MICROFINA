package com.microfina.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * GageCredit – movable-asset pledge (gage) securing a credit.
 * Covers physical assets: vehicles, equipment, livestock, inventory.
 *
 * DDL source of truth: P4-008-CREATE-TABLE-gageCredit.xml.
 * Spec: Phase 4, Section 11.
 */
@Entity
@Table(name = "gageCredit")
@DynamicUpdate
public class GageCredit implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IDGAGE", nullable = false)
    private Long idGage;

    @Size(max = 255)
    @Column(name = "DESCRIPTION", length = 255)
    private String description;

    @Column(name = "VALEUR", precision = 19, scale = 4)
    private BigDecimal valeur;

    /** VEHICULE | MATERIEL | BETAIL | STOCK | AUTRE. */
    @Size(max = 100)
    @Column(name = "TYPE_GAGE", length = 100)
    private String typeGage;

    @Size(max = 100)
    @Column(name = "NUMERO_SERIE", length = 100)
    private String numeroSerie;

    @Column(name = "DATE_GAGE")
    private LocalDate dateGage;

    @Version
    @Column(name = "VERSION")
    private Integer version = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name                 = "idcredit",
        referencedColumnName = "IDCREDIT",
        foreignKey           = @ForeignKey(name = "FK_gageCredit_Credits")
    )
    private Credits credit;

    // ── Generated (was Lombok) ──────────────────────────────────────────────

    public GageCredit() {
    }

    public GageCredit(Long idGage, String description, BigDecimal valeur, String typeGage, String numeroSerie, LocalDate dateGage, Integer version, Credits credit) {
        this.idGage = idGage;
        this.description = description;
        this.valeur = valeur;
        this.typeGage = typeGage;
        this.numeroSerie = numeroSerie;
        this.dateGage = dateGage;
        this.version = version;
        this.credit = credit;
    }

    public Long getIdGage() {
        return idGage;
    }

    public void setIdGage(Long idGage) {
        this.idGage = idGage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getValeur() {
        return valeur;
    }

    public void setValeur(BigDecimal valeur) {
        this.valeur = valeur;
    }

    public String getTypeGage() {
        return typeGage;
    }

    public void setTypeGage(String typeGage) {
        this.typeGage = typeGage;
    }

    public String getNumeroSerie() {
        return numeroSerie;
    }

    public void setNumeroSerie(String numeroSerie) {
        this.numeroSerie = numeroSerie;
    }

    public LocalDate getDateGage() {
        return dateGage;
    }

    public void setDateGage(LocalDate dateGage) {
        this.dateGage = dateGage;
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

    @Override
    public String toString() {
        return "GageCredit("
            + "idGage=" + idGage
            + ")";
    }
}
