import { Component, OnInit, signal, computed } from '@angular/core';
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
  lignes  = signal<JournalLigne[]>([]);
  agence  = signal<string>('');
  date    = signal<string>('');
  loading = signal(false);
  error   = signal<string | null>(null);

  totalDebit  = computed<number>(() =>
    this.lignes().reduce((s, l) => s + (l.debit  ?? 0), 0)
  );
  totalCredit = computed<number>(() =>
    this.lignes().reduce((s, l) => s + (l.credit ?? 0), 0)
  );

  constructor(private svc: ComptabiliteService) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.svc.getJournal(this.agence() || undefined, this.date() || undefined).subscribe({
      next: data => { this.lignes.set(data); this.loading.set(false); },
      error: e   => { this.error.set('Erreur : ' + (e.error?.message ?? e.message)); this.loading.set(false); }
    });
  }

  reset(): void {
    this.agence.set('');
    this.date.set('');
    this.load();
  }
}
