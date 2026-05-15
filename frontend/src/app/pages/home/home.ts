import { Component, OnInit, signal, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ReportingService, Indicateur, RatiosBcm, EtatCredit } from '../../services/reporting.service';
import { MembresService, Membre } from '../../services/membres.service';
import { CreditsService, Credit } from '../../services/credits.service';
import { AgencesService, Agence } from '../../services/agences.service';
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
  user  = this.auth.currentUser;
  today = new Date();

  can = (...privs: string[]) => this.auth.hasAnyPrivilege(...privs);

  roleLabel = computed(() => {
    const map: Record<string, string> = {
      ADMIN:         'Administrateur système',
      SUPERVISEUR:   'Manager',
      COMPTABLE:     'Comptable',
      CAISSIER:      'Caissier',
      COMITE_CREDIT: 'Comité de crédit',
      AGENT_CREDIT:  'Agent de crédit',
      AUDITEUR:      'Auditeur interne',
    };
    const role = this.user()?.role ?? '';
    return map[role] ?? role;
  });

  indicateurs = signal<Indicateur[]>([]);
  ratios      = signal<RatiosBcm[]>([]);
  credits     = signal<EtatCredit[]>([]);

  // ── Sources de secours (calcul direct depuis membres/crédits/agences) ───
  membresAll = signal<Membre[]>([]);
  creditsAll = signal<Credit[]>([]);
  agencesAll = signal<Agence[]>([]);

  loading = signal(true);
  error   = signal<string | null>(null);

  // ── KPIs agrégés (privilégie reporting, sinon recalcule depuis sources) ─
  nbMembres = computed<number>(() => {
    const fromRep = sum(this.indicateurs(), i => i.nbMembresEmprunteurs);
    return fromRep || this.membresAll().length;
  });
  nbMembresActifs = computed<number>(() => {
    const fromRep = sum(this.indicateurs(), i => i.nbMembresActifs);
    if (fromRep) return fromRep;
    return this.membresAll().filter(m => isActif(m.etat) || isActif(m.statut)).length;
  });
  nbCreditsActifs = computed<number>(() => {
    const fromRep = sum(this.indicateurs(), i => i.nbCreditsActifs);
    if (fromRep) return fromRep;
    return this.creditsAll().filter(c => c.statut === 'DEBLOQUE').length;
  });
  encoursBrutTotal = computed<number>(() => {
    const fromRep = sum(this.indicateurs(), i => i.montantEncours);
    if (fromRep) return fromRep;
    return this.creditsAll()
      .filter(c => c.statut === 'DEBLOQUE')
      .reduce((s, c) => s + (c.soldeCapital || 0), 0);
  });
  montantDecaisseJour = computed<number>(() => {
    const fromRep = sum(this.indicateurs(), i => i.montantDebloqueTotal);
    if (fromRep) return fromRep;
    const today = todayIso();
    return this.creditsAll()
      .filter(c => c.dateDeblocage?.startsWith(today))
      .reduce((s, c) => s + (c.montantDebloquer || c.montantAccorde || 0), 0);
  });
  montantRemboJour = computed<number>(() => sum(this.indicateurs(), i => i.montantRembourseTotal));
  encaissJour      = computed<number>(() => sum(this.indicateurs(), i => i.nbReglements));

  par30Moyen = computed<number>(() => {
    const r = this.ratios();
    const enc = sum(r, x => x.encoursBrut);
    if (!enc) return 0;
    return sum(r, x => (x.tauxPar30 ?? 0) * (x.encoursBrut ?? 0)) / enc;
  });
  par90Moyen = computed<number>(() => {
    const r = this.ratios();
    const enc = sum(r, x => x.encoursBrut);
    if (!enc) return 0;
    return sum(r, x => (x.tauxPar90 ?? 0) * (x.encoursBrut ?? 0)) / enc;
  });
  tauxCouvertureMoyen = computed<number>(() => {
    const r = this.ratios();
    const enc = sum(r, x => x.encoursBrut);
    if (!enc) return 0;
    return sum(r, x => (x.ratioCouvertureGaranties ?? 0) * (x.encoursBrut ?? 0)) / enc;
  });

  // ── Donut PAR ───────────────────────────────────────────────────────────
  donutPar = computed<DonutSlice[]>(() => {
    const buckets: Record<string, number> = { SAIN: 0, PAR30: 0, PAR60: 0, PAR90: 0 };
    const repCredits = this.credits();
    if (repCredits.length) {
      for (const k of repCredits) {
        const cat = (k.categoriePar || 'SAIN').toUpperCase();
        if (buckets[cat] != null) buckets[cat]++;
        else buckets['SAIN']++;
      }
    } else {
      for (const c of this.creditsAll()) {
        if (c.statut !== 'DEBLOQUE') continue;
        buckets['SAIN']++;
      }
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
  barEncours = computed<BarItem[]>(() => {
    const rep = this.indicateurs();
    if (rep.length) {
      return [...rep]
        .map(i => ({ label: i.nomAgence || i.codeAgence, value: i.montantEncours ?? 0 }))
        .sort((a, b) => b.value - a.value)
        .slice(0, 8);
    }
    const map = new Map<string, number>();
    for (const c of this.creditsAll()) {
      if (c.statut !== 'DEBLOQUE') continue;
      const key = c.agenceNom || c.agenceCode || '—';
      map.set(key, (map.get(key) || 0) + (c.soldeCapital || 0));
    }
    return [...map.entries()]
      .map(([label, value]) => ({ label, value }))
      .sort((a, b) => b.value - a.value)
      .slice(0, 8);
  });
  barEncoursMax = computed<number>(() => Math.max(1, ...this.barEncours().map(b => b.value)));

  // ── Donut flux du jour ─────────────────────────────────────────────────
  donutFlux = computed<DonutSlice[]>(() => [
    { label: 'Décaissé',  value: this.montantDecaisseJour(), color: '#1970c2' },
    { label: 'Remboursé', value: this.montantRemboJour(),    color: '#28a745' },
    { label: 'Encaissé',  value: this.encaissJour(),          color: '#6f42c1' }
  ]);
  donutFluxTotal = computed<number>(() => sum(this.donutFlux(), s => s.value));

  // ── Derniers crédits décaissés ─────────────────────────────────────────
  lastCredits = computed<EtatCredit[]>(() => {
    const rep = [...this.credits()]
      .filter(c => c.dateDeblocage)
      .sort((a, b) => (b.dateDeblocage ?? '').localeCompare(a.dateDeblocage ?? ''))
      .slice(0, 6);
    if (rep.length) return rep;
    return this.creditsAll()
      .filter(c => c.dateDeblocage)
      .sort((a, b) => (b.dateDeblocage ?? '').localeCompare(a.dateDeblocage ?? ''))
      .slice(0, 6)
      .map(c => creditToEtat(c));
  });

  // ── Indicateurs détaillés par agence (avec fallback) ───────────────────
  indicateursTable = computed<Indicateur[]>(() => {
    const rep = this.indicateurs();
    if (rep.length) return rep;
    const byAg = new Map<string, Indicateur>();
    for (const a of this.agencesAll()) {
      byAg.set(a.codeAgence, {
        codeAgence: a.codeAgence,
        nomAgence: a.nomAgence || a.codeAgence,
        nbMembresEmprunteurs: 0,
        nbMembresActifs: 0,
        nbCreditsTotal: 0,
        nbCreditsActifs: 0,
        montantEncours: 0,
        montantDebloqueTotal: 0,
        nbReglements: 0,
        montantRembourseTotal: 0
      });
    }
    const ensure = (code: string, nom?: string): Indicateur => {
      let row = byAg.get(code);
      if (!row) {
        row = {
          codeAgence: code, nomAgence: nom || code,
          nbMembresEmprunteurs: 0, nbMembresActifs: 0,
          nbCreditsTotal: 0, nbCreditsActifs: 0,
          montantEncours: 0, montantDebloqueTotal: 0,
          nbReglements: 0, montantRembourseTotal: 0
        };
        byAg.set(code, row);
      }
      return row;
    };
    for (const m of this.membresAll()) {
      const code = m.agenceCode || '—';
      const row = ensure(code, m.agenceLibelle);
      row.nbMembresEmprunteurs++;
      if (isActif(m.etat) || isActif(m.statut)) row.nbMembresActifs++;
    }
    const today = todayIso();
    for (const c of this.creditsAll()) {
      const code = c.agenceCode || '—';
      const row = ensure(code, c.agenceNom);
      row.nbCreditsTotal++;
      if (c.statut === 'DEBLOQUE') {
        row.nbCreditsActifs++;
        row.montantEncours += c.soldeCapital || 0;
      }
      if (c.dateDeblocage?.startsWith(today)) {
        row.montantDebloqueTotal += c.montantDebloquer || c.montantAccorde || 0;
      }
    }
    return [...byAg.values()].filter(r =>
      r.nbMembresEmprunteurs || r.nbCreditsTotal
    );
  });

  constructor(
    private reporting: ReportingService,
    private membresSvc: MembresService,
    private creditsSvc: CreditsService,
    private agencesSvc: AgencesService
  ) {}

  ngOnInit(): void { this.refresh(); }

  refresh(): void {
    this.loading.set(true);
    this.error.set(null);
    let pending = 6;
    const done = () => { pending--; if (pending <= 0) this.loading.set(false); };

    /**
     * Les endpoints /reporting/* exigent PRIV_VIEW_REPORTS. En 401/403 on
     * masque l'erreur et on bascule sur les services de base (membres,
     * crédits, agences) pour calculer les KPIs directement.
     */
    const isAccessError = (e: any): boolean =>
      e?.status === 401 || e?.status === 403;

    this.reporting.getIndicateurs().subscribe({
      next: list => { this.indicateurs.set(list); done(); },
      error: e   => {
        if (!isAccessError(e)) {
          this.error.set('Erreur indicateurs : ' + (e.error?.message ?? e.message));
        }
        done();
      }
    });
    this.reporting.getRatiosBcm().subscribe({
      next: list => { this.ratios.set(list); done(); },
      error: ()  => done()
    });
    this.reporting.getEtatCredits().subscribe({
      next: list => { this.credits.set(list); done(); },
      error: ()  => done()
    });

    // Fallback : sources de base toujours chargées en parallèle.
    this.membresSvc.search('', '', '', 0, 500).subscribe({
      next: p => { this.membresAll.set(p.content || []); done(); },
      error: () => done()
    });
    this.creditsSvc.search('', '', '', 0, 500).subscribe({
      next: p => { this.creditsAll.set(p.content || []); done(); },
      error: () => done()
    });
    this.agencesSvc.getAll(true).subscribe({
      next: list => { this.agencesAll.set(list || []); done(); },
      error: () => done()
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

// ── Helpers ────────────────────────────────────────────────────────────────
function sum<T>(arr: T[], pick: (x: T) => number): number {
  return arr.reduce((s, x) => s + (pick(x) || 0), 0);
}

function isActif(v?: string): boolean {
  const s = (v || '').toUpperCase();
  return s === 'ACTIF' || s === 'VALIDE' || s === 'VALIDÉ' || s === 'VALIDE_AGENT';
}

function todayIso(): string {
  return new Date().toISOString().slice(0, 10);
}

function creditToEtat(c: Credit): EtatCredit {
  return {
    idCredit: c.idCredit ?? 0,
    numCredit: c.numCredit ?? '',
    statutCredit: c.statut ?? '',
    objetCredit: c.objetCredit ?? '',
    numMembre: c.membreNum ?? '',
    nomMembre: c.membreNom ?? '',
    prenomMembre: c.membrePrenom ?? '',
    sexe: '',
    codeAgence: c.agenceCode ?? '',
    nomAgence: c.agenceNom ?? '',
    nomAgent: '',
    dateDemande: c.dateDemande ?? '',
    dateDeblocage: c.dateDeblocage ?? '',
    dateEcheanceFinale: c.dateEcheance ?? '',
    montantAccorde: c.montantAccorde ?? 0,
    montantDebloque: c.montantDebloquer ?? 0,
    soldeCapital: c.soldeCapital ?? 0,
    soldeTotal: (c.soldeCapital ?? 0) + (c.soldeInteret ?? 0) + (c.soldePenalite ?? 0),
    joursRetard: 0,
    totalArrieres: 0,
    categoriePar: 'SAIN',
    totalGaranties: 0,
    tauxCouverturePct: 0
  };
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
