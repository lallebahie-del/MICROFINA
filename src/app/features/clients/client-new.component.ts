import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { ToastService } from '../../core/toast.service';

interface Branch { id: string; name: string; }

@Component({
  selector: 'app-client-new',
  standalone: true,
  imports: [FormsModule, RouterLink],
  template: `
    <div class="page-header">
      <div><h1>Nouveau client</h1><div class="text-muted">Créer un dossier client</div></div>
      <a class="btn btn-secondary" routerLink="/clients">← Retour</a>
    </div>

    <div class="card">
      <form (ngSubmit)="submit()">
        <div class="form-grid">
          <div class="form-row"><label>Nom complet *</label><input class="input" [(ngModel)]="form.fullName" name="fullName" required /></div>
          <div class="form-row"><label>Type *</label>
            <select class="select" [(ngModel)]="form.clientType" name="clientType" required>
              <option value="particulier">Particulier</option>
              <option value="entreprise">Entreprise</option>
              <option value="association">Association</option>
            </select>
          </div>
          <div class="form-row"><label>Téléphone</label><input class="input" [(ngModel)]="form.phone" name="phone" /></div>
          <div class="form-row"><label>Email</label><input class="input" type="email" [(ngModel)]="form.email" name="email" /></div>
          <div class="form-row"><label>N° pièce d'identité</label><input class="input" [(ngModel)]="form.idNumber" name="idNumber" /></div>
          <div class="form-row"><label>Date de naissance</label><input class="input" type="date" [(ngModel)]="form.birthDate" name="birthDate" /></div>
          <div class="form-row"><label>Profession</label><input class="input" [(ngModel)]="form.profession" name="profession" /></div>
          <div class="form-row"><label>Agence *</label>
            <select class="select" [(ngModel)]="form.branchId" name="branchId" required>
              <option value="">Sélectionner</option>
              @for (b of branches(); track b.id) { <option [value]="b.id">{{ b.name }}</option> }
            </select>
          </div>
          <div class="form-row" style="grid-column: 1 / -1;"><label>Adresse</label><textarea class="textarea" [(ngModel)]="form.address" name="address"></textarea></div>
        </div>
        <div class="flex gap-2 justify-between mt-4">
          <a class="btn btn-secondary" routerLink="/clients">Annuler</a>
          <button class="btn" type="submit" [disabled]="saving()">{{ saving() ? 'Enregistrement…' : 'Créer le client' }}</button>
        </div>
      </form>
    </div>
  `,
})
export class ClientNewComponent implements OnInit {
  private api = inject(ApiService);
  private router = inject(Router);
  private toast = inject(ToastService);

  branches = signal<Branch[]>([]);
  saving = signal(false);
  form = {
    fullName: '', clientType: 'particulier', phone: '', email: '',
    idNumber: '', birthDate: '', profession: '', branchId: '', address: '',
  };

  ngOnInit() { this.api.get<Branch[]>('/branches').subscribe((b) => this.branches.set(b)); }

  submit() {
    if (!this.form.fullName || !this.form.branchId) {
      this.toast.error('Nom et agence requis');
      return;
    }
    const payload: Record<string, unknown> = { ...this.form };
    Object.keys(payload).forEach((k) => { if (payload[k] === '') delete payload[k]; });
    this.saving.set(true);
    this.api.post('/clients', payload).subscribe({
      next: () => { this.toast.success('Client créé'); this.router.navigate(['/clients']); },
      error: (e) => { this.saving.set(false); this.toast.error(e?.error?.error ?? 'Erreur de création'); },
    });
  }
}
