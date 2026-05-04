import { Component, OnInit } from '@angular/core';
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
  balance: BalanceCompte[] = [];
  agence = '';
  loading = false;

  constructor(private svc: ComptabiliteService) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading = true;
    this.svc.getBalance(this.agence || undefined).subscribe({
      next: data => { this.balance = data; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  get totalDebit(): number {
    return this.balance.reduce((s, l) => s + (l.totalDebit ?? 0), 0);
  }

  get totalCredit(): number {
    return this.balance.reduce((s, l) => s + (l.totalCredit ?? 0), 0);
  }
}
