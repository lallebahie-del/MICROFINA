import { Component, inject } from '@angular/core';
import { AuthService } from '../../core/auth.service';
import { ROLE_LABELS } from '../../core/format';

@Component({
  selector: 'app-profil',
  standalone: true,
  template: `
    @if (auth.user(); as u) {
      <div class="profil-container">
        <!-- Page Header -->
        <div class="profil-header">
          <h1 class="page-title">Profil</h1>
          <p class="page-subtitle">Vos informations et sécurité</p>
        </div>

        <!-- Informations Card -->
        <div class="card profil-card">
          <div class="card-header">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="opacity: 0.8;"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
            <h2 class="section-title">Informations</h2>
          </div>
          
          <div class="info-grid">
            <div class="info-group">
              <label class="info-label">NOM COMPLET</label>
              <div class="info-value">{{ u.fullName }}</div>
            </div>
            <div class="info-group">
              <label class="info-label">IDENTIFIANT</label>
              <div class="info-value">{{ u.username }}</div>
            </div>
            <div class="info-group">
              <label class="info-label">EMAIL</label>
              <div class="info-value">{{ u.email }}</div>
            </div>
            <div class="info-group">
              <label class="info-label">RÔLE</label>
              <div class="info-value">
                <span class="role-badge">{{ roleLabel(u.role) }}</span>
              </div>
            </div>
            <div class="info-group full-width">
              <label class="info-label">AGENCE</label>
              <div class="info-value">Agence {{ u.branchName || 'Dakar Plateau' }}</div>
            </div>
          </div>
        </div>

        <!-- Password Card -->
        <div class="card profil-card mt-6">
          <h2 class="section-title mb-6">Changer mon mot de passe</h2>
          
          <div class="password-form">
            <div class="form-group">
              <label class="form-label">Nouveau mot de passe</label>
              <input type="password" class="form-input" />
            </div>
            <div class="form-group">
              <label class="form-label">Confirmer</label>
              <input type="password" class="form-input" />
            </div>
            <button class="btn-update">Mettre à jour</button>
          </div>
        </div>
      </div>
    }
  `,
  styles: [`
    .profil-container { max-width: 900px; margin: 0 auto; padding-top: 2rem; }
    
    .profil-header { margin-bottom: 2.5rem; }
    .page-title { font-size: 2.5rem; font-weight: 700; color: #0f172a; margin: 0; }
    .page-subtitle { font-size: 1.15rem; color: #64748b; margin: 0.5rem 0 0 0; }

    .profil-card { background: white; border: 1px solid #e2e8f0; border-radius: 12px; padding: 1.75rem; box-shadow: 0 1px 3px rgba(0,0,0,0.02); }
    
    .card-header { display: flex; align-items: center; justify-content: flex-start; margin-bottom: 1.5rem; border: none; padding: 0; background: none; gap: 12px; }
    .section-title { font-size: 1.25rem; font-weight: 600; color: #1e293b; margin: 0; }
    
    .info-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 1.5rem; }
    .info-group.full-width { grid-column: span 2; }
    
    .info-label { display: block; font-size: 0.75rem; font-weight: 600; color: #94a3b8; letter-spacing: 0.05em; margin-bottom: 0.4rem; }
    .info-value { font-size: 1.05rem; font-weight: 500; color: #0f172a; }
    
    .role-badge { background: #f8fafc; border: 1px solid #f1f5f9; padding: 0.25rem 0.75rem; border-radius: 8px; font-size: 0.85rem; color: #475569; font-weight: 600; }
    
    .password-form { max-width: 450px; }
    .form-group { margin-bottom: 1rem; }
    .form-label { display: block; font-size: 0.95rem; font-weight: 500; color: #1e293b; margin-bottom: 0.4rem; }
    .form-input { width: 100%; padding: 0.7rem 1rem; border: 1px solid #e2e8f0; border-radius: 8px; background: #ffffff; font-size: 0.95rem; transition: all 0.2s; }
    .form-input:focus { outline: none; border-color: #0f766e; box-shadow: 0 0 0 3px rgba(15, 118, 110, 0.1); }
    
    .btn-update { background: #0f766e; color: white; border: none; padding: 0.75rem 1.5rem; border-radius: 8px; font-weight: 600; font-size: 0.95rem; cursor: pointer; transition: all 0.2s; margin-top: 0.5rem; }
    .btn-update:hover { background: #0d5c55; transform: translateY(-1px); }
    
    .mt-6 { margin-top: 1.25rem; }
    .mb-6 { margin-bottom: 1.25rem; }
  `]
})
export class ProfilComponent {
  auth = inject(AuthService);
  roleLabel = (s: string) => ROLE_LABELS[s] ?? s;
}
