import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ComptabiliteService, Ecriture } from '../../services/comptabilite.service';

@Component({
  selector: 'app-comptabilite-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './comptabilite-list.html'
})
export class ComptabiliteListComponent implements OnInit {
  ecritures: Ecriture[] = [];
  agenceFilter = '';
  loading = false;
  error = '';

  constructor(private comptabiliteService: ComptabiliteService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.comptabiliteService.getEcritures(this.agenceFilter || undefined).subscribe({
      next: data => { this.ecritures = data; this.loading = false; },
      error: () => { this.error = 'Erreur lors du chargement.'; this.loading = false; }
    });
  }

  lettrer(id: number): void {
    const code = prompt('Code de lettrage (max 10 caract.) :');
    if (!code) return;
    this.comptabiliteService.lettrer(id, code).subscribe({ next: () => this.load() });
  }
}
