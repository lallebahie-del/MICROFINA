import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminService } from '../../services/admin.service';

@Component({
  selector: 'app-backup',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './backup.html'
})
export class BackupComponent implements OnInit {
  sauvegardes = signal<string[]>([]);
  loading = signal(false);
  backupLoading = signal(false);
  success = signal<string | null>(null);
  error = signal<string | null>(null);

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.loading.set(true);
    this.adminService.listerSauvegardes().subscribe({
      next: data => { this.sauvegardes.set(data); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  lancerBackup(): void {
    if (!confirm('Lancer une sauvegarde complète de la base de données ?')) return;
    this.backupLoading.set(true);
    this.success.set(null);
    this.error.set(null);
    this.adminService.backup().subscribe({
      next: r => {
        this.success.set(`Sauvegarde créée : ${r.fichier}`);
        this.backupLoading.set(false);
        this.sauvegardes.update(list => [r.fichier.split(/[\\/]/).pop() ?? r.fichier, ...list]);
      },
      error: () => { this.error.set('Erreur lors de la sauvegarde'); this.backupLoading.set(false); }
    });
  }
}
