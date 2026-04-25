import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { formatFCFA, formatDate, statusBadge, STATUS_LABELS, PRODUCT_LABELS, isAuditor } from '../../core/format';

interface Credit {
  id: string; reference: string; clientId: string; clientName: string | null;
  product: string; principal: number; outstanding: number; durationMonths: number;
  interestRate: number; status: string; daysOverdue: number; createdAt: string;
}

@Component({
  selector: 'app-credits-list',
  standalone: true,
  imports: [FormsModule, RouterLink],
  template: `
    <div class="page-header">
      <div><h1>Crédits</h1><div class="text-muted">{{ filtered().length }} crédit(s) · Encours : <strong>{{ fmt(totalOut()) }}</strong></div></div>
      <div class="flex gap-2">
        <a class="btn btn-secondary" routerLink="/credits/simulator">Simulateur</a>
        @if (!isAud()) { <a class="btn" routerLink="/credits/new">+ Nouveau crédit</a> }
      </div>
    </div>

    <div class="card mb-4">
      <div class="form-grid">
        <div class="form-row" style="margin:0;"><label>Recherche</label><input class="input" placeholder="Référence, client…" [(ngModel)]="search" /></div>
        <div class="form-row" style="margin:0;"><label>Statut</label>
          <select class="select" [(ngModel)]="statusFilter">
            <option value="">Tous</option>
            <option value="demande">Demande</option>
            <option value="approuve">Approuvé</option>
            <option value="debloque">Débloqué</option>
            <option value="en_cours">En cours</option>
            <option value="en_retard">En retard</option>
            <option value="solde">Soldé</option>
            <option value="rejete">Rejeté</option>
          </select>
        </div>
      </div>
    </div>

    <div class="table-wrap">
      <table class="table">
        <thead><tr><th>Référence</th><th>Client</th><th>Produit</th><th class="num">Principal</th><th class="num">Encours</th><th class="num">Durée</th><th>Statut</th><th>Date</th></tr></thead>
        <tbody>
          @for (c of filtered(); track c.id) {
            <tr style="cursor:pointer;" (click)="open(c.id)">
              <td><strong>{{ c.reference }}</strong></td>
              <td>{{ c.clientName ?? '—' }}</td>
              <td>{{ productLabel(c.product) }}</td>
              <td class="num">{{ fmt(c.principal) }}</td>
              <td class="num">{{ fmt(c.outstanding) }}</td>
              <td class="num">{{ c.durationMonths }} mois</td>
              <td>
                <span class="badge" [class]="badge(c.status)">{{ label(c.status) }}</span>
                @if (c.daysOverdue > 0) { <small style="color:var(--color-danger);"> · {{ c.daysOverdue }}j</small> }
              </td>
              <td>{{ fmtDate(c.createdAt) }}</td>
            </tr>
          } @empty { <tr><td colspan="8"><div class="empty">Aucun crédit</div></td></tr> }
        </tbody>
      </table>
    </div>
  `,
})
export class CreditsListComponent implements OnInit {
  private api = inject(ApiService);
  private auth = inject(AuthService);
  private router = inject(Router);
  credits = signal<Credit[]>([]);
  search = ''; statusFilter = '';
  isAud = computed(() => isAuditor(this.auth.user()?.role));

  filtered = computed(() => {
    const s = this.search.toLowerCase().trim();
    return this.credits().filter((c) => {
      const ms = !s || c.reference.toLowerCase().includes(s) || (c.clientName ?? '').toLowerCase().includes(s);
      const mt = !this.statusFilter || c.status === this.statusFilter;
      return ms && mt;
    });
  });
  totalOut = computed(() => this.filtered().reduce((s, c) => s + c.outstanding, 0));
  ngOnInit() { this.api.get<Credit[]>('/credits').subscribe((r) => this.credits.set(r)); }
  fmt = formatFCFA; fmtDate = formatDate;
  badge = statusBadge;
  label = (s: string) => STATUS_LABELS[s] ?? s;
  productLabel = (s: string) => PRODUCT_LABELS[s] ?? s;
  open(id: string) { this.router.navigate(['/credits', id]); }
}
