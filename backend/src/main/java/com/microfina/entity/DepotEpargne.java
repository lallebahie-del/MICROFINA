package com.microfina.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DepotEpargne – versement d'épargne au guichet caisse (sous-type de {@link OperationCaisse}).
 *
 * Sous-type minimal : tous les champs métier de l'opération sont portés par
 * la table parente {@code OperationCaisse}. Cette table ne contient que la
 * clé primaire partagée (héritage JOINED).
 *
 * DDL source of truth: P7-002-CREATE-TABLE-DepotEpargne.xml.
 * Spec: cahier §3.1.1 (Opérations de caisse – dépôts épargne).
 */
@Entity
@Table(name = "DepotEpargne")
@PrimaryKeyJoinColumn(
    name       = "id",
    foreignKey = @ForeignKey(name = "FK_DepotEpargne_OperationCaisse")
)
@DiscriminatorValue("DepotEpargne")
@DynamicUpdate
public class DepotEpargne extends OperationCaisse implements Serializable {

    private static final long serialVersionUID = 1L;

    // ── Constructeurs ─────────────────────────────────────────────

    public DepotEpargne() {
        super();
    }

    public DepotEpargne(Long id, String numPiece, LocalDate dateOperation,
                        BigDecimal montant, ModePaiementCaisse modePaiement,
                        String motif, String utilisateur,
                        StatutOperationCaisse statut, Integer version,
                        CompteEps compteEps, Agence agence,
                        Comptabilite comptabilite) {
        super(id, numPiece, dateOperation, montant, modePaiement, motif,
              utilisateur, statut, version, compteEps, agence, comptabilite);
    }

    @Override
    public String toString() {
        return "DepotEpargne(id=" + getId()
            + ", numPiece=" + getNumPiece()
            + ", montant=" + getMontant() + ")";
    }
}
