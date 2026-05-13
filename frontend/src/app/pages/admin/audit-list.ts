import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService, JournalAudit } from '../../services/admin.service';

@Component({
  selector: 'app-audit-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './audit-list.html'
})
export class AuditListComponent implements OnInit {
  private all = signal<JournalAudit[]>([]);
  loading = signal(false);
  error   = signal<string | null>(null);
  selected = signal<JournalAudit | null>(null);

  search       = signal('');
  filterAction = signal<string>('');
  filterEntite = signal<string>('');

  filtered = computed<JournalAudit[]>(() => {
    const q = this.search().trim().toLowerCase();
    const fA = this.filterAction();
    const fE = this.filterEntite();
    return this.all().filter(l => {
      if (fA && l.action !== fA) return false;
      if (fE && l.entite !== fE) return false;
      if (!q) return true;
      return (l.utilisateur?.toLowerCase().includes(q))
          || (l.idEntite?.toLowerCase().includes(q) ?? false)
          || (l.entite?.toLowerCase().includes(q) ?? false);
    });
  });

  actions = computed<string[]>(() =>
    Array.from(new Set(this.all().map(l => l.action).filter(a => !!a))).sort()
  );

  entites = computed<string[]>(() =>
    Array.from(new Set(this.all().map(l => l.entite).filter(e => !!e))).sort()
  );

  constructor(private adminService: AdminService) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.adminService.getAuditLog().subscribe({
      next: data => { this.all.set(data); this.loading.set(false); },
      error: e   => { this.error.set('Erreur : ' + (e.error?.message ?? e.message)); this.loading.set(false); }
    });
  }

  resetFilters(): void {
    this.search.set('');
    this.filterAction.set('');
    this.filterEntite.set('');
  }

  actionClass(action: string): string {
    if (!action) return 'badge badge-info';
    const a = action.toUpperCase();
    if (a.includes('DELETE') || a.includes('REJECT') || a.includes('SUPPR')) return 'badge badge-danger';
    if (a.includes('CREATE') || a.includes('VALIDATE') || a.includes('CREE')) return 'badge badge-success';
    if (a.includes('UPDATE') || a.includes('MODIF')) return 'badge badge-warning';
    if (a.includes('LOGIN') || a.includes('LOGOUT')) return 'badge badge-info';
    return 'badge badge-primary';
  }

  showDetail(l: JournalAudit): void { this.selected.set(l); }
  closeDetail(): void { this.selected.set(null); }
}
