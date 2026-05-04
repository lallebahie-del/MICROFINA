package com.pfe.backend.service;

import com.microfina.entity.Budget;
import com.microfina.entity.StatutBudget;
import com.pfe.backend.dto.BudgetDTO;
import com.pfe.backend.exception.BusinessException;
import com.pfe.backend.exception.ClotureException;
import com.pfe.backend.exception.ResourceNotFoundException;
import com.pfe.backend.repository.AgenceRepository;
import com.pfe.backend.repository.BudgetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * BudgetService – logique métier pour la gestion budgétaire.
 *
 * <p>Cycle de vie : BROUILLON → VALIDE → CLOTURE.</p>
 */
@Service
@Transactional(readOnly = true)
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final AgenceRepository agenceRepository;

    public BudgetService(
            BudgetRepository budgetRepository,
            AgenceRepository agenceRepository
    ) {
        this.budgetRepository = budgetRepository;
        this.agenceRepository = agenceRepository;
    }

    // ── Lecture ───────────────────────────────────────────────────────────────

    /**
     * Retourne tous les budgets.
     */
    public List<BudgetDTO.Response> findAll() {
        return budgetRepository.findAll()
                .stream()
                .map(BudgetDTO.Response::from)
                .toList();
    }

    /**
     * Retourne un budget par son identifiant.
     *
     * @param id identifiant technique
     * @return le DTO correspondant
     * @throws ResourceNotFoundException si introuvable
     */
    public BudgetDTO.Response findById(Long id) {
        Budget b = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget", id));
        return BudgetDTO.Response.from(b);
    }

    /**
     * Retourne les budgets d'un exercice fiscal.
     *
     * @param exerciceFiscal l'année de l'exercice
     * @return liste des DTOs
     */
    public List<BudgetDTO.Response> findByExercice(Integer exerciceFiscal) {
        return budgetRepository.findByExerciceFiscal(exerciceFiscal)
                .stream()
                .map(BudgetDTO.Response::from)
                .toList();
    }

    /**
     * Retourne les budgets d'une agence.
     *
     * @param codeAgence le code agence
     * @return liste des DTOs
     */
    public List<BudgetDTO.Response> findByAgence(String codeAgence) {
        return budgetRepository.findByAgence_CodeAgence(codeAgence)
                .stream()
                .map(BudgetDTO.Response::from)
                .toList();
    }

    // ── Écriture ──────────────────────────────────────────────────────────────

    /**
     * Crée un nouveau budget au statut BROUILLON.
     *
     * @param req le payload de création
     * @return le DTO du budget créé
     */
    @Transactional
    public BudgetDTO.Response create(BudgetDTO.CreateRequest req) {
        Budget b = new Budget();
        b.setExerciceFiscal(req.exerciceFiscal());
        b.setDateCreation(req.dateCreation());
        b.setUtilisateur(req.utilisateur());
        b.setStatut(StatutBudget.BROUILLON);

        if (req.codeAgence() != null && !req.codeAgence().isBlank()) {
            b.setAgence(agenceRepository.getReferenceById(req.codeAgence()));
        }

        Budget saved = budgetRepository.save(b);
        return BudgetDTO.Response.from(saved);
    }

    /**
     * Met à jour les champs modifiables d'un budget.
     *
     * @param id  identifiant du budget
     * @param req le payload de mise à jour
     * @return le DTO mis à jour
     * @throws ResourceNotFoundException si introuvable
     */
    @Transactional
    public BudgetDTO.Response update(Long id, BudgetDTO.UpdateRequest req) {
        Budget b = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget", id));

        if (req.statut() != null && !req.statut().isBlank()) {
            b.setStatut(StatutBudget.valueOf(req.statut()));
        }
        if (req.montantTotalRecettes() != null) {
            b.setMontantTotalRecettes(req.montantTotalRecettes());
        }
        if (req.montantTotalDepenses() != null) {
            b.setMontantTotalDepenses(req.montantTotalDepenses());
        }
        if (req.utilisateur() != null) {
            b.setUtilisateur(req.utilisateur());
        }

        Budget saved = budgetRepository.save(b);
        return BudgetDTO.Response.from(saved);
    }

    /**
     * Valide un budget (BROUILLON → VALIDE).
     *
     * @param id identifiant du budget
     * @return le DTO mis à jour
     * @throws ResourceNotFoundException si introuvable
     * @throws BusinessException         si le statut n'est pas BROUILLON
     */
    @Transactional
    public BudgetDTO.Response valider(Long id) {
        Budget b = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget", id));

        if (b.getStatut() != StatutBudget.BROUILLON) {
            throw new BusinessException(
                    "Le budget " + id + " ne peut être validé : statut actuel = " + b.getStatut()
                    + " (attendu : BROUILLON)");
        }

        b.setStatut(StatutBudget.VALIDE);
        b.setDateValidation(LocalDate.now());

        Budget saved = budgetRepository.save(b);
        return BudgetDTO.Response.from(saved);
    }

    /**
     * Clôture un budget (VALIDE → CLOTURE).
     *
     * @param id identifiant du budget
     * @return le DTO mis à jour
     * @throws ResourceNotFoundException si introuvable
     * @throws BusinessException         si le statut n'est pas VALIDE
     * @throws ClotureException          si le budget est déjà clôturé
     */
    @Transactional
    public BudgetDTO.Response cloturer(Long id) {
        Budget b = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget", id));

        if (b.getStatut() == StatutBudget.CLOTURE) {
            throw new ClotureException("Le budget " + id + " est déjà clôturé.");
        }

        if (b.getStatut() != StatutBudget.VALIDE) {
            throw new BusinessException(
                    "Le budget " + id + " ne peut être clôturé : statut actuel = " + b.getStatut()
                    + " (attendu : VALIDE)");
        }

        b.setStatut(StatutBudget.CLOTURE);

        Budget saved = budgetRepository.save(b);
        return BudgetDTO.Response.from(saved);
    }
}
