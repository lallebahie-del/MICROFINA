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
  restoringFile = signal<string | null>(null);
  success = signal<string | null>(null);
  error = signal<string | null>(null);

  constructor(private adminService: AdminService) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.adminService.listerSauvegardes().subscribe({
      next: data => { this.sauvegardes.set(data); this.loading.set(false); },
      error: e   => { this.error.set('Erreur : ' + (e.error?.message ?? e.message)); this.loading.set(false); }
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
        this.load();
      },
      error: e => {
        this.error.set('Erreur sauvegarde : ' + (e.error?.message ?? e.message));
        this.backupLoading.set(false);
      }
    });
  }

  restaurer(filename: string): void {
    if (!confirm(`Restaurer la base depuis ${filename} ?\n\n⚠ Cette opération va ÉCRASER les données actuelles. Vérifie que tu as une sauvegarde récente.`)) return;
    this.restoringFile.set(filename);
    this.success.set(null);
    this.error.set(null);
    this.adminService.restaurerSauvegarde(filename).subscribe({
      next: r => {
        this.restoringFile.set(null);
        this.success.set(`Restauration ${r.statut} depuis ${r.fichier}`);
      },
      error: e => {
        this.restoringFile.set(null);
        this.error.set('Erreur restauration : ' + (e.error?.message ?? e.message));
      }
    });
  }
}
