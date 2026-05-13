package com.pfe.backend.service;

import com.microfina.entity.Comptabilite;
import com.microfina.entity.LigneBudget;
import com.microfina.entity.MouvementBudget;
import com.pfe.backend.exception.ResourceNotFoundException;
import com.pfe.backend.repository.ComptabiliteRepository;
import com.pfe.backend.repository.LigneBudgetRepository;
import com.pfe.backend.repository.MouvementBudgetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class MouvementBudgetService {

    private final MouvementBudgetRepository repo;
    private final LigneBudgetRepository     ligneRepo;
    private final ComptabiliteRepository    comptabiliteRepo;
    private final LigneBudgetService        ligneService;

    public MouvementBudgetService(MouvementBudgetRepository repo,
                                  LigneBudgetRepository ligneRepo,
                                  ComptabiliteRepository comptabiliteRepo,
                                  LigneBudgetService ligneService) {
        this.repo = repo;
        this.ligneRepo = ligneRepo;
        this.comptabiliteRepo = comptabiliteRepo;
        this.ligneService = ligneService;
    }

    public List<MouvementBudget> findByLigne(Long ligneBudgetId) {
        return repo.findByLigneBudget_Id(ligneBudgetId);
    }

    public List<MouvementBudget> findByBudget(Long budgetId) {
        return repo.findByLigneBudget_Budget_Id(budgetId);
    }

    public MouvementBudget findById(Long id) {
        return repo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("MouvementBudget", id));
    }

    @Transactional
    public MouvementBudget create(Long ligneBudgetId,
                                  Long idComptabilite,
                                  LocalDate dateMouvement,
                                  BigDecimal montant,
                                  String libelle,
                                  String utilisateur) {
        LigneBudget ligne = ligneRepo.findById(ligneBudgetId)
            .orElseThrow(() -> new ResourceNotFoundException("LigneBudget", ligneBudgetId));
        Comptabilite ecriture = comptabiliteRepo.findById(idComptabilite)
            .orElseThrow(() -> new ResourceNotFoundException("Comptabilite", idComptabilite));

        if (montant == null || montant.signum() <= 0) {
            throw new IllegalArgumentException("Le montant doit être strictement positif.");
        }

        MouvementBudget mvt = new MouvementBudget();
        mvt.setLigneBudget(ligne);
        mvt.setComptabilite(ecriture);
        mvt.setDateMouvement(dateMouvement != null ? dateMouvement : LocalDate.now());
        mvt.setMontant(montant);
        mvt.setLibelle(libelle);
        mvt.setUtilisateur(utilisateur);

        MouvementBudget saved = repo.save(mvt);
        ligneService.recalcBudgetTotaux(ligne.getBudget());
        return saved;
    }

    @Transactional
    public void delete(Long id) {
        MouvementBudget mvt = findById(id);
        LigneBudget ligne = mvt.getLigneBudget();
        repo.delete(mvt);
        ligneService.recalcBudgetTotaux(ligne.getBudget());
    }
}
