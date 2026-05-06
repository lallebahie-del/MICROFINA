package com.microfina.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * RemiseCheque – présentation d'un chèque à l'encaissement (sous-type de {@link OperationBanque}).
 *
 * Une remise de chèque fait transiter un {@link Cheque} de l'état EMIS vers
 * ENCAISSE (ou REJETE en cas d'impayé). La date valeur détermine la date
 * effective de crédit en comptabilité.
 *
 * DDL source of truth: P6-011-CREATE-TABLE-RemiseCheque.xml.
 * Spec: cahier §6 (Module Banque – remise de chèques).
 */
@Entity
@Table(name = "RemiseCheque")
@PrimaryKeyJoinColumn(
    name       = "id",
    foreignKey = @ForeignKey(name = "FK_RemiseCheque_OperationBanque")
)
@DiscriminatorValue("RemiseCheque")
@DynamicUpdate
public class RemiseCheque extends OperationBanque implements Serializable {

    private static final long serialVersionUID = 1L;

    // ── Champs métier ─────────────────────────────────────────────

    /** Nom ou code de la banque ayant présenté le chèque. */
    @Size(max = 255)
    @Column(name = "banque_presentatrice", length = 255)
    private String banquePresentatrice;

    /** Date de valeur : date effective de crédit au compte. */
    @Column(name = "date_valeur")
    private LocalDate dateValeur;

    // ── Associations ──────────────────────────────────────────────

    /**
     * Chèque objet de la remise.
     * FK vers Cheque(id).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name       = "cheque_id",
        foreignKey = @ForeignKey(name = "FK_RemiseCheque_Cheque")
    )
    private Cheque cheque;

    // ── Constructeurs ─────────────────────────────────────────────

    public RemiseCheque() {
        super();
    }

    public RemiseCheque(Long id, LocalDate dateOperation, BigDecimal montant,
                        StatutOperationBanque statut, String utilisateur, Integer version,
                        CompteBanque compteBanque, Agence agence, Comptabilite comptabilite,
                        String banquePresentatrice, LocalDate dateValeur, Cheque cheque) {
        super(id, dateOperation, montant, statut, utilisateur, version,
              compteBanque, agence, comptabilite);
        this.banquePresentatrice = banquePresentatrice;
        this.dateValeur = dateValeur;
        this.cheque = cheque;
    }

    // ── Accesseurs ────────────────────────────────────────────────

    public String getBanquePresentatrice() { return banquePresentatrice; }
    public void setBanquePresentatrice(String banquePresentatrice) { this.banquePresentatrice = banquePresentatrice; }

    public LocalDate getDateValeur() { return dateValeur; }
    public void setDateValeur(LocalDate dateValeur) { this.dateValeur = dateValeur; }

    public Cheque getCheque() { return cheque; }
    public void setCheque(Cheque cheque) { this.cheque = cheque; }

    @Override
    public String toString() {
        return "RemiseCheque(id=" + getId() + ", dateValeur=" + dateValeur
            + ", montant=" + getMontant() + ")";
    }
}
