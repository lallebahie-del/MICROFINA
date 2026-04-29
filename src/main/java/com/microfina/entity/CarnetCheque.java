package com.microfina.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * CarnetCheque – carnet de chèques remis à un membre.
 *
 * Un carnet est associé à un {@link CompteBanque} et à un {@link Membres}.
 * Son cycle de vie est décrit par {@link StatutCarnetCheque}.
 * Chaque feuillet individuel est modélisé par {@link Cheque}.
 *
 * DDL source of truth: P6-007-CREATE-TABLE-CarnetCheque.xml.
 * Spec: cahier §6 (Module Banque – carnets de chèques).
 */
@Entity
@Table(name = "CarnetCheque")
@DynamicUpdate
public class CarnetCheque implements Serializable {

    private static final long serialVersionUID = 1L;

    // ── PK ────────────────────────────────────────────────────────

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    // ── Champs métier ─────────────────────────────────────────────

    /** Numéro d'identification du carnet (séquence imprimée). */
    @Size(max = 50)
    @Column(name = "numero_carnet", length = 50, nullable = false)
    private String numeroCarnet;

    /** Date de dépôt de la demande de carnet. */
    @Column(name = "date_demande")
    private LocalDate dateDemande;

    /** Date de remise effective du carnet au membre. */
    @Column(name = "date_remise")
    private LocalDate dateRemise;

    /** Nombre de feuillets dans le carnet (défaut : 25). */
    @Column(name = "nombre_cheques", nullable = false)
    private Integer nombreCheques = 25;

    /** État courant du carnet. */
    @Enumerated(EnumType.STRING)
    @Column(name = "statut", length = 20, nullable = false)
    private StatutCarnetCheque statut = StatutCarnetCheque.DEMANDE;

    // ── Optimistic lock ───────────────────────────────────────────

    @Version
    @Column(name = "version")
    private Integer version = 0;

    // ── Associations ──────────────────────────────────────────────

    /**
     * Compte bancaire sur lequel les chèques du carnet seront tirés.
     * FK vers CompteBanque(id).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name       = "compte_banque_id",
        foreignKey = @ForeignKey(name = "FK_CarnetCheque_CompteBanque")
    )
    private CompteBanque compteBanque;

    /**
     * Membre propriétaire du carnet.
     * FK vers Membres(NUM_MEMBRE).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name       = "num_membre",
        foreignKey = @ForeignKey(name = "FK_CarnetCheque_Membres")
    )
    private Membres membre;

    // ── Constructeurs ─────────────────────────────────────────────

    public CarnetCheque() {
    }

    public CarnetCheque(Long id, String numeroCarnet, LocalDate dateDemande,
                        LocalDate dateRemise, Integer nombreCheques,
                        StatutCarnetCheque statut, Integer version,
                        CompteBanque compteBanque, Membres membre) {
        this.id = id;
        this.numeroCarnet = numeroCarnet;
        this.dateDemande = dateDemande;
        this.dateRemise = dateRemise;
        this.nombreCheques = nombreCheques;
        this.statut = statut;
        this.version = version;
        this.compteBanque = compteBanque;
        this.membre = membre;
    }

    // ── Accesseurs ────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumeroCarnet() { return numeroCarnet; }
    public void setNumeroCarnet(String numeroCarnet) { this.numeroCarnet = numeroCarnet; }

    public LocalDate getDateDemande() { return dateDemande; }
    public void setDateDemande(LocalDate dateDemande) { this.dateDemande = dateDemande; }

    public LocalDate getDateRemise() { return dateRemise; }
    public void setDateRemise(LocalDate dateRemise) { this.dateRemise = dateRemise; }

    public Integer getNombreCheques() { return nombreCheques; }
    public void setNombreCheques(Integer nombreCheques) { this.nombreCheques = nombreCheques; }

    public StatutCarnetCheque getStatut() { return statut; }
    public void setStatut(StatutCarnetCheque statut) { this.statut = statut; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public CompteBanque getCompteBanque() { return compteBanque; }
    public void setCompteBanque(CompteBanque compteBanque) { this.compteBanque = compteBanque; }

    public Membres getMembre() { return membre; }
    public void setMembre(Membres membre) { this.membre = membre; }

    @Override
    public String toString() {
        return "CarnetCheque(id=" + id + ", numeroCarnet=" + numeroCarnet + ")";
    }
}
