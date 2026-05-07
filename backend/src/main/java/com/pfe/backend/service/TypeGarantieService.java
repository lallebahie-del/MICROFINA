package com.pfe.backend.service;

import com.microfina.entity.TypeGarantie;
import com.pfe.backend.exception.ResourceNotFoundException;
import com.pfe.backend.repository.TypeGarantieRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TypeGarantieService {

    private final TypeGarantieRepository repo;

    public TypeGarantieService(TypeGarantieRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public TypeGarantie create(TypeGarantie input) {
        if (input.getCode() == null || input.getCode().isBlank()) {
            throw new IllegalArgumentException("code est obligatoire");
        }
        if (repo.existsById(input.getCode())) {
            throw new IllegalStateException("Type de garantie déjà existant : " + input.getCode());
        }
        if (input.getActif() == null) input.setActif(Boolean.TRUE);
        return repo.save(input);
    }

    @Transactional
    public TypeGarantie update(String code, TypeGarantie input) {
        TypeGarantie existing = repo.findById(code)
            .orElseThrow(() -> new ResourceNotFoundException("TypeGarantie", code));

        if (input.getLibelle() != null)     existing.setLibelle(input.getLibelle());
        if (input.getDescription() != null) existing.setDescription(input.getDescription());
        if (input.getActif() != null)       existing.setActif(input.getActif());

        return repo.save(existing);
    }

    @Transactional
    public void delete(String code) {
        if (!repo.existsById(code)) {
            throw new ResourceNotFoundException("TypeGarantie", code);
        }
        try {
            repo.deleteById(code);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException(
                "Suppression impossible : le type de garantie " + code + " est utilisé. Désactivez-le (actif=false) plutôt que de le supprimer.");
        }
    }
}
