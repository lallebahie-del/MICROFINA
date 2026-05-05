import { Component, OnInit, signal } from '@angular/core';
import { CommonModule }              from '@angular/common';
import { FormsModule }               from '@angular/forms';
import { Router, RouterModule }      from '@angular/router';
import { HttpErrorResponse }         from '@angular/common/http';
import { MembresService, Membre, PageResult } from '../../services/membres.service';

@Component({
  selector: 'app-membres-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './membres-list.html'
})
export class MembresListComponent implements OnInit {

  // ── State ──────────────────────────────────────────────────────
  result   = signal<PageResult<Membre> | null>(null);
  loading  = signal(false);
  error    = signal<string | null>(null);

  // ── Filters ────────────────────────────────────────────────────
  search = '';
  statut = '';
  etat   = '';
  page   = 0;
  size   = 20;

  constructor(
    private service: MembresService,
    private router:  Router
  ) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.service.search(this.search, this.statut, this.etat, this.page, this.size)
      .subscribe({
        next:  r => { this.result.set(r); this.loading.set(false); },
        error: e => {
          let detail = String(e);
          if (e instanceof HttpErrorResponse) {
            if (e.status === 401) {
              detail = 'non authentifié (401). Reconnectez-vous.';
            } else if (e.status === 403) {
              detail = 'accès refusé (403) — privilège manquant pour lire les membres.';
            } else if (e.status === 0) {
              detail = 'impossible de joindre l’API (serveur arrêté, mauvaise URL ou CORS). Vérifiez que le backend tourne sur ' + (e.url ?? 'l’URL configurée dans environment.apiUrl') + '.';
            } else {
              detail = e.error?.message ?? e.message ?? e.statusText ?? String(e.status);
            }
          }
          this.error.set('Erreur de chargement : ' + detail);
          this.loading.set(false);
        }
      });
  }

  onSearch(): void { this.page = 0; this.load(); }
  onReset():  void { this.search = ''; this.statut = ''; this.etat = ''; this.page = 0; this.load(); }
  goPage(p: number): void { this.page = p; this.load(); }

  edit(numMembre: string): void {
    this.router.navigate(['/membres', numMembre, 'edit']);
  }

  nouveau(): void {
    this.router.navigate(['/membres', 'nouveau']);
  }

  desactiver(numMembre: string): void {
    if (!confirm(`Désactiver le membre ${numMembre} ?`)) return;
    this.service.desactiver(numMembre).subscribe({
      next:  () => this.load(),
      error: e  => this.error.set('Erreur : ' + (e.message ?? e))
    });
  }

  get pages(): number[] {
    const total = this.result()?.totalPages ?? 0;
    return Array.from({ length: total }, (_, i) => i);
  }
}
