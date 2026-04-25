import { Component, inject, OnInit, signal } from '@angular/core';
import { ApiService } from '../../core/api.service';
import { formatFCFA, formatNumber } from '../../core/format';

interface DashboardData {
  totalClients: number;
  newClientsMonth: number;
  totalDeposits: number;
  totalCreditsGranted: number;
  totalRepayments: number;
  creditsOverdueCount: number;
  creditsOverdueAmount: number;
  cashBalance: number;
  activeCredits: number;
  portfolioAtRisk: number;
}
interface TrendPoint { label: string; value: number; }

@Component({
  selector: 'app-dashboard',
  standalone: true,
  template: `
    <div class="dashboard-header" style="margin-bottom: 2rem;">
      <h1 style="font-size: 2.5rem; font-weight: 700; color: #1e293b; margin-bottom: 0.25rem;">Tableau de bord</h1>
      <div style="color: #64748b; font-size: 1.1rem;">Vue d'ensemble de l'institution</div>
    </div>

    @if (loading()) {
      <div class="stats-grid">
        @for (i of [1,2,3,4]; track i) {
          <div class="stat-card skeleton"></div>
        }
      </div>
    } @else if (data(); as d) {
      <div class="stats-grid">
        <!-- Total Clients -->
        <div class="stat-card" style="border-left: 4px solid #3b82f6;">
          <div class="stat-header">
            <span class="stat-label">Total Clients</span>
            <span class="stat-icon" style="color: #3b82f6;">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
            </span>
          </div>
          <div class="stat-value">{{ d.totalClients }}</div>
          <div class="stat-delta">+{{ d.newClientsMonth }} ce mois</div>
        </div>

        <!-- Total Dépôts -->
        <div class="stat-card" style="border-left: 4px solid #10b981;">
          <div class="stat-header">
            <span class="stat-label">Total Dépôts</span>
            <span class="stat-icon" style="color: #10b981;">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M19 5c-1.5 0-2.8 1.4-3 2-3.5-1.5-11-.3-11 5 0 1.8 0 3 2 4.5V20h4v-2h3v2h4v-4c1-.5 1.7-1 2-2h2v-4h-2c0-1-.5-1.5-1-2 .5-.5 1.2-1.1 1-3-.1-1.2-1.5-1.7-2-1.5zM9 14c0 .6-.4 1-1 1s-1-.4-1-1 .4-1 1-1 1 .4 1 1z"/></svg>
            </span>
          </div>
          <div class="stat-value">{{ num(d.totalDeposits) }}</div>
          <div class="stat-unit">FCFA</div>
          <div class="stat-delta">Base de financement</div>
        </div>

        <!-- Crédits Accordés -->
        <div class="stat-card" style="border-left: 4px solid #8b5cf6;">
          <div class="stat-header">
            <span class="stat-label">Crédits Accordés</span>
            <span class="stat-icon" style="color: #8b5cf6;">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="1" y="4" width="22" height="16" rx="2" ry="2"/><line x1="1" y1="10" x2="23" y2="10"/></svg>
            </span>
          </div>
          <div class="stat-value">{{ num(d.totalCreditsGranted) }}</div>
          <div class="stat-unit">FCFA</div>
          <div class="stat-delta">{{ d.activeCredits }} crédits actifs</div>
        </div>

        <!-- Portefeuille à Risque -->
        <div class="stat-card" style="border-left: 4px solid #ef4444;">
          <div class="stat-header">
            <span class="stat-label">Portefeuille à Risque</span>
            <span class="stat-icon" style="color: #ef4444;">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
            </span>
          </div>
          <div class="stat-value" style="color: #ef4444;">{{ d.portfolioAtRisk }}%</div>
          <div class="stat-delta">{{ d.creditsOverdueCount }} crédits en retard</div>
        </div>
      </div>

      <div class="charts-grid">
        <!-- Chart 1: Evolution des Dépôts -->
        <div class="chart-card">
          <h3 class="chart-title">Évolution des Dépôts</h3>
          <div class="chart-container" style="height: 250px;">
             <div class="chart-y-axis">
               <span>1.8M</span>
               <span>1.35M</span>
               <span>0.9M</span>
             </div>
             <div class="chart-content">
               <svg viewBox="0 0 400 200" preserveAspectRatio="none" style="width: 100%; height: 200px;">
                 <defs>
                   <linearGradient id="areaGradient" x1="0" y1="0" x2="0" y2="1">
                     <stop offset="0%" stop-color="#0f766e" stop-opacity="0.2"/>
                     <stop offset="100%" stop-color="#0f766e" stop-opacity="0"/>
                   </linearGradient>
                 </defs>
                 <path d="M0,150 Q50,80 100,120 T200,60 T300,100 T400,80 L400,200 L0,200 Z" fill="url(#areaGradient)"/>
                 <path d="M0,150 Q50,80 100,120 T200,60 T300,100 T400,80" fill="none" stroke="#0f766e" stroke-width="2"/>
               </svg>
             </div>
          </div>
        </div>

        <!-- Chart 2: Remboursements Récents -->
        <div class="chart-card">
          <h3 class="chart-title">Remboursements Récents</h3>
          <p class="chart-subtitle">Évolution des recouvrements mensuels</p>
          <div class="chart-container" style="height: 250px; display: flex; align-items: flex-end; justify-content: space-around; padding: 0 1rem;">
             @for (p of repayments().slice(0, 6); track p.label) {
               <div style="display: flex; flex-direction: column; align-items: center; gap: 0.5rem; flex: 1;">
                 <div [style.height.px]="p.value / 100000" style="width: 24px; background: #0f766e; border-radius: 4px 4px 0 0;"></div>
                 <span style="font-size: 0.7rem; color: #64748b;">{{ p.label }}</span>
               </div>
             }
          </div>
        </div>
      </div>
    }
  `,
  styles: [`
    .stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 1.5rem; margin-bottom: 2rem; }
    .stat-card { background: white; padding: 1.5rem; border-radius: 12px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); position: relative; }
    .stat-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 1rem; }
    .stat-label { font-weight: 600; color: #64748b; font-size: 0.9rem; }
    .stat-icon { width: 32px; height: 32px; display: flex; align-items: center; justify-content: center; font-size: 1rem; }
    .stat-value { font-size: 2rem; font-weight: 700; color: #1e293b; line-height: 1; }
    .stat-unit { font-size: 1.1rem; font-weight: 700; color: #1e293b; margin-top: 0.25rem; }
    .stat-delta { font-size: 0.85rem; color: #94a3b8; margin-top: 0.5rem; }
    
    .charts-grid { display: grid; grid-template-columns: 1.5fr 1fr; gap: 1.5rem; }
    .chart-card { background: white; padding: 1.5rem; border-radius: 12px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
    .chart-title { font-size: 1.1rem; font-weight: 600; color: #1e293b; margin-bottom: 0.5rem; }
    .chart-subtitle { font-size: 0.9rem; color: #94a3b8; margin-bottom: 1.5rem; }
    
    .chart-container { position: relative; display: flex; }
    .chart-y-axis { display: flex; flex-direction: column; justify-content: space-between; padding-right: 1rem; color: #94a3b8; font-size: 0.75rem; height: 200px; border-right: 1px dashed #e2e8f0; }
    .chart-content { flex: 1; height: 200px; padding-left: 1rem; position: relative; }
    
    .skeleton { height: 160px; background: linear-gradient(90deg, #f1f5f9 25%, #e2e8f0 50%, #f1f5f9 75%); background-size: 200% 100%; animation: loading 1.5s infinite; }
    @keyframes loading { from { background-position: 200% 0; } to { background-position: -200% 0; } }
  `]
})
export class DashboardComponent implements OnInit {
  private api = inject(ApiService);
  loading = signal(true);
  data = signal<DashboardData | null>(null);
  deposits = signal<TrendPoint[]>([]);
  credits = signal<TrendPoint[]>([]);
  repayments = signal<TrendPoint[]>([]);

  today = new Date().toLocaleDateString('fr-FR', { day: 'numeric', month: 'long', year: 'numeric' });

  ngOnInit() {
    this.api.get<DashboardData>('/reports/dashboard').subscribe({
      next: (d) => { this.data.set(d); this.loading.set(false); },
      error: () => this.loading.set(false),
    });
    this.api.get<TrendPoint[]>('/reports/deposits-trend').subscribe((p) => this.deposits.set(p));
    this.api.get<TrendPoint[]>('/reports/credits-trend').subscribe((p) => this.credits.set(p));
    this.api.get<TrendPoint[]>('/reports/repayments-monthly').subscribe((p) => this.repayments.set(p));
  }

  fmt = formatFCFA;
  num = (n: number) => formatNumber(n);
  barHeight(v: number, all: TrendPoint[]): number {
    const max = Math.max(...all.map((p) => p.value), 1);
    return Math.max(5, Math.round((v / max) * 100));
  }
}
