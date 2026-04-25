import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { formatDate, formatFCFA, statusBadge, STATUS_LABELS } from '../../core/format';

interface ClientDetail {
  id: string; code: string; fullName: string; clientType: string;
  phone: string | null; email: string | null; address: string | null;
  idNumber: string | null; birthDate: string | null; profession: string | null;
  branchId: string | null; branchName: string | null; createdAt: string;
  accounts: Array<{ id: string; accountNumber: string; accountType: string; balance: number; status: string }>;
  credits: Array<{ id: string; reference: string; product: string; principal: number; outstanding: number; status: string }>;
}

@Component({
  selector: 'app-client-detail',
  standalone: true,
  imports: [RouterLink],
  template: `
    @if (data(); as c) {
      <div class="page-header">
        <div><h1>{{ c.fullName }}</h1><div class="text-muted">{{ c.code }} · <span class="badge">{{ c.clientType }}</span></div></div>
        <a class="btn btn-secondary" routerLink="/clients">← Retour</a>
      </div>

      <div class="grid grid-cols-2 gap-4 mb-6">
        <div class="card">
          <div class="card-header"><h3 class="card-title">Identité</h3></div>
          <div class="grid grid-cols-2 gap-2">
            <div><div class="text-xs text-muted">Téléphone</div><div>{{ c.phone ?? '—' }}</div></div>
            <div><div class="text-xs text-muted">Email</div><div>{{ c.email ?? '—' }}</div></div>
            <div><div class="text-xs text-muted">N° pièce</div><div>{{ c.idNumber ?? '—' }}</div></div>
            <div><div class="text-xs text-muted">Naissance</div><div>{{ fmtDate(c.birthDate) }}</div></div>
            <div><div class="text-xs text-muted">Profession</div><div>{{ c.profession ?? '—' }}</div></div>
            <div><div class="text-xs text-muted">Agence</div><div>{{ c.branchName ?? '—' }}</div></div>
            <div style="grid-column: 1 / -1;"><div class="text-xs text-muted">Adresse</div><div>{{ c.address ?? '—' }}</div></div>
          </div>
        </div>

        <div class="card">
          <div class="card-header"><h3 class="card-title">Comptes ({{ c.accounts.length }})</h3></div>
          @if (c.accounts.length === 0) { <div class="text-muted text-sm">Aucun compte</div> }
          @for (a of c.accounts; track a.id) {
            <a [routerLink]="['/comptes', a.id]" style="display:block; padding:.65rem; border:1px solid var(--border); border-radius:8px; margin-bottom:.5rem; text-decoration:none; color:inherit;">
              <div class="flex justify-between"><strong>{{ a.accountNumber }}</strong><span class="badge" [class]="statusBadge(a.status)">{{ label(a.status) }}</span></div>
              <div class="flex justify-between text-sm"><span class="text-muted">{{ a.accountType }}</span><strong>{{ fmt(a.balance) }}</strong></div>
            </a>
          }
        </div>
      </div>

      <div class="card">
        <div class="card-header"><h3 class="card-title">Crédits ({{ c.credits.length }})</h3></div>
        @if (c.credits.length === 0) { <div class="text-muted text-sm">Aucun crédit</div> } @else {
          <div class="table-wrap" style="border:none;">
            <table class="table">
              <thead><tr><th>Référence</th><th>Produit</th><th class="num">Principal</th><th class="num">Encours</th><th>Statut</th></tr></thead>
              <tbody>
                @for (cr of c.credits; track cr.id) {
                  <tr style="cursor:pointer;" [routerLink]="['/credits', cr.id]">
                    <td><strong>{{ cr.reference }}</strong></td>
                    <td>{{ cr.product }}</td>
                    <td class="num">{{ fmt(cr.principal) }}</td>
                    <td class="num">{{ fmt(cr.outstanding) }}</td>
                    <td><span class="badge" [class]="statusBadge(cr.status)">{{ label(cr.status) }}</span></td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
        }
      </div>
    } @else if (loading()) {
      <div class="card"><div class="spinner" style="margin:2rem auto;"></div></div>
    }
  `,
})
export class ClientDetailComponent implements OnInit {
  private api = inject(ApiService);
  private route = inject(ActivatedRoute);
  data = signal<ClientDetail | null>(null);
  loading = signal(true);

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id')!;
    this.api.get<ClientDetail>(`/clients/${id}`).subscribe({
      next: (d) => { this.data.set(d); this.loading.set(false); },
      error: () => this.loading.set(false),
    });
  }
  fmt = formatFCFA; fmtDate = formatDate;
  statusBadge = statusBadge;
  label = (s: string) => STATUS_LABELS[s] ?? s;
}
