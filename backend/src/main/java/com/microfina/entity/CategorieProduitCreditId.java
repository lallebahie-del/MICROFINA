package com.microfina.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

/**
 * Composite primary key for CATEGORIE_PRODUIT_CREDIT.
 * Combines the category code and the credit product code.
 */
@Embeddable
public class CategorieProduitCreditId implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank
    @Size(max = 25)
    @Column(name = "CODE_CATEGORIE", length = 25, nullable = false)
    private String codeCategorie;

    @NotBlank
    @Size(max = 20)
    @Column(name = "CODE_PRODUIT_CREDIT", length = 20, nullable = false)
    private String codeProduitCredit;

    // ── Generated (was Lombok) ──────────────────────────────────────────────

    public CategorieProduitCreditId() {
    }

    public CategorieProduitCreditId(String codeCategorie, String codeProduitCredit) {
        this.codeCategorie = codeCategorie;
        this.codeProduitCredit = codeProduitCredit;
    }

    public String getCodeCategorie() {
        return codeCategorie;
    }

    public void setCodeCategorie(String codeCategorie) {
        this.codeCategorie = codeCategorie;
    }

    public String getCodeProduitCredit() {
        return codeProduitCredit;
    }

    public void setCodeProduitCredit(String codeProduitCredit) {
        this.codeProduitCredit = codeProduitCredit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CategorieProduitCreditId)) return false;
        CategorieProduitCreditId other = (CategorieProduitCreditId) o;
        return             java.util.Objects.equals(this.codeCategorie, other.codeCategorie) &&
            java.util.Objects.equals(this.codeProduitCredit, other.codeProduitCredit);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(this.codeCategorie, this.codeProduitCredit);
    }
}
