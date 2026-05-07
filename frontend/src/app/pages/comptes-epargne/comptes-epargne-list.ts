import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ComptesEpargneService, CompteEpargne } from '../../services/comptes-epargne.service';
import { AgencesService, Agence } from '../../services/agences.service';

@Component({
  selector: 'app-comptes-epargne-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './comptes-epargne-list.html'
})
export class ComptesEpargneListComponent implements OnInit {
  private all = signal<CompteEpargne[]>([]);
  agences = signal<Agence[]>([]);

  search       = signal('');
  agenceFilter = signal<string>('');
  filtreStatut = signal<string>('');

  comptes = computed<CompteEpargne[]>(() => {
    const q = this.search().trim().toLowerCase();
    const fS = this.filtreStatut();
    return this.all().filter(c => {
      if (fS && c.statut !== fS) return false;
      if (!q) return true;
      return (c.numCompte?.toLowerCase().includes(q))
          || (c.numMembre?.toLowerCase().includes(q))
          || (c.nomMembre?.toLowerCase().includes(q) ?? false);
    });
  });

  totalSolde = computed<number>(() => this.comptes().reduce((s, c) => s + (c.solde ?? 0), 0));

  loading = signal(false);
  saving  = signal(false);
  error   = signal<string | null>(null);
  success = signal<string | null>(null);

  showForm = signal(false);
  form: Partial<CompteEpargne> = { tauxInteret: 0, solde: 0 };

  constructor(
    private svc: ComptesEpargneService,
    private agencesSvc: AgencesService
  ) {}

  ngOnInit(): void {
    this.load();
    this.agencesSvc.getAll(true).subscribe({ next: list => this.agences.set(list) });
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.svc.getAll(this.agenceFilter() || undefined).subscribe({
      next: data => { this.all.set(data); this.loading.set(false); },
      error: e   => { this.error.set('Erreur : ' + (e.error?.message ?? e.message)); this.loading.set(false); }
    });
  }

  resetFilters(): void {
    this.search.set('');
    this.agenceFilter.set('');
    this.filtreStatut.set('');
    this.load();
  }

  openNew(): void {
    this.form = { tauxInteret: 0, solde: 0, dateOuverture: new Date().toISOString().slice(0, 10) };
    this.showForm.set(true);
    this.error.set(null);
    this.success.set(null);
  }

  cancel(): void {
    this.showForm.set(false);
  }

  submit(): void {
    if (!this.form.numCompte || !this.form.numMembre || !this.form.agence) {
      this.error.set('N° compte, N° membre et agence sont obligatoires.');
      return;
    }
    this.saving.set(true);
    this.error.set(null);

    this.svc.ouvrir(this.form).subscribe({
      next: () => {
        this.saving.set(false);
        this.success.set('Compte épargne ouvert.');
        this.showForm.set(false);
        this.load();
      },
      error: e => {
        this.saving.set(false);
        this.error.set('Erreur : ' + (e.error?.message ?? e.message));
      }
    });
  }

  bloquer(numCompte: string): void {
    if (!confirm(`Bloquer le compte ${numCompte} ?`)) return;
    this.svc.bloquer(numCompte).subscribe({
      next: () => { this.success.set('Compte bloqué.'); this.load(); },
      error: e => this.error.set('Erreur : ' + (e.error?.message ?? e.message))
    });
  }

  statutClass(s: string): string {
    if (s === 'ACTIF')  return 'badge badge-success';
    if (s === 'BLOQUE') return 'badge badge-warning';
    if (s === 'FERME')  return 'badge badge-danger';
    return 'badge badge-info';
  }
}
