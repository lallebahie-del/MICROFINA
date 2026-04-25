import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { ToastService } from '../../core/toast.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule],
  template: `
    <div class="login-shell">
      <div class="login-card">
        <div class="logo">µF</div>
        <h1>Système de Gestion IMF</h1>
        <p class="sub">Microfinance Institution Information System</p>

        @if (errorMsg()) {
          <div class="toast error" style="margin-bottom:1rem;position:static;animation:none;">{{ errorMsg() }}</div>
        }

        <form (ngSubmit)="submit()" #f="ngForm" autocomplete="on">
          <div class="form-row">
            <label for="username">Nom d'utilisateur</label>
            <input id="username" name="username" class="input" [(ngModel)]="username" (input)="errorMsg.set(null)" required autofocus autocomplete="username" />
          </div>
          <div class="form-row">
            <label for="password">Mot de passe</label>
            <input id="password" type="password" name="password" class="input" [(ngModel)]="password" (input)="errorMsg.set(null)" required autocomplete="current-password" />
          </div>
          <button type="submit" class="btn w-full" [disabled]="loading() || f.invalid">
            {{ loading() ? 'Connexion…' : 'Se connecter' }}
          </button>
        </form>

        <div class="divider"></div>
        <div class="text-xs text-muted">
          <strong>Comptes de démonstration :</strong><br />
          admin / admin123 — Administrateur<br />
          directeur / password123 — Directeur<br />
          caisse.dakar / password123 — Caisse
        </div>
      </div>
    </div>
  `,
})
export class LoginComponent {
  private auth = inject(AuthService);
  private router = inject(Router);
  private toast = inject(ToastService);

  username = '';
  password = '';
  loading = signal(false);
  errorMsg = signal<string | null>(null);

  submit() {
    this.errorMsg.set(null);
    this.loading.set(true);
    this.auth.login(this.username.trim(), this.password).subscribe({
      next: (u) => {
        this.loading.set(false);
        this.toast.success(`Bienvenue ${u.fullName}`);
        this.router.navigate(['/']);
      },
      error: (err) => {
        this.loading.set(false);
        const msg = err?.error?.error ?? 'Connexion échouée. Vérifiez vos identifiants.';
        this.errorMsg.set(msg);
      },
    });
  }
}
