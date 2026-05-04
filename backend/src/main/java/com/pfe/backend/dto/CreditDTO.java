package com.pfe.backend.dto;

import com.microfina.entity.Credits;
import com.microfina.entity.CreditStatut;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * CreditDTO – flattened view of a Credits entity for the Angular list and detail pages.
 */
public class CreditDTO {

    // ── Request records (Phase 11.1 — @Valid bodies) ──────────────────────────

    /**
     * Corps de la requête POST /api/v1/credits.
     * Seuls les champs raisonnablement saisissables à la création sont exposés.
     */
    public record CreateRequest(
            @NotBlank(message = "Le numéro membre est obligatoire")
            String numMembre,

            @NotBlank(message = "Le code produit est obligatoire")
            String numProduit,

            @NotBlank(message = "Le code agence est obligatoire")
            String codeAgence,

            @NotNull(message = "Le montant demandé est obligatoire")
            @DecimalMin(value = "0.01", message = "Le montant doit être supérieur à 0")
            BigDecimal montantDemande,

            @NotBlank(message = "La périodicité est obligatoire")
            String periodicite,

            @NotNull(message = "La durée est obligatoire")
            @Min(value = 1, message = "La durée doit être ≥ 1")
            Integer duree,

            @NotNull(message = "Le nombre d'échéances est obligatoire")
            @Min(value = 1, message = "Le nombre d'échéances doit être ≥ 1")
            Integer nombreEcheance,

            Integer delaiGrace,
            String  objetCredit,
            Integer numeroCycle,
            LocalDate dateDemande,

            /* Taux (optionnels — copie depuis produit si absents) */
            BigDecimal tauxInteret,
            BigDecimal tauxPenalite,
            BigDecimal tauxCommission,
            BigDecimal tauxAssurance
    ) {}

    /**
     * Corps de la requête PUT /api/v1/credits/{id}.
     * Modification uniquement en statut BROUILLON.
     */
    public record UpdateRequest(
            @NotNull(message = "Le montant demandé est obligatoire")
            @DecimalMin(value = "0.01", message = "Le montant doit être supérieur à 0")
            BigDecimal montantDemande,

            @NotBlank(message = "La périodicité est obligatoire")
            String periodicite,

            @NotNull(message = "La durée est obligatoire")
            @Min(value = 1, message = "La durée doit être ≥ 1")
            Integer duree,

            @NotNull(message = "Le nombre d'échéances est obligatoire")
            @Min(value = 1, message = "Le nombre d'échéances doit être ≥ 1")
            Integer nombreEcheance,

            Integer delaiGrace,
            String  objetCredit,
            Integer numeroCycle,
            LocalDate dateDemande,
            BigDecimal tauxInteret,
            BigDecimal tauxPenalite,
            BigDecimal tauxCommission,
            BigDecimal tauxAssurance
    ) {}

    // ─────────────────────────────────────────────────────────────────────────

    // ── Identity ──────────────────────────────────────────────────
    private Long         idCredit;
    private String       numCredit;
    private CreditStatut statut;

    // ── Amounts ───────────────────────────────────────────────────
    private BigDecimal montantDemande;
    private BigDecimal montantAccorde;
    private BigDecimal montantDebloquer;
    private BigDecimal soldeCapital;
    private BigDecimal soldeInteret;
    private BigDecimal soldePenalite;

    // ── Rates ─────────────────────────────────────────────────────
    private BigDecimal tauxInteret;
    private BigDecimal tauxPenalite;
    private BigDecimal tauxCommission;
    private BigDecimal tauxAssurance;

    // ── Schedule ──────────────────────────────────────────────────
    private Integer duree;
    private Integer nombreEcheance;
    private Integer delaiGrace;
    private String  periodicite;

    // ── Dates ─────────────────────────────────────────────────────
    private LocalDate dateDemande;
    private LocalDate dateAccord;
    private LocalDate dateDeblocage;
    private LocalDate dateEcheance;
    private LocalDate dateCloture;

    // ── Classification ────────────────────────────────────────────
    private String  objetCredit;
    private Integer numeroCycle;

    // ── FK labels ─────────────────────────────────────────────────
    private String membreNum;
    private String membreNom;
    private String membrePrenom;
    private String produitCode;
    private String produitNom;
    private String agenceCode;
    private String agenceNom;

    public CreditDTO() {}

    public static CreditDTO from(Credits c) {
        CreditDTO dto = new CreditDTO();
        dto.idCredit         = c.getIdCredit();
        dto.numCredit        = c.getNumCredit();
        dto.statut           = c.getStatut();
        dto.montantDemande   = c.getMontantDemande();
        dto.montantAccorde   = c.getMontantAccorde();
        dto.montantDebloquer = c.getMontantDebloquer();
        dto.soldeCapital     = c.getSoldeCapital();
        dto.soldeInteret     = c.getSoldeInteret();
        dto.soldePenalite    = c.getSoldePenalite();
        dto.tauxInteret      = c.getTauxInteret();
        dto.tauxPenalite     = c.getTauxPenalite();
        dto.tauxCommission   = c.getTauxCommission();
        dto.tauxAssurance    = c.getTauxAssurance();
        dto.duree            = c.getDuree();
        dto.nombreEcheance   = c.getNombreEcheance();
        dto.delaiGrace       = c.getDelaiGrace();
        dto.periodicite      = c.getPeriodicite();
        dto.dateDemande      = c.getDateDemande();
        dto.dateAccord       = c.getDateAccord();
        dto.dateDeblocage    = c.getDateDeblocage();
        dto.dateEcheance     = c.getDateEcheance();
        dto.dateCloture      = c.getDateCloture();
        dto.objetCredit      = c.getObjetCredit();
        dto.numeroCycle      = c.getNumeroCycle();

        if (c.getMembre() != null) {
            dto.membreNum    = c.getMembre().getNumMembre();
            dto.membreNom    = c.getMembre().getNom();
            dto.membrePrenom = c.getMembre().getPrenom();
        }
        if (c.getProduitCredit() != null) {
            dto.produitCode = c.getProduitCredit().getNumProduit();
            dto.produitNom  = c.getProduitCredit().getNomProduit();
        }
        if (c.getAgence() != null) {
            dto.agenceCode = c.getAgence().getCodeAgence();
            dto.agenceNom  = c.getAgence().getNomAgence();
        }

        return dto;
    }

    // ── Getters / Setters ─────────────────────────────────────────

    public Long getIdCredit()                       { return idCredit; }
    public void setIdCredit(Long v)                 { this.idCredit = v; }

    public String getNumCredit()                    { return numCredit; }
    public void   setNumCredit(String v)            { this.numCredit = v; }

    public CreditStatut getStatut()                 { return statut; }
    public void         setStatut(CreditStatut v)   { this.statut = v; }

    public BigDecimal getMontantDemande()            { return montantDemande; }
    public void       setMontantDemande(BigDecimal v){ this.montantDemande = v; }

    public BigDecimal getMontantAccorde()            { return montantAccorde; }
    public void       setMontantAccorde(BigDecimal v){ this.montantAccorde = v; }

    public BigDecimal getMontantDebloquer()               { return montantDebloquer; }
    public void       setMontantDebloquer(BigDecimal v)   { this.montantDebloquer = v; }

    public BigDecimal getSoldeCapital()              { return soldeCapital; }
    public void       setSoldeCapital(BigDecimal v)  { this.soldeCapital = v; }

    public BigDecimal getSoldeInteret()              { return soldeInteret; }
    public void       setSoldeInteret(BigDecimal v)  { this.soldeInteret = v; }

    public BigDecimal getSoldePenalite()             { return soldePenalite; }
    public void       setSoldePenalite(BigDecimal v) { this.soldePenalite = v; }

    public BigDecimal getTauxInteret()               { return tauxInteret; }
    public void       setTauxInteret(BigDecimal v)   { this.tauxInteret = v; }

    public BigDecimal getTauxPenalite()              { return tauxPenalite; }
    public void       setTauxPenalite(BigDecimal v)  { this.tauxPenalite = v; }

    public BigDecimal getTauxCommission()            { return tauxCommission; }
    public void       setTauxCommission(BigDecimal v){ this.tauxCommission = v; }

    public BigDecimal getTauxAssurance()             { return tauxAssurance; }
    public void       setTauxAssurance(BigDecimal v) { this.tauxAssurance = v; }

    public Integer getDuree()                        { return duree; }
    public void    setDuree(Integer v)               { this.duree = v; }

    public Integer getNombreEcheance()               { return nombreEcheance; }
    public void    setNombreEcheance(Integer v)      { this.nombreEcheance = v; }

    public Integer getDelaiGrace()                   { return delaiGrace; }
    public void    setDelaiGrace(Integer v)          { this.delaiGrace = v; }

    public String getPeriodicite()                   { return periodicite; }
    public void   setPeriodicite(String v)           { this.periodicite = v; }

    public LocalDate getDateDemande()                { return dateDemande; }
    public void      setDateDemande(LocalDate v)     { this.dateDemande = v; }

    public LocalDate getDateAccord()                 { return dateAccord; }
    public void      setDateAccord(LocalDate v)      { this.dateAccord = v; }

    public LocalDate getDateDeblocage()              { return dateDeblocage; }
    public void      setDateDeblocage(LocalDate v)   { this.dateDeblocage = v; }

    public LocalDate getDateEcheance()               { return dateEcheance; }
    public void      setDateEcheance(LocalDate v)    { this.dateEcheance = v; }

    public LocalDate getDateCloture()                { return dateCloture; }
    public void      setDateCloture(LocalDate v)     { this.dateCloture = v; }

    public String getObjetCredit()                   { return objetCredit; }
    public void   setObjetCredit(String v)           { this.objetCredit = v; }

    public Integer getNumeroCycle()                  { return numeroCycle; }
    public void    setNumeroCycle(Integer v)         { this.numeroCycle = v; }

    public String getMembreNum()                     { return membreNum; }
    public void   setMembreNum(String v)             { this.membreNum = v; }

    public String getMembreNom()                     { return membreNom; }
    public void   setMembreNom(String v)             { this.membreNom = v; }

    public String getMembrePrenom()                  { return membrePrenom; }
    public void   setMembrePrenom(String v)          { this.membrePrenom = v; }

    public String getProduitCode()                   { return produitCode; }
    public void   setProduitCode(String v)           { this.produitCode = v; }

    public String getProduitNom()                    { return produitNom; }
    public void   setProduitNom(String v)            { this.produitNom = v; }

    public String getAgenceCode()                    { return agenceCode; }
    public void   setAgenceCode(String v)            { this.agenceCode = v; }

    public String getAgenceNom()                     { return agenceNom; }
    public void   setAgenceNom(String v)             { this.agenceNom = v; }
}
