package com.microfina.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Virement – opération de virement bancaire (sous-type de {@link OperationBanque}).
 *
 * Supporte les virements intra-bancaires (entre comptes de la même banque) et
 * inter-bancaires (entre banques différentes), selon {@link TypeVirement}.
 *
 * DDL source of truth: P6-010-CREATE-TABLE-Virement.xml.
 * Spec: cahier §6 (Module Banque – virements bancaires).
 */
@Entity
@Table(name = "Virement")
@PrimaryKeyJoinColumn(
    name       = "id",
    foreignKey = @ForeignKey(name = "FK_Virement_OperationBanque")
)
@DiscriminatorValue("Virement")
@DynamicUpdate
public class Virement extends OperationBanque implements Serializable {

    private static final long serialVersionUID = 1L;

    // ── Champs métier ─────────────────────────────────────────────

    /** Nature du virement : intra ou inter-bancaire. */
    @Enumerated(EnumType.STRING)
    @Column(name = "type_virement", length = 15, nullable = false)
    private TypeVirement typeVirement;

    /** Référence externe (n° de virement chez la banque correspondante). */
    @Size(max = 100)
    @Column(name = "reference_externe", length = 100)
    private String referenceExterne;

    /** Motif ou libellé du virement. */
    @Size(max = 255)
    @Column(name = "motif", length = 255)
    private String motif;

    // ── Associations ──────────────────────────────────────────────

    /**
     * Compte bancaire débiteur (source des fonds).
     * FK vers CompteBanque(id).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name       = "compte_source_id",
        foreignKey = @ForeignKey(name = "FK_Virement_CompteSource")
    )
    private CompteBanque compteSource;

    /**
     * Compte bancaire créditeur (destination des fonds).
     * FK vers CompteBanque(id).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name       = "compte_destination_id",
        foreignKey = @ForeignKey(name = "FK_Virement_CompteDest")
    )
    private CompteBanque compteDestination;

    // ── Constructeurs ─────────────────────────────────────────────

    public Virement() {
        super();
    }

    public Virement(Long id, LocalDate dateOperation, BigDecimal montant,
                    StatutOperationBanque statut, String utilisateur, Integer version,
                    CompteBanque compteBanque, Agence agence, Comptabilite comptabilite,
                    TypeVirement typeVirement, String referenceExterne, String motif,
                    CompteBanque compteSource, CompteBanque compteDestination) {
        super(id, dateOperation, montant, statut, utilisateur, version,
              compteBanque, agence, comptabilite);
        this.typeVirement = typeVirement;
        this.referenceExterne = referenceExterne;
        this.motif = motif;
        this.compteSource = compteSource;
        this.compteDestination = compteDestination;
    }

    // ── Accesseurs ────────────────────────────────────────────────

    public TypeVirement getTypeVirement() { return typeVirement; }
    public void setTypeVirement(TypeVirement typeVirement) { this.typeVirement = typeVirement; }

    public String getReferenceExterne() { return referenceExterne; }
    public void setReferenceExterne(String referenceExterne) { this.referenceExterne = referenceExterne; }

    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }

    public CompteBanque getCompteSource() { return compteSource; }
    public void setCompteSource(CompteBanque compteSource) { this.compteSource = compteSource; }

    public CompteBanque getCompteDestination() { return compteDestination; }
    public void setCompteDestination(CompteBanque compteDestination) { this.compteDestination = compteDestination; }

    @Override
    public String toString() {
        return "Virement(id=" + getId() + ", typeVirement=" + typeVirement
            + ", montant=" + getMontant() + ")";
    }
}
