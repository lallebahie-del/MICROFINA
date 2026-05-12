package com.pfe.backend.service;

import com.microfina.entity.OperationBanque;
import com.microfina.entity.StatutOperationBanque;
import com.pfe.backend.dto.OperationBanqueDTO;
import com.pfe.backend.exception.BusinessException;
import com.pfe.backend.exception.ResourceNotFoundException;
import com.pfe.backend.repository.AgenceRepository;
import com.pfe.backend.repository.ComptabiliteRepository;
import com.pfe.backend.repository.CompteBanqueRepository;
import com.pfe.backend.repository.OperationBanqueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * OperationBanqueApplicationService – logique métier pour les opérations bancaires.
 *
 * <p>Nommé {@code OperationBanqueApplicationService} pour éviter tout conflit
 * avec un éventuel {@code OperationBanqueService} existant dans le module domaine.</p>
 *
 * <p>Cycle de vie : EN_ATTENTE → VALIDE ou ANNULE.</p>
 */
@Service
@Transactional(readOnly = true)
public class OperationBanqueApplicationService {

    private final OperationBanqueRepository operationBanqueRepository;
    private final CompteBanqueRepository    compteBanqueRepository;
    private final AgenceRepository          agenceRepository;
    private final ComptabiliteRepository    comptabiliteRepository;

    public OperationBanqueApplicationService(
            OperationBanqueRepository operationBanqueRepository,
            CompteBanqueRepository    compteBanqueRepository,
            AgenceRepository          agenceRepository,
            ComptabiliteRepository    comptabiliteRepository
    ) {
        this.operationBanqueRepository = operationBanqueRepository;
        this.compteBanqueRepository    = compteBanqueRepository;
        this.agenceRepository          = agenceRepository;
        this.comptabiliteRepository    = comptabiliteRepository;
    }

    // ── Lecture ───────────────────────────────────────────────────────────────

    /**
     * Retourne toutes les opérations bancaires.
     */
    public List<OperationBanqueDTO.Response> findAll() {
        return operationBanqueRepository.findAll()
                .stream()
                .map(OperationBanqueDTO.Response::from)
                .toList();
    }

    /**
     * Retourne une opération bancaire par son identifiant.
     *
     * @param id identifiant technique
     * @return le DTO correspondant
     * @throws ResourceNotFoundException si introuvable
     */
    public OperationBanqueDTO.Response findById(Long id) {
        OperationBanque op = operationBanqueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OperationBanque", id));
        return OperationBanqueDTO.Response.from(op);
    }

    /**
     * Retourne les opérations bancaires d'une agence.
     *
     * @param codeAgence le code agence
     * @return liste des DTOs
     */
    public List<OperationBanqueDTO.Response> findByAgence(String codeAgence) {
        return operationBanqueRepository.findByAgence_CodeAgence(codeAgence)
                .stream()
                .map(OperationBanqueDTO.Response::from)
                .toList();
    }

    /**
     * Retourne les opérations liées à un compte bancaire.
     *
     * @param compteBanqueId l'identifiant du compte bancaire
     * @return liste des DTOs
     */
    public List<OperationBanqueDTO.Response> findByCompteBanque(Long compteBanqueId) {
        return operationBanqueRepository.findByCompteBanque_Id(compteBanqueId)
                .stream()
                .map(OperationBanqueDTO.Response::from)
                .toList();
    }

    // ── Écriture ──────────────────────────────────────────────────────────────

    /**
     * Crée une nouvelle opération bancaire.
     *
     * @param req le payload de création
     * @return le DTO de l'opération créée
     */
    @Transactional
    public OperationBanqueDTO.Response create(OperationBanqueDTO.CreateRequest req) {
        OperationBanque op = new OperationBanque();
        op.setDateOperation(req.dateOperation());
        op.setMontant(req.montant());
        op.setUtilisateur(req.utilisateur());
        op.setStatut(StatutOperationBanque.EN_ATTENTE);

        // FK optionnel – compte bancaire
        if (req.compteBanqueId() != null) {
            op.setCompteBanque(compteBanqueRepository.getReferenceById(req.compteBanqueId()));
        }

        // FK agence (nullable)
        if (req.codeAgence() != null && !req.codeAgence().isBlank()) {
            op.setAgence(agenceRepository.getReferenceById(req.codeAgence()));
        }

        // FK comptabilité — use provided id or auto-create a minimal entry
        if (req.idComptabilite() != null) {
            op.setComptabilite(comptabiliteRepository.getReferenceById(req.idComptabilite()));
        } else {
            com.microfina.entity.Comptabilite compta = new com.microfina.entity.Comptabilite();
            compta.setDateOperation(req.dateOperation());
            compta.setLibelle(req.utilisateur());
            compta = comptabiliteRepository.save(compta);
            op.setComptabilite(compta);
        }

        OperationBanque saved = operationBanqueRepository.save(op);
        return OperationBanqueDTO.Response.from(saved);
    }

    /**
     * Valide une opération bancaire en attente.
     *
     * @param id identifiant de l'opération
     * @return le DTO mis à jour
     * @throws ResourceNotFoundException si introuvable
     * @throws BusinessException         si le statut n'est pas EN_ATTENTE
     */
    @Transactional
    public OperationBanqueDTO.Response valider(Long id) {
        OperationBanque op = operationBanqueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OperationBanque", id));

        if (op.getStatut() != StatutOperationBanque.EN_ATTENTE) {
            throw new BusinessException(
                    "L'opération bancaire " + id + " ne peut être validée : statut actuel = "
                    + op.getStatut());
        }

        op.setStatut(StatutOperationBanque.VALIDE);
        OperationBanque saved = operationBanqueRepository.save(op);
        return OperationBanqueDTO.Response.from(saved);
    }

    /**
     * Annule une opération bancaire.
     *
     * @param id identifiant de l'opération
     * @return le DTO mis à jour
     * @throws ResourceNotFoundException si introuvable
     * @throws BusinessException         si l'opération est déjà validée
     */
    @Transactional
    public OperationBanqueDTO.Response annuler(Long id) {
        OperationBanque op = operationBanqueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OperationBanque", id));

        if (op.getStatut() == StatutOperationBanque.VALIDE) {
            throw new BusinessException(
                    "L'opération bancaire " + id + " est déjà validée et ne peut être annulée.");
        }

        op.setStatut(StatutOperationBanque.ANNULE);
        OperationBanque saved = operationBanqueRepository.save(op);
        return OperationBanqueDTO.Response.from(saved);
    }
}
