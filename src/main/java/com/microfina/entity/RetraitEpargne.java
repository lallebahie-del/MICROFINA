package com.microfina.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * RetraitEpargne – retrait d'épargne au guichet caisse (sous-type de {@link OperationCaisse}).
 *
 * Sous-type minimal : tous les champs métier de l'opération sont portés par
 * la table parente {@code OperationCaisse}. Cette table ne contient que la
 * clé primaire partagée (héritage JOINED).
 *
 * DDL source of truth: P7-003-CREATE-TABLE-RetraitEpargne.xml.
 * Spec: cahier §3.1.1 (Opérations de caisse – retraits épargne).
 */
@Entity
@Table(name = "RetraitEpargne")
@PrimaryKeyJoinColumn(
    name       = "id",
    foreignKey = @ForeignKey(name = "FK_RetraitEpargne_OperationCaisse")
)
@DiscriminatorValue("RetraitEpargne")
@DynamicUpdate
public class RetraitEpargne extends OperationCaisse implements Serializable {

    private static final long serialVersionUID = 1L;

    // ── Constructeurs ─────────────────────────────────────────────

    public RetraitEpargne() {
        super();
    }

    public RetraitEpargne(Long id, String numPiece, LocalDate dateOperation,
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
        return "RetraitEpargne(id=" + getId()
            + ", numPiece=" + getNumPiece()
            + ", montant=" + getMontant() + ")";
    }
}
