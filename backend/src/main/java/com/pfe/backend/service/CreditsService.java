package com.pfe.backend.service;

import com.microfina.entity.Amortp;
import com.microfina.entity.Agence;
import com.microfina.entity.Credits;
import com.microfina.entity.CreditStatut;
import com.microfina.entity.Membres;
import com.microfina.entity.ProduitCredit;
import com.microfina.service.AmortissementService;
import com.pfe.backend.dto.CreditDTO;
import com.pfe.backend.exception.ResourceNotFoundException;
import com.pfe.backend.repository.AgenceRepository;
import com.pfe.backend.repository.AmortpRepository;
import com.pfe.backend.repository.CreditsRepository;
import com.pfe.backend.repository.MembresRepository;
import com.pfe.backend.repository.ProduitCreditRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class CreditsService {

    private final CreditsRepository      repo;
    private final MembresRepository      membresRepo;
    private final ProduitCreditRepository produitRepo;
    private final AgenceRepository       agenceRepo;
    private final AmortissementService   amortissementService;
    private final AmortpRepository       amortpRepo;

    public CreditsService(CreditsRepository repo,
                          MembresRepository membresRepo,
                          ProduitCreditRepository produitRepo,
                          AgenceRepository agenceRepo,
                          AmortissementService amortissementService,
                          AmortpRepository amortpRepo) {
        this.repo                = repo;
        this.membresRepo         = membresRepo;
        this.produitRepo         = produitRepo;
        this.agenceRepo          = agenceRepo;
        this.amortissementService = amortissementService;
        this.amortpRepo          = amortpRepo;
    }

    /** Paginated search. statut may be null. */
    public Map<String, Object> search(String search, String statut,
                                      String numMembre, int page, int size) {
        CreditStatut statutEnum = null;
        if (statut != null && !statut.isBlank()) {
            try { statutEnum = CreditStatut.valueOf(statut); }
            catch (IllegalArgumentException ignored) {}
        }

        Page<Credits> result = repo.search(
            search, statutEnum, numMembre,
            PageRequest.of(page, size, Sort.by("idCredit").descending())
        );
        return Map.of(
            "content",       result.getContent().stream().map(CreditDTO::from).toList(),
            "totalElements", result.getTotalElements(),
            "totalPages",    result.getTotalPages(),
            "page",          result.getNumber(),
            "size",          result.getSize()
        );
    }

    public Optional<CreditDTO> findById(Long id) {
        return repo.findById(id).map(CreditDTO::from);
    }

    /** Create a new credit (statut defaults to BROUILLON). */
    @Transactional
    public CreditDTO create(Credits credit) {
        credit.setIdCredit(null);   // ensure insert, not update
        credit.setStatut(CreditStatut.BROUILLON);
        return CreditDTO.from(repo.save(credit));
    }

    /**
     * Create a new credit from a validated DTO request (Phase 11.1).
     * Resolves FK associations via JPA reference proxies.
     */
    @Transactional
    public CreditDTO create(CreditDTO.CreateRequest req) {
        Credits credit = new Credits();
        credit.setMembre(membresRepo.getReferenceById(req.numMembre()));
        credit.setProduitCredit(produitRepo.getReferenceById(req.numProduit()));
        credit.setAgence(agenceRepo.getReferenceById(req.codeAgence()));
        credit.setMontantDemande(req.montantDemande());
        credit.setPeriodicite(req.periodicite());
        credit.setDuree(req.duree());
        credit.setNombreEcheance(req.nombreEcheance());
        credit.setDelaiGrace(req.delaiGrace());
        credit.setObjetCredit(req.objetCredit());
        credit.setNumeroCycle(req.numeroCycle());
        credit.setDateDemande(req.dateDemande());
        if (req.tauxInteret()    != null) credit.setTauxInteret(req.tauxInteret());
        if (req.tauxPenalite()   != null) credit.setTauxPenalite(req.tauxPenalite());
        if (req.tauxCommission() != null) credit.setTauxCommission(req.tauxCommission());
        if (req.tauxAssurance()  != null) credit.setTauxAssurance(req.tauxAssurance());
        return create(credit);
    }

    /**
     * Update from validated DTO request (Phase 11.1).
     */
    @Transactional
    public CreditDTO update(Long id, CreditDTO.UpdateRequest req) {
        Credits patch = new Credits();
        patch.setMontantDemande(req.montantDemande());
        patch.setPeriodicite(req.periodicite());
        patch.setDuree(req.duree());
        patch.setNombreEcheance(req.nombreEcheance());
        patch.setDelaiGrace(req.delaiGrace());
        patch.setObjetCredit(req.objetCredit());
        patch.setNumeroCycle(req.numeroCycle());
        patch.setDateDemande(req.dateDemande());
        patch.setTauxInteret(req.tauxInteret());
        patch.setTauxPenalite(req.tauxPenalite());
        patch.setTauxCommission(req.tauxCommission());
        patch.setTauxAssurance(req.tauxAssurance());
        return update(id, patch);
    }

    /** Update editable fields (only allowed in BROUILLON). */
    @Transactional
    public CreditDTO update(Long id, Credits patch) {
        Credits existing = repo.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (existing.getStatut() != CreditStatut.BROUILLON) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Seul un crédit en statut BROUILLON peut être modifié.");
        }
        patch.setIdCredit(id);
        patch.setStatut(existing.getStatut());
        return CreditDTO.from(repo.save(patch));
    }

    /**
     * Lifecycle transition – moves the credit to the next statut.
     *
     * Allowed transitions:
     *   BROUILLON → SOUMIS        (by agent)
     *   SOUMIS → VALIDE_AGENT     (by agent)
     *   VALIDE_AGENT → VALIDE_COMITE (by comite)
     *   VALIDE_COMITE → DEBLOQUE  (by admin – triggers amortisation)
     *   Any → REJETE              (by agent/comite/admin)
     */
    @Transactional
    public CreditDTO transitionner(Long id, String nouveauStatut) {
        Credits credit = repo.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        CreditStatut target;
        try { target = CreditStatut.valueOf(nouveauStatut); }
        catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Statut inconnu: " + nouveauStatut);
        }

        validateTransition(credit.getStatut(), target);
        credit.setStatut(target);

        if (target == CreditStatut.DEBLOQUE) {
            BigDecimal montant = (credit.getMontantAccorde() != null
                    && credit.getMontantAccorde().compareTo(BigDecimal.ZERO) > 0)
                    ? credit.getMontantAccorde()
                    : credit.getMontantDemande();
            credit.setMontantDebloquer(montant);
            credit.setSoldeCapital(montant);
            credit.setDateDeblocage(LocalDate.now());
        }

        Credits saved = repo.save(credit);

        if (target == CreditStatut.DEBLOQUE) {
            List<Amortp> rows = amortissementService.genererTableauPreview(saved);
            rows.forEach(r -> r.setCredit(saved));
            amortpRepo.saveAll(rows);
        }

        return CreditDTO.from(saved);
    }

    private void validateTransition(CreditStatut from, CreditStatut to) {
        boolean valid = switch (from) {
            case BROUILLON    -> to == CreditStatut.SOUMIS || to == CreditStatut.REJETE;
            case SOUMIS       -> to == CreditStatut.VALIDE_AGENT || to == CreditStatut.REJETE;
            case VALIDE_AGENT -> to == CreditStatut.VALIDE_COMITE || to == CreditStatut.REJETE;
            case VALIDE_COMITE-> to == CreditStatut.DEBLOQUE || to == CreditStatut.REJETE;
            case DEBLOQUE     -> to == CreditStatut.SOLDE;
            default           -> false;
        };
        if (!valid) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Transition interdite : " + from + " → " + to);
        }
    }

    @Transactional
    public boolean delete(Long id) {
        if (!repo.existsById(id)) return false;
        repo.deleteById(id);
        return true;
    }
}
