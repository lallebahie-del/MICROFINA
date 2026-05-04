package com.microfina.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * ProduitIslamic – reference table for Islamic finance product types.
 *
 * Supported types include Mourabaha (cost-plus sale), Moucharaka (partnership),
 * and Ijara (leasing). A {@link ProduitCredit} may optionally link to one
 * Islamic product type to classify its financing method.
 *
 * DDL source of truth: P3-002-CREATE-TABLE-produit_islamic.xml.
 * Spec: Phase 3, Section 1.
 */
@Entity
@Table(name = "produit_islamic")
@DynamicUpdate
public class ProduitIslamic implements Serializable {

    private static final long serialVersionUID = 1L;

    // ── PK ────────────────────────────────────────────────────────

    /** Short code, e.g. "MOURABAHA", "MOUCHARAKA", "IJARA". */
    @Id
    @NotBlank
    @Size(max = 50)
    @Column(name = "code_produit", length = 50, nullable = false)
    private String codeProduit;

    // ── Description ───────────────────────────────────────────────

    @Size(max = 200)
    @Column(name = "libelle", length = 200)
    private String libelle;

    @Size(max = 255)
    @Column(name = "description", length = 255)
    private String description;

    // ── Flags ─────────────────────────────────────────────────────

    /** 1 = active. */
    @Column(name = "actif")
    private Integer actif = 1;

    // ── Moucharaka: profit-sharing ratio (%) ─────────────────────

    /** Profit-sharing ratio applied in Moucharaka contracts. */
    @Column(name = "taux_partage_benefice", precision = 19, scale = 4)
    private BigDecimal tauxPartageBenefice;

    // ── Mourabaha / Ijara ratio columns (§3.1.1) ─────────────────

    /**
     * Mourabaha – proportion of the total financing amount that represents
     * the institution's acquisition cost (0 &lt; cost_price_ratio ≤ 1).
     * e.g. 0.85 means 85 % of the contract value is the cost price.
     */
    @Column(name = "cost_price_ratio", precision = 19, scale = 4)
    private BigDecimal costPriceRatio;

    /**
     * Mourabaha – profit mark-up (ribh) applied on top of the cost price,
     * declared transparently to the customer.
     * e.g. 0.15 = 15 % margin over the acquisition cost.
     */
    @Column(name = "markup_ratio", precision = 19, scale = 4)
    private BigDecimal markupRatio;

    /**
     * Ijara – residual (buy-back / balloon) value of the leased asset at
     * contract end, expressed as a ratio of the original asset value.
     * e.g. 0.10 = the asset retains 10 % of its value at term end.
     */
    @Column(name = "residual_value_ratio", precision = 19, scale = 4)
    private BigDecimal residualValueRatio;

    // ── Optimistic lock ───────────────────────────────────────────

    @Version
    @Column(name = "VERSION")
    private Integer version = 0;

    // ── Generated (was Lombok) ──────────────────────────────────────────────

    public ProduitIslamic() {
    }

    public ProduitIslamic(String codeProduit, String libelle, String description, Integer actif, BigDecimal tauxPartageBenefice, BigDecimal costPriceRatio, BigDecimal markupRatio, BigDecimal residualValueRatio, Integer version) {
        this.codeProduit = codeProduit;
        this.libelle = libelle;
        this.description = description;
        this.actif = actif;
        this.tauxPartageBenefice = tauxPartageBenefice;
        this.costPriceRatio = costPriceRatio;
        this.markupRatio = markupRatio;
        this.residualValueRatio = residualValueRatio;
        this.version = version;
    }

    public String getCodeProduit() {
        return codeProduit;
    }

    public void setCodeProduit(String codeProduit) {
        this.codeProduit = codeProduit;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getActif() {
        return actif;
    }

    public void setActif(Integer actif) {
        this.actif = actif;
    }

    public BigDecimal getTauxPartageBenefice() {
        return tauxPartageBenefice;
    }

    public void setTauxPartageBenefice(BigDecimal tauxPartageBenefice) {
        this.tauxPartageBenefice = tauxPartageBenefice;
    }

    public BigDecimal getCostPriceRatio() {
        return costPriceRatio;
    }

    public void setCostPriceRatio(BigDecimal costPriceRatio) {
        this.costPriceRatio = costPriceRatio;
    }

    public BigDecimal getMarkupRatio() {
        return markupRatio;
    }

    public void setMarkupRatio(BigDecimal markupRatio) {
        this.markupRatio = markupRatio;
    }

    public BigDecimal getResidualValueRatio() {
        return residualValueRatio;
    }

    public void setResidualValueRatio(BigDecimal residualValueRatio) {
        this.residualValueRatio = residualValueRatio;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "ProduitIslamic("
            + "codeProduit=" + codeProduit
            + ")";
    }
}
