package com.pfe.backend.dto;

import com.microfina.entity.ProduitCredit;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * ProduitCreditDTO – flattened view of the ProduitCredit entity
 * for the credit-product list and configuration form.
 */
public class ProduitCreditDTO {

    // ── Request records (Phase 11.1) ──────────────────────────────────────────

    /**
     * Corps de la requête POST /api/v1/produits-credit.
     */
    public record CreateRequest(
            @NotBlank(message = "Le code produit est obligatoire")
            @Size(max = 20, message = "numProduit ≤ 20 caractères")
            String numProduit,

            @NotBlank(message = "Le nom produit est obligatoire")
            @Size(max = 100, message = "nomProduit ≤ 100 caractères")
            String nomProduit,

            String description,

            @NotNull(message = "Le statut actif est obligatoire")
            Integer actif,

            String typeCredit,
            String typeClient,

            @DecimalMin(value = "0", message = "montantMin ≥ 0")
            BigDecimal montantMin,

            @DecimalMin(value = "0", message = "montantMax ≥ 0")
            BigDecimal montantMax,

            @Min(value = 1, message = "dureeMin ≥ 1") Integer dureeMin,
            @Min(value = 1, message = "dureeMax ≥ 1") Integer dureeMax,

            @DecimalMin(value = "0", message = "tauxInteret ≥ 0")    BigDecimal tauxInteret,
            @DecimalMin(value = "0", message = "tauxInteretMin ≥ 0") BigDecimal tauxInteretMin,
            @DecimalMin(value = "0", message = "tauxInteretMax ≥ 0") BigDecimal tauxInteretMax,
            @DecimalMin(value = "0", message = "tauxPenalite ≥ 0")   BigDecimal tauxPenalite,
            @DecimalMin(value = "0", message = "tauxCommission ≥ 0") BigDecimal tauxCommission,
            @DecimalMin(value = "0", message = "tauxAssurance ≥ 0")  BigDecimal tauxAssurance,

            String  periodiciteRemboursement,
            Integer nombreEcheance,
            Integer delaiGrace,
            String  typeGrace,
            Integer garantieRequise,
            Integer autoriserReneg,
            Integer autoriserRemboursementAnticipe,
            Integer decaissementNet,
            String  codeFamilleProduit,
            Integer codeModeCalcul
    ) {}

    /**
     * Corps de la requête PUT /api/v1/produits-credit/{numProduit}.
     * numProduit vient du path variable.
     */
    public record UpdateRequest(
            @NotBlank(message = "Le nom produit est obligatoire")
            @Size(max = 100) String nomProduit,

            String description,
            Integer actif,
            String  typeCredit,
            String  typeClient,

            @DecimalMin(value = "0") BigDecimal montantMin,
            @DecimalMin(value = "0") BigDecimal montantMax,
            @Min(1) Integer dureeMin,
            @Min(1) Integer dureeMax,

            @DecimalMin(value = "0") BigDecimal tauxInteret,
            @DecimalMin(value = "0") BigDecimal tauxInteretMin,
            @DecimalMin(value = "0") BigDecimal tauxInteretMax,
            @DecimalMin(value = "0") BigDecimal tauxPenalite,
            @DecimalMin(value = "0") BigDecimal tauxCommission,
            @DecimalMin(value = "0") BigDecimal tauxAssurance,

            String  periodiciteRemboursement,
            Integer nombreEcheance,
            Integer delaiGrace,
            String  typeGrace,
            Integer garantieRequise,
            Integer autoriserReneg,
            Integer autoriserRemboursementAnticipe,
            Integer decaissementNet,
            String  codeFamilleProduit,
            Integer codeModeCalcul
    ) {}

    // ─────────────────────────────────────────────────────────────────────────

    // ── Identity ──────────────────────────────────────────────────
    private String     numProduit;
    private String     nomProduit;
    private String     description;
    private Integer    actif;
    private String     typeCredit;
    private String     typeClient;

    // ── Amount limits ─────────────────────────────────────────────
    private BigDecimal montantMin;
    private BigDecimal montantMax;

    // ── Duration ──────────────────────────────────────────────────
    private Integer    dureeMin;
    private Integer    dureeMax;

    // ── Rates ─────────────────────────────────────────────────────
    private BigDecimal tauxInteret;
    private BigDecimal tauxInteretMin;
    private BigDecimal tauxInteretMax;
    private BigDecimal tauxPenalite;
    private BigDecimal tauxCommission;
    private BigDecimal tauxAssurance;

    // ── Schedule ──────────────────────────────────────────────────
    private String     periodiciteRemboursement;
    private Integer    nombreEcheance;
    private Integer    delaiGrace;
    private String     typeGrace;

    // ── Flags ─────────────────────────────────────────────────────
    private Integer    garantieRequise;
    private Integer    autoriserReneg;
    private Integer    autoriserRemboursementAnticipe;
    private Integer    decaissementNet;

    // ── FK labels ─────────────────────────────────────────────────
    private String     familleProduitCode;
    private String     familleProduitLibelle;
    private String     modeCalculCode;
    private String     modeCalculLibelle;

    public ProduitCreditDTO() {}

    public static ProduitCreditDTO from(ProduitCredit p) {
        ProduitCreditDTO dto = new ProduitCreditDTO();
        dto.numProduit                    = p.getNumProduit();
        dto.nomProduit                    = p.getNomProduit();
        dto.description                   = p.getDescription();
        dto.actif                         = p.getActif();
        dto.typeCredit                    = p.getTypeCredit();
        dto.typeClient                    = p.getTypeClient();
        dto.montantMin                    = p.getMontantMin();
        dto.montantMax                    = p.getMontantMax();
        dto.dureeMin                      = p.getDureeMin();
        dto.dureeMax                      = p.getDureeMax();
        dto.tauxInteret                   = p.getTauxInteret();
        dto.tauxInteretMin                = p.getTauxInteretMin();
        dto.tauxInteretMax                = p.getTauxInteretMax();
        dto.tauxPenalite                  = p.getTauxPenalite();
        dto.tauxCommission                = p.getTauxCommission();
        dto.tauxAssurance                 = p.getTauxAssurance();
        dto.periodiciteRemboursement      = p.getPeriodiciteRemboursement();
        dto.nombreEcheance                = p.getNombreEcheance();
        dto.delaiGrace                    = p.getDelaiGrace();
        dto.typeGrace                     = p.getTypeGrace();
        dto.garantieRequise               = p.getGarantieRequise();
        dto.autoriserReneg                = p.getAutoriserReneg();
        dto.autoriserRemboursementAnticipe = p.getAutoriserRemboursementAnticipe();
        dto.decaissementNet               = p.getDecaissementNet();

        if (p.getFamilleProduitCredit() != null) {
            dto.familleProduitCode    = p.getFamilleProduitCredit().getCodeFamilleProduitCredit();
            dto.familleProduitLibelle = p.getFamilleProduitCredit().getNomFamilleProduitCredit();
        }
        if (p.getModeDeCalculInteret() != null) {
            dto.modeCalculCode    = String.valueOf(p.getModeDeCalculInteret().getCodeMode());
            dto.modeCalculLibelle = p.getModeDeCalculInteret().getDescription();
        }

        return dto;
    }

    // ── Getters / Setters ─────────────────────────────────────────

    public String getNumProduit()                        { return numProduit; }
    public void   setNumProduit(String v)                { this.numProduit = v; }

    public String getNomProduit()                        { return nomProduit; }
    public void   setNomProduit(String v)                { this.nomProduit = v; }

    public String getDescription()                       { return description; }
    public void   setDescription(String v)               { this.description = v; }

    public Integer getActif()                            { return actif; }
    public void    setActif(Integer v)                   { this.actif = v; }

    public String getTypeCredit()                        { return typeCredit; }
    public void   setTypeCredit(String v)                { this.typeCredit = v; }

    public String getTypeClient()                        { return typeClient; }
    public void   setTypeClient(String v)                { this.typeClient = v; }

    public BigDecimal getMontantMin()                    { return montantMin; }
    public void       setMontantMin(BigDecimal v)        { this.montantMin = v; }

    public BigDecimal getMontantMax()                    { return montantMax; }
    public void       setMontantMax(BigDecimal v)        { this.montantMax = v; }

    public Integer getDureeMin()                         { return dureeMin; }
    public void    setDureeMin(Integer v)                { this.dureeMin = v; }

    public Integer getDureeMax()                         { return dureeMax; }
    public void    setDureeMax(Integer v)                { this.dureeMax = v; }

    public BigDecimal getTauxInteret()                   { return tauxInteret; }
    public void       setTauxInteret(BigDecimal v)       { this.tauxInteret = v; }

    public BigDecimal getTauxInteretMin()                { return tauxInteretMin; }
    public void       setTauxInteretMin(BigDecimal v)    { this.tauxInteretMin = v; }

    public BigDecimal getTauxInteretMax()                { return tauxInteretMax; }
    public void       setTauxInteretMax(BigDecimal v)    { this.tauxInteretMax = v; }

    public BigDecimal getTauxPenalite()                  { return tauxPenalite; }
    public void       setTauxPenalite(BigDecimal v)      { this.tauxPenalite = v; }

    public BigDecimal getTauxCommission()                { return tauxCommission; }
    public void       setTauxCommission(BigDecimal v)    { this.tauxCommission = v; }

    public BigDecimal getTauxAssurance()                 { return tauxAssurance; }
    public void       setTauxAssurance(BigDecimal v)     { this.tauxAssurance = v; }

    public String getPeriodiciteRemboursement()          { return periodiciteRemboursement; }
    public void   setPeriodiciteRemboursement(String v)  { this.periodiciteRemboursement = v; }

    public Integer getNombreEcheance()                   { return nombreEcheance; }
    public void    setNombreEcheance(Integer v)          { this.nombreEcheance = v; }

    public Integer getDelaiGrace()                       { return delaiGrace; }
    public void    setDelaiGrace(Integer v)              { this.delaiGrace = v; }

    public String getTypeGrace()                         { return typeGrace; }
    public void   setTypeGrace(String v)                 { this.typeGrace = v; }

    public Integer getGarantieRequise()                  { return garantieRequise; }
    public void    setGarantieRequise(Integer v)         { this.garantieRequise = v; }

    public Integer getAutoriserReneg()                   { return autoriserReneg; }
    public void    setAutoriserReneg(Integer v)          { this.autoriserReneg = v; }

    public Integer getAutoriserRemboursementAnticipe()   { return autoriserRemboursementAnticipe; }
    public void    setAutoriserRemboursementAnticipe(Integer v) { this.autoriserRemboursementAnticipe = v; }

    public Integer getDecaissementNet()                  { return decaissementNet; }
    public void    setDecaissementNet(Integer v)         { this.decaissementNet = v; }

    public String getFamilleProduitCode()                { return familleProduitCode; }
    public void   setFamilleProduitCode(String v)        { this.familleProduitCode = v; }

    public String getFamilleProduitLibelle()             { return familleProduitLibelle; }
    public void   setFamilleProduitLibelle(String v)     { this.familleProduitLibelle = v; }

    public String getModeCalculCode()                    { return modeCalculCode; }
    public void   setModeCalculCode(String v)            { this.modeCalculCode = v; }

    public String getModeCalculLibelle()                 { return modeCalculLibelle; }
    public void   setModeCalculLibelle(String v)         { this.modeCalculLibelle = v; }
}
