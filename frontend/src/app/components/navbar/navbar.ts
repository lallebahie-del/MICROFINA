import {
  Component,
  ElementRef,
  HostListener,
  OnInit,
  computed,
  inject,
  signal,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { AdminService } from '../../services/admin.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css',
})
export class NavbarComponent implements OnInit {
  readonly authService = inject(AuthService);
  private readonly admin = inject(AdminService);
  private readonly host  = inject(ElementRef<HTMLElement>);

  showMenu = signal(false);

  /** Nom complet récupéré côté API (admin uniquement) ; sinon fallback sur le login. */
  private readonly nomComplet = signal<string | null>(null);

  readonly displayName = computed(
    () => this.nomComplet() ?? this.authService.currentUser()?.username ?? 'Utilisateur'
  );

  ngOnInit(): void {
    // Tentative best-effort : si l'utilisateur a PRIV_MANAGE_USERS, on récupère
    // son nom complet pour l'afficher dans le menu. Sinon on retombe sur le login.
    if (this.authService.hasAnyPrivilege('PRIV_MANAGE_USERS')) {
      const login = this.authService.currentUser()?.username;
      if (!login) return;
      this.admin.getUtilisateurs().subscribe({
        next: list => {
          const me = list.find(u => u.login === login);
          if (me?.nomComplet) this.nomComplet.set(me.nomComplet);
        },
        error: () => { /* silencieux : on garde le fallback login */ },
      });
    }
  }

  toggleMenu(e: MouseEvent): void {
    e.stopPropagation();
    this.showMenu.update(v => !v);
  }

  closeMenu(): void {
    this.showMenu.set(false);
  }

  logout(): void {
    this.closeMenu();
    this.authService.logout();
  }

  /** Ferme le menu si on clique en dehors du composant. */
  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    if (!this.showMenu()) return;
    const root = this.host.nativeElement as HTMLElement;
    if (!root.contains(event.target as Node)) this.closeMenu();
  }

  /** Ferme le menu si on appuie sur Escape. */
  @HostListener('document:keydown.escape')
  onEscape(): void {
    if (this.showMenu()) this.closeMenu();
  }
}
