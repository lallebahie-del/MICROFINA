package com.pfe.backend.export;

import com.lowagie.text.*;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.pfe.backend.exception.BusinessException;
import com.pfe.backend.export.dto.BilanExportDto;
import com.pfe.backend.export.dto.EtatCreditExportDto;
import com.pfe.backend.export.dto.ListeClientExportDto;
import com.pfe.backend.export.dto.RatiosBcmExportDto;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ExportService — génération de fichiers Excel, Word et PDF à partir des vues
 * de reporting Phase 10.4.
 *
 * <h2>Formats produits</h2>
 * <ul>
 *   <li><b>Excel (.xlsx)</b> — portefeuille crédit, ratios BCM, bilan, liste clients</li>
 *   <li><b>Word (.docx)</b> — rapport bilan + compte de résultat narratif</li>
 *   <li><b>PDF (.pdf)</b> — liste clients, rapport ratios BCM</li>
 * </ul>
 *
 * <h2>Sources de données</h2>
 * <p>Toutes les requêtes attaquent les vues Phase 10.4 via {@link JdbcTemplate}
 * (pas d'entités JPA pour les vues SQL Server). Aucune requête directe sur les
 * tables sources — conformément à la règle d'usage définie en P10-001d.</p>
 *
 * <h2>Thread-safety</h2>
 * <p>{@code JdbcTemplate} est thread-safe ; chaque méthode crée son propre
 * workbook/document en mémoire et renvoie un {@code byte[]} immutable.</p>
 */
@Service
public class ExportService {

    private static final DateTimeFormatter DATE_FR = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String MRU = "MRU";

    private final JdbcTemplate jdbc;

    public ExportService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // =========================================================================
    //  EXCEL — Portefeuille crédit (vue_etat_credits)
    // =========================================================================

    /**
     * Exporte le portefeuille crédit complet au format Excel.
     *
     * @param agence filtre agence (null = toutes agences)
     * @return contenu du fichier .xlsx en mémoire
     */
    public byte[] exportPortefeuilleCreditExcel(String agence) throws IOException {

        List<EtatCreditExportDto> lignes = queryEtatCredits(agence);

        try (XSSFWorkbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = wb.createSheet("Portefeuille Crédit");
            sheet.setDefaultColumnWidth(18);

            // ── Styles ────────────────────────────────────────────────────
            CellStyle entete  = styleEntete(wb);
            CellStyle montant = styleMontant(wb);
            CellStyle pct     = stylePourcentage(wb);
            CellStyle normal  = styleNormal(wb);
            CellStyle alerte  = styleAlerte(wb);

            // ── Titre ─────────────────────────────────────────────────────
            Row titre = sheet.createRow(0);
            Cell cellTitre = titre.createCell(0);
            cellTitre.setCellValue("PORTEFEUILLE CRÉDIT — " +
                    (agence != null ? "Agence " + agence : "Toutes agences") +
                    " — " + LocalDate.now().format(DATE_FR));
            cellTitre.setCellStyle(styleTitre(wb));
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 17));

            // ── En-têtes colonnes ─────────────────────────────────────────
            String[] cols = {
                "N° Crédit", "Statut", "Membre", "Prénom", "Agence", "Agent",
                "Dt Déblocage", "Dt Échéance", "Mnt Accordé", "Mnt Débloqué",
                "Solde Capital", "Solde Total", "J. Retard",
                "Total Arriérés", "Catégorie PAR", "Total Garanties",
                "Tx Couverture %", "Objet"
            };
            Row rowEntete = sheet.createRow(1);
            for (int i = 0; i < cols.length; i++) {
                Cell c = rowEntete.createCell(i);
                c.setCellValue(cols[i]);
                c.setCellStyle(entete);
            }

            // ── Données ───────────────────────────────────────────────────
            int rowIdx = 2;
            for (EtatCreditExportDto d : lignes) {
                Row row = sheet.createRow(rowIdx++);
                boolean enRetard = d.joursRetard() != null && d.joursRetard() > 0;

                setCell(row,  0, d.numCredit(),                                    enRetard ? alerte : normal);
                setCell(row,  1, d.statutCredit(),                                 normal);
                setCell(row,  2, d.nomMembre(),                                    normal);
                setCell(row,  3, d.prenomMembre(),                                 normal);
                setCell(row,  4, d.nomAgence(),                                    normal);
                setCell(row,  5, d.nomAgent(),                                     normal);
                setCell(row,  6, formatDate(d.dateDeblocage()),                    normal);
                setCell(row,  7, formatDate(d.dateEcheanceFinale()),               normal);
                setCellNum(row, 8,  d.montantAccorde(),                            montant);
                setCellNum(row, 9,  d.montantDebloque(),                           montant);
                setCellNum(row, 10, d.soldeCapital(),                              montant);
                setCellNum(row, 11, d.soldeTotal(),                                montant);
                setCell(row, 12, d.joursRetard() != null ? d.joursRetard().toString() : "0", enRetard ? alerte : normal);
                setCellNum(row, 13, d.totalArrieres(),                             enRetard ? alerte : montant);
                setCell(row, 14, d.categoriePar(),                                 enRetard ? alerte : normal);
                setCellNum(row, 15, d.totalGaranties(),                            montant);
                setCellPct(row, 16, d.tauxCouverturePct(),                         pct);
                setCell(row, 17, d.objetCredit(),                                  normal);
            }

            // ── Ligne totaux ──────────────────────────────────────────────
            Row totaux = sheet.createRow(rowIdx);
            setCell(totaux, 0, "TOTAUX", entete);
            setCellNum(totaux, 8,  sumBD(lignes, EtatCreditExportDto::montantAccorde),  entete);
            setCellNum(totaux, 9,  sumBD(lignes, EtatCreditExportDto::montantDebloque), entete);
            setCellNum(totaux, 10, sumBD(lignes, EtatCreditExportDto::soldeCapital),    entete);
            setCellNum(totaux, 13, sumBD(lignes, EtatCreditExportDto::totalArrieres),   entete);
            setCellNum(totaux, 15, sumBD(lignes, EtatCreditExportDto::totalGaranties),  entete);

            // Figer la rangée d'en-têtes
            sheet.createFreezePane(0, 2);

            wb.write(out);
            return out.toByteArray();
        }
    }

    // =========================================================================
    //  EXCEL — Ratios BCM (vue_ratios_bcm)
    // =========================================================================

    /**
     * Exporte les ratios prudentiels BCM par agence au format Excel.
     */
    public byte[] exportRatiosBcmExcel() throws IOException {

        List<RatiosBcmExportDto> lignes = queryRatiosBcm();

        try (XSSFWorkbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = wb.createSheet("Ratios BCM");
            sheet.setDefaultColumnWidth(20);

            CellStyle entete  = styleEntete(wb);
            CellStyle montant = styleMontant(wb);
            CellStyle pct     = stylePourcentage(wb);
            CellStyle normal  = styleNormal(wb);

            // Titre
            Row titre = sheet.createRow(0);
            Cell t = titre.createCell(0);
            t.setCellValue("RATIOS PRUDENTIELS BCM — " + LocalDate.now().format(DATE_FR));
            t.setCellStyle(styleTitre(wb));
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 15));

            // En-têtes
            String[] cols = {
                "Agence", "Nom Agence",
                "Encours Brut", "Encours Net", "Nb Crédits",
                "Capital PAR30", "Taux PAR30 %",
                "Capital PAR90", "Taux PAR90 %",
                "Total Arriérés", "Tx Portefeuille Risque %",
                "Total Remboursé", "Total Échu", "Tx Remboursement %",
                "Total Garanties", "Ratio Couverture %"
            };
            Row rowE = sheet.createRow(1);
            for (int i = 0; i < cols.length; i++) {
                Cell c = rowE.createCell(i);
                c.setCellValue(cols[i]);
                c.setCellStyle(entete);
            }

            int rowIdx = 2;
            for (RatiosBcmExportDto d : lignes) {
                Row row = sheet.createRow(rowIdx++);
                setCell(row,  0, d.codeAgence(),          normal);
                setCell(row,  1, d.nomAgence(),           normal);
                setCellNum(row,  2, d.encoursBrut(),       montant);
                setCellNum(row,  3, d.encoursNet(),        montant);
                setCell(row,  4, d.nbCreditsActifs() != null ? d.nbCreditsActifs().toString() : "0", normal);
                setCellNum(row,  5, d.capitalRisquePar30(), montant);
                setCellPct(row,  6, d.tauxPar30() != null ? d.tauxPar30() * 100 : 0.0, pct);
                setCellNum(row,  7, d.capitalRisquePar90(), montant);
                setCellPct(row,  8, d.tauxPar90() != null ? d.tauxPar90() * 100 : 0.0, pct);
                setCellNum(row,  9, d.totalArrieres(),     montant);
                setCellPct(row, 10, d.tauxPortefeuilleRisque() != null ? d.tauxPortefeuilleRisque() * 100 : 0.0, pct);
                setCellNum(row, 11, d.totalRembourse(),    montant);
                setCellNum(row, 12, d.totalEchu(),         montant);
                setCellPct(row, 13, d.tauxRemboursement() != null ? d.tauxRemboursement() * 100 : 0.0, pct);
                setCellNum(row, 14, d.totalGaranties(),    montant);
                setCellPct(row, 15, d.ratioCouvertureGaranties() != null ? d.ratioCouvertureGaranties() * 100 : 0.0, pct);
            }

            sheet.createFreezePane(0, 2);
            wb.write(out);
            return out.toByteArray();
        }
    }

    // =========================================================================
    //  EXCEL — Bilan (vue_bilan)
    // =========================================================================

    /**
     * Exporte le bilan simplifié au format Excel, organisé en deux colonnes
     * ACTIF / PASSIF par classe de compte.
     */
    public byte[] exportBilanExcel() throws IOException {

        List<BilanExportDto> lignes = queryBilan();

        try (XSSFWorkbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = wb.createSheet("Bilan");
            sheet.setDefaultColumnWidth(22);

            CellStyle entete  = styleEntete(wb);
            CellStyle montant = styleMontant(wb);
            CellStyle normal  = styleNormal(wb);

            Row titre = sheet.createRow(0);
            Cell t = titre.createCell(0);
            t.setCellValue("BILAN SIMPLIFIÉ SYSCOHADA — " + LocalDate.now().format(DATE_FR));
            t.setCellStyle(styleTitre(wb));
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 9));

            String[] cols = {
                "Classe", "N° Compte", "Rubrique", "Libellé Rubrique",
                "Agence", "Nb Écritures",
                "Total Débit", "Total Crédit",
                "Montant ACTIF", "Montant PASSIF"
            };
            Row rowE = sheet.createRow(1);
            for (int i = 0; i < cols.length; i++) {
                Cell c = rowE.createCell(i);
                c.setCellValue(cols[i]);
                c.setCellStyle(entete);
            }

            int rowIdx = 2;
            for (BilanExportDto d : lignes) {
                Row row = sheet.createRow(rowIdx++);
                setCell(row, 0, d.classeCompte(), normal);
                setCell(row, 1, d.numCompte(),    normal);
                setCell(row, 2, d.rubrique(),     normal);
                setCell(row, 3, d.libelleRubrique(), normal);
                setCell(row, 4, d.nomAgence(),    normal);
                setCell(row, 5, d.nbEcritures() != null ? d.nbEcritures().toString() : "0", normal);
                setCellNum(row, 6, d.totalDebit(),    montant);
                setCellNum(row, 7, d.totalCredit(),   montant);
                setCellNum(row, 8, d.montantActif(),  montant);
                setCellNum(row, 9, d.montantPassif(), montant);
            }

            // Totaux ACTIF / PASSIF
            Row tot = sheet.createRow(rowIdx);
            setCell(tot, 2, "TOTAL", entete);
            setCellNum(tot, 8, lignes.stream()
                    .map(BilanExportDto::montantActif)
                    .filter(v -> v != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add), entete);
            setCellNum(tot, 9, lignes.stream()
                    .map(BilanExportDto::montantPassif)
                    .filter(v -> v != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add), entete);

            sheet.createFreezePane(0, 2);
            wb.write(out);
            return out.toByteArray();
        }
    }

    // =========================================================================
    //  WORD — Rapport financier (bilan narratif + compte de résultat)
    // =========================================================================

    /**
     * Génère un rapport Word narratif consolidant le bilan et le compte de résultat.
     *
     * @return contenu du fichier .docx en mémoire
     */
    public byte[] exportRapportFinancierWord() throws IOException {

        List<BilanExportDto> bilan   = queryBilan();
        List<RatiosBcmExportDto> ratios = queryRatiosBcm();

        try (XWPFDocument doc = new XWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // ── Page de titre ─────────────────────────────────────────────
            XWPFParagraph titreP = doc.createParagraph();
            titreP.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titreR = titreP.createRun();
            titreR.setBold(true);
            titreR.setFontSize(18);
            titreR.setText("RAPPORT FINANCIER — MICROFINA");
            titreR.addBreak();
            titreR.setFontSize(12);
            titreR.setText("État arrêté au " + LocalDate.now().format(DATE_FR));

            doc.createParagraph(); // espace

            // ── Section 1 : Bilan ─────────────────────────────────────────
            ajouterTitreSection(doc, "1. BILAN SIMPLIFIÉ");

            BigDecimal totalActif  = bilan.stream()
                    .map(BilanExportDto::montantActif)
                    .filter(v -> v != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalPassif = bilan.stream()
                    .map(BilanExportDto::montantPassif)
                    .filter(v -> v != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            ajouterParagraphe(doc,
                    "Le total de l'actif s'élève à " + formatMontant(totalActif) + " " + MRU + ". " +
                    "Le total du passif s'élève à " + formatMontant(totalPassif) + " " + MRU + ".");

            // Tableau bilan (regroupé par rubrique)
            XWPFTable tBilan = doc.createTable(2, 3);
            styleEnteteTableWord(tBilan.getRow(0), new String[]{"Rubrique", "Montant ACTIF (" + MRU + ")", "Montant PASSIF (" + MRU + ")"});
            BigDecimal actifTotal  = BigDecimal.ZERO;
            BigDecimal passifTotal = BigDecimal.ZERO;
            for (String rubrique : List.of("ACTIF", "PASSIF")) {
                BigDecimal mont = bilan.stream()
                        .filter(b -> rubrique.equals(b.rubrique()))
                        .map("ACTIF".equals(rubrique) ? BilanExportDto::montantActif : BilanExportDto::montantPassif)
                        .filter(v -> v != null)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                XWPFTableRow row = tBilan.createRow();
                row.getCell(0).setText(rubrique);
                if ("ACTIF".equals(rubrique)) {
                    row.getCell(1).setText(formatMontant(mont));
                    row.getCell(2).setText("-");
                    actifTotal = mont;
                } else {
                    row.getCell(1).setText("-");
                    row.getCell(2).setText(formatMontant(mont));
                    passifTotal = mont;
                }
            }
            // Supprimer la ligne vide initiale
            tBilan.removeRow(1);

            doc.createParagraph();

            // ── Section 2 : Ratios BCM ─────────────────────────────────────
            ajouterTitreSection(doc, "2. RATIOS PRUDENTIELS BCM");

            for (RatiosBcmExportDto r : ratios) {
                ajouterTitreSousSection(doc, "Agence : " + r.nomAgence());

                XWPFTable tRatios = doc.createTable();
                ajouterLigneTableWord(tRatios, "Encours brut",                    formatMontant(r.encoursBrut()) + " " + MRU);
                ajouterLigneTableWord(tRatios, "Encours net (solde capital)",      formatMontant(r.encoursNet()) + " " + MRU);
                ajouterLigneTableWord(tRatios, "PAR > 30 jours",                  formatPct(r.tauxPar30()));
                ajouterLigneTableWord(tRatios, "PAR > 90 jours",                  formatPct(r.tauxPar90()));
                ajouterLigneTableWord(tRatios, "Taux de remboursement",            formatPct(r.tauxRemboursement()));
                ajouterLigneTableWord(tRatios, "Ratio couverture garanties",       formatPct(r.ratioCouvertureGaranties()));
                ajouterLigneTableWord(tRatios, "Taux portefeuille à risque",       formatPct(r.tauxPortefeuilleRisque()));

                doc.createParagraph();
            }

            // ── Section 3 : Observations ──────────────────────────────────
            ajouterTitreSection(doc, "3. OBSERVATIONS ET RECOMMANDATIONS");
            ajouterParagraphe(doc,
                    "Ce rapport a été généré automatiquement par le système MICROFINA " +
                    "à partir des données en base à la date d'arrêté. " +
                    "Les ratios BCM sont calculés conformément à la circulaire de la Banque Centrale " +
                    "de Mauritanie sur le reporting prudentiel des systèmes financiers décentralisés.");

            doc.write(out);
            return out.toByteArray();
        }
    }

    // =========================================================================
    //  PDF — Liste des clients (vue_liste_clients)
    // =========================================================================

    /**
     * Génère la liste clients au format PDF.
     *
     * @param agence filtre agence (null = toutes agences)
     * @return contenu du fichier .pdf en mémoire
     */
    public byte[] exportListeClientsPdf(String agence) throws IOException {

        List<ListeClientExportDto> clients = queryListeClients(agence);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Document doc = new Document(PageSize.A4.rotate(), 20, 20, 30, 30);
            PdfWriter.getInstance(doc, out);
            doc.open();

            // ── Titre ─────────────────────────────────────────────────────
            Font fTitre = new Font(Font.HELVETICA, 14, Font.BOLD, Color.DARK_GRAY);
            Font fSousTitre = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.DARK_GRAY);

            Paragraph titre = new Paragraph(
                    "LISTE DES CLIENTS MICROFINA\n", fTitre);
            titre.setAlignment(Element.ALIGN_CENTER);
            doc.add(titre);

            Paragraph sousTitre = new Paragraph(
                    (agence != null ? "Agence : " + agence + " — " : "Toutes agences — ") +
                    "Arrêté au " + LocalDate.now().format(DATE_FR) +
                    " — " + clients.size() + " membre(s)\n\n", fSousTitre);
            sousTitre.setAlignment(Element.ALIGN_CENTER);
            doc.add(sousTitre);

            // ── Tableau ───────────────────────────────────────────────────
            float[] colWidths = {8f, 10f, 10f, 5f, 8f, 8f, 8f, 7f, 7f, 7f, 7f, 7f, 7f};
            PdfPTable table = new PdfPTable(colWidths);
            table.setWidthPercentage(100);

            String[] entetes = {
                "N° Membre", "Nom", "Prénom", "Sexe", "Agence", "Statut",
                "Épargne (MRU)", "Crédits", "Encours (MRU)",
                "J. Retard", "Arriérés (MRU)", "PAR", "Garanties (MRU)"
            };
            for (String h : entetes) {
                ajouterCelluleEntete(table, h);
            }

            Font fData = new Font(Font.HELVETICA, 7);
            Font fAlerte = new Font(Font.HELVETICA, 7, Font.BOLD, Color.RED);

            for (ListeClientExportDto c : clients) {
                boolean enRetard = c.maxJoursRetard() != null && c.maxJoursRetard() > 0;
                Font f = enRetard ? fAlerte : fData;

                ajouterCellule(table, c.numMembre(),                          fData);
                ajouterCellule(table, c.nom(),                                fData);
                ajouterCellule(table, c.prenom(),                             fData);
                ajouterCellule(table, c.sexe(),                               fData);
                ajouterCellule(table, c.nomAgence(),                          fData);
                ajouterCellule(table, c.statutMembre(),                       fData);
                ajouterCelluleDroite(table, formatMontant(c.totalEpargne()),  fData);
                ajouterCellule(table, String.valueOf(c.nbCreditsActifs() != null ? c.nbCreditsActifs() : 0), fData);
                ajouterCelluleDroite(table, formatMontant(c.encoursCapital()), f);
                ajouterCellule(table, String.valueOf(c.maxJoursRetard() != null ? c.maxJoursRetard() : 0), f);
                ajouterCelluleDroite(table, formatMontant(c.totalArrieres()), f);
                ajouterCellule(table, c.categoriePar() != null ? c.categoriePar() : "SAIN", f);
                ajouterCelluleDroite(table, formatMontant(c.totalGaranties()), fData);
            }

            doc.add(table);
            doc.close();
            return out.toByteArray();
        }
    }

    // =========================================================================
    //  PDF — Rapport ratios BCM (vue_ratios_bcm)
    // =========================================================================

    /**
     * Génère le rapport ratios BCM au format PDF.
     */
    public byte[] exportRatiosBcmPdf() throws IOException {

        List<RatiosBcmExportDto> ratios = queryRatiosBcm();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Document doc = new Document(PageSize.A4, 36, 36, 50, 36);
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font fTitre    = new Font(Font.HELVETICA, 16, Font.BOLD);
            Font fSection  = new Font(Font.HELVETICA, 12, Font.BOLD, Color.DARK_GRAY);
            Font fNormal   = new Font(Font.HELVETICA, 10);
            Font fSmall    = new Font(Font.HELVETICA, 8);

            doc.add(new Paragraph("RAPPORT RATIOS PRUDENTIELS BCM", fTitre));
            doc.add(new Paragraph("MICROFINA — Arrêté au " + LocalDate.now().format(DATE_FR) + "\n\n", fNormal));

            for (RatiosBcmExportDto r : ratios) {
                doc.add(new Paragraph("Agence : " + r.nomAgence() + " (" + r.codeAgence() + ")", fSection));
                doc.add(new Paragraph(" "));

                PdfPTable t = new PdfPTable(2);
                t.setWidthPercentage(80);

                ajouterLignePdf(t, "Encours brut",                 formatMontant(r.encoursBrut())    + " " + MRU, fSmall);
                ajouterLignePdf(t, "Encours net",                  formatMontant(r.encoursNet())     + " " + MRU, fSmall);
                ajouterLignePdf(t, "Nb crédits actifs",            String.valueOf(r.nbCreditsActifs() != null ? r.nbCreditsActifs() : 0), fSmall);
                ajouterLignePdf(t, "PAR > 30 jours",               formatPct(r.tauxPar30()),          fSmall);
                ajouterLignePdf(t, "PAR > 90 jours",               formatPct(r.tauxPar90()),          fSmall);
                ajouterLignePdf(t, "Taux de remboursement",        formatPct(r.tauxRemboursement()),  fSmall);
                ajouterLignePdf(t, "Total remboursé",              formatMontant(r.totalRembourse())  + " " + MRU, fSmall);
                ajouterLignePdf(t, "Ratio couverture garanties",   formatPct(r.ratioCouvertureGaranties()), fSmall);
                ajouterLignePdf(t, "Taux portefeuille à risque",   formatPct(r.tauxPortefeuilleRisque()), fSmall);

                doc.add(t);
                doc.add(new Paragraph("\n"));
            }

            doc.close();
            return out.toByteArray();
        }
    }

    // =========================================================================
    //  Requêtes SQL sur les vues Phase 10.4
    // =========================================================================

    private List<EtatCreditExportDto> queryEtatCredits(String agence) {
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
        return jdbc.query(sql, params, (rs, n) -> mapEtatCredit(rs));
    }

    private List<ListeClientExportDto> queryListeClients(String agence) {
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
        return jdbc.query(sql, params, (rs, n) -> mapListeClient(rs));
    }

    private List<RatiosBcmExportDto> queryRatiosBcm() {
        return jdbc.query("""
            SELECT code_agence, nom_agence,
                   encours_brut, encours_net, nb_credits_actifs,
                   capital_risque_par30, taux_par_30,
                   capital_risque_par90, taux_par_90,
                   total_arrieres, taux_portefeuille_risque,
                   total_rembourse, total_echu, taux_remboursement,
                   total_garanties, ratio_couverture_garanties
            FROM vue_ratios_bcm
            ORDER BY code_agence
            """, (rs, n) -> mapRatiosBcm(rs));
    }

    private List<BilanExportDto> queryBilan() {
        return jdbc.query("""
            SELECT classe_compte, num_compte, rubrique, libelle_rubrique,
                   code_agence, nom_agence, nb_ecritures,
                   total_debit, total_credit, solde_net,
                   montant_actif, montant_passif,
                   date_premiere_ecriture, date_derniere_ecriture
            FROM vue_bilan
            ORDER BY classe_compte, num_compte, code_agence
            """, (rs, n) -> mapBilan(rs));
    }

    // =========================================================================
    //  RowMappers
    // =========================================================================

    private EtatCreditExportDto mapEtatCredit(ResultSet rs) throws SQLException {
        return new EtatCreditExportDto(
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
                toLocalDate(rs, "date_demande"),
                toLocalDate(rs, "date_deblocage"),
                toLocalDate(rs, "date_echeance_finale"),
                rs.getBigDecimal("montant_accorde"),
                rs.getBigDecimal("montant_debloque"),
                rs.getBigDecimal("solde_capital"),
                rs.getBigDecimal("solde_total_du"),
                rs.getInt("jours_retard"),
                rs.getBigDecimal("total_arrieres"),
                rs.getString("categorie_par"),
                rs.getBigDecimal("total_garanties"),
                rs.getDouble("taux_couverture_pct")
        );
    }

    private ListeClientExportDto mapListeClient(ResultSet rs) throws SQLException {
        return new ListeClientExportDto(
                rs.getString("num_membre"),
                rs.getString("nom"),
                rs.getString("prenom"),
                rs.getString("sexe"),
                toLocalDate(rs, "datenaissance"),
                rs.getString("statut_membre"),
                toLocalDate(rs, "date_adhesion"),
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
        );
    }

    private RatiosBcmExportDto mapRatiosBcm(ResultSet rs) throws SQLException {
        return new RatiosBcmExportDto(
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
        );
    }

    private BilanExportDto mapBilan(ResultSet rs) throws SQLException {
        return new BilanExportDto(
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
                toLocalDate(rs, "date_premiere_ecriture"),
                toLocalDate(rs, "date_derniere_ecriture")
        );
    }

    // =========================================================================
    //  Utilitaires Excel (Apache POI)
    // =========================================================================

    private CellStyle styleTitre(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        org.apache.poi.ss.usermodel.Font f = wb.createFont();
        f.setBold(true);
        f.setFontHeightInPoints((short) 13);
        s.setFont(f);
        s.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        f.setColor(IndexedColors.WHITE.getIndex());
        s.setAlignment(HorizontalAlignment.CENTER);
        return s;
    }

    private CellStyle styleEntete(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        org.apache.poi.ss.usermodel.Font f = wb.createFont();
        f.setBold(true);
        f.setColor(IndexedColors.WHITE.getIndex());
        s.setFont(f);
        s.setFillForegroundColor(IndexedColors.DARK_TEAL.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setBorderBottom(BorderStyle.THIN);
        return s;
    }

    private CellStyle styleMontant(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        DataFormat fmt = wb.createDataFormat();
        s.setDataFormat(fmt.getFormat("#,##0.00"));
        s.setAlignment(HorizontalAlignment.RIGHT);
        return s;
    }

    private CellStyle stylePourcentage(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        DataFormat fmt = wb.createDataFormat();
        s.setDataFormat(fmt.getFormat("0.00%"));
        s.setAlignment(HorizontalAlignment.RIGHT);
        return s;
    }

    private CellStyle styleNormal(Workbook wb) {
        return wb.createCellStyle();
    }

    private CellStyle styleAlerte(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        org.apache.poi.ss.usermodel.Font f = wb.createFont();
        f.setBold(true);
        f.setColor(IndexedColors.DARK_RED.getIndex());
        s.setFont(f);
        return s;
    }

    private void setCell(Row row, int col, String val, CellStyle style) {
        Cell c = row.createCell(col);
        c.setCellValue(val != null ? val : "");
        c.setCellStyle(style);
    }

    private void setCellNum(Row row, int col, BigDecimal val, CellStyle style) {
        Cell c = row.createCell(col);
        c.setCellValue(val != null ? val.doubleValue() : 0.0);
        c.setCellStyle(style);
    }

    private void setCellPct(Row row, int col, Double val, CellStyle style) {
        Cell c = row.createCell(col);
        c.setCellValue(val != null ? val / 100.0 : 0.0);
        c.setCellStyle(style);
    }

    private BigDecimal sumBD(List<EtatCreditExportDto> list,
                             java.util.function.Function<EtatCreditExportDto, BigDecimal> fn) {
        return list.stream().map(fn).filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // =========================================================================
    //  Utilitaires Word (Apache POI XWPF)
    // =========================================================================

    private void ajouterTitreSection(XWPFDocument doc, String texte) {
        XWPFParagraph p = doc.createParagraph();
        p.setStyle("Heading1");
        XWPFRun r = p.createRun();
        r.setBold(true);
        r.setFontSize(13);
        r.setText(texte);
    }

    private void ajouterTitreSousSection(XWPFDocument doc, String texte) {
        XWPFParagraph p = doc.createParagraph();
        XWPFRun r = p.createRun();
        r.setBold(true);
        r.setFontSize(11);
        r.setText(texte);
    }

    private void ajouterParagraphe(XWPFDocument doc, String texte) {
        XWPFParagraph p = doc.createParagraph();
        XWPFRun r = p.createRun();
        r.setFontSize(10);
        r.setText(texte);
    }

    private void styleEnteteTableWord(XWPFTableRow row, String[] vals) {
        for (int i = 0; i < vals.length; i++) {
            XWPFTableCell cell = i < row.getTableCells().size()
                    ? row.getCell(i) : row.addNewTableCell();
            cell.setText(vals[i]);
            cell.getCTTc().addNewTcPr().addNewShd()
                    .setFill("1F4E79");
        }
    }

    private void ajouterLigneTableWord(XWPFTable table, String libelle, String valeur) {
        XWPFTableRow row = table.createRow();
        if (row.getTableCells().isEmpty()) row.addNewTableCell();
        while (row.getTableCells().size() < 2) row.addNewTableCell();
        row.getCell(0).setText(libelle);
        row.getCell(1).setText(valeur);
    }

    // =========================================================================
    //  Utilitaires PDF (OpenPDF)
    // =========================================================================

    private void ajouterCelluleEntete(PdfPTable table, String texte) {
        Font f = new Font(Font.HELVETICA, 7, Font.BOLD, Color.WHITE);
        PdfPCell cell = new PdfPCell(new Phrase(texte, f));
        cell.setBackgroundColor(new Color(31, 78, 121));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(3);
        table.addCell(cell);
    }

    private void ajouterCellule(PdfPTable table, String texte, Font f) {
        PdfPCell cell = new PdfPCell(new Phrase(texte != null ? texte : "", f));
        cell.setPadding(2);
        table.addCell(cell);
    }

    private void ajouterCelluleDroite(PdfPTable table, String texte, Font f) {
        PdfPCell cell = new PdfPCell(new Phrase(texte != null ? texte : "", f));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setPadding(2);
        table.addCell(cell);
    }

    private void ajouterLignePdf(PdfPTable table, String libelle, String valeur, Font f) {
        PdfPCell c1 = new PdfPCell(new Phrase(libelle, f));
        c1.setBackgroundColor(new Color(240, 240, 240));
        c1.setPadding(4);
        table.addCell(c1);

        PdfPCell c2 = new PdfPCell(new Phrase(valeur, f));
        c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        c2.setPadding(4);
        table.addCell(c2);
    }

    // =========================================================================
    //  Utilitaires communs
    // =========================================================================

    private LocalDate toLocalDate(ResultSet rs, String col) throws SQLException {
        java.sql.Date d = rs.getDate(col);
        return d != null ? d.toLocalDate() : null;
    }

    private String formatDate(LocalDate d) {
        return d != null ? d.format(DATE_FR) : "";
    }

    private String formatMontant(BigDecimal v) {
        if (v == null) return "0,00";
        return String.format("%,.2f", v.setScale(2, RoundingMode.HALF_UP));
    }

    private String formatPct(Double v) {
        if (v == null) return "0,00 %";
        return String.format("%.2f %%", v * 100);
    }

    // =========================================================================
    //  Utilitaires Map<String,Object> (pour queryForList)
    // =========================================================================

    private String str(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v != null ? v.toString() : "";
    }

    private BigDecimal bd(Map<String, Object> m, String key) {
        Object v = m.get(key);
        if (v == null) return BigDecimal.ZERO;
        if (v instanceof BigDecimal bigDecimal) return bigDecimal;
        return new BigDecimal(v.toString());
    }

    // =========================================================================
    //  Requêtes SQL supplémentaires (Phase 11.3)
    // =========================================================================

    private List<Map<String, Object>> queryIndicateurs(String agence) {
        String sql = "SELECT code_agence, nom_agence, nb_credits_total, nb_credits_actifs, " +
            "nb_credits_retard, montant_encours, montant_arrieres, taux_remboursement_global, " +
            "total_epargne, nb_membres_actifs FROM vue_indicateurs_performance" +
            (agence != null ? " WHERE code_agence = ?" : "") + " ORDER BY code_agence";
        Object[] params = agence != null ? new Object[]{agence} : new Object[0];
        return jdbc.queryForList(sql, params);
    }

    private List<Map<String, Object>> queryBalanceComptes(String agence) {
        String sql = "SELECT num_compte, code_agence, nom_agence, nb_ecritures, " +
            "total_debit, total_credit, solde_net FROM vue_balance_comptes" +
            (agence != null ? " WHERE code_agence = ?" : "") + " ORDER BY num_compte, code_agence";
        Object[] params = agence != null ? new Object[]{agence} : new Object[0];
        return jdbc.queryForList(sql, params);
    }

    private List<Map<String, Object>> queryJournal(String agence, String date) {
        StringBuilder sql = new StringBuilder(
            "SELECT idcomptabilite, dateoperation, num_piece, libelle, num_compte, " +
            "sens, debit, credit, code_agence FROM vue_journal_comptable WHERE 1=1");
        ArrayList<Object> params = new ArrayList<>();
        if (agence != null) { sql.append(" AND code_agence = ?"); params.add(agence); }
        if (date != null)   { sql.append(" AND dateoperation = ?"); params.add(date); }
        sql.append(" ORDER BY dateoperation DESC, idcomptabilite DESC");
        return jdbc.queryForList(sql.toString(), params.toArray());
    }

    // =========================================================================
    //  EXCEL — Indicateurs de performance (vue_indicateurs_performance)
    // =========================================================================

    /**
     * Exporte les indicateurs de performance par agence au format Excel.
     *
     * @param agence filtre agence (null = toutes agences)
     * @return contenu du fichier .xlsx en mémoire
     */
    public byte[] exportIndicateursExcel(String agence) throws IOException {
        List<Map<String, Object>> lignes = queryIndicateurs(agence);
        try (XSSFWorkbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = wb.createSheet("Indicateurs");
            sheet.setDefaultColumnWidth(18);

            CellStyle entete  = styleEntete(wb);
            CellStyle montant = styleMontant(wb);
            CellStyle pct     = stylePourcentage(wb);
            CellStyle normal  = styleNormal(wb);

            Row titre = sheet.createRow(0);
            Cell cellTitre = titre.createCell(0);
            cellTitre.setCellValue("INDICATEURS DE PERFORMANCE — " +
                    (agence != null ? "Agence " + agence : "Toutes agences") +
                    " — " + LocalDate.now().format(DATE_FR));
            cellTitre.setCellStyle(styleTitre(wb));
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 9));

            String[] cols = {
                "Agence", "Nom agence", "Crédits total", "Crédits actifs",
                "Crédits retard", "Encours (MRU)", "Arriérés (MRU)", "Taux remb. %",
                "Épargne (MRU)", "Membres actifs"
            };
            Row rowEntete = sheet.createRow(1);
            for (int i = 0; i < cols.length; i++) {
                Cell c = rowEntete.createCell(i);
                c.setCellValue(cols[i]);
                c.setCellStyle(entete);
            }

            int r = 2;
            for (Map<String, Object> m : lignes) {
                Row row = sheet.createRow(r++);
                setCell(row, 0, str(m, "code_agence"),                           normal);
                setCell(row, 1, str(m, "nom_agence"),                             normal);
                setCellNum(row, 2, bd(m, "nb_credits_total"),                     montant);
                setCellNum(row, 3, bd(m, "nb_credits_actifs"),                    montant);
                setCellNum(row, 4, bd(m, "nb_credits_retard"),                    montant);
                setCellNum(row, 5, bd(m, "montant_encours"),                      montant);
                setCellNum(row, 6, bd(m, "montant_arrieres"),                     montant);
                setCellPct(row, 7, bd(m, "taux_remboursement_global").doubleValue(), pct);
                setCellNum(row, 8, bd(m, "total_epargne"),                        montant);
                setCellNum(row, 9, bd(m, "nb_membres_actifs"),                    montant);
            }

            for (int i = 0; i < cols.length; i++) sheet.autoSizeColumn(i);
            sheet.createFreezePane(0, 2);
            wb.write(out);
            return out.toByteArray();
        }
    }

    // =========================================================================
    //  EXCEL — Liste des clients (vue_liste_clients)
    // =========================================================================

    /**
     * Exporte la liste des clients au format Excel.
     *
     * @param agence filtre agence (null = toutes agences)
     * @return contenu du fichier .xlsx en mémoire
     */
    public byte[] exportListeClientsExcel(String agence) throws IOException {
        List<ListeClientExportDto> lignes = queryListeClients(agence);
        try (XSSFWorkbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = wb.createSheet("Liste Clients");
            sheet.setDefaultColumnWidth(18);

            CellStyle entete  = styleEntete(wb);
            CellStyle montant = styleMontant(wb);
            CellStyle normal  = styleNormal(wb);

            Row titre = sheet.createRow(0);
            Cell cellTitre = titre.createCell(0);
            cellTitre.setCellValue("LISTE DES CLIENTS — " +
                    (agence != null ? "Agence " + agence : "Toutes agences") +
                    " — " + LocalDate.now().format(DATE_FR));
            cellTitre.setCellStyle(styleTitre(wb));
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 9));

            String[] cols = {
                "N° Membre", "Nom", "Prénom", "Sexe", "Agence", "Nom agence",
                "Crédits actifs", "Encours capital (MRU)", "Épargne (MRU)", "Catégorie PAR"
            };
            Row rowEntete = sheet.createRow(1);
            for (int i = 0; i < cols.length; i++) {
                Cell c = rowEntete.createCell(i);
                c.setCellValue(cols[i]);
                c.setCellStyle(entete);
            }

            int r = 2;
            for (ListeClientExportDto d : lignes) {
                Row row = sheet.createRow(r++);
                setCell(row,    0, d.numMembre(),                                                    normal);
                setCell(row,    1, d.nom(),                                                          normal);
                setCell(row,    2, d.prenom(),                                                       normal);
                setCell(row,    3, d.sexe(),                                                         normal);
                setCell(row,    4, d.codeAgence(),                                                   normal);
                setCell(row,    5, d.nomAgence(),                                                    normal);
                setCellNum(row, 6, d.nbCreditsActifs() != null ? BigDecimal.valueOf(d.nbCreditsActifs()) : BigDecimal.ZERO, montant);
                setCellNum(row, 7, d.encoursCapital(),                                               montant);
                setCellNum(row, 8, d.totalEpargne(),                                                 montant);
                setCell(row,    9, d.categoriePar() != null ? d.categoriePar() : "SAIN",            normal);
            }

            for (int i = 0; i < cols.length; i++) sheet.autoSizeColumn(i);
            sheet.createFreezePane(0, 2);
            wb.write(out);
            return out.toByteArray();
        }
    }

    // =========================================================================
    //  EXCEL — Balance des comptes (vue_balance_comptes)
    // =========================================================================

    /**
     * Exporte la balance des comptes au format Excel.
     *
     * @param agence filtre agence (null = toutes agences)
     * @return contenu du fichier .xlsx en mémoire
     */
    public byte[] exportBalanceComptesExcel(String agence) throws IOException {
        List<Map<String, Object>> lignes = queryBalanceComptes(agence);
        try (XSSFWorkbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = wb.createSheet("Balance Comptes");
            sheet.setDefaultColumnWidth(18);

            CellStyle entete  = styleEntete(wb);
            CellStyle montant = styleMontant(wb);
            CellStyle normal  = styleNormal(wb);

            Row titre = sheet.createRow(0);
            Cell cellTitre = titre.createCell(0);
            cellTitre.setCellValue("BALANCE DES COMPTES — " +
                    (agence != null ? "Agence " + agence : "Toutes agences") +
                    " — " + LocalDate.now().format(DATE_FR));
            cellTitre.setCellStyle(styleTitre(wb));
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

            String[] cols = {
                "N° Compte", "Agence", "Nom agence", "Nb écritures",
                "Total débit (MRU)", "Total crédit (MRU)", "Solde net (MRU)"
            };
            Row rowEntete = sheet.createRow(1);
            for (int i = 0; i < cols.length; i++) {
                Cell c = rowEntete.createCell(i);
                c.setCellValue(cols[i]);
                c.setCellStyle(entete);
            }

            int r = 2;
            for (Map<String, Object> m : lignes) {
                Row row = sheet.createRow(r++);
                setCell(row,    0, str(m, "num_compte"),    normal);
                setCell(row,    1, str(m, "code_agence"),   normal);
                setCell(row,    2, str(m, "nom_agence"),    normal);
                setCellNum(row, 3, bd(m, "nb_ecritures"),   montant);
                setCellNum(row, 4, bd(m, "total_debit"),    montant);
                setCellNum(row, 5, bd(m, "total_credit"),   montant);
                setCellNum(row, 6, bd(m, "solde_net"),      montant);
            }

            for (int i = 0; i < cols.length; i++) sheet.autoSizeColumn(i);
            sheet.createFreezePane(0, 2);
            wb.write(out);
            return out.toByteArray();
        }
    }

    // =========================================================================
    //  EXCEL — Journal comptable (vue_journal_comptable)
    // =========================================================================

    /**
     * Exporte le journal comptable au format Excel.
     *
     * @param agence filtre agence (null = toutes agences)
     * @param date   filtre date opération ISO (null = toutes dates)
     * @return contenu du fichier .xlsx en mémoire
     */
    public byte[] exportJournalExcel(String agence, String date) throws IOException {
        List<Map<String, Object>> lignes = queryJournal(agence, date);
        try (XSSFWorkbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = wb.createSheet("Journal");
            sheet.setDefaultColumnWidth(18);

            CellStyle entete  = styleEntete(wb);
            CellStyle montant = styleMontant(wb);
            CellStyle normal  = styleNormal(wb);

            Row titre = sheet.createRow(0);
            Cell cellTitre = titre.createCell(0);
            cellTitre.setCellValue("JOURNAL COMPTABLE — " +
                    (agence != null ? "Agence " + agence : "Toutes agences") +
                    (date != null ? " — Date : " + date : "") +
                    " — " + LocalDate.now().format(DATE_FR));
            cellTitre.setCellStyle(styleTitre(wb));
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 8));

            String[] cols = {
                "ID", "Date opération", "N° Pièce", "Libellé",
                "N° Compte", "Sens", "Débit (MRU)", "Crédit (MRU)", "Agence"
            };
            Row rowEntete = sheet.createRow(1);
            for (int i = 0; i < cols.length; i++) {
                Cell c = rowEntete.createCell(i);
                c.setCellValue(cols[i]);
                c.setCellStyle(entete);
            }

            int r = 2;
            for (Map<String, Object> m : lignes) {
                Row row = sheet.createRow(r++);
                setCell(row,    0, str(m, "idcomptabilite"),  normal);
                setCell(row,    1, str(m, "dateoperation"),   normal);
                setCell(row,    2, str(m, "num_piece"),       normal);
                setCell(row,    3, str(m, "libelle"),         normal);
                setCell(row,    4, str(m, "num_compte"),      normal);
                setCell(row,    5, str(m, "sens"),            normal);
                setCellNum(row, 6, bd(m, "debit"),            montant);
                setCellNum(row, 7, bd(m, "credit"),           montant);
                setCell(row,    8, str(m, "code_agence"),     normal);
            }

            for (int i = 0; i < cols.length; i++) sheet.autoSizeColumn(i);
            sheet.createFreezePane(0, 2);
            wb.write(out);
            return out.toByteArray();
        }
    }

    // =========================================================================
    //  PDF — Bilan (vue_bilan)
    // =========================================================================

    /**
     * Génère le bilan simplifié au format PDF.
     *
     * @return contenu du fichier .pdf en mémoire
     */
    public byte[] exportBilanPdf() throws IOException {
        List<BilanExportDto> lignes = queryBilan();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4.rotate(), 20, 20, 30, 30);
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font fTitre  = new Font(Font.HELVETICA, 14, Font.BOLD, Color.DARK_GRAY);
            Font fSub    = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.DARK_GRAY);
            Font fSmall  = new Font(Font.HELVETICA, 7);
            Font fHeader = new Font(Font.HELVETICA, 7, Font.BOLD, Color.WHITE);

            Paragraph titre = new Paragraph("BILAN SIMPLIFIÉ SYSCOHADA\n", fTitre);
            titre.setAlignment(Element.ALIGN_CENTER);
            doc.add(titre);

            Paragraph sousTitre = new Paragraph(
                    "Arrêté au " + LocalDate.now().format(DATE_FR) +
                    " — " + lignes.size() + " ligne(s)\n\n", fSub);
            sousTitre.setAlignment(Element.ALIGN_CENTER);
            doc.add(sousTitre);

            float[] colWidths = {6f, 8f, 10f, 8f, 10f, 10f, 10f, 10f};
            PdfPTable table = new PdfPTable(colWidths);
            table.setWidthPercentage(100);

            String[] entetes = {
                "Classe", "N° Compte", "Rubrique", "Agence",
                "Total Débit", "Total Crédit", "Montant ACTIF", "Montant PASSIF"
            };
            for (String h : entetes) {
                PdfPCell cell = new PdfPCell(new Phrase(h, fHeader));
                cell.setBackgroundColor(new Color(31, 78, 121));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(3);
                table.addCell(cell);
            }

            for (BilanExportDto d : lignes) {
                ajouterCellule(table, d.classeCompte(),              fSmall);
                ajouterCellule(table, d.numCompte(),                 fSmall);
                ajouterCellule(table, d.rubrique(),                  fSmall);
                ajouterCellule(table, d.nomAgence(),                 fSmall);
                ajouterCelluleDroite(table, formatMontant(d.totalDebit()),   fSmall);
                ajouterCelluleDroite(table, formatMontant(d.totalCredit()),  fSmall);
                ajouterCelluleDroite(table, formatMontant(d.montantActif()), fSmall);
                ajouterCelluleDroite(table, formatMontant(d.montantPassif()), fSmall);
            }

            doc.add(table);
            doc.close();
            return out.toByteArray();
        }
    }

    // =========================================================================
    //  PDF — Journal comptable (vue_journal_comptable)
    // =========================================================================

    /**
     * Génère le journal comptable au format PDF.
     *
     * @param agence filtre agence (null = toutes agences)
     * @param date   filtre date opération ISO (null = toutes dates)
     * @return contenu du fichier .pdf en mémoire
     */
    public byte[] exportJournalPdf(String agence, String date) throws IOException {
        List<Map<String, Object>> lignes = queryJournal(agence, date);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4.rotate(), 20, 20, 30, 30);
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font fTitre  = new Font(Font.HELVETICA, 14, Font.BOLD, Color.DARK_GRAY);
            Font fSub    = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.DARK_GRAY);
            Font fSmall  = new Font(Font.HELVETICA, 7);
            Font fHeader = new Font(Font.HELVETICA, 7, Font.BOLD, Color.WHITE);

            Paragraph titre = new Paragraph("JOURNAL COMPTABLE\n", fTitre);
            titre.setAlignment(Element.ALIGN_CENTER);
            doc.add(titre);

            Paragraph sousTitre = new Paragraph(
                    (agence != null ? "Agence : " + agence + " — " : "Toutes agences — ") +
                    (date != null ? "Date : " + date + " — " : "") +
                    "Arrêté au " + LocalDate.now().format(DATE_FR) +
                    " — " + lignes.size() + " ligne(s)\n\n", fSub);
            sousTitre.setAlignment(Element.ALIGN_CENTER);
            doc.add(sousTitre);

            float[] colWidths = {10f, 8f, 8f, 18f, 8f, 5f, 10f, 10f};
            PdfPTable table = new PdfPTable(colWidths);
            table.setWidthPercentage(100);

            String[] entetes = {
                "Date opération", "N° Pièce", "N° Compte", "Libellé",
                "Sens", "Agence", "Débit (MRU)", "Crédit (MRU)"
            };
            for (String h : entetes) {
                PdfPCell cell = new PdfPCell(new Phrase(h, fHeader));
                cell.setBackgroundColor(new Color(31, 78, 121));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(3);
                table.addCell(cell);
            }

            for (Map<String, Object> m : lignes) {
                ajouterCellule(table, str(m, "dateoperation"),              fSmall);
                ajouterCellule(table, str(m, "num_piece"),                  fSmall);
                ajouterCellule(table, str(m, "num_compte"),                 fSmall);
                ajouterCellule(table, str(m, "libelle"),                    fSmall);
                ajouterCellule(table, str(m, "sens"),                       fSmall);
                ajouterCellule(table, str(m, "code_agence"),                fSmall);
                ajouterCelluleDroite(table, formatMontant(bd(m, "debit")),  fSmall);
                ajouterCelluleDroite(table, formatMontant(bd(m, "credit")), fSmall);
            }

            doc.add(table);
            doc.close();
            return out.toByteArray();
        }
    }

    // =========================================================================
    //  PDF — Balance des comptes (vue_balance_comptes)
    // =========================================================================

    /**
     * Génère la balance des comptes au format PDF.
     *
     * @param agence filtre agence (null = toutes agences)
     * @return contenu du fichier .pdf en mémoire
     */
    public byte[] exportBalanceComptesPdf(String agence) throws IOException {
        List<Map<String, Object>> lignes = queryBalanceComptes(agence);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4.rotate(), 20, 20, 30, 30);
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font fTitre  = new Font(Font.HELVETICA, 14, Font.BOLD, Color.DARK_GRAY);
            Font fSub    = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.DARK_GRAY);
            Font fSmall  = new Font(Font.HELVETICA, 7);
            Font fHeader = new Font(Font.HELVETICA, 7, Font.BOLD, Color.WHITE);

            Paragraph titre = new Paragraph("BALANCE DES COMPTES\n", fTitre);
            titre.setAlignment(Element.ALIGN_CENTER);
            doc.add(titre);

            Paragraph sousTitre = new Paragraph(
                    (agence != null ? "Agence : " + agence + " — " : "Toutes agences — ") +
                    "Arrêté au " + LocalDate.now().format(DATE_FR) +
                    " — " + lignes.size() + " ligne(s)\n\n", fSub);
            sousTitre.setAlignment(Element.ALIGN_CENTER);
            doc.add(sousTitre);

            float[] colWidths = {10f, 8f, 20f, 8f, 12f, 12f, 12f};
            PdfPTable table = new PdfPTable(colWidths);
            table.setWidthPercentage(100);

            String[] entetes = {
                "N° Compte", "Agence", "Nom agence", "Nb écritures",
                "Total Débit", "Total Crédit", "Solde net"
            };
            for (String h : entetes) {
                PdfPCell cell = new PdfPCell(new Phrase(h, fHeader));
                cell.setBackgroundColor(new Color(31, 78, 121));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(3);
                table.addCell(cell);
            }

            for (Map<String, Object> m : lignes) {
                ajouterCellule(table, str(m, "num_compte"),                       fSmall);
                ajouterCellule(table, str(m, "code_agence"),                      fSmall);
                ajouterCellule(table, str(m, "nom_agence"),                       fSmall);
                ajouterCellule(table, str(m, "nb_ecritures"),                     fSmall);
                ajouterCelluleDroite(table, formatMontant(bd(m, "total_debit")),  fSmall);
                ajouterCelluleDroite(table, formatMontant(bd(m, "total_credit")), fSmall);
                ajouterCelluleDroite(table, formatMontant(bd(m, "solde_net")),    fSmall);
            }

            doc.add(table);
            doc.close();
            return out.toByteArray();
        }
    }

    // =========================================================================
    //  WORD — Portefeuille crédit (vue_etat_credits)
    // =========================================================================

    /**
     * Génère le portefeuille crédit au format Word.
     *
     * @param agence filtre agence (null = toutes agences)
     * @return contenu du fichier .docx en mémoire
     */
    public byte[] exportPortefeuilleCreditWord(String agence) throws IOException {
        List<EtatCreditExportDto> lignes = queryEtatCredits(agence);

        try (XWPFDocument doc = new XWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            XWPFParagraph titreP = doc.createParagraph();
            titreP.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titreR = titreP.createRun();
            titreR.setBold(true);
            titreR.setFontSize(16);
            titreR.setText("PORTEFEUILLE CRÉDIT — MICROFINA");
            titreR.addBreak();
            titreR.setFontSize(11);
            titreR.setText((agence != null ? "Agence : " + agence + " — " : "Toutes agences — ") +
                    "État arrêté au " + LocalDate.now().format(DATE_FR));

            doc.createParagraph();

            ajouterTitreSection(doc, "ÉTAT DU PORTEFEUILLE CRÉDIT");

            XWPFTable table = doc.createTable();
            styleEnteteTableWord(table.getRow(0),
                    new String[]{"N° Crédit", "N° Membre", "Statut", "Mnt accordé", "Solde total dû", "J. retard", "Catégorie PAR"});

            for (EtatCreditExportDto d : lignes) {
                XWPFTableRow row = table.createRow();
                while (row.getTableCells().size() < 7) row.addNewTableCell();
                row.getCell(0).setText(d.numCredit() != null ? d.numCredit() : "");
                row.getCell(1).setText(d.numMembre() != null ? d.numMembre() : "");
                row.getCell(2).setText(d.statutCredit() != null ? d.statutCredit() : "");
                row.getCell(3).setText(formatMontant(d.montantAccorde()) + " " + MRU);
                row.getCell(4).setText(formatMontant(d.soldeTotal()) + " " + MRU);
                row.getCell(5).setText(d.joursRetard() != null ? d.joursRetard().toString() : "0");
                row.getCell(6).setText(d.categoriePar() != null ? d.categoriePar() : "SAIN");
            }

            doc.createParagraph();
            ajouterParagraphe(doc, "Nombre total de crédits : " + lignes.size());

            doc.write(out);
            return out.toByteArray();
        }
    }

    // =========================================================================
    //  WORD — Bilan (vue_bilan)
    // =========================================================================

    /**
     * Génère le bilan au format Word.
     *
     * @return contenu du fichier .docx en mémoire
     */
    public byte[] exportBilanWord() throws IOException {
        List<BilanExportDto> lignes = queryBilan();

        try (XWPFDocument doc = new XWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            XWPFParagraph titreP = doc.createParagraph();
            titreP.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titreR = titreP.createRun();
            titreR.setBold(true);
            titreR.setFontSize(16);
            titreR.setText("BILAN SIMPLIFIÉ SYSCOHADA — MICROFINA");
            titreR.addBreak();
            titreR.setFontSize(11);
            titreR.setText("État arrêté au " + LocalDate.now().format(DATE_FR));

            doc.createParagraph();

            ajouterTitreSection(doc, "BILAN");

            BigDecimal totalActif  = lignes.stream()
                    .map(BilanExportDto::montantActif)
                    .filter(v -> v != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalPassif = lignes.stream()
                    .map(BilanExportDto::montantPassif)
                    .filter(v -> v != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            ajouterParagraphe(doc,
                    "Total ACTIF : " + formatMontant(totalActif) + " " + MRU +
                    "   |   Total PASSIF : " + formatMontant(totalPassif) + " " + MRU);

            doc.createParagraph();

            XWPFTable table = doc.createTable();
            styleEnteteTableWord(table.getRow(0),
                    new String[]{"Classe", "Rubrique", "Montant ACTIF (" + MRU + ")", "Montant PASSIF (" + MRU + ")"});

            for (BilanExportDto d : lignes) {
                XWPFTableRow row = table.createRow();
                while (row.getTableCells().size() < 4) row.addNewTableCell();
                row.getCell(0).setText(d.classeCompte() != null ? d.classeCompte() : "");
                row.getCell(1).setText(d.rubrique() != null ? d.rubrique() : "");
                row.getCell(2).setText(formatMontant(d.montantActif()));
                row.getCell(3).setText(formatMontant(d.montantPassif()));
            }

            doc.write(out);
            return out.toByteArray();
        }
    }

    // =========================================================================
    //  Export Sage Compta — format ligne L
    // =========================================================================

    /**
     * Génère un export au format Sage Compta ligne L (ASCII CSV).
     *
     * <h3>Format Sage Compta ligne L</h3>
     * <p>Chaque ligne représente une écriture comptable avec les colonnes :</p>
     * <pre>
     *   L ; CODE_JOURNAL ; DATE(DD/MM/YYYY) ; NUMPIECE ; COMPTE ; LIBELLE ;
     *   DEBIT ; CREDIT ; CODE_LETTRAGE ; DATE_ECHEANCE ; CODE_AGENCE
     * </pre>
     *
     * <p>Les montants sont formatés avec 2 décimales et point comme séparateur décimal
     * (convention Sage).</p>
     *
     * @param agence  filtre optionnel sur le code agence
     * @return contenu CSV encodé en UTF-8 (BOM inclus pour Excel)
     */
    public byte[] exportSageComptaL(String agence) {
        String sql = """
            SELECT IDCOMPTABILITE, DATEOPERATION, NUMPIECE, planComptable,
                   LIBELLE, DEBIT, CREDIT, LETTRE, DATEECHEANCE,
                   CODE_AGENCE, journal
            FROM comptabilite
            WHERE DATEOPERATION IS NOT NULL
            """ + (agence != null ? " AND CODE_AGENCE = '" + agence.replace("'", "''") + "'" : "") +
            " ORDER BY DATEOPERATION, IDCOMPTABILITE";

        List<Map<String, Object>> rows = jdbc.queryForList(sql);

        StringBuilder sb = new StringBuilder();
        // BOM UTF-8 pour compatibilité Excel
        sb.append('\uFEFF');
        // En-tête
        sb.append("TYPE;CODE_JOURNAL;DATE;NUMPIECE;COMPTE;LIBELLE;DEBIT;CREDIT;LETTRAGE;DATE_ECHEANCE;CODE_AGENCE\r\n");

        DateTimeFormatter sageDateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (Map<String, Object> row : rows) {
            sb.append("L").append(';');
            // Code journal (numérique → chaîne)
            Object journal = row.get("journal");
            sb.append(journal != null ? journal.toString() : "").append(';');
            // Date opération
            Object dateOp = row.get("DATEOPERATION");
            if (dateOp instanceof java.sql.Date d) {
                sb.append(d.toLocalDate().format(sageDateFmt));
            } else if (dateOp != null) {
                sb.append(dateOp.toString().substring(0, 10).replace("-", "/"));
            }
            sb.append(';');
            // Numéro de pièce
            sb.append(nullSafe(row.get("NUMPIECE"))).append(';');
            // Compte
            sb.append(nullSafe(row.get("planComptable"))).append(';');
            // Libellé (protéger le ; dans les libellés)
            sb.append(nullSafe(row.get("LIBELLE")).replace(";", " ")).append(';');
            // Débit
            sb.append(formatSageMontant(row.get("DEBIT"))).append(';');
            // Crédit
            sb.append(formatSageMontant(row.get("CREDIT"))).append(';');
            // Code lettrage
            sb.append(nullSafe(row.get("LETTRE"))).append(';');
            // Date échéance
            Object dateEch = row.get("DATEECHEANCE");
            if (dateEch instanceof java.sql.Date d) {
                sb.append(d.toLocalDate().format(sageDateFmt));
            }
            sb.append(';');
            // Code agence
            sb.append(nullSafe(row.get("CODE_AGENCE")));
            sb.append("\r\n");
        }

        return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private static String nullSafe(Object v) {
        return v != null ? v.toString() : "";
    }

    private static String formatSageMontant(Object v) {
        if (v == null) return "0.00";
        try {
            BigDecimal bd = (v instanceof BigDecimal bv) ? bv : new BigDecimal(v.toString());
            return bd.setScale(2, RoundingMode.HALF_UP).toPlainString();
        } catch (Exception e) {
            return "0.00";
        }
    }

    // =========================================================================
    //  Requêtes SQL — nouvelles vues (GAP 2)
    // =========================================================================

    private List<Map<String, Object>> queryCompteResultat(String agence) {
        String sql = "SELECT code_agence, nom_agence, libelle_poste, montant_produits, montant_charges, " +
            "solde_net FROM vue_compte_resultat" +
            (agence != null ? " WHERE code_agence = ?" : "") + " ORDER BY code_agence, libelle_poste";
        Object[] params = agence != null ? new Object[]{agence} : new Object[0];
        return jdbc.queryForList(sql, params);
    }

    private List<Map<String, Object>> queryTableauFinancement(String agence) {
        String sql = "SELECT code_agence, nom_agence, libelle_ressource, montant_emplois, montant_ressources, " +
            "variation_nette FROM vue_tableau_financement" +
            (agence != null ? " WHERE code_agence = ?" : "") + " ORDER BY code_agence, libelle_ressource";
        Object[] params = agence != null ? new Object[]{agence} : new Object[0];
        return jdbc.queryForList(sql, params);
    }

    private List<Map<String, Object>> queryBalanceAgee(String agence) {
        String sql = "SELECT num_membre, nom_membre, code_agence, nom_agence, tranche_0_30, tranche_31_90, " +
            "tranche_91_180, tranche_181_360, tranche_plus_360, total_echu " +
            "FROM vue_balance_agee" +
            (agence != null ? " WHERE code_agence = ?" : "") + " ORDER BY code_agence, nom_membre";
        Object[] params = agence != null ? new Object[]{agence} : new Object[0];
        return jdbc.queryForList(sql, params);
    }

    // =========================================================================
    //  PDF — Portefeuille crédit (vue_etat_credits)
    // =========================================================================

    /**
     * Génère le portefeuille crédit au format PDF.
     *
     * @param agence filtre agence (null = toutes agences)
     * @return contenu du fichier .pdf en mémoire
     */
    public byte[] exportPortefeuilleCreditPdf(String agence) throws IOException {
        List<EtatCreditExportDto> lignes = queryEtatCredits(agence);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4.rotate(), 20, 20, 30, 30);
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font fTitre  = new Font(Font.HELVETICA, 14, Font.BOLD, Color.DARK_GRAY);
            Font fSub    = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.DARK_GRAY);
            Font fSmall  = new Font(Font.HELVETICA, 7);
            Font fAlerte = new Font(Font.HELVETICA, 7, Font.BOLD, Color.RED);
            Font fHeader = new Font(Font.HELVETICA, 7, Font.BOLD, Color.WHITE);

            Paragraph titre = new Paragraph("PORTEFEUILLE CRÉDIT MICROFINA\n", fTitre);
            titre.setAlignment(Element.ALIGN_CENTER);
            doc.add(titre);

            Paragraph sousTitre = new Paragraph(
                    (agence != null ? "Agence : " + agence + " — " : "Toutes agences — ") +
                    "Arrêté au " + LocalDate.now().format(DATE_FR) +
                    " — " + lignes.size() + " crédit(s)\n\n", fSub);
            sousTitre.setAlignment(Element.ALIGN_CENTER);
            doc.add(sousTitre);

            float[] colWidths = {10f, 8f, 10f, 10f, 8f, 8f, 8f, 8f, 8f, 7f};
            PdfPTable table = new PdfPTable(colWidths);
            table.setWidthPercentage(100);

            String[] entetes = {
                "N° Crédit", "Statut", "Membre", "Agence",
                "Mnt accordé", "Solde capital", "Solde total",
                "J. retard", "Arriérés", "PAR"
            };
            for (String h : entetes) {
                PdfPCell cell = new PdfPCell(new Phrase(h, fHeader));
                cell.setBackgroundColor(new Color(31, 78, 121));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(3);
                table.addCell(cell);
            }

            for (EtatCreditExportDto d : lignes) {
                boolean enRetard = d.joursRetard() != null && d.joursRetard() > 0;
                Font f = enRetard ? fAlerte : fSmall;
                ajouterCellule(table, d.numCredit(),                         fSmall);
                ajouterCellule(table, d.statutCredit(),                      fSmall);
                ajouterCellule(table, d.nomMembre() + " " + d.prenomMembre(), fSmall);
                ajouterCellule(table, d.nomAgence(),                          fSmall);
                ajouterCelluleDroite(table, formatMontant(d.montantAccorde()), fSmall);
                ajouterCelluleDroite(table, formatMontant(d.soldeCapital()),   fSmall);
                ajouterCelluleDroite(table, formatMontant(d.soldeTotal()),     f);
                ajouterCellule(table, d.joursRetard() != null ? d.joursRetard().toString() : "0", f);
                ajouterCelluleDroite(table, formatMontant(d.totalArrieres()), f);
                ajouterCellule(table, d.categoriePar() != null ? d.categoriePar() : "SAIN", f);
            }

            doc.add(table);
            doc.close();
            return out.toByteArray();
        }
    }

    // =========================================================================
    //  WORD — Ratios BCM (vue_ratios_bcm)
    // =========================================================================

    /**
     * Génère les ratios BCM au format Word.
     *
     * @return contenu du fichier .docx en mémoire
     */
    public byte[] exportRatiosBcmWord() throws IOException {
        List<RatiosBcmExportDto> ratios = queryRatiosBcm();

        try (XWPFDocument doc = new XWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            XWPFParagraph titreP = doc.createParagraph();
            titreP.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titreR = titreP.createRun();
            titreR.setBold(true);
            titreR.setFontSize(16);
            titreR.setText("RATIOS PRUDENTIELS BCM — MICROFINA");
            titreR.addBreak();
            titreR.setFontSize(11);
            titreR.setText("État arrêté au " + LocalDate.now().format(DATE_FR));

            doc.createParagraph();

            for (RatiosBcmExportDto r : ratios) {
                ajouterTitreSection(doc, "Agence : " + r.nomAgence() + " (" + r.codeAgence() + ")");

                XWPFTable table = doc.createTable();
                ajouterLigneTableWord(table, "Encours brut",               formatMontant(r.encoursBrut()) + " " + MRU);
                ajouterLigneTableWord(table, "Encours net",                formatMontant(r.encoursNet()) + " " + MRU);
                ajouterLigneTableWord(table, "Nb crédits actifs",          String.valueOf(r.nbCreditsActifs() != null ? r.nbCreditsActifs() : 0));
                ajouterLigneTableWord(table, "PAR > 30 jours",             formatPct(r.tauxPar30()));
                ajouterLigneTableWord(table, "PAR > 90 jours",             formatPct(r.tauxPar90()));
                ajouterLigneTableWord(table, "Taux de remboursement",      formatPct(r.tauxRemboursement()));
                ajouterLigneTableWord(table, "Ratio couverture garanties", formatPct(r.ratioCouvertureGaranties()));
                ajouterLigneTableWord(table, "Taux portefeuille à risque", formatPct(r.tauxPortefeuilleRisque()));

                doc.createParagraph();
            }

            doc.write(out);
            return out.toByteArray();
        }
    }

    // =========================================================================
    //  WORD — Balance des comptes (vue_balance_comptes)
    // =========================================================================

    /**
     * Génère la balance des comptes au format Word.
     *
     * @param agence filtre agence (null = toutes agences)
     * @return contenu du fichier .docx en mémoire
     */
    public byte[] exportBalanceComptesWord(String agence) throws IOException {
        List<Map<String, Object>> lignes = queryBalanceComptes(agence);

        try (XWPFDocument doc = new XWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            XWPFParagraph titreP = doc.createParagraph();
            titreP.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titreR = titreP.createRun();
            titreR.setBold(true);
            titreR.setFontSize(16);
            titreR.setText("BALANCE DES COMPTES — MICROFINA");
            titreR.addBreak();
            titreR.setFontSize(11);
            titreR.setText((agence != null ? "Agence : " + agence + " — " : "Toutes agences — ") +
                    "État arrêté au " + LocalDate.now().format(DATE_FR));

            doc.createParagraph();
            ajouterTitreSection(doc, "BALANCE DES COMPTES");

            XWPFTable table = doc.createTable();
            styleEnteteTableWord(table.getRow(0),
                    new String[]{"N° Compte", "Agence", "Nom agence", "Nb écritures", "Total débit (" + MRU + ")", "Total crédit (" + MRU + ")", "Solde net (" + MRU + ")"});

            for (Map<String, Object> m : lignes) {
                XWPFTableRow row = table.createRow();
                while (row.getTableCells().size() < 7) row.addNewTableCell();
                row.getCell(0).setText(str(m, "num_compte"));
                row.getCell(1).setText(str(m, "code_agence"));
                row.getCell(2).setText(str(m, "nom_agence"));
                row.getCell(3).setText(str(m, "nb_ecritures"));
                row.getCell(4).setText(formatMontant(bd(m, "total_debit")));
                row.getCell(5).setText(formatMontant(bd(m, "total_credit")));
                row.getCell(6).setText(formatMontant(bd(m, "solde_net")));
            }

            doc.write(out);
            return out.toByteArray();
        }
    }

    // =========================================================================
    //  WORD — Journal comptable (vue_journal_comptable)
    // =========================================================================

    /**
     * Génère le journal comptable au format Word.
     *
     * @param agence filtre agence (null = toutes agences)
     * @return contenu du fichier .docx en mémoire
     */
    public byte[] exportJournalWord(String agence) throws IOException {
        List<Map<String, Object>> lignes = queryJournal(agence, null);

        try (XWPFDocument doc = new XWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            XWPFParagraph titreP = doc.createParagraph();
            titreP.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titreR = titreP.createRun();
            titreR.setBold(true);
            titreR.setFontSize(16);
            titreR.setText("JOURNAL COMPTABLE — MICROFINA");
            titreR.addBreak();
            titreR.setFontSize(11);
            titreR.setText((agence != null ? "Agence : " + agence + " — " : "Toutes agences — ") +
                    "État arrêté au " + LocalDate.now().format(DATE_FR));

            doc.createParagraph();
            ajouterTitreSection(doc, "JOURNAL COMPTABLE");

            XWPFTable table = doc.createTable();
            styleEnteteTableWord(table.getRow(0),
                    new String[]{"Date", "N° Pièce", "N° Compte", "Libellé", "Sens", "Débit (" + MRU + ")", "Crédit (" + MRU + ")", "Agence"});

            for (Map<String, Object> m : lignes) {
                XWPFTableRow row = table.createRow();
                while (row.getTableCells().size() < 8) row.addNewTableCell();
                row.getCell(0).setText(str(m, "dateoperation"));
                row.getCell(1).setText(str(m, "num_piece"));
                row.getCell(2).setText(str(m, "num_compte"));
                row.getCell(3).setText(str(m, "libelle"));
                row.getCell(4).setText(str(m, "sens"));
                row.getCell(5).setText(formatMontant(bd(m, "debit")));
                row.getCell(6).setText(formatMontant(bd(m, "credit")));
                row.getCell(7).setText(str(m, "code_agence"));
            }

            ajouterParagraphe(doc, "Nombre total d'écritures : " + lignes.size());

            doc.write(out);
            return out.toByteArray();
        }
    }

    // =========================================================================
    //  PDF — Indicateurs de performance (vue_indicateurs_performance)
    // =========================================================================

    /**
     * Génère les indicateurs de performance au format PDF.
     *
     * @param agence filtre agence (null = toutes agences)
     * @return contenu du fichier .pdf en mémoire
     */
    public byte[] exportIndicateursPdf(String agence) throws IOException {
        List<Map<String, Object>> lignes = queryIndicateurs(agence);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4.rotate(), 20, 20, 30, 30);
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font fTitre  = new Font(Font.HELVETICA, 14, Font.BOLD, Color.DARK_GRAY);
            Font fSub    = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.DARK_GRAY);
            Font fSmall  = new Font(Font.HELVETICA, 7);
            Font fHeader = new Font(Font.HELVETICA, 7, Font.BOLD, Color.WHITE);

            Paragraph titre = new Paragraph("INDICATEURS DE PERFORMANCE\n", fTitre);
            titre.setAlignment(Element.ALIGN_CENTER);
            doc.add(titre);

            Paragraph sousTitre = new Paragraph(
                    (agence != null ? "Agence : " + agence + " — " : "Toutes agences — ") +
                    "Arrêté au " + LocalDate.now().format(DATE_FR) +
                    " — " + lignes.size() + " agence(s)\n\n", fSub);
            sousTitre.setAlignment(Element.ALIGN_CENTER);
            doc.add(sousTitre);

            float[] colWidths = {8f, 15f, 8f, 8f, 8f, 12f, 12f, 10f, 12f, 9f};
            PdfPTable table = new PdfPTable(colWidths);
            table.setWidthPercentage(100);

            String[] entetes = {
                "Agence", "Nom agence", "Crédits total", "Crédits actifs",
                "Crédits retard", "Encours (MRU)", "Arriérés (MRU)",
                "Tx remb. %", "Épargne (MRU)", "Membres actifs"
            };
            for (String h : entetes) {
                PdfPCell cell = new PdfPCell(new Phrase(h, fHeader));
                cell.setBackgroundColor(new Color(31, 78, 121));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(3);
                table.addCell(cell);
            }

            for (Map<String, Object> m : lignes) {
                ajouterCellule(table, str(m, "code_agence"),                              fSmall);
                ajouterCellule(table, str(m, "nom_agence"),                               fSmall);
                ajouterCelluleDroite(table, formatMontant(bd(m, "nb_credits_total")),     fSmall);
                ajouterCelluleDroite(table, formatMontant(bd(m, "nb_credits_actifs")),    fSmall);
                ajouterCelluleDroite(table, formatMontant(bd(m, "nb_credits_retard")),    fSmall);
                ajouterCelluleDroite(table, formatMontant(bd(m, "montant_encours")),      fSmall);
                ajouterCelluleDroite(table, formatMontant(bd(m, "montant_arrieres")),     fSmall);
                ajouterCelluleDroite(table, formatPct(bd(m, "taux_remboursement_global").doubleValue() / 100.0), fSmall);
                ajouterCelluleDroite(table, formatMontant(bd(m, "total_epargne")),        fSmall);
                ajouterCelluleDroite(table, formatMontant(bd(m, "nb_membres_actifs")),    fSmall);
            }

            doc.add(table);
            doc.close();
            return out.toByteArray();
        }
    }

    // =========================================================================
    //  WORD — Indicateurs de performance (vue_indicateurs_performance)
    // =========================================================================

    /**
     * Génère les indicateurs de performance au format Word.
     *
     * @param agence filtre agence (null = toutes agences)
     * @return contenu du fichier .docx en mémoire
     */
    public byte[] exportIndicateursWord(String agence) throws IOException {
        List<Map<String, Object>> lignes = queryIndicateurs(agence);

        try (XWPFDocument doc = new XWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            XWPFParagraph titreP = doc.createParagraph();
            titreP.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titreR = titreP.createRun();
            titreR.setBold(true);
            titreR.setFontSize(16);
            titreR.setText("INDICATEURS DE PERFORMANCE — MICROFINA");
            titreR.addBreak();
            titreR.setFontSize(11);
            titreR.setText((agence != null ? "Agence : " + agence + " — " : "Toutes agences — ") +
                    "État arrêté au " + LocalDate.now().format(DATE_FR));

            doc.createParagraph();
            ajouterTitreSection(doc, "INDICATEURS DE PERFORMANCE PAR AGENCE");

            for (Map<String, Object> m : lignes) {
                ajouterTitreSousSection(doc, "Agence : " + str(m, "nom_agence") + " (" + str(m, "code_agence") + ")");

                XWPFTable table = doc.createTable();
                ajouterLigneTableWord(table, "Crédits total",          str(m, "nb_credits_total"));
                ajouterLigneTableWord(table, "Crédits actifs",         str(m, "nb_credits_actifs"));
                ajouterLigneTableWord(table, "Crédits en retard",      str(m, "nb_credits_retard"));
                ajouterLigneTableWord(table, "Encours",                formatMontant(bd(m, "montant_encours")) + " " + MRU);
                ajouterLigneTableWord(table, "Arriérés",               formatMontant(bd(m, "montant_arrieres")) + " " + MRU);
                ajouterLigneTableWord(table, "Taux remboursement",     formatPct(bd(m, "taux_remboursement_global").doubleValue() / 100.0));
                ajouterLigneTableWord(table, "Total épargne",          formatMontant(bd(m, "total_epargne")) + " " + MRU);
                ajouterLigneTableWord(table, "Membres actifs",         str(m, "nb_membres_actifs"));

                doc.createParagraph();
            }

            doc.write(out);
            return out.toByteArray();
        }
    }

    // =========================================================================
    //  WORD — Liste clients (vue_liste_clients)
    // =========================================================================

    /**
     * Génère la liste des clients au format Word.
     *
     * @param agence filtre agence (null = toutes agences)
     * @return contenu du fichier .docx en mémoire
     */
    public byte[] exportListeClientsWord(String agence) throws IOException {
        List<ListeClientExportDto> lignes = queryListeClients(agence);

        try (XWPFDocument doc = new XWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            XWPFParagraph titreP = doc.createParagraph();
            titreP.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titreR = titreP.createRun();
            titreR.setBold(true);
            titreR.setFontSize(16);
            titreR.setText("LISTE DES CLIENTS — MICROFINA");
            titreR.addBreak();
            titreR.setFontSize(11);
            titreR.setText((agence != null ? "Agence : " + agence + " — " : "Toutes agences — ") +
                    "État arrêté au " + LocalDate.now().format(DATE_FR));

            doc.createParagraph();
            ajouterTitreSection(doc, "LISTE DES MEMBRES");

            XWPFTable table = doc.createTable();
            styleEnteteTableWord(table.getRow(0),
                    new String[]{"N° Membre", "Nom", "Prénom", "Sexe", "Agence", "Statut", "Encours (" + MRU + ")", "Catégorie PAR"});

            for (ListeClientExportDto d : lignes) {
                XWPFTableRow row = table.createRow();
                while (row.getTableCells().size() < 8) row.addNewTableCell();
                row.getCell(0).setText(d.numMembre() != null ? d.numMembre() : "");
                row.getCell(1).setText(d.nom() != null ? d.nom() : "");
                row.getCell(2).setText(d.prenom() != null ? d.prenom() : "");
                row.getCell(3).setText(d.sexe() != null ? d.sexe() : "");
                row.getCell(4).setText(d.nomAgence() != null ? d.nomAgence() : "");
                row.getCell(5).setText(d.statutMembre() != null ? d.statutMembre() : "");
                row.getCell(6).setText(formatMontant(d.encoursCapital()));
                row.getCell(7).setText(d.categoriePar() != null ? d.categoriePar() : "SAIN");
            }

            doc.createParagraph();
            ajouterParagraphe(doc, "Nombre total de membres : " + lignes.size());

            doc.write(out);
            return out.toByteArray();
        }
    }

    // =========================================================================
    //  EXCEL — Rapport financier (vue_bilan + vue_ratios_bcm)
    // =========================================================================

    /**
     * Génère le rapport financier au format Excel.
     *
     * @return contenu du fichier .xlsx en mémoire
     */
    public byte[] exportRapportFinancierExcel() throws IOException {
        List<BilanExportDto> bilan   = queryBilan();
        List<RatiosBcmExportDto> ratios = queryRatiosBcm();

        try (XSSFWorkbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // Feuille Bilan
            Sheet sheetBilan = wb.createSheet("Bilan");
            sheetBilan.setDefaultColumnWidth(20);

            CellStyle entete  = styleEntete(wb);
            CellStyle montant = styleMontant(wb);
            CellStyle normal  = styleNormal(wb);

            Row titreBilan = sheetBilan.createRow(0);
            Cell t1 = titreBilan.createCell(0);
            t1.setCellValue("RAPPORT FINANCIER — BILAN — " + LocalDate.now().format(DATE_FR));
            t1.setCellStyle(styleTitre(wb));
            sheetBilan.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));

            String[] colsBilan = {"Classe", "N° Compte", "Rubrique", "Agence", "Montant ACTIF (" + MRU + ")", "Montant PASSIF (" + MRU + ")"};
            Row rowEBilan = sheetBilan.createRow(1);
            for (int i = 0; i < colsBilan.length; i++) {
                Cell c = rowEBilan.createCell(i);
                c.setCellValue(colsBilan[i]);
                c.setCellStyle(entete);
            }
            int ri = 2;
            for (BilanExportDto d : bilan) {
                Row row = sheetBilan.createRow(ri++);
                setCell(row,    0, d.classeCompte(),  normal);
                setCell(row,    1, d.numCompte(),     normal);
                setCell(row,    2, d.rubrique(),      normal);
                setCell(row,    3, d.nomAgence(),     normal);
                setCellNum(row, 4, d.montantActif(),  montant);
                setCellNum(row, 5, d.montantPassif(), montant);
            }
            sheetBilan.createFreezePane(0, 2);

            // Feuille Ratios BCM
            Sheet sheetRatios = wb.createSheet("Ratios BCM");
            sheetRatios.setDefaultColumnWidth(20);

            Row titreRatios = sheetRatios.createRow(0);
            Cell t2 = titreRatios.createCell(0);
            t2.setCellValue("RAPPORT FINANCIER — RATIOS BCM — " + LocalDate.now().format(DATE_FR));
            t2.setCellStyle(styleTitre(wb));
            sheetRatios.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));

            String[] colsRatios = {"Agence", "Nom Agence", "Encours brut", "Encours net",
                "PAR30 (%)", "PAR90 (%)", "Tx remb. (%)", "Ratio couverture (%)"};
            Row rowERatios = sheetRatios.createRow(1);
            for (int i = 0; i < colsRatios.length; i++) {
                Cell c = rowERatios.createCell(i);
                c.setCellValue(colsRatios[i]);
                c.setCellStyle(styleEntete(wb));
            }
            CellStyle pct = stylePourcentage(wb);
            CellStyle mont2 = styleMontant(wb);
            CellStyle norm2 = styleNormal(wb);
            int rj = 2;
            for (RatiosBcmExportDto r : ratios) {
                Row row = sheetRatios.createRow(rj++);
                setCell(row,    0, r.codeAgence(), norm2);
                setCell(row,    1, r.nomAgence(),  norm2);
                setCellNum(row, 2, r.encoursBrut(), mont2);
                setCellNum(row, 3, r.encoursNet(),  mont2);
                setCellPct(row, 4, r.tauxPar30()  != null ? r.tauxPar30()  * 100 : 0.0, pct);
                setCellPct(row, 5, r.tauxPar90()  != null ? r.tauxPar90()  * 100 : 0.0, pct);
                setCellPct(row, 6, r.tauxRemboursement() != null ? r.tauxRemboursement() * 100 : 0.0, pct);
                setCellPct(row, 7, r.ratioCouvertureGaranties() != null ? r.ratioCouvertureGaranties() * 100 : 0.0, pct);
            }
            sheetRatios.createFreezePane(0, 2);

            wb.write(out);
            return out.toByteArray();
        }
    }

    // =========================================================================
    //  PDF — Rapport financier (vue_bilan + vue_ratios_bcm)
    // =========================================================================

    /**
     * Génère le rapport financier au format PDF.
     *
     * @return contenu du fichier .pdf en mémoire
     */
    public byte[] exportRapportFinancierPdf() throws IOException {
        List<BilanExportDto> bilan   = queryBilan();
        List<RatiosBcmExportDto> ratios = queryRatiosBcm();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 36, 36, 50, 36);
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font fTitre   = new Font(Font.HELVETICA, 16, Font.BOLD);
            Font fSection = new Font(Font.HELVETICA, 12, Font.BOLD, Color.DARK_GRAY);
            Font fNormal  = new Font(Font.HELVETICA, 10);
            Font fSmall   = new Font(Font.HELVETICA, 8);
            Font fHeader  = new Font(Font.HELVETICA, 8, Font.BOLD, Color.WHITE);

            doc.add(new Paragraph("RAPPORT FINANCIER MICROFINA", fTitre));
            doc.add(new Paragraph("Arrêté au " + LocalDate.now().format(DATE_FR) + "\n\n", fNormal));

            // Section Bilan
            doc.add(new Paragraph("1. BILAN SIMPLIFIÉ", fSection));
            doc.add(new Paragraph(" "));

            BigDecimal totalActif  = bilan.stream().map(BilanExportDto::montantActif).filter(v -> v != null).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalPassif = bilan.stream().map(BilanExportDto::montantPassif).filter(v -> v != null).reduce(BigDecimal.ZERO, BigDecimal::add);
            doc.add(new Paragraph("Total ACTIF : " + formatMontant(totalActif) + " " + MRU +
                    "   |   Total PASSIF : " + formatMontant(totalPassif) + " " + MRU + "\n", fNormal));

            float[] colsBilan = {8f, 12f, 12f, 10f, 10f};
            PdfPTable tBilan = new PdfPTable(colsBilan);
            tBilan.setWidthPercentage(100);
            for (String h : new String[]{"Classe", "Rubrique", "Agence", "Montant ACTIF", "Montant PASSIF"}) {
                PdfPCell cell = new PdfPCell(new Phrase(h, fHeader));
                cell.setBackgroundColor(new Color(31, 78, 121));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(3);
                tBilan.addCell(cell);
            }
            for (BilanExportDto d : bilan) {
                ajouterCellule(tBilan, d.classeCompte(),              fSmall);
                ajouterCellule(tBilan, d.rubrique(),                  fSmall);
                ajouterCellule(tBilan, d.nomAgence(),                 fSmall);
                ajouterCelluleDroite(tBilan, formatMontant(d.montantActif()),  fSmall);
                ajouterCelluleDroite(tBilan, formatMontant(d.montantPassif()), fSmall);
            }
            doc.add(tBilan);
            doc.add(new Paragraph("\n"));

            // Section Ratios BCM
            doc.add(new Paragraph("2. RATIOS PRUDENTIELS BCM", fSection));
            doc.add(new Paragraph(" "));

            for (RatiosBcmExportDto r : ratios) {
                doc.add(new Paragraph("Agence : " + r.nomAgence() + " (" + r.codeAgence() + ")", fSection));
                PdfPTable tR = new PdfPTable(2);
                tR.setWidthPercentage(80);
                ajouterLignePdf(tR, "Encours brut",               formatMontant(r.encoursBrut()) + " " + MRU, fSmall);
                ajouterLignePdf(tR, "Encours net",                formatMontant(r.encoursNet()) + " " + MRU, fSmall);
                ajouterLignePdf(tR, "PAR > 30 jours",             formatPct(r.tauxPar30()),  fSmall);
                ajouterLignePdf(tR, "PAR > 90 jours",             formatPct(r.tauxPar90()),  fSmall);
                ajouterLignePdf(tR, "Taux de remboursement",      formatPct(r.tauxRemboursement()), fSmall);
                ajouterLignePdf(tR, "Ratio couverture garanties", formatPct(r.ratioCouvertureGaranties()), fSmall);
                doc.add(tR);
                doc.add(new Paragraph("\n"));
            }

            doc.close();
            return out.toByteArray();
        }
    }

    // =========================================================================
    //  EXCEL — Compte de résultat (vue_compte_resultat)
    // =========================================================================

    /**
     * Génère le compte de résultat au format Excel.
     *
     * @param agence filtre agence (null = toutes agences)
     * @return contenu du fichier .xlsx en mémoire
     */
    public byte[] exportCompteResultatExcel(String agence) throws IOException {
        List<Map<String, Object>> lignes = queryCompteResultat(agence);

        try (XSSFWorkbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = wb.createSheet("Compte de Résultat");
            sheet.setDefaultColumnWidth(22);

            CellStyle entete  = styleEntete(wb);
            CellStyle montant = styleMontant(wb);
            CellStyle normal  = styleNormal(wb);

            Row titre = sheet.createRow(0);
            Cell cellTitre = titre.createCell(0);
            cellTitre.setCellValue("COMPTE DE RÉSULTAT — " +
                    (agence != null ? "Agence " + agence : "Toutes agences") +
                    " — " + LocalDate.now().format(DATE_FR));
            cellTitre.setCellStyle(styleTitre(wb));
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));

            String[] cols = {"Agence", "Nom agence", "Poste", "Produits (" + MRU + ")", "Charges (" + MRU + ")", "Solde net (" + MRU + ")"};
            Row rowEntete = sheet.createRow(1);
            for (int i = 0; i < cols.length; i++) {
                Cell c = rowEntete.createCell(i);
                c.setCellValue(cols[i]);
                c.setCellStyle(entete);
            }

            int r = 2;
            for (Map<String, Object> m : lignes) {
                Row row = sheet.createRow(r++);
                setCell(row,    0, str(m, "code_agence"),          normal);
                setCell(row,    1, str(m, "nom_agence"),           normal);
                setCell(row,    2, str(m, "libelle_poste"),        normal);
                setCellNum(row, 3, bd(m, "montant_produits"),      montant);
                setCellNum(row, 4, bd(m, "montant_charges"),       montant);
                setCellNum(row, 5, bd(m, "solde_net"),             montant);
            }

            for (int i = 0; i < cols.length; i++) sheet.autoSizeColumn(i);
            sheet.createFreezePane(0, 2);
            wb.write(out);
            return out.toByteArray();
        }
    }

    // =========================================================================
    //  PDF — Compte de résultat (vue_compte_resultat)
    // =========================================================================

    /**
     * Génère le compte de résultat au format PDF.
     *
     * @param agence filtre agence (null = toutes agences)
     * @return contenu du fichier .pdf en mémoire
     */
    public byte[] exportCompteResultatPdf(String agence) throws IOException {
        List<Map<String, Object>> lignes = queryCompteResultat(agence);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4.rotate(), 20, 20, 30, 30);
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font fTitre  = new Font(Font.HELVETICA, 14, Font.BOLD, Color.DARK_GRAY);
            Font fSub    = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.DARK_GRAY);
            Font fSmall  = new Font(Font.HELVETICA, 7);
            Font fHeader = new Font(Font.HELVETICA, 7, Font.BOLD, Color.WHITE);

            Paragraph titre = new Paragraph("COMPTE DE RÉSULTAT\n", fTitre);
            titre.setAlignment(Element.ALIGN_CENTER);
            doc.add(titre);

            Paragraph sousTitre = new Paragraph(
                    (agence != null ? "Agence : " + agence + " — " : "Toutes agences — ") +
                    "Arrêté au " + LocalDate.now().format(DATE_FR) +
                    " — " + lignes.size() + " ligne(s)\n\n", fSub);
            sousTitre.setAlignment(Element.ALIGN_CENTER);
            doc.add(sousTitre);

            float[] colWidths = {8f, 18f, 18f, 14f, 14f, 14f};
            PdfPTable table = new PdfPTable(colWidths);
            table.setWidthPercentage(100);

            for (String h : new String[]{"Agence", "Nom agence", "Poste", "Produits", "Charges", "Solde net"}) {
                PdfPCell cell = new PdfPCell(new Phrase(h, fHeader));
                cell.setBackgroundColor(new Color(31, 78, 121));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(3);
                table.addCell(cell);
            }

            for (Map<String, Object> m : lignes) {
                ajouterCellule(table, str(m, "code_agence"),                              fSmall);
                ajouterCellule(table, str(m, "nom_agence"),                               fSmall);
                ajouterCellule(table, str(m, "libelle_poste"),                            fSmall);
                ajouterCelluleDroite(table, formatMontant(bd(m, "montant_produits")),     fSmall);
                ajouterCelluleDroite(table, formatMontant(bd(m, "montant_charges")),      fSmall);
                ajouterCelluleDroite(table, formatMontant(bd(m, "solde_net")),            fSmall);
            }

            doc.add(table);
            doc.close();
            return out.toByteArray();
        }
    }

    // =========================================================================
    //  WORD — Compte de résultat (vue_compte_resultat)
    // =========================================================================

    /**
     * Génère le compte de résultat au format Word.
     *
     * @param agence filtre agence (null = toutes agences)
     * @return contenu du fichier .docx en mémoire
     */
    public byte[] exportCompteResultatWord(String agence) throws IOException {
        List<Map<String, Object>> lignes = queryCompteResultat(agence);

        try (XWPFDocument doc = new XWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            XWPFParagraph titreP = doc.createParagraph();
            titreP.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titreR = titreP.createRun();
            titreR.setBold(true);
            titreR.setFontSize(16);
            titreR.setText("COMPTE DE RÉSULTAT — MICROFINA");
            titreR.addBreak();
            titreR.setFontSize(11);
            titreR.setText((agence != null ? "Agence : " + agence + " — " : "Toutes agences — ") +
                    "État arrêté au " + LocalDate.now().format(DATE_FR));

            doc.createParagraph();
            ajouterTitreSection(doc, "COMPTE DE RÉSULTAT");

            XWPFTable table = doc.createTable();
            styleEnteteTableWord(table.getRow(0),
                    new String[]{"Agence", "Nom agence", "Poste", "Produits (" + MRU + ")", "Charges (" + MRU + ")", "Solde net (" + MRU + ")"});

            for (Map<String, Object> m : lignes) {
                XWPFTableRow row = table.createRow();
                while (row.getTableCells().size() < 6) row.addNewTableCell();
                row.getCell(0).setText(str(m, "code_agence"));
                row.getCell(1).setText(str(m, "nom_agence"));
                row.getCell(2).setText(str(m, "libelle_poste"));
                row.getCell(3).setText(formatMontant(bd(m, "montant_produits")));
                row.getCell(4).setText(formatMontant(bd(m, "montant_charges")));
                row.getCell(5).setText(formatMontant(bd(m, "solde_net")));
            }

            doc.write(out);
            return out.toByteArray();
        }
    }

    // =========================================================================
    //  EXCEL — Tableau de financement (vue_tableau_financement)
    // =========================================================================

    /**
     * Génère le tableau de financement au format Excel.
     *
     * @param agence filtre agence (null = toutes agences)
     * @return contenu du fichier .xlsx en mémoire
     */
    public byte[] exportTableauFinancementExcel(String agence) throws IOException {
        List<Map<String, Object>> lignes = queryTableauFinancement(agence);

        try (XSSFWorkbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = wb.createSheet("Tableau Financement");
            sheet.setDefaultColumnWidth(22);

            CellStyle entete  = styleEntete(wb);
            CellStyle montant = styleMontant(wb);
            CellStyle normal  = styleNormal(wb);

            Row titre = sheet.createRow(0);
            Cell cellTitre = titre.createCell(0);
            cellTitre.setCellValue("TABLEAU DE FINANCEMENT — " +
                    (agence != null ? "Agence " + agence : "Toutes agences") +
                    " — " + LocalDate.now().format(DATE_FR));
            cellTitre.setCellStyle(styleTitre(wb));
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));

            String[] cols = {"Agence", "Nom agence", "Ressource/Emploi", "Emplois (" + MRU + ")", "Ressources (" + MRU + ")", "Variation nette (" + MRU + ")"};
            Row rowEntete = sheet.createRow(1);
            for (int i = 0; i < cols.length; i++) {
                Cell c = rowEntete.createCell(i);
                c.setCellValue(cols[i]);
                c.setCellStyle(entete);
            }

            int r = 2;
            for (Map<String, Object> m : lignes) {
                Row row = sheet.createRow(r++);
                setCell(row,    0, str(m, "code_agence"),             normal);
                setCell(row,    1, str(m, "nom_agence"),              normal);
                setCell(row,    2, str(m, "libelle_ressource"),       normal);
                setCellNum(row, 3, bd(m, "montant_emplois"),          montant);
                setCellNum(row, 4, bd(m, "montant_ressources"),       montant);
                setCellNum(row, 5, bd(m, "variation_nette"),          montant);
            }

            for (int i = 0; i < cols.length; i++) sheet.autoSizeColumn(i);
            sheet.createFreezePane(0, 2);
            wb.write(out);
            return out.toByteArray();
        }
    }

    // =========================================================================
    //  PDF — Tableau de financement (vue_tableau_financement)
    // =========================================================================

    /**
     * Génère le tableau de financement au format PDF.
     *
     * @param agence filtre agence (null = toutes agences)
     * @return contenu du fichier .pdf en mémoire
     */
    public byte[] exportTableauFinancementPdf(String agence) throws IOException {
        List<Map<String, Object>> lignes = queryTableauFinancement(agence);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4.rotate(), 20, 20, 30, 30);
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font fTitre  = new Font(Font.HELVETICA, 14, Font.BOLD, Color.DARK_GRAY);
            Font fSub    = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.DARK_GRAY);
            Font fSmall  = new Font(Font.HELVETICA, 7);
            Font fHeader = new Font(Font.HELVETICA, 7, Font.BOLD, Color.WHITE);

            Paragraph titre = new Paragraph("TABLEAU DE FINANCEMENT\n", fTitre);
            titre.setAlignment(Element.ALIGN_CENTER);
            doc.add(titre);

            Paragraph sousTitre = new Paragraph(
                    (agence != null ? "Agence : " + agence + " — " : "Toutes agences — ") +
                    "Arrêté au " + LocalDate.now().format(DATE_FR) +
                    " — " + lignes.size() + " ligne(s)\n\n", fSub);
            sousTitre.setAlignment(Element.ALIGN_CENTER);
            doc.add(sousTitre);

            float[] colWidths = {8f, 18f, 18f, 14f, 14f, 14f};
            PdfPTable table = new PdfPTable(colWidths);
            table.setWidthPercentage(100);

            for (String h : new String[]{"Agence", "Nom agence", "Ressource/Emploi", "Emplois", "Ressources", "Variation nette"}) {
                PdfPCell cell = new PdfPCell(new Phrase(h, fHeader));
                cell.setBackgroundColor(new Color(31, 78, 121));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(3);
                table.addCell(cell);
            }

            for (Map<String, Object> m : lignes) {
                ajouterCellule(table, str(m, "code_agence"),                             fSmall);
                ajouterCellule(table, str(m, "nom_agence"),                              fSmall);
                ajouterCellule(table, str(m, "libelle_ressource"),                       fSmall);
                ajouterCelluleDroite(table, formatMontant(bd(m, "montant_emplois")),     fSmall);
                ajouterCelluleDroite(table, formatMontant(bd(m, "montant_ressources")),  fSmall);
                ajouterCelluleDroite(table, formatMontant(bd(m, "variation_nette")),     fSmall);
            }

            doc.add(table);
            doc.close();
            return out.toByteArray();
        }
    }

    // =========================================================================
    //  WORD — Tableau de financement (vue_tableau_financement)
    // =========================================================================

    /**
     * Génère le tableau de financement au format Word.
     *
     * @param agence filtre agence (null = toutes agences)
     * @return contenu du fichier .docx en mémoire
     */
    public byte[] exportTableauFinancementWord(String agence) throws IOException {
        List<Map<String, Object>> lignes = queryTableauFinancement(agence);

        try (XWPFDocument doc = new XWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            XWPFParagraph titreP = doc.createParagraph();
            titreP.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titreR = titreP.createRun();
            titreR.setBold(true);
            titreR.setFontSize(16);
            titreR.setText("TABLEAU DE FINANCEMENT — MICROFINA");
            titreR.addBreak();
            titreR.setFontSize(11);
            titreR.setText((agence != null ? "Agence : " + agence + " — " : "Toutes agences — ") +
                    "État arrêté au " + LocalDate.now().format(DATE_FR));

            doc.createParagraph();
            ajouterTitreSection(doc, "TABLEAU DE FINANCEMENT");

            XWPFTable table = doc.createTable();
            styleEnteteTableWord(table.getRow(0),
                    new String[]{"Agence", "Nom agence", "Ressource/Emploi", "Emplois (" + MRU + ")", "Ressources (" + MRU + ")", "Variation nette (" + MRU + ")"});

            for (Map<String, Object> m : lignes) {
                XWPFTableRow row = table.createRow();
                while (row.getTableCells().size() < 6) row.addNewTableCell();
                row.getCell(0).setText(str(m, "code_agence"));
                row.getCell(1).setText(str(m, "nom_agence"));
                row.getCell(2).setText(str(m, "libelle_ressource"));
                row.getCell(3).setText(formatMontant(bd(m, "montant_emplois")));
                row.getCell(4).setText(formatMontant(bd(m, "montant_ressources")));
                row.getCell(5).setText(formatMontant(bd(m, "variation_nette")));
            }

            doc.write(out);
            return out.toByteArray();
        }
    }

    // =========================================================================
    //  EXCEL — Balance âgée (vue_balance_agee)
    // =========================================================================

    /**
     * Génère la balance âgée des arriérés au format Excel.
     *
     * @param agence filtre agence (null = toutes agences)
     * @return contenu du fichier .xlsx en mémoire
     */
    public byte[] exportBalanceAgeeExcel(String agence) throws IOException {
        List<Map<String, Object>> lignes = queryBalanceAgee(agence);

        try (XSSFWorkbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = wb.createSheet("Balance Agée");
            sheet.setDefaultColumnWidth(18);

            CellStyle entete  = styleEntete(wb);
            CellStyle montant = styleMontant(wb);
            CellStyle normal  = styleNormal(wb);

            Row titre = sheet.createRow(0);
            Cell cellTitre = titre.createCell(0);
            cellTitre.setCellValue("BALANCE ÂGÉE DES ARRIÉRÉS — " +
                    (agence != null ? "Agence " + agence : "Toutes agences") +
                    " — " + LocalDate.now().format(DATE_FR));
            cellTitre.setCellStyle(styleTitre(wb));
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 8));

            String[] cols = {
                "N° Membre", "Nom membre", "Agence", "Nom agence",
                "0-30 j (" + MRU + ")", "31-90 j (" + MRU + ")",
                "91-180 j (" + MRU + ")", "181-360 j (" + MRU + ")",
                "> 360 j (" + MRU + ")", "Total échu (" + MRU + ")"
            };
            Row rowEntete = sheet.createRow(1);
            for (int i = 0; i < cols.length; i++) {
                Cell c = rowEntete.createCell(i);
                c.setCellValue(cols[i]);
                c.setCellStyle(entete);
            }

            int r = 2;
            for (Map<String, Object> m : lignes) {
                Row row = sheet.createRow(r++);
                setCell(row,    0, str(m, "num_membre"),           normal);
                setCell(row,    1, str(m, "nom_membre"),           normal);
                setCell(row,    2, str(m, "code_agence"),          normal);
                setCell(row,    3, str(m, "nom_agence"),           normal);
                setCellNum(row, 4, bd(m, "tranche_0_30"),          montant);
                setCellNum(row, 5, bd(m, "tranche_31_90"),         montant);
                setCellNum(row, 6, bd(m, "tranche_91_180"),        montant);
                setCellNum(row, 7, bd(m, "tranche_181_360"),       montant);
                setCellNum(row, 8, bd(m, "tranche_plus_360"),      montant);
                setCellNum(row, 9, bd(m, "total_echu"),            montant);
            }

            for (int i = 0; i < cols.length; i++) sheet.autoSizeColumn(i);
            sheet.createFreezePane(0, 2);
            wb.write(out);
            return out.toByteArray();
        }
    }

    // =========================================================================
    //  PDF — Balance âgée (vue_balance_agee)
    // =========================================================================

    /**
     * Génère la balance âgée des arriérés au format PDF.
     *
     * @param agence filtre agence (null = toutes agences)
     * @return contenu du fichier .pdf en mémoire
     */
    public byte[] exportBalanceAgeePdf(String agence) throws IOException {
        List<Map<String, Object>> lignes = queryBalanceAgee(agence);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4.rotate(), 20, 20, 30, 30);
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font fTitre  = new Font(Font.HELVETICA, 14, Font.BOLD, Color.DARK_GRAY);
            Font fSub    = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.DARK_GRAY);
            Font fSmall  = new Font(Font.HELVETICA, 7);
            Font fHeader = new Font(Font.HELVETICA, 7, Font.BOLD, Color.WHITE);

            Paragraph titre = new Paragraph("BALANCE ÂGÉE DES ARRIÉRÉS\n", fTitre);
            titre.setAlignment(Element.ALIGN_CENTER);
            doc.add(titre);

            Paragraph sousTitre = new Paragraph(
                    (agence != null ? "Agence : " + agence + " — " : "Toutes agences — ") +
                    "Arrêté au " + LocalDate.now().format(DATE_FR) +
                    " — " + lignes.size() + " ligne(s)\n\n", fSub);
            sousTitre.setAlignment(Element.ALIGN_CENTER);
            doc.add(sousTitre);

            float[] colWidths = {8f, 14f, 7f, 12f, 9f, 9f, 9f, 9f, 9f, 10f};
            PdfPTable table = new PdfPTable(colWidths);
            table.setWidthPercentage(100);

            for (String h : new String[]{"N° Membre", "Nom membre", "Agence", "Nom agence",
                    "0-30 j", "31-90 j", "91-180 j", "181-360 j", "> 360 j", "Total échu"}) {
                PdfPCell cell = new PdfPCell(new Phrase(h, fHeader));
                cell.setBackgroundColor(new Color(31, 78, 121));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(3);
                table.addCell(cell);
            }

            for (Map<String, Object> m : lignes) {
                ajouterCellule(table, str(m, "num_membre"),                             fSmall);
                ajouterCellule(table, str(m, "nom_membre"),                             fSmall);
                ajouterCellule(table, str(m, "code_agence"),                            fSmall);
                ajouterCellule(table, str(m, "nom_agence"),                             fSmall);
                ajouterCelluleDroite(table, formatMontant(bd(m, "tranche_0_30")),       fSmall);
                ajouterCelluleDroite(table, formatMontant(bd(m, "tranche_31_90")),      fSmall);
                ajouterCelluleDroite(table, formatMontant(bd(m, "tranche_91_180")),     fSmall);
                ajouterCelluleDroite(table, formatMontant(bd(m, "tranche_181_360")),    fSmall);
                ajouterCelluleDroite(table, formatMontant(bd(m, "tranche_plus_360")),   fSmall);
                ajouterCelluleDroite(table, formatMontant(bd(m, "total_echu")),         fSmall);
            }

            doc.add(table);
            doc.close();
            return out.toByteArray();
        }
    }

    // =========================================================================
    //  WORD — Balance âgée (vue_balance_agee)
    // =========================================================================

    /**
     * Génère la balance âgée des arriérés au format Word.
     *
     * @param agence filtre agence (null = toutes agences)
     * @return contenu du fichier .docx en mémoire
     */
    public byte[] exportBalanceAgeeWord(String agence) throws IOException {
        List<Map<String, Object>> lignes = queryBalanceAgee(agence);

        try (XWPFDocument doc = new XWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            XWPFParagraph titreP = doc.createParagraph();
            titreP.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titreR = titreP.createRun();
            titreR.setBold(true);
            titreR.setFontSize(16);
            titreR.setText("BALANCE ÂGÉE DES ARRIÉRÉS — MICROFINA");
            titreR.addBreak();
            titreR.setFontSize(11);
            titreR.setText((agence != null ? "Agence : " + agence + " — " : "Toutes agences — ") +
                    "État arrêté au " + LocalDate.now().format(DATE_FR));

            doc.createParagraph();
            ajouterTitreSection(doc, "BALANCE ÂGÉE");

            XWPFTable table = doc.createTable();
            styleEnteteTableWord(table.getRow(0),
                    new String[]{"N° Membre", "Nom membre", "Agence", "0-30 j", "31-90 j", "91-180 j", "181-360 j", "> 360 j", "Total échu"});

            for (Map<String, Object> m : lignes) {
                XWPFTableRow row = table.createRow();
                while (row.getTableCells().size() < 9) row.addNewTableCell();
                row.getCell(0).setText(str(m, "num_membre"));
                row.getCell(1).setText(str(m, "nom_membre"));
                row.getCell(2).setText(str(m, "code_agence"));
                row.getCell(3).setText(formatMontant(bd(m, "tranche_0_30")));
                row.getCell(4).setText(formatMontant(bd(m, "tranche_31_90")));
                row.getCell(5).setText(formatMontant(bd(m, "tranche_91_180")));
                row.getCell(6).setText(formatMontant(bd(m, "tranche_181_360")));
                row.getCell(7).setText(formatMontant(bd(m, "tranche_plus_360")));
                row.getCell(8).setText(formatMontant(bd(m, "total_echu")));
            }

            doc.createParagraph();
            ajouterParagraphe(doc, "Nombre de membres : " + lignes.size());

            doc.write(out);
            return out.toByteArray();
        }
    }

    // =========================================================================
    //  PDF — Portefeuille / Etat crédits (vue_etat_credits) — alias portefeuille
    // =========================================================================

    /**
     * Génère l'état du portefeuille (alias) au format PDF.
     *
     * @param agence filtre agence (null = toutes agences)
     * @return contenu du fichier .pdf en mémoire
     */
    public byte[] exportPortefeuillePdf(String agence) throws IOException {
        return exportPortefeuilleCreditPdf(agence);
    }

    /**
     * Génère l'état du portefeuille au format Word.
     *
     * @param agence filtre agence (null = toutes agences)
     * @return contenu du fichier .docx en mémoire
     */
    public byte[] exportPortefeuilleWord(String agence) throws IOException {
        return exportPortefeuilleCreditWord(agence);
    }

    /**
     * Génère l'état du portefeuille au format Excel.
     *
     * @param agence filtre agence (null = toutes agences)
     * @return contenu du fichier .xlsx en mémoire
     */
    public byte[] exportPortefeuilleExcel(String agence) throws IOException {
        return exportPortefeuilleCreditExcel(agence);
    }

    // =========================================================================
    //  Dispatch unifié — export(etat, format, agence)
    // =========================================================================

    /**
     * Méthode de dispatch unifiée : route vers la bonne méthode d'export
     * selon la combinaison etat/format.
     *
     * @param etat   identifiant de l'état (ex : "credits", "bilan")
     * @param format format de fichier ("excel", "pdf", "word")
     * @param agence filtre agence optionnel
     * @return contenu du fichier en mémoire
     * @throws BusinessException si la combinaison etat/format n'est pas supportée
     */
    public byte[] export(String etat, String format, String agence) throws IOException {
        return switch (etat + "/" + format) {
            // ── credits ──────────────────────────────────────────────────────
            case "credits/excel"                -> exportPortefeuilleCreditExcel(agence);
            case "credits/pdf"                  -> exportPortefeuilleCreditPdf(agence);
            case "credits/word"                 -> exportPortefeuilleCreditWord(agence);
            // ── ratios-bcm ───────────────────────────────────────────────────
            case "ratios-bcm/excel"             -> exportRatiosBcmExcel();
            case "ratios-bcm/pdf"               -> exportRatiosBcmPdf();
            case "ratios-bcm/word"              -> exportRatiosBcmWord();
            // ── bilan ────────────────────────────────────────────────────────
            case "bilan/excel"                  -> exportBilanExcel();
            case "bilan/pdf"                    -> exportBilanPdf();
            case "bilan/word"                   -> exportBilanWord();
            // ── balance-comptes ──────────────────────────────────────────────
            case "balance-comptes/excel"        -> exportBalanceComptesExcel(agence);
            case "balance-comptes/pdf"          -> exportBalanceComptesPdf(agence);
            case "balance-comptes/word"         -> exportBalanceComptesWord(agence);
            // ── journal ──────────────────────────────────────────────────────
            case "journal/excel"                -> exportJournalExcel(agence, null);
            case "journal/pdf"                  -> exportJournalPdf(agence, null);
            case "journal/word"                 -> exportJournalWord(agence);
            // ── indicateurs ──────────────────────────────────────────────────
            case "indicateurs/excel"            -> exportIndicateursExcel(agence);
            case "indicateurs/pdf"              -> exportIndicateursPdf(agence);
            case "indicateurs/word"             -> exportIndicateursWord(agence);
            // ── liste-clients ────────────────────────────────────────────────
            case "liste-clients/excel"          -> exportListeClientsExcel(agence);
            case "liste-clients/pdf"            -> exportListeClientsPdf(agence);
            case "liste-clients/word"           -> exportListeClientsWord(agence);
            // ── rapport-financier ────────────────────────────────────────────
            case "rapport-financier/excel"      -> exportRapportFinancierExcel();
            case "rapport-financier/pdf"        -> exportRapportFinancierPdf();
            case "rapport-financier/word"       -> exportRapportFinancierWord();
            // ── compte-resultat ──────────────────────────────────────────────
            case "compte-resultat/excel"        -> exportCompteResultatExcel(agence);
            case "compte-resultat/pdf"          -> exportCompteResultatPdf(agence);
            case "compte-resultat/word"         -> exportCompteResultatWord(agence);
            // ── tableau-financement ──────────────────────────────────────────
            case "tableau-financement/excel"    -> exportTableauFinancementExcel(agence);
            case "tableau-financement/pdf"      -> exportTableauFinancementPdf(agence);
            case "tableau-financement/word"     -> exportTableauFinancementWord(agence);
            // ── balance-agee ─────────────────────────────────────────────────
            case "balance-agee/excel"           -> exportBalanceAgeeExcel(agence);
            case "balance-agee/pdf"             -> exportBalanceAgeePdf(agence);
            case "balance-agee/word"            -> exportBalanceAgeeWord(agence);
            // ── portefeuille ─────────────────────────────────────────────────
            case "portefeuille/excel"           -> exportPortefeuilleExcel(agence);
            case "portefeuille/pdf"             -> exportPortefeuillePdf(agence);
            case "portefeuille/word"            -> exportPortefeuilleWord(agence);
            // ── défaut ───────────────────────────────────────────────────────
            default -> throw new BusinessException("Export non supporté : " + etat + "/" + format);
        };
    }
}
