import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AgencesService, Agence } from '../../services/agences.service';

@Component({
  selector: 'app-agences-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './agences-list.html'
})
export class AgencesListComponent implements OnInit {
  private all: Agence[] = [];
  agences: Agence[] = [];

  search: string = '';
  filtreActif: string = '';
  filtreType: string = '';

  loading  = signal(false);
  saving   = signal(false);
  error    = signal<string | null>(null);
  success  = signal<string | null>(null);

  showForm  = signal(false);
  editingCode = signal<string | null>(null);

  form: Agence = { codeAgence: '', actif: true };

  constructor(private svc: AgencesService) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.svc.getAll().subscribe({
      next: data => { this.all = data; this.applyFilters(); this.loading.set(false); },
      error: e   => { this.error.set('Erreur de chargement : ' + e.message); this.loading.set(false); }
    });
  }

  applyFilters(): void {
    const q = this.search.trim().toLowerCase();
    this.agences = this.all.filter(a => {
      if (this.filtreActif !== '' && (this.filtreActif === 'true') !== !!a.actif) return false;
      if (this.filtreType === 'siege'  && a.isSiege !== '1') return false;
      if (this.filtreType === 'agence' && a.isSiege === '1') return false;
      if (!q) return true;
      return (a.codeAgence?.toLowerCase().includes(q))
          || (a.nomAgence?.toLowerCase().includes(q) ?? false)
          || (a.nomCourt?.toLowerCase().includes(q) ?? false)
          || (a.nomPrenomChefAgence?.toLowerCase().includes(q) ?? false)
          || (a.chefAgence?.toLowerCase().includes(q) ?? false);
    });
  }

  resetFilters(): void {
    this.search = '';
    this.filtreActif = '';
    this.filtreType = '';
    this.applyFilters();
  }

  openNew(): void {
    this.form = { codeAgence: '', actif: true, isSiege: '0' };
    this.editingCode.set(null);
    this.showForm.set(true);
    this.error.set(null);
    this.success.set(null);
  }

  openEdit(a: Agence): void {
    this.form = { ...a };
    this.editingCode.set(a.codeAgence);
    this.showForm.set(true);
    this.error.set(null);
    this.success.set(null);
  }

  cancel(): void {
    this.showForm.set(false);
    this.form = { codeAgence: '', actif: true };
  }

  submit(): void {
    if (!this.form.codeAgence || !this.form.nomAgence) {
      this.error.set('Le code et le nom de l\'agence sont obligatoires.');
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
        this.success.set(code ? 'Agence mise à jour.' : 'Agence créée.');
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
    if (!confirm(`Supprimer l'agence ${code} ?`)) return;
    this.svc.delete(code).subscribe({
      next: () => { this.success.set('Agence supprimée.'); this.load(); },
      error: e => this.error.set('Erreur suppression : ' + (e.error?.message ?? e.message))
    });
  }
}
