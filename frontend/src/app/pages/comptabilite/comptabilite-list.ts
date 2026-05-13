import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ComptabiliteService, Ecriture } from '../../services/comptabilite.service';

@Component({
  selector: 'app-comptabilite-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './comptabilite-list.html'
})
export class ComptabiliteListComponent implements OnInit {
  private all = signal<Ecriture[]>([]);

  search       = signal('');
  filtreLettre = signal<string>('');
  agenceFilter = signal<string>('');

  ecritures = computed<Ecriture[]>(() => {
    const q = this.search().trim().toLowerCase();
    const fL = this.filtreLettre();
    return this.all().filter(e => {
      if (fL === 'lettre' && !e.codeLettrage) return false;
      if (fL === 'non' && e.codeLettrage) return false;
      if (!q) return true;
      return (e.numCompte?.toLowerCase().includes(q))
          || (e.libelle?.toLowerCase().includes(q) ?? false)
          || (e.codeLettrage?.toLowerCase().includes(q) ?? false);
    });
  });

  totalDebit = computed<number>(() =>
    this.ecritures().reduce((sum, e) => sum + (e.debit ?? 0), 0)
  );
  totalCredit = computed<number>(() =>
    this.ecritures().reduce((sum, e) => sum + (e.credit ?? 0), 0)
  );

  loading = signal(false);
  saving  = signal(false);
  error   = signal<string | null>(null);
  success = signal<string | null>(null);

  showLettrage = signal<number | null>(null);
  codeLettrage = '';

  constructor(private svc: ComptabiliteService) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.svc.getEcritures(this.agenceFilter() || undefined).subscribe({
      next: data => { this.all.set(data); this.loading.set(false); },
      error: e   => { this.error.set('Erreur : ' + (e.error?.message ?? e.message)); this.loading.set(false); }
    });
  }

  resetFilters(): void {
    this.search.set('');
    this.filtreLettre.set('');
    this.agenceFilter.set('');
    this.load();
  }

  openLettrage(id: number): void {
    this.showLettrage.set(id);
    this.codeLettrage = '';
  }

  submitLettrage(): void {
    const id = this.showLettrage();
    if (!id || !this.codeLettrage.trim()) {
      this.error.set('Le code de lettrage est obligatoire.');
      return;
    }
    this.saving.set(true);
    this.svc.lettrer(id, this.codeLettrage.trim()).subscribe({
      next: () => {
        this.saving.set(false);
        this.showLettrage.set(null);
        this.success.set('Lettrage appliqué.');
        this.load();
      },
      error: e => {
        this.saving.set(false);
        this.error.set('Erreur : ' + (e.error?.message ?? e.message));
      }
    });
  }
}
