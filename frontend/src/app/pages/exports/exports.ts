import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ExportsService, ExportEtat, ExportFormat } from '../../services/exports.service';

interface ExportItem {
  label: string;
  description: string;
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
  agenceFilter = signal<string>('');
  loading      = signal<string | null>(null);
  error        = signal<string | null>(null);
  success      = signal<string | null>(null);

  items: ExportItem[] = [
    { label: 'Portefeuille crédit',         description: 'Détail des crédits actifs avec encours et statuts',                etat: 'credits',             formats: ['excel', 'pdf', 'word'] },
    { label: 'Ratios BCM',                  description: 'PAR30, PAR90, taux de couverture, indicateurs réglementaires',     etat: 'ratios-bcm',          formats: ['excel', 'pdf'] },
    { label: 'Indicateurs de performance',  description: 'Membres, crédits, encours et flux par agence',                     etat: 'indicateurs',         formats: ['excel'] },
    { label: 'Liste des clients',           description: 'Annuaire des membres avec coordonnées et encours',                 etat: 'liste-clients',       formats: ['excel', 'pdf'] },
    { label: 'Bilan simplifié',             description: 'État Actif/Passif au format réglementaire BCM',                    etat: 'bilan',               formats: ['excel', 'pdf', 'word'] },
    { label: 'Compte de résultat',          description: 'Charges et produits de la période',                                etat: 'compte-resultat',     formats: ['excel', 'pdf', 'word'] },
    { label: 'Balance des comptes',         description: 'Soldes débiteurs et créditeurs par compte',                        etat: 'balance-comptes',     formats: ['excel', 'pdf'] },
    { label: 'Journal comptable',           description: 'Écritures du journal pour la période sélectionnée',                etat: 'journal',             formats: ['excel', 'pdf'] },
    { label: 'Tableau de financement',      description: 'Emplois et ressources, variations du fonds de roulement',          etat: 'tableau-financement', formats: ['excel', 'pdf', 'word'] },
    { label: 'Balance âgée',                description: 'Antériorité des arriérés par tranche (PAR30, PAR60, PAR90)',       etat: 'balance-agee',        formats: ['excel', 'pdf'] },
    { label: 'Portefeuille (récap.)',       description: 'Récapitulatif synthétique du portefeuille de crédit',              etat: 'portefeuille',        formats: ['excel', 'pdf', 'word'] },
    { label: 'Rapport financier narratif',  description: 'Document narratif consolidé pour audit ou conseil',                etat: 'rapport-financier',   formats: ['word'] }
  ];

  constructor(private svc: ExportsService) {}

  download(etat: ExportEtat, format: ExportFormat): void {
    const key = `${etat}-${format}`;
    this.loading.set(key);
    this.error.set(null);
    this.success.set(null);

    this.svc.export(etat, format, this.agenceFilter() || undefined).subscribe({
      next: blob => {
        this.svc.downloadBlob(blob, `${etat}_${format}_${today()}.${ext(format)}`);
        this.loading.set(null);
        this.success.set(`Téléchargement ${etat} (${format.toUpperCase()}) terminé.`);
      },
      error: e => {
        this.loading.set(null);
        this.error.set('Erreur : ' + (e.error?.message ?? e.message));
      }
    });
  }

  downloadSage(): void {
    this.loading.set('sage');
    this.error.set(null);
    this.success.set(null);

    this.svc.exportSage(this.agenceFilter() || undefined).subscribe({
      next: blob => {
        this.svc.downloadBlob(blob, `sage_compta_L_${today()}.csv`);
        this.loading.set(null);
        this.success.set('Export Sage généré.');
      },
      error: e => {
        this.loading.set(null);
        this.error.set('Erreur : ' + (e.error?.message ?? e.message));
      }
    });
  }

  isLoading(etat: ExportEtat, format: ExportFormat): boolean {
    return this.loading() === `${etat}-${format}`;
  }

  formatBadge(format: ExportFormat): string {
    if (format === 'excel') return 'badge-success';
    if (format === 'word')  return 'badge-info';
    return 'badge-danger';
  }
}

function today(): string {
  return new Date().toISOString().slice(0, 10).replace(/-/g, '');
}

function ext(f: ExportFormat): string {
  return f === 'excel' ? 'xlsx' : f === 'word' ? 'docx' : 'pdf';
}
