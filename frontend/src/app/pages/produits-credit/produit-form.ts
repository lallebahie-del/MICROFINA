import { Component, OnInit, signal } from '@angular/core';
import { CommonModule }              from '@angular/common';
import { FormsModule }               from '@angular/forms';
import { ActivatedRoute, Router }    from '@angular/router';
import { ProduitsCreditService, ProduitCredit } from '../../services/produits-credit.service';

@Component({
  selector: 'app-produit-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './produit-form.html'
})
export class ProduitFormComponent implements OnInit {

  isNew      = true;
  numProduit: string | null = null;

  loading = signal(false);
  saving  = signal(false);
  error   = signal<string | null>(null);
  success = signal<string | null>(null);

  form: Partial<ProduitCredit> = {
    actif:                          1,
    tauxInteret:                    0,
    tauxPenalite:                   0,
    tauxCommission:                 0,
    tauxAssurance:                  0,
    garantieRequise:                0,
    autoriserReneg:                 0,
    autoriserRemboursementAnticipe: 0,
    decaissementNet:                0,
    periodiciteRemboursement:       'M'
  };

  constructor(
    private route:   ActivatedRoute,
    private router:  Router,
    private service: ProduitsCreditService
  ) {}

  ngOnInit(): void {
    this.numProduit = this.route.snapshot.paramMap.get('numProduit');
    this.isNew = (this.numProduit === 'nouveau' || !this.numProduit);

    if (!this.isNew && this.numProduit) {
      this.loading.set(true);
      this.service.getOne(this.numProduit).subscribe({
        next:  p => { this.form = { ...p }; this.loading.set(false); },
        error: e => { this.error.set('Erreur : ' + e.message); this.loading.set(false); }
      });
    }
  }

  submit(): void {
    if (!this.form.numProduit?.trim()) { this.error.set('Le code produit est obligatoire.'); return; }
    if (!this.form.nomProduit?.trim()) { this.error.set('Le nom du produit est obligatoire.'); return; }

    this.saving.set(true);
    this.error.set(null);

    const call = this.isNew
      ? this.service.create(this.form)
      : this.service.update(this.numProduit!, this.form);

    call.subscribe({
      next: () => {
        this.saving.set(false);
        this.success.set(this.isNew ? 'Produit créé.' : 'Produit mis à jour.');
        setTimeout(() => this.router.navigate(['/produits-credit']), 1200);
      },
      error: e => {
        this.saving.set(false);
        this.error.set('Erreur : ' + (e.error?.message ?? e.message ?? e));
      }
    });
  }

  annuler(): void { this.router.navigate(['/produits-credit']); }
}
