import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { ToastService } from '../../core/toast.service';

interface Client { id: string; fullName: string; code: string; }

@Component({
  selector: 'app-account-new',
  standalone: true,
  imports: [FormsModule, RouterLink],
  template: `
    <div class="page-header">
      <div><h1>Nouveau compte</h1><div class="text-muted">Ouverture d'un compte client</div></div>
      <a class="btn btn-secondary" routerLink="/comptes">← Retour</a>
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
          <div class="form-row"><label>Type de compte *</label>
            <select class="select" [(ngModel)]="form.accountType" name="accountType" required>
              <option value="epargne">Épargne</option>
              <option value="courant">Courant</option>
              <option value="depot_terme">Dépôt à terme</option>
              <option value="plan_epargne">Plan d'épargne</option>
            </select>
          </div>
          <div class="form-row"><label>Devise</label>
            <select class="select" [(ngModel)]="form.currency" name="currency"><option value="XOF">XOF (FCFA)</option></select>
          </div>
          <div class="form-row"><label>Dépôt initial (FCFA)</label><input class="input" type="number" min="0" [(ngModel)]="form.initialDeposit" name="initialDeposit" /></div>
        </div>
        <div class="flex justify-between mt-4">
          <a class="btn btn-secondary" routerLink="/comptes">Annuler</a>
          <button class="btn" type="submit" [disabled]="saving()">{{ saving() ? 'Création…' : 'Ouvrir le compte' }}</button>
        </div>
      </form>
    </div>
  `,
})
export class AccountNewComponent implements OnInit {
  private api = inject(ApiService);
  private router = inject(Router);
  private toast = inject(ToastService);
  clients = signal<Client[]>([]);
  saving = signal(false);
  form = { clientId: '', accountType: 'epargne', currency: 'XOF', initialDeposit: 0 };

  ngOnInit() { this.api.get<Client[]>('/clients').subscribe((c) => this.clients.set(c)); }
  submit() {
    if (!this.form.clientId) { this.toast.error('Client requis'); return; }
    this.saving.set(true);
    this.api.post('/accounts', { ...this.form, initialDeposit: Number(this.form.initialDeposit) || 0 }).subscribe({
      next: () => { this.toast.success('Compte créé'); this.router.navigate(['/comptes']); },
      error: (e) => { this.saving.set(false); this.toast.error(e?.error?.error ?? 'Erreur'); },
    });
  }
}
