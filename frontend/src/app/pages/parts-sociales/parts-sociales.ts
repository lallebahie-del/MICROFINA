import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PartsSocialesService, ProduitPartSociale } from '../../services/parts-sociales.service';

@Component({
  selector: 'app-parts-sociales',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './parts-sociales.html'
})
export class PartsSocialesComponent implements OnInit {

  items    = signal<ProduitPartSociale[]>([]);
  loading  = signal(false);
  saving   = signal(false);
  error    = signal<string | null>(null);
  success  = signal<string | null>(null);

  showForm  = signal(false);
  editingId = signal<string | null>(null);

  form: Partial<ProduitPartSociale> = { actif: 1 };

  constructor(private service: PartsSocialesService) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.service.findAll().subscribe({
      next:  list => { this.items.set(list); this.loading.set(false); },
      error: e    => { this.error.set('Erreur : ' + e.message); this.loading.set(false); }
    });
  }

  openNew(): void {
    this.form = { actif: 1 };
    this.editingId.set(null);
    this.showForm.set(true);
    this.error.set(null);
    this.success.set(null);
  }

  openEdit(item: ProduitPartSociale): void {
    this.form = { ...item };
    this.editingId.set(item.numProduit);
    this.showForm.set(true);
    this.error.set(null);
    this.success.set(null);
  }

  cancel(): void {
    this.showForm.set(false);
    this.form = { actif: 1 };
  }

  submit(): void {
    if (!this.form.numProduit || !this.form.nomProduit) {
      this.error.set('Le code et le nom du produit sont obligatoires.');
      return;
    }
    this.saving.set(true);
    this.error.set(null);

    const call = this.editingId()
      ? this.service.update(this.editingId()!, this.form as ProduitPartSociale)
      : this.service.create(this.form as ProduitPartSociale);

    call.subscribe({
      next: () => {
        this.saving.set(false);
        this.success.set(this.editingId() ? 'Produit mis à jour.' : 'Produit créé.');
        this.showForm.set(false);
        this.load();
      },
      error: e => {
        this.saving.set(false);
        this.error.set('Erreur : ' + (e.error?.message ?? e.message));
      }
    });
  }

  delete(id: string): void {
    if (!confirm('Supprimer ce produit ?')) return;
    this.service.delete(id).subscribe({
      next:  () => this.load(),
      error: e  => this.error.set('Erreur suppression : ' + e.message)
    });
  }

  formatMontant(v?: number): string {
    if (v == null) return '—';
    return new Intl.NumberFormat('fr-MR', { style: 'currency', currency: 'MRU',
      minimumFractionDigits: 0 }).format(v);
  }
}
