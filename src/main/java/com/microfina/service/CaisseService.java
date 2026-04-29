package com.microfina.service;

import com.microfina.entity.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * CaisseService – service métier pour les opérations de caisse épargne.
 *
 * <p>Couvre les trois flux cahier §3.1.1 :
 * <ul>
 *   <li><b>Dépôt</b>   – crédit sur compte épargne d'un membre.</li>
 *   <li><b>Retrait</b> – débit sur compte épargne (contrôle solde disponible).</li>
 *   <li><b>Frais d'adhésion</b> – encaissement des frais liés à l'adhésion d'un membre.</li>
 * </ul>
 *
 * <p>Chaque opération est <strong>atomique</strong> : la persistance de
 * l'opération caisse et l'écriture {@link Comptabilite} se font dans la
 * même transaction JPA. Toute exception provoque un rollback global.</p>
 *
 * <p>Le solde courant du {@link CompteEps} est porté par {@code montantDepot}.
 * Le montant bloqué ({@code montantBloque}) est déduit pour calculer le solde
 * disponible lors d'un retrait.</p>
 *
 * DDL source of truth: P7-001 à P7-004.
 * Spec: cahier §3.1.1 (Opérations de caisse – adhésion, dépôt, retrait,
 * remboursement crédit).
 */
@Service
@Transactional
public class CaisseService {

    @PersistenceContext
    private EntityManager em;

    /** Préfixe utilisé pour générer les numéros de pièce caisse. */
    private static final String PREFIXE_PIECE = "PC-";

    /** Compteur en mémoire (redémarré à chaque déploiement ; en prod : séquence DB). */
    private static final AtomicLong compteurPiece = new AtomicLong(1);

    // ═══════════════════════════════════════════════════════════════
    // DÉPÔT ÉPARGNE
    // ═══════════════════════════════════════════════════════════════

    /**
     * Effectue un dépôt sur le compte épargne d'un membre.
     *
     * <p>Flux atomique :
     * <ol>
     *   <li>Valider le montant (&gt; 0).</li>
     *   <li>Créer l'écriture {@link Comptabilite} (crédit compte épargne).</li>
     *   <li>Créer et persister le {@link DepotEpargne}.</li>
     *   <li>Mettre à jour {@link CompteEps#getMontantDepot()} += montant.</li>
     * </ol>
     *
     * @param compteEps    compte épargne du membre
     * @param agence       agence traitante
     * @param montant      montant à déposer (DECIMAL 19,4, &gt; 0)
     * @param modePaiement mode de versement (ESPECES, CHEQUE, VIREMENT, MOBILE_MONEY)
     * @param motif        libellé descriptif
     * @param utilisateur  login du caissier
     * @return le {@link DepotEpargne} persisté et validé
     */
    public DepotEpargne deposer(CompteEps compteEps,
                                Agence agence,
                                BigDecimal montant,
                                ModePaiementCaisse modePaiement,
                                String motif,
                                String utilisateur) {
        validerMontantPositif(montant);

        String numPiece = genererNumeroPiece("DEP");

        // 1. Écriture comptable
        Comptabilite ecriture = creerEcritureComptable(
            montant,
            "DEPOT_EPARGNE",
            "Dépôt épargne cpt " + compteEps.getNumCompte() + " – " + motif,
            utilisateur,
            numPiece
        );
        em.persist(ecriture);

        // 2. Opération de dépôt
        DepotEpargne depot = new DepotEpargne();
        depot.setNumPiece(numPiece);
        depot.setDateOperation(LocalDate.now());
        depot.setMontant(montant);
        depot.setModePaiement(modePaiement != null ? modePaiement : ModePaiementCaisse.ESPECES);
        depot.setMotif(motif);
        depot.setUtilisateur(utilisateur);
        depot.setStatut(StatutOperationCaisse.VALIDE);
        depot.setCompteEps(compteEps);
        depot.setAgence(agence);
        depot.setComptabilite(ecriture);
        em.persist(depot);

        // 3. Mise à jour du solde
        BigDecimal ancienSolde = compteEps.getMontantDepot() != null
            ? compteEps.getMontantDepot() : BigDecimal.ZERO;
        compteEps.setMontantDepot(ancienSolde.add(montant));
        em.merge(compteEps);

        return depot;
    }

    // ═══════════════════════════════════════════════════════════════
    // RETRAIT ÉPARGNE
    // ═══════════════════════════════════════════════════════════════

    /**
     * Effectue un retrait sur le compte épargne d'un membre.
     *
     * <p>Le solde disponible est calculé comme :
     * {@code soldeDisponible = montantDepot - montantBloque}.
     * Le retrait est refusé si le montant demandé excède ce disponible.</p>
     *
     * <p>Flux atomique :
     * <ol>
     *   <li>Valider le montant et le solde disponible.</li>
     *   <li>Créer l'écriture {@link Comptabilite} (débit compte épargne).</li>
     *   <li>Créer et persister le {@link RetraitEpargne}.</li>
     *   <li>Décrémenter {@link CompteEps#getMontantDepot()}.</li>
     * </ol>
     *
     * @param compteEps    compte épargne du membre
     * @param agence       agence traitante
     * @param montant      montant à retirer (&gt; 0 et ≤ solde disponible)
     * @param modePaiement mode de sortie des fonds
     * @param motif        justification du retrait
     * @param utilisateur  login du caissier
     * @return le {@link RetraitEpargne} persisté et validé
     * @throws IllegalStateException si le solde disponible est insuffisant
     */
    public RetraitEpargne retirer(CompteEps compteEps,
                                   Agence agence,
                                   BigDecimal montant,
                                   ModePaiementCaisse modePaiement,
                                   String motif,
                                   String utilisateur) {
        validerMontantPositif(montant);
        validerSoldeDisponible(compteEps, montant);

        String numPiece = genererNumeroPiece("RET");

        // 1. Écriture comptable
        Comptabilite ecriture = creerEcritureComptable(
            montant,
            "RETRAIT_EPARGNE",
            "Retrait épargne cpt " + compteEps.getNumCompte() + " – " + motif,
            utilisateur,
            numPiece
        );
        em.persist(ecriture);

        // 2. Opération de retrait
        RetraitEpargne retrait = new RetraitEpargne();
        retrait.setNumPiece(numPiece);
        retrait.setDateOperation(LocalDate.now());
        retrait.setMontant(montant);
        retrait.setModePaiement(modePaiement != null ? modePaiement : ModePaiementCaisse.ESPECES);
        retrait.setMotif(motif);
        retrait.setUtilisateur(utilisateur);
        retrait.setStatut(StatutOperationCaisse.VALIDE);
        retrait.setCompteEps(compteEps);
        retrait.setAgence(agence);
        retrait.setComptabilite(ecriture);
        em.persist(retrait);

        // 3. Débit du solde
        BigDecimal ancienSolde = compteEps.getMontantDepot() != null
            ? compteEps.getMontantDepot() : BigDecimal.ZERO;
        compteEps.setMontantDepot(ancienSolde.subtract(montant));
        em.merge(compteEps);

        return retrait;
    }

    // ═══════════════════════════════════════════════════════════════
    // FRAIS D'ADHÉSION
    // ═══════════════════════════════════════════════════════════════

    /**
     * Enregistre le paiement des frais d'adhésion d'un membre.
     *
     * <p>Les frais d'adhésion ne sont pas liés à un compte épargne (le compte
     * peut ne pas encore exister à l'instant de la demande d'adhésion).
     * Le {@link ValeurFraisMembre} encapsule le barème applicable.</p>
     *
     * <p>Flux atomique :
     * <ol>
     *   <li>Valider le montant (&gt; 0).</li>
     *   <li>Créer l'écriture {@link Comptabilite} (recette frais).</li>
     *   <li>Créer et persister le {@link FraisAdhesion}.</li>
     * </ol>
     *
     * @param agence             agence traitante
     * @param valeurFraisMembre  barème de frais applicable au membre
     * @param montant            montant réellement perçu (peut différer du barème)
     * @param modePaiement       mode de versement
     * @param motif              libellé justificatif
     * @param utilisateur        login du caissier
     * @return le {@link FraisAdhesion} persisté et validé
     */
    public FraisAdhesion payerFraisAdhesion(Agence agence,
                                             ValeurFraisMembre valeurFraisMembre,
                                             BigDecimal montant,
                                             ModePaiementCaisse modePaiement,
                                             String motif,
                                             String utilisateur) {
        validerMontantPositif(montant);

        String numPiece = genererNumeroPiece("ADH");

        // 1. Écriture comptable
        Comptabilite ecriture = creerEcritureComptable(
            montant,
            "FRAIS_ADHESION",
            "Frais adhésion – " + (motif != null ? motif : ""),
            utilisateur,
            numPiece
        );
        em.persist(ecriture);

        // 2. Opération frais
        FraisAdhesion frais = new FraisAdhesion();
        frais.setNumPiece(numPiece);
        frais.setDateOperation(LocalDate.now());
        frais.setMontant(montant);
        frais.setModePaiement(modePaiement != null ? modePaiement : ModePaiementCaisse.ESPECES);
        frais.setMotif(motif);
        frais.setUtilisateur(utilisateur);
        frais.setStatut(StatutOperationCaisse.VALIDE);
        frais.setCompteEps(null);      // pas encore de compte à cette étape
        frais.setAgence(agence);
        frais.setComptabilite(ecriture);
        frais.setValeurFraisMembre(valeurFraisMembre);
        em.persist(frais);

        return frais;
    }

    // ═══════════════════════════════════════════════════════════════
    // MÉTHODES PRIVÉES
    // ═══════════════════════════════════════════════════════════════

    /**
     * Génère un numéro de pièce unique au format PC-{type}-{YYYYMMDD}-{seq}.
     */
    private String genererNumeroPiece(String type) {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        return PREFIXE_PIECE + type + "-" + date + "-" + compteurPiece.getAndIncrement();
    }

    /**
     * Construit une écriture comptable minimale pour une opération caisse.
     */
    private Comptabilite creerEcritureComptable(BigDecimal montant,
                                                 String reference,
                                                 String libelle,
                                                 String codeOperateur,
                                                 String numPiece) {
        Comptabilite c = new Comptabilite();
        c.setDateOperation(LocalDate.now());
        c.setDebit(montant);
        c.setCredit(BigDecimal.ZERO);
        c.setLibelle(libelle);
        c.setReference(reference);
        c.setCodeEmp(codeOperateur);
        c.setNumPiece(numPiece);
        c.setEtat("VALIDE");
        return c;
    }

    /** Lève une exception si le montant est nul ou négatif. */
    private void validerMontantPositif(BigDecimal montant) {
        if (montant == null || montant.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                "Le montant doit être strictement positif. Reçu : " + montant
            );
        }
    }

    /**
     * Lève une exception si le solde disponible est insuffisant.
     * Solde disponible = montantDepot − montantBloque.
     */
    private void validerSoldeDisponible(CompteEps compte, BigDecimal montant) {
        BigDecimal depot   = compte.getMontantDepot()  != null ? compte.getMontantDepot()  : BigDecimal.ZERO;
        BigDecimal bloque  = compte.getMontantBloque() != null ? compte.getMontantBloque() : BigDecimal.ZERO;
        BigDecimal disponible = depot.subtract(bloque);

        if (disponible.compareTo(montant) < 0) {
            throw new IllegalStateException(
                "Solde disponible insuffisant sur le compte " + compte.getNumCompte()
                + " : disponible=" + disponible + ", montant demandé=" + montant
            );
        }
    }
}
