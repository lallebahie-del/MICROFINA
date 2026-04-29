package com.pfe.backend.service;

import com.microfina.entity.ActionAudit;
import com.microfina.entity.JournalAudit;
import com.pfe.backend.dto.JournalAuditDTO;
import com.pfe.backend.exception.ResourceNotFoundException;
import com.pfe.backend.repository.JournalAuditRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * JournalAuditService — consultation du journal d'audit (lecture seule).
 */
@Service
@Transactional(readOnly = true)
public class JournalAuditService {

    private final JournalAuditRepository journalAuditRepository;

    public JournalAuditService(JournalAuditRepository journalAuditRepository) {
        this.journalAuditRepository = journalAuditRepository;
    }

    /**
     * Retourne toutes les entrées du journal d'audit.
     */
    public List<JournalAuditDTO.Response> findAll() {
        return journalAuditRepository.findAll()
            .stream()
            .map(JournalAuditDTO.Response::from)
            .toList();
    }

    /**
     * Retourne une entrée du journal par son identifiant technique.
     *
     * @param id identifiant technique
     * @return DTO Response
     * @throws ResourceNotFoundException si introuvable
     */
    public JournalAuditDTO.Response findById(Long id) {
        JournalAudit j = journalAuditRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("JournalAudit", id));
        return JournalAuditDTO.Response.from(j);
    }

    /**
     * Retourne les entrées du journal pour un utilisateur donné.
     *
     * @param utilisateur login de l'utilisateur
     * @return liste de DTOs Response
     */
    public List<JournalAuditDTO.Response> findByUtilisateur(String utilisateur) {
        return journalAuditRepository.findByUtilisateur(utilisateur)
            .stream()
            .map(JournalAuditDTO.Response::from)
            .toList();
    }

    /**
     * Retourne les entrées du journal pour une entité donnée.
     *
     * @param entite nom simple de la classe entité (ex : "Credits")
     * @return liste de DTOs Response
     */
    public List<JournalAuditDTO.Response> findByEntite(String entite) {
        return journalAuditRepository.findByEntite(entite)
            .stream()
            .map(JournalAuditDTO.Response::from)
            .toList();
    }

    /**
     * Retourne les entrées du journal pour un type d'action donné.
     *
     * @param actionStr code de l'action (CREATE, UPDATE, DELETE, LOGIN, LOGOUT)
     * @return liste de DTOs Response
     * @throws com.pfe.backend.exception.BusinessException si le code action est invalide
     */
    public List<JournalAuditDTO.Response> findByAction(String actionStr) {
        ActionAudit action;
        try {
            action = ActionAudit.valueOf(actionStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new com.pfe.backend.exception.BusinessException(
                "Type d'action inconnu : " + actionStr
                    + ". Valeurs acceptées : CREATE, UPDATE, DELETE, LOGIN, LOGOUT"
            );
        }
        return journalAuditRepository.findByAction(action)
            .stream()
            .map(JournalAuditDTO.Response::from)
            .toList();
    }
}
