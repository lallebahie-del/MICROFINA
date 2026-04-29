package com.microfina.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * CreditGroupeMembre – per-member individual credit share within a group loan.
 *
 * Each row records one member's allocation in a {@link CreditsGroupe} and
 * tracks their personal outstanding balance.
 *
 * Unique constraint: one row per (idcreditgroupe, nummembre).
 *
 * DDL source of truth: P4-003-CREATE-TABLE-creditGroupeMembre.xml.
 * Spec: Phase 4, Section 8.
 */
@Entity
@Table(
    name = "creditGroupeMembre",
    uniqueConstraints = @UniqueConstraint(
        name        = "UQ_creditGroupeMembre_credit_membre",
        columnNames = {"idcreditgroupe", "nummembre"}
    )
)
@DynamicUpdate
public class CreditGroupeMembre implements Serializable {

    private static final long serialVersionUID = 1L;

    // ── PK ────────────────────────────────────────────────────────

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IDCREDITGROUPEMEMBRE", nullable = false)
    private Long idCreditGroupeMembre;

    // ── Member's share ────────────────────────────────────────────

    /** Individual disbursement amount for this member. */
    @Column(name = "MONTANT_INDIVIDUEL", precision = 19, scale = 4)
    private BigDecimal montantIndividuel;

    /**
     * Proportional share of the group total (0–1).
     * e.g. 0.25 means this member carries 25 % of the group credit.
     */
    @Column(name = "PART", precision = 19, scale = 4)
    private BigDecimal part;

    /** Individual outstanding principal balance (updated on each repayment). */
    @Column(name = "SOLDE_CAPITAL", precision = 19, scale = 4)
    private BigDecimal soldeCapital;

    /** Per-member credit status (mirrors {@link CreditStatut}). */
    @Enumerated(EnumType.STRING)
    @Column(name = "STATUT", length = 20)
    private CreditStatut statut = CreditStatut.BROUILLON;

    // ── Optimistic lock ───────────────────────────────────────────

    @Version
    @Column(name = "VERSION")
    private Integer version = 0;

    // ── FKs ───────────────────────────────────────────────────────

    /**
     * The group credit this member participates in.
     * FK to Credits(IDCREDIT).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name                 = "idcreditgroupe",
        referencedColumnName = "IDCREDIT",
        foreignKey           = @ForeignKey(name = "FK_creditGroupeMembre_Credits")
    )
    private CreditsGroupe creditsGroupe;

    /**
     * The participating member.
     * FK to membres(NUM_MEMBRE).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name                 = "nummembre",
        referencedColumnName = "NUM_MEMBRE",
        foreignKey           = @ForeignKey(name = "FK_creditGroupeMembre_membres")
    )
    private Membres membre;

    // ── Generated (was Lombok) ──────────────────────────────────────────────

    public CreditGroupeMembre() {
    }

    public CreditGroupeMembre(Long idCreditGroupeMembre, BigDecimal montantIndividuel, BigDecimal part, BigDecimal soldeCapital, CreditStatut statut, Integer version, CreditsGroupe creditsGroupe, Membres membre) {
        this.idCreditGroupeMembre = idCreditGroupeMembre;
        this.montantIndividuel = montantIndividuel;
        this.part = part;
        this.soldeCapital = soldeCapital;
        this.statut = statut;
        this.version = version;
        this.creditsGroupe = creditsGroupe;
        this.membre = membre;
    }

    public Long getIdCreditGroupeMembre() {
        return idCreditGroupeMembre;
    }

    public void setIdCreditGroupeMembre(Long idCreditGroupeMembre) {
        this.idCreditGroupeMembre = idCreditGroupeMembre;
    }

    public BigDecimal getMontantIndividuel() {
        return montantIndividuel;
    }

    public void setMontantIndividuel(BigDecimal montantIndividuel) {
        this.montantIndividuel = montantIndividuel;
    }

    public BigDecimal getPart() {
        return part;
    }

    public void setPart(BigDecimal part) {
        this.part = part;
    }

    public BigDecimal getSoldeCapital() {
        return soldeCapital;
    }

    public void setSoldeCapital(BigDecimal soldeCapital) {
        this.soldeCapital = soldeCapital;
    }

    public CreditStatut getStatut() {
        return statut;
    }

    public void setStatut(CreditStatut statut) {
        this.statut = statut;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public CreditsGroupe getCreditsGroupe() {
        return creditsGroupe;
    }

    public void setCreditsGroupe(CreditsGroupe creditsGroupe) {
        this.creditsGroupe = creditsGroupe;
    }

    public Membres getMembre() {
        return membre;
    }

    public void setMembre(Membres membre) {
        this.membre = membre;
    }

    @Override
    public String toString() {
        return "CreditGroupeMembre("
            + "idCreditGroupeMembre=" + idCreditGroupeMembre
            + ")";
    }
}
