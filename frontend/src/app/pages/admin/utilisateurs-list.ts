import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminService, Utilisateur } from '../../services/admin.service';

@Component({
  selector: 'app-utilisateurs-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './utilisateurs-list.html'
})
export class UtilisateursListComponent implements OnInit {
  utilisateurs = signal<Utilisateur[]>([]);
  loading = signal(false);
  error = signal<string | null>(null);

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.loading.set(true);
    this.adminService.getUtilisateurs().subscribe({
      next: data => { this.utilisateurs.set(data); this.loading.set(false); },
      error: () => { this.error.set('Erreur chargement utilisateurs'); this.loading.set(false); }
    });
  }

  desactiver(id: number): void {
    if (!confirm('Désactiver cet utilisateur ?')) return;
    this.adminService.desactiverUtilisateur(id).subscribe({
      next: () => this.utilisateurs.update(list => list.map(u => u.id === id ? {...u, actif: false} : u)),
      error: () => this.error.set('Erreur désactivation')
    });
  }
}
