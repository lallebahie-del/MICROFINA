import { Component, OnInit, signal } from '@angular/core';
import { CommonModule }              from '@angular/common';
import { FormsModule }               from '@angular/forms';
import { ActivatedRoute, Router }    from '@angular/router';
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
      error: () => {}
    });
    this.membresService.search('', 'ACTIF', 'ACTIF', 0, 100).subscribe({
      next: r => this.membres.set(r.content),
      error: () => {}
    });

    if (!this.isNew && this.creditId) {
      this.loading.set(true);
      this.creditsService.getOne(this.creditId).subscribe({
        next:  c => { this.form = { ...c }; this.loading.set(false); },
        error: e => { this.error.set('Erreur : ' + e.message); this.loading.set(false); }
      });
    }
  }

  // When product is selected, prefill rates from the product
  onProduitChange(): void {
    const p = this.produits().find(p => p.numProduit === this.form.produitCode);
    if (!p) return;
    this.form = {
      ...this.form,
      tauxInteret:    p.tauxInteret,
      tauxPenalite:   p.tauxPenalite,
      tauxCommission: p.tauxCommission,
      tauxAssurance:  p.tauxAssurance,
      duree:          p.dureeMin,
      nombreEcheance: p.nombreEcheance,
      delaiGrace:     p.delaiGrace,
      periodicite:    p.periodiciteRemboursement
    };
  }

  submit(): void {
    this.saving.set(true);
    this.error.set(null);

    // Map form's flat FK fields to the entity shape the backend expects
    const payload: any = { ...this.form };
    if (payload.membreNum)  { payload.membre    = { numMembre: payload.membreNum }; }
    if (payload.produitCode){ payload.produitCredit = { numProduit: payload.produitCode }; }

    const call = this.isNew
      ? this.creditsService.create(payload)
      : this.creditsService.update(this.creditId!, payload);

    call.subscribe({
      next: saved => {
        this.saving.set(false);
        this.success.set(this.isNew ? 'Crédit créé avec succès.' : 'Crédit mis à jour.');
        setTimeout(() => this.router.navigate(['/credits', saved.idCredit]), 1200);
      },
      error: e => {
        this.saving.set(false);
        this.error.set('Erreur : ' + (e.error?.message ?? e.message ?? e));
      }
    });
  }

  annuler(): void {
    if (this.creditId) this.router.navigate(['/credits', this.creditId]);
    else               this.router.navigate(['/credits']);
  }
}
