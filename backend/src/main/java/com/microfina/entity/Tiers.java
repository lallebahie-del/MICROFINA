package com.microfina.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;

/**
 * TIERS – generic party/entity root (JOINED inheritance).
 *
 * Subtypes mapped with JOINED strategy:
 *   • {@link Personne}     – physical or legal persons (gestionnaires, commerciaux…)
 *   • {@link MembreGroupe} – a member's group-participation record
 *   • {@link Signataire}   – signatory of a corporate member
 *
 * DDL source of truth: P2-001-CREATE-TABLE-TIERS.xml.
 * Spec p.64: CODE_TIERS AN/50 PK, DESIGNATION AN/255,
 *            REFERENCE_TIERS AN/100, DTYPE AN/100, VERSION N.
 */
@Entity
@Table(name = "TIERS")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(
    name              = "DTYPE",
    discriminatorType = DiscriminatorType.STRING,
    columnDefinition  = "NVARCHAR(100)"
)
@DiscriminatorValue("TIERS")
@DynamicUpdate
public class Tiers implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Business key (AN/50). */
    @Id
    @Size(max = 50)
    @Column(name = "CODE_TIERS", length = 50, nullable = false)
    private String codeTiers;

    /** Full designation / name (AN/255). */
    @Size(max = 255)
    @Column(name = "DESIGNATION", length = 255)
    private String designation;

    /** External reference code (AN/100). */
    @Size(max = 100)
    @Column(name = "REFERENCE_TIERS", length = 100)
    private String referenceTiers;

    @Version
    @Column(name = "VERSION")
    private Integer version = 0;

    // ── Generated (was Lombok) ──────────────────────────────────────────────

    public Tiers() {
    }

    public Tiers(String codeTiers, String designation, String referenceTiers, Integer version) {
        this.codeTiers = codeTiers;
        this.designation = designation;
        this.referenceTiers = referenceTiers;
        this.version = version;
    }

    public String getCodeTiers() {
        return codeTiers;
    }

    public void setCodeTiers(String codeTiers) {
        this.codeTiers = codeTiers;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getReferenceTiers() {
        return referenceTiers;
    }

    public void setReferenceTiers(String referenceTiers) {
        this.referenceTiers = referenceTiers;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "Tiers("
            + "codeTiers=" + codeTiers
            + ")";
    }
}
