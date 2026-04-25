import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { formatDate, isAuditor } from '../../core/format';

interface Client {
  id: string; code: string; fullName: string; clientType: string;
  phone: string | null; email: string | null; branchId: string | null; branchName: string | null;
  createdAt: string;
}

@Component({
  selector: 'app-clients-list',
  standalone: true,
  imports: [FormsModule, RouterLink],
  template: `
    <div class="page-header">
      <div><h1>Clients</h1><div class="text-muted">{{ filtered().length }} client(s)</div></div>
      @if (!isAud()) { <a class="btn" routerLink="/clients/new">+ Nouveau client</a> }
    </div>

    <div class="card mb-4">
      <div class="form-grid">
        <div class="form-row" style="margin:0;">
          <label>Recherche</label>
          <input class="input" placeholder="Nom, code, téléphone…" [(ngModel)]="search" />
        </div>
        <div class="form-row" style="margin:0;">
          <label>Type</label>
          <select class="select" [(ngModel)]="typeFilter">
            <option value="">Tous</option>
            <option value="particulier">Particulier</option>
            <option value="entreprise">Entreprise</option>
            <option value="association">Association</option>
          </select>
        </div>
      </div>
    </div>

    <div class="table-wrap">
      <table class="table">
        <thead><tr>
          <th>Code</th><th>Nom</th><th>Type</th><th>Téléphone</th><th>Agence</th><th>Créé le</th>
        </tr></thead>
        <tbody>
          @for (c of filtered(); track c.id) {
            <tr style="cursor:pointer;" (click)="open(c.id)">
              <td><strong>{{ c.code }}</strong></td>
              <td>{{ c.fullName }}</td>
              <td><span class="badge">{{ c.clientType }}</span></td>
              <td>{{ c.phone ?? '—' }}</td>
              <td>{{ c.branchName ?? '—' }}</td>
              <td>{{ fmtDate(c.createdAt) }}</td>
            </tr>
          } @empty {
            <tr><td colspan="6"><div class="empty">Aucun client</div></td></tr>
          }
        </tbody>
      </table>
    </div>
  `,
})
export class ClientsListComponent implements OnInit {
  private api = inject(ApiService);
  private auth = inject(AuthService);
  private router = inject(Router);

  clients = signal<Client[]>([]);
  search = '';
  typeFilter = '';

  isAud = computed(() => isAuditor(this.auth.user()?.role));

  filtered = computed(() => {
    const s = this.search.toLowerCase().trim();
    return this.clients().filter((c) => {
      const matchSearch = !s || c.fullName.toLowerCase().includes(s) || c.code.toLowerCase().includes(s) || (c.phone ?? '').toLowerCase().includes(s);
      const matchType = !this.typeFilter || c.clientType === this.typeFilter;
      return matchSearch && matchType;
    });
  });

  ngOnInit() { this.api.get<Client[]>('/clients').subscribe((rows) => this.clients.set(rows)); }
  fmtDate = formatDate;
  open(id: string) { this.router.navigate(['/clients', id]); }
}
