package com.microfina.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * LigneBudget – détail budgétaire d'un {@link Budget}.
 * <p>
 * Chaque ligne est rattachée à une rubrique budgétaire identifiée par
 * {@code codeRubrique} et peut être associée à un compte du plan comptable
 * ({@code compte}). La nature de la ligne est définie par {@link TypeLigneBudget} :
 * RECETTE ou DEPENSE.
 * </p>
 * <p>
 * Le suivi d'exécution est assuré par les {@link MouvementBudget} qui alimentent
 * {@code montantRealise} au fur et à mesure des opérations comptables.
 * </p>
 * <p>
 * Note : la colonne {@code compte} est stockée en tant que référence libre (NVARCHAR).
 * La contrainte d'intégrité référentielle vers la table {@code Compte} est gérée
 * uniquement au niveau DDL (FK_LigneBudget_Compte). Aucun mapping JPA {@code @ManyToOne}
 * n'est créé pour cette relation afin de préserver la simplicité du modèle.
 * </p>
 *
 * DDL source of truth: P8-002-CREATE-TABLE-LigneBudget.xml.
 * Spec: cahier §3.1.1 (LigneBudget).
 */
@Entity
@Table(name = "LigneBudget")
@DynamicUpdate
public class LigneBudget implements Serializable {

    private static final long serialVersionUID = 1L;

    // ── PK ────────────────────────────────────────────────────────

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    // ── Champs métier ─────────────────────────────────────────────

    /**
     * Code de la rubrique budgétaire (ex. "REC-001", "DEP-ADM-002").
     * NVARCHAR(20) NOT NULL.
     */
    @NotBlank
    @Size(max = 20)
    @Column(name = "code_rubrique", length = 20, nullable = false)
    private String codeRubrique;

    /**
     * Libellé descriptif de la ligne budgétaire.
     * NVARCHAR(255) NOT NULL.
     */
    @NotBlank
    @Size(max = 255)
    @Column(name = "libelle", length = 255, nullable = false)
    private String libelle;

    /**
     * Nature de la ligne : RECETTE ou DEPENSE.
     * NVARCHAR(10) NOT NULL DEFAULT 'RECETTE'.
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type_ligne", length = 10, nullable = false)
    private TypeLigneBudget typeLigne = TypeLigneBudget.RECETTE;

    /**
     * Montant budgété (prévisionnel) pour cette ligne.
     * DECIMAL(19,4) NOT NULL DEFAULT 0.
     */
    @NotNull
    @Column(name = "montant_prevu", precision = 19, scale = 4, nullable = false)
    private BigDecimal montantPrevu = BigDecimal.ZERO;

    /**
     * Montant réellement consommé ou encaissé, calculé à partir des mouvements.
     * DECIMAL(19,4) NOT NULL DEFAULT 0.
     */
    @NotNull
    @Column(name = "montant_realise", precision = 19, scale = 4, nullable = false)
    private BigDecimal montantRealise = BigDecimal.ZERO;

    /**
     * Référence libre au compte du plan comptable.
     * La contrainte FK DDL (FK_LigneBudget_Compte) garantit l'intégrité côté base.
     * NVARCHAR(255) NULL.
     */
    @Size(max = 255)
    @Column(name = "compte", length = 255)
    private String compte;

    // ── Optimistic locking ────────────────────────────────────────

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    // ── Relations ─────────────────────────────────────────────────

    /**
     * Budget parent auquel appartient cette ligne.
     * FK_LigneBudget_Budget → Budget(id). NOT NULL.
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "budget_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "FK_LigneBudget_Budget")
    )
    private Budget budget;

    // ── Constructeurs ─────────────────────────────────────────────

    public LigneBudget() {
    }

    // ── Getters / Setters ─────────────────────────────────────────

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodeRubrique() {
        return codeRubrique;
    }

    public void setCodeRubrique(String codeRubrique) {
        this.codeRubrique = codeRubrique;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public TypeLigneBudget getTypeLigne() {
        return typeLigne;
    }

    public void setTypeLigne(TypeLigneBudget typeLigne) {
        this.typeLigne = typeLigne;
    }

    public BigDecimal getMontantPrevu() {
        return montantPrevu;
    }

    public void setMontantPrevu(BigDecimal montantPrevu) {
        this.montantPrevu = montantPrevu;
    }

    public BigDecimal getMontantRealise() {
        return montantRealise;
    }

    public void setMontantRealise(BigDecimal montantRealise) {
        this.montantRealise = montantRealise;
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

    public Budget getBudget() {
        return budget;
    }

    public void setBudget(Budget budget) {
        this.budget = budget;
    }
}
