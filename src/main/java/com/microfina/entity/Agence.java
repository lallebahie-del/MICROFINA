package com.microfina.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * AGENCE – represents a branch office within the institution.
 * Each agency inherits configuration from its parent institution.
 */
@Entity
@Table(name = "AGENCE")
@DynamicUpdate
public class Agence implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @NotBlank
    @Size(max = 25)
    @Column(name = "CODE_AGENCE", length = 25, nullable = false)
    private String codeAgence;

    @Column(name = "ACTIF", nullable = false)
    private Boolean actif = true;

    @Size(max = 255)
    @Column(name = "basecommune", length = 255)
    private String baseCommune;

    @Size(max = 255)
    @Column(name = "baseDeDonnee", length = 255)
    private String baseDeDonnee;

    @Column(name = "Bordereau")
    private Boolean bordereau = false;

    @Size(max = 255)
    @Column(name = "COMPTE_CREDITEUR", length = 255)
    private String compteCrediteur;

    @Column(name = "Compteur")
    private Integer compteur;

    @Size(max = 255)
    @Column(name = "dbDriver", length = 255)
    private String dbDriver;

    /** "S" = siège (head office), "A" = agence. */
    @Size(max = 1)
    @Column(name = "ISSIEGE", length = 1)
    private String isSiege;

    @Size(max = 255)
    @Column(name = "login", length = 255)
    private String login;

    @Size(max = 255)
    @Column(name = "NOMAGENCE", length = 255)
    private String nomAgence;

    @Size(max = 255)
    @Column(name = "NUMAGENCE", length = 255)
    private String numAgence;

    @Size(max = 255)
    @Column(name = "NUMERO_SMS", length = 255)
    private String numeroSms;

    @Size(max = 255)
    @Column(name = "passWord", length = 255)
    private String passWord;

    @Size(max = 255)
    @Column(name = "port", length = 255)
    private String port;

    @Size(max = 255)
    @Column(name = "PREF", length = 255)
    private String pref;

    @Size(max = 255)
    @Column(name = "schemas", length = 255)
    private String schemas;

    @Size(max = 255)
    @Column(name = "serveur", length = 255)
    private String serveur;

    @Size(max = 255)
    @Column(name = "SUFF", length = 255)
    private String suff;

    @Version
    @Column(name = "VERSION")
    private Integer version = 0;

    /** FK to institution. */
    @Size(max = 255)
    @Column(name = "institution", length = 255)
    private String institution;

    /** FK to zoneGeographique. */
    @Column(name = "zoneGeographique")
    private Integer zoneGeographique;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ADRESSE_IDADRESSE", referencedColumnName = "IDADRESSE",
                foreignKey = @ForeignKey(name = "FK_AGENCE_ADRESSE"))
    private Adresse adresse;

    @Size(max = 48)
    @Column(name = "chefAgence", length = 48)
    private String chefAgence;

    @Size(max = 15)
    @Column(name = "NUMCOMPTE", length = 15)
    private String numCompte;

    @Size(max = 100)
    @Column(name = "PATH", length = 100)
    private String path;

    @Size(max = 100)
    @Column(name = "PATH_PHOTO_REEL", length = 100)
    private String pathPhotoReel;

    @Size(max = 100)
    @Column(name = "PATH_PHOTO_SIMPLE", length = 100)
    private String pathPhotoSimple;

    @Size(max = 100)
    @Column(name = "PATH_SIGNATURE_SIMPLE", length = 100)
    private String pathSignatureSimple;

    @Size(max = 100)
    @Column(name = "PATH_SIGNATURE_REEL", length = 100)
    private String pathSignatureReel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "devise_local", referencedColumnName = "CODE_DEVISE",
                foreignKey = @ForeignKey(name = "FK_AGENCE_devise_local"))
    private DeviseLocal deviseLocal;

    @Size(max = 30)
    @Column(name = "compteCaisse", length = 30)
    private String compteCaisse;

    @Size(max = 30)
    @Column(name = "sufixe_compte", length = 30)
    private String sufixeCompte;

    @Column(name = "MAX_MONT_DECAISS_CAISSE", precision = 19, scale = 4)
    private BigDecimal maxMontDecaissCaisse;

    @Column(name = "MIN_MONT_DECAISS_CAISSE", precision = 19, scale = 4)
    private BigDecimal minMontDecaissCaisse;

    @Column(name = "MAX_MONT_DECAISS_BANQUE_PRELEV", precision = 19, scale = 4)
    private BigDecimal maxMontDecaissBanquePrelev;

    @Column(name = "MIN_MONT_DECAISS_BANQUE_PRELEV", precision = 19, scale = 4)
    private BigDecimal minMontDecaissBanquePrelev;

    @Column(name = "filte_mbre_mobile")
    private Boolean filteMbreMobile = false;

    @Column(name = "daterref_debut_penalite")
    private LocalDate daterrefDebutPenalite;

    @Column(name = "longitude", precision = 19, scale = 4)
    private BigDecimal longitude;

    @Column(name = "latitude", precision = 19, scale = 4)
    private BigDecimal latitude;

    @Column(name = "DERNIERE_JRNEE_OUVERTE")
    private LocalDate derniereJrneeOuverte;

    @Size(max = 100)
    @Column(name = "nom_court", length = 100)
    private String nomCourt;

    @Column(name = "indiceMembre")
    private Integer indiceMembre;

    @Column(name = "indicecarnet")
    private Integer indiceCarnet;

    @Size(max = 255)
    @Column(name = "NOM_PRENOM_CHEF_AGENCE", length = 255)
    private String nomPrenomChefAgence;

    @Size(max = 15)
    @Column(name = "liaison_trsfInterne", length = 15)
    private String liaisonTrsfInterne;

    @Size(max = 15)
    @Column(name = "liaison_virement", length = 15)
    private String liaisonVirement;

    // ── Generated (was Lombok) ──────────────────────────────────────────────

    public Agence() {
    }

    public Agence(String codeAgence, Boolean actif, String baseCommune, String baseDeDonnee, Boolean bordereau, String compteCrediteur, Integer compteur, String dbDriver, String isSiege, String login, String nomAgence, String numAgence, String numeroSms, String passWord, String port, String pref, String schemas, String serveur, String suff, Integer version, String institution, Integer zoneGeographique, Adresse adresse, String chefAgence, String numCompte, String path, String pathPhotoReel, String pathPhotoSimple, String pathSignatureSimple, String pathSignatureReel, DeviseLocal deviseLocal, String compteCaisse, String sufixeCompte, BigDecimal maxMontDecaissCaisse, BigDecimal minMontDecaissCaisse, BigDecimal maxMontDecaissBanquePrelev, BigDecimal minMontDecaissBanquePrelev, Boolean filteMbreMobile, LocalDate daterrefDebutPenalite, BigDecimal longitude, BigDecimal latitude, LocalDate derniereJrneeOuverte, String nomCourt, Integer indiceMembre, Integer indiceCarnet, String nomPrenomChefAgence, String liaisonTrsfInterne, String liaisonVirement) {
        this.codeAgence = codeAgence;
        this.actif = actif;
        this.baseCommune = baseCommune;
        this.baseDeDonnee = baseDeDonnee;
        this.bordereau = bordereau;
        this.compteCrediteur = compteCrediteur;
        this.compteur = compteur;
        this.dbDriver = dbDriver;
        this.isSiege = isSiege;
        this.login = login;
        this.nomAgence = nomAgence;
        this.numAgence = numAgence;
        this.numeroSms = numeroSms;
        this.passWord = passWord;
        this.port = port;
        this.pref = pref;
        this.schemas = schemas;
        this.serveur = serveur;
        this.suff = suff;
        this.version = version;
        this.institution = institution;
        this.zoneGeographique = zoneGeographique;
        this.adresse = adresse;
        this.chefAgence = chefAgence;
        this.numCompte = numCompte;
        this.path = path;
        this.pathPhotoReel = pathPhotoReel;
        this.pathPhotoSimple = pathPhotoSimple;
        this.pathSignatureSimple = pathSignatureSimple;
        this.pathSignatureReel = pathSignatureReel;
        this.deviseLocal = deviseLocal;
        this.compteCaisse = compteCaisse;
        this.sufixeCompte = sufixeCompte;
        this.maxMontDecaissCaisse = maxMontDecaissCaisse;
        this.minMontDecaissCaisse = minMontDecaissCaisse;
        this.maxMontDecaissBanquePrelev = maxMontDecaissBanquePrelev;
        this.minMontDecaissBanquePrelev = minMontDecaissBanquePrelev;
        this.filteMbreMobile = filteMbreMobile;
        this.daterrefDebutPenalite = daterrefDebutPenalite;
        this.longitude = longitude;
        this.latitude = latitude;
        this.derniereJrneeOuverte = derniereJrneeOuverte;
        this.nomCourt = nomCourt;
        this.indiceMembre = indiceMembre;
        this.indiceCarnet = indiceCarnet;
        this.nomPrenomChefAgence = nomPrenomChefAgence;
        this.liaisonTrsfInterne = liaisonTrsfInterne;
        this.liaisonVirement = liaisonVirement;
    }

    public String getCodeAgence() {
        return codeAgence;
    }

    public void setCodeAgence(String codeAgence) {
        this.codeAgence = codeAgence;
    }

    public Boolean getActif() {
        return actif;
    }

    public void setActif(Boolean actif) {
        this.actif = actif;
    }

    public String getBaseCommune() {
        return baseCommune;
    }

    public void setBaseCommune(String baseCommune) {
        this.baseCommune = baseCommune;
    }

    public String getBaseDeDonnee() {
        return baseDeDonnee;
    }

    public void setBaseDeDonnee(String baseDeDonnee) {
        this.baseDeDonnee = baseDeDonnee;
    }

    public Boolean getBordereau() {
        return bordereau;
    }

    public void setBordereau(Boolean bordereau) {
        this.bordereau = bordereau;
    }

    public String getCompteCrediteur() {
        return compteCrediteur;
    }

    public void setCompteCrediteur(String compteCrediteur) {
        this.compteCrediteur = compteCrediteur;
    }

    public Integer getCompteur() {
        return compteur;
    }

    public void setCompteur(Integer compteur) {
        this.compteur = compteur;
    }

    public String getDbDriver() {
        return dbDriver;
    }

    public void setDbDriver(String dbDriver) {
        this.dbDriver = dbDriver;
    }

    public String getIsSiege() {
        return isSiege;
    }

    public void setIsSiege(String isSiege) {
        this.isSiege = isSiege;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getNomAgence() {
        return nomAgence;
    }

    public void setNomAgence(String nomAgence) {
        this.nomAgence = nomAgence;
    }

    public String getNumAgence() {
        return numAgence;
    }

    public void setNumAgence(String numAgence) {
        this.numAgence = numAgence;
    }

    public String getNumeroSms() {
        return numeroSms;
    }

    public void setNumeroSms(String numeroSms) {
        this.numeroSms = numeroSms;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getPref() {
        return pref;
    }

    public void setPref(String pref) {
        this.pref = pref;
    }

    public String getSchemas() {
        return schemas;
    }

    public void setSchemas(String schemas) {
        this.schemas = schemas;
    }

    public String getServeur() {
        return serveur;
    }

    public void setServeur(String serveur) {
        this.serveur = serveur;
    }

    public String getSuff() {
        return suff;
    }

    public void setSuff(String suff) {
        this.suff = suff;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getInstitution() {
        return institution;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

    public Integer getZoneGeographique() {
        return zoneGeographique;
    }

    public void setZoneGeographique(Integer zoneGeographique) {
        this.zoneGeographique = zoneGeographique;
    }

    public Adresse getAdresse() {
        return adresse;
    }

    public void setAdresse(Adresse adresse) {
        this.adresse = adresse;
    }

    public String getChefAgence() {
        return chefAgence;
    }

    public void setChefAgence(String chefAgence) {
        this.chefAgence = chefAgence;
    }

    public String getNumCompte() {
        return numCompte;
    }

    public void setNumCompte(String numCompte) {
        this.numCompte = numCompte;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPathPhotoReel() {
        return pathPhotoReel;
    }

    public void setPathPhotoReel(String pathPhotoReel) {
        this.pathPhotoReel = pathPhotoReel;
    }

    public String getPathPhotoSimple() {
        return pathPhotoSimple;
    }

    public void setPathPhotoSimple(String pathPhotoSimple) {
        this.pathPhotoSimple = pathPhotoSimple;
    }

    public String getPathSignatureSimple() {
        return pathSignatureSimple;
    }

    public void setPathSignatureSimple(String pathSignatureSimple) {
        this.pathSignatureSimple = pathSignatureSimple;
    }

    public String getPathSignatureReel() {
        return pathSignatureReel;
    }

    public void setPathSignatureReel(String pathSignatureReel) {
        this.pathSignatureReel = pathSignatureReel;
    }

    public DeviseLocal getDeviseLocal() {
        return deviseLocal;
    }

    public void setDeviseLocal(DeviseLocal deviseLocal) {
        this.deviseLocal = deviseLocal;
    }

    public String getCompteCaisse() {
        return compteCaisse;
    }

    public void setCompteCaisse(String compteCaisse) {
        this.compteCaisse = compteCaisse;
    }

    public String getSufixeCompte() {
        return sufixeCompte;
    }

    public void setSufixeCompte(String sufixeCompte) {
        this.sufixeCompte = sufixeCompte;
    }

    public BigDecimal getMaxMontDecaissCaisse() {
        return maxMontDecaissCaisse;
    }

    public void setMaxMontDecaissCaisse(BigDecimal maxMontDecaissCaisse) {
        this.maxMontDecaissCaisse = maxMontDecaissCaisse;
    }

    public BigDecimal getMinMontDecaissCaisse() {
        return minMontDecaissCaisse;
    }

    public void setMinMontDecaissCaisse(BigDecimal minMontDecaissCaisse) {
        this.minMontDecaissCaisse = minMontDecaissCaisse;
    }

    public BigDecimal getMaxMontDecaissBanquePrelev() {
        return maxMontDecaissBanquePrelev;
    }

    public void setMaxMontDecaissBanquePrelev(BigDecimal maxMontDecaissBanquePrelev) {
        this.maxMontDecaissBanquePrelev = maxMontDecaissBanquePrelev;
    }

    public BigDecimal getMinMontDecaissBanquePrelev() {
        return minMontDecaissBanquePrelev;
    }

    public void setMinMontDecaissBanquePrelev(BigDecimal minMontDecaissBanquePrelev) {
        this.minMontDecaissBanquePrelev = minMontDecaissBanquePrelev;
    }

    public Boolean getFilteMbreMobile() {
        return filteMbreMobile;
    }

    public void setFilteMbreMobile(Boolean filteMbreMobile) {
        this.filteMbreMobile = filteMbreMobile;
    }

    public LocalDate getDaterrefDebutPenalite() {
        return daterrefDebutPenalite;
    }

    public void setDaterrefDebutPenalite(LocalDate daterrefDebutPenalite) {
        this.daterrefDebutPenalite = daterrefDebutPenalite;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public LocalDate getDerniereJrneeOuverte() {
        return derniereJrneeOuverte;
    }

    public void setDerniereJrneeOuverte(LocalDate derniereJrneeOuverte) {
        this.derniereJrneeOuverte = derniereJrneeOuverte;
    }

    public String getNomCourt() {
        return nomCourt;
    }

    public void setNomCourt(String nomCourt) {
        this.nomCourt = nomCourt;
    }

    public Integer getIndiceMembre() {
        return indiceMembre;
    }

    public void setIndiceMembre(Integer indiceMembre) {
        this.indiceMembre = indiceMembre;
    }

    public Integer getIndiceCarnet() {
        return indiceCarnet;
    }

    public void setIndiceCarnet(Integer indiceCarnet) {
        this.indiceCarnet = indiceCarnet;
    }

    public String getNomPrenomChefAgence() {
        return nomPrenomChefAgence;
    }

    public void setNomPrenomChefAgence(String nomPrenomChefAgence) {
        this.nomPrenomChefAgence = nomPrenomChefAgence;
    }

    public String getLiaisonTrsfInterne() {
        return liaisonTrsfInterne;
    }

    public void setLiaisonTrsfInterne(String liaisonTrsfInterne) {
        this.liaisonTrsfInterne = liaisonTrsfInterne;
    }

    public String getLiaisonVirement() {
        return liaisonVirement;
    }

    public void setLiaisonVirement(String liaisonVirement) {
        this.liaisonVirement = liaisonVirement;
    }

    @Override
    public String toString() {
        return "Agence("
            + "codeAgence=" + codeAgence
            + ")";
    }
}
