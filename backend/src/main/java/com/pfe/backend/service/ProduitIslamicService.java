package com.pfe.backend.service;

import com.microfina.entity.ProduitIslamic;
import com.pfe.backend.exception.ResourceNotFoundException;
import com.pfe.backend.repository.ProduitIslamicRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProduitIslamicService {

    private final ProduitIslamicRepository repo;

    public ProduitIslamicService(ProduitIslamicRepository repo) {
        this.repo = repo;
    }

    public List<ProduitIslamic> findAll() {
        return repo.findAll();
    }

    public List<ProduitIslamic> findActifs() {
        return repo.findByActif(1);
    }

    public ProduitIslamic findById(String code) {
        return repo.findById(code)
            .orElseThrow(() -> new ResourceNotFoundException("ProduitIslamic", code));
    }

    @Transactional
    public ProduitIslamic create(ProduitIslamic input) {
        if (input.getCodeProduit() == null || input.getCodeProduit().isBlank()) {
            throw new IllegalArgumentException("codeProduit est obligatoire");
        }
        if (repo.existsById(input.getCodeProduit())) {
            throw new IllegalStateException("Produit islamique déjà existant : " + input.getCodeProduit());
        }
        if (input.getActif() == null) input.setActif(1);
        return repo.save(input);
    }

    @Transactional
    public ProduitIslamic update(String code, ProduitIslamic input) {
        ProduitIslamic existing = repo.findById(code)
            .orElseThrow(() -> new ResourceNotFoundException("ProduitIslamic", code));

        if (input.getLibelle() != null)             existing.setLibelle(input.getLibelle());
        if (input.getDescription() != null)         existing.setDescription(input.getDescription());
        if (input.getActif() != null)               existing.setActif(input.getActif());
        if (input.getTauxPartageBenefice() != null) existing.setTauxPartageBenefice(input.getTauxPartageBenefice());
        if (input.getCostPriceRatio() != null)      existing.setCostPriceRatio(input.getCostPriceRatio());
        if (input.getMarkupRatio() != null)         existing.setMarkupRatio(input.getMarkupRatio());
        if (input.getResidualValueRatio() != null)  existing.setResidualValueRatio(input.getResidualValueRatio());

        return repo.save(existing);
    }

    @Transactional
    public void delete(String code) {
        if (!repo.existsById(code)) {
            throw new ResourceNotFoundException("ProduitIslamic", code);
        }
        try {
            repo.deleteById(code);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException(
                "Suppression impossible : le produit islamique " + code + " est utilisé par un produit crédit. Désactivez-le (actif=0) plutôt que de le supprimer.");
        }
    }
}
