package com.pfe.backend.service;

import com.microfina.entity.Comptabilite;
import com.microfina.entity.Credits;
import com.microfina.entity.ProduitCredit;
import com.pfe.backend.repository.ComptabiliteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class CreditAccountingService {

    private final ComptabiliteRepository comptabiliteRepository;

    public CreditAccountingService(ComptabiliteRepository comptabiliteRepository) {
        this.comptabiliteRepository = comptabiliteRepository;
    }

    /**
     * Écriture minimaliste de déblocage (sortie de fonds).
     *
     * Note: le modèle comptable complet multi-lignes est hors scope ici ; on persiste
     * une écriture de journal minimale, comme le font les services caisse/banque.
     */
    @Transactional
    public Comptabilite ecritureDeblocage(Credits credit,
                                          BigDecimal montant,
                                          String utilisateur,
                                          String reference,
                                          String numPiece) {
        ProduitCredit produit = credit != null ? credit.getProduitCredit() : null;
        String compteSortie = produit != null ? produit.getCompteDeblocage() : null;

        Comptabilite c = new Comptabilite();
        c.setDateOperation(LocalDate.now());
        c.setDebit(BigDecimal.ZERO);
        c.setCredit(montant);
        c.setLibelle("Déblocage crédit " + (credit != null ? credit.getNumCredit() : ""));
        c.setReference(reference);
        c.setCodeEmp(utilisateur);
        c.setNumPiece(numPiece);
        c.setEtat("VALIDE");
        c.setPlanComptable(compteSortie);
        c.setNumCredit(credit != null ? credit.getNumCredit() : null);
        c.setUtilisateur(utilisateur);
        if (credit != null && credit.getAgence() != null) {
            c.setAgence(credit.getAgence());
        }
        return comptabiliteRepository.save(c);
    }

    /**
     * Écriture minimaliste de remboursement reçu en caisse (entrée de fonds).
     */
    @Transactional
    public Comptabilite ecritureRemboursementCaisse(Credits credit,
                                                    BigDecimal montant,
                                                    String utilisateur,
                                                    String reference,
                                                    String numPiece) {
        ProduitCredit produit = credit != null ? credit.getProduitCredit() : null;
        String compteEntree = produit != null ? produit.getCompteDeblocage() : null;

        Comptabilite c = new Comptabilite();
        c.setDateOperation(LocalDate.now());
        c.setDebit(montant);
        c.setCredit(BigDecimal.ZERO);
        c.setLibelle("Remboursement crédit " + (credit != null ? credit.getNumCredit() : ""));
        c.setReference(reference);
        c.setCodeEmp(utilisateur);
        c.setNumPiece(numPiece);
        c.setEtat("VALIDE");
        c.setPlanComptable(compteEntree);
        c.setNumCredit(credit != null ? credit.getNumCredit() : null);
        c.setUtilisateur(utilisateur);
        if (credit != null && credit.getAgence() != null) {
            c.setAgence(credit.getAgence());
        }
        return comptabiliteRepository.save(c);
    }
}

