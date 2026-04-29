package com.pfe.backend.service;

import com.microfina.entity.Membres;
import com.pfe.backend.dto.MembreDTO;
import com.pfe.backend.repository.AgenceRepository;
import com.pfe.backend.repository.MembresRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class MembresService {

    private final MembresRepository repo;
    private final AgenceRepository  agenceRepo;

    public MembresService(MembresRepository repo, AgenceRepository agenceRepo) {
        this.repo       = repo;
        this.agenceRepo = agenceRepo;
    }

    /**
     * Paginated search – all params are optional (pass empty string to skip).
     */
    public Map<String, Object> search(String search, String statut, String etat,
                                      int page, int size) {
        Page<Membres> result = repo.search(
            search, statut, etat,
            PageRequest.of(page, size, Sort.by("nom").ascending())
        );
        return Map.of(
            "content",       result.getContent().stream().map(MembreDTO::from).toList(),
            "totalElements", result.getTotalElements(),
            "totalPages",    result.getTotalPages(),
            "page",          result.getNumber(),
            "size",          result.getSize()
        );
    }

    /** Single member by primary key. */
    public Optional<MembreDTO> findById(String numMembre) {
        return repo.findById(numMembre).map(MembreDTO::from);
    }

    /** Create or update a member. */
    @Transactional
    public MembreDTO save(Membres membre) {
        return MembreDTO.from(repo.save(membre));
    }

    /**
     * Create a member from a validated DTO request (Phase 11.1).
     */
    @Transactional
    public MembreDTO create(MembreDTO.CreateRequest req) {
        Membres m = buildFromCreate(req);
        return save(m);
    }

    /**
     * Update a member from a validated DTO request (Phase 11.1).
     */
    @Transactional
    public MembreDTO update(String numMembre, MembreDTO.UpdateRequest req) {
        Membres m = repo.findById(numMembre)
                .orElseThrow(() -> new com.pfe.backend.exception.ResourceNotFoundException("Membre", numMembre));
        applyUpdate(m, req);
        return save(m);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Membres buildFromCreate(MembreDTO.CreateRequest req) {
        Membres m = new Membres();
        m.setNumMembre(req.numMembre());
        m.setNom(req.nom());
        m.setPrenom(req.prenom());
        m.setSexe(req.sexe());
        m.setDtype(req.dtype() != null ? req.dtype() : "Membres");
        if (req.codeAgence() != null) m.setAgence(agenceRepo.getReferenceById(req.codeAgence()));
        m.setDateNaissance(req.dateNaissance());
        m.setLieuNaissance(req.lieuNaissance());
        m.setSituationMatrimoniale(req.situationMatrimoniale());
        m.setCategorie(req.categorie());
        m.setSecteurActivite(req.secteurActivite());
        m.setNumeroNationalId(req.numeroNationalId());
        m.setMatriculeMembre(req.matriculeMembre());
        m.setRaisonSociale(req.raisonSociale());
        m.setEtat(req.etat() != null ? req.etat() : "ACTIF");
        m.setStatut(req.statut() != null ? req.statut() : "EN_ATTENTE");
        m.setDateDemande(req.dateDemande());
        m.setDepot(req.depot());
        m.setDroitEntree(req.droitEntree());
        m.setObservation(req.observation());
        m.setPersonneAcontacter(req.personneAcontacter());
        m.setContactPersonneContact(req.contactPersonneContact());
        m.setInfoPersonneContact(req.infoPersonneContact());
        return m;
    }

    private void applyUpdate(Membres m, MembreDTO.UpdateRequest req) {
        m.setNom(req.nom());
        m.setPrenom(req.prenom());
        m.setSexe(req.sexe());
        if (req.dtype() != null) m.setDtype(req.dtype());
        if (req.codeAgence() != null) m.setAgence(agenceRepo.getReferenceById(req.codeAgence()));
        m.setDateNaissance(req.dateNaissance());
        m.setLieuNaissance(req.lieuNaissance());
        m.setSituationMatrimoniale(req.situationMatrimoniale());
        m.setCategorie(req.categorie());
        m.setSecteurActivite(req.secteurActivite());
        m.setNumeroNationalId(req.numeroNationalId());
        m.setMatriculeMembre(req.matriculeMembre());
        m.setRaisonSociale(req.raisonSociale());
        if (req.etat()   != null) m.setEtat(req.etat());
        if (req.statut() != null) m.setStatut(req.statut());
        m.setDateDemande(req.dateDemande());
        m.setDepot(req.depot());
        m.setDroitEntree(req.droitEntree());
        m.setObservation(req.observation());
        m.setPersonneAcontacter(req.personneAcontacter());
        m.setContactPersonneContact(req.contactPersonneContact());
        m.setInfoPersonneContact(req.infoPersonneContact());
    }

    /** Soft-delete: set etat = 'INACTIF'. */
    @Transactional
    public boolean desactiver(String numMembre) {
        return repo.findById(numMembre).map(m -> {
            m.setEtat("INACTIF");
            repo.save(m);
            return true;
        }).orElse(false);
    }

    /** Hard-delete (use with caution – FK constraints may prevent this). */
    @Transactional
    public boolean delete(String numMembre) {
        if (!repo.existsById(numMembre)) return false;
        repo.deleteById(numMembre);
        return true;
    }
}
