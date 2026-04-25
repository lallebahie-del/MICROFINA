import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { ToastService } from '../../core/toast.service';
import { formatFCFA, formatDate, formatDateTime, statusBadge, STATUS_LABELS, PRODUCT_LABELS, isAuditor, OP_TYPE_LABELS } from '../../core/format';

interface ScheduleRow { installment: number; dueDate: string; principal: number; interest: number; total: number; balance: number; paid?: boolean; }
interface CreditDetail {
  id: string; reference: string; clientId: string; clientName: string | null;
  product: string; principal: number; interestRate: number; durationMonths: number;
  monthlyPayment: number; outstanding: number; status: string;
  guarantee: string | null; purpose: string | null;
  disbursedAt: string | null; nextDueDate: string | null; daysOverdue: number; createdAt: string;
  schedule: ScheduleRow[];
  payments: Array<{ id: string; reference: string; operationType: string; amount: number; userName: string | null; createdAt: string }>;
}

@Component({
  selector: 'app-credit-detail',
  standalone: true,
  imports: [RouterLink],
  template: `
    @if (data(); as c) {
      <div class="page-header">
        <div>
          <h1>{{ c.reference }}</h1>
          <div class="text-muted">
            <a [routerLink]="['/clients', c.clientId]">{{ c.clientName ?? '—' }}</a> · {{ productLabel(c.product) }} ·
            <span class="badge" [class]="badge(c.status)">{{ label(c.status) }}</span>
          </div>
        </div>
        <a class="btn btn-secondary" routerLink="/credits">← Retour</a>
      </div>

      <div class="grid grid-cols-4 gap-4 mb-6">
        <div class="stat"><div class="label">Principal</div><div class="value">{{ fmt(c.principal) }}</div></div>
        <div class="stat"><div class="label">Encours</div><div class="value">{{ fmt(c.outstanding) }}</div></div>
        <div class="stat"><div class="label">Mensualité</div><div class="value">{{ fmt(c.monthlyPayment) }}</div></div>
        <div class="stat"><div class="label">Taux / Durée</div><div class="value">{{ c.interestRate }}% · {{ c.durationMonths }}m</div></div>
      </div>

      <div class="grid grid-cols-2 gap-4 mb-6">
        <div class="card">
          <div class="card-header"><h3 class="card-title">Informations</h3></div>
          <div class="grid grid-cols-2 gap-2">
            <div><div class="text-xs text-muted">Décaissé le</div><div>{{ fmtDate(c.disbursedAt) }}</div></div>
            <div><div class="text-xs text-muted">Prochaine échéance</div><div>{{ fmtDate(c.nextDueDate) }}</div></div>
            <div><div class="text-xs text-muted">Garantie</div><div>{{ c.guarantee ?? '—' }}</div></div>
            <div><div class="text-xs text-muted">Retard</div><div [style.color]="c.daysOverdue > 0 ? 'var(--color-danger)' : ''">{{ c.daysOverdue }} jours</div></div>
            <div style="grid-column: 1 / -1;"><div class="text-xs text-muted">Objet</div><div>{{ c.purpose ?? '—' }}</div></div>
          </div>
        </div>

        @if (!isAud()) {
          <div class="card">
            <div class="card-header"><h3 class="card-title">Actions</h3></div>
            <div class="flex flex-col gap-2">
              @if (c.status === 'demande') {
                <button class="btn" (click)="changeStatus('approuve')">Approuver la demande</button>
                <button class="btn btn-danger" (click)="changeStatus('rejete')">Rejeter la demande</button>
              }
              @if (c.status === 'approuve') {
                <button class="btn" (click)="changeStatus('debloque')">Débloquer le crédit</button>
              }
              @if (c.status === 'en_cours' || c.status === 'debloque') {
                <button class="btn btn-secondary" (click)="changeStatus('en_retard')">Marquer en retard</button>
                <button class="btn" (click)="changeStatus('solde')">Marquer soldé</button>
              }
              @if (c.status === 'en_retard') {
                <button class="btn" (click)="changeStatus('en_cours')">Régulariser</button>
                <button class="btn" (click)="changeStatus('solde')">Marquer soldé</button>
              }
            </div>
          </div>
        }
      </div>

      <div class="card mb-4">
        <div class="card-header"><h3 class="card-title">Échéancier</h3></div>
        <div class="table-wrap" style="border:none; max-height: 400px; overflow-y: auto;">
          <table class="table">
            <thead><tr><th class="num">N°</th><th>Échéance</th><th class="num">Capital</th><th class="num">Intérêt</th><th class="num">Total</th><th class="num">Restant</th></tr></thead>
            <tbody>
              @for (row of c.schedule; track row.installment) {
                <tr [style.opacity]="row.paid ? '.55' : '1'">
                  <td class="num">{{ row.installment }}</td>
                  <td>{{ fmtDate(row.dueDate) }}</td>
                  <td class="num">{{ fmt(row.principal) }}</td>
                  <td class="num">{{ fmt(row.interest) }}</td>
                  <td class="num"><strong>{{ fmt(row.total) }}</strong></td>
                  <td class="num">{{ fmt(row.balance) }}</td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      </div>

      <div class="card">
        <div class="card-header"><h3 class="card-title">Remboursements</h3></div>
        @if (c.payments.length === 0) { <div class="text-muted text-sm">Aucun remboursement</div> } @else {
          <div class="table-wrap" style="border:none;">
            <table class="table">
              <thead><tr><th>Référence</th><th>Type</th><th class="num">Montant</th><th>Agent</th><th>Date</th></tr></thead>
              <tbody>
                @for (p of c.payments; track p.id) {
                  <tr>
                    <td>{{ p.reference }}</td>
                    <td><span class="badge">{{ opLabel(p.operationType) }}</span></td>
                    <td class="num">{{ fmt(p.amount) }}</td>
                    <td>{{ p.userName ?? '—' }}</td>
                    <td>{{ fmtDt(p.createdAt) }}</td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
        }
      </div>
    }
  `,
})
export class CreditDetailComponent implements OnInit {
  private api = inject(ApiService);
  private auth = inject(AuthService);
  private route = inject(ActivatedRoute);
  private toast = inject(ToastService);
  data = signal<CreditDetail | null>(null);
  isAud = computed(() => isAuditor(this.auth.user()?.role));

  private id = '';
  ngOnInit() {
    this.id = this.route.snapshot.paramMap.get('id')!;
    this.refresh();
  }
  refresh() { this.api.get<CreditDetail>(`/credits/${this.id}`).subscribe((d) => this.data.set(d)); }

  changeStatus(status: string) {
    this.api.patch(`/credits/${this.id}`, { status }).subscribe({
      next: () => { this.toast.success('Statut mis à jour'); this.refresh(); },
      error: (e) => this.toast.error(e?.error?.error ?? 'Erreur'),
    });
  }

  fmt = formatFCFA; fmtDate = formatDate; fmtDt = formatDateTime;
  badge = statusBadge;
  label = (s: string) => STATUS_LABELS[s] ?? s;
  productLabel = (s: string) => PRODUCT_LABELS[s] ?? s;
  opLabel = (s: string) => OP_TYPE_LABELS[s] ?? s;
}
