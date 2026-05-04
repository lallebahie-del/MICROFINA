package com.pfe.backend.service;

import com.microfina.entity.FamilleProduitCredit;
import com.microfina.entity.ModeDeCalculInteret;
import com.microfina.entity.ProduitCredit;
import com.pfe.backend.dto.ProduitCreditDTO;
import com.pfe.backend.exception.ResourceNotFoundException;
import com.pfe.backend.repository.ProduitCreditRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ProduitCreditService {

    private final ProduitCreditRepository repo;

    @PersistenceContext
    private EntityManager em;

    public ProduitCreditService(ProduitCreditRepository repo) {
        this.repo = repo;
    }

    /** All active products (for dropdown lists in credit-creation forms). */
    public List<ProduitCreditDTO> listActifs() {
        return repo.findByActif(1).stream().map(ProduitCreditDTO::from).toList();
    }

    /** Paginated search. */
    public Map<String, Object> search(String search, Integer actif, int page, int size) {
        Page<ProduitCredit> result = repo.search(
            search, actif,
            PageRequest.of(page, size, Sort.by("nomProduit").ascending())
        );
        return Map.of(
            "content",       result.getContent().stream().map(ProduitCreditDTO::from).toList(),
            "totalElements", result.getTotalElements(),
            "totalPages",    result.getTotalPages(),
            "page",          result.getNumber(),
            "size",          result.getSize()
        );
    }

    public Optional<ProduitCreditDTO> findById(String numProduit) {
        return repo.findById(numProduit).map(ProduitCreditDTO::from);
    }

    @Transactional
    public ProduitCreditDTO save(ProduitCredit produit) {
        return ProduitCreditDTO.from(repo.save(produit));
    }

    /**
     * Create a product from a validated DTO request (Phase 11.1).
     */
    @Transactional
    public ProduitCreditDTO create(ProduitCreditDTO.CreateRequest req) {
        ProduitCredit p = new ProduitCredit();
        p.setNumProduit(req.numProduit());
        applyCommon(p, req.nomProduit(), req.description(), req.actif(), req.typeCredit(),
                req.typeClient(), req.montantMin(), req.montantMax(), req.dureeMin(), req.dureeMax(),
                req.tauxInteret(), req.tauxInteretMin(), req.tauxInteretMax(),
                req.tauxPenalite(), req.tauxCommission(), req.tauxAssurance(),
                req.periodiciteRemboursement(), req.nombreEcheance(), req.delaiGrace(),
                req.typeGrace(), req.garantieRequise(), req.autoriserReneg(),
                req.autoriserRemboursementAnticipe(), req.decaissementNet(),
                req.codeFamilleProduit(), req.codeModeCalcul());
        return save(p);
    }

    /**
     * Update a product from a validated DTO request (Phase 11.1).
     */
    @Transactional
    public ProduitCreditDTO update(String numProduit, ProduitCreditDTO.UpdateRequest req) {
        ProduitCredit p = repo.findById(numProduit)
                .orElseThrow(() -> new ResourceNotFoundException("ProduitCredit", numProduit));
        applyCommon(p, req.nomProduit(), req.description(), req.actif(), req.typeCredit(),
                req.typeClient(), req.montantMin(), req.montantMax(), req.dureeMin(), req.dureeMax(),
                req.tauxInteret(), req.tauxInteretMin(), req.tauxInteretMax(),
                req.tauxPenalite(), req.tauxCommission(), req.tauxAssurance(),
                req.periodiciteRemboursement(), req.nombreEcheance(), req.delaiGrace(),
                req.typeGrace(), req.garantieRequise(), req.autoriserReneg(),
                req.autoriserRemboursementAnticipe(), req.decaissementNet(),
                req.codeFamilleProduit(), req.codeModeCalcul());
        return save(p);
    }

    // ── Helper interne ────────────────────────────────────────────────────────

    @SuppressWarnings("java:S107")
    private void applyCommon(ProduitCredit p,
            String nomProduit, String description, Integer actif,
            String typeCredit, String typeClient,
            java.math.BigDecimal montantMin, java.math.BigDecimal montantMax,
            Integer dureeMin, Integer dureeMax,
            java.math.BigDecimal tauxInteret, java.math.BigDecimal tauxInteretMin,
            java.math.BigDecimal tauxInteretMax, java.math.BigDecimal tauxPenalite,
            java.math.BigDecimal tauxCommission, java.math.BigDecimal tauxAssurance,
            String periodiciteRemboursement, Integer nombreEcheance, Integer delaiGrace,
            String typeGrace, Integer garantieRequise, Integer autoriserReneg,
            Integer autoriserRemboursementAnticipe, Integer decaissementNet,
            String codeFamilleProduit, Integer codeModeCalcul) {

        p.setNomProduit(nomProduit);
        p.setDescription(description);
        if (actif != null)                      p.setActif(actif);
        p.setTypeCredit(typeCredit);
        p.setTypeClient(typeClient);
        p.setMontantMin(montantMin);
        p.setMontantMax(montantMax);
        p.setDureeMin(dureeMin);
        p.setDureeMax(dureeMax);
        p.setTauxInteret(tauxInteret);
        p.setTauxInteretMin(tauxInteretMin);
        p.setTauxInteretMax(tauxInteretMax);
        p.setTauxPenalite(tauxPenalite);
        p.setTauxCommission(tauxCommission);
        p.setTauxAssurance(tauxAssurance);
        p.setPeriodiciteRemboursement(periodiciteRemboursement);
        p.setNombreEcheance(nombreEcheance);
        p.setDelaiGrace(delaiGrace);
        p.setTypeGrace(typeGrace);
        p.setGarantieRequise(garantieRequise);
        p.setAutoriserReneg(autoriserReneg);
        p.setAutoriserRemboursementAnticipe(autoriserRemboursementAnticipe);
        p.setDecaissementNet(decaissementNet);
        if (codeFamilleProduit != null)
            p.setFamilleProduitCredit(em.getReference(FamilleProduitCredit.class, codeFamilleProduit));
        if (codeModeCalcul != null)
            p.setModeDeCalculInteret(em.getReference(ModeDeCalculInteret.class, codeModeCalcul));
    }

    @Transactional
    public boolean delete(String numProduit) {
        if (!repo.existsById(numProduit)) return false;
        repo.deleteById(numProduit);
        return true;
    }
}
