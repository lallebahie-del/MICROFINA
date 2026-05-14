import { Component, OnInit, signal } from '@angular/core';
import { CommonModule }              from '@angular/common';
import { FormsModule }               from '@angular/forms';
import { Router, RouterModule }      from '@angular/router';
import { CreditsService, Credit, CreditStatut, PageResult } from '../../services/credits.service';
import { PaginationBarComponent } from '../../components/pagination-bar/pagination-bar.component';
import { DEFAULT_LIST_PAGE_SIZE } from '../../shared/list-pagination';

@Component({
  selector: 'app-credits-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, PaginationBarComponent],
  templateUrl: './credits-list.html'
})
export class CreditsListComponent implements OnInit {

  result  = signal<PageResult<Credit> | null>(null);
  loading = signal(false);
  error   = signal<string | null>(null);

  search    = '';
  statut    = '';
  numMembre = '';
  page      = 0;
  /** Taille de page (API) — pagination serveur (même que les autres listes) */
  size      = DEFAULT_LIST_PAGE_SIZE;

  readonly statuts: CreditStatut[] = [
    'BROUILLON','SOUMIS','VALIDE_AGENT','VALIDE_COMITE','DEBLOQUE','SOLDE','REJETE'
  ];

  constructor(
    private service: CreditsService,
    private router:  Router
  ) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.service.search(this.search, this.statut, this.numMembre, this.page, this.size).subscribe({
      next:  r => { this.result.set(r); this.loading.set(false); },
      error: e => { this.error.set('Erreur : ' + e.message); this.loading.set(false); }
    });
  }

  onSearch(): void { this.page = 0; this.load(); }
  onReset():  void { this.search = ''; this.statut = ''; this.numMembre = ''; this.page = 0; this.load(); }
  goPage(p: number): void { this.page = p; this.load(); }

  detail(id: number): void      { this.router.navigate(['/credits', id]); }
  nouveau(): void                { this.router.navigate(['/credits', 'nouveau']); }

  soumettre(c: Credit): void {
    if (!confirm(`Soumettre le crédit ${c.numCredit} ?`)) return;
    this.transitionner(c.idCredit!, 'SOUMIS');
  }

  transitionner(id: number, statut: CreditStatut): void {
    this.service.transitionner(id, statut).subscribe({
      next:  () => this.load(),
      error: e  => this.error.set('Erreur : ' + (e.error?.message ?? e.message))
    });
  }

  badgeClass(statut: string): string {
    switch (statut) {
      case 'BROUILLON':     return 'badge badge-warning';
      case 'SOUMIS':        return 'badge badge-info';
      case 'VALIDE_AGENT':  return 'badge badge-info';
      case 'VALIDE_COMITE': return 'badge badge-primary';
      case 'DEBLOQUE':      return 'badge badge-success';
      case 'SOLDE':         return 'badge badge-success';
      case 'REJETE':        return 'badge badge-danger';
      default:              return 'badge';
    }
  }
}
