package com.pfe.backend.service;

import com.microfina.entity.ModePaiementCaisse;
import com.microfina.entity.OperationCaisse;
import com.microfina.entity.StatutOperationCaisse;
import com.pfe.backend.dto.OperationCaisseDTO;
import com.pfe.backend.exception.BusinessException;
import com.pfe.backend.exception.ResourceNotFoundException;
import com.pfe.backend.repository.AgenceRepository;
import com.pfe.backend.repository.ComptabiliteRepository;
import com.pfe.backend.repository.CompteEpsRepository;
import com.pfe.backend.repository.OperationCaisseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * OperationCaisseService – logique métier pour les opérations de caisse.
 *
 * <p>Cycle de vie : EN_ATTENTE → VALIDE ou ANNULE.</p>
 */
@Service
@Transactional(readOnly = true)
public class OperationCaisseService {

    private final OperationCaisseRepository operationCaisseRepository;
    private final CompteEpsRepository       compteEpsRepository;
    private final AgenceRepository          agenceRepository;
    private final ComptabiliteRepository    comptabiliteRepository;

    public OperationCaisseService(
            OperationCaisseRepository operationCaisseRepository,
            CompteEpsRepository       compteEpsRepository,
            AgenceRepository          agenceRepository,
            ComptabiliteRepository    comptabiliteRepository
    ) {
        this.operationCaisseRepository = operationCaisseRepository;
        this.compteEpsRepository       = compteEpsRepository;
        this.agenceRepository          = agenceRepository;
        this.comptabiliteRepository    = comptabiliteRepository;
    }

    // ── Lecture ───────────────────────────────────────────────────────────────

    /**
     * Retourne toutes les opérations de caisse.
     */
    public List<OperationCaisseDTO.Response> findAll() {
        return operationCaisseRepository.findAll()
                .stream()
                .map(OperationCaisseDTO.Response::from)
                .toList();
    }

    /**
     * Retourne une opération par son identifiant.
     *
     * @param id identifiant technique
     * @return le DTO correspondant
     * @throws ResourceNotFoundException si introuvable
     */
    public OperationCaisseDTO.Response findById(Long id) {
        OperationCaisse op = operationCaisseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OperationCaisse", id));
        return OperationCaisseDTO.Response.from(op);
    }

    /**
     * Retourne les opérations d'une agence.
     *
     * @param codeAgence le code agence
     * @return liste des DTOs
     */
    public List<OperationCaisseDTO.Response> findByAgence(String codeAgence) {
        return operationCaisseRepository.findByAgence_CodeAgence(codeAgence)
                .stream()
                .map(OperationCaisseDTO.Response::from)
                .toList();
    }

    /**
     * Retourne les opérations d'un compte épargne.
     *
     * @param numCompte le numéro de compte
     * @return liste des DTOs
     */
    public List<OperationCaisseDTO.Response> findByCompte(String numCompte) {
        return operationCaisseRepository.findByCompteEps_NumCompte(numCompte)
                .stream()
                .map(OperationCaisseDTO.Response::from)
                .toList();
    }

    // ── Écriture ──────────────────────────────────────────────────────────────

    /**
     * Crée une nouvelle opération de caisse.
     *
     * @param req le payload de création
     * @return le DTO de l'opération créée
     */
    @Transactional
    public OperationCaisseDTO.Response create(OperationCaisseDTO.CreateRequest req) {
        OperationCaisse op = new OperationCaisse();
        op.setNumPiece(req.numPiece());
        op.setDateOperation(req.dateOperation());
        op.setMontant(req.montant());

        if (req.modePaiement() != null && !req.modePaiement().isBlank()) {
            op.setModePaiement(ModePaiementCaisse.valueOf(req.modePaiement()));
        }
        op.setMotif(req.motif());
        op.setUtilisateur(req.utilisateur());
        op.setStatut(StatutOperationCaisse.EN_ATTENTE);

        // FK optionnel – compte épargne
        if (req.numCompte() != null && !req.numCompte().isBlank()) {
            op.setCompteEps(compteEpsRepository.getReferenceById(req.numCompte()));
        }

        // FK agence (nullable)
        if (req.codeAgence() != null && !req.codeAgence().isBlank()) {
            op.setAgence(agenceRepository.getReferenceById(req.codeAgence()));
        }

        // FK comptabilité (obligatoire)
        op.setComptabilite(comptabiliteRepository.getReferenceById(req.idComptabilite()));

        OperationCaisse saved = operationCaisseRepository.save(op);
        return OperationCaisseDTO.Response.from(saved);
    }

    /**
     * Valide une opération en attente.
     *
     * @param id identifiant de l'opération
     * @return le DTO mis à jour
     * @throws ResourceNotFoundException si introuvable
     * @throws BusinessException         si le statut n'est pas EN_ATTENTE
     */
    @Transactional
    public OperationCaisseDTO.Response valider(Long id) {
        OperationCaisse op = operationCaisseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OperationCaisse", id));

        if (op.getStatut() != StatutOperationCaisse.EN_ATTENTE) {
            throw new BusinessException(
                    "L'opération " + id + " ne peut être validée : statut actuel = " + op.getStatut());
        }

        op.setStatut(StatutOperationCaisse.VALIDE);
        OperationCaisse saved = operationCaisseRepository.save(op);
        return OperationCaisseDTO.Response.from(saved);
    }

    /**
     * Annule une opération en attente.
     *
     * @param id identifiant de l'opération
     * @return le DTO mis à jour
     * @throws ResourceNotFoundException si introuvable
     * @throws BusinessException         si l'opération est déjà validée
     */
    @Transactional
    public OperationCaisseDTO.Response annuler(Long id) {
        OperationCaisse op = operationCaisseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OperationCaisse", id));

        if (op.getStatut() == StatutOperationCaisse.VALIDE) {
            throw new BusinessException(
                    "L'opération " + id + " est déjà validée et ne peut être annulée.");
        }

        op.setStatut(StatutOperationCaisse.ANNULE);
        OperationCaisse saved = operationCaisseRepository.save(op);
        return OperationCaisseDTO.Response.from(saved);
    }
}
