package com.microfina.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * HypothequeCredit – real-estate / immovable-property mortgage
 * securing a credit loan.
 *
 * DDL source of truth: P4-007-CREATE-TABLE-HYPOTHEQUE_CREDIT.xml.
 * Spec: Phase 4, Section 11.
 */
@Entity
@Table(name = "HYPOTHEQUE_CREDIT")
@DynamicUpdate
public class HypothequeCredit implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IDHYPOTHEQUE", nullable = false)
    private Long idHypotheque;

    @Size(max = 255)
    @Column(name = "DESCRIPTION", length = 255)
    private String description;

    @Column(name = "VALEUR_ESTIMEE", precision = 19, scale = 4)
    private BigDecimal valeurEstimee;

    /** Forced-sale (liquidation) value. */
    @Column(name = "VALEUR_FORCEE", precision = 19, scale = 4)
    private BigDecimal valeurForcee;

    /** TERRAIN | BATIMENT | VILLA | APPARTEMENT | COMMERCE. */
    @Size(max = 100)
    @Column(name = "TYPE_BIEN", length = 100)
    private String typeBien;

    @Size(max = 255)
    @Column(name = "LOCALISATION", length = 255)
    private String localisation;

    /** Land-registry / titre foncier reference number. */
    @Size(max = 100)
    @Column(name = "TITRE_FONCIER", length = 100)
    private String titreFoncier;

    @Column(name = "DATE_HYPOTHEQUE")
    private LocalDate dateHypotheque;

    @Version
    @Column(name = "VERSION")
    private Integer version = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name                 = "idcredit",
        referencedColumnName = "IDCREDIT",
        foreignKey           = @ForeignKey(name = "FK_HYPOTHEQUE_CREDIT_Credits")
    )
    private Credits credit;

    // ── Generated (was Lombok) ──────────────────────────────────────────────

    public HypothequeCredit() {
    }

    public HypothequeCredit(Long idHypotheque, String description, BigDecimal valeurEstimee, BigDecimal valeurForcee, String typeBien, String localisation, String titreFoncier, LocalDate dateHypotheque, Integer version, Credits credit) {
        this.idHypotheque = idHypotheque;
        this.description = description;
        this.valeurEstimee = valeurEstimee;
        this.valeurForcee = valeurForcee;
        this.typeBien = typeBien;
        this.localisation = localisation;
        this.titreFoncier = titreFoncier;
        this.dateHypotheque = dateHypotheque;
        this.version = version;
        this.credit = credit;
    }

    public Long getIdHypotheque() {
        return idHypotheque;
    }

    public void setIdHypotheque(Long idHypotheque) {
        this.idHypotheque = idHypotheque;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getValeurEstimee() {
        return valeurEstimee;
    }

    public void setValeurEstimee(BigDecimal valeurEstimee) {
        this.valeurEstimee = valeurEstimee;
    }

    public BigDecimal getValeurForcee() {
        return valeurForcee;
    }

    public void setValeurForcee(BigDecimal valeurForcee) {
        this.valeurForcee = valeurForcee;
    }

    public String getTypeBien() {
        return typeBien;
    }

    public void setTypeBien(String typeBien) {
        this.typeBien = typeBien;
    }

    public String getLocalisation() {
        return localisation;
    }

    public void setLocalisation(String localisation) {
        this.localisation = localisation;
    }

    public String getTitreFoncier() {
        return titreFoncier;
    }

    public void setTitreFoncier(String titreFoncier) {
        this.titreFoncier = titreFoncier;
    }

    public LocalDate getDateHypotheque() {
        return dateHypotheque;
    }

    public void setDateHypotheque(LocalDate dateHypotheque) {
        this.dateHypotheque = dateHypotheque;
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
        return "HypothequeCredit("
            + "idHypotheque=" + idHypotheque
            + ")";
    }
}
