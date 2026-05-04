import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { BanqueService, Banque } from '../../services/banque.service';

@Component({
  selector: 'app-banques-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './banques-list.html'
})
export class BanquesListComponent implements OnInit {
  banques = signal<Banque[]>([]);
  loading = signal(false);
  error = signal<string | null>(null);

  constructor(private banqueService: BanqueService) {}

  ngOnInit(): void {
    this.loading.set(true);
    this.banqueService.getAll().subscribe({
      next: data => { this.banques.set(data); this.loading.set(false); },
      error: e => { this.error.set('Erreur chargement banques'); this.loading.set(false); }
    });
  }

  supprimer(code: string): void {
    if (!confirm(`Supprimer la banque ${code} ?`)) return;
    this.banqueService.delete(code).subscribe({
      next: () => this.banques.update(list => list.filter(b => b.codeBanque !== code)),
      error: () => this.error.set('Erreur lors de la suppression')
    });
  }
}
