package com.microfina.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.util.Objects;

/**
 * ProduitCreditAgenceId – 3-column composite PK for {@link ProduitCreditAgence}.
 *
 * Columns: (CODE_AGENCE, NUMPDTCREDIT, CODE_OBJET_CREDIT).
 *
 * DDL source of truth: P3-004-CREATE-TABLE-PRODUIT_CREDIT_AGENCE.xml.
 */
@Embeddable
public class ProduitCreditAgenceId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Size(max = 50)
    @Column(name = "CODE_AGENCE", length = 50, nullable = false)
    private String codeAgence;

    @Size(max = 20)
    @Column(name = "NUMPDTCREDIT", length = 20, nullable = false)
    private String numPdtCredit;

    /**
     * Credit object code (e.g. "HAB"=Habitat, "AGR"=Agriculture).
     * May be an empty string when the product applies to all objects.
     */
    @Size(max = 50)
    @Column(name = "CODE_OBJET_CREDIT", length = 50, nullable = false)
    private String codeObjetCredit;

    // ── equals / hashCode required for @EmbeddedId ────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProduitCreditAgenceId that)) return false;
        return Objects.equals(codeAgence, that.codeAgence)
            && Objects.equals(numPdtCredit, that.numPdtCredit)
            && Objects.equals(codeObjetCredit, that.codeObjetCredit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codeAgence, numPdtCredit, codeObjetCredit);
    }

    // ── Generated (was Lombok) ──────────────────────────────────────────────

    public ProduitCreditAgenceId() {
    }

    public ProduitCreditAgenceId(String codeAgence, String numPdtCredit, String codeObjetCredit) {
        this.codeAgence = codeAgence;
        this.numPdtCredit = numPdtCredit;
        this.codeObjetCredit = codeObjetCredit;
    }

    public String getCodeAgence() {
        return codeAgence;
    }

    public void setCodeAgence(String codeAgence) {
        this.codeAgence = codeAgence;
    }

    public String getNumPdtCredit() {
        return numPdtCredit;
    }

    public void setNumPdtCredit(String numPdtCredit) {
        this.numPdtCredit = numPdtCredit;
    }

    public String getCodeObjetCredit() {
        return codeObjetCredit;
    }

    public void setCodeObjetCredit(String codeObjetCredit) {
        this.codeObjetCredit = codeObjetCredit;
    }
}
