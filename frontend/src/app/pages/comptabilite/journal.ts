import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ComptabiliteService, JournalLigne } from '../../services/comptabilite.service';

@Component({
  selector: 'app-journal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './journal.html'
})
export class JournalComponent implements OnInit {
  lignes: JournalLigne[] = [];
  agence = '';
  date = '';
  loading = false;

  constructor(private svc: ComptabiliteService) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading = true;
    this.svc.getJournal(this.agence || undefined, this.date || undefined).subscribe({
      next: data => { this.lignes = data; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }
}
