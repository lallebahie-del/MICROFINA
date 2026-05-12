import 'package:intl/intl.dart';
import 'package:pdf/pdf.dart';
import 'package:pdf/widgets.dart' as pw;
import 'package:printing/printing.dart';
import '../constants/app_geo.dart';
import '../../data/models/extra_models.dart';

class PdfGeneratorService {
  static Future<void> generateAndPreviewTransactions({
    required String accountName,
    required String accountId,
    required List<EpargneTransactionModel> transactions,
  }) async {
    final pdf = pw.Document();
    final currencyFormat = NumberFormat.currency(
      locale: 'fr_FR',
      symbol: 'FCFA',
      decimalDigits: 0,
    );

    pdf.addPage(
      pw.MultiPage(
        pageFormat: PdfPageFormat.a4,
        margin: const pw.EdgeInsets.all(32),
        build: (context) => [
          _buildHeader(accountName, accountId),
          pw.SizedBox(height: 20),
          _buildTransactionsTable(transactions, currencyFormat),
          pw.SizedBox(height: 20),
          _buildFooter(),
        ],
      ),
    );

    await Printing.layoutPdf(
      onLayout: (PdfPageFormat format) async => pdf.save(),
      name: 'releve_transactions_$accountId.pdf',
    );
  }

  static Future<void> generateAndPreviewCertificate({
    required CertificatModel certificat,
  }) async {
    final pdf = pw.Document();
    final currencyFormat = NumberFormat.currency(
      locale: 'fr_FR',
      symbol: 'FCFA',
      decimalDigits: 0,
    );

    pdf.addPage(
      pw.Page(
        pageFormat: PdfPageFormat.a4,
        build: (context) => pw.Container(
          padding: const pw.EdgeInsets.all(40),
          decoration: pw.BoxDecoration(
            border: pw.Border.all(color: PdfColors.blue900, width: 2),
          ),
          child: pw.Column(
            crossAxisAlignment: pw.CrossAxisAlignment.center,
            children: [
              pw.Text(
                'microCredit',
                style: pw.TextStyle(
                  fontSize: 28,
                  fontWeight: pw.FontWeight.bold,
                  color: PdfColors.blue900,
                ),
              ),
              pw.SizedBox(height: 10),
              pw.Text(
                'CERTIFICAT DE DÉPÔT À TERME',
                style: pw.TextStyle(
                  fontSize: 20,
                  fontWeight: pw.FontWeight.bold,
                ),
              ),
              pw.SizedBox(height: 40),
              pw.Divider(color: PdfColors.grey400),
              pw.SizedBox(height: 40),
              pw.Text(
                'Il est certifié par la présente que',
                style: const pw.TextStyle(fontSize: 14),
              ),
              pw.SizedBox(height: 10),
              pw.Text(
                'Un client de microCredit',
                style: pw.TextStyle(
                  fontSize: 18,
                  fontWeight: pw.FontWeight.bold,
                ),
              ),
              pw.SizedBox(height: 20),
              pw.Text(
                'a effectué un placement sous les conditions suivantes :',
                style: const pw.TextStyle(fontSize: 14),
              ),
              pw.SizedBox(height: 40),
              pw.Row(
                mainAxisAlignment: pw.MainAxisAlignment.spaceBetween,
                children: [
                  _buildCertItem(
                    'Numéro du Certificat',
                    certificat.numeroCertificat,
                  ),
                  _buildCertItem(
                    'Date d\'échéance',
                    DateFormat('dd/MM/yyyy').format(certificat.maturityDate),
                  ),
                ],
              ),
              pw.SizedBox(height: 30),
              pw.Row(
                mainAxisAlignment: pw.MainAxisAlignment.spaceBetween,
                children: [
                  _buildCertItem(
                    'Montant Principal',
                    currencyFormat.format(certificat.montantPlacement),
                  ),
                  _buildCertItem(
                    'Taux d\'Intérêt',
                    '${certificat.tauxInteret}%',
                  ),
                ],
              ),
              pw.Spacer(),
              pw.Row(
                mainAxisAlignment: pw.MainAxisAlignment.spaceBetween,
                children: [
                  pw.Column(
                    children: [
                      pw.Text(
                        'Date d\'émission',
                        style: const pw.TextStyle(fontSize: 10),
                      ),
                      pw.Text(
                        DateFormat('dd/MM/yyyy').format(DateTime.now()),
                        style: pw.TextStyle(fontWeight: pw.FontWeight.bold),
                      ),
                    ],
                  ),
                  pw.Column(
                    children: [
                      pw.Text(
                        'Cachet de l\'institution',
                        style: const pw.TextStyle(fontSize: 10),
                      ),
                      pw.SizedBox(height: 40),
                      pw.Text(
                        'SIGNATURE AUTORISÉE',
                        style: pw.TextStyle(
                          fontSize: 10,
                          fontWeight: pw.FontWeight.bold,
                          color: PdfColors.blue900,
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );

    await Printing.layoutPdf(
      onLayout: (PdfPageFormat format) async => pdf.save(),
      name: 'certificat_${certificat.numeroCertificat}.pdf',
    );
  }

  static pw.Widget _buildCertItem(String label, String value) {
    return pw.Column(
      crossAxisAlignment: pw.CrossAxisAlignment.start,
      children: [
        pw.Text(
          label,
          style: const pw.TextStyle(fontSize: 10, color: PdfColors.grey600),
        ),
        pw.SizedBox(height: 4),
        pw.Text(
          value,
          style: pw.TextStyle(fontSize: 14, fontWeight: pw.FontWeight.bold),
        ),
      ],
    );
  }

  static pw.Widget _buildHeader(String accountName, String accountId) {
    return pw.Row(
      mainAxisAlignment: pw.MainAxisAlignment.spaceBetween,
      children: [
        pw.Column(
          crossAxisAlignment: pw.CrossAxisAlignment.start,
          children: [
            pw.Text(
              'microCredit',
              style: pw.TextStyle(
                fontSize: 24,
                fontWeight: pw.FontWeight.bold,
                color: PdfColors.blue900,
              ),
            ),
            pw.Text(
              'Votre banque, partout avec vous.',
              style: const pw.TextStyle(fontSize: 10, color: PdfColors.grey700),
            ),
          ],
        ),
        pw.Column(
          crossAxisAlignment: pw.CrossAxisAlignment.end,
          children: [
            pw.Text(
              'RELEVÉ DE COMPTE',
              style: pw.TextStyle(fontSize: 16, fontWeight: pw.FontWeight.bold),
            ),
            pw.Text(
              'Compte: $accountName',
              style: const pw.TextStyle(fontSize: 12),
            ),
            pw.Text(
              'ID: $accountId',
              style: const pw.TextStyle(fontSize: 10, color: PdfColors.grey600),
            ),
            pw.Text(
              'Généré le: ${DateFormat('dd/MM/yyyy HH:mm').format(DateTime.now())}',
              style: const pw.TextStyle(fontSize: 10),
            ),
          ],
        ),
      ],
    );
  }

  static pw.Widget _buildTransactionsTable(
    List<EpargneTransactionModel> transactions,
    NumberFormat format,
  ) {
    final headers = ['Date', 'Libellé', 'Type', 'Montant'];

    return pw.TableHelper.fromTextArray(
      headers: headers,
      data: transactions.map((tx) {
        final date = DateTime.parse(tx.date);
        return [
          DateFormat('dd/MM/yyyy').format(date),
          tx.libelle,
          tx.type,
          format.format(tx.montant),
        ];
      }).toList(),
      headerStyle: pw.TextStyle(
        fontWeight: pw.FontWeight.bold,
        color: PdfColors.white,
      ),
      headerDecoration: const pw.BoxDecoration(color: PdfColors.blue900),
      cellHeight: 30,
      cellAlignments: {
        0: pw.Alignment.centerLeft,
        1: pw.Alignment.centerLeft,
        2: pw.Alignment.center,
        3: pw.Alignment.centerRight,
      },
    );
  }

  static pw.Widget _buildFooter() {
    return pw.Column(
      children: [
        pw.Divider(color: PdfColors.grey300),
        pw.SizedBox(height: 10),
        pw.Text(
          'Merci de votre confiance.',
          style: const pw.TextStyle(fontSize: 10, color: PdfColors.grey700),
        ),
        pw.Text(
          'microCredit - Siège social: ${AppGeo.headquartersAddress}',
          style: const pw.TextStyle(fontSize: 8, color: PdfColors.grey500),
        ),
      ],
    );
  }
}
