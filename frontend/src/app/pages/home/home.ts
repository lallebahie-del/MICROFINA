import { Component, OnInit, signal, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ReportingService, Indicateur, RatiosBcm, EtatCredit } from '../../services/reporting.service';
import { AuthService } from '../../core/auth.service';

interface DonutSlice {
  label: string;
  value: number;
  color: string;
}

interface BarItem {
  label: string;
  value: number;
}

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './home.html',
  styleUrl: './home.css'
})
export class HomeComponent implements OnInit {

  private auth = inject(AuthService);
  user = this.auth.currentUser;
  today = new Date();

  indicateurs = signal<Indicateur[]>([]);
  ratios      = signal<RatiosBcm[]>([]);
  credits     = signal<EtatCredit[]>([]);

  loading = signal(true);
  error   = signal<string | null>(null);

  // ── KPIs agrégés ────────────────────────────────────────────────────────
  nbMembres        = computed<number>(() => sum(this.indicateurs(), i => i.nbMembres));
  nbMembresActifs  = computed<number>(() => sum(this.indicateurs(), i => i.nbMembresActifs));
  nbCreditsActifs  = computed<number>(() => sum(this.indicateurs(), i => i.nbCreditsActifs));
  encoursBrutTotal = computed<number>(() => sum(this.indicateurs(), i => i.encoursBrut));
  montantDecaisseJour = computed<number>(() => sum(this.indicateurs(), i => i.montantDecaisse));
  montantRemboJour    = computed<number>(() => sum(this.indicateurs(), i => i.montantRembourse));
  encaissJour         = computed<number>(() => sum(this.indicateurs(), i => i.encaissementsJour));

  par30Moyen = computed<number>(() => {
    const r = this.ratios();
    const enc = sum(r, x => x.encoursBrut);
    if (!enc) return 0;
    return sum(r, x => (x.par30 ?? 0) * (x.encoursBrut ?? 0)) / enc;
  });
  par90Moyen = computed<number>(() => {
    const r = this.ratios();
    const enc = sum(r, x => x.encoursBrut);
    if (!enc) return 0;
    return sum(r, x => (x.par90 ?? 0) * (x.encoursBrut ?? 0)) / enc;
  });
  tauxCouvertureMoyen = computed<number>(() => {
    const r = this.ratios();
    const enc = sum(r, x => x.encoursBrut);
    if (!enc) return 0;
    return sum(r, x => (x.tauxCouverture ?? 0) * (x.encoursBrut ?? 0)) / enc;
  });

  // ── Donut PAR ───────────────────────────────────────────────────────────
  donutPar = computed<DonutSlice[]>(() => {
    const c = this.credits();
    const buckets: Record<string, number> = { SAIN: 0, PAR30: 0, PAR60: 0, PAR90: 0 };
    for (const k of c) {
      const cat = (k.categoriePar || 'SAIN').toUpperCase();
      if (buckets[cat] != null) buckets[cat]++;
      else buckets['SAIN']++;
    }
    return [
      { label: 'Sain',    value: buckets['SAIN'],  color: '#28a745' },
      { label: 'PAR 30',  value: buckets['PAR30'], color: '#ffc107' },
      { label: 'PAR 60',  value: buckets['PAR60'], color: '#fd7e14' },
      { label: 'PAR 90+', value: buckets['PAR90'], color: '#dc3545' }
    ];
  });
  donutParTotal = computed<number>(() => sum(this.donutPar(), s => s.value));

  // ── Bar : encours par agence (top 8) ───────────────────────────────────
  barEncours = computed<BarItem[]>(() =>
    [...this.indicateurs()]
      .map(i => ({ label: i.nomAgence || i.codeAgence, value: i.encoursBrut ?? 0 }))
      .sort((a, b) => b.value - a.value)
      .slice(0, 8)
  );
  barEncoursMax = computed<number>(() => Math.max(1, ...this.barEncours().map(b => b.value)));

  // ── Donut flux du jour ─────────────────────────────────────────────────
  donutFlux = computed<DonutSlice[]>(() => [
    { label: 'Décaissé',  value: this.montantDecaisseJour(), color: '#1970c2' },
    { label: 'Remboursé', value: this.montantRemboJour(),    color: '#28a745' },
    { label: 'Encaissé',  value: this.encaissJour(),          color: '#6f42c1' }
  ]);
  donutFluxTotal = computed<number>(() => sum(this.donutFlux(), s => s.value));

  // ── Derniers crédits décaissés ─────────────────────────────────────────
  lastCredits = computed<EtatCredit[]>(() =>
    [...this.credits()]
      .filter(c => c.dateDeblocage)
      .sort((a, b) => (b.dateDeblocage ?? '').localeCompare(a.dateDeblocage ?? ''))
      .slice(0, 6)
  );

  constructor(private reporting: ReportingService) {}

  ngOnInit(): void { this.refresh(); }

  refresh(): void {
    this.loading.set(true);
    this.error.set(null);
    let pending = 3;
    const done = () => { pending--; if (pending === 0) this.loading.set(false); };

    this.reporting.getIndicateurs().subscribe({
      next: list => { this.indicateurs.set(list); done(); },
      error: e   => { this.error.set('Erreur indicateurs : ' + (e.error?.message ?? e.message)); done(); }
    });
    this.reporting.getRatiosBcm().subscribe({
      next: list => { this.ratios.set(list); done(); },
      error: ()  => done()
    });
    this.reporting.getEtatCredits().subscribe({
      next: list => { this.credits.set(list); done(); },
      error: ()  => done()
    });
  }

  // ── Helper SVG donut ─────────────────────────────────────────────────
  donutPath(slice: DonutSlice, slices: DonutSlice[], total: number): string {
    if (!total) return '';
    const r = 70, cx = 80, cy = 80;
    const startA = startAngle(slices, slice, total);
    const endA   = startA + (slice.value / total) * 360;
    return arcPath(cx, cy, r, startA, endA);
  }

  // ── Format ──────────────────────────────────────────────────────────
  fmt(n: number): string {
    if (n == null || isNaN(n)) return '—';
    if (Math.abs(n) >= 1_000_000) return (n / 1_000_000).toFixed(1) + ' M';
    if (Math.abs(n) >= 1_000)     return (n / 1_000).toFixed(1) + ' k';
    return new Intl.NumberFormat('fr-FR').format(Math.round(n));
  }

  fmtPct(n: number): string {
    if (n == null || isNaN(n)) return '—';
    return (n * 100).toFixed(2) + ' %';
  }

  parClass(p: number): string {
    if (p > 0.05) return 'kpi-bad';
    if (p > 0.03) return 'kpi-warn';
    return 'kpi-ok';
  }

  pctMembresActifs = computed<number>(() => {
    const t = this.nbMembres();
    return t ? (this.nbMembresActifs() / t) * 100 : 0;
  });
}

// ── Helpers SVG ────────────────────────────────────────────────────────────
function sum<T>(arr: T[], pick: (x: T) => number): number {
  return arr.reduce((s, x) => s + (pick(x) || 0), 0);
}

function startAngle(slices: DonutSlice[], current: DonutSlice, total: number): number {
  let a = -90;
  for (const s of slices) {
    if (s === current) return a;
    a += (s.value / total) * 360;
  }
  return a;
}

function arcPath(cx: number, cy: number, r: number, startA: number, endA: number): string {
  if (Math.abs(endA - startA) >= 359.99) {
    // cercle plein
    return `M ${cx} ${cy - r} A ${r} ${r} 0 1 1 ${cx - 0.01} ${cy - r} Z`;
  }
  const start = polar(cx, cy, r, startA);
  const end   = polar(cx, cy, r, endA);
  const large = endA - startA <= 180 ? 0 : 1;
  return `M ${cx} ${cy} L ${start.x} ${start.y} A ${r} ${r} 0 ${large} 1 ${end.x} ${end.y} Z`;
}

function polar(cx: number, cy: number, r: number, angle: number): { x: number; y: number } {
  const rad = (angle * Math.PI) / 180;
  return { x: cx + r * Math.cos(rad), y: cy + r * Math.sin(rad) };
}
