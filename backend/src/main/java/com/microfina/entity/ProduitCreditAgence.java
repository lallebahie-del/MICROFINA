package com.microfina.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * ProduitCreditAgence – branch-level availability and override parameters
 * for a credit product.
 *
 * Each row enables (or restricts) a {@link ProduitCredit} at a specific
 * {@link Agence} for a given credit-object code.
 * Branch-level overrides (montantMin/Max, tauxInteret) take precedence over
 * the product defaults when set.
 *
 * Composite PK: see {@link ProduitCreditAgenceId} (CODE_AGENCE, NUMPDTCREDIT,
 * CODE_OBJET_CREDIT).
 *
 * DDL source of truth: P3-004-CREATE-TABLE-PRODUIT_CREDIT_AGENCE.xml.
 * Spec: Phase 3, Section 1.
 */
@Entity
@Table(name = "PRODUIT_CREDIT_AGENCE")
@DynamicUpdate
public class ProduitCreditAgence implements Serializable {

    private static final long serialVersionUID = 1L;

    // ── Composite PK ─────────────────────────────────────────────

    @EmbeddedId
    private ProduitCreditAgenceId id;

    // ── Branch-level overrides ────────────────────────────────────

    @Column(name = "MONTANTMIN", precision = 19, scale = 4)
    private BigDecimal montantMin;

    @Column(name = "MONTANTMAX", precision = 19, scale = 4)
    private BigDecimal montantMax;

    @Column(name = "TAUXINTERET", precision = 19, scale = 4)
    private BigDecimal tauxInteret;

    /** 1 = product is active at this branch. */
    @Column(name = "ACTIF")
    private Integer actif = 1;

    // ── Optimistic lock ───────────────────────────────────────────

    @Version
    @Column(name = "VERSION")
    private Integer version = 0;

    // ── FKs ───────────────────────────────────────────────────────

    /**
     * Branch.
     * FK to AGENCE(CODE_AGENCE).
     * Maps via composite PK column – insertable/updatable=false.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name                 = "CODE_AGENCE",
        referencedColumnName = "CODE_AGENCE",
        insertable           = false,
        updatable            = false,
        foreignKey           = @ForeignKey(name = "FK_PRODUIT_CREDIT_AGENCE_AGENCE")
    )
    private Agence agence;

    /**
     * Credit product.
     * FK to produitcredit(NUMPRODUIT).
     * Maps via composite PK column – insertable/updatable=false.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name                 = "NUMPDTCREDIT",
        referencedColumnName = "NUMPRODUIT",
        insertable           = false,
        updatable            = false,
        foreignKey           = @ForeignKey(name = "FK_PRODUIT_CREDIT_AGENCE_produitcredit")
    )
    private ProduitCredit produitCredit;

    // ── Generated (was Lombok) ──────────────────────────────────────────────

    public ProduitCreditAgence() {
    }

    public ProduitCreditAgence(ProduitCreditAgenceId id, BigDecimal montantMin, BigDecimal montantMax, BigDecimal tauxInteret, Integer actif, Integer version, Agence agence, ProduitCredit produitCredit) {
        this.id = id;
        this.montantMin = montantMin;
        this.montantMax = montantMax;
        this.tauxInteret = tauxInteret;
        this.actif = actif;
        this.version = version;
        this.agence = agence;
        this.produitCredit = produitCredit;
    }

    public ProduitCreditAgenceId getId() {
        return id;
    }

    public void setId(ProduitCreditAgenceId id) {
        this.id = id;
    }

    public BigDecimal getMontantMin() {
        return montantMin;
    }

    public void setMontantMin(BigDecimal montantMin) {
        this.montantMin = montantMin;
    }

    public BigDecimal getMontantMax() {
        return montantMax;
    }

    public void setMontantMax(BigDecimal montantMax) {
        this.montantMax = montantMax;
    }

    public BigDecimal getTauxInteret() {
        return tauxInteret;
    }

    public void setTauxInteret(BigDecimal tauxInteret) {
        this.tauxInteret = tauxInteret;
    }

    public Integer getActif() {
        return actif;
    }

    public void setActif(Integer actif) {
        this.actif = actif;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Agence getAgence() {
        return agence;
    }

    public void setAgence(Agence agence) {
        this.agence = agence;
    }

    public ProduitCredit getProduitCredit() {
        return produitCredit;
    }

    public void setProduitCredit(ProduitCredit produitCredit) {
        this.produitCredit = produitCredit;
    }

    @Override
    public String toString() {
        return "ProduitCreditAgence("
            + "id=" + id
            + ")";
    }
}
