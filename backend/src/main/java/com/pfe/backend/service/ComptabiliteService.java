package com.pfe.backend.service;

import com.pfe.backend.dto.ComptabiliteDTO;
import com.pfe.backend.exception.ResourceNotFoundException;
import com.pfe.backend.repository.ComptabiliteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import java.util.List;

/**
 * ComptabiliteService – accès en lecture seule aux écritures comptables.
 *
 * <p>La comptabilité est peuplée par les modules fonctionnels (caisse, banque, budget).
 * Ce service expose uniquement des opérations de lecture.</p>
 */
@Service
@Transactional(readOnly = true)
public class ComptabiliteService {

    private final ComptabiliteRepository comptabiliteRepository;

    public ComptabiliteService(ComptabiliteRepository comptabiliteRepository) {
        this.comptabiliteRepository = comptabiliteRepository;
    }

    // ── Lecture ───────────────────────────────────────────────────────────────

    /**
     * Retourne toutes les écritures comptables.
     */
    public List<ComptabiliteDTO.Response> findAll() {
        return comptabiliteRepository.findAll()
                .stream()
                .map(ComptabiliteDTO.Response::from)
                .toList();
    }

    /**
     * Retourne une écriture comptable par son identifiant.
     *
     * @param id l'identifiant de l'écriture
     * @return le DTO correspondant
     * @throws ResourceNotFoundException si introuvable
     */
    public ComptabiliteDTO.Response findById(Long id) {
        return comptabiliteRepository.findById(id)
                .map(ComptabiliteDTO.Response::from)
                .orElseThrow(() -> new ResourceNotFoundException("Comptabilite", id));
    }

    /**
     * Retourne les écritures comptables d'une agence.
     *
     * @param codeAgence le code de l'agence
     * @return liste des DTOs
     */
    public List<ComptabiliteDTO.Response> findByAgence(String codeAgence) {
        return comptabiliteRepository.findByCodeAgence(codeAgence)
                .stream()
                .map(ComptabiliteDTO.Response::from)
                .toList();
    }

    // ── Lettrage ──────────────────────────────────────────────────────────────

    /**
     * Lettre une écriture comptable.
     *
     * <p>Met à jour le champ {@code LETTRE} et le champ {@code DATELETTRAGE}
     * (date du jour serveur).</p>
     *
     * @param id            identifiant de l'écriture
     * @param codeLettrage  code alphanumérique de lettrage (max 20 chars)
     * @return le DTO mis à jour
     * @throws ResourceNotFoundException si l'écriture est introuvable
     */
    @Transactional
    public ComptabiliteDTO.Response lettrer(Long id, String codeLettrage) {
        var ecriture = comptabiliteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comptabilite", id));
        ecriture.setLettre(codeLettrage);
        ecriture.setDateLettrage(LocalDate.now());
        return ComptabiliteDTO.Response.from(comptabiliteRepository.save(ecriture));
    }
}
