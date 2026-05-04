import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ComptesEpargneService, CompteEpargne } from '../../services/comptes-epargne.service';

@Component({
  selector: 'app-epargne-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './epargne-list.html'
})
export class EpargneListComponent implements OnInit {
  comptes: CompteEpargne[] = [];
  agenceFilter = '';
  loading = false;
  selectedId: number | null = null;
  montant = 0;
  libelle = '';

  constructor(private svc: ComptesEpargneService) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading = true;
    this.svc.getAll(this.agenceFilter || undefined).subscribe({
      next: data => { this.comptes = data; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  depot(id: number): void {
    this.svc.depot(id, this.montant, this.libelle).subscribe({ next: () => this.load() });
  }

  retrait(id: number): void {
    this.svc.retrait(id, this.montant, this.libelle).subscribe({ next: () => this.load() });
  }
}
