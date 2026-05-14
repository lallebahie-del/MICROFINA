import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService, Role } from '../../services/admin.service';
import { PaginationBarComponent } from '../../components/pagination-bar/pagination-bar.component';
import { DEFAULT_LIST_PAGE_SIZE, listClampPage, listSlice } from '../../shared/list-pagination';

@Component({
  selector: 'app-roles-list',
  standalone: true,
  imports: [CommonModule, FormsModule, PaginationBarComponent],
  templateUrl: './roles-list.html'
})
export class RolesListComponent implements OnInit {
  items   = signal<Role[]>([]);
  page    = signal(0);
  readonly pageSize = DEFAULT_LIST_PAGE_SIZE;

  clampedRolesPage = computed(() =>
    listClampPage(this.page(), this.items().length, this.pageSize)
  );

  pagedRoles = computed(() =>
    listSlice(this.items(), this.clampedRolesPage(), this.pageSize)
  );
  loading = signal(false);
  saving  = signal(false);
  error   = signal<string | null>(null);
  success = signal<string | null>(null);

  showForm  = signal(false);
  editingId = signal<number | null>(null);

  form: Partial<Role> = { codeRole: '', libelle: '' };

  constructor(private svc: AdminService) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.svc.getRoles().subscribe({
      next: data => { this.items.set(data); this.page.set(0); this.loading.set(false); },
      error: e   => { this.error.set('Erreur : ' + (e.error?.message ?? e.message)); this.loading.set(false); }
    });
  }

  openNew(): void {
    this.form = { codeRole: '', libelle: '' };
    this.editingId.set(null);
    this.showForm.set(true);
    this.error.set(null);
    this.success.set(null);
  }

  openEdit(r: Role): void {
    this.form = { ...r };
    this.editingId.set(r.id);
    this.showForm.set(true);
    this.error.set(null);
    this.success.set(null);
  }

  cancel(): void {
    this.showForm.set(false);
    this.form = { codeRole: '', libelle: '' };
  }

  submit(): void {
    if (!this.form.codeRole || !this.form.libelle) {
      this.error.set('Le code et le libellé sont obligatoires.');
      return;
    }
    this.saving.set(true);
    this.error.set(null);

    const id = this.editingId();
    const call = id
      ? this.svc.updateRole(id, this.form)
      : this.svc.createRole(this.form);

    call.subscribe({
      next: () => {
        this.saving.set(false);
        this.success.set(id ? 'Rôle mis à jour.' : 'Rôle créé.');
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
    if (!confirm(`Supprimer le rôle ${code} ?`)) return;
    this.svc.deleteRole(id).subscribe({
      next: () => { this.success.set('Rôle supprimé.'); this.load(); },
      error: e => this.error.set('Erreur : ' + (e.error?.message ?? e.message))
    });
  }

  onRolesPageChange(p: number): void {
    this.page.set(listClampPage(p, this.items().length, this.pageSize));
  }
}
