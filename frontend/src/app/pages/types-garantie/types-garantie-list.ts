import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TypesGarantieService, TypeGarantie } from '../../services/types-garantie.service';

@Component({
  selector: 'app-types-garantie-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './types-garantie-list.html'
})
export class TypesGarantieListComponent implements OnInit {

  items   = signal<TypeGarantie[]>([]);
  loading = signal(false);
  saving  = signal(false);
  error   = signal<string | null>(null);
  success = signal<string | null>(null);

  showForm    = signal(false);
  editingCode = signal<string | null>(null);

  form: TypeGarantie = { code: '', libelle: '', actif: true };

  constructor(private svc: TypesGarantieService) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.svc.findAll().subscribe({
      next: list => { this.items.set(list); this.loading.set(false); },
      error: e   => { this.error.set('Erreur : ' + e.message); this.loading.set(false); }
    });
  }

  openNew(): void {
    this.form = { code: '', libelle: '', actif: true };
    this.editingCode.set(null);
    this.showForm.set(true);
    this.error.set(null);
    this.success.set(null);
  }

  openEdit(t: TypeGarantie): void {
    this.form = { ...t };
    this.editingCode.set(t.code);
    this.showForm.set(true);
    this.error.set(null);
    this.success.set(null);
  }

  cancel(): void {
    this.showForm.set(false);
    this.form = { code: '', libelle: '', actif: true };
  }

  submit(): void {
    if (!this.form.code || !this.form.libelle) {
      this.error.set('Le code et le libellé sont obligatoires.');
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
        this.success.set(code ? 'Type mis à jour.' : 'Type créé.');
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
    if (!confirm(`Supprimer le type de garantie ${code} ?`)) return;
    this.svc.delete(code).subscribe({
      next: () => { this.success.set('Type supprimé.'); this.load(); },
      error: e => this.error.set('Erreur suppression : ' + (e.error?.message ?? e.message))
    });
  }
}
