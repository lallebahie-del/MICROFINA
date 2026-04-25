import { Component, inject, OnInit, signal } from '@angular/core';
import { ApiService } from '../../core/api.service';
import { formatFCFA, PRODUCT_LABELS } from '../../core/format';

interface AgingBucket { bucket: string; count: number; amount: number; }
interface ByProduct { category: string; amount: number; count: number; }

@Component({
  selector: 'app-reporting',
  standalone: true,
  template: `
    <div class="page-header">
      <div><h1>Reporting</h1><div class="text-muted">Indicateurs d'analyse du portefeuille</div></div>
    </div>

    <div class="grid grid-cols-2 gap-4 mb-6">
      <div class="card">
        <div class="card-header"><h3 class="card-title">Balance âgée du portefeuille</h3></div>
        <div class="table-wrap" style="border:none;">
          <table class="table">
            <thead><tr><th>Tranche</th><th class="num">Crédits</th><th class="num">Encours</th></tr></thead>
            <tbody>
              @for (b of aging(); track b.bucket) {
                <tr>
                  <td><strong>{{ b.bucket }}</strong></td>
                  <td class="num">{{ b.count }}</td>
                  <td class="num">{{ fmt(b.amount) }}</td>
                </tr>
              }
            </tbody>
            <tfoot>
              <tr style="font-weight:700; background:#f8fafc;">
                <td>TOTAL</td>
                <td class="num">{{ totalCount() }}</td>
                <td class="num">{{ fmt(totalAmount()) }}</td>
              </tr>
            </tfoot>
          </table>
        </div>
      </div>

      <div class="card">
        <div class="card-header"><h3 class="card-title">Crédits par produit</h3></div>
        <div style="display:flex; flex-direction:column; gap:.5rem;">
          @for (p of products(); track p.category) {
            <div>
              <div class="flex justify-between">
                <strong>{{ productLabel(p.category) }}</strong>
                <span>{{ p.count }} · {{ fmt(p.amount) }}</span>
              </div>
              <div style="background:#e2e8f0; border-radius:999px; height:8px; overflow:hidden; margin-top:.25rem;">
                <div [style.width.%]="ratio(p.amount)" style="height:100%; background:linear-gradient(90deg, var(--color-primary), var(--color-primary-light));"></div>
              </div>
            </div>
          } @empty { <div class="empty">Aucun crédit</div> }
        </div>
      </div>
    </div>

    <div class="card">
      <div class="card-header"><h3 class="card-title">Indicateurs clés (résumé)</h3></div>
      <div class="grid grid-cols-3 gap-4">
        <div><div class="text-muted text-sm">Total crédits ouverts</div><div class="text-xl font-bold">{{ totalAmount() }}</div></div>
        <div><div class="text-muted text-sm">Crédits à risque (+30j)</div><div class="text-xl font-bold" style="color:var(--color-danger);">{{ atRiskAmount() }}</div></div>
        <div><div class="text-muted text-sm">PAR (>30 jours)</div><div class="text-xl font-bold">{{ par() }}%</div></div>
      </div>
    </div>
  `,
})
export class ReportingComponent implements OnInit {
  private api = inject(ApiService);
  aging = signal<AgingBucket[]>([]);
  products = signal<ByProduct[]>([]);
  ngOnInit() {
    this.api.get<AgingBucket[]>('/reports/aging-balance').subscribe((r) => this.aging.set(r));
    this.api.get<ByProduct[]>('/reports/credits-by-product').subscribe((r) => this.products.set(r));
  }
  totalCount() { return this.aging().reduce((s, b) => s + b.count, 0); }
  totalAmount = () => formatFCFA(this.aging().reduce((s, b) => s + b.amount, 0));
  atRiskAmount = () => formatFCFA(this.aging().filter((b) => b.bucket !== 'À jour').reduce((s, b) => s + b.amount, 0));
  par(): number {
    const total = this.aging().reduce((s, b) => s + b.amount, 0);
    if (!total) return 0;
    const risk = this.aging().filter((b) => b.bucket !== 'À jour' && b.bucket !== '1-30 jours').reduce((s, b) => s + b.amount, 0);
    return Math.round((risk / total) * 10000) / 100;
  }
  ratio(amount: number): number {
    const max = Math.max(...this.products().map((p) => p.amount), 1);
    return Math.round((amount / max) * 100);
  }
  fmt = formatFCFA;
  productLabel = (s: string) => PRODUCT_LABELS[s] ?? s;
}
