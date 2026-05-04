package com.pfe.backend.repository;

import com.microfina.entity.LigneBudget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * LigneBudgetRepository – accès JPA à la table LigneBudget.
 */
@Repository
public interface LigneBudgetRepository extends JpaRepository<LigneBudget, Long> {

    /**
     * Retourne toutes les lignes budgétaires d'un budget.
     *
     * @param budgetId l'identifiant du budget
     * @return liste des lignes
     */
    List<LigneBudget> findByBudget_Id(Long budgetId);
}
