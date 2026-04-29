package com.pfe.backend.service;

import com.microfina.entity.Banque;
import com.pfe.backend.dto.BanqueDTO;
import com.pfe.backend.exception.BusinessException;
import com.pfe.backend.exception.ResourceNotFoundException;
import com.pfe.backend.repository.BanqueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * BanqueService — logique métier pour le référentiel bancaire.
 *
 * <p>Toutes les méthodes de lecture sont exécutées dans une transaction
 * en lecture seule. Les opérations d'écriture ouvrent une transaction
 * en lecture-écriture.</p>
 */
@Service
@Transactional(readOnly = true)
public class BanqueService {

    private final BanqueRepository banqueRepository;

    public BanqueService(BanqueRepository banqueRepository) {
        this.banqueRepository = banqueRepository;
    }

    // ── Lectures ──────────────────────────────────────────────────────

    /**
     * Retourne toutes les banques (actives et inactives).
     */
    public List<BanqueDTO.Response> findAll() {
        return banqueRepository.findAll()
                .stream()
                .map(BanqueDTO.Response::from)
                .toList();
    }

    /**
     * Retourne uniquement les banques dont le flag {@code actif} est {@code true}.
     */
    public List<BanqueDTO.Response> findActives() {
        return banqueRepository.findByActifTrue()
                .stream()
                .map(BanqueDTO.Response::from)
                .toList();
    }

    /**
     * Retourne la banque identifiée par {@code codeBanque}.
     *
     * @throws ResourceNotFoundException si aucune banque ne correspond
     */
    public BanqueDTO.Response findById(String codeBanque) {
        Banque banque = banqueRepository.findById(codeBanque)
                .orElseThrow(() -> new ResourceNotFoundException("Banque", codeBanque));
        return BanqueDTO.Response.from(banque);
    }

    // ── Écritures ─────────────────────────────────────────────────────

    /**
     * Crée une nouvelle banque.
     *
     * @throws BusinessException si un enregistrement avec le même {@code codeBanque} existe déjà
     */
    @Transactional
    public BanqueDTO.Response create(BanqueDTO.CreateRequest req) {
        if (banqueRepository.existsById(req.codeBanque())) {
            throw new BusinessException(
                "BANQUE_CODE_DUPLICATE",
                "Une banque avec le code '" + req.codeBanque() + "' existe déjà."
            );
        }

        Banque banque = new Banque();
        banque.setCodeBanque(req.codeBanque());
        banque.setNom(req.nom());
        banque.setSwiftBic(req.swiftBic());
        banque.setAdresse(req.adresse());
        banque.setPays(req.pays());
        banque.setActif(req.actif() != null ? req.actif() : Boolean.TRUE);

        return BanqueDTO.Response.from(banqueRepository.save(banque));
    }

    /**
     * Met à jour les champs modifiables d'une banque existante.
     *
     * @throws ResourceNotFoundException si aucune banque ne correspond
     */
    @Transactional
    public BanqueDTO.Response update(String codeBanque, BanqueDTO.UpdateRequest req) {
        Banque banque = banqueRepository.findById(codeBanque)
                .orElseThrow(() -> new ResourceNotFoundException("Banque", codeBanque));

        banque.setNom(req.nom());
        banque.setSwiftBic(req.swiftBic());
        banque.setAdresse(req.adresse());
        banque.setPays(req.pays());
        if (req.actif() != null) {
            banque.setActif(req.actif());
        }

        return BanqueDTO.Response.from(banqueRepository.save(banque));
    }

    /**
     * Supprime définitivement une banque.
     *
     * @throws ResourceNotFoundException si aucune banque ne correspond
     */
    @Transactional
    public void delete(String codeBanque) {
        if (!banqueRepository.existsById(codeBanque)) {
            throw new ResourceNotFoundException("Banque", codeBanque);
        }
        banqueRepository.deleteById(codeBanque);
    }
}
