import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { ToastService } from '../../core/toast.service';
import { formatFCFA, formatDateTime, OP_TYPE_LABELS, isAuditor } from '../../core/format';

interface Op {
  id: string; reference: string; operationType: string; amount: number;
  accountId: string | null; accountNumber: string | null; clientName: string | null;
  notes: string | null; userName: string | null; createdAt: string;
}
interface Account { id: string; accountNumber: string; clientName: string | null; balance: number; }

@Component({
  selector: 'app-caisse',
  standalone: true,
  imports: [FormsModule],
  template: `
    <div class="page-header">
      <div><h1>Caisse</h1><div class="text-muted">Opérations d'espèces du jour</div></div>
    </div>

    <div class="grid grid-cols-3 gap-4 mb-6">
      <div class="stat"><div class="label">Entrées du jour</div><div class="value" style="color:var(--color-success);">{{ fmt(entriesToday()) }}</div></div>
      <div class="stat"><div class="label">Sorties du jour</div><div class="value" style="color:var(--color-danger);">{{ fmt(exitsToday()) }}</div></div>
      <div class="stat"><div class="label">Solde net du jour</div><div class="value">{{ fmt(entriesToday() - exitsToday()) }}</div></div>
    </div>

    @if (!isAud()) {
    <div class="card mb-6">
      <div class="card-header"><h3 class="card-title">Nouvelle opération</h3></div>
      <form (ngSubmit)="submit()">
        <div class="form-grid">
          <div class="form-row"><label>Compte *</label>
            <select class="select" [(ngModel)]="form.accountId" name="accountId" required>
              <option value="">Sélectionner</option>
              @for (a of accounts(); track a.id) { <option [value]="a.id">{{ a.accountNumber }} — {{ a.clientName }} ({{ fmt(a.balance) }})</option> }
            </select>
          </div>
          <div class="form-row"><label>Type *</label>
            <select class="select" [(ngModel)]="form.operationType" name="operationType" required>
              <option value="depot">Dépôt</option>
              <option value="retrait">Retrait</option>
              <option value="remboursement">Remboursement crédit</option>
              <option value="frais">Frais</option>
            </select>
          </div>
          <div class="form-row"><label>Montant (FCFA) *</label><input class="input" type="number" min="1" [(ngModel)]="form.amount" name="amount" required /></div>
          <div class="form-row"><label>Note</label><input class="input" [(ngModel)]="form.notes" name="notes" /></div>
        </div>
        <div class="flex justify-end mt-2">
          <button class="btn" type="submit" [disabled]="saving()">{{ saving() ? 'Enregistrement…' : 'Enregistrer' }}</button>
        </div>
      </form>
    </div>
    }

    <div class="card">
      <div class="card-header"><h3 class="card-title">Historique récent</h3><span class="text-muted text-sm">{{ ops().length }} opérations</span></div>
      <div class="table-wrap" style="border:none;">
        <table class="table">
          <thead><tr><th>Référence</th><th>Type</th><th>Compte</th><th>Client</th><th class="num">Montant</th><th>Agent</th><th>Date</th></tr></thead>
          <tbody>
            @for (o of ops(); track o.id) {
              <tr>
                <td>{{ o.reference }}</td>
                <td><span class="badge">{{ label(o.operationType) }}</span></td>
                <td>{{ o.accountNumber ?? '—' }}</td>
                <td>{{ o.clientName ?? '—' }}</td>
                <td class="num">{{ fmt(o.amount) }}</td>
                <td>{{ o.userName ?? '—' }}</td>
                <td>{{ fmtDt(o.createdAt) }}</td>
              </tr>
            } @empty { <tr><td colspan="7"><div class="empty">Aucune opération</div></td></tr> }
          </tbody>
        </table>
      </div>
    </div>
  `,
})
export class CaisseComponent implements OnInit {
  private api = inject(ApiService);
  private auth = inject(AuthService);
  private toast = inject(ToastService);
  ops = signal<Op[]>([]);
  accounts = signal<Account[]>([]);
  saving = signal(false);
  isAud = computed(() => isAuditor(this.auth.user()?.role));
  form = { accountId: '', operationType: 'depot', amount: 0, notes: '' };

  ngOnInit() { this.refresh(); this.api.get<Account[]>('/accounts').subscribe((a) => this.accounts.set(a)); }
  refresh() { this.api.get<Op[]>('/cash/operations').subscribe((r) => this.ops.set(r)); }

  entriesToday = computed(() => this.todayOps().filter((o) => ['depot','adhesion','remboursement'].includes(o.operationType)).reduce((s, o) => s + o.amount, 0));
  exitsToday = computed(() => this.todayOps().filter((o) => !['depot','adhesion','remboursement'].includes(o.operationType)).reduce((s, o) => s + o.amount, 0));
  private todayOps() {
    const today = new Date().toDateString();
    return this.ops().filter((o) => new Date(o.createdAt).toDateString() === today);
  }

  submit() {
    if (!this.form.accountId || !this.form.amount) { this.toast.error('Compte et montant requis'); return; }
    this.saving.set(true);
    const payload = { ...this.form, amount: Number(this.form.amount) };
    this.api.post('/cash/operations', payload).subscribe({
      next: () => {
        this.saving.set(false);
        this.toast.success('Opération enregistrée');
        this.form = { accountId: '', operationType: 'depot', amount: 0, notes: '' };
        this.refresh();
        this.api.get<Account[]>('/accounts').subscribe((a) => this.accounts.set(a));
      },
      error: (e) => { this.saving.set(false); this.toast.error(e?.error?.error ?? 'Erreur'); },
    });
  }

  fmt = formatFCFA; fmtDt = formatDateTime;
  label = (s: string) => OP_TYPE_LABELS[s] ?? s;
}
