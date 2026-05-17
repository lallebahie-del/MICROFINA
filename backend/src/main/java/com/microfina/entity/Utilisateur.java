package com.microfina.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Utilisateur – compte applicatif d'un utilisateur du système Microfina++.
 *
 * <p>Contrairement à {@link AgentCredit} (ancienne table d'authentification),
 * cette entité centralise tous les comptes utilisateurs avec un modèle
 * de sécurité granulaire basé sur des rôles ({@link Role}) et des
 * privilèges ({@link Privilege}).</p>
 *
 * <p>Le champ {@code motDePasseHash} contient un hash BCrypt (coût 12).</p>
 *
 * <p>Table cible : {@code Utilisateur}</p>
 */
@Entity
@Table(name = "Utilisateur")
@DynamicUpdate
public class Utilisateur implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Identifiant technique généré automatiquement. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /** Identifiant de connexion unique (nom d'utilisateur). */
    @Column(name = "login", length = 100, unique = true, nullable = false)
    private String login;

    /** Hash BCrypt du mot de passe (coût 12). Ne jamais exposer en clair. */
    @Column(name = "mot_de_passe_hash", length = 255, nullable = false)
    private String motDePasseHash;

    /** Nom complet de l'utilisateur (prénom + nom). */
    @Column(name = "nom_complet", length = 255)
    private String nomComplet;

    /** Adresse e-mail professionnelle de l'utilisateur. */
    @Column(name = "email", length = 255)
    private String email;

    /** Numéro de téléphone de l'utilisateur. */
    @Column(name = "telephone", length = 30)
    private String telephone;

    /** Indique si le compte est actif. {@code false} bloque l'authentification. */
    @Column(name = "actif")
    private Boolean actif = true;

    /** Date d'expiration du compte. Passée cette date, la connexion est refusée. */
    @Column(name = "date_expiration_compte")
    private LocalDate dateExpirationCompte;

    /** Horodatage de la dernière connexion réussie. */
    @Column(name = "derniere_connexion", columnDefinition = "DATETIME2")
    private LocalDateTime derniereConnexion;

    /** Nombre d'échecs de connexion consécutifs (réinitialisé après succès). */
    @Column(name = "nombre_echecs")
    private Integer nombreEchecs = 0;

    /** Colonne de verrouillage optimiste. */
    @Version
    @Column(name = "version")
    private Integer version;

    /**
     * Agence à laquelle l'utilisateur est rattaché.
     * Chargement différé pour éviter les requêtes inutiles.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "code_agence",
                foreignKey = @ForeignKey(name = "FK_Utilisateur_Agence"))
    private Agence agence;

    /** Membre lié (inscription mobile) — FK vers membres.NUM_MEMBRE. */
    @Column(name = "num_membre", length = 25)
    private String numMembre;

    /** Adresse complète (profil mobile, copie à l'inscription). */
    @Column(name = "adresse", length = 500)
    private String adresse;

    @Column(name = "ville", length = 255)
    private String ville;

    @Column(name = "latitude", length = 50)
    private String latitude;

    @Column(name = "longitude", length = 50)
    private String longitude;

    /** Numéro du compte courant ouvert à l'inscription mobile. */
    @Column(name = "num_compte_courant", length = 255)
    private String numCompteCourant;

    // ── Constructeurs ──────────────────────────────────────────────────────────

    /** Constructeur sans arguments requis par JPA. */
    public Utilisateur() {
    }

    /**
     * Constructeur complet.
     *
     * @param id                   identifiant technique
     * @param login                login de connexion
     * @param motDePasseHash       hash BCrypt du mot de passe
     * @param nomComplet           nom complet
     * @param email                adresse e-mail
     * @param telephone            numéro de téléphone
     * @param actif                compte actif ou non
     * @param dateExpirationCompte date d'expiration du compte
     * @param derniereConnexion    horodatage de la dernière connexion
     * @param nombreEchecs         nombre d'échecs de connexion consécutifs
     * @param version              version pour le verrouillage optimiste
     * @param agence               agence de rattachement
     */
    public Utilisateur(Long id, String login, String motDePasseHash, String nomComplet,
                       String email, String telephone, Boolean actif,
                       LocalDate dateExpirationCompte, LocalDateTime derniereConnexion,
                       Integer nombreEchecs, Integer version, Agence agence) {
        this.id = id;
        this.login = login;
        this.motDePasseHash = motDePasseHash;
        this.nomComplet = nomComplet;
        this.email = email;
        this.telephone = telephone;
        this.actif = actif;
        this.dateExpirationCompte = dateExpirationCompte;
        this.derniereConnexion = derniereConnexion;
        this.nombreEchecs = nombreEchecs;
        this.version = version;
        this.agence = agence;
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

    /** @return login de connexion */
    public String getLogin() {
        return login;
    }

    /** @param login login de connexion */
    public void setLogin(String login) {
        this.login = login;
    }

    /** @return hash BCrypt du mot de passe */
    public String getMotDePasseHash() {
        return motDePasseHash;
    }

    /** @param motDePasseHash hash BCrypt du mot de passe */
    public void setMotDePasseHash(String motDePasseHash) {
        this.motDePasseHash = motDePasseHash;
    }

    /** @return nom complet de l'utilisateur */
    public String getNomComplet() {
        return nomComplet;
    }

    /** @param nomComplet nom complet de l'utilisateur */
    public void setNomComplet(String nomComplet) {
        this.nomComplet = nomComplet;
    }

    /** @return adresse e-mail */
    public String getEmail() {
        return email;
    }

    /** @param email adresse e-mail */
    public void setEmail(String email) {
        this.email = email;
    }

    /** @return numéro de téléphone */
    public String getTelephone() {
        return telephone;
    }

    /** @param telephone numéro de téléphone */
    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    /** @return {@code true} si le compte est actif */
    public Boolean getActif() {
        return actif;
    }

    /** @param actif {@code true} pour activer le compte */
    public void setActif(Boolean actif) {
        this.actif = actif;
    }

    /** @return date d'expiration du compte */
    public LocalDate getDateExpirationCompte() {
        return dateExpirationCompte;
    }

    /** @param dateExpirationCompte date d'expiration du compte */
    public void setDateExpirationCompte(LocalDate dateExpirationCompte) {
        this.dateExpirationCompte = dateExpirationCompte;
    }

    /** @return horodatage de la dernière connexion réussie */
    public LocalDateTime getDerniereConnexion() {
        return derniereConnexion;
    }

    /** @param derniereConnexion horodatage de la dernière connexion réussie */
    public void setDerniereConnexion(LocalDateTime derniereConnexion) {
        this.derniereConnexion = derniereConnexion;
    }

    /** @return nombre d'échecs de connexion consécutifs */
    public Integer getNombreEchecs() {
        return nombreEchecs;
    }

    /** @param nombreEchecs nombre d'échecs de connexion consécutifs */
    public void setNombreEchecs(Integer nombreEchecs) {
        this.nombreEchecs = nombreEchecs;
    }

    /** @return version pour le verrouillage optimiste */
    public Integer getVersion() {
        return version;
    }

    /** @param version version pour le verrouillage optimiste */
    public void setVersion(Integer version) {
        this.version = version;
    }

    /** @return agence de rattachement */
    public Agence getAgence() {
        return agence;
    }

    /** @param agence agence de rattachement */
    public void setAgence(Agence agence) {
        this.agence = agence;
    }

    public String getNumMembre() {
        return numMembre;
    }

    public void setNumMembre(String numMembre) {
        this.numMembre = numMembre;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getNumCompteCourant() {
        return numCompteCourant;
    }

    public void setNumCompteCourant(String numCompteCourant) {
        this.numCompteCourant = numCompteCourant;
    }

    @Override
    public String toString() {
        return "Utilisateur("
            + "id=" + id
            + ", login=" + login
            + ")";
    }
}
