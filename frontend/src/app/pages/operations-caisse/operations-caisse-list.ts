import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { OperationsCaisseService, OperationCaisse } from '../../services/operations-caisse.service';
import { AgencesService, Agence } from '../../services/agences.service';

@Component({
  selector: 'app-operations-caisse-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './operations-caisse-list.html'
})
export class OperationsCaisseListComponent implements OnInit {
  operations = signal<OperationCaisse[]>([]);
  agences    = signal<Agence[]>([]);

  agenceFilter = signal<string>('');
  page         = signal(0);
  totalPages   = signal(0);

  loading = signal(false);
  saving  = signal(false);
  error   = signal<string | null>(null);
  success = signal<string | null>(null);

  showForm = signal(false);
  form: Partial<OperationCaisse> = {
    typeOperation: 'DEPOT',
    devise: 'MRU',
    montant: 0
  };

  readonly typesOperation = ['DEPOT', 'RETRAIT', 'VERSEMENT', 'PAIEMENT'];

  constructor(
    private svc: OperationsCaisseService,
    private agencesSvc: AgencesService
  ) {}

  ngOnInit(): void {
    this.load();
    this.agencesSvc.getAll(true).subscribe({ next: list => this.agences.set(list) });
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.svc.getAll(this.agenceFilter() || undefined, this.page()).subscribe({
      next: (resp: any) => {
        this.operations.set(resp.content ?? resp);
        this.totalPages.set(resp.totalPages ?? 0);
        this.loading.set(false);
      },
      error: e => {
        this.error.set('Erreur : ' + (e.error?.message ?? e.message));
        this.loading.set(false);
      }
    });
  }

  prev(): void { if (this.page() > 0)                  { this.page.set(this.page() - 1); this.load(); } }
  next(): void { if (this.page() < this.totalPages() - 1) { this.page.set(this.page() + 1); this.load(); } }

  openNew(): void {
    this.form = {
      typeOperation: 'DEPOT',
      devise: 'MRU',
      montant: 0,
      dateOperation: new Date().toISOString().slice(0, 10)
    };
    this.showForm.set(true);
    this.error.set(null);
    this.success.set(null);
  }

  cancel(): void {
    this.showForm.set(false);
  }

  submit(): void {
    if (!this.form.typeOperation || !this.form.montant || !this.form.agence) {
      this.error.set('Type, montant et agence sont obligatoires.');
      return;
    }
    if ((this.form.montant as number) <= 0) {
      this.error.set('Le montant doit être strictement positif.');
      return;
    }
    this.saving.set(true);
    this.error.set(null);

    this.svc.create(this.form).subscribe({
      next: () => {
        this.saving.set(false);
        this.success.set('Opération enregistrée.');
        this.showForm.set(false);
        this.page.set(0);
        this.load();
      },
      error: e => {
        this.saving.set(false);
        this.error.set('Erreur : ' + (e.error?.message ?? e.message));
      }
    });
  }

  statutClass(statut: string): string {
    const s = (statut || '').toUpperCase();
    if (s.includes('VALIDE') || s.includes('SUCC')) return 'badge badge-success';
    if (s.includes('REJET') || s.includes('ECH'))   return 'badge badge-danger';
    if (s.includes('ATTENTE') || s.includes('PEND')) return 'badge badge-warning';
    return 'badge badge-info';
  }

  typeClass(t: string): string {
    if (t === 'DEPOT' || t === 'VERSEMENT') return 'badge badge-success';
    if (t === 'RETRAIT' || t === 'PAIEMENT') return 'badge badge-danger';
    return 'badge badge-info';
  }
}
