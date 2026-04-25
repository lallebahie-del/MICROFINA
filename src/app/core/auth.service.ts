import { Injectable, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { ApiService } from './api.service';

export interface SessionUser {
  id: string;
  username: string;
  fullName: string;
  email: string | null;
  role: string;
  branchId: string | null;
  branchName: string | null;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private api = inject(ApiService);

  user = signal<SessionUser | null>({
    id: '1',
    username: 'admin',
    fullName: 'Administrateur Système',
    email: 'admin@microfin.sn',
    role: 'admin',
    branchId: 'b1',
    branchName: 'Dakar Plateau'
  });
  loaded = signal(true);

  loadMe(): Observable<SessionUser> {
    return this.api.get<SessionUser>('/auth/me');
  }

  login(username: string, password: string): Observable<SessionUser> {
    return this.api.post<SessionUser>('/auth/login', { username, password }).pipe(
      tap((u) => { this.user.set(u); this.loaded.set(true); })
    );
  }

  logout(): Observable<{ ok: boolean }> {
    return this.api.post<{ ok: boolean }>('/auth/logout').pipe(
      tap(() => this.user.set(null))
    );
  }

  hasRole(roles: string[]): boolean {
    const u = this.user();
    return !!u && roles.includes(u.role);
  }
}
