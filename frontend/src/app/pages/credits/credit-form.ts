import { Component, OnInit, signal } from '@angular/core';
import { CommonModule }              from '@angular/common';
import { FormsModule }               from '@angular/forms';
import { ActivatedRoute, Router }    from '@angular/router';
import { forkJoin }                  from 'rxjs';
import { CreditsService, Credit }    from '../../services/credits.service';
import { ProduitsCreditService, ProduitCredit } from '../../services/produits-credit.service';
import { MembresService, Membre }    from '../../services/membres.service';

@Component({
  selector: 'app-credit-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './credit-form.html'
})
export class CreditFormComponent implements OnInit {

  isNew     = true;
  creditId: number | null = null;

  loading  = signal(false);
  saving   = signal(false);
  error    = signal<string | null>(null);
  success  = signal<string | null>(null);

  // Dropdown data
  produits = signal<ProduitCredit[]>([]);
  membres  = signal<Membre[]>([]);

  // Selected product (for Islamic mode detection)
  selectedProduit = signal<ProduitCredit | null>(null);

  get isIslamic(): boolean {
    return this.selectedProduit()?.familleProduitCode === 'FAM_ISL';
  }

  get islamicType(): 'MOURABAHA' | 'MOUCHARAKA' | 'IJARA' | null {
    const code = this.selectedProduit()?.codeProduitIslamic ?? this.selectedProduit()?.numProduit;
    if (!code) return null;
    if (code.includes('ISL_01') || code.includes('MOURABAHA')) return 'MOURABAHA';
    if (code.includes('ISL_02') || code.includes('MOUCHARAKA')) return 'MOUCHARAKA';
    if (code.includes('ISL_03') || code.includes('IJARA')) return 'IJARA';
    return null;
  }

  form: Partial<Credit> = {
    periodicite: 'M',
    duree:        12,
    nombreEcheance: 12,
    delaiGrace:    0,
    dateDemande:   new Date().toISOString().slice(0, 10)
  };

  constructor(
    private route:          ActivatedRoute,
    private router:         Router,
    private creditsService: CreditsService,
    private produitsService: ProduitsCreditService,
    private membresService: MembresService
  ) {}

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    this.isNew    = (idParam === 'nouveau' || !idParam);
    this.creditId = this.isNew ? null : Number(idParam);

    // Load dropdowns in parallel
    this.produitsService.listActifs().subscribe({
      next: list => this.produits.set(list),
      error: e => this.error.set('Impossible de charger les produits : ' + (e.error?.message ?? e.message ?? 'Erreur réseau'))
    });
    this.membresService.search('', '', '', 0, 200).subscribe({
      next: r => this.membres.set(r.content),
      error: e => this.error.set('Impossible de charger les membres : ' + (e.error?.message ?? e.message ?? 'Erreur réseau'))
    });

    if (!this.isNew && this.creditId) {
      this.loading.set(true);
      forkJoin({
        credit:   this.creditsService.getOne(this.creditId),
        produits: this.produitsService.listActifs()
      }).subscribe({
        next: ({ credit, produits }) => {
          this.form = { ...credit };
          this.produits.set(produits);
          const p = produits.find(p => p.numProduit === credit.produitCode);
          if (p) this.selectedProduit.set(p);
          this.loading.set(false);
        },
        error: e => { this.error.set('Erreur : ' + e.message); this.loading.set(false); }
      });
    }
  }

  // When member is selected, auto-fill agenceCode from member's agency
  onMembreChange(): void {
    const m = this.membres().find(m => m.numMembre === this.form.membreNum);
    if (m?.agenceCode) {
      this.form = { ...this.form, agenceCode: m.agenceCode };
    }
  }

  // When product is selected, prefill rates from the product
  onProduitChange(): void {
    const p = this.produits().find(p => p.numProduit === this.form.produitCode);
    if (!p) { this.selectedProduit.set(null); return; }
    this.selectedProduit.set(p);
    this.form = {
      ...this.form,
      tauxInteret:    p.tauxInteret    ?? 0,
      tauxPenalite:   p.tauxPenalite   ?? 0,
      tauxCommission: p.tauxCommission ?? 0,
      tauxAssurance:  p.tauxAssurance  ?? 0,
      duree:          p.dureeMin,
      nombreEcheance: p.nombreEcheance,
      delaiGrace:     p.delaiGrace     ?? 0,
      periodicite:    p.periodiciteRemboursement
    };
  }

  submit(): void {
    this.saving.set(true);
    this.error.set(null);

    if (this.isNew) {
      // Build flat CreateRequest matching the backend record
      const payload = {
        numMembre:     this.form.membreNum,
        numProduit:    this.form.produitCode,
        codeAgence:    this.form.agenceCode ?? 'NKC',
        montantDemande: this.form.montantDemande,
        periodicite:   this.form.periodicite,
        duree:         this.form.duree,
        nombreEcheance: this.form.nombreEcheance,
        delaiGrace:    this.form.delaiGrace ?? 0,
        objetCredit:   this.form.objetCredit,
        dateDemande:   this.form.dateDemande,
        tauxInteret:   this.form.tauxInteret,
        tauxPenalite:  this.form.tauxPenalite,
        tauxCommission: this.form.tauxCommission,
        tauxAssurance:  this.form.tauxAssurance,
      };
      this.creditsService.create(payload).subscribe({
        next: saved => {
          this.saving.set(false);
          this.success.set('Crédit créé avec succès.');
          setTimeout(() => this.router.navigate(['/credits', saved.idCredit]), 1200);
        },
        error: e => {
          this.saving.set(false);
          this.error.set('Erreur : ' + (e.error?.message ?? e.message ?? e));
        }
      });
    } else {
      // UpdateRequest — montant, schedule, taux only
      const payload = {
        montantDemande:  this.form.montantDemande,
        periodicite:     this.form.periodicite,
        duree:           this.form.duree,
        nombreEcheance:  this.form.nombreEcheance,
        delaiGrace:      this.form.delaiGrace ?? 0,
        objetCredit:     this.form.objetCredit,
        dateDemande:     this.form.dateDemande,
        tauxInteret:     this.form.tauxInteret,
        tauxPenalite:    this.form.tauxPenalite,
        tauxCommission:  this.form.tauxCommission,
        tauxAssurance:   this.form.tauxAssurance,
      };
      this.creditsService.update(this.creditId!, payload).subscribe({
        next: saved => {
          this.saving.set(false);
          this.success.set('Crédit mis à jour.');
          setTimeout(() => this.router.navigate(['/credits', saved.idCredit]), 1200);
        },
        error: e => {
          this.saving.set(false);
          this.error.set('Erreur : ' + (e.error?.message ?? e.message ?? e));
        }
      });
    }
  }

  annuler(): void {
    if (this.creditId) this.router.navigate(['/credits', this.creditId]);
    else               this.router.navigate(['/credits']);
  }
}
