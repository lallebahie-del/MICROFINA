import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProduitsIslamicService, ProduitIslamic } from '../../services/produits-islamic.service';

@Component({
  selector: 'app-produits-islamic-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './produits-islamic-list.html'
})
export class ProduitsIslamicListComponent implements OnInit {

  items   = signal<ProduitIslamic[]>([]);
  loading = signal(false);
  saving  = signal(false);
  error   = signal<string | null>(null);
  success = signal<string | null>(null);

  showForm    = signal(false);
  editingCode = signal<string | null>(null);

  form: ProduitIslamic = { codeProduit: '', actif: 1 };

  constructor(private svc: ProduitsIslamicService) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.svc.findAll().subscribe({
      next: list => { this.items.set(list); this.loading.set(false); },
      error: e   => { this.error.set('Erreur : ' + e.message); this.loading.set(false); }
    });
  }

  openNew(): void {
    this.form = { codeProduit: '', actif: 1 };
    this.editingCode.set(null);
    this.showForm.set(true);
    this.error.set(null);
    this.success.set(null);
  }

  openEdit(p: ProduitIslamic): void {
    this.form = { ...p };
    this.editingCode.set(p.codeProduit);
    this.showForm.set(true);
    this.error.set(null);
    this.success.set(null);
  }

  cancel(): void {
    this.showForm.set(false);
    this.form = { codeProduit: '', actif: 1 };
  }

  submit(): void {
    if (!this.form.codeProduit) {
      this.error.set('Le code est obligatoire.');
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
        this.success.set(code ? 'Produit mis à jour.' : 'Produit créé.');
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
    if (!confirm(`Supprimer le produit ${code} ?`)) return;
    this.svc.delete(code).subscribe({
      next: () => { this.success.set('Produit supprimé.'); this.load(); },
      error: e => this.error.set('Erreur suppression : ' + (e.error?.message ?? e.message))
    });
  }

  formatPct(v?: number): string {
    if (v == null) return '—';
    return (v * 100).toFixed(2) + ' %';
  }
}
