import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { ToastService } from '../../core/toast.service';
import { formatFCFA, isAuditor } from '../../core/format';

interface Line { id: string; label: string; category: string | null; type: string; planned: number; realized: number; period: string; }

@Component({
  selector: 'app-budget',
  standalone: true,
  imports: [FormsModule],
  template: `
    <div class="page-header">
      <div><h1>Budget</h1><div class="text-muted">Suivi prévisionnel vs réalisé</div></div>
    </div>

    <div class="grid grid-cols-4 gap-4 mb-6">
      <div class="stat"><div class="label">Recettes prévues</div><div class="value">{{ fmt(plannedIn()) }}</div></div>
      <div class="stat"><div class="label">Recettes réalisées</div><div class="value" style="color:var(--color-success);">{{ fmt(realizedIn()) }}</div></div>
      <div class="stat"><div class="label">Dépenses prévues</div><div class="value">{{ fmt(plannedOut()) }}</div></div>
      <div class="stat"><div class="label">Dépenses réalisées</div><div class="value" style="color:var(--color-danger);">{{ fmt(realizedOut()) }}</div></div>
    </div>

    @if (!isAud()) {
    <div class="card mb-6">
      <div class="card-header"><h3 class="card-title">Nouvelle ligne budgétaire</h3></div>
      <form (ngSubmit)="submit()">
        <div class="form-grid">
          <div class="form-row"><label>Libellé *</label><input class="input" [(ngModel)]="form.label" name="label" required /></div>
          <div class="form-row"><label>Catégorie</label><input class="input" [(ngModel)]="form.category" name="category" /></div>
          <div class="form-row"><label>Type *</label>
            <select class="select" [(ngModel)]="form.type" name="type" required>
              <option value="recette">Recette</option>
              <option value="depense">Dépense</option>
            </select>
          </div>
          <div class="form-row"><label>Période *</label><input class="input" placeholder="2026" [(ngModel)]="form.period" name="period" required /></div>
          <div class="form-row"><label>Prévu (FCFA) *</label><input class="input" type="number" min="0" [(ngModel)]="form.planned" name="planned" required /></div>
          <div class="form-row"><label>Réalisé (FCFA)</label><input class="input" type="number" min="0" [(ngModel)]="form.realized" name="realized" /></div>
        </div>
        <div class="flex justify-end mt-2">
          <button class="btn" type="submit" [disabled]="saving()">{{ saving() ? 'Enregistrement…' : 'Ajouter' }}</button>
        </div>
      </form>
    </div>
    }

    <div class="card">
      <div class="card-header"><h3 class="card-title">Lignes budgétaires</h3></div>
      <div class="table-wrap" style="border:none;">
        <table class="table">
          <thead><tr><th>Libellé</th><th>Catégorie</th><th>Type</th><th>Période</th><th class="num">Prévu</th><th class="num">Réalisé</th><th class="num">Écart</th><th>Taux</th></tr></thead>
          <tbody>
            @for (l of lines(); track l.id) {
              <tr>
                <td><strong>{{ l.label }}</strong></td>
                <td>{{ l.category ?? '—' }}</td>
                <td><span class="badge" [class.badge-success]="l.type === 'recette'" [class.badge-warning]="l.type === 'depense'">{{ l.type }}</span></td>
                <td>{{ l.period }}</td>
                <td class="num">{{ fmt(l.planned) }}</td>
                <td class="num">{{ fmt(l.realized) }}</td>
                <td class="num" [style.color]="(l.realized - l.planned) >= 0 ? 'var(--color-success)' : 'var(--color-danger)'">
                  {{ fmt(l.realized - l.planned) }}
                </td>
                <td>
                  <div style="background:#e2e8f0; border-radius:999px; height:6px; width:80px; overflow:hidden;">
                    <div [style.width.%]="rate(l)" [style.background]="rate(l) > 100 ? 'var(--color-danger)' : 'var(--color-primary)'" style="height:100%;"></div>
                  </div>
                  <span class="text-xs text-muted">{{ rate(l) }}%</span>
                </td>
              </tr>
            } @empty { <tr><td colspan="8"><div class="empty">Aucune ligne</div></td></tr> }
          </tbody>
        </table>
      </div>
    </div>
  `,
})
export class BudgetComponent implements OnInit {
  private api = inject(ApiService);
  private auth = inject(AuthService);
  private toast = inject(ToastService);
  lines = signal<Line[]>([]);
  saving = signal(false);
  isAud = computed(() => isAuditor(this.auth.user()?.role));
  form = { label: '', category: '', type: 'recette', period: String(new Date().getFullYear()), planned: 0, realized: 0 };

  ngOnInit() { this.refresh(); }
  refresh() { this.api.get<Line[]>('/budget/lines').subscribe((r) => this.lines.set(r)); }

  plannedIn = computed(() => this.lines().filter((l) => l.type === 'recette').reduce((s, l) => s + l.planned, 0));
  realizedIn = computed(() => this.lines().filter((l) => l.type === 'recette').reduce((s, l) => s + l.realized, 0));
  plannedOut = computed(() => this.lines().filter((l) => l.type === 'depense').reduce((s, l) => s + l.planned, 0));
  realizedOut = computed(() => this.lines().filter((l) => l.type === 'depense').reduce((s, l) => s + l.realized, 0));

  rate(l: Line): number { return l.planned > 0 ? Math.min(150, Math.round((l.realized / l.planned) * 100)) : 0; }

  submit() {
    if (!this.form.label || !this.form.planned) { this.toast.error('Libellé et montant prévu requis'); return; }
    this.saving.set(true);
    const payload = { ...this.form, planned: Number(this.form.planned), realized: Number(this.form.realized) || 0, category: this.form.category || undefined };
    this.api.post('/budget/lines', payload).subscribe({
      next: () => { this.saving.set(false); this.toast.success('Ligne ajoutée'); this.form = { label: '', category: '', type: 'recette', period: String(new Date().getFullYear()), planned: 0, realized: 0 }; this.refresh(); },
      error: (e) => { this.saving.set(false); this.toast.error(e?.error?.error ?? 'Erreur'); },
    });
  }
  fmt = formatFCFA;
}
