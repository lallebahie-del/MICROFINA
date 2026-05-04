import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { BudgetService, Budget } from '../../services/budget.service';

@Component({
  selector: 'app-budget-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './budget-list.html'
})
export class BudgetListComponent implements OnInit {
  budgets = signal<Budget[]>([]);
  loading = signal(false);
  error = signal<string | null>(null);
  success = signal<string | null>(null);

  constructor(private budgetService: BudgetService) {}

  ngOnInit(): void {
    this.loading.set(true);
    this.budgetService.getAll().subscribe({
      next: data => { this.budgets.set(data); this.loading.set(false); },
      error: () => { this.error.set('Erreur chargement budgets'); this.loading.set(false); }
    });
  }

  valider(id: number): void {
    if (!confirm('Valider ce budget ?')) return;
    this.budgetService.valider(id).subscribe({
      next: updated => {
        this.budgets.update(list => list.map(b => b.id === id ? updated : b));
        this.success.set('Budget validé');
      },
      error: e => this.error.set('Erreur lors de la validation')
    });
  }

  cloturer(id: number): void {
    if (!confirm('Clôturer ce budget ? Cette action est irréversible.')) return;
    this.budgetService.cloturer(id).subscribe({
      next: updated => {
        this.budgets.update(list => list.map(b => b.id === id ? updated : b));
        this.success.set('Budget clôturé');
      },
      error: e => this.error.set('Erreur lors de la clôture')
    });
  }

  statutClass(statut: string): string {
    return statut === 'VALIDE' ? 'badge badge-success'
         : statut === 'CLOTURE' ? 'badge badge-danger'
         : 'badge badge-secondary';
  }
}
