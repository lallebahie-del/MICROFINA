package com.pfe.backend.service;

import com.microfina.entity.*;
import com.pfe.backend.dto.ReglementDTO;
import com.pfe.backend.exception.BusinessException;
import com.pfe.backend.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReglementService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final CreditsRepository creditsRepository;
    private final AmortpRepository amortpRepository;
    private final ReglementRepository reglementRepository;
    private final DetailReglementRepository detailReglementRepository;
    private final AgenceRepository agenceRepository;
    private final CreditAccountingService accountingService;

    public ReglementService(CreditsRepository creditsRepository,
                            AmortpRepository amortpRepository,
                            ReglementRepository reglementRepository,
                            DetailReglementRepository detailReglementRepository,
                            AgenceRepository agenceRepository,
                            CreditAccountingService accountingService) {
        this.creditsRepository = creditsRepository;
        this.amortpRepository = amortpRepository;
        this.reglementRepository = reglementRepository;
        this.detailReglementRepository = detailReglementRepository;
        this.agenceRepository = agenceRepository;
        this.accountingService = accountingService;
    }

    @Transactional
    public ReglementDTO.Response encaisserRemboursementCaisse(Long idCredit,
                                                             ReglementDTO.RemboursementCaisseRequest req,
                                                             String utilisateur) {
        Credits credit = creditsRepository.findById(idCredit)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Crédit introuvable : " + idCredit));

        if (credit.getStatut() == null || !credit.getStatut().accepteRemboursement()) {
            throw new BusinessException("Le remboursement n'est possible que pour un crédit débloqué.");
        }

        BigDecimal montant = req.montant();
        if (montant == null || montant.compareTo(ZERO) <= 0) {
            throw new BusinessException("Le montant doit être strictement positif.");
        }

        LocalDate dateReglement = req.dateReglement() != null ? req.dateReglement() : LocalDate.now();

        List<Amortp> echeances = amortpRepository.findByCredit_IdCreditOrderByNumEcheanceAsc(idCredit);
        if (echeances.isEmpty()) {
            throw new BusinessException("Aucune échéance trouvée pour ce crédit (amortissement non généré).");
        }

        // 1) Écriture comptable (obligatoire: FK NOT NULL sur Reglement.idcomptabilite)
        Comptabilite ecriture = accountingService.ecritureRemboursementCaisse(
                credit,
                montant,
                utilisateur,
                "REMBOURSEMENT_CREDIT#" + idCredit,
                req.numPiece() != null && !req.numPiece().isBlank()
                        ? req.numPiece()
                        : "CR-REM-" + idCredit + "-" + dateReglement
        );

        // 2) Construire Reglement (header)
        Reglement reglement = new Reglement();
        reglement.setCredit(credit);
        reglement.setDateReglement(dateReglement);
        reglement.setMontantTotal(montant);
        reglement.setNumPiece(req.numPiece());
        reglement.setModePaiement(req.modePaiement() != null && !req.modePaiement().isBlank()
                ? req.modePaiement()
                : "ESPECES");
        reglement.setStatut("VALIDE");
        reglement.setIdComptabilite(ecriture.getIdComptabilite());
        reglement.setUtilisateur(utilisateur);

        if (req.codeAgence() != null && !req.codeAgence().isBlank()) {
            reglement.setAgence(agenceRepository.getReferenceById(req.codeAgence()));
        } else if (credit.getAgence() != null) {
            reglement.setAgence(credit.getAgence());
        }

        // 3) Allocation sur échéances (oldest first)
        Allocation allocation = allouerMontant(montant, echeances, dateReglement);

        reglement.setMontantCapital(allocation.totalCapital);
        reglement.setMontantInteret(allocation.totalInteret);
        reglement.setMontantPenalite(allocation.totalPenalite);
        reglement.setMontantAssurance(allocation.totalAssurance);
        reglement.setMontantCommission(allocation.totalCommission);
        reglement.setMontantTaxe(allocation.totalTaxe);

        Reglement savedReglement = reglementRepository.save(reglement);

        // 4) Détails + mise à jour Amortp
        for (DetailLine dl : allocation.details) {
            if (dl.isEmpty()) continue;

            DetailReglement d = new DetailReglement();
            d.setReglement(savedReglement);
            d.setAmortp(dl.amortp);
            d.setMontantCapital(dl.capital);
            d.setMontantInteret(dl.interet);
            d.setMontantPenalite(dl.penalite);
            d.setMontantAssurance(dl.assurance);
            d.setMontantCommission(dl.commission);
            detailReglementRepository.save(d);
        }

        for (Amortp a : echeances) {
            amortpRepository.save(a);
        }

        // 5) Mettre à jour soldes du crédit
        credit.setSoldeCapital(nz(credit.getSoldeCapital()).subtract(allocation.totalCapital));
        credit.setSoldeInteret(nz(credit.getSoldeInteret()).subtract(allocation.totalInteret));
        credit.setSoldePenalite(nz(credit.getSoldePenalite()).subtract(allocation.totalPenalite));
        creditsRepository.save(credit);

        return new ReglementDTO.Response(
                savedReglement.getIdReglement(),
                idCredit,
                savedReglement.getDateReglement(),
                savedReglement.getMontantTotal(),
                savedReglement.getIdComptabilite(),
                savedReglement.getStatut()
        );
    }

    private Allocation allouerMontant(BigDecimal montant, List<Amortp> echeances, LocalDate dateReglement) {
        BigDecimal remaining = montant;
        Allocation allocation = new Allocation();

        for (Amortp a : echeances) {
            if (remaining.compareTo(ZERO) <= 0) break;

            DetailLine line = new DetailLine(a);

            // Pénalité
            BigDecimal duePen = restant(nz(a.getPenalite()), nz(a.getPenaliteReglee()));
            BigDecimal payPen = min(remaining, duePen);
            if (payPen.compareTo(ZERO) > 0) {
                a.setPenaliteReglee(nz(a.getPenaliteReglee()).add(payPen));
                remaining = remaining.subtract(payPen);
                line.penalite = payPen;
                allocation.totalPenalite = allocation.totalPenalite.add(payPen);
            }

            // Taxe (non stockée dans DetailReglement)
            BigDecimal dueTaxe = restant(nz(a.getTaxe()), nz(a.getTaxeReglee()));
            BigDecimal payTaxe = min(remaining, dueTaxe);
            if (payTaxe.compareTo(ZERO) > 0) {
                a.setTaxeReglee(nz(a.getTaxeReglee()).add(payTaxe));
                remaining = remaining.subtract(payTaxe);
                allocation.totalTaxe = allocation.totalTaxe.add(payTaxe);
            }

            // Commission
            BigDecimal dueCom = restant(nz(a.getCommission()), nz(a.getCommissionReglee()));
            BigDecimal payCom = min(remaining, dueCom);
            if (payCom.compareTo(ZERO) > 0) {
                a.setCommissionReglee(nz(a.getCommissionReglee()).add(payCom));
                remaining = remaining.subtract(payCom);
                line.commission = payCom;
                allocation.totalCommission = allocation.totalCommission.add(payCom);
            }

            // Assurance
            BigDecimal dueAss = restant(nz(a.getAssurance()), nz(a.getAssuranceReglee()));
            BigDecimal payAss = min(remaining, dueAss);
            if (payAss.compareTo(ZERO) > 0) {
                a.setAssuranceReglee(nz(a.getAssuranceReglee()).add(payAss));
                remaining = remaining.subtract(payAss);
                line.assurance = payAss;
                allocation.totalAssurance = allocation.totalAssurance.add(payAss);
            }

            // Intérêt / Marge
            BigDecimal dueInt = restant(nz(a.getInteret()), nz(a.getInteretRembourse()));
            BigDecimal payInt = min(remaining, dueInt);
            if (payInt.compareTo(ZERO) > 0) {
                a.setInteretRembourse(nz(a.getInteretRembourse()).add(payInt));
                remaining = remaining.subtract(payInt);
                line.interet = payInt;
                allocation.totalInteret = allocation.totalInteret.add(payInt);
            }

            // Capital
            BigDecimal dueCap = restant(nz(a.getCapital()), nz(a.getCapitalRembourse()));
            BigDecimal payCap = min(remaining, dueCap);
            if (payCap.compareTo(ZERO) > 0) {
                a.setCapitalRembourse(nz(a.getCapitalRembourse()).add(payCap));
                remaining = remaining.subtract(payCap);
                line.capital = payCap;
                allocation.totalCapital = allocation.totalCapital.add(payCap);
            }

            updateStatutEcheance(a, dateReglement);
            allocation.details.add(line);
        }

        return allocation;
    }

    private void updateStatutEcheance(Amortp a, LocalDate today) {
        BigDecimal restantTotal = restant(
                nz(a.getTotalEcheance()),
                nz(a.getCapitalRembourse())
                        .add(nz(a.getInteretRembourse()))
                        .add(nz(a.getPenaliteReglee()))
                        .add(nz(a.getAssuranceReglee()))
                        .add(nz(a.getCommissionReglee()))
                        .add(nz(a.getTaxeReglee()))
        );

        if (restantTotal.compareTo(ZERO) <= 0) {
            a.setStatutEcheance("REGLE");
            if (a.getDateReglement() == null) a.setDateReglement(today);
            return;
        }

        boolean partiel = nz(a.getCapitalRembourse()).compareTo(ZERO) > 0
                || nz(a.getInteretRembourse()).compareTo(ZERO) > 0
                || nz(a.getPenaliteReglee()).compareTo(ZERO) > 0
                || nz(a.getAssuranceReglee()).compareTo(ZERO) > 0
                || nz(a.getCommissionReglee()).compareTo(ZERO) > 0
                || nz(a.getTaxeReglee()).compareTo(ZERO) > 0;

        if (a.getDateEcheance() != null && a.getDateEcheance().isBefore(today)) {
            a.setStatutEcheance(partiel ? "PARTIELLEMENT_REGLE" : "EN_RETARD");
        } else {
            a.setStatutEcheance(partiel ? "PARTIELLEMENT_REGLE" : "EN_ATTENTE");
        }
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? ZERO : v;
    }

    private static BigDecimal restant(BigDecimal due, BigDecimal paid) {
        BigDecimal r = nz(due).subtract(nz(paid));
        return r.compareTo(ZERO) < 0 ? ZERO : r;
    }

    private static BigDecimal min(BigDecimal a, BigDecimal b) {
        return a.compareTo(b) <= 0 ? a : b;
    }

    private static final class Allocation {
        BigDecimal totalCapital = ZERO;
        BigDecimal totalInteret = ZERO;
        BigDecimal totalPenalite = ZERO;
        BigDecimal totalAssurance = ZERO;
        BigDecimal totalCommission = ZERO;
        BigDecimal totalTaxe = ZERO;
        List<DetailLine> details = new ArrayList<>();
    }

    private static final class DetailLine {
        final Amortp amortp;
        BigDecimal capital = ZERO;
        BigDecimal interet = ZERO;
        BigDecimal penalite = ZERO;
        BigDecimal assurance = ZERO;
        BigDecimal commission = ZERO;

        DetailLine(Amortp amortp) { this.amortp = amortp; }

        boolean isEmpty() {
            return capital.compareTo(ZERO) == 0
                    && interet.compareTo(ZERO) == 0
                    && penalite.compareTo(ZERO) == 0
                    && assurance.compareTo(ZERO) == 0
                    && commission.compareTo(ZERO) == 0;
        }
    }
}

