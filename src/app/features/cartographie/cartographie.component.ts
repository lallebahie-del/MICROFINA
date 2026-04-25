import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { ApiService } from '../../core/api.service';
import { formatFCFA } from '../../core/format';

interface BranchPoint { id: string; name: string; city: string; latitude: number; longitude: number; clients: number; credits: number; }

@Component({
  selector: 'app-cartographie',
  standalone: true,
  template: `
    <div class="page-header">
      <div><h1>Cartographie</h1><div class="text-muted">Implantations des agences en Afrique de l'Ouest</div></div>
    </div>

    <div class="grid grid-cols-3 gap-4 mb-6">
      <div class="stat"><div class="label">Agences</div><div class="value">{{ branches().length }}</div></div>
      <div class="stat"><div class="label">Clients totaux</div><div class="value">{{ totalClients() }}</div></div>
      <div class="stat"><div class="label">Encours total</div><div class="value">{{ fmt(totalCredits()) }}</div></div>
    </div>

    <div class="grid gap-4" style="grid-template-columns: 2fr 1fr;">
      <div class="card">
        <div class="card-header"><h3 class="card-title">Carte des agences</h3></div>
        <svg viewBox="-20 4 12 14" style="width:100%; height:480px; background:#e0f2fe; border-radius:8px;">
          <!-- Approximation Afrique de l'Ouest -->
          <rect x="-20" y="4" width="12" height="14" fill="#dbeafe" />
          <path d="M-18 6 L-9 6 L-9 17 L-18 17 Z" fill="#bbf7d0" opacity=".6" />
          @for (b of branches(); track b.id) {
            <g [attr.transform]="'translate(' + (-b.longitude) + ',' + (-b.latitude + 20) + ')'">
              <circle r="0.4" [attr.fill]="b.credits > 50000000 ? '#dc2626' : '#0f766e'" stroke="white" stroke-width="0.08" />
              <text y="-0.6" font-size="0.5" text-anchor="middle" fill="#0f172a" font-weight="700">{{ b.name }}</text>
            </g>
          }
        </svg>
      </div>
      <div class="card">
        <div class="card-header"><h3 class="card-title">Liste des agences</h3></div>
        <div style="display:flex; flex-direction:column; gap:.5rem; max-height:480px; overflow-y:auto;">
          @for (b of branches(); track b.id) {
            <div style="border:1px solid var(--border); border-radius:8px; padding:.65rem;">
              <div style="font-weight:600;">{{ b.name }}</div>
              <div class="text-xs text-muted">{{ b.city }} · {{ b.latitude }}, {{ b.longitude }}</div>
              <div class="flex justify-between mt-2">
                <small>👥 {{ b.clients }} clients</small>
                <small><strong>{{ fmt(b.credits) }}</strong></small>
              </div>
            </div>
          } @empty { <div class="empty">Aucune agence</div> }
        </div>
      </div>
    </div>
  `,
})
export class CartographieComponent implements OnInit {
  private api = inject(ApiService);
  branches = signal<BranchPoint[]>([]);
  ngOnInit() { this.api.get<BranchPoint[]>('/reports/branches-map').subscribe((r) => this.branches.set(r)); }
  totalClients = computed(() => this.branches().reduce((s, b) => s + b.clients, 0));
  totalCredits = computed(() => this.branches().reduce((s, b) => s + b.credits, 0));
  fmt = formatFCFA;
}
