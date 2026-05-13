package com.microfina.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.util.Objects;

/**
 * ProduitCreditTypeMembreId – 2-column composite PK for
 * {@link ProduitCreditTypeMembre}.
 *
 * Columns: (numproduit, typemembre).
 *
 * DDL source of truth: P3-005-CREATE-TABLE-ProduitCreditTypeMembre.xml.
 */
@Embeddable
public class ProduitCreditTypeMembreId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Size(max = 20)
    @Column(name = "numproduit", length = 20, nullable = false)
    private String numProduit;

    /**
     * Member type code, e.g. PP=Personne Physique, PM=Personne Morale,
     * GS=Groupe Solidaire.
     */
    @Size(max = 50)
    @Column(name = "typemembre", length = 50, nullable = false)
    private String typeMembre;

    // ── equals / hashCode required for @EmbeddedId ────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProduitCreditTypeMembreId that)) return false;
        return Objects.equals(numProduit, that.numProduit)
            && Objects.equals(typeMembre, that.typeMembre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(numProduit, typeMembre);
    }

    // ── Generated (was Lombok) ──────────────────────────────────────────────

    public ProduitCreditTypeMembreId() {
    }

    public ProduitCreditTypeMembreId(String numProduit, String typeMembre) {
        this.numProduit = numProduit;
        this.typeMembre = typeMembre;
    }

    public String getNumProduit() {
        return numProduit;
    }

    public void setNumProduit(String numProduit) {
        this.numProduit = numProduit;
    }

    public String getTypeMembre() {
        return typeMembre;
    }

    public void setTypeMembre(String typeMembre) {
        this.typeMembre = typeMembre;
    }
}
