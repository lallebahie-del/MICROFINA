import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { GarantiesService, Garantie } from '../../services/garanties.service';

@Component({
  selector: 'app-garanties-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './garanties-list.html'
})
export class GarantiesListComponent implements OnInit {
  garanties: Garantie[] = [];
  creditIdFilter = '';
  loading = false;
  error = '';

  constructor(private svc: GarantiesService) {}

  ngOnInit(): void {
    if (this.creditIdFilter) this.load();
  }

  load(): void {
    if (!this.creditIdFilter) return;
    this.loading = true;
    this.svc.getByCreditId(+this.creditIdFilter).subscribe({
      next: data => { this.garanties = data; this.loading = false; },
      error: () => { this.error = 'Erreur de chargement.'; this.loading = false; }
    });
  }

  liberer(idGarantie: number): void {
    if (!confirm('Confirmer la libération de la garantie ?')) return;
    this.svc.liberer(idGarantie).subscribe({ next: () => this.load() });
  }
}
