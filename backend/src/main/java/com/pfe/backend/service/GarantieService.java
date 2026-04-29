package com.pfe.backend.service;

import com.microfina.entity.Credits;
import com.microfina.entity.Garantie;
import com.microfina.entity.Membres;
import com.microfina.entity.TypeGarantie;
import com.pfe.backend.dto.GarantieDTO;
import com.pfe.backend.mapper.GarantieMapper;
import com.pfe.backend.repository.CreditsRepository;
import com.pfe.backend.repository.GarantieRepository;
import com.pfe.backend.repository.MembresRepository;
import com.pfe.backend.repository.TypeGarantieRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * GarantieService — logique métier pour la gestion des garanties.
 *
 * <h2>Règles métier</h2>
 * <ul>
 *   <li>Un {@code TypeGarantie} doit exister et être actif avant de créer
 *       une garantie.</li>
 *   <li>Le crédit couvert doit exister ; son statut n'est pas contraint ici
 *       (une garantie peut être saisie dès la demande).</li>
 *   <li>Le membre garant est optionnel ; s'il est renseigné, il doit exister.</li>
 *   <li>{@code tauxCouverture} est calculé automatiquement à la saisie si
 *       {@code MONTANT_DEBLOQUER > 0}.</li>
 *   <li>La mainlevée positionne le statut à {@code LIBERE} et enregistre
 *       {@code dateMainlevee}.</li>
 * </ul>
 */
@Service
@Transactional
public class GarantieService {

    private final GarantieRepository     garantieRepo;
    private final TypeGarantieRepository typeGarantieRepo;
    private final CreditsRepository      creditsRepo;
    private final MembresRepository      membresRepo;
    private final GarantieMapper         mapper;

    public GarantieService(GarantieRepository garantieRepo,
                           TypeGarantieRepository typeGarantieRepo,
                           CreditsRepository creditsRepo,
                           MembresRepository membresRepo,
                           GarantieMapper mapper) {
        this.garantieRepo     = garantieRepo;
        this.typeGarantieRepo = typeGarantieRepo;
        this.creditsRepo      = creditsRepo;
        this.membresRepo      = membresRepo;
        this.mapper           = mapper;
    }

    // ── Création ─────────────────────────────────────────────────────────

    /**
     * Enregistre une nouvelle garantie.
     *
     * @param req        données de saisie
     * @param utilisateur login du saisissant (issu du contexte JWT)
     * @return DTO de la garantie créée
     */
    public GarantieDTO enregistrer(GarantieDTO.CreationRequest req, String utilisateur) {

        TypeGarantie type = typeGarantieRepo.findById(req.codeTypeGarantie())
                .filter(t -> Boolean.TRUE.equals(t.getActif()))
                .orElseThrow(() -> new EntityNotFoundException(
                        "Type de garantie inconnu ou inactif : " + req.codeTypeGarantie()));

        Credits credit = creditsRepo.findById(req.idCredit())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Crédit introuvable : " + req.idCredit()));

        Garantie g = new Garantie(type, credit, req.valeurEstimee(), utilisateur);

        // Garant optionnel
        if (req.numMembreGarant() != null && !req.numMembreGarant().isBlank()) {
            Membres garant = membresRepo.findById(req.numMembreGarant())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Membre garant introuvable : " + req.numMembreGarant()));
            g.setMembreGarant(garant);
        }

        // Champs optionnels
        if (req.dateEvaluation()   != null) g.setDateEvaluation(req.dateEvaluation());
        if (req.referenceDocument() != null) g.setReferenceDocument(req.referenceDocument());
        if (req.observations()     != null) g.setObservations(req.observations());

        // Calcul automatique du taux de couverture
        if (credit.getMontantDebloquer() != null
                && credit.getMontantDebloquer().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal taux = req.valeurEstimee()
                    .divide(credit.getMontantDebloquer(), 6, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            g.setTauxCouverture(taux);
        }

        return mapper.toDto(garantieRepo.save(g));
    }

    // ── Lecture ───────────────────────────────────────────────────────────

    /**
     * Liste les garanties actives d'un crédit.
     */
    @Transactional(readOnly = true)
    public List<GarantieDTO> findActivesParCredit(Long idCredit) {
        return mapper.toDtoList(garantieRepo.findActivesParCredit(idCredit));
    }

    /**
     * Retourne une garantie par son identifiant.
     */
    @Transactional(readOnly = true)
    public GarantieDTO findById(Long id) {
        return mapper.toDto(garantieRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Garantie introuvable : " + id)));
    }

    // ── Couverture ────────────────────────────────────────────────────────

    /**
     * Calcule la couverture globale (toutes garanties actives) d'un crédit.
     */
    @Transactional(readOnly = true)
    public GarantieDTO.CouvertureDTO calculerCouverture(Long idCredit) {
        Credits credit = creditsRepo.findById(idCredit)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Crédit introuvable : " + idCredit));

        BigDecimal totalGaranties = garantieRepo.sommeCouvertureActive(idCredit);
        List<Garantie> actives    = garantieRepo.findActivesParCredit(idCredit);

        double tauxPct = 0.0;
        if (credit.getMontantDebloquer() != null
                && credit.getMontantDebloquer().compareTo(BigDecimal.ZERO) > 0
                && totalGaranties.compareTo(BigDecimal.ZERO) > 0) {
            tauxPct = totalGaranties
                    .divide(credit.getMontantDebloquer(), 6, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
        }

        return new GarantieDTO.CouvertureDTO(
                credit.getIdCredit(),
                credit.getNumCredit(),
                credit.getMontantDebloquer(),
                totalGaranties,
                tauxPct,
                actives.size()
        );
    }

    // ── Mainlevée ─────────────────────────────────────────────────────────

    /**
     * Prononce la mainlevée d'une garantie (passage au statut LIBERE).
     */
    public GarantieDTO liberer(Long id, GarantieDTO.MainleveeRequest req) {
        Garantie g = garantieRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Garantie introuvable : " + id));

        if (!"ACTIF".equals(g.getStatut())) {
            throw new IllegalStateException(
                    "La garantie " + id + " n'est pas au statut ACTIF (statut actuel : " + g.getStatut() + ")");
        }

        g.setStatut("LIBERE");
        g.setDateMainlevee(req.dateMainlevee());
        if (req.observations() != null) g.setObservations(req.observations());

        return mapper.toDto(garantieRepo.save(g));
    }
}
