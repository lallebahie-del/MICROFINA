package com.microfina.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * JobExecution — traçabilité des traitements batchs planifiés.
 *
 * <p>Chaque exécution (manuelle ou automatique) d'un job planifié est enregistrée
 * dans cette table pour auditer les traitements de nuit et les lancements manuels.</p>
 */
@Entity
@Table(
    name = "job_execution",
    indexes = {
        @Index(name = "IDX_job_execution_nom_debut", columnList = "nom_job, date_debut")
    }
)
public class JobExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_job_execution", nullable = false)
    private Long idJobExecution;

    /** Nom du job (ex : CALCUL_INTERETS, RECALCUL_PAR, CLOTURE_JOURNALIERE). */
    @Column(name = "nom_job", length = 100, nullable = false)
    private String nomJob;

    /** Date et heure de début d'exécution. */
    @Column(name = "date_debut", nullable = false)
    private LocalDateTime dateDebut;

    /** Date et heure de fin d'exécution (null si encore en cours). */
    @Column(name = "date_fin")
    private LocalDateTime dateFin;

    /** Statut : EN_COURS | SUCCES | ECHEC | ANNULE. */
    @Column(name = "statut", length = 20, nullable = false)
    private String statut;

    /** Message de résultat ou d'erreur. */
    @Column(name = "message", length = 1000)
    private String message;

    /** Nombre d'enregistrements traités (optionnel). */
    @Column(name = "nb_traites")
    private Integer nbTraites;

    /** Utilisateur qui a déclenché l'exécution ('SCHEDULER' si automatique). */
    @Column(name = "declencheur", length = 100)
    private String declencheur;

    @PrePersist
    private void prePersist() {
        if (dateDebut == null) dateDebut = LocalDateTime.now();
        if (statut    == null) statut    = "EN_COURS";
    }

    // ── Constantes statut ──────────────────────────────────────────────────────
    public static final String STATUT_EN_COURS = "EN_COURS";
    public static final String STATUT_SUCCES   = "SUCCES";
    public static final String STATUT_ECHEC    = "ECHEC";

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public Long getIdJobExecution() { return idJobExecution; }
    public void setIdJobExecution(Long id) { this.idJobExecution = id; }

    public String getNomJob() { return nomJob; }
    public void setNomJob(String nomJob) { this.nomJob = nomJob; }

    public LocalDateTime getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDateTime dateDebut) { this.dateDebut = dateDebut; }

    public LocalDateTime getDateFin() { return dateFin; }
    public void setDateFin(LocalDateTime dateFin) { this.dateFin = dateFin; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Integer getNbTraites() { return nbTraites; }
    public void setNbTraites(Integer nbTraites) { this.nbTraites = nbTraites; }

    public String getDeclencheur() { return declencheur; }
    public void setDeclencheur(String declencheur) { this.declencheur = declencheur; }
}
