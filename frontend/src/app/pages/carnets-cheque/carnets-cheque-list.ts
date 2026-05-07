import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CarnetsChequeService, CarnetCheque } from '../../services/carnets-cheque.service';
import { AgencesService, Agence } from '../../services/agences.service';

@Component({
  selector: 'app-carnets-cheque-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './carnets-cheque-list.html'
})
export class CarnetsChequeListComponent implements OnInit {
  private all = signal<CarnetCheque[]>([]);
  agences = signal<Agence[]>([]);

  search       = signal('');
  agenceFilter = signal<string>('');
  filtreStatut = signal<string>('');

  carnets = computed<CarnetCheque[]>(() => {
    const q = this.search().trim().toLowerCase();
    const fS = this.filtreStatut();
    return this.all().filter(c => {
      if (fS && c.statut !== fS) return false;
      if (!q) return true;
      return (c.numCarnet?.toLowerCase().includes(q))
          || (c.numMembre?.toLowerCase().includes(q))
          || (c.nomMembre?.toLowerCase().includes(q) ?? false);
    });
  });

  loading = signal(false);
  saving  = signal(false);
  error   = signal<string | null>(null);
  success = signal<string | null>(null);

  showForm = signal(false);
  form: Partial<CarnetCheque> = { nbFeuillets: 25 };

  constructor(
    private svc: CarnetsChequeService,
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
    this.form = {
      nbFeuillets: 25,
      dateEmission: new Date().toISOString().slice(0, 10)
    };
    this.showForm.set(true);
    this.error.set(null);
    this.success.set(null);
  }

  cancel(): void {
    this.showForm.set(false);
  }

  submit(): void {
    if (!this.form.numCarnet || !this.form.numMembre || !this.form.agence
        || !this.form.numeroPremierCheque || !this.form.numeroDernierCheque) {
      this.error.set('Tous les champs marqués (*) sont obligatoires.');
      return;
    }
    this.saving.set(true);
    this.error.set(null);

    this.svc.emettre(this.form).subscribe({
      next: () => {
        this.saving.set(false);
        this.success.set('Carnet émis.');
        this.showForm.set(false);
        this.load();
      },
      error: e => {
        this.saving.set(false);
        this.error.set('Erreur : ' + (e.error?.message ?? e.message));
      }
    });
  }

  bloquer(id: number, num: string): void {
    if (!confirm(`Bloquer le carnet ${num} ?`)) return;
    this.svc.bloquer(id).subscribe({
      next: () => { this.success.set('Carnet bloqué.'); this.load(); },
      error: e => this.error.set('Erreur : ' + (e.error?.message ?? e.message))
    });
  }

  statutClass(s: string): string {
    if (s === 'ACTIF' || s === 'EMIS')     return 'badge badge-success';
    if (s === 'BLOQUE')                     return 'badge badge-warning';
    if (s === 'ANNULE' || s === 'EPUISE')   return 'badge badge-danger';
    return 'badge badge-info';
  }
}
