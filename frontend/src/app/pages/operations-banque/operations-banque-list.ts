import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { OperationsBanqueService, OperationBanque } from '../../services/operations-banque.service';

@Component({
  selector: 'app-operations-banque-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './operations-banque-list.html'
})
export class OperationsBanqueListComponent implements OnInit {
  operations: OperationBanque[] = [];
  agenceFilter = '';
  page = 0;
  totalPages = 0;
  loading = false;

  constructor(private svc: OperationsBanqueService) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading = true;
    this.svc.getAll(this.agenceFilter || undefined, this.page).subscribe({
      next: (resp: any) => {
        this.operations = resp.content ?? resp;
        this.totalPages = resp.totalPages ?? 0;
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  prev(): void { if (this.page > 0) { this.page--; this.load(); } }
  next(): void { if (this.page < this.totalPages - 1) { this.page++; this.load(); } }
}
