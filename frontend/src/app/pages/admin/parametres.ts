import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminParametresService, Parametre } from '../../services/admin-parametres.service';
import { AgencesService, Agence } from '../../services/agences.service';

@Component({
  selector: 'app-admin-parametres',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './parametres.html'
})
export class AdminParametresComponent implements OnInit {

  items    = signal<Parametre[]>([]);
  agences  = signal<Agence[]>([]);
  loading  = signal(false);
  saving   = signal(false);
  error    = signal<string | null>(null);
  success  = signal<string | null>(null);

  showForm  = signal(false);
  editingId = signal<number | null>(null);

  form: Parametre = {};

  constructor(
    private svc: AdminParametresService,
    private agencesSvc: AgencesService
  ) {}

  ngOnInit(): void {
    this.load();
    this.agencesSvc.getAll(true).subscribe({
      next: list => this.agences.set(list)
    });
  }

  load(): void {
    this.loading.set(true);
    this.svc.findAll().subscribe({
      next: list => { this.items.set(list); this.loading.set(false); },
      error: e   => { this.error.set('Erreur : ' + (e.error?.message ?? e.message)); this.loading.set(false); }
    });
  }

  openNew(): void {
    this.form = { useMultidevise: 'N' };
    this.editingId.set(null);
    this.showForm.set(true);
    this.error.set(null);
    this.success.set(null);
  }

  openEdit(p: Parametre): void {
    this.form = { ...p };
    this.editingId.set(p.idParametre ?? null);
    this.showForm.set(true);
    this.error.set(null);
    this.success.set(null);
  }

  cancel(): void {
    this.showForm.set(false);
    this.form = {};
  }

  submit(): void {
    this.saving.set(true);
    this.error.set(null);

    const id = this.editingId();
    const call = id != null
      ? this.svc.update(id, this.form)
      : this.svc.create(this.form);

    call.subscribe({
      next: () => {
        this.saving.set(false);
        this.success.set(id != null ? 'Paramètre mis à jour.' : 'Paramètre créé.');
        this.showForm.set(false);
        this.load();
      },
      error: e => {
        this.saving.set(false);
        this.error.set('Erreur : ' + (e.error?.message ?? e.message));
      }
    });
  }

  delete(id?: number): void {
    if (id == null) return;
    if (!confirm(`Supprimer le paramètre #${id} ?`)) return;
    this.svc.delete(id).subscribe({
      next: () => { this.success.set('Paramètre supprimé.'); this.load(); },
      error: e => this.error.set('Erreur suppression : ' + (e.error?.message ?? e.message))
    });
  }

  agenceLabel(code?: string): string {
    if (!code) return '— global —';
    const a = this.agences().find(x => x.codeAgence === code);
    return a ? `${a.codeAgence} — ${a.nomAgence}` : code;
  }
}
