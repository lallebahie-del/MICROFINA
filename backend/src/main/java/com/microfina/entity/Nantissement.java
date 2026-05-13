package com.microfina.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Nantissement – lien over intangible or financial assets securing a credit.
 * Covers: fonds de commerce, créances, titres, stocks.
 * Distinct from {@link GageCredit} (physical) and {@link HypothequeCredit} (real estate).
 *
 * DDL source of truth: P4-010-CREATE-TABLE-Nantissement.xml.
 * Spec: Phase 4, Section 11.
 */
@Entity
@Table(name = "Nantissement")
@DynamicUpdate
public class Nantissement implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IDNANTISSEMENT", nullable = false)
    private Long idNantissement;

    @Size(max = 255)
    @Column(name = "DESCRIPTION", length = 255)
    private String description;

    @Column(name = "VALEUR", precision = 19, scale = 4)
    private BigDecimal valeur;

    /** FONDS_COMMERCE | CREANCE | TITRES | STOCK | AUTRE. */
    @Size(max = 100)
    @Column(name = "TYPE_NANTISSEMENT", length = 100)
    private String typeNantissement;

    /** RCCM, notarial deed, or official registration reference. */
    @Size(max = 100)
    @Column(name = "REFERENCE_ACTE", length = 100)
    private String referenceActe;

    @Column(name = "DATE_NANTISSEMENT")
    private LocalDate dateNantissement;

    @Version
    @Column(name = "VERSION")
    private Integer version = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name                 = "idcredit",
        referencedColumnName = "IDCREDIT",
        foreignKey           = @ForeignKey(name = "FK_Nantissement_Credits")
    )
    private Credits credit;

    // ── Generated (was Lombok) ──────────────────────────────────────────────

    public Nantissement() {
    }

    public Nantissement(Long idNantissement, String description, BigDecimal valeur, String typeNantissement, String referenceActe, LocalDate dateNantissement, Integer version, Credits credit) {
        this.idNantissement = idNantissement;
        this.description = description;
        this.valeur = valeur;
        this.typeNantissement = typeNantissement;
        this.referenceActe = referenceActe;
        this.dateNantissement = dateNantissement;
        this.version = version;
        this.credit = credit;
    }

    public Long getIdNantissement() {
        return idNantissement;
    }

    public void setIdNantissement(Long idNantissement) {
        this.idNantissement = idNantissement;
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

    public String getTypeNantissement() {
        return typeNantissement;
    }

    public void setTypeNantissement(String typeNantissement) {
        this.typeNantissement = typeNantissement;
    }

    public String getReferenceActe() {
        return referenceActe;
    }

    public void setReferenceActe(String referenceActe) {
        this.referenceActe = referenceActe;
    }

    public LocalDate getDateNantissement() {
        return dateNantissement;
    }

    public void setDateNantissement(LocalDate dateNantissement) {
        this.dateNantissement = dateNantissement;
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
        return "Nantissement("
            + "idNantissement=" + idNantissement
            + ")";
    }
}
