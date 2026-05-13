import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ComptabiliteService, BalanceCompte } from '../../services/comptabilite.service';

@Component({
  selector: 'app-balance',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './balance.html'
})
export class BalanceComponent implements OnInit {
  balance = signal<BalanceCompte[]>([]);
  agence  = signal<string>('');
  loading = signal(false);
  error   = signal<string | null>(null);

  totalDebit  = computed<number>(() =>
    this.balance().reduce((s, l) => s + (l.totalDebit  ?? 0), 0)
  );
  totalCredit = computed<number>(() =>
    this.balance().reduce((s, l) => s + (l.totalCredit ?? 0), 0)
  );

  constructor(private svc: ComptabiliteService) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.svc.getBalance(this.agence() || undefined).subscribe({
      next: data => { this.balance.set(data); this.loading.set(false); },
      error: e   => { this.error.set('Erreur : ' + (e.error?.message ?? e.message)); this.loading.set(false); }
    });
  }

  reset(): void {
    this.agence.set('');
    this.load();
  }
}
