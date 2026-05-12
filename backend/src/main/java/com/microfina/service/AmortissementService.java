package com.microfina.service;

import com.microfina.entity.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * AmortissementService – génère et gère le tableau d'amortissement
 * ({@link Amortp}) d'un crédit ({@link Credits}).
 *
 * ══════════════════════════════════════════════════════════════════════
 * Dispatch du mode de calcul
 * ══════════════════════════════════════════════════════════════════════
 * L'algorithme est sélectionné par lecture de
 * {@link Credits#getModeDeCalculInteret()} comparé à
 * {@link ModeCalculInteretConstant} :
 *
 *   DEGRESSIF  → amortissement dégressif (solde restant dû) – le plus courant
 *   SIMPLE     → intérêt plat sur le capital initial
 *   COMPOSE    → intérêts composés (capitalisés chaque période)
 *
 *   FIXE supprimé (redondant avec SIMPLE – voir ModeCalculInteretConstant).
 *
 * ══════════════════════════════════════════════════════════════════════
 * Précision arithmétique
 * ══════════════════════════════════════════════════════════════════════
 * Les taux intermédiaires (annualRate, r) sont calculés avec
 * MathContext(19, HALF_UP) pour éviter la perte de précision cumulée.
 * L'arrondi à 4 décimales (DECIMAL 19,4) n'est appliqué qu'au moment
 * d'écrire les valeurs finales dans les lignes Amortp.
 *
 * ══════════════════════════════════════════════════════════════════════
 * Événement DEBLOQUE – ce qui doit se passer
 * ══════════════════════════════════════════════════════════════════════
 * Quand un crédit passe à {@link CreditStatut#DEBLOQUE} :
 *  1. {@link #genererTableau(Credits)} construit les lignes Amortp.
 *  2. Le service Comptabilité poste l'écriture de déblocage :
 *       Débit:  COMPTE_CAPITAL (créance sur le membre)
 *       Crédit: COMPTE_DEBLOCAGE / CompteEps du membre (sortie de fonds)
 *  3. Un {@link HistoriqueVisaCredit} enregistre l'étape DEBLOCAGE.
 *  4. {@link Credits#setStatut(CreditStatut)} → DEBLOQUE.
 *  5. Credits.SOLDE_CAPITAL ← MONTANT_DEBLOQUER.
 */
@Service
@Transactional
public class AmortissementService {

    /**
     * MathContext pour les calculs de taux intermédiaires.
     * 19 chiffres significatifs, arrondi HALF_UP.
     * Correspond à la précision maximale de DECIMAL(19,4).
     */
    private static final MathContext MC    = new MathContext(19, RoundingMode.HALF_UP);

    /**
     * Nombre de décimales pour les montants stockés en base (DECIMAL 19,4).
     * Appliqué UNIQUEMENT aux valeurs finales écrites dans les lignes Amortp.
     */
    private static final int           SCALE   = 4;
    private static final RoundingMode  ROUNDING = RoundingMode.HALF_UP;

    // ── API publique ──────────────────────────────────────────────

    /**
     * Génère le tableau d'amortissement complet pour le crédit donné.
     *
     * <p>À appeler exactement une fois, immédiatement après le passage
     * du crédit à {@link CreditStatut#DEBLOQUE}.
     * Délègue vers l'algorithme approprié selon
     * {@link ModeDeCalculInteret#getModeCalcul()}.
     *
     * @param credit crédit entièrement renseigné et DEBLOQUE (non null)
     * @return liste ordonnée de {@link Amortp} prête à persister (non sauvegardée)
     * @throws IllegalStateException    si {@code credit.statut != DEBLOQUE}
     * @throws IllegalArgumentException si un paramètre obligatoire est manquant
     */
    public List<Amortp> genererTableau(Credits credit) {
        validerAvantGeneration(credit);

        // Financement islamique: la colonne INTERET représente la marge bénéficiaire.
        if (isIslamic(credit)) {
            return genererIslamic(credit);
        }

        String modeCalcul = credit.getModeDeCalculInteret().getModeCalcul();

        return switch (modeCalcul) {
            case ModeCalculInteretConstant.DEGRESSIF -> genererDegressif(credit);
            case ModeCalculInteretConstant.SIMPLE    -> genererSimple(credit);
            case ModeCalculInteretConstant.COMPOSE   -> genererCompose(credit);
            default -> throw new IllegalArgumentException(
                "Mode de calcul d'intérêt inconnu ou supprimé: « " + modeCalcul
                + " ». Utiliser DEGRESSIF, SIMPLE ou COMPOSE.");
        };
    }

    /**
     * Échéancier prévisionnel (non persisté) : même moteur que {@link #genererTableau(Credits)}
     * mais sans exiger le statut DEBLOQUE. Le principal retenu est, par ordre de priorité :
     * montant débloqué &gt; 0, montant accordé &gt; 0, montant demandé.
     */
    public List<Amortp> genererTableauPreview(Credits source) {
        Credits shadow = buildShadowForPreview(source);
        return genererTableau(shadow);
    }

    /**
     * Montant principal utilisé pour une prévisualisation (même règle de priorité que
     * {@link #genererTableauPreview(Credits)}).
     */
    public BigDecimal resolveMontantPreviewPrincipal(Credits source) {
        BigDecimal p = resolvePreviewPrincipal(source);
        if (p == null || p.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                "Montant prévisionnel indéterminé : renseigner un montant débloqué, accordé ou demandé positif.");
        }
        return p;
    }

    private Credits buildShadowForPreview(Credits source) {
        if (source == null) {
            throw new IllegalArgumentException("Le crédit ne peut pas être null.");
        }
        BigDecimal principal = resolveMontantPreviewPrincipal(source);
        if (source.getNombreEcheance() == null || source.getNombreEcheance() <= 0) {
            throw new IllegalArgumentException(
                "Le nombre d'échéances doit être supérieur à zéro.");
        }
        if (!isIslamic(source) && source.getTauxInteret() == null) {
            throw new IllegalArgumentException("Le taux d'intérêt est obligatoire pour la prévisualisation.");
        }

        // Resolve mode — default to DEGRESSIF when not set (most common for microfinance)
        ModeDeCalculInteret modeEffectif = source.getModeDeCalculInteret();
        if (!isIslamic(source) && modeEffectif == null) {
            modeEffectif = new ModeDeCalculInteret();
            modeEffectif.setModeCalcul(ModeCalculInteretConstant.DEGRESSIF);
        }

        Credits s = new Credits();
        s.setIdCredit(source.getIdCredit());
        s.setStatut(CreditStatut.DEBLOQUE);
        s.setMontantDebloquer(principal);
        s.setNombreEcheance(source.getNombreEcheance());
        s.setPeriodicite(source.getPeriodicite());
        s.setProduitCredit(source.getProduitCredit());
        s.setModeDeCalculInteret(modeEffectif);
        s.setTauxInteret(source.getTauxInteret());
        s.setTauxPenalite(source.getTauxPenalite());
        s.setTauxCommission(source.getTauxCommission());
        s.setTauxAssurance(source.getTauxAssurance());
        s.setMembre(source.getMembre());
        s.setAgence(source.getAgence());
        s.setDatePremièreEcheance(previewPremiereDateEcheance(source));
        return s;
    }

    private LocalDate previewPremiereDateEcheance(Credits source) {
        if (source.getDatePremièreEcheance() != null) {
            return source.getDatePremièreEcheance();
        }
        if (source.getDateDeblocage() != null) {
            return avancerDate(source.getDateDeblocage(), source.getPeriodicite());
        }
        LocalDate base = source.getDateAccord() != null
                ? source.getDateAccord()
                : (source.getDateDemande() != null ? source.getDateDemande() : LocalDate.now());
        return avancerDate(base, source.getPeriodicite());
    }

    private BigDecimal resolvePreviewPrincipal(Credits source) {
        if (source.getMontantDebloquer() != null
                && source.getMontantDebloquer().compareTo(BigDecimal.ZERO) > 0) {
            return source.getMontantDebloquer();
        }
        if (source.getMontantAccorde() != null
                && source.getMontantAccorde().compareTo(BigDecimal.ZERO) > 0) {
            return source.getMontantAccorde();
        }
        return source.getMontantDemande();
    }

    // ── DEGRESSIF – solde restant dû (le plus courant) ────────────

    /**
     * Amortissement dégressif (annuité constante).
     *
     * Formule de la mensualité constante :
     *   instalment = P × r / (1 − (1+r)^−n)
     *
     * Pour chaque période k :
     *   intérêt_k  = solde_k × r
     *   capital_k  = instalment − intérêt_k
     *   solde_{k+1} = solde_k − capital_k
     *
     * La dernière échéance absorbe l'arrondi résiduel.
     *
     * Précision : r est calculé avec MC (pas d'arrondi intermédiaire) ;
     * seuls les montants écrits dans Amortp sont arrondis à SCALE décimales.
     */
    private List<Amortp> genererDegressif(Credits credit) {
        List<Amortp> rows = new ArrayList<>();

        BigDecimal principal      = credit.getMontantDebloquer();
        int        n              = credit.getNombreEcheance();
        int        periodsPerYear = periodesParAn(credit.getPeriodicite());

        // ── Précision : division exacte avec MathContext ──────────
        BigDecimal annualRate = credit.getTauxInteret()
            .divide(BigDecimal.valueOf(100), MC);                    // ex: 0.18000000000000000000
        BigDecimal r = annualRate
            .divide(BigDecimal.valueOf(periodsPerYear), MC);         // ex: 0.01500000000000000000

        // instalment = P × r / (1 − (1+r)^−n) – arrondi final à SCALE
        BigDecimal onePlusR    = BigDecimal.ONE.add(r);
        BigDecimal onePlusRpowN = onePlusR.pow(n, MC);
        BigDecimal denominator  = BigDecimal.ONE.subtract(
            BigDecimal.ONE.divide(onePlusRpowN, MC));
        BigDecimal instalment  = principal.multiply(r, MC)
            .divide(denominator, MC)
            .setScale(SCALE, ROUNDING);

        BigDecimal outstandingCapital = principal;
        LocalDate  dueDate            = premiereDateEcheance(credit);

        for (int k = 1; k <= n; k++) {
            // Intérêt exact, non arrondi avant la soustraction
            BigDecimal interetExact = outstandingCapital.multiply(r, MC);
            BigDecimal interest     = interetExact.setScale(SCALE, ROUNDING);
            BigDecimal capital;

            if (k == n) {
                capital = outstandingCapital.setScale(SCALE, ROUNDING); // solde résiduel
            } else {
                capital = instalment.subtract(interest).setScale(SCALE, ROUNDING);
            }

            outstandingCapital = outstandingCapital.subtract(capital).setScale(SCALE, ROUNDING);

            rows.add(buildRow(credit, k, dueDate, capital, interest, outstandingCapital));
            dueDate = avancerDate(dueDate, credit.getPeriodicite());
        }

        return rows;
    }

    // ── SIMPLE – intérêt plat sur le capital initial ──────────────

    /**
     * Amortissement à intérêt simple (taux plat).
     *
     * Intérêt total = P × taux_annuel × n / périodes_par_an
     * Intérêt par période = intérêt_total / n (constant, ne diminue pas)
     * Capital par période  = P / n (tranches égales)
     *
     * Précision : les taux sont calculés avec MC ; l'arrondi SCALE
     * n'est appliqué qu'aux montants inscrits dans les lignes Amortp.
     */
    private List<Amortp> genererSimple(Credits credit) {
        List<Amortp> rows = new ArrayList<>();

        BigDecimal principal      = credit.getMontantDebloquer();
        int        n              = credit.getNombreEcheance();
        int        periodsPerYear = periodesParAn(credit.getPeriodicite());

        BigDecimal annualRate = credit.getTauxInteret()
            .divide(BigDecimal.valueOf(100), MC);

        // Intérêt total exact, puis répartition uniforme
        BigDecimal totalInterest = principal
            .multiply(annualRate, MC)
            .multiply(BigDecimal.valueOf(n), MC)
            .divide(BigDecimal.valueOf(periodsPerYear), MC);

        BigDecimal interestPerPeriod = totalInterest
            .divide(BigDecimal.valueOf(n), MC);
        BigDecimal capitalPerPeriod  = principal
            .divide(BigDecimal.valueOf(n), MC);

        BigDecimal outstandingCapital = principal;
        LocalDate  dueDate            = premiereDateEcheance(credit);

        for (int k = 1; k <= n; k++) {
            // Dernière période : absorber les arrondis résiduels
            BigDecimal capital  = (k == n)
                ? outstandingCapital.setScale(SCALE, ROUNDING)
                : capitalPerPeriod.setScale(SCALE, ROUNDING);

            BigDecimal interest = (k == n)
                ? totalInterest
                    .subtract(interestPerPeriod.multiply(BigDecimal.valueOf(n - 1), MC))
                    .setScale(SCALE, ROUNDING)
                : interestPerPeriod.setScale(SCALE, ROUNDING);

            outstandingCapital = outstandingCapital.subtract(capital).setScale(SCALE, ROUNDING);

            rows.add(buildRow(credit, k, dueDate, capital, interest, outstandingCapital));
            dueDate = avancerDate(dueDate, credit.getPeriodicite());
        }

        return rows;
    }

    // ── COMPOSE – intérêts composés ───────────────────────────────

    /**
     * Amortissement à intérêts composés.
     *
     * Pour un crédit entièrement amorti sans période de grâce, la formule
     * d'annuité constante produit le même tableau qu'en DEGRESSIF.
     * La différence s'exprime lors des scénarios de grâce ou d'impayés
     * capitalisés, qui seront gérés dans une phase ultérieure.
     */
    private List<Amortp> genererCompose(Credits credit) {
        return genererDegressif(credit);
    }

    // ── Constructeur de ligne Amortp ──────────────────────────────

    /**
     * Construit une ligne {@link Amortp} avec tous les montants calculés.
     *
     * TOTAL_ECHEANCE = CAPITAL + INTERET + ASSURANCE + COMMISSION + TAXE
     * (PENALITE = 0 à la génération ; mise à jour par le job nocturne).
     *
     * Le taux de taxe (TVA) est délégué à {@link #calculerTaxe}.
     * Tous les montants sont arrondis à {@link #SCALE} décimales
     * (seul endroit où SCALE est appliqué).
     */
    private Amortp buildRow(Credits credit,
                            int numEcheance,
                            LocalDate dateEcheance,
                            BigDecimal capital,
                            BigDecimal interet,
                            BigDecimal soldeCapital) {

        BigDecimal assurance   = fraisProrata(credit.getMontantDebloquer(),
                                              credit.getTauxAssurance(),
                                              credit.getNombreEcheance());
        BigDecimal commission  = fraisProrata(credit.getMontantDebloquer(),
                                              credit.getTauxCommission(),
                                              credit.getNombreEcheance());
        BigDecimal taxe        = calculerTaxe(interet, commission, credit);

        BigDecimal total = capital
            .add(interet)
            .add(assurance)
            .add(commission)
            .add(taxe)
            .setScale(SCALE, ROUNDING);

        Amortp row = new Amortp();
        row.setCredit(credit);
        row.setNumEcheance(numEcheance);
        row.setDateEcheance(dateEcheance);
        row.setCapital(capital);
        row.setInteret(interet);
        row.setPenalite(BigDecimal.ZERO);
        row.setAssurance(assurance);
        row.setCommission(commission);
        row.setTaxe(taxe);
        row.setTotalEcheance(total);
        row.setSoldeCapital(soldeCapital);
        // Montants réglés initialisés à zéro
        row.setCapitalRembourse(BigDecimal.ZERO);
        row.setInteretRembourse(BigDecimal.ZERO);
        row.setPenaliteReglee(BigDecimal.ZERO);
        row.setAssuranceReglee(BigDecimal.ZERO);
        row.setCommissionReglee(BigDecimal.ZERO);
        row.setTaxeReglee(BigDecimal.ZERO);
        row.setStatutEcheance("EN_ATTENTE");
        return row;
    }

    // ── Calcul de la taxe (TVA / conformité mauritanienne) ────────

    /**
     * Calcule la taxe due sur les intérêts et commissions d'une échéance.
     *
     * <p>En Mauritanie, la TVA (taux en vigueur) est appliquée sur les
     * produits financiers (intérêts + commissions). Le taux peut évoluer ;
     * il sera exposé sur {@code ProduitCredit.tauxTaxe} dans la Phase 5.
     *
     * <p>Par défaut, si aucun taux n'est configuré sur le produit, la taxe
     * est zéro (comportement conservateur).
     *
     * @param interet    intérêt de la période (déjà arrondi à SCALE)
     * @param commission commission de la période (déjà arrondie à SCALE)
     * @param credit     crédit portant la configuration produit
     * @return montant de taxe arrondi à SCALE décimales
     */
    protected BigDecimal calculerTaxe(BigDecimal interet,
                                      BigDecimal commission,
                                      Credits credit) {
        // TODO Phase 5 : lire ProduitCredit.getTauxTaxe() quand le champ sera disponible
        BigDecimal tauxTaxe = BigDecimal.ZERO;

        if (tauxTaxe.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return interet.add(commission)
            .multiply(tauxTaxe, MC)
            .divide(BigDecimal.valueOf(100), MC)
            .setScale(SCALE, ROUNDING);
    }

    // ── Accroissement des pénalités (job nocturne) ────────────────

    /**
     * Comptabilise les pénalités de retard sur toutes les échéances EN_RETARD
     * du crédit. À appeler par un batch quotidien.
     *
     * pénalité_k = capital_restant_k × (tauxPénalité / 100) × joursRetard / 365
     *
     * @param credit le crédit à contrôler
     * @param today  date de référence (généralement {@code LocalDate.now()})
     * @param rows   liste mutable des lignes Amortp du crédit
     */
    public void accroitrePenalites(Credits credit, LocalDate today, List<Amortp> rows) {
        if (credit.getTauxPenalite() == null
            || credit.getTauxPenalite().compareTo(BigDecimal.ZERO) == 0) {
            return;
        }

        // Taux journalier exact, MC – arrondi seulement sur le montant final
        BigDecimal dailyRate = credit.getTauxPenalite()
            .divide(BigDecimal.valueOf(100), MC)
            .divide(BigDecimal.valueOf(365), MC);

        int toleranceDays = (credit.getProduitCredit() != null
            && credit.getProduitCredit().getJoursTolerancePenalite() != null)
            ? credit.getProduitCredit().getJoursTolerancePenalite()
            : 0;

        for (Amortp row : rows) {
            if (row.isRegle()) continue;
            if (row.getDateEcheance() == null) continue;

            long daysLate = today.toEpochDay() - row.getDateEcheance().toEpochDay();
            if (daysLate <= toleranceDays) continue;

            row.setStatutEcheance("EN_RETARD");

            BigDecimal newPenalty = row.getCapitalRestant()
                .multiply(dailyRate, MC)
                .multiply(BigDecimal.valueOf(daysLate), MC)
                .setScale(SCALE, ROUNDING);

            row.setPenalite(newPenalty);
            row.setTotalEcheance(
                row.getCapitalRestant()
                    .add(row.getInteretRestant())
                    .add(newPenalty)
                    .add(row.getTaxeRestante())
                    .setScale(SCALE, ROUNDING)
            );
        }
    }

    // ── Validation avant génération ───────────────────────────────

    private void validerAvantGeneration(Credits credit) {
        if (credit == null) {
            throw new IllegalArgumentException("Le crédit ne peut pas être null.");
        }
        if (credit.getStatut() != CreditStatut.DEBLOQUE) {
            throw new IllegalStateException(
                "Le tableau d'amortissement ne peut être généré qu'après le déblocage. "
                + "Statut actuel: " + credit.getStatut());
        }
        if (credit.getModeDeCalculInteret() == null) {
            throw new IllegalArgumentException(
                "Le mode de calcul d'intérêt est obligatoire.");
        }
        if (credit.getMontantDebloquer() == null
            || credit.getMontantDebloquer().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                "Le montant débloqué doit être strictement positif.");
        }
        if (credit.getNombreEcheance() == null || credit.getNombreEcheance() <= 0) {
            throw new IllegalArgumentException(
                "Le nombre d'échéances doit être supérieur à zéro.");
        }
        // En islamique, le taux d'intérêt n'est pas requis (marge calculée via ProduitIslamic).
        if (!isIslamic(credit) && credit.getTauxInteret() == null) {
            throw new IllegalArgumentException("Le taux d'intérêt est obligatoire.");
        }
    }

    private boolean isIslamic(Credits credit) {
        return credit != null
                && credit.getProduitCredit() != null
                && credit.getProduitCredit().getProduitIslamic() != null;
    }

    /**
     * Génération simplifiée d'un échéancier islamique:
     * - INTERET = marge bénéficiaire (répartie à plat)
     * - CAPITAL = remboursement du principal (avec cas Ijara: valeur résiduelle en dernière échéance)
     */
    private List<Amortp> genererIslamic(Credits credit) {
        List<Amortp> rows = new ArrayList<>();

        ProduitIslamic isl = credit.getProduitCredit().getProduitIslamic();
        String code = isl.getCodeProduit() != null ? isl.getCodeProduit().trim().toUpperCase(Locale.ROOT) : "";

        BigDecimal principal = credit.getMontantDebloquer();
        int n = credit.getNombreEcheance();

        BigDecimal margeTotale = islamicMargeTotale(code, isl, principal);
        BigDecimal margePeriode = margeTotale
                .divide(BigDecimal.valueOf(n), MC)
                .setScale(SCALE, ROUNDING);

        BigDecimal residual = BigDecimal.ZERO;
        if ("IJARA".equals(code) && isl.getResidualValueRatio() != null) {
            residual = principal.multiply(isl.getResidualValueRatio(), MC).setScale(SCALE, ROUNDING);
            if (residual.compareTo(principal) > 0) residual = BigDecimal.ZERO;
        }

        BigDecimal outstandingCapital = principal.setScale(SCALE, ROUNDING);
        LocalDate dueDate = premiereDateEcheance(credit);

        if ("IJARA".equals(code) && n > 1 && residual.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal amortised = principal.subtract(residual).max(BigDecimal.ZERO);
            BigDecimal capitalPeriode = amortised
                    .divide(BigDecimal.valueOf(n - 1L), MC)
                    .setScale(SCALE, ROUNDING);

            for (int k = 1; k <= n; k++) {
                BigDecimal capital;
                if (k < n) {
                    capital = (k == n - 1)
                            ? outstandingCapital.subtract(residual).setScale(SCALE, ROUNDING)
                            : capitalPeriode;
                } else {
                    capital = residual.setScale(SCALE, ROUNDING);
                }
                outstandingCapital = outstandingCapital.subtract(capital).setScale(SCALE, ROUNDING);
                rows.add(buildRow(credit, k, dueDate, capital, margePeriode, outstandingCapital));
                dueDate = avancerDate(dueDate, credit.getPeriodicite());
            }
            return rows;
        }

        BigDecimal capitalPeriode = principal
                .divide(BigDecimal.valueOf(n), MC)
                .setScale(SCALE, ROUNDING);

        for (int k = 1; k <= n; k++) {
            BigDecimal capital = (k == n) ? outstandingCapital : capitalPeriode;
            outstandingCapital = outstandingCapital.subtract(capital).setScale(SCALE, ROUNDING);
            rows.add(buildRow(credit, k, dueDate, capital, margePeriode, outstandingCapital));
            dueDate = avancerDate(dueDate, credit.getPeriodicite());
        }

        return rows;
    }

    /**
     * Marge totale (finance islamique) : Mourabaha = (P × costPriceRatio) × markupRatio
     * (si {@code costPriceRatio} est absent, la base reste P). Autres produits : P × markup
     * ou, à défaut, {@code tauxPartageBenefice}.
     */
    private BigDecimal islamicMargeTotale(String code, ProduitIslamic isl, BigDecimal principal) {
        String u = code == null ? "" : code.trim().toUpperCase(Locale.ROOT);
        boolean mourabaha = u.contains("MOURABAHA") || u.contains("MURABAHA") || u.contains("MURABIHA");

        BigDecimal costBase = principal;
        if (mourabaha && isl.getCostPriceRatio() != null) {
            costBase = principal.multiply(isl.getCostPriceRatio(), MC);
        }

        BigDecimal markup = isl.getMarkupRatio();
        if (markup == null) {
            markup = isl.getTauxPartageBenefice();
        }
        if (markup == null) {
            markup = BigDecimal.ZERO;
        }

        BigDecimal baseForMarge = mourabaha ? costBase : principal;
        return baseForMarge.multiply(markup, MC).setScale(SCALE, ROUNDING);
    }

    // ── Première date d'échéance ──────────────────────────────────

    private LocalDate premiereDateEcheance(Credits credit) {
        if (credit.getDatePremièreEcheance() != null) {
            return credit.getDatePremièreEcheance();
        }
        return avancerDate(credit.getDateDeblocage(), credit.getPeriodicite());
    }

    // ── Avance d'une période ──────────────────────────────────────

    private LocalDate avancerDate(LocalDate from, String periodicite) {
        if (from == null) return null;
        return switch (periodicite == null ? "M" : periodicite) {
            case ModeCalculInteretConstant.PERIODICITE_HEBDOMADAIRE -> from.plusWeeks(1);
            case ModeCalculInteretConstant.PERIODICITE_QUINZAINE    -> from.plusWeeks(2);
            case ModeCalculInteretConstant.PERIODICITE_BIMENSUEL    -> from.plusMonths(2);
            case ModeCalculInteretConstant.PERIODICITE_TRIMESTRIEL  -> from.plusMonths(3);
            case ModeCalculInteretConstant.PERIODICITE_SEMESTRIEL   -> from.plusMonths(6);
            case ModeCalculInteretConstant.PERIODICITE_ANNUEL       -> from.plusYears(1);
            default                                                  -> from.plusMonths(1);
        };
    }

    // ── Périodes par an ───────────────────────────────────────────

    private int periodesParAn(String periodicite) {
        return switch (periodicite == null ? "M" : periodicite) {
            case ModeCalculInteretConstant.PERIODICITE_HEBDOMADAIRE -> 52;
            case ModeCalculInteretConstant.PERIODICITE_QUINZAINE    -> 26;
            case ModeCalculInteretConstant.PERIODICITE_BIMENSUEL    -> 6;
            case ModeCalculInteretConstant.PERIODICITE_TRIMESTRIEL  -> 4;
            case ModeCalculInteretConstant.PERIODICITE_SEMESTRIEL   -> 2;
            case ModeCalculInteretConstant.PERIODICITE_ANNUEL       -> 1;
            default                                                  -> 12;
        };
    }

    // ── Frais prorata (assurance / commission par période) ────────

    /**
     * Répartit un frais annuel en parts égales sur les n échéances.
     * Calcul exact avec MC ; arrondi à SCALE seulement au retour.
     */
    private BigDecimal fraisProrata(BigDecimal base, BigDecimal tauxAnnuel, int n) {
        if (tauxAnnuel == null || tauxAnnuel.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return base
            .multiply(tauxAnnuel, MC)
            .divide(BigDecimal.valueOf(100), MC)
            .divide(BigDecimal.valueOf(n), MC)
            .setScale(SCALE, ROUNDING);
    }
}
