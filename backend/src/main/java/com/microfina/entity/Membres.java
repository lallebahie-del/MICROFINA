package com.microfina.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Membres – core member enrollment table.
 *
 * NOT a subtype of Tiers; it is a standalone entity that holds a FK
 * {@code CODE_TIERS} pointing to the member's TIERS entry.
 * Own DTYPE discriminator allows physical / moral / groupement subtypes
 * within the same table (SINGLE_TABLE pattern on membres itself if needed
 * in a future phase; for now the discriminator column is just a label).
 *
 * DDL source of truth: P2-003-CREATE-TABLE-membres.xml.
 * Spec p.40-43 (80+ columns).
 *
 * Phase-2+ FK columns (categorie, produitEpargne, typePiece, pays, …) are
 * stored as raw String/Long values; constraints deferred to later phases.
 */
@Entity
@Table(name = "membres")
@DynamicUpdate
public class Membres implements Serializable {

    private static final long serialVersionUID = 1L;

    // ── PK ────────────────────────────────────────────────────────

    @Id
    @NotBlank
    @Size(max = 25)
    @Column(name = "NUM_MEMBRE", length = 25, nullable = false)
    private String numMembre;

    /** Discriminator label (physical / moral / groupement). */
    @Size(max = 31)
    @Column(name = "DTYPE", length = 31)
    private String dtype;

    // ── Core identity ─────────────────────────────────────────────

    @Size(max = 255)
    @Column(name = "NOM", length = 255)
    private String nom;

    @Size(max = 255)
    @Column(name = "PRENOM", length = 255)
    private String prenom;

    @Size(max = 255)
    @Column(name = "NOMJEUNEFILLE", length = 255)
    private String nomJeuneFille;

    @Size(max = 255)
    @Column(name = "nomMere", length = 255)
    private String nomMere;

    @Size(max = 255)
    @Column(name = "nomPere", length = 255)
    private String nomPere;

    @Size(max = 1)
    @Column(name = "SEXE", length = 1)
    private String sexe;

    @Column(name = "DATENAISSANCE")
    private LocalDate dateNaissance;

    @Size(max = 255)
    @Column(name = "LIEUNAISSANCE", length = 255)
    private String lieuNaissance;

    @Column(name = "AGE")
    private Integer age;

    @Size(max = 1)
    @Column(name = "ANALPHABETE", length = 1)
    private String analphabete;

    @Size(max = 255)
    @Column(name = "SituationMatrimoniale", length = 255)
    private String situationMatrimoniale;

    @Size(max = 255)
    @Column(name = "matriculemembre", length = 255)
    private String matriculeMembre;

    @Size(max = 255)
    @Column(name = "personneAcontacter", length = 255)
    private String personneAcontacter;

    // ── Membership status ─────────────────────────────────────────

    @Size(max = 255)
    @Column(name = "ETAT", length = 255)
    private String etat;

    @Size(max = 255)
    @Column(name = "STATUT", length = 255)
    private String statut;

    @Column(name = "DATECREATIONUSER")
    private LocalDate dateCreationUser;

    @Column(name = "dateDemande")
    private LocalDate dateDemande;

    @Column(name = "DATEVALIDATION")
    private LocalDate dateValidation;

    @Column(name = "DATEDEPART")
    private LocalDate dateDepart;

    @Column(name = "dateExpirPiece")
    private LocalDate dateExpirPiece;

    @Column(name = "DEPART")
    private Integer depart;

    @Size(max = 255)
    @Column(name = "MOTIFREJET", length = 255)
    private String motifRejet;

    @Column(name = "VISE")
    private Integer vise;

    // ── Counter & numbering ───────────────────────────────────────

    @Column(name = "compteur")
    private Long compteur;

    @Size(max = 255)
    @Column(name = "numManuelle", length = 255)
    private String numManuelle;

    @Column(name = "Numutil")
    private Long numUtil;

    // ── Financial ─────────────────────────────────────────────────

    @Column(name = "NBREPAS")
    private Long nbrePas;

    @Column(name = "depot", precision = 19, scale = 4)
    private BigDecimal depot;

    @Column(name = "DROITENTREE", precision = 19, scale = 4)
    private BigDecimal droitEntree;

    @Column(name = "FRAISOUVERT", precision = 19, scale = 4)
    private BigDecimal fraisOuvert;

    @Column(name = "mtSensibilisateur", precision = 19, scale = 4)
    private BigDecimal mtSensibilisateur;

    @Column(name = "nbre_signataire_exige")
    private Integer nbreSignataireExige;

    @Column(name = "sans_nni")
    private Integer sansNni;

    // ── Contact information ───────────────────────────────────────

    @Size(max = 255)
    @Column(name = "CONTACTPERSONNECONTACT", length = 255)
    private String contactPersonneContact;

    @Size(max = 255)
    @Column(name = "INFOPERSONNECONTACT", length = 255)
    private String infoPersonneContact;

    @Size(max = 255)
    @Column(name = "lieutEtablissementCarte", length = 255)
    private String lieutEtablissementCarte;

    @Size(max = 1)
    @Column(name = "MEMBREPRIVILEGE", length = 1)
    private String membrePrivilege;

    @Size(max = 255)
    @Column(name = "Observation", length = 255)
    private String observation;

    @Size(max = 255)
    @Column(name = "CARTE", length = 255)
    private String carte;

    // ── Family & social ───────────────────────────────────────────

    @Column(name = "NBRE_ENF")
    private Integer nbreEnf;

    @Column(name = "NBRE_PERS_CHARG")
    private Integer nbrePersCharg;

    @Size(max = 1)
    @Column(name = "LOGEMENT", length = 1)
    private String logement;

    @Column(name = "personcharge")
    private Integer personCharge;

    @Column(name = "nbreenfant")
    private Integer nbreEnfant;

    @Size(max = 1)
    @Column(name = "chargeable", length = 1)
    private String chargeable;

    // ── Document / piece ─────────────────────────────────────────

    @Column(name = "dateCreerPiece")
    private LocalDate dateCreerPiece;

    @Column(name = "PRELEVERAGIOS")
    private Integer preleverAgios;

    @Column(name = "CALCULERINTERET")
    private Integer calculerInteret;

    @Column(name = "RESIDENT")
    private Integer resident;

    @Size(max = 50)
    @Column(name = "pieceTransfert", length = 50)
    private String pieceTransfert;

    // ── Geography & location ──────────────────────────────────────

    @Size(max = 50)
    @Column(name = "longitude", length = 50)
    private String longitude;

    @Size(max = 50)
    @Column(name = "latitude", length = 50)
    private String latitude;

    @Column(name = "zoneg")
    private Long zoneG;

    // ── Account defaults ──────────────────────────────────────────

    @Size(max = 10)
    @Column(name = "devise_premier_compte", length = 10)
    private String devisePremiereCompte;

    @Size(max = 5)
    @Column(name = "code_retrait", length = 5)
    private String codeRetrait;

    // ── Legal member (moral person) columns ───────────────────────

    @Size(max = 255)
    @Column(name = "GROUPE", length = 255)
    private String groupe;

    @Size(max = 255)
    @Column(name = "raison_sociale", length = 255)
    private String raisonSociale;

    @Column(name = "DATEFONDATION")
    private LocalDate dateFondation;

    @Column(name = "nbrefemme")
    private Integer nbreFemme;

    @Column(name = "nbrehomme")
    private Integer nbreHomme;

    @Size(max = 30)
    @Column(name = "rccm", length = 30)
    private String rccm;

    @Size(max = 30)
    @Column(name = "numInstallation", length = 30)
    private String numInstallation;

    @Column(name = "cogerance")
    private Integer cogerance;

    @Size(max = 30)
    @Column(name = "NUM_AGREMENT", length = 30)
    private String numAgrement;

    @Column(name = "DATE_AGREMENT")
    private LocalDate dateAgrement;

    @Size(max = 30)
    @Column(name = "NUM_CONVENTION", length = 30)
    private String numConvention;

    @Column(name = "DATE_CONVENTION")
    private LocalDate dateConvention;

    @Size(max = 255)
    @Column(name = "raison_depart", length = 255)
    private String raisonDepart;

    @Column(name = "dirigeant")
    private Integer dirigeant;

    @Size(max = 25)
    @Column(name = "code_membre_def", length = 25)
    private String codeMembreDef;

    @Size(max = 25)
    @Column(name = "code_membre", length = 25)
    private String codeMembre;

    @Size(max = 50)
    @Column(name = "CODE_EMPLOYE", length = 50)
    private String codeEmploye;

    @Size(max = 50)
    @Column(name = "numero_national_id", length = 50)
    private String numeroNationalId;

    @Column(name = "datereel")
    private LocalDate dateReel;

    // ── Optimistic lock ───────────────────────────────────────────

    @Version
    @Column(name = "VERSION")
    private Integer version = 0;

    // ── FK to Phase-1 tables (wired) ─────────────────────────────

    /** Agence du membre – FK to AGENCE (Phase 1). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name                 = "agence",
        referencedColumnName = "CODE_AGENCE",
        foreignKey           = @ForeignKey(name = "FK_membres_AGENCE")
    )
    private Agence agence;

    /** Agence d'origine – FK to AGENCE (Phase 1). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name                 = "agenceOrigine",
        referencedColumnName = "CODE_AGENCE",
        foreignKey           = @ForeignKey(name = "FK_membres_AGENCE_origine")
    )
    private Agence agenceOrigine;

    /** Address – FK to ADRESSE (Phase 1). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name                 = "ADRESSE_IDADRESSE",
        referencedColumnName = "IDADRESSE",
        foreignKey           = @ForeignKey(name = "FK_membres_ADRESSE")
    )
    private Adresse adresse;

    /**
     * Forme juridique – FK to FormeJuridique (Phase 1, numeric PK).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name                 = "formeJuridique",
        referencedColumnName = "CODE",
        foreignKey           = @ForeignKey(name = "FK_membres_FormeJuridique")
    )
    private FormeJuridique formeJuridique;

    /**
     * FK to zoneGeographique (Phase 1, numeric Long PK).
     * Stored as raw Long to avoid circular dependency; @ManyToOne mapping
     * can be promoted once phase-2 relationship direction is confirmed.
     */
    @Column(name = "zoneGeographique")
    private Long zoneGeographique;

    /** TIERS entry for this member (Phase 2). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name                 = "CODE_TIERS",
        referencedColumnName = "CODE_TIERS",
        foreignKey           = @ForeignKey(name = "FK_membres_TIERS")
    )
    private Tiers tiers;

    // ── Phase-2+ FK columns (no @ManyToOne yet) ───────────────────

    /** Catégorie membre (AN/25). Constraint deferred. */
    @Size(max = 25)
    @Column(name = "categorie", length = 25)
    private String categorie;

    @Size(max = 50)
    @Column(name = "commercial", length = 50)
    private String commercial;

    @Size(max = 50)
    @Column(name = "gestionnaire", length = 50)
    private String gestionnaire;

    @Size(max = 25)
    @Column(name = "membrePere", length = 25)
    private String membrePere;

    @Size(max = 50)
    @Column(name = "parrain", length = 50)
    private String parrain;

    @Size(max = 50)
    @Column(name = "PARTSOCIALE", length = 50)
    private String partSociale;

    @Column(name = "pays")
    private Long pays;

    @Size(max = 50)
    @Column(name = "personneContacter", length = 50)
    private String personneContacter;

    @Size(max = 20)
    @Column(name = "produitEpargne", length = 20)
    private String produitEpargne;

    @Size(max = 20)
    @Column(name = "secteurActivite", length = 20)
    private String secteurActivite;

    @Size(max = 50)
    @Column(name = "sensibilisateur", length = 50)
    private String sensibilisateur;

    @Column(name = "sousTypeMembre")
    private Long sousTypeMembre;

    @Column(name = "typeClient")
    private Long typeClient;

    @Column(name = "typePiece")
    private Long typePiece;

    @Size(max = 50)
    @Column(name = "utilisateur", length = 50)
    private String utilisateur;

    @Size(max = 25)
    @Column(name = "zone", length = 25)
    private String zone;

    @Column(name = "photo")
    private Long photo;

    @Column(name = "TypeActivite")
    private Long typeActivite;

    @Column(name = "typeGroupement")
    private Long typeGroupement;

    @Column(name = "civilite")
    private Long civilite;

    @Column(name = "section")
    private Long section;

    @Column(name = "signature")
    private Long signature;

    @Column(name = "ACTIVITE")
    private Long activite;

    @Column(name = "ID_STRUCT_ADMIN")
    private Long idStructAdmin;

    @Column(name = "ID_GS")
    private Long idGs;

    @Column(name = "MOTIFDEPART")
    private Long motifDepart;

    @Column(name = "paysCreerPiece")
    private Long paysCreerPiece;

    @Size(max = 255)
    @Column(name = "guichet_mobile", length = 255)
    private String guichetMobile;

    @Column(name = "profession")
    private Long profession;

    // ── Generated (was Lombok) ──────────────────────────────────────────────

    public Membres() {
    }

    public Membres(String numMembre, String dtype, String nom, String prenom, String nomJeuneFille, String nomMere, String nomPere, String sexe, LocalDate dateNaissance, String lieuNaissance, Integer age, String analphabete, String situationMatrimoniale, String matriculeMembre, String personneAcontacter, String etat, String statut, LocalDate dateCreationUser, LocalDate dateDemande, LocalDate dateValidation, LocalDate dateDepart, LocalDate dateExpirPiece, Integer depart, String motifRejet, Integer vise, Long compteur, String numManuelle, Long numUtil, Long nbrePas, BigDecimal depot, BigDecimal droitEntree, BigDecimal fraisOuvert, BigDecimal mtSensibilisateur, Integer nbreSignataireExige, Integer sansNni, String contactPersonneContact, String infoPersonneContact, String lieutEtablissementCarte, String membrePrivilege, String observation, String carte, Integer nbreEnf, Integer nbrePersCharg, String logement, Integer personCharge, Integer nbreEnfant, String chargeable, LocalDate dateCreerPiece, Integer preleverAgios, Integer calculerInteret, Integer resident, String pieceTransfert, String longitude, String latitude, Long zoneG, String devisePremiereCompte, String codeRetrait, String groupe, String raisonSociale, LocalDate dateFondation, Integer nbreFemme, Integer nbreHomme, String rccm, String numInstallation, Integer cogerance, String numAgrement, LocalDate dateAgrement, String numConvention, LocalDate dateConvention, String raisonDepart, Integer dirigeant, String codeMembreDef, String codeMembre, String codeEmploye, String numeroNationalId, LocalDate dateReel, Integer version, Agence agence, Agence agenceOrigine, Adresse adresse, FormeJuridique formeJuridique, Long zoneGeographique, Tiers tiers, String categorie, String commercial, String gestionnaire, String membrePere, String parrain, String partSociale, Long pays, String personneContacter, String produitEpargne, String secteurActivite, String sensibilisateur, Long sousTypeMembre, Long typeClient, Long typePiece, String utilisateur, String zone, Long photo, Long typeActivite, Long typeGroupement, Long civilite, Long section, Long signature, Long activite, Long idStructAdmin, Long idGs, Long motifDepart, Long paysCreerPiece, String guichetMobile, Long profession) {
        this.numMembre = numMembre;
        this.dtype = dtype;
        this.nom = nom;
        this.prenom = prenom;
        this.nomJeuneFille = nomJeuneFille;
        this.nomMere = nomMere;
        this.nomPere = nomPere;
        this.sexe = sexe;
        this.dateNaissance = dateNaissance;
        this.lieuNaissance = lieuNaissance;
        this.age = age;
        this.analphabete = analphabete;
        this.situationMatrimoniale = situationMatrimoniale;
        this.matriculeMembre = matriculeMembre;
        this.personneAcontacter = personneAcontacter;
        this.etat = etat;
        this.statut = statut;
        this.dateCreationUser = dateCreationUser;
        this.dateDemande = dateDemande;
        this.dateValidation = dateValidation;
        this.dateDepart = dateDepart;
        this.dateExpirPiece = dateExpirPiece;
        this.depart = depart;
        this.motifRejet = motifRejet;
        this.vise = vise;
        this.compteur = compteur;
        this.numManuelle = numManuelle;
        this.numUtil = numUtil;
        this.nbrePas = nbrePas;
        this.depot = depot;
        this.droitEntree = droitEntree;
        this.fraisOuvert = fraisOuvert;
        this.mtSensibilisateur = mtSensibilisateur;
        this.nbreSignataireExige = nbreSignataireExige;
        this.sansNni = sansNni;
        this.contactPersonneContact = contactPersonneContact;
        this.infoPersonneContact = infoPersonneContact;
        this.lieutEtablissementCarte = lieutEtablissementCarte;
        this.membrePrivilege = membrePrivilege;
        this.observation = observation;
        this.carte = carte;
        this.nbreEnf = nbreEnf;
        this.nbrePersCharg = nbrePersCharg;
        this.logement = logement;
        this.personCharge = personCharge;
        this.nbreEnfant = nbreEnfant;
        this.chargeable = chargeable;
        this.dateCreerPiece = dateCreerPiece;
        this.preleverAgios = preleverAgios;
        this.calculerInteret = calculerInteret;
        this.resident = resident;
        this.pieceTransfert = pieceTransfert;
        this.longitude = longitude;
        this.latitude = latitude;
        this.zoneG = zoneG;
        this.devisePremiereCompte = devisePremiereCompte;
        this.codeRetrait = codeRetrait;
        this.groupe = groupe;
        this.raisonSociale = raisonSociale;
        this.dateFondation = dateFondation;
        this.nbreFemme = nbreFemme;
        this.nbreHomme = nbreHomme;
        this.rccm = rccm;
        this.numInstallation = numInstallation;
        this.cogerance = cogerance;
        this.numAgrement = numAgrement;
        this.dateAgrement = dateAgrement;
        this.numConvention = numConvention;
        this.dateConvention = dateConvention;
        this.raisonDepart = raisonDepart;
        this.dirigeant = dirigeant;
        this.codeMembreDef = codeMembreDef;
        this.codeMembre = codeMembre;
        this.codeEmploye = codeEmploye;
        this.numeroNationalId = numeroNationalId;
        this.dateReel = dateReel;
        this.version = version;
        this.agence = agence;
        this.agenceOrigine = agenceOrigine;
        this.adresse = adresse;
        this.formeJuridique = formeJuridique;
        this.zoneGeographique = zoneGeographique;
        this.tiers = tiers;
        this.categorie = categorie;
        this.commercial = commercial;
        this.gestionnaire = gestionnaire;
        this.membrePere = membrePere;
        this.parrain = parrain;
        this.partSociale = partSociale;
        this.pays = pays;
        this.personneContacter = personneContacter;
        this.produitEpargne = produitEpargne;
        this.secteurActivite = secteurActivite;
        this.sensibilisateur = sensibilisateur;
        this.sousTypeMembre = sousTypeMembre;
        this.typeClient = typeClient;
        this.typePiece = typePiece;
        this.utilisateur = utilisateur;
        this.zone = zone;
        this.photo = photo;
        this.typeActivite = typeActivite;
        this.typeGroupement = typeGroupement;
        this.civilite = civilite;
        this.section = section;
        this.signature = signature;
        this.activite = activite;
        this.idStructAdmin = idStructAdmin;
        this.idGs = idGs;
        this.motifDepart = motifDepart;
        this.paysCreerPiece = paysCreerPiece;
        this.guichetMobile = guichetMobile;
        this.profession = profession;
    }

    public String getNumMembre() {
        return numMembre;
    }

    public void setNumMembre(String numMembre) {
        this.numMembre = numMembre;
    }

    public String getDtype() {
        return dtype;
    }

    public void setDtype(String dtype) {
        this.dtype = dtype;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getNomJeuneFille() {
        return nomJeuneFille;
    }

    public void setNomJeuneFille(String nomJeuneFille) {
        this.nomJeuneFille = nomJeuneFille;
    }

    public String getNomMere() {
        return nomMere;
    }

    public void setNomMere(String nomMere) {
        this.nomMere = nomMere;
    }

    public String getNomPere() {
        return nomPere;
    }

    public void setNomPere(String nomPere) {
        this.nomPere = nomPere;
    }

    public String getSexe() {
        return sexe;
    }

    public void setSexe(String sexe) {
        this.sexe = sexe;
    }

    public LocalDate getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(LocalDate dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public String getLieuNaissance() {
        return lieuNaissance;
    }

    public void setLieuNaissance(String lieuNaissance) {
        this.lieuNaissance = lieuNaissance;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getAnalphabete() {
        return analphabete;
    }

    public void setAnalphabete(String analphabete) {
        this.analphabete = analphabete;
    }

    public String getSituationMatrimoniale() {
        return situationMatrimoniale;
    }

    public void setSituationMatrimoniale(String situationMatrimoniale) {
        this.situationMatrimoniale = situationMatrimoniale;
    }

    public String getMatriculeMembre() {
        return matriculeMembre;
    }

    public void setMatriculeMembre(String matriculeMembre) {
        this.matriculeMembre = matriculeMembre;
    }

    public String getPersonneAcontacter() {
        return personneAcontacter;
    }

    public void setPersonneAcontacter(String personneAcontacter) {
        this.personneAcontacter = personneAcontacter;
    }

    public String getEtat() {
        return etat;
    }

    public void setEtat(String etat) {
        this.etat = etat;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public LocalDate getDateCreationUser() {
        return dateCreationUser;
    }

    public void setDateCreationUser(LocalDate dateCreationUser) {
        this.dateCreationUser = dateCreationUser;
    }

    public LocalDate getDateDemande() {
        return dateDemande;
    }

    public void setDateDemande(LocalDate dateDemande) {
        this.dateDemande = dateDemande;
    }

    public LocalDate getDateValidation() {
        return dateValidation;
    }

    public void setDateValidation(LocalDate dateValidation) {
        this.dateValidation = dateValidation;
    }

    public LocalDate getDateDepart() {
        return dateDepart;
    }

    public void setDateDepart(LocalDate dateDepart) {
        this.dateDepart = dateDepart;
    }

    public LocalDate getDateExpirPiece() {
        return dateExpirPiece;
    }

    public void setDateExpirPiece(LocalDate dateExpirPiece) {
        this.dateExpirPiece = dateExpirPiece;
    }

    public Integer getDepart() {
        return depart;
    }

    public void setDepart(Integer depart) {
        this.depart = depart;
    }

    public String getMotifRejet() {
        return motifRejet;
    }

    public void setMotifRejet(String motifRejet) {
        this.motifRejet = motifRejet;
    }

    public Integer getVise() {
        return vise;
    }

    public void setVise(Integer vise) {
        this.vise = vise;
    }

    public Long getCompteur() {
        return compteur;
    }

    public void setCompteur(Long compteur) {
        this.compteur = compteur;
    }

    public String getNumManuelle() {
        return numManuelle;
    }

    public void setNumManuelle(String numManuelle) {
        this.numManuelle = numManuelle;
    }

    public Long getNumUtil() {
        return numUtil;
    }

    public void setNumUtil(Long numUtil) {
        this.numUtil = numUtil;
    }

    public Long getNbrePas() {
        return nbrePas;
    }

    public void setNbrePas(Long nbrePas) {
        this.nbrePas = nbrePas;
    }

    public BigDecimal getDepot() {
        return depot;
    }

    public void setDepot(BigDecimal depot) {
        this.depot = depot;
    }

    public BigDecimal getDroitEntree() {
        return droitEntree;
    }

    public void setDroitEntree(BigDecimal droitEntree) {
        this.droitEntree = droitEntree;
    }

    public BigDecimal getFraisOuvert() {
        return fraisOuvert;
    }

    public void setFraisOuvert(BigDecimal fraisOuvert) {
        this.fraisOuvert = fraisOuvert;
    }

    public BigDecimal getMtSensibilisateur() {
        return mtSensibilisateur;
    }

    public void setMtSensibilisateur(BigDecimal mtSensibilisateur) {
        this.mtSensibilisateur = mtSensibilisateur;
    }

    public Integer getNbreSignataireExige() {
        return nbreSignataireExige;
    }

    public void setNbreSignataireExige(Integer nbreSignataireExige) {
        this.nbreSignataireExige = nbreSignataireExige;
    }

    public Integer getSansNni() {
        return sansNni;
    }

    public void setSansNni(Integer sansNni) {
        this.sansNni = sansNni;
    }

    public String getContactPersonneContact() {
        return contactPersonneContact;
    }

    public void setContactPersonneContact(String contactPersonneContact) {
        this.contactPersonneContact = contactPersonneContact;
    }

    public String getInfoPersonneContact() {
        return infoPersonneContact;
    }

    public void setInfoPersonneContact(String infoPersonneContact) {
        this.infoPersonneContact = infoPersonneContact;
    }

    public String getLieutEtablissementCarte() {
        return lieutEtablissementCarte;
    }

    public void setLieutEtablissementCarte(String lieutEtablissementCarte) {
        this.lieutEtablissementCarte = lieutEtablissementCarte;
    }

    public String getMembrePrivilege() {
        return membrePrivilege;
    }

    public void setMembrePrivilege(String membrePrivilege) {
        this.membrePrivilege = membrePrivilege;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    public String getCarte() {
        return carte;
    }

    public void setCarte(String carte) {
        this.carte = carte;
    }

    public Integer getNbreEnf() {
        return nbreEnf;
    }

    public void setNbreEnf(Integer nbreEnf) {
        this.nbreEnf = nbreEnf;
    }

    public Integer getNbrePersCharg() {
        return nbrePersCharg;
    }

    public void setNbrePersCharg(Integer nbrePersCharg) {
        this.nbrePersCharg = nbrePersCharg;
    }

    public String getLogement() {
        return logement;
    }

    public void setLogement(String logement) {
        this.logement = logement;
    }

    public Integer getPersonCharge() {
        return personCharge;
    }

    public void setPersonCharge(Integer personCharge) {
        this.personCharge = personCharge;
    }

    public Integer getNbreEnfant() {
        return nbreEnfant;
    }

    public void setNbreEnfant(Integer nbreEnfant) {
        this.nbreEnfant = nbreEnfant;
    }

    public String getChargeable() {
        return chargeable;
    }

    public void setChargeable(String chargeable) {
        this.chargeable = chargeable;
    }

    public LocalDate getDateCreerPiece() {
        return dateCreerPiece;
    }

    public void setDateCreerPiece(LocalDate dateCreerPiece) {
        this.dateCreerPiece = dateCreerPiece;
    }

    public Integer getPreleverAgios() {
        return preleverAgios;
    }

    public void setPreleverAgios(Integer preleverAgios) {
        this.preleverAgios = preleverAgios;
    }

    public Integer getCalculerInteret() {
        return calculerInteret;
    }

    public void setCalculerInteret(Integer calculerInteret) {
        this.calculerInteret = calculerInteret;
    }

    public Integer getResident() {
        return resident;
    }

    public void setResident(Integer resident) {
        this.resident = resident;
    }

    public String getPieceTransfert() {
        return pieceTransfert;
    }

    public void setPieceTransfert(String pieceTransfert) {
        this.pieceTransfert = pieceTransfert;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public Long getZoneG() {
        return zoneG;
    }

    public void setZoneG(Long zoneG) {
        this.zoneG = zoneG;
    }

    public String getDevisePremiereCompte() {
        return devisePremiereCompte;
    }

    public void setDevisePremiereCompte(String devisePremiereCompte) {
        this.devisePremiereCompte = devisePremiereCompte;
    }

    public String getCodeRetrait() {
        return codeRetrait;
    }

    public void setCodeRetrait(String codeRetrait) {
        this.codeRetrait = codeRetrait;
    }

    public String getGroupe() {
        return groupe;
    }

    public void setGroupe(String groupe) {
        this.groupe = groupe;
    }

    public String getRaisonSociale() {
        return raisonSociale;
    }

    public void setRaisonSociale(String raisonSociale) {
        this.raisonSociale = raisonSociale;
    }

    public LocalDate getDateFondation() {
        return dateFondation;
    }

    public void setDateFondation(LocalDate dateFondation) {
        this.dateFondation = dateFondation;
    }

    public Integer getNbreFemme() {
        return nbreFemme;
    }

    public void setNbreFemme(Integer nbreFemme) {
        this.nbreFemme = nbreFemme;
    }

    public Integer getNbreHomme() {
        return nbreHomme;
    }

    public void setNbreHomme(Integer nbreHomme) {
        this.nbreHomme = nbreHomme;
    }

    public String getRccm() {
        return rccm;
    }

    public void setRccm(String rccm) {
        this.rccm = rccm;
    }

    public String getNumInstallation() {
        return numInstallation;
    }

    public void setNumInstallation(String numInstallation) {
        this.numInstallation = numInstallation;
    }

    public Integer getCogerance() {
        return cogerance;
    }

    public void setCogerance(Integer cogerance) {
        this.cogerance = cogerance;
    }

    public String getNumAgrement() {
        return numAgrement;
    }

    public void setNumAgrement(String numAgrement) {
        this.numAgrement = numAgrement;
    }

    public LocalDate getDateAgrement() {
        return dateAgrement;
    }

    public void setDateAgrement(LocalDate dateAgrement) {
        this.dateAgrement = dateAgrement;
    }

    public String getNumConvention() {
        return numConvention;
    }

    public void setNumConvention(String numConvention) {
        this.numConvention = numConvention;
    }

    public LocalDate getDateConvention() {
        return dateConvention;
    }

    public void setDateConvention(LocalDate dateConvention) {
        this.dateConvention = dateConvention;
    }

    public String getRaisonDepart() {
        return raisonDepart;
    }

    public void setRaisonDepart(String raisonDepart) {
        this.raisonDepart = raisonDepart;
    }

    public Integer getDirigeant() {
        return dirigeant;
    }

    public void setDirigeant(Integer dirigeant) {
        this.dirigeant = dirigeant;
    }

    public String getCodeMembreDef() {
        return codeMembreDef;
    }

    public void setCodeMembreDef(String codeMembreDef) {
        this.codeMembreDef = codeMembreDef;
    }

    public String getCodeMembre() {
        return codeMembre;
    }

    public void setCodeMembre(String codeMembre) {
        this.codeMembre = codeMembre;
    }

    public String getCodeEmploye() {
        return codeEmploye;
    }

    public void setCodeEmploye(String codeEmploye) {
        this.codeEmploye = codeEmploye;
    }

    public String getNumeroNationalId() {
        return numeroNationalId;
    }

    public void setNumeroNationalId(String numeroNationalId) {
        this.numeroNationalId = numeroNationalId;
    }

    public LocalDate getDateReel() {
        return dateReel;
    }

    public void setDateReel(LocalDate dateReel) {
        this.dateReel = dateReel;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Agence getAgence() {
        return agence;
    }

    public void setAgence(Agence agence) {
        this.agence = agence;
    }

    public Agence getAgenceOrigine() {
        return agenceOrigine;
    }

    public void setAgenceOrigine(Agence agenceOrigine) {
        this.agenceOrigine = agenceOrigine;
    }

    public Adresse getAdresse() {
        return adresse;
    }

    public void setAdresse(Adresse adresse) {
        this.adresse = adresse;
    }

    public FormeJuridique getFormeJuridique() {
        return formeJuridique;
    }

    public void setFormeJuridique(FormeJuridique formeJuridique) {
        this.formeJuridique = formeJuridique;
    }

    public Long getZoneGeographique() {
        return zoneGeographique;
    }

    public void setZoneGeographique(Long zoneGeographique) {
        this.zoneGeographique = zoneGeographique;
    }

    public Tiers getTiers() {
        return tiers;
    }

    public void setTiers(Tiers tiers) {
        this.tiers = tiers;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public String getCommercial() {
        return commercial;
    }

    public void setCommercial(String commercial) {
        this.commercial = commercial;
    }

    public String getGestionnaire() {
        return gestionnaire;
    }

    public void setGestionnaire(String gestionnaire) {
        this.gestionnaire = gestionnaire;
    }

    public String getMembrePere() {
        return membrePere;
    }

    public void setMembrePere(String membrePere) {
        this.membrePere = membrePere;
    }

    public String getParrain() {
        return parrain;
    }

    public void setParrain(String parrain) {
        this.parrain = parrain;
    }

    public String getPartSociale() {
        return partSociale;
    }

    public void setPartSociale(String partSociale) {
        this.partSociale = partSociale;
    }

    public Long getPays() {
        return pays;
    }

    public void setPays(Long pays) {
        this.pays = pays;
    }

    public String getPersonneContacter() {
        return personneContacter;
    }

    public void setPersonneContacter(String personneContacter) {
        this.personneContacter = personneContacter;
    }

    public String getProduitEpargne() {
        return produitEpargne;
    }

    public void setProduitEpargne(String produitEpargne) {
        this.produitEpargne = produitEpargne;
    }

    public String getSecteurActivite() {
        return secteurActivite;
    }

    public void setSecteurActivite(String secteurActivite) {
        this.secteurActivite = secteurActivite;
    }

    public String getSensibilisateur() {
        return sensibilisateur;
    }

    public void setSensibilisateur(String sensibilisateur) {
        this.sensibilisateur = sensibilisateur;
    }

    public Long getSousTypeMembre() {
        return sousTypeMembre;
    }

    public void setSousTypeMembre(Long sousTypeMembre) {
        this.sousTypeMembre = sousTypeMembre;
    }

    public Long getTypeClient() {
        return typeClient;
    }

    public void setTypeClient(Long typeClient) {
        this.typeClient = typeClient;
    }

    public Long getTypePiece() {
        return typePiece;
    }

    public void setTypePiece(Long typePiece) {
        this.typePiece = typePiece;
    }

    public String getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(String utilisateur) {
        this.utilisateur = utilisateur;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public Long getPhoto() {
        return photo;
    }

    public void setPhoto(Long photo) {
        this.photo = photo;
    }

    public Long getTypeActivite() {
        return typeActivite;
    }

    public void setTypeActivite(Long typeActivite) {
        this.typeActivite = typeActivite;
    }

    public Long getTypeGroupement() {
        return typeGroupement;
    }

    public void setTypeGroupement(Long typeGroupement) {
        this.typeGroupement = typeGroupement;
    }

    public Long getCivilite() {
        return civilite;
    }

    public void setCivilite(Long civilite) {
        this.civilite = civilite;
    }

    public Long getSection() {
        return section;
    }

    public void setSection(Long section) {
        this.section = section;
    }

    public Long getSignature() {
        return signature;
    }

    public void setSignature(Long signature) {
        this.signature = signature;
    }

    public Long getActivite() {
        return activite;
    }

    public void setActivite(Long activite) {
        this.activite = activite;
    }

    public Long getIdStructAdmin() {
        return idStructAdmin;
    }

    public void setIdStructAdmin(Long idStructAdmin) {
        this.idStructAdmin = idStructAdmin;
    }

    public Long getIdGs() {
        return idGs;
    }

    public void setIdGs(Long idGs) {
        this.idGs = idGs;
    }

    public Long getMotifDepart() {
        return motifDepart;
    }

    public void setMotifDepart(Long motifDepart) {
        this.motifDepart = motifDepart;
    }

    public Long getPaysCreerPiece() {
        return paysCreerPiece;
    }

    public void setPaysCreerPiece(Long paysCreerPiece) {
        this.paysCreerPiece = paysCreerPiece;
    }

    public String getGuichetMobile() {
        return guichetMobile;
    }

    public void setGuichetMobile(String guichetMobile) {
        this.guichetMobile = guichetMobile;
    }

    public Long getProfession() {
        return profession;
    }

    public void setProfession(Long profession) {
        this.profession = profession;
    }

    @Override
    public String toString() {
        return "Membres("
            + "numMembre=" + numMembre
            + ")";
    }
}
