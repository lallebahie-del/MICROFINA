package com.pfe.backend.repository;

import com.microfina.entity.MouvementBudget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * MouvementBudgetRepository – accès JPA à la table MouvementBudget.
 */
@Repository
public interface MouvementBudgetRepository extends JpaRepository<MouvementBudget, Long> {

    /** Mouvements d'une ligne budgétaire donnée. */
    List<MouvementBudget> findByLigneBudget_Id(Long ligneBudgetId);

    /** Mouvements de toutes les lignes d'un budget donné. */
    List<MouvementBudget> findByLigneBudget_Budget_Id(Long budgetId);
}
