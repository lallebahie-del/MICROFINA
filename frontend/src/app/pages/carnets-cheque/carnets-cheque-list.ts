import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CarnetsChequeService, CarnetCheque } from '../../services/carnets-cheque.service';

@Component({
  selector: 'app-carnets-cheque-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './carnets-cheque-list.html'
})
export class CarnetsChequeListComponent implements OnInit {
  carnets: CarnetCheque[] = [];
  agenceFilter = '';
  loading = false;

  constructor(private svc: CarnetsChequeService) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading = true;
    this.svc.getAll(this.agenceFilter || undefined).subscribe({
      next: data => { this.carnets = data; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  bloquer(id: number): void {
    if (!confirm('Bloquer ce carnet de chèques ?')) return;
    this.svc.bloquer(id).subscribe({ next: () => this.load() });
  }
}
