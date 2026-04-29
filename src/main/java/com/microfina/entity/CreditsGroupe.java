package com.microfina.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * CreditsGroupe – JOINED inheritance subtype of {@link Credits} for
 * group / solidarity loans.
 *
 * Adds group-specific columns to the base Credit record:
 *   • Number of participating members
 *   • Individual amount per member
 *   • Group type classification (SOLIDAIRE / CONJOINT)
 *
 * Per-member individual breakdowns are stored in {@link CreditGroupeMembre}.
 *
 * DDL source of truth: P4-002-CREATE-TABLE-Creditsgroupe.xml.
 * Spec: Phase 4, Section 8.
 */
@Entity
@Table(name = "Creditsgroupe")
@PrimaryKeyJoinColumn(
    name               = "IDCREDIT",
    referencedColumnName = "IDCREDIT",
    foreignKey         = @ForeignKey(name = "FK_Creditsgroupe_Credits")
)
@DiscriminatorValue("CreditsGroupe")
@DynamicUpdate
public class CreditsGroupe extends Credits implements Serializable {

    private static final long serialVersionUID = 1L;

    // ── Group-specific columns ────────────────────────────────────

    /** Total number of members sharing this group loan. */
    @Column(name = "NOMBRE_MEMBRES")
    private Integer nombreMembres;

    /**
     * Individual disbursement amount per member.
     * Default = MONTANT_ACCORDE / NOMBRE_MEMBRES;
     * may vary per member via {@link CreditGroupeMembre#getMontantIndividuel()}.
     */
    @Column(name = "MONTANT_PAR_MEMBRE", precision = 19, scale = 4)
    private BigDecimal montantParMembre;

    /**
     * Group solidarity type.
     * SOLIDAIRE = joint and several liability (all members liable for all).
     * CONJOINT   = each member liable for their own share only.
     */
    @Size(max = 30)
    @Column(name = "TYPE_GROUPE", length = 30)
    private String typeGroupe;

    // VERSION inherited from Credits root (JOINED – no duplicate column).

    // ── Generated (was Lombok) ──────────────────────────────────────────────

    public CreditsGroupe() {
    }

    public Integer getNombreMembres() {
        return nombreMembres;
    }

    public void setNombreMembres(Integer nombreMembres) {
        this.nombreMembres = nombreMembres;
    }

    public BigDecimal getMontantParMembre() {
        return montantParMembre;
    }

    public void setMontantParMembre(BigDecimal montantParMembre) {
        this.montantParMembre = montantParMembre;
    }

    public String getTypeGroupe() {
        return typeGroupe;
    }

    public void setTypeGroupe(String typeGroupe) {
        this.typeGroupe = typeGroupe;
    }

    @Override
    public String toString() {
        return "CreditsGroupe(" + super.toString() + ", "
            + 
", nombreMembres=" + nombreMembres            + ", montantParMembre=" + montantParMembre            + ", typeGroupe=" + typeGroupe
            + ")";
    }
}
