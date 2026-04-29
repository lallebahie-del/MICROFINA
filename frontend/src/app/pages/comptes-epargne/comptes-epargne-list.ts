import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ComptesEpargneService, CompteEpargne } from '../../services/comptes-epargne.service';

@Component({
  selector: 'app-comptes-epargne-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './comptes-epargne-list.html'
})
export class ComptesEpargneListComponent implements OnInit {
  comptes: CompteEpargne[] = [];
  agenceFilter = '';
  loading = false;

  constructor(private svc: ComptesEpargneService) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading = true;
    this.svc.getAll(this.agenceFilter || undefined).subscribe({
      next: data => { this.comptes = data; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  bloquer(id: number): void {
    if (!confirm('Bloquer ce compte épargne ?')) return;
    this.svc.bloquer(id).subscribe({ next: () => this.load() });
  }
}
