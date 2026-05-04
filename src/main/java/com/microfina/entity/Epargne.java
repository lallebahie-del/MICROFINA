package com.microfina.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Epargne – savings movement (base class; SINGLE_TABLE inheritance).
 *
 * Subclassed by {@link EpargneGroupe} for group-member savings movements.
 * DTYPE discriminator: "Epargne" | "EpargneGroupe".
 *
 * DDL source of truth: P2-007-CREATE-TABLE-EPARGNE.xml.
 * Spec p.31-32.
 *
 * Phase-2+ FK columns (origine, CODE_MANDATAIRE) stored as raw values.
 */
@Entity
@Table(name = "EPARGNE")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
    name              = "DTYPE",
    discriminatorType = DiscriminatorType.STRING,
    columnDefinition  = "NVARCHAR(31)"
)
@DiscriminatorValue("Epargne")
@DynamicUpdate
public class Epargne implements Serializable {

    private static final long serialVersionUID = 1L;

    // ── PK ────────────────────────────────────────────────────────

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IDEPARGNE", nullable = false)
    private Long idEpargne;

    // ── Operation metadata ────────────────────────────────────────

    @Column(name = "DATEVALEUR")
    private LocalDate dateValeur;

    @Column(name = "DATEOPERATION")
    private LocalDate dateOperation;

    @Size(max = 255)
    @Column(name = "CODETYPEOPERATION", length = 255)
    private String codeTypeOperation;

    @Size(max = 255)
    @Column(name = "LIBELLEOPERATION", length = 255)
    private String libelleOperation;

    @Size(max = 255)
    @Column(name = "NUMPIECE", length = 255)
    private String numPiece;

    @Size(max = 255)
    @Column(name = "TIREUR", length = 255)
    private String tireur;

    @Size(max = 255)
    @Column(name = "MANDATAIRE", length = 255)
    private String mandataire;

    @Size(max = 255)
    @Column(name = "AUTRES", length = 255)
    private String autres;

    @Size(max = 255)
    @Column(name = "ENCAISSE", length = 255)
    private String encaisse;

    @Column(name = "DETAILOK")
    private Integer detailOk;

    @Size(max = 255)
    @Column(name = "CODE_GUICHET", length = 255)
    private String codeGuichet;

    @Size(max = 15)
    @Column(name = "CODE_CHEQUE_CLIENT", length = 15)
    private String codeChequeClient;

    @Size(max = 25)
    @Column(name = "compteeps_old", length = 25)
    private String compteEpsOld;

    @Size(max = 30)
    @Column(name = "compte", length = 30)
    private String compte;

    // ── Financial amounts ─────────────────────────────────────────

    @Column(name = "MONTANTCREDIT", precision = 19, scale = 4)
    private BigDecimal montantCredit;

    @Column(name = "MONTANTDEBIT", precision = 19, scale = 4)
    private BigDecimal montantDebit;

    @Column(name = "INTERETCREDIT", precision = 19, scale = 4)
    private BigDecimal interetCredit;

    @Column(name = "INTERETDEBIT", precision = 19, scale = 4)
    private BigDecimal interetDebit;

    @Column(name = "SOLDEANCIEN", precision = 19, scale = 4)
    private BigDecimal soldeAncien;

    @Column(name = "SOLDENOUVEAU", precision = 19, scale = 4)
    private BigDecimal soldeNouveau;

    @Column(name = "NOMBREPARTSOCIAL")
    private Long nombrePartSocial;

    @Column(name = "CALCULINTERET")
    private Integer calculInteret;

    /** Account number reference (AN/255 per spec). */
    @Size(max = 255)
    @Column(name = "NUMCOMPTE", length = 255)
    private String numCompte;

    // ── User ──────────────────────────────────────────────────────

    @Size(max = 48)
    @Column(name = "utilisateur", length = 48)
    private String utilisateur;

    // ── Optimistic lock ───────────────────────────────────────────

    @Version
    @Column(name = "VERSION")
    private Integer version = 0;

    // ── FKs (wired, Phase 2) ──────────────────────────────────────

    /** Savings account this movement belongs to. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name                 = "compteEps",
        referencedColumnName = "NUMCOMPTE",
        foreignKey           = @ForeignKey(name = "FK_EPARGNE_COMPTEEPS")
    )
    private CompteEps compteEps;

    /** Branch where the operation was performed. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name                 = "agence",
        referencedColumnName = "CODE_AGENCE",
        foreignKey           = @ForeignKey(name = "FK_EPARGNE_AGENCE")
    )
    private Agence agence;

    // ── Phase-2+ FK columns ───────────────────────────────────────

    /** Origine (N type – numeric ref). Constraint deferred. */
    @Column(name = "origine")
    private Long origine;

    /** Code mandataire (AN/25). Constraint deferred. */
    @Size(max = 25)
    @Column(name = "CODE_MANDATAIRE", length = 25)
    private String codeMandataire;

    // ── Generated (was Lombok) ──────────────────────────────────────────────

    public Epargne() {
    }

    public Epargne(Long idEpargne, LocalDate dateValeur, LocalDate dateOperation, String codeTypeOperation, String libelleOperation, String numPiece, String tireur, String mandataire, String autres, String encaisse, Integer detailOk, String codeGuichet, String codeChequeClient, String compteEpsOld, String compte, BigDecimal montantCredit, BigDecimal montantDebit, BigDecimal interetCredit, BigDecimal interetDebit, BigDecimal soldeAncien, BigDecimal soldeNouveau, Long nombrePartSocial, Integer calculInteret, String numCompte, String utilisateur, Integer version, CompteEps compteEps, Agence agence, Long origine, String codeMandataire) {
        this.idEpargne = idEpargne;
        this.dateValeur = dateValeur;
        this.dateOperation = dateOperation;
        this.codeTypeOperation = codeTypeOperation;
        this.libelleOperation = libelleOperation;
        this.numPiece = numPiece;
        this.tireur = tireur;
        this.mandataire = mandataire;
        this.autres = autres;
        this.encaisse = encaisse;
        this.detailOk = detailOk;
        this.codeGuichet = codeGuichet;
        this.codeChequeClient = codeChequeClient;
        this.compteEpsOld = compteEpsOld;
        this.compte = compte;
        this.montantCredit = montantCredit;
        this.montantDebit = montantDebit;
        this.interetCredit = interetCredit;
        this.interetDebit = interetDebit;
        this.soldeAncien = soldeAncien;
        this.soldeNouveau = soldeNouveau;
        this.nombrePartSocial = nombrePartSocial;
        this.calculInteret = calculInteret;
        this.numCompte = numCompte;
        this.utilisateur = utilisateur;
        this.version = version;
        this.compteEps = compteEps;
        this.agence = agence;
        this.origine = origine;
        this.codeMandataire = codeMandataire;
    }

    public Long getIdEpargne() {
        return idEpargne;
    }

    public void setIdEpargne(Long idEpargne) {
        this.idEpargne = idEpargne;
    }

    public LocalDate getDateValeur() {
        return dateValeur;
    }

    public void setDateValeur(LocalDate dateValeur) {
        this.dateValeur = dateValeur;
    }

    public LocalDate getDateOperation() {
        return dateOperation;
    }

    public void setDateOperation(LocalDate dateOperation) {
        this.dateOperation = dateOperation;
    }

    public String getCodeTypeOperation() {
        return codeTypeOperation;
    }

    public void setCodeTypeOperation(String codeTypeOperation) {
        this.codeTypeOperation = codeTypeOperation;
    }

    public String getLibelleOperation() {
        return libelleOperation;
    }

    public void setLibelleOperation(String libelleOperation) {
        this.libelleOperation = libelleOperation;
    }

    public String getNumPiece() {
        return numPiece;
    }

    public void setNumPiece(String numPiece) {
        this.numPiece = numPiece;
    }

    public String getTireur() {
        return tireur;
    }

    public void setTireur(String tireur) {
        this.tireur = tireur;
    }

    public String getMandataire() {
        return mandataire;
    }

    public void setMandataire(String mandataire) {
        this.mandataire = mandataire;
    }

    public String getAutres() {
        return autres;
    }

    public void setAutres(String autres) {
        this.autres = autres;
    }

    public String getEncaisse() {
        return encaisse;
    }

    public void setEncaisse(String encaisse) {
        this.encaisse = encaisse;
    }

    public Integer getDetailOk() {
        return detailOk;
    }

    public void setDetailOk(Integer detailOk) {
        this.detailOk = detailOk;
    }

    public String getCodeGuichet() {
        return codeGuichet;
    }

    public void setCodeGuichet(String codeGuichet) {
        this.codeGuichet = codeGuichet;
    }

    public String getCodeChequeClient() {
        return codeChequeClient;
    }

    public void setCodeChequeClient(String codeChequeClient) {
        this.codeChequeClient = codeChequeClient;
    }

    public String getCompteEpsOld() {
        return compteEpsOld;
    }

    public void setCompteEpsOld(String compteEpsOld) {
        this.compteEpsOld = compteEpsOld;
    }

    public String getCompte() {
        return compte;
    }

    public void setCompte(String compte) {
        this.compte = compte;
    }

    public BigDecimal getMontantCredit() {
        return montantCredit;
    }

    public void setMontantCredit(BigDecimal montantCredit) {
        this.montantCredit = montantCredit;
    }

    public BigDecimal getMontantDebit() {
        return montantDebit;
    }

    public void setMontantDebit(BigDecimal montantDebit) {
        this.montantDebit = montantDebit;
    }

    public BigDecimal getInteretCredit() {
        return interetCredit;
    }

    public void setInteretCredit(BigDecimal interetCredit) {
        this.interetCredit = interetCredit;
    }

    public BigDecimal getInteretDebit() {
        return interetDebit;
    }

    public void setInteretDebit(BigDecimal interetDebit) {
        this.interetDebit = interetDebit;
    }

    public BigDecimal getSoldeAncien() {
        return soldeAncien;
    }

    public void setSoldeAncien(BigDecimal soldeAncien) {
        this.soldeAncien = soldeAncien;
    }

    public BigDecimal getSoldeNouveau() {
        return soldeNouveau;
    }

    public void setSoldeNouveau(BigDecimal soldeNouveau) {
        this.soldeNouveau = soldeNouveau;
    }

    public Long getNombrePartSocial() {
        return nombrePartSocial;
    }

    public void setNombrePartSocial(Long nombrePartSocial) {
        this.nombrePartSocial = nombrePartSocial;
    }

    public Integer getCalculInteret() {
        return calculInteret;
    }

    public void setCalculInteret(Integer calculInteret) {
        this.calculInteret = calculInteret;
    }

    public String getNumCompte() {
        return numCompte;
    }

    public void setNumCompte(String numCompte) {
        this.numCompte = numCompte;
    }

    public String getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(String utilisateur) {
        this.utilisateur = utilisateur;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public CompteEps getCompteEps() {
        return compteEps;
    }

    public void setCompteEps(CompteEps compteEps) {
        this.compteEps = compteEps;
    }

    public Agence getAgence() {
        return agence;
    }

    public void setAgence(Agence agence) {
        this.agence = agence;
    }

    public Long getOrigine() {
        return origine;
    }

    public void setOrigine(Long origine) {
        this.origine = origine;
    }

    public String getCodeMandataire() {
        return codeMandataire;
    }

    public void setCodeMandataire(String codeMandataire) {
        this.codeMandataire = codeMandataire;
    }

    @Override
    public String toString() {
        return "Epargne("
            + "idEpargne=" + idEpargne
            + ")";
    }
}
