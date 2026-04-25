import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { ToastService } from '../../core/toast.service';
import { formatDateTime, ROLE_LABELS } from '../../core/format';

interface AppUser {
  id: string; username: string; fullName: string; email: string | null; role: string;
  branchId: string | null; branchName: string | null; active: boolean;
  lastLoginAt: string | null; createdAt: string;
}
interface Branch { id: string; name: string; }

@Component({
  selector: 'app-utilisateurs',
  standalone: true,
  imports: [FormsModule],
  template: `
    <div class="page-header">
      <div><h1>Utilisateurs</h1><div class="text-muted">{{ users().length }} utilisateur(s)</div></div>
      @if (isAdmin()) { <button class="btn" (click)="showForm.set(!showForm())">{{ showForm() ? 'Annuler' : '+ Nouvel utilisateur' }}</button> }
    </div>

    @if (isAdmin() && showForm()) {
    <div class="card mb-6">
      <div class="card-header"><h3 class="card-title">Nouvel utilisateur</h3></div>
      <form (ngSubmit)="submit()">
        <div class="form-grid">
          <div class="form-row"><label>Identifiant *</label><input class="input" [(ngModel)]="form.username" name="username" required /></div>
          <div class="form-row"><label>Mot de passe *</label><input class="input" type="password" [(ngModel)]="form.password" name="password" required /></div>
          <div class="form-row"><label>Nom complet *</label><input class="input" [(ngModel)]="form.fullName" name="fullName" required /></div>
          <div class="form-row"><label>Email</label><input class="input" type="email" [(ngModel)]="form.email" name="email" /></div>
          <div class="form-row"><label>Rôle *</label>
            <select class="select" [(ngModel)]="form.role" name="role" required>
              @for (r of roles; track r) { <option [value]="r">{{ roleLabel(r) }}</option> }
            </select>
          </div>
          <div class="form-row"><label>Agence</label>
            <select class="select" [(ngModel)]="form.branchId" name="branchId">
              <option value="">Aucune</option>
              @for (b of branches(); track b.id) { <option [value]="b.id">{{ b.name }}</option> }
            </select>
          </div>
        </div>
        <div class="flex justify-end mt-2">
          <button class="btn" type="submit" [disabled]="saving()">{{ saving() ? 'Création…' : 'Créer' }}</button>
        </div>
      </form>
    </div>
    }

    <div class="table-wrap">
      <table class="table">
        <thead><tr><th>Identifiant</th><th>Nom</th><th>Rôle</th><th>Agence</th><th>Actif</th><th>Dernière connexion</th><th>Actions</th></tr></thead>
        <tbody>
          @for (u of users(); track u.id) {
            <tr>
              <td><strong>{{ u.username }}</strong></td>
              <td>{{ u.fullName }}<div class="text-xs text-muted">{{ u.email ?? '' }}</div></td>
              <td><span class="badge badge-info">{{ roleLabel(u.role) }}</span></td>
              <td>{{ u.branchName ?? '—' }}</td>
              <td><span class="badge" [class.badge-success]="u.active" [class.badge-danger]="!u.active">{{ u.active ? 'Actif' : 'Désactivé' }}</span></td>
              <td>{{ fmtDt(u.lastLoginAt) }}</td>
              <td>
                @if (isAdmin()) {
                  <button class="btn btn-sm btn-secondary" (click)="toggle(u)">{{ u.active ? 'Désactiver' : 'Activer' }}</button>
                }
              </td>
            </tr>
          }
        </tbody>
      </table>
    </div>
  `,
})
export class UtilisateursComponent implements OnInit {
  private api = inject(ApiService);
  private auth = inject(AuthService);
  private toast = inject(ToastService);
  users = signal<AppUser[]>([]);
  branches = signal<Branch[]>([]);
  saving = signal(false);
  showForm = signal(false);
  roles = ['admin', 'directeur', 'chef_agence', 'auditeur', 'agent_caisse', 'agent_credit', 'comptable'];
  isAdmin = computed(() => this.auth.user()?.role === 'admin');
  form = { username: '', password: '', fullName: '', email: '', role: 'agent_caisse', branchId: '' };

  ngOnInit() {
    this.refresh();
    this.api.get<Branch[]>('/branches').subscribe((b) => this.branches.set(b));
  }
  refresh() { this.api.get<AppUser[]>('/users').subscribe((u) => this.users.set(u)); }

  submit() {
    if (!this.form.username || !this.form.password || !this.form.fullName) { this.toast.error('Champs requis manquants'); return; }
    this.saving.set(true);
    const payload: Record<string, unknown> = { ...this.form };
    if (!payload['email']) delete payload['email'];
    if (!payload['branchId']) delete payload['branchId'];
    this.api.post('/users', payload).subscribe({
      next: () => { this.saving.set(false); this.toast.success('Utilisateur créé'); this.form = { username: '', password: '', fullName: '', email: '', role: 'agent_caisse', branchId: '' }; this.showForm.set(false); this.refresh(); },
      error: (e) => { this.saving.set(false); this.toast.error(e?.error?.error ?? 'Erreur'); },
    });
  }

  toggle(u: AppUser) {
    this.api.patch(`/users/${u.id}`, { active: !u.active }).subscribe({
      next: () => { this.toast.success('Utilisateur mis à jour'); this.refresh(); },
      error: (e) => this.toast.error(e?.error?.error ?? 'Erreur'),
    });
  }

  fmtDt = formatDateTime;
  roleLabel = (s: string) => ROLE_LABELS[s] ?? s;
}
