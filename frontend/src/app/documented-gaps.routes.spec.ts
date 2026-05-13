import { Routes } from '@angular/router';
import { routes } from './app.routes';

/**
 * Contrat documenté : voir docs/FONCTIONNALITES-NON-IMPLEMENTEES.md §2.
 * Ces tests reflètent l’état actuel des manques UI. Quand une fonctionnalité
 * est livrée (route + navigation), mettre à jour ce fichier et le document.
 */
function collectRoutePaths(routeList: Routes, prefix = ''): string[] {
  const out: string[] = [];
  for (const r of routeList) {
    if (!r || typeof r !== 'object') continue;
    const rec = r as Record<string, unknown>;
    const path = typeof rec['path'] === 'string' ? (rec['path'] as string) : '';
    const full = path ? (prefix ? `${prefix}/${path}` : path) : prefix;
    if (full && full !== '**') {
      out.push(full.replace(/\/+/g, '/'));
    }
    const children = rec['children'];
    if (Array.isArray(children)) {
      out.push(...collectRoutePaths(children as Routes, full));
    }
  }
  return out;
}

describe('Documented UI gaps (FONCTIONNALITES-NON-IMPLEMENTEES)', () => {
  const paths = collectRoutePaths(routes);

  it('n’a pas encore de route dédiée « parts-sociales »', () => {
    expect(paths.some(p => p.includes('parts-sociales'))).toBe(false);
  });

  it('n’a pas encore de route admin pour les jobs planifiés', () => {
    expect(paths.some(p => p === 'admin/jobs' || p.endsWith('/admin/jobs'))).toBe(false);
  });

  it('n’a pas encore de route admin pour la clôture périodique', () => {
    expect(paths.some(p => p.includes('admin/cloture'))).toBe(false);
  });

  it('n’a pas encore de route admin pour les paramètres système', () => {
    expect(paths.some(p => p.includes('admin/parametres'))).toBe(false);
  });

  it('expose les routes budgets et comptabilité (consultation)', () => {
    expect(paths.some(p => p === 'budgets' || p.endsWith('/budgets'))).toBe(true);
    expect(paths.some(p => p.includes('comptabilite'))).toBe(true);
  });
});
