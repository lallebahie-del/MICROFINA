package com.pfe.backend.service;

import com.pfe.backend.dto.SimulationCreditDTO.EcheanceDto;
import com.pfe.backend.dto.SimulationCreditDTO.SimulationRequest;
import com.pfe.backend.dto.SimulationCreditDTO.SimulationResponse;
import com.pfe.backend.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * AmortissementCalculator — calcul d'amortissement français (annuités constantes).
 * Formule : E = P × r / (1 − (1+r)^−n)
 * où r = taux périodique, n = nombre d'échéances.
 */
@Component
public class AmortissementCalculator {

    private static final MathContext MC = new MathContext(15, RoundingMode.HALF_UP);
    private static final int SCALE = 4;

    /**
     * Calcule le tableau d'amortissement complet.
     *
     * @param req paramètres de simulation
     * @return SimulationResponse avec tableau d'amortissement
     */
    public SimulationResponse calculer(SimulationRequest req) {
        int diviseurAnnuel = diviseur(req.periodicite());
        BigDecimal tauxPeriodique = req.tauxAnnuel()
            .divide(BigDecimal.valueOf(100L * diviseurAnnuel), MC);

        int n = req.nombreEcheances();
        BigDecimal p = req.montantPrincipal();

        BigDecimal echeance;
        if (tauxPeriodique.compareTo(BigDecimal.ZERO) == 0) {
            echeance = p.divide(BigDecimal.valueOf(n), SCALE, RoundingMode.HALF_UP);
        } else {
            // E = P × r / (1 − (1+r)^−n)
            BigDecimal unPlusR = BigDecimal.ONE.add(tauxPeriodique, MC);
            BigDecimal puissance = unPlusR.pow(-n, MC);
            BigDecimal denominateur = BigDecimal.ONE.subtract(puissance, MC);
            echeance = p.multiply(tauxPeriodique, MC)
                        .divide(denominateur, SCALE, RoundingMode.HALF_UP);
        }

        List<EcheanceDto> tableau = new ArrayList<>();
        BigDecimal capitalRestant = p.setScale(SCALE, RoundingMode.HALF_UP);
        BigDecimal totalInteret = BigDecimal.ZERO;
        BigDecimal echeanceCourante = echeance;

        for (int i = 1; i <= n; i++) {
            BigDecimal interet = capitalRestant.multiply(tauxPeriodique, MC)
                                              .setScale(SCALE, RoundingMode.HALF_UP);
            BigDecimal capitalRembourse = echeanceCourante.subtract(interet)
                                                          .setScale(SCALE, RoundingMode.HALF_UP);
            if (i == n) {
                // Ajustement dernière échéance pour solde exact
                capitalRembourse = capitalRestant;
                echeanceCourante = capitalRembourse.add(interet).setScale(SCALE, RoundingMode.HALF_UP);
            }
            tableau.add(new EcheanceDto(i, capitalRestant, interet, capitalRembourse, echeanceCourante));
            totalInteret = totalInteret.add(interet);
            capitalRestant = capitalRestant.subtract(capitalRembourse)
                                           .setScale(SCALE, RoundingMode.HALF_UP);
        }

        BigDecimal totalRembourse = p.add(totalInteret).setScale(SCALE, RoundingMode.HALF_UP);

        return new SimulationResponse(
            req.montantPrincipal(),
            req.tauxAnnuel(),
            n,
            req.periodicite(),
            echeance,
            totalRembourse,
            totalInteret.setScale(SCALE, RoundingMode.HALF_UP),
            tableau
        );
    }

    private int diviseur(String periodicite) {
        return switch (periodicite.toUpperCase()) {
            case "MENSUEL"     -> 12;
            case "TRIMESTRIEL" -> 4;
            case "SEMESTRIEL"  -> 2;
            case "ANNUEL"      -> 1;
            default -> throw new BusinessException(
                "Périodicité inconnue: " + periodicite + ". Valeurs: MENSUEL, TRIMESTRIEL, SEMESTRIEL, ANNUEL"
            );
        };
    }
}
