import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from './auth.service';

/**
 * authInterceptor – functional HTTP interceptor (Angular 15+ style).
 *
 * Automatically attaches the JWT Bearer Authorization header to every
 * outgoing HTTP request whose URL starts with the configured API base.
 *
 * Requests to other origins (e.g. Google Fonts CDN) are passed through
 * unchanged so we don't accidentally expose credentials.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const header      = authService.authHeader();

  // Only attach credentials to calls going to our own API
  // Covers: local dev (8080), Docker-mapped port (8081), and relative /api paths
  const isApiCall = req.url.includes('localhost:8080')
                 || req.url.includes('localhost:8081')
                 || req.url.startsWith('/api');

  if (header && isApiCall) {
    const authenticatedReq = req.clone({
      setHeaders: { Authorization: header }
    });
    return next(authenticatedReq);
  }

  return next(req);
};
