import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap, catchError, throwError, map } from 'rxjs';
import { environment } from '../../environments/environment';

export interface AuthUser {
  username: string;
  role: string;
  token: string; // Raw JWT issued by POST /api/auth/login
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
          username: response.username ?? username,
          role:     response.role     ?? 'UNKNOWN',
          token:    response.token
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

  // ── Private helpers ─────────────────────────────────────────────────

  private loadFromSession(): AuthUser | null {
    try {
      const raw = sessionStorage.getItem(this.STORAGE_KEY);
      return raw ? JSON.parse(raw) : null;
    } catch {
      return null;
    }
  }
}
