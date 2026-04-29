package com.microfina.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Caution – formal financial surety bond securing a credit.
 * Issued by a bank, insurer, employer, or mutual fund committing to pay
 * the outstanding balance if the borrower defaults.
 *
 * DDL source of truth: P4-009-CREATE-TABLE-Caution.xml.
 * Spec: Phase 4, Section 11.
 */
@Entity
@Table(name = "Caution")
@DynamicUpdate
public class Caution implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IDCAUTION", nullable = false)
    private Long idCaution;

    @Size(max = 255)
    @Column(name = "NOM_CAUTION", length = 255)
    private String nomCaution;

    /** BANCAIRE | EMPLOYEUR | ASSURANCE | MUTUELLE | AUTRE. */
    @Size(max = 50)
    @Column(name = "TYPE_CAUTION", length = 50)
    private String typeCaution;

    /** Amount guaranteed (may be partial coverage of the credit). */
    @Column(name = "MONTANT_CAUTION", precision = 19, scale = 4)
    private BigDecimal montantCaution;

    @Size(max = 100)
    @Column(name = "REFERENCE_DOC", length = 100)
    private String referenceDoc;

    @Column(name = "DATE_CAUTION")
    private LocalDate dateCaution;

    @Column(name = "DATE_EXPIRATION")
    private LocalDate dateExpiration;

    @Version
    @Column(name = "VERSION")
    private Integer version = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name                 = "idcredit",
        referencedColumnName = "IDCREDIT",
        foreignKey           = @ForeignKey(name = "FK_Caution_Credits")
    )
    private Credits credit;

    // ── Generated (was Lombok) ──────────────────────────────────────────────

    public Caution() {
    }

    public Caution(Long idCaution, String nomCaution, String typeCaution, BigDecimal montantCaution, String referenceDoc, LocalDate dateCaution, LocalDate dateExpiration, Integer version, Credits credit) {
        this.idCaution = idCaution;
        this.nomCaution = nomCaution;
        this.typeCaution = typeCaution;
        this.montantCaution = montantCaution;
        this.referenceDoc = referenceDoc;
        this.dateCaution = dateCaution;
        this.dateExpiration = dateExpiration;
        this.version = version;
        this.credit = credit;
    }

    public Long getIdCaution() {
        return idCaution;
    }

    public void setIdCaution(Long idCaution) {
        this.idCaution = idCaution;
    }

    public String getNomCaution() {
        return nomCaution;
    }

    public void setNomCaution(String nomCaution) {
        this.nomCaution = nomCaution;
    }

    public String getTypeCaution() {
        return typeCaution;
    }

    public void setTypeCaution(String typeCaution) {
        this.typeCaution = typeCaution;
    }

    public BigDecimal getMontantCaution() {
        return montantCaution;
    }

    public void setMontantCaution(BigDecimal montantCaution) {
        this.montantCaution = montantCaution;
    }

    public String getReferenceDoc() {
        return referenceDoc;
    }

    public void setReferenceDoc(String referenceDoc) {
        this.referenceDoc = referenceDoc;
    }

    public LocalDate getDateCaution() {
        return dateCaution;
    }

    public void setDateCaution(LocalDate dateCaution) {
        this.dateCaution = dateCaution;
    }

    public LocalDate getDateExpiration() {
        return dateExpiration;
    }

    public void setDateExpiration(LocalDate dateExpiration) {
        this.dateExpiration = dateExpiration;
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
        return "Caution("
            + "idCaution=" + idCaution
            + ")";
    }
}
