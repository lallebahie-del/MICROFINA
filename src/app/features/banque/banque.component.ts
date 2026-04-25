import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { ToastService } from '../../core/toast.service';
import { formatFCFA, formatDateTime, OP_TYPE_LABELS, isAuditor } from '../../core/format';

interface BankOp {
  id: string; reference: string; operationType: string; amount: number;
  bankName: string; bankAccount: string | null; notes: string | null;
  userName: string | null; createdAt: string;
}

@Component({
  selector: 'app-banque',
  standalone: true,
  imports: [FormsModule],
  template: `
    <div class="page-header">
      <div><h1>Banque</h1><div class="text-muted">Mouvements bancaires</div></div>
    </div>

    <div class="grid grid-cols-3 gap-4 mb-6">
      <div class="stat"><div class="label">Versements</div><div class="value" style="color:var(--color-success);">{{ fmt(deposits()) }}</div></div>
      <div class="stat"><div class="label">Prélèvements</div><div class="value" style="color:var(--color-danger);">{{ fmt(withdrawals()) }}</div></div>
      <div class="stat"><div class="label">Solde net</div><div class="value">{{ fmt(deposits() - withdrawals()) }}</div></div>
    </div>

    @if (!isAud()) {
    <div class="card mb-6">
      <div class="card-header"><h3 class="card-title">Nouveau mouvement</h3></div>
      <form (ngSubmit)="submit()">
        <div class="form-grid">
          <div class="form-row"><label>Type *</label>
            <select class="select" [(ngModel)]="form.operationType" name="operationType" required>
              <option value="versement">Versement banque</option>
              <option value="prelevement">Prélèvement banque</option>
            </select>
          </div>
          <div class="form-row"><label>Banque *</label><input class="input" [(ngModel)]="form.bankName" name="bankName" required /></div>
          <div class="form-row"><label>N° compte bancaire</label><input class="input" [(ngModel)]="form.bankAccount" name="bankAccount" /></div>
          <div class="form-row"><label>Montant (FCFA) *</label><input class="input" type="number" min="1" [(ngModel)]="form.amount" name="amount" required /></div>
          <div class="form-row" style="grid-column: 1 / -1;"><label>Note</label><input class="input" [(ngModel)]="form.notes" name="notes" /></div>
        </div>
        <div class="flex justify-end mt-2">
          <button class="btn" type="submit" [disabled]="saving()">{{ saving() ? 'Enregistrement…' : 'Enregistrer' }}</button>
        </div>
      </form>
    </div>
    }

    <div class="card">
      <div class="card-header"><h3 class="card-title">Historique</h3></div>
      <div class="table-wrap" style="border:none;">
        <table class="table">
          <thead><tr><th>Référence</th><th>Type</th><th>Banque</th><th>Compte</th><th class="num">Montant</th><th>Note</th><th>Agent</th><th>Date</th></tr></thead>
          <tbody>
            @for (o of ops(); track o.id) {
              <tr>
                <td>{{ o.reference }}</td>
                <td><span class="badge">{{ label(o.operationType) }}</span></td>
                <td>{{ o.bankName }}</td>
                <td>{{ o.bankAccount ?? '—' }}</td>
                <td class="num">{{ fmt(o.amount) }}</td>
                <td>{{ o.notes ?? '—' }}</td>
                <td>{{ o.userName ?? '—' }}</td>
                <td>{{ fmtDt(o.createdAt) }}</td>
              </tr>
            } @empty { <tr><td colspan="8"><div class="empty">Aucun mouvement</div></td></tr> }
          </tbody>
        </table>
      </div>
    </div>
  `,
})
export class BanqueComponent implements OnInit {
  private api = inject(ApiService);
  private auth = inject(AuthService);
  private toast = inject(ToastService);
  ops = signal<BankOp[]>([]);
  saving = signal(false);
  isAud = computed(() => isAuditor(this.auth.user()?.role));
  form = { operationType: 'versement', bankName: '', bankAccount: '', amount: 0, notes: '' };

  ngOnInit() { this.refresh(); }
  refresh() { this.api.get<BankOp[]>('/bank/operations').subscribe((r) => this.ops.set(r)); }

  deposits = computed(() => this.ops().filter((o) => o.operationType === 'versement').reduce((s, o) => s + o.amount, 0));
  withdrawals = computed(() => this.ops().filter((o) => o.operationType === 'prelevement').reduce((s, o) => s + o.amount, 0));

  submit() {
    if (!this.form.bankName || !this.form.amount) { this.toast.error('Banque et montant requis'); return; }
    this.saving.set(true);
    this.api.post('/bank/operations', { ...this.form, amount: Number(this.form.amount) }).subscribe({
      next: () => { this.saving.set(false); this.toast.success('Mouvement enregistré'); this.form = { operationType: 'versement', bankName: '', bankAccount: '', amount: 0, notes: '' }; this.refresh(); },
      error: (e) => { this.saving.set(false); this.toast.error(e?.error?.error ?? 'Erreur'); },
    });
  }

  fmt = formatFCFA; fmtDt = formatDateTime;
  label = (s: string) => OP_TYPE_LABELS[s] ?? s;
}
