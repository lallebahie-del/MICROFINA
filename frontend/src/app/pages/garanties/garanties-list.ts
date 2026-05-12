import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { GarantiesService, Garantie } from '../../services/garanties.service';
import { TypesGarantieService, TypeGarantie } from '../../services/types-garantie.service';

@Component({
  selector: 'app-garanties-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './garanties-list.html'
})
export class GarantiesListComponent implements OnInit {
  garanties = signal<Garantie[]>([]);
  types     = signal<TypeGarantie[]>([]);

  creditIdFilter = signal<string>('');

  loading = signal(false);
  saving  = signal(false);
  error   = signal<string | null>(null);
  success = signal<string | null>(null);

  showForm = signal(false);
  form: any = { codeTypeGarantie: '', valeurEstimee: 0 };

  constructor(
    private svc: GarantiesService,
    private typesSvc: TypesGarantieService
  ) {}

  ngOnInit(): void {
    this.typesSvc.findActifs().subscribe({ next: list => this.types.set(list) });
  }

  load(): void {
    if (!this.creditIdFilter()) {
      this.error.set('Saisis un ID crédit pour rechercher.');
      return;
    }
    this.loading.set(true);
    this.error.set(null);
    this.svc.getByCreditId(+this.creditIdFilter()).subscribe({
      next: data => { this.garanties.set(data); this.loading.set(false); },
      error: e   => { this.error.set('Erreur : ' + (e.error?.message ?? e.message)); this.loading.set(false); }
    });
  }

  openNew(): void {
    if (!this.creditIdFilter()) {
      this.error.set('Sélectionne d\'abord un crédit (saisir l\'ID).');
      return;
    }
    this.form = {
      idCredit: +this.creditIdFilter(),
      codeTypeGarantie: '',
      valeurEstimee: 0,
      dateEvaluation: new Date().toISOString().slice(0, 10)
    };
    this.showForm.set(true);
    this.error.set(null);
    this.success.set(null);
  }

  cancel(): void {
    this.showForm.set(false);
  }

  submit(): void {
    if (!this.form.codeTypeGarantie || !this.form.idCredit || !this.form.valeurEstimee) {
      this.error.set('Type, crédit et valeur estimée sont obligatoires.');
      return;
    }
    this.saving.set(true);
    this.error.set(null);

    this.svc.ajouter(this.form).subscribe({
      next: () => {
        this.saving.set(false);
        this.success.set('Garantie ajoutée.');
        this.showForm.set(false);
        this.load();
      },
      error: e => {
        this.saving.set(false);
        this.error.set('Erreur : ' + (e.error?.message ?? e.message));
      }
    });
  }

  liberer(idGarantie: number): void {
    if (!confirm('Confirmer la libération de cette garantie ?')) return;
    this.svc.liberer(idGarantie).subscribe({
      next: () => { this.success.set('Garantie libérée.'); this.load(); },
      error: e => this.error.set('Erreur : ' + (e.error?.message ?? e.message))
    });
  }

  statutClass(s?: string): string {
    if (!s) return 'badge badge-info';
    if (s === 'ACTIF')  return 'badge badge-info';
    if (s === 'LIBERE') return 'badge badge-success';
    if (s === 'SAISI')  return 'badge badge-warning';
    if (s === 'EXPIRE') return 'badge badge-danger';
    return 'badge badge-primary';
  }
}
