import { Component, OnInit, signal } from '@angular/core';
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
  logs = signal<JournalAudit[]>([]);
  filtered = signal<JournalAudit[]>([]);
  loading = signal(false);
  filterUser = '';
  filterAction = '';

  readonly actions = ['', 'CREATE', 'UPDATE', 'DELETE', 'LOGIN', 'LOGOUT'];

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.loading.set(true);
    this.adminService.getAuditLog().subscribe({
      next: data => { this.logs.set(data); this.filtered.set(data); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  applyFilter(): void {
    this.filtered.set(this.logs().filter(l =>
      (!this.filterUser || l.utilisateur?.toLowerCase().includes(this.filterUser.toLowerCase())) &&
      (!this.filterAction || l.action === this.filterAction)
    ));
  }

  actionClass(action: string): string {
    return action === 'DELETE' ? 'badge badge-danger'
         : action === 'CREATE' ? 'badge badge-success'
         : action === 'LOGIN' ? 'badge badge-info'
         : 'badge badge-secondary';
  }
}
