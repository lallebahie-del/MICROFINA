package com.pfe.backend.service;

import com.microfina.entity.CarnetCheque;
import com.microfina.entity.StatutCarnetCheque;
import com.pfe.backend.dto.CarnetChequeDTO;
import com.pfe.backend.exception.BusinessException;
import com.pfe.backend.exception.ResourceNotFoundException;
import com.pfe.backend.repository.CarnetChequeRepository;
import com.pfe.backend.repository.CompteBanqueRepository;
import com.pfe.backend.repository.MembresRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CarnetChequeService — logique métier pour les carnets de chèques.
 *
 * <p>Toutes les méthodes de lecture sont exécutées en transaction lecture-seule.
 * Les méthodes d'écriture ouvrent une transaction lecture-écriture.</p>
 */
@Service
@Transactional(readOnly = true)
public class CarnetChequeService {

    private final CarnetChequeRepository carnetChequeRepository;
    private final CompteBanqueRepository compteBanqueRepository;
    private final MembresRepository      membresRepository;

    public CarnetChequeService(
            CarnetChequeRepository carnetChequeRepository,
            CompteBanqueRepository compteBanqueRepository,
            MembresRepository      membresRepository) {
        this.carnetChequeRepository = carnetChequeRepository;
        this.compteBanqueRepository  = compteBanqueRepository;
        this.membresRepository       = membresRepository;
    }

    // ── Lectures ──────────────────────────────────────────────────────

    /** Retourne tous les carnets de chèques. */
    public List<CarnetChequeDTO.Response> findAll() {
        return carnetChequeRepository.findAll()
                .stream()
                .map(CarnetChequeDTO.Response::from)
                .toList();
    }

    /**
     * Retourne un carnet de chèques par son identifiant.
     *
     * @throws ResourceNotFoundException si le carnet n'existe pas
     */
    public CarnetChequeDTO.Response findById(Long id) {
        CarnetCheque carnet = carnetChequeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CarnetCheque", id));
        return CarnetChequeDTO.Response.from(carnet);
    }

    /** Retourne tous les carnets de chèques d'un membre donné. */
    public List<CarnetChequeDTO.Response> findByMembre(String numMembre) {
        return carnetChequeRepository.findByMembre_NumMembre(numMembre)
                .stream()
                .map(CarnetChequeDTO.Response::from)
                .toList();
    }

    // ── Écritures ─────────────────────────────────────────────────────

    /**
     * Crée un nouveau carnet de chèques.
     *
     * @throws BusinessException si un carnet avec le même {@code numeroCarnet} est en doublon
     *                           (vérification optionnelle selon règle métier)
     */
    @Transactional
    public CarnetChequeDTO.Response create(CarnetChequeDTO.CreateRequest req) {
        CarnetCheque carnet = new CarnetCheque();
        carnet.setNumeroCarnet(req.numeroCarnet());
        carnet.setCompteBanque(compteBanqueRepository.getReferenceById(req.compteBanqueId()));
        carnet.setMembre(membresRepository.getReferenceById(req.numMembre()));
        carnet.setDateDemande(req.dateDemande());
        if (req.nombreCheques() != null) {
            carnet.setNombreCheques(req.nombreCheques());
        }
        carnet.setStatut(StatutCarnetCheque.DEMANDE);

        return CarnetChequeDTO.Response.from(carnetChequeRepository.save(carnet));
    }

    /**
     * Met à jour un carnet de chèques existant.
     *
     * @throws ResourceNotFoundException si le carnet n'existe pas
     * @throws BusinessException         si la valeur de statut est invalide
     */
    @Transactional
    public CarnetChequeDTO.Response update(Long id, CarnetChequeDTO.UpdateRequest req) {
        CarnetCheque carnet = carnetChequeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CarnetCheque", id));

        if (req.statut() != null) {
            try {
                carnet.setStatut(StatutCarnetCheque.valueOf(req.statut()));
            } catch (IllegalArgumentException e) {
                throw new BusinessException(
                    "STATUT_INVALIDE",
                    "Valeur de statut invalide : '" + req.statut() + "'. "
                    + "Valeurs acceptées : DEMANDE, IMPRIME, REMIS, EPUISE, PERDU, OPPOSITION."
                );
            }
        }
        if (req.dateRemise() != null) {
            carnet.setDateRemise(req.dateRemise());
        }
        if (req.nombreCheques() != null) {
            carnet.setNombreCheques(req.nombreCheques());
        }

        return CarnetChequeDTO.Response.from(carnetChequeRepository.save(carnet));
    }

    /**
     * Supprime définitivement un carnet de chèques.
     *
     * @throws ResourceNotFoundException si le carnet n'existe pas
     */
    @Transactional
    public void delete(Long id) {
        if (!carnetChequeRepository.existsById(id)) {
            throw new ResourceNotFoundException("CarnetCheque", id);
        }
        carnetChequeRepository.deleteById(id);
    }
}
