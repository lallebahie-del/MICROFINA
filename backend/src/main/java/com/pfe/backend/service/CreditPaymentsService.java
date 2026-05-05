package com.pfe.backend.service;

import com.microfina.entity.Amortp;
import com.microfina.entity.Credits;
import com.microfina.service.AmortissementService;
import com.pfe.backend.dto.CreditPaymentsDTO;
import com.pfe.backend.repository.AmortpRepository;
import com.pfe.backend.repository.CreditsRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class CreditPaymentsService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final CreditsRepository creditsRepository;
    private final AmortpRepository amortpRepository;
    private final AmortissementService amortissementService;

    public CreditPaymentsService(
            CreditsRepository creditsRepository,
            AmortpRepository amortpRepository,
            AmortissementService amortissementService) {
        this.creditsRepository = creditsRepository;
        this.amortpRepository = amortpRepository;
        this.amortissementService = amortissementService;
    }

    public CreditPaymentsDTO.Response getAmortpSuivi(Long idCredit) {
        if (!creditsRepository.existsById(idCredit)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Crédit introuvable : " + idCredit);
        }

        LocalDate today = LocalDate.now();
        List<Amortp> rows = amortpRepository.findByCredit_IdCreditOrderByNumEcheanceAsc(idCredit);

        BigDecimal totalDu = ZERO;
        BigDecimal totalPaye = ZERO;
        BigDecimal totalRestant = ZERO;
        long nbEnRetard = 0;

        BigDecimal current = ZERO;
        BigDecimal d1_30 = ZERO;
        BigDecimal d31_60 = ZERO;
        BigDecimal d61_90 = ZERO;
        BigDecimal d90_plus = ZERO;

        List<CreditPaymentsDTO.EcheanceDTO> echeances = new ArrayList<>(rows.size());

        for (Amortp a : rows) {
            BigDecimal du = nz(a.getTotalEcheance());
            BigDecimal paye = nz(a.getCapitalRembourse())
                    .add(nz(a.getInteretRembourse()))
                    .add(nz(a.getPenaliteReglee()))
                    .add(nz(a.getAssuranceReglee()))
                    .add(nz(a.getCommissionReglee()))
                    .add(nz(a.getTaxeReglee()));

            BigDecimal restant = du.subtract(paye);
            if (restant.compareTo(ZERO) < 0) restant = ZERO;

            Integer joursRetard = 0;
            if (a.getDateEcheance() != null && restant.compareTo(ZERO) > 0) {
                long days = today.toEpochDay() - a.getDateEcheance().toEpochDay();
                if (days > 0) {
                    joursRetard = (int) Math.min(Integer.MAX_VALUE, days);
                }
            }

            if (joursRetard > 0) {
                nbEnRetard++;
            }

            // Buckets balance âgée (sur restant dû)
            if (restant.compareTo(ZERO) > 0) {
                if (joursRetard <= 0) {
                    current = current.add(restant);
                } else if (joursRetard <= 30) {
                    d1_30 = d1_30.add(restant);
                } else if (joursRetard <= 60) {
                    d31_60 = d31_60.add(restant);
                } else if (joursRetard <= 90) {
                    d61_90 = d61_90.add(restant);
                } else {
                    d90_plus = d90_plus.add(restant);
                }
            }

            totalDu = totalDu.add(du);
            totalPaye = totalPaye.add(paye);
            totalRestant = totalRestant.add(restant);

            echeances.add(new CreditPaymentsDTO.EcheanceDTO(
                    a.getIdAmortp(),
                    a.getNumEcheance(),
                    a.getDateEcheance(),
                    a.getDateReglement(),
                    a.getStatutEcheance(),

                    nz(a.getCapital()),
                    nz(a.getInteret()),
                    nz(a.getPenalite()),
                    nz(a.getAssurance()),
                    nz(a.getCommission()),
                    nz(a.getTaxe()),
                    du,

                    nz(a.getCapitalRembourse()),
                    nz(a.getInteretRembourse()),
                    nz(a.getPenaliteReglee()),
                    nz(a.getAssuranceReglee()),
                    nz(a.getCommissionReglee()),
                    nz(a.getTaxeReglee()),
                    paye,

                    restant,
                    joursRetard
            ));
        }

        CreditPaymentsDTO.AgingBucketDTO buckets = new CreditPaymentsDTO.AgingBucketDTO(
                current, d1_30, d31_60, d61_90, d90_plus
        );
        CreditPaymentsDTO.SummaryDTO summary = new CreditPaymentsDTO.SummaryDTO(
                totalDu,
                totalPaye,
                totalRestant,
                rows.size(),
                nbEnRetard,
                buckets
        );

        return new CreditPaymentsDTO.Response(idCredit, today, summary, echeances);
    }

    public CreditPaymentsDTO.PreviewResponse getAmortissementPreview(Long idCredit) {
        Credits credit = creditsRepository.findWithAmortissementContextById(idCredit)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Crédit introuvable : " + idCredit));
        try {
            BigDecimal principalUtilise = amortissementService.resolveMontantPreviewPrincipal(credit);
            boolean islamique = credit.getProduitCredit() != null
                    && credit.getProduitCredit().getProduitIslamic() != null;
            List<Amortp> rows = amortissementService.genererTableauPreview(credit);
            LocalDate today = LocalDate.now();
            List<CreditPaymentsDTO.PreviewEcheanceDTO> echeances = new ArrayList<>(rows.size());
            for (Amortp a : rows) {
                echeances.add(new CreditPaymentsDTO.PreviewEcheanceDTO(
                        a.getNumEcheance(),
                        a.getDateEcheance(),
                        nz(a.getCapital()),
                        nz(a.getInteret()),
                        nz(a.getAssurance()),
                        nz(a.getCommission()),
                        nz(a.getTaxe()),
                        nz(a.getTotalEcheance()),
                        nz(a.getSoldeCapital())
                ));
            }
            return new CreditPaymentsDTO.PreviewResponse(
                    idCredit,
                    principalUtilise,
                    islamique,
                    today,
                    echeances
            );
        } catch (IllegalArgumentException | IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? ZERO : v;
    }
}

