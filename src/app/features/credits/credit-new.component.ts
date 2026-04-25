import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { ToastService } from '../../core/toast.service';
import { PRODUCT_LABELS } from '../../core/format';

interface Client { id: string; fullName: string; code: string; }

@Component({
  selector: 'app-credit-new',
  standalone: true,
  imports: [FormsModule, RouterLink],
  template: `
    <div class="page-header">
      <div><h1>Nouveau crédit</h1><div class="text-muted">Demande de crédit</div></div>
      <a class="btn btn-secondary" routerLink="/credits">← Retour</a>
    </div>
    <div class="card">
      <form (ngSubmit)="submit()">
        <div class="form-grid">
          <div class="form-row"><label>Client *</label>
            <select class="select" [(ngModel)]="form.clientId" name="clientId" required>
              <option value="">Sélectionner</option>
              @for (c of clients(); track c.id) { <option [value]="c.id">{{ c.code }} — {{ c.fullName }}</option> }
            </select>
          </div>
          <div class="form-row"><label>Produit *</label>
            <select class="select" [(ngModel)]="form.product" name="product" required>
              @for (p of products; track p) { <option [value]="p">{{ productLabel(p) }}</option> }
            </select>
          </div>
          <div class="form-row"><label>Principal (FCFA) *</label><input class="input" type="number" min="1" [(ngModel)]="form.principal" name="principal" required /></div>
          <div class="form-row"><label>Taux d'intérêt annuel (%) *</label><input class="input" type="number" step="0.01" min="0" [(ngModel)]="form.interestRate" name="interestRate" required /></div>
          <div class="form-row"><label>Durée (mois) *</label><input class="input" type="number" min="1" max="120" [(ngModel)]="form.durationMonths" name="durationMonths" required /></div>
          <div class="form-row"><label>Garantie</label><input class="input" [(ngModel)]="form.guarantee" name="guarantee" /></div>
          <div class="form-row" style="grid-column: 1 / -1;"><label>Objet du crédit</label><textarea class="textarea" [(ngModel)]="form.purpose" name="purpose"></textarea></div>
        </div>
        <div class="flex justify-between mt-4">
          <a class="btn btn-secondary" routerLink="/credits">Annuler</a>
          <button class="btn" type="submit" [disabled]="saving()">{{ saving() ? 'Création…' : 'Créer la demande' }}</button>
        </div>
      </form>
    </div>
  `,
})
export class CreditNewComponent implements OnInit {
  private api = inject(ApiService);
  private router = inject(Router);
  private toast = inject(ToastService);
  clients = signal<Client[]>([]);
  saving = signal(false);
  products = ['classique', 'mourabaha', 'moucharaka', 'ijara', 'consommation', 'decouvert'];
  form = { clientId: '', product: 'classique', principal: 0, interestRate: 12, durationMonths: 12, guarantee: '', purpose: '' };

  ngOnInit() { this.api.get<Client[]>('/clients').subscribe((c) => this.clients.set(c)); }

  submit() {
    if (!this.form.clientId || !this.form.principal) { this.toast.error('Client et principal requis'); return; }
    this.saving.set(true);
    const payload = {
      ...this.form,
      principal: Number(this.form.principal),
      interestRate: Number(this.form.interestRate),
      durationMonths: Number(this.form.durationMonths),
    };
    this.api.post('/credits', payload).subscribe({
      next: () => { this.toast.success('Crédit créé'); this.router.navigate(['/credits']); },
      error: (e) => { this.saving.set(false); this.toast.error(e?.error?.error ?? 'Erreur'); },
    });
  }
  productLabel = (s: string) => PRODUCT_LABELS[s] ?? s;
}
