import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { formatDateTime } from '../../core/format';

interface AuditLog {
  id: string; action: string; entity: string; entityId: string | null;
  userId: string | null; userName: string | null;
  details: string | null; ipAddress: string | null; createdAt: string;
}

@Component({
  selector: 'app-audit',
  standalone: true,
  imports: [FormsModule],
  template: `
    <div class="page-header">
      <div><h1>Audit</h1><div class="text-muted">Journal des actions ({{ filtered().length }} entrées)</div></div>
    </div>

    <div class="card mb-4">
      <div class="form-grid">
        <div class="form-row" style="margin:0;"><label>Action</label>
          <select class="select" [(ngModel)]="actionFilter">
            <option value="">Toutes</option>
            <option value="LOGIN">LOGIN</option>
            <option value="LOGOUT">LOGOUT</option>
            <option value="CREATE">CREATE</option>
            <option value="UPDATE">UPDATE</option>
            <option value="DELETE">DELETE</option>
          </select>
        </div>
        <div class="form-row" style="margin:0;"><label>Entité</label>
          <select class="select" [(ngModel)]="entityFilter">
            <option value="">Toutes</option>
            <option value="user">User</option>
            <option value="client">Client</option>
            <option value="account">Account</option>
            <option value="credit">Credit</option>
            <option value="cash_operation">Cash op</option>
            <option value="bank_operation">Bank op</option>
            <option value="journal_entry">Journal</option>
            <option value="budget_line">Budget</option>
          </select>
        </div>
      </div>
    </div>

    <div class="table-wrap">
      <table class="table">
        <thead><tr><th>Date</th><th>Utilisateur</th><th>Action</th><th>Entité</th><th>Détails</th><th>IP</th></tr></thead>
        <tbody>
          @for (l of filtered(); track l.id) {
            <tr>
              <td>{{ fmtDt(l.createdAt) }}</td>
              <td>{{ l.userName ?? '—' }}</td>
              <td><span class="badge" [class]="badgeFor(l.action)">{{ l.action }}</span></td>
              <td>{{ l.entity }}</td>
              <td>{{ l.details ?? '—' }}</td>
              <td><code style="font-size:.75rem; color:var(--text-muted);">{{ l.ipAddress ?? '—' }}</code></td>
            </tr>
          } @empty { <tr><td colspan="6"><div class="empty">Aucun événement</div></td></tr> }
        </tbody>
      </table>
    </div>
  `,
})
export class AuditComponent implements OnInit {
  private api = inject(ApiService);
  logs = signal<AuditLog[]>([]);
  actionFilter = ''; entityFilter = '';
  ngOnInit() { this.api.get<AuditLog[]>('/audit/logs', { limit: 500 }).subscribe((r) => this.logs.set(r)); }
  filtered = computed(() => this.logs().filter((l) => (!this.actionFilter || l.action === this.actionFilter) && (!this.entityFilter || l.entity === this.entityFilter)));
  badgeFor(a: string): string {
    if (a === 'CREATE') return 'badge-success';
    if (a === 'DELETE') return 'badge-danger';
    if (a === 'UPDATE') return 'badge-warning';
    if (a === 'LOGIN' || a === 'LOGOUT') return 'badge-info';
    return 'badge-muted';
  }
  fmtDt = formatDateTime;
}
