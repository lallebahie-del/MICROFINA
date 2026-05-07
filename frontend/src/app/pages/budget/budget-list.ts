import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BudgetService, Budget } from '../../services/budget.service';
import { AgencesService, Agence } from '../../services/agences.service';

@Component({
  selector: 'app-budget-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './budget-list.html'
})
export class BudgetListComponent implements OnInit {
  budgets = signal<Budget[]>([]);
  agences = signal<Agence[]>([]);

  loading = signal(false);
  saving  = signal(false);
  error   = signal<string | null>(null);
  success = signal<string | null>(null);

  showForm  = signal(false);
  editingId = signal<number | null>(null);

  form: Partial<Budget> = {
    exerciceFiscal: new Date().getFullYear(),
    montantTotalRecettes: 0,
    montantTotalDepenses: 0,
    statut: 'BROUILLON'
  };

  constructor(
    private budgetService: BudgetService,
    private agencesSvc: AgencesService
  ) {}

  ngOnInit(): void {
    this.load();
    this.agencesSvc.getAll(true).subscribe({ next: list => this.agences.set(list) });
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.budgetService.getAll().subscribe({
      next: data => { this.budgets.set(data); this.loading.set(false); },
      error: e   => { this.error.set('Erreur : ' + (e.error?.message ?? e.message)); this.loading.set(false); }
    });
  }

  openNew(): void {
    this.form = {
      exerciceFiscal: new Date().getFullYear(),
      montantTotalRecettes: 0,
      montantTotalDepenses: 0,
      statut: 'BROUILLON'
    };
    this.editingId.set(null);
    this.showForm.set(true);
    this.error.set(null);
    this.success.set(null);
  }

  openEdit(b: Budget): void {
    this.form = { ...b };
    this.editingId.set(b.id);
    this.showForm.set(true);
    this.error.set(null);
    this.success.set(null);
  }

  cancel(): void {
    this.showForm.set(false);
  }

  submit(): void {
    if (!this.form.exerciceFiscal) {
      this.error.set('L\'exercice fiscal est obligatoire.');
      return;
    }
    this.saving.set(true);
    this.error.set(null);

    const id = this.editingId();
    const call = id
      ? this.budgetService.update(id, this.form)
      : this.budgetService.create(this.form);

    call.subscribe({
      next: () => {
        this.saving.set(false);
        this.success.set(id ? 'Budget mis à jour.' : 'Budget créé.');
        this.showForm.set(false);
        this.load();
      },
      error: e => {
        this.saving.set(false);
        this.error.set('Erreur : ' + (e.error?.message ?? e.message));
      }
    });
  }

  valider(id: number): void {
    if (!confirm('Valider ce budget ?')) return;
    this.budgetService.valider(id).subscribe({
      next: () => { this.success.set('Budget validé.'); this.load(); },
      error: e => this.error.set('Erreur : ' + (e.error?.message ?? e.message))
    });
  }

  cloturer(id: number): void {
    if (!confirm('Clôturer ce budget ?\nCette action est irréversible.')) return;
    this.budgetService.cloturer(id).subscribe({
      next: () => { this.success.set('Budget clôturé.'); this.load(); },
      error: e => this.error.set('Erreur : ' + (e.error?.message ?? e.message))
    });
  }

  statutClass(statut: string): string {
    return statut === 'VALIDE'  ? 'badge badge-success'
         : statut === 'CLOTURE' ? 'badge badge-danger'
         : 'badge badge-info';
  }
}
