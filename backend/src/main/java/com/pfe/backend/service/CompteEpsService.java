package com.pfe.backend.service;

import com.microfina.entity.CompteEps;
import com.pfe.backend.dto.CompteEpsDTO;
import com.pfe.backend.exception.BusinessException;
import com.pfe.backend.exception.ResourceNotFoundException;
import com.pfe.backend.repository.AgenceRepository;
import com.pfe.backend.repository.CompteEpsRepository;
import com.pfe.backend.repository.MembresRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * CompteEpsService — logique métier pour les comptes épargne (COMPTEEPS).
 *
 * <p>Toutes les méthodes de lecture sont exécutées en transaction lecture-seule.
 * Les méthodes d'écriture ouvrent une transaction lecture-écriture.</p>
 */
@Service
@Transactional(readOnly = true)
public class CompteEpsService {

    private final CompteEpsRepository compteEpsRepository;
    private final MembresRepository   membresRepository;
    private final AgenceRepository    agenceRepository;

    public CompteEpsService(
            CompteEpsRepository compteEpsRepository,
            MembresRepository   membresRepository,
            AgenceRepository    agenceRepository) {
        this.compteEpsRepository = compteEpsRepository;
        this.membresRepository   = membresRepository;
        this.agenceRepository    = agenceRepository;
    }

    // ── Lectures ──────────────────────────────────────────────────────

    /** Retourne tous les comptes épargne. */
    public List<CompteEpsDTO.Response> findAll() {
        return compteEpsRepository.findAll()
                .stream()
                .map(CompteEpsDTO.Response::from)
                .toList();
    }

    /**
     * Retourne un compte épargne par son numéro.
     *
     * @throws ResourceNotFoundException si le compte n'existe pas
     */
    public CompteEpsDTO.Response findById(String numCompte) {
        CompteEps compte = compteEpsRepository.findById(numCompte)
                .orElseThrow(() -> new ResourceNotFoundException("CompteEps", numCompte));
        return CompteEpsDTO.Response.from(compte);
    }

    /** Retourne tous les comptes épargne appartenant à un membre. */
    public List<CompteEpsDTO.Response> findByMembre(String numMembre) {
        return compteEpsRepository.findByMembre_NumMembre(numMembre)
                .stream()
                .map(CompteEpsDTO.Response::from)
                .toList();
    }

    /** Retourne tous les comptes épargne gérés par une agence. */
    public List<CompteEpsDTO.Response> findByAgence(String codeAgence) {
        return compteEpsRepository.findByAgence_CodeAgence(codeAgence)
                .stream()
                .map(CompteEpsDTO.Response::from)
                .toList();
    }

    // ── Écritures ─────────────────────────────────────────────────────

    /**
     * Crée un nouveau compte épargne.
     *
     * @throws BusinessException si un compte avec le même {@code numCompte} existe déjà
     */
    @Transactional
    public CompteEpsDTO.Response create(CompteEpsDTO.CreateRequest req) {
        if (compteEpsRepository.existsById(req.numCompte())) {
            throw new BusinessException(
                "COMPTE_EPS_DUPLICATE",
                "Un compte épargne avec le numéro '" + req.numCompte() + "' existe déjà."
            );
        }

        CompteEps compte = new CompteEps();
        compte.setNumCompte(req.numCompte());
        compte.setMembre(membresRepository.getReferenceById(req.numMembre()));
        if (req.codeAgence() != null && !req.codeAgence().isBlank()) {
            compte.setAgence(agenceRepository.getReferenceById(req.codeAgence()));
        }
        compte.setProduitEpargne(req.produitEpargne());
        compte.setTypeEpargne(req.typeEpargne());
        compte.setRemarque(req.remarque());
        compte.setMontantOuvert(req.montantOuvert());
        compte.setTauxInteret(req.tauxInteret());
        compte.setDateCreation(req.dateCreation());
        compte.setDateEcheance(req.dateEcheance());
        compte.setDuree(req.duree());
        // Initialiser les flags par défaut
        compte.setBloque("N");
        compte.setFerme("N");
        compte.setExonere("N");

        return CompteEpsDTO.Response.from(compteEpsRepository.save(compte));
    }

    /**
     * Met à jour les champs modifiables d'un compte épargne existant.
     *
     * @throws ResourceNotFoundException si le compte n'existe pas
     */
    @Transactional
    public CompteEpsDTO.Response update(String numCompte, CompteEpsDTO.UpdateRequest req) {
        CompteEps compte = compteEpsRepository.findById(numCompte)
                .orElseThrow(() -> new ResourceNotFoundException("CompteEps", numCompte));

        if (req.typeEpargne() != null) {
            compte.setTypeEpargne(req.typeEpargne());
        }
        if (req.remarque() != null) {
            compte.setRemarque(req.remarque());
        }
        if (req.bloque() != null) {
            compte.setBloque(req.bloque());
        }
        if (req.ferme() != null) {
            compte.setFerme(req.ferme());
        }
        if (req.exonere() != null) {
            compte.setExonere(req.exonere());
        }
        if (req.montantBloque() != null) {
            compte.setMontantBloque(req.montantBloque());
        }
        if (req.tauxInteret() != null) {
            compte.setTauxInteret(req.tauxInteret());
        }
        if (req.dateEcheance() != null) {
            compte.setDateEcheance(req.dateEcheance());
        }
        if (req.dateFermee() != null) {
            compte.setDateFermee(req.dateFermee());
        }

        return CompteEpsDTO.Response.from(compteEpsRepository.save(compte));
    }

    @Transactional
    public CompteEpsDTO.MouvementResponse depot(String numCompte, CompteEpsDTO.MouvementRequest req) {
        CompteEps compte = compteEpsRepository.findById(numCompte)
                .orElseThrow(() -> new ResourceNotFoundException("CompteEps", numCompte));
        if ("O".equals(compte.getBloque())) {
            throw new BusinessException("COMPTE_BLOQUE", "Ce compte est bloqué.");
        }
        if ("O".equals(compte.getFerme())) {
            throw new BusinessException("COMPTE_FERME", "Ce compte est fermé.");
        }
        BigDecimal actuel = compte.getMontantDepot() != null ? compte.getMontantDepot() : BigDecimal.ZERO;
        compte.setMontantDepot(actuel.add(req.montant()));
        compteEpsRepository.save(compte);
        BigDecimal ouvert = compte.getMontantOuvert() != null ? compte.getMontantOuvert() : BigDecimal.ZERO;
        return new CompteEpsDTO.MouvementResponse(numCompte, "DEPOT", req.montant(),
                ouvert.add(compte.getMontantDepot()));
    }

    @Transactional
    public CompteEpsDTO.MouvementResponse retrait(String numCompte, CompteEpsDTO.MouvementRequest req) {
        CompteEps compte = compteEpsRepository.findById(numCompte)
                .orElseThrow(() -> new ResourceNotFoundException("CompteEps", numCompte));
        if ("O".equals(compte.getBloque())) {
            throw new BusinessException("COMPTE_BLOQUE", "Ce compte est bloqué.");
        }
        if ("O".equals(compte.getFerme())) {
            throw new BusinessException("COMPTE_FERME", "Ce compte est fermé.");
        }
        BigDecimal ouvert = compte.getMontantOuvert() != null ? compte.getMontantOuvert() : BigDecimal.ZERO;
        BigDecimal depot  = compte.getMontantDepot()  != null ? compte.getMontantDepot()  : BigDecimal.ZERO;
        BigDecimal soldeActuel = ouvert.add(depot);
        if (req.montant().compareTo(soldeActuel) > 0) {
            throw new BusinessException("SOLDE_INSUFFISANT",
                    "Solde insuffisant : " + soldeActuel + " MRU disponible.");
        }
        compte.setMontantDepot(depot.subtract(req.montant()));
        compteEpsRepository.save(compte);
        return new CompteEpsDTO.MouvementResponse(numCompte, "RETRAIT", req.montant(),
                ouvert.add(compte.getMontantDepot()));
    }

    /**
     * Supprime définitivement un compte épargne.
     *
     * @throws ResourceNotFoundException si le compte n'existe pas
     */
    @Transactional
    public void delete(String numCompte) {
        if (!compteEpsRepository.existsById(numCompte)) {
            throw new ResourceNotFoundException("CompteEps", numCompte);
        }
        compteEpsRepository.deleteById(numCompte);
    }
}
