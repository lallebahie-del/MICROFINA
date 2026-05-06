package com.microfina.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * CompteBanque – compte bancaire ouvert auprès d'une banque partenaire.
 *
 * Un compte est rattaché à une {@link Banque} et à une {@link Agence}.
 * Il sert de support aux opérations bancaires ({@link OperationBanque}) et
 * aux carnets de chèques ({@link CarnetCheque}).
 *
 * DDL source of truth: P6-006-CREATE-TABLE-CompteBanque.xml.
 * Spec: cahier §6 (Module Banque – comptes bancaires).
 */
@Entity
@Table(name = "CompteBanque")
@DynamicUpdate
public class CompteBanque implements Serializable {

    private static final long serialVersionUID = 1L;

    // ── PK ────────────────────────────────────────────────────────

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    // ── Champs métier ─────────────────────────────────────────────

    /** Numéro de compte bancaire (RIB, IBAN ou format local). */
    @Size(max = 34)
    @Column(name = "numero_compte", length = 34, nullable = false, unique = true)
    private String numeroCompte;

    /** Libellé descriptif du compte. */
    @Size(max = 255)
    @Column(name = "libelle", length = 255)
    private String libelle;

    /** Code devise ISO 4217 (défaut : MRU – Ouguiya mauritanien). */
    @Size(max = 3)
    @Column(name = "devise", length = 3, nullable = false)
    private String devise = "MRU";

    /** Solde courant du compte. */
    @Column(name = "solde", precision = 19, scale = 4)
    private BigDecimal solde;

    /** {@code true} = compte actif et utilisable. */
    @Column(name = "actif", nullable = false)
    private Boolean actif = true;

    // ── Optimistic lock ───────────────────────────────────────────

    @Version
    @Column(name = "version")
    private Integer version = 0;

    // ── Associations ──────────────────────────────────────────────

    /**
     * Banque domiciliataire du compte.
     * FK vers Banque(code_banque).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name       = "code_banque",
        foreignKey = @ForeignKey(name = "FK_CompteBanque_Banque")
    )
    private Banque banque;

    /**
     * Agence gestionnaire du compte.
     * FK vers AGENCE(CODE_AGENCE).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name       = "code_agence",
        foreignKey = @ForeignKey(name = "FK_CompteBanque_Agence")
    )
    private Agence agence;

    // ── Constructeurs ─────────────────────────────────────────────

    public CompteBanque() {
    }

    public CompteBanque(Long id, String numeroCompte, String libelle, String devise,
                        BigDecimal solde, Boolean actif, Integer version,
                        Banque banque, Agence agence) {
        this.id = id;
        this.numeroCompte = numeroCompte;
        this.libelle = libelle;
        this.devise = devise;
        this.solde = solde;
        this.actif = actif;
        this.version = version;
        this.banque = banque;
        this.agence = agence;
    }

    // ── Accesseurs ────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumeroCompte() { return numeroCompte; }
    public void setNumeroCompte(String numeroCompte) { this.numeroCompte = numeroCompte; }

    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }

    public String getDevise() { return devise; }
    public void setDevise(String devise) { this.devise = devise; }

    public BigDecimal getSolde() { return solde; }
    public void setSolde(BigDecimal solde) { this.solde = solde; }

    public Boolean getActif() { return actif; }
    public void setActif(Boolean actif) { this.actif = actif; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public Banque getBanque() { return banque; }
    public void setBanque(Banque banque) { this.banque = banque; }

    public Agence getAgence() { return agence; }
    public void setAgence(Agence agence) { this.agence = agence; }

    @Override
    public String toString() {
        return "CompteBanque(id=" + id + ", numeroCompte=" + numeroCompte + ")";
    }
}
