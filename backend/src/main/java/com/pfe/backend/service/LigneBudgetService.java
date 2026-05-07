package com.pfe.backend.service;

import com.microfina.entity.Budget;
import com.microfina.entity.LigneBudget;
import com.microfina.entity.MouvementBudget;
import com.microfina.entity.TypeLigneBudget;
import com.pfe.backend.exception.ResourceNotFoundException;
import com.pfe.backend.repository.BudgetRepository;
import com.pfe.backend.repository.LigneBudgetRepository;
import com.pfe.backend.repository.MouvementBudgetRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class LigneBudgetService {

    private final LigneBudgetRepository       repo;
    private final BudgetRepository            budgetRepo;
    private final MouvementBudgetRepository   mouvementRepo;

    public LigneBudgetService(LigneBudgetRepository repo,
                              BudgetRepository budgetRepo,
                              MouvementBudgetRepository mouvementRepo) {
        this.repo = repo;
        this.budgetRepo = budgetRepo;
        this.mouvementRepo = mouvementRepo;
    }

    public List<LigneBudget> findByBudget(Long budgetId) {
        return repo.findByBudget_Id(budgetId);
    }

    public LigneBudget findById(Long id) {
        return repo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("LigneBudget", id));
    }

    @Transactional
    public LigneBudget create(Long budgetId,
                              String codeRubrique,
                              String libelle,
                              TypeLigneBudget typeLigne,
                              BigDecimal montantPrevu,
                              String compte) {
        Budget budget = budgetRepo.findById(budgetId)
            .orElseThrow(() -> new ResourceNotFoundException("Budget", budgetId));

        if (!com.microfina.entity.StatutBudget.BROUILLON.equals(budget.getStatut())) {
            throw new IllegalStateException("Impossible d'ajouter une ligne sur un budget non BROUILLON (statut=" + budget.getStatut() + ").");
        }

        LigneBudget ligne = new LigneBudget();
        ligne.setBudget(budget);
        ligne.setCodeRubrique(codeRubrique);
        ligne.setLibelle(libelle);
        ligne.setTypeLigne(typeLigne != null ? typeLigne : TypeLigneBudget.RECETTE);
        ligne.setMontantPrevu(montantPrevu != null ? montantPrevu : BigDecimal.ZERO);
        ligne.setMontantRealise(BigDecimal.ZERO);
        ligne.setCompte(compte);

        LigneBudget saved = repo.save(ligne);
        recalcBudgetTotaux(budget);
        return saved;
    }

    @Transactional
    public LigneBudget update(Long id,
                              String codeRubrique,
                              String libelle,
                              TypeLigneBudget typeLigne,
                              BigDecimal montantPrevu,
                              String compte) {
        LigneBudget ligne = findById(id);

        if (!com.microfina.entity.StatutBudget.BROUILLON.equals(ligne.getBudget().getStatut())) {
            throw new IllegalStateException("Impossible de modifier une ligne sur un budget non BROUILLON.");
        }

        if (codeRubrique != null) ligne.setCodeRubrique(codeRubrique);
        if (libelle != null)      ligne.setLibelle(libelle);
        if (typeLigne != null)    ligne.setTypeLigne(typeLigne);
        if (montantPrevu != null) ligne.setMontantPrevu(montantPrevu);
        if (compte != null)       ligne.setCompte(compte);

        LigneBudget saved = repo.save(ligne);
        recalcBudgetTotaux(ligne.getBudget());
        return saved;
    }

    @Transactional
    public void delete(Long id) {
        LigneBudget ligne = findById(id);

        if (!com.microfina.entity.StatutBudget.BROUILLON.equals(ligne.getBudget().getStatut())) {
            throw new IllegalStateException("Impossible de supprimer une ligne sur un budget non BROUILLON.");
        }

        Budget budget = ligne.getBudget();
        try {
            repo.delete(ligne);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException(
                "Suppression impossible : la ligne porte des mouvements. Supprime d'abord les mouvements liés.");
        }
        recalcBudgetTotaux(budget);
    }

    /**
     * Recalcule {@code Budget.montantTotalRecettes/Depenses} à partir des lignes
     * et le {@code montantRealise} des lignes à partir des mouvements.
     */
    void recalcBudgetTotaux(Budget budget) {
        List<LigneBudget> lignes = repo.findByBudget_Id(budget.getId());

        for (LigneBudget l : lignes) {
            List<MouvementBudget> mvts = mouvementRepo.findByLigneBudget_Id(l.getId());
            BigDecimal realise = mvts.stream()
                .map(MouvementBudget::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (realise.compareTo(l.getMontantRealise()) != 0) {
                l.setMontantRealise(realise);
                repo.save(l);
            }
        }

        BigDecimal recettes = lignes.stream()
            .filter(l -> l.getTypeLigne() == TypeLigneBudget.RECETTE)
            .map(LigneBudget::getMontantPrevu)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal depenses = lignes.stream()
            .filter(l -> l.getTypeLigne() == TypeLigneBudget.DEPENSE)
            .map(LigneBudget::getMontantPrevu)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        budget.setMontantTotalRecettes(recettes);
        budget.setMontantTotalDepenses(depenses);
        budgetRepo.save(budget);
    }
}
