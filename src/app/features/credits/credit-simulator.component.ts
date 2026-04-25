import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { ToastService } from '../../core/toast.service';
import { formatFCFA, formatDate } from '../../core/format';

interface ScheduleRow { installment: number; dueDate: string; principal: number; interest: number; total: number; balance: number; }
interface SimResult { monthlyPayment: number; totalInterest: number; totalToRepay: number; schedule: ScheduleRow[]; }

@Component({
  selector: 'app-credit-simulator',
  standalone: true,
  imports: [FormsModule, RouterLink],
  template: `
    <div class="page-header">
      <div><h1>Simulateur de crédit</h1><div class="text-muted">Calcul d'échéancier — méthode amortissement constant</div></div>
      <a class="btn btn-secondary" routerLink="/credits">← Retour</a>
    </div>

    <div class="card mb-4">
      <form (ngSubmit)="run()">
        <div class="form-grid">
          <div class="form-row"><label>Montant principal (FCFA)</label><input class="input" type="number" min="1" [(ngModel)]="principal" name="principal" /></div>
          <div class="form-row"><label>Taux d'intérêt annuel (%)</label><input class="input" type="number" step="0.01" min="0" [(ngModel)]="rate" name="rate" /></div>
          <div class="form-row"><label>Durée (mois)</label><input class="input" type="number" min="1" max="120" [(ngModel)]="duration" name="duration" /></div>
        </div>
        <div class="flex justify-end mt-2">
          <button class="btn" type="submit" [disabled]="loading()">{{ loading() ? 'Calcul…' : 'Simuler' }}</button>
        </div>
      </form>
    </div>

    @if (result(); as r) {
      <div class="grid grid-cols-3 gap-4 mb-6">
        <div class="stat"><div class="label">Mensualité</div><div class="value">{{ fmt(r.monthlyPayment) }}</div></div>
        <div class="stat"><div class="label">Intérêts totaux</div><div class="value">{{ fmt(r.totalInterest) }}</div></div>
        <div class="stat"><div class="label">Total à rembourser</div><div class="value">{{ fmt(r.totalToRepay) }}</div></div>
      </div>

      <div class="card">
        <div class="card-header"><h3 class="card-title">Tableau d'amortissement</h3></div>
        <div class="table-wrap" style="border:none;">
          <table class="table">
            <thead><tr><th class="num">N°</th><th>Échéance</th><th class="num">Capital</th><th class="num">Intérêt</th><th class="num">Mensualité</th><th class="num">Capital restant</th></tr></thead>
            <tbody>
              @for (row of r.schedule; track row.installment) {
                <tr>
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
    }
  `,
})
export class CreditSimulatorComponent {
  private api = inject(ApiService);
  private toast = inject(ToastService);
  principal = 500000; rate = 12; duration = 12;
  loading = signal(false);
  result = signal<SimResult | null>(null);

  run() {
    this.loading.set(true);
    this.api.post<SimResult>('/credits/simulate', {
      principal: Number(this.principal),
      interestRate: Number(this.rate),
      durationMonths: Number(this.duration),
    }).subscribe({
      next: (r) => { this.loading.set(false); this.result.set(r); },
      error: (e) => { this.loading.set(false); this.toast.error(e?.error?.error ?? 'Erreur'); },
    });
  }
  fmt = formatFCFA; fmtDate = formatDate;
}
