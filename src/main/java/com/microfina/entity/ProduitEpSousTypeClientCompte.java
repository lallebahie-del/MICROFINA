package com.microfina.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;

/**
 * ProduitEpSousTypeClientCompte – maps a savings product to the
 * sub-types of client it supports, and defines the account-number
 * format / prefix for each (product, sub-type) combination.
 *
 * Unique constraint: one row per (numproduit, SOUS_TYPE_CLIENT).
 *
 * DDL source of truth: P3-008-CREATE-TABLE-ProduitEPSousTypeClientCompte.xml.
 * Spec: Phase 3, Section 2.
 */
@Entity
@Table(
    name = "ProduitEPSousTypeClientCompte",
    uniqueConstraints = @UniqueConstraint(
        name        = "UQ_ProduitEPSousTypeClientCompte_prod_type",
        columnNames = {"numproduit", "SOUS_TYPE_CLIENT"}
    )
)
@DynamicUpdate
public class ProduitEpSousTypeClientCompte implements Serializable {

    private static final long serialVersionUID = 1L;

    // ── PK ────────────────────────────────────────────────────────

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IDPRODUITEPCLIENT", nullable = false)
    private Long idProduitEpClient;

    // ── Client sub-type ───────────────────────────────────────────

    /**
     * Sub-type of client account.
     * PP = Personne Physique, PM = Personne Morale, GS = Groupe Solidaire.
     */
    @Size(max = 50)
    @Column(name = "SOUS_TYPE_CLIENT", length = 50)
    private String sousTypeClient;

    // ── Account-number generation ─────────────────────────────────

    /** Account-number prefix applied when generating new account numbers. */
    @Size(max = 20)
    @Column(name = "PREFIXE_NUMCOMPTE", length = 20)
    private String prefixeNumCompte;

    /** Sequence/pattern name for auto-generated account numbers. */
    @Size(max = 50)
    @Column(name = "SEQUENCE_COMPTE", length = 50)
    private String sequenceCompte;

    /** 1 = combination is active. */
    @Column(name = "ACTIF")
    private Integer actif = 1;

    // ── Optimistic lock ───────────────────────────────────────────

    @Version
    @Column(name = "VERSION")
    private Integer version = 0;

    // ── FK ────────────────────────────────────────────────────────

    /**
     * Savings product.
     * FK to PRODUITEPARGNE(NUMPRODUIT).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name                 = "numproduit",
        referencedColumnName = "NUMPRODUIT",
        foreignKey           = @ForeignKey(name = "FK_ProduitEPSousTypeClientCompte_PRODUITEPARGNE")
    )
    private ProduitEpargne produitEpargne;

    // ── Generated (was Lombok) ──────────────────────────────────────────────

    public ProduitEpSousTypeClientCompte() {
    }

    public ProduitEpSousTypeClientCompte(Long idProduitEpClient, String sousTypeClient, String prefixeNumCompte, String sequenceCompte, Integer actif, Integer version, ProduitEpargne produitEpargne) {
        this.idProduitEpClient = idProduitEpClient;
        this.sousTypeClient = sousTypeClient;
        this.prefixeNumCompte = prefixeNumCompte;
        this.sequenceCompte = sequenceCompte;
        this.actif = actif;
        this.version = version;
        this.produitEpargne = produitEpargne;
    }

    public Long getIdProduitEpClient() {
        return idProduitEpClient;
    }

    public void setIdProduitEpClient(Long idProduitEpClient) {
        this.idProduitEpClient = idProduitEpClient;
    }

    public String getSousTypeClient() {
        return sousTypeClient;
    }

    public void setSousTypeClient(String sousTypeClient) {
        this.sousTypeClient = sousTypeClient;
    }

    public String getPrefixeNumCompte() {
        return prefixeNumCompte;
    }

    public void setPrefixeNumCompte(String prefixeNumCompte) {
        this.prefixeNumCompte = prefixeNumCompte;
    }

    public String getSequenceCompte() {
        return sequenceCompte;
    }

    public void setSequenceCompte(String sequenceCompte) {
        this.sequenceCompte = sequenceCompte;
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

    public ProduitEpargne getProduitEpargne() {
        return produitEpargne;
    }

    public void setProduitEpargne(ProduitEpargne produitEpargne) {
        this.produitEpargne = produitEpargne;
    }

    @Override
    public String toString() {
        return "ProduitEpSousTypeClientCompte("
            + "idProduitEpClient=" + idProduitEpClient
            + ")";
    }
}
