package com.microfina.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;

/**
 * CATEGORIE_PRODUIT_CREDIT – maps credit product categories to specific
 * credit products. Uses a composite PK (codeCategorie + codeProduitCredit).
 */
@Entity
@Table(name = "CATEGORIE_PRODUIT_CREDIT")
@DynamicUpdate
public class CategorieProduitCredit implements Serializable {

    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private CategorieProduitCreditId id;

    @Column(name = "ACTIF", nullable = false)
    private Boolean actif = true;

    @Version
    @Column(name = "VERSION")
    private Integer version = 0;

    // ── Generated (was Lombok) ──────────────────────────────────────────────

    public CategorieProduitCredit() {
    }

    public CategorieProduitCredit(CategorieProduitCreditId id, Boolean actif, Integer version) {
        this.id = id;
        this.actif = actif;
        this.version = version;
    }

    public CategorieProduitCreditId getId() {
        return id;
    }

    public void setId(CategorieProduitCreditId id) {
        this.id = id;
    }

    public Boolean getActif() {
        return actif;
    }

    public void setActif(Boolean actif) {
        this.actif = actif;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "CategorieProduitCredit("
            + "id=" + id
            + ")";
    }
}
