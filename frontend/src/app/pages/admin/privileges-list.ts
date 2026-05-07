import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService, Privilege } from '../../services/admin.service';

@Component({
  selector: 'app-privileges-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './privileges-list.html'
})
export class PrivilegesListComponent implements OnInit {
  private all = signal<Privilege[]>([]);

  search = signal('');
  filtreModule = signal<string>('');

  privileges = computed<Privilege[]>(() => {
    const q = this.search().trim().toLowerCase();
    const fm = this.filtreModule();
    return this.all().filter(p => {
      if (fm && p.module !== fm) return false;
      if (!q) return true;
      return p.codePrivilege.toLowerCase().includes(q)
          || p.libelle.toLowerCase().includes(q);
    });
  });

  modules = computed<string[]>(() =>
    Array.from(new Set(this.all().map(p => p.module))).sort()
  );

  loading = signal(false);
  saving  = signal(false);
  error   = signal<string | null>(null);
  success = signal<string | null>(null);

  showForm  = signal(false);
  editingId = signal<number | null>(null);

  form: Partial<Privilege> = { codePrivilege: '', libelle: '', module: '' };

  constructor(private svc: AdminService) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.svc.getPrivileges().subscribe({
      next: data => { this.all.set(data); this.loading.set(false); },
      error: e   => { this.error.set('Erreur : ' + (e.error?.message ?? e.message)); this.loading.set(false); }
    });
  }

  resetFilters(): void {
    this.search.set('');
    this.filtreModule.set('');
  }

  openNew(): void {
    this.form = { codePrivilege: '', libelle: '', module: '' };
    this.editingId.set(null);
    this.showForm.set(true);
    this.error.set(null);
    this.success.set(null);
  }

  openEdit(p: Privilege): void {
    this.form = { ...p };
    this.editingId.set(p.id);
    this.showForm.set(true);
    this.error.set(null);
    this.success.set(null);
  }

  cancel(): void {
    this.showForm.set(false);
    this.form = { codePrivilege: '', libelle: '', module: '' };
  }

  submit(): void {
    if (!this.form.codePrivilege || !this.form.libelle || !this.form.module) {
      this.error.set('Code, libellé et module sont obligatoires.');
      return;
    }
    this.saving.set(true);
    this.error.set(null);

    const id = this.editingId();
    const call = id
      ? this.svc.updatePrivilege(id, this.form)
      : this.svc.createPrivilege(this.form);

    call.subscribe({
      next: () => {
        this.saving.set(false);
        this.success.set(id ? 'Privilège mis à jour.' : 'Privilège créé.');
        this.showForm.set(false);
        this.load();
      },
      error: e => {
        this.saving.set(false);
        this.error.set('Erreur : ' + (e.error?.message ?? e.message));
      }
    });
  }

  delete(id: number, code: string): void {
    if (!confirm(`Supprimer le privilège ${code} ?`)) return;
    this.svc.deletePrivilege(id).subscribe({
      next: () => { this.success.set('Privilège supprimé.'); this.load(); },
      error: e => this.error.set('Erreur : ' + (e.error?.message ?? e.message))
    });
  }
}
