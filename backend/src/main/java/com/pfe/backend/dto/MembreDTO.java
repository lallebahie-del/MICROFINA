package com.pfe.backend.dto;

import com.microfina.entity.Membres;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * MembreDTO – flattened projection for the Membres list and form views.
 *
 * Only the fields needed by the Angular UI are exposed here.
 * Lazy-loaded FK associations are resolved to their ID / label strings
 * to avoid N+1 queries when serialising.
 */
public class MembreDTO {

    // ── Request records (Phase 11.1) ──────────────────────────────────────────

    /**
     * Corps de la requête POST /api/v1/membres.
     */
    public record CreateRequest(
            @NotBlank(message = "Le numéro membre est obligatoire")
            @Size(max = 20, message = "numMembre ≤ 20 caractères")
            String numMembre,

            @NotBlank(message = "Le nom est obligatoire")
            @Size(max = 100, message = "nom ≤ 100 caractères")
            String nom,

            @Size(max = 100) String prenom,

            @Pattern(regexp = "^(M|F|)$", message = "sexe doit être M, F ou vide")
            String sexe,

            String dtype,
            String codeAgence,
            LocalDate dateNaissance,
            String lieuNaissance,
            String situationMatrimoniale,
            String categorie,
            String secteurActivite,

            @Size(max = 50) String numeroNationalId,
            @Size(max = 50) String matriculeMembre,
            @Size(max = 200) String raisonSociale,

            String etat,
            String statut,
            LocalDate dateDemande,
            BigDecimal depot,
            BigDecimal droitEntree,
            String observation,
            String personneAcontacter,
            String contactPersonneContact,
            String infoPersonneContact
    ) {}

    /**
     * Corps de la requête PUT /api/v1/membres/{numMembre}.
     * numMembre vient du path variable — pas dans le corps.
     */
    public record UpdateRequest(
            @NotBlank(message = "Le nom est obligatoire")
            @Size(max = 100, message = "nom ≤ 100 caractères")
            String nom,

            @Size(max = 100) String prenom,

            @Pattern(regexp = "^(M|F|)$", message = "sexe doit être M, F ou vide")
            String sexe,

            String dtype,
            String codeAgence,
            LocalDate dateNaissance,
            String lieuNaissance,
            String situationMatrimoniale,
            String categorie,
            String secteurActivite,

            @Size(max = 50) String numeroNationalId,
            @Size(max = 50) String matriculeMembre,
            @Size(max = 200) String raisonSociale,

            String etat,
            String statut,
            LocalDate dateDemande,
            BigDecimal depot,
            BigDecimal droitEntree,
            String observation,
            String personneAcontacter,
            String contactPersonneContact,
            String infoPersonneContact
    ) {}

    // ─────────────────────────────────────────────────────────────────────────

    // ── Identity ──────────────────────────────────────────────────
    private String   numMembre;
    private String   dtype;
    private String   nom;
    private String   prenom;
    private String   nomJeuneFille;
    private String   sexe;
    private LocalDate dateNaissance;
    private String   lieuNaissance;
    private Integer  age;
    private String   situationMatrimoniale;
    private String   matriculeMembre;
    private String   numeroNationalId;
    private String   raisonSociale;         // moral persons

    // ── Status ────────────────────────────────────────────────────
    private String   etat;
    private String   statut;
    private LocalDate dateDemande;
    private LocalDate dateValidation;
    private LocalDate dateDepart;
    private String   motifRejet;

    // ── Contact ───────────────────────────────────────────────────
    private String   personneAcontacter;
    private String   contactPersonneContact;
    private String   infoPersonneContact;
    private String   observation;

    // ── FK labels (resolved from associations) ────────────────────
    private String   agenceCode;
    private String   agenceLibelle;
    private String   categorie;
    private String   secteurActivite;

    // ── Financial ─────────────────────────────────────────────────
    private BigDecimal depot;
    private BigDecimal droitEntree;

    // ── Misc ──────────────────────────────────────────────────────
    private String   codeMembre;

    // ── Constructor (from entity) ─────────────────────────────────

    public MembreDTO() {}

    /**
     * Factory method: builds a DTO from a JPA entity.
     * Association fields are resolved without triggering LAZY loading
     * by checking for null proxies before calling getters.
     */
    public static MembreDTO from(Membres m) {
        MembreDTO dto = new MembreDTO();
        dto.numMembre             = m.getNumMembre();
        dto.dtype                 = m.getDtype();
        dto.nom                   = m.getNom();
        dto.prenom                = m.getPrenom();
        dto.nomJeuneFille         = m.getNomJeuneFille();
        dto.sexe                  = m.getSexe();
        dto.dateNaissance         = m.getDateNaissance();
        dto.lieuNaissance         = m.getLieuNaissance();
        dto.age                   = m.getAge();
        dto.situationMatrimoniale = m.getSituationMatrimoniale();
        dto.matriculeMembre       = m.getMatriculeMembre();
        dto.numeroNationalId      = m.getNumeroNationalId();
        dto.raisonSociale         = m.getRaisonSociale();
        dto.etat                  = m.getEtat();
        dto.statut                = m.getStatut();
        dto.dateDemande           = m.getDateDemande();
        dto.dateValidation        = m.getDateValidation();
        dto.dateDepart            = m.getDateDepart();
        dto.motifRejet            = m.getMotifRejet();
        dto.personneAcontacter    = m.getPersonneAcontacter();
        dto.contactPersonneContact = m.getContactPersonneContact();
        dto.infoPersonneContact   = m.getInfoPersonneContact();
        dto.observation           = m.getObservation();
        dto.categorie             = m.getCategorie();
        dto.secteurActivite       = m.getSecteurActivite();
        dto.depot                 = m.getDepot();
        dto.droitEntree           = m.getDroitEntree();
        dto.codeMembre            = m.getCodeMembre();

        // Resolve lazy FK
        if (m.getAgence() != null) {
            dto.agenceCode    = m.getAgence().getCodeAgence();
            dto.agenceLibelle = m.getAgence().getNomAgence();
        }

        return dto;
    }

    // ── Getters / Setters ─────────────────────────────────────────

    public String getNumMembre()                    { return numMembre; }
    public void   setNumMembre(String v)            { this.numMembre = v; }

    public String getDtype()                        { return dtype; }
    public void   setDtype(String v)                { this.dtype = v; }

    public String getNom()                          { return nom; }
    public void   setNom(String v)                  { this.nom = v; }

    public String getPrenom()                       { return prenom; }
    public void   setPrenom(String v)               { this.prenom = v; }

    public String getNomJeuneFille()                { return nomJeuneFille; }
    public void   setNomJeuneFille(String v)        { this.nomJeuneFille = v; }

    public String getSexe()                         { return sexe; }
    public void   setSexe(String v)                 { this.sexe = v; }

    public LocalDate getDateNaissance()             { return dateNaissance; }
    public void      setDateNaissance(LocalDate v)  { this.dateNaissance = v; }

    public String getLieuNaissance()                { return lieuNaissance; }
    public void   setLieuNaissance(String v)        { this.lieuNaissance = v; }

    public Integer getAge()                         { return age; }
    public void    setAge(Integer v)                { this.age = v; }

    public String getSituationMatrimoniale()        { return situationMatrimoniale; }
    public void   setSituationMatrimoniale(String v){ this.situationMatrimoniale = v; }

    public String getMatriculeMembre()              { return matriculeMembre; }
    public void   setMatriculeMembre(String v)      { this.matriculeMembre = v; }

    public String getNumeroNationalId()             { return numeroNationalId; }
    public void   setNumeroNationalId(String v)     { this.numeroNationalId = v; }

    public String getRaisonSociale()                { return raisonSociale; }
    public void   setRaisonSociale(String v)        { this.raisonSociale = v; }

    public String getEtat()                         { return etat; }
    public void   setEtat(String v)                 { this.etat = v; }

    public String getStatut()                       { return statut; }
    public void   setStatut(String v)               { this.statut = v; }

    public LocalDate getDateDemande()               { return dateDemande; }
    public void      setDateDemande(LocalDate v)    { this.dateDemande = v; }

    public LocalDate getDateValidation()            { return dateValidation; }
    public void      setDateValidation(LocalDate v) { this.dateValidation = v; }

    public LocalDate getDateDepart()                { return dateDepart; }
    public void      setDateDepart(LocalDate v)     { this.dateDepart = v; }

    public String getMotifRejet()                   { return motifRejet; }
    public void   setMotifRejet(String v)           { this.motifRejet = v; }

    public String getPersonneAcontacter()           { return personneAcontacter; }
    public void   setPersonneAcontacter(String v)   { this.personneAcontacter = v; }

    public String getContactPersonneContact()       { return contactPersonneContact; }
    public void   setContactPersonneContact(String v){ this.contactPersonneContact = v; }

    public String getInfoPersonneContact()          { return infoPersonneContact; }
    public void   setInfoPersonneContact(String v)  { this.infoPersonneContact = v; }

    public String getObservation()                  { return observation; }
    public void   setObservation(String v)          { this.observation = v; }

    public String getAgenceCode()                   { return agenceCode; }
    public void   setAgenceCode(String v)           { this.agenceCode = v; }

    public String getAgenceLibelle()                { return agenceLibelle; }
    public void   setAgenceLibelle(String v)        { this.agenceLibelle = v; }

    public String getCategorie()                    { return categorie; }
    public void   setCategorie(String v)            { this.categorie = v; }

    public String getSecteurActivite()              { return secteurActivite; }
    public void   setSecteurActivite(String v)      { this.secteurActivite = v; }

    public BigDecimal getDepot()                    { return depot; }
    public void       setDepot(BigDecimal v)        { this.depot = v; }

    public BigDecimal getDroitEntree()              { return droitEntree; }
    public void       setDroitEntree(BigDecimal v)  { this.droitEntree = v; }

    public String getCodeMembre()                   { return codeMembre; }
    public void   setCodeMembre(String v)           { this.codeMembre = v; }
}
