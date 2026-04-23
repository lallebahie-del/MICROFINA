import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { LoginResponse, User } from '../models/auth.model';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8081/api/auth';
  currentUser = signal<User | null>(null);

  constructor(private http: HttpClient, private router: Router) {
    const savedUser = localStorage.getItem('user');
    if (savedUser) {
      this.currentUser.set(JSON.parse(savedUser));
    }
  }

  login(username: string, password: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/signin`, { username, password }).pipe(
      tap(response => {
        const user: User = {
          username: response.username,
          profile: response.profile,
          permissions: response.permissions,
          token: response.token
        };
        this.currentUser.set(user);
        localStorage.setItem('user', JSON.stringify(user));
        localStorage.setItem('token', response.token);
      })
    );
  }

  logout() {
    this.currentUser.set(null);
    localStorage.removeItem('user');
    localStorage.removeItem('token');
    this.router.navigate(['/login']);
  }

  isAuthenticated(): boolean {
    return !!this.currentUser();
  }

  hasPermission(permission: string): boolean {
    const user = this.currentUser();
    return user ? user.permissions.includes(permission) : false;
  }

  getRedirectUrl(): string {
    const user = this.currentUser();
    if (!user) return '/login';

    switch (user.profile) {
      case 'CHARGE_CLIENTELE': return '/charge-clientele';
      case 'CHARGE_CREDIT': return '/charge-credit';
      case 'CHEF_AGENCE': return '/chef-agence';
      case 'CAISSIER': return '/caissier';
      case 'COMPTABLE': return '/comptable';
      case 'RESP_EPARGNE': return '/resp-epargne';
      case 'RESP_RISQUES': return '/resp-risques';
      case 'AUDITEUR': return '/auditeur';
      case 'ADMIN_SYS': return '/admin-sys';
      case 'DIRIGEANT': return '/dirigeant';
      default: return '/home';
    }
  }
}
