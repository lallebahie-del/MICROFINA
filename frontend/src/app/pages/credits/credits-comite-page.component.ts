import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CreditWorkflowService } from '../../services/credit-workflow.service';
import { GarantiesService, Garantie } from '../../services/garanties.service';
import {
  WorkflowCreditSummary,
  WorkflowTimelineEntry,
  AnalyseFinanciereDTO,
  etapeLabel
} from '../../models/credit-workflow.model';

interface DetailPanel {
  idCredit: number;
  numCredit: string;
  loading: boolean;
  analyse?: AnalyseFinanciereDTO | null;
  garanties?: Garantie[];
  timeline?: WorkflowTimelineEntry[];
}

@Component({
  selector: 'app-credits-comite-page',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './credits-comite-page.component.html'
})
export class CreditsComitePageComponent implements OnInit {

  private all = signal<WorkflowCreditSummary[]>([]);

  search       = signal<string>('');
  filtreAgence = signal<string>('');
  filtreMontantMin = signal<number | null>(null);

  dossiers = computed<WorkflowCreditSummary[]>(() => {
    const q = this.search().trim().toLowerCase();
    const fA = this.filtreAgence();
    const fM = this.filtreMontantMin();
    return this.all().filter(d => {
      if (fA && d.codeAgence !== fA) return false;
      if (fM && (d.montantDemande ?? 0) < fM) return false;
      if (!q) return true;
      return (d.numCredit?.toLowerCase().includes(q))
          || (d.nomMembre?.toLowerCase().includes(q) ?? false)
          || (d.prenomMembre?.toLowerCase().includes(q) ?? false)
          || (d.numMembre?.toLowerCase().includes(q) ?? false);
    });
  });

  totalDossiers = computed<number>(() => this.dossiers().length);
  montantCumule = computed<number>(() =>
    this.dossiers().reduce((s, d) => s + (d.montantDemande ?? 0), 0)
  );
  agencesUniques = computed<string[]>(() => {
    const set = new Set<string>();
    this.all().forEach(d => { if (d.codeAgence) set.add(d.codeAgence); });
    return Array.from(set).sort();
  });

  loading = signal(false);
  saving  = signal<number | null>(null);
  error   = signal<string | null>(null);
  success = signal<string | null>(null);

  decisionDossier = signal<{ id: number; type: 'approuver' | 'rejeter' } | null>(null);
  commentaire = '';

  panels = signal<Map<number, DetailPanel>>(new Map());

  etapeLabel = etapeLabel;

  constructor(
    private workflowSvc: CreditWorkflowService,
    private garantiesSvc: GarantiesService,
    private router: Router
  ) {}

  ngOnInit(): void { this.charger(); }

  charger(): void {
    this.loading.set(true);
    this.error.set(null);
    this.workflowSvc.getComitePending().subscribe({
      next: data => { this.all.set(data); this.loading.set(false); },
      error: e   => { this.error.set('Erreur : ' + (e.error?.message ?? e.message)); this.loading.set(false); }
    });
  }

  resetFilters(): void {
    this.search.set('');
    this.filtreAgence.set('');
    this.filtreMontantMin.set(null);
  }

  togglePanel(d: WorkflowCreditSummary): void {
    const map = new Map(this.panels());
    if (map.has(d.idCredit)) {
      map.delete(d.idCredit);
      this.panels.set(map);
      return;
    }
    map.set(d.idCredit, { idCredit: d.idCredit, numCredit: d.numCredit, loading: true });
    this.panels.set(map);

    let pending = 3;
    const done = () => {
      pending--;
      if (pending === 0) this.updatePanel(d.idCredit, p => p.loading = false);
    };

    this.workflowSvc.getAnalyse(d.idCredit).subscribe({
      next: a => { this.updatePanel(d.idCredit, p => p.analyse = a); done(); },
      error: () => { this.updatePanel(d.idCredit, p => p.analyse = null); done(); }
    });
    this.garantiesSvc.getByCreditId(d.idCredit).subscribe({
      next: g => { this.updatePanel(d.idCredit, p => p.garanties = g); done(); },
      error: () => { this.updatePanel(d.idCredit, p => p.garanties = []); done(); }
    });
    this.workflowSvc.getTimeline(d.idCredit).subscribe({
      next: t => { this.updatePanel(d.idCredit, p => p.timeline = t); done(); },
      error: () => { this.updatePanel(d.idCredit, p => p.timeline = []); done(); }
    });
  }

  private updatePanel(id: number, mutator: (p: DetailPanel) => void): void {
    const m = new Map(this.panels());
    const p = m.get(id);
    if (p) { mutator(p); m.set(id, p); this.panels.set(m); }
  }

  panelFor(id: number): DetailPanel | undefined {
    return this.panels().get(id);
  }

  ouvrirWorkflow(id: number): void {
    this.router.navigate(['/credits', id, 'workflow']);
  }

  detail(id: number): void {
    this.router.navigate(['/credits', id]);
  }

  openApprouver(id: number): void {
    this.decisionDossier.set({ id, type: 'approuver' });
    this.commentaire = '';
    this.error.set(null);
    this.success.set(null);
  }

  openRejeter(id: number): void {
    this.decisionDossier.set({ id, type: 'rejeter' });
    this.commentaire = '';
    this.error.set(null);
    this.success.set(null);
  }

  cancelDecision(): void {
    this.decisionDossier.set(null);
  }

  submitDecision(): void {
    const d = this.decisionDossier();
    if (!d) return;
    if (d.type === 'rejeter' && !this.commentaire.trim()) {
      this.error.set('Le motif est obligatoire pour rejeter un dossier.');
      return;
    }
    this.saving.set(d.id);
    this.error.set(null);

    const call = d.type === 'approuver'
      ? this.workflowSvc.comiteApprouver(d.id, { commentaire: this.commentaire || undefined })
      : this.workflowSvc.comiteRejeter (d.id, { commentaire: this.commentaire });

    call.subscribe({
      next: () => {
        this.saving.set(null);
        this.success.set(`Dossier #${d.id} ${d.type === 'approuver' ? 'approuvé' : 'rejeté'}.`);
        this.decisionDossier.set(null);
        this.charger();
      },
      error: e => {
        this.saving.set(null);
        this.error.set('Erreur : ' + (e.error?.message ?? e.message));
      }
    });
  }

  fmt(n?: number): string {
    if (n == null || isNaN(n)) return '—';
    return new Intl.NumberFormat('fr-FR', { maximumFractionDigits: 0 }).format(n);
  }

  capaciteRemb(a?: AnalyseFinanciereDTO | null): string {
    if (!a || !a.revenusMensuels || a.revenusMensuels === 0) return '—';
    const ratio = ((a.chargesMensuelles ?? 0) / a.revenusMensuels) * 100;
    return ratio.toFixed(1) + ' %';
  }

  capaciteClass(a?: AnalyseFinanciereDTO | null): string {
    if (!a || !a.revenusMensuels) return 'badge badge-info';
    const ratio = (a.chargesMensuelles ?? 0) / a.revenusMensuels;
    if (ratio >= 0.7) return 'badge badge-danger';
    if (ratio >= 0.5) return 'badge badge-warning';
    return 'badge badge-success';
  }

  totalGaranties(g?: Garantie[]): number {
    if (!g) return 0;
    return g.reduce((s, x) => s + (x.valeurEstimee ?? 0), 0);
  }

  tauxCouverture(p: DetailPanel): string {
    const total = this.totalGaranties(p.garanties);
    const dossier = this.all().find(d => d.idCredit === p.idCredit);
    const montant = dossier?.montantDemande ?? 0;
    if (!montant) return '—';
    return ((total / montant) * 100).toFixed(1) + ' %';
  }

  joursClass(j?: number): string {
    if (j == null) return 'badge badge-info';
    if (j > 30) return 'badge badge-danger';
    if (j > 14) return 'badge badge-warning';
    return 'badge badge-success';
  }
}
