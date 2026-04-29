package com.microfina.service;

import com.microfina.entity.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * OperationBanqueService – service métier pour les opérations bancaires.
 *
 * <p>Toute opération (dépôt, retrait, virement, remise de chèque) est traitée
 * de manière <strong>atomique</strong> : la persistance de l'opération et la
 * création de l'écriture comptable ({@link Comptabilite}) se font dans la même
 * transaction JPA. Si l'une échoue, tout est annulé.</p>
 *
 * <p>Chaque méthode publique est annotée {@code @Transactional} pour garantir
 * l'atomicité conforme au cahier des charges BCM.</p>
 *
 * DDL source of truth: P6-005 à P6-009.
 * Spec: cahier §3.1.1 (Module Banque — opérations bancaires).
 */
@Service
@Transactional
public class OperationBanqueService {

    @PersistenceContext
    private EntityManager em;

    // ═══════════════════════════════════════════════════════════════
    // DÉPÔT BANCAIRE
    // ═══════════════════════════════════════════════════════════════

    /**
     * Enregistre un dépôt sur un compte bancaire et comptabilise l'écriture.
     *
     * <p>Flux :
     * <ol>
     *   <li>Créer et persister l'écriture {@link Comptabilite} (débit Caisse / crédit Dépôt).</li>
     *   <li>Créer et persister le {@link DepotBanque} référençant cette écriture.</li>
     *   <li>Mettre à jour {@link CompteBanque#getSolde()} += montant.</li>
     * </ol>
     *
     * @param compteBanque  compte bancaire cible
     * @param agence        agence initiatrice
     * @param montant       montant à déposer (DECIMAL 19,4, &gt; 0)
     * @param modePaiement  "ESPECES" | "VIREMENT" | "CHEQUE"
     * @param motif         libellé de l'opération
     * @param utilisateur   login de l'opérateur
     * @return le {@link DepotBanque} persisté
     */
    public DepotBanque deposer(CompteBanque compteBanque,
                               Agence agence,
                               BigDecimal montant,
                               String modePaiement,
                               String motif,
                               String utilisateur) {
        validerMontant(montant);

        // 1. Écriture comptable
        Comptabilite ecriture = creerEcritureComptable(
            montant,
            "DEPOT_BANQUE",
            "Dépôt bancaire – " + motif,
            utilisateur
        );
        em.persist(ecriture);

        // 2. Opération de dépôt
        DepotBanque depot = new DepotBanque();
        depot.setDateOperation(LocalDate.now());
        depot.setMontant(montant);
        depot.setCompteBanque(compteBanque);
        depot.setAgence(agence);
        depot.setComptabilite(ecriture);
        depot.setStatut(StatutOperationBanque.VALIDE);
        depot.setUtilisateur(utilisateur);
        depot.setMotif(motif);
        depot.setModePaiement(modePaiement != null ? modePaiement : "ESPECES");
        em.persist(depot);

        // 3. Mise à jour du solde
        BigDecimal nouveauSolde = compteBanque.getSolde().add(montant);
        compteBanque.setSolde(nouveauSolde);
        em.merge(compteBanque);

        return depot;
    }

    // ═══════════════════════════════════════════════════════════════
    // RETRAIT BANCAIRE
    // ═══════════════════════════════════════════════════════════════

    /**
     * Enregistre un retrait sur un compte bancaire et comptabilise l'écriture.
     *
     * @param compteBanque  compte bancaire source
     * @param agence        agence initiatrice
     * @param montant       montant à retirer (&gt; 0 et ≤ solde disponible)
     * @param modePaiement  mode de sortie des fonds
     * @param motif         libellé justificatif
     * @param utilisateur   login de l'opérateur
     * @return le {@link RetraitBanque} persisté
     * @throws IllegalStateException si le solde est insuffisant
     */
    public RetraitBanque retirer(CompteBanque compteBanque,
                                 Agence agence,
                                 BigDecimal montant,
                                 String modePaiement,
                                 String motif,
                                 String utilisateur) {
        validerMontant(montant);
        validerSolde(compteBanque, montant);

        // 1. Écriture comptable
        Comptabilite ecriture = creerEcritureComptable(
            montant,
            "RETRAIT_BANQUE",
            "Retrait bancaire – " + motif,
            utilisateur
        );
        em.persist(ecriture);

        // 2. Opération de retrait
        RetraitBanque retrait = new RetraitBanque();
        retrait.setDateOperation(LocalDate.now());
        retrait.setMontant(montant);
        retrait.setCompteBanque(compteBanque);
        retrait.setAgence(agence);
        retrait.setComptabilite(ecriture);
        retrait.setStatut(StatutOperationBanque.VALIDE);
        retrait.setUtilisateur(utilisateur);
        retrait.setMotif(motif);
        retrait.setModePaiement(modePaiement != null ? modePaiement : "ESPECES");
        em.persist(retrait);

        // 3. Débit du solde
        BigDecimal nouveauSolde = compteBanque.getSolde().subtract(montant);
        compteBanque.setSolde(nouveauSolde);
        em.merge(compteBanque);

        return retrait;
    }

    // ═══════════════════════════════════════════════════════════════
    // VIREMENT
    // ═══════════════════════════════════════════════════════════════

    /**
     * Effectue un virement entre deux comptes bancaires.
     *
     * <p>Le débit est appliqué sur {@code compteSource} et le crédit sur
     * {@code compteDestination}. Une seule écriture comptable est générée
     * (double-entrée interne). Pour les virements INTER_BANQUE, la contrepartie
     * externe est gérée par le module de compensation BCM.</p>
     *
     * @param compteSource       compte à débiter
     * @param compteDestination  compte à créditer
     * @param agence             agence initiatrice
     * @param montant            montant du virement (&gt; 0)
     * @param typeVirement       INTRA_BANQUE ou INTER_BANQUE
     * @param referenceExterne   numéro de référence externe (swift, RIB, etc.)
     * @param motif              libellé
     * @param utilisateur        login de l'opérateur
     * @return le {@link Virement} persisté
     */
    public Virement virer(CompteBanque compteSource,
                          CompteBanque compteDestination,
                          Agence agence,
                          BigDecimal montant,
                          TypeVirement typeVirement,
                          String referenceExterne,
                          String motif,
                          String utilisateur) {
        validerMontant(montant);
        validerSolde(compteSource, montant);

        // 1. Écriture comptable
        Comptabilite ecriture = creerEcritureComptable(
            montant,
            "VIREMENT_" + typeVirement.name(),
            "Virement " + typeVirement + " – " + motif,
            utilisateur
        );
        em.persist(ecriture);

        // 2. Virement
        Virement virement = new Virement();
        virement.setDateOperation(LocalDate.now());
        virement.setMontant(montant);
        virement.setCompteBanque(compteSource);   // compte principal opération
        virement.setCompteSource(compteSource);
        virement.setCompteDestination(compteDestination);
        virement.setAgence(agence);
        virement.setComptabilite(ecriture);
        virement.setStatut(StatutOperationBanque.VALIDE);
        virement.setUtilisateur(utilisateur);
        virement.setTypeVirement(typeVirement);
        virement.setReferenceExterne(referenceExterne);
        virement.setMotif(motif);
        em.persist(virement);

        // 3. Mouvement de soldes
        compteSource.setSolde(compteSource.getSolde().subtract(montant));
        em.merge(compteSource);

        if (typeVirement == TypeVirement.INTRA_BANQUE) {
            // Crédit immédiat pour les virements internes
            compteDestination.setSolde(compteDestination.getSolde().add(montant));
            em.merge(compteDestination);
        }
        // Pour INTER_BANQUE : la mise à jour de compteDestination est gérée
        // après confirmation de la chambre de compensation BCM.

        return virement;
    }

    // ═══════════════════════════════════════════════════════════════
    // REMISE DE CHÈQUE
    // ═══════════════════════════════════════════════════════════════

    /**
     * Enregistre la remise d'un chèque à l'encaissement.
     *
     * <p>Le chèque passe au statut ENCAISSE et une écriture comptable
     * est créée. Le solde du compte bénéficiaire est mis à jour.</p>
     *
     * @param cheque              chèque à encaisser (statut EMIS attendu)
     * @param compteBeneficiaire  compte crédité lors de l'encaissement
     * @param agence              agence traitante
     * @param banquePresentatrice banque présentatrice (libellé)
     * @param dateValeur          date de valeur de l'encaissement
     * @param utilisateur         login de l'opérateur
     * @return la {@link RemiseCheque} persistée
     * @throws IllegalStateException si le chèque n'est pas au statut EMIS
     */
    public RemiseCheque encaisserCheque(Cheque cheque,
                                        CompteBanque compteBeneficiaire,
                                        Agence agence,
                                        String banquePresentatrice,
                                        LocalDate dateValeur,
                                        String utilisateur) {
        if (cheque.getStatut() != StatutCheque.EMIS) {
            throw new IllegalStateException(
                "Le chèque n° " + cheque.getNumero()
                + " ne peut pas être encaissé (statut actuel : " + cheque.getStatut() + ")"
            );
        }

        // 1. Écriture comptable
        Comptabilite ecriture = creerEcritureComptable(
            cheque.getMontant(),
            "REMISE_CHEQUE",
            "Remise chèque n° " + cheque.getNumero() + " – " + banquePresentatrice,
            utilisateur
        );
        em.persist(ecriture);

        // 2. Remise chèque
        RemiseCheque remise = new RemiseCheque();
        remise.setDateOperation(LocalDate.now());
        remise.setMontant(cheque.getMontant());
        remise.setCompteBanque(compteBeneficiaire);
        remise.setAgence(agence);
        remise.setComptabilite(ecriture);
        remise.setStatut(StatutOperationBanque.VALIDE);
        remise.setUtilisateur(utilisateur);
        remise.setCheque(cheque);
        remise.setBanquePresentatrice(banquePresentatrice);
        remise.setDateValeur(dateValeur != null ? dateValeur : LocalDate.now());
        em.persist(remise);

        // 3. Mise à jour du chèque et du solde
        cheque.setStatut(StatutCheque.ENCAISSE);
        cheque.setDateEncaissement(LocalDate.now());
        em.merge(cheque);

        compteBeneficiaire.setSolde(compteBeneficiaire.getSolde().add(cheque.getMontant()));
        em.merge(compteBeneficiaire);

        return remise;
    }

    // ═══════════════════════════════════════════════════════════════
    // GESTION CARNETS DE CHÈQUE
    // ═══════════════════════════════════════════════════════════════

    /**
     * Crée une demande de carnet de chèque pour un membre.
     *
     * @param compteBanque  compte bancaire associé
     * @param membre        membre demandeur
     * @param numeroCarnet  numéro de série du carnet
     * @param nombreCheques nombre de formules (25 ou 50)
     * @return le {@link CarnetCheque} persisté au statut DEMANDE
     */
    public CarnetCheque demanderCarnet(CompteBanque compteBanque,
                                       Membres membre,
                                       String numeroCarnet,
                                       int nombreCheques) {
        CarnetCheque carnet = new CarnetCheque();
        carnet.setCompteBanque(compteBanque);
        carnet.setMembre(membre);
        carnet.setNumeroCarnet(numeroCarnet);
        carnet.setNombreCheques(nombreCheques > 0 ? nombreCheques : 25);
        carnet.setDateDemande(LocalDate.now());
        carnet.setStatut(StatutCarnetCheque.DEMANDE);
        em.persist(carnet);
        return carnet;
    }

    /**
     * Marque un carnet comme remis au membre.
     *
     * @param carnet carnet à remettre (statut IMPRIME attendu)
     * @return le carnet mis à jour
     */
    public CarnetCheque remettreCarnnet(CarnetCheque carnet) {
        if (carnet.getStatut() != StatutCarnetCheque.IMPRIME) {
            throw new IllegalStateException(
                "Le carnet " + carnet.getNumeroCarnet()
                + " doit être IMPRIME avant remise (statut actuel : " + carnet.getStatut() + ")"
            );
        }
        carnet.setStatut(StatutCarnetCheque.REMIS);
        carnet.setDateRemise(LocalDate.now());
        return em.merge(carnet);
    }

    // ═══════════════════════════════════════════════════════════════
    // MÉTHODES PRIVÉES
    // ═══════════════════════════════════════════════════════════════

    /**
     * Construit une écriture comptable minimale pour une opération bancaire.
     * Le plan de compte (CompteAuxi / compteDestinataire) doit être complété
     * par la couche comptable en amont selon le référentiel de comptes BCM.
     *
     * @param montant       montant de l'écriture (porté en débit)
     * @param reference     code de type d'opération (ex : "DEPOT_BANQUE")
     * @param libelle       libellé lisible de l'écriture
     * @param codeOperateur code de l'opérateur (codeEmp)
     */
    private Comptabilite creerEcritureComptable(BigDecimal montant,
                                                 String reference,
                                                 String libelle,
                                                 String codeOperateur) {
        Comptabilite c = new Comptabilite();
        c.setDateOperation(LocalDate.now());
        c.setDebit(montant);
        c.setCredit(BigDecimal.ZERO);
        c.setLibelle(libelle);
        c.setReference(reference);
        c.setCodeEmp(codeOperateur);
        c.setEtat("VALIDE");
        return c;
    }

    /** Lève une exception si le montant est nul ou négatif. */
    private void validerMontant(BigDecimal montant) {
        if (montant == null || montant.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le montant doit être strictement positif.");
        }
    }

    /** Lève une exception si le solde du compte est insuffisant. */
    private void validerSolde(CompteBanque compte, BigDecimal montant) {
        if (compte.getSolde().compareTo(montant) < 0) {
            throw new IllegalStateException(
                "Solde insuffisant sur le compte " + compte.getNumeroCompte()
                + " : solde=" + compte.getSolde() + ", montant demandé=" + montant
            );
        }
    }
}
