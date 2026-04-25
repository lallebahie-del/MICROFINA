import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { ToastService } from '../../core/toast.service';
import { formatFCFA, formatDate, isAuditor } from '../../core/format';

interface Entry { id: string; reference: string; journal: string; accountCode: string; accountLabel: string; debit: number; credit: number; narration: string | null; entryDate: string; }
interface BalanceRow { accountCode: string; accountLabel: string; totalDebit: number; totalCredit: number; balance: number; }

@Component({
  selector: 'app-comptabilite',
  standalone: true,
  imports: [FormsModule],
  template: `
    <div class="page-header">
      <div><h1>Comptabilité</h1><div class="text-muted">Journal et balance</div></div>
      <div class="flex gap-2">
        <button class="btn-ghost btn btn-sm" [class.btn]="tab() === 'journal'" (click)="tab.set('journal')">Journal</button>
        <button class="btn-ghost btn btn-sm" [class.btn]="tab() === 'balance'" (click)="tab.set('balance')">Balance</button>
      </div>
    </div>

    @if (tab() === 'journal') {
      @if (!isAud()) {
      <div class="card mb-6">
        <div class="card-header"><h3 class="card-title">Nouvelle écriture</h3></div>
        <form (ngSubmit)="submit()">
          <div class="form-grid">
            <div class="form-row"><label>Journal *</label>
              <select class="select" [(ngModel)]="form.journal" name="journal" required>
                <option value="general">Journal général</option>
                <option value="caisse">Caisse</option>
                <option value="banque">Banque</option>
                <option value="achat">Achats</option>
                <option value="vente">Ventes</option>
              </select>
            </div>
            <div class="form-row"><label>Date *</label><input class="input" type="date" [(ngModel)]="form.entryDate" name="entryDate" required /></div>
            <div class="form-row"><label>N° compte *</label><input class="input" [(ngModel)]="form.accountCode" name="accountCode" required /></div>
            <div class="form-row"><label>Libellé compte *</label><input class="input" [(ngModel)]="form.accountLabel" name="accountLabel" required /></div>
            <div class="form-row"><label>Débit (FCFA)</label><input class="input" type="number" min="0" [(ngModel)]="form.debit" name="debit" /></div>
            <div class="form-row"><label>Crédit (FCFA)</label><input class="input" type="number" min="0" [(ngModel)]="form.credit" name="credit" /></div>
            <div class="form-row" style="grid-column:1/-1;"><label>Narration</label><input class="input" [(ngModel)]="form.narration" name="narration" /></div>
          </div>
          <div class="flex justify-end mt-2">
            <button class="btn" type="submit" [disabled]="saving()">{{ saving() ? 'Enregistrement…' : 'Enregistrer écriture' }}</button>
          </div>
        </form>
      </div>
      }

      <div class="card">
        <div class="card-header"><h3 class="card-title">Journal des écritures</h3></div>
        <div class="table-wrap" style="border:none;">
          <table class="table">
            <thead><tr><th>Référence</th><th>Date</th><th>Journal</th><th>Compte</th><th>Libellé</th><th class="num">Débit</th><th class="num">Crédit</th><th>Narration</th></tr></thead>
            <tbody>
              @for (e of entries(); track e.id) {
                <tr>
                  <td>{{ e.reference }}</td>
                  <td>{{ fmtDate(e.entryDate) }}</td>
                  <td><span class="badge">{{ e.journal }}</span></td>
                  <td>{{ e.accountCode }}</td>
                  <td>{{ e.accountLabel }}</td>
                  <td class="num">{{ e.debit > 0 ? fmt(e.debit) : '—' }}</td>
                  <td class="num">{{ e.credit > 0 ? fmt(e.credit) : '—' }}</td>
                  <td>{{ e.narration ?? '—' }}</td>
                </tr>
              } @empty { <tr><td colspan="8"><div class="empty">Aucune écriture</div></td></tr> }
            </tbody>
            <tfoot>
              <tr style="font-weight:700; background:#f8fafc;">
                <td colspan="5">TOTAUX</td>
                <td class="num">{{ fmt(totalDebit()) }}</td>
                <td class="num">{{ fmt(totalCredit()) }}</td>
                <td></td>
              </tr>
            </tfoot>
          </table>
        </div>
      </div>
    } @else {
      <div class="card">
        <div class="card-header"><h3 class="card-title">Balance des comptes</h3></div>
        <div class="table-wrap" style="border:none;">
          <table class="table">
            <thead><tr><th>Code</th><th>Libellé</th><th class="num">Débit</th><th class="num">Crédit</th><th class="num">Solde</th></tr></thead>
            <tbody>
              @for (b of balance(); track b.accountCode) {
                <tr>
                  <td><strong>{{ b.accountCode }}</strong></td>
                  <td>{{ b.accountLabel }}</td>
                  <td class="num">{{ fmt(b.totalDebit) }}</td>
                  <td class="num">{{ fmt(b.totalCredit) }}</td>
                  <td class="num" [style.color]="b.balance >= 0 ? 'var(--color-success)' : 'var(--color-danger)'">{{ fmt(b.balance) }}</td>
                </tr>
              } @empty { <tr><td colspan="5"><div class="empty">Aucun mouvement</div></td></tr> }
            </tbody>
          </table>
        </div>
      </div>
    }
  `,
})
export class ComptabiliteComponent implements OnInit {
  private api = inject(ApiService);
  private auth = inject(AuthService);
  private toast = inject(ToastService);
  tab = signal<'journal' | 'balance'>('journal');
  entries = signal<Entry[]>([]);
  balance = signal<BalanceRow[]>([]);
  saving = signal(false);
  isAud = computed(() => isAuditor(this.auth.user()?.role));
  form = { journal: 'general', entryDate: new Date().toISOString().slice(0, 10), accountCode: '', accountLabel: '', debit: 0, credit: 0, narration: '' };

  ngOnInit() { this.refresh(); }
  refresh() {
    this.api.get<Entry[]>('/accounting/entries').subscribe((r) => this.entries.set(r));
    this.api.get<BalanceRow[]>('/accounting/balance').subscribe((r) => this.balance.set(r));
  }
  totalDebit = computed(() => this.entries().reduce((s, e) => s + e.debit, 0));
  totalCredit = computed(() => this.entries().reduce((s, e) => s + e.credit, 0));

  submit() {
    if (!this.form.accountCode || !this.form.accountLabel) { this.toast.error('Compte requis'); return; }
    this.saving.set(true);
    const payload = { ...this.form, debit: Number(this.form.debit) || 0, credit: Number(this.form.credit) || 0, narration: this.form.narration || undefined };
    this.api.post('/accounting/entries', payload).subscribe({
      next: () => { this.saving.set(false); this.toast.success('Écriture enregistrée'); this.form = { journal: 'general', entryDate: new Date().toISOString().slice(0, 10), accountCode: '', accountLabel: '', debit: 0, credit: 0, narration: '' }; this.refresh(); },
      error: (e) => { this.saving.set(false); this.toast.error(e?.error?.error ?? 'Erreur'); },
    });
  }
  fmt = formatFCFA; fmtDate = formatDate;
}
