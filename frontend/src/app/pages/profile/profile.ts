import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../core/auth.service';
import { AdminService, Utilisateur } from '../../services/admin.service';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './profile.html',
  styleUrl: './profile.css',
})
export class ProfileComponent implements OnInit {
  private readonly auth  = inject(AuthService);
  private readonly admin = inject(AdminService);

  readonly user    = this.auth.currentUser;
  readonly canEdit = computed(() => this.auth.hasAnyPrivilege('PRIV_MANAGE_USERS'));

  /** Détails complets (récupérés depuis l'API si le profil a accès à l'admin). */
  details = signal<Utilisateur | null>(null);
  loading = signal(false);
  loadError = signal<string | null>(null);

  /** Formulaire changement mot de passe. */
  newMdp     = signal('');
  confirmMdp = signal('');

  saving      = signal(false);
  pwdError    = signal<string | null>(null);
  pwdSuccess  = signal<string | null>(null);

  /** Affiche un libellé lisible pour le rôle. */
  readonly roleLabel = computed(() => {
    const r = this.user()?.role ?? '';
    if (!r) return '—';
    const cleaned = r.startsWith('ROLE_') ? r.substring(5) : r;
    const map: Record<string, string> = {
      ADMIN:  'Administrateur',
      AGENT:  'Agent de crédit',
      COMITE: 'Comité de crédit',
    };
    return map[cleaned.toUpperCase()] ?? cleaned;
  });

  ngOnInit(): void {
    this.loadDetails();
  }

  private loadDetails(): void {
    const login = this.user()?.username;
    if (!login || !this.canEdit()) return;

    this.loading.set(true);
    this.admin.getUtilisateurs().subscribe({
      next: list => {
        this.loading.set(false);
        const me = list.find(u => u.login === login);
        if (me) this.details.set(me);
      },
      error: () => {
        this.loading.set(false);
        this.loadError.set("Impossible de charger vos informations détaillées.");
      },
    });
  }

  submitPassword(): void {
    this.pwdError.set(null);
    this.pwdSuccess.set(null);

    const a = this.newMdp().trim();
    const b = this.confirmMdp().trim();

    if (!a || !b) {
      this.pwdError.set('Veuillez remplir les deux champs.');
      return;
    }
    if (a.length < 8) {
      this.pwdError.set('Le mot de passe doit contenir au moins 8 caractères.');
      return;
    }
    if (a !== b) {
      this.pwdError.set('La confirmation ne correspond pas.');
      return;
    }

    const id = this.details()?.id;
    if (!id) {
      this.pwdError.set('Impossible de changer le mot de passe sans accès administrateur. Contactez votre administrateur.');
      return;
    }

    this.saving.set(true);
    this.admin.reinitialiserMdp(id, a).subscribe({
      next: () => {
        this.saving.set(false);
        this.pwdSuccess.set('Mot de passe mis à jour avec succès.');
        this.newMdp.set('');
        this.confirmMdp.set('');
      },
      error: e => {
        this.saving.set(false);
        this.pwdError.set('Erreur : ' + (e.error?.message ?? e.message ?? 'inconnue'));
      },
    });
  }
}
