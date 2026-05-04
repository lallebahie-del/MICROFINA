package com.pfe.backend.service;

import com.microfina.entity.Budget;
import com.microfina.entity.StatutBudget;
import com.pfe.backend.exception.BusinessException;
import com.pfe.backend.exception.ClotureException;
import com.pfe.backend.repository.BudgetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ClotureService {

    private static final Logger log = LoggerFactory.getLogger(ClotureService.class);

    private final JdbcTemplate jdbc;
    private final BudgetRepository budgetRepository;

    public ClotureService(JdbcTemplate jdbc, BudgetRepository budgetRepository) {
        this.jdbc = jdbc;
        this.budgetRepository = budgetRepository;
    }

    @Transactional
    public void clotureMensuelle(int annee, int mois) {
        if (mois < 1 || mois > 12) {
            throw new BusinessException("Mois invalide : " + mois + ". Attendu : 1-12");
        }
        LocalDate debut = LocalDate.of(annee, mois, 1);
        LocalDate fin   = debut.plusMonths(1).minusDays(1);

        log.info("[Cloture] Vérification opérations en attente du {} au {}", debut, fin);

        Integer nbCaisse = jdbc.queryForObject(
            "SELECT COUNT(*) FROM OperationCaisse WHERE statut = 'EN_ATTENTE' " +
            "AND date_operation BETWEEN ? AND ?",
            Integer.class, debut, fin);
        if (nbCaisse != null && nbCaisse > 0) {
            throw new ClotureException(nbCaisse + " opération(s) de caisse en attente pour " + debut + " → " + fin);
        }

        Integer nbBanque = jdbc.queryForObject(
            "SELECT COUNT(*) FROM OperationBanque WHERE statut = 'EN_ATTENTE' " +
            "AND date_operation BETWEEN ? AND ?",
            Integer.class, debut, fin);
        if (nbBanque != null && nbBanque > 0) {
            throw new ClotureException(nbBanque + " opération(s) bancaire(s) en attente pour " + debut + " → " + fin);
        }

        log.info("[Cloture] Clôture mensuelle {}/{} terminée", mois, annee);
    }

    @Transactional
    public void clotureAnnuelle(int annee) {
        List<Budget> budgets = budgetRepository.findByExerciceFiscal(annee);
        if (budgets.isEmpty()) {
            throw new BusinessException("Aucun budget trouvé pour l'exercice " + annee);
        }
        for (Budget b : budgets) {
            if (b.getStatut() == StatutBudget.CLOTURE) { continue; }
            if (b.getStatut() == StatutBudget.BROUILLON) {
                throw new BusinessException("Budget " + b.getId() + " en BROUILLON — validez-le avant clôture.");
            }
            b.setStatut(StatutBudget.CLOTURE);
            budgetRepository.save(b);
        }
        log.info("[Cloture] Clôture annuelle {} terminée — {} budget(s) clôturé(s)", annee, budgets.size());
    }
}
