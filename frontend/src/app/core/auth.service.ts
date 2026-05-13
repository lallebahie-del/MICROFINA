import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap, catchError, throwError, map } from 'rxjs';
import { environment } from '../../environments/environment';

export interface AuthUser {
  username: string;
  role: string;
  token: string; // Raw JWT issued by POST /api/auth/login
  privileges: string[];
}

interface LoginResponse {
  token: string;
  username: string;
  role: string;
  expiresInMs?: number;
}

/**
 * AuthService – manages JWT authentication for the Microfina++ SPA.
 *
 * Flow (matches AuthController):
 *   1. POST /api/auth/login { username, password }
 *      → { token, username, role, expiresInMs }
 *   2. Store the JWT in sessionStorage and attach it as
 *      `Authorization: Bearer <token>` on every subsequent API call.
 *
 * Credentials are held in sessionStorage so they survive F5 refresh
 * within the same browser tab but are discarded when the tab is closed.
 *
 * The service exposes:
 *  - currentUser  (signal)   – the logged-in user or null
 *  - isLoggedIn   (computed) – true when a user is authenticated
 *  - login()      – POSTs to /api/auth/login and stores the JWT
 *  - logout()     – clears state and navigates to /login
 *  - authHeader() – returns the Authorization header value for HTTP calls
 */
@Injectable({ providedIn: 'root' })
export class AuthService {

  private readonly STORAGE_KEY = 'microfina_auth';

  // Reactive state – components subscribe via currentUser() or isLoggedIn()
  readonly currentUser = signal<AuthUser | null>(this.loadFromSession());
  readonly isLoggedIn  = computed(() => this.currentUser() !== null);

  constructor(private http: HttpClient, private router: Router) {}

  /**
   * Attempts login by calling POST /api/auth/login with the credentials.
   * On success the JWT is stored and the user is redirected to /.
   * On failure a descriptive error observable is returned.
   */
  login(username: string, password: string): Observable<AuthUser> {
    return this.http.post<LoginResponse>(
      `${environment.apiUrl}/api/auth/login`,
      { username, password }
    ).pipe(
      map(response => {
        const user: AuthUser = {
          username:   response.username ?? username,
          role:       response.role     ?? 'UNKNOWN',
          token:      response.token,
          privileges: this.decodePrivileges(response.token)
        };
        return user;
      }),
      tap(user => {
        this.currentUser.set(user);
        sessionStorage.setItem(this.STORAGE_KEY, JSON.stringify(user));
        this.router.navigate(['/']);
      }),
      catchError(err => {
        const message = err.status === 401
          ? 'Identifiants incorrects. Vérifiez votre login et mot de passe.'
          : err.status === 403
            ? 'Accès refusé.'
            : 'Impossible de joindre le serveur. Vérifiez que le backend est démarré.';
        return throwError(() => new Error(message));
      })
    );
  }

  logout(): void {
    this.currentUser.set(null);
    sessionStorage.removeItem(this.STORAGE_KEY);
    this.router.navigate(['/login']);
  }

  /** Returns the Authorization header value to attach to outgoing HTTP calls. */
  authHeader(): string | null {
    const user = this.currentUser();
    return user ? `Bearer ${user.token}` : null;
  }

  /** @deprecated kept for backwards compatibility, returns the Bearer header. */
  basicHeader(): string | null {
    return this.authHeader();
  }

  /** Returns true if the current user has ALL of the given privileges. */
  hasPrivilege(...privs: string[]): boolean {
    const user = this.currentUser();
    if (!user) return false;
    return privs.every(p => user.privileges.includes(p));
  }

  /** Returns true if the current user has AT LEAST ONE of the given privileges. */
  hasAnyPrivilege(...privs: string[]): boolean {
    const user = this.currentUser();
    if (!user) return false;
    return privs.some(p => user.privileges.includes(p));
  }

  // ── Private helpers ─────────────────────────────────────────────────

  private loadFromSession(): AuthUser | null {
    try {
      const raw = sessionStorage.getItem(this.STORAGE_KEY);
      if (!raw) return null;
      const parsed: AuthUser = JSON.parse(raw);
      // Re-decode privileges from token in case the stored array is missing (migration)
      if (!parsed.privileges || parsed.privileges.length === 0) {
        parsed.privileges = this.decodePrivileges(parsed.token);
      }
      return parsed;
    } catch {
      return null;
    }
  }

  /** Decodes the `roles` claim from a JWT payload (no signature check needed client-side). */
  private decodePrivileges(token: string): string[] {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const roles: string = payload['roles'] ?? '';
      return roles.split(',').map(r => r.trim()).filter(r => r.length > 0);
    } catch {
      return [];
    }
  }
}
