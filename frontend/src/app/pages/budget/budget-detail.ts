import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { BudgetService, Budget } from '../../services/budget.service';
import { LignesBudgetService, LigneBudget, LigneBudgetWriteRequest } from '../../services/lignes-budget.service';
import { MouvementsBudgetService, MouvementBudget, MouvementBudgetWriteRequest } from '../../services/mouvements-budget.service';
import { ComptabiliteService, Ecriture } from '../../services/comptabilite.service';

@Component({
  selector: 'app-budget-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './budget-detail.html'
})
export class BudgetDetailComponent implements OnInit {

  budget    = signal<Budget | null>(null);
  lignes    = signal<LigneBudget[]>([]);
  mouvements = signal<MouvementBudget[]>([]);
  ecritures = signal<Ecriture[]>([]);

  loading = signal(false);
  saving  = signal(false);
  error   = signal<string | null>(null);
  success = signal<string | null>(null);

  // Form ligne
  showLigneForm = signal(false);
  editingLigneId = signal<number | null>(null);
  ligneForm: LigneBudgetWriteRequest = {
    codeRubrique: '',
    libelle: '',
    typeLigne: 'RECETTE',
    montantPrevu: 0
  };

  // Form mouvement
  showMvtFormForLigne = signal<number | null>(null);
  mvtForm: MouvementBudgetWriteRequest = {
    idComptabilite: 0,
    dateMouvement: new Date().toISOString().slice(0, 10),
    montant: 0
  };

  // Filters
  filtreType = signal<string>('');

  filteredLignes = computed<LigneBudget[]>(() => {
    const f = this.filtreType();
    return f ? this.lignes().filter(l => l.typeLigne === f) : this.lignes();
  });

  totalRecettesPrevu = computed<number>(() =>
    this.lignes().filter(l => l.typeLigne === 'RECETTE').reduce((s, l) => s + (l.montantPrevu ?? 0), 0)
  );
  totalRecettesRealise = computed<number>(() =>
    this.lignes().filter(l => l.typeLigne === 'RECETTE').reduce((s, l) => s + (l.montantRealise ?? 0), 0)
  );
  totalDepensesPrevu = computed<number>(() =>
    this.lignes().filter(l => l.typeLigne === 'DEPENSE').reduce((s, l) => s + (l.montantPrevu ?? 0), 0)
  );
  totalDepensesRealise = computed<number>(() =>
    this.lignes().filter(l => l.typeLigne === 'DEPENSE').reduce((s, l) => s + (l.montantRealise ?? 0), 0)
  );

  isBrouillon = computed<boolean>(() => this.budget()?.statut === 'BROUILLON');

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private budgetSvc: BudgetService,
    private lignesSvc: LignesBudgetService,
    private mouvementsSvc: MouvementsBudgetService,
    private comptaSvc: ComptabiliteService
  ) {}

  ngOnInit(): void {
    const id = +this.route.snapshot.params['id'];
    if (!id) { this.router.navigate(['/budgets']); return; }
    this.load(id);
  }

  load(id: number): void {
    this.loading.set(true);
    this.error.set(null);
    this.budgetSvc.getById(id).subscribe({
      next: b => this.budget.set(b),
      error: e => this.error.set('Erreur budget : ' + (e.error?.message ?? e.message))
    });
    this.lignesSvc.findByBudget(id).subscribe({
      next: l => { this.lignes.set(l); this.loading.set(false); },
      error: e => { this.error.set('Erreur lignes : ' + (e.error?.message ?? e.message)); this.loading.set(false); }
    });
    this.mouvementsSvc.findByBudget(id).subscribe({
      next: m => this.mouvements.set(m)
    });
    // Pré-charger les écritures (utiles au form mouvement)
    this.comptaSvc.getEcritures().subscribe({
      next: list => this.ecritures.set(list)
    });
  }

  reload(): void {
    const id = this.budget()?.id;
    if (id) this.load(id);
  }

  // ── Lignes ──────────────────────────────────────────────────────────────

  openLigneNew(): void {
    this.ligneForm = { codeRubrique: '', libelle: '', typeLigne: 'RECETTE', montantPrevu: 0 };
    this.editingLigneId.set(null);
    this.showLigneForm.set(true);
    this.error.set(null);
    this.success.set(null);
  }

  openLigneEdit(l: LigneBudget): void {
    this.ligneForm = {
      codeRubrique: l.codeRubrique,
      libelle: l.libelle,
      typeLigne: l.typeLigne,
      montantPrevu: l.montantPrevu,
      compte: l.compte
    };
    this.editingLigneId.set(l.id);
    this.showLigneForm.set(true);
    this.error.set(null);
    this.success.set(null);
  }

  cancelLigne(): void { this.showLigneForm.set(false); }

  submitLigne(): void {
    if (!this.ligneForm.codeRubrique || !this.ligneForm.libelle) {
      this.error.set('Code rubrique et libellé sont obligatoires.');
      return;
    }
    const id = this.editingLigneId();
    const budgetId = this.budget()!.id;
    this.saving.set(true);
    this.error.set(null);

    const call = id
      ? this.lignesSvc.update(id, this.ligneForm)
      : this.lignesSvc.create(budgetId, this.ligneForm);

    call.subscribe({
      next: () => {
        this.saving.set(false);
        this.success.set(id ? 'Ligne mise à jour.' : 'Ligne créée.');
        this.showLigneForm.set(false);
        this.reload();
      },
      error: e => {
        this.saving.set(false);
        this.error.set('Erreur : ' + (e.error?.message ?? e.message));
      }
    });
  }

  deleteLigne(id: number, libelle: string): void {
    if (!confirm(`Supprimer la ligne "${libelle}" ?`)) return;
    this.lignesSvc.delete(id).subscribe({
      next: () => { this.success.set('Ligne supprimée.'); this.reload(); },
      error: e => this.error.set('Erreur : ' + (e.error?.message ?? e.message))
    });
  }

  // ── Mouvements ─────────────────────────────────────────────────────────

  openMouvementForm(ligneId: number): void {
    this.mvtForm = {
      idComptabilite: 0,
      dateMouvement: new Date().toISOString().slice(0, 10),
      montant: 0,
      libelle: ''
    };
    this.showMvtFormForLigne.set(ligneId);
    this.error.set(null);
    this.success.set(null);
  }

  cancelMouvement(): void {
    this.showMvtFormForLigne.set(null);
  }

  submitMouvement(): void {
    const ligneId = this.showMvtFormForLigne();
    if (!ligneId) return;
    if (!this.mvtForm.idComptabilite || this.mvtForm.montant <= 0) {
      this.error.set('Écriture comptable et montant > 0 obligatoires.');
      return;
    }
    this.saving.set(true);
    this.error.set(null);

    this.mouvementsSvc.create(ligneId, this.mvtForm).subscribe({
      next: () => {
        this.saving.set(false);
        this.success.set('Mouvement enregistré.');
        this.showMvtFormForLigne.set(null);
        this.reload();
      },
      error: e => {
        this.saving.set(false);
        this.error.set('Erreur : ' + (e.error?.message ?? e.message));
      }
    });
  }

  deleteMouvement(id: number): void {
    if (!confirm(`Supprimer le mouvement #${id} ?`)) return;
    this.mouvementsSvc.delete(id).subscribe({
      next: () => { this.success.set('Mouvement supprimé.'); this.reload(); },
      error: e => this.error.set('Erreur : ' + (e.error?.message ?? e.message))
    });
  }

  // ── Helpers ─────────────────────────────────────────────────────────────

  mouvementsForLigne(ligneId: number): MouvementBudget[] {
    return this.mouvements().filter(m => m.ligneBudgetId === ligneId);
  }

  pctRealise(l: LigneBudget): number {
    if (!l.montantPrevu) return 0;
    return Math.min(100, Math.round((l.montantRealise / l.montantPrevu) * 100));
  }

  ecartLigne(l: LigneBudget): number {
    return (l.montantPrevu ?? 0) - (l.montantRealise ?? 0);
  }

  statutClass(s?: string): string {
    if (s === 'VALIDE')  return 'badge badge-success';
    if (s === 'CLOTURE') return 'badge badge-danger';
    return 'badge badge-info';
  }
}
