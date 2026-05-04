import { Component, OnInit, signal } from '@angular/core';
import { CommonModule }              from '@angular/common';
import { FormsModule }               from '@angular/forms';
import { Router, RouterModule }      from '@angular/router';
import { ProduitsCreditService, ProduitCredit, PageResult } from '../../services/produits-credit.service';

@Component({
  selector: 'app-produits-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './produits-list.html'
})
export class ProduitsListComponent implements OnInit {

  result  = signal<PageResult<ProduitCredit> | null>(null);
  loading = signal(false);
  error   = signal<string | null>(null);

  search = '';
  actif: number | undefined = undefined;
  page   = 0;
  size   = 20;

  constructor(
    private service: ProduitsCreditService,
    private router:  Router
  ) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.service.search(this.search, this.actif, this.page, this.size).subscribe({
      next:  r => { this.result.set(r); this.loading.set(false); },
      error: e => { this.error.set('Erreur : ' + e.message); this.loading.set(false); }
    });
  }

  onSearch(): void { this.page = 0; this.load(); }
  onReset():  void { this.search = ''; this.actif = undefined; this.page = 0; this.load(); }
  goPage(p: number): void { this.page = p; this.load(); }

  edit(numProduit: string):    void { this.router.navigate(['/produits-credit', numProduit, 'edit']); }
  nouveau():                   void { this.router.navigate(['/produits-credit', 'nouveau']); }

  supprimer(numProduit: string): void {
    if (!confirm(`Supprimer le produit ${numProduit} ?`)) return;
    this.service.delete(numProduit).subscribe({
      next:  () => this.load(),
      error: e  => this.error.set('Erreur : ' + e.message)
    });
  }

  get pages(): number[] {
    const total = this.result()?.totalPages ?? 0;
    return Array.from({ length: total }, (_, i) => i);
  }
}
