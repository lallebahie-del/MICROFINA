package com.microfina.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * MouvementBudget – écriture budgétaire associée à une {@link LigneBudget}.
 * <p>
 * Chaque mouvement trace une exécution budgétaire réelle (décaissement ou
 * encaissement) et est obligatoirement lié à une écriture comptable
 * ({@link Comptabilite}) qui constitue la pièce justificative.
 * </p>
 * <p>
 * L'accumulation des mouvements sur une ligne permet de calculer
 * {@code LigneBudget.montantRealise} et, par agrégation,
 * {@code Budget.montantTotalRecettes} / {@code Budget.montantTotalDepenses}.
 * </p>
 *
 * DDL source of truth: P8-003-CREATE-TABLE-MouvementBudget.xml.
 * Spec: cahier §3.1.1 (MouvementBudget).
 */
@Entity
@Table(name = "MouvementBudget")
@DynamicUpdate
public class MouvementBudget implements Serializable {

    private static final long serialVersionUID = 1L;

    // ── PK ────────────────────────────────────────────────────────

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    // ── Champs métier ─────────────────────────────────────────────

    /**
     * Date à laquelle le mouvement budgétaire est constaté.
     * DATE NOT NULL.
     */
    @NotNull
    @Column(name = "date_mouvement", nullable = false)
    private LocalDate dateMouvement;

    /**
     * Montant du mouvement budgétaire (toujours positif, le sens est porté
     * par le type de ligne RECETTE/DEPENSE).
     * DECIMAL(19,4) NOT NULL.
     */
    @NotNull
    @Column(name = "montant", precision = 19, scale = 4, nullable = false)
    private BigDecimal montant;

    /**
     * Libellé explicatif du mouvement (ex. "Facture fournisseur N°2024-0042").
     * NVARCHAR(500) NULL.
     */
    @Size(max = 500)
    @Column(name = "libelle", length = 500)
    private String libelle;

    /**
     * Identifiant de l'utilisateur ayant saisi le mouvement.
     * NVARCHAR(100) NULL.
     */
    @Size(max = 100)
    @Column(name = "utilisateur", length = 100)
    private String utilisateur;

    // ── Optimistic locking ────────────────────────────────────────

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    // ── Relations ─────────────────────────────────────────────────

    /**
     * Ligne budgétaire à laquelle ce mouvement est rattaché.
     * FK_MouvementBudget_LigneBudget → LigneBudget(id). NOT NULL.
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "ligne_budget_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "FK_MouvementBudget_LigneBudget")
    )
    private LigneBudget ligneBudget;

    /**
     * Écriture comptable justifiant ce mouvement budgétaire.
     * FK_MouvementBudget_Comptabilite → comptabilite(IDCOMPTABILITE). NOT NULL.
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "idcomptabilite",
        referencedColumnName = "IDCOMPTABILITE",
        nullable = false,
        foreignKey = @ForeignKey(name = "FK_MouvementBudget_Comptabilite")
    )
    private Comptabilite comptabilite;

    // ── Constructeurs ─────────────────────────────────────────────

    public MouvementBudget() {
    }

    // ── Getters / Setters ─────────────────────────────────────────

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDateMouvement() {
        return dateMouvement;
    }

    public void setDateMouvement(LocalDate dateMouvement) {
        this.dateMouvement = dateMouvement;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public String getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(String utilisateur) {
        this.utilisateur = utilisateur;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public LigneBudget getLigneBudget() {
        return ligneBudget;
    }

    public void setLigneBudget(LigneBudget ligneBudget) {
        this.ligneBudget = ligneBudget;
    }

    public Comptabilite getComptabilite() {
        return comptabilite;
    }

    public void setComptabilite(Comptabilite comptabilite) {
        this.comptabilite = comptabilite;
    }
}
