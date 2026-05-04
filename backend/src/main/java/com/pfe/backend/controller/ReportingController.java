package com.pfe.backend.controller;

import com.pfe.backend.export.ExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.pfe.backend.export.dto.BilanExportDto;
import com.pfe.backend.export.dto.EtatCreditExportDto;
import com.pfe.backend.export.dto.ListeClientExportDto;
import com.pfe.backend.export.dto.RatiosBcmExportDto;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * ReportingController — API JSON des vues de reporting Phase 10.4.
 *
 * <h2>Base path</h2>
 * <pre>GET /api/v1/reporting/...</pre>
 *
 * <h2>Vues exposées</h2>
 * <pre>
 *   GET /api/v1/reporting/etat-credits          — vue_etat_credits (filtre ?agence=)
 *   GET /api/v1/reporting/ratios-bcm            — vue_ratios_bcm
 *   GET /api/v1/reporting/indicateurs           — vue_indicateurs_performance
 *   GET /api/v1/reporting/liste-clients         — vue_liste_clients (filtre ?agence=)
 *   GET /api/v1/reporting/balance-comptes       — vue_balance_comptes (filtre ?agence=)
 *   GET /api/v1/reporting/journal               — vue_journal_comptable (filtre ?agence= &date=)
 *   GET /api/v1/reporting/bilan                 — vue_bilan
 * </pre>
 *
 * <h2>Sécurité</h2>
 * <p>Requiert {@code ROLE_ADMIN}, {@code ROLE_REPORT} ou {@code ROLE_AGENT}
 * (lecture seule de ses propres agences — contrôle fin laissé au frontend).</p>
 *
 * <h2>Réutilisation</h2>
 * <p>Les DTOs produits ici sont les mêmes que ceux utilisés par
 * {@link ExportService} — une seule source de vérité.</p>
 */
@Tag(name = "Reporting BCM", description = "Tableaux de bord et indicateurs du portefeuille")
@RestController
@RequestMapping("/api/v1/reporting")
@PreAuthorize("hasAuthority('PRIV_VIEW_REPORTS')")
public class ReportingController {

    private final JdbcTemplate jdbc;

    public ReportingController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ── État portefeuille crédit ──────────────────────────────────────────

    /**
     * @param agence filtre optionnel sur le code agence
     */
    @Operation(summary = "État du portefeuille crédit par agence")
    @GetMapping("/etat-credits")
    public ResponseEntity<List<EtatCreditExportDto>> etatCredits(
            @RequestParam(required = false) String agence) {

        String sql = """
            SELECT idcredit, numcredit, statut_credit, objet_credit,
                   num_membre, nom_membre, prenom_membre, sexe,
                   code_agence, nom_agence, nom_agent,
                   date_demande, date_deblocage, date_echeance_finale,
                   montant_accorde, montant_debloque,
                   solde_capital, solde_total_du,
                   jours_retard, total_arrieres, categorie_par,
                   total_garanties, taux_couverture_pct
            FROM vue_etat_credits
            """ + (agence != null ? "WHERE code_agence = ?" : "") +
            " ORDER BY statut_credit, numcredit";

        Object[] params = agence != null ? new Object[]{agence} : new Object[0];
        List<EtatCreditExportDto> data = jdbc.query(sql, params, (rs, n) ->
                new EtatCreditExportDto(
                        rs.getLong("idcredit"),
                        rs.getString("numcredit"),
                        rs.getString("statut_credit"),
                        rs.getString("objet_credit"),
                        rs.getString("num_membre"),
                        rs.getString("nom_membre"),
                        rs.getString("prenom_membre"),
                        rs.getString("sexe"),
                        rs.getString("code_agence"),
                        rs.getString("nom_agence"),
                        rs.getString("nom_agent"),
                        toLocalDate(rs.getDate("date_demande")),
                        toLocalDate(rs.getDate("date_deblocage")),
                        toLocalDate(rs.getDate("date_echeance_finale")),
                        rs.getBigDecimal("montant_accorde"),
                        rs.getBigDecimal("montant_debloque"),
                        rs.getBigDecimal("solde_capital"),
                        rs.getBigDecimal("solde_total_du"),
                        rs.getInt("jours_retard"),
                        rs.getBigDecimal("total_arrieres"),
                        rs.getString("categorie_par"),
                        rs.getBigDecimal("total_garanties"),
                        rs.getDouble("taux_couverture_pct")
                ));
        return ResponseEntity.ok(data);
    }

    // ── Ratios BCM ────────────────────────────────────────────────────────

    @Operation(summary = "Ratios prudentiels BCM (PAR30, PAR90, couverture)")
    @GetMapping("/ratios-bcm")
    @PreAuthorize("hasAnyAuthority('PRIV_VIEW_REPORTS','PRIV_EXPORT_REPORTS')")
    public ResponseEntity<List<RatiosBcmExportDto>> ratiosBcm() {
        List<RatiosBcmExportDto> data = jdbc.query("""
                SELECT code_agence, nom_agence,
                       encours_brut, encours_net, nb_credits_actifs,
                       capital_risque_par30, taux_par_30,
                       capital_risque_par90, taux_par_90,
                       total_arrieres, taux_portefeuille_risque,
                       total_rembourse, total_echu, taux_remboursement,
                       total_garanties, ratio_couverture_garanties
                FROM vue_ratios_bcm
                ORDER BY code_agence
                """, (rs, n) -> new RatiosBcmExportDto(
                rs.getString("code_agence"),
                rs.getString("nom_agence"),
                rs.getBigDecimal("encours_brut"),
                rs.getBigDecimal("encours_net"),
                rs.getInt("nb_credits_actifs"),
                rs.getBigDecimal("capital_risque_par30"),
                rs.getDouble("taux_par_30"),
                rs.getBigDecimal("capital_risque_par90"),
                rs.getDouble("taux_par_90"),
                rs.getBigDecimal("total_arrieres"),
                rs.getDouble("taux_portefeuille_risque"),
                rs.getBigDecimal("total_rembourse"),
                rs.getBigDecimal("total_echu"),
                rs.getDouble("taux_remboursement"),
                rs.getBigDecimal("total_garanties"),
                rs.getDouble("ratio_couverture_garanties")
        ));
        return ResponseEntity.ok(data);
    }

    // ── Indicateurs de performance ────────────────────────────────────────

    @Operation(summary = "Indicateurs de performance par agence")
    @GetMapping("/indicateurs")
    public ResponseEntity<List<Map<String, Object>>> indicateurs(
            @RequestParam(required = false) String agence) {

        String sql = """
            SELECT code_agence, nom_agence,
                   nb_credits_total, nb_credits_actifs, nb_credits_soldes,
                   montant_encours, montant_debloque_total,
                   nb_credits_retard, taux_credits_retard, nb_echeances_en_retard,
                   montant_arrieres, montant_rembourse_total,
                   montant_interet_percu, montant_commission_percu,
                   nb_reglements, total_echu, taux_remboursement_global,
                   nb_comptes_epargne_actifs, total_epargne, total_epargne_bloquee,
                   nb_membres_actifs, nb_membres_emprunteurs
            FROM vue_indicateurs_performance
            """ + (agence != null ? "WHERE code_agence = ?" : "") +
            " ORDER BY code_agence";

        Object[] params = agence != null ? new Object[]{agence} : new Object[0];
        return ResponseEntity.ok(jdbc.queryForList(sql, params));
    }

    // ── Liste clients ─────────────────────────────────────────────────────

    @Operation(summary = "Liste des clients par agence")
    @GetMapping("/liste-clients")
    public ResponseEntity<List<ListeClientExportDto>> listeClients(
            @RequestParam(required = false) String agence) {

        String sql = """
            SELECT num_membre, nom, prenom, sexe, datenaissance,
                   statut_membre, date_adhesion, code_agence, nom_agence,
                   nb_comptes_epargne, total_epargne, total_epargne_bloquee,
                   nb_credits_total, nb_credits_actifs,
                   montant_total_accorde, encours_capital,
                   nb_credits_retard, categorie_par_pire,
                   max_jours_retard, total_arrieres,
                   nb_garanties, total_garanties
            FROM vue_liste_clients
            """ + (agence != null ? "WHERE code_agence = ?" : "") +
            " ORDER BY nom, prenom";

        Object[] params = agence != null ? new Object[]{agence} : new Object[0];
        List<ListeClientExportDto> data = jdbc.query(sql, params, (rs, n) ->
                new ListeClientExportDto(
                        rs.getString("num_membre"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("sexe"),
                        toLocalDate(rs.getDate("datenaissance")),
                        rs.getString("statut_membre"),
                        toLocalDate(rs.getDate("date_adhesion")),
                        rs.getString("code_agence"),
                        rs.getString("nom_agence"),
                        rs.getInt("nb_comptes_epargne"),
                        rs.getBigDecimal("total_epargne"),
                        rs.getBigDecimal("total_epargne_bloquee"),
                        rs.getInt("nb_credits_total"),
                        rs.getInt("nb_credits_actifs"),
                        rs.getBigDecimal("montant_total_accorde"),
                        rs.getBigDecimal("encours_capital"),
                        rs.getInt("nb_credits_retard"),
                        rs.getString("categorie_par_pire"),
                        rs.getInt("max_jours_retard"),
                        rs.getBigDecimal("total_arrieres"),
                        rs.getInt("nb_garanties"),
                        rs.getBigDecimal("total_garanties")
                ));
        return ResponseEntity.ok(data);
    }

    // ── Balance des comptes ───────────────────────────────────────────────

    @Operation(summary = "Balance des comptes du plan comptable")
    @GetMapping("/balance-comptes")
    @PreAuthorize("hasAnyAuthority('PRIV_VIEW_REPORTS','PRIV_EXPORT_REPORTS')")
    public ResponseEntity<List<Map<String, Object>>> balanceComptes(
            @RequestParam(required = false) String agence) {

        String sql = """
            SELECT num_compte, code_agence, nom_agence, nb_ecritures,
                   total_debit, total_credit, solde_net,
                   solde_debiteur, solde_crediteur,
                   date_premiere_ecriture, date_derniere_ecriture
            FROM vue_balance_comptes
            """ + (agence != null ? "WHERE code_agence = ?" : "") +
            " ORDER BY num_compte, code_agence";

        Object[] params = agence != null ? new Object[]{agence} : new Object[0];
        return ResponseEntity.ok(jdbc.queryForList(sql, params));
    }

    // ── Journal comptable ─────────────────────────────────────────────────

    /**
     * @param agence filtre optionnel
     * @param date   filtre optionnel — format YYYY-MM-DD
     */
    @Operation(summary = "Journal comptable par agence et date")
    @GetMapping("/journal")
    @PreAuthorize("hasAnyAuthority('PRIV_VIEW_REPORTS','PRIV_EXPORT_REPORTS')")
    public ResponseEntity<List<Map<String, Object>>> journal(
            @RequestParam(required = false) String agence,
            @RequestParam(required = false) String date) {

        StringBuilder sql = new StringBuilder("""
            SELECT idcomptabilite, dateoperation, num_piece, libelle,
                   num_compte, compte_contrepartie, sens, debit, credit,
                   code_agence, nom_agence, agent_saisie, type_operation,
                   montant_net, idreglement, date_reglement,
                   montant_reglement, mode_paiement, statut_reglement,
                   numcredit_reglement
            FROM vue_journal_comptable
            WHERE 1=1
            """);

        java.util.List<Object> params = new java.util.ArrayList<>();
        if (agence != null) { sql.append(" AND code_agence = ?"); params.add(agence); }
        if (date   != null) { sql.append(" AND dateoperation = ?"); params.add(date); }
        sql.append(" ORDER BY dateoperation DESC, idcomptabilite DESC");

        return ResponseEntity.ok(jdbc.queryForList(sql.toString(), params.toArray()));
    }

    // ── Bilan ─────────────────────────────────────────────────────────────

    @Operation(summary = "Bilan simplifié par classe de compte")
    @GetMapping("/bilan")
    @PreAuthorize("hasAnyAuthority('PRIV_VIEW_REPORTS','PRIV_EXPORT_REPORTS')")
    public ResponseEntity<List<BilanExportDto>> bilan() {
        List<BilanExportDto> data = jdbc.query("""
                SELECT classe_compte, num_compte, rubrique, libelle_rubrique,
                       code_agence, nom_agence, nb_ecritures,
                       total_debit, total_credit, solde_net,
                       montant_actif, montant_passif,
                       date_premiere_ecriture, date_derniere_ecriture
                FROM vue_bilan
                ORDER BY classe_compte, num_compte, code_agence
                """, (rs, n) -> new BilanExportDto(
                rs.getString("classe_compte"),
                rs.getString("num_compte"),
                rs.getString("rubrique"),
                rs.getString("libelle_rubrique"),
                rs.getString("code_agence"),
                rs.getString("nom_agence"),
                rs.getLong("nb_ecritures"),
                rs.getBigDecimal("total_debit"),
                rs.getBigDecimal("total_credit"),
                rs.getBigDecimal("solde_net"),
                rs.getBigDecimal("montant_actif"),
                rs.getBigDecimal("montant_passif"),
                toLocalDate(rs.getDate("date_premiere_ecriture")),
                toLocalDate(rs.getDate("date_derniere_ecriture"))
        ));
        return ResponseEntity.ok(data);
    }

    // ── Grand livre ───────────────────────────────────────────────────────

    /**
     * Grand livre comptable — toutes les lignes d'écriture avec cumuls.
     *
     * @param agence filtre optionnel sur le code agence
     * @param compte filtre optionnel sur le numéro de compte (planComptable)
     */
    @Operation(summary = "Grand livre comptable par compte et agence")
    @GetMapping("/grand-livre")
    @PreAuthorize("hasAnyAuthority('PRIV_VIEW_REPORTS','PRIV_EXPORT_REPORTS')")
    public ResponseEntity<List<Map<String, Object>>> grandLivre(
            @RequestParam(required = false) String agence,
            @RequestParam(required = false) String compte) {

        StringBuilder sql = new StringBuilder("""
            SELECT idcomptabilite, dateoperation, num_piece_comptable,
                   num_compte, compte_auxiliaire, compte_tiers, compte_contrepartie,
                   sens, ref_credit_comptable, lettrage, etat_ecriture, marque_validation,
                   agence_ecriture, agent_saisie, statut_reglement, num_credit,
                   solde_capital_credit, date_echeance_credit, total_revenus_ecriture
            FROM vue_grand_livre
            WHERE 1=1
            """);

        java.util.List<Object> params = new java.util.ArrayList<>();
        if (agence != null) { sql.append(" AND agence_ecriture = ?"); params.add(agence); }
        if (compte != null) { sql.append(" AND num_compte LIKE ?"); params.add(compte + "%"); }
        sql.append(" ORDER BY dateoperation DESC, idcomptabilite DESC");

        return ResponseEntity.ok(jdbc.queryForList(sql.toString(), params.toArray()));
    }

    // ── Compte de résultat ────────────────────────────────────────────────

    /**
     * Compte de résultat par agence et période (produits - charges).
     *
     * @param agence filtre optionnel
     * @param annee  filtre optionnel (AAAA)
     * @param mois   filtre optionnel (1-12)
     */
    @Operation(summary = "Compte de résultat par agence et période")
    @GetMapping("/compte-resultat")
    @PreAuthorize("hasAnyAuthority('PRIV_VIEW_REPORTS','PRIV_EXPORT_REPORTS')")
    public ResponseEntity<List<Map<String, Object>>> compteResultat(
            @RequestParam(required = false) String agence,
            @RequestParam(required = false) Integer annee,
            @RequestParam(required = false) Integer mois) {

        StringBuilder sql = new StringBuilder("""
            SELECT annee, mois, code_agence, nom_agence,
                   interets_percus, commissions_percues, taxes_percues,
                   penalites_percues, assurances_percues, autres_produits_comptables,
                   total_produits, total_charges, resultat_net,
                   nb_reglements, montant_total_rembourse
            FROM vue_compte_resultat
            WHERE 1=1
            """);

        java.util.List<Object> params = new java.util.ArrayList<>();
        if (agence != null) { sql.append(" AND code_agence = ?"); params.add(agence); }
        if (annee  != null) { sql.append(" AND annee = ?"); params.add(annee); }
        if (mois   != null) { sql.append(" AND mois  = ?"); params.add(mois); }
        sql.append(" ORDER BY annee DESC, mois DESC, code_agence");

        return ResponseEntity.ok(jdbc.queryForList(sql.toString(), params.toArray()));
    }

    // ── Tableau de financement ────────────────────────────────────────────

    /**
     * Tableau de financement (flux d'entrées / sorties de trésorerie).
     *
     * @param agence filtre optionnel
     * @param date   filtre optionnel — format YYYY-MM-DD
     */
    @Operation(summary = "Tableau de financement (flux de trésorerie)")
    @GetMapping("/tableau-financement")
    @PreAuthorize("hasAnyAuthority('PRIV_VIEW_REPORTS','PRIV_EXPORT_REPORTS')")
    public ResponseEntity<List<Map<String, Object>>> tableauFinancement(
            @RequestParam(required = false) String agence,
            @RequestParam(required = false) String date) {

        StringBuilder sql = new StringBuilder("""
            SELECT date_operation, code_agence, nom_agence,
                   remboursements_recus, depots_caisse, depots_banque, total_entrees,
                   deblocages_credits, retraits_caisse, retraits_banque, total_sorties,
                   solde_net_jour
            FROM vue_tableau_financement
            WHERE 1=1
            """);

        java.util.List<Object> params = new java.util.ArrayList<>();
        if (agence != null) { sql.append(" AND code_agence = ?"); params.add(agence); }
        if (date   != null) { sql.append(" AND date_operation = ?"); params.add(date); }
        sql.append(" ORDER BY date_operation DESC, code_agence");

        return ResponseEntity.ok(jdbc.queryForList(sql.toString(), params.toArray()));
    }

    // ── Utilitaire ────────────────────────────────────────────────────────

    private static java.time.LocalDate toLocalDate(java.sql.Date d) {
        return d != null ? d.toLocalDate() : null;
    }
}
