package com.pfe.backend.service;

import com.microfina.entity.Agence;
import com.pfe.backend.exception.ResourceNotFoundException;
import com.pfe.backend.repository.AgenceRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AgenceService {

    private final AgenceRepository repo;

    public AgenceService(AgenceRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public Agence create(Agence input) {
        if (input.getCodeAgence() == null || input.getCodeAgence().isBlank()) {
            throw new IllegalArgumentException("codeAgence est obligatoire");
        }
        if (repo.existsById(input.getCodeAgence())) {
            throw new IllegalStateException("Agence déjà existante : " + input.getCodeAgence());
        }
        if (input.getActif() == null) input.setActif(Boolean.TRUE);
        return repo.save(input);
    }

    @Transactional
    public Agence update(String code, Agence input) {
        Agence existing = repo.findById(code)
            .orElseThrow(() -> new ResourceNotFoundException("Agence", code));

        if (input.getNomAgence() != null)         existing.setNomAgence(input.getNomAgence());
        if (input.getNomCourt() != null)          existing.setNomCourt(input.getNomCourt());
        if (input.getActif() != null)             existing.setActif(input.getActif());
        if (input.getIsSiege() != null)           existing.setIsSiege(input.getIsSiege());
        if (input.getChefAgence() != null)        existing.setChefAgence(input.getChefAgence());
        if (input.getNomPrenomChefAgence() != null) existing.setNomPrenomChefAgence(input.getNomPrenomChefAgence());
        if (input.getInstitution() != null)       existing.setInstitution(input.getInstitution());
        if (input.getZoneGeographique() != null)  existing.setZoneGeographique(input.getZoneGeographique());
        if (input.getNumCompte() != null)         existing.setNumCompte(input.getNumCompte());
        if (input.getCompteCaisse() != null)      existing.setCompteCaisse(input.getCompteCaisse());
        if (input.getCompteCrediteur() != null)   existing.setCompteCrediteur(input.getCompteCrediteur());
        if (input.getNumeroSms() != null)         existing.setNumeroSms(input.getNumeroSms());
        if (input.getLongitude() != null)         existing.setLongitude(input.getLongitude());
        if (input.getLatitude() != null)          existing.setLatitude(input.getLatitude());

        return repo.save(existing);
    }

    @Transactional
    public void delete(String code) {
        if (!repo.existsById(code)) {
            throw new ResourceNotFoundException("Agence", code);
        }
        try {
            repo.deleteById(code);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException(
                "Suppression impossible : l'agence " + code + " est référencée par d'autres entités. Désactivez-la (actif=false) plutôt que de la supprimer.");
        }
    }
}
