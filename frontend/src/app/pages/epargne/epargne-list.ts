import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ComptesEpargneService, CompteEpargne } from '../../services/comptes-epargne.service';
import { AgencesService, Agence } from '../../services/agences.service';

@Component({
  selector: 'app-epargne-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './epargne-list.html'
})
export class EpargneListComponent implements OnInit {
  comptes = signal<CompteEpargne[]>([]);
  agences = signal<Agence[]>([]);

  agenceFilter = signal<string>('');
  loading = signal(false);
  saving  = signal(false);
  error   = signal<string | null>(null);
  success = signal<string | null>(null);

  showOp = signal<{ type: 'depot' | 'retrait'; numCompte: string; solde: number } | null>(null);
  montant = 0;
  libelle = '';

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
      next: data => { this.comptes.set(data); this.loading.set(false); },
      error: e   => { this.error.set('Erreur : ' + (e.error?.message ?? e.message)); this.loading.set(false); }
    });
  }

  openDepot(c: CompteEpargne): void {
    this.showOp.set({ type: 'depot', numCompte: c.numCompte, solde: c.solde });
    this.montant = 0;
    this.libelle = '';
    this.error.set(null);
    this.success.set(null);
  }

  openRetrait(c: CompteEpargne): void {
    this.showOp.set({ type: 'retrait', numCompte: c.numCompte, solde: c.solde });
    this.montant = 0;
    this.libelle = '';
    this.error.set(null);
    this.success.set(null);
  }

  cancelOp(): void { this.showOp.set(null); }

  submitOp(): void {
    const op = this.showOp();
    if (!op) return;
    if (this.montant <= 0) {
      this.error.set('Le montant doit être strictement positif.');
      return;
    }
    if (op.type === 'retrait' && this.montant > op.solde) {
      this.error.set(`Solde insuffisant (${op.solde} MRU).`);
      return;
    }
    this.saving.set(true);
    this.error.set(null);

    const call = op.type === 'depot'
      ? this.svc.depot(op.numCompte, this.montant, this.libelle)
      : this.svc.retrait(op.numCompte, this.montant, this.libelle);

    call.subscribe({
      next: () => {
        this.saving.set(false);
        this.success.set(`${op.type === 'depot' ? 'Dépôt' : 'Retrait'} de ${this.montant} MRU effectué sur ${op.numCompte}.`);
        this.showOp.set(null);
        this.load();
      },
      error: e => {
        this.saving.set(false);
        this.error.set('Erreur : ' + (e.error?.message ?? e.message));
      }
    });
  }

  statutClass(s: string): string {
    if (s === 'ACTIF')  return 'badge badge-success';
    if (s === 'BLOQUE') return 'badge badge-warning';
    if (s === 'FERME')  return 'badge badge-danger';
    return 'badge badge-info';
  }
}
