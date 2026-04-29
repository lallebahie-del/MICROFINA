package com.microfina.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * JournalAudit – trace immuable de toute opération sensible dans le système.
 *
 * <p>Chaque ligne enregistre qui a fait quoi, sur quelle entité, et quand.
 * Les anciennes et nouvelles valeurs sont stockées en JSON sérialisé
 * (colonne {@code NVARCHAR(MAX)} pour SQL Server) afin de pouvoir
 * reconstituer l'historique complet d'une entité.</p>
 *
 * <p>Les entrées d'audit sont persistées dans une transaction
 * {@code REQUIRES_NEW} (via {@link com.microfina.security.AuditAspect})
 * afin de survivre aux rollbacks de la transaction principale.</p>
 *
 * <p>Table cible : {@code JournalAudit}</p>
 */
@Entity
@Table(name = "JournalAudit")
@DynamicUpdate
public class JournalAudit implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Identifiant technique généré automatiquement. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /** Horodatage de l'action auditée (DATETIME2 pour SQL Server). */
    @Column(name = "date_action", columnDefinition = "DATETIME2", nullable = false)
    private LocalDateTime dateAction;

    /** Login de l'utilisateur ayant effectué l'action. */
    @Column(name = "utilisateur", length = 100)
    private String utilisateur;

    /** Type d'action effectuée (CREATE, UPDATE, DELETE, LOGIN, LOGOUT). */
    @Enumerated(EnumType.STRING)
    @Column(name = "action")
    private ActionAudit action;

    /** Nom simple de la classe de l'entité concernée (ex : "Credits"). */
    @Column(name = "entite", length = 100)
    private String entite;

    /** Identifiant en chaîne de l'entité concernée (peut être null). */
    @Column(name = "id_entite", length = 100)
    private String idEntite;

    /** Sérialisation JSON de l'entité avant modification (null pour CREATE). */
    @Column(name = "ancienne_valeur", columnDefinition = "NVARCHAR(MAX)")
    private String ancienneValeur;

    /** Sérialisation JSON de l'entité après modification (null pour DELETE). */
    @Column(name = "nouvelle_valeur", columnDefinition = "NVARCHAR(MAX)")
    private String nouvelleValeur;

    /** Adresse IP du client ayant effectué la requête (IPv4 ou IPv6). */
    @Column(name = "adresse_ip", length = 45)
    private String adresseIp;

    /** User-Agent HTTP du navigateur ou du client API. */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /** Colonne de verrouillage optimiste. */
    @Version
    @Column(name = "version")
    private Integer version;

    // ── Constructeurs ──────────────────────────────────────────────────────────

    /** Constructeur sans arguments requis par JPA. */
    public JournalAudit() {
    }

    /**
     * Constructeur complet.
     *
     * @param id             identifiant technique
     * @param dateAction     horodatage de l'action
     * @param utilisateur    login de l'auteur de l'action
     * @param action         type d'action
     * @param entite         nom simple de l'entité concernée
     * @param idEntite       identifiant de l'entité (en chaîne)
     * @param ancienneValeur JSON de l'entité avant modification
     * @param nouvelleValeur JSON de l'entité après modification
     * @param adresseIp      adresse IP du client
     * @param userAgent      User-Agent du client
     * @param version        version pour le verrouillage optimiste
     */
    public JournalAudit(Long id, LocalDateTime dateAction, String utilisateur,
                        ActionAudit action, String entite, String idEntite,
                        String ancienneValeur, String nouvelleValeur,
                        String adresseIp, String userAgent, Integer version) {
        this.id = id;
        this.dateAction = dateAction;
        this.utilisateur = utilisateur;
        this.action = action;
        this.entite = entite;
        this.idEntite = idEntite;
        this.ancienneValeur = ancienneValeur;
        this.nouvelleValeur = nouvelleValeur;
        this.adresseIp = adresseIp;
        this.userAgent = userAgent;
        this.version = version;
    }

    // ── Getters / Setters ──────────────────────────────────────────────────────

    /** @return identifiant technique */
    public Long getId() {
        return id;
    }

    /** @param id identifiant technique */
    public void setId(Long id) {
        this.id = id;
    }

    /** @return horodatage de l'action */
    public LocalDateTime getDateAction() {
        return dateAction;
    }

    /** @param dateAction horodatage de l'action */
    public void setDateAction(LocalDateTime dateAction) {
        this.dateAction = dateAction;
    }

    /** @return login de l'auteur de l'action */
    public String getUtilisateur() {
        return utilisateur;
    }

    /** @param utilisateur login de l'auteur de l'action */
    public void setUtilisateur(String utilisateur) {
        this.utilisateur = utilisateur;
    }

    /** @return type d'action */
    public ActionAudit getAction() {
        return action;
    }

    /** @param action type d'action */
    public void setAction(ActionAudit action) {
        this.action = action;
    }

    /** @return nom simple de l'entité concernée */
    public String getEntite() {
        return entite;
    }

    /** @param entite nom simple de l'entité concernée */
    public void setEntite(String entite) {
        this.entite = entite;
    }

    /** @return identifiant de l'entité en chaîne */
    public String getIdEntite() {
        return idEntite;
    }

    /** @param idEntite identifiant de l'entité en chaîne */
    public void setIdEntite(String idEntite) {
        this.idEntite = idEntite;
    }

    /** @return JSON de l'entité avant modification */
    public String getAncienneValeur() {
        return ancienneValeur;
    }

    /** @param ancienneValeur JSON de l'entité avant modification */
    public void setAncienneValeur(String ancienneValeur) {
        this.ancienneValeur = ancienneValeur;
    }

    /** @return JSON de l'entité après modification */
    public String getNouvelleValeur() {
        return nouvelleValeur;
    }

    /** @param nouvelleValeur JSON de l'entité après modification */
    public void setNouvelleValeur(String nouvelleValeur) {
        this.nouvelleValeur = nouvelleValeur;
    }

    /** @return adresse IP du client */
    public String getAdresseIp() {
        return adresseIp;
    }

    /** @param adresseIp adresse IP du client */
    public void setAdresseIp(String adresseIp) {
        this.adresseIp = adresseIp;
    }

    /** @return User-Agent du client */
    public String getUserAgent() {
        return userAgent;
    }

    /** @param userAgent User-Agent du client */
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    /** @return version pour le verrouillage optimiste */
    public Integer getVersion() {
        return version;
    }

    /** @param version version pour le verrouillage optimiste */
    public void setVersion(Integer version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "JournalAudit("
            + "id=" + id
            + ", action=" + action
            + ", utilisateur=" + utilisateur
            + ", dateAction=" + dateAction
            + ")";
    }
}
