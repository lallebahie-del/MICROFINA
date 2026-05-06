package com.pfe.backend.service;

import com.microfina.entity.ProduitPartSociale;
import com.pfe.backend.exception.ResourceNotFoundException;
import com.pfe.backend.repository.ProduitPartSocialeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ProduitPartSocialeService {

    private final ProduitPartSocialeRepository repo;

    public ProduitPartSocialeService(ProduitPartSocialeRepository repo) {
        this.repo = repo;
    }

    public List<ProduitPartSociale> findAll() {
        return repo.findAll();
    }

    public List<ProduitPartSociale> findActifs() {
        return repo.findByActif(1);
    }

    public Optional<ProduitPartSociale> findById(String id) {
        return repo.findById(id);
    }

    @Transactional
    public ProduitPartSociale save(ProduitPartSociale produit) {
        return repo.save(produit);
    }

    @Transactional
    public ProduitPartSociale update(String id, ProduitPartSociale req) {
        ProduitPartSociale existing = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProduitPartSociale", id));
        existing.setNomProduit(req.getNomProduit());
        existing.setDescription(req.getDescription());
        existing.setActif(req.getActif());
        existing.setValeurNominale(req.getValeurNominale());
        existing.setNombrePartsMin(req.getNombrePartsMin());
        existing.setNombrePartsMax(req.getNombrePartsMax());
        existing.setTauxDividende(req.getTauxDividende());
        existing.setPeriodiciteDividende(req.getPeriodiciteDividende());
        existing.setCompteCapitalSocial(req.getCompteCapitalSocial());
        existing.setCompteDividende(req.getCompteDividende());
        existing.setCompteReserve(req.getCompteReserve());
        return repo.save(existing);
    }

    @Transactional
    public boolean delete(String id) {
        if (!repo.existsById(id)) return false;
        repo.deleteById(id);
        return true;
    }
}
