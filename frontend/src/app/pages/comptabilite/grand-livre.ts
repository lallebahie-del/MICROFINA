import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ComptabiliteService, GrandLivreLigne } from '../../services/comptabilite.service';

@Component({
  selector: 'app-grand-livre',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './grand-livre.html'
})
export class GrandLivreComponent implements OnInit {
  lignes: GrandLivreLigne[] = [];
  agence = '';
  compte = '';
  loading = false;

  constructor(private svc: ComptabiliteService) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading = true;
    this.svc.getGrandLivre(this.agence || undefined, this.compte || undefined).subscribe({
      next: data => { this.lignes = data; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }
}
