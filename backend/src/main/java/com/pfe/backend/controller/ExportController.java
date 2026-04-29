package com.pfe.backend.controller;

import com.pfe.backend.export.ExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * ExportController — endpoints REST pour les exports de rapports BCM.
 *
 * <h2>Base path</h2>
 * <pre>GET /api/v1/export/...</pre>
 *
 * <h2>Sécurité</h2>
 * <p>Tous les endpoints requièrent le rôle {@code ROLE_REPORT} ou
 * {@code ROLE_ADMIN}. Les exports contiennent des données financières
 * sensibles soumises au secret BCM.</p>
 *
 * <h2>Paramètre commun</h2>
 * <p>{@code agence} (optionnel) — filtre les données sur une agence précise.
 * Si absent ou vide, l'export couvre toutes les agences.</p>
 *
 * <h2>Formats de réponse</h2>
 * <ul>
 *   <li>{@code application/vnd.openxmlformats-officedocument.spreadsheetml.sheet} — Excel</li>
 *   <li>{@code application/vnd.openxmlformats-officedocument.wordprocessingml.document} — Word</li>
 *   <li>{@code application/pdf} — PDF</li>
 * </ul>
 */
@Tag(name = "Export BCM", description = "Exports Excel, Word et PDF des états réglementaires BCM")
@RestController
@RequestMapping("/api/v1/export")
@PreAuthorize("hasAuthority('PRIV_EXPORT_REPORTS')")
public class ExportController {

    private static final DateTimeFormatter DATE_FICHIER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final ExportService exportService;

    public ExportController(ExportService exportService) {
        this.exportService = exportService;
    }

    // =========================================================================
    //  Excel
    // =========================================================================

    /**
     * Exporte le portefeuille crédit au format Excel.
     *
     * <pre>GET /api/v1/export/credits/excel?agence=AG001</pre>
     *
     * @param agence code agence (optionnel)
     */
    @Operation(summary = "Portefeuille crédit — Excel")
    @GetMapping("/credits/excel")
    public ResponseEntity<byte[]> exportPortefeuilleCreditExcel(
            @RequestParam(required = false) String agence) throws IOException {

        byte[] data = exportService.exportPortefeuilleCreditExcel(agence);
        String nom  = "portefeuille_credit_" + today() + ".xlsx";
        return excelResponse(data, nom);
    }

    /**
     * Exporte les ratios prudentiels BCM au format Excel.
     *
     * <pre>GET /api/v1/export/ratios-bcm/excel</pre>
     */
    @Operation(summary = "Ratios BCM — Excel")
    @GetMapping("/ratios-bcm/excel")
    public ResponseEntity<byte[]> exportRatiosBcmExcel() throws IOException {
        byte[] data = exportService.exportRatiosBcmExcel();
        return excelResponse(data, "ratios_bcm_" + today() + ".xlsx");
    }

    /**
     * Exporte le bilan simplifié au format Excel.
     *
     * <pre>GET /api/v1/export/bilan/excel</pre>
     */
    @Operation(summary = "Bilan — Excel")
    @GetMapping("/bilan/excel")
    public ResponseEntity<byte[]> exportBilanExcel() throws IOException {
        byte[] data = exportService.exportBilanExcel();
        return excelResponse(data, "bilan_" + today() + ".xlsx");
    }

    // =========================================================================
    //  Word
    // =========================================================================

    /**
     * Génère le rapport financier narratif (bilan + ratios BCM) au format Word.
     *
     * <pre>GET /api/v1/export/rapport-financier/word</pre>
     */
    @Operation(summary = "Rapport financier — Word")
    @GetMapping("/rapport-financier/word")
    public ResponseEntity<byte[]> exportRapportFinancierWord() throws IOException {
        byte[] data = exportService.exportRapportFinancierWord();
        return wordResponse(data, "rapport_financier_" + today() + ".docx");
    }

    // =========================================================================
    //  PDF
    // =========================================================================

    /**
     * Exporte la liste des clients au format PDF.
     *
     * <pre>GET /api/v1/export/clients/pdf?agence=AG001</pre>
     *
     * @param agence code agence (optionnel)
     */
    @Operation(summary = "Liste des clients — PDF")
    @GetMapping("/clients/pdf")
    public ResponseEntity<byte[]> exportListeClientsPdf(
            @RequestParam(required = false) String agence) throws IOException {

        byte[] data = exportService.exportListeClientsPdf(agence);
        String nom  = "liste_clients_" + today() + ".pdf";
        return pdfResponse(data, nom);
    }

    /**
     * Exporte le rapport ratios BCM au format PDF.
     *
     * <pre>GET /api/v1/export/ratios-bcm/pdf</pre>
     */
    @Operation(summary = "Ratios BCM — PDF")
    @GetMapping("/ratios-bcm/pdf")
    public ResponseEntity<byte[]> exportRatiosBcmPdf() throws IOException {
        byte[] data = exportService.exportRatiosBcmPdf();
        return pdfResponse(data, "ratios_bcm_" + today() + ".pdf");
    }

    // ── Indicateurs ────────────────────────────────────────────────────────

    @Operation(summary = "Indicateurs de performance par agence — Excel")
    @GetMapping("/indicateurs/excel")
    public ResponseEntity<byte[]> exportIndicateursExcel(
            @RequestParam(required = false) String agence) throws IOException {
        byte[] data = exportService.exportIndicateursExcel(agence);
        return excelResponse(data, "indicateurs_" + today() + ".xlsx");
    }

    // ── Liste clients ──────────────────────────────────────────────────────

    @Operation(summary = "Liste des clients — Excel")
    @GetMapping("/liste-clients/excel")
    public ResponseEntity<byte[]> exportListeClientsExcel(
            @RequestParam(required = false) String agence) throws IOException {
        byte[] data = exportService.exportListeClientsExcel(agence);
        return excelResponse(data, "liste_clients_" + today() + ".xlsx");
    }

    @Operation(summary = "Liste des clients — Word")
    @GetMapping("/liste-clients/word")
    public ResponseEntity<byte[]> exportListeClientsWord(
            @RequestParam(required = false) String agence) throws IOException {
        // Reuse PDF export for now — extend ExportService if word version needed
        byte[] data = exportService.exportListeClientsPdf(agence);
        return pdfResponse(data, "liste_clients_" + today() + ".pdf");
    }

    // ── Balance des comptes ────────────────────────────────────────────────

    @Operation(summary = "Balance des comptes — Excel")
    @GetMapping("/balance-comptes/excel")
    public ResponseEntity<byte[]> exportBalanceComptesExcel(
            @RequestParam(required = false) String agence) throws IOException {
        byte[] data = exportService.exportBalanceComptesExcel(agence);
        return excelResponse(data, "balance_comptes_" + today() + ".xlsx");
    }

    @Operation(summary = "Balance des comptes — PDF")
    @GetMapping("/balance-comptes/pdf")
    public ResponseEntity<byte[]> exportBalanceComptesPdf(
            @RequestParam(required = false) String agence) throws IOException {
        byte[] data = exportService.exportBalanceComptesPdf(agence);
        return pdfResponse(data, "balance_comptes_" + today() + ".pdf");
    }

    // ── Journal comptable ──────────────────────────────────────────────────

    @Operation(summary = "Journal comptable — Excel")
    @GetMapping("/journal/excel")
    public ResponseEntity<byte[]> exportJournalExcel(
            @RequestParam(required = false) String agence,
            @RequestParam(required = false) String date) throws IOException {
        byte[] data = exportService.exportJournalExcel(agence, date);
        return excelResponse(data, "journal_" + today() + ".xlsx");
    }

    @Operation(summary = "Journal comptable — PDF")
    @GetMapping("/journal/pdf")
    public ResponseEntity<byte[]> exportJournalPdf(
            @RequestParam(required = false) String agence,
            @RequestParam(required = false) String date) throws IOException {
        byte[] data = exportService.exportJournalPdf(agence, date);
        return pdfResponse(data, "journal_" + today() + ".pdf");
    }

    // ── Bilan ──────────────────────────────────────────────────────────────

    @Operation(summary = "Bilan simplifié — PDF")
    @GetMapping("/bilan/pdf")
    public ResponseEntity<byte[]> exportBilanPdf() throws IOException {
        byte[] data = exportService.exportBilanPdf();
        return pdfResponse(data, "bilan_" + today() + ".pdf");
    }

    @Operation(summary = "Bilan simplifié — Word")
    @GetMapping("/bilan/word")
    public ResponseEntity<byte[]> exportBilanWord() throws IOException {
        byte[] data = exportService.exportBilanWord();
        return wordResponse(data, "bilan_" + today() + ".docx");
    }

    // ── Export Sage Compta ligne L ─────────────────────────────────────────────

    /**
     * Exporte le journal comptable au format Sage Compta ligne L (CSV).
     *
     * <pre>GET /api/v1/export/comptable/sage?agence=AG001</pre>
     *
     * <p>Format : une ligne par écriture comptable, type L, séparateur point-virgule,
     * encodage UTF-8 avec BOM pour compatibilité Excel.</p>
     *
     * @param agence code agence (optionnel)
     */
    @Operation(summary = "Export Sage Compta ligne L — CSV")
    @GetMapping("/comptable/sage")
    public ResponseEntity<byte[]> exportSageComptaL(
            @RequestParam(required = false) String agence) {

        byte[] data = exportService.exportSageComptaL(agence);
        String nom  = "sage_compta_L_" + today() + ".csv";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .headers(attachmentHeaders(nom))
                .body(data);
    }

    // ── Portefeuille crédit — Word ─────────────────────────────────────────

    @Operation(summary = "Portefeuille crédit — Word")
    @GetMapping("/credits/word")
    public ResponseEntity<byte[]> exportPortefeuilleCreditWord(
            @RequestParam(required = false) String agence) throws IOException {
        byte[] data = exportService.exportPortefeuilleCreditWord(agence);
        return wordResponse(data, "portefeuille_credit_" + today() + ".docx");
    }

    // =========================================================================
    //  Endpoints spécifiques 12 états × 3 formats (section cahier §3.1.3)
    // =========================================================================

    // ── credits ────────────────────────────────────────────────────────────────

    @Operation(summary = "Export credits au format pdf")
    @GetMapping("/credits/pdf")
    @PreAuthorize("hasAuthority('PRIV_VIEW_REPORTS')")
    public ResponseEntity<byte[]> exportCreditsPdf(
            @RequestParam(required = false) String agence) throws IOException {
        byte[] data = exportService.export("credits", "pdf", agence);
        return pdfResponse(data, "credits_" + today() + ".pdf");
    }

    // ── balance ────────────────────────────────────────────────────────────────

    @Operation(summary = "Export balance au format excel")
    @GetMapping("/balance/excel")
    @PreAuthorize("hasAuthority('PRIV_VIEW_REPORTS')")
    public ResponseEntity<byte[]> exportBalanceExcel(
            @RequestParam(required = false) String agence) throws IOException {
        byte[] data = exportService.export("balance", "excel", agence);
        return excelResponse(data, "balance_" + today() + ".xlsx");
    }

    @Operation(summary = "Export balance au format pdf")
    @GetMapping("/balance/pdf")
    @PreAuthorize("hasAuthority('PRIV_VIEW_REPORTS')")
    public ResponseEntity<byte[]> exportBalancePdf(
            @RequestParam(required = false) String agence) throws IOException {
        byte[] data = exportService.export("balance", "pdf", agence);
        return pdfResponse(data, "balance_" + today() + ".pdf");
    }

    @Operation(summary = "Export balance au format word")
    @GetMapping("/balance/word")
    @PreAuthorize("hasAuthority('PRIV_VIEW_REPORTS')")
    public ResponseEntity<byte[]> exportBalanceWord(
            @RequestParam(required = false) String agence) throws IOException {
        byte[] data = exportService.export("balance", "word", agence);
        return wordResponse(data, "balance_" + today() + ".docx");
    }

    // ── journal ────────────────────────────────────────────────────────────────

    @Operation(summary = "Export journal au format word")
    @GetMapping("/journal/word")
    @PreAuthorize("hasAuthority('PRIV_VIEW_REPORTS')")
    public ResponseEntity<byte[]> exportJournalWord(
            @RequestParam(required = false) String agence) throws IOException {
        byte[] data = exportService.export("journal", "word", agence);
        return wordResponse(data, "journal_" + today() + ".docx");
    }

    // ── grand-livre ────────────────────────────────────────────────────────────

    @Operation(summary = "Export grand-livre au format excel")
    @GetMapping("/grand-livre/excel")
    @PreAuthorize("hasAuthority('PRIV_VIEW_REPORTS')")
    public ResponseEntity<byte[]> exportGrandLivreExcel(
            @RequestParam(required = false) String agence) throws IOException {
        byte[] data = exportService.export("grand-livre", "excel", agence);
        return excelResponse(data, "grand-livre_" + today() + ".xlsx");
    }

    @Operation(summary = "Export grand-livre au format pdf")
    @GetMapping("/grand-livre/pdf")
    @PreAuthorize("hasAuthority('PRIV_VIEW_REPORTS')")
    public ResponseEntity<byte[]> exportGrandLivrePdf(
            @RequestParam(required = false) String agence) throws IOException {
        byte[] data = exportService.export("grand-livre", "pdf", agence);
        return pdfResponse(data, "grand-livre_" + today() + ".pdf");
    }

    @Operation(summary = "Export grand-livre au format word")
    @GetMapping("/grand-livre/word")
    @PreAuthorize("hasAuthority('PRIV_VIEW_REPORTS')")
    public ResponseEntity<byte[]> exportGrandLivreWord(
            @RequestParam(required = false) String agence) throws IOException {
        byte[] data = exportService.export("grand-livre", "word", agence);
        return wordResponse(data, "grand-livre_" + today() + ".docx");
    }

    // ── compte-resultat ────────────────────────────────────────────────────────

    @Operation(summary = "Export compte-resultat au format excel")
    @GetMapping("/compte-resultat/excel")
    @PreAuthorize("hasAuthority('PRIV_VIEW_REPORTS')")
    public ResponseEntity<byte[]> exportCompteResultatExcel(
            @RequestParam(required = false) String agence) throws IOException {
        byte[] data = exportService.export("compte-resultat", "excel", agence);
        return excelResponse(data, "compte-resultat_" + today() + ".xlsx");
    }

    @Operation(summary = "Export compte-resultat au format pdf")
    @GetMapping("/compte-resultat/pdf")
    @PreAuthorize("hasAuthority('PRIV_VIEW_REPORTS')")
    public ResponseEntity<byte[]> exportCompteResultatPdf(
            @RequestParam(required = false) String agence) throws IOException {
        byte[] data = exportService.export("compte-resultat", "pdf", agence);
        return pdfResponse(data, "compte-resultat_" + today() + ".pdf");
    }

    @Operation(summary = "Export compte-resultat au format word")
    @GetMapping("/compte-resultat/word")
    @PreAuthorize("hasAuthority('PRIV_VIEW_REPORTS')")
    public ResponseEntity<byte[]> exportCompteResultatWord(
            @RequestParam(required = false) String agence) throws IOException {
        byte[] data = exportService.export("compte-resultat", "word", agence);
        return wordResponse(data, "compte-resultat_" + today() + ".docx");
    }

    // ── tableau-financement ────────────────────────────────────────────────────

    @Operation(summary = "Export tableau-financement au format excel")
    @GetMapping("/tableau-financement/excel")
    @PreAuthorize("hasAuthority('PRIV_VIEW_REPORTS')")
    public ResponseEntity<byte[]> exportTableauFinancementExcel(
            @RequestParam(required = false) String agence) throws IOException {
        byte[] data = exportService.export("tableau-financement", "excel", agence);
        return excelResponse(data, "tableau-financement_" + today() + ".xlsx");
    }

    @Operation(summary = "Export tableau-financement au format pdf")
    @GetMapping("/tableau-financement/pdf")
    @PreAuthorize("hasAuthority('PRIV_VIEW_REPORTS')")
    public ResponseEntity<byte[]> exportTableauFinancementPdf(
            @RequestParam(required = false) String agence) throws IOException {
        byte[] data = exportService.export("tableau-financement", "pdf", agence);
        return pdfResponse(data, "tableau-financement_" + today() + ".pdf");
    }

    @Operation(summary = "Export tableau-financement au format word")
    @GetMapping("/tableau-financement/word")
    @PreAuthorize("hasAuthority('PRIV_VIEW_REPORTS')")
    public ResponseEntity<byte[]> exportTableauFinancementWord(
            @RequestParam(required = false) String agence) throws IOException {
        byte[] data = exportService.export("tableau-financement", "word", agence);
        return wordResponse(data, "tableau-financement_" + today() + ".docx");
    }

    // ── balance-agee ───────────────────────────────────────────────────────────

    @Operation(summary = "Export balance-agee au format excel")
    @GetMapping("/balance-agee/excel")
    @PreAuthorize("hasAuthority('PRIV_VIEW_REPORTS')")
    public ResponseEntity<byte[]> exportBalanceAgeeExcel(
            @RequestParam(required = false) String agence) throws IOException {
        byte[] data = exportService.export("balance-agee", "excel", agence);
        return excelResponse(data, "balance-agee_" + today() + ".xlsx");
    }

    @Operation(summary = "Export balance-agee au format pdf")
    @GetMapping("/balance-agee/pdf")
    @PreAuthorize("hasAuthority('PRIV_VIEW_REPORTS')")
    public ResponseEntity<byte[]> exportBalanceAgeePdf(
            @RequestParam(required = false) String agence) throws IOException {
        byte[] data = exportService.export("balance-agee", "pdf", agence);
        return pdfResponse(data, "balance-agee_" + today() + ".pdf");
    }

    @Operation(summary = "Export balance-agee au format word")
    @GetMapping("/balance-agee/word")
    @PreAuthorize("hasAuthority('PRIV_VIEW_REPORTS')")
    public ResponseEntity<byte[]> exportBalanceAgeeWord(
            @RequestParam(required = false) String agence) throws IOException {
        byte[] data = exportService.export("balance-agee", "word", agence);
        return wordResponse(data, "balance-agee_" + today() + ".docx");
    }

    // ── ratios ─────────────────────────────────────────────────────────────────

    @Operation(summary = "Export ratios au format excel")
    @GetMapping("/ratios/excel")
    @PreAuthorize("hasAuthority('PRIV_VIEW_REPORTS')")
    public ResponseEntity<byte[]> exportRatiosExcel(
            @RequestParam(required = false) String agence) throws IOException {
        byte[] data = exportService.export("ratios", "excel", agence);
        return excelResponse(data, "ratios_" + today() + ".xlsx");
    }

    // =========================================================================
    //  Endpoint unifié
    // =========================================================================

    @Operation(summary = "Export unifié — GET /api/v1/export/{etat}/{format}")
    @GetMapping("/{etat}/{format}")
    public ResponseEntity<byte[]> exportUnifie(
            @PathVariable String etat,
            @PathVariable String format,
            @RequestParam(required = false) String agence) throws IOException {
        byte[] data = exportService.export(etat, format, agence);
        String filename = etat + "_" + format + "_" + today() + "." + ext(format);
        return switch (format) {
            case "excel" -> excelResponse(data, filename);
            case "word"  -> wordResponse(data, filename);
            default      -> pdfResponse(data, filename);
        };
    }

    private static String ext(String format) {
        return switch (format) {
            case "excel" -> "xlsx";
            case "word"  -> "docx";
            default      -> "pdf";
        };
    }

    // =========================================================================
    //  Helpers
    // =========================================================================

    private static final MediaType EXCEL =
            MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private static final MediaType WORD =
            MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document");

    private ResponseEntity<byte[]> excelResponse(byte[] data, String filename) {
        return ResponseEntity.ok()
                .contentType(EXCEL)
                .headers(attachmentHeaders(filename))
                .body(data);
    }

    private ResponseEntity<byte[]> wordResponse(byte[] data, String filename) {
        return ResponseEntity.ok()
                .contentType(WORD)
                .headers(attachmentHeaders(filename))
                .body(data);
    }

    private ResponseEntity<byte[]> pdfResponse(byte[] data, String filename) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .headers(attachmentHeaders(filename))
                .body(data);
    }

    private HttpHeaders attachmentHeaders(String filename) {
        HttpHeaders h = new HttpHeaders();
        h.setContentDisposition(
                ContentDisposition.attachment().filename(filename).build());
        return h;
    }

    private String today() {
        return LocalDate.now().format(DATE_FICHIER);
    }
}
