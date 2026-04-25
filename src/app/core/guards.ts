import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { AuthService } from './auth.service';

export const authGuard: CanActivateFn = async () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (!auth.loaded()) {
    try { await firstValueFrom(auth.loadMe()); } catch { /* ignore */ }
  }
  if (auth.user()) return true;
  router.navigate(['/login']);
  return false;
};

export const roleGuard = (roles: string[]): CanActivateFn => async () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (!auth.loaded()) {
    try { await firstValueFrom(auth.loadMe()); } catch { /* ignore */ }
  }
  const u = auth.user();
  if (!u) { router.navigate(['/login']); return false; }
  if (!roles.includes(u.role)) { router.navigate(['/']); return false; }
  return true;
};
