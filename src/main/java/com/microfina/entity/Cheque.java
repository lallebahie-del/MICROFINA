package com.microfina.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Cheque – feuillet de chèque individuel extrait d'un {@link CarnetCheque}.
 *
 * Son cycle de vie est décrit par {@link StatutCheque}.
 * Lorsqu'il est présenté à l'encaissement, une {@link RemiseCheque} est créée.
 *
 * DDL source of truth: P6-008-CREATE-TABLE-Cheque.xml.
 * Spec: cahier §6 (Module Banque – chèques individuels).
 */
@Entity
@Table(name = "Cheque")
@DynamicUpdate
public class Cheque implements Serializable {

    private static final long serialVersionUID = 1L;

    // ── PK ────────────────────────────────────────────────────────

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    // ── Champs métier ─────────────────────────────────────────────

    /** Numéro imprimé sur le chèque (séquentiel dans le carnet). */
    @Size(max = 20)
    @Column(name = "numero", length = 20, nullable = false)
    private String numero;

    /** Montant libellé sur le chèque. */
    @Column(name = "montant", precision = 19, scale = 4)
    private BigDecimal montant;

    /** Nom du bénéficiaire inscrit sur le chèque. */
    @Size(max = 255)
    @Column(name = "beneficiaire", length = 255)
    private String beneficiaire;

    /** Date d'émission du chèque. */
    @Column(name = "date_emission")
    private LocalDate dateEmission;

    /** Date effective d'encaissement par le bénéficiaire. */
    @Column(name = "date_encaissement")
    private LocalDate dateEncaissement;

    /** État courant du chèque. */
    @Enumerated(EnumType.STRING)
    @Column(name = "statut", length = 10, nullable = false)
    private StatutCheque statut = StatutCheque.EMIS;

    // ── Optimistic lock ───────────────────────────────────────────

    @Version
    @Column(name = "version")
    private Integer version = 0;

    // ── Associations ──────────────────────────────────────────────

    /**
     * Carnet auquel appartient ce feuillet.
     * FK vers CarnetCheque(id).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name       = "carnet_cheque_id",
        foreignKey = @ForeignKey(name = "FK_Cheque_CarnetCheque")
    )
    private CarnetCheque carnetCheque;

    // ── Constructeurs ─────────────────────────────────────────────

    public Cheque() {
    }

    public Cheque(Long id, String numero, BigDecimal montant, String beneficiaire,
                  LocalDate dateEmission, LocalDate dateEncaissement,
                  StatutCheque statut, Integer version, CarnetCheque carnetCheque) {
        this.id = id;
        this.numero = numero;
        this.montant = montant;
        this.beneficiaire = beneficiaire;
        this.dateEmission = dateEmission;
        this.dateEncaissement = dateEncaissement;
        this.statut = statut;
        this.version = version;
        this.carnetCheque = carnetCheque;
    }

    // ── Accesseurs ────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public BigDecimal getMontant() { return montant; }
    public void setMontant(BigDecimal montant) { this.montant = montant; }

    public String getBeneficiaire() { return beneficiaire; }
    public void setBeneficiaire(String beneficiaire) { this.beneficiaire = beneficiaire; }

    public LocalDate getDateEmission() { return dateEmission; }
    public void setDateEmission(LocalDate dateEmission) { this.dateEmission = dateEmission; }

    public LocalDate getDateEncaissement() { return dateEncaissement; }
    public void setDateEncaissement(LocalDate dateEncaissement) { this.dateEncaissement = dateEncaissement; }

    public StatutCheque getStatut() { return statut; }
    public void setStatut(StatutCheque statut) { this.statut = statut; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public CarnetCheque getCarnetCheque() { return carnetCheque; }
    public void setCarnetCheque(CarnetCheque carnetCheque) { this.carnetCheque = carnetCheque; }

    @Override
    public String toString() {
        return "Cheque(id=" + id + ", numero=" + numero + ")";
    }
}
