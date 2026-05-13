package com.pfe.backend.service;

import com.microfina.entity.CompteBanque;
import com.microfina.entity.CompteEps;
import com.pfe.backend.dto.WorkflowDTO;
import com.pfe.backend.exception.BusinessException;
import com.pfe.backend.exception.ResourceNotFoundException;
import com.pfe.backend.repository.CompteBanqueRepository;
import com.pfe.backend.repository.CompteEpsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * CreditDisbursementService — matérialise le déblocage par un mouvement de fonds
 * qui impacte le solde d'une caisse (CompteEps) ou d'une banque (CompteBanque).
 *
 * La comptabilisation détaillée et la création d'opérations (OperationCaisse/OperationBanque)
 * sont traitées séparément par CreditAccountingService (todo suivant).
 */
@Service
public class CreditDisbursementService {

    private final CompteBanqueRepository compteBanqueRepository;
    private final CompteEpsRepository compteEpsRepository;

    public CreditDisbursementService(CompteBanqueRepository compteBanqueRepository,
                                     CompteEpsRepository compteEpsRepository) {
        this.compteBanqueRepository = compteBanqueRepository;
        this.compteEpsRepository = compteEpsRepository;
    }

    @Transactional
    public void debloquerFonds(WorkflowDTO.DeblocageRequest req) {
        String canal = req.canal() == null ? "" : req.canal().trim().toUpperCase();
        BigDecimal montant = req.montantDeblocage();

        switch (canal) {
            case "BANQUE" -> debiterBanque(req.compteBanqueId(), montant);
            case "CAISSE" -> debiterCaisse(req.numCompteCaisse(), montant);
            case "WALLET" -> {
                // Géré par WalletService (initiation + callback) si activé.
                // Ici on n'impacte pas de solde local tant que le callback n'a pas confirmé.
            }
            default -> throw new BusinessException("Canal de déblocage invalide: " + req.canal()
                + ". Valeurs attendues: CAISSE, BANQUE, WALLET");
        }
    }

    private void debiterBanque(Long compteBanqueId, BigDecimal montant) {
        if (compteBanqueId == null) {
            throw new BusinessException("compteBanqueId est obligatoire quand canal = BANQUE.");
        }
        CompteBanque compte = compteBanqueRepository.findById(compteBanqueId)
                .orElseThrow(() -> new ResourceNotFoundException("CompteBanque", compteBanqueId));

        BigDecimal solde = compte.getSolde() == null ? BigDecimal.ZERO : compte.getSolde();
        if (solde.compareTo(montant) < 0) {
            throw new BusinessException("Solde bancaire insuffisant. Solde=" + solde + ", montant=" + montant);
        }
        compte.setSolde(solde.subtract(montant));
        compteBanqueRepository.save(compte);
    }

    private void debiterCaisse(String numCompteCaisse, BigDecimal montant) {
        if (numCompteCaisse == null || numCompteCaisse.isBlank()) {
            throw new BusinessException("numCompteCaisse est obligatoire quand canal = CAISSE.");
        }
        CompteEps compte = compteEpsRepository.findById(numCompteCaisse)
                .orElseThrow(() -> new ResourceNotFoundException("CompteEps", numCompteCaisse));

        BigDecimal depot = compte.getMontantDepot() == null ? BigDecimal.ZERO : compte.getMontantDepot();
        BigDecimal bloque = compte.getMontantBloque() == null ? BigDecimal.ZERO : compte.getMontantBloque();
        BigDecimal disponible = depot.subtract(bloque);

        if (disponible.compareTo(montant) < 0) {
            throw new BusinessException("Solde caisse insuffisant. Disponible=" + disponible + ", montant=" + montant);
        }

        compte.setMontantDepot(depot.subtract(montant));
        compteEpsRepository.save(compte);
    }
}

