package com.pfe.backend.repository;

import com.microfina.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * BudgetRepository – accès JPA à la table Budget.
 */
@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    /**
     * Retourne tous les budgets d'un exercice fiscal.
     *
     * @param exerciceFiscal l'année de l'exercice
     * @return liste des budgets
     */
    List<Budget> findByExerciceFiscal(Integer exerciceFiscal);

    /**
     * Retourne tous les budgets d'une agence.
     *
     * @param codeAgence le code de l'agence
     * @return liste des budgets
     */
    List<Budget> findByAgence_CodeAgence(String codeAgence);

    /**
     * Retourne le budget unique d'un exercice pour une agence donnée.
     *
     * @param exerciceFiscal l'année de l'exercice
     * @param codeAgence     le code de l'agence
     * @return le budget s'il existe
     */
    Optional<Budget> findByExerciceFiscalAndAgence_CodeAgence(Integer exerciceFiscal, String codeAgence);
}
