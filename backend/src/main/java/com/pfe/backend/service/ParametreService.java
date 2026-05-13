package com.pfe.backend.service;

import com.microfina.entity.Agence;
import com.microfina.entity.Parametre;
import com.pfe.backend.dto.ParametreDTO;
import com.pfe.backend.exception.ResourceNotFoundException;
import com.pfe.backend.repository.AgenceRepository;
import com.pfe.backend.repository.ParametreRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * ParametreService — gestion des paramètres de configuration par agence.
 */
@Service
@Transactional(readOnly = true)
public class ParametreService {

    private final ParametreRepository parametreRepository;
    private final AgenceRepository agenceRepository;

    public ParametreService(ParametreRepository parametreRepository,
                             AgenceRepository agenceRepository) {
        this.parametreRepository = parametreRepository;
        this.agenceRepository = agenceRepository;
    }

    /**
     * Retourne la liste de tous les paramètres.
     */
    public List<ParametreDTO.Response> findAll() {
        return parametreRepository.findAll()
            .stream()
            .map(ParametreDTO.Response::from)
            .toList();
    }

    /**
     * Retourne un paramètre par son identifiant technique.
     *
     * @param id identifiant technique
     * @return DTO Response
     * @throws ResourceNotFoundException si introuvable
     */
    public ParametreDTO.Response findById(Long id) {
        Parametre p = parametreRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Parametre", id));
        return ParametreDTO.Response.from(p);
    }

    /**
     * Retourne le paramètre rattaché à une agence donnée.
     *
     * @param codeAgence code de l'agence
     * @return DTO Response
     * @throws ResourceNotFoundException si aucun paramètre pour cette agence
     */
    @Cacheable(value = "parametres", key = "#codeAgence ?: 'ALL'")
    public ParametreDTO.Response findByAgence(String codeAgence) {
        Parametre p = parametreRepository.findByAgence_CodeAgence(codeAgence)
            .orElseThrow(() -> new ResourceNotFoundException("Parametre pour agence", codeAgence));
        return ParametreDTO.Response.from(p);
    }

    /**
     * Crée un nouveau paramètre.
     *
     * @param req données de création
     * @return DTO Response du paramètre créé
     */
    @Transactional
    @CacheEvict(value = "parametres", allEntries = true)
    public ParametreDTO.Response create(ParametreDTO.CreateRequest req) {
        Parametre p = new Parametre();
        p.setMaxiJourOuvert(req.maxiJourOuvert());
        p.setPrefixe(req.prefixe());
        p.setSuffixe(req.suffixe());
        p.setUseMultidevise(req.useMultidevise());

        if (req.codeAgence() != null && !req.codeAgence().isBlank()) {
            Agence agence = agenceRepository.findById(req.codeAgence())
                .orElseThrow(() -> new ResourceNotFoundException("Agence", req.codeAgence()));
            p.setAgence(agence);
        }

        return ParametreDTO.Response.from(parametreRepository.save(p));
    }

    /**
     * Met à jour partiellement un paramètre (patch — seuls les champs non-null sont appliqués).
     *
     * @param id  identifiant technique
     * @param req champs à mettre à jour
     * @return DTO Response mis à jour
     * @throws ResourceNotFoundException si le paramètre est introuvable
     */
    @Transactional
    @CacheEvict(value = "parametres", allEntries = true)
    public ParametreDTO.Response update(Long id, ParametreDTO.UpdateRequest req) {
        Parametre p = parametreRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Parametre", id));

        if (req.maxiJourOuvert() != null)  p.setMaxiJourOuvert(req.maxiJourOuvert());
        if (req.prefixe() != null)         p.setPrefixe(req.prefixe());
        if (req.suffixe() != null)         p.setSuffixe(req.suffixe());
        if (req.useMultidevise() != null)  p.setUseMultidevise(req.useMultidevise());

        if (req.codeAgence() != null) {
            if (req.codeAgence().isBlank()) {
                p.setAgence(null);
            } else {
                Agence agence = agenceRepository.findById(req.codeAgence())
                    .orElseThrow(() -> new ResourceNotFoundException("Agence", req.codeAgence()));
                p.setAgence(agence);
            }
        }

        return ParametreDTO.Response.from(parametreRepository.save(p));
    }

    /**
     * Supprime un paramètre par son identifiant.
     */
    @Transactional
    @CacheEvict(value = "parametres", allEntries = true)
    public void delete(Long id) {
        if (!parametreRepository.existsById(id)) {
            throw new ResourceNotFoundException("Parametre", id);
        }
        parametreRepository.deleteById(id);
    }
}
