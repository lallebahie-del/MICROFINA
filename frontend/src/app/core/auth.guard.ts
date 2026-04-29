import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

/**
 * authGuard – functional route guard (Angular 15+ style).
 *
 * Protects routes that require an authenticated user.
 * Unauthenticated visitors are redirected to /login.
 *
 * Usage in app.routes.ts:
 *   { path: '', component: LayoutComponent, canActivate: [authGuard], ... }
 */
export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router      = inject(Router);

  if (authService.isLoggedIn()) {
    return true;
  }

  // Redirect to login and block navigation
  return router.createUrlTree(['/login']);
};
