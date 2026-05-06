package com.microfina.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;

/**
 * ModelCompteProduitCredit – accounting model template for a credit product.
 *
 * Drives the chart-of-accounts entries generated when a credit operation
 * (disbursement, repayment, penalty, etc.) is posted to the ledger.
 *
 * DDL source of truth: P3-001-CREATE-TABLE-Model_Compte_Produit_Credit.xml.
 * Spec: Phase 3, Section 1.
 */
@Entity
@Table(name = "Model_Compte_Produit_Credit")
@DynamicUpdate
public class ModelCompteProduitCredit implements Serializable {

    private static final long serialVersionUID = 1L;

    // ── PK ────────────────────────────────────────────────────────

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_MODEL_PRODUIT_CREDIT", nullable = false)
    private Long idModelProduitCredit;

    // ── Label ─────────────────────────────────────────────────────

    @Size(max = 255)
    @Column(name = "LIBELLEMODELPRODUITCREDIT", length = 255)
    private String libelleModelProduitCredit;

    // ── Flags ─────────────────────────────────────────────────────

    /**
     * 1 = the accounting model is backed by an earmarked/restricted resource
     * (ressource affectée).
     */
    @Column(name = "ressource_affecte")
    private Integer ressourceAffecte;

    /**
     * 1 = the accounting model follows Islamic-finance principles
     * (Mourabaha / Moucharaka / Ijara).
     */
    @Column(name = "model_islamic")
    private Integer modelIslamic;

    // ── Optimistic lock ───────────────────────────────────────────

    @Version
    @Column(name = "VERSION")
    private Integer version = 0;

    // ── Generated (was Lombok) ──────────────────────────────────────────────

    public ModelCompteProduitCredit() {
    }

    public ModelCompteProduitCredit(Long idModelProduitCredit, String libelleModelProduitCredit, Integer ressourceAffecte, Integer modelIslamic, Integer version) {
        this.idModelProduitCredit = idModelProduitCredit;
        this.libelleModelProduitCredit = libelleModelProduitCredit;
        this.ressourceAffecte = ressourceAffecte;
        this.modelIslamic = modelIslamic;
        this.version = version;
    }

    public Long getIdModelProduitCredit() {
        return idModelProduitCredit;
    }

    public void setIdModelProduitCredit(Long idModelProduitCredit) {
        this.idModelProduitCredit = idModelProduitCredit;
    }

    public String getLibelleModelProduitCredit() {
        return libelleModelProduitCredit;
    }

    public void setLibelleModelProduitCredit(String libelleModelProduitCredit) {
        this.libelleModelProduitCredit = libelleModelProduitCredit;
    }

    public Integer getRessourceAffecte() {
        return ressourceAffecte;
    }

    public void setRessourceAffecte(Integer ressourceAffecte) {
        this.ressourceAffecte = ressourceAffecte;
    }

    public Integer getModelIslamic() {
        return modelIslamic;
    }

    public void setModelIslamic(Integer modelIslamic) {
        this.modelIslamic = modelIslamic;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "ModelCompteProduitCredit("
            + "idModelProduitCredit=" + idModelProduitCredit
            + ")";
    }
}
