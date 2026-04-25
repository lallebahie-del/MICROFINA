import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { formatFCFA, formatDate, isAuditor, statusBadge, STATUS_LABELS } from '../../core/format';

interface Account {
  id: string; accountNumber: string; clientId: string; clientName: string | null;
  accountType: string; balance: number; currency: string; status: string; createdAt: string;
}

@Component({
  selector: 'app-accounts-list',
  standalone: true,
  imports: [FormsModule, RouterLink],
  template: `
    <div class="page-header">
      <div><h1>Comptes</h1><div class="text-muted">{{ filtered().length }} compte(s) · Solde total : <strong>{{ fmt(total()) }}</strong></div></div>
      @if (!isAud()) { <a class="btn" routerLink="/comptes/new">+ Nouveau compte</a> }
    </div>

    <div class="card mb-4">
      <div class="form-grid">
        <div class="form-row" style="margin:0;"><label>Recherche</label><input class="input" placeholder="Numéro, client…" [(ngModel)]="search" /></div>
        <div class="form-row" style="margin:0;"><label>Type</label>
          <select class="select" [(ngModel)]="typeFilter">
            <option value="">Tous</option>
            <option value="epargne">Épargne</option>
            <option value="courant">Courant</option>
            <option value="depot_terme">Dépôt à terme</option>
            <option value="plan_epargne">Plan d'épargne</option>
          </select>
        </div>
      </div>
    </div>

    <div class="table-wrap">
      <table class="table">
        <thead><tr><th>N° compte</th><th>Client</th><th>Type</th><th class="num">Solde</th><th>Statut</th><th>Créé le</th></tr></thead>
        <tbody>
          @for (a of filtered(); track a.id) {
            <tr style="cursor:pointer;" (click)="open(a.id)">
              <td><strong>{{ a.accountNumber }}</strong></td>
              <td>{{ a.clientName ?? '—' }}</td>
              <td><span class="badge">{{ a.accountType }}</span></td>
              <td class="num"><strong>{{ fmt(a.balance) }}</strong></td>
              <td><span class="badge" [class]="badge(a.status)">{{ label(a.status) }}</span></td>
              <td>{{ fmtDate(a.createdAt) }}</td>
            </tr>
          } @empty { <tr><td colspan="6"><div class="empty">Aucun compte</div></td></tr> }
        </tbody>
      </table>
    </div>
  `,
})
export class AccountsListComponent implements OnInit {
  private api = inject(ApiService);
  private auth = inject(AuthService);
  private router = inject(Router);
  accounts = signal<Account[]>([]);
  search = ''; typeFilter = '';
  isAud = computed(() => isAuditor(this.auth.user()?.role));
  filtered = computed(() => {
    const s = this.search.toLowerCase().trim();
    return this.accounts().filter((a) => {
      const ms = !s || a.accountNumber.toLowerCase().includes(s) || (a.clientName ?? '').toLowerCase().includes(s);
      const mt = !this.typeFilter || a.accountType === this.typeFilter;
      return ms && mt;
    });
  });
  total = computed(() => this.filtered().reduce((sum, a) => sum + a.balance, 0));
  ngOnInit() { this.api.get<Account[]>('/accounts').subscribe((r) => this.accounts.set(r)); }
  fmt = formatFCFA; fmtDate = formatDate;
  badge = statusBadge;
  label = (s: string) => STATUS_LABELS[s] ?? s;
  open(id: string) { this.router.navigate(['/comptes', id]); }
}
