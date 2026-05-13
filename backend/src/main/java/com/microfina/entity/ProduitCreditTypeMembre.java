package com.microfina.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;

/**
 * ProduitCreditTypeMembre – restricts a credit product to eligible member types.
 *
 * Each row indicates that the credit product identified by
 * {@link ProduitCreditTypeMembreId#getNumProduit()} is accessible to
 * members of type {@link ProduitCreditTypeMembreId#getTypeMembre()}.
 *
 * Composite PK: see {@link ProduitCreditTypeMembreId} (numproduit, typemembre).
 *
 * DDL source of truth: P3-005-CREATE-TABLE-ProduitCreditTypeMembre.xml.
 * Spec: Phase 3, Section 1.
 */
@Entity
@Table(name = "ProduitCreditTypeMembre")
@DynamicUpdate
public class ProduitCreditTypeMembre implements Serializable {

    private static final long serialVersionUID = 1L;

    // ── Composite PK ─────────────────────────────────────────────

    @EmbeddedId
    private ProduitCreditTypeMembreId id;

    // ── Optional label ────────────────────────────────────────────

    @Size(max = 255)
    @Column(name = "libelle_type_membre", length = 255)
    private String libelleTypeMembre;

    /** 1 = combination is active. */
    @Column(name = "actif")
    private Integer actif = 1;

    // ── Optimistic lock ───────────────────────────────────────────

    @Version
    @Column(name = "VERSION")
    private Integer version = 0;

    // ── FK ────────────────────────────────────────────────────────

    /**
     * Credit product.
     * FK to produitcredit(NUMPRODUIT).
     * Maps via composite PK column – insertable/updatable=false.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name                 = "numproduit",
        referencedColumnName = "NUMPRODUIT",
        insertable           = false,
        updatable            = false,
        foreignKey           = @ForeignKey(name = "FK_ProduitCreditTypeMembre_produitcredit")
    )
    private ProduitCredit produitCredit;

    // ── Generated (was Lombok) ──────────────────────────────────────────────

    public ProduitCreditTypeMembre() {
    }

    public ProduitCreditTypeMembre(ProduitCreditTypeMembreId id, String libelleTypeMembre, Integer actif, Integer version, ProduitCredit produitCredit) {
        this.id = id;
        this.libelleTypeMembre = libelleTypeMembre;
        this.actif = actif;
        this.version = version;
        this.produitCredit = produitCredit;
    }

    public ProduitCreditTypeMembreId getId() {
        return id;
    }

    public void setId(ProduitCreditTypeMembreId id) {
        this.id = id;
    }

    public String getLibelleTypeMembre() {
        return libelleTypeMembre;
    }

    public void setLibelleTypeMembre(String libelleTypeMembre) {
        this.libelleTypeMembre = libelleTypeMembre;
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

    public ProduitCredit getProduitCredit() {
        return produitCredit;
    }

    public void setProduitCredit(ProduitCredit produitCredit) {
        this.produitCredit = produitCredit;
    }

    @Override
    public String toString() {
        return "ProduitCreditTypeMembre("
            + "id=" + id
            + ")";
    }
}
