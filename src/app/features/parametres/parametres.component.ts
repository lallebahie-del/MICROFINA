import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { ToastService } from '../../core/toast.service';

interface Branch { id: string; code: string; name: string; city: string; address: string | null; latitude: number | null; longitude: number | null; }

@Component({
  selector: 'app-parametres',
  standalone: true,
  imports: [FormsModule],
  template: `
    <div class="page-header">
      <div><h1>Paramètres</h1><div class="text-muted">Configuration de l'institution</div></div>
    </div>

    <div class="grid grid-cols-2 gap-4 mb-6">
      <div class="card">
        <div class="card-header"><h3 class="card-title">Informations institution</h3></div>
        <div class="form-row"><label>Nom</label><input class="input" value="MicroFin SI" disabled /></div>
        <div class="form-row"><label>Devise</label><input class="input" value="XOF (FCFA)" disabled /></div>
        <div class="form-row"><label>Méthode amortissement</label><input class="input" value="Constant" disabled /></div>
        <div class="form-row"><label>Plafond crédit (par défaut)</label><input class="input" value="10 000 000 FCFA" disabled /></div>
      </div>

      <div class="card">
        <div class="card-header"><h3 class="card-title">Politique de sécurité</h3></div>
        <ul style="margin:0; padding-left:1.25rem; color:var(--text); font-size:.85rem;">
          <li>Verrouillage compte après 5 tentatives</li>
          <li>Durée verrouillage : 15 minutes</li>
          <li>Hash mot de passe : bcrypt (10 rounds)</li>
          <li>Sessions stockées en PostgreSQL</li>
          <li>Audit complet de toutes les opérations sensibles</li>
        </ul>
      </div>
    </div>

    <div class="card mb-6">
      <div class="card-header"><h3 class="card-title">Nouvelle agence</h3></div>
      <form (ngSubmit)="submit()">
        <div class="form-grid">
          <div class="form-row"><label>Code *</label><input class="input" [(ngModel)]="form.code" name="code" required /></div>
          <div class="form-row"><label>Nom *</label><input class="input" [(ngModel)]="form.name" name="name" required /></div>
          <div class="form-row"><label>Ville *</label><input class="input" [(ngModel)]="form.city" name="city" required /></div>
          <div class="form-row"><label>Adresse</label><input class="input" [(ngModel)]="form.address" name="address" /></div>
          <div class="form-row"><label>Latitude</label><input class="input" type="number" step="0.0001" [(ngModel)]="form.latitude" name="latitude" /></div>
          <div class="form-row"><label>Longitude</label><input class="input" type="number" step="0.0001" [(ngModel)]="form.longitude" name="longitude" /></div>
        </div>
        <div class="flex justify-end mt-2">
          <button class="btn" type="submit" [disabled]="saving()">{{ saving() ? 'Création…' : 'Ajouter agence' }}</button>
        </div>
      </form>
    </div>

    <div class="card">
      <div class="card-header"><h3 class="card-title">Agences ({{ branches().length }})</h3></div>
      <div class="table-wrap" style="border:none;">
        <table class="table">
          <thead><tr><th>Code</th><th>Nom</th><th>Ville</th><th>Adresse</th><th>Coordonnées</th></tr></thead>
          <tbody>
            @for (b of branches(); track b.id) {
              <tr>
                <td><strong>{{ b.code }}</strong></td>
                <td>{{ b.name }}</td>
                <td>{{ b.city }}</td>
                <td>{{ b.address ?? '—' }}</td>
                <td>{{ b.latitude ?? '—' }}, {{ b.longitude ?? '—' }}</td>
              </tr>
            }
          </tbody>
        </table>
      </div>
    </div>
  `,
})
export class ParametresComponent implements OnInit {
  private api = inject(ApiService);
  private toast = inject(ToastService);
  branches = signal<Branch[]>([]);
  saving = signal(false);
  form: { code: string; name: string; city: string; address: string; latitude: number | null; longitude: number | null } = {
    code: '', name: '', city: '', address: '', latitude: null, longitude: null,
  };
  ngOnInit() { this.refresh(); }
  refresh() { this.api.get<Branch[]>('/branches').subscribe((b) => this.branches.set(b)); }
  submit() {
    if (!this.form.code || !this.form.name || !this.form.city) { this.toast.error('Code, nom, ville requis'); return; }
    this.saving.set(true);
    const payload: Record<string, unknown> = { ...this.form };
    if (!payload['address']) delete payload['address'];
    if (payload['latitude'] === null || payload['latitude'] === '') delete payload['latitude']; else payload['latitude'] = Number(payload['latitude']);
    if (payload['longitude'] === null || payload['longitude'] === '') delete payload['longitude']; else payload['longitude'] = Number(payload['longitude']);
    this.api.post('/branches', payload).subscribe({
      next: () => { this.saving.set(false); this.toast.success('Agence créée'); this.form = { code: '', name: '', city: '', address: '', latitude: null, longitude: null }; this.refresh(); },
      error: (e) => { this.saving.set(false); this.toast.error(e?.error?.error ?? 'Erreur'); },
    });
  }
}
