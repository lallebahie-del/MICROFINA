import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { formatFCFA, formatDateTime, statusBadge, STATUS_LABELS, OP_TYPE_LABELS } from '../../core/format';

interface AccountDetail {
  id: string; accountNumber: string; clientId: string; clientName: string | null;
  accountType: string; balance: number; currency: string; status: string; createdAt: string;
  operations: Array<{ id: string; reference: string; operationType: string; amount: number; notes: string | null; userName: string | null; createdAt: string }>;
}

@Component({
  selector: 'app-account-detail',
  standalone: true,
  imports: [RouterLink],
  template: `
    @if (data(); as a) {
      <div class="page-header">
        <div><h1>{{ a.accountNumber }}</h1>
          <div class="text-muted">
            <a [routerLink]="['/clients', a.clientId]">{{ a.clientName ?? '—' }}</a> ·
            <span class="badge">{{ a.accountType }}</span> ·
            <span class="badge" [class]="badge(a.status)">{{ label(a.status) }}</span>
          </div>
        </div>
        <a class="btn btn-secondary" routerLink="/comptes">← Retour</a>
      </div>

      <div class="grid grid-cols-3 gap-4 mb-6">
        <div class="stat"><div class="label">Solde</div><div class="value">{{ fmt(a.balance) }}</div></div>
        <div class="stat"><div class="label">Devise</div><div class="value">{{ a.currency }}</div></div>
        <div class="stat"><div class="label">Opérations</div><div class="value">{{ a.operations.length }}</div></div>
      </div>

      <div class="card">
        <div class="card-header"><h3 class="card-title">Historique des opérations</h3></div>
        @if (a.operations.length === 0) { <div class="text-muted text-sm">Aucune opération</div> } @else {
          <div class="table-wrap" style="border:none;">
            <table class="table">
              <thead><tr><th>Référence</th><th>Type</th><th class="num">Montant</th><th>Note</th><th>Agent</th><th>Date</th></tr></thead>
              <tbody>
                @for (o of a.operations; track o.id) {
                  <tr>
                    <td>{{ o.reference }}</td>
                    <td><span class="badge">{{ opLabel(o.operationType) }}</span></td>
                    <td class="num" [style.color]="isCredit(o.operationType) ? 'var(--color-success)' : 'var(--color-danger)'">
                      {{ isCredit(o.operationType) ? '+' : '−' }} {{ fmt(o.amount) }}
                    </td>
                    <td>{{ o.notes ?? '—' }}</td>
                    <td>{{ o.userName ?? '—' }}</td>
                    <td>{{ fmtDt(o.createdAt) }}</td>
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
export class AccountDetailComponent implements OnInit {
  private api = inject(ApiService);
  private route = inject(ActivatedRoute);
  data = signal<AccountDetail | null>(null);
  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id')!;
    this.api.get<AccountDetail>(`/accounts/${id}`).subscribe((d) => this.data.set(d));
  }
  fmt = formatFCFA; fmtDt = formatDateTime;
  badge = statusBadge;
  label = (s: string) => STATUS_LABELS[s] ?? s;
  opLabel = (s: string) => OP_TYPE_LABELS[s] ?? s;
  isCredit = (t: string) => t === 'depot' || t === 'adhesion' || t === 'remboursement' || t === 'versement';
}
