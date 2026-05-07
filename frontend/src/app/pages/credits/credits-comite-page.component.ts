import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CreditWorkflowService } from '../../services/credit-workflow.service';
import { WorkflowCreditSummary, etapeLabel } from '../../models/credit-workflow.model';

@Component({
  selector: 'app-credits-comite-page',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './credits-comite-page.component.html'
})
export class CreditsComitePageComponent implements OnInit {

  dossiers = signal<WorkflowCreditSummary[]>([]);
  loading  = signal(false);
  saving   = signal<number | null>(null);   // idCredit en cours d'action
  error    = signal<string | null>(null);
  success  = signal<string | null>(null);

  decisionDossier = signal<{ id: number; type: 'approuver' | 'rejeter' } | null>(null);
  commentaire = '';

  etapeLabel = etapeLabel;

  constructor(
    private workflowSvc: CreditWorkflowService,
    private router: Router
  ) {}

  ngOnInit(): void { this.charger(); }

  charger(): void {
    this.loading.set(true);
    this.error.set(null);
    this.workflowSvc.getComitePending().subscribe({
      next: data => { this.dossiers.set(data); this.loading.set(false); },
      error: e   => { this.error.set('Erreur : ' + (e.error?.message ?? e.message)); this.loading.set(false); }
    });
  }

  ouvrir(id: number): void {
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

  etapeBadge(etape: string): string {
    const e = (etape || '').toUpperCase();
    if (e.includes('COMITE'))    return 'badge badge-warning';
    if (e.includes('VISA_RC'))   return 'badge badge-info';
    if (e.includes('VISA_SF'))   return 'badge badge-info';
    if (e.includes('ANALYSE'))   return 'badge badge-primary';
    if (e.includes('VALID'))     return 'badge badge-success';
    if (e.includes('REJET'))     return 'badge badge-danger';
    return 'badge badge-info';
  }
}
