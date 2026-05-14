import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { OperationsCaisseService, OperationCaisse, OperationCaisseForm } from '../../services/operations-caisse.service';
import { AgencesService, Agence } from '../../services/agences.service';
import { PaginationBarComponent } from '../../components/pagination-bar/pagination-bar.component';
import { DEFAULT_LIST_PAGE_SIZE, listClampPage, listSlice } from '../../shared/list-pagination';

@Component({
  selector: 'app-operations-caisse-list',
  standalone: true,
  imports: [CommonModule, FormsModule, PaginationBarComponent],
  templateUrl: './operations-caisse-list.html'
})
export class OperationsCaisseListComponent implements OnInit {
  allOperations = signal<OperationCaisse[]>([]);
  agences         = signal<Agence[]>([]);

  agenceFilter = signal<string>('');
  page           = signal(0);
  readonly pageSize = DEFAULT_LIST_PAGE_SIZE;

  clampedOpPage = computed(() =>
    listClampPage(this.page(), this.allOperations().length, this.pageSize)
  );

  pagedOperations = computed(() =>
    listSlice(this.allOperations(), this.clampedOpPage(), this.pageSize)
  );

  loading = signal(false);
  saving  = signal(false);
  error   = signal<string | null>(null);
  success = signal<string | null>(null);

  showForm = signal(false);
  form: Partial<OperationCaisseForm> = {
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
    this.svc.getAll(this.agenceFilter() || undefined).subscribe({
      next: (list: OperationCaisse[]) => {
        this.allOperations.set(list);
        this.page.set(0);
        this.loading.set(false);
      },
      error: e => {
        this.error.set('Erreur : ' + (e.error?.message ?? e.message));
        this.loading.set(false);
      }
    });
  }

  onAgenceFilterChange(v: string): void {
    this.agenceFilter.set(v);
    this.load();
  }

  onOpPageChange(p: number): void {
    this.page.set(listClampPage(p, this.allOperations().length, this.pageSize));
  }

  openNew(): void {
    this.form = {
      typeOperation: 'DEPOT',
      modePaiement: 'ESPECES',
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
    if (!this.form.dateOperation) {
      this.error.set('La date est obligatoire.');
      return;
    }
    this.saving.set(true);
    this.error.set(null);

    this.svc.create(this.form).subscribe({
      next: () => {
        this.saving.set(false);
        this.success.set('Opération enregistrée.');
        this.showForm.set(false);
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

  typeClass(motif: string): string {
    const m = (motif || '').toUpperCase();
    if (m.includes('DEPOT') || m.includes('VERSEMENT')) return 'badge badge-success';
    if (m.includes('RETRAIT') || m.includes('PAIEMENT')) return 'badge badge-danger';
    return 'badge badge-info';
  }
}
