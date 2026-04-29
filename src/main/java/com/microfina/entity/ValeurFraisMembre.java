package com.microfina.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * ValeurFraisMembre – adhesion fee snapshot per member.
 *
 * Created when a member enrols; each row freezes the computed fee amount
 * for one {@link ValeurFrais} definition against the enrolling member.
 * Used during adhesion validation to generate the accounting entries.
 *
 * Unique constraint: one fee-value row per (member, fee definition).
 *
 * DDL source of truth: P2-009-CREATE-TABLE-ValeurFraisMembre.xml.
 * Spec: inferred from Adhésion diagram p.74 and description p.75.
 */
@Entity
@Table(
    name = "ValeurFraisMembre",
    uniqueConstraints = @UniqueConstraint(
        name        = "UQ_ValeurFraisMembre_membre_frais",
        columnNames = {"nummembre", "valeurfrais"}
    )
)
@DynamicUpdate
public class ValeurFraisMembre implements Serializable {

    private static final long serialVersionUID = 1L;

    // ── PK ────────────────────────────────────────────────────────

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IDVALEURFRAISMEMBRE", nullable = false)
    private Long idValeurFraisMembre;

    // ── Fee amount frozen at enrolment ────────────────────────────

    /** Computed fee amount (DECIMAL 19,4). */
    @Column(name = "MONTANT", precision = 19, scale = 4)
    private BigDecimal montant;

    /**
     * Accounting account override (AN/15 – matches ValeurFrais.compte).
     * If null, the account from ValeurFrais is used.
     */
    @Size(max = 15)
    @Column(name = "compte", length = 15)
    private String compte;

    // ── Optimistic lock ───────────────────────────────────────────

    @Version
    @Column(name = "VERSION")
    private Integer version = 0;

    // ── FKs ───────────────────────────────────────────────────────

    /**
     * The enrolling member.
     * FK to membres(NUM_MEMBRE).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name                 = "nummembre",
        referencedColumnName = "NUM_MEMBRE",
        foreignKey           = @ForeignKey(name = "FK_ValeurFraisMembre_membres")
    )
    private Membres membre;

    /**
     * The fee definition from Phase 1.
     * FK to ValeurFrais(IDVALEURFRAIS).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name                 = "valeurfrais",
        referencedColumnName = "IDVALEURFRAIS",
        foreignKey           = @ForeignKey(name = "FK_ValeurFraisMembre_ValeurFrais")
    )
    private ValeurFrais valeurFrais;

    // ── Generated (was Lombok) ──────────────────────────────────────────────

    public ValeurFraisMembre() {
    }

    public ValeurFraisMembre(Long idValeurFraisMembre, BigDecimal montant, String compte, Integer version, Membres membre, ValeurFrais valeurFrais) {
        this.idValeurFraisMembre = idValeurFraisMembre;
        this.montant = montant;
        this.compte = compte;
        this.version = version;
        this.membre = membre;
        this.valeurFrais = valeurFrais;
    }

    public Long getIdValeurFraisMembre() {
        return idValeurFraisMembre;
    }

    public void setIdValeurFraisMembre(Long idValeurFraisMembre) {
        this.idValeurFraisMembre = idValeurFraisMembre;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public String getCompte() {
        return compte;
    }

    public void setCompte(String compte) {
        this.compte = compte;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Membres getMembre() {
        return membre;
    }

    public void setMembre(Membres membre) {
        this.membre = membre;
    }

    public ValeurFrais getValeurFrais() {
        return valeurFrais;
    }

    public void setValeurFrais(ValeurFrais valeurFrais) {
        this.valeurFrais = valeurFrais;
    }

    @Override
    public String toString() {
        return "ValeurFraisMembre("
            + "idValeurFraisMembre=" + idValeurFraisMembre
            + ")";
    }
}
