import { Component, OnInit, signal } from '@angular/core';
import { CommonModule }              from '@angular/common';
import { FormsModule }               from '@angular/forms';
import { ActivatedRoute, Router }    from '@angular/router';
import { CreditsService, Credit, CreditStatut } from '../../services/credits.service';
import {
  CreditPaymentsService,
  CreditPaymentsResponse,
  CreditPaymentsPreviewResponse,
} from '../../services/credit-payments.service';

@Component({
  selector: 'app-credit-detail',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './credit-detail.html'
})
export class CreditDetailComponent implements OnInit {

  credit  = signal<Credit | null>(null);
  loading = signal(false);
  error   = signal<string | null>(null);
  success = signal<string | null>(null);

  activeTab = signal<'resume' | 'payments' | 'preview'>('resume');
  payments = signal<CreditPaymentsResponse | null>(null);
  paymentsLoading = signal(false);
  paymentsError = signal<string | null>(null);
  preview = signal<CreditPaymentsPreviewResponse | null>(null);
  previewLoading = signal(false);
  previewError = signal<string | null>(null);

  // Payment modal
  showPaymentModal = signal(false);
  paymentForm = { montant: 0, dateReglement: '', numPiece: '', modePaiement: 'ESPECES' };
  paymentSaving = signal(false);
  paymentError = signal<string | null>(null);

  constructor(
    private route:       ActivatedRoute,
    private router:      Router,
    private service:     CreditsService,
    private paymentsSvc: CreditPaymentsService,
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) { this.router.navigate(['/credits']); return; }
    this.loading.set(true);
    this.service.getOne(id).subscribe({
      next:  c => { this.credit.set(c); this.loading.set(false); },
      error: e => { this.error.set('Erreur : ' + e.message); this.loading.set(false); }
    });
  }

  setTab(tab: 'resume' | 'payments' | 'preview'): void {
    this.activeTab.set(tab);
    if (tab === 'payments') {
      this.loadPayments();
    }
    if (tab === 'preview') {
      this.loadPreview();
    }
  }

  loadPayments(): void {
    const c = this.credit();
    if (!c?.idCredit) return;
    if (this.paymentsLoading()) return;

    this.paymentsError.set(null);
    this.paymentsLoading.set(true);
    this.paymentsSvc.getSuivi(c.idCredit).subscribe({
      next: r => { this.payments.set(r); this.paymentsLoading.set(false); },
      error: e => {
        this.paymentsError.set(e.error?.message ?? e.message ?? 'Erreur lors du chargement des échéances.');
        this.paymentsLoading.set(false);
      }
    });
  }

  loadPreview(): void {
    const c = this.credit();
    if (!c?.idCredit) return;
    if (this.previewLoading()) return;

    this.previewError.set(null);
    this.previewLoading.set(true);
    this.paymentsSvc.getAmortissementPreview(c.idCredit).subscribe({
      next: r => {
        this.preview.set(r);
        this.previewLoading.set(false);
      },
      error: e => {
        this.previewError.set(
          e.error?.message ?? e.message ?? 'Impossible de calculer le prévisionnel.',
        );
        this.previewLoading.set(false);
      },
    });
  }

  openPaymentModal(): void {
    const today = new Date().toISOString().slice(0, 10);
    this.paymentForm = { montant: 0, dateReglement: today, numPiece: '', modePaiement: 'ESPECES' };
    this.paymentError.set(null);
    this.showPaymentModal.set(true);
  }

  closePaymentModal(): void {
    this.showPaymentModal.set(false);
  }

  submitPayment(): void {
    const c = this.credit();
    if (!c?.idCredit || !this.paymentForm.montant) return;
    this.paymentSaving.set(true);
    this.paymentError.set(null);
    this.paymentsSvc.encaisserPaiement(c.idCredit, {
      montant:       this.paymentForm.montant,
      dateReglement: this.paymentForm.dateReglement || undefined,
      numPiece:      this.paymentForm.numPiece || undefined,
      modePaiement:  this.paymentForm.modePaiement,
      codeAgence:    c.agenceCode,
    }).subscribe({
      next: r => {
        this.paymentSaving.set(false);
        this.showPaymentModal.set(false);
        this.success.set(`Paiement de ${r.montantTotal?.toLocaleString('fr-FR')} MRU enregistré.`);
        setTimeout(() => this.success.set(null), 4000);
        // Reload payments tab
        this.payments.set(null);
        this.loadPayments();
      },
      error: e => {
        this.paymentSaving.set(false);
        this.paymentError.set(e.error?.message ?? e.message ?? 'Erreur lors de l\'enregistrement.');
      }
    });
  }

  transitionner(statut: CreditStatut): void {
    const c = this.credit();
    if (!c?.idCredit) return;
    const labels: Record<string, string> = {
      SOUMIS:         'Soumettre ce crédit',
      VALIDE_AGENT:   'Valider en tant qu\'agent',
      VALIDE_COMITE:  'Valider en tant que comité',
      DEBLOQUE:       'Débloquer (décaisser) ce crédit',
      REJETE:         'Rejeter ce crédit',
      SOLDE:          'Marquer comme soldé',
    };
    if (!confirm(`${labels[statut] ?? statut} ?`)) return;

    this.error.set(null);
    this.service.transitionner(c.idCredit, statut).subscribe({
      next: updated => {
        this.credit.set(updated);
        this.success.set(`Statut mis à jour : ${updated.statut}`);
        setTimeout(() => this.success.set(null), 3000);
      },
      error: e => this.error.set(e.error?.message ?? e.message)
    });
  }

  edit(): void {
    const c = this.credit();
    if (c?.idCredit) this.router.navigate(['/credits', c.idCredit, 'edit']);
  }

  retour(): void { this.router.navigate(['/credits']); }

  badgeClass(statut: string): string {
    switch (statut) {
      case 'BROUILLON':     return 'badge badge-warning';
      case 'SOUMIS':        return 'badge badge-info';
      case 'VALIDE_AGENT':  return 'badge badge-info';
      case 'VALIDE_COMITE': return 'badge badge-primary';
      case 'DEBLOQUE':      return 'badge badge-success';
      case 'SOLDE':         return 'badge badge-success';
      case 'REJETE':        return 'badge badge-danger';
      default:              return 'badge';
    }
  }
}
