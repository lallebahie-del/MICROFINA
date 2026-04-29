import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { OperationsCaisseService, OperationCaisse } from '../../services/operations-caisse.service';

@Component({
  selector: 'app-operations-caisse-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './operations-caisse-list.html'
})
export class OperationsCaisseListComponent implements OnInit {
  operations: OperationCaisse[] = [];
  agenceFilter = '';
  page = 0;
  totalPages = 0;
  loading = false;

  constructor(private svc: OperationsCaisseService) {}

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
