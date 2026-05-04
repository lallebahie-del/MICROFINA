import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule }  from '@angular/forms';
import { Observable } from 'rxjs';
import {
  ReportingService,
  EtatCredit, RatiosBcm, Indicateur,
  LigneBalance, LigneBilan, ListeClient
} from '../../services/reporting.service';

type TabId = 'credits' | 'ratios' | 'indicateurs' | 'bilan' | 'balance' | 'clients';

@Component({
  selector: 'app-reporting',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './reporting.html',
  styleUrl: './reporting.css'
})
export class ReportingComponent implements OnInit {

  activeTab = signal<TabId>('credits');
  loading   = signal(false);
  error     = signal<string | null>(null);
  agence    = '';

  // Données des onglets
  credits    = signal<EtatCredit[]>([]);
  ratios     = signal<RatiosBcm[]>([]);
  indicateurs = signal<Indicateur[]>([]);
  bilan      = signal<LigneBilan[]>([]);
  balance    = signal<LigneBalance[]>([]);
  clients    = signal<ListeClient[]>([]);

  readonly tabs: { id: TabId; label: string }[] = [
    { id: 'credits',     label: 'État Crédits' },
    { id: 'ratios',      label: 'Ratios BCM' },
    { id: 'indicateurs', label: 'Indicateurs' },
    { id: 'bilan',       label: 'Bilan' },
    { id: 'balance',     label: 'Balance' },
    { id: 'clients',     label: 'Liste Clients' },
  ];

  constructor(private reportingService: ReportingService) {}

  ngOnInit(): void { this.loadTab(this.activeTab()); }

  selectTab(tab: TabId): void {
    this.activeTab.set(tab);
    this.loadTab(tab);
  }

  loadTab(tab: TabId): void {
    this.loading.set(true);
    this.error.set(null);

    const obs$: Observable<unknown[]> = (() => {
      switch (tab) {
        case 'credits':     return this.reportingService.getEtatCredits(this.agence) as Observable<unknown[]>;
        case 'ratios':      return this.reportingService.getRatiosBcm() as Observable<unknown[]>;
        case 'indicateurs': return this.reportingService.getIndicateurs(this.agence) as Observable<unknown[]>;
        case 'bilan':       return this.reportingService.getBilan() as Observable<unknown[]>;
        case 'balance':     return this.reportingService.getBalance() as Observable<unknown[]>;
        case 'clients':     return this.reportingService.getListeClients(this.agence) as Observable<unknown[]>;
      }
    })();

    obs$?.subscribe({
      next: (data: unknown[]) => {
        switch (tab) {
          case 'credits':     this.credits.set(data as EtatCredit[]); break;
          case 'ratios':      this.ratios.set(data as RatiosBcm[]); break;
          case 'indicateurs': this.indicateurs.set(data as Indicateur[]); break;
          case 'bilan':       this.bilan.set(data as LigneBilan[]); break;
          case 'balance':     this.balance.set(data as LigneBalance[]); break;
          case 'clients':     this.clients.set(data as ListeClient[]); break;
        }
        this.loading.set(false);
      },
      error: (e: Error) => {
        this.error.set('Erreur chargement : ' + e.message);
        this.loading.set(false);
      }
    });
  }

  onFilter(): void { this.loadTab(this.activeTab()); }

  // ── Exports ───────────────────────────────────────────────────────────────

  exportCreditsExcel(): void {
    this.reportingService.exportCreditsExcel(this.agence).subscribe(blob =>
      this.telecharger(blob, 'portefeuille-credits.xlsx'));
  }

  exportRatiosExcel(): void {
    this.reportingService.exportRatiosExcel().subscribe(blob =>
      this.telecharger(blob, 'ratios-bcm.xlsx'));
  }

  exportBilanExcel(): void {
    this.reportingService.exportBilanExcel().subscribe(blob =>
      this.telecharger(blob, 'bilan.xlsx'));
  }

  exportRapportWord(): void {
    this.reportingService.exportRapportWord().subscribe(blob =>
      this.telecharger(blob, 'rapport-financier.docx'));
  }

  exportClientsPdf(): void {
    this.reportingService.exportClientsPdf(this.agence).subscribe(blob =>
      this.telecharger(blob, 'liste-clients.pdf'));
  }

  exportRatiosPdf(): void {
    this.reportingService.exportRatiosPdf().subscribe(blob =>
      this.telecharger(blob, 'ratios-bcm.pdf'));
  }

  private telecharger(blob: Blob, filename: string): void {
    const url = URL.createObjectURL(blob);
    const a   = document.createElement('a');
    a.href = url; a.download = filename; a.click();
    URL.revokeObjectURL(url);
  }

  // ── Helpers template ──────────────────────────────────────────────────────

  couleurPar(cat: string): string {
    switch (cat) {
      case 'PAR30':      return '#f39c12';
      case 'PAR90':      return '#e67e22';
      case 'PAR180':     return '#e74c3c';
      case 'PAR180_PLUS': return '#8e44ad';
      default:           return '#27ae60';
    }
  }

  formatMontant(v: number): string {
    if (v == null) return '—';
    return new Intl.NumberFormat('fr-MR', { style: 'currency', currency: 'MRU',
      minimumFractionDigits: 0, maximumFractionDigits: 0 }).format(v);
  }

  formatPct(v: number): string {
    if (v == null) return '—';
    return (v * 100).toFixed(2) + '%';
  }
}
