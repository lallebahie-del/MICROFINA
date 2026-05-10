import 'dart:typed_data';
import 'package:intl/intl.dart';
import 'package:pdf/pdf.dart';
import 'package:pdf/widgets.dart' as pw;
import 'package:printing/printing.dart';

class PdfGeneratorService {
  static Future<void> generateAndPreviewTransactions({
    required String accountName,
    required String accountId,
    required List<Map<String, dynamic>> transactions,
  }) async {
    final pdf = pw.Document();
    final currencyFormat = NumberFormat.currency(locale: 'fr_FR', symbol: 'FCFA', decimalDigits: 0);

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

  static pw.Widget _buildHeader(String accountName, String accountId) {
    return pw.Row(
      mainAxisAlignment: pw.MainAxisAlignment.spaceBetween,
      children: [
        pw.Column(
          crossAxisAlignment: pw.CrossAxisAlignment.start,
          children: [
            pw.Text('microCredit', style: pw.TextStyle(fontSize: 24, fontWeight: pw.FontWeight.bold, color: PdfColors.blue900)),
            pw.Text('Votre banque, partout avec vous.', style: const pw.TextStyle(fontSize: 10, color: PdfColors.grey700)),
          ],
        ),
        pw.Column(
          crossAxisAlignment: pw.CrossAxisAlignment.end,
          children: [
            pw.Text('RELEVÉ DE COMPTE', style: pw.TextStyle(fontSize: 16, fontWeight: pw.FontWeight.bold)),
            pw.Text('Compte: $accountName', style: const pw.TextStyle(fontSize: 12)),
            pw.Text('ID: $accountId', style: const pw.TextStyle(fontSize: 10, color: PdfColors.grey600)),
            pw.Text('Généré le: ${DateFormat('dd/MM/yyyy HH:mm').format(DateTime.now())}', style: const pw.TextStyle(fontSize: 10)),
          ],
        ),
      ],
    );
  }

  static pw.Widget _buildTransactionsTable(List<Map<String, dynamic>> transactions, NumberFormat format) {
    final headers = ['Date', 'Libellé', 'Type', 'Montant'];
    
    return pw.TableHelper.fromTextArray(
      headers: headers,
      data: transactions.map((tx) {
        final date = DateTime.parse(tx['date']);
        return [
          DateFormat('dd/MM/yyyy').format(date),
          tx['libelle'],
          tx['type'],
          format.format(tx['montant']),
        ];
      }).toList(),
      headerStyle: pw.TextStyle(fontWeight: pw.FontWeight.bold, color: PdfColors.white),
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
        pw.Text('Merci de votre confiance.', style: const pw.TextStyle(fontSize: 10, color: PdfColors.grey700)),
        pw.Text('microCredit - Siège social: Avenue Cheikh Anta Diop, Dakar, Sénégal', style: const pw.TextStyle(fontSize: 8, color: PdfColors.grey500)),
      ],
    );
  }
}
