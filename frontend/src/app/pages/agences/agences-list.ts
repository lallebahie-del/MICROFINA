import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AgencesService, Agence } from '../../services/agences.service';

@Component({
  selector: 'app-agences-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './agences-list.html'
})
export class AgencesListComponent implements OnInit {
  agences: Agence[] = [];
  filtreActif: string = '';
  loading = false;

  constructor(private svc: AgencesService) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading = true;
    const actif = this.filtreActif === '' ? undefined : this.filtreActif === 'true';
    this.svc.getAll(actif).subscribe({
      next: data => { this.agences = data; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }
}
