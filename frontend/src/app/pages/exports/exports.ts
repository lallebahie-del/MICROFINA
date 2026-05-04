import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ExportsService, ExportEtat, ExportFormat } from '../../services/exports.service';

interface ExportItem {
  label: string;
  etat: ExportEtat;
  formats: ExportFormat[];
}

@Component({
  selector: 'app-exports',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './exports.html'
})
export class ExportsComponent {
  agenceFilter = '';
  loading = false;

  items: ExportItem[] = [
    { label: 'Portefeuille crédit',       etat: 'credits',            formats: ['excel', 'pdf', 'word'] },
    { label: 'Ratios BCM',                etat: 'ratios-bcm',         formats: ['excel', 'pdf'] },
    { label: 'Bilan simplifié',           etat: 'bilan',              formats: ['excel', 'pdf', 'word'] },
    { label: 'Balance des comptes',       etat: 'balance-comptes',    formats: ['excel', 'pdf'] },
    { label: 'Journal comptable',         etat: 'journal',            formats: ['excel', 'pdf'] },
    { label: 'Indicateurs de perf.',      etat: 'indicateurs',        formats: ['excel'] },
    { label: 'Liste des clients',         etat: 'liste-clients',      formats: ['excel', 'pdf'] },
    { label: 'Rapport financier narratif',etat: 'rapport-financier',  formats: ['word'] },
    { label: 'Compte de résultat',        etat: 'compte-resultat',    formats: ['excel', 'pdf', 'word'] },
    { label: 'Tableau de financement',    etat: 'tableau-financement',formats: ['excel', 'pdf', 'word'] },
    { label: 'Balance âgée',              etat: 'balance-agee',       formats: ['excel', 'pdf'] },
    { label: 'Portefeuille (récap.)',      etat: 'portefeuille',       formats: ['excel', 'pdf', 'word'] },
  ];

  constructor(private svc: ExportsService) {}

  download(etat: ExportEtat, format: ExportFormat): void {
    this.loading = true;
    this.svc.export(etat, format, this.agenceFilter || undefined).subscribe({
      next: blob => {
        this.svc.downloadBlob(blob, `${etat}_${format}_${today()}.${ext(format)}`);
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  downloadSage(): void {
    this.loading = true;
    this.svc.exportSage(this.agenceFilter || undefined).subscribe({
      next: blob => { this.svc.downloadBlob(blob, `sage_compta_L_${today()}.csv`); this.loading = false; },
      error: () => { this.loading = false; }
    });
  }
}

function today(): string {
  return new Date().toISOString().slice(0, 10).replace(/-/g, '');
}

function ext(f: ExportFormat): string {
  return f === 'excel' ? 'xlsx' : f === 'word' ? 'docx' : 'pdf';
}
