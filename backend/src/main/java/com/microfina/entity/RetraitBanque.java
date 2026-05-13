package com.microfina.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * RetraitBanque – retrait de fonds depuis un compte bancaire (sous-type de {@link OperationBanque}).
 *
 * Enregistre les sorties de trésorerie opérées sur un {@link CompteBanque}.
 * Le mode de paiement par défaut est "ESPECES" mais peut être "CHEQUE", "VIREMENT", etc.
 *
 * DDL source of truth: P6-012-CREATE-TABLE-RetraitBanque.xml.
 * Spec: cahier §6 (Module Banque – retraits bancaires).
 */
@Entity
@Table(name = "RetraitBanque")
@PrimaryKeyJoinColumn(
    name       = "id",
    foreignKey = @ForeignKey(name = "FK_RetraitBanque_OperationBanque")
)
@DiscriminatorValue("RetraitBanque")
@DynamicUpdate
public class RetraitBanque extends OperationBanque implements Serializable {

    private static final long serialVersionUID = 1L;

    // ── Champs métier ─────────────────────────────────────────────

    /** Motif ou libellé du retrait. */
    @Size(max = 255)
    @Column(name = "motif", length = 255)
    private String motif;

    /** Mode de décaissement utilisé (défaut : ESPECES). */
    @Size(max = 30)
    @Column(name = "mode_paiement", length = 30, nullable = false)
    private String modePaiement = "ESPECES";

    // ── Constructeurs ─────────────────────────────────────────────

    public RetraitBanque() {
        super();
    }

    public RetraitBanque(Long id, LocalDate dateOperation, BigDecimal montant,
                         StatutOperationBanque statut, String utilisateur, Integer version,
                         CompteBanque compteBanque, Agence agence, Comptabilite comptabilite,
                         String motif, String modePaiement) {
        super(id, dateOperation, montant, statut, utilisateur, version,
              compteBanque, agence, comptabilite);
        this.motif = motif;
        this.modePaiement = modePaiement;
    }

    // ── Accesseurs ────────────────────────────────────────────────

    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }

    public String getModePaiement() { return modePaiement; }
    public void setModePaiement(String modePaiement) { this.modePaiement = modePaiement; }

    @Override
    public String toString() {
        return "RetraitBanque(id=" + getId() + ", montant=" + getMontant()
            + ", modePaiement=" + modePaiement + ")";
    }
}
