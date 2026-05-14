import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BanqueService, Banque } from '../../services/banque.service';
import { PaginationBarComponent } from '../../components/pagination-bar/pagination-bar.component';
import { DEFAULT_LIST_PAGE_SIZE, listClampPage, listSlice } from '../../shared/list-pagination';

@Component({
  selector: 'app-banques-list',
  standalone: true,
  imports: [CommonModule, FormsModule, PaginationBarComponent],
  templateUrl: './banques-list.html'
})
export class BanquesListComponent implements OnInit {
  private all = signal<Banque[]>([]);

  search       = signal('');
  filtreActif  = signal<string>('');
  pageBanque   = signal(0);
  readonly pageSize = DEFAULT_LIST_PAGE_SIZE;

  banques = computed<Banque[]>(() => {
    const q = this.search().trim().toLowerCase();
    const fA = this.filtreActif();
    return this.all().filter(b => {
      if (fA !== '' && (fA === 'true') !== !!b.actif) return false;
      if (!q) return true;
      return (b.codeBanque?.toLowerCase().includes(q))
          || (b.nom?.toLowerCase().includes(q))
          || (b.swiftBic?.toLowerCase().includes(q) ?? false)
          || (b.pays?.toLowerCase().includes(q) ?? false);
    });
  });

  /** Page bornée au nombre de pages (après filtre / chargement) */
  clampedPageBanque = computed(() =>
    listClampPage(this.pageBanque(), this.banques().length, this.pageSize)
  );

  pagedBanques = computed(() =>
    listSlice(this.banques(), this.clampedPageBanque(), this.pageSize)
  );

  loading = signal(false);
  saving  = signal(false);
  error   = signal<string | null>(null);
  success = signal<string | null>(null);

  showForm  = signal(false);
  editingCode = signal<string | null>(null);

  form: Partial<Banque> = { actif: true };

  constructor(private svc: BanqueService) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.svc.getAll().subscribe({
      next: data => {
        this.all.set(data);
        this.pageBanque.set(0);
        this.loading.set(false);
      },
      error: e   => { this.error.set('Erreur : ' + (e.error?.message ?? e.message)); this.loading.set(false); }
    });
  }

  resetFilters(): void {
    this.search.set('');
    this.filtreActif.set('');
    this.pageBanque.set(0);
  }

  onSearchChange(v: string): void {
    this.search.set(v);
    this.pageBanque.set(0);
  }

  onFiltreActifChange(v: string): void {
    this.filtreActif.set(v);
    this.pageBanque.set(0);
  }

  onBanquePageChange(p: number): void {
    this.pageBanque.set(listClampPage(p, this.banques().length, this.pageSize));
  }

  openNew(): void {
    this.form = { actif: true };
    this.editingCode.set(null);
    this.showForm.set(true);
    this.error.set(null);
    this.success.set(null);
  }

  openEdit(b: Banque): void {
    this.form = { ...b };
    this.editingCode.set(b.codeBanque);
    this.showForm.set(true);
    this.error.set(null);
    this.success.set(null);
  }

  cancel(): void {
    this.showForm.set(false);
    this.form = { actif: true };
  }

  submit(): void {
    if (!this.form.codeBanque || !this.form.nom) {
      this.error.set('Le code et le nom sont obligatoires.');
      return;
    }
    this.saving.set(true);
    this.error.set(null);

    const code = this.editingCode();
    const call = code
      ? this.svc.update(code, this.form)
      : this.svc.create(this.form);

    call.subscribe({
      next: () => {
        this.saving.set(false);
        this.success.set(code ? 'Banque mise à jour.' : 'Banque créée.');
        this.showForm.set(false);
        this.load();
      },
      error: e => {
        this.saving.set(false);
        this.error.set('Erreur : ' + (e.error?.message ?? e.message));
      }
    });
  }

  delete(code: string): void {
    if (!confirm(`Supprimer la banque ${code} ?`)) return;
    this.svc.delete(code).subscribe({
      next: () => { this.success.set('Banque supprimée.'); this.load(); },
      error: e => this.error.set('Erreur : ' + (e.error?.message ?? e.message))
    });
  }
}
