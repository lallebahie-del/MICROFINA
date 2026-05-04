package com.microfina.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * FraisAdhesion – encaissement des frais d'adhésion membre (sous-type de {@link OperationCaisse}).
 *
 * Enregistre le paiement des frais calculés dans {@link ValeurFraisMembre}.
 * La relation vers {@link CompteEps} héritée de {@link OperationCaisse} est NULLABLE
 * car le membre peut ne pas encore disposer d'un compte épargne au moment
 * de l'encaissement des frais d'adhésion.
 *
 * DDL source of truth: P7-004-CREATE-TABLE-FraisAdhesion.xml.
 * Spec: cahier §3.1.1 (Opérations de caisse – frais d'adhésion).
 */
@Entity
@Table(name = "FraisAdhesion")
@PrimaryKeyJoinColumn(
    name       = "id",
    foreignKey = @ForeignKey(name = "FK_FraisAdhesion_OperationCaisse")
)
@DiscriminatorValue("FraisAdhesion")
@DynamicUpdate
public class FraisAdhesion extends OperationCaisse implements Serializable {

    private static final long serialVersionUID = 1L;

    // ── Champs métier ─────────────────────────────────────────────

    /**
     * Barème de frais d'adhésion appliqué à ce membre.
     * FK vers ValeurFraisMembre(IDVALEURFRAISMEMBRE). NULLABLE.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name       = "id_valeur_frais_membre",
        foreignKey = @ForeignKey(name = "FK_FraisAdhesion_ValeurFraisMembre")
    )
    private ValeurFraisMembre valeurFraisMembre;

    // ── Constructeurs ─────────────────────────────────────────────

    public FraisAdhesion() {
        super();
    }

    public FraisAdhesion(Long id, String numPiece, LocalDate dateOperation,
                         BigDecimal montant, ModePaiementCaisse modePaiement,
                         String motif, String utilisateur,
                         StatutOperationCaisse statut, Integer version,
                         CompteEps compteEps, Agence agence,
                         Comptabilite comptabilite,
                         ValeurFraisMembre valeurFraisMembre) {
        super(id, numPiece, dateOperation, montant, modePaiement, motif,
              utilisateur, statut, version, compteEps, agence, comptabilite);
        this.valeurFraisMembre = valeurFraisMembre;
    }

    // ── Accesseurs ────────────────────────────────────────────────

    public ValeurFraisMembre getValeurFraisMembre() { return valeurFraisMembre; }
    public void setValeurFraisMembre(ValeurFraisMembre valeurFraisMembre) {
        this.valeurFraisMembre = valeurFraisMembre;
    }

    @Override
    public String toString() {
        return "FraisAdhesion(id=" + getId()
            + ", numPiece=" + getNumPiece()
            + ", montant=" + getMontant()
            + ", valeurFraisMembre=" + (valeurFraisMembre != null ? valeurFraisMembre.getIdValeurFraisMembre() : null)
            + ")";
    }
}
