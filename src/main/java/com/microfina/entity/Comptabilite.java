package com.microfina.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * comptabilite – core journal-entry / accounting ledger.
 * 100 % faithful to DB-FINA-202112-001 p.18-19.
 *
 * Key spec corrections applied:
 *  - LETTRE is NVARCHAR/255 (NOT a Boolean).
 *  - SOLDE removed (belongs to c_plancomptable, not this table).
 *  - All D-type columns mapped to {@link LocalDate}.
 */
@Entity
@Table(
    name = "comptabilite",
    indexes = {
        @Index(name = "IDX_comptabilite_DATEOPERATION", columnList = "DATEOPERATION"),
        @Index(name = "IDX_comptabilite_CompteAuxi",    columnList = "CompteAuxi")
    }
)
@DynamicUpdate
public class Comptabilite implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IDCOMPTABILITE", nullable = false)
    private Long idComptabilite;

    // ── Financial amounts ────────────────────────────────────────

    @Column(name = "CREDIT", precision = 19, scale = 4)
    private BigDecimal credit;

    @Column(name = "DEBIT", precision = 19, scale = 4)
    private BigDecimal debit;

    @Column(name = "ENTREE", precision = 19, scale = 4)
    private BigDecimal entree;

    @Column(name = "SORTIE", precision = 19, scale = 4)
    private BigDecimal sortie;

    @Column(name = "PROVENANCE", precision = 19, scale = 4)
    private BigDecimal provenance;

    // ── Dates (D-type → LocalDate) ───────────────────────────────

    @Column(name = "DATEOPERATION")
    private LocalDate dateOperation;

    @Column(name = "DATESERVEUR")
    private LocalDate dateServeur;

    @Column(name = "DATEECHEANCE")
    private LocalDate dateEcheance;

    @Column(name = "DATELETTRAGE")
    private LocalDate dateLettrage;

    @Column(name = "DATEPIECE")
    private LocalDate datePiece;

    @Column(name = "dateamort")
    private LocalDate dateAmort;

    @Column(name = "DATE_RAPPROCHEMENT")
    private LocalDate dateRapprochement;

    // ── Status / flag strings ────────────────────────────────────

    /** Operation state. AN/255. */
    @Size(max = 255)
    @Column(name = "ETAT", length = 255)
    private String etat;

    /**
     * Lettrage / reconciliation code.
     * Spec type: AN/255 — NOT a Boolean.
     */
    @Size(max = 255)
    @Column(name = "LETTRE", length = 255)
    private String lettre;

    /**
     * "1" if the piece has been posted to accounting, "0" otherwise.
     * AN/255 per spec.
     */
    @Size(max = 255)
    @Column(name = "MARQUE", length = 255)
    private String marque;

    // ── Identifiers and references ───────────────────────────────

    /** Unique operation ID string. AN/255. */
    @Size(max = 255)
    @Column(name = "ID", length = 255)
    private String id;

    /** Operation label. AN/255. */
    @Size(max = 255)
    @Column(name = "LIBELLE", length = 255)
    private String libelle;

    /** Number of the cancelled piece. AN/20. */
    @Size(max = 20)
    @Column(name = "NPIECE", length = 20)
    private String nPiece;

    /** Credit number associated with the operation. AN/255. */
    @Size(max = 255)
    @Column(name = "NUMCREDIT", length = 255)
    private String numCredit;

    /** Piece / document number. AN/255. */
    @Size(max = 255)
    @Column(name = "NUMPIECE", length = 255)
    private String numPiece;

    /** External reference. AN/255. */
    @Size(max = 255)
    @Column(name = "REFERENCE", length = 255)
    private String reference;

    /** Validation test flag. AN/255. */
    @Size(max = 255)
    @Column(name = "TESTSUP", length = 255)
    private String testSup;

    /** Employee / user code linked to the entry. AN/48. */
    @Size(max = 48)
    @Column(name = "CODE_EMP", length = 48)
    private String codeEmp;

    /** Destination account number. AN/255. */
    @Size(max = 255)
    @Column(name = "COMPTEDESTINATAIRE", length = 255)
    private String compteDestinataire;

    /** Invoice reference. AN/255. */
    @Size(max = 255)
    @Column(name = "facture", length = 255)
    private String facture;

    /** Cheque number. AN/255. */
    @Size(max = 255)
    @Column(name = "numerocheque", length = 255)
    private String numeroCheque;

    // ── Account references ───────────────────────────────────────

    /** Member auxiliary account number. AN/25. */
    @Size(max = 25)
    @Column(name = "CompteAuxi", length = 25)
    private String compteAuxi;

    /** Tiers account code. AN/5. */
    @Size(max = 5)
    @Column(name = "compteTiers", length = 5)
    private String compteTiers;

    /** Secondary account (contra). AN/15. */
    @Size(max = 15)
    @Column(name = "Compte1", length = 15)
    private String compte1;

    /** Debit/credit direction code. AN/255. */
    @Size(max = 255)
    @Column(name = "CODE_SENS", length = 255)
    private String codeSens;

    // ── Settlement / reimbursement references ────────────────────

    /** Supplier settlement reference. AN/40. */
    @Size(max = 40)
    @Column(name = "NUM_REGLEMENT_FOUR", length = 40)
    private String numReglementFour;

    /** Client settlement reference. AN/40. */
    @Size(max = 40)
    @Column(name = "NUM_REGLEMENT_CLIENT", length = 40)
    private String numReglementClient;

    /** General settlement reference. AN/25. */
    @Size(max = 25)
    @Column(name = "NUM_REGLEMENT", length = 25)
    private String numReglement;

    // ── Budget / follow-up references ────────────────────────────

    /** Funder / bailleur code. AN/25, FK. */
    @Size(max = 25)
    @Column(name = "CODE_BAILLEUR", length = 25)
    private String codeBailleur;

    /** Budget code. AN/25, FK. */
    @Size(max = 25)
    @Column(name = "CODE_BUDGET", length = 25)
    private String codeBudget;

    /** Follow-up type. AN/15. */
    @Size(max = 15)
    @Column(name = "TYPE_SUIVI", length = 15)
    private String typeSuivi;

    /** Follow-up reference. AN/15. */
    @Size(max = 15)
    @Column(name = "REFERENCE_SUIVI", length = 15)
    private String referenceSuivi;

    /** Budget type code. AN/15, FK. */
    @Size(max = 15)
    @Column(name = "CODE_TYPE_BUDGET", length = 15)
    private String codeTypeBudget;

    /** Budget line plan id. Numeric. */
    @Column(name = "ID_LIGNE_PLAN_BUDGETAIRE")
    private Long idLignePlanBudgetaire;

    /** Code agence (secondary branch reference). AN/25, FK. */
    @Size(max = 25)
    @Column(name = "CODE_AGENCE", length = 25)
    private String codeAgence;

    /** External agent. AN/50, FK. */
    @Size(max = 50)
    @Column(name = "agent_externe", length = 50)
    private String agentExterne;

    /** Budget line code. AN/50, FK. */
    @Size(max = 50)
    @Column(name = "CODE_LIGNE_BUDGETAIRE", length = 50)
    private String codeLigneBudgetaire;

    /** Validation code. AN/20. */
    @Size(max = 20)
    @Column(name = "codeValidation", length = 20)
    private String codeValidation;

    /** Currency code for the operation. AN/5. */
    @Size(max = 5)
    @Column(name = "code_devise", length = 5)
    private String codeDevise;

    /** Savings account code. AN/255, FK. */
    @Size(max = 255)
    @Column(name = "CODE_COMPTE_EPARGNE", length = 255)
    private String codeCompteEpargne;

    /** Third-party code. AN/50, FK. */
    @Size(max = 50)
    @Column(name = "CODE_TIERS", length = 50)
    private String codeTiers;

    /** User code. AN/48. */
    @Size(max = 48)
    @Column(name = "utilisateur", length = 48)
    private String utilisateur;

    @Version
    @Column(name = "VERSION")
    private Integer version = 0;

    // ── FK relationships ─────────────────────────────────────────

    /** FK to destination guichet. AN/255. */
    @Size(max = 255)
    @Column(name = "guichetDestinataire", length = 255)
    private String guichetDestinataire;

    /** FK to origin guichet. AN/255. */
    @Size(max = 255)
    @Column(name = "guichetOrigine", length = 255)
    private String guichetOrigine;

    /** FK to caisse (cash desk) performing the operation. AN/255. */
    @Size(max = 255)
    @Column(name = "CODECAISSE", length = 255)
    private String codeCaisse;

    /** FK to c_plancomptable (account number). AN/15. */
    @Size(max = 15)
    @Column(name = "planComptable", length = 15)
    private String planComptable;

    /** FK to journal (numeric ID). */
    @Column(name = "journal")
    private Long journal;

    /** FK to origin-of-operation table (numeric ID). */
    @Column(name = "origine")
    private Long origine;

    /** FK to AGENCE – primary branch of the operation. AN/25. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agence", referencedColumnName = "CODE_AGENCE",
                foreignKey = @ForeignKey(name = "FK_comptabilite_AGENCE"))
    private Agence agence;

    // ── Lifecycle ────────────────────────────────────────────────

    @PrePersist
    private void prePersist() {
        if (dateServeur == null) {
            dateServeur = java.time.LocalDate.now();
        }
    }

    // ── Generated (was Lombok) ──────────────────────────────────────────────

    public Comptabilite() {
    }

    public Comptabilite(Long idComptabilite, BigDecimal credit, BigDecimal debit, BigDecimal entree, BigDecimal sortie, BigDecimal provenance, LocalDate dateOperation, LocalDate dateServeur, LocalDate dateEcheance, LocalDate dateLettrage, LocalDate datePiece, LocalDate dateAmort, LocalDate dateRapprochement, String etat, String lettre, String marque, String id, String libelle, String nPiece, String numCredit, String numPiece, String reference, String testSup, String codeEmp, String compteDestinataire, String facture, String numeroCheque, String compteAuxi, String compteTiers, String compte1, String codeSens, String numReglementFour, String numReglementClient, String numReglement, String codeBailleur, String codeBudget, String typeSuivi, String referenceSuivi, String codeTypeBudget, Long idLignePlanBudgetaire, String codeAgence, String agentExterne, String codeLigneBudgetaire, String codeValidation, String codeDevise, String codeCompteEpargne, String codeTiers, String utilisateur, Integer version, String guichetDestinataire, String guichetOrigine, String codeCaisse, String planComptable, Long journal, Long origine, Agence agence) {
        this.idComptabilite = idComptabilite;
        this.credit = credit;
        this.debit = debit;
        this.entree = entree;
        this.sortie = sortie;
        this.provenance = provenance;
        this.dateOperation = dateOperation;
        this.dateServeur = dateServeur;
        this.dateEcheance = dateEcheance;
        this.dateLettrage = dateLettrage;
        this.datePiece = datePiece;
        this.dateAmort = dateAmort;
        this.dateRapprochement = dateRapprochement;
        this.etat = etat;
        this.lettre = lettre;
        this.marque = marque;
        this.id = id;
        this.libelle = libelle;
        this.nPiece = nPiece;
        this.numCredit = numCredit;
        this.numPiece = numPiece;
        this.reference = reference;
        this.testSup = testSup;
        this.codeEmp = codeEmp;
        this.compteDestinataire = compteDestinataire;
        this.facture = facture;
        this.numeroCheque = numeroCheque;
        this.compteAuxi = compteAuxi;
        this.compteTiers = compteTiers;
        this.compte1 = compte1;
        this.codeSens = codeSens;
        this.numReglementFour = numReglementFour;
        this.numReglementClient = numReglementClient;
        this.numReglement = numReglement;
        this.codeBailleur = codeBailleur;
        this.codeBudget = codeBudget;
        this.typeSuivi = typeSuivi;
        this.referenceSuivi = referenceSuivi;
        this.codeTypeBudget = codeTypeBudget;
        this.idLignePlanBudgetaire = idLignePlanBudgetaire;
        this.codeAgence = codeAgence;
        this.agentExterne = agentExterne;
        this.codeLigneBudgetaire = codeLigneBudgetaire;
        this.codeValidation = codeValidation;
        this.codeDevise = codeDevise;
        this.codeCompteEpargne = codeCompteEpargne;
        this.codeTiers = codeTiers;
        this.utilisateur = utilisateur;
        this.version = version;
        this.guichetDestinataire = guichetDestinataire;
        this.guichetOrigine = guichetOrigine;
        this.codeCaisse = codeCaisse;
        this.planComptable = planComptable;
        this.journal = journal;
        this.origine = origine;
        this.agence = agence;
    }

    public Long getIdComptabilite() {
        return idComptabilite;
    }

    public void setIdComptabilite(Long idComptabilite) {
        this.idComptabilite = idComptabilite;
    }

    public BigDecimal getCredit() {
        return credit;
    }

    public void setCredit(BigDecimal credit) {
        this.credit = credit;
    }

    public BigDecimal getDebit() {
        return debit;
    }

    public void setDebit(BigDecimal debit) {
        this.debit = debit;
    }

    public BigDecimal getEntree() {
        return entree;
    }

    public void setEntree(BigDecimal entree) {
        this.entree = entree;
    }

    public BigDecimal getSortie() {
        return sortie;
    }

    public void setSortie(BigDecimal sortie) {
        this.sortie = sortie;
    }

    public BigDecimal getProvenance() {
        return provenance;
    }

    public void setProvenance(BigDecimal provenance) {
        this.provenance = provenance;
    }

    public LocalDate getDateOperation() {
        return dateOperation;
    }

    public void setDateOperation(LocalDate dateOperation) {
        this.dateOperation = dateOperation;
    }

    public LocalDate getDateServeur() {
        return dateServeur;
    }

    public void setDateServeur(LocalDate dateServeur) {
        this.dateServeur = dateServeur;
    }

    public LocalDate getDateEcheance() {
        return dateEcheance;
    }

    public void setDateEcheance(LocalDate dateEcheance) {
        this.dateEcheance = dateEcheance;
    }

    public LocalDate getDateLettrage() {
        return dateLettrage;
    }

    public void setDateLettrage(LocalDate dateLettrage) {
        this.dateLettrage = dateLettrage;
    }

    public LocalDate getDatePiece() {
        return datePiece;
    }

    public void setDatePiece(LocalDate datePiece) {
        this.datePiece = datePiece;
    }

    public LocalDate getDateAmort() {
        return dateAmort;
    }

    public void setDateAmort(LocalDate dateAmort) {
        this.dateAmort = dateAmort;
    }

    public LocalDate getDateRapprochement() {
        return dateRapprochement;
    }

    public void setDateRapprochement(LocalDate dateRapprochement) {
        this.dateRapprochement = dateRapprochement;
    }

    public String getEtat() {
        return etat;
    }

    public void setEtat(String etat) {
        this.etat = etat;
    }

    public String getLettre() {
        return lettre;
    }

    public void setLettre(String lettre) {
        this.lettre = lettre;
    }

    public String getMarque() {
        return marque;
    }

    public void setMarque(String marque) {
        this.marque = marque;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public String getNPiece() {
        return nPiece;
    }

    public void setNPiece(String nPiece) {
        this.nPiece = nPiece;
    }

    public String getNumCredit() {
        return numCredit;
    }

    public void setNumCredit(String numCredit) {
        this.numCredit = numCredit;
    }

    public String getNumPiece() {
        return numPiece;
    }

    public void setNumPiece(String numPiece) {
        this.numPiece = numPiece;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getTestSup() {
        return testSup;
    }

    public void setTestSup(String testSup) {
        this.testSup = testSup;
    }

    public String getCodeEmp() {
        return codeEmp;
    }

    public void setCodeEmp(String codeEmp) {
        this.codeEmp = codeEmp;
    }

    public String getCompteDestinataire() {
        return compteDestinataire;
    }

    public void setCompteDestinataire(String compteDestinataire) {
        this.compteDestinataire = compteDestinataire;
    }

    public String getFacture() {
        return facture;
    }

    public void setFacture(String facture) {
        this.facture = facture;
    }

    public String getNumeroCheque() {
        return numeroCheque;
    }

    public void setNumeroCheque(String numeroCheque) {
        this.numeroCheque = numeroCheque;
    }

    public String getCompteAuxi() {
        return compteAuxi;
    }

    public void setCompteAuxi(String compteAuxi) {
        this.compteAuxi = compteAuxi;
    }

    public String getCompteTiers() {
        return compteTiers;
    }

    public void setCompteTiers(String compteTiers) {
        this.compteTiers = compteTiers;
    }

    public String getCompte1() {
        return compte1;
    }

    public void setCompte1(String compte1) {
        this.compte1 = compte1;
    }

    public String getCodeSens() {
        return codeSens;
    }

    public void setCodeSens(String codeSens) {
        this.codeSens = codeSens;
    }

    public String getNumReglementFour() {
        return numReglementFour;
    }

    public void setNumReglementFour(String numReglementFour) {
        this.numReglementFour = numReglementFour;
    }

    public String getNumReglementClient() {
        return numReglementClient;
    }

    public void setNumReglementClient(String numReglementClient) {
        this.numReglementClient = numReglementClient;
    }

    public String getNumReglement() {
        return numReglement;
    }

    public void setNumReglement(String numReglement) {
        this.numReglement = numReglement;
    }

    public String getCodeBailleur() {
        return codeBailleur;
    }

    public void setCodeBailleur(String codeBailleur) {
        this.codeBailleur = codeBailleur;
    }

    public String getCodeBudget() {
        return codeBudget;
    }

    public void setCodeBudget(String codeBudget) {
        this.codeBudget = codeBudget;
    }

    public String getTypeSuivi() {
        return typeSuivi;
    }

    public void setTypeSuivi(String typeSuivi) {
        this.typeSuivi = typeSuivi;
    }

    public String getReferenceSuivi() {
        return referenceSuivi;
    }

    public void setReferenceSuivi(String referenceSuivi) {
        this.referenceSuivi = referenceSuivi;
    }

    public String getCodeTypeBudget() {
        return codeTypeBudget;
    }

    public void setCodeTypeBudget(String codeTypeBudget) {
        this.codeTypeBudget = codeTypeBudget;
    }

    public Long getIdLignePlanBudgetaire() {
        return idLignePlanBudgetaire;
    }

    public void setIdLignePlanBudgetaire(Long idLignePlanBudgetaire) {
        this.idLignePlanBudgetaire = idLignePlanBudgetaire;
    }

    public String getCodeAgence() {
        return codeAgence;
    }

    public void setCodeAgence(String codeAgence) {
        this.codeAgence = codeAgence;
    }

    public String getAgentExterne() {
        return agentExterne;
    }

    public void setAgentExterne(String agentExterne) {
        this.agentExterne = agentExterne;
    }

    public String getCodeLigneBudgetaire() {
        return codeLigneBudgetaire;
    }

    public void setCodeLigneBudgetaire(String codeLigneBudgetaire) {
        this.codeLigneBudgetaire = codeLigneBudgetaire;
    }

    public String getCodeValidation() {
        return codeValidation;
    }

    public void setCodeValidation(String codeValidation) {
        this.codeValidation = codeValidation;
    }

    public String getCodeDevise() {
        return codeDevise;
    }

    public void setCodeDevise(String codeDevise) {
        this.codeDevise = codeDevise;
    }

    public String getCodeCompteEpargne() {
        return codeCompteEpargne;
    }

    public void setCodeCompteEpargne(String codeCompteEpargne) {
        this.codeCompteEpargne = codeCompteEpargne;
    }

    public String getCodeTiers() {
        return codeTiers;
    }

    public void setCodeTiers(String codeTiers) {
        this.codeTiers = codeTiers;
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

    public String getGuichetDestinataire() {
        return guichetDestinataire;
    }

    public void setGuichetDestinataire(String guichetDestinataire) {
        this.guichetDestinataire = guichetDestinataire;
    }

    public String getGuichetOrigine() {
        return guichetOrigine;
    }

    public void setGuichetOrigine(String guichetOrigine) {
        this.guichetOrigine = guichetOrigine;
    }

    public String getCodeCaisse() {
        return codeCaisse;
    }

    public void setCodeCaisse(String codeCaisse) {
        this.codeCaisse = codeCaisse;
    }

    public String getPlanComptable() {
        return planComptable;
    }

    public void setPlanComptable(String planComptable) {
        this.planComptable = planComptable;
    }

    public Long getJournal() {
        return journal;
    }

    public void setJournal(Long journal) {
        this.journal = journal;
    }

    public Long getOrigine() {
        return origine;
    }

    public void setOrigine(Long origine) {
        this.origine = origine;
    }

    public Agence getAgence() {
        return agence;
    }

    public void setAgence(Agence agence) {
        this.agence = agence;
    }

    @Override
    public String toString() {
        return "Comptabilite("
            + "idComptabilite=" + idComptabilite
            + ")";
    }
}
