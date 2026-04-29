package com.microfina.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * institution – top-level MFI institution record.
 * Holds all global configuration flags, accounting skeleton accounts, and
 * operational parameters that cascade to all branches.
 */
@Entity
@Table(name = "institution")
@DynamicUpdate
public class Institution implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @NotBlank
    @Size(max = 255)
    @Column(name = "CODE_INSTITUTION", length = 255, nullable = false)
    private String codeInstitution;

    @Column(name = "ACTIF", nullable = false)
    private Boolean actif = true;

    @Size(max = 255)
    @Column(name = "DENOMINATION", length = 255)
    private String denomination;

    @Size(max = 255)
    @Column(name = "DESCRIPTION", length = 255)
    private String description;

    @Size(max = 255)
    @Column(name = "DIRECTEUR", length = 255)
    private String directeur;

    @Size(max = 255)
    @Column(name = "MODEUSAGE", length = 255)
    private String modeUsage;

    @Size(max = 255)
    @Column(name = "NUMEROAGREMAT", length = 255)
    private String numeroAgremat;

    @Size(max = 255)
    @Column(name = "raison_sociale", length = 255)
    private String raisonSociale;

    @Version
    @Column(name = "VERSION")
    private Integer version = 0;

    @Size(max = 15)
    @Column(name = "COMPTE_HORSBILAN1", length = 15)
    private String compteHorsBilan1;

    @Size(max = 15)
    @Column(name = "COMPTE_HORSBILAN2", length = 15)
    private String compteHorsBilan2;

    /** FK to zoneGeographique. */
    @Column(name = "zoneGeographique")
    private Integer zoneGeographique;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ADRESSE_IDADRESSE", referencedColumnName = "IDADRESSE",
                foreignKey = @ForeignKey(name = "FK_institution_ADRESSE"))
    private Adresse adresse;

    @Size(max = 255)
    @Column(name = "LOGO_INST", length = 255)
    private String logoInst;

    @Column(name = "niveau_affichage_struct")
    private Integer niveauAffichageStruct;

    @Size(max = 255)
    @Column(name = "serveurSms", length = 255)
    private String serveurSms;

    // ── Password policy flags ────────────────────────────────────
    @Column(name = "CARACTEREALPHA")
    private Boolean caractereAlpha = false;

    @Column(name = "CARACTERENUMERIQUE")
    private Boolean caractereNumerique = true;

    @Column(name = "CARACTERESPECIAUX")
    private Boolean caractereSpeciaux = false;

    @Column(name = "TAILLEMOTDEPASSE")
    private Integer tailleMoDePassE;

    @Column(name = "VALIDITEMOTDEPASS")
    private Integer validiteMotDePass;

    // ── Operational flags ────────────────────────────────────────
    @Column(name = "COMPTEBANQUE_ASSOCIABLE")
    private Boolean compteBanqueAssociable = false;

    @Column(name = "PARTSOCIALE_OBLIGATOIRE")
    private Boolean partSocialeObligatoire = true;

    @Column(name = "MAXIJOUROUVERT")
    private Integer maxiJourOuvert;

    @Column(name = "NBRMAXCREDITSIMULTANE")
    private Integer nbrMaxCreditSimultane;

    @Column(name = "echeanceMaxCalculPenalite")
    private Integer echeanceMaxCalculPenalite;

    @Column(name = "EXCLUR_JR_FERIER_TAB_AMOR")
    private Boolean exclurJrFerierTabAmor = false;

    @Column(name = "RESILIER_CONTRAT_MT_INSUFISANT")
    private Boolean resilierContratMtInsufisant = false;

    @Column(name = "duree_desarchivage_credit")
    private Integer dureeDesarchivageCredit;

    @Size(max = 40)
    @Column(name = "PERIODE_INTERET", length = 40)
    private String periodeInteret;

    @Column(name = "REMB_AUTO_PAR_PRELEVEMENT")
    private Boolean rembAutoParPrelevement = false;

    @Column(name = "REECHELONNE_CAPITAL_REST_SEUL")
    private Boolean reechelonneCapitalRestSeul = false;

    // ── Cash-desk thresholds ─────────────────────────────────────
    @Column(name = "MAX_MONT_DECAISS_CAISSE", precision = 19, scale = 4)
    private BigDecimal maxMontDecaissCaisse;

    @Column(name = "MIN_MONT_DECAISS_CAISSE", precision = 19, scale = 4)
    private BigDecimal minMontDecaissCaisse;

    @Column(name = "MAX_MONT_DECAISS_BANQUE_PRELEV", precision = 19, scale = 4)
    private BigDecimal maxMontDecaissBanquePrelev;

    @Column(name = "MIN_MONT_DECAISS_BANQUE_PRELEV", precision = 19, scale = 4)
    private BigDecimal minMontDecaissBanquePrelev;

    // ── Currency / country ───────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "devise_principale", referencedColumnName = "CODE_DEVISE",
                foreignKey = @ForeignKey(name = "FK_institution_devise_principale"))
    private DeviseLocal devisePrincipale;

    @Column(name = "pays")
    private Integer pays;

    @Size(max = 255)
    @Column(name = "parent", length = 255)
    private String parent;

    @Size(max = 15)
    @Column(name = "categorie_inst", length = 15)
    private String categorieInst;

    @Size(max = 20)
    @Column(name = "AGENCE_DIRECTION", length = 20)
    private String agenceDirection;

    @Size(max = 200)
    @Column(name = "LOGO_PART", length = 200)
    private String logoPart;

    // ── Generated (was Lombok) ──────────────────────────────────────────────

    public Institution() {
    }

    public Institution(String codeInstitution, Boolean actif, String denomination, String description, String directeur, String modeUsage, String numeroAgremat, String raisonSociale, Integer version, String compteHorsBilan1, String compteHorsBilan2, Integer zoneGeographique, Adresse adresse, String logoInst, Integer niveauAffichageStruct, String serveurSms, Boolean caractereAlpha, Boolean caractereNumerique, Boolean caractereSpeciaux, Integer tailleMoDePassE, Integer validiteMotDePass, Boolean compteBanqueAssociable, Boolean partSocialeObligatoire, Integer maxiJourOuvert, Integer nbrMaxCreditSimultane, Integer echeanceMaxCalculPenalite, Boolean exclurJrFerierTabAmor, Boolean resilierContratMtInsufisant, Integer dureeDesarchivageCredit, String periodeInteret, Boolean rembAutoParPrelevement, Boolean reechelonneCapitalRestSeul, BigDecimal maxMontDecaissCaisse, BigDecimal minMontDecaissCaisse, BigDecimal maxMontDecaissBanquePrelev, BigDecimal minMontDecaissBanquePrelev, DeviseLocal devisePrincipale, Integer pays, String parent, String categorieInst, String agenceDirection, String logoPart) {
        this.codeInstitution = codeInstitution;
        this.actif = actif;
        this.denomination = denomination;
        this.description = description;
        this.directeur = directeur;
        this.modeUsage = modeUsage;
        this.numeroAgremat = numeroAgremat;
        this.raisonSociale = raisonSociale;
        this.version = version;
        this.compteHorsBilan1 = compteHorsBilan1;
        this.compteHorsBilan2 = compteHorsBilan2;
        this.zoneGeographique = zoneGeographique;
        this.adresse = adresse;
        this.logoInst = logoInst;
        this.niveauAffichageStruct = niveauAffichageStruct;
        this.serveurSms = serveurSms;
        this.caractereAlpha = caractereAlpha;
        this.caractereNumerique = caractereNumerique;
        this.caractereSpeciaux = caractereSpeciaux;
        this.tailleMoDePassE = tailleMoDePassE;
        this.validiteMotDePass = validiteMotDePass;
        this.compteBanqueAssociable = compteBanqueAssociable;
        this.partSocialeObligatoire = partSocialeObligatoire;
        this.maxiJourOuvert = maxiJourOuvert;
        this.nbrMaxCreditSimultane = nbrMaxCreditSimultane;
        this.echeanceMaxCalculPenalite = echeanceMaxCalculPenalite;
        this.exclurJrFerierTabAmor = exclurJrFerierTabAmor;
        this.resilierContratMtInsufisant = resilierContratMtInsufisant;
        this.dureeDesarchivageCredit = dureeDesarchivageCredit;
        this.periodeInteret = periodeInteret;
        this.rembAutoParPrelevement = rembAutoParPrelevement;
        this.reechelonneCapitalRestSeul = reechelonneCapitalRestSeul;
        this.maxMontDecaissCaisse = maxMontDecaissCaisse;
        this.minMontDecaissCaisse = minMontDecaissCaisse;
        this.maxMontDecaissBanquePrelev = maxMontDecaissBanquePrelev;
        this.minMontDecaissBanquePrelev = minMontDecaissBanquePrelev;
        this.devisePrincipale = devisePrincipale;
        this.pays = pays;
        this.parent = parent;
        this.categorieInst = categorieInst;
        this.agenceDirection = agenceDirection;
        this.logoPart = logoPart;
    }

    public String getCodeInstitution() {
        return codeInstitution;
    }

    public void setCodeInstitution(String codeInstitution) {
        this.codeInstitution = codeInstitution;
    }

    public Boolean getActif() {
        return actif;
    }

    public void setActif(Boolean actif) {
        this.actif = actif;
    }

    public String getDenomination() {
        return denomination;
    }

    public void setDenomination(String denomination) {
        this.denomination = denomination;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDirecteur() {
        return directeur;
    }

    public void setDirecteur(String directeur) {
        this.directeur = directeur;
    }

    public String getModeUsage() {
        return modeUsage;
    }

    public void setModeUsage(String modeUsage) {
        this.modeUsage = modeUsage;
    }

    public String getNumeroAgremat() {
        return numeroAgremat;
    }

    public void setNumeroAgremat(String numeroAgremat) {
        this.numeroAgremat = numeroAgremat;
    }

    public String getRaisonSociale() {
        return raisonSociale;
    }

    public void setRaisonSociale(String raisonSociale) {
        this.raisonSociale = raisonSociale;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getCompteHorsBilan1() {
        return compteHorsBilan1;
    }

    public void setCompteHorsBilan1(String compteHorsBilan1) {
        this.compteHorsBilan1 = compteHorsBilan1;
    }

    public String getCompteHorsBilan2() {
        return compteHorsBilan2;
    }

    public void setCompteHorsBilan2(String compteHorsBilan2) {
        this.compteHorsBilan2 = compteHorsBilan2;
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

    public String getLogoInst() {
        return logoInst;
    }

    public void setLogoInst(String logoInst) {
        this.logoInst = logoInst;
    }

    public Integer getNiveauAffichageStruct() {
        return niveauAffichageStruct;
    }

    public void setNiveauAffichageStruct(Integer niveauAffichageStruct) {
        this.niveauAffichageStruct = niveauAffichageStruct;
    }

    public String getServeurSms() {
        return serveurSms;
    }

    public void setServeurSms(String serveurSms) {
        this.serveurSms = serveurSms;
    }

    public Boolean getCaractereAlpha() {
        return caractereAlpha;
    }

    public void setCaractereAlpha(Boolean caractereAlpha) {
        this.caractereAlpha = caractereAlpha;
    }

    public Boolean getCaractereNumerique() {
        return caractereNumerique;
    }

    public void setCaractereNumerique(Boolean caractereNumerique) {
        this.caractereNumerique = caractereNumerique;
    }

    public Boolean getCaractereSpeciaux() {
        return caractereSpeciaux;
    }

    public void setCaractereSpeciaux(Boolean caractereSpeciaux) {
        this.caractereSpeciaux = caractereSpeciaux;
    }

    public Integer getTailleMoDePassE() {
        return tailleMoDePassE;
    }

    public void setTailleMoDePassE(Integer tailleMoDePassE) {
        this.tailleMoDePassE = tailleMoDePassE;
    }

    public Integer getValiditeMotDePass() {
        return validiteMotDePass;
    }

    public void setValiditeMotDePass(Integer validiteMotDePass) {
        this.validiteMotDePass = validiteMotDePass;
    }

    public Boolean getCompteBanqueAssociable() {
        return compteBanqueAssociable;
    }

    public void setCompteBanqueAssociable(Boolean compteBanqueAssociable) {
        this.compteBanqueAssociable = compteBanqueAssociable;
    }

    public Boolean getPartSocialeObligatoire() {
        return partSocialeObligatoire;
    }

    public void setPartSocialeObligatoire(Boolean partSocialeObligatoire) {
        this.partSocialeObligatoire = partSocialeObligatoire;
    }

    public Integer getMaxiJourOuvert() {
        return maxiJourOuvert;
    }

    public void setMaxiJourOuvert(Integer maxiJourOuvert) {
        this.maxiJourOuvert = maxiJourOuvert;
    }

    public Integer getNbrMaxCreditSimultane() {
        return nbrMaxCreditSimultane;
    }

    public void setNbrMaxCreditSimultane(Integer nbrMaxCreditSimultane) {
        this.nbrMaxCreditSimultane = nbrMaxCreditSimultane;
    }

    public Integer getEcheanceMaxCalculPenalite() {
        return echeanceMaxCalculPenalite;
    }

    public void setEcheanceMaxCalculPenalite(Integer echeanceMaxCalculPenalite) {
        this.echeanceMaxCalculPenalite = echeanceMaxCalculPenalite;
    }

    public Boolean getExclurJrFerierTabAmor() {
        return exclurJrFerierTabAmor;
    }

    public void setExclurJrFerierTabAmor(Boolean exclurJrFerierTabAmor) {
        this.exclurJrFerierTabAmor = exclurJrFerierTabAmor;
    }

    public Boolean getResilierContratMtInsufisant() {
        return resilierContratMtInsufisant;
    }

    public void setResilierContratMtInsufisant(Boolean resilierContratMtInsufisant) {
        this.resilierContratMtInsufisant = resilierContratMtInsufisant;
    }

    public Integer getDureeDesarchivageCredit() {
        return dureeDesarchivageCredit;
    }

    public void setDureeDesarchivageCredit(Integer dureeDesarchivageCredit) {
        this.dureeDesarchivageCredit = dureeDesarchivageCredit;
    }

    public String getPeriodeInteret() {
        return periodeInteret;
    }

    public void setPeriodeInteret(String periodeInteret) {
        this.periodeInteret = periodeInteret;
    }

    public Boolean getRembAutoParPrelevement() {
        return rembAutoParPrelevement;
    }

    public void setRembAutoParPrelevement(Boolean rembAutoParPrelevement) {
        this.rembAutoParPrelevement = rembAutoParPrelevement;
    }

    public Boolean getReechelonneCapitalRestSeul() {
        return reechelonneCapitalRestSeul;
    }

    public void setReechelonneCapitalRestSeul(Boolean reechelonneCapitalRestSeul) {
        this.reechelonneCapitalRestSeul = reechelonneCapitalRestSeul;
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

    public DeviseLocal getDevisePrincipale() {
        return devisePrincipale;
    }

    public void setDevisePrincipale(DeviseLocal devisePrincipale) {
        this.devisePrincipale = devisePrincipale;
    }

    public Integer getPays() {
        return pays;
    }

    public void setPays(Integer pays) {
        this.pays = pays;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public String getCategorieInst() {
        return categorieInst;
    }

    public void setCategorieInst(String categorieInst) {
        this.categorieInst = categorieInst;
    }

    public String getAgenceDirection() {
        return agenceDirection;
    }

    public void setAgenceDirection(String agenceDirection) {
        this.agenceDirection = agenceDirection;
    }

    public String getLogoPart() {
        return logoPart;
    }

    public void setLogoPart(String logoPart) {
        this.logoPart = logoPart;
    }

    @Override
    public String toString() {
        return "Institution("
            + "codeInstitution=" + codeInstitution
            + ")";
    }
}
