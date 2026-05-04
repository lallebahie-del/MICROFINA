package com.microfina.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DetailReglement – allocation of a {@link Reglement} to a specific
 * {@link Amortp} instalment.
 *
 * A single Reglement may settle multiple instalments (e.g. a member
 * paying off three overdue instalments at once). Each DetailReglement
 * line records exactly how much of the payment was applied to one
 * instalment's capital, interest, penalties, insurance, and commission.
 *
 * Unique constraint: one allocation line per (Reglement, Amortp).
 *
 * When a DetailReglement is persisted, the service layer must:
 *  1. Increment the corresponding Amortp.*_REMBOURSE / *_REGLEE columns.
 *  2. Update Amortp.STATUT_ECHEANCE (EN_ATTENTE → PARTIELLEMENT_REGLE → REGLE).
 *  3. Decrement Credits.SOLDE_CAPITAL / SOLDE_INTERET / SOLDE_PENALITE.
 *
 * DDL source of truth: P4-012-CREATE-TABLE-detail_reglement.xml.
 * Spec: Phase 4, Section 10.
 */
@Entity
@Table(
    name = "detail_reglement",
    uniqueConstraints = @UniqueConstraint(
        name        = "UQ_detail_reglement_reglement_amortp",
        columnNames = {"idreglement", "idamortp"}
    )
)
@DynamicUpdate
public class DetailReglement implements Serializable {

    private static final long serialVersionUID = 1L;

    // ── PK ────────────────────────────────────────────────────────

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IDDETAILREGLEMENT", nullable = false)
    private Long idDetailReglement;

    // ── Amounts allocated to this Amortp row ──────────────────────

    @Column(name = "MONTANT_CAPITAL", precision = 19, scale = 4, nullable = false)
    private BigDecimal montantCapital = BigDecimal.ZERO;

    @Column(name = "MONTANT_INTERET", precision = 19, scale = 4, nullable = false)
    private BigDecimal montantInteret = BigDecimal.ZERO;

    @Column(name = "MONTANT_PENALITE", precision = 19, scale = 4, nullable = false)
    private BigDecimal montantPenalite = BigDecimal.ZERO;

    @Column(name = "MONTANT_ASSURANCE", precision = 19, scale = 4, nullable = false)
    private BigDecimal montantAssurance = BigDecimal.ZERO;

    @Column(name = "MONTANT_COMMISSION", precision = 19, scale = 4, nullable = false)
    private BigDecimal montantCommission = BigDecimal.ZERO;

    // ── Optimistic lock ───────────────────────────────────────────

    @Version
    @Column(name = "VERSION")
    private Integer version = 0;

    // ── FKs ───────────────────────────────────────────────────────

    /**
     * The payment header this allocation belongs to.
     * FK to Reglement(IDREGLEMENT).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name                 = "idreglement",
        referencedColumnName = "IDREGLEMENT",
        foreignKey           = @ForeignKey(name = "FK_detail_reglement_Reglement")
    )
    private Reglement reglement;

    /**
     * The specific instalment being partially or fully settled.
     * FK to Amortp(IDAMORTP).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name                 = "idamortp",
        referencedColumnName = "IDAMORTP",
        foreignKey           = @ForeignKey(name = "FK_detail_reglement_Amortp")
    )
    private Amortp amortp;

    // ── Generated (was Lombok) ──────────────────────────────────────────────

    public DetailReglement() {
    }

    public DetailReglement(Long idDetailReglement, BigDecimal montantCapital, BigDecimal montantInteret, BigDecimal montantPenalite, BigDecimal montantAssurance, BigDecimal montantCommission, Integer version, Reglement reglement, Amortp amortp) {
        this.idDetailReglement = idDetailReglement;
        this.montantCapital = montantCapital;
        this.montantInteret = montantInteret;
        this.montantPenalite = montantPenalite;
        this.montantAssurance = montantAssurance;
        this.montantCommission = montantCommission;
        this.version = version;
        this.reglement = reglement;
        this.amortp = amortp;
    }

    public Long getIdDetailReglement() {
        return idDetailReglement;
    }

    public void setIdDetailReglement(Long idDetailReglement) {
        this.idDetailReglement = idDetailReglement;
    }

    public BigDecimal getMontantCapital() {
        return montantCapital;
    }

    public void setMontantCapital(BigDecimal montantCapital) {
        this.montantCapital = montantCapital;
    }

    public BigDecimal getMontantInteret() {
        return montantInteret;
    }

    public void setMontantInteret(BigDecimal montantInteret) {
        this.montantInteret = montantInteret;
    }

    public BigDecimal getMontantPenalite() {
        return montantPenalite;
    }

    public void setMontantPenalite(BigDecimal montantPenalite) {
        this.montantPenalite = montantPenalite;
    }

    public BigDecimal getMontantAssurance() {
        return montantAssurance;
    }

    public void setMontantAssurance(BigDecimal montantAssurance) {
        this.montantAssurance = montantAssurance;
    }

    public BigDecimal getMontantCommission() {
        return montantCommission;
    }

    public void setMontantCommission(BigDecimal montantCommission) {
        this.montantCommission = montantCommission;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Reglement getReglement() {
        return reglement;
    }

    public void setReglement(Reglement reglement) {
        this.reglement = reglement;
    }

    public Amortp getAmortp() {
        return amortp;
    }

    public void setAmortp(Amortp amortp) {
        this.amortp = amortp;
    }

    @Override
    public String toString() {
        return "DetailReglement("
            + "idDetailReglement=" + idDetailReglement
            + ")";
    }
}
