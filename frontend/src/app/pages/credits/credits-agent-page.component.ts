import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';
import { CreditWorkflowService } from '../../services/credit-workflow.service';
import {
  WorkflowCreditSummary,
  AnalyseFinanciereCreateRequest,
  WorkflowDecisionRequest,
  etapeLabel
} from '../../models/credit-workflow.model';

type ActionType = 'completude' | 'analyse' | 'visa-rc' | 'rejeter';

interface ActionState {
  idCredit: number;
  numCredit: string;
  type: ActionType;
}

@Component({
  selector: 'app-credits-agent-page',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './credits-agent-page.component.html'
})
export class CreditsAgentPageComponent implements OnInit {

  private all = signal<WorkflowCreditSummary[]>([]);

  // Filtres
  search       = signal<string>('');
  filtreAgence = signal<string>('');
  filtreEtape  = signal<string>('');

  dossiers = computed<WorkflowCreditSummary[]>(() => {
    const q = this.search().trim().toLowerCase();
    const fA = this.filtreAgence();
    const fE = this.filtreEtape();
    return this.all().filter(d => {
      if (fA && d.codeAgence !== fA) return false;
      if (fE && d.etapeCourante !== fE) return false;
      if (!q) return true;
      return (d.numCredit?.toLowerCase().includes(q))
          || (d.nomMembre?.toLowerCase().includes(q) ?? false)
          || (d.prenomMembre?.toLowerCase().includes(q) ?? false)
          || (d.numMembre?.toLowerCase().includes(q) ?? false);
    });
  });

  // Stats par étape
  countCompletude = computed<number>(() =>
    this.all().filter(d => d.etapeCourante === 'COMPLETUDE').length
  );
  countAnalyse = computed<number>(() =>
    this.all().filter(d => d.etapeCourante === 'ANALYSE_FINANCIERE').length
  );
  countVisaRc = computed<number>(() =>
    this.all().filter(d => d.etapeCourante === 'VISA_RC').length
  );

  montantCompletude = computed<number>(() =>
    this.sumMontants(d => d.etapeCourante === 'COMPLETUDE')
  );
  montantAnalyse = computed<number>(() =>
    this.sumMontants(d => d.etapeCourante === 'ANALYSE_FINANCIERE')
  );
  montantVisaRc = computed<number>(() =>
    this.sumMontants(d => d.etapeCourante === 'VISA_RC')
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

  // Action en cours
  action = signal<ActionState | null>(null);
  commentaire = '';

  // Form analyse financière
  analyseForm: AnalyseFinanciereCreateRequest = {
    revenusMensuels: 0,
    chargesMensuelles: 0,
    totalActif: undefined,
    totalPassif: undefined,
    commentaire: '',
    avisAgent: ''
  };

  etapeLabel = etapeLabel;

  constructor(
    private workflowSvc: CreditWorkflowService,
    private router: Router
  ) {}

  ngOnInit(): void { this.charger(); }

  charger(): void {
    this.loading.set(true);
    this.error.set(null);
    this.workflowSvc.getAgentPending().subscribe({
      next: data => { this.all.set(data); this.loading.set(false); },
      error: e   => { this.error.set('Erreur : ' + (e.error?.message ?? e.message)); this.loading.set(false); }
    });
  }

  resetFilters(): void {
    this.search.set('');
    this.filtreAgence.set('');
    this.filtreEtape.set('');
  }

  // ── Helpers stats ─────────────────────────────────────────────────────
  private sumMontants(filter: (d: WorkflowCreditSummary) => boolean): number {
    return this.all().filter(filter).reduce((s, d) => s + (d.montantDemande ?? 0), 0);
  }

  fmt(n?: number): string {
    if (n == null || isNaN(n)) return '—';
    return new Intl.NumberFormat('fr-FR', { maximumFractionDigits: 0 }).format(n);
  }

  joursClass(j?: number): string {
    if (j == null) return 'badge badge-info';
    if (j > 30) return 'badge badge-danger';
    if (j > 14) return 'badge badge-warning';
    return 'badge badge-success';
  }

  etapeBadge(e: string): string {
    if (e === 'COMPLETUDE')          return 'badge badge-info';
    if (e === 'ANALYSE_FINANCIERE')  return 'badge badge-warning';
    if (e === 'VISA_RC')             return 'badge badge-primary';
    return 'badge badge-info';
  }

  // ── Actions ───────────────────────────────────────────────────────────
  ouvrir(id: number): void {
    this.router.navigate(['/credits', id, 'workflow']);
  }

  detail(id: number): void {
    this.router.navigate(['/credits', id]);
  }

  openCompletude(d: WorkflowCreditSummary): void {
    this.action.set({ idCredit: d.idCredit, numCredit: d.numCredit, type: 'completude' });
    this.commentaire = '';
    this.error.set(null);
    this.success.set(null);
  }

  openAnalyse(d: WorkflowCreditSummary): void {
    this.action.set({ idCredit: d.idCredit, numCredit: d.numCredit, type: 'analyse' });
    this.analyseForm = {
      revenusMensuels: 0,
      chargesMensuelles: 0,
      totalActif: undefined,
      totalPassif: undefined,
      commentaire: '',
      avisAgent: ''
    };
    this.error.set(null);
    this.success.set(null);
  }

  openVisaRc(d: WorkflowCreditSummary): void {
    this.action.set({ idCredit: d.idCredit, numCredit: d.numCredit, type: 'visa-rc' });
    this.commentaire = '';
    this.error.set(null);
    this.success.set(null);
  }

  openRejeter(d: WorkflowCreditSummary): void {
    this.action.set({ idCredit: d.idCredit, numCredit: d.numCredit, type: 'rejeter' });
    this.commentaire = '';
    this.error.set(null);
    this.success.set(null);
  }

  cancelAction(): void {
    this.action.set(null);
  }

  submitAction(): void {
    const a = this.action();
    if (!a) return;

    if (a.type === 'rejeter' && !this.commentaire.trim()) {
      this.error.set('Le motif est obligatoire pour rejeter un dossier.');
      return;
    }
    if (a.type === 'analyse' && (this.analyseForm.revenusMensuels <= 0)) {
      this.error.set('Les revenus mensuels doivent être strictement positifs.');
      return;
    }

    this.saving.set(a.idCredit);
    this.error.set(null);

    const req: WorkflowDecisionRequest = { commentaire: this.commentaire || undefined };

    let call: Observable<any>;
    switch (a.type) {
      case 'completude':
        call = this.workflowSvc.completude(a.idCredit, req);
        break;
      case 'visa-rc':
        call = this.workflowSvc.visaRc(a.idCredit, req);
        break;
      case 'rejeter':
        call = this.workflowSvc.rejeter(a.idCredit, req);
        break;
      case 'analyse':
        call = this.workflowSvc.analyse(a.idCredit, this.analyseForm);
        break;
      default:
        return;
    }

    call.subscribe({
      next: () => {
        this.saving.set(null);
        const labels: Record<ActionType, string> = {
          'completude': 'Complétude validée',
          'analyse':    'Analyse financière enregistrée',
          'visa-rc':    'Visa RC apposé',
          'rejeter':    'Dossier rejeté'
        };
        this.success.set(`${labels[a.type]} sur le dossier ${a.numCredit}.`);
        this.action.set(null);
        this.charger();
      },
      error: (e: any) => {
        this.saving.set(null);
        this.error.set('Erreur : ' + (e.error?.message ?? e.message));
      }
    });
  }

  actionLabel(t: ActionType): string {
    return t === 'completude' ? 'Valider la complétude'
         : t === 'analyse'    ? "Saisir l'analyse financière"
         : t === 'visa-rc'    ? 'Apposer le visa RC'
         : 'Rejeter le dossier';
  }

  // Capacité de remboursement live (pour le formulaire)
  capaciteLive(): string {
    const r = this.analyseForm.revenusMensuels ?? 0;
    if (!r) return '—';
    return (((this.analyseForm.chargesMensuelles ?? 0) / r) * 100).toFixed(1) + ' %';
  }

  capaciteLiveClass(): string {
    const r = this.analyseForm.revenusMensuels ?? 0;
    if (!r) return 'badge badge-info';
    const ratio = (this.analyseForm.chargesMensuelles ?? 0) / r;
    if (ratio >= 0.7) return 'badge badge-danger';
    if (ratio >= 0.5) return 'badge badge-warning';
    return 'badge badge-success';
  }
}
